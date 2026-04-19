package com.kakarote.crm.common;

import cn.hutool.core.util.StrUtil;
import com.kakarote.crm.constant.CrmEnum;
import com.kakarote.crm.entity.PO.CrmField;
import com.kakarote.crm.entity.PO.CrmFieldSort;
import com.kakarote.crm.service.ICrmFieldService;
import com.kakarote.crm.service.ICrmFieldSortService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 修复历史订单模块字段在错误字符集下导入后出现的乱码元数据。
 */
@Component
@Slf4j
public class OrderMetadataRepairRunner implements ApplicationRunner {

    private static final String ORDER_STATUS_OPTIONS = "草稿,报价中,已成交,已关闭";

    private static final Map<String, String> ORDER_FIELD_NAME_MAP = new LinkedHashMap<>();

    private static final Map<String, String> ORDER_FIELD_SORT_NAME_MAP = new LinkedHashMap<>();

    private static final List<OrderFieldDefinition> ORDER_FIELD_DEFINITIONS = new ArrayList<>();

    static {
        ORDER_FIELD_NAME_MAP.put("order_number", "订单编号");
        ORDER_FIELD_NAME_MAP.put("title", "订单标题");
        ORDER_FIELD_NAME_MAP.put("order_status", "订单状态");
        ORDER_FIELD_NAME_MAP.put("quote_amount", "报价金额");
        ORDER_FIELD_NAME_MAP.put("purchase_cost", "采购成本");
        ORDER_FIELD_NAME_MAP.put("logistics_cost", "物流成本");
        ORDER_FIELD_NAME_MAP.put("profit_amount", "利润金额");
        ORDER_FIELD_NAME_MAP.put("profit_rate", "利润率");
        ORDER_FIELD_NAME_MAP.put("remark", "备注");
        ORDER_FIELD_NAME_MAP.put("owner_user_id", "负责人");

        ORDER_FIELD_SORT_NAME_MAP.put("orderNumber", "订单编号");
        ORDER_FIELD_SORT_NAME_MAP.put("title", "订单标题");
        ORDER_FIELD_SORT_NAME_MAP.put("orderStatus", "订单状态");
        ORDER_FIELD_SORT_NAME_MAP.put("quoteAmount", "报价金额");
        ORDER_FIELD_SORT_NAME_MAP.put("purchaseCost", "采购成本");
        ORDER_FIELD_SORT_NAME_MAP.put("logisticsCost", "物流成本");
        ORDER_FIELD_SORT_NAME_MAP.put("profitAmount", "利润金额");
        ORDER_FIELD_SORT_NAME_MAP.put("profitRate", "利润率");
        ORDER_FIELD_SORT_NAME_MAP.put("remark", "备注");
        ORDER_FIELD_SORT_NAME_MAP.put("ownerUserName", "负责人");
        ORDER_FIELD_SORT_NAME_MAP.put("ownerDeptName", "所属部门");
        ORDER_FIELD_SORT_NAME_MAP.put("updateTime", "更新时间");
        ORDER_FIELD_SORT_NAME_MAP.put("createTime", "创建时间");
        ORDER_FIELD_SORT_NAME_MAP.put("createUserName", "创建人");

        ORDER_FIELD_DEFINITIONS.add(new OrderFieldDefinition("order_number", "订单编号", 1, 255, 1, 1, 0, null, 176, 1, 50, null, "0,0"));
        ORDER_FIELD_DEFINITIONS.add(new OrderFieldDefinition("title", "订单标题", 1, 255, 0, 1, 1, null, 176, 1, 50, null, "0,1"));
        ORDER_FIELD_DEFINITIONS.add(new OrderFieldDefinition("order_status", "订单状态", 3, null, 0, 0, 2, ORDER_STATUS_OPTIONS, 176, 1, 50, null, "1,0"));
        ORDER_FIELD_DEFINITIONS.add(new OrderFieldDefinition("quote_amount", "报价金额", 6, null, 0, 0, 3, null, 176, 1, 50, 2, "1,1"));
        ORDER_FIELD_DEFINITIONS.add(new OrderFieldDefinition("purchase_cost", "采购成本", 6, null, 0, 0, 4, null, 176, 1, 50, 2, "2,0"));
        ORDER_FIELD_DEFINITIONS.add(new OrderFieldDefinition("logistics_cost", "物流成本", 6, null, 0, 0, 5, null, 176, 1, 50, 2, "2,1"));
        ORDER_FIELD_DEFINITIONS.add(new OrderFieldDefinition("profit_amount", "利润金额", 6, null, 0, 0, 6, null, 176, 1, 50, 2, "3,0"));
        ORDER_FIELD_DEFINITIONS.add(new OrderFieldDefinition("profit_rate", "利润率", 42, null, 0, 0, 7, null, 176, 1, 50, 2, "3,1"));
        ORDER_FIELD_DEFINITIONS.add(new OrderFieldDefinition("remark", "备注", 2, 1000, 0, 0, 8, null, 176, 1, 50, null, "4,0"));
        ORDER_FIELD_DEFINITIONS.add(new OrderFieldDefinition("owner_user_id", "负责人", 28, null, 0, 1, 9, null, 176, 1, 50, null, "4,1"));
    }

