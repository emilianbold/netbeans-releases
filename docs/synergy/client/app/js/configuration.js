"use strict";
(function () {
    /**
     * 
     * Container for synergy configuration
     * @type Synergy
     */
    function Synergy(SynergyHandlers) {
        var synergy = this;
        this.version = "1.0.12";
        this.hostname = window.location.hostname;
        this.baseURL = this.hostname + "/synergy";
        this.bugtrackingSystems = {};
        this.issues = new function () {
            this.singleIssueLink = function (project, issue, includeText) {
                if (synergy.bugtrackingSystems.hasOwnProperty(project) && typeof synergy.bugtrackingSystems[project].getDisplayLink === "function") {
                    return synergy.bugtrackingSystems[project].getDisplayLink(issue, includeText);
                }
                return "";
            };
            this.viewLinkObjects = function (project, issues, includeText) {
                if (issues instanceof Array) {
                    return this.viewLink(project, issues);
                }
                var _all = [];
                for (var i in issues) {
                    if (issues.hasOwnProperty(i)) {
                        _all.push(issues[i]);
                    }
                }
                return this.viewLink(project, _all, includeText);
            };

            this.viewLink = function (project, issues, includeText) {
                if (synergy.bugtrackingSystems.hasOwnProperty(project) && typeof synergy.bugtrackingSystems[project].getMultiDisplayLink === "function") {
                    return synergy.bugtrackingSystems[project].getMultiDisplayLink(issues, includeText);
                }
                return "";
            };
            this.reportLink = function (project, product, component, version, summary, caseId, suiteId) {
                if (synergy.bugtrackingSystems.hasOwnProperty(project) && typeof synergy.bugtrackingSystems[project].getReportLink === "function") {
                    return synergy.bugtrackingSystems[project].getReportLink(product, component, version, summary, caseId, suiteId);
                }
                return "";
            };
        };

        this.assignmentPage = 20;
        this.commentsPage = 30;
        this.adminRoles = ["admin", "manager"];
        this.publisher = new SynergyHandlers.SynergyObserver();
        this.logger = new SynergyHandlers.SynergyLogger();
        this.httpTimeout = 60000;
        /**
         * Fallback for backward compatibility (in prev. versions of Synergy, specification didn't have project property
         * @type String
         */
        this.product = "NetBeans";

        /**
         * If true, Synergy will track how long was given case being tested and update estimated case duration with this value after submitting
         * @type Boolean
         */
        this.trackCaseDuration = true;

        /**
         * Default number of miliseconds before cookie is expired
         * @type Number
         */
        this.defaultCookiesExpiration = 12 * 60 * 60 * 1000;
        this.uploadFileLimit = 20000000;
        /**
         * For dialogs. Properties modal, modalBody and modalHeader are IDs (with #) of elements in modal div
         * Sample usage: $scope.SYNERGY.modal.update("Login failed", "Incorrect credentials, please try again" + data.toString());
         $scope.SYNERGY.modal.show();
         * @type type
         */
        this.modal = {
            modal: "#myModal",
            modalBody: "#modal-body",
            modalHeader: "#myModalLabel",
            update: function (header, body) {
                $(this.modalHeader).text(header);
                $(this.modalBody).text(body);
            },
            show: function () {
                $(this.modal).modal("toggle");
            }
        };


        this.server = new SynergyHandlers.SynergyServer({
            "db": "../../server/api/db.php",
            "specifications": "../../server/api/specifications.php",
            "specification": "../../server/api/specification.php",
            "session": "../../server/api/login.php",
            "assignment": "../../server/api/assignment.php",
            "assignments": "../../server/api/assignments.php",
            "assignment_bugs": "../../server/api/assignment_bugs.php",
            "tribe_assignments": "../../server/api/tribe_assignments.php",
            "attachment": "../../server/api/attachment.php",
            "attachments": "../../server/api/attachments.php",
            "run_attachment": "../../server/api/run_attachment.php",
            "favorites": "../../server/api/favorites.php",
            "favorite": "../../server/api/favorite.php",
            "suite": "../../server/api/suite.php",
            "case": "../../server/api/case.php",
            "cases": "../../server/api/cases.php",
            "job": "../../server/api/job.php",
            "label": "../../server/api/label.php",
            "labels": "../../server/api/labels.php",
            "log": "../../server/api/log.php",
            "user": "../../server/api/user.php",
            "profile_img": "../../server/api/profile_img.php",
            "users": "../../server/api/users.php",
            "tribe": "../../server/api/tribe.php",
            "tribe_specification": "../../server/api/tribe_specification.php",
            "tribes": "../../server/api/tribes.php",
            "versions": "../../server/api/versions.php",
            "version": "../../server/api/version.php",
            "platform": "../../server/api/platform.php",
            "platforms": "../../server/api/platforms.php",
            "runs": "../../server/api/runs.php",
            "image": "../../server/api/image.php",
            "images": "../../server/api/images.php",
            "issue": "../../server/api/issue.php",
            "proxy": "../../server/api/proxy.php",
            "run": "../../server/api/run.php",
            "run_notifications": "../../server/api/run_notifications.php",
            "events": "../../server/api/events.php",
            "configuration": "../../server/api/configuration.php",
            "about": "../../server/api/about.php",
            "sanitizer": "../../server/api/sanitizer.php",
            "search": "../../server/api/search.php",
            "products": "../../server/api/products.php",
            "revisions": "../../server/api/revisions.php",
            "statistics": "../../server/api/statistics.php",
            "statistics_archived": "../../server/data/test_runs/",
            "statistics_fallback": "../../server/archive/test_runs_data/",
            "statistics_filter": "../../server/api/statistics_filter.php",
            "comments": "../../server/api/comments.php",
            "assignment_comments": "../../server/api/assignment_comments.php",
            "specification_request": "../../server/api/specification_request.php",
            "versionLength": "../../server/api/specification_length.php",
            "assignment_exists": "../../server/api/assignment_exists.php",
            "review": "../../server/api/review.php",
            "review_assignment": "../../server/api/review_assignment.php",
            "reviews": "../../server/api/reviews.php",
            "projects": "../../server/api/projects.php",
            "project": "../../server/api/project.php",
            "register": "../../server/api/register.php",
            "run_specifications": "../../server/api/run_specifications.php",
            "run_tribes": "../../server/api/run_tribes.php",
            "refresh": "../../server/api/refresh.php"
        });
        /**
         * To specify server endpoints used by Synergy Client
         * 
         */


        /**
         * Holds information about current session and modifies page upon session state
         */
        this.session = {
            isLoggedIn: false,
            username: "",
            firstName: "",
            lastName: "",
            role: "",
            created: "",
            session_id: "",
            token: "",
            cookieIsValid: function (creationTime) {
                return ((new Date().getTime() - parseInt(creationTime, 10)) < synergy.defaultCookiesExpiration);
            },
            /**
             * Hides login form after successful login
             * 
             */
            hideLoginForm: function () {
                $("#synergy_login_form").css("display", "none");
                $("#synergy_login_form_log").css("display", "none");
            },
            clearSession: function () {
                synergy.session.isLoggedIn = false;
                synergy.session.username = "";
                synergy.session.lastName = "";
                synergy.session.firstName = "";
                synergy.session.role = "";
                synergy.session.created = -1;
                synergy.session.session_id = "";
                synergy.session.token = "";
                synergy.session.showLoginForm();
                synergy.session.hideUserMenu();
            },
            setSession: function (data) {
                synergy.session.hideLoginForm();
                synergy.session.showUserMenu(data.username);
                synergy.session.isLoggedIn = true;
                synergy.session.username = data.username;
                synergy.session.lastName = data.lastName;
                synergy.session.firstName = data.firstName;
                synergy.session.role = data.role;
                synergy.session.session_id = data.session_id;
                synergy.session.token = data.session_id;
                synergy.session.created = data.created;
            },
            showUserMenu: function (username) {
                $("#usermenu_user").html(username + "&nbsp;<b class=\"caret\" id=\"userCaret\"></b>");
                $("#synergy_usermenu").css("display", "block");
            },
            showLoginForm: function () {
                $("#synergy_login_form").css("display", "block");
            },
            hideUserMenu: function () {
                $("#synergy_usermenu").css("display", "none");
            },
            hasAdminRights: function () {
                if (typeof synergy.session !== "undefined" && typeof synergy.session.role !== "undefined" && synergy.adminRoles.indexOf(synergy.session.role) > -1) {
                    return true;
                } else {
                    return false;
                }
            },
            /**
             * Displays user menu that is shown if user is logged in
             * 
             */
            createUserMenu: function () {
                $("#synergy_session").append("<ul class=\"nav pull-right\"><li class=\"dropdown\"><a href=\"#\" class=\"dropdown-toggle btn-primary\" data-toggle=\"dropdown\" style=\"color: white\">" + synergy.session.username + " <b class=\"caret\"></b></a>" +
                        "<ul class=\"dropdown-menu\"><li><a href=\"#favorites\">Favorites</a></li><li><span ng-click=\"logout();\">Logout</span></li>" +
                        "</ul></li></ul>");
            }
        };

        /**
         * Some useful functions
         */
        this.util = {
            /**
             * Converts associated array (object) to indexed based array
             * @param {type} data
             * @returns {Array|Synergy.util.toIndexedArray._a}
             */
            toIndexedArray: function (data) {
                var _a = [];
                for (var i in data) {
                    if (data.hasOwnProperty(i)) {
                        _a.push(data[i]);
                    }
                }
                return _a;
            },
            /**
             * Sets cookie value
             * @param {type} name
             * @param {type} value
             */
            setCookie: function (name, value) {
                var date = new Date();
                date.setTime(date.getTime() + (synergy.defaultCookiesExpiration));
                var expires = "; expires=" + date.toGMTString();
                window.document.cookie = name + "=" + value + expires + "; path=/";
            },
            /**
             * Scrolls window so the beginning of element with given ID is visible in viewport
             * @param {type} elementID
             * @returns {undefined}
             */
            scrollTo: function (elementID) {
                var positionX = 0;
                var positionY = 0;
                var navbar = window.document.getElementById("navbar-top");
                var element = window.document.getElementById(elementID);
                while (element !== null) {
                    positionX += element.offsetLeft;
                    positionY += element.offsetTop;
                    element = element.offsetParent;
                }
                window.scrollTo(positionX, positionY - navbar.offsetHeight);
            },
            /**
             * Returns cookie value
             * @param {type} name
             * @returns {unresolved}
             */
            getCookie: function (name) {
                name += "=";
                var ca = window.document.cookie.split(";");
                for (var i = 0; i < ca.length; i++) {
                    var c = ca[i];
                    while (c.charAt(0) === " ") {
                        c = c.substring(1, c.length);
                    }
                    if (c.indexOf(name) === 0) {
                        return c.substring(name.length, c.length);
                    }
                }
            },
            /**
             * Deletes cookie
             * @param {type} name
             * @returns {undefined}
             */
            deleteCookie: function (name) {
                window.document.cookie = name + "=;; path=/";
            },
            encodeHTML: function () {
                // encodes all <pre> tags
                var pre = $("pre");
                pre.html($("<div/>").text(pre.html).html());

            }


        };

        /**
         * Manipulates with cache used in Synergy. This implementation relies on localStorage
         */
        this.cache = {
            /**
             * If value with given key exists, updates it. Otherwise new record is stored in localstorage
             */
            "put": function (key, value) {
                if (window.localStorage) {
                    window.localStorage.removeItem(key);
                    try {
                        window.localStorage.setItem(key, JSON.stringify(value));
                    } catch (e) {
                        if (e.code === 22 || e.code === 21 || e.code === 20) {
                            this.drop();
                            window.localStorage.setItem(key, JSON.stringify(value));
                        }
                    }
                }
            },
            /**
             * Removes everything from localStorage
             * @returns {undefined}
             */
            "drop": function () {
                for (var i = 0, max = window.localStorage.length; i < max; i++) {
                    window.localStorage.removeItem(window.localStorage.key(0));
                }
            },
            "clear": function (key) {
                if (window.localStorage) {
                    window.localStorage.removeItem(key);
                }
            },
            "get": function (key) {
                if (window.localStorage) {
                    return JSON.parse(window.localStorage.getItem(key));
                }
                return null;
            }

        };

        /**
         * URL where should Synergy redirect you to login
         */
        this.ssoLoginUrl = "https://netbeans.org/people/login?original_uri=";
        this.ssoLogoutUrl = "https://netbeans.org/people/logout?original_uri=";
        this.getLoginRedirectUrl = function (loginUrl, redirectUrl) {
            loginUrl += encodeURI(redirectUrl);
            return loginUrl + "?revalidate=1";
        };
        this.getLogoutRedirectUrl = function (logoutUrl, redirectUrl) {
            logoutUrl += encodeURI(redirectUrl);
            return logoutUrl + "?revalidate=1";
        };

        this.useSSO = false;
    }

    angular.module("synergy.core", ["synergy.handlers"])
            .factory("SynergyCore", ["SynergyHandlers", function (SynergyHandlers) {
                    var _appCore = null;
                    return {
                        init: function () {
                            if (_appCore !== null) {
                                throw new Error("Application already initialized");
                            } else {
                                _appCore = new Synergy(SynergyHandlers);
                                return _appCore;
                            }
                        }
                    };
                }]);
})();