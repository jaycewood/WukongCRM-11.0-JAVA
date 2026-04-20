package com.kakarote.crm.controller;

import cn.hutool.core.util.StrUtil;
import com.kakarote.core.common.FieldEnum;
import com.kakarote.core.common.R;
import com.kakarote.core.common.Result;
import com.kakarote.core.common.SubModelType;
import com.kakarote.core.common.log.BehaviorEnum;
import com.kakarote.core.common.log.SysLog;
import com.kakarote.core.common.log.SysLogHandler;
import com.kakarote.core.entity.BasePage;
import com.kakarote.core.feign.crm.entity.SimpleCrmEntity;
import com.kakarote.core.servlet.ApplicationContextHolder;
import com.kakarote.core.servlet.upload.FileEntity;
import com.kakarote.core.utils.UserUtil;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.crm.common.CrmModel;
import com.kakarote.crm.common.log.CrmOrderLog;
import com.kakarote.crm.constant.CrmEnum;
import com.kakarote.crm.entity.BO.CrmOrderRelationPageBO;
import com.kakarote.crm.entity.BO.CrmOrderSaveBO;
import com.kakarote.crm.entity.BO.CrmSearchBO;
import com.kakarote.crm.entity.BO.UploadExcelBO;
import com.kakarote.crm.entity.PO.CrmOrderProduct;
import com.kakarote.crm.entity.VO.CrmModelFiledVO;
import com.kakarote.crm.service.CrmUploadExcelService;
import com.kakarote.crm.service.ICrmOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/crmOrder")
@Api(tags = "订单模块")
@SysLog(subModel = SubModelType.CRM_ORDER, logClass = CrmOrderLog.class)
public class CrmOrderController {

    @Autowired
    private ICrmOrderService crmOrderService;

    @PostMapping("/queryPageList")
    @ApiOperation("查询订单列表")
    public Result<BasePage<Map<String, Object>>> queryPageList(@RequestBody CrmSearchBO search) {
        search.setPageType(1);
        return R.ok(crmOrderService.queryPageList(search));
    }

    @PostMapping("/queryPageListByRelation")
    @ApiOperation("按关联对象查询订单列表")
    public Result<BasePage<Map<String, Object>>> queryPageListByRelation(@RequestBody CrmOrderRelationPageBO search) {
        search.setPageType(1);
        return R.ok(crmOrderService.queryPageListByRelation(search));
    }

    @PostMapping("/querySimpleEntity")
    @ApiOperation("查询简单订单对象")
    public Result<List<SimpleCrmEntity>> querySimpleEntity(@RequestBody List<Integer> ids) {
        return R.ok(crmOrderService.querySimpleEntity(ids));
    }

    @PostMapping("/downloadExcel")
    @ApiOperation("下载导入模板")
    public void downloadExcel(HttpServletResponse response) throws IOException {
        crmOrderService.downloadExcel(response);
    }

    @PostMapping("/allExportExcel")
    @ApiOperation("全部导出")
    @SysLogHandler(behavior = BehaviorEnum.EXCEL_EXPORT, object = "订单导出", detail = "全部导出")
    public void allExportExcel(@RequestBody CrmSearchBO search, HttpServletResponse response) {
        search.setPageType(0);
        crmOrderService.exportExcel(response, search);
    }

    @PostMapping("/batchExportExcel")
    @ApiOperation("选中导出")
    @SysLogHandler(behavior = BehaviorEnum.EXCEL_EXPORT, object = "订单导出", detail = "选中导出")
    public void batchExportExcel(@RequestBody @ApiParam("订单ID列表") List<Integer> ids, HttpServletResponse response) {
        CrmSearchBO search = new CrmSearchBO();
        search.setPageType(0);
        search.setLabel(CrmEnum.ORDER.getType());
        CrmSearchBO.Search entity = new CrmSearchBO.Search();
        entity.setFormType(FieldEnum.TEXT.getFormType());
        entity.setSearchEnum(CrmSearchBO.FieldSearchEnum.ID);
        entity.setValues(ids.stream().map(Object::toString).collect(Collectors.toList()));
        search.getSearchList().add(entity);
        crmOrderService.exportExcel(response, search);
    }

