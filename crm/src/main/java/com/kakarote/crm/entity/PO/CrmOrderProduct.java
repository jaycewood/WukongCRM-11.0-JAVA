package com.kakarote.crm.entity.PO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("wk_crm_order_product")
@ApiModel(value = "CrmOrderProduct对象", description = "订单报价/利润明细表")
public class CrmOrderProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "r_id", type = IdType.AUTO)
    private Integer rId;

    @ApiModelProperty("订单ID")
    private Integer orderId;

    @ApiModelProperty("产品ID")
    private Integer productId;

    @ApiModelProperty("产品名称快照")
    private String productName;

    @ApiModelProperty("产品标准单价")
    private BigDecimal price;

    @ApiModelProperty("报价单价")
    private BigDecimal salesPrice;

    @ApiModelProperty("数量")
    private BigDecimal num;

    @ApiModelProperty("折扣")
    private BigDecimal discount;

    @ApiModelProperty("报价小计")
    private BigDecimal subtotal;

    @ApiModelProperty("采购单价")
    private BigDecimal purchasePrice;

    @ApiModelProperty("采购成本")
    private BigDecimal purchaseCost;

    @ApiModelProperty("物流成本")
    private BigDecimal logisticsCost;

    @ApiModelProperty("利润金额")
    private BigDecimal profitAmount;

    @ApiModelProperty("利润率")
    private BigDecimal profitRate;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("明细备注")
    private String remark;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("产品名称")
    @TableField(exist = false)
    private String name;

    @ApiModelProperty("产品分类名称")
    @TableField(exist = false)
    private String categoryName;

    @ApiModelProperty("总成本")
    @TableField(exist = false)
    private BigDecimal totalCost;
}
