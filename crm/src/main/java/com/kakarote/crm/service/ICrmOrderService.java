package com.kakarote.crm.service;

import com.kakarote.core.entity.BasePage;
import com.kakarote.core.feign.crm.entity.SimpleCrmEntity;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.core.servlet.upload.FileEntity;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.crm.common.CrmModel;
import com.kakarote.crm.entity.BO.CrmOrderSaveBO;
import com.kakarote.crm.entity.BO.CrmOrderRelationPageBO;
import com.kakarote.crm.entity.BO.CrmSearchBO;
import com.kakarote.crm.entity.PO.CrmOrder;
import com.kakarote.crm.entity.PO.CrmOrderProduct;
import com.kakarote.crm.entity.VO.CrmModelFiledVO;

import java.util.List;
import java.util.Map;

public interface ICrmOrderService extends BaseService<CrmOrder> {

    BasePage<Map<String, Object>> queryPageList(CrmSearchBO search);

    BasePage<Map<String, Object>> queryPageListByRelation(CrmOrderRelationPageBO search);

    List<SimpleCrmEntity> querySimpleEntity(List<Integer> ids);

    void addOrUpdate(CrmOrderSaveBO crmModel);

    CrmModel queryById(Integer orderId);

    List<CrmModelFiledVO> queryField(Integer id);

    List<List<CrmModelFiledVO>> queryFormPositionField(Integer id);

    List<CrmModelFiledVO> information(Integer orderId);

    List<FileEntity> queryFileList(Integer id);

    List<CrmOrderProduct> queryProductList(Integer orderId);

    List<JSONObject> queryQuotationList(Integer orderId);

    List<JSONObject> queryProfitList(Integer orderId);

    void deleteByIds(List<Integer> ids);

    String getOrderName(Integer orderId);
}
