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
 * 修复历史订单模块字段在错误字符集下导入后出现的乱码元数据。
 */
@Component
@Slf4j
public class OrderMetadataRepairRunner implements ApplicationRunner {

    private static final String ORDER_STATUS_OPTIONS = "草稿,报价中,已成交,已关闭";

    private static final Map<String, String> ORDER_FIELD_NAME_MAP = new LinkedHashMap<>();

    private static final Map<String, String> ORDER_FIELD_SORT_NAME_MAP = new LinkedHashMap<>();

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
    }

    private final ICrmFieldService crmFieldService;

    private final ICrmFieldSortService crmFieldSortService;

    public OrderMetadataRepairRunner(ICrmFieldService crmFieldService, ICrmFieldSortService crmFieldSortService) {
        this.crmFieldService = crmFieldService;
        this.crmFieldSortService = crmFieldSortService;
    }

    @Override
    public void run(ApplicationArguments args) {
        repairOrderFields();
        repairOrderFieldSorts();
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
}
