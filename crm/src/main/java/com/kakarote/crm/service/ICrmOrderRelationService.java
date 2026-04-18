package com.kakarote.crm.service;

import com.kakarote.core.feign.crm.entity.SimpleCrmEntity;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.crm.constant.CrmEnum;
import com.kakarote.crm.entity.PO.CrmOrderRelation;

import java.util.List;
import java.util.Map;

public interface ICrmOrderRelationService extends BaseService<CrmOrderRelation> {

    void saveRelationData(Integer orderId, Map<String, Object> entity);

    Map<CrmEnum, List<SimpleCrmEntity>> queryRelationMap(Integer orderId);

    List<Integer> queryOrderIds(Integer relationType, Integer relationId);

    Map<String, String> queryRelationNameMap(Integer orderId);
}
