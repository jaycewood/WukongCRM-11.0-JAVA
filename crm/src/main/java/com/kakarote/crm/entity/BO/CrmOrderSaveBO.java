package com.kakarote.crm.entity.BO;

import com.kakarote.crm.entity.PO.CrmOrderProduct;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString
@ApiModel("crm订单保存对象")
public class CrmOrderSaveBO extends CrmModelSaveBO {

    @ApiModelProperty("订单报价明细")
    private List<CrmOrderProduct> product;

    @ApiModelProperty("订单报价明细，product 的别名")
    private List<CrmOrderProduct> quotationList;

    @ApiModelProperty("是否强制按汇率重算订单级金额")
    private Boolean forceExchangeConversion;

    public List<CrmOrderProduct> getProductList() {
        if (product != null) {
            return product;
        }
        if (quotationList != null) {
            return quotationList;
        }
        return Collections.emptyList();
    }

    public boolean isForceExchangeConversion() {
        return Boolean.TRUE.equals(forceExchangeConversion);
    }
}
