package com.kakarote.admin.common;

import com.kakarote.admin.entity.PO.AdminConfig;
import com.kakarote.admin.service.IAdminConfigService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 为历史库补齐 CRM 订单自动编号配置。
 */
@Component
public class OrderConfigInitRunner implements ApplicationRunner {

    private final IAdminConfigService adminConfigService;

    public OrderConfigInitRunner(IAdminConfigService adminConfigService) {
        this.adminConfigService = adminConfigService;
    }

    @Override
    public void run(ApplicationArguments args) {
        AdminConfig config = adminConfigService.queryFirstConfigByNameAndValue("numberSetting", "19");
        if (config != null) {
            return;
        }
        adminConfigService.save(new AdminConfig()
                .setName("numberSetting")
                .setValue("19")
                .setStatus(0)
                .setDescription("自动编号设置"));
    }
}
