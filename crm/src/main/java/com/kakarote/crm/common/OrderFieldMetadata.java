package com.kakarote.crm.common;

import cn.hutool.core.util.StrUtil;
import com.kakarote.core.common.Const;
import com.kakarote.core.common.FieldEnum;
import com.kakarote.crm.constant.CrmEnum;
import com.kakarote.crm.entity.PO.CrmField;
import com.kakarote.crm.entity.VO.CrmModelFiledVO;

import java.util.*;

/**
 * 订单固定字段的标准元数据定义。
 */
public final class OrderFieldMetadata {

    public static final String ORDER_STATUS_OPTIONS = "草稿,报价中,生成中,出货中,运输中,货物妥投,已成交,已关闭";

    private static final Map<String, String> ORDER_FIELD_NAME_MAP = new LinkedHashMap<>();

    private static final Map<String, String> ORDER_FIELD_SORT_NAME_MAP = new LinkedHashMap<>();

    private static final Map<String, OrderFieldDefinition> ORDER_FIELD_DEFINITION_MAP = new LinkedHashMap<>();

    static {
        ORDER_FIELD_NAME_MAP.put("order_number", "订单编号");
        ORDER_FIELD_NAME_MAP.put("title", "订单标题");
        ORDER_FIELD_NAME_MAP.put("order_status", "订单状态");
        ORDER_FIELD_NAME_MAP.put("logistics_number", "物流单号");
        ORDER_FIELD_NAME_MAP.put("exchange_rate", "汇率换算");
        ORDER_FIELD_NAME_MAP.put("quote_amount", "报价金额");
        ORDER_FIELD_NAME_MAP.put("purchase_cost", "采购成本");
        ORDER_FIELD_NAME_MAP.put("logistics_cost", "物流成本");
        ORDER_FIELD_NAME_MAP.put("handling_fee_cost", "平手续成本");
        ORDER_FIELD_NAME_MAP.put("consumable_cost", "耗材成本");
        ORDER_FIELD_NAME_MAP.put("other_cost", "其他成本");
        ORDER_FIELD_NAME_MAP.put("profit_amount", "利润金额");
        ORDER_FIELD_NAME_MAP.put("profit_rate", "利润率");
        ORDER_FIELD_NAME_MAP.put("remark", "备注");
        ORDER_FIELD_NAME_MAP.put("owner_user_id", "负责人");

        ORDER_FIELD_SORT_NAME_MAP.put("orderNumber", "订单编号");
        ORDER_FIELD_SORT_NAME_MAP.put("title", "订单标题");
        ORDER_FIELD_SORT_NAME_MAP.put("orderStatus", "订单状态");
        ORDER_FIELD_SORT_NAME_MAP.put("logisticsNumber", "物流单号");
        ORDER_FIELD_SORT_NAME_MAP.put("exchangeRate", "汇率换算");
        ORDER_FIELD_SORT_NAME_MAP.put("quoteAmount", "报价金额");
        ORDER_FIELD_SORT_NAME_MAP.put("purchaseCost", "采购成本");
        ORDER_FIELD_SORT_NAME_MAP.put("logisticsCost", "物流成本");
        ORDER_FIELD_SORT_NAME_MAP.put("handlingFeeCost", "平手续成本");
        ORDER_FIELD_SORT_NAME_MAP.put("consumableCost", "耗材成本");
        ORDER_FIELD_SORT_NAME_MAP.put("otherCost", "其他成本");
        ORDER_FIELD_SORT_NAME_MAP.put("profitAmount", "利润金额");
        ORDER_FIELD_SORT_NAME_MAP.put("profitRate", "利润率");
        ORDER_FIELD_SORT_NAME_MAP.put("remark", "备注");
        ORDER_FIELD_SORT_NAME_MAP.put("ownerUserName", "负责人");
        ORDER_FIELD_SORT_NAME_MAP.put("ownerDeptName", "所属部门");
        ORDER_FIELD_SORT_NAME_MAP.put("updateTime", "更新时间");
        ORDER_FIELD_SORT_NAME_MAP.put("createTime", "创建时间");
        ORDER_FIELD_SORT_NAME_MAP.put("createUserName", "创建人");

        register(new OrderFieldDefinition("order_number", "订单编号", 1, 255, 1, 1, 0, null, 176, 1, 50, null, "0,0"));
        register(new OrderFieldDefinition("title", "订单标题", 1, 255, 0, 1, 1, null, 176, 1, 50, null, "0,1"));
        register(new OrderFieldDefinition("order_status", "订单状态", 3, null, 0, 0, 2, ORDER_STATUS_OPTIONS, 176, 1, 50, null, "1,0"));
        register(new OrderFieldDefinition("logistics_number", "物流单号", 1, 255, 0, 0, 3, null, 176, 1, 50, null, "1,1"));
        register(new OrderFieldDefinition("exchange_rate", "汇率换算", 6, null, 0, 0, 4, null, 176, 1, 50, 6, "2,0"));
        register(new OrderFieldDefinition("quote_amount", "报价金额", 6, null, 0, 0, 5, null, 176, 1, 50, 2, "2,1"));
        register(new OrderFieldDefinition("purchase_cost", "采购成本", 6, null, 0, 0, 6, null, 176, 1, 50, 2, "3,0"));
        register(new OrderFieldDefinition("logistics_cost", "物流成本", 6, null, 0, 0, 7, null, 176, 1, 50, 2, "3,1"));
        register(new OrderFieldDefinition("handling_fee_cost", "平手续成本", 6, null, 0, 0, 8, null, 176, 1, 50, 2, "4,0"));
        register(new OrderFieldDefinition("consumable_cost", "耗材成本", 6, null, 0, 0, 9, null, 176, 1, 50, 2, "4,1"));
        register(new OrderFieldDefinition("other_cost", "其他成本", 6, null, 0, 0, 10, null, 176, 1, 50, 2, "5,0"));
        register(new OrderFieldDefinition("profit_amount", "利润金额", 6, null, 0, 0, 11, null, 176, 1, 50, 2, "5,1"));
        register(new OrderFieldDefinition("profit_rate", "利润率", 42, null, 0, 0, 12, null, 176, 1, 50, 2, "6,0"));
        register(new OrderFieldDefinition("remark", "备注", 2, 1000, 0, 0, 13, null, 176, 1, 50, null, "6,1"));
        register(new OrderFieldDefinition("owner_user_id", "负责人", 28, null, 0, 1, 14, null, 176, 1, 50, null, "7,0"));
    }

