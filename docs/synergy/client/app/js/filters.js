"use strict";

angular.module("synergy.filters", []).filter("assignmentFilter", function () {
    return function (items, sortConfig) {
        if (items) {
            window.console.log(items.length);
            window.console.log(sortConfig);
        }
        return items;
    };
}).filter("addCaseStatus", function () {
    return function (text, opt) {
        var i;
        $.each(opt.scope.allCases, function (index, item) {
            if (item.caseId === opt.item.caseId && item.suiteId === opt.item.suiteId) {
                i = index;
                return false;
            }
        });
        var elem = angular.element("select#" + opt.selectId + " > option[value='" + i + "']");
        elem.attr("class", "case" + opt.item.result);
        return text;
    };
}).filter("stillValidIssues", function () {
    return function (issues) {
        if (typeof issues === "undefined") {
            return issues;
        }
        var filtered = [];
        for (var i = 0, max = issues.length; i < max; i++) {
            if (issues[i].isStillValid === true) {
                filtered.push(issues[i]);
            }
        }
        return filtered;
    };
});