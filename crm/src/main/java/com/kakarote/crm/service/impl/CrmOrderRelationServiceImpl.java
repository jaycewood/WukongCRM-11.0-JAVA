package com.kakarote.crm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kakarote.core.common.Const;
import com.kakarote.core.feign.crm.entity.SimpleCrmEntity;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.crm.constant.CrmEnum;
import com.kakarote.crm.entity.PO.CrmOrderRelation;
import com.kakarote.crm.mapper.CrmOrderRelationMapper;
import com.kakarote.crm.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CrmOrderRelationServiceImpl extends BaseServiceImpl<CrmOrderRelationMapper, CrmOrderRelation> implements ICrmOrderRelationService {

    @Autowired
    private ICrmLeadsService crmLeadsService;
    @Autowired
    private ICrmCustomerService crmCustomerService;
    @Autowired
    private ICrmContactsService crmContactsService;
    @Autowired
    private ICrmBusinessService crmBusinessService;
    @Autowired
    private ICrmContractService crmContractService;
    @Autowired
    private ICrmReceivablesService crmReceivablesService;
    @Autowired
    private ICrmInvoiceService crmInvoiceService;
    @Autowired
    private ICrmReturnVisitService crmReturnVisitService;
    @Autowired
    private ICrmProductService crmProductService;

    @Override
    public void saveRelationData(Integer orderId, Map<String, Object> entity) {
        remove(new LambdaQueryWrapper<CrmOrderRelation>().eq(CrmOrderRelation::getOrderId, orderId));
        List<CrmOrderRelation> relationList = new ArrayList<>();
        saveByType(relationList, orderId, CrmEnum.LEADS, entity.get("leadsIds"));
        saveByType(relationList, orderId, CrmEnum.CUSTOMER, entity.get("customerIds"));
        saveByType(relationList, orderId, CrmEnum.CONTACTS, entity.get("contactsIds"));
        saveByType(relationList, orderId, CrmEnum.BUSINESS, entity.get("businessIds"));
        saveByType(relationList, orderId, CrmEnum.CONTRACT, entity.get("contractIds"));
        saveByType(relationList, orderId, CrmEnum.RECEIVABLES, entity.get("receivablesIds"));
        saveByType(relationList, orderId, CrmEnum.INVOICE, entity.get("invoiceIds"));
        saveByType(relationList, orderId, CrmEnum.RETURN_VISIT, entity.get("returnVisitIds"));
        saveByType(relationList, orderId, CrmEnum.PRODUCT, entity.get("productIds"));
        if (!relationList.isEmpty()) {
            saveBatch(relationList, Const.BATCH_SAVE_SIZE);
        }
    }

    @Override
    public Map<CrmEnum, List<SimpleCrmEntity>> queryRelationMap(Integer orderId) {
        List<CrmOrderRelation> relations = lambdaQuery().eq(CrmOrderRelation::getOrderId, orderId).list();
        Map<CrmEnum, List<SimpleCrmEntity>> result = new EnumMap<>(CrmEnum.class);
        for (CrmOrderRelation relation : relations) {
            CrmEnum crmEnum = CrmEnum.parse(relation.getRelationType());
            SimpleCrmEntity entity = new SimpleCrmEntity();
            entity.setId(relation.getRelationId());
            entity.setName(relation.getRelationName());
            result.computeIfAbsent(crmEnum, k -> new ArrayList<>()).add(entity);
        }
        return result;
    }

    @Override
    public List<Integer> queryOrderIds(Integer relationType, Integer relationId) {
        if (relationType == null || relationId == null) {
            return Collections.emptyList();
        }
        return lambdaQuery()
                .select(CrmOrderRelation::getOrderId)
                .eq(CrmOrderRelation::getRelationType, relationType)
                .eq(CrmOrderRelation::getRelationId, relationId)
                .list()
                .stream()
                .map(CrmOrderRelation::getOrderId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> queryRelationNameMap(Integer orderId) {
        Map<CrmEnum, List<SimpleCrmEntity>> relationMap = queryRelationMap(orderId);
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put("leadsNames", joinNames(relationMap.get(CrmEnum.LEADS)));
        nameMap.put("customerNames", joinNames(relationMap.get(CrmEnum.CUSTOMER)));
        nameMap.put("contactsNames", joinNames(relationMap.get(CrmEnum.CONTACTS)));
        nameMap.put("businessNames", joinNames(relationMap.get(CrmEnum.BUSINESS)));
        nameMap.put("contractNames", joinNames(relationMap.get(CrmEnum.CONTRACT)));
        nameMap.put("receivablesNames", joinNames(relationMap.get(CrmEnum.RECEIVABLES)));
        nameMap.put("invoiceNames", joinNames(relationMap.get(CrmEnum.INVOICE)));
        nameMap.put("returnVisitNames", joinNames(relationMap.get(CrmEnum.RETURN_VISIT)));
        nameMap.put("productNames", joinNames(relationMap.get(CrmEnum.PRODUCT)));
        return nameMap;
    }

    private void saveByType(List<CrmOrderRelation> relationList, Integer orderId, CrmEnum crmEnum, Object value) {
        List<Integer> ids = parseIds(value);
        if (ids.isEmpty()) {
            return;
        }
        Map<Integer, String> nameMap = queryNameMap(crmEnum, ids);
        for (Integer id : ids) {
            relationList.add(new CrmOrderRelation()
                    .setOrderId(orderId)
                    .setRelationType(crmEnum.getType())
                    .setRelationId(id)
                    .setRelationName(nameMap.getOrDefault(id, String.valueOf(id))));
        }
    }

    private Map<Integer, String> queryNameMap(CrmEnum crmEnum, List<Integer> ids) {
        List<SimpleCrmEntity> entities;
        switch (crmEnum) {
            case LEADS:
                entities = crmLeadsService.querySimpleEntity(ids);
                break;
            case CUSTOMER:
                entities = crmCustomerService.querySimpleEntity(ids);
                break;
            case CONTACTS:
                entities = crmContactsService.querySimpleEntity(ids);
                break;
            case BUSINESS:
                entities = crmBusinessService.querySimpleEntity(ids);
                break;
            case CONTRACT:
                entities = crmContractService.querySimpleEntity(ids);
                break;
            case RECEIVABLES:
                entities = crmReceivablesService.querySimpleEntity(ids);
                break;
            case INVOICE:
                entities = crmInvoiceService.querySimpleEntity(ids);
                break;
            case RETURN_VISIT:
                entities = crmReturnVisitService.querySimpleEntity(ids);
                break;
            case PRODUCT:
                entities = crmProductService.querySimpleEntity(ids);
                break;
            default:
                entities = Collections.emptyList();
                break;
        }
        return entities.stream().collect(Collectors.toMap(SimpleCrmEntity::getId, SimpleCrmEntity::getName, (a, b) -> a));
    }

    private List<Integer> parseIds(Object value) {
        if (value == null) {
            return Collections.emptyList();
        }
        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            List<Integer> ids = new ArrayList<>();
            for (Object item : collection) {
                if (item instanceof Map) {
                    Object id = ((Map<?, ?>) item).get("id");
                    if (id == null) {
                        id = ((Map<?, ?>) item).get("value");
                    }
                    if (id == null) {
                        id = ((Map<?, ?>) item).entrySet().stream()
                                .filter(entry -> entry.getKey() != null && entry.getKey().toString().endsWith("Id"))
                                .map(Map.Entry::getValue)
                                .findFirst()
                                .orElse(null);
                    }
                    if (id != null) {
                        ids.add(Convert.toInt(id));
                    }
                } else {
                    ids.add(Convert.toInt(item));
                }
            }
            return ids.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        }
        return StrUtil.splitTrim(Convert.toStr(value), Const.SEPARATOR)
                .stream()
                .map(Convert::toInt)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private String joinNames(List<SimpleCrmEntity> entities) {
        if (CollUtil.isEmpty(entities)) {
            return "";
        }
        return entities.stream().map(SimpleCrmEntity::getName).collect(Collectors.joining(Const.SEPARATOR));
    }
}