    private OrderFieldMetadata() {
    }

    private static void register(OrderFieldDefinition definition) {
        ORDER_FIELD_DEFINITION_MAP.put(definition.getFieldName(), definition);
    }

    public static Collection<OrderFieldDefinition> definitions() {
        return Collections.unmodifiableCollection(ORDER_FIELD_DEFINITION_MAP.values());
    }

    public static Map<String, String> fieldNameMap() {
        return Collections.unmodifiableMap(ORDER_FIELD_NAME_MAP);
    }

    public static Map<String, String> fieldSortNameMap() {
        return Collections.unmodifiableMap(ORDER_FIELD_SORT_NAME_MAP);
    }

    public static boolean isOrderFixedField(String fieldName) {
        return definition(fieldName) != null;
    }

    public static OrderFieldDefinition definition(String fieldName) {
        if (StrUtil.isBlank(fieldName)) {
            return null;
        }
        OrderFieldDefinition definition = ORDER_FIELD_DEFINITION_MAP.get(fieldName);
        if (definition != null) {
            return definition;
        }
        return ORDER_FIELD_DEFINITION_MAP.get(StrUtil.toUnderlineCase(fieldName));
    }

    public static boolean applyCanonicalMetadata(CrmField field) {
        OrderFieldDefinition definition = definition(field.getFieldName());
        return definition != null && definition.apply(field);
    }

    public static boolean applyCanonicalMetadata(CrmModelFiledVO field) {
        OrderFieldDefinition definition = definition(field.getFieldName());
        return definition != null && definition.apply(field);
    }

