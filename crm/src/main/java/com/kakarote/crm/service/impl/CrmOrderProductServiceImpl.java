package com.kakarote.crm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.crm.entity.PO.CrmOrderProduct;
import com.kakarote.crm.mapper.CrmOrderProductMapper;
import com.kakarote.crm.service.ICrmOrderProductService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class CrmOrderProductServiceImpl extends BaseServiceImpl<CrmOrderProductMapper, CrmOrderProduct> implements ICrmOrderProductService {

    @Override
    public void deleteByOrderId(Integer... orderIds) {
        if (orderIds == null || orderIds.length == 0) {
            return;
        }
        remove(new LambdaQueryWrapper<CrmOrderProduct>().in(CrmOrderProduct::getOrderId, Arrays.asList(orderIds)));
    }

    @Override
    public void saveByOrderId(Integer orderId, List<CrmOrderProduct> productList) {
        deleteByOrderId(orderId);
        if (productList == null || productList.isEmpty()) {
            return;
        }
        productList.forEach(product -> product.setOrderId(orderId));
        saveBatch(productList);
    }

    @Override
    public List<CrmOrderProduct> queryByOrderId(Integer orderId) {
        if (orderId == null) {
            return Collections.emptyList();
        }
        return getBaseMapper().queryByOrderId(orderId);
    }
}
