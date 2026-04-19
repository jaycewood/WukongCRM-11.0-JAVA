(function() {
  var installed = false;
  var LOCKED_FIELDS = {
    quoteAmount: true,
    purchaseCost: true,
    logisticsCost: true,
    profitAmount: true,
    profitRate: true
  };

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

  function installPatch() {
    if (installed || !window.app || !window.app.constructor || typeof window.app.constructor.mixin !== "function") {
      return false;
    }

    installed = true;

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
            credentials: "same-origin",
            headers: {
              "Content-Type": "application/json;charset=UTF-8"
            },
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
                try {
                  if (!field || !field.fieldName || field.fieldName === "product") {
                    return;
                  }

                  var item = vm.getFormItemDefaultProperty(field);
                  var canEdit = vm.getItemIsCanEdit(field, vm.action.type);

                  item.show = assistIds.indexOf(field.formAssistId) === -1;
                  if (item.show && canEdit) {
                    fieldRules[item.field] = vm.getRules(field);
                  }
                  item.disabled = !canEdit || !!LOCKED_FIELDS[item.field];
                  if (item.show) {
                    fieldForm[item.field] = vm.getItemValue(field, vm.action.data, vm.action.type);
                  }

                  parsedRow.push(item);
                  baseFields.push(field);
                } catch (error) {
                  console.error("order create field parse error", field, error);
                }
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
              vm.$message.error("订单表单初始化失败，请刷新后重试");
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
