package com.kakarote.crm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kakarote.core.common.Const;
import com.kakarote.core.field.FieldService;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.crm.common.CrmModel;
import com.kakarote.crm.entity.PO.CrmOrderData;
import com.kakarote.crm.entity.VO.CrmModelFiledVO;
import com.kakarote.crm.mapper.CrmOrderDataMapper;
import com.kakarote.crm.service.ICrmOrderDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CrmOrderDataServiceImpl extends BaseServiceImpl<CrmOrderDataMapper, CrmOrderData> implements ICrmOrderDataService {

    @Autowired
    private FieldService fieldService;

    @Override
    public void saveData(List<CrmModelFiledVO> array, String batchId) {
        remove(new LambdaQueryWrapper<CrmOrderData>().eq(CrmOrderData::getBatchId, batchId));
        if (array == null || array.isEmpty()) {
            return;
        }
        List<CrmOrderData> dataList = new ArrayList<>();
        Date now = new Date();
        for (CrmModelFiledVO obj : array) {
            CrmOrderData orderData = BeanUtil.copyProperties(obj, CrmOrderData.class);
            orderData.setValue(fieldService.convertObjectValueToString(obj.getType(), obj.getValue(), orderData.getValue()));
            orderData.setName(obj.getFieldName());
            orderData.setCreateTime(now);
            orderData.setBatchId(batchId);
            dataList.add(orderData);
        }
        saveBatch(dataList, Const.BATCH_SAVE_SIZE);
    }

    @Override
    public void setDataByBatchId(CrmModel crmModel) {
        list(new LambdaQueryWrapper<CrmOrderData>().eq(CrmOrderData::getBatchId, crmModel.getBatchId()))
                .forEach(data -> crmModel.put(data.getName(), data.getValue()));
    }

    @Override
    public void deleteByBatchId(List<String> batchList) {
        if (batchList == null || batchList.isEmpty()) {
            return;
        }
        remove(new LambdaQueryWrapper<CrmOrderData>().in(CrmOrderData::getBatchId, batchList));
    }
}
