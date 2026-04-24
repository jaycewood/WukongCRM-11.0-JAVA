package com.kakarote.crm.common;

import cn.hutool.core.util.StrUtil;
import com.kakarote.core.common.Const;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 修复历史订单模块字段在错误字符集下导入后出现的乱码和脏元数据。
 */
@Component
@Slf4j
public class OrderMetadataRepairRunner implements ApplicationRunner {

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
        ensureOrderFieldSorts();
    }

    private void ensureBaseOrderFields() {
        List<CrmField> fields = crmFieldService.lambdaQuery()
                .eq(CrmField::getLabel, CrmEnum.ORDER.getType())
                .list();
        Map<String, CrmField> fieldMap = new LinkedHashMap<>();
        fields.forEach(field -> fieldMap.put(field.getFieldName(), field));
        int created = 0;
        int repaired = 0;
        for (OrderFieldMetadata.OrderFieldDefinition definition : OrderFieldMetadata.definitions()) {
            CrmField field = fieldMap.get(definition.getFieldName());
            if (field == null) {
                crmFieldService.save(definition.toField());
                created++;
                continue;
            }
            if (OrderFieldMetadata.applyCanonicalMetadata(field)) {
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
            String expectedName = OrderFieldMetadata.fieldNameMap().get(field.getFieldName());
            if (expectedName != null && shouldRepair(field.getName(), expectedName)) {
                field.setName(expectedName);
                changed = true;
            }
            if ("order_status".equals(field.getFieldName())
                    && shouldRepair(field.getOptions(), OrderFieldMetadata.ORDER_STATUS_OPTIONS)) {
                field.setOptions(OrderFieldMetadata.ORDER_STATUS_OPTIONS);
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
            String expectedName = OrderFieldMetadata.fieldSortNameMap().get(fieldSort.getFieldName());
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

    private void ensureOrderFieldSorts() {
        List<CrmField> fields = crmFieldService.lambdaQuery()
                .eq(CrmField::getLabel, CrmEnum.ORDER.getType())
                .eq(CrmField::getIsHidden, 0)
                .orderByAsc(CrmField::getSorting)
                .list();
        if (fields.isEmpty()) {
            return;
        }
        List<CrmFieldSort> fieldSorts = crmFieldSortService.lambdaQuery()
                .eq(CrmFieldSort::getLabel, CrmEnum.ORDER.getType())
                .list();
        Map<Long, Set<String>> userFieldNameMap = new LinkedHashMap<>();
        for (CrmFieldSort fieldSort : fieldSorts) {
            if (fieldSort.getUserId() == null) {
                continue;
            }
            userFieldNameMap.computeIfAbsent(fieldSort.getUserId(), key -> new HashSet<>()).add(fieldSort.getFieldName());
        }
        if (userFieldNameMap.isEmpty()) {
            return;
        }
        List<CrmFieldSort> missingFieldSorts = new ArrayList<>();
        for (Map.Entry<Long, Set<String>> entry : userFieldNameMap.entrySet()) {
            Long userId = entry.getKey();
            Set<String> fieldNames = entry.getValue();
            for (CrmField field : fields) {
                String fieldName = StrUtil.toCamelCase(field.getFieldName());
                if (fieldNames.contains(fieldName)) {
                    continue;
                }
                CrmFieldSort fieldSort = new CrmFieldSort();
                fieldSort.setFieldId(field.getFieldId());
                fieldSort.setFieldName(fieldName);
                fieldSort.setName(field.getName());
                fieldSort.setSort(field.getSorting());
                fieldSort.setUserId(userId);
                fieldSort.setStyle(100);
                fieldSort.setIsHide(0);
                fieldSort.setLabel(CrmEnum.ORDER.getType());
                fieldSort.setType(field.getType());
                missingFieldSorts.add(fieldSort);
            }
        }
        if (!missingFieldSorts.isEmpty()) {
            crmFieldSortService.saveBatch(missingFieldSorts, Const.BATCH_SAVE_SIZE);
            log.info("created {} missing order field sort rows", missingFieldSorts.size());
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
}
