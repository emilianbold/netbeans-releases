"use strict";

(function () {
    function SampleSubscriber($scope, data, event) {
//    window.console.log("Received: " + event);
    }
    /**
     * Top level main controller. All other controllers are nested and have access to this $scope.
     * Handles all session related features - log in, log out, update session information in
     * SYNERGY.session and hides/displays login form
     * @param {SessionFct} sessionHttp description
     */
    function SynergyCtrl($scope, $location, sessionHttp, $cookieStore, $timeout, ngProgress, $templateCache, searchHttp, projectsHttp, sessionService, SynergyApp, specificationCache, SynergyUtils, SynergyCore) {
        $scope.SYNERGY = SynergyCore.init();
        SynergyApp.setApp($scope.SYNERGY);
        specificationCache.resetCurrentSpecification();
        $scope.suggestions = [];
        $scope.SYNERGY.publisher.subscribe(function ($scope, data, event) {
            new SampleSubscriber($scope, data, event);
        }, "specListLoaded");
        $scope.location = $location;
        $scope.$on("$routeChangeStart", function (scope, next, current) { // to hide alert box whenever path is changed
            $scope.SYNERGY.logger.print = false;
        });

        $scope.username = "";
        $scope.password = "";
        $scope.searchedItem = "";
        $scope.role = "";
        $scope.cookieChecked = false; // need to check at least once in case PHPSESSID cookie is different in Synergy.cookie
        $scope.isLoggedIn = false;
        $scope.breadcrumbs = [];
        $scope.busyBrand = "";

        /**
         * Shows generic dialog asking user to wait
         */
        $scope.showWaitDialog = function () {
            $scope.SYNERGY.modal.update("Processing data...", "Please wait");
            $scope.SYNERGY.modal.show();
        };

        /**
         * Makes Synergy logo glowing (and turning off)
         * @returns {undefined}
         */
        $scope.toggleBusyBrand = function () {
            $scope.busyBrand = $scope.busyBrand.length > 0 ? "" : "brand_busy";
        };

        $scope.getLocalTime = function (dateString, useShortMonth) {
            return SynergyUtils.UTCToLocal(dateString, useShortMonth);
        };
        $scope.getDate = function (dateString) {
            return SynergyUtils.UTCToDate(dateString);
        };
        $scope.getLocalDateTime = function (dateString) {
            return SynergyUtils.UTCToLocalDateTime(dateString);
        };
        $scope.getUTCTime = function (dateString) {
            return SynergyUtils.localToUTC(dateString);
        };

        /**
         * General method to show & log with level INFO message from HTTP Factory
         * @param {type} data
         * @param {type} status HTTP status     
         */
        $scope.generalHttpFactoryError = function (data, status) {
            $scope.SYNERGY.logger.log("Action failed", data, "INFO", "alert-error");
        };

        /**
         * Turns busy mode on, meaning logo is glowing and displays progress bar at the top of the page
         */
        $scope.busyModeOn = function () {
            $scope.busyBrand = "brand_busy";
            ngProgress.set(0);
            $timeout(function () {
                if (ngProgress.status() < 90) {
                    ngProgress.set(ngProgress.status() + Math.round(Math.random() * (10 - 5 + 1) + 5));
                }
            }, 50);
            window.document.body.style.cursor = "wait";
        };
        /**
         * Logs possible timeout
         */
        $scope.$on("possibleTimeout", function () {
            $scope.SYNERGY.logger.error("Uknown response", "Seems like timeout occurred, please try to reload page", "DEBUG");
        });

        $scope.$on("hideUserMenu", function () {
            $scope.SYNERGY.session.hideUserMenu();
        });
        $scope.$on("refreshRole", function () {
            $scope.role = $scope.SYNERGY.session.role;
        });

        $scope.$on("busyMode", function (event, args) {
            if (args) {
                $scope.busyBrand = "brand_busy";
            } else {
                window.document.body.style.cursor = "default";
                $scope.busyBrand = "";
                ngProgress.complete(100);
            }
        });

        /**
         * Sends only check for user session, if there is none, discards any session information stored in browser
         */
        $scope.init = function (callback) {
            var _c = $cookieStore.get("session"); //user key throws error
            if ($scope.cookieChecked && _c && $scope.SYNERGY.session.cookieIsValid(_c.created) && typeof _c.token !== "undefined") {// && _c.length > 0
                //var session = window.JSON.parse(_c);
                var session = _c;
                $scope.SYNERGY.session.hideLoginForm();
                $scope.SYNERGY.session.showUserMenu(session.username);
                $scope.SYNERGY.session.isLoggedIn = true;
                $scope.SYNERGY.session.username = session.username;
                $scope.SYNERGY.session.role = session.role;
                $scope.SYNERGY.session.lastName = (session.hasOwnProperty("lastName") && session.lastName.length > 0) ? session.lastName : "";
                $scope.SYNERGY.session.firstName = (session.hasOwnProperty("firstName") && session.firstName.length > 0) ? session.firstName : "";
                $scope.role = session.role;
                $scope.SYNERGY.session.session_id = session.session_id;
                $scope.SYNERGY.session.created = session.created;
                $scope.SYNERGY.session.token = session.session_id;
                sessionService.setSession($scope.SYNERGY.session);
                $timeout(callback, 0);
            } else {
                sessionHttp.infoConditional($scope, function (data) {
                    $scope.cookieChecked = true;
                    $scope.SYNERGY.session.hideLoginForm();
                    $scope.SYNERGY.session.showUserMenu(data.username);
                    $scope.SYNERGY.session.isLoggedIn = true;
                    $scope.SYNERGY.session.username = data.username;
                    $scope.SYNERGY.session.lastName = data.lastName;
                    $scope.SYNERGY.session.firstName = data.firstName;
                    $scope.SYNERGY.session.role = data.role;
                    $scope.role = data.role;
                    $scope.SYNERGY.session.session_id = data.session_id;
                    $scope.SYNERGY.session.token = data.session_id;
                    $scope.SYNERGY.session.created = data.created;
                    sessionService.setSession($scope.SYNERGY.session);
                    $cookieStore.put("session", ({firstName: data.firstName, lastName: data.lastName, username: data.username, role: data.role, token: data.token, created: 1000 * parseInt(data.created, 10), session_id: data.session_id}));
                    callback();
                }, function (data) {
                    $cookieStore.remove("session");
                    sessionService.clearSession();
                    $scope.cookieChecked = false;
                    //  $scope.SYNERGY.session.showLoginForm();
                    $scope.SYNERGY.session.hideUserMenu();
                    callback();
                });
            }
        };

        /**
         * Redirects to login page if SSO is not used. If SSO is used, it sends "active" request to server. Server checks SSO session
         * and if session exists and it's valid, returns user information. If session is not valid, server returns HTTP 307 and client redirects to 
         * SSO login page.
         */
        $scope.login = function () {
            if (!$scope.SYNERGY.useSSO) {
                $location.path("login");
            } else {
                sessionHttp.get($scope, function (data) {
                    $scope.SYNERGY.session.isLoggedIn = true;
                    $scope.SYNERGY.session.username = data.username;
                    $scope.SYNERGY.session.role = data.role;
                    $scope.role = data.role;
                    $scope.SYNERGY.session.lastName = data.lastName;
                    $scope.SYNERGY.session.firstName = data.firstName;
                    $scope.SYNERGY.session.created = 1000 * parseInt(data.created, 10);
                    $scope.SYNERGY.session.session_id = data.session_id;
                    $scope.SYNERGY.session.token = data.session_id;
                    $scope.SYNERGY.session.hideLoginForm();
                    $scope.SYNERGY.session.showUserMenu(data.username);
                    sessionService.setSession($scope.SYNERGY.session);
                    $cookieStore.put("session", ({username: data.username, role: data.role, token: data.token, created: 1000 * parseInt(data.created, 10), session_id: data.session_id}));
                    window.location.reload();
                }, function (data, status) {
                    $scope.SYNERGY.logger.log("Action failed", data + ":" + status, "DEBUG", "alert-error");
                    sessionService.clearSession();
                    status = parseInt(status, 10);
                    switch (status) {
                        case 307:
                            window.location.href = $scope.SYNERGY.getLoginRedirectUrl($scope.SYNERGY.ssoLoginUrl, window.location.href);
                            break;
                        case 400:
                            $scope.SYNERGY.logger.log("Login failed", data, "INFO", "alert-error");
                            break;
                        default:
                            $scope.SYNERGY.logger.log("Login failed", "Incorrect credentials, please try again ", "INFO", "alert-error");
                            break;
                    }
                });
            }
        };

        /**
         * Sends logout request to serverpr
         */
        $scope.logout = function () {
            $cookieStore.remove("session");
            $scope.SYNERGY.session.clearSession();
            $scope.role = "";
            sessionService.clearSession();
            sessionHttp.logout($scope, function (data) {
                if ($scope.SYNERGY.useSSO) {
                    window.location.href = $scope.SYNERGY.getLogoutRedirectUrl($scope.SYNERGY.ssoLogoutUrl, (window.location.href));
                } else {
                    window.location.reload();
                }
            }, function (data) {
                $scope.SYNERGY.logger.log("Logout failed", data.toString(), "INFO", "alert-error");
            });
        };

        /**
         * Listens to updateNavbar and highlights received link in top nav bar
         */
        $scope.$on("updateNavbar", function (event, args) {
            try {
                $("ul#navbar li").each(function () {
                    if ($(this).attr("class") === "active") {
                        $(this).attr("class", "");
                    }
                });
            } catch (e) {
            }
            try {
                $("ul#navbar li#" + args.item).attr("class", "active");
            } catch (e) {
            }
        });


        function splitMergeBreadCrumbs(currentTitle, currentTitleIndex, breadCrumbs) {
            var p1 = breadCrumbs.slice(0, currentTitleIndex);
            var p2 = breadCrumbs.slice(currentTitleIndex + 1);
            p2.push(currentTitle);
            return p1.concat(p2);
        }

        /**
         * Updates breadcrumbs menu
         */
        $scope.$on("updateBreadcrumbs", function (event, args) {
            var i = 0;
            if ($scope.breadcrumbs.length === 5) {
                for (i = 0; i < 5; i++) {
                    if (typeof $scope.breadcrumbs[i] !== "undefined" && $scope.breadcrumbs[i].title === args.title) {
                        $scope.breadcrumbs = splitMergeBreadCrumbs(args, i, $scope.breadcrumbs);
                        return;
                    }
                }

                for (i = 0; i < 4; i++) {
                    $scope.breadcrumbs[i] = $scope.breadcrumbs[i + 1];
                }
                $scope.breadcrumbs[4] = args;
            } else {
                for (i = 0; i < 4; i++) {
                    if (typeof $scope.breadcrumbs[i] !== "undefined" && $scope.breadcrumbs[i].title === args.title) {
                        $scope.breadcrumbs = splitMergeBreadCrumbs(args, i, $scope.breadcrumbs);
                        return;
                    }
                }

                if (typeof $scope.breadcrumbs[$scope.breadcrumbs.length] === "undefined" && (typeof $scope.breadcrumbs[$scope.breadcrumbs.length - 1] === "undefined" || $scope.breadcrumbs[$scope.breadcrumbs.length - 1].title !== args.title)) {
                    $scope.breadcrumbs[$scope.breadcrumbs.length] = args;
                }
            }
        });

        /**
         * Listens on key press (Enter) when cursor is in search field
         */
        $scope.synergySearch = function () {
            $location.path("search/" + ($scope.searchedItem));
        };

        $scope.goToSearch = function (suggestedItem) {
            $location.path(suggestedItem.link);
        };

        $scope.searchAhead = function () {
            if ($scope.searchedItem.length < 2) {
                return;
            }
            searchHttp.getFewSpecifications($scope, $scope.searchedItem, function (data) {
                var a = [];
                for (var i = 0, max = data.length; i < max; i++) {
                    if (data[i].project === null || data[i].project.length < 1) {
                        a.push({title: data[i].title + " (" + $scope.SYNERGY.product + " " + data[i].version + ")", link: data[i].type + "/" + data[i].id});
                    } else {
                        a.push({title: data[i].title + " (" + data[i].project + " " + data[i].version + ")", link: data[i].type + "/" + data[i].id});
                    }

                }
                $scope.suggestions = a;
                //   $("#typeahead").typeahead({source: a});
            }, function (data) {
                $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
            });
        };

        $scope.encodeURIComponent = function (s) {
            return encodeURIComponent(s);
        };

        // register bugtracking link scripts for all projects on page load  
        projectsHttp.getAll($scope, function (data) { // todo add caching
            for (var i = 0, max = data.length; i < max; i++) {
                if (data[i].reportLink !== null || data[i].viewLink !== null) {
                    var fnca,
                            fncb,
                            fncc;
                    /* jshint ignore:start */
                    try {
                        fnca = eval("(function(){ var a=" + data[i].viewLink + "; return a;})(); ");
                    } catch (e) {
                        fnca = null;
                    }
                    try {
                        fncb = eval("(function(){ var a=" + data[i].multiViewLink + "; return a;})(); ");
                    } catch (e) {
                        fncb = null;
                    }
                    try {
                        fncc = eval("(function(){ var a=" + data[i].reportLink + "; return a;})(); ");
                    } catch (e) {
                        fncc = null;
                    }

                    $scope.SYNERGY.bugtrackingSystems[data[i].name] = {
                        getDisplayLink: fnca,
                        getMultiDisplayLink: fncb,
                        getReportLink: fncc
                    };
                    /* jshint ignore:end */
                }
            }
        }, function () {
        });

    }
    function SearchCtrl($scope, $routeParams, searchHttp) {

        $scope.searched = $routeParams.search;
        $scope.results = [];
        $scope.escapedSearched = decodeURIComponent($scope.searched);

        $scope.fetch = function () {
            searchHttp.get($scope, $scope.searched, function (data) {
                $scope.results = data;
            }, function (data) {
                $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
            });
        };

        $scope.printResult = function (item) {
            switch (item.type) {
                case "specification":
                    return "<a href=\"#specification/" + item.id + "\">" + item.title + " (" + $scope.SYNERGY.product + " " + item.version + ")" + "</a>";
                case "suite":
                    return "<a href=\"#suite/" + item.id + "\">" + item.title + "</a>";
                default:
                    break;
            }
        };

        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
    }
    /**
     * Loads list of test specifications
     * @param {SpecificationsFct} specificationsHttp description
     * @param {VersionsFct} versionsHttp description
     * @param {UserFct} userFct description
     * @param {SpecificationFct} specificationFct description
     */
    function SpecPoolCtrl($scope, $routeParams, specificationsHttp, versionsHttp) {//authService
        $scope.$emit("updateNavbar", {item: "nav_specs"});
        $scope.specs = [];
        $scope.version = $routeParams.id || null; // selected version
        $scope.versions = []; // all versions available
        $scope.projects = [];
        $scope.orderProp = "title";
        $scope.rights = 0;
        $scope.isLoggedIn = (typeof $scope.SYNERGY.session.session_id !== "undefined" && $scope.SYNERGY.session.session_id.length > 1) ? 1 : 0;
        /**
         * used for caching specifications in given version. This in-memory cache is valid only while user in on this page
         */
        var cache = {};

        /**
         * Loads data from server
         */
        function init() {
            $scope.isLoggedIn = (typeof $scope.SYNERGY.session.session_id !== "undefined" && $scope.SYNERGY.session.session_id.length > 1) ? 1 : 0;
            if (typeof $scope.SYNERGY.session.session_id !== "undefined" && $scope.SYNERGY.session.session_id.length > 1) {
                $scope.rights = 1;
            }
            versionsHttp.get($scope, true, function (data) {
                data.unshift({id: -1, name: "all"});
                $scope.versions = data;
                $scope.version = $scope.version || data[0].name;
                specificationsHttp.get($scope, $scope.version, function (data) {


                    var _projects = [];
                    for (var i = 0, imax = data.length; i < imax; i++) {
                        if (data[i].projects.length > 0) {
                            data[i]._project = data[i].projects[0].name;
                            if (_projects.indexOf(data[i].projects[0].name) < 0) {
                                _projects.push(data[i].projects[0].name);
                            }
                        } else {
                            data[i]._project = $scope.SYNERGY.product;
                            data[i].projects = [{name: $scope.SYNERGY.product, id: -2}];
                            if (_projects.indexOf($scope.SYNERGY.product) < 0) {
                                _projects.push($scope.SYNERGY.product);
                            }
                        }
                    }
                    _projects.push("All");
                    $scope.projects = _projects;
                    cache[$scope.version + "projects"] = _projects;

                    $scope.specs = data;
                    cache[$scope.version] = data;
                    $scope.$emit("updateBreadcrumbs", {link: "specifications", title: "Test Specifications"});

                }, function (data, status) {
                    if (parseInt(status, 10) !== 404) {
                        $scope.SYNERGY.logger.log("Action failed", data.toString(), "INFO", "alert-error");
                    } else {
                        $scope.SYNERGY.logger.log("", "No results", "INFO");
                    }
                });
            }, $scope.generalHttpFactoryError);
        }

        $scope.init(function () {
            init();
        });

        /**
         * Loads specifications for given version. First it checks cache and if it doesn't contain data for
         * given version, asks server
         */
        $scope.filter = function () {

            if (cache.hasOwnProperty($scope.version)) {
                $scope.specs = cache[$scope.version];
                $scope.projects = cache[$scope.version + "projects"];
                return;
            }

            specificationsHttp.get($scope, $scope.version, function (data) {
                var _projects = [];
                for (var i = 0, imax = data.length; i < imax; i++) {
                    if (data[i].ext.hasOwnProperty("projects") && data[i].ext.projects.length > 0) {
                        data[i]._project = data[i].ext.projects[0].name;
                        if (_projects.indexOf(data[i].ext.projects[0].name) < 0) {
                            _projects.push(data[i].ext.projects[0].name);
                        }
                    } else {
                        data[i]._project = $scope.SYNERGY.product;
                        if (_projects.indexOf($scope.SYNERGY.product) < 0) {
                            _projects.push($scope.SYNERGY.product);
                        }
                    }
                }
                _projects.push("All");
                $scope.projects = _projects;
                $scope.specs = data;
                cache[$scope.version] = data;
                cache[$scope.version + "projects"] = _projects;
                $scope.$emit("updateBreadcrumbs", {link: "specifications", title: "Test Specifications"});

            }, function (data, status) {
                if (parseInt(status, 10) !== 404) {
                    $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
                } else {
                    $scope.SYNERGY.logger.log("", "No results", "INFO");
                }
            });
        };

    }
    /**
     *  
     * @param {SpecificationsFct} specificationsHttp
     * @param {RunsFct} runsHttp
     * @returns {undefined} 
     **/
    function HomeCtrl($scope, specificationsHttp, runsHttp, calendarHttp) {

        $scope.runs = {testRuns: []};
        $scope.specs = [];
        $scope.$emit("updateNavbar", {item: "nav_home"});

        /**
         * Loads latest test runs and test specifications
         */
        $scope.fetch = function () {
            runsHttp.getLatest($scope, 7, function (data) {
                if (typeof data.testRuns !== "undefined") {

                    data.testRuns.forEach(function (trun) {
                        if (trun.projectName === null || trun.projectName === "") {
                            trun.projectName = $scope.SYNERGY.product;
                        }
                    });

                    $scope.runs = data;
                }
            }, function (data) {
                $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
            });
            specificationsHttp.latest($scope, function (result) {
                $scope.specs = result;
                $scope.SYNERGY.publisher.publish(1, $scope, "specListLoaded");
            }, function (data) {
                $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
            });
        };

        function loadCalendar() {
            calendarHttp.getEvents($scope, function (data) {
                var items = [];

                for (var i = 0, max = data.length; i < max; i += 1) {
                    items.push({url: "#/run/" + data[i].id, title: data[i].title, start: new Date(data[i].start.substr(0, 4), parseInt(data[i].start.substr(4, 2), 10) - 1, data[i].start.substr(6, 2)), end: new Date(data[i].end.substr(0, 4), parseInt(data[i].end.substr(4, 2), 10) - 1, data[i].end.substr(6, 2))});
                }

                $("#cal").fullCalendar({
                    header: {
                        left: "prev,next today",
                        center: "title",
                        right: "month,agendaWeek,agendaDay"
                    },
                    editable: true,
                    events: items,
                    eventMouseover: function (event, jsEvent, view) {
                        if (view.name !== "agendaDay") {
                            $(jsEvent.target).attr("title", event.title);
                        }
                    }
                });
            }, function () {
                window.console.log("Failed to load calendar events");
            });
        }

        var self = $scope;
        $scope.init(function () {
            self.fetch();
            loadCalendar();
        });
    }
    /**
     * View 1
     * @param {RunFct} runHttp
     * @param {AssignmentFct} assignmentHttp description
     * @param {AttachmentFct} attachmentHttp description
     * @returns {undefined}
     */
    function RunCtrl($scope, utils, $location, $routeParams, runHttp, assignmentHttp, attachmentHttp, reviewHttp, SynergyUtils, SynergyHandlers, SynergyIssue) {

        $scope.$emit("updateNavbar", {item: "nav_runs"});
        $scope.$emit("updateBreadcrumbs", {link: "runs", title: "Test Runs"});
        $scope.id = $routeParams.id || -1;
        $scope.run = {};
        $scope.rights = 0;
        var currentActionId = -1;
        var currentAction = "";
        $scope.username = $scope.SYNERGY.session.username || "";
        $scope.attachmentBase = $scope.SYNERGY.server.buildURL("run_attachment", {});
        $scope.isLoading = false;
        $scope.tribes = [];
        var tribes = [];
        var leaderIsRemoving = false;
        $scope.explainModal = "";
        $scope.filter = {
            "assignee": "All",
            "specification": "All",
            "platform": "All",
            "tribe": "All"
        };
        var issueCollector = new SynergyIssue.RunIssuesCollector();
        $scope.P1Issues = [];
        $scope.P2Issues = [];
        $scope.P3Issues = [];
        $scope.unresolvedIssues = [];
        $scope.allIssues = [];
        $scope.specifications = [];
        $scope.assignees = [];
        $scope.project = {"name": "", "id": -1};
        $scope.pageSize = $scope.SYNERGY.assignmentPage;
        var _coverage = {};
        $scope.coverage = {};
        $scope.sortConfig = {
            property: ["id"],
            descending: [false]
        };
        $scope.orderingProperties = {
            "userDisplayName": {
                "desc": false,
                "asc": false
            },
            "specification": {
                "desc": false,
                "asc": false
            },
            "platform": {
                "desc": false,
                "asc": false
            }
        };
        $scope.testRunIsAvailable = false;
        $scope.pageReviewsExpanded = false;

        /**
         * Loads test run
         */
        $scope.fetch = function () {
            tribes = [];
            $scope.run = {};
            issueCollector = new SynergyIssue.RunIssuesCollector();
            $scope.P1Issues = [];
            $scope.P2Issues = [];
            $scope.P3Issues = [];
            $scope.unresolvedIssues = [];
            $scope.allIssues = [];
            $scope.specifications = [];
            $scope.assignees = [];
            $scope.project = {"name": "", "id": -1};
            _coverage = {};
            $scope.coverage = {};
            $scope.username = $scope.SYNERGY.session.username || "";
            runHttp.get($scope, $scope.id, function (data) {
                setProject(data);
                countResults(data);
                setTestRunIsAvailable(data);
                $scope.run = data;
                collectFilterData(data);
                $scope.$emit("updateBreadcrumbs", {link: "run/" + $scope.id + "/v/1", title: $scope.run.title});
                SynergyUtils.ProgressChart([((data.completed / data.total) * 100), 100 - ((data.completed / data.total) * 100)], ["#08c", "#ccc"], ["completed", ""], "canvas2");
                try {
                    if (data.controls.length > 0) {
                        $scope.rights = 1;
                    }
                } catch (e) {
                }
            }, $scope.generalHttpFactoryError);
        };

        function setTestRunIsAvailable(testRun) {
            var start = $scope.getDate(testRun.start);
            var stop = $scope.getDate(testRun.end);
            var today = new Date();
            $scope.testRunIsAvailable = (today >= start && today <= stop);
        }

        function setProject(data) {
            if (data.projectName !== null) {
                $scope.project = {"name": data.projectName, "id": data.projectId};
            } else {
                $scope.project = {"name": $scope.SYNERGY.product, "id": -2};
            }
        }

        $scope.toggleReivewSection = function () {
            $scope.pageReviewsExpanded = !$scope.pageReviewsExpanded;
            $("#pageReviews").collapse("toggle");
        };

        function collectFilterData(data) {
            var assignees = [];
            var platforms = [];
            var specifications = [];
            var tribes = [];
            for (var i = 0, max = data.assignments.length; i < max; i++) {
                if (assignees.indexOf(data.assignments[i].userDisplayName) < 0) {
                    assignees.push(data.assignments[i].userDisplayName);
                }
                if (specifications.indexOf(data.assignments[i].specification) < 0) {
                    specifications.push(data.assignments[i].specification);
                }
                if (platforms.indexOf(data.assignments[i].platform) < 0) {
                    platforms.push(data.assignments[i].platform);
                }
                for (var j = 0, max2 = data.assignments[i].tribes.length; j < max2; j++) {
                    if (tribes.indexOf(data.assignments[i].tribes[j]) < 0) {
                        tribes.push(data.assignments[i].tribes[j]);
                    }
                }
            }
            assignees.push("All");
            assignees.sort(function (a, b) {
                return a.toLowerCase() < b.toLowerCase() ? -1 : 1;
            });

            specifications.push("All");
            specifications.sort(function (a, b) {
                return a.toLowerCase() < b.toLowerCase() ? -1 : 1;
            });
            tribes.push("All");
            tribes.sort(function (a, b) {
                return a.toLowerCase() < b.toLowerCase() ? -1 : 1;
            });

            platforms.push("All");
            platforms.sort(function (a, b) {
                return a.toLowerCase() < b.toLowerCase() ? -1 : 1;
            });

            $scope.assignees = assignees;
            $scope.platforms = platforms;
            $scope.specifications = specifications;
            $scope.tribes = tribes;
        }

        $scope.assignessFilter = function (assignmentRecord) {
            if ($scope.filter.assignee && $scope.filter.assignee !== "All" && assignmentRecord.userDisplayName !== $scope.filter.assignee) {
                return false;
            }
            if ($scope.filter.specification && $scope.filter.specification !== "All" && assignmentRecord.specification !== $scope.filter.specification) {
                return false;
            }
            if ($scope.filter.platform && $scope.filter.platform !== "All" && assignmentRecord.platform !== $scope.filter.platform) {
                return false;
            }
            if ($scope.filter.tribe && $scope.filter.tribe !== "All" && assignmentRecord.tribes.indexOf($scope.filter.tribe) < 0) {
                return false;
            }
            return true;
        };

        /**
         * Counts number of passed/failed/skipped cases
         * @param {TestRun} run
         */
        function countResults(run) {
            var result = {
                "failed": 0,
                "passed": 0,
                "skipped": 0
            };

            if (SynergyUtils.definedNotNull(run) && SynergyUtils.definedNotNull(run.assignments)) {
                for (var i = 0, max = run.assignments.length; i < max; i++) {
                    if (!_coverage[run.assignments[i].platform]) {// FIXME
                        _coverage[run.assignments[i].platform] = {total: 0, completed: 0, name: run.assignments[i].platform};
                    }
                    issueCollector.addIssues(run.assignments[i].issues);
                    _coverage[run.assignments[i].platform].total += parseInt(run.assignments[i].total, 10);
                    _coverage[run.assignments[i].platform].completed += parseInt(run.assignments[i].completed, 10);
                    result.failed += Math.floor(run.assignments[i].failed);
                    result.passed += Math.floor(run.assignments[i].passed);
                    result.skipped += Math.floor(run.assignments[i].skipped);
                }

                var t = (result.failed + result.passed + result.skipped) / 100;
                if (t > 0) {
                    var f = Math.floor(result.failed * 10 / t) / 10;
                    var p = Math.round(result.passed * 10 / t) / 10;
                    SynergyUtils.ProgressChart([p, f, Math.round(10 * (100 - (f + p))) / 10], ["#62c462", "#ee5f5b", "#c67605"], ["passed", "failed", "skipped"], "canvas1");
                }
            }

            for (var j in _coverage) {
                if (_coverage.hasOwnProperty(j)) {
                    _coverage[j].progress = Math.round(10 * 100 * (_coverage[j].completed / _coverage[j].total)) / 10;
                }
            }
            $scope.coverage = _coverage;
            SynergyUtils.ProgressChart([issueCollector.issuesStats.opened / (issueCollector.issuesStats.total / 100), 100 - (issueCollector.issuesStats.opened / (issueCollector.issuesStats.total / 100))], ["#ccc", "#62c462"], ["Unresolved", "Resolved"], "issuesResolution");
            SynergyUtils.ProgressChart([issueCollector.issuesStats.P1 / (issueCollector.issuesStats.total / 100), issueCollector.issuesStats.P2 / (issueCollector.issuesStats.total / 100), issueCollector.issuesStats.P3 / (issueCollector.issuesStats.total / 100), issueCollector.issuesStats.P4 / (issueCollector.issuesStats.total / 100)], ["#ee5f5b", "#f89406", "#fbeed5", "#ccc"], ["P1 (" + issueCollector.issuesStats.P1 + ")", "P2 (" + issueCollector.issuesStats.P2 + ")", "P3 (" + issueCollector.issuesStats.P3 + ")", "P4 (" + issueCollector.issuesStats.P4 + ")"], "issuesPriority");
            $scope.allIssues = issueCollector.issues;
            $scope.unresolvedIssues = issueCollector.issuesStats.unresolvedIssues;
            $scope.P1Issues = issueCollector.issuesStats.P1Issues;
            $scope.P2Issues = issueCollector.issuesStats.P2Issues;
            $scope.P3Issues = issueCollector.issuesStats.P3Issues;

        }
        $scope.createCoverageChart = function (c) {
            try {
                SynergyUtils.ProgressChart([c.progress, 100 - c.progress], ["#08c", "#ccc"], ["finished", ""], "coverage" + c.name);
            } catch (e) {
            }
        };

        $scope.nextPage = function () {
            $scope.isLoading = true;
            $scope.pageSize += $scope.SYNERGY.assignmentPage;
            $scope.isLoading = false;
        };

        function resetFilters() {
            $scope.sortConfig = {
                "property": ["id"],
                "descending": [false]
            };
            $scope.orderingProperties = {
                "userDisplayName": {
                    "desc": false,
                    "asc": false
                },
                "specification": {
                    "desc": false,
                    "asc": false
                },
                "platform": {
                    "desc": false,
                    "asc": false
                }
            };
        }

        $scope.changeSorting = function (prop, order) {
            var _p = prop;
            prop = order ? prop : "-" + prop;
            if ($scope.sortConfig.property.indexOf(_p) > -1 || $scope.sortConfig.property.indexOf("-" + _p) > -1) {
                var i = $scope.sortConfig.property.indexOf(_p);
                if (i < 0) {
                    i = $scope.sortConfig.property.indexOf("-" + _p);
                }
                if ($scope.sortConfig.descending[i] === order) { // click on the same order arrow => remove it from filter
                    $scope.sortConfig.property.splice(i, 1);
                    $scope.sortConfig.descending.splice(i, 1);
                    $scope.orderingProperties[_p].desc = false; // reset css for this property
                    $scope.orderingProperties[_p].asc = false;
                    if ($scope.sortConfig.property.length === 0) { // resel css to default values and reset filter to ID 
                        resetFilters();
                    }
                } else {
                    $scope.orderingProperties[_p].desc = !$scope.orderingProperties[_p].desc; // invert css
                    $scope.orderingProperties[_p].asc = !$scope.orderingProperties[_p].asc;
                    var _orig = {// just place holder
                        "descending": !$scope.sortConfig.descending[i],
                        "property": $scope.sortConfig.property[i]
                    };
                    $scope.sortConfig.descending.splice(i, 1); // remove it from array of filters
                    $scope.sortConfig.property.splice(i, 1);
                    $scope.sortConfig.descending.splice(0, 0, _orig.descending); // insert it to level of filters at the beginning
                    (_orig.property.indexOf("-") === 0) ? $scope.sortConfig.property.splice(0, 0, _orig.property.substring(1, _orig.property.length)) : $scope.sortConfig.property.splice(0, 0, "-" + _orig.property);
                }
            } else {
                if ($scope.sortConfig.property.length === 1 && $scope.sortConfig.property[0] === "id") { // if no ordering so far, simply replace ID with selected property
                    $scope.sortConfig = {
                        property: [prop],
                        descending: [order]
                    };
                } else {
                    $scope.sortConfig.property.splice(0, 0, prop); // insert at the beginning
                    $scope.sortConfig.descending.splice(0, 0, order);
                }

                $scope.orderingProperties[_p].desc = !order; // css update
                $scope.orderingProperties[_p].asc = order;
            }
        };

        $scope.bugs = [];
        $scope.bugsAssignmentId = -1;

        $scope.alterBugs = function (assignment) {
            $scope.bugsAssignmentId = assignment.id;
            var a = [];
            for (var i = 0, max = assignment.issues.length; i < max; i++) {
                a.push({
                    "id": assignment.issues[i].bugId,
                    "stillValid": true,
                    "changeCount": false
                });
            }
            $scope.bugs = a;
            $("#ticketsModal").modal("toggle");
        };

        $scope.performAlterBugs = function () {
            var newIssues = [];
            var countDiff = 0;
            for (var i = 0, max = $scope.bugs.length; i < max; i++) {
                if ($scope.bugs[i].stillValid) {
                    newIssues.push($scope.bugs[i].id);
                } else {
                    if ($scope.bugs[i].changeCount) {
                        countDiff++;
                    }
                }
            }

            assignmentHttp.alterBugs($scope, $scope.bugsAssignmentId, {issues: newIssues.join(";"), diffCount: countDiff}, function () {
                $scope.SYNERGY.logger.log("Done", "Issues updated", "INFO", "alert-success");
                $scope.fetch();
            }, $scope.generalHttpFactoryError);


            $("#ticketsModal").modal("toggle");
        };
        /**
         * Redirects to page so user starts testing and completing his assignments
         * @param {Number} mode
         * @param {Number} assignmentId assignment ID
         */
        $scope.startAssignment = function (mode, assignmentId) {
            if (parseInt(mode, 10) === 2) {// restart => show modal confirmation
                $("#deleteModalLabel").text("Restart assignment?");
                $("#deleteModalBody").html("<p>Do you really want to restart this assignment? All saved progress will be lost as if you never started it. If you want to Continue saved assignment, please use 'Play' button instead</p>");
                $("#deleteModal").modal("toggle");
                currentAction = "restartAssignment";
                currentActionId = assignmentId;
            } else {
                $location.path("/assignment/" + assignmentId + "/v/" + mode);
            }
        };

        $scope.startReviewAssignment = function (mode, assignmentId) {
            if (parseInt(mode, 10) === 2) {// restart => show modal confirmation
                $("#deleteModalLabel").text("Restart assignment?");
                $("#deleteModalBody").html("<p>Do you really want to restart this assignment? All saved comments will be lost as if you never started it. If you want to Continue saved assignment, please use 'Play' button instead</p>");
                $("#deleteModal").modal("toggle");
                currentAction = "restartReviewAssignment";
                currentActionId = assignmentId;
            } else {
                $location.path("/review/" + assignmentId + "/continue");
            }
        };

        /**
         * Starts with action on given test run. If the action name is different than "delete", redirection is done.
         * Otherwise confirmation dialog is opened
         * @param {String} action action name
         */
        $scope.performRun = function (action) {
            switch (action) {
                case "delete":
                    $("#deleteModalLabel").text("Delete test run?");
                    $("#deleteModalBody").html("<p>Do you really want to delete test run?</p>");
                    $("#deleteModal").modal("toggle");
                    currentAction = "deleteRun";
                    break;
                case "notify":
                    $("#deleteModalLabel").text("Send notifications?");
                    $("#deleteModalBody").html("<p>Do you really want to send email notifications to testers with incomplete test assignment?</p>");
                    $("#deleteModal").modal("toggle");
                    currentAction = "notify";
                    break;
                case "freeze":
                    var target = ($scope.run.isActive ? 0 : 1);
                    runHttp.freezeRun($scope, $scope.id, target, function (data) {
                        $scope.run.isActive = target;
                        $scope.SYNERGY.logger.log("Done", "Test run " + (target === 1 ? "unfrozen" : "frozen"), "INFO", "alert-success");
                    }, $scope.generalHttpFactoryError);
                    break;
                default:
                    $location.path("/administration/run/" + $scope.id + "/" + action);
                    break;
            }
        };

        /**
         * Starts with action on given test assignment
         * Otherwise confirmation dialog is opened
         * @param {String} action action name
         * @param {Number} id assignment ID
         */
        $scope.performAssignment = function (action, id, createdBy) {
            if (action !== "delete") {
                $location.path("suite/" + id + "/" + action);
            } else {
                switch (action) {
                    case "delete":
                        currentAction = "deleteAssignment";
                        currentActionId = id;
                        leaderIsRemoving = (createdBy === 3) ? true : false;
                        $("#deleteModalLabel").text("Delete test assignment?");
                        $("#deleteModalBody").html("<p>Do you really want to delete test assignment?</p>");
                        $("#deleteModal").modal("toggle");
                        break;
                    default:
                        break;
                }
            }
        };
        $scope.performReviewAssignment = function (action, id, createdBy) {
            switch (action) {
                case "delete":
                    currentAction = "deleteReviewAssignment";
                    currentActionId = id;
                    leaderIsRemoving = (createdBy === 3) ? true : false;
                    $("#deleteModalLabel").text("Delete review assignment?");
                    $("#deleteModalBody").html("<p>Do you really want to delete review assignment?</p>");
                    $("#deleteModal").modal("toggle");
                    break;
                default:
                    $location.path("/review/" + id + "/" + action);
                    break;
            }
        };

        function deleteReviewAssignment() {
            if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                return;
            }
            reviewHttp.remove($scope, currentActionId, function (data) {
                $scope.SYNERGY.logger.log("Done", "Assignment deleted", "INFO", "alert-success");
                $scope.fetch();
            }, $scope.generalHttpFactoryError);
        }

        $scope.deleteAssignment = function () {
            if (!leaderIsRemoving) {

                if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                    return;
                }
                assignmentHttp.remove($scope, currentActionId, function (data) {
                    $scope.SYNERGY.logger.log("Done", "Assignment deleted", "INFO", "alert-success");
                    $scope.fetch();
                }, $scope.generalHttpFactoryError);
            } else {
                $("#explainModal").modal("toggle");
                if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1 || $scope.explanation.length < 1) {
                    return;
                }
                assignmentHttp.removeByLeader($scope, currentActionId, $scope.explanation, function (data) {
                    $scope.SYNERGY.logger.log("Done", "Assignment deleted", "INFO", "alert-success");
                    $scope.fetch();
                }, $scope.generalHttpFactoryError);
            }
        };

        /**
         * Executes some action based on value of $scope.currentAction
         */
        $scope.performAction = function () {
            switch (currentAction) {
                case "restartAssignment":
                    $("#deleteModal").modal("toggle");
                    $location.path("/assignment/" + currentActionId + "/v/2");
                    break;
                case "restartReviewAssignment":
                    $("#deleteModal").modal("toggle");
                    $location.path("/review/" + currentActionId + "/restart");
                    break;
                case "deleteAssignment":
                    $("#deleteModal").modal("toggle");
                    leaderIsRemoving ? $("#explainModal").modal("toggle") : $scope.deleteAssignment();
                    break;
                case "deleteReviewAssignment":
                    $("#deleteModal").modal("toggle");
                    deleteReviewAssignment();
                    break;
                case "notify":
                    $("#deleteModal").modal("toggle");
                    runHttp.sendNotifications($scope, $scope.id, function (data) {
                        $scope.SYNERGY.logger.log("Done", data, "INFO", "alert-success");
                    }, function (data) {
                        $scope.SYNERGY.logger.log("Action failed", "", "INFO", "alert-error");
                        $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
                    });
                    break;
                case "deleteRun":
                    $("#deleteModal").modal("toggle");
                    if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                        return;
                    }
                    runHttp.remove($scope, $scope.id, function (data) {
                        $scope.SYNERGY.modal.update("Test run removed", "");
                        $scope.SYNERGY.modal.show();
                        $location.path("/runs");
                    }, function (data) {
                        $scope.SYNERGY.modal.update("Action failed", "");
                        $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
                        $scope.SYNERGY.modal.show();
                    });
                    break;
                case "deleteAttachment":
                    $("#deleteModal").modal("toggle");
                    if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                        return;
                    }
                    attachmentHttp.removeRunAttachment($scope, currentActionId, function (data) {
                        $scope.SYNERGY.logger.log("Done", "Attachment deleted", "INFO", "alert-success");
                        $scope.fetch();
                    }, function (data) {
                        $scope.SYNERGY.logger.log("Action failed", "", "INFO", "alert-error");
                        $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
                        $scope.fetch();
                    });
                    break;
                default:
                    break;
            }
        };

        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });

        // ATTACHMENT UPLOAD HANDLING
        new SynergyHandlers.FileUploader([], "dropbox", $scope.SYNERGY.uploadFileLimit, $scope.SYNERGY.server.buildURL("run_attachment", {"id": $scope.id}), function (title, msg, level, style, fileName) {
            $scope.SYNERGY.logger.log(title, msg, level, style);
            $scope.fileName = fileName;
            $scope.fetch();
        }, function (title, msg, level, style) {
            $scope.SYNERGY.logger.log(title, msg, level, style);
        });
    }



    function RunCtrlCase($scope, utils, $location, $routeParams, runHttp, SynergyUtils, SynergyHandlers, SynergyIssue) {
        $scope.$emit("updateNavbar", {item: "nav_runs"});
        $scope.id = $routeParams.id || -1;

        $scope.run = {};
        $scope.project = {"name": "", "id": -1};
        $scope.isLoading = false;
        $scope.testRunIsAvailable = false;
        $scope.specs = [];
        $scope.overview = {
            totalTc: 0,
            pRate: 0,
            testers: 0,
            duration: "",
            time: ""
        };

        $scope.labels = [];
        $scope.selectedLabels = [];
        $scope.resultFilter = {
            passed: true,
            failed: true,
            skipped: true,
            passed_with_issues: true
        };

        var allExpanded = false;

        function setProject(data) {
            if (data.projectName !== null) {
                $scope.project = {"name": data.projectName, "id": data.projectId};
            } else {
                $scope.project = {"name": $scope.SYNERGY.product, "id": -2};
            }
        }

        /**
         * Loads test run
         */
        $scope.fetch = function () {
            $scope.project = {"name": "", "id": -1};
            $scope.isLoading = true;
            $scope.run = {};
            runHttp.getBlobs($scope, $scope.id, function (data) {
                $scope.run = data;
                setProject(data);
                $scope.$emit("updateBreadcrumbs", {link: "run/" + $scope.id + "/v/3", title: $scope.run.title});
                $scope.isLoading = false;
                buildData();
            }, $scope.generalHttpFactoryError);
        };

        $scope.toggleExpand = function (item) {
            item.expanded = !item.expanded;
            if (item.suites) {
                $("#spec" + item.id).collapse("toggle");
            } else {
                $("#suite" + item.id).collapse("toggle");
            }
        };

        $scope.toggleAll = function () {
            if (allExpanded) {
                $(".collapse").collapse("hide");
            } else {
                $(".collapse").collapse("show");
            }
            allExpanded = !allExpanded;
        };

        function buildData() {
            var arrObj = {};
            var knownSpecs = [];
            var _currentSuite;
            var _currentSpec;

            var totalTc = 0;
            var passedTc = 0;
            var failedTc = 0;
            var skippedTc = 0;
            var totalMins = 0;

            $scope.run.durations.forEach(function (x) {
                totalMins += x.duration;
            });

            var passedWithIssues = 0;
            var _users = [];
            var _allLabels = [];

            $scope.run.blobs.forEach(function (blob) {
                if (_users.indexOf(blob.user) < 0) {
                    _users.push(blob.user);
                }
                if (blob.label.length > 0 && _allLabels.indexOf(blob.label) < 0) {
                    _allLabels.push(blob.label);
                }

                if (knownSpecs.indexOf(blob.specification.id) < 0) {
                    knownSpecs.push(blob.specification.id);
                    arrObj["_" + blob.specification.id] = {
                        name: blob.specification.name,
                        id: blob.specification.id,
                        expanded: false,
                        label: blob.label,
                        version: blob.specification.version,
                        suites: {}
                    };
                }
                _currentSpec = arrObj["_" + blob.specification.id];
                // for each suite
                blob.specification.suites.forEach(function (suite) {

                    if (!_currentSpec.suites.hasOwnProperty("_" + suite.id)) {
                        _currentSpec.suites["_" + suite.id] = {
                            name: suite.name,
                            id: suite.id,
                            expanded: false,
                            testCases: {}
                        };
                    }

                    _currentSuite = _currentSpec.suites["_" + suite.id];

                    suite.testCases.forEach(function (tc) {
                        if (!_currentSuite.testCases.hasOwnProperty("_" + tc.id)) {
                            _currentSuite.testCases["_" + tc.id] = {
                                name: tc.name,
                                id: tc.id,
                                results: []
                            };
                        }
                        if (tc.finished === 1) {
                            totalTc++;
                            var _s = getResult(tc);
                            if (_s === "passed") {
                                passedTc++;
                            } else if (_s === "passed with issues") {
                                passedTc++;
                                passedWithIssues++;
                            } else if (_s === "failed") {
                                failedTc++;
                            } else if (_s === "skipped") {
                                skippedTc++;
                            }
                            _currentSuite.testCases["_" + tc.id].results.push({
                                result: _s,
                                visible: true,
                                issuesLbl: tc.issues.length > 1 ? "issues" : (tc.issues.length === 0 ? "" : tc.issues[0]),
                                link: "#/case/" + tc.id + "/suite/" + suite.id,
                                resultClass: _s.replace(/\s/g, "_"),
                                platform: blob.platform,
                                user: blob.user,
                                issues: tc.issues.length > 0 ? tc.issues : []
                            });
                        }

                    });
                });
            });


            var arr = [];
            var _x;
            var _y;
            var _suites;
            var _cases;
            for (var k in arrObj) {
                if (arrObj.hasOwnProperty(k)) {
                    _x = arrObj[k];
                    _suites = [];
                    for (var l in _x.suites) {
                        if (_x.suites.hasOwnProperty(l)) {

                            _y = _x.suites[l];
                            _cases = [];
                            for (var m in _y.testCases) {
                                if (_y.testCases.hasOwnProperty(m)) {
                                    _cases.push(_y.testCases[m]);
                                }
                            }
                            _y.testCases = _cases;
                            _suites.push(_y);
                        }
                    }
                    _x.suites = _suites;
                    arr.push(_x);
                }
            }

            var _pRateRound = Math.round(100 * 10 * passedTc / totalTc) / 10;
            var _fRateRound = Math.round(100 * 10 * failedTc / totalTc) / 10;
            var _pRateRound2 = Math.round(100 * 10 * passedWithIssues / totalTc) / 10;
            var _sRateRound = Math.round(10 * (100 - _pRateRound - _fRateRound - _pRateRound2)) / 10;

            var start = $scope.getDate($scope.run.start);
            var stop = $scope.getDate($scope.run.end);

            $scope.overview = {
                totalTc: totalTc,
                pRate: _pRateRound + "% passed, " + _pRateRound2 + "% passed with issues, " + _fRateRound + "% failed and " + _sRateRound + "% skipped",
                testers: _users.length,
                duration: getDuration(stop.getTime() - start.getTime()),
                time: totalMins > 59 ? Math.floor(totalMins / 60) + " hours and " + (totalMins % 60) + " minutes" : totalMins + " minutes"
            };
            $scope.specs = arr;
            $scope.labels = _allLabels;
        }


        function getDuration(duration) {

            var seconds = Math.floor(duration / 1000);
            var minutes = Math.floor(seconds / 60);
            var hours = Math.floor(minutes / 60);
            var days = Math.floor(hours / 24);
            hours = hours - (days * 24);
            minutes = minutes - (days * 24 * 60) - (hours * 60);
            seconds = seconds - (days * 24 * 60 * 60) - (hours * 60 * 60) - (minutes * 60);

            var result = "";
            if (days > 0) {
                result += (days + " days|");
            }
            if (hours > 0) {
                result += (hours + " hours|");
            }
            if (minutes > 0) {
                result += (minutes + " minutes|");
            }
            if (seconds > 0) {
                result += (seconds + " seconds|");
            }

            result = result.replace(/\|/g, " ");
            if (result.length === 0) {
                result = "0 seconds";
            }

            return result;
        }

        $scope.filter = function () {
            var _v;
            $scope.specs.forEach(function (spec) {
                spec.suites.forEach(function (suite) {
                    suite.testCases.forEach(function (tcase) {
                        tcase.results.forEach(function (result) {
                            _v = shouldDisplay(result);
                            _v = _v && ($scope.selectedLabels.length > 0 ? $scope.selectedLabels.indexOf(spec.label) > -1 : true);
                            result.visible = _v;
                        });
                    });
                });
            });





        };

        function shouldDisplay(result) {
            return $scope.resultFilter[result.resultClass];
        }

        function getResult(tc) {
            if (tc.result === "passed" && tc.issues.length > 0) {
                return "passed with issues";
            } else {
                return tc.result;
            }
        }

        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
    }

    function RunCtrlUser($scope, $location, $routeParams, runHttp, assignmentHttp, attachmentHttp, reviewHttp, SynergyUtils, SynergyHandlers, SynergyIssue) {

        $scope.$emit("updateNavbar", {item: "nav_runs"});
        $scope.id = $routeParams.id || -1;
        $scope.platforms = [];
        $scope.run = {};
        $scope.rights = 0;
        var leaderIsRemoving = false;
        $scope.explainModal = "";
        var currentActionId = -1;
        var currentAction = "";
        $scope.username = $scope.SYNERGY.session.username || "";
        $scope.attachmentBase = $scope.SYNERGY.server.buildURL("run_attachment", {});
        $scope.assignees = [];
        $scope.specifications = [];
        $scope.tribes = [];
        $scope.project = {"name": "", "id": -1};
        var tribes = [];
        $scope.filter = {
            "assignee": "All",
            "specification": "All",
            "tribe": "All"
        };
        $scope.coverage = [];
        var _coverage = [];
        $scope.isLoading = false;
        $scope.pageSize = $scope.SYNERGY.assignmentPage;
        $scope.sortConfig = {
            property: ["id"],
            descending: [false]
        };
        $scope.P1Issues = [];
        $scope.P2Issues = [];
        $scope.P3Issues = [];
        $scope.unresolvedIssues = [];
        $scope.allIssues = [];
        $scope.orderingProperties = {
            "userDisplayName": {
                "desc": false,
                "asc": false
            },
            "specification": {
                "desc": false,
                "asc": false
            }
        };
        $scope.pageReviewsExpanded = false;
        $scope.testRunIsAvailable = false;
        var issueCollector = new SynergyIssue.RunIssuesCollector();

        /**
         * Loads test run
         */
        $scope.fetch = function () {
            issueCollector = new SynergyIssue.RunIssuesCollector();
            $scope.P1Issues = [];
            tribes = [];
            _coverage = [];
            $scope.project = {"name": "", "id": -1};
            $scope.P2Issues = [];
            $scope.P3Issues = [];
            $scope.unresolvedIssues = [];
            $scope.allIssues = [];
            $scope.isLoading = true;
            $scope.coverage = [];
            $scope.tribes = [];
            $scope.platforms = [];
            $scope.assignees = [];
            $scope.specifications = [];
            $scope.run = {};
            $scope.username = $scope.SYNERGY.session.username || "";
            runHttp.getUserCentric($scope, $scope.id, function (data) {
                setTestRunIsAvailable(data);
                $scope.run = data;
                setProject(data);
                $scope.$emit("updateBreadcrumbs", {link: "run/" + $scope.id + "/v/2", title: $scope.run.title});
                getPlatforms();
                buildAssignments();
                SynergyUtils.ProgressChart([((data.completed / data.total) * 100), 100 - ((data.completed / data.total) * 100)], ["#08c", "#ccc"], ["completed", ""], "canvas2");
                try {
                    if (data.controls.length > 0) {
                        $scope.rights = 1;
                    }
                } catch (e) {
                }
                $scope.isLoading = false;
            }, $scope.generalHttpFactoryError);
        };

        $scope.toggleReivewSection = function () {
            $scope.pageReviewsExpanded = !$scope.pageReviewsExpanded;
            $("#pageReviews").collapse("toggle");
        };

        function setTestRunIsAvailable(testRun) {
            var start = $scope.getDate(testRun.start);
            var stop = $scope.getDate(testRun.end);
            var today = new Date();
            $scope.testRunIsAvailable = (today >= start && today <= stop);
        }

        function setProject(data) {
            if (data.projectName !== null) {
                $scope.project = {"name": data.projectName, "id": data.projectId};
            } else {
                $scope.project = {"name": $scope.SYNERGY.product, "id": -2};
            }
        }

        $scope.assignessFilter = function (assignmentRecord) {
            if ($scope.filter.assignee && $scope.filter.assignee !== "All" && assignmentRecord.userDisplayName !== $scope.filter.assignee) {
                return false;
            }
            if ($scope.filter.specification && $scope.filter.specification !== "All" && assignmentRecord.specification !== $scope.filter.specification) {
                return false;
            }
            if ($scope.filter.tribe && $scope.filter.tribe !== "All" && assignmentRecord.tribes.indexOf($scope.filter.tribe) < 0) {
                return false;
            }
            return true;
        };

        $scope.nextPage = function () {
            $scope.isLoading = true;
            $scope.pageSize += $scope.SYNERGY.assignmentPage;
            $scope.isLoading = false;
        };

        $scope.bugs = [];
        $scope.bugsAssignmentId = -1;

        $scope.alterBugs = function (assignment) {
            $scope.bugsAssignmentId = assignment.id;
            var a = [];
            for (var i = 0, max = assignment.issues.length; i < max; i++) {
                a.push({
                    "id": assignment.issues[i].bugId,
                    "stillValid": true,
                    "changeCount": false
                });
            }
            $scope.bugs = a;
            $("#ticketsModal").modal("toggle");
        };

        $scope.performAlterBugs = function () {
            var newIssues = [];
            var countDiff = 0;
            for (var i = 0, max = $scope.bugs.length; i < max; i++) {
                if ($scope.bugs[i].stillValid) {
                    newIssues.push($scope.bugs[i].id);
                } else {
                    if ($scope.bugs[i].changeCount) {
                        countDiff++;
                    }
                }
            }

            assignmentHttp.alterBugs($scope, $scope.bugsAssignmentId, {issues: newIssues.join(";"), diffCount: countDiff}, function () {
                $scope.SYNERGY.logger.log("Done", "Issues updated", "INFO", "alert-success");
                $scope.fetch();
            }, $scope.generalHttpFactoryError);


            $("#ticketsModal").modal("toggle");
        };

        function resetFilters() {
            $scope.sortConfig = {
                "property": ["id"],
                "descending": [false]
            };
            $scope.orderingProperties = {
                "userDisplayName": {
                    "desc": false,
                    "asc": false
                },
                "specification": {
                    "desc": false,
                    "asc": false
                }
            };
        }

        $scope.changeSorting = function (prop, order) {
            var _p = prop;
            prop = order ? prop : "-" + prop;
            if ($scope.sortConfig.property.indexOf(_p) > -1 || $scope.sortConfig.property.indexOf("-" + _p) > -1) {
                var i = $scope.sortConfig.property.indexOf(_p);
                if (i < 0) {
                    i = $scope.sortConfig.property.indexOf("-" + _p);
                }
                if ($scope.sortConfig.descending[i] === order) { // click on the same order arrow => remove it from filter
                    $scope.sortConfig.property.splice(i, 1);
                    $scope.sortConfig.descending.splice(i, 1);
                    $scope.orderingProperties[_p].desc = false; // reset css for this property
                    $scope.orderingProperties[_p].asc = false;
                    if ($scope.sortConfig.property.length === 0) { // resel css to default values and reset filter to ID 
                        resetFilters();
                    }
                } else {
                    $scope.orderingProperties[_p].desc = !$scope.orderingProperties[_p].desc; // invert css
                    $scope.orderingProperties[_p].asc = !$scope.orderingProperties[_p].asc;
                    var _orig = {// just place holder
                        "descending": !$scope.sortConfig.descending[i],
                        "property": $scope.sortConfig.property[i]
                    };
                    $scope.sortConfig.descending.splice(i, 1); // remove it from array of filters
                    $scope.sortConfig.property.splice(i, 1);
                    $scope.sortConfig.descending.splice(0, 0, _orig.descending); // insert it to level of filters at the beginning
                    (_orig.property.indexOf("-") === 0) ? $scope.sortConfig.property.splice(0, 0, _orig.property.substring(1, _orig.property.length)) : $scope.sortConfig.property.splice(0, 0, "-" + _orig.property);
                }
            } else {
                if ($scope.sortConfig.property.length === 1 && $scope.sortConfig.property[0] === "id") { // if no ordering so far, simply replace ID with selected property
                    $scope.sortConfig = {
                        property: [prop],
                        descending: [order]
                    };
                } else {
                    $scope.sortConfig.property.splice(0, 0, prop); // insert at the beginning
                    $scope.sortConfig.descending.splice(0, 0, order);
                }

                $scope.orderingProperties[_p].desc = !order; // css update
                $scope.orderingProperties[_p].asc = order;
            }
        };

        $scope.createCoverageChart = function (c) {
            try {
                SynergyUtils.ProgressChart([c.progress, 100 - c.progress], ["#08c", "#ccc"], ["finished", ""], "coverage" + c.name);
            } catch (e) {
            }
        };

        function buildAssignments() {
            var result = {
                "failed": 0,
                "passed": 0,
                "skipped": 0
            };
            var assignees = [];
            var specs = [];
            tribes = [];
            var duplicateAssignments = [];
            var prettyAssignments = [];
            var allResolved;
            for (var username in $scope.run.assignments) {
                if ($scope.run.assignments.hasOwnProperty(username)) {
                    var user = $scope.run.assignments[username];
                    var line = {
                        assignments: []
                    };
                    for (var assign = 0, maxAssign = user.assignments.length; assign < maxAssign; assign++) {
                        var assignment = user.assignments[assign];
                        line.userDisplayName = assignment.userDisplayName;
                        line.username = assignment.username;
                        line.specificationId = assignment.specificationId;
                        line.label = assignment.label;
                        line.labelId = assignment.labelId;
                        line.specification = assignment.specification;
                        line.tribes = assignment.tribes;
                        assignees.indexOf(line.userDisplayName) < 0 && assignees.push(line.userDisplayName);
                        specs.indexOf(line.specification) < 0 && specs.push(line.specification);
                        getTribes(assignment);
                        assignment.total = parseInt(assignment.total, 10);
                        assignment.completed = parseInt(assignment.completed, 10);
                        assignment.failedColor = "#62c462";
                        allResolved = allIssuesResolved(assignment.issues);
                        if (parseInt(assignment.failed, 10) > 0 && !allResolved) {
                            assignment.failedColor = "#ee5f5b";
                        }
                        if (assignment.total > 0) {
                            assignment.progress = Math.round(100 * 10 * assignment.completed / assignment.total) / 10;
                            assignment.progressLabel = "Completed " + assignment.progress + "%";
                        } else {
                            assignment.progressLabel = "No cases to test";
                            assignment.progress = 100;
                        }
                        assignment.info = (allResolved && assignment.progress === 100 && parseInt(assignment.failed, 10) === 0) ? "finished" : assignment.info;
                        var _t = (assignment.started.length > 0 ? "Started: " + assignment.started : "");
                        _t += (assignment.lastUpdated.length > 0 ? "; Last updated: " + assignment.lastUpdated : "");
                        assignment.tooltip = assignment.total > 0 ? _t : "At the time of assignment creation, there were no matching cases. You can try to start this assignment to see if situation has changed";
                        if (assignment.issues.length === 1) {
                            assignment.issuesLink = (assignment.issues.length > 0) ? "<a style='color: " + assignment.failedColor + "; font-weight: bold' href='" + $scope.SYNERGY.issues.viewLink($scope.project.name, assignment.issues) + "'>" + assignment.issues.length + " issue</a>&nbsp;" : "&nbsp;";
                        } else if (assignment.issues.length > 1) {
                            assignment.issuesLink = (assignment.issues.length > 0) ? "<a style='color: " + assignment.failedColor + "; font-weight: bold' href='" + $scope.SYNERGY.issues.viewLink($scope.project.name, assignment.issues) + "'>" + assignment.issues.length + " issues</a>&nbsp;" : "&nbsp;";
                        }
                        if (typeof line.assignments[$scope.platforms.indexOf(assignment.platform)] !== "undefined") {
                            line.assignments[$scope.platforms.indexOf(assignment.platform)].duplicates = true;
                            assignment.duplicates = true;
                            duplicateAssignments.push(assignment);
                        } else {
                            assignment.duplicates = false;
                            line.assignments[$scope.platforms.indexOf(assignment.platform)] = assignment;
                        }
                        if (!_coverage[$scope.platforms.indexOf(assignment.platform)]) { // FIXME
                            _coverage[$scope.platforms.indexOf(assignment.platform)] = {total: 0, completed: 0, name: assignment.platform};
                        }

                        _coverage[$scope.platforms.indexOf(assignment.platform)].total += parseInt(assignment.total, 10);
                        _coverage[$scope.platforms.indexOf(assignment.platform)].completed += parseInt(assignment.completed, 10);

                        result.failed += Math.floor(assignment.failed);
                        result.passed += Math.floor(assignment.passed);
                        result.skipped += Math.floor(assignment.skipped);
                    }

                    for (var p = 0, maxp = $scope.platforms.length; p < maxp; p++) {
                        if (!line.assignments[p]) {
                            line.assignments[p] = {"_hidden": new Date()};
                        }
                    }

                    prettyAssignments.push(line);
                }
            }

            // duplicates
            for (var dupl = 0, maxa = duplicateAssignments.length; dupl < maxa; dupl++) {
                prettyAssignments.push(getLineForDupliciteAssignment(duplicateAssignments[dupl]));
            }

            assignees.push("All");
            assignees.sort(function (a, b) {
                return a.toLowerCase() < b.toLowerCase() ? -1 : 1;
            });
            specs.push("All");
            specs.sort(function (a, b) {
                return a.toLowerCase() < b.toLowerCase() ? -1 : 1;
            });
            tribes.push("All");
            tribes.sort(function (a, b) {
                return a.toLowerCase() < b.toLowerCase() ? -1 : 1;
            });
            $scope.assignees = assignees;
            $scope.specifications = specs;
            $scope.tribes = tribes;
            countResults(result);
            $scope.prettyAssignments = prettyAssignments;
            SynergyUtils.ProgressChart([issueCollector.issuesStats.opened / (issueCollector.issuesStats.total / 100), 100 - (issueCollector.issuesStats.opened / (issueCollector.issuesStats.total / 100))], ["#ccc", "#62c462"], ["Unresolved", "Resolved"], "issuesResolution");
            SynergyUtils.ProgressChart([issueCollector.issuesStats.unknown / (issueCollector.issuesStats.total / 100), issueCollector.issuesStats.P1 / (issueCollector.issuesStats.total / 100), issueCollector.issuesStats.P2 / (issueCollector.issuesStats.total / 100), issueCollector.issuesStats.P3 / (issueCollector.issuesStats.total / 100), issueCollector.issuesStats.P4 / (issueCollector.issuesStats.total / 100)], ["#E8EBAB", "#ee5f5b", "#f89406", "#fbeed5", "#ccc"], ["Unknown (" + issueCollector.issuesStats.unknown + ")", "P1 (" + issueCollector.issuesStats.P1 + ")", "P2 (" + issueCollector.issuesStats.P2 + ")", "P3 (" + issueCollector.issuesStats.P3 + ")", "P4 (" + issueCollector.issuesStats.P4 + ")"], "issuesPriority");
            $scope.allIssues = issueCollector.issues;
            $scope.unresolvedIssues = issueCollector.issuesStats.unresolvedIssues;
            $scope.P1Issues = issueCollector.issuesStats.P1Issues;
            $scope.P2Issues = issueCollector.issuesStats.P2Issues;
            $scope.P3Issues = issueCollector.issuesStats.P3Issues;
        }

        function getLineForDupliciteAssignment(assignment) {
            var line = {
                assignments: []
            };
            line.userDisplayName = assignment.userDisplayName;
            line.username = assignment.username;
            line.specificationId = assignment.specificationId;
            line.label = assignment.label;
            line.labelId = assignment.labelId;
            line.specification = assignment.specification;
            line.tribes = assignment.tribes;
            line.assignments[$scope.platforms.indexOf(assignment.platform)] = assignment;
            for (var p = 0, maxp = $scope.platforms.length; p < maxp; p++) {
                if (!line.assignments[p]) {
                    line.assignments[p] = {"_hidden": new Date()};
                }
            }
            return line;
        }

        function allIssuesResolved(issues) {
            var result = true;
            for (var i = 0, max = issues.length; i < max; i++) {
                issueCollector.addIssue(issues[i]);
                if (issues[i].status.toLowerCase() !== "resolved" && issues[i].status.toLowerCase() !== "closed" && issues[i].status.toLowerCase() !== "verified") {
                    result = false;
                }
            }
            return result;
        }

        function getTribes(assignment) {
            for (var i = 0, max = assignment.tribes.length; i < max; i++) {
                if (tribes.indexOf(assignment.tribes[i]) < 0) {
                    tribes.push(assignment.tribes[i]);
                }
            }
        }

        // retrieves all distinct platforms from all assignments and assign them to $scope.platforms
        function getPlatforms() {
            for (var username in $scope.run.assignments) {
                if ($scope.run.assignments.hasOwnProperty(username)) {
                    var user = $scope.run.assignments[username];
                    for (var assignment in user.assignments) {
                        if ($scope.platforms.indexOf(user.assignments[assignment].platform) < 0) {
                            $scope.platforms.push(user.assignments[assignment].platform);
                        }
                    }
                }
            }
            $scope.platforms.sort();
        }
        /**
         * Counts number of passed/failed/skipped cases
         * @param {TestRun} run
         */
        function countResults(result) {
            var t = (result.failed + result.passed + result.skipped) / 100;
            if (t > 0) {
                var f = Math.floor(result.failed * 10 / t) / 10;
                var p = Math.round(result.passed * 10 / t) / 10;
                SynergyUtils.ProgressChart([p, f, Math.round(10 * (100 - (f + p))) / 10], ["#62c462", "#ee5f5b", "#c67605"], ["passed", "failed", "skipped"], "canvas1");
            }

            for (var i = 0, max = _coverage.length; i < max; i++) {
                _coverage[i].progress = Math.round(10 * 100 * (_coverage[i].completed / _coverage[i].total)) / 10;
            }
            $scope.coverage = _coverage;

        }

        /**
         * Redirects to page so user starts testing and completing his assignments
         * @param {Number} mode
         * @param {Number} assignmentId assignment ID
         */
        $scope.startAssignment = function (mode, assignmentId) {
            if (parseInt(mode, 10) === 2) {// restart => show modal confirmation
                $("#deleteModalLabel").text("Restart assignment?");
                $("#deleteModalBody").html("<p>Do you really want to restart this assignment? All saved progress will be lost as if you never started it. If you want to Continue saved assignment, please use 'Play' button instead</p>");
                $("#deleteModal").modal("toggle");
                currentAction = "restartAssignment";
                currentActionId = assignmentId;
            } else {
                $location.path("/assignment/" + assignmentId + "/v/" + mode);
            }
        };

        /**
         * Starts with action on given test run. If the action name is different than "delete", redirection is done.
         * Otherwise confirmation dialog is opened
         * @param {String} action action name
         */
        $scope.performRun = function (action) {
            switch (action) {
                case "delete":
                    $("#deleteModalLabel").text("Delete test run?");
                    $("#deleteModalBody").html("<p>Do you really want to delete test run?</p>");
                    $("#deleteModal").modal("toggle");
                    currentAction = "deleteRun";
                    break;
                case "notify":
                    $("#deleteModalLabel").text("Send notifications?");
                    $("#deleteModalBody").html("<p>Do you really want to send email notifications to testers with incomplete test assignment?</p>");
                    $("#deleteModal").modal("toggle");
                    currentAction = "notify";
                    break;
                case "freeze":
                    var target = ($scope.run.isActive ? 0 : 1);
                    runHttp.freezeRun($scope, $scope.id, target, function (data) {
                        $scope.run.isActive = target;
                        (target === 1) ? $scope.SYNERGY.logger.log("Done", "Test run unfrozen", "INFO", "alert-success") : $scope.SYNERGY.logger.log("Done", "Test run frozen", "INFO", "alert-success");
                    }, $scope.generalHttpFactoryError);
                    break;
                default:
                    $location.path("/administration/run/" + $scope.id + "/" + action);
                    break;
            }
        };

        /**
         * Starts with action on given run attachment
         * Otherwise confirmation dialog is opened
         * @param {String} action action name
         * @param {Number} id attachment ID
         */
        $scope.performAttachment = function (action, id) {
            switch (action) {
                case "delete":
                    $("#deleteModalLabel").text("Delete attachment?");
                    $("#deleteModalBody").html("<p>Do you really want to delete attachment?</p>");
                    $("#deleteModal").modal("toggle");
                    currentAction = "deleteAttachment";
                    currentActionId = id;
                    break;
                default:
                    break;
            }
        };

        /**
         * Redirects user to page where he can create a new run assignment
         */
        $scope.forwardToCreateAssignment = function () {
            $location.path("/administration/assignment/create/run/" + $scope.id);
        };

        /**
         * Redirects user to page where he can create a new matrix run assignment
         */
        $scope.forwardToCreateMatrixAssignment = function () {
            $location.path("/administration/assignment/creatematrix/run/" + $scope.id);
        };

        /**
         * Starts with action on given test assignment
         * Otherwise confirmation dialog is opened
         * @param {String} action action name
         * @param {Number} id assignment ID
         */
        $scope.performAssignment = function (action, id, createdBy) {
            if (action !== "delete") {
                $location.path("suite/" + id + "/" + action);
            } else {
                switch (action) {
                    case "delete":
                        leaderIsRemoving = (createdBy === 3) ? true : false;
                        $("#deleteModalLabel").text("Delete test assignment?");
                        $("#deleteModalBody").html("<p>Do you really want to delete test assignment?</p>");
                        $("#deleteModal").modal("toggle");
                        currentAction = "deleteAssignment";
                        currentActionId = id;
                        break;
                    default:
                        break;
                }
            }
        };

        $scope.deleteAssignment = function () {
            if (!leaderIsRemoving) {
                if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                    return;
                }
                assignmentHttp.remove($scope, currentActionId, function (data) {
                    $scope.SYNERGY.logger.log("Done", "Assignment deleted", "INFO", "alert-success");
                    $scope.fetch();
                }, $scope.generalHttpFactoryError);
            } else {
                $("#explainModal").modal("toggle");
                if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1 || $scope.explanation.length < 1) {
                    return;
                }
                assignmentHttp.removeByLeader($scope, currentActionId, $scope.explanation, function (data) {
                    $scope.SYNERGY.logger.log("Done", "Assignment deleted", "INFO", "alert-success");
                    $scope.fetch();
                }, $scope.generalHttpFactoryError);
            }
        };

        $scope.startReviewAssignment = function (mode, assignmentId) {
            if (parseInt(mode, 10) === 2) {// restart => show modal confirmation
                $("#deleteModalLabel").text("Restart assignment?");
                $("#deleteModalBody").html("<p>Do you really want to restart this assignment? All saved comments will be lost as if you never started it. If you want to Continue saved assignment, please use 'Play' button instead</p>");
                $("#deleteModal").modal("toggle");
                currentAction = "restartReviewAssignment";
                currentActionId = assignmentId;
            } else {
                $location.path("/review/" + assignmentId + "/continue");
            }
        };

        $scope.performReviewAssignment = function (action, id, createdBy) {
            switch (action) {
                case "delete":
                    currentAction = "deleteReviewAssignment";
                    currentActionId = id;
                    leaderIsRemoving = (createdBy === 3) ? true : false;
                    $("#deleteModalLabel").text("Delete review assignment?");
                    $("#deleteModalBody").html("<p>Do you really want to delete review assignment?</p>");
                    $("#deleteModal").modal("toggle");
                    break;
                default:
                    $location.path("/review/" + id + "/" + action);
                    break;
            }
        };

        function deleteReviewAssignment() {
            if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                return;
            }
            reviewHttp.remove($scope, currentActionId, function (data) {
                $scope.SYNERGY.logger.log("Done", "Assignment deleted", "INFO", "alert-success");
                $scope.fetch();
            }, $scope.generalHttpFactoryError);
        }
        /**
         * Executes some action based on value of $scope.currentAction
         */
        $scope.performAction = function () {
            switch (currentAction) {
                case "restartAssignment":
                    $("#deleteModal").modal("toggle");
                    $location.path("/assignment/" + currentActionId + "/v/2");
                    break;
                case "deleteAssignment":
                    $("#deleteModal").modal("toggle");
                    leaderIsRemoving ? $("#explainModal").modal("toggle") : $scope.deleteAssignment();
                    break;
                case "restartReviewAssignment":
                    $("#deleteModal").modal("toggle");
                    $location.path("/review/" + currentActionId + "/restart");
                    break;
                case "deleteReviewAssignment":
                    $("#deleteModal").modal("toggle");
                    deleteReviewAssignment();
                    break;
                case "notify":
                    $("#deleteModal").modal("toggle");
                    runHttp.sendNotifications($scope, $scope.id, function (data) {
                        $scope.SYNERGY.logger.log("Done", data, "INFO", "alert-success");
                    }, $scope.generalHttpFactoryError);
                    break;
                case "deleteRun":
                    $("#deleteModal").modal("toggle");
                    if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                        return;
                    }
                    runHttp.remove($scope, $scope.id, function (data) {
                        $scope.SYNERGY.modal.update("Test run removed", "");
                        $scope.SYNERGY.modal.show();
                        $location.path("/runs");
                    }, function (data) {
                        $scope.SYNERGY.modal.update("Action failed", "");
                        $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
                        $scope.SYNERGY.modal.show();
                    });
                    break;
                case "deleteAttachment":
                    $("#deleteModal").modal("toggle");
                    if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                        return;
                    }
                    attachmentHttp.removeRunAttachment($scope, currentActionId, function (data) {
                        $scope.SYNERGY.logger.log("Done", "Attachment deleted", "INFO", "alert-success");
                        $scope.fetch();
                    }, function (data) {
                        $scope.SYNERGY.logger.log("Action failed", "", "INFO", "alert-error");
                        $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
                        $scope.fetch();
                    });
                    break;
                default:
                    break;
            }
        };

        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });

        // ATTACHMENT UPLOAD HANDLING
        new SynergyHandlers.FileUploader([], "dropbox", $scope.SYNERGY.uploadFileLimit, $scope.SYNERGY.server.buildURL("run_attachment", {"id": $scope.id}), function (title, msg, level, style, fileName) {
            $scope.SYNERGY.logger.log(title, msg, level, style);
            $scope.fileName = fileName;
            $scope.fetch();
        }, function (title, msg, level, style) {
            $scope.SYNERGY.logger.log(title, msg, level, style);
        });
    }
    /**
     * @param {VersionsFct} versionsHttp
     * @param {SpecificationFct} specificationHttp description
     * @param {SuiteFct} suiteHttp description
     * @param {AttachmentFct} attachmentHttp description
     * @param {UsersFct} usersHttp description
     *  * @param {JobFct} jobHttp description
     */
    function SpecificationCtrl($scope, utils, $location, $routeParams, versionsHttp, specificationHttp, suiteHttp, attachmentHttp, usersHttp, jobHttp, userHttp, sanitizerHttp, labelsHttp, projectsHttp, specificationCache, SynergyUtils, SynergyModels, SynergyHandlers) {//authService

        var self = this;
        $scope.project = null;
        $scope.$emit("updateNavbar", {item: "nav_specs"});
        $scope.specification = {};
        $scope.refreshCodemirror = false; // on edit/create page it is necessary to refresh editor element
        $scope.id = $routeParams.id || -1;
        $scope.rights = 0;
        $scope.readOnlyJobs = [];
        $scope.users = []; // used on edit page to select owner
        $scope.filename = "";
        $scope.attachmentBase = $scope.SYNERGY.server.buildURL("attachment", {});
        $scope.versions = [];
        $scope.version = "";
        $scope.newLabel = "";
        $scope.removeLabel = "";
        self.simpleName = $routeParams.simpleName || "";
        self.simpleVersion = $routeParams.simpleVersion || "";
        self.originalSimpleName = "";
        $scope.keepSimpleNameTrack = true;
        $scope.filterLabel = $routeParams.label || "All";
        $scope.labels = [];
        $scope.realLabels = []; // without all
        $scope.removalUsers = "";
        $scope.requestMsg = "";
        $scope.projects = [];
        var specificationDurationCache = {};
        var currentAction = "";
        var currentActionId = -1;
        var currentSuiteId = -1;

        /**
         * Loads data from server
         */
        $scope.fetch = function (useCache) {
            if (self.simpleName.length > 0) {
                loadSpecificationFromAlias();
                return;
            }

            if (window.location.href.indexOf("/title/") > -1) {// shouldn't be, caused by some issue in .htaccess
                $location.path("");
                return;
            }

            switch (getAction()) {
                case "create":
                    versionsHttp.get($scope, true, function (data) {
                        $scope.versions = data;
                        $scope.version = data[0].name || "";
                        $scope.refreshCodemirror = true;
                    }, $scope.generalHttpFactoryError);

                    projectsHttp.getAll($scope, function (data) {
                        $scope.projects = data;
                        $scope.project = $scope.projects[0];
                    }, $scope.generalHttpFactoryError);

                    break;
                case "1":
                    if ($scope.id < 0) {
                        return;
                    }
                    if (specificationCache.getCurrentSpecificationId() === parseInt($scope.id, 10)) {
                        displaySimpleSpecification(specificationCache.getCurrentSpecification());
                    } else {
                        specificationCache.resetCurrentSpecification();
                        specificationHttp.get($scope, useCache, $scope.id, function (data) {
                            displaySimpleSpecification(data);
                        }, $scope.generalHttpFactoryError);
                    }
                    break;
                case "2":
                    if ($scope.id < 0) {
                        return;
                    }
                    specificationHttp.getFull($scope, useCache, $scope.id, function (data) {
                        $scope.specification = data;
                        setProject(data);
                        specificationDurationCache.All = data.estimation;
                        specificationCache.setCurrentSpecification(data, $scope.project);
                        getRemovalUsers();
                        $scope.labels = getLabels(data);
                        $scope.newname = data.title;
                        resolveContinuousJobs(data.ext.continuous_integration);
                        $scope.$emit("updateBreadcrumbs", {link: "specification/" + $scope.id + "/v/2", title: data.title});
                        try {
                            if (data.controls.length > 0) {
                                $scope.rights = 1;
                            }
                        } catch (e) {
                        }

                    }, $scope.generalHttpFactoryError);
                    break;
                default : // edit
                    if ($scope.id < 0) {
                        return;
                    }
                    if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                        break;
                    }

                    specificationHttp.get($scope, false, $scope.id, function (data) {
                        self.originalSimpleName = data.simpleName;
                        setProject(data);
                        $scope.refreshCodemirror = true;
                        if (SynergyUtils.definedNotNull(data.ext.continuous_integration)) {
                            for (var j = 0, max = data.ext.continuous_integration.length; j < max; j++) {
                                data.ext.continuous_integration[j].jobUrl = data.ext.continuous_integration[j].jobUrl.substring(0, data.ext.continuous_integration[j].jobUrl.indexOf("/lastCompletedBuild"));
                            }
                        }
                        $scope.$emit("updateBreadcrumbs", {link: "specification/" + $scope.id, title: data.title});
                        try {
                            if (data.controls.length > 0) {
                                $scope.rights = 1;
                            }
                        } catch (e) {
                        }
                        $scope.specification = data;
                        $scope.specification.originalOwner = data.owner;

                        projectsHttp.getAll($scope, function (data) {

                            var defaultProject = true;
                            for (var p = 0, maxp = data.length; p < maxp; p++) {
                                if (data[p].name === $scope.project.name) {
                                    $scope.project.id = data[p].id;
                                    defaultProject = false;
                                    break;
                                }
                            }
                            if (defaultProject) {
                                data.push($scope.project);
                            }
                            $scope.projects = data;

                        }, $scope.generalHttpFactoryError);

                        usersHttp.getAll($scope, function (data) {
                            $scope.users = data.users;
                        }, $scope.generalHttpFactoryError);
                    }, $scope.generalHttpFactoryError);

                    break;
            }
        };

        function displaySimpleSpecification(data) {
            $scope.specification = data;
            $scope.newname = data.title;
            setProject(data);
            resolveContinuousJobs(data.ext.continuous_integration);
            getRemovalUsers();
            $scope.$emit("updateBreadcrumbs", {link: "specification/" + $scope.id + "/v/1", title: data.title});
            try {
                if (data.controls.length > 0) {
                    $scope.rights = 1;
                }
            } catch (e) {
            }
        }

        function setProject(data) {
            if (data.hasOwnProperty("ext") && data.ext.hasOwnProperty("projects") && data.ext.projects.length > 0) {
                $scope.project = data.ext.projects[0];
            } else {
                $scope.project = {"name": $scope.SYNERGY.product, id: -2};
            }
        }

        /**
         * Sets all users that requested specification removal to $scope.removalUsers
         */
        function getRemovalUsers() {
            var _l = "";
            for (var i = 0, max = $scope.specification.ext.removalRequests.length; i < max; i++) {
                _l += $scope.specification.ext.removalRequests[i].username + ", ";
            }
            $scope.removalUsers = (_l.length === 1) ? "" : _l.substr(0, _l.length - 2);
        }

        /**
         * Returns action based on URL (edit, create, 1,2)
         * @returns {String} action
         */
        function getAction() {
            var url = window.location.href;
            var stringId = $scope.id + "";
            var _s = url.lastIndexOf("/" + $scope.id + "/") + stringId.length + 2;
            var _e = url.indexOf("/", _s);
            var action = (_e > -1) ? url.substring(_s, _e) : url.substring(_s, url.length);
            if (action === "v") {
                return (url.indexOf($scope.id + "/v/1") > -1) ? "1" : "2";
            }
            return action;
        }

        $scope.getSpecificationDuration = function () {
            if (!$scope.filterLabel || $scope.filterLabel === "All") {
                return $scope.specification.estimation;
            }

            if (typeof specificationDurationCache[$scope.filterLabel] !== "undefined") {
                return specificationDurationCache[$scope.filterLabel];
            }

            var time = 0;
            for (var i = 0, max = $scope.specification.testSuites.length; i < max; i++) {
                for (var j = 0, max2 = $scope.specification.testSuites[i].testCases.length; j < max2; j++) {
                    for (var k = 0, max3 = $scope.specification.testSuites[i].testCases[j].keywords.length; k < max3; k++) {
                        if ($scope.specification.testSuites[i].testCases[j].keywords[k] === $scope.filterLabel) {
                            time += $scope.specification.testSuites[i].testCases[j].duration;
                        }
                    }
                }
            }
            specificationDurationCache[$scope.filterLabel] = time;
            return time;
        }

        /**
         * Returns true or false if test case matches label filter
         * @param {type} testCase
         * @returns {Boolean} true if test case has given label or if searched label is set to All, false if it doesn't 
         */
        $scope.hasLabel = function (testCase, a) {
            if (!$scope.filterLabel || $scope.filterLabel === "All") {
                return true;
            }
            for (var i = 0, max = testCase.keywords.length; i < max; i++) {
                if (testCase.keywords[i] === $scope.filterLabel) {
                    return true;
                }
            }
            return false;
        };
        /**
         * Asks server to sanitize input and renders it to the Preview tab
         */
        $scope.loadPreview = function () {
            var _t = "<h1>" + ($scope.specification.title || "") + "</h1><h3>Description</h3><div class='well'>" + ($scope.specification.desc || "") + "</div>";
            sanitizerHttp.getSanitizedInput($scope, _t, function (data) {
                $scope.preview = data;
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Toggle state of specification in user's favorites list
         */
        $scope.toggleFavorite = function () {
            var target = parseInt($scope.specification.isFavorite, 10) > 0 ? 0 : 1;
            var spec = new SynergyModels.Specification("", "", "", "", $scope.id);
            spec.isFavorite = target;
            userHttp.toggleFavorite($scope, spec, function (data) {
                $scope.specification.isFavorite = target;
                if (target === 0) {
                    $scope.SYNERGY.logger.log("Done", "Specification removed from favorites", "INFO", "alert-success");
                } else {
                    $scope.SYNERGY.logger.log("Done", "Specification added to favorites", "INFO", "alert-success");
                }
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Loads specification based on simple name property in URL
         * @returns {undefined}
         */
        function loadSpecificationFromAlias() {
            self.simpleName = decodeURIComponent(decodeURIComponent(self.simpleName));
            specificationHttp.getFullAlias($scope, false, self.simpleName, self.simpleVersion, function (data) {
                $scope.specification = data;
                $scope.labels = getLabels(data);
                specificationDurationCache.All = data.estimation;
                setProject(data);
                specificationCache.setCurrentSpecification(data, $scope.project);
                $scope.newname = data.title;
                $scope.id = data.id;
                resolveContinuousJobs(data.ext.continuous_integration);
                $scope.$emit("updateBreadcrumbs", {link: "specification/" + data.id + "/v/2", title: data.title});
                try {
                    if (data.controls.length > 0) {
                        $scope.rights = 1;
                    }
                } catch (e) {
                }
            }, $scope.generalHttpFactoryError);
        }

        /**
         * Collects all distinct labels from specification, adds "All" label and sorts them alphabetically
         * @param {Specification} data
         * @returns {Array} array of strings
         */
        function getLabels(data) {
            var labels = [];
            var realLabels = [];
            for (var i = 0, max = data.testSuites.length; i < max; i++) {
                for (var j = 0, max2 = data.testSuites[i].testCases.length; j < max2; j++) {
                    for (var k = 0, max3 = data.testSuites[i].testCases[j].keywords.length; k < max3; k++) {
                        if (labels.indexOf(data.testSuites[i].testCases[j].keywords[k]) < 0) {
                            labels.push(data.testSuites[i].testCases[j].keywords[k]);
                            realLabels.push(data.testSuites[i].testCases[j].keywords[k]);
                        }
                    }
                }
            }

            labels.sort(function (a, b) {
                return a.toLowerCase() < b.toLowerCase() ? -1 : 1;
            });
            realLabels.sort(function (a, b) {
                return a.toLowerCase() < b.toLowerCase() ? -1 : 1;
            });
            $scope.realLabels = realLabels;
            if ($scope.realLabels.length > 0) {
                $scope.removeLabel = $scope.realLabels[0];
            }
            labels.push("All");
            return labels;
        }

        /**
         * Resolve each continuous job
         */
        function resolveContinuousJobs(jobs) {

            function doResolve(data) {
                $scope.readOnlyJobs.push(data);
            }

            function logError(data) {
                $scope.SYNERGY.logger.log("Failed to load data", data, "DEBUG", "alert-error");
            }

            $scope.readOnlyJobs = [];
            if (!SynergyUtils.definedNotNull(jobs)) {
                return;
            }
            for (var i = 0, max = jobs.length; i < max; i++) {
                jobHttp.resolve($scope, jobs[i], doResolve, logError);
            }
        }

        /**
         * General attachment action
         * @param {String} action
         * @param {Number} id attachment ID
         */
        $scope.performAttachment = function (action, id) {
            switch (action) {
                case "delete":
                    $("#deleteModalLabel").text("Delete attachment?");
                    $("#deleteModalBody").html("<p>Do you really want to delete attachment?</p>");
                    $("#modal_confirm_ok").attr("ng-click", "deleteAttachment()");
                    $("#deleteModal").modal("toggle");
                    currentAction = "deleteAttachment";
                    currentActionId = id;
                    break;
                default:
                    $location.path("specification_attachment/" + id + "/" + action);
                    break;
            }
        };

        $scope.showJobsModal = function () {
            $("#jobsModal").modal("toggle");
        };

        /**
         * Removes job from specification
         */
        $scope.removeJob = function (jobId) {
            jobHttp.remove($scope, jobId, $scope.specification.id, function (data) {
                for (var i = 0, max = $scope.specification.ext.continuous_integration.length; i < max; i++) {
                    if ($scope.specification.ext.continuous_integration[i].id === jobId) {
                        $scope.specification.ext.continuous_integration.splice(i, 1);
                        break;
                    }
                }
                $scope.SYNERGY.modal.update("Job removed", "");
                $scope.SYNERGY.modal.show();
            }, function (data) {
                $scope.SYNERGY.modal.show();
                $scope.SYNERGY.logger.log("Action failed", data, "INFO", "alert-error");
                $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
            });
        };
        /**
         * Adds job to specification
         */
        $scope.addJob = function () {
            var job = new SynergyModels.Job($scope.jobToBeAdded, $scope.id, -1);
            jobHttp.create($scope, job, function (data) {
                $scope.specification.ext.continuous_integration.push(job);
                $scope.SYNERGY.modal.update("Job added", "");
                $scope.SYNERGY.modal.show();
            }, $scope.generalHttpFactoryError);
        };
        /**
         * Clones specification (sends request to server)
         * @returns {unresolved}
         */
        $scope.clone = function () {
            if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                return;
            }
            if (!$scope.newname) {
                $scope.newname = $scope.specification.title;
            }
            if ($scope.newname.length < 1 || $scope.cloneVersion.length < 1) {
                $scope.SYNERGY.modal.update("Missing parameters", "Please add a new name and version");
                $scope.SYNERGY.modal.show();
            } else {
                specificationHttp.clone($scope, currentActionId, $scope.newname, $scope.cloneVersion, function (newLink) {
                    var id = newLink.substring(newLink.indexOf("=") + 1);
                    $scope.SYNERGY.logger.log("Done", "Specification created, see " + window.location.hostname + window.location.pathname + "#/specification/" + id, "INFO", "alert-success");
                }, $scope.generalHttpFactoryError);
            }
        };

        /**
         * Starts with action on given test suite
         * Otherwise confirmation dialog is opened
         * @param {String} action action name
         * @param {Number} id suite ID
         */
        $scope.performSuite = function (action, id) {
            // delete could be done on a same page
            switch (action) {
                case "delete":
                    $("#deleteModalLabel").text("Delete test suite?");
                    $("#deleteModalBody").html("<p>Do you really want to delete test suite?</p>");
                    $("#deleteModal").modal("toggle");
                    currentAction = "deleteSuite";
                    currentActionId = id;
                    break;
                case "labels":
                    //  $("#addLabelsModalLabel").text("Add label to all cases in suite");
                    $("#addLabelsModal").modal("toggle");
                    currentAction = "labels";
                    currentActionId = id;
                    break;
                default:
                    $location.path("suite/" + id + "/" + action);
                    break;
            }

        };

        $scope.performCase = function (action, id, suiteId) {
            switch (action) {
                case "delete":
                    $("#deleteModalLabel").text("Delete test case?");
                    $("#deleteModalBody").html("<p>This will only remove reference to this test case in this suite. Continue?</p>");
                    $("#deleteModal").modal("toggle");
                    currentAction = "deleteCase";
                    currentActionId = id;
                    currentSuiteId = suiteId;
                    break;
                default:
                    break;
            }
        };

        $scope.deleteCase = function () {
            $("#deleteModal").modal("toggle");
            if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                return;
            }
            specificationCache.resetCurrentSpecification();
            var toBeRemovedCaseId = parseInt(currentActionId, 10);
            var toBeRemovedSuiteId = parseInt(currentSuiteId, 10);
            suiteHttp.removeCase($scope, currentSuiteId, currentActionId, function (data) {
                $scope.SYNERGY.logger.log("Done", "Test Case removed from test suite", "INFO", "alert-success");
                for (var i = 0, max = $scope.specification.testSuites.length; i < max; i++) {
                    if ($scope.specification.testSuites[i].id === toBeRemovedSuiteId) {
                        var s = $scope.specification.testSuites[i];
                        for (var j = 0, max2 = s.testCases.length; j < max2; j++) {
                            if (s.testCases[j].id === toBeRemovedCaseId) {
                                s.testCases.splice(j, 1);
                                return;
                            }
                        }

                    }
                }
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Executes some action based on value of $scope.currentAction
         */
        $scope.performAction = function (labelMode) {
            switch (currentAction) {
                case "deleteAttachment":
                    $scope.deleteAttachment();
                    break;
                case "cloneSpecification":
                    $scope.clone();
                    break;
                case "ownershipRequest":
                    specificationHttp.requestOwnership($scope, new SynergyModels.OwnershipRequest($scope.id, $scope.SYNERGY.session.username, $scope.requestMsg), function () {
                        $scope.SYNERGY.logger.log("Done", "Request has been sent to owner", "INFO", "alert-success");
                        $scope.fetch();
                    }, $scope.generalHttpFactoryError);
                    break;
                case "deleteSpecification":
                    $scope.deleteSpecification();
                    break;
                case "deleteSuite":
                    $("#deleteModal").modal("toggle");
                    if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                        return;
                    }
                    specificationCache.resetCurrentSpecification();
                    var toBeRemovedSuiteId = parseInt(currentActionId, 10);
                    suiteHttp.remove($scope, currentActionId, function () {
                        $scope.SYNERGY.logger.log("Done", "Test suite deleted", "INFO", "alert-success");
                        for (var i = 0, max = $scope.specification.testSuites.length; i < max; i++) {
                            if ($scope.specification.testSuites[i].id === toBeRemovedSuiteId) {
                                $scope.specification.testSuites.splice(i, 1);
                                return;
                            }
                        }
                    }, $scope.generalHttpFactoryError);
                    break;
                case "deleteCase":
                    $scope.deleteCase();
                    break;
                case "labels":
                    if (labelMode === "add") {
                        addLabels();
                    } else {
                        removeLabels();
                    }
                    break;
                default:
                    break;
            }
        };

        /**
         * Removes label from suite and then removes it from $scope.specification
         */
        function removeLabels() {
            if ($scope.removeLabel.length < 1) {
                return;
            }
            var id = parseInt(currentActionId, 10);
            specificationCache.resetCurrentSpecification();
            labelsHttp.removeFromSuite($scope, currentActionId, $scope.removeLabel, function () {
                $scope.SYNERGY.logger.log("Done", "Labels removed", "INFO", "alert-success");
                for (var i = 0, max = $scope.specification.testSuites.length; i < max; i++) {
                    if (parseInt($scope.specification.testSuites[i].id, 10) === id) {
                        for (var j = 0, max2 = $scope.specification.testSuites[i].testCases.length; j < max2; j++) {
                            var index = $scope.specification.testSuites[i].testCases[j].keywords.indexOf($scope.removeLabel);
                            if (index > -1) {
                                $scope.specification.testSuites[i].testCases[j].keywords.splice(index, 1);
                            }
                        }
                        break;
                    }
                }
                $scope.labels = getLabels($scope.specification);
            }, $scope.generalHttpFactoryError);
            $("#addLabelsModal").modal("toggle");
        }

        function addLabels() {
            if ($scope.newLabel.length < 1) {
                return;
            }
            var id = parseInt(currentActionId, 10);
            specificationCache.resetCurrentSpecification();
            labelsHttp.createForSuite($scope, currentActionId, $scope.newLabel, function () {
                $scope.SYNERGY.logger.log("Done", "Labels added", "INFO", "alert-success");
                for (var i = 0, max = $scope.specification.testSuites.length; i < max; i++) {
                    if (parseInt($scope.specification.testSuites[i].id, 10) === id) {
                        for (var j = 0, max2 = $scope.specification.testSuites[i].testCases.length; j < max2; j++) {
                            if ($scope.specification.testSuites[i].testCases[j].keywords.indexOf($scope.newLabel) < 0) {
                                $scope.specification.testSuites[i].testCases[j].keywords.push($scope.newLabel);
                            }
                        }
                        break;
                    }
                }
                $scope.labels = getLabels($scope.specification);
            }, $scope.generalHttpFactoryError);
            $("#addLabelsModal").modal("toggle");
        }

        /**
         * Removes attachment (calls server)
         * @returns {unresolved}
         */
        $scope.deleteAttachment = function () {
            $("#deleteModal").modal("toggle");
            if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                return;
            }
            var toBeRemoved = currentActionId;
            attachmentHttp.removeSpecAttachment($scope, currentActionId, $scope.specification.id, function (data) {
                $scope.SYNERGY.logger.log("Done", "Attachment deleted", "INFO", "alert-success");
                for (var i = 0, max = $scope.specification.attachments.length; i < max; i++) {
                    if ($scope.specification.attachments[i].id === toBeRemoved) {
                        $scope.specification.attachments.splice(i, 1);
                        return;
                    }
                }
            }, function (data) {
                $scope.SYNERGY.logger.log("Action failed", data, "INFO", "alert-error");
                $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
                $scope.fetch();
            });
        };

        /**
         * Executes some action with specification based on value of $scope.currentAction
         */
        $scope.performSpecification = function (action) {
            // delete could be done on a same page
            switch (action) {
                case "delete":
                    $("#deleteModalLabel").text("Delete specification?");
                    $("#deleteModalBody").html("<p>Do you really want to delete specification?</p>");
                    $("#deleteModal").modal("toggle");
                    currentAction = "deleteSpecification";
                    break;
                case "clone":
                    currentActionId = $scope.id;
                    currentAction = "cloneSpecification";
                    versionsHttp.get($scope, true, function (data) {
                        $scope.versions = data;
                        $("#dupliciteSpecModal").modal("toggle");
                    }, $scope.generalHttpFactoryError);
                    break;
                case "ownershipRequest":
                    if ($scope.SYNERGY.session.username.length < 1) {
                        return;
                    }
                    $("#ownershipRequestModal").modal("toggle");
                    currentActionId = $scope.id;
                    currentAction = "ownershipRequest";
                    break;
                default:
                    $location.path("specification/" + $scope.specification.id + "/" + action);
                    break;
            }

        };

        /**
         * Goes back in history by 1 step
         */
        $scope.cancel = function () {
            window.history.back();
        };

        /**
         * Deletes specification (calls server)
         * @returns {unresolved}
         */
        $scope.deleteSpecification = function () {
            $("#deleteModal").modal("toggle");
            if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                return;
            }
            specificationCache.resetCurrentSpecification();
            specificationHttp.remove($scope, $scope.specification.id, function (data, status) {
                if (status === 202) {
                    $scope.SYNERGY.logger.log("Done", "Request to remove this specification has been sent to owner", "INFO", "alert-success");
                    $scope.specification.ext.removalRequests.push({"username": $scope.SYNERGY.session.username});
                    getRemovalUsers();
                } else {
                    $scope.SYNERGY.modal.update("Specification deleted", "");
                    $scope.SYNERGY.modal.show();
                    $location.path("specifications");
                }
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Saves modified specification
         */
        $scope.save = function () {
            if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                return;
            }
            var spec = new SynergyModels.Specification($scope.specification.title, $scope.specification.desc, "", $scope.specification.owner, $scope.specification.id);
            var intProjectId = parseInt($scope.project.id, 10);
            spec.ext.projects = [{"name": $scope.projects.filter(function (item, index) {
                        if (parseInt(item.id, 10) === intProjectId) {
                            return item.name;
                        }
                    })[0].name, "id": intProjectId}];
            spec.setSimpleName($scope.specification.simpleName);
            if ($scope.myForm.$invalid || typeof spec.desc === "undefined" || spec.desc.length < 0) {
                $scope.SYNERGY.modal.update("Missing required fields", "");
                $scope.SYNERGY.modal.show();
                return;
            }
            specificationCache.resetCurrentSpecification();
            specificationHttp.edit($scope, spec, $scope.minorEdit ? true : false, $scope.keepSimpleNameTrack, function (data) {
                $scope.SYNERGY.modal.update("Specification updated", "");
                $scope.SYNERGY.modal.show();
                $location.path("specification/" + $scope.specification.id);
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Creates a new specification
         * @returns {unresolved}
         */
        $scope.create = function () {
            if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                return;
            }
            if ($scope.myForm.$invalid || typeof $scope.specification.desc === "undefined" || $scope.specification.desc.length < 0) {
                $scope.SYNERGY.modal.update("Missing required fields", "");
                $scope.SYNERGY.modal.show();
                return;
            }
            specificationCache.resetCurrentSpecification();
            var spec = new SynergyModels.Specification($scope.specification.title, $scope.specification.desc, $scope.version, "", -1);
            var intProjectId = parseInt($scope.project.id, 10);
            spec.ext.projects = [{"name": $scope.projects.filter(function (item, index) {
                        if (parseInt(item.id, 10) === intProjectId) {
                            return item.name;
                        }
                    })[0].name, "id": intProjectId}];
            spec.setSimpleName($scope.specification.title);
            specificationHttp.create($scope, spec, function (data) {
                $scope.SYNERGY.modal.update("Specification created", "");
                var id = data.substring(data.indexOf("=") + 1, data.length - 1);
                $location.path("specification/" + id);
                $scope.SYNERGY.modal.show();
            }, $scope.generalHttpFactoryError);
        };

// ATTACHMENT UPLOAD
        new SynergyHandlers.FileUploader([], "dropbox", $scope.SYNERGY.uploadFileLimit, $scope.SYNERGY.server.buildURL("attachment", {"id": $scope.id, "type": "specification"}), function (title, msg, level, style, fileName) {
            $scope.SYNERGY.logger.log(title, msg, level, style);
            $scope.fileName = fileName;
            specificationCache.resetCurrentSpecification();
            attachmentHttp.getAttachmentsForSpecification($scope, $scope.specification.id, function (data) {
                $scope.specification.attachments = data;
            }, function (data) {
                $scope.SYNERGY.logger.log("Failed to refresh list of attachments", data, "INFO", "alert-error");
                $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
                try {
                    if (!$scope.$$phase) {
                        $scope.$apply();
                    }
                } catch (e) {
                }
            });
        }, function (title, msg, level, style) {
            $scope.SYNERGY.logger.log("Action failed", msg, "INFO", "alert-error");
            $scope.SYNERGY.logger.log(title, msg, level, style);
            try {
                if (!$scope.$$phase) {
                    $scope.$apply();
                }
            } catch (e) {
            }
        });

        $scope.uploadFile = function () {
            new SynergyHandlers.FileUploader([], "dropbox", $scope.SYNERGY.uploadFileLimit, $scope.SYNERGY.server.buildURL("attachment", {"id": $scope.id, "type": "specification"}), function (title, msg, level, style, fileName) {
                $scope.SYNERGY.logger.log(title, msg, level, style);
                $scope.fileName = fileName;
                specificationCache.resetCurrentSpecification();
                attachmentHttp.getAttachmentsForSpecification($scope, $scope.specification.id, function (data) {
                    $scope.specification.attachments = data;
                }, function (data) {
                    $scope.SYNERGY.logger.log("Failed to refresh list of attachments", data, "INFO", "alert-error");
                    $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
                    try {
                        if (!$scope.$$phase) {
                            $scope.$apply();
                        }
                    } catch (e) {
                    }
                });
            }, function (title, msg, level, style) {

                $scope.SYNERGY.logger.log("Action failed", msg, "INFO", "alert-error");
                $scope.SYNERGY.logger.log(title, msg, level, style);
                try {
                    if (!$scope.$$phase) {
                        $scope.$apply();
                    }
                } catch (e) {
                }
            }).uploadFileFromFileChooser("fileToUpload");
        };
        var scope = $scope;
        $scope.init(function () {
            scope.fetch(true);
        });
    }
    /**
     * 
     * @param {SpecificationFct} specificationHttp
     * @param {SuiteFct} suiteHttp
     * @param {CasesFct} casesHttp
     * @returns {undefined}
     */
    function SuiteCtrl($scope, utils, $location, $routeParams, $timeout, specificationHttp, suiteHttp, casesHttp, productsHttp, sanitizerHttp, specificationCache, SynergyModels) {//authService
        $scope.$emit("updateNavbar", {item: "nav_specs"});
        $scope.suite = {};
        $scope.project = "";
        $scope.refreshCodemirror = false;
        $scope.id = $routeParams.id || -1;
        $scope.rights = 0;
        // for create only
        $scope.c_version = $routeParams.version || ""; // used on create page to display version
        $scope.c_specificationId = $routeParams.specification || ""; // used on create page to display specification link
        $scope.c_specification = {}; // used on create page to display specification title
        $scope.case_suggestions = []; // matching cases when adding existing case to suite
        $scope.caseToBeAdded = ""; // the name that user types in add existing case dialog
        $scope.availableProducts = false;
        $scope.products = [];
        $scope.oldNotification = "";
        $scope.components = [];

        var currentAction = "";
        var currentActionId = -1;

        /**
         * Loads data from server
         */
        $scope.fetch = function () {
            var action = window.location + "";
            action = action.substring(action.lastIndexOf("/") + 1);
            switch (action) {
                case "create":
                    if (parseInt($scope.c_specificationId, 10) > 0) {
                        specificationHttp.get($scope, true, $scope.c_specificationId, function (data) {
                            setLastSuiteOrder(data);
                            setProject(data);
                            $scope.c_specification = data;
                            loadProducts();
                        }, $scope.generalHttpFactoryError);
                    }
                    break;
                case "1":
                    if ($scope.id < 0) {
                        return;
                    }
                    var cachedSuite = specificationCache.getCurrentSuite(parseInt($scope.id, 10));
                    if (cachedSuite) {
                        $scope.suite = cachedSuite;
                        $scope.project = specificationCache.getCurrentProjectName();
                        $scope.$emit("updateBreadcrumbs", {link: "suite/" + $scope.id + "/v/1", title: $scope.suite.title});
                        try {
                            if ($scope.suite.controls.length > 0) {
                                $scope.rights = 1;
                            }
                        } catch (e) {
                        }
                        return;
                    }



                    suiteHttp.get($scope, true, $scope.id, function (data) {
                        $scope.suite = data;
                        setProject(data);
                        $scope.$emit("updateBreadcrumbs", {link: "suite/" + $scope.id + "/v/1", title: data.title});
                        try {
                            if (data.controls.length > 0) {
                                $scope.rights = 1;
                            }
                        } catch (e) {
                        }
                    }, $scope.generalHttpFactoryError);
                    break;
                default :
                    if ($scope.id < 0) {
                        return;
                    }
                    if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                        break;
                    }
                    suiteHttp.get($scope, false, $scope.id, function (data) {
                        $scope.suite = data;
                        setProject(data);
                        $scope.refreshCodemirror = true;
                        try {
                            if (data.controls.length > 0) {
                                $scope.rights = 1;
                            }
                        } catch (e) {
                        }

                        loadProducts();

                    }, $scope.generalHttpFactoryError);
                    break;
            }
        };

        function setProject(data) {
            if (data.hasOwnProperty("ext") && data.ext.hasOwnProperty("projects") && data.ext.projects.length > 0) {
                $scope.project = data.ext.projects[0].name;
            } else {
                $scope.project = $scope.SYNERGY.product;
            }
        }

        function setLastSuiteOrder(data) {
            try {
                $scope.suite.order = (data.testSuites[data.testSuites.length - 1].order + 1) || 1;
            } catch (e) {
                $scope.suite.order = 1;
            }
        }
        /**
         * Loads sanitized preview from server
         */
        $scope.loadPreview = function () {
            var _t = "<h1>" + ($scope.suite.title || "") + "</h1><h3>Setup</h3><div class='well'>" + ($scope.suite.desc || "") + "</div>";
            sanitizerHttp.getSanitizedInput($scope, _t, function (data) {
                $scope.preview = data;
            }, $scope.generalHttpFactoryError);
        };

        function loadProducts() {
            productsHttp.get($scope, function (data) {
                if (data.length > 0) {
                    var p = [];
                    for (var j = 0, max2 = data.length; j < max2; j++) {
                        p.push(new SynergyModels.Product(data[j].name, data[j].components));
                    }
                    $scope.products = p;
                    $scope.availableProducts = true;

                    var oldPreferences = $scope.SYNERGY.cache.get("product_component");
                    if (oldPreferences && (($scope.suite.product === "unknown" && $scope.suite.component === "unknown") || (typeof $scope.suite.product === "undefined"))) {
                        for (var i = 0, max = $scope.products.length; i < max; i++) {
                            if (data[i].name === oldPreferences.product) {
                                $scope.suite.product = $scope.products[i];//select current product in form
                                $scope.suite.component = oldPreferences.component;
                                setComponent(i);
                                $scope.oldNotification = "Selected product/component are based on previously used values and do not match actual settings of this suite";
                                return;
                            }
                        }
                    } else {
                        for (var i = 0, max = $scope.products.length; i < max; i++) {
                            if (data[i].name === $scope.suite.product || typeof $scope.suite.product === "undefined") {
                                $scope.suite.product = $scope.products[i];//select current product in form
                                setComponent(i);
                                return;
                            }
                        }
                    }
                    $scope.suite.product = $scope.products[0];
                    setComponent(0);

                }
            }, function () {
            });
        }

        function setComponent(productIndex) {
            $scope.components = $scope.products[productIndex].components;
            for (var i = 0, max = $scope.components.length; i < max; i++) {
                if ($scope.components[i].name === $scope.suite.component || typeof $scope.suite.component === "undefined" || (typeof $scope.suite.component.name !== "undefined" && $scope.components[i].name === $scope.suite.component.name)) {
                    $scope.suite.component = $scope.components[i];
                    return;
                }
            }

            $scope.suite.component = $scope.components[0];
        }

        $scope.productChanged = function () {
            for (var i = 0, max = $scope.products.length; i < max; i++) {
                if ($scope.products[i].name === $scope.suite.product.name) {
                    $scope.suite.product = $scope.products[i];//select current product in form
                    setComponent(i);
                    break;
                }
            }
        };

        /**
         * Starts with action on given test case
         * Otherwise confirmation dialog is opened
         * @param {String} action action name
         * @param {Number} id suite ID
         */
        $scope.performCase = function (action, id) {
            switch (action) {
                case "delete":
                    $("#deleteModalLabel").text("Delete test case?");
                    $("#deleteModalBody").html("<p>This will only remove reference to this test case in this suite. Continue?</p>");
                    $("#deleteModal").modal("toggle");
                    currentAction = "deleteCase";
                    currentActionId = id;
                    break;
                default:
                    $location.path("case/" + id + "/suite/" + $scope.id + "/" + action);
                    break;
            }
        };

        /**
         * Executes some action based on value of currentAction
         */
        $scope.performAction = function () {
            switch (currentAction) {
                case "deleteCase":
                    $scope.deleteCase();
                    break;
                case "deleteSuite":
                    $scope.deleteSuite();
                    break;
                default:
                    break;
            }
        };

        /**
         * Removes test case
         */
        $scope.deleteCase = function () {
            $("#deleteModal").modal("toggle");
            if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                return;
            }
            specificationCache.resetCurrentSpecification();
            var caseToBeRemovedId = parseInt(currentActionId, 10);
            suiteHttp.removeCase($scope, $scope.suite.id, currentActionId, function (data) {
                $scope.SYNERGY.logger.log("Done", "Test Case removed from test suite", "INFO", "alert-success");
                for (var i = 0, max = $scope.suite.testCases.length; i < max; i++) {
                    if ($scope.suite.testCases[i].id === caseToBeRemovedId) {
                        $scope.suite.testCases.splice(i, 1);
                        return;
                    }
                }
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Adds case to suite
         * @param {type} caseId
         */
        $scope.addCase = function (caseId) {
            $("#addCaseModal").modal("toggle");
            specificationCache.resetCurrentSpecification();
            suiteHttp.addCase($scope, $scope.id, caseId, function (data) {
                $scope.SYNERGY.logger.log("Done", "Test Case added to test suite", "INFO", "alert-success");
                $scope.fetch();
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Starts with action on given test suite
         * Otherwise confirmation dialog is opened
         * @param {String} action action name
         * @param {Number} id suite ID
         */
        $scope.performSuite = function (action) {
            switch (action) {
                case "delete":
                    $("#deleteModalLabel").text("Delete test suite?");
                    $("#deleteModalBody").html("<p>Do you really want to delete test suite?</p>");
                    $("#deleteModal").modal("toggle");
                    currentAction = "deleteSuite";
                    break;
                default:
                    $location.path("suite/" + $scope.suite.id + "/" + action);
                    break;
            }
        };

        $scope.cancel = function () {
            window.history.back();
        };

        /**
         * Deletes test suite
         */
        $scope.deleteSuite = function () {
            $("#deleteModal").modal("toggle");
            if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                return;
            }
            specificationCache.resetCurrentSpecification();
            suiteHttp.remove($scope, $scope.suite.id, function (data) {
                $scope.SYNERGY.modal.update("Test Suite deleted", "");
                $scope.SYNERGY.modal.show();
                $location.path("specification/" + $scope.suite.specificationId);
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Saves modifications in suite
         */
        $scope.save = function () {
            if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                return;
            }
            if ($scope.myForm.$invalid || $scope.myForm2.$invalid || typeof $scope.suite.desc === "undefined" || $scope.suite.desc.length < 0) {
                $scope.SYNERGY.modal.update("Missing required fields", "");
                $scope.SYNERGY.modal.show();
                return;
            }
            specificationCache.resetCurrentSpecification();
            var product = ($scope.availableProducts) ? $scope.suite.product.name : $scope.suite.product;
            var component = ($scope.availableProducts) ? $scope.suite.component.name : $scope.suite.component;
            var suite = new SynergyModels.Suite($scope.suite.title, $scope.suite.desc, product, component, $scope.suite.id);
            suite.order = $scope.suite.order;
            suiteHttp.edit($scope, suite, $scope.minorEdit ? true : false, function (data) {
                $scope.SYNERGY.modal.update("Test Suite updated", "");
                $scope.SYNERGY.modal.show();
                window.history.back();
            }, $scope.generalHttpFactoryError);

            if ($scope.availableProducts) {
                $scope.SYNERGY.cache.put("product_component", {"product": $scope.suite.product.name, "component": $scope.suite.component.name});
            }

        };

        /**
         * Creates a new test suite
         */
        $scope.create = function () {
            $scope.suite.specificationId = $scope.c_specificationId;

            if ($scope.myForm.$invalid || $scope.myForm2.$invalid || typeof $scope.suite.desc === "undefined" || $scope.suite.desc.length < 0) {
                $scope.SYNERGY.modal.update("Missing required fields", "");
                $scope.SYNERGY.modal.show();
                return;
            }

            if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                return;
            }
            specificationCache.resetCurrentSpecification();
            var product = ($scope.availableProducts) ? $scope.suite.product.name : $scope.suite.product;
            var component = ($scope.availableProducts) ? $scope.suite.component.name : $scope.suite.component;
            var suite = new SynergyModels.Suite($scope.suite.title, $scope.suite.desc, product, component, -1);
            suite.order = $scope.suite.order;
            suite.specificationId = $scope.suite.specificationId;
            suiteHttp.create($scope, suite, function (data) {
                $scope.SYNERGY.modal.update("Test Suite created", "");
                $scope.SYNERGY.modal.show();
                window.history.back();
            }, $scope.generalHttpFactoryError);

            if ($scope.availableProducts) {
                $scope.SYNERGY.cache.put("product_component", {"product": $scope.suite.product.name, "component": $scope.suite.component});
            }

        };

        /**
         * Displays dialog for adding existing cases to suite
         */
        $scope.showAddCaseModal = function () {
            $scope.case_suggestions = [];
            $scope.caseToBeAdded = "";
            $("#addCaseModal").modal("toggle");
        };

        /**
         * Reduces list of offered cases based on $scope.caseToBeAdded
         */
        $scope.filterCases = function () {
            $timeout(function () {
                casesHttp.getMatching($scope, $scope.caseToBeAdded, function (data) {
                    $scope.case_suggestions = data;
                }, $scope.generalHttpFactoryError);
            }, 600);
        };
// INIT
        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
    }
    /**
     * 
     * @param {SuiteFct} suiteHttp
     * @param {CaseFct} caseHttp
     * @param {ImageFct} imageHttp
     * @returns {undefined} */
    function CaseCtrl($scope, utils, $location, $routeParams, suiteHttp, caseHttp, imageHttp, issueHttp, labelHttp, sanitizerHttp, specificationCache, SynergyModels, SynergyHandlers) {//authService
        $scope.$emit("updateNavbar", {item: "nav_specs"});
        $scope.testCase = {};
        $scope.project = "";
        $scope.refreshCodemirror = false;
        $scope.id = $routeParams.id || -1;
        $scope.parentSuite = $routeParams.parent || -1;
        $scope.rights = 0;
        var currentAction = "";
        var currentActionId = -1;
        $scope.labelToBeAdded = "";
        $scope.preview = ($scope.parentSuite < 0) ? 1 : 0; // whether or not the case is displayed without suite context
        $scope.c_suite = {}; // used in create page to display suite information
        var originalDuration = 0; // used in edit page, when user submits edited case, when submitted duration is different than originalDuration, server restarts duration count

        $scope.loadPreview = function () {
            var _t = "<h1>" + ($scope.testCase.title || "") + "</h1><h3>Steps</h3><div>" + ($scope.testCase.steps || "") + "</div><h3>Expected result:</h3><div class='result well'>" + ($scope.testCase.result || "") + "</div>";
            sanitizerHttp.getSanitizedInput($scope, _t, function (data) {
                $scope.preview = data;
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Loads data from server
         */
        $scope.fetch = function (useCache) {
            var action = window.location + "";
            action = action.substring(action.lastIndexOf("/") + 1);
            switch (action) {
                case "create":
                    $scope.testCase.steps = "<ol>\n<li></li>\n<li></li>\n<li></li>\n<li></li>\n<ol>";
                    $scope.testCase.duration = 1;
                    if (parseInt($scope.parentSuite, 10) > 0) {
                        suiteHttp.get($scope, useCache, $scope.parentSuite, function (data) {
                            setProject(data);
                            setLastCaseOrder(data);

                            $scope.c_suite = data;
                        }, $scope.generalHttpFactoryError);
                    }
                    break;
                case "1":
                    if ($scope.id < 0) {
                        return;
                    }
                    var cachedCase = specificationCache.getCurrentCase(parseInt($scope.id, 10), parseInt($scope.parentSuite, 10));
                    if (cachedCase) {
                        $scope.testCase = cachedCase;
                        $scope.project = specificationCache.getCurrentProjectName();
                        $scope.$emit("updateBreadcrumbs", {link: "case/" + $scope.id + "/suite/" + $scope.parentSuite + "/v/1", title: $scope.testCase.title});
                        try {
                            if ($scope.testCase.controls.length > 0) {
                                $scope.rights = 1;
                            }
                        } catch (e) {
                        }
                        return;
                    }

                    caseHttp.get($scope, useCache, $scope.id, $scope.parentSuite, function (data) {
                        $scope.testCase = data;
                        setProject(data);
                        $scope.$emit("updateBreadcrumbs", {link: "case/" + $scope.id + "/suite/" + $scope.parentSuite + "/v/1", title: data.title});
                        try {
                            if (data.controls.length > 0) {
                                $scope.rights = 1;
                            }
                        } catch (e) {
                        }
                    }, $scope.generalHttpFactoryError);
                    break;
                default :
                    if ($scope.id < 0) {
                        return;
                    }
                    if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                        break;
                    }
                    caseHttp.get($scope, false, $scope.id, $scope.parentSuite, function (data) {
                        $scope.testCase = data;
                        setProject(data);
                        $scope.refreshCodemirror = true;
                        $scope.testCase.suiteId = $scope.parentSuite;
                        originalDuration = parseInt(data.duration, 10);
                        try {
                            if (data.controls.length > 0) {
                                $scope.rights = 1;
                            }
                        } catch (e) {
                        }
                    }, $scope.generalHttpFactoryError);
                    break;
            }
            // FIXME this is loaded twice on document load
        };

        function setProject(data) {
            if (data.hasOwnProperty("ext") && data.ext.hasOwnProperty("projects") && data.ext.projects.length > 0) {
                $scope.project = data.ext.projects[0].name;
            } else {
                $scope.project = $scope.SYNERGY.product;
            }
        }

        /**
         * When creating a new case, set order to be (previous case+1)
         * @param {Suite} data
         * @returns {undefined}
         */
        function setLastCaseOrder(data) {
            try {
                $scope.testCase.order = (data.testCases[data.testCases.length - 1].order + 1) || 1;
            } catch (e) {
                $scope.testCase.order = 1;
            }
        }

        /**
         * Creates a new test case
         */
        $scope.create = function () {
            if (parseInt($scope.parentSuite, 10) < 1) {// parent suite HAS to be defined
                return;
            }
            if ($scope.myForm.$invalid || $scope.myForm2.$invalid || typeof $scope.testCase.steps === "undefined" || typeof $scope.testCase.steps.length < 0 || typeof $scope.testCase.result === "undefined" || typeof $scope.testCase.result.length < 0) {
                $scope.SYNERGY.modal.update("Missing required fields", "");
                $scope.SYNERGY.modal.show();
                return;
            }
            specificationCache.resetCurrentSpecification();
            var testCase = new SynergyModels.TestCase($scope.testCase.title, $scope.testCase.steps, $scope.testCase.result, $scope.testCase.duration, -1);
            testCase.suiteId = $scope.parentSuite;
            testCase.order = $scope.testCase.order;
            if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                return;
            }
            caseHttp.create($scope, testCase, function (data) {
                $scope.SYNERGY.modal.update("Test Case created", "");
                $scope.SYNERGY.modal.show();
                window.history.back();
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Based on $scope.currentAction, performs some action
         */
        $scope.performAction = function () {
            switch (currentAction) {
                case "deleteCase":
                    $("#deleteModal").modal("toggle");
                    if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1 || $scope.parentSuite < 1) {
                        return;
                    }
                    specificationCache.resetCurrentSpecification();
                    suiteHttp.removeCase($scope, $scope.parentSuite, $scope.id, function (data) {
                        $scope.SYNERGY.modal.update("Test Case removed from test suite", "");
                        $scope.SYNERGY.modal.show();
                        window.history.back();
                    }, $scope.generalHttpFactoryError);
                    break;
                case "deleteImage":
                    var idToBeRemoved = currentActionId;
                    specificationCache.resetCurrentSpecification();
                    $("#deleteImageModal").modal("toggle");
                    imageHttp.remove($scope, currentActionId, $scope.parentSuite, function (data) {
                        $scope.SYNERGY.logger.log("Done", "Image removed", "INFO", "alert-success");
                        for (var i = 0, max = $scope.testCase.images.length; i < max; i++) {
                            if ($scope.testCase.images[i].id === idToBeRemoved) {
                                $scope.testCase.images.splice(i, 1);
                                return;
                            }
                        }
                    }, $scope.generalHttpFactoryError);
                    break;
                default:
                    break;
            }
        };

        /**
         * Starts with action on current test case
         * Otherwise confirmation dialog is opened
         * @param {String} action action name
         */
        $scope.performCase = function (action) {
            // delete could be done on a same page
            if ($scope.parentSuite < 0) {
                return;
            }
            switch (action) {
                case "delete":
                    $("#deleteModalLabel").text("Delete test case?");
                    $("#deleteModalBody").html("<p>This will only remove reference to this test case in this suite. Continue?</p>");
                    $("#deleteModal").modal("toggle");
                    currentAction = "deleteCase";
                    break;
                default:
                    $location.path("case/" + $scope.testCase.id + "/suite/" + $scope.parentSuite + "/" + action);
                    break;
            }
        };

        /**
         * Starts with action on given test case's image
         * Otherwise confirmation dialog is opened
         * @param {String} action action name
         * @param {Number} id image ID
         */
        $scope.performImage = function (action, id) {
            // delete could be done on a same page
            if ($scope.parentSuite < 0) {
                return;
            }
            switch (action) {
                case "delete":
                    $("#deleteImageModalLabel").text("Delete image?");
                    $("#deleteImageModalBody").html("<p>This will delete image from this test case. Continue?</p>");
                    $("#deleteImageModal").modal("toggle");
                    currentAction = "deleteImage";
                    currentActionId = id;
                    break;
                default:
                    $location.path("image/" + id + "/" + action);
                    break;
            }

        };

        $scope.cancel = function () {
            window.history.back();
        };

        /**
         * Saves modified test case to server
         * @param {Number} mode if 0, this test case will be cloned and modifications will be applied only to this suite, if 1 all suites will be affected
         */
        $scope.save = function (mode) {
            if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                return;
            }
            if ($scope.myForm.$invalid || $scope.myForm2.$invalid) {
                $scope.SYNERGY.modal.update("Missing required fields", "");
                $scope.SYNERGY.modal.show();
                return;
            }
            specificationCache.resetCurrentSpecification();
            var t_case = new SynergyModels.TestCase($scope.testCase.title, $scope.testCase.steps, $scope.testCase.result, $scope.testCase.duration, $scope.testCase.id);
            t_case.suiteId = $scope.parentSuite;
            t_case.order = $scope.testCase.order;
            t_case.orginalDuration = originalDuration;
            caseHttp.edit($scope, mode, t_case, $scope.minorEdit ? true : false, function (data) {
                $scope.SYNERGY.modal.update("Test Case updated", "");
                $scope.SYNERGY.modal.show();
                window.history.back();
            }, $scope.generalHttpFactoryError);
        };
        $scope.showAddIssueModal = function () {
            $scope.issueToBeAdded = "";
            $("#addIssueModal").modal("toggle");
        };
        $scope.addIssue = function () {
            specificationCache.resetCurrentSpecification();
            issueHttp.create($scope, {testCaseId: $scope.testCase.id, id: $scope.issueToBeAdded}, function (data) {
                $scope.SYNERGY.logger.log("Issue added", "", "INFO", "alert-success");
                $scope.testCase.issues.push({"bugId": $scope.issueToBeAdded, "title": "", "resolution": "", "id": -1});
            }, $scope.generalHttpFactoryError);
        };

        $scope.removeIssue = function (id) {
            specificationCache.resetCurrentSpecification();
            issueHttp.remove($scope, {testCaseId: $scope.testCase.id, id: id}, function (data) {
                $scope.SYNERGY.logger.log("Issue removed", "", "INFO", "alert-success");
                for (var i = 0, max = $scope.testCase.issues.length; i < max; i++) {
                    if ($scope.testCase.issues[i].bugId === id) {
                        $scope.testCase.issues.splice(i, 1);
                        return;
                    }
                }
            }, $scope.generalHttpFactoryError);
        };

        $scope.showAddLabelModal = function () {
            $scope.labelToBeAdded = "";
            $("#addLabelModal").modal("toggle");
        };
        $scope.addLabel = function () {
            specificationCache.resetCurrentSpecification();
            labelHttp.create($scope, {"label": $scope.labelToBeAdded, "testCaseId": $scope.testCase.id, "suiteId": $scope.parentSuite}, function (data) {
                $scope.SYNERGY.logger.log("Label added", "", "INFO", "alert-success");
                $scope.testCase.keywords.push($scope.labelToBeAdded.toLowerCase());
            }, $scope.generalHttpFactoryError);
        };
        $scope.removeLabel = function (label) {
            specificationCache.resetCurrentSpecification();
            labelHttp.remove($scope, {"label": label, "testCaseId": $scope.testCase.id, "suiteId": $scope.parentSuite}, function (data) {
                $scope.SYNERGY.logger.log("Label removed", "", "INFO", "alert-success");
                $scope.testCase.keywords.splice($scope.testCase.keywords.indexOf(label), 1);
            }, $scope.generalHttpFactoryError);
        };
// INIT
        var self = $scope;
        $scope.init(function () {
            self.fetch(true);
        });

        // D&D images

        var fu = new SynergyHandlers.FileUploader([], "fakeID", $scope.SYNERGY.uploadFileLimit, $scope.SYNERGY.server.buildURL("image", {"id": $scope.id, "suiteId": $scope.parentSuite, "title": encodeURIComponent($scope.imageTitle)}), function (title, msg, level, style, fileName) {
            $scope.SYNERGY.logger.log(title, msg, level, style);
            $scope.fileName = fileName;
            specificationCache.resetCurrentSpecification();
            imageHttp.getImagesForCase($scope, $scope.testCase.id, $scope.parentSuite, function (data) {
                $scope.testCase.images = data;
            }, function (data) {
                $scope.SYNERGY.logger.log("Unable to refresh list of images", "", "INFO", "alert-error");
                $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
            });
        }, function (title, msg, level, style) {
            $scope.SYNERGY.logger.log("Action failed", msg, "INFO", "alert-error");
            $scope.SYNERGY.logger.log(title, msg, level, style);
            try {
                if (!$scope.$$phase) {
                    $scope.$apply();
                }
            } catch (e) {
            }
        });
        fu.initForImages("dropbox");

        $scope.uploadFile = function () {
            fu.uploadImage($scope.SYNERGY.server.buildURL("image", {"id": $scope.id, "suiteId": $scope.parentSuite, "title": encodeURIComponent($scope.imageTitle)}));
        };

    }
    /**
     * 
     * @param {UserFct} userHttp
     */
    function ProfileCtrl($scope, $routeParams, userHttp, $location, SynergyModels, SynergyHandlers) {//authService
        $scope.$emit("updateNavbar", {item: "nav_home"});
        $scope.user = {authorOf: [], membership: [], favorites: [], assignments: []};
        $scope.username = $routeParams.user || "";
        $scope.rights = 0;
        $scope.passwordChangeAllowed = !$scope.SYNERGY.useSSO;
        $scope.updatePassword = false;

        $scope.isLoggedIn = (typeof $scope.SYNERGY.session.session_id !== "undefined" && $scope.SYNERGY.session.session_id.length > 1) ? 1 : 0;
        /**
         * Loads data from server
         */
        $scope.fetch = function () {
            $scope.isLoggedIn = (typeof $scope.SYNERGY.session.session_id !== "undefined" && $scope.SYNERGY.session.session_id.length > 1) ? 1 : 0;
            var action = window.location + "";
            action = action.substring(action.lastIndexOf("/") + 1);
            if ($scope.username.length < 1) {
                if (typeof $scope.SYNERGY.session.session_id !== "undefined" || $scope.SYNERGY.session.session_id.length > 1) {
                    $scope.username = $scope.SYNERGY.session.username;
                }
            }
            if ($scope.username.length < 1) {
                return;
            }
            userHttp.get($scope, $scope.username, function (data) {

                data.assignments.forEach(function (trun) {
                    if (trun.projectName === null || trun.projectName === "") {
                        trun.projectName = $scope.SYNERGY.product;
                    }
                });
                setProject(data.authorOf);
                setProject(data.ownerOf);
                setProject(data.favorites);
                $scope.user = data;
                if ($scope.username === $scope.SYNERGY.session.username) {
                    $scope.rights = 1;
                }

                $scope.$emit("updateBreadcrumbs", {link: "user/" + $scope.username, title: $scope.username});
            }, $scope.generalHttpFactoryError);
        };

        function setProject(specifications) {
            for (var i = 0, max = specifications.length; i < max; i++) {
                specifications[i]._project = specifications[i].ext.hasOwnProperty("projects") && specifications[i].ext.projects.length > 0 ? specifications[i].ext.projects[0].name : $scope.SYNERGY.product;
            }
        }

        $scope.editName = function () {
            if ($scope.rights === 1) {
                var invalidPassword = typeof $scope.user.password === "undefined" || $scope.user.password === null || $scope.user.password.length < 1;
                if ($scope.profileForm.$invalid || ($scope.updatePassword && invalidPassword)) {
                    $scope.SYNERGY.modal.update("Missing required fields", "");
                    $scope.SYNERGY.modal.show();
                    return;
                }

                var _u = new SynergyModels.User($scope.user.firstName, $scope.user.lastName, $scope.user.username, "", -1);
                _u.emailNotifications = $scope.user.emailNotifications;
                _u.email = $scope.user.email;
                if ($scope.updatePassword) {
                    _u.password = $scope.user.password;
                }
                userHttp.edit($scope, _u, function (data) {
                    $scope.SYNERGY.logger.log("Done", "updated", "INFO", "alert-success");
                }, $scope.generalHttpFactoryError);
            }
        };

        /**
         * Removes specification from user's list of favorites
         * @param {Number} id specification ID
         */
        $scope.toggleFavorite = function (id) {
            var spec = {"id": id, "isFavorite": 0};
            userHttp.toggleFavorite($scope, spec, function (data) {
                $scope.SYNERGY.logger.log("Done", "Specification removed from favorites", "INFO", "alert-success");
                for (var k = 0, max = $scope.user.favorites.length; k < max; k += 1) {
                    if (parseInt($scope.user.favorites[k].id, 10) === parseInt(id, 10)) {
                        $scope.user.favorites.splice(k, 1);
                        return;
                    }
                }
            }, $scope.generalHttpFactoryError);
        };
        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });

        $scope.uploadFile = function () {
            new SynergyHandlers.FileUploader([], "dropbox", $scope.SYNERGY.uploadFileLimit, $scope.SYNERGY.server.buildURL("profile_img", {"id": $scope.user.id}), function (title, msg, level, style, fileName, newSrc) {
                $scope.SYNERGY.logger.log(title, msg, level, style);
                $scope.user.profileImg = newSrc;
                try {
                    if (!$scope.$$phase) {
                        $scope.$apply();
                    }
                } catch (e) {
                }
            }, function (title, msg, level, style) {
                $scope.SYNERGY.logger.log("Action failed", msg, "INFO", "alert-error");
                try {
                    if (!$scope.$$phase) {
                        $scope.$apply();
                    }
                } catch (e) {
                }
            }).uploadFileFromFileChooser("fileToUpload");
        };

        $scope.resetFile = function () {
            userHttp.resetProfileImg($scope, $scope.user.id, function (data) {
                $scope.user.profileImg = data;
            }, $scope.generalHttpFactoryError);
        };

    }
    /**
     * @param {LabelFct} labelHttp
     * @returns {undefined} 
     */
    function LabelFilterCtrl($scope, $routeParams, labelHttp) {//authService
        $scope.$emit("updateNavbar", {item: "nav_home"});
        $scope.result = {};
        $scope.label = $routeParams.label || "";
        $scope.page = $routeParams.page || 1;
        $scope.next = 0;
        $scope.prev = 0;
        $scope.nextPage = 1;
        $scope.prevPage = 1;
        /**
         * Loads data from server
         */
        $scope.fetch = function () {
            if ($scope.label.length > 0) {
                labelHttp.findCases($scope, $scope.label, $scope.page, function (data) {
                    $scope.result = data;
                    $scope.$emit("updateBreadcrumbs", {link: "label/" + $scope.label + "/page/1", title: $scope.label});
                    $scope.next = (data.nextUrl.length > 1) ? 1 : 0;
                    $scope.prev = (data.prevUrl.length > 1) ? 1 : 0;
                    $scope.nextPage = parseInt($scope.page, 10) + 1;
                    $scope.prevPage = parseInt($scope.page, 10) - 1;
                }, $scope.generalHttpFactoryError);
            }
        };
        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
    }
    /**
     * @param {UsersFct} usersHttp
     * @param {TribeFct} tribeHttp description
     * @returns {undefined} 
     */
    function TribeCtrl($scope, $location, $routeParams, $timeout, usersHttp, tribeHttp, sanitizerHttp, specificationsHttp, SynergyUtils, SynergyModels) {//authService
        $scope.$emit("updateNavbar", {item: "nav_home"});
        $scope.tribe = {};
        $scope.id = $routeParams.id || -1;
        $scope.rights = 0;
        $scope.refreshCodemirror = false;
        var currentAction = "";
        var currentId = "";
        $scope.suggestions = [];
        $scope.users_suggestions = [];
        $scope.userToBeAdded = "pepa";
        $scope.specifications = [];
        $scope.newSpecification = -1;
        $scope.users = [];
        $scope.loadingUsers = false;
        $scope.toggleMembers = false;
        /**
         * Loads data from server
         */
        $scope.fetch = function () {
            var action = window.location + "";
            action = action.substring(action.lastIndexOf("/") + 1);
            if ($scope.id > 0) {
                tribeHttp.get($scope, false, $scope.id, function (data) {
                    setProject(data.ext);
                    $scope.tribe = data;
                    $scope.refreshCodemirror = true;
                    $scope.$emit("updateBreadcrumbs", {link: "tribe/" + $scope.id, title: data.name});
                    if ($scope.tribe.controls.length > 0) {
                        $scope.rights = 1;
                    }
                    if (action === "edit") {
                        loadSpecifications();
                        loadUsers();
                    }
                }, $scope.generalHttpFactoryError);
            }
        };

        function setProject(ext) {
            if (!ext.hasOwnProperty("specifications")) {
                return;
            }
            for (var i = 0, max = ext.specifications.length; i < max; i++) {
                ext.specifications[i]._project = ext.specifications[i].hasOwnProperty("projects") && ext.specifications[i].projects.length > 0 ? ext.specifications[i].projects[0].name : $scope.SYNERGY.product;
            }
        }

        $scope.loadPreview = function () {
            var _t = "<h1>" + ($scope.tribe.name || "") + "</h1><h3>Description</h3><div class='well'>" + ($scope.tribe.description || "") + "</div>";
            sanitizerHttp.getSanitizedInput($scope, _t, function (data) {
                $scope.preview = data;
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Loads all specifications (in edit page so tribe leader can add specification to tribe)
         */
        function loadSpecifications() {
            specificationsHttp.get($scope, "allRaw", function (data) {
                var d = [];
                var p;
                for (var i = 0, max = data.length; i < max; i += 1) {
                    p = data[i].ext.hasOwnProperty("projects") && data[i].ext.projects.length > 0 ? data[i].ext.projects[0].name : $scope.SYNERGY.product;
                    d[i] = {
                        title: data[i].title,
                        version: data[i].version,
                        value: data[i].title + " (" + p + " " + data[i].version + ")",
                        id: data[i].id
                    };
                }
                $scope.specifications = d;
            }, $scope.generalHttpFactoryError);
        }

        $scope.addSpecification = function () {
            var _index = parseInt($scope.newSpecification);
            for (var i = 0, max = $scope.tribe.ext.specifications.length; i < max; i++) {
                if (parseInt($scope.tribe.ext.specifications[i].id) === _index) {
                    $scope.SYNERGY.logger.log("Oops", "Specification already added to tribe", "INFO", "alert-info");
                    return;
                }
            }

            tribeHttp.addSpecification($scope, $scope.id, $scope.newSpecification, function (data, specificationId) {
                $scope.SYNERGY.logger.log("Done", "Specification added to tribe", "INFO", "alert-success");
                var matchingIndex = findSpecification(parseInt(specificationId, 10));
                if (matchingIndex > 0) {
                    $scope.tribe.ext.specifications.push($scope.specifications[matchingIndex]);
                }
            }, $scope.generalHttpFactoryError);
        };

        $scope.removeSpecification = function (specificationId) {
            tribeHttp.removeSpecification($scope, $scope.id, specificationId, function (data, specificationId) {
                $scope.SYNERGY.logger.log("Done", "Specification removed from tribe", "INFO", "alert-success");
                for (var i = 0, max = $scope.tribe.ext.specifications.length; i < max; i++) {
                    if (parseInt($scope.tribe.ext.specifications[i].id, 10) === specificationId) {
                        $scope.tribe.ext.specifications.splice(i, 1);
                        return;
                    }
                }
            }, $scope.generalHttpFactoryError);
        };

        function findSpecification(id) {
            for (var i = 0, max = $scope.specifications.length; i < max; i++) {
                if ($scope.specifications[i].id === id) {
                    return i;
                }
            }
            return -1;
        }

        function loadUsers() {
            if ($scope.users.length > 0) { // already loaded
                return;
            }
            $scope.loadingUsers = true;
            usersHttp.getAll($scope, function (data) {
                if (SynergyUtils.definedNotNull(data) && SynergyUtils.definedNotNull(data.users)) {
                    for (var i = 0, max = data.users.length; i < max; i++) {
                        data.users[i].displayName = data.users[i].firstName + " " + data.users[i].lastName + " (" + data.users[i].username + ")";
                    }
                }
                $scope.loadingUsers = false;
                $scope.users = data.users;
            }, function (data) {
                $scope.SYNERGY.logger.log("Action failed", "Unable to load list of users", "INFO", "alert-error");
                $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
            });
        }
        /**
         * Shows confirmation dialog to remove user from tribe and saves selected username as currentId
         * @param {String} username
         */
        $scope.removeFromTribe = function (username) {
            $("#deleteModalLabel").text("Remove user from tribe?");
            $("#deleteModalBody").html("<p>Do you really want to revoke user's membership?</p>");
            $("#deleteModal").modal("toggle");
            currentAction = "deleteUser";
            currentId = username;
        };

        /**
         * Removes users from tribe (calls server)
         * @param {String} username username of users to be removed from tribe
         */
        $scope.performRemoveFromTribe = function (username) {
            $("#deleteModal").modal("toggle");
            var memberToBeRemoved = username;
            tribeHttp.revokeMembership($scope, username, $scope.id, function (data) {
                $scope.SYNERGY.logger.log("Done", "User removed from tribe", "INFO", "alert-success");
                for (var i = 0, max = $scope.tribe.members.length; i < max; i++) {
                    if ($scope.tribe.members[i].username === memberToBeRemoved) {
                        $scope.tribe.members.splice(i, 1);
                        return;
                    }
                }
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Bases od $scope.currentAction it calls some method
         */
        $scope.performAction = function () {
            switch (currentAction) {
                case "deleteUser":
                    $scope.performRemoveFromTribe(currentId);
                    break;
                default:
                    break;
            }
        };

        /**
         * Performrs action with current tribe
         * @param {String} action unless it is "delete", it reroutes to tribe/id/action otherwise shows confirmation dialog
         */
        $scope.performTribeAction = function (action) {
            if (action !== "delete") {
                $location.path("tribe/" + $scope.id + "/" + action);
            }
        };

        $scope.cancel = function () {
            window.history.back();
        };

        /**
         * Saves modifications made to tribe
         * @returns {unresolved}
         */
        $scope.save = function () {
            if ($scope.myForm.$invalid || $scope.myForm2.$invalid || typeof $scope.tribe.description === "undefined" || $scope.tribe.description.length < 0) {
                $scope.SYNERGY.modal.update("Missing required fields", "");
                $scope.SYNERGY.modal.show();
                return;
            }
            var tribe = new SynergyModels.Tribe($scope.tribe.name, $scope.tribe.description, $scope.tribe.leaderUsername, $scope.tribe.id);
            tribeHttp.edit($scope, tribe, function (data) {
                $scope.SYNERGY.modal.update("Tribe updated", "");
                $scope.SYNERGY.modal.show();
                $location.path("tribe/" + $scope.tribe.id);
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Adds user of given username to tribe
         */
        $scope.addUser = function () {
            tribeHttp.newMembership($scope, {username: $scope.userToBeAdded}, $scope.tribe.id, function (data) {
                $scope.SYNERGY.logger.log("Done", "User added to tribe", "INFO", "alert-success");
                $scope.fetch();
            }, $scope.generalHttpFactoryError);
        };

        $scope.showAddUserModal = function () {
            $scope.toggleMembers = !$scope.toggleMembers;
            loadUsers();
        };
        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
    }
    /**
     * @param {RunsFct} runsHttp
     */
    function RunsCtrl($scope, $routeParams, runsHttp) {
        $scope.runs = [];
        $scope.page = $routeParams.page || 1;
        $scope.next = 0;
        $scope.prev = 0;
        $scope.nextPage = 1;
        $scope.prevPage = 1;

        /**
         * Retrieves list of test runs
         */
        $scope.fetch = function () {
            $scope.$emit("updateBreadcrumbs", {link: "runs", title: "Test Runs"});
            $scope.$emit("updateNavbar", {item: "nav_runs"});
            runsHttp.get($scope, $scope.page, function (data) {

                data.testRuns.forEach(function (trun) {
                    if (trun.projectName === null || trun.projectName === "") {
                        trun.projectName = $scope.SYNERGY.product;
                    }
                });

                $scope.runs = data;
                $scope.next = (data.nextUrl.length > 1) ? 1 : 0;
                $scope.prev = (data.prevUrl.length > 1) ? 1 : 0;
                $scope.nextPage = parseInt($scope.page, 10) + 1;
                $scope.prevPage = parseInt($scope.page, 10) - 1;
            }, $scope.generalHttpFactoryError, true);
        };
        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
    }
    /**
     * @param {AssignmentFct} assignmentHttp
     * @returns {undefined} */
    function AssignmentCtrl($scope, $routeParams, assignmentHttp) {//authService
        $scope.$emit("updateNavbar", {item: "nav_home"});
        $scope.assignment = {};
        $scope.currentCase = {};
        $scope.currentCaseId = -1;
        $scope.currentSuiteId = -1;
        $scope.id = $routeParams.id || -1;
        $scope.mode = $routeParams.mode || 2;
        $scope.project = {};
        $scope.timeLeft = 0;
        $scope.suiteIndex = 0;
        $scope.caseIndex = 0;
        $scope.casesFinished = 0;
        $scope.newIssue = "";
        $scope.attachmentBase = $scope.SYNERGY.server.buildURL("attachment", {});
        var timeFinished = 0;
        var _time = 0;
        $scope.failedAttempt = false;
        var started = 0;
        var cachedData = {};
        $scope.cacheDate = {};
        $scope.comments = [];
        $scope.allCases = [];
        $scope.suiteSetupDisplayed = false;
        $scope.toggleAction = "display";
        $scope.caseToPrint = {};
        $scope.errorMsg = "";
        $scope.somethingCompleted = false;
        $scope.pauseButtonTitle = "No test case has been completed yet or nothing has changed since resuming testing";
        /**
         * Loads data from server
         */
        $scope.fetch = function () {
            if ($scope.id > 0) {

                if (cachedDataExists()) {
                    $scope.cacheDate = cachedData.date;
                    $("#attemptModal").modal("toggle");
                    return;
                }

                assignmentHttp.getCommentTypes($scope, function (data) {
                    data.push({"name": "No comment", "id": -1});
                    $scope.comments = data;
                }, $scope.generalHttpFactoryError);

                switch ($scope.mode) {
                    case "1":
                        assignmentHttp.start($scope, $scope.id, function (data) {
                            $scope.assignment = data;
                            setProject(data.specificationData);
                            getTimeLeft();
                            collectCases();
                            $scope.casesFinished = parseInt(100 * (parseInt(data.completed, 10) / parseInt(data.total, 10)), 10) || 0;
                            parseData(0, 0, 0);
                            $scope.caseToPrint = $scope.allCases.filter(function (e) {
                                return e.caseId === $scope.currentCase.caseId && e.suiteId === $scope.currentCase.suiteId;
                            })[0];
                        }, $scope.generalHttpFactoryError);
                        break;
                    case "2":
                        assignmentHttp.restart($scope, $scope.id, function (data) {
                            $scope.assignment = data;
                            setProject(data.specificationData);
                            $scope.timeLeft = parseInt($scope.assignment.specificationData.estimation, 10);
                            collectCases();
                            $scope.casesFinished = parseInt(100 * (parseInt(data.completed, 10) / parseInt(data.total, 10)), 10) || 0;
                            parseData(0, 0, 0);
                            $scope.caseToPrint = $scope.allCases.filter(function (e) {
                                return e.caseId === $scope.currentCase.caseId && e.suiteId === $scope.currentCase.suiteId;
                            })[0];
                        }, $scope.generalHttpFactoryError);
                        break;
                    default:
                        break;
                }

            }
        };
        function setProject(data) {
            if (data.hasOwnProperty("ext") && data.ext.hasOwnProperty("projects") && data.ext.projects.length > 0) {
                $scope.project = data.ext.projects[0];
            } else {
                $scope.project = {"name": $scope.SYNERGY.product, id: -2};
            }
        }
        /**
         * Collects all cases to show them in navigation combo box together with their potential progress
         * @returns {undefined}
         */
        function collectCases() {
            var cases = [];
            for (var i = 0, max = $scope.assignment.specificationData.testSuites.length; i < max; i += 1) {
                for (var j = 0, max2 = $scope.assignment.specificationData.testSuites[i].testCases.length; j < max2; j += 1) {
                    var p = getProgressForCase($scope.assignment.specificationData.testSuites[i].testCases[j].id, $scope.assignment.specificationData.testSuites[i].id);
                    cases.push({
                        "name": $scope.assignment.specificationData.testSuites[i].testCases[j].title + " (" + $scope.assignment.specificationData.testSuites[i].title + ")" + ((parseInt(p.finished, 10) === 1) ? " - [" + p.result + "]" : ""),
                        "caseId": $scope.assignment.specificationData.testSuites[i].testCases[j].id,
                        "suiteId": $scope.assignment.specificationData.testSuites[i].id,
                        "progress": p,
                        "result": p.result
                    });
                }
            }
            $scope.allCases = cases;
        }

        /**
         * Sets common properties and prints case selected in combo box
         */
        $scope.traverseCase = function () {
            var oldSuiteId = $scope.currentSuiteId;
            $scope.currentCaseId = parseInt($scope.caseToPrint.caseId, 10);
            $scope.currentSuiteId = parseInt($scope.caseToPrint.suiteId, 10);

            var _s;
            for (var i = 0, max = $scope.assignment.progress.specification.testSuites.length; i < max; i += 1) {
                _s = $scope.assignment.progress.specification.testSuites[i];
                if (parseInt(_s.id, 10) === parseInt($scope.caseToPrint.suiteId, 10)) {
                    for (var j = 0, max2 = _s.testCases.length; j < max2; j += 1) {
                        if (parseInt(_s.testCases[j].id, 10) === parseInt($scope.caseToPrint.caseId, 10)) {
                            $scope.suiteIndex = i;
                            $scope.caseIndex = j;
                        }
                    }
                }
            }

            if (oldSuiteId !== $scope.currentSuiteId) {
                $scope.suiteSetupDisplayed = true;
                $scope.toggleAction = "hide";
            }

            printCase($scope.currentCaseId, $scope.currentSuiteId);
        };

        $scope.toggleSetup = function () {
            if ($scope.suiteSetupDisplayed) {
                $scope.toggleAction = "display";
            } else {
                $scope.toggleAction = "hide";
            }
            $scope.suiteSetupDisplayed = !$scope.suiteSetupDisplayed;
        };

        /**
         * Checks cache for possibly not submitted data
         * @returns {Boolean} true if there are cached data
         */
        function cachedDataExists() {
            cachedData = $scope.SYNERGY.cache.get("assignment_progress_" + $scope.id);
            return cachedData && cachedData.date && cachedData.progress ? true : false;
        }

        $scope.sendCached = function (send) {
            if (send) {
                $("#attemptModal").modal("toggle");
                $scope.assignment.progress = cachedData.progress;
                $scope.sendResults();
            } else {
                $scope.SYNERGY.cache.clear("assignment_progress_" + $scope.id);
                $scope.fetch();
            }
        };

        /**
         * Finds first unfinished test case. There are 2 approaches: iterate over progress and find matching test case or iterate over specification
         * data and find matching progress. The better is 2nd one because if specification has some new cases thar are not in this progress, it
         * can be dynamically adjusted.
         * @param {Number} caseStartIndex
         * @param {Number} suiteStartIndex
         * @param {Number} timeOffset
         */
        function parseData(caseStartIndex, suiteStartIndex, timeOffset) {
            var _s;
            // find 1st not yet tested test case
            for (var i = suiteStartIndex, max = $scope.assignment.progress.specification.testSuites.length; i < max; i += 1) {
                _s = $scope.assignment.progress.specification.testSuites[i];
                for (var j = caseStartIndex, max2 = _s.testCases.length; j < max2; j += 1) {
                    if (parseInt(_s.testCases[j].finished, 10) === 0) { // find first not finished case
                        $scope.currentCaseId = parseInt(_s.testCases[j].id, 10);
                        $scope.currentSuiteId = parseInt(_s.id, 10);
                        printCase($scope.currentCaseId, $scope.currentSuiteId);
                        $scope.suiteIndex = i;
                        $scope.caseIndex = j;
                        if (i !== suiteStartIndex || (caseStartIndex === 0 && suiteStartIndex === 0)) {
                            $scope.suiteSetupDisplayed = true;
                            $scope.toggleAction = "hide";
                        }
                        return;
                    }
                }
                caseStartIndex = 0; // to start from beginning in next suite
            }
            $scope.timeLeft = 0;
            $scope.sendResults(); // this happens if all cases are finished (otherwise return in the for statement above is used this code not executed)
        }

        function getTimeLeft() {
            var _s;
            for (var i = 0, max = $scope.assignment.progress.specification.testSuites.length; i < max; i += 1) {
                _s = $scope.assignment.progress.specification.testSuites[i];
                for (var j = 0, max2 = _s.testCases.length; j < max2; j += 1) {
                    if (parseInt(_s.testCases[j].finished, 10) !== 0) {
                        if (_s.testCases[j].hasOwnProperty("originalDuration")) {
                            timeFinished += parseInt(Math.round(_s.testCases[j].originalDuration), 10);
                        } else {
                            timeFinished += parseInt(Math.round(_s.testCases[j].duration), 10);
                        }
                    }
                }

            }
            $scope.timeLeft = parseInt($scope.assignment.specificationData.estimation, 10) - timeFinished;
        }

        /**
         * Sends results of testing to server
         */
        $scope.sendResults = function () {
            if ($scope.failedAttempt) {
                $("#deleteModal").modal("toggle");
            }
            $scope.showWaitDialog();
            assignmentHttp.submitResults($scope, $scope.id, $scope.assignment.progress, function (data) {
                $scope.SYNERGY.modal.update("Results submitted", "Thank you for testing");
                $scope.SYNERGY.modal.show();
                $scope.SYNERGY.cache.clear("assignment_progress_" + $scope.id);
                $scope.assignment = {};
                $scope.currentCase = {};
                $scope.currentCaseId = -1;
                $scope.currentSuiteId = -1;
                $scope.id = $routeParams.id || -1;
                $scope.mode = $routeParams.mode || 2;
                $scope.timeLeft = 0;
                $scope.suiteIndex = 0;
                $scope.caseIndex = 0;
                $scope.casesFinished = 0;
                $scope.newIssue = "";
                $scope.failedAttempt = false;
                window.history.back();
            }, function (data) {
                $scope.errorMsg = data || "";
                $scope.SYNERGY.modal.show();// hide wait dialog
                if (!$scope.failedAttempt) {
                    $scope.failedAttempt = true;
                    $("#deleteModal").modal("toggle");
                }
                $scope.SYNERGY.logger.log("Action failed", data, "INFO", "alert-error");
                $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
            });
        };

        /**
         * Procceeds to the next case in testing. Based on result parameter it marks test case as passed, failed or skipped
         * and if $scope.trackCaseDuration, it sets new duration of the case (not that it sends time to server but server must be
         *  configured to track case duration as well)
         * @param {type} result
         * @returns {unresolved}
         */
        $scope.next = function (result) {

            if (parseInt($scope.assignment.completed, 10) === parseInt($scope.assignment.total, 10)) {
                $scope.SYNERGY.modal.update("Testing finished", "");
                $scope.SYNERGY.modal.show();
                return;
            }
            var alreadyTested = (parseInt($scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].finished, 10) === 1) ? true : false;
            switch (result) {
                case "passed":
                    if (!alreadyTested) {
                        $scope.assignment.completed++;
                        $scope.casesFinished = parseInt(100 * (parseInt($scope.assignment.completed, 10) / parseInt($scope.assignment.total, 10)), 10);

                        if ($scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].hasOwnProperty("originalDuration")) {
                            timeFinished += parseInt(Math.round($scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].originalDuration), 10);
                        } else {
                            timeFinished += parseInt(Math.round($scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].duration), 10);
                        }
                        $scope.timeLeft = parseInt($scope.assignment.specificationData.estimation, 10) - timeFinished;

                    }

                    var timeTaken = (new Date().getTime()) - started;
                    $scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].finished = 1;
                    $scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].result = result;
                    $scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].comment = $scope.currentCase.comment;
                    $scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].commentFreeText = ($scope.currentCase.comment !== -1) ? $scope.currentCase.commentFreeText.substr(0, 100) : "";

                    if (!$scope.SYNERGY.trackCaseDuration) {
                        timeTaken = $scope.currentCase.duration * 60000;
                    }

                    if ($scope.newIssue.length > 0) {
                        $scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].issue = $scope.newIssue.split(" ");
                    } else {
                        $scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].issue = "";
                    }
                    $scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].duration = timeTaken;
                    $scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].originalDuration = $scope.currentCase.duration;
                    parseData($scope.caseIndex + 1, $scope.suiteIndex, $scope.currentCase.duration);
                    break;
                case "failed":
                    if ($scope.newIssue.length > 0) {
                        if (!alreadyTested) {
                            $scope.assignment.completed++;
                            $scope.casesFinished = parseInt(100 * (parseInt($scope.assignment.completed, 10) / parseInt($scope.assignment.total, 10)), 10);

                            if ($scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].hasOwnProperty("originalDuration")) {
                                timeFinished += parseInt(Math.round($scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].originalDuration), 10);
                            } else {
                                timeFinished += parseInt(Math.round($scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].duration), 10);
                            }
                            $scope.timeLeft = parseInt($scope.assignment.specificationData.estimation, 10) - timeFinished;

                        }
                        var timeTaken = (new Date().getTime()) - started;
                        if (!$scope.SYNERGY.trackCaseDuration) {
                            timeTaken = $scope.currentCase.duration * 60000;
                        }



                        $scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].issue = $scope.newIssue.split(" ");
                        $scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].finished = 1;
                        $scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].result = result;
                        $scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].duration = timeTaken;
                        $scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].originalDuration = $scope.currentCase.duration;
                        $scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].comment = $scope.currentCase.comment;
                        $scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].commentFreeText = ($scope.currentCase.comment !== -1) ? $scope.currentCase.commentFreeText.substr(0, 100) : "";
                        parseData($scope.caseIndex + 1, $scope.suiteIndex, $scope.currentCase.duration);

                    } else {
                        $scope.SYNERGY.modal.update("Missing issue number", "");
                        $scope.SYNERGY.modal.show();
                        return;
                    }
                    break;
                case "skipped":
                    if ($scope.currentCase.comment === -1) {
                        $scope.SYNERGY.modal.update("Missing reason for skipping", "Please select comment using the combo box below Skip button");
                        $scope.SYNERGY.modal.show();
                        return;
                    }



                    if (!alreadyTested) {
                        $scope.assignment.completed++;
                        $scope.casesFinished = parseInt(100 * (parseInt($scope.assignment.completed, 10) / parseInt($scope.assignment.total, 10)), 10);
                        if ($scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].hasOwnProperty("originalDuration")) {
                            timeFinished += parseInt(Math.round($scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].originalDuration), 10);
                        } else {
                            timeFinished += parseInt(Math.round($scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].duration), 10);
                        }
                        $scope.timeLeft = parseInt($scope.assignment.specificationData.estimation, 10) - timeFinished;
                    }
                    $scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].issue = "";
                    $scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].finished = 1;
                    $scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].result = result;
                    $scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].originalDuration = $scope.currentCase.duration;
                    $scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].comment = $scope.currentCase.comment;
                    $scope.assignment.progress.specification.testSuites[$scope.suiteIndex].testCases[$scope.caseIndex].commentFreeText = ($scope.currentCase.comment !== -1) ? $scope.currentCase.commentFreeText.substr(0, 100) : "";
                    parseData($scope.caseIndex + 1, $scope.suiteIndex, $scope.currentCase.duration);
                    break;
                default:
                    break;
            }
            $scope.somethingCompleted = true;
            $scope.pauseButtonTitle = "Save current progress and continue later";
            $scope.SYNERGY.cache.put("assignment_progress_" + $scope.id, {"date": new Date().toString(), "progress": $scope.assignment.progress});
            collectCases();
            $scope.caseToPrint = $scope.allCases.filter(function (e) {
                return e.caseId === $scope.currentCase.caseId && e.suiteId === $scope.currentCase.suiteId;
            })[0];
            $scope.SYNERGY.util.scrollTo("caseTitle");
        };

        /**
         * It goes through specification and sets case in suite with suiteId with caseId to current so it is displayed to user.
         * If caseId is -1, first case in first suite is displayed
         * @param {Number} caseId
         * @param {Number} suiteId
         */
        function printCase(caseId, suiteId) {
            $scope.newIssue = "";
            if (caseId === -1) {
                for (var i = 0, max = $scope.assignment.specificationData.testSuites.length; i < max; i += 1) {
                    for (var j = 0, max2 = $scope.assignment.specificationData.testSuites[i].testCases.length; j < max2; j += 1) {

                        $scope.suiteIndex = i;
                        $scope.caseIndex = j;
                        started = new Date().getTime();
                        $scope.currentCase = {
                            "title": $scope.assignment.specificationData.testSuites[i].testCases[j].title,
                            "caseId": $scope.assignment.specificationData.testSuites[i].testCases[j].id,
                            "suiteId": $scope.assignment.specificationData.testSuites[i].id,
                            "images": $scope.assignment.specificationData.testSuites[i].testCases[j].images,
                            "duration": parseInt($scope.assignment.specificationData.testSuites[i].testCases[j].duration, 10),
                            "steps": $scope.assignment.specificationData.testSuites[i].testCases[j].steps,
                            "suiteTitle": $scope.assignment.specificationData.testSuites[i].title,
                            "result": $scope.assignment.specificationData.testSuites[i].testCases[j].result,
                            "issues": $scope.assignment.specificationData.testSuites[i].testCases[j].issues,
                            "suiteSetup": $scope.assignment.specificationData.testSuites[i].desc,
                            "product": $scope.assignment.specificationData.testSuites[i].product,
                            "comment": -1,
                            "commentFreeText": "",
                            "progress": {"finished": 0, "id": caseId, "result": "", "duration": 0, "issue": [], "comment": -1, "commentFreeText": ""},
                            "component": $scope.assignment.specificationData.testSuites[i].component
                        };
                        return;
                    }
                }

                return;
            }
            for (var i = 0, max = $scope.assignment.specificationData.testSuites.length; i < max; i += 1) {
                if (suiteId === parseInt($scope.assignment.specificationData.testSuites[i].id, 10)) {
                    for (var j = 0, max2 = $scope.assignment.specificationData.testSuites[i].testCases.length; j < max2; j += 1) {
                        if (caseId === parseInt($scope.assignment.specificationData.testSuites[i].testCases[j].id, 10)) {
                            $scope.currentCase = {
                                "title": $scope.assignment.specificationData.testSuites[i].testCases[j].title,
                                "caseId": $scope.assignment.specificationData.testSuites[i].testCases[j].id,
                                "suiteId": $scope.assignment.specificationData.testSuites[i].id,
                                "images": $scope.assignment.specificationData.testSuites[i].testCases[j].images,
                                "duration": parseInt($scope.assignment.specificationData.testSuites[i].testCases[j].duration, 10),
                                "steps": $scope.assignment.specificationData.testSuites[i].testCases[j].steps,
                                "suiteTitle": $scope.assignment.specificationData.testSuites[i].title,
                                "result": $scope.assignment.specificationData.testSuites[i].testCases[j].result,
                                "issues": $scope.assignment.specificationData.testSuites[i].testCases[j].issues,
                                "suiteSetup": $scope.assignment.specificationData.testSuites[i].desc,
                                "product": $scope.assignment.specificationData.testSuites[i].product,
                                "comment": -1,
                                "commentFreeText": "",
                                "progress": getProgressForCase(caseId, suiteId),
                                "component": $scope.assignment.specificationData.testSuites[i].component
                            };
                            initValuesFromProgress();
                            started = new Date().getTime();
                            return;
                        }
                    }
                }
            }
        }


        function initValuesFromProgress() {
            // issues
            if ($scope.currentCase.progress.issue && $scope.currentCase.progress.issue.length > 0) {
                $scope.newIssue = $scope.currentCase.progress.issue.join(" ");
            }
            // comment
            if ($scope.currentCase.progress.comment && $scope.currentCase.progress.comment > 0) {
                $scope.currentCase.comment = $scope.currentCase.progress.comment;
                $scope.currentCase.commentFreeText = ($scope.currentCase.progress.hasOwnProperty("commentFreeText")) ? $scope.currentCase.progress.commentFreeText : "";
            }
        }

        function getProgressForCase(caseId, suiteId) {
            for (var i = 0, max = $scope.assignment.progress.specification.testSuites.length; i < max; i++) {
                if (parseInt($scope.assignment.progress.specification.testSuites[i].id, 10) === parseInt(suiteId, 10)) {
                    for (var j = 0, max2 = $scope.assignment.progress.specification.testSuites[i].testCases.length; j < max2; j++) {
                        if (parseInt($scope.assignment.progress.specification.testSuites[i].testCases[j].id, 10) === parseInt(caseId, 10)) {
                            return $scope.assignment.progress.specification.testSuites[i].testCases[j];
                        }
                    }
                }
            }
            return {"finished": 0, "id": caseId, "result": "", "duration": 0, "issue": [], "comment": -1};
        }

        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
    }