    private static void applyFormPosition(CrmModelFiledVO field, String formPosition) {
        field.setFormPosition(formPosition);
        if (StrUtil.isBlank(formPosition)) {
            field.setXAxis(-1);
            field.setYAxis(-1);
            return;
        }
        String[] axisArr = formPosition.split(Const.SEPARATOR);
        if (axisArr.length == 2 && axisArr[0].matches("[0-9]+") && axisArr[1].matches("[0-9]+")) {
            field.setXAxis(Integer.valueOf(axisArr[0]));
            field.setYAxis(Integer.valueOf(axisArr[1]));
            return;
        }
        field.setXAxis(-1);
        field.setYAxis(-1);
    }

    public static final class OrderFieldDefinition {
        private final String fieldName;
        private final String name;
        private final Integer type;
        private final Integer maxLength;
        private final Integer isUnique;
        private final Integer isNull;
        private final Integer sorting;
        private final String options;
        private final Integer operating;
        private final Integer fieldType;
        private final Integer stylePercent;
        private final Integer precisions;
        private final String formPosition;

        private OrderFieldDefinition(String fieldName, String name, Integer type, Integer maxLength, Integer isUnique,
                                     Integer isNull, Integer sorting, String options, Integer operating,
                                     Integer fieldType, Integer stylePercent, Integer precisions, String formPosition) {
            this.fieldName = fieldName;
            this.name = name;
            this.type = type;
            this.maxLength = maxLength;
            this.isUnique = isUnique;
            this.isNull = isNull;
            this.sorting = sorting;
            this.options = options;
            this.operating = operating;
            this.fieldType = fieldType;
            this.stylePercent = stylePercent;
            this.precisions = precisions;
            this.formPosition = formPosition;
        }

        public String getFieldName() {
            return fieldName;
        }

        public CrmField toField() {
            String defaultValue = defaultValue();
            CrmField field = new CrmField()
                    .setFieldName(fieldName)
                    .setName(name)
                    .setType(type)
                    .setLabel(CrmEnum.ORDER.getType())
                    .setMaxLength(maxLength)
                    .setDefaultValue(defaultValue)
                    .setIsUnique(isUnique)
                    .setIsNull(isNull)
                    .setSorting(sorting)
                    .setOptions(options)
                    .setOperating(operating)
                    .setIsHidden(0)
                    .setFieldType(fieldType)
                    .setStylePercent(stylePercent)
                    .setPrecisions(precisions)
                    .setRemark(null)
                    .setRelevant(null)
                    .setFormAssistId(null)
                    .setMaxNumRestrict(null)
                    .setMinNumRestrict(null);
            field.setFormPosition(formPosition);
            return field;
        }

        private boolean apply(CrmField field) {
            boolean changed = false;
            if (!Objects.equals(field.getType(), type)) {
                field.setType(type);
                changed = true;
            }
            if (!Objects.equals(field.getMaxLength(), maxLength)) {
                field.setMaxLength(maxLength);
                changed = true;
            }
            if (!Objects.equals(field.getIsUnique(), isUnique)) {
                field.setIsUnique(isUnique);
                changed = true;
            }
            if (!Objects.equals(field.getIsNull(), isNull)) {
                field.setIsNull(isNull);
                changed = true;
            }
            if (!Objects.equals(field.getSorting(), sorting)) {
                field.setSorting(sorting);
                changed = true;
            }
            if (!Objects.equals(field.getOptions(), options)) {
                field.setOptions(options);
                changed = true;
            }
            if (!Objects.equals(field.getOperating(), operating)) {
                field.setOperating(operating);
                changed = true;
            }
            if (!Objects.equals(field.getIsHidden(), 0)) {
                field.setIsHidden(0);
                changed = true;
            }
            if (!Objects.equals(field.getFieldType(), fieldType)) {
                field.setFieldType(fieldType);
                changed = true;
            }
            if (!Objects.equals(field.getStylePercent(), stylePercent)) {
                field.setStylePercent(stylePercent);
                changed = true;
            }
            if (!Objects.equals(field.getPrecisions(), precisions)) {
                field.setPrecisions(precisions);
                changed = true;
            }
            if (!Objects.equals(field.getName(), name)) {
                field.setName(name);
                changed = true;
            }
            if (!Objects.equals(field.getFormPosition(), formPosition)) {
                field.setFormPosition(formPosition);
                changed = true;
            }
            if (!Objects.equals(field.getRemark(), null)) {
                field.setRemark(null);
                changed = true;
            }
            if (!Objects.equals(field.getRelevant(), null)) {
                field.setRelevant(null);
                changed = true;
            }
            if (!Objects.equals(field.getFormAssistId(), null)) {
                field.setFormAssistId(null);
                changed = true;
            }
            if (!Objects.equals(field.getMaxNumRestrict(), null)) {
                field.setMaxNumRestrict(null);
                changed = true;
            }
            if (!Objects.equals(field.getMinNumRestrict(), null)) {
                field.setMinNumRestrict(null);
                changed = true;
            }
            if (field.getDefaultValue() == null) {
                field.setDefaultValue(defaultValue());
                changed = true;
            } else if (!Objects.equals(field.getDefaultValue(), defaultValue())) {
                field.setDefaultValue(defaultValue());
                changed = true;
            }
            return changed;
        }

