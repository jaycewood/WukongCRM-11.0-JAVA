package com.kakarote.crm.mapper;

import com.kakarote.core.servlet.BaseMapper;
import com.kakarote.crm.entity.PO.CrmOrderProduct;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CrmOrderProductMapper extends BaseMapper<CrmOrderProduct> {

    List<CrmOrderProduct> queryByOrderId(@Param("orderId") Integer orderId);
}
