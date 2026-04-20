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
      if (!storage || typeof storage.getItem !== "function") {
        return "";
      }
      var rawValue = storage.getItem(key);
      if (!rawValue) {
        return "";
      }
      try {
        var parsedValue = JSON.parse(rawValue);
        if (parsedValue && typeof parsedValue === "object" && parsedValue.data !== undefined) {
          return parsedValue.data;
        }
        return parsedValue;
      } catch (e) {
        return rawValue;
      }
    } catch (e) {
      return "";
    }
  }

  function getAdminToken() {
    var token = getCookie("Admin-Token");
    if (token) {
      window.__orderCreateTokenSource = "cookie";
      return token;
    }

    token = getStorageValue(window.localStorage, "Admin-Token");
    if (token) {
      window.__orderCreateTokenSource = "localStorage";
      return token;
    }

    token = getStorageValue(window.sessionStorage, "Admin-Token");
    if (token) {
      window.__orderCreateTokenSource = "sessionStorage";
      return token;
    }

    window.__orderCreateTokenSource = "empty";
    return "";
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

  function isOrderCreateComponent(vm) {
    if (!vm || vm.__orderCreatePatched__ || !vm.$options) {
      return false;
    }

    var options = vm.$options || {};
    var methods = options.methods || {};

    if (options.name === "OrderCreate") {
      return true;
    }

    if (typeof methods.getRelationFields === "function" &&
      typeof methods.getProductField === "function" &&
      typeof methods.getProductValue === "function" &&
      typeof methods.appendRelationParams === "function" &&
      typeof methods.getRelationIds === "function") {
      return true;
    }

    if (options.props && options.props.action &&
      typeof methods.getField === "function" &&
      typeof methods.saveClick === "function" &&
      typeof methods.submiteParams === "function" &&
      typeof methods.otherChange === "function") {
      return true;
    }

    return false;
  }

  function installPatch() {
    if (installed || !window.app || !window.app.constructor || typeof window.app.constructor.mixin !== "function") {
      return false;
    }

    installed = true;
    window.__orderCreatePatchVersion = "2026-04-20-v4";

    window.app.constructor.mixin({
      created: function() {
        if (!isOrderCreateComponent(this)) {
          return;
        }

        this.__orderCreatePatched__ = true;
        window.__orderCreateVm = this;
        window.__orderCreatePatchHit = {
          name: this.$options && this.$options.name || "",
          time: new Date().toISOString()
        };

        this.getField = function() {
          var vm = this;
          if (!vm.action || typeof vm.action !== "object") {
            vm.action = {};
          }
          if (!vm.action.data || typeof vm.action.data !== "object") {
            vm.action.data = {};
          }
          if (!vm.action.type) {
            vm.action.type = "save";
          }

          var action = vm.action;
          var actionData = action.data;
          var isUpdate = action.type === "update";
          var url = isUpdate ? "/crmOrder/field/" + action.id : "/crmOrder/field";
          var payload = isUpdate ? { id: action.id } : {};

          window.__orderCreateNormalizedAction = {
            type: action.type,
            hasData: !!actionData,
            keys: Object.keys(actionData)
          };

          vm.loading = true;

          fetch(url, {
            method: "POST",
            credentials: "include",
            headers: buildHeaders(),
            body: JSON.stringify(payload)
          }).then(function(response) {
            return response.json();
          }).then(function(result) {
            window.__orderCreateLastFieldResult = result;
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
                var canEdit = vm.getItemIsCanEdit(field, action.type);

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
                  fieldForm[item.field] = vm.getItemValue(field, actionData, action.type);
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
            window.__orderCreateLastFieldError = error;
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
