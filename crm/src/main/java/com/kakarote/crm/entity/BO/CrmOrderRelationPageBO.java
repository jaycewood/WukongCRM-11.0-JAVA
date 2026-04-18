package com.kakarote.crm.entity.BO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("订单关联查询BO")
public class CrmOrderRelationPageBO extends CrmSearchBO {

    @ApiModelProperty("关联模块类型，对应CrmEnum.type")
    private Integer relationType;

    @ApiModelProperty("关联模块ID")
    private Integer relationId;
}