    private final ICrmFieldService crmFieldService;

    private final ICrmFieldSortService crmFieldSortService;

    public OrderMetadataRepairRunner(ICrmFieldService crmFieldService, ICrmFieldSortService crmFieldSortService) {
        this.crmFieldService = crmFieldService;
        this.crmFieldSortService = crmFieldSortService;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureBaseOrderFields();
        repairOrderFields();
        repairOrderFieldSorts();
    }

    private void ensureBaseOrderFields() {
        List<CrmField> fields = crmFieldService.lambdaQuery()
                .eq(CrmField::getLabel, CrmEnum.ORDER.getType())
                .list();
        Map<String, CrmField> fieldMap = new LinkedHashMap<>();
        fields.forEach(field -> fieldMap.put(field.getFieldName(), field));
        int created = 0;
        int repaired = 0;
        for (OrderFieldDefinition definition : ORDER_FIELD_DEFINITIONS) {
            CrmField field = fieldMap.get(definition.fieldName);
            if (field == null) {
                crmFieldService.save(definition.toField());
                created++;
                continue;
            }
            boolean changed = false;
            if (!Objects.equals(field.getType(), definition.type)) {
                field.setType(definition.type);
                changed = true;
            }
            if (!Objects.equals(field.getFieldType(), definition.fieldType)) {
                field.setFieldType(definition.fieldType);
                changed = true;
            }
            if (!Objects.equals(field.getOperating(), definition.operating)) {
                field.setOperating(definition.operating);
                changed = true;
            }
            if (!Objects.equals(field.getIsHidden(), 0)) {
                field.setIsHidden(0);
                changed = true;
            }
            if (!Objects.equals(field.getIsUnique(), definition.isUnique)) {
                field.setIsUnique(definition.isUnique);
                changed = true;
            }
            if (!Objects.equals(field.getIsNull(), definition.isNull)) {
                field.setIsNull(definition.isNull);
                changed = true;
            }
            if (!Objects.equals(field.getSorting(), definition.sorting)) {
                field.setSorting(definition.sorting);
                changed = true;
            }
            if (!Objects.equals(field.getStylePercent(), definition.stylePercent)) {
                field.setStylePercent(definition.stylePercent);
                changed = true;
            }
            if (!Objects.equals(field.getMaxLength(), definition.maxLength)) {
                field.setMaxLength(definition.maxLength);
                changed = true;
            }
            if (!Objects.equals(field.getPrecisions(), definition.precisions)) {
                field.setPrecisions(definition.precisions);
                changed = true;
            }
            if (isInvalidFormPosition(field.getFormPosition())) {
                field.setFormPosition(definition.formPosition);
                changed = true;
            }
            if (shouldRepair(field.getName(), definition.name)) {
                field.setName(definition.name);
                changed = true;
            }
            if (Objects.equals("order_status", definition.fieldName)
                    && shouldRepair(field.getOptions(), definition.options)) {
                field.setOptions(definition.options);
                changed = true;
            }
            if (changed) {
                crmFieldService.updateById(field);
                repaired++;
            }
        }
        if (created > 0) {
            log.info("created {} missing order field rows", created);
        }
        if (repaired > 0) {
            log.info("repaired {} order field definition rows", repaired);
        }
    }

