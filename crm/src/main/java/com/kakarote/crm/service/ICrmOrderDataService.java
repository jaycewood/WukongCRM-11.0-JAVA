package com.kakarote.crm.service;

import com.kakarote.core.servlet.BaseService;
import com.kakarote.crm.common.CrmModel;
import com.kakarote.crm.entity.PO.CrmOrderData;
import com.kakarote.crm.entity.VO.CrmModelFiledVO;

import java.util.List;

public interface ICrmOrderDataService extends BaseService<CrmOrderData> {

    void saveData(List<CrmModelFiledVO> array, String batchId);

    void setDataByBatchId(CrmModel crmModel);

    void deleteByBatchId(List<String> batchList);
}
