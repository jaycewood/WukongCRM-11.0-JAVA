package com.kakarote.crm.service;

import com.kakarote.core.servlet.BaseService;
import com.kakarote.crm.entity.PO.CrmOrderProduct;

import java.util.List;

public interface ICrmOrderProductService extends BaseService<CrmOrderProduct> {

    void deleteByOrderId(Integer... orderIds);

    void saveByOrderId(Integer orderId, List<CrmOrderProduct> productList);

    List<CrmOrderProduct> queryByOrderId(Integer orderId);
}
