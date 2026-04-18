package com.kakarote.admin.common;

import com.kakarote.admin.entity.PO.AdminMenu;
import com.kakarote.admin.service.IAdminMenuService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 为历史库补齐 CRM 订单权限菜单。
 */
@Component
public class OrderMenuInitRunner implements ApplicationRunner {

    private final IAdminMenuService adminMenuService;

    public OrderMenuInitRunner(IAdminMenuService adminMenuService) {
        this.adminMenuService = adminMenuService;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Integer> menuIds = Arrays.asList(943, 944, 945, 946, 947, 948);
        List<Integer> existingIds = new ArrayList<>();
        adminMenuService.listByIds(menuIds).forEach(menu -> existingIds.add(menu.getMenuId()));
        if (existingIds.size() == menuIds.size()) {
            return;
        }

        List<AdminMenu> menus = new ArrayList<>();
        addIfMissing(menus, existingIds, new AdminMenu().setMenuId(943).setParentId(1).setMenuName("订单管理").setRealm("order").setMenuType(1).setSort(11).setStatus(1));
        addIfMissing(menus, existingIds, new AdminMenu().setMenuId(944).setParentId(943).setMenuName("新建").setRealm("save").setRealmUrl("/crmOrder/add").setMenuType(3).setSort(1).setStatus(1));
        addIfMissing(menus, existingIds, new AdminMenu().setMenuId(945).setParentId(943).setMenuName("编辑").setRealm("update").setRealmUrl("/crmOrder/update").setMenuType(3).setSort(1).setStatus(1));
        addIfMissing(menus, existingIds, new AdminMenu().setMenuId(946).setParentId(943).setMenuName("查看列表").setRealm("index").setRealmUrl("/crmOrder/queryPageList").setMenuType(3).setSort(1).setStatus(1));
        addIfMissing(menus, existingIds, new AdminMenu().setMenuId(947).setParentId(943).setMenuName("查看详情").setRealm("read").setRealmUrl("/crmOrder/queryById/*").setMenuType(3).setSort(1).setStatus(1));
        addIfMissing(menus, existingIds, new AdminMenu().setMenuId(948).setParentId(943).setMenuName("删除").setRealm("delete").setRealmUrl("/crmOrder/deleteByIds").setMenuType(3).setSort(1).setStatus(1));
        if (!menus.isEmpty()) {
            adminMenuService.saveBatch(menus);
        }
    }

    private void addIfMissing(List<AdminMenu> menus, List<Integer> existingIds, AdminMenu menu) {
        if (!existingIds.contains(menu.getMenuId())) {
            menus.add(menu);
        }
    }
}
