"use strict";


angular.module("synergy", ["ui.codemirror",
    "infinite-scroll",
    "ui.select2",
    "ngCookies",
    "ngProgress",
    "ui.bootstrap",
    "synergy.http",
    "synergy.test",
    "synergy.core",
    "synergy.controllers",
    "synergy.directives",
    "synergy.models",
    "synergy.utils",
    "synergy.handlers",
    "synergy.filters"])
        .config(function ($provide, $routeProvider, $httpProvider) {

            $provide.factory("MyHttpInterceptor", ["$q", "$rootScope", "SynergyApp", "$injector", "$cookieStore", "sessionService",
                function ($q, $rootScope, SynergyApp, $injector, $cookieStore, sessionService) { // TODO in case of AngularJS update, this is likely obsolete

                    var IGNORE_URLS = ["../../server/api/login.php", "../../server/api/login.php?&return=1"];
                    var refreshPromise = null;
                    function SessionRenewal() {
                        this.originalResponse = null;
                        this.intervalId = -1;
                        this.TOKEN_LENGTH = 32;
                        this.token = null;
                        this.counter = 0;
                    }

                    SessionRenewal.prototype.getToken = function () {
                        var text = "";
                        var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
                        for (var i = 0; i < this.TOKEN_LENGTH; i++) {
                            text += possible.charAt(Math.floor(Math.random() * possible.length));
                        }
                        return text + new Date().getTime();
                    };
                    SessionRenewal.prototype.reset = function () {
                        window.clearInterval(this.intervalId);
                        this.counter = 0;
                        this.token = null;
                        this.intervalId = -1;
                        this.originalResponse = null;
                    };
                    SessionRenewal.prototype.checkForNewLogin = function () {
                        this.counter++;
                        var self = this;

                        $injector.get("$http").get(SynergyApp.getApp().server.buildURL("refresh", {"token": self.token})).success(function (result) {
                            window.clearInterval(self.intervalId);
                            self.reset();
                            refreshPromise.resolve(self.originalResponse);
                        }).error(function (data, status, headers, config) {
                            if (self.counter > 100) {
                                self.reset();
                                refreshPromise.reject(self.originalResponse);
                            }
                        });
                    };
                    SessionRenewal.prototype.getRedirectUrl = function () {
                        var url = window.location.href.substring(0, window.location.href.indexOf("#"));
                        url = url.endsWith(".html") ? url.substring(0, url.lastIndexOf("/") + 1) : url;
                        this.token = this.getToken();
                        url += "login.html?token=" + this.token;
                        if (SynergyApp.getApp().useSSO) {
                            var base = SynergyApp.getApp().getLoginRedirectUrl(SynergyApp.getApp().ssoLoginUrl, url);
                            return base.substring(0, base.indexOf("?revalidate=1"));
                        } else {
                            // todo fix, std login page is synergy/client/app/#/login, which is not visible for server though...
                            throw new Error("Not implemented");
                        }
                    };
                    SessionRenewal.prototype.init = function (response) {
                        refreshPromise = $q.defer();

                        this.originalResponse = response;
                        $("#myModalLabel").text("Please login");
                        $("#modal-body").html("Your session has expired. <br/><a href='" + this.getRedirectUrl() + "' target='_blank'>Click to login</a>");
                        if (!$("#myModal").hasClass("in")) {
                            $("#myModal").modal("toggle");
                        }
                        var self = this;
                        this.intervalId = window.setInterval(function () {
                            self.checkForNewLogin();
                        }, 2000);

                        return refreshPromise.promise;
                    };

                    SessionRenewal.prototype.displayDialog = function (isVisible) {
                        if ($("#myModal").hasClass("in")) {
                            $("#myModal").modal("toggle");
                        }
                    };

                    SessionRenewal.prototype.renew = function (response) {
                        var self = this;
                        return this.init(response)
                                .then(function () {
                                    return $injector.get("sessionHttp").infoConditionalPromise(SynergyApp.getApp().server.buildURL("session"));
                                })
                                .then(function (data) {
                                    SynergyApp.getApp().session.setSession(data.data);
                                    sessionService.setSession(SynergyApp.getApp().session);
                                    $cookieStore.put("session", ({firstName: data.firstName, lastName: data.lastName, username: data.username, role: data.role, token: data.token, created: 1000 * parseInt(data.created, 10), session_id: data.session_id}));
                                    $rootScope.$broadcast("refreshRole");
                                    self.displayDialog(false);
                                    var $h = $injector.get("$http");
                                    return $h(response.config);
                                }, function () {
                                    $cookieStore.remove("session");
                                    sessionService.clearSession();
                                    SynergyApp.getApp().session.clearSession();
                                    $rootScope.$broadcast("hideUserMenu", false);
                                    $rootScope.$broadcast("refreshRole");
                                    self.displayDialog(false);
                                    return $q.reject(response);
                                });
                    };

                    var sessionRenewal = new SessionRenewal();

                    return function (promise) {
                        return promise.then(function (response) {
                            $rootScope.$broadcast("busyMode", false);
                            window.document.body.style.cursor = "default";
                            return response;
                        }, function (response) {
                            $rootScope.$broadcast("busyMode", false);
                            window.document.body.style.cursor = "default";
                            if (parseInt(response.status, 10) === 0) {
                                $rootScope.$broadcast("possibleTimeout", false);
                            }
                            if (parseInt(response.status, 10) === 401 && IGNORE_URLS.indexOf(response.config.url) < 0) { // expired session
                                return sessionRenewal.renew(response);
                            } else {
                                return $q.reject(response);
                            }
                        });
                    };
                }]);

            ($httpProvider.interceptors) ? $httpProvider.interceptors.push("MyHttpInterceptor") : $httpProvider.responseInterceptors.push("MyHttpInterceptor");

            $provide.factory("utils", function () {
                var u = {};
                u.escape = function (s) {
                    return encodeURIComponent(s);
                };
                return u;
            });

            $provide.factory("issue", function ($http) {
                return {
                    getIssue: function (id, $scope) { // not elegant way, $scope should be injected not from function call
                        $http.get($scope.SYNERGY.server.buildURL("issue", {"id": id})).success(function (data) {
                            return data;
                        }).error(function (data) {
                            window.console.log("Issue " + id + " not found: " + data);
                        });
                    }
                };
            });

            // PUBLIC ROUTING
            $routeProvider.when("/specifications/:id", {templateUrl: "partials/public/view/specpool.html?v=1521293430023", controller: "SpecPoolCtrl"});
            $routeProvider.when("/statistics/:id", {templateUrl: "partials/public/view/statistics.html?v=1521293430023", controller: "StatisticsCtrl"});
            $routeProvider.when("/statistics/:id/archive", {templateUrl: "partials/public/view/statistics.html?v=1521293430023", controller: "StatisticsCtrl"});
            $routeProvider.when("/search/:search", {templateUrl: "partials/public/view/search.html?v=1521293430023", controller: "SearchCtrl"});
            $routeProvider.when("/specifications", {templateUrl: "partials/public/view/specpool.html?v=1521293430023", controller: "SpecPoolCtrl"});
            $routeProvider.when("/specification/:id/create", {templateUrl: "partials/public/create/specification.html?v=1521293430023", controller: "SpecificationCtrl"});
            $routeProvider.when("/specification/:id/edit", {templateUrl: "partials/public/edit/specification.html?v=1521293430023", controller: "SpecificationCtrl"});
            $routeProvider.when("/specification/:id/v/1", {templateUrl: "partials/public/view/specification_view_1.html?v=1521293430023", controller: "SpecificationCtrl"});
            $routeProvider.when("/specification/:id/v/2", {templateUrl: "partials/public/view/specification_view_2.html?v=1521293430023", controller: "SpecificationCtrl"});
            $routeProvider.when("/specification/:id/v/2/:label", {templateUrl: "partials/public/view/specification_view_2.html?v=1521293430023", controller: "SpecificationCtrl"});
            $routeProvider.when("/title/:simpleName/:simpleVersion", {templateUrl: "partials/public/view/specification_view_2.html?v=1521293430023", controller: "SpecificationCtrl"});
            $routeProvider.when("/title/:simpleName/", {redirectTo: "/title/:simpleName/latest"});
            $routeProvider.when("/specification/:id", {redirectTo: "/specification/:id/v/2"});
            $routeProvider.when("/suite/:id/:specification/:version/create", {templateUrl: "partials/public/create/suite.html?v=1521293430023", controller: "SuiteCtrl"});
            $routeProvider.when("/suite/:id/edit", {templateUrl: "partials/public/edit/suite.html?v=1521293430023", controller: "SuiteCtrl"});
            $routeProvider.when("/suite/:id/v/1", {templateUrl: "partials/public/view/suite.html?v=1521293430023", controller: "SuiteCtrl"});
            $routeProvider.when("/suite/:id", {redirectTo: "/suite/:id/v/1"});
            $routeProvider.when("/case/:id/suite/:parent/edit", {templateUrl: "partials/public/edit/case.html?v=1521293430023", controller: "CaseCtrl"});
            $routeProvider.when("/case/:id/suite/:parent/create", {templateUrl: "partials/public/create/case.html?v=1521293430023", controller: "CaseCtrl"});
            $routeProvider.when("/case/:id/suite/:parent/v/1", {templateUrl: "partials/public/view/case.html?v=1521293430023", controller: "CaseCtrl"});
            $routeProvider.when("/assignment_comments/:id", {templateUrl: "partials/public/view/assignment_comments.html?v=1521293430023", controller: "AssignmentCommentsCtrl"});
            $routeProvider.when("/case/:id/suite/:parent", {redirectTo: "/case/:id/suite/:parent/v/1"});
            $routeProvider.when("/case/:id", {redirectTo: "/case/:id/suite/-1/v/1"});
            $routeProvider.when("/run/:id/v/1", {templateUrl: "partials/public/view/run_view_1.html?v=1521293430023", controller: "RunCtrl"});
            $routeProvider.when("/run/:id/coverage", {templateUrl: "partials/public/view/run_coverage.html?v=1521293430023", controller: "RunCoverageCtrl"});
            $routeProvider.when("/run/:id/v/2", {templateUrl: "partials/public/view/run_view_2.html?v=1521293430023", controller: "RunCtrlUser"});
            $routeProvider.when("/run/:id/v/3", {templateUrl: "partials/public/view/run_view_3.html?v=1521293430023", controller: "RunCtrlCase"});
            $routeProvider.when("/run/:id", {redirectTo: "/run/:id/v/2"});
            $routeProvider.when("/runs/page/:page", {templateUrl: "partials/public/view/runs.html?v=1521293430023", controller: "RunsCtrl"});
            $routeProvider.when("/runs", {redirectTo: "/runs/page/1"});
            $routeProvider.when("/user", {templateUrl: "partials/public/view/profile.html?v=1521293430023", controller: "ProfileCtrl"});
            $routeProvider.when("/user/:user", {templateUrl: "partials/public/view/profile.html?v=1521293430023", controller: "ProfileCtrl"});
            $routeProvider.when("/label/:label/page/:page", {templateUrl: "partials/public/view/label.html?v=1521293430023", controller: "LabelFilterCtrl"});
            $routeProvider.when("/label/:label", {redirectTo: "/label/:label/page/1"});
            $routeProvider.when("/tribe/:id/edit", {templateUrl: "partials/public/edit/tribe.html?v=1521293430023", controller: "TribeCtrl"});
            $routeProvider.when("/tribe/:id/view", {templateUrl: "partials/public/view/tribe.html?v=1521293430023", controller: "TribeCtrl"});
            $routeProvider.when("/tribes", {templateUrl: "partials/public/view/tribes.html?v=1521293430023", controller: "TribesCtrl"});
            $routeProvider.when("/calendar", {templateUrl: "partials/public/view/calendar.html?v=1521293430023", controller: "CalendarCtrl"});
            $routeProvider.when("/tribe/:id", {redirectTo: "/tribe/:id/view"});
            $routeProvider.when("/", {templateUrl: "partials/public/view/home.html?v=1521293430023", controller: "HomeCtrl"});
            $routeProvider.when("/assignment/:id/v/:mode", {templateUrl: "partials/public/view/assignment.html?v=1521293430023", controller: "AssignmentCtrl"});
            $routeProvider.when("/assignment/create/run/:id", {templateUrl: "partials/public/create/assignment.html?v=1521293430023", controller: "AssignmentVolunteerCtrl"});
            $routeProvider.when("/assignment/create/tribe/run/:id", {templateUrl: "partials/public/create/assignment_tribe.html?v=1521293430023", controller: "AssignmentTribeCtrl"});
            $routeProvider.when("/about", {templateUrl: "partials/public/view/about.html?v=1521293430023", controller: "AboutCtrl"});
            $routeProvider.when("/revisions/:id", {templateUrl: "partials/public/view/revisions.html?v=1521293430023", controller: "RevisionCtrl"});
            $routeProvider.when("/review/:id/view", {templateUrl: "partials/public/view/review.html?v=1521293430023", controller: "ReviewCtrl"});
            $routeProvider.when("/review/:id/:action", {templateUrl: "partials/public/edit/review.html?v=1521293430023", controller: "ReviewCtrl"});
            $routeProvider.when("/register", {templateUrl: "partials/public/view/register.html?v=1521293430023", controller: "RegisterCtrl"});
            $routeProvider.when("/login", {templateUrl: "partials/public/view/login.html?v=1521293430023", controller: "LoginCtrl"});
            $routeProvider.when("/recover", {templateUrl: "partials/public/view/recover.html?v=1521293430023", controller: "RecoverCtrl"});

            // ADMINSTRATION ROUTING
            $routeProvider.when("/administration", {templateUrl: "partials/admin/view/home.html?v=1521293430023", controller: "AdminHomeCtrl"});
            $routeProvider.when("/administration/versions", {templateUrl: "partials/admin/view/versions.html?v=1521293430023", controller: "AdminVersionCtrl"});
            $routeProvider.when("/administration/runs/page/:page", {templateUrl: "partials/admin/view/runs.html?v=1521293430023", controller: "AdminRunsCtrl"});
            $routeProvider.when("/administration/run/-1/create", {templateUrl: "partials/admin/create/run.html?v=1521293430023", controller: "AdminRunCtrl"});
            $routeProvider.when("/administration/assignment/create/run/:id", {templateUrl: "partials/admin/create/assignment.html?v=1521293430023", controller: "AdminAssignmentCtrl"});
            $routeProvider.when("/administration/assignment/creatematrix/run/:id", {templateUrl: "partials/admin/create/matrix_assignment.html?v=1521293430023", controller: "AdminMatrixAssignmentCtrl"});
            $routeProvider.when("/administration/run/:id/edit", {templateUrl: "partials/admin/edit/run.html?v=1521293430023", controller: "AdminRunCtrl"});
            $routeProvider.when("/administration/runs", {redirectTo: "/administration/runs/page/1"});
            $routeProvider.when("/administration/tribes/create", {templateUrl: "partials/admin/create/tribe.html?v=1521293430023", controller: "AdminTribesCtrl"});
            $routeProvider.when("/administration/tribes", {templateUrl: "partials/admin/view/tribes.html?v=1521293430023", controller: "AdminTribesCtrl"});
            $routeProvider.when("/administration/platforms", {templateUrl: "partials/admin/view/platforms.html?v=1521293430023", controller: "AdminPlatformsCtrl"});
            $routeProvider.when("/administration/users/page/:page", {templateUrl: "partials/admin/view/users.html?v=1521293430023", controller: "AdminUsersCtrl"});
            $routeProvider.when("/administration/user/:username/edit", {templateUrl: "partials/admin/edit/user.html?v=1521293430023", controller: "AdminUserCtrl"});
            $routeProvider.when("/administration/user/:username/create", {templateUrl: "partials/admin/create/user.html?v=1521293430023", controller: "AdminUserCtrl"});
            $routeProvider.when("/administration/users", {redirectTo: "/administration/users/page/1"});
            $routeProvider.when("/administration/setting", {templateUrl: "partials/admin/view/settings.html?v=1521293430023", controller: "AdminSettingCtrl"});
            $routeProvider.when("/administration/log", {templateUrl: "partials/admin/view/log.html?v=1521293430023", controller: "AdminLogCtrl"});
            $routeProvider.when("/administration/database", {templateUrl: "partials/admin/view/database.html?v=1521293430023", controller: "AdminDatabaseCtrl"});
            $routeProvider.when("/administration/reviews", {templateUrl: "partials/admin/view/reviews.html?v=1521293430023", controller: "AdminReviewsCtrl"});
            $routeProvider.when("/administration/projects", {templateUrl: "partials/admin/view/projects.html?v=1521293430023", controller: "AdminProjectsCtrl"});
            $routeProvider.when("/administration/project/:id/edit", {templateUrl: "partials/admin/edit/project.html?v=1521293430023", controller: "AdminProjectCtrl"});
            $routeProvider.otherwise({redirectTo: "/"});

        }).run(["$rootScope", "$injector", "sessionService", function ($rootScope, $injector, sessionService) {
        $injector.get("$http").defaults.transformRequest = function (data, headersGetter) {
            var _t = sessionService.getToken();
            if (_t) {
                headersGetter()["Synergy-Authorization"] = _t;
            }
            return data;
        };
        /*
         Receive emitted message and broadcast it.
         Event names must be distinct or browser will blow up!
         */
        $rootScope.$on("handleEmit", function (event, args) {
            $rootScope.$broadcast("handleBroadcast", args);
        });
    }]);