// ADMINISTRATION
    function AdminHomeCtrl($scope) {
        if (!$scope.SYNERGY.session.hasAdminRights()) {
            return;
        }

    }
    /**
     * 
     * @param {VersionsFct} versionsHttp
     * @param {VersionFct} versionHttp
     */
    function AdminVersionCtrl($scope, versionsHttp, versionHttp, SynergyModels) {

        $scope.versions = [];
        $scope.versionAffectedId = 0; // when editing version, modal dialog is opened, this is to show version name when typing new name
        $scope.versionAffected = "";
        $scope.newname = "";

        $scope.fetch = function () {
            if (!$scope.SYNERGY.session.hasAdminRights()) {
                return;
            }
            versionsHttp.getAll($scope, function (data) {
                $scope.versions = data;
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Based on action parameter, it displays some window and sets versionId and version as to be affected by future modifications
         * @param {String} action
         * @param {Number} versionId
         * @param {String} version
         */
        $scope.perform = function (action, versionId, version, isObsolete) {
            switch (action) {
                case "edit":
                    $scope.newname = version;
                    $scope.isObsolete = (parseInt(isObsolete, 10) === 1) ? true : false;
                    $scope.versionAffectedId = versionId;
                    $scope.versionAffected = version;
                    $("#editVersionModal").modal("toggle");
                    break;
                case "create":
                    $scope.newname = "";
                    $("#createVersionModal").modal("toggle");
                    break;
                case "delete":
                    $scope.versionAffectedId = versionId;
                    $scope.versionAffected = version;
                    $("#deleteModal").modal("toggle");
                    break;
                default :
                    break;
            }
        };

        /**
         * Renames version
         */
        $scope.rename = function () {

            if ($scope.myForm.$invalid) {
                $scope.SYNERGY.modal.update("Missing required fields", "");
                $scope.SYNERGY.modal.show();
                return;
            }

            versionHttp.edit($scope, new SynergyModels.Version($scope.newname, $scope.versionAffectedId, $scope.isObsolete), function () {
                $scope.SYNERGY.logger.log("Done", "Version renamed", "INFO", "alert-success");
                $scope.fetch();
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Deletes version
         */
        $scope.remove = function () {
            $("#deleteModal").modal("toggle");
            versionHttp.remove($scope, $scope.versionAffectedId, function () {
                $scope.SYNERGY.logger.log("Done", "Version removed", "INFO", "alert-success");
                $scope.fetch();
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Creates a new version
         */
        $scope.create = function () {

            if ($scope.myForm2.$invalid) {
                $scope.SYNERGY.modal.update("Missing required fields", "");
                $scope.SYNERGY.modal.show();
                return;
            }
            versionHttp.create($scope, new SynergyModels.Version($scope.newname, -1, false), function (data) {
                $scope.SYNERGY.logger.log("Done", "Version created", "INFO", "alert-success");
                $scope.fetch();
            }, $scope.generalHttpFactoryError);
        };
        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
    }
    /**
     * 
     * @param {type} $scope
     * @param {PlatformsFct} platformsHttp
     * @param {PlatformFct} platformHttp
     * @returns {undefined} */
    function AdminPlatformsCtrl($scope, platformsHttp, platformHttp, SynergyModels) {

        $scope.platforms = [];
        $scope.platformAffectedId = 0;
        $scope.platformAffected = "";
        $scope.newname = "";

        $scope.fetch = function () {
            if (!$scope.SYNERGY.session.hasAdminRights()) {
                return;
            }
            platformsHttp.get($scope, function (data) {
                $scope.platforms = data;
            }, $scope.generalHttpFactoryError, false);
        };

        /**
         * Opens dialog based on action value and saves $scope.platformAffectedId and $scope.platformAffected (platform name and ID to be affected
         * by future changes)
         * @param {String} action action name
         * @param {Number} platformId platform ID
         * @param {Number} platform platform name
         */
        $scope.perform = function (action, platformId, platform, isActive) {
            switch (action) {
                case "edit":
                    $scope.platformAffectedId = platformId;
                    $scope.platformAffected = platform;
                    $scope.newname = platform;
                    $scope.isActive = (parseInt(isActive, 10) === 1) ? true : false;
                    $("#editPlatformModal").modal("toggle");
                    break;
                case "create":
                    $("#createPlatformModal").modal("toggle");
                    break;
                case "delete":
                    $scope.platformAffectedId = platformId;
                    $scope.platformAffected = platform;
                    $("#deleteModal").modal("toggle");
                    break;
                default :
                    break;
            }
        };

        /**
         * Removes platform
         */
        $scope.remove = function () {
            $("#deleteModal").modal("toggle");
            platformHttp.remove($scope, $scope.platformAffectedId, function (data) {
                $scope.SYNERGY.logger.log("Done", "Platform removed", "INFO", "alert-success");
                $scope.fetch();
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Renames platform
         */
        $scope.rename = function () {

            if ($scope.myForm.$invalid) {
                $scope.SYNERGY.modal.update("Missing required fields", "");
                $scope.SYNERGY.modal.show();
                return;
            }

            platformHttp.edit($scope, new SynergyModels.Platform($scope.newname, $scope.platformAffectedId, $scope.isActive), function (data) {
                $scope.SYNERGY.logger.log("Done", "Platform renamed", "INFO", "alert-success");
                $scope.fetch();
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Creates a new platform
         * @returns {unresolved}
         */
        $scope.create = function () {

            if ($scope.myForm2.$invalid) {
                $scope.SYNERGY.modal.update("Missing required fields", "");
                $scope.SYNERGY.modal.show();
                return;
            }

            platformHttp.create($scope, new SynergyModels.Platform($scope.newname, -1), function (data) {
                $scope.SYNERGY.logger.log("Done", "Platform created", "INFO", "alert-success");
                $scope.fetch();
            }, $scope.generalHttpFactoryError);
        };
        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
    }
    /**
     * @param {UsersFct} usersHttp
     * @param {TribeFct} tribeHttp
     * @param {TribesFct} tribesHttp
     * @returns {undefined} */
    function AdminTribesCtrl($scope, $location, $timeout, usersHttp, tribeHttp, tribesHttp, sanitizerHttp, SynergyUtils, SynergyModels) {

        $scope.tribes = [];
        $scope.tribe = {}; // for new tribe page
        $scope.tribeAffectedId = 0;
        $scope.tribeAffected = "";
        $scope.newname = "";
        $scope.users = [];
        $scope.importTribesUrl = "http://" + window.location.host + "/dashboard/web/a_tribes.php?export=true";

        $scope.fetch = function () {
            if (!$scope.SYNERGY.session.hasAdminRights()) {
                return;
            }
            loadUsers();
            tribesHttp.get($scope, function (data) {
                $scope.tribes = data;
            }, $scope.generalHttpFactoryError);
        };


        $scope.importTribes = function () {
            tribesHttp.importTribes($scope, $scope.importTribesUrl, function (data) {
                $scope.SYNERGY.logger.log("Done", data + " tribes imported", "INFO", "alert-success");
                $scope.fetch();
            }, $scope.generalHttpFactoryError);
        };


        /**
         * Removes tribe
         */
        $scope.deleteTribe = function () {
            $("#deleteModal").modal("toggle");
            tribeHttp.remove($scope, $scope.tribeAffectedId, function (data) {
                $scope.SYNERGY.logger.log("Done", "Tribe removed", "INFO", "alert-success");
                $scope.fetch();
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Redirects to URL based on action value
         * @param {String} action action to be done
         * @param {String} id Tribe ID
         * @param {String} tribe Tribe name
         */
        $scope.perform = function (action, id, tribe) {
            switch (action) {
                case "create":
                    $location.path("administration/tribes/" + action);
                    break;
                default :
                    break;
            }
        };

        /**
         * Shows delete confirmation dialog
         * @param {Number} id tribe ID to be removed
         * @param {String} tribe tribe name
         */
        $scope.deleteModal = function (id, tribe) {
            $scope.tribeAffectedId = id;
            $scope.tribeAffected = tribe;
            $("#deleteModal").modal("toggle");
        };

        function loadUsers() {
            usersHttp.getAll($scope, function (data) {
                if (SynergyUtils.definedNotNull(data) && SynergyUtils.definedNotNull(data.users)) {
                    for (var i = 0, max = data.users.length; i < max; i++) {
                        data.users[i].displayName = data.users[i].firstName + " " + data.users[i].lastName + " (" + data.users[i].username + ")";
                    }
                }

                $scope.users = data.users;
                if (SynergyUtils.definedNotNull(data) && data.users.length > 0) {
                    $scope.tribe.leaderUsername = data.users[0].displayName;
                }
            }, $scope.generalHttpFactoryError);
        }

        $scope.loadPreview = function () {
            var _t = "<h1>" + ($scope.tribe.name || "") + "</h1><h3>Description</h3><div class='well'>" + ($scope.tribe.description || "") + "</div>";
            sanitizerHttp.getSanitizedInput($scope, _t, function (data) {
                $scope.preview = data;
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Creates a new tribe
         */
        $scope.create = function () {

            if ($scope.myForm2.$invalid || $scope.myForm.$invalid) {
                $scope.SYNERGY.modal.update("Missing required fields", "");
                $scope.SYNERGY.modal.show();
                return;
            }
            var tribe = new SynergyModels.Tribe($scope.tribe.name, $scope.tribe.description, ($scope.tribe.leaderUsername), -1);
            tribeHttp.create($scope, tribe, function (data) {
                $scope.SYNERGY.modal.update("Tribe created", "");
                $scope.SYNERGY.modal.show();
                window.history.back();
            }, $scope.generalHttpFactoryError);
        };

        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
    }
    /**
     * @param {RunsFct} runsHttp
     * @param {RunFct} runHttp
     */
    function AdminRunsCtrl($scope, $location, $routeParams, runsHttp, runHttp) {
        $scope.runs = [];
        $scope.page = $routeParams.page || 1;
        $scope.orderProp = "title";
        $scope.next = 0;
        $scope.prev = 0;
        $scope.nextPage = 1;
        $scope.prevPage = 1;
        var currentActionId = -1;
        var currentAction = "";

        $scope.fetch = function () {
            if (!$scope.SYNERGY.session.hasAdminRights()) {
                return;
            }
            runsHttp.get($scope, $scope.page, function (data) {

                data.testRuns.forEach(function (trun) {
                    if (trun.projectName === null || trun.projectName === "") {
                        trun.projectName = $scope.SYNERGY.product;
                    }
                });

                $scope.runs = data;
                $scope.next = (data.nextUrl.length > 1) ? 1 : 0;
                $scope.prev = (data.prevUrl.length > 1) ? 1 : 0;
                $scope.nextPage = parseInt($scope.page, 10) + 1;
                $scope.prevPage = parseInt($scope.page, 10) - 1;
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Unless action is delete, it redirects to action URL, otherwise opens confirmation dialog
         * @param {String} action action
         * @param {Number} id run ID
         * @param {String} run run name
         */
        $scope.perform = function (action, id, run) {
            switch (action) {
                case "delete":
                    $("#deleteModalLabel").text("Delete test run?");
                    $("#deleteModalBody").html("<p>This action will also delete all test assignments for this test run. Do you want to continue?</p>");
                    $("#deleteModal").modal("toggle");
                    currentActionId = id;
                    currentAction = "delete";
                    break;
                case "notify":
                    $("#deleteModalLabel").text("Send notifications?");
                    $("#deleteModalBody").html("<p>Do you really want to send email notifications to testers with incomplete test assignment?</p>");
                    $("#deleteModal").modal("toggle");
                    currentAction = "notify";
                    currentActionId = id;
                    break;
                case "freeze":
                    var _run = $scope.runs.testRuns.filter(function (e) {
                        return e.id === id;
                    })[0];

                    var target = (_run.isActive ? 0 : 1);
                    runHttp.freezeRun($scope, id, target, function (data) {
                        _run = $scope.runs.testRuns.filter(function (e) {
                            return e.id === id;
                        })[0];
                        _run.isActive = target;
                        $scope.SYNERGY.logger.log("Done", "Test run " + (target === 1 ? "unfrozen" : "frozen"), "INFO", "alert-success");
                    }, $scope.generalHttpFactoryError);
                    break;
                default:
                    $location.path("administration/run/" + id + "/" + action);
                    break;
            }
        };

        $scope.performAction = function () {
            switch (currentAction) {
                case "delete":
                    remove();
                    break;
                case "notify":
                    $("#deleteModal").modal("toggle");
                    runHttp.sendNotifications($scope, currentActionId, function (data) {
                        $scope.SYNERGY.logger.log("Done", data, "INFO", "alert-success");
                    }, function (data) {
                        $scope.SYNERGY.logger.log("Action failed", "", "INFO", "alert-error");
                        $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
                    });
                    break;
                default:
                    break;
            }
        };

        /**
         * Removes test run
         */
        function remove() {
            $("#deleteModal").modal("toggle");
            runHttp.remove($scope, currentActionId, function (data) {
                $scope.SYNERGY.logger.log("Done", "Test run removed", "INFO", "alert-success");
                $scope.fetch();
            }, $scope.generalHttpFactoryError);
        }
        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
    }
    /**
     * @param {RunFct} runHttp
     */
    function AdminRunCtrl($scope, $routeParams, runHttp, $location, attachmentHttp, sanitizerHttp, projectsHttp, SynergyModels, SynergyHandlers) {
        $scope.attachmentBase = $scope.SYNERGY.server.buildURL("run_attachment", {});
        $scope.refreshCodemirror = false;
        $scope.projects = [];
        $scope.testRun = new SynergyModels.TestRun("", "", "", "", "");
        $scope.id = $routeParams.id || -1;
        var currentAction = "";
        var currentActionId = -1;

        try {
            $("#start").datetimepicker({dateFormat: "yy-mm-dd", timeFormat: "HH:mm:ss"});
            $("#end").datetimepicker({dateFormat: "yy-mm-dd", timeFormat: "HH:mm:ss"});
        } catch (e) {
        }
        $scope.loadPreview = function () {
            var _t = "<h1>" + ($scope.testRun.title || "") + "</h1><h3>Description</h3><div class='well'>" + ($scope.testRun.desc || "") + "</div>";
            sanitizerHttp.getSanitizedInput($scope, _t, function (data) {
                $scope.preview = data;
            }, $scope.generalHttpFactoryError);
        };
        /**
         * Starts with action on given run attachment
         * Otherwise confirmation dialog is opened
         * @param {String} action action name
         * @param {Number} id attachment ID
         */
        $scope.performAttachment = function (action, id) {
            switch (action) {
                case "delete":
                    $("#deleteModalLabel").text("Delete attachment?");
                    $("#deleteModalBody").html("<p>Do you really want to delete attachment?</p>");
                    $("#deleteModal").modal("toggle");
                    currentAction = "deleteAttachment";
                    currentActionId = id;
                    break;
                default:
                    break;
            }
        };

        $scope.performAction = function () {
            switch (currentAction) {
                case "deleteAttachment":
                    $("#deleteModal").modal("toggle");
                    if (typeof $scope.SYNERGY.session.session_id === "undefined" || $scope.SYNERGY.session.session_id.length < 1) {
                        return;
                    }
                    attachmentHttp.removeRunAttachment($scope, currentActionId, function (data) {
                        $scope.SYNERGY.logger.log("Done", "Attachment deleted", "INFO", "alert-success");
                        $scope.fetch();
                    }, function (data) {
                        $scope.SYNERGY.logger.log("Action failed", "", "INFO", "alert-error");
                        $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
                        $scope.fetch();
                    });
                    break;
                default:
                    break;
            }
        };

        $scope.cancel = function () {
            window.history.back();
        };

        /**
         * Creates a new test run
         */
        $scope.create = function () {
            var start = $("#start").val();
            var stop = $("#end").val();
            if ($scope.myForm.$invalid || start.length < 1 || stop.length < 1) {
                $scope.SYNERGY.modal.update("Missing required fields", "");
                $scope.SYNERGY.modal.show();
                return;
            }
            var _run = new SynergyModels.TestRun($scope.testRun.title, $scope.testRun.desc, $scope.getUTCTime(start), $scope.getUTCTime(stop), $scope.id).setNotifications($scope.testRun.notifications);
            _run.projectId = $scope.testRun.projectId;
            runHttp.create($scope, _run, function (data) {
                $scope.SYNERGY.modal.update("Test run created", "");
                $scope.SYNERGY.modal.show();
                $location.path("administration/runs/page/1");
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Modifies test run
         */
        $scope.edit = function () {
            var start = $("#start").val();
            var stop = $("#end").val();
            if ($scope.myForm.$invalid || start.length < 1 || stop.length < 1) {
                $scope.SYNERGY.modal.update("Missing required fields", "");
                $scope.SYNERGY.modal.show();
                return;
            }
            var _run = new SynergyModels.TestRun($scope.testRun.title, $scope.testRun.desc, $scope.getUTCTime(start), $scope.getUTCTime(stop), $scope.id).setNotifications($scope.testRun.notifications);
            _run.projectId = $scope.testRun.projectId;
            runHttp.edit($scope, _run, function (data) {
                $scope.SYNERGY.modal.update("Test run updated", "");
                $scope.SYNERGY.modal.show();
                window.history.back();
            }, $scope.generalHttpFactoryError);
        };

        $scope.uploadFile = function () {
            new SynergyHandlers.FileUploader([], "dropbox", $scope.SYNERGY.uploadFileLimit, $scope.SYNERGY.server.buildURL("run_attachment", {"id": $scope.id}), function (title, msg, level, style, fileName) {
                $scope.SYNERGY.logger.log(title, msg, level, style);
                $scope.fileName = fileName;
                $scope.fetch();
            }, function (title, msg, level, style) {
                $scope.SYNERGY.logger.log(title, msg, level, style);
            }).uploadFileFromFileChooser("fileToUpload");
        };

        $scope.validDates = function () {
            $scope.testRun.start = $("#start").val();
            $scope.testRun.stop = $("#end").val();
            return ($scope.testRun.start.length < 1 || $scope.testRun.stop.length < 1) ? false : true;
        };

        $scope.fetch = function () {
            if (!$scope.SYNERGY.session.hasAdminRights()) {
                return;
            }
            projectsHttp.getAll($scope, function (data) {
                $scope.projects = data;
                if ($scope.testRun.projectName === null && $scope.projects.length > 0) {
                    $scope.testRun.projectId = $scope.projects[0].id;
                }
            }, $scope.generalHttpFactoryError);
            var action = window.location + "";
            action = action.substring(action.lastIndexOf("/") + 1);
            switch (action) {
                case "edit":
                    if ($scope.id < 0) {
                        return;
                    }
                    runHttp.getOverview($scope, false, $scope.id, function (data) {
                        $scope.testRun = data;
                        $scope.testRun.start = $scope.getLocalDateTime($scope.testRun.start);
                        $scope.testRun.end = $scope.getLocalDateTime($scope.testRun.end);
                        if ($scope.projects.length > 0 && data.projectName === null) {
                            $scope.testRun.projectId = $scope.projects[0].id;
                        }
                        $scope.refreshCodemirror = true;
                    }, $scope.generalHttpFactoryError);
                    break;
                default:
                    break;
            }
        };

        new SynergyHandlers.FileUploader([], "dropbox", $scope.SYNERGY.uploadFileLimit, $scope.SYNERGY.server.buildURL("run_attachment", {"id": $scope.id}), function (title, msg, level, style, fileName) {
            $scope.SYNERGY.logger.log(title, msg, level, style);
            $scope.fileName = fileName;
            $scope.fetch();
        }, function (title, msg, level, style) {
            $scope.SYNERGY.logger.log(title, msg, level, style);
        });

        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
    }
    /**
     * 
     * @param {type} $scope
     * @param {type} $routeParams
     * @param {AssignmentsFct} assignmentsHttp
     * @param {UsersFct} usersHttp
     * @param {LabelsFct} labelsHttp
     * @param {PlatformsFct} platformsHttp
     * @param {TribesFct} tribesHttp
     * @param {VersionsFct} versionsHttp
     * @returns {undefined}
     */
    function AdminMatrixAssignmentCtrl($scope, $routeParams, assignmentsHttp, usersHttp, labelsHttp, platformsHttp, tribesHttp, versionsHttp, SynergyUtils) {
        $scope.assignment = {platforms: [], users: [], tribes: [], runId: $routeParams.id};
        $scope.selectedPlatforms = [];
        $scope.platforms = [];
        $scope.platformId = -1;
        $scope.ready = 0; // if < 5, page shows "please wait" message 

        $scope.fetch = function () {
            // load all required data
            loadPlatforms();
            loadLabels();
            loadUsers();
            loadVersions();
            tribesHttp.get($scope, function (data) {
                $scope.tribes = data;
                $scope.ready++;
            }, $scope.generalHttpFactoryError);
        };

        function loadVersions() {
            versionsHttp.get($scope, false, function (data) {
                $scope.versions = data;
                $scope.ready++;
            }, $scope.generalHttpFactoryError);
        }

        function loadPlatforms() {
            platformsHttp.get($scope, function (data) {
                $scope.platforms = data;
                $scope.ready++;
            }, $scope.generalHttpFactoryError, false);
        }

        function loadUsers() {
            usersHttp.getAll($scope, function (data) {
                if (SynergyUtils.definedNotNull(data) && SynergyUtils.definedNotNull(data.users)) {
                    for (var i = 0, max = data.users.length; i < max; i++) {
                        data.users[i].displayName = data.users[i].firstName + " " + data.users[i].lastName + " (" + data.users[i].username + ")";
                    }
                }
                $scope.ready++;
                $scope.users = data.users;
            }, $scope.generalHttpFactoryError);
        }

        function loadLabels() {
            labelsHttp.getAll($scope, function (data) {
                $scope.ready++;
                data.push({"label": "None", "id": "-1"});
                $scope.labels = data;
            }, $scope.generalHttpFactoryError);
        }

        $scope.create = function () {
            if (($scope.assignment.users.length < 1 && $scope.assignment.tribes.length < 1) || $scope.platforms.length < 1) {
                $scope.SYNERGY.modal.update("Missing required fields", "");
                $scope.SYNERGY.modal.show();
                return;
            }
            $scope.showWaitDialog();
            assignmentsHttp.create($scope, $scope.assignment, function (data) {
                $scope.SYNERGY.modal.update("Assignment created", "");
                $scope.SYNERGY.modal.show();
                window.history.back();
            }, function (data, status) {
                $scope.SYNERGY.modal.show();
                $scope.SYNERGY.logger.log("Action failed for users ", data, "INFO", "alert-error");
                $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
            });
        };
        /**
         * Adds platform to selected
         */
        $scope.addSelectedPlatform = function () {
            if (!$scope.platformId || platformAlreadySelected(parseInt($scope.platformId, 10))) { // avoid on load displaying empty 
                return;
            }
            $scope.assignment.platforms.push({id: parseInt($scope.platformId, 10), name: getPlatformName(parseInt($scope.platformId, 10))});
            $scope.platformId = -1;
        };

        $scope.addSelectedTribe = function () {
            if (!$scope.tribeId || tribeAlreadySelected(parseInt($scope.tribeId, 10))) { // avoid on load displaying empty 
                return;
            }
            $scope.assignment.tribes.push({id: parseInt($scope.tribeId, 10), name: getTribeName(parseInt($scope.tribeId, 10))});
            $scope.tribeId = -1;
        };

        $scope.addSelectedUser = function () {
            if (!$scope.username || userAlreadySelected($scope.username)) { // avoid on load displaying empty 
                return;
            }
            $scope.assignment.users.push({username: $scope.username, displayName: getDisplayName($scope.username)});
            $scope.username = "";
        };

        function userAlreadySelected(username) {
            for (var i = 0, max = $scope.assignment.users.length; i < max; i++) {
                if (username === $scope.assignment.users[i].username) {
                    return true;
                }
            }
            return false;
        }

        function tribeAlreadySelected(tribeID) {
            for (var i = 0, max = $scope.assignment.tribes.length; i < max; i++) {
                if (tribeID === $scope.assignment.tribes[i].id) {
                    return true;
                }
            }
            return false;
        }

        function platformAlreadySelected(platformID) {
            for (var i = 0, max = $scope.assignment.platforms.length; i < max; i++) {
                if (platformID === $scope.assignment.platforms[i].id) {
                    return true;
                }
            }
            return false;
        }

        /**
         * removes platform from selected
         * @param {Number} platformID
         */
        $scope.removeSelectedPlatform = function (platformID) {
            for (var i = 0, max = $scope.assignment.platforms.length; i < max; i++) {
                if (platformID === $scope.assignment.platforms[i].id) {
                    $scope.assignment.platforms.splice(i, 1);
                    $scope.platformId = -1;
                    return;
                }
            }
        };

        $scope.removeSelectedTribe = function (tribeId) {
            for (var i = 0, max = $scope.assignment.tribes.length; i < max; i++) {
                if (tribeId === $scope.assignment.tribes[i].id) {
                    $scope.assignment.tribes.splice(i, 1);
                    $scope.tribesId = -1;
                    return;
                }
            }
        };

        $scope.removeSelectedUser = function (username) {
            for (var i = 0, max = $scope.assignment.users.length; i < max; i++) {
                if (username === $scope.assignment.users[i].username) {
                    $scope.assignment.users.splice(i, 1);
                    $scope.username = -1;
                    return;
                }
            }
        };
        function getDisplayName(username) {
            for (var i = 0, max = $scope.users.length; i < max; i++) {
                if ($scope.users[i].username === username) {
                    return $scope.users[i].displayName;
                }
            }
            return "oops";
        }

        function getTribeName(tribeID) {
            for (var i = 0, max = $scope.tribes.length; i < max; i++) {
                if ($scope.tribes[i].id === tribeID) {
                    return $scope.tribes[i].name;
                }
            }
            return "oops";
        }

        function getPlatformName(platformID) {
            for (var i = 0, max = $scope.platforms.length; i < max; i++) {
                if ($scope.platforms[i].id === platformID) {
                    return $scope.platforms[i].name;
                }
            }
            return "oops";
        }

        $scope.cancel = function () {
            window.history.back();
        };

        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
    }
    /**
     * 
     * @param {SpecificationsFct} specificationsHttp
     * @param {AssignmentFct} assignmentHttp
     * @param {UsersFct} usersHttp
     * @param {LabelsFct} labelsHttp
     * @param {PlatformsFct} platformsHttp description
     * @param {TribesFct} tribesHttp description
     * @returns {undefined} */
    function AdminAssignmentCtrl($scope, $timeout, $routeParams, specificationsHttp, assignmentsHttp, usersHttp, labelsHttp, platformsHttp, tribesHttp, versionsHttp, assignmentHttp, runHttp, SynergyUtils, SynergyModels) {
        $scope.testRunId = $routeParams.id;
        $scope.assignments = [];
        $scope.platforms = [];
        $scope.users = [];
        $scope.ready = 0; // if < 4, message is displayed
        $scope.value = {};

        $scope.fetch = function () {
            if (!$scope.SYNERGY.session.hasAdminRights()) {
                return;
            }
            loadPlatforms();
            loadUsers();
            loadLabels();
            loadSpecifications();
        };

        function loadPlatforms() {
            platformsHttp.get($scope, function (data) {
                $scope.platforms = data;
                $scope.ready++;
                if (SynergyUtils.definedNotNull(data) && data.length > 0) {
                    $scope.value.platform = data[0];
                }
            }, $scope.generalHttpFactoryError, false);
        }

        function loadLabels() {
            labelsHttp.getAll($scope, function (data) {
                $scope.ready++;
                data.push({"label": "None", "id": "-1"});
                $scope.labels = data;
                $scope.value.label = data[data.length - 1];
            }, $scope.generalHttpFactoryError);
        }

        function loadUsers() {
            usersHttp.getAll($scope, function (data) {
                if (SynergyUtils.definedNotNull(data) && data.users) {
                    for (var i = 0, max = data.users.length; i < max; i++) {
                        data.users[i].displayName = data.users[i].firstName + " " + data.users[i].lastName + " (" + data.users[i].username + ")";
                    }
                }
                $scope.ready++;
                $scope.users = data.users;
                if (SynergyUtils.definedNotNull(data) && data.users.length > 0) {
                    $scope.value.user = $scope.users[0];
                }
            }, $scope.generalHttpFactoryError);
        }

        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });

        /**
         * Adds assignment to the assignments array that will be sent to server
         */
        $scope.addAssignment = function () {
            if (!$scope.value.specification) {
                $scope.SYNERGY.logger.showMsg("Oops", "No specification selected (maybe tribe does not have any specification?)", "alert-error");
                return;
            }
            var assignment = new SynergyModels.TestAssignment(parseInt($scope.value.platform.id, 10), $scope.value.user.username, parseInt($scope.value.label.id, 10));
            assignment.specificationId = parseInt($scope.value.specification.id, 10);
            assignment.testRunId = parseInt($scope.testRunId, 10);
            assignment.display = {
                user: $scope.value.user.firstName + " " + $scope.value.user.lastName,
                label: $scope.value.label.label,
                platform: $scope.value.platform.name,
                duplicates: false,
                specification: $scope.value.specification.value
            };
            $scope.assignments.push(assignment);
            assignmentHttp.checkExists($scope, assignment).then(function (data) {
                assignment.display.duplicates = true;
            }, function (response) {
                switch (response.status) {
                    case 404:
                        assignment.display.duplicates = false;
                        break;
                    default:
                        $scope.SYNERGY.logger.log("Action failed", "Unable to check for duplicates", "INFO", "alert-error");
                        break;
                }
            });
        };

        $scope.removeAssignment = function (index) {
            $scope.assignments.splice(index, 1);
        };

        function loadSpecifications() {
            runHttp.getSpecifications($scope, $scope.testRunId, function (data) {
                $scope.specifications = [];
                for (var i = 0, max = data.specifications.length; i < max; i += 1) {
                    $scope.specifications[i] = {
                        value: data.specifications[i].title + " (" + ((data.projectName === null || typeof data.projectName === "undefined") ? $scope.SYNERGY.product : data.projectName) + " " + data.specifications[i].version + ")",
                        info: "<a href='#/specification/" + data.specifications[i].id + "'>view</a>",
                        id: data.specifications[i].id,
                        type: "specification",
                        version: data.specifications[i].version
                    };
                }
                $scope.value.specification = $scope.specifications[0];
                $scope.ready++;
            }, $scope.generalHttpFactoryError);
        }

        $scope.cancel = function () {
            window.history.back();
        };

        /**
         * Creates a new test assignment
         * @returns {unresolved}
         */
        $scope.create = function () {

            $scope.showWaitDialog();
            assignmentsHttp.createForUsers($scope, $scope.assignments, function (data) {
                $scope.SYNERGY.modal.update("Assignments created", "");
                $scope.SYNERGY.modal.show();
                window.history.back();
            }, function (data) {
                $scope.SYNERGY.modal.show();
                $scope.SYNERGY.logger.log("Action failed ", data, "INFO", "alert-error");
                $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
            });
        };
    }
    /**
     * @param {UserFct} userHttp
     * @param {UsersFct} usersHttp
     * @returns {undefined} 
     */
    function AdminUsersCtrl($scope, $location, $routeParams, userHttp, usersHttp) {
        $scope.users = [];
        $scope.page = $routeParams.page || 1;
        var currentUser = "";
        $scope.importUsersUrl = "http://" + window.location.host + "/dashboard/web/a_netcatusers.php";
        $scope.fetch = function () {
            if (!$scope.SYNERGY.session.hasAdminRights()) {
                return;
            }
            usersHttp.get($scope, $scope.page, function (data) {
                $scope.users = data.users;
                $scope.next = (data.nextUrl.length > 1) ? 1 : 0;
                $scope.prev = (data.prevUrl.length > 1) ? 1 : 0;
                $scope.nextPage = parseInt($scope.page, 10) + 1;
                $scope.prevPage = parseInt($scope.page, 10) - 1;
            }, $scope.generalHttpFactoryError);
        };

        $scope.importUsers = function () {
            usersHttp.importUsers($scope, $scope.importUsersUrl, function (data) {
                $scope.SYNERGY.logger.log("Done", data + " users imported", "INFO", "alert-success");
                $scope.fetch();
            }, $scope.generalHttpFactoryError);
        };

        $scope.retireUsers = function () {
            usersHttp.retireUsersWithRole($scope, "tester", function () {
                $scope.SYNERGY.logger.log("Done", "Removed users with role 'tester' retired", "INFO", "alert-success");
                $scope.fetch();
            }, $scope.generalHttpFactoryError);
        };

        $scope.perform = function (action, username) {
            if (action !== "delete") {
                $location.path("administration/user/" + username + "/" + action);
            } else {
                switch (action) {
                    case "delete":
                        $("#deleteModal").modal("toggle");
                        currentUser = username;
                        break;
                    default:
                        break;
                }
            }
        };

        $scope.newuser = function () {
            $location.path("administration/user/-1/create");
        };

        /**
         * Deletes user
         */
        $scope.deleteUser = function () {
            $("#deleteModal").modal("toggle");
            userHttp.remove($scope, currentUser, function (data) {
                $scope.SYNERGY.logger.log("Done", "User removed", "INFO", "alert-success");
                $scope.fetch();
            }, $scope.generalHttpFactoryError);
        };

        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
    }
    /**
     * 
     * @param {UserFct} userHttp
     * @returns {undefined} 
     **/
    function AdminUserCtrl($scope, $routeParams, userHttp, SynergyModels) {
        $scope.user = {
        };
        $scope.username = $routeParams.username || "";
        $scope.passwordChangeAllowed = !$scope.SYNERGY.useSSO;
        $scope.updatePassword = false;
        this.oldUsername = ""; // in case admin changes username, need to track of the old one
        var self = this;
        $scope.fetch = function () {
            if (!$scope.SYNERGY.session.hasAdminRights()) {
                return;
            }
            if ($scope.username === "-1") {
                // new user
                return;
            }
            userHttp.get($scope, $scope.username, function (data) {
                self.oldUsername = $scope.username;
                $scope.user = data;
            }, $scope.generalHttpFactoryError);
        };

        $scope.cancel = function () {
            window.history.back();
        };

        /**
         * Creates user
         */
        $scope.save = function () {
            var invalidPassword = typeof $scope.user.password === "undefined" || $scope.user.password === null || $scope.user.password.length < 1;
            if ($scope.myForm.$invalid || ($scope.updatePassword && invalidPassword)) {
                $scope.SYNERGY.modal.update("Missing required fields", "");
                $scope.SYNERGY.modal.show();
                return;
            }
            var u = new SynergyModels.User($scope.user.firstName, $scope.user.lastName, $scope.user.username, $scope.user.role, -1);
            u.email = $scope.user.email;
            if ($scope.updatePassword) {
                u.password = $scope.user.password;
            }
            u.emailNotifications = $scope.user.emailNotifications;
            userHttp.create($scope, u, function (data) {
                $scope.SYNERGY.modal.update("User created", "");
                $scope.SYNERGY.modal.show();
                window.history.back();
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Updates user
         * @returns {unresolved}
         */
        $scope.edit = function () {
            var invalidPassword = typeof $scope.user.password === "undefined" || $scope.user.password === null || $scope.user.password.length < 1;
            if ($scope.myForm.$invalid || ($scope.updatePassword && invalidPassword)) {
                $scope.SYNERGY.modal.update("Missing required fields", "");
                $scope.SYNERGY.modal.show();
                return;
            }
            var u = new SynergyModels.User($scope.user.firstName, $scope.user.lastName, $scope.user.username, $scope.user.role, -1, self.oldUsername);
            u.email = $scope.user.email;
            if ($scope.updatePassword) {
                u.password = $scope.user.password;
            }
            u.emailNotifications = $scope.user.emailNotifications;
            userHttp.edit($scope, u, function (data) {
                $scope.SYNERGY.modal.update("User updated", "");
                $scope.SYNERGY.modal.show();
                window.history.back();
            }, $scope.generalHttpFactoryError);
        };

        var self2 = $scope;
        $scope.init(function () {
            self2.fetch();
        });
    }
    /**
     * 
     * @param {SettingsFct} settingsHttp
     * @returns {undefined} 
     */
    function AdminSettingCtrl($scope, settingsHttp) {
        $scope.settings = [];
        $scope.newValue = "";
        $scope.newDesc = "";
        $scope.newKey = "";

        $scope.fetch = function () {
            if (!$scope.SYNERGY.session.hasAdminRights()) {
                return;
            }
            settingsHttp.get($scope, function (data) {
                $scope.settings = data;
            }, $scope.generalHttpFactoryError);
        };

        $scope.save = function () {
            for (var i = 0, limit = $scope.settings.length; i < limit; i += 1) {
                if ($scope.settings[i].value.length < 1) {
                    $scope.SYNERGY.logger.log("Missing value: ", $scope.settings[i].key, "INFO", "alert-error");
                    return;
                }
            }
            settingsHttp.edit($scope, $scope.settings, function (data) {
                $scope.SYNERGY.logger.log("Done", "Settings updated", "INFO", "alert-success");
                $scope.fetch();
            }, $scope.generalHttpFactoryError);
        };

        $scope.addSetting = function () {
            $scope.settings.push({"value": $scope.newValue, "label": $scope.newDesc, "key": $scope.newKey});
            $scope.newValue = $scope.newDesc = $scope.newKey = "";
        };

        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
    }
    /**
     * 
     * @param {AboutFct} aboutHttp
     * @returns {undefined} 
     */
    function AboutCtrl($scope, aboutHttp) {
        $scope.$emit("updateNavbar", {item: "nav_empty"});
        $scope.statistics = [];

        $scope.fetch = function () {
            aboutHttp.get($scope, function (data) {
                $scope.$emit("updateBreadcrumbs", {link: "about", title: "About"});
                $scope.statistics = data;
            }, $scope.generalHttpFactoryError);
        };
        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
    }
    function TribesCtrl($scope, $location, $timeout, tribesHttp) {
        $scope.$emit("updateNavbar", {item: "nav_empty"});
        $scope.tribes = [];

        $scope.fetch = function () {
            tribesHttp.get($scope, function (data) {
                $scope.tribes = data;
                $scope.$emit("updateBreadcrumbs", {link: "tribes", title: "Tribes"});
            }, $scope.generalHttpFactoryError, true);
        };
        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
    }
    function AdminLogCtrl($scope, logHttp, sessionRenewalHttp) {
        $scope.logContent = "";
        $scope.fetch = function () {
            logHttp.get($scope, function (data) {
                $scope.logContent = data;
            }, $scope.generalHttpFactoryError);
        };

        $scope.deleteLog = function () {
            logHttp.remove($scope, function () {
                $scope.logContent = "";
                $scope.SYNERGY.logger.log("Deleted", "", "INFO", "alert-info");
            }, $scope.generalHttpFactoryError);
        };

        $scope.sso = function () {
            sessionRenewalHttp.test($scope, function (d) {
                window.console.log("Done");
            }, function (d) {
                window.console.log("Opps, not done at all");
            });
        };

        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
    }
    function AdminDatabaseCtrl($scope, databaseHttp) {
        $scope.order = "ASC";
        $scope.tables = [];
        $scope.columns = [];
        $scope.orderBy = "";
        $scope.data = [];
        $scope.limit = 10;
        $scope.selectedTable = "";

        $scope.fetch = function () {
            databaseHttp.getTables($scope, function (data) {
                $scope.tables = data;
            }, $scope.generalHttpFactoryError);
        };

        $scope.loadTable = function () {
            databaseHttp.getColumns($scope, $scope.selectedTable, function (data) {
                $scope.columns = data;
                $scope.orderBy = data[0];
            }, $scope.generalHttpFactoryError);
        };

        $scope.show = function () {
            databaseHttp.listTable($scope, $scope.selectedTable, $scope.limit || 10, $scope.order, $scope.orderBy || "id", function (data) {
                $scope.data = data;
            }, $scope.generalHttpFactoryError);
        };

        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
    }
    function AssignmentVolunteerCtrl($scope, $routeParams, specificationsHttp, assignmentHttp, labelsHttp, platformsHttp, versionsHttp, reviewHttp, runHttp, SynergyUtils, SynergyModels) {
        $scope.suggestions = [];
        $scope.specifications = [];
        $scope.assignment = {
            testRun: $routeParams.id,
            username: "",
            tribe: -1,
            specification: "",
            label: "",
            platform: "",
            specificationId: "",
            labelId: "",
            platformId: ""
        };
        $scope.platforms = [];
        $scope.ready = 0;
        $scope.assignmentType = "test";
        $scope.reviewUrl;
        $scope.availablePages = [];
        $scope.reviewPage = null;
        $scope.showOnlyNotUsedPages = true;
        var deleteModalDisplayed = false;

        $scope.initData = function () {
            if ($scope.assignmentType === "review") {
                $scope.changeAvailablePages();
            }
        };

        $scope.fetch = function () {
            loadPlatforms();
            loadLabels();
            loadVersions();
            loadSpecifications();
        };

        $scope.changeAvailablePages = function () {
            if ($scope.showOnlyNotUsedPages) {
                reviewHttp.listNotStarted($scope, $scope.assignment.testRun, function (d) {
                    $scope.availablePages = d;
                }, $scope.generalHttpFactoryError);
            } else {
                reviewHttp.list($scope, function (d) {
                    $scope.availablePages = d;
                }, $scope.generalHttpFactoryError);
            }
        };

        function loadPlatforms() {
            platformsHttp.get($scope, function (data) {
                $scope.platforms = data;
                $scope.ready++;
                if (SynergyUtils.definedNotNull(data) && data.length > 0) {
                    $scope.assignment.platform = data[0].id;
                }
            }, $scope.generalHttpFactoryError, true);
        }

        function loadVersions() {
            versionsHttp.get($scope, true, function (data) {
                $scope.versions = data;
                $scope.ready++;
                if (SynergyUtils.definedNotNull(data) && data.length > 0) {
                    $scope.selectedVersion = data[0].name;
                    $scope.filter();
                }
            }, $scope.generalHttpFactoryError);
        }

        $scope.filter = function () {
            var matching = [];
            for (var i = 0, max = $scope.specifications.length; i < max; i++) {
                if ($scope.selectedVersion === $scope.specifications[i].version) {
                    matching.push($scope.specifications[i]);
                }
            }
            $scope.suggestions = matching;
        };


        function loadLabels() {
            labelsHttp.getAll($scope, function (data) {
                $scope.ready++;
                data.push({"label": "None", "id": "-1"});
                $scope.labels = data;
                $scope.assignment.label = data[data.length - 1].id;
            }, $scope.generalHttpFactoryError);
        }

        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });

        function loadSpecifications() {
            runHttp.getSpecifications($scope, $routeParams.id, function (data) {
                $scope.specifications = [];
                for (var i = 0, max = data.specifications.length; i < max; i += 1) {
                    $scope.specifications[i] = {
                        value: data.specifications[i].title + " (" + ((data.projectName === null || typeof data.projectName === "undefined") ? $scope.SYNERGY.product : data.projectName) + " " + data.specifications[i].version + ")",
                        info: "<a href='#/specification/" + data.specifications[i].id + "'>view</a>",
                        id: data.specifications[i].id,
                        type: "specification",
                        version: data.specifications[i].version
                    };
                }
                $scope.filter();
                $scope.ready++;
            }, $scope.generalHttpFactoryError);
        }


        $scope.cancel = function () {
            window.history.back();
        };


        function checkAssignmentExists(assignment) {
            assignmentHttp.checkExists($scope, assignment).then(function (data) {
                $("#deleteModal").modal("toggle");
                deleteModalDisplayed = true;
            }, function (response) {
                switch (response.status) {
                    case 404:
                        submitAssignmentData(assignment);
                        break;
                    default:
                        $scope.SYNERGY.logger.log("Action failed", "Unable to check for duplicates", "INFO", "alert-error");
                        break;
                }
            });
        }

        function submitAssignmentData(assignment) {
            $scope.showWaitDialog();
            assignmentHttp.createVolunteer($scope, assignment, function (data) {
                $scope.SYNERGY.modal.update("Assignment created", "");
                $scope.SYNERGY.modal.show();
                window.history.back();
            }, function (data) {
                $scope.SYNERGY.modal.show();
                $scope.SYNERGY.logger.log("Action failed", data, "INFO", "alert-error");
                $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
            });
        }


        function submitReviewAssignment() {
            if ($scope.reviewPage === null) {
                $scope.SYNERGY.modal.update("Missing required fields", "");
                $scope.SYNERGY.modal.show();
                return;
            }
            var reviewAssignment = new SynergyModels.ReviewAssignment($scope.SYNERGY.session.username, $scope.reviewPage.url);
            reviewAssignment.owner = $scope.reviewPage.owner;
            reviewAssignment.title = $scope.reviewPage.title;
            reviewAssignment.testRunId = parseInt($scope.assignment.testRun, 10);

            reviewHttp.createVolunteer($scope, reviewAssignment, function (data) {
                $scope.SYNERGY.modal.update("Assignment created", "");
                $scope.SYNERGY.modal.show();
                window.history.back();
            }, function (data) {
                $scope.SYNERGY.modal.show();
                $scope.SYNERGY.logger.log("Action failed", data, "INFO", "alert-error");
                $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
            });

        }

        /**
         * Creates a new test assignment
         * @returns {unresolved}
         */
        $scope.create = function (skipDuplicateCheck) {

            if ($scope.assignmentType === "review") {
                submitReviewAssignment();
                return;
            }

            var assignment = new SynergyModels.TestAssignment(parseInt($scope.assignment.platform, 10), $scope.SYNERGY.session.username, parseInt($scope.assignment.label, 10));
            assignment.specificationId = parseInt($scope.assignment.specificationId, 10);
            assignment.testRunId = parseInt($scope.assignment.testRun, 10);

            if (!$scope.assignment.specificationId || assignment.platformId < 0) {
                $scope.SYNERGY.modal.update("Missing required fields", "");
                $scope.SYNERGY.modal.show();
                return;
            }

            if (($scope.assignedToUser && assignment.username.length < 1) || ($scope.assignedToTribe && $scope.assignment.tribe.id < 1)) {
                $scope.SYNERGY.modal.update("Missing assignee", "");
                $scope.SYNERGY.modal.show();
                return;
            }

            if (assignment.labelId < 0 && parseInt($scope.assignment.label, 10) > 0) {
                $scope.SYNERGY.modal.update("Label does not exist", "");
                $scope.SYNERGY.modal.show();
                return;
            }

            if (!skipDuplicateCheck) {
                checkAssignmentExists(assignment);
            } else {
                if (deleteModalDisplayed) {
                    $("#deleteModal").modal("toggle");
                }
                submitAssignmentData(assignment);
            }
        };
    }
    /**
     * 
     * @param {type} $scope
     * @param {type} $routeParams
     * @param {RevisionsFct} revisionsHttp
     * @returns {undefined}
     */
    function RevisionCtrl($scope, $routeParams, revisionsHttp) {

        $scope.revisions = [];
        $scope.specificationId = $routeParams.id;

        $scope.fetch = function () {
            revisionsHttp.listRevisions($scope, $scope.specificationId, function (data) {
                $scope.revisions = data;
            }, $scope.generalHttpFactoryError);
        };

        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
        /**
         * Loads 2 revisions from server and calls diffRevisions() to print them
         */
        $scope.compareRevisions = function () {
            revisionsHttp.getRevisions($scope, $scope.choiceA, $scope.choiceB, $scope.specificationId, function (data) {
                diffRevisions(data[0], data[1]);
            }, $scope.generalHttpFactoryError);
        };

        function diffRevisions(rev1, rev2) {
            var base = difflib.stringAsLines(rev1.content);
            var newtxt = difflib.stringAsLines(rev2.content);
            var sm = new difflib.SequenceMatcher(base, newtxt);
            var opcodes = sm.get_opcodes();
            var diffoutputdiv = $("#diffoutput");
            diffoutputdiv.html("");

            diffoutputdiv.append(diffview.buildView({baseTextLines: base,
                newTextLines: newtxt,
                opcodes: opcodes,
                baseTextName: $scope.getLocalTime(rev1.date, true) + "(" + rev1.author + ")",
                newTextName: $scope.getLocalTime(rev2.date, true) + "(" + rev2.author + ")",
                contextSize: null,
                viewType: 1}));
        }
    }
    function CalendarCtrl($scope, calendarHttp) {
        $scope.$emit("updateBreadcrumbs", {link: "calendar", title: "Calendar"});

        function loadCalendar() {
            calendarHttp.getEvents($scope, function (data) {
                var items = [];

                for (var i = 0, max = data.length; i < max; i += 1) {
                    items.push({url: "#/run/" + data[i].id, title: data[i].title, start: new Date(data[i].start.substr(0, 4), parseInt(data[i].start.substr(4, 2), 10) - 1, data[i].start.substr(6, 2)), end: new Date(data[i].end.substr(0, 4), parseInt(data[i].end.substr(4, 2), 10) - 1, data[i].end.substr(6, 2))});
                }

                $("#calendar").fullCalendar({
                    header: {
                        left: "prev,next today",
                        center: "title",
                        right: "month,agendaWeek,agendaDay"
                    },
                    editable: true,
                    events: items,
                    eventMouseover: function (event, jsEvent, view) {
                        if (view.name !== "agendaDay") {
                            $(jsEvent.target).attr("title", event.title);
                        }
                    }
                });
            }, function () {
                window.console.log("Failed to load calendar events");
            });
        }
        loadCalendar();

    }
    function AssignmentTribeCtrl($scope, $routeParams, tribesHttp, labelsHttp, platformsHttp, assignmentsHttp, assignmentHttp, SynergyModels) {

        $scope.tribes = [];
        $scope.platforms = [];
        $scope.labels = [];
        $scope.ready = 0;
        $scope.value = {};
        $scope.assignments = [];
        $scope.runId = $routeParams.id;

        $scope.fetch = function () {
            loadTribes();
            loadLabels();
            loadPlatforms();
        };

        function loadPlatforms() {
            platformsHttp.get($scope, function (data) {
                $scope.platforms = data;
                $scope.value.platform = data[0];
                $scope.ready++;
            }, $scope.generalHttpFactoryError, true);
        }

        function loadLabels() {
            labelsHttp.getAll($scope, function (data) {
                $scope.ready++;
                data.push({"label": "None", "id": "-1"});
                $scope.value.label = data[data.length - 1];
                $scope.labels = data;
            }, $scope.generalHttpFactoryError);
        }
        /**
         * Adds assignment to the array of assignments to be sent to server
         */
        $scope.addAssignment = function () {
            if (!$scope.value.specification) {
                $scope.SYNERGY.logger.showMsg("Oops", "No specification selected (maybe tribe does not have any specification?)", "alert-error");
                return;
            }
            var assignment = new SynergyModels.TestAssignment(parseInt($scope.value.platform.id, 10), $scope.value.user.username, parseInt($scope.value.label.id, 10));
            assignment.specificationId = parseInt($scope.value.specification.id, 10);
            assignment.testRunId = parseInt($scope.runId, 10);
            assignment.tribeId = parseInt($scope.selectedTribe.id, 10);
            assignment.display = {
                tribe: $scope.selectedTribe.name,
                user: $scope.value.user.firstName + " " + $scope.value.user.lastName,
                label: $scope.value.label.label,
                platform: $scope.value.platform.name,
                specification: $scope.value.specification.title + " (" + $scope.SYNERGY.product + " " + $scope.value.specification.version + ")",
                duplicates: false
            };
            $scope.assignments.push(assignment);
            assignmentHttp.checkExists($scope, assignment).then(function (data) {
                assignment.display.duplicates = true;
            }, function (response) {
                switch (response.status) {
                    case 404:
                        assignment.display.duplicates = false;
                        break;
                    default:
                        $scope.SYNERGY.logger.log("Action failed", "Unable to check for duplicates", "INFO", "alert-error");
                        break;
                }
            });
        };
        /**
         * Respons to change of tribe, sets preselected specification and user
         */
        $scope.filterTribe = function () {
            $scope.value.user = $scope.selectedTribe.members[0];
            if ($scope.selectedTribe.ext.specifications) {
                $scope.value.specification = $scope.selectedTribe.ext.specifications[0];
            }
        };

        $scope.removeAssignment = function (index) {
            $scope.assignments.splice(index, 1);
        };

        function setProject(data) {
            for (var t = 0, max = data.length; t < max; t++) {
                if (data[t].hasOwnProperty("ext") && data[t].ext.hasOwnProperty("specifications")) {
                    for (var s = 0, maxs = data[t].ext.specifications.length; s < maxs; s++) {
                        if (data[t].ext.specifications[s].hasOwnProperty("projects") && data[t].ext.specifications[s].projects.length > 0) {
                            data[t].ext.specifications[s]._project = data[t].ext.specifications[s].projects[0].name;
                        } else {
                            data[t].ext.specifications[s]._project = $scope.SYNERGY.product;
                        }
                    }
                }
            }
        }

        function loadTribes() {
            tribesHttp.getTribesForRun($scope, $scope.SYNERGY.session.username, $scope.runId, function (data) {
                setProject(data);
                $scope.tribes = data;
                $scope.selectedTribe = data[0];
                $scope.value.user = data[0].members[0];
                if (data[0].ext.specifications) {
                    $scope.value.specification = data[0].ext.specifications[0];
                }
                $scope.ready++;
            }, $scope.generalHttpFactoryError);
        }

        $scope.cancel = function () {
            window.history.back();
        };
        $scope.create = function () {
            assignmentsHttp.createForTribes($scope, $scope.assignments, function (data) {
                $scope.SYNERGY.modal.update("Assignments created", "");
                $scope.SYNERGY.modal.show();
                window.history.back();
            }, $scope.generalHttpFactoryError);
        };

        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });

    }
    function StatisticsCtrl($scope, statisticsHttp, $routeParams, SynergyUtils, SynergyModels, SynergyHandlers, SynergyIssue) {
        $scope.id = $routeParams.id;
        $scope.timeView = "all";
        $scope.inArchive = window.location.href.indexOf("archive") > 0 ? true : false;
        var archivedData = {};
        /**
         * Order object for Tribes table
         */
        $scope.orderProp = {
            prop: "time",
            descending: true
        };

        try {
            $("#start").datetimepicker({dateFormat: "yy-mm-dd", timeFormat: "HH:mm:ss"});
            $("#end").datetimepicker({dateFormat: "yy-mm-dd", timeFormat: "HH:mm:ss"});
        } catch (e) {

        }
        $scope.P1Issues = [];
        $scope.P2Issues = [];
        $scope.P3Issues = [];
        $scope.unresolvedIssues = [];
        $scope.unknownIssues = [];
        $scope.allIssues = [];
        $scope.reviewTotal = {};

        /**
         * Order object for testers table
         */
        $scope.orderPropU = {
            prop: "time",
            descending: true
        };
        $scope.orderPropR = {
            prop: "time",
            descending: true
        };

        var cache = {};
        $scope.tribes = [];
        var tribesSpecs = [];
        $scope.data = {};
        $scope.computed = {};
        var totalCounts = {};
        var fallback = false;

        $scope.fetch = function () {
            if ($scope.inArchive) {
                statisticsHttp.getArchive($scope, $scope.id, fallback, function (data) {
                    $scope.data = data;
                    cache = data;
                    tribesSpecs = data.tribes;
                    evaluateComputedData(data);
                    if (data.reviews) {
                        evaluateReviews(data.reviews);
                    }
                }, function (data, status) {
                    if (status === 404 && !fallback) {
                        $scope.SYNERGY.logger.log("Opps", "Statistics not found, trying alternative way", "INFO", "alert-warning");
                        fallback = true;
                        self.fetch();
                        return;
                    }
                    $scope.SYNERGY.logger.log("Action failed", data, "INFO", "alert-error");
                    $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
                });
            } else {
                statisticsHttp.get($scope, $scope.id, function (data) {
                    cache = data;
                    $scope.data = data;
                    tribesSpecs = data.tribes;
                    evaluateComputedData(data);
                    if (data.reviews) {
                        evaluateReviews(data.reviews);
                    }
                }, $scope.generalHttpFactoryError);
            }
        };

        $scope.getAllTimeData = function () {
            if ($scope.timeView === "all") {
                $scope.data = cache;
                tribesSpecs = cache.tribes;
                evaluateComputedData(cache);
                if ($scope.data.reviews) {
                    evaluateReviews($scope.data.reviews);
                }
            } else {
                $("#start").val("");
                $("#end").val("");
            }
        };

        function evaluateReviews(reviews) {
            var total = {
                total: reviews.length,
                completed: 0,
                time: 0,
                comments: 0,
                reviewers: 0
            };
            var collector = new SynergyModels.UserReviewStats();
            for (var i = 0, max = reviews.length; i < max; i++) {
                if (reviews[i].isFinished) {
                    total.completed++;
                }
                total.time += reviews[i].timeTaken;
                total.comments += reviews[i].numberOfComments;
                collector.addReview(reviews[i]);
            }
            total.time = Math.floor(total.time / 60) + " hours and " + (total.time % 60) + " minutes";
            $scope.reviews = collector.finish();
            total.reviewers = $scope.reviews.length;
            total.finishRate = (Math.round(1000 * total.completed / total.total) / 10) || 0;
            $scope.reviewTotal = total;
        }

        /**
         * Loads statistics for time period
         */
        $scope.filterStatistics = function () {
            var start = $("#start").val();
            var stop = $("#end").val();

            if (start.length < 1 || stop.length < 1) {
                $scope.SYNERGY.logger.log("Missing data", "Please specify both 'from' and 'to' time periods", "INFO", "alert-error");
                return;
            }

            if ($scope.inArchive) {
                filterArchivedData(start, stop);
            } else {
                statisticsHttp.getPeriod($scope, $scope.id, {"from": $scope.getUTCTime(start), "to": $scope.getUTCTime(stop)}, function (data) {
                    $scope.data = data;
                    tribesSpecs = data.tribes;
                    evaluateComputedData(data);
                    if ($scope.data.reviews) {
                        evaluateReviews($scope.data.reviews);
                    }
                }, $scope.generalHttpFactoryError);
            }
        };


        function filterArchivedData(start, stop) {
            start = SynergyUtils.localToUTCTimestamp(start);
            stop = SynergyUtils.localToUTCTimestamp(stop);
            $scope.data = new SynergyHandlers.ArchiveDataFilter(SynergyUtils.shallowClone(cache)).getData(start, stop);
            tribesSpecs = $scope.data.tribes;
            evaluateComputedData($scope.data);
            if ($scope.data.reviews) {
                evaluateReviews($scope.data.reviews);
            }
        }

        $scope.sortTribes = function (prop) {
            if ($scope.orderProp.prop === prop) {
                $scope.orderProp.descending = !$scope.orderProp.descending;
            } else {
                $scope.orderProp = {
                    prop: prop,
                    descending: true
                };
            }
        };

        $scope.sortUsers = function (prop) {
            if ($scope.orderPropU.prop === prop) {
                $scope.orderPropU.descending = !$scope.orderPropU.descending;
            } else {
                $scope.orderPropU = {
                    prop: prop,
                    descending: true
                };
            }
        };
        $scope.sortReviewers = function (prop) {
            if ($scope.orderPropR.prop === prop) {
                $scope.orderPropR.descending = !$scope.orderPropR.descending;
            } else {
                $scope.orderPropR = {
                    prop: prop,
                    descending: true
                };
            }
        };

        /**
         * Goes through loaded data and count overall statistics, draw charts etc.
         */
        function evaluateComputedData(data) {
            var computed = {};
            computed.testers = getTesters(data);
            computed.testers = computed.testers.filter(function (t) {
                return t.completedCases > 0;
            });
            computed.testersCount = computed.testers.length;
            computed.time = Math.floor(totalCounts.timeToComplete / 60) + " hours and " + (totalCounts.timeToComplete % 60) + " minutes";
            computed.completedRelative = (Math.round(1000 * data.testRun.completed / data.testRun.total) / 10) || 0;
            computed.passRate = (Math.round(1000 * totalCounts.passed / data.testRun.completed) / 10) || 0;
            $scope.tribes = getTribesData(data);
            $scope.issuesTotal = data.issues.length;
            $scope.issuesUrl = $scope.SYNERGY.issues.viewLink(data.testRun.projectName, data.issues);
            $scope.issueColor = getIssueColor(data.issues);
            var issueCollector = new SynergyIssue.RunIssuesCollector();
            issueCollector.addIssues(data.issues);
            $scope.unresolvedIssues = issueCollector.issuesStats.unresolvedIssues;
            $scope.P1Issues = issueCollector.issuesStats.P1Issues;
            $scope.unknownIssues = issueCollector.issuesStats.unknownIssues;
            $scope.P2Issues = issueCollector.issuesStats.P2Issues;
            $scope.P3Issues = issueCollector.issuesStats.P3Issues;
            $scope.computed = computed;
            SynergyUtils.ProgressChart([issueCollector.issuesStats.opened / (issueCollector.issuesStats.total / 100), 100 - (issueCollector.issuesStats.opened / (issueCollector.issuesStats.total / 100))], ["#ccc", "#62c462"], ["Unresolved", "Resolved"], "issuesResolution");
            SynergyUtils.ProgressChart([issueCollector.issuesStats.P1 / (issueCollector.issuesStats.total / 100), issueCollector.issuesStats.P2 / (issueCollector.issuesStats.total / 100), issueCollector.issuesStats.P3 / (issueCollector.issuesStats.total / 100), issueCollector.issuesStats.P4 / (issueCollector.issuesStats.total / 100)], ["#ee5f5b", "#f89406", "#fbeed5", "#ccc"], ["P1 (" + issueCollector.issuesStats.P1 + ")", "P2 (" + issueCollector.issuesStats.P2 + ")", "P3 (" + issueCollector.issuesStats.P3 + ")", "P4 (" + issueCollector.issuesStats.P4 + ")"], "issuesPriority");
            printCharts();
        }

        function printCharts() {
            SynergyUtils.ProgressChart([$scope.computed.completedRelative, 100 - $scope.computed.completedRelative], ["#08c", "#ccc"], ["Executed cases", "Pending cases"], "canvas1");
            SynergyUtils.ProgressChart([$scope.computed.passRate, 100 - $scope.computed.passRate], ["#62c462", "#ccc"], ["Passed cases", "Other cases"], "canvas2");
        }

        /**
         * Based on number and priority of bugs, colors the Issues div
         */
        function getIssueColor(issues) {
            var sums = {"P1": 0, "P2": 0, "P3": 0, "P4": 0};
            for (var i = 0, max = issues.length; i < max; i++) {
                sums[issues[i].priority]++;
            }
            if (sums.P1 > 0) {
                return "bugs-error";
            }

            if (sums.P2 === 1) {
                return "bugs-warning";
            }

            if (sums.P2 > 1) {
                return "bugs-orange";
            }

            return "bugs-success";
        }

        function getTesters(data) {
            totalCounts.passed = 0;
            totalCounts.timeToComplete = 0;
            if (!data.assigneesOverview) {
                return [];
            }
            var testers = [];
            var _hours = 0;
            for (var i in data.assigneesOverview) {
                if (data.assigneesOverview.hasOwnProperty(i)) {
                    _hours = 0;
                    var t = {
                        "completedCases": 0,
                        "username": i,
                        "name": data.assigneesOverview[i].name,
                        "time": 0,
                        "prettyTime": ""
                    };
                    for (var k = 0, max3 = data.assigneesOverview[i].assignments.length; k < max3; k++) {
                        totalCounts.timeToComplete += data.assigneesOverview[i].assignments[k].totalTime;
                        t.time += data.assigneesOverview[i].assignments[k].totalTime;
                        totalCounts.passed += parseInt(data.assigneesOverview[i].assignments[k].passedCases, 10);
                        t.completedCases += parseInt(data.assigneesOverview[i].assignments[k].completedCases, 10);
                    }
                    _hours = Math.floor(t.time / 60);
                    t.prettyTime = (_hours > 0 ? (_hours + " hours and ") : "") + (t.time % 60) + " minutes";
                    testers.push(t);
                }
            }
            return testers;
        }

        /**
         * Iterates over all assignments to create statistics for tribes based on assignee and specification
         */
        function getTribesData(data) {
            if (!data.assigneesOverview) {
                return;
            }
            var assignments = data.assigneesOverview;
            var tribes = {};
            var _t;
            for (var i in assignments) {
                if (assignments.hasOwnProperty(i)) {
                    for (var j = 0, max2 = assignments[i].tribes.length; j < max2; j++) { // for each user's tribe
                        _t = assignments[i].tribes[j];
                        if (!tribes[_t.name]) {
                            tribes[_t.name] = new SynergyModels.TribeRunStats(_t.name, _t.id, tribesSpecs);
                        }
                        tribes[_t.name].addAssignment(assignments[i].assignments); // all assignments for given user
                    }
                }
            }
            return $scope.SYNERGY.util.toIndexedArray(tribes);
        }

        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
    }
    function AssignmentCommentsCtrl($scope, $routeParams, assignmentHttp, SynergyUtils) {
        $scope.id = $routeParams.id || -1;
        $scope.filters = {};
        $scope.isLoading = false;
        $scope.pageSize = $scope.SYNERGY.commentsPage;
        $scope.filter = {
            "case": "All",
            "specification": "All",
            "reporter": "All",
            "resolver": "All",
            "comment": "All",
            "status": "All"
        };
        $scope.duplicates = [];
        var duplicatesFull = [];
        var commentToBeResolved = {};
        $scope.fetch = function () {
            if ($scope.id < 0) {
                return;
            }
            assignmentHttp.getComments($scope, $scope.id, function (data) {
                $scope.data = data;
                collectFilters(data);
            }, $scope.generalHttpFactoryError);
        };

        $scope.nextPage = function () {
            $scope.isLoading = true;
            $scope.pageSize += $scope.SYNERGY.commentsPage;
            $scope.isLoading = false;
        };

        /**
         * Collects all distinct cases, specifications, reporters etc. to populate folter combo boxes
         */
        function collectFilters(data) {

            var cases = [];
            var specifications = [];
            var reporters = [];
            var comments = [];
            var status = [];
            var resolvers = [];

            for (var i = 0, max = data.comments.length; i < max; i++) {
                if (cases.indexOf(data.comments[i].caseTitle) < 0) {
                    cases.push(data.comments[i].caseTitle);
                }
                if (specifications.indexOf(data.comments[i].specificationTitle) < 0) {
                    specifications.push(data.comments[i].specificationTitle);
                }
                if (reporters.indexOf(data.comments[i].authorDisplayName) < 0) {
                    reporters.push(data.comments[i].authorDisplayName);
                }
                if (status.indexOf(data.comments[i].resolution) < 0) {
                    status.push(data.comments[i].resolution);
                }
                if (SynergyUtils.definedNotNull(data.comments[i].resolverDisplayName) && resolvers.indexOf(data.comments[i].resolverDisplayName) < 0) {
                    resolvers.push(data.comments[i].resolverDisplayName);
                }
                if (comments.indexOf(data.comments[i].commentText) < 0) {
                    comments.push(data.comments[i].commentText);
                }
            }

            var sortFnc = function (a, b) {
                a = a.toLowerCase();
                b = b.toLowerCase();
                if (a === b) {
                    return 0;
                }
                return a > b ? 1 : -1;
            };

            cases.sort(sortFnc);
            cases.push("All");
            specifications.sort(sortFnc);
            specifications.push("All");
            reporters.sort(sortFnc);
            reporters.push("All");
            comments.sort(sortFnc);
            comments.push("All");
            status.sort(sortFnc);
            status.push("All");
            resolvers.sort(sortFnc);
            resolvers.push("All");
            $scope.filters = {
                "cases": cases,
                "specifications": specifications,
                "reporters": reporters,
                "comments": comments,
                "status": status,
                "resolvers": resolvers
            };
        }

        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });

        $scope.commentFilter = function (comment) {
            if ($scope.filter["case"] && $scope.filter["case"] !== "All" && comment.caseTitle !== $scope.filter["case"]) {
                return false;
            }

            if ($scope.filter.specification && $scope.filter.specification !== "All" && comment.specificationTitle !== $scope.filter.specification) {
                return false;
            }
            if ($scope.filter.reporter && $scope.filter.reporter !== "All" && comment.authorDisplayName !== $scope.filter.reporter) {
                return false;
            }
            if ($scope.filter.comment && $scope.filter.comment !== "All" && comment.commentText !== $scope.filter.comment) {
                return false;
            }
            if ($scope.filter.status && $scope.filter.status !== "All" && comment.resolution !== $scope.filter.status) {
                return false;
            }
            if ($scope.filter.resolver && $scope.filter.resolver !== "All" && comment.resolverDisplayName !== $scope.filter.resolver) {
                return false;
            }
            return true;
        };

        /**
         * Resolves particular comment, if there are duplicate comments (same case id, suite id and comment type), offers to resolve them as well
         */
        $scope.resolveComment = function (comment) {
            commentToBeResolved = comment;
            $scope.duplicates = findSimilarComments(comment);
            duplicatesFull.push(comment);
            if ($scope.duplicates.length === 0) {
                $scope.resolveContinue(false);
            } else {
                $("#resolveModal").modal("toggle");
            }
        };

        /**
         * Returns similar comments for given comment (same case id, suite id and comment type)
         */
        function findSimilarComments(comment) {
            var similars = [];
            duplicatesFull = [];
            for (var i = 0, max = $scope.data.comments.length; i < max; i++) { // compare case_id, suite_id and comment type
                if ($scope.data.comments[i].caseId === comment.caseId &&
                        $scope.data.comments[i].id !== comment.id &&
                        $scope.data.comments[i].suiteId === comment.suiteId &&
                        $scope.data.comments[i].commentText === comment.commentText &&
                        $scope.data.comments[i].resolution === "new") {
                    duplicatesFull.push($scope.data.comments[i]);
                    similars.push({"id": $scope.data.comments[i].id});
                }
            }
            return similars;
        }

        $scope.resolveContinue = function (resolveDuplicates) {
            if (resolveDuplicates) {
                $scope.duplicates.push({"id": commentToBeResolved.id});
            }
            $scope.showWaitDialog();
            assignmentHttp.resolveComments($scope, (resolveDuplicates ? $scope.duplicates : [{"id": commentToBeResolved.id}]), function (data) {
                $scope.showWaitDialog();
                if (resolveDuplicates) {
                    for (var i = 0, max = duplicatesFull.length; i < max; i++) {
                        duplicatesFull[i].resolution = "resolved";
                        duplicatesFull[i].resolverUsername = $scope.SYNERGY.session.username;
                        duplicatesFull[i].resolverDisplayName = $scope.SYNERGY.session.firstName + " " + $scope.SYNERGY.session.lastName;
                    }
                    $("#resolveModal").modal("toggle");
                } else {
                    duplicatesFull[duplicatesFull.length - 1].resolution = "resolved";
                    duplicatesFull[duplicatesFull.length - 1].resolverUsername = $scope.SYNERGY.session.username;
                    duplicatesFull[duplicatesFull.length - 1].resolverDisplayName = $scope.SYNERGY.session.firstName + " " + $scope.SYNERGY.session.lastName;
                }

                $scope.SYNERGY.logger.log("Done", "Comments resolved", "INFO", "alert-success");
            }, function (data) {
                $scope.SYNERGY.modal.show();
                $scope.SYNERGY.logger.log("Action failed", data, "INFO", "alert-error");
                $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
            });

        };
    }
    function ReviewCtrl($scope, $routeParams, reviewHttp, SynergyUtils, SynergyModels) {
        function addListener() {
            if (window.addEventListener) {
                window.addEventListener("message", handleIframeSelection);
            } else {
                window.attachEvent("onmessage", handleIframeSelection);
            }
        }

        var iframe = window.document.getElementById("tutorialFrame");
        var action = $routeParams.action || "view";
        $scope.id = parseInt($routeParams.id, 10) || -1;
        $scope.review = {};
        $scope.currentComment = new SynergyModels.ReviewComment($scope.tutorialUrl);
        addListener();
        $scope.frameSrc = "";
        $scope.isEditing = false;
        $scope.commentAction = "Add";
        var oldCopy = null;
        var editingIndex = -1;
        var cachedData = null;
        var started = 0;
        var sentAs = -1;
        var pageLength = -1;

        function fetch() {
            if (cachedDataExists()) {
                $scope.sendCached(true);
            } else {
                reviewHttp.get($scope, $scope.id, action, function (data) {
                    $scope.review = data;
                    loadPage();
                }, $scope.generalHttpFactoryError);
            }
        }


        function removeListener() {
            if (window.addEventListener) {
                window.removeEventListener("message", handleIframeSelection);
            } else {
                window.detachEvent("onmessage", handleIframeSelection);
            }
        }

        function handleIframeSelection(msg) {
            var _data = msg.data;
            if (typeof _data === "string") {
                _data = JSON.parse(msg.data);
            }

            switch (_data.action) {
                case "added":
                    $scope.currentComment.addElement(_data.xpath, _data.element);
                    break;
                case "removed":
                    $scope.currentComment.removeElement(_data.xpath);
                    break;
                default: // wordCount
                    pageLength = parseInt(_data.words, 10);
                    break;
            }
            try {
                if (!$scope.$$phase) {
                    $scope.$apply();
                }
            } catch (e) {
            }
        }

        function loadPage() {
            $scope.frameSrc = "../../server/api/review.php?url=" + encodeURIComponent($scope.review.reviewUrl);
            window.document.getElementById("frameContainer").innerHTML = "<iframe data-ng-hide=\"frameSrc.length < 1\" id=\"tutorialFrame\" style=\"width: 100%; height: 1000px\" src=\"" + $scope.frameSrc + "\"></iframe>";
            started = new Date().getTime();
            iframe = window.document.getElementById("tutorialFrame");
        }

        function cachedDataExists() {
            cachedData = $scope.SYNERGY.cache.get("review_progress_" + $scope.id);
            return cachedData && cachedData.date && cachedData.progress ? true : false;
        }

        $scope.cancelEditing = function () {
            $scope.currentComment = new SynergyModels.ReviewComment($scope.review.reviewUrl);
            $scope.review.comments[editingIndex] = oldCopy;
            iframe.contentWindow.postMessage("{\"action\" : \"clear\"}", "*");
            $scope.isEditing = false;
            $scope.commentAction = "Add";
        };

        $scope.sendCached = function (send) {
            if (send) {
                $("#attemptModal").modal("toggle");
                $scope.review.comments = cachedData.progress;
                $scope.review.timeTaken = cachedData.timeTaken;
                $scope.review.weight = cachedData.weight;
                $scope.submit(sentAs);
            } else {
                $scope.SYNERGY.cache.clear("review_progress_" + $scope.id);
                fetch();
            }
        };

        /**
         * @param {Number} mode 1 means finished, 0 means paused
         */
        $scope.submit = function (mode) {
            if (sentAs === -1) { // assign only for the first time
                sentAs = mode;
            }
            mode = sentAs; // fallback to the first value prior any failures
            var assignment = new SynergyModels.ReviewAssignment($scope.SYNERGY.session.username, $scope.review.reviewUrl);
            assignment.comments = $scope.review.comments;
            assignment.id = $scope.id;
            assignment.testRunId = $scope.review.testRunId;
            assignment.isFinished = parseInt(mode, 10) === 1 ? true : false;
            assignment.timeTaken = new Date().getTime() - started;
            assignment.weight = pageLength;
            $scope.showWaitDialog();
            $scope.SYNERGY.cache.put("review_progress_" + $scope.id, {"date": new Date().toString(), "timeTaken": assignment.timeTaken, "weight": pageLength, "progress": assignment.comments});
            reviewHttp.submitResults($scope, $scope.id, assignment, function () {
                $scope.SYNERGY.modal.update("Results submitted", "Thank you for review");
                $scope.SYNERGY.modal.show();
                $scope.SYNERGY.cache.clear("review_progress_" + $scope.id);
                removeListener();
                $scope.review = {};
                $scope.failedAttempt = false;
                $scope.currentComment = new SynergyModels.ReviewComment($scope.tutorialUrl);
                window.history.back();
            }, function (data) {
                $scope.SYNERGY.modal.show();// hide wait dialog
                if (!$scope.failedAttempt) {
                    $scope.failedAttempt = true;
                    $("#deleteModal").modal("toggle");
                }
                $scope.SYNERGY.logger.log("Action failed", data, "INFO", "alert-error");
                $scope.SYNERGY.logger.log("Action failed", data.toString(), "DEBUG", "alert-error");
            });
        };

        $scope.addComment = function () {
            if (!$scope.isEditing) {
                $scope.review.comments.push($scope.currentComment);
            }
            $scope.isEditing = false;
            $scope.commentAction = "Add";
            $scope.SYNERGY.cache.put("review_progress_" + $scope.id, {"date": new Date().toString(), "timeTaken": new Date().getTime() - started, "weight": pageLength, "progress": $scope.review.comments});
            $scope.currentComment = new SynergyModels.ReviewComment($scope.review.reviewUrl);
            iframe.contentWindow.postMessage("{\"action\" : \"clear\"}", "*");
        };

        $scope.editComment = function (index) {
            oldCopy = SynergyUtils.shallowClone($scope.review.comments[index]);
            editingIndex = index;
            $scope.currentComment = $scope.review.comments[index];
            $scope.isEditing = true;
            $scope.commentAction = "Edit";
            iframe.contentWindow.postMessage(JSON.stringify({"action": "highlight", "elements": $scope.currentComment.elements}), "*");
        };

        $scope.viewComment = function (index) {
            iframe.contentWindow.postMessage(JSON.stringify({"action": "highlight", "elements": $scope.review.comments[index].elements}), "*");
        };

        $scope.removeComment = function (index) {
            $scope.review.comments.splice(index, 1);
            iframe.contentWindow.postMessage("{\"action\" : \"clear\"}", "*");
        };

        $scope.init(function () {
            fetch();
        });

    }
    function AdminReviewsCtrl($scope, reviewHttp) {

        $scope.existingReviewPages = [];
        $scope.importUrl = "http://wiki.netbeans.org/NetBeansTutorialsForCommunityReview";
        $scope.newReview = {
            title: "",
            owner: "",
            url: ""
        };


        function fetch() {
            if (!$scope.SYNERGY.session.hasAdminRights()) {
                return;
            }
            reviewHttp.list($scope, function (data) {
                $scope.existingReviewPages = data;
            }, $scope.generalHttpFactoryError);
        }

        $scope.importData = function () {
            reviewHttp.importFromUrl($scope, $scope.importUrl, function () {
                $scope.SYNERGY.logger.log("Done", "Data imported", "INFO", "alert-success");
                fetch();
            }, $scope.generalHttpFactoryError);
        };


        $scope.create = function () {
            reviewHttp.create($scope, $scope.newReview, function () {
                $scope.SYNERGY.logger.log("Done", "Review page created", "INFO", "alert-success");
                fetch();
            }, $scope.generalHttpFactoryError);
        };

        $scope.perform = function (action) {
            switch (action) {
                case "create":
                    $scope.newReview = {
                        title: "",
                        owner: "",
                        url: ""
                    };
                    $("#createTutorialModal").modal("toggle");
                    break;
                default :
                    break;
            }
        };


        $scope.init(function () {
            fetch();
        });
    }
    function AdminProjectsCtrl($scope, projectsHttp, projectHttp, $location, SynergyModels) {

        $scope.projects = [];
        var projectAffectedId = 0; // when editing version, modal dialog is opened, this is to show version name when typing new name
        $scope.newname = "";

        $scope.fetch = function () {
            if (!$scope.SYNERGY.session.hasAdminRights()) {
                return;
            }
            projectsHttp.getAll($scope, function (data) {
                $scope.projects = data;
            }, $scope.generalHttpFactoryError);
        };

        $scope.perform = function (action, projectId, projectName) {
            switch (action) {
                case "edit":
                    $location.path("administration/project/" + projectId + "/edit");
                    break;
                case "create":
                    $scope.newname = "";
                    $("#createVersionModal").modal("toggle");
                    break;
                case "delete":
                    projectAffectedId = projectId;
                    $("#deleteModal").modal("toggle");
                    break;
                default :
                    break;
            }
        };

        /**
         * Deletes version
         */
        $scope.remove = function () {
            $("#deleteModal").modal("toggle");
            projectHttp.remove($scope, projectAffectedId, function () {
                $scope.SYNERGY.logger.log("Done", "Project removed", "INFO", "alert-success");
                $scope.fetch();
            }, $scope.generalHttpFactoryError);
        };

        /**
         * Creates a new version
         */
        $scope.create = function () {
            projectHttp.create($scope, new SynergyModels.Version($scope.newname, -1, false), function (data) {
                $scope.SYNERGY.logger.log("Done", "Project created", "INFO", "alert-success");
                $scope.fetch();
            }, $scope.generalHttpFactoryError);
        };
        var self = $scope;
        $scope.init(function () {
            self.fetch();
        });
    }
    function RegisterCtrl($scope, registerHttp, SynergyModels) {

        $scope.username = "";
        $scope.email = "";
        $scope.firstname = "";
        $scope.lastname = "";
        $scope.password = "";
        $scope.password2 = "";

        $scope.register = function () {
            if (!$scope.registerForm.$valid || $scope.password !== $scope.password2) {
                $scope.SYNERGY.logger.log("Oops", "Seems like form is not properly filled", "INFO", "alert-danger");
                return;
            }

            var reg = new SynergyModels.Registration($scope.username, $scope.email, $scope.firstname, $scope.lastname);
            reg.password = $scope.password;
            registerHttp.doRegister($scope, reg, function () {
                window.history.back();
            }, $scope.generalHttpFactoryError);
        };

        $scope.init(function () {
        });
    }
    /**
     * 
     * @param {type} $scope
     * @param {SessionFct} sessionHttp
     * @returns {undefined}
     */
    function LoginCtrl($scope, sessionHttp, $location) {
        $scope.username = "";
        $scope.password = "";

        $scope.login = function () {
            if (!$scope.loginForm.$valid) {
                $scope.SYNERGY.logger.log("Oops", "Seems like form is not properly filled", "INFO", "alert-danger");
                return;
            }
            sessionHttp.login($scope, {username: $scope.username, password: $scope.password}, function () {
                window.history.back();
            }, function (data, status, headers, config) {
                $scope.SYNERGY.logger.log("Oops", "Wrong username or password", "INFO", "alert-danger");
            });
        };

        $scope.init(function () {
        });

    }

    function RecoverCtrl($scope, sessionHttp) {
        $scope.username = "";
        $scope.reset = function () {
            if (!$scope.resetForm.$valid) {
                $scope.SYNERGY.logger.log("Oops", "Seems like form is not properly filled", "INFO", "alert-danger");
                return;
            }
            sessionHttp.resetPassword($scope, $scope.username, function () {
                $scope.SYNERGY.logger.log("Done", "New password has been sent to you, check your email.", "INFO", "alert-success");
            }, function (data, status, headers, config) {
                $scope.SYNERGY.logger.log("Oops", data, "INFO", "alert-danger");
            });
        };

        $scope.init(function () {
        });
    }

    function AdminProjectCtrl($scope, projectHttp, $routeParams, SynergyModels) {
        $scope.project = {
            id: $routeParams.id
        };
        $scope.refreshCodemirror = false;

        function fetch() {
            projectHttp.get($scope, $scope.project.id, function (data) {
                $scope.project = data;
                $scope.refreshCodemirror = true;
            }, $scope.generalHttpFactoryError);
        }

        $scope.init(function () {
            fetch();
        });

        $scope.save = function () {
            var pr = new SynergyModels.Project($scope.project.id, $scope.project.name);
            pr.reportLink = $scope.project.reportLink;
            pr.viewLink = $scope.project.viewLink;
            pr.bugTrackingSystem = $scope.project.bugTrackingSystem;
            pr.multiViewLink = $scope.project.multiViewLink;
            projectHttp.edit($scope, pr, function () {
                $scope.SYNERGY.logger.log("Done", "Project updated", "INFO", "alert-success");
                fetch();
            }, $scope.generalHttpFactoryError);
        };

        $scope.cancel = function () {
            window.history.back();
        };

    }

    function RunCoverageCtrl($scope, $routeParams, versionsHttp, specificationsHttp, testRunCoverageHandler, versionHandler, runHttp, specificationLengthHttp) {

        $scope.allVersions = [];
        $scope.platforms = [];
        $scope.versionToCover = null;
        $scope.selectedVersions = [];
        $scope.specifications = [];
        $scope.syncCounter = 0;
        $scope.syncTarget = 3;
        $scope.testRunId = parseInt($routeParams.id, 10);
        $scope.testRunTitle = null;
        $scope.coverageTotals = null;
        $scope.totals = {
            finishedCases: 0,
            totalCases: 0
        };
        var allSpecifications = [];
        var run = null;
        var specificationSizeCache = {};

        $scope.addVersion = function () {
            var versionToCover = parseInt($scope.versionToCover, 10);
            for (var i = 0, max = $scope.selectedVersions.length; i < max; i++) {
                if ($scope.selectedVersions[i].id === $scope.allVersions[versionToCover].id) {
                    return;
                }
            }
            $scope.selectedVersions.push($scope.allVersions[versionToCover]);
            sortVersions();
            $scope.allVersions[versionToCover].isVisible = false;
        };

        $scope.getPercentage = function (finished, total) {
            if (total === 0 || typeof total === "undefined") {
                return new Number(0).toFixed(2);
            } else {
                return (finished / (total / 100)).toFixed(2);
            }
        };

        $scope.removeCoveredVersion = function ($index) {
            var removedVersion = $scope.selectedVersions.splice($index, 1)[0];
            $scope.allVersions[removedVersion.index].isVisible = true;
            sortVersions();
        };

        $scope.getProgressClass = function (finished, total) {
            if (finished === total && total > 0) { // 1/1
                return "finished";
            } else if (finished === total) { // 0/0
                return "warning";
            } else if (finished > 0) { // 2/4
                return "unfinished";
            } else {
                return "warning"; // 0/4
            }
        };

        $scope.loadCoverage = function () {
            var filteredSpecifications = testRunCoverageHandler.collectSpecificationsTotalsPerPlatform(filterSpecifications(), run.assignments, $scope.platforms, $scope.selectedVersions);
            loadSpecsWithoutAssignment(filteredSpecifications, function () {
                $scope.specifications = filteredSpecifications;
                $scope.coverageTotals = testRunCoverageHandler.getTotalRow($scope.specifications, $scope.platforms);
                $scope.specificationTotals = testRunCoverageHandler.getSpecificationTotalAcrossPlatforms($scope.specifications, $scope.platforms);
                $scope.totals = testRunCoverageHandler.getTotals($scope.specifications, $scope.platforms);
            });
        };

        $scope.isVisible = function (version) {
            return version.isVisible;
        };

        $scope.toggle = function () {
            $scope.coverageTotals = testRunCoverageHandler.getTotalRow($scope.specifications, $scope.platforms);
            $scope.specificationTotals = testRunCoverageHandler.getSpecificationTotalAcrossPlatforms($scope.specifications, $scope.platforms);
            $scope.totals = testRunCoverageHandler.getTotals($scope.specifications, $scope.platforms);
        };

        function loadSpecsWithoutAssignment(specifications, callback) {
            var query = testRunCoverageHandler.getEmptySpecifications(specifications, specificationSizeCache);
            if (query.ids.length > 0) {
                specificationLengthHttp.get($scope, query, function (data) {
                    for (var id in data) {
                        if (data.hasOwnProperty(id) && !specificationSizeCache.hasOwnProperty(id)) {
                            specificationSizeCache[id] = data[id];
                        }
                    }
                    testRunCoverageHandler.addCaseCounts(specifications, specificationSizeCache);
                    callback();
                }, $scope.generalHttpFactoryError);
            } else {
                testRunCoverageHandler.addCaseCounts(specifications, specificationSizeCache);
                callback();
            }
        }

        function sortVersions() {
            $scope.selectedVersions.sort(function (a, b) {
                if (a.floatValue > b.floatValue) {
                    return -1;
                }
                if (a.floatValue < b.floatValue) {
                    return 1;
                }
                return 0;
            });
        }

        function fetch() {
            loadVersions();
            loadSpecifications();
            loadRun(parsePlatforms);
        }

        function filterSpecifications() {
            var filteredSpecifications = [];
            var latestMatchingSpec;
            for (var i = 0, max = allSpecifications.length; i < max; i++) {
                latestMatchingSpec = testRunCoverageHandler.getLatestMatchingSpecification(allSpecifications[i], $scope.selectedVersions, $scope.platforms);
                if (latestMatchingSpec) {
                    filteredSpecifications.push(latestMatchingSpec);
                }
            }
            return filteredSpecifications;
        }


        function loadSpecifications() {
            specificationsHttp.get($scope, "all", function (data) {
                allSpecifications = data;
                $scope.syncCounter++;
            }, $scope.generalHttpFactoryError);
        }

        function loadRun(callback) {
            runHttp.get($scope, $scope.testRunId, function (data) {
                run = data;
                $scope.testRunTitle = run.title;
                $scope.$emit("updateBreadcrumbs", {link: "run/" + $scope.testRunId + "/coverage", title: run.title + " Coverage"});
                callback();
                $scope.syncCounter++;
            }, $scope.generalHttpFactoryError);
        }
        /**
         * Need to get platforms from assignments in case platform has been renamed
         * @returns {undefined}
         */
        function parsePlatforms() {
            var platformsObj = {};
            for (var i = 0, max = run.assignments.length; i < max; i++) {
                if (!platformsObj.hasOwnProperty(run.assignments[i].platform)) {
                    platformsObj[run.assignments[i].platform] = {
                        name: run.assignments[i].platform,
                        isVisible: true
                    };
                }
            }
            var platforms = [];
            var _p;
            var index = 0;
            for (var p in platformsObj) {
                _p = platformsObj[p];
                _p.index = index;
                index++;
                platforms.push(_p);
            }
            $scope.platforms = platforms;
        }

        function loadVersions() {
            versionsHttp.get($scope, true, function (data) {
                for (var i = 0, max = data.length; i < max; i++) {
                    data[i].isVisible = true;
                    data[i].index = i;
                    data[i].floatValue = versionHandler.getVersionFloatValue(data[i].name);
                }
                $scope.allVersions = data;
                $scope.syncCounter++;
            }, $scope.generalHttpFactoryError);
        }

        $scope.init(function () {
            fetch();
        });
    }

    angular.module("synergy.controllers", ["synergy.models", "synergy.utils", "synergy.models", "synergy.core"])
            .controller("SynergyCtrl", ["$scope", "$location", "sessionHttp", "$cookieStore", "$timeout", "ngProgress", "$templateCache", "searchHttp", "projectsHttp", "sessionService", "SynergyApp", "specificationCache", "SynergyUtils", "SynergyCore", SynergyCtrl])
            .controller("SearchCtrl", ["$scope", "$routeParams", "searchHttp", SearchCtrl])
            .controller("SpecPoolCtrl", ["$scope", "$routeParams", "specificationsHttp", "versionsHttp", SpecPoolCtrl])
            .controller("HomeCtrl", ["$scope", "specificationsHttp", "runsHttp", "calendarHttp", HomeCtrl])
            .controller("RunCtrl", ["$scope", "utils", "$location", "$routeParams", "runHttp", "assignmentHttp", "attachmentHttp", "reviewHttp", "SynergyUtils", "SynergyHandlers", "SynergyIssue", RunCtrl])
            .controller("RunCtrlCase", ["$scope", "utils", "$location", "$routeParams", "runHttp", "SynergyUtils", "SynergyHandlers", "SynergyIssue", RunCtrlCase])
            .controller("RunCtrlUser", ["$scope", "$location", "$routeParams", "runHttp", "assignmentHttp", "attachmentHttp", "reviewHttp", "SynergyUtils", "SynergyHandlers", "SynergyIssue", RunCtrlUser])
            .controller("SpecificationCtrl", ["$scope", "utils", "$location", "$routeParams", "versionsHttp", "specificationHttp", "suiteHttp", "attachmentHttp", "usersHttp", "jobHttp", "userHttp", "sanitizerHttp", "labelsHttp", "projectsHttp", "specificationCache", "SynergyUtils", "SynergyModels", "SynergyHandlers", SpecificationCtrl])
            .controller("SuiteCtrl", ["$scope", "utils", "$location", "$routeParams", "$timeout", "specificationHttp", "suiteHttp", "casesHttp", "productsHttp", "sanitizerHttp", "specificationCache", "SynergyModels", SuiteCtrl])
            .controller("CaseCtrl", ["$scope", "utils", "$location", "$routeParams", "suiteHttp", "caseHttp", "imageHttp", "issueHttp", "labelHttp", "sanitizerHttp", "specificationCache", "SynergyModels", "SynergyHandlers", CaseCtrl])
            .controller("ProfileCtrl", ["$scope", "$routeParams", "userHttp", "$location", "SynergyModels", "SynergyHandlers", ProfileCtrl])
            .controller("LabelFilterCtrl", ["$scope", "$routeParams", "labelHttp", LabelFilterCtrl])
            .controller("TribeCtrl", ["$scope", "$location", "$routeParams", "$timeout", "usersHttp", "tribeHttp", "sanitizerHttp", "specificationsHttp", "SynergyUtils", "SynergyModels", TribeCtrl])
            .controller("RunsCtrl", ["$scope", "$routeParams", "runsHttp", RunsCtrl])
            .controller("AssignmentCtrl", ["$scope", "$routeParams", "assignmentHttp", AssignmentCtrl])
            .controller("AdminHomeCtrl", ["$scope", AdminHomeCtrl])
            .controller("AdminVersionCtrl", ["$scope", "versionsHttp", "versionHttp", "SynergyModels", AdminVersionCtrl])
            .controller("AdminPlatformsCtrl", ["$scope", "platformsHttp", "platformHttp", "SynergyModels", AdminPlatformsCtrl])
            .controller("AdminTribesCtrl", ["$scope", "$location", "$timeout", "usersHttp", "tribeHttp", "tribesHttp", "sanitizerHttp", "SynergyUtils", "SynergyModels", AdminTribesCtrl])
            .controller("AdminRunsCtrl", ["$scope", "$location", "$routeParams", "runsHttp", "runHttp", AdminRunsCtrl])
            .controller("AdminRunCtrl", ["$scope", "$routeParams", "runHttp", "$location", "attachmentHttp", "sanitizerHttp", "projectsHttp", "SynergyModels", "SynergyHandlers", AdminRunCtrl])
            .controller("AdminMatrixAssignmentCtrl", ["$scope", "$routeParams", "assignmentsHttp", "usersHttp", "labelsHttp", "platformsHttp", "tribesHttp", "versionsHttp", "SynergyUtils", AdminMatrixAssignmentCtrl])
            .controller("AdminAssignmentCtrl", ["$scope", "$timeout", "$routeParams", "specificationsHttp", "assignmentsHttp", "usersHttp", "labelsHttp", "platformsHttp", "tribesHttp", "versionsHttp", "assignmentHttp", "runHttp", "SynergyUtils", "SynergyModels", AdminAssignmentCtrl])
            .controller("AdminUsersCtrl", ["$scope", "$location", "$routeParams", "userHttp", "usersHttp", AdminUsersCtrl])
            .controller("AdminUserCtrl", ["$scope", "$routeParams", "userHttp", "SynergyModels", AdminUserCtrl])
            .controller("AdminSettingCtrl", ["$scope", "settingsHttp", AdminSettingCtrl])
            .controller("AboutCtrl", ["$scope", "aboutHttp", AboutCtrl])
            .controller("TribesCtrl", ["$scope", "$location", "$timeout", "tribesHttp", TribesCtrl])
            .controller("AdminLogCtrl", ["$scope", "logHttp", "sessionRenewalHttp", AdminLogCtrl])
            .controller("AdminDatabaseCtrl", ["$scope", "databaseHttp", AdminDatabaseCtrl])
            .controller("AssignmentVolunteerCtrl", ["$scope", "$routeParams", "specificationsHttp", "assignmentHttp", "labelsHttp", "platformsHttp", "versionsHttp", "reviewHttp", "runHttp", "SynergyUtils", "SynergyModels", AssignmentVolunteerCtrl])
            .controller("RevisionCtrl", ["$scope", "$routeParams", "revisionsHttp", RevisionCtrl])
            .controller("CalendarCtrl", ["$scope", "calendarHttp", CalendarCtrl])
            .controller("AssignmentTribeCtrl", ["$scope", "$routeParams", "tribesHttp", "labelsHttp", "platformsHttp", "assignmentsHttp", "assignmentHttp", "SynergyModels", AssignmentTribeCtrl])
            .controller("StatisticsCtrl", ["$scope", "statisticsHttp", "$routeParams", "SynergyUtils", "SynergyModels", "SynergyHandlers", "SynergyIssue", StatisticsCtrl])
            .controller("AssignmentCommentsCtrl", ["$scope", "$routeParams", "assignmentHttp", "SynergyUtils", AssignmentCommentsCtrl])
            .controller("ReviewCtrl", ["$scope", "$routeParams", "reviewHttp", "SynergyUtils", "SynergyModels", ReviewCtrl])
            .controller("AdminReviewsCtrl", ["$scope", "reviewHttp", AdminReviewsCtrl])
            .controller("AdminProjectsCtrl", ["$scope", "projectsHttp", "projectHttp", "$location", "SynergyModels", AdminProjectsCtrl])
            .controller("RegisterCtrl", ["$scope", "registerHttp", "SynergyModels", RegisterCtrl])
            .controller("LoginCtrl", ["$scope", "sessionHttp", "$location", LoginCtrl])
            .controller("RecoverCtrl", ["$scope", "sessionHttp", RecoverCtrl])
            .controller("RunCoverageCtrl", ["$scope", "$routeParams", "versionsHttp", "specificationsHttp", "TestRunCoverageHandler", "VersionHandler", "runHttp", "specificationLengthHttp", RunCoverageCtrl])
            .controller("AdminProjectCtrl", ["$scope", "projectHttp", "$routeParams", "SynergyModels", AdminProjectCtrl]);
})();