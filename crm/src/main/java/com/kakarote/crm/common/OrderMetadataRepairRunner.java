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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