    @PostMapping("/uploadExcel")
    @ApiOperation("导入订单")
    @SysLogHandler(behavior = BehaviorEnum.EXCEL_IMPORT, object = "导入订单", detail = "导入订单")
    public Result<Long> uploadExcel(@RequestParam("file") MultipartFile file,
                                    @RequestParam("repeatHandling") Integer repeatHandling) {
        UploadExcelBO uploadExcelBO = new UploadExcelBO();
        uploadExcelBO.setUserInfo(UserUtil.getUser());
        uploadExcelBO.setCrmEnum(CrmEnum.ORDER);
        uploadExcelBO.setPoolId(null);
        uploadExcelBO.setRepeatHandling(repeatHandling);
        Long messageId = ApplicationContextHolder.getBean(CrmUploadExcelService.class).uploadExcel(file, uploadExcelBO);
        return R.ok(messageId);
    }

    @PostMapping("/add")
    @ApiOperation("新增订单")
    @SysLogHandler(behavior = BehaviorEnum.SAVE, object = "#crmModel.entity[orderNumber]")
    public Result add(@RequestBody CrmOrderSaveBO crmModel) {
        crmOrderService.addOrUpdate(crmModel);
        return R.ok();
    }

    @PostMapping("/update")
    @ApiOperation("修改订单")
    @SysLogHandler(behavior = BehaviorEnum.UPDATE)
    public Result update(@RequestBody CrmOrderSaveBO crmModel) {
        crmOrderService.addOrUpdate(crmModel);
        return R.ok();
    }

    @PostMapping("/queryById/{orderId}")
    @ApiOperation("根据ID查询订单")
    public Result<CrmModel> queryById(@PathVariable("orderId") @ApiParam("订单ID") Integer orderId) {
        return R.ok(crmOrderService.queryById(orderId));
    }

    @PostMapping("/field")
    @ApiOperation("查询新增所需字段")
    public Result<List> queryOrderField(@RequestParam(value = "type", required = false) String type) {
        if (StrUtil.isNotEmpty(type)) {
            return R.ok(crmOrderService.queryField(null));
        }
        return R.ok(crmOrderService.queryFormPositionField(null));
    }

    @PostMapping("/field/{id}")
    @ApiOperation("查询修改数据所需信息")
    public Result<List> queryField(@PathVariable("id") @ApiParam("订单ID") Integer id,
                                   @RequestParam(value = "type", required = false) String type) {
        if (StrUtil.isNotEmpty(type)) {
            return R.ok(crmOrderService.queryField(id));
        }
        return R.ok(crmOrderService.queryFormPositionField(id));
    }

    @PostMapping("/information/{id}")
    @ApiOperation("查询详情页信息")
    public Result<List<CrmModelFiledVO>> information(@PathVariable("id") @ApiParam("订单ID") Integer id) {
        return R.ok(crmOrderService.information(id));
    }

    @PostMapping("/queryFileList")
    @ApiOperation("查询附件列表")
    public Result<List<FileEntity>> queryFileList(@RequestParam("id") @ApiParam("订单ID") Integer id) {
        return R.ok(crmOrderService.queryFileList(id));
    }

    @PostMapping("/queryProductList/{orderId}")
    @ApiOperation("查询订单产品明细")
    public Result<List<CrmOrderProduct>> queryProductList(@PathVariable("orderId") @ApiParam("订单ID") Integer orderId) {
        return R.ok(crmOrderService.queryProductList(orderId));
    }

    @PostMapping("/queryQuotationList/{orderId}")
    @ApiOperation("查询订单报价表单")
    public Result<List<JSONObject>> queryQuotationList(@PathVariable("orderId") @ApiParam("订单ID") Integer orderId) {
        return R.ok(crmOrderService.queryQuotationList(orderId));
    }

    @PostMapping("/queryProfitList/{orderId}")
    @ApiOperation("查询订单利润表单")
    public Result<List<JSONObject>> queryProfitList(@PathVariable("orderId") @ApiParam("订单ID") Integer orderId) {
        return R.ok(crmOrderService.queryProfitList(orderId));
    }

    @PostMapping("/deleteByIds")
    @ApiOperation("删除订单")
    @SysLogHandler(behavior = BehaviorEnum.DELETE)
    public Result deleteByIds(@RequestBody List<Integer> ids) {
        crmOrderService.deleteByIds(ids);
        return R.ok();
    }
}