    private void repairOrderFields() {
        List<CrmField> fields = crmFieldService.lambdaQuery()
                .eq(CrmField::getLabel, CrmEnum.ORDER.getType())
                .list();
        int repaired = 0;
        for (CrmField field : fields) {
            boolean changed = false;
            String expectedName = ORDER_FIELD_NAME_MAP.get(field.getFieldName());
            if (expectedName != null && shouldRepair(field.getName(), expectedName)) {
                field.setName(expectedName);
                changed = true;
            }
            if ("order_status".equals(field.getFieldName()) && shouldRepair(field.getOptions(), ORDER_STATUS_OPTIONS)) {
                field.setOptions(ORDER_STATUS_OPTIONS);
                changed = true;
            }
            if (changed) {
                crmFieldService.updateById(field);
                repaired++;
            }
        }
        if (repaired > 0) {
            log.info("repaired {} order field metadata rows", repaired);
        }
    }

    private void repairOrderFieldSorts() {
        List<CrmFieldSort> fieldSorts = crmFieldSortService.lambdaQuery()
                .eq(CrmFieldSort::getLabel, CrmEnum.ORDER.getType())
                .list();
        int repaired = 0;
        for (CrmFieldSort fieldSort : fieldSorts) {
            String expectedName = ORDER_FIELD_SORT_NAME_MAP.get(fieldSort.getFieldName());
            if (expectedName != null && shouldRepair(fieldSort.getName(), expectedName)) {
                fieldSort.setName(expectedName);
                crmFieldSortService.updateById(fieldSort);
                repaired++;
            }
        }
        if (repaired > 0) {
            log.info("repaired {} order field sort rows", repaired);
        }
    }

    private boolean shouldRepair(String currentValue, String expectedValue) {
        if (StrUtil.isBlank(expectedValue) || StrUtil.equals(currentValue, expectedValue)) {
            return false;
        }
        if (StrUtil.isBlank(currentValue)) {
            return true;
        }
        if (currentValue.contains("�")) {
            return true;
        }
        boolean containsCjk = currentValue.codePoints().anyMatch(this::isCjk);
        boolean containsLatinSupplement = currentValue.codePoints().anyMatch(codePoint -> codePoint >= 0x00C0 && codePoint <= 0x00FF);
        return !containsCjk && containsLatinSupplement;
    }

    private boolean isCjk(int codePoint) {
        Character.UnicodeScript script = Character.UnicodeScript.of(codePoint);
        return script == Character.UnicodeScript.HAN;
    }

    private boolean isInvalidFormPosition(String formPosition) {
        if (StrUtil.isBlank(formPosition)) {
            return true;
        }
        String[] axisArr = formPosition.split(",");
        if (axisArr.length != 2) {
            return true;
        }
        return !axisArr[0].matches("[0-9]+") || !axisArr[1].matches("[0-9]+");
    }

    private static class OrderFieldDefinition {
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

        private CrmField toField() {
            CrmField field = new CrmField()
                    .setFieldName(fieldName)
                    .setName(name)
                    .setType(type)
                    .setLabel(CrmEnum.ORDER.getType())
                    .setMaxLength(maxLength)
                    .setDefaultValue("")
                    .setIsUnique(isUnique)
                    .setIsNull(isNull)
                    .setSorting(sorting)
                    .setOptions(options)
                    .setOperating(operating)
                    .setIsHidden(0)
                    .setFieldType(fieldType)
                    .setStylePercent(stylePercent)
                    .setPrecisions(precisions);
            field.setFormPosition(formPosition);
            return field;
        }
    }
}
