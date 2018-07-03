"use strict";
/* Directives */

angular.module("synergy.directives", []).directive("login", ["$http", function($http) {
        return {
            restrict: "E",
            replace: true,
            templateUrl: "partials/directives/loginBt.html",
            link: function(scope, element, attrs) {

            }
        };
    }]).directive("calendar", ["calendarFct", function(calendarFct) {
        return {
            restrict: "E",
            replace: true,
            templateUrl: "partials/directives/calendar.html",
            link: function(scope, element, attrs) {
                var mainScope = scope;
                calendarFct.getEvents(scope, function(data) {

                    var events = [];
                    for (var i = 0, max = data.length; i < max; i += 1) {
                        events.push({url: "#/run/" + data[i].id + "/v/1", title: data[i].title, start: new Date(data[i].start.substr(0, 4), parseInt(data[i].start.substr(4, 2), 10) - 1, data[i].start.substr(6, 2)), end: new Date(data[i].end.substr(0, 4), parseInt(data[i].end.substr(4, 2), 10) - 1, data[i].end.substr(6, 2))});
                    }

                    mainScope.events = events;
                }, function(data) {
                });
            }
        };
    }]).directive("sylabel", [function() {
        return {
            restrict: "E",
            replace: true,
            scope: {
                "keyword": "=label"
            },
            template: "<span><a href=\"#/label/{{keyword}}\" class=\"nostyle\"><span class=\"label label-{{keyword}}\">{{keyword}}</span></a>&nbsp;</span>",
            link: function(scope, element, attrs) {
            }
        };
    }]).directive("syissue", [function() {
        return {
            restrict: "E",
            template: "<span><a data-ng-hide=\"issue.resolution == 'NEW' || issue.resolution == 'STARTED' || issue.resolution == 'REOPENED' || issue.resolution == ''\" title=\"[{{issue.resolution}}] {{issue.title}}\" href=\"{{izurl}}\" class=\"nostyle\">   <span class=\"label\">{{issue.bugId}}</span></a><a data-ng-show=\"issue.resolution == 'NEW' || issue.resolution == 'STARTED' || issue.resolution == 'REOPENED' || issue.resolution == ''\" title=\"[{{issue.resolution}}] {{issue.title}}\" href=\"{{izurl}}\" class=\"nostyle\">   <span class=\"label label-important\">{{issue.bugId}}</span></a></span>",
            replace: true,
            scope: {
                "issue": "=issue",
                "izurl": "=baseurl"
            },
            link: function(scope, element, attrs) {
            }
        };
    }]).directive("syissueDetail", [function() {
        return {
            restrict: "E",
            template: "<div><span><a data-ng-hide=\"issue.resolution == 'NEW' || issue.resolution == 'STARTED' || issue.resolution == 'REOPENED' || issue.resolution == ''\" title=\"[{{issue.resolution}}]\" href=\"{{izurl}}\" class=\"nostyle\">   <span class=\"label\">#{{issue.bugId}}:&nbsp;{{issue.title}}</span></a><a data-ng-show=\"issue.resolution == 'NEW' || issue.resolution == 'STARTED' || issue.resolution == 'REOPENED' || issue.resolution == ''\" title=\"[{{issue.resolution}}]\" href=\"{{izurl}}\" class=\"nostyle\">   <span class=\"label label-important\">#{{issue.bugId}}:&nbsp;{{issue.title}}</span></a></span></div>",
            replace: true,
            scope: {
                "issue": "=issue",
                "izurl": "=baseurl"
            },
            link: function(scope, element, attrs) {
            }
        };
    }]).directive("unbindable", [function() {
        return {
            scope: true
        };
    }]).directive("syspecificationsall", function() {
    return {
        restrict: "EA",
        replace: true,
        scope: {
            "data": "=data",
            "projects": "=projects",
            "defaultProduct": "=defaultProduct"
        },
        controller: function($scope) {
            $scope.encodeURIComponent = function(s) {
                return encodeURIComponent(s);
            };

            $scope.project = "All";
            $scope.projectFilter = function(record) {
                if ($scope.project === "All") {
                    return true;
                }
                for (var i = 0, max = record.projects.length; i < max; i++) {
                    if (record.projects[i].name === $scope.project) {
                        return true;
                    }
                }
                return false;
            };

        },
        template: "<div><span data-ng-hide=\"projects.length ==2\" style=\"font-size:14px\">Filter by project </span><select data-ng-hide=\"projects.length ==2\" data-ng-model=\"project\" data-ng-options=\"p as p for p in projects\"></select><table class=\"table table-condensed table-bordered table-hover\"><thead><tr><th></th><th data-ng-repeat=\"d in data[0].specifications\">{{d.version}}</th></tr></thead><tbody><tr data-ng-repeat=\"d in data|filter:projectFilter\"><td><a href=\"#/title/{{encodeURIComponent(encodeURIComponent(d.simpleName))}}\"> {{d.title}}&nbsp;<small style=\"color: #999\">{{d._project}}</small></a></td><td data-ng-repeat=\"v in d.specifications\"><span><small title=\"Role: {{v.ownerRole}}\" class=\"owner_{{v.ownerRole}}\">{{v.owner || \"\"}}</small></span></td></tr></tbody></table></div>"
    };
}).directive("syspecifications", function() {
    return {
        restrict: "EA",
        replace: true,
        scope: {
            "data": "=data",
            "projects": "=projects",
            "defaultProduct": "=defaultProduct"
        },
        controller: function($scope) {
            $scope.project = "All";

            $scope.projectFilter = function(record) {
                if ($scope.project === "All" || !record.ext.hasOwnProperty("projects")) {
                    return true;
                }
                for (var i = 0, max = record.ext.projects.length; i < max; i++) {
                    if (record.ext.projects[i].name === $scope.project) {
                        return true;
                    }
                }
                return false;
            };
        },
        template: "<div><span data-ng-hide=\"projects.length ==2\" style=\"font-size:14px\">Filter by project </span><select data-ng-hide=\"projects.length ==2\" data-ng-model=\"project\" data-ng-options=\"p as p for p in projects\"></select><table><tr data-ng-repeat=\"spec in data|filter:projectFilter\"><td style=\"vertical-align: central\"><a href=\"#/specification/{{spec.id}}\">&rightarrow;&nbsp;{{spec.title}}&nbsp;<small style=\"color: #999\">{{spec._project}}</small></a>&nbsp;&nbsp;</td></tr></table></div>"
    };
}).directive("onEnter", function() {
    return function(scope, element, attrs) {
        element.bind("keydown keypress", function(event) {
            if (event.which === 13) {
                scope.$apply(function() {
                    scope.$eval(attrs.onEnter);
                });
                event.preventDefault();
            } else {
                if (event.which !== 0 && event.charCode !== 0 && !event.ctrlKey && !event.metaKey && !event.altKey) {
                    if (!scope.$$phase) {
                        scope.$apply(function() {
                            scope.$eval(attrs.onType);
                        });
                    }
                }
            }
        });
    };
}).directive("typeaheads", function($timeout, $location) {
    return {
        restrict: "AEC",
        replace: true,
        scope: {
            items: "=",
            prompt: "@",
            title: "@",
            subtitle: "@",
            model: "=",
            onSelect: "&",
            enter: "&",
            type: "&"
        },
        link: function(scope, elem, attrs) {

            scope.handleSelection = function(selectedItem) {
                scope.model = "";
                scope.selected = true;
                $location.path(selectedItem.link);
            };
            scope.selected = true;

            elem.bind("keydown keypress", function(event) {
                if (event.which === 13) {
                    $timeout(function() {
                        scope.enter();
                    }, 10);
                    scope.$apply(function() {
                        scope.selected = true;
                    });
                } else if (event.which === 27 || event.which === 8) {
                    scope.$apply(function() {
                        scope.selected = true;
                    });
                } else {
                    if (event.which !== 0 && event.charCode !== 0 && !event.ctrlKey && !event.metaKey && !event.altKey) {
                        $timeout(function() {
                            scope.type();
                        }, 1);
                        scope.$apply(function() {
                            scope.selected = false;
                        });
                    }
                }
            });
        },
        template: "<div><div class=\"navbar-search dropdown\"><input type=\"search\" class=\"search-query\" ng-model=\"model\" placeholder=\"{{prompt}}\" ng-keydown=\"selected=!selected\"/><br/><ul class=\"typeahead dropdown-menu\" data-ng-hide=\"selected || items.length < 1\"  id=\"typeahead_search\"><li ng-repeat=\"item in items |filter:model\" ng-click=\"handleSelection(item)\" ><a>{{item.title}}</a></li></ul></div></div>"
    };
});
/* jshint ignore:start */
/* ng-infinite-scroll - v1.0.0 - 2013-02-23 */
// http://binarymuse.github.io/ngInfiniteScroll
angular.module("infinite-scroll", []).directive("infiniteScroll", ["$rootScope", "$window", "$timeout", function(i, n, e) {
        return{link: function(t, l, o) {
                var r, c, f, a;
                return n = angular.element(n), f = 0, null != o.infiniteScrollDistance && t.$watch(o.infiniteScrollDistance, function(i) {
                    return f = parseInt(i, 10)
                }), a = !0, r = !1, null != o.infiniteScrollDisabled && t.$watch(o.infiniteScrollDisabled, function(i) {
                    return a = !i, a && r ? (r = !1, c()) : void 0
                }), c = function() {
                    var e, c, u, d;
                    return d = n.height() + n.scrollTop(), e = l.offset().top + l.height(), c = e - d, u = n.height() * f >= c, u && a ? i.$$phase ? t.$eval(o.infiniteScroll) : t.$apply(o.infiniteScroll) : u ? r = !0 : void 0
                }, n.on("scroll", c), t.$on("$destroy", function() {
                    return n.off("scroll", c)
                }), e(function() {
                    return o.infiniteScrollImmediateCheck ? t.$eval(o.infiniteScrollImmediateCheck) ? c() : void 0 : c()
                }, 0)
            }}
    }]);

