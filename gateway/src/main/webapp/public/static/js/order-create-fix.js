(function() {
  var installed = false;
  var AUTO_CALC_FIELDS = {
    quoteAmount: true,
    purchaseCost: true,
    logisticsCost: true,
    profitAmount: true,
    profitRate: true
  };
  var HIDDEN_SYSTEM_FIELDS = {
    createTime: true,
    createUserName: true,
    updateTime: true,
    ownerUserName: true,
    ownerDeptName: true,
    create_time: true,
    create_user_name: true,
    update_time: true,
    owner_user_name: true,
    owner_dept_name: true
  };

  function getCookie(name) {
    var encodedName = encodeURIComponent(name) + "=";
    var cookieText = document.cookie || "";
    var cookieList = cookieText.split("; ");
    for (var i = 0; i < cookieList.length; i += 1) {
      if (cookieList[i].indexOf(encodedName) === 0) {
        return decodeURIComponent(cookieList[i].slice(encodedName.length));
      }
    }
    return "";
  }

  function getStorageValue(storage, key) {
    try {
      return storage && typeof storage.getItem === "function" ? storage.getItem(key) || "" : "";
    } catch (e) {
      return "";
    }
  }

  function getAdminToken() {
    return getCookie("Admin-Token") ||
      getStorageValue(window.localStorage, "Admin-Token") ||
      getStorageValue(window.sessionStorage, "Admin-Token");
  }

  function buildHeaders() {
    var headers = {
      "Content-Type": "application/json;charset=UTF-8",
      "Accept": "application/json, text/plain, */*"
    };
    var token = getAdminToken();
    if (token) {
      headers["Admin-Token"] = token;
    }
    return headers;
  }

  function toRows(data) {
    if (!Array.isArray(data)) {
      return [];
    }
    return data.map(function(item) {
      if (Array.isArray(item)) {
        return item;
      }
      return item ? [item] : [];
    }).filter(function(row) {
      return row.length > 0;
    });
  }

  function clone(value) {
    if (value === null || value === undefined) {
      return value;
    }
    return JSON.parse(JSON.stringify(value));
  }

  function installPatch() {
    if (installed || !window.app || !window.app.constructor || typeof window.app.constructor.mixin !== "function") {
      return false;
    }

    installed = true;
    window.__orderCreatePatchVersion = "2026-04-20";

    window.app.constructor.mixin({
      beforeCreate: function() {
        if (!this.$options || this.$options.name !== "OrderCreate" || this.__orderCreatePatched__) {
          return;
        }

        this.__orderCreatePatched__ = true;

        this.getField = function() {
          var vm = this;
          var isUpdate = vm.action && vm.action.type === "update";
          var url = isUpdate ? "/crmOrder/field/" + vm.action.id : "/crmOrder/field";
          var payload = isUpdate ? { id: vm.action.id } : {};

          vm.loading = true;

          fetch(url, {
            method: "POST",
            credentials: "include",
            headers: buildHeaders(),
            body: JSON.stringify(payload)
          }).then(function(response) {
            return response.json();
          }).then(function(result) {
            if (!result || result.code !== 0) {
              throw result || new Error("empty response");
            }

            var rows = toRows(result.data);
            var assistIds = vm.getFormAssistIds(rows);
            var baseFields = [];
            var fieldList = [];
            var fieldForm = {};
            var fieldRules = {};

            rows.forEach(function(row) {
              var parsedRow = [];

              row.forEach(function(field) {
                if (!field || !field.fieldName || !field.formType || field.fieldName === "product") {
                  return;
                }

                var item = vm.getFormItemDefaultProperty(field);
                var canEdit = vm.getItemIsCanEdit(field, vm.action.type);

                item.show = assistIds.indexOf(field.formAssistId) === -1;
                if (HIDDEN_SYSTEM_FIELDS[item.field] || HIDDEN_SYSTEM_FIELDS[field.fieldName]) {
                  item.show = false;
                }

                if (item.show && canEdit) {
                  if (field.autoGeneNumber == 1) {
                    item.placeholder = "根据编号规则自动生成，支持手动输入";
                    var copyField = clone(field);
                    copyField.isNull = 0;
                    fieldRules[item.field] = vm.getRules(copyField);
                  } else {
                    fieldRules[item.field] = vm.getRules(field);
                  }
                }

                if (typeof vm.getItemRadio === "function") {
                  vm.getItemRadio(field, item);
                }

                item.disabled = !canEdit || !!AUTO_CALC_FIELDS[item.field];

                if (item.show) {
                  fieldForm[item.field] = vm.getItemValue(field, vm.action.data, vm.action.type);
                }

                parsedRow.push(item);
                baseFields.push(field);
              });

              if (parsedRow.length > 0) {
                fieldList.push(parsedRow);
              }
            });

            var relationFields = vm.getRelationFields();
            relationFields.forEach(function(field) {
              fieldForm[field.field] = vm.normalizeRelationList(vm.getRelationValue(field.formType), field.formType);
              fieldRules[field.field] = field.rules || [];
            });

            var productField = vm.getProductField();
            fieldForm[productField.field] = vm.getProductValue();
            fieldRules[productField.field] = productField.rules;

            fieldList.push(relationFields);
            fieldList.push([productField]);

            vm.baseFields = baseFields;
            vm.fieldList = fieldList;
            vm.fieldForm = fieldForm;
            vm.fieldRules = fieldRules;
            vm.loading = false;
          }).catch(function(error) {
            console.error("order create init error", error);
            vm.loading = false;
            if (vm.$message && typeof vm.$message.error === "function") {
              vm.$message.error(error && error.msg ? error.msg : "订单表单初始化失败，请刷新后重试");
            }
          });
        };
      }
    });

    return true;
  }

  if (installPatch()) {
    return;
  }

  var retryCount = 0;
  var timer = setInterval(function() {
    retryCount += 1;
    if (installPatch() || retryCount >= 100) {
      clearInterval(timer);
    }
  }, 200);
})();
