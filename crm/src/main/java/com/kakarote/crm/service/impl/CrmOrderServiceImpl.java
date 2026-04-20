package com.kakarote.crm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kakarote.core.common.FieldEnum;
import com.kakarote.core.entity.BasePage;
import com.kakarote.core.exception.CrmException;
import com.kakarote.core.feign.admin.entity.AdminConfig;
import com.kakarote.core.feign.admin.entity.SimpleUser;
import com.kakarote.core.feign.admin.service.AdminFileService;
import com.kakarote.core.feign.admin.service.AdminService;
import com.kakarote.core.feign.crm.entity.SimpleCrmEntity;
import com.kakarote.core.field.FieldService;
import com.kakarote.core.servlet.ApplicationContextHolder;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.core.servlet.upload.FileEntity;
import com.kakarote.core.utils.ExcelParseUtil;
import com.kakarote.core.utils.UserCacheUtil;
import com.kakarote.core.utils.UserUtil;
import com.kakarote.crm.common.ActionRecordUtil;
import com.kakarote.crm.common.CrmModel;
import com.kakarote.crm.constant.CrmCodeEnum;
import com.kakarote.crm.constant.CrmEnum;
import com.kakarote.crm.entity.BO.CrmOrderRelationPageBO;
import com.kakarote.crm.entity.BO.CrmOrderSaveBO;
import com.kakarote.crm.entity.BO.CrmSearchBO;
import com.kakarote.crm.entity.PO.CrmField;
import com.kakarote.crm.entity.PO.CrmOrder;
import com.kakarote.crm.entity.PO.CrmOrderData;
import com.kakarote.crm.entity.PO.CrmOrderProduct;
import com.kakarote.crm.entity.PO.CrmProduct;
import com.kakarote.crm.entity.VO.CrmFieldSortVO;
import com.kakarote.crm.entity.VO.CrmModelFiledVO;
import com.kakarote.crm.mapper.CrmOrderMapper;
import com.kakarote.crm.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CrmOrderServiceImpl extends BaseServiceImpl<CrmOrderMapper, CrmOrder> implements ICrmOrderService, CrmPageService {

    @Autowired
    private ICrmFieldService crmFieldService;
    @Autowired
    private ICrmOrderDataService crmOrderDataService;
    @Autowired
    private ICrmOrderRelationService crmOrderRelationService;
    @Autowired
    private ICrmOrderProductService crmOrderProductService;
    @Autowired
    private ICrmNumberSettingService crmNumberSettingService;
    @Autowired
    private ICrmProductService crmProductService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private ActionRecordUtil actionRecordUtil;
    @Autowired
    private AdminFileService adminFileService;
    @Autowired
    private FieldService fieldService;

    @Override
    public BasePage<Map<String, Object>> queryPageList(CrmSearchBO search) {
        return queryList(search, false);
    }

    @Override
    public BasePage<Map<String, Object>> queryPageListByRelation(CrmOrderRelationPageBO search) {
        List<Integer> orderIds = crmOrderRelationService.queryOrderIds(search.getRelationType(), search.getRelationId());
        BasePage<Map<String, Object>> emptyPage = new BasePage<>();
        emptyPage.setCurrent(search.getPage());
        emptyPage.setSize(search.getLimit());
        emptyPage.setTotal(0);
        emptyPage.setList(new ArrayList<>());
        if (orderIds.isEmpty()) {
            return emptyPage;
        }
        CrmSearchBO relationSearch = new CrmSearchBO();
        relationSearch.setPage(search.getPage());
        relationSearch.setLimit(search.getLimit());
        relationSearch.setSearch(search.getSearch());
        relationSearch.setSortField(search.getSortField());
        relationSearch.setOrder(search.getOrder());
        relationSearch.setPageType(1);
        relationSearch.getSearchList().addAll(search.getSearchList());
        relationSearch.getSearchList().add(new CrmSearchBO.Search(null, null, CrmSearchBO.FieldSearchEnum.ID,
                orderIds.stream().map(String::valueOf).collect(Collectors.toList())));
        return queryPageList(relationSearch);
    }

    @Override
    public List<SimpleCrmEntity> querySimpleEntity(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return lambdaQuery()
                .select(CrmOrder::getOrderId, CrmOrder::getOrderNumber, CrmOrder::getTitle)
                .in(CrmOrder::getOrderId, ids)
                .list()
                .stream()
                .map(order -> {
                    SimpleCrmEntity entity = new SimpleCrmEntity();
                    entity.setId(order.getOrderId());
                    entity.setName(StrUtil.blankToDefault(order.getOrderNumber(), order.getTitle()));
                    return entity;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdate(CrmOrderSaveBO crmModel) {
        if (crmModel.getEntity() == null) {
            crmModel.setEntity(new HashMap<>());
        }
        CrmOrder crmOrder = BeanUtil.copyProperties(crmModel.getEntity(), CrmOrder.class);
        CrmOrder oldOrder = ObjectUtil.isNotEmpty(crmOrder.getOrderId()) ? getById(crmOrder.getOrderId()) : null;
        String batchId = StrUtil.isNotEmpty(crmOrder.getBatchId()) ? crmOrder.getBatchId()
                : oldOrder != null && StrUtil.isNotEmpty(oldOrder.getBatchId()) ? oldOrder.getBatchId() : IdUtil.simpleUUID();
        List<CrmOrderProduct> orderProducts = prepareOrderProducts(crmModel.getProductList());
        BigDecimal manualQuoteAmount = getEntityBigDecimal(crmModel.getEntity(), "totalPrice");
        mergeProductRelation(crmModel.getEntity(), orderProducts);
        applyAmountSummary(crmOrder, oldOrder, orderProducts, crmModel.isForceExchangeConversion(), manualQuoteAmount);
        crmOrder.setBatchId(batchId);
        actionRecordUtil.updateRecord(crmModel.getField(), Dict.create().set("batchId", batchId).set("dataTableName", "wk_crm_order_data"));
        crmOrderDataService.saveData(crmModel.getField(), batchId);
        if (ObjectUtil.isNotEmpty(crmOrder.getOrderId())) {
            Integer count = lambdaQuery().eq(CrmOrder::getOrderNumber, crmOrder.getOrderNumber()).ne(CrmOrder::getOrderId, crmOrder.getOrderId()).count();
            if (count != 0) {
                throw new CrmException(CrmCodeEnum.CRM_ORDER_NUM_ERROR);
            }
            actionRecordUtil.updateRecord(BeanUtil.beanToMap(getById(crmOrder.getOrderId())), BeanUtil.beanToMap(crmOrder), CrmEnum.ORDER, getOrderName(crmOrder.getOrderId()), crmOrder.getOrderId());
            crmOrder.setUpdateTime(DateUtil.date());
            updateById(crmOrder);
        } else {
            List<AdminConfig> configList = adminService.queryConfigByName("numberSetting").getData();
            configList.stream()
                    .filter(config -> Objects.equals(getLabel().getType().toString(), config.getValue()))
                    .findFirst()
                    .ifPresent(config -> {
                        if (config.getStatus() == 1 && StrUtil.isEmpty(crmOrder.getOrderNumber())) {
                            crmOrder.setOrderNumber(crmNumberSettingService.generateNumber(config, new Date()));
                        }
                    });
            Integer count = lambdaQuery().eq(CrmOrder::getOrderNumber, crmOrder.getOrderNumber()).count();
            if (count != 0) {
                throw new CrmException(CrmCodeEnum.CRM_ORDER_NUM_ERROR);
            }
            crmOrder.setUpdateTime(new Date());
            if (crmOrder.getOwnerUserId() == null) {
                crmOrder.setOwnerUserId(UserUtil.getUserId());
            }
            save(crmOrder);
            actionRecordUtil.addRecord(crmOrder.getOrderId(), CrmEnum.ORDER, getOrderName(crmOrder.getOrderId()));
        }
        orderProducts.forEach(product -> product.setOrderId(crmOrder.getOrderId()));
        crmOrderProductService.saveByOrderId(crmOrder.getOrderId(), orderProducts);
        crmOrderRelationService.saveRelationData(crmOrder.getOrderId(), crmModel.getEntity());
        crmModel.setEntity(BeanUtil.beanToMap(getById(crmOrder.getOrderId())));
        savePage(crmModel, crmOrder.getOrderId(), false);
    }

    @Override
    public CrmModel queryById(Integer orderId) {
        if (orderId == null) {
            return emptyOrderModel();
        }
        CrmOrder crmOrder = getById(orderId);
        if (crmOrder == null) {
            return emptyOrderModel();
        }
        CrmModel crmModel = new CrmModel(CrmEnum.ORDER.getType());
        crmModel.putAll(BeanUtil.beanToMap(crmOrder));
        crmModel.setOwnerUserName(UserCacheUtil.getUserName(crmModel.getOwnerUserId()));
        crmOrderDataService.setDataByBatchId(crmModel);
        List<CrmOrderProduct> orderProducts = loadOrderProducts(orderId);
        crmModel.put("product", orderProducts);
        crmModel.put("quotationList", buildQuotationList(orderProducts));
        crmModel.put("profitList", buildProfitList(orderProducts));
        Map<CrmEnum, List<SimpleCrmEntity>> relationMap = crmOrderRelationService.queryRelationMap(orderId);
        crmModel.put("leadsIds", toRelationList(relationMap.get(CrmEnum.LEADS)));
        crmModel.put("customerIds", toRelationList(relationMap.get(CrmEnum.CUSTOMER)));
        crmModel.put("contactsIds", toRelationList(relationMap.get(CrmEnum.CONTACTS)));
        crmModel.put("businessIds", toRelationList(relationMap.get(CrmEnum.BUSINESS)));
        crmModel.put("contractIds", toRelationList(relationMap.get(CrmEnum.CONTRACT)));
        crmModel.put("receivablesIds", toRelationList(relationMap.get(CrmEnum.RECEIVABLES)));
        crmModel.put("invoiceIds", toRelationList(relationMap.get(CrmEnum.INVOICE)));
        crmModel.put("returnVisitIds", toRelationList(relationMap.get(CrmEnum.RETURN_VISIT)));
        crmModel.put("productIds", toRelationList(relationMap.get(CrmEnum.PRODUCT)));
        crmModel.putAll(crmOrderRelationService.queryRelationNameMap(orderId));
        List<String> noAuthFields = ApplicationContextHolder.getBean(ICrmRoleFieldService.class).queryNoAuthField(crmModel.getLabel());
        noAuthFields.forEach(crmModel::remove);
        return crmModel;
    }

    @Override
    public List<CrmModelFiledVO> queryField(Integer id) {
        CrmModel crmModel = queryById(id);
        List<CrmModelFiledVO> fieldList = crmFieldService.queryField(crmModel);
        if (id == null) {
            fieldList.forEach(field -> {
                if ("ownerUserId".equals(field.getFieldName())) {
                    SimpleUser user = new SimpleUser();
                    user.setUserId(UserUtil.getUserId());
                    user.setRealname(UserUtil.getUser().getRealname());
                    field.setDefaultValue(Collections.singleton(user));
                }
            });
        }
        return fieldList;
    }

    @Override
    public List<List<CrmModelFiledVO>> queryFormPositionField(Integer id) {
        CrmModel crmModel = queryById(id);
        List<List<CrmModelFiledVO>> fieldList = crmFieldService.queryFormPositionFieldVO(crmModel);
        if (id == null) {
            for (List<CrmModelFiledVO> filedVOList : fieldList) {
                filedVOList.forEach(field -> {
                    if ("ownerUserId".equals(field.getFieldName())) {
                        SimpleUser user = new SimpleUser();
                        user.setUserId(UserUtil.getUserId());
                        user.setRealname(UserUtil.getUser().getRealname());
                        field.setDefaultValue(Collections.singleton(user));
                    }
                });
            }
        }
        return fieldList;
    }

    @Override
    public List<CrmModelFiledVO> information(Integer orderId) {
        CrmModel crmModel = queryById(orderId);
        List<CrmModelFiledVO> fieldList = crmFieldService.queryField(crmModel);
        fieldList.addAll(appendInformation(crmModel));
        fieldList.add(new CrmModelFiledVO("leadsNames", FieldEnum.TEXTAREA, "关联线索", 1).setValue(crmModel.get("leadsNames")));
        fieldList.add(new CrmModelFiledVO("customerNames", FieldEnum.TEXTAREA, "关联客户", 1).setValue(crmModel.get("customerNames")));
        fieldList.add(new CrmModelFiledVO("contactsNames", FieldEnum.TEXTAREA, "关联联系人", 1).setValue(crmModel.get("contactsNames")));
        fieldList.add(new CrmModelFiledVO("businessNames", FieldEnum.TEXTAREA, "关联商机", 1).setValue(crmModel.get("businessNames")));
        fieldList.add(new CrmModelFiledVO("contractNames", FieldEnum.TEXTAREA, "关联合同", 1).setValue(crmModel.get("contractNames")));
        fieldList.add(new CrmModelFiledVO("receivablesNames", FieldEnum.TEXTAREA, "关联回款", 1).setValue(crmModel.get("receivablesNames")));
        fieldList.add(new CrmModelFiledVO("invoiceNames", FieldEnum.TEXTAREA, "关联发票", 1).setValue(crmModel.get("invoiceNames")));
        fieldList.add(new CrmModelFiledVO("returnVisitNames", FieldEnum.TEXTAREA, "关联回访", 1).setValue(crmModel.get("returnVisitNames")));
        fieldList.add(new CrmModelFiledVO("productNames", FieldEnum.TEXTAREA, "关联产品", 1).setValue(crmModel.get("productNames")));
        return fieldList;
    }

    @Override
    public List<FileEntity> queryFileList(Integer id) {
        List<FileEntity> fileEntityList = new ArrayList<>();
        CrmOrder crmOrder = getById(id);
        if (crmOrder == null) {
            return fileEntityList;
        }
        adminFileService.queryFileList(crmOrder.getBatchId()).getData().forEach(fileEntity -> {
            fileEntity.setSource("附件上传");
            fileEntity.setReadOnly(0);
            fileEntityList.add(fileEntity);
        });
        List<CrmField> crmFields = crmFieldService.queryFileField();
        if (!crmFields.isEmpty()) {
            LambdaQueryWrapper<CrmOrderData> wrapper = new LambdaQueryWrapper<>();
            wrapper.select(CrmOrderData::getValue);
            wrapper.eq(CrmOrderData::getBatchId, crmOrder.getBatchId());
            wrapper.in(CrmOrderData::getFieldId, crmFields.stream().map(CrmField::getFieldId).collect(Collectors.toList()));
            List<FileEntity> data = adminFileService.queryFileList(crmOrderDataService.listObjs(wrapper, Object::toString)).getData();
            data.forEach(fileEntity -> {
                fileEntity.setSource("订单详情");
                fileEntity.setReadOnly(1);
                fileEntityList.add(fileEntity);
            });
        }
        return fileEntityList;
    }

    @Override
    public List<CrmOrderProduct> queryProductList(Integer orderId) {
        return loadOrderProducts(orderId);
    }

    @Override
    public List<JSONObject> queryQuotationList(Integer orderId) {
        return buildQuotationList(loadOrderProducts(orderId));
    }

    @Override
    public List<JSONObject> queryProfitList(Integer orderId) {
        return buildProfitList(loadOrderProducts(orderId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<Integer> ids) {
        List<String> batchList = lambdaQuery().select(CrmOrder::getBatchId).in(CrmOrder::getOrderId, ids).list()
                .stream().map(CrmOrder::getBatchId).filter(StrUtil::isNotEmpty).collect(Collectors.toList());
        crmOrderDataService.deleteByBatchId(batchList);
        crmOrderProductService.deleteByOrderId(ids.toArray(new Integer[0]));
        crmOrderRelationService.remove(new LambdaQueryWrapper<com.kakarote.crm.entity.PO.CrmOrderRelation>().in(com.kakarote.crm.entity.PO.CrmOrderRelation::getOrderId, ids));
        removeByIds(ids);
        deletePage(ids);
    }

    @Override
    public void downloadExcel(HttpServletResponse response) throws IOException {
        List<CrmModelFiledVO> fieldList = queryField(null);
        removeFieldByType(fieldList);
        fieldList.removeIf(field -> Arrays.asList("product", "ownerUserId", "owner_user_id", "profitAmount", "profit_amount", "profitRate", "profit_rate")
                .contains(field.getFieldName()));
        int ownerIndex = 0;
        for (int i = 0; i < fieldList.size(); i++) {
            if ("title".equals(fieldList.get(i).getFieldName())) {
                ownerIndex = i + 1;
                break;
            }
        }
        fieldList.add(ownerIndex, new CrmModelFiledVO("ownerUserName", FieldEnum.TEXT, "负责人", 1).setIsNull(1));
        ExcelParseUtil.importExcel(new ExcelParseUtil.ExcelParseService() {
            @Override
            public void castData(Map<String, Object> record, Map<String, Integer> headMap) {
            }

            @Override
            public String getExcelName() {
                return "订单";
            }

            @Override
            public String getMergeContent(String module) {
                return super.getMergeContent(module) + "\n7、订单导入模板不包含产品明细，报价/成本按表头字段导入";
            }
        }, fieldList, response, "crm");
    }

    @Override
    public void exportExcel(HttpServletResponse response, CrmSearchBO search) {
        List<Map<String, Object>> dataList = queryList(search, true).getList();
        List<CrmFieldSortVO> headList = crmFieldService.queryListHead(getLabel().getType());
        ExcelParseUtil.exportExcel(dataList, new ExcelParseUtil.ExcelParseService() {
            @Override
            public void castData(Map<String, Object> record, Map<String, Integer> headMap) {
                for (String fieldName : headMap.keySet()) {
                    record.put(fieldName, ActionRecordUtil.parseValue(record.get(fieldName), headMap.get(fieldName), false));
                }
            }

            @Override
            public String getExcelName() {
                return "订单";
            }
        }, headList, response);
    }

    @Override
    public String getOrderName(Integer orderId) {
        CrmOrder order = getById(orderId);
        if (order == null) {
            return "";
        }
        return StrUtil.blankToDefault(order.getOrderNumber(), order.getTitle());
    }

    @Override
    public void setOtherField(Map<String, Object> map) {
        Object orderId = map.get("orderId");
        if (orderId != null) {
            map.putAll(crmOrderRelationService.queryRelationNameMap(Integer.valueOf(orderId.toString())));
        }
        Object ownerUserId = map.get("ownerUserId");
        Object createUserId = map.get("createUserId");
        map.put("ownerUserName", ownerUserId == null ? "" : UserCacheUtil.getUserName(Long.valueOf(ownerUserId.toString())));
        map.put("createUserName", createUserId == null ? "" : UserCacheUtil.getUserName(Long.valueOf(createUserId.toString())));
    }

    @Override
    public Dict getSearchTransferMap() {
        return Dict.create();
    }

    @Override
    public String[] appendSearch() {
        return new String[]{"orderNumber", "title", "customerNames", "contractNames", "productNames"};
    }

    @Override
    public CrmEnum getLabel() {
        return CrmEnum.ORDER;
    }

    @Override
    public List<CrmModelFiledVO> queryDefaultField() {
        List<CrmModelFiledVO> fieldList = crmFieldService.queryField(getLabel().getType());
        fieldList.add(new CrmModelFiledVO("updateTime", FieldEnum.DATETIME, 1));
        fieldList.add(new CrmModelFiledVO("createTime", FieldEnum.DATETIME, 1));
        fieldList.add(new CrmModelFiledVO("ownerUserId", FieldEnum.USER, 1));
        fieldList.add(new CrmModelFiledVO("createUserId", FieldEnum.USER, 1));
        fieldList.add(new CrmModelFiledVO("leadsNames", FieldEnum.TEXT, 1));
        fieldList.add(new CrmModelFiledVO("customerNames", FieldEnum.TEXT, 1));
        fieldList.add(new CrmModelFiledVO("contactsNames", FieldEnum.TEXT, 1));
        fieldList.add(new CrmModelFiledVO("businessNames", FieldEnum.TEXT, 1));
        fieldList.add(new CrmModelFiledVO("contractNames", FieldEnum.TEXT, 1));
        fieldList.add(new CrmModelFiledVO("receivablesNames", FieldEnum.TEXT, 1));
        fieldList.add(new CrmModelFiledVO("invoiceNames", FieldEnum.TEXT, 1));
        fieldList.add(new CrmModelFiledVO("returnVisitNames", FieldEnum.TEXT, 1));
        fieldList.add(new CrmModelFiledVO("productNames", FieldEnum.TEXT, 1));
        return fieldList;
    }

    private List<JSONObject> toRelationList(List<SimpleCrmEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(entity -> new JSONObject().fluentPut("id", entity.getId()).fluentPut("name", entity.getName()))
                .collect(Collectors.toList());
    }

    private CrmModel emptyOrderModel() {
        CrmModel crmModel = new CrmModel(CrmEnum.ORDER.getType());
        crmModel.put("leadsIds", new ArrayList<>());
        crmModel.put("customerIds", new ArrayList<>());
        crmModel.put("contactsIds", new ArrayList<>());
        crmModel.put("businessIds", new ArrayList<>());
        crmModel.put("contractIds", new ArrayList<>());
        crmModel.put("receivablesIds", new ArrayList<>());
        crmModel.put("invoiceIds", new ArrayList<>());
        crmModel.put("returnVisitIds", new ArrayList<>());
        crmModel.put("productIds", new ArrayList<>());
        crmModel.put("product", new ArrayList<>());
        crmModel.put("quotationList", new ArrayList<>());
        crmModel.put("profitList", new ArrayList<>());
        return crmModel;
    }

    private void mergeProductRelation(Map<String, Object> entity, List<CrmOrderProduct> orderProducts) {
        LinkedHashSet<Integer> productIds = new LinkedHashSet<>();
        Object relationValue = entity.get("productIds");
        if (relationValue instanceof Collection) {
            for (Object item : (Collection<?>) relationValue) {
                if (item instanceof Map) {
                    Object id = ((Map<?, ?>) item).get("id");
                    if (id != null) {
                        productIds.add(Integer.valueOf(id.toString()));
                    }
                } else if (item != null) {
                    productIds.add(Integer.valueOf(item.toString()));
                }
            }
        }
        orderProducts.stream().map(CrmOrderProduct::getProductId).filter(Objects::nonNull).forEach(productIds::add);
        entity.put("productIds", productIds.stream()
                .map(id -> new JSONObject().fluentPut("id", id))
                .collect(Collectors.toList()));
    }

    private List<CrmOrderProduct> prepareOrderProducts(List<CrmOrderProduct> sourceList) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> productIds = sourceList.stream().map(CrmOrderProduct::getProductId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Integer, CrmProduct> productMap = productIds.isEmpty() ? Collections.emptyMap() : crmProductService.lambdaQuery()
                .in(CrmProduct::getProductId, productIds)
                .list()
                .stream()
                .collect(Collectors.toMap(CrmProduct::getProductId, product -> product, (a, b) -> a));
        List<CrmOrderProduct> productList = new ArrayList<>();
        int sort = 0;
        for (CrmOrderProduct source : sourceList) {
            if (source == null) {
                continue;
            }
            CrmOrderProduct product = BeanUtil.copyProperties(source, CrmOrderProduct.class);
            CrmProduct crmProduct = product.getProductId() == null ? null : productMap.get(product.getProductId());
            if (crmProduct != null) {
                if (StrUtil.isEmpty(product.getProductName())) {
                    product.setProductName(crmProduct.getName());
                }
                if (StrUtil.isEmpty(product.getUnit())) {
                    product.setUnit(crmProduct.getUnit());
                }
                if (product.getPrice() == null) {
                    product.setPrice(crmProduct.getPrice());
                }
            }
            if (product.getDiscount() == null) {
                product.setDiscount(new BigDecimal("100.00"));
            } else {
                product.setDiscount(scale(product.getDiscount()));
            }
            BigDecimal subtotal = calculateSubtotal(product.getSalesPrice(), product.getNum(), product.getDiscount(), product.getSubtotal());
            BigDecimal purchaseCost = calculatePurchaseCost(product.getPurchasePrice(), product.getNum(), product.getPurchaseCost());
            BigDecimal logisticsCost = scale(product.getLogisticsCost());
            product.setPrice(scale(product.getPrice()));
            product.setSalesPrice(scale(product.getSalesPrice()));
            product.setNum(scale(product.getNum()));
            product.setPurchasePrice(scale(product.getPurchasePrice()));
            product.setSubtotal(subtotal);
            product.setPurchaseCost(purchaseCost);
            product.setLogisticsCost(logisticsCost);
            BigDecimal totalCost = purchaseCost.add(logisticsCost);
            product.setProfitAmount(scale(subtotal.subtract(totalCost)));
            product.setProfitRate(calculateRate(product.getProfitAmount(), subtotal));
            product.setTotalCost(totalCost);
            product.setSort(product.getSort() == null ? sort : product.getSort());
            productList.add(product);
            sort++;
        }
        return productList;
    }

    private void applyAmountSummary(CrmOrder crmOrder, CrmOrder oldOrder, List<CrmOrderProduct> orderProducts,
                                    boolean forceExchangeConversion, BigDecimal manualQuoteAmount) {
        BigDecimal exchangeRate = scaleExchangeRate(crmOrder.getExchangeRate());
        BigDecimal quoteAmount = BigDecimal.ZERO;
        BigDecimal purchaseCost = BigDecimal.ZERO;
        BigDecimal logisticsCost = BigDecimal.ZERO;
        BigDecimal handlingFeeCost = scale(crmOrder.getHandlingFeeCost());
        BigDecimal consumableCost = scale(crmOrder.getConsumableCost());
        BigDecimal otherCost = scale(crmOrder.getOtherCost());
        BigDecimal finalQuoteAmount;
        BigDecimal finalPurchaseCost;
        BigDecimal finalLogisticsCost;
        BigDecimal finalHandlingFeeCost;
        BigDecimal finalConsumableCost;
        BigDecimal finalOtherCost;
        if (!orderProducts.isEmpty()) {
            for (CrmOrderProduct product : orderProducts) {
                quoteAmount = quoteAmount.add(scale(product.getSubtotal()));
                purchaseCost = purchaseCost.add(scale(product.getPurchaseCost()));
                logisticsCost = logisticsCost.add(scale(product.getLogisticsCost()));
            }
            BigDecimal effectiveQuoteAmount = manualQuoteAmount == null ? quoteAmount : scale(manualQuoteAmount);
            // 订单明细和附加成本统一按订单汇率折算后，再计算订单利润。
            finalQuoteAmount = scale(effectiveQuoteAmount.multiply(exchangeRate));
            finalPurchaseCost = scale(purchaseCost.multiply(exchangeRate));
            finalLogisticsCost = scale(logisticsCost.multiply(exchangeRate));
            finalHandlingFeeCost = scale(handlingFeeCost.multiply(exchangeRate));
            finalConsumableCost = scale(consumableCost.multiply(exchangeRate));
            finalOtherCost = scale(otherCost.multiply(exchangeRate));
        } else if (oldOrder == null || forceExchangeConversion) {
            // 无明细的新建订单或导入覆盖场景按录入汇率折算订单级金额。
            finalQuoteAmount = scale(scale(crmOrder.getQuoteAmount()).multiply(exchangeRate));
            finalPurchaseCost = scale(scale(crmOrder.getPurchaseCost()).multiply(exchangeRate));
            finalLogisticsCost = scale(scale(crmOrder.getLogisticsCost()).multiply(exchangeRate));
            finalHandlingFeeCost = scale(handlingFeeCost.multiply(exchangeRate));
            finalConsumableCost = scale(consumableCost.multiply(exchangeRate));
            finalOtherCost = scale(otherCost.multiply(exchangeRate));
        } else {
            // 无明细的历史订单编辑场景按当前订单金额直接保存，避免重复折算。
            finalQuoteAmount = scale(crmOrder.getQuoteAmount());
            finalPurchaseCost = scale(crmOrder.getPurchaseCost());
            finalLogisticsCost = scale(crmOrder.getLogisticsCost());
            finalHandlingFeeCost = handlingFeeCost;
            finalConsumableCost = consumableCost;
            finalOtherCost = otherCost;
        }
        BigDecimal totalCost = finalPurchaseCost
                .add(finalLogisticsCost)
                .add(finalHandlingFeeCost)
                .add(finalConsumableCost)
                .add(finalOtherCost);
        crmOrder.setExchangeRate(exchangeRate);
        crmOrder.setQuoteAmount(finalQuoteAmount);
        crmOrder.setPurchaseCost(finalPurchaseCost);
        crmOrder.setLogisticsCost(finalLogisticsCost);
        crmOrder.setHandlingFeeCost(finalHandlingFeeCost);
        crmOrder.setConsumableCost(finalConsumableCost);
        crmOrder.setOtherCost(finalOtherCost);
        crmOrder.setProfitAmount(scale(finalQuoteAmount.subtract(totalCost)));
        crmOrder.setProfitRate(calculateRate(crmOrder.getProfitAmount(), crmOrder.getQuoteAmount()));
    }

    private List<CrmOrderProduct> loadOrderProducts(Integer orderId) {
        List<CrmOrderProduct> orderProducts = crmOrderProductService.queryByOrderId(orderId);
        orderProducts.forEach(product -> {
            if (StrUtil.isEmpty(product.getName())) {
                product.setName(product.getProductName());
            }
            product.setTotalCost(scale(product.getPurchaseCost()).add(scale(product.getLogisticsCost())));
        });
        return orderProducts;
    }

    private List<JSONObject> buildQuotationList(List<CrmOrderProduct> orderProducts) {
        return orderProducts.stream().map(product -> new JSONObject()
                .fluentPut("rId", product.getRId())
                .fluentPut("orderId", product.getOrderId())
                .fluentPut("productId", product.getProductId())
                .fluentPut("productName", StrUtil.blankToDefault(product.getName(), product.getProductName()))
                .fluentPut("name", StrUtil.blankToDefault(product.getName(), product.getProductName()))
                .fluentPut("categoryName", product.getCategoryName())
                .fluentPut("unit", product.getUnit())
                .fluentPut("price", product.getPrice())
                .fluentPut("salesPrice", product.getSalesPrice())
                .fluentPut("num", product.getNum())
                .fluentPut("discount", product.getDiscount())
                .fluentPut("subtotal", product.getSubtotal())
                .fluentPut("remark", product.getRemark()))
                .collect(Collectors.toList());
    }

    private List<JSONObject> buildProfitList(List<CrmOrderProduct> orderProducts) {
        return orderProducts.stream().map(product -> new JSONObject()
                .fluentPut("rId", product.getRId())
                .fluentPut("orderId", product.getOrderId())
                .fluentPut("productId", product.getProductId())
                .fluentPut("productName", StrUtil.blankToDefault(product.getName(), product.getProductName()))
                .fluentPut("name", StrUtil.blankToDefault(product.getName(), product.getProductName()))
                .fluentPut("categoryName", product.getCategoryName())
                .fluentPut("unit", product.getUnit())
                .fluentPut("price", product.getPrice())
                .fluentPut("salesPrice", product.getSalesPrice())
                .fluentPut("num", product.getNum())
                .fluentPut("discount", product.getDiscount())
                .fluentPut("subtotal", product.getSubtotal())
                .fluentPut("purchasePrice", product.getPurchasePrice())
                .fluentPut("purchaseCost", product.getPurchaseCost())
                .fluentPut("logisticsCost", product.getLogisticsCost())
                .fluentPut("totalCost", product.getTotalCost())
                .fluentPut("profitAmount", product.getProfitAmount())
                .fluentPut("profitRate", product.getProfitRate())
                .fluentPut("remark", product.getRemark()))
                .collect(Collectors.toList());
    }

    private BigDecimal calculateSubtotal(BigDecimal salesPrice, BigDecimal num, BigDecimal discount, BigDecimal subtotal) {
        if (subtotal != null) {
            return scale(subtotal);
        }
        if (salesPrice != null && num != null) {
            return salesPrice
                    .multiply(num)
                    .multiply(scale(discount))
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
        return scale(subtotal);
    }

    private BigDecimal calculatePurchaseCost(BigDecimal purchasePrice, BigDecimal num, BigDecimal purchaseCost) {
        if (purchasePrice != null && num != null) {
            return purchasePrice.multiply(num).setScale(2, RoundingMode.HALF_UP);
        }
        return scale(purchaseCost);
    }

    private BigDecimal calculateRate(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || BigDecimal.ZERO.compareTo(denominator) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return scale(numerator).multiply(new BigDecimal("100")).divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleExchangeRate(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ONE.setScale(6, RoundingMode.HALF_UP);
        }
        return value.setScale(6, RoundingMode.HALF_UP);
    }

    private BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getEntityBigDecimal(Map<String, Object> entity, String field) {
        if (entity == null || StrUtil.isBlank(field)) {
            return null;
        }
        Object value = entity.get(field);
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        String text = value.toString();
        if (StrUtil.isBlank(text)) {
            return null;
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
