package com.kakarote.crm.common.log;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.kakarote.core.common.log.Content;
import com.kakarote.core.servlet.ApplicationContextHolder;
import com.kakarote.crm.constant.CrmEnum;
import com.kakarote.crm.entity.BO.CrmModelSaveBO;
import com.kakarote.crm.entity.PO.CrmOrder;
import com.kakarote.crm.service.ICrmOrderService;
import com.kakarote.crm.service.ICrmOrderDataService;

import java.util.ArrayList;
import java.util.List;

public class CrmOrderLog {

    private final SysLogUtil sysLogUtil = ApplicationContextHolder.getBean(SysLogUtil.class);
    private final ICrmOrderService crmOrderService = ApplicationContextHolder.getBean(ICrmOrderService.class);
    private final ICrmOrderDataService crmOrderDataService = ApplicationContextHolder.getBean(ICrmOrderDataService.class);

    public Content update(CrmModelSaveBO crmModel) {
        CrmOrder crmOrder = BeanUtil.copyProperties(crmModel.getEntity(), CrmOrder.class);
        String batchId = StrUtil.isNotEmpty(crmOrder.getBatchId()) ? crmOrder.getBatchId() : IdUtil.simpleUUID();
        sysLogUtil.updateRecord(crmModel.getField(), Dict.create().set("batchId", batchId).set("dataTableName", "wk_crm_order_data"));
        crmOrderDataService.saveData(crmModel.getField(), batchId);
        CrmOrder oldOrder = crmOrderService.getById(crmOrder.getOrderId());
        return sysLogUtil.updateRecord(BeanUtil.beanToMap(oldOrder), BeanUtil.beanToMap(crmOrder), CrmEnum.ORDER, crmOrderService.getOrderName(crmOrder.getOrderId()));
    }

    public List<Content> deleteByIds(List<Integer> ids) {
        List<Content> contentList = new ArrayList<>();
        for (Integer id : ids) {
            contentList.add(sysLogUtil.addDeleteActionRecord(CrmEnum.ORDER, crmOrderService.getOrderName(id)));
        }
        return contentList;
    }
}