/*
 ngProgress 1.0.3 - slim, site-wide progressbar for AngularJS 
 (C) 2013 - Victor Bjelkholm 
 License: MIT 
 Source: https://github.com/VictorBjelkholm/ngProgress 
 Date Compiled: 2013-09-13 
 */
angular.module("ngProgress.provider", ["ngProgress.directive"]).provider("ngProgress", function() {
    "use strict";
    this.autoStyle = !0, this.count = 0, this.height = "2px", this.color = "#0e90d2", this.$get = ["$document", "$window", "$compile", "$rootScope", "$timeout", function(a, b, c, d, e) {
            var f = this.count, g = this.height, h = this.color, i = d, j = a.find("body"), k = c("<ng-progress></ng-progress>")(i);
            j.append(k), i.count = f, void 0 !== g && k.eq(0).children().css("height", g), void 0 !== h && (k.eq(0).children().css("background-color", h), k.eq(0).children().css("color", h));
            var l = 0;
            return{start: function() {
                    this.show();
                    var a = this;
                    l = setInterval(function() {
                        if (isNaN(f))
                            clearInterval(l), f = 0, a.hide();
                        else {
                            var b = 100 - f;
                            f += .15 * Math.pow(1 - Math.sqrt(b), 2), a.updateCount(f)
                        }
                    }, 200)
                }, updateCount: function(a) {
                    i.count = a, i.$$phase || i.$apply()
                }, height: function(a) {
                    return void 0 !== a && (g = a, i.height = g, i.$$phase || i.$apply()), g
                }, color: function(a) {
                    return void 0 !== a && (h = a, i.color = h, i.$$phase || i.$apply()), h
                }, hide: function() {
                    k.children().css("opacity", "0");
                    var a = this;
                    e(function() {
                        k.children().css("width", "0%"), e(function() {
                            a.show()
                        }, 500)
                    }, 500)
                }, show: function() {
                    e(function() {
                        k.children().css("opacity", "1")
                    }, 100)
                }, status: function() {
                    return f
                }, stop: function() {
                    clearInterval(l)
                }, set: function(a) {
                    return this.show(), this.updateCount(a), f = a, clearInterval(l), f
                }, css: function(a) {
                    return k.children().css(a)
                }, reset: function() {
                    return clearInterval(l), f = 0, this.updateCount(f), 0
                }, complete: function() {
                    f = 100, this.updateCount(f);
                    var a = this;
                    return e(function() {
                        a.hide(), e(function() {
                            f = 0, a.updateCount(f)
                        }, 500)
                    }, 1e3), f
                }}
        }], this.setColor = function(a) {
        return void 0 !== a && (this.color = a), this.color
    }, this.setHeight = function(a) {
        return void 0 !== a && (this.height = a), this.height
    }
}), angular.module("ngProgress.directive", []).directive("ngProgress", ["$window", "$rootScope", function(a, b) {
        var c = {replace: !0, restrict: "E", link: function(a, c) {
                b.$watch("count", function(b) {
                    (void 0 !== b || null !== b) && (a.counter = b, c.eq(0).children().css("width", b + "%"))
                }), b.$watch("color", function(b) {
                    (void 0 !== b || null !== b) && (a.color = b, c.eq(0).children().css("background-color", b), c.eq(0).children().css("color", b))
                }), b.$watch("height", function(b) {
                    (void 0 !== b || null !== b) && (a.height = b, c.eq(0).children().css("height", b))
                })
            }, template: '<div id="ngProgress-container"><div id="ngProgress"></div></div>'};
        return c
    }]), angular.module("ngProgress", ["ngProgress.directive", "ngProgress.provider"]);
/* jshint ignore:end*/