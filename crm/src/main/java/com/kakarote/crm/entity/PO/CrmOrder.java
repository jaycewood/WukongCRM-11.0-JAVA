package com.kakarote.crm.entity.PO;

import com.baomidou.mybatisplus.annotation.FieldFill;
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
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("wk_crm_order")
@ApiModel(value = "CrmOrder对象", description = "CRM订单")
public class CrmOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "order_id", type = IdType.AUTO)
    @ApiModelProperty("订单ID")
    private Integer orderId;

    @ApiModelProperty("订单编号")
    private String orderNumber;

    @ApiModelProperty("订单标题")
    private String title;

    @ApiModelProperty("订单状态")
    private Integer orderStatus;

    @ApiModelProperty("汇率换算")
    private BigDecimal exchangeRate;

    @ApiModelProperty("报价金额")
    private BigDecimal quoteAmount;

    @ApiModelProperty("采购成本")
    private BigDecimal purchaseCost;

    @ApiModelProperty("物流成本")
    private BigDecimal logisticsCost;

    @ApiModelProperty("平手续成本")
    private BigDecimal handlingFeeCost;

    @ApiModelProperty("耗材成本")
    private BigDecimal consumableCost;

    @ApiModelProperty("其他成本")
    private BigDecimal otherCost;

    @ApiModelProperty("利润金额")
    private BigDecimal profitAmount;

    @ApiModelProperty("利润率")
    private BigDecimal profitRate;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("负责人")
    @TableField(fill = FieldFill.INSERT)
    private Long ownerUserId;

    @ApiModelProperty("创建人")
    @TableField(fill = FieldFill.INSERT)
    private Long createUserId;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    @ApiModelProperty("批次ID")
    private String batchId;
}
