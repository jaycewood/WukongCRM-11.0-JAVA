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
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("wk_crm_order_relation")
@ApiModel(value = "CrmOrderRelation对象", description = "订单关联关系")
public class CrmOrderRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("订单ID")
    private Integer orderId;

    @ApiModelProperty("关联模块类型")
    private Integer relationType;

    @ApiModelProperty("关联模块ID")
    private Integer relationId;

    @ApiModelProperty("关联名称快照")
    private String relationName;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