        private boolean apply(CrmModelFiledVO field) {
            boolean changed = false;
            if (!Objects.equals(field.getFieldName(), StrUtil.toCamelCase(fieldName))) {
                field.setFieldName(fieldName);
                changed = true;
            }
            if (!Objects.equals(field.getName(), name)) {
                field.setName(name);
                changed = true;
            }
            if (!Objects.equals(field.getType(), type)) {
                field.setType(type);
                changed = true;
            }
            String formType = FieldEnum.parse(type).getFormType();
            if (!Objects.equals(field.getFormType(), formType)) {
                field.setFormType(formType);
                changed = true;
            }
            if (!Objects.equals(field.getMaxLength(), maxLength)) {
                field.setMaxLength(maxLength);
                changed = true;
            }
            if (!Objects.equals(field.getIsUnique(), isUnique)) {
                field.setIsUnique(isUnique);
                changed = true;
            }
            if (!Objects.equals(field.getIsNull(), isNull)) {
                field.setIsNull(isNull);
                changed = true;
            }
            if (!Objects.equals(field.getSorting(), sorting)) {
                field.setSorting(sorting);
                changed = true;
            }
            if (!Objects.equals(field.getOptions(), options)) {
                field.setOptions(options);
                changed = true;
            }
            if (!Objects.equals(field.getFieldType(), fieldType)) {
                field.setFieldType(fieldType);
                changed = true;
            }
            if (!Objects.equals(field.getStylePercent(), stylePercent)) {
                field.setStylePercent(stylePercent);
                changed = true;
            }
            if (!Objects.equals(field.getPrecisions(), precisions)) {
                field.setPrecisions(precisions);
                changed = true;
            }
            if (!Objects.equals(field.getFormPosition(), formPosition)
                    || field.getXAxis() == null
                    || field.getYAxis() == null) {
                applyFormPosition(field, formPosition);
                changed = true;
            } else {
                applyFormPosition(field, formPosition);
            }
            if (!Objects.equals(field.getRemark(), null)) {
                field.setRemark(null);
                changed = true;
            }
            if (!Objects.equals(field.getFormAssistId(), null)) {
                field.setFormAssistId(null);
                changed = true;
            }
            if (!Objects.equals(field.getDefaultValue(), defaultValue())) {
                field.setDefaultValue(defaultValue());
                changed = true;
            }
            if (field.getOptionsData() != null) {
                field.setOptionsData(null);
                changed = true;
            }
            if (FieldEnum.SELECT.getType().equals(type)) {
                List<Object> setting = new ArrayList<>(StrUtil.splitTrim(options, Const.SEPARATOR));
                if (!Objects.equals(field.getSetting(), setting)) {
                    field.setSetting(setting);
                    changed = true;
                }
            } else if (field.getSetting() == null) {
                field.setSetting(new ArrayList<>());
                changed = true;
            }
            return changed;
        }

        private String defaultValue() {
            if ("exchange_rate".equals(fieldName)) {
                return "1";
            }
            return "";
        }
    }
}
