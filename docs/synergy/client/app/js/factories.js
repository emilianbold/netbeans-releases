"use strict";

angular.module("synergy.test", []).factory("synergyTest", ["$http", function ($http) {
        return {
            test: function ($scope) {
                window.console.log($scope.SYNERGY);
            }
        };
    }]);

angular.module("synergy.http", []).factory("specificationsHttp", ["$http", "$timeout", function ($http, $timeout) {

        /**
         * Handles operation with a list of specifications
         * @param {type} $http
         * @returns {SpecificationsFct}
         */
        function SpecificationsFct($http, $timeout) {

            /**
             * Loads latest specifications
             * @param {type} $scope
             * @param {Function} onSuccess
             * @param {Function} onFail
             */
            this.latest = function ($scope, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("specifications", {"mode": "latest", limit: 10}), {"cache": true, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Loads all specifications for given version
             * @param {type} $scope
             * @param {String} version
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.get = function ($scope, version, onSuccess, onFail) {
                version = (version.length > 0) ? {"version": version} : {};
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("specifications", version), {"cache": false, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Loads specifications matching given filter
             * @param {type} $scope
             * @param {String} filter
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.filter = function ($scope, filter, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("specifications", {"query": filter, "mode": "filter"}), {"cache": false, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

        }

        return new SpecificationsFct($http, $timeout);
    }]).factory("versionsHttp", ["$http", "$timeout", function ($http, $timeout) {

        function VersionsFct($http, $timeout) {

            /**
             * Retrieves list of versions (visible ones)
             * @param {Function} onSuccess
             * @param {Function} onFail
             */
            this.get = function ($scope, useCache, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("versions", {}), {"cache": useCache, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Retrieves all versions, even inactive ones
             */
            this.getAll = function ($scope, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("versions", {"all": 1}), {"cache": false, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }

        return new VersionsFct($http, $timeout);
    }]).factory("specificationLengthHttp", ["$http", "$timeout", function ($http, $timeout) {

        function SpecificationLengthHttp($http, $timeout) {

            /**
             * Returns number of test cases of given specifications
             * @param {Object} query object with array property called "ids" and array contains spec IDs
             * @param {Function} onSuccess
             * @param {Function} onFail
             */
            this.get = function ($scope, query, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("versionLength", {}), JSON.stringify(query)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };


        }

        return new SpecificationLengthHttp($http, $timeout);
    }]).factory("versionHttp", ["$http", "$timeout", function ($http, $timeout) {

        function VersionFct($http, $timeout) {


            /**
             * Updates version
             * @param {type} $scope
             * @param {Synergy.model.Version} version object with 2 properties: id and name
             * @param {Function} onSuccess
             * @param {Function} onFail
             */
            this.edit = function ($scope, version, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("version", {}), JSON.stringify(version)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Removes version
             * @param {type} $scope
             * @param {Number} versionId version ID
             * @param {Function} onSuccess
             * @param {Function} onFail
             */
            this.remove = function ($scope, versionId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http({method: "DELETE", url: $scope.SYNERGY.server.buildURL("version", {"id": versionId})}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Creates version
             * @param {type} $scope
             * @param {Synergy.model.Version} version new version to be created with property "name"
             * @param {Function} onSuccess
             * @param {Function} onFail
             */
            this.create = function ($scope, version, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("version", {}), JSON.stringify(version)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }

        return new VersionFct($http, $timeout);
    }]).factory("userHttp", ["$http", "$timeout", function ($http, $timeout) {

        function UserFct($http, $timeout) {

            /**
             * Updates user
             * @param {type} $scope
             * @param {Synergy.model.User} user
             * @param {Function} onSuccess
             * @param {Function} onFail
             */
            this.edit = function ($scope, user, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("user", {"action": "editUser"}), JSON.stringify(user)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Retrieves user
             * @param {String} username user"s username
             * @param {Function} onSuccess
             * @param {Function} onFail
             */
            this.get = function ($scope, username, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("user", {"user": username}), {"cache": false, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Creates user
             * @param {type} $scope
             * @param {Synergy.model.User} user user
             * @param {Function} onSuccess
             * @param {Function} onFail
             */
            this.create = function ($scope, user, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("user", {}), JSON.stringify(user)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Toggles state of user"s favorite specification
             * @param {type} $scope
             * @param {Synergy.model.Specification} specification
             * @param {Function} onSuccess
             * @param {Function} onFail
             */
            this.toggleFavorite = function ($scope, specification, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("user", {"action": "toggleFavorite"}), JSON.stringify(specification)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Removes user
             * @param {type} $scope
             * @param {String} username
             * @param {Function} onSuccess
             * @param {Function} onFail
             */
            this.remove = function ($scope, username, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http({method: "DELETE", url: $scope.SYNERGY.server.buildURL("user", {"username": username})}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            this.resetProfileImg = function ($scope, userId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http({method: "DELETE", url: $scope.SYNERGY.server.buildURL("profile_img", {"id": userId})}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }

        return new UserFct($http, $timeout);
    }]).factory("specificationHttp", ["$http", "$timeout", function ($http, $timeout) {

        function SpecificationFct($http, $timeout) {

            /**
             * Deletes specification
             * @param {type} $scope
             * @param {Number} specificationId specification id
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.remove = function ($scope, specificationId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http({method: "DELETE", url: $scope.SYNERGY.server.buildURL("specification", {"id": specificationId})}).success(function (result, status) {
                    $timeout(function () {
                        onSuccess(result, status);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Sends request for onwership to server
             * @param {type} $scope
             * @param {Synergy.model.OwnershipRequest} request
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.requestOwnership = function ($scope, request, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("specification_request", {}), JSON.stringify(request)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Updates specification
             * @param {type} $scope
             * @param {Synergy.model.Specification} specification
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.edit = function ($scope, specification, isMinorEdit, keepSimpleNameTrack, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("specification", {"id": specification.id, "minorEdit": isMinorEdit, "keepSimpleName": keepSimpleNameTrack}), JSON.stringify(specification)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Creates specification
             * @param {type} $scope
             * @param {Synergy.model.Specification} specification
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.create = function ($scope, specification, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("specification", {"mode": "create"}), JSON.stringify(specification)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Returns full specification (for continuous view 2)
             * @param {type} $scope
             * @param {Number} specificationId specification id
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.getFull = function ($scope, useCache, specificationId, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("specification", {"view": "cont", "id": specificationId}), {"cache": false, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Returns full specification based on simple name and version
             */
            this.getFullAlias = function ($scope, useCache, simpleName, simpleVersion, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("specification", {"view": "contAlias", "id": -1, "simpleName": simpleName, "simpleVersion": simpleVersion}), {"cache": false, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Returns basic specification info (for view 1)
             * @param {type} $scope
             * @param {Number} specificationId specification id
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.get = function ($scope, useCache, specificationId, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("specification", {"id": specificationId}), {"cache": false, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Clones specification
             * @param {type} $scope
             * @param {Number} specificationId specification id
             * @param {String} newName name of new cloned specification
             * @param {String} clonedVersion version to be cloned of (target version)
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.clone = function ($scope, specificationId, newName, clonedVersion, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                var spec = {
                    "newName": newName,
                    "version": clonedVersion
                };
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("specification", {"mode": "clone", "id": specificationId}), JSON.stringify(spec)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

        }

        return new SpecificationFct($http, $timeout);
    }]).factory("suiteHttp", ["$http", "$timeout", function ($http, $timeout) {

        function SuiteFct($http, $timeout) {

            /**
             * Deletes suites
             * @param {type} $scope
             * @param {Number} suiteId suite id
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.remove = function ($scope, suiteId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http({method: "DELETE", url: $scope.SYNERGY.server.buildURL("suite", {"id": suiteId})}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Returns suite
             * @param {type} $scope
             * @param {Number} suiteId suite id
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.get = function ($scope, useCache, suiteId, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("suite", {"id": suiteId}), {"cache": false, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Removes given case from suite
             * @param {type} $scope
             * @param {Number} suiteId suiteID to be affected
             * @param {Number} caseId caseID to be removed from suite
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.removeCase = function ($scope, suiteId, caseId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("suite", {"id": suiteId, "caseId": caseId, "action": "deleteCase"})).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Adds given case from suite
             * @param {type} $scope
             * @param {Number} suiteId suiteID to be affected
             * @param {Number} caseId caseID to be added to suite
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.addCase = function ($scope, suiteId, caseId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("suite", {"id": suiteId, "caseId": caseId, "action": "addCase"})).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Updates suite
             * @param {type} $scope
             * @param {Synergy.model.Suite} suite
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.edit = function ($scope, suite, isMinorEdit, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("suite", {"id": suite.id, "minorEdit": isMinorEdit}), JSON.stringify(suite)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Creates suite
             * @param {type} $scope
             * @param {Synergy.model.Suite} suite
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.create = function ($scope, suite, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("suite", {"id": suite.id}), JSON.stringify(suite)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }

        return new SuiteFct($http, $timeout);
    }]).factory("casesHttp", ["$http", "$timeout", function ($http, $timeout) {

        function CasesFct($http, $timeout) {

            /**
             * Returns cases with titles matching to given parameter
             * @param {type} $scope
             * @param {String} filter 
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.getMatching = function ($scope, filter, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("cases", {"case": filter}), {"cache": true, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }

        return new CasesFct($http, $timeout);
    }]).factory("caseHttp", ["$http", "$timeout", function ($http, $timeout) {

        function CaseFct($http, $timeout) {

            /**
             * Returns case in context of given suite
             * @param {type} $scope
             * @param {Number} caseId
             * @param {Number} suiteId
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.get = function ($scope, useCache, caseId, suiteId, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("case", {"id": caseId, "suite": suiteId}), {"cache": false, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Creates case
             * @param {type} $scope
             * @param {Synergy.model.TestCase} testCase
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.create = function ($scope, testCase, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("case", {}), JSON.stringify(testCase)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Updates case
             * @param {type} $scope
             * @param {Number} mode if 0, this test case will be cloned and modifications will be applied only to this suite, if 1 all suites will be affected
             * @param {Synergy.model.TestCase} testCase
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.edit = function ($scope, mode, testCase, isMinorEdit, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("case", {"id": testCase.id, "action": "editCase", "mode": mode, "minorEdit": isMinorEdit}), JSON.stringify(testCase)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }

        return new CaseFct($http, $timeout);
    }]).factory("runsHttp", ["$http", "$timeout", function ($http, $timeout) {

        function RunsFct($http, $timeout) {

            /**
             * Returns latest test runs
             * @param {type} $scope
             * @param {Number} limit number of runs to be returned
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.getLatest = function ($scope, limit, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("runs", {"mode": "latest", "limit": limit}), {"cache": true, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    window.console.log("____" + typeof onFail);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Returns paginated test runs
             * @param {type} $scope
             * @param {Number} page page number (first page is 1)
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.get = function ($scope, page, onSuccess, onFail, cache) {
                cache = (typeof cache === "undefined") ? false : cache;
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("runs", {"page": page}), {"timeout": $scope.SYNERGY.httpTimeout, "cache": cache}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }

        return new RunsFct($http, $timeout);
    }]).factory("runHttp", ["$http", "$timeout", function ($http, $timeout) {

        function RunFct($http, $timeout) {

            /**
             * Returns test run (full info)
             * @param {type} $scope
             * @param {Number} runId test run ID
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.get = function ($scope, runId, onSuccess, onFail) {
                this._get($scope, runId, onSuccess, onFail, "full");
            };

            this._get = function ($scope, runId, onSuccess, onFail, mode) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("run", {"id": runId, "mode": mode}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            this.getBlobs = function ($scope, runId, onSuccess, onFail) {
               this._get($scope, runId, onSuccess, onFail, "blob");
            };

            /**
             * Returns specifications for given test run 
             * @param {type} $scope
             * @param {Number} runId test run ID
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.getSpecifications = function ($scope, runId, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("run_specifications", {"testRunId": runId}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
            /**
             * Sends request to send mail notifications to all assignees with incomplete assignment
             */
            this.sendNotifications = function ($scope, runId, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("run_notifications", {"id": runId, "mode": "full"}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Returns test run like simple get(), but assignments are grouped by specification and user in instances of User instead of TestAssignment
             * @param {type} $scope
             * @param {type} runId
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.getUserCentric = function ($scope, runId, onSuccess, onFail) {
                this._get($scope, runId, onSuccess, onFail, "peruser");
            };

            /**
             * Returns overview of test run (basic info)
             * @param {type} $scope
             * @param {Number} runId test run ID
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.getOverview = function ($scope, useCache, runId, onSuccess, onFail) {
                this._get($scope, runId, onSuccess, onFail, "simple");
            };

            /**
             * Deletes test run
             * @param {type} $scope
             * @param {Number} runId test run ID
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.remove = function ($scope, runId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http({method: "DELETE", url: $scope.SYNERGY.server.buildURL("run", {"id": runId})}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Modifies test run
             * @param {type} $scope
             * @param {Synergy.model.TestRun} run test run
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.edit = function ($scope, run, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("run", {"id": run.id}), JSON.stringify(run)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Sends request to toggle on/off test run freeze
             * @param {type} $scope
             * @param {type} runId
             * @param {Number} freeze if 1, run will be frozen, if 0 it will be unfrozen
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.freezeRun = function ($scope, runId, freeze, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("run", {"id": runId, "mode": "freeze", "freeze": freeze}), JSON.stringify({})).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Creates test run
             * @param {type} $scope
             * @param {Synergy.model.TestRun} run test run
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.create = function ($scope, run, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("run", {}), JSON.stringify(run)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }

        return new RunFct($http, $timeout);
    }]).factory("assignmentHttp", ["$http", "$timeout", function ($http, $timeout) {

        function AssignmentFct($http, $timeout) {

            /**
             * Returns test assignment
             * @param {type} $scope
             * @param {Number} assignmentId assignment ID
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.get = function ($scope, assignmentId, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("assignment", {"id": assignmentId}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
            /**
             * Checks if similar (same) assignment already exists. Unlike any other method in these factories, this method returns promise (to test different approach)
             * @param {type} $scope
             * @param {Synergy.model.TestAssignment} assignment
             * @returns {Promise} $promise
             */
            this.checkExists = function ($scope, assignment) {
                $scope.busyModeOn();
                return $http.post($scope.SYNERGY.server.buildURL("assignment_exists", {}), JSON.stringify(assignment), {"timeout": $scope.SYNERGY.httpTimeout});
            };

            /**
             * Returns assignment name and its comments
             */
            this.getComments = function ($scope, assignmentId, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("assignment_comments", {"id": assignmentId}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Removes test assignment
             * @param {type} $scope
             * @param {Number} assignmentId assignment ID
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.remove = function ($scope, assignmentId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http({method: "DELETE", url: $scope.SYNERGY.server.buildURL("assignment", {"id": assignmentId})}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
            /**
             * Removes test assignment with explanation
             * @param {type} $scope
             * @param {Number} assignmentId assignment ID
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.removeByLeader = function ($scope, assignmentId, explanation, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http({method: "DELETE", url: $scope.SYNERGY.server.buildURL("assignment", {"id": assignmentId}), headers: {"Synergy-comment": encodeURIComponent(explanation)}}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Starts test assignment
             * @param {type} $scope
             * @param {Number} assignmentId assignment ID
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.start = function ($scope, assignmentId, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("assignment", {"mode": "start", "id": assignmentId}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Loads possible comments
             * @param {type} $scope
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.getCommentTypes = function ($scope, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("comments", {}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Restarts test assignment
             * @param {type} $scope
             * @param {Number} assignmentId assignment ID
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.restart = function ($scope, assignmentId, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.defaults.headers.common["Synergy-Timestamp"] = encodeURIComponent(new Date().toMysqlFormat());
                $http.get($scope.SYNERGY.server.buildURL("assignment", {"mode": "restart", "id": assignmentId, "datetime": new Date().toMysqlFormat()}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Alters issues on closed assignment
             * @param {type} $scope
             * @param {type} assignmentId
             * @param {type} data
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.alterBugs = function ($scope, assignmentId, data, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.defaults.headers.common["Synergy-Timestamp"] = encodeURIComponent(new Date().toMysqlFormat());
                $http.put($scope.SYNERGY.server.buildURL("assignment_bugs", {"id": assignmentId, "datetime": new Date().toMysqlFormat()}), JSON.stringify(data)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Submits test assignment results
             * @param {type} $scope
             * @param {Number} assignmentId assignment ID
             * @param {Object} results
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.submitResults = function ($scope, assignmentId, results, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.defaults.headers.common["Synergy-Timestamp"] = encodeURIComponent(new Date().toMysqlFormat());
                $http.put($scope.SYNERGY.server.buildURL("assignment", {"id": assignmentId, "datetime": new Date().toMysqlFormat()}), JSON.stringify(results)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Sends request to resolve given comments
             * @param {type} $scope
             * @param {Array} comments array of objects with single property "id"
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.resolveComments = function ($scope, comments, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("assignment_comments", {}), JSON.stringify(comments)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Creates test assignment
             * @param {type} $scope
             * @param {Synergy.model.TestAssignment} assignment
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.create = function ($scope, assignment, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("assignment", {}), JSON.stringify(assignment)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
            /**
             * Creates a volunteerr test assignment
             * @param {type} $scope
             * @param {Synergy.model.TestAssignment} assignment
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.createVolunteer = function ($scope, assignment, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("assignment", {"volunteer": true}), JSON.stringify(assignment)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }

        return new AssignmentFct($http, $timeout);
    }]).factory("usersHttp", ["$http", "$timeout", function ($http, $timeout) {

        function UsersFct($http, $timeout) {

            /**
             * Returns paginated list of users
             * @param {type} $scope
             * @param {Number} page page number, starts with 1
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.get = function ($scope, page, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("users", {"page": page}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            this.retireUsersWithRole = function ($scope, roleName, onSuccess, onFail) {
                $scope.busyModeOn();
                $http({method: "PUT", url: $scope.SYNERGY.server.buildURL("users", {"role": roleName})}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Requests user imports
             * @param {type} $scope
             * @param {type} sourceUrl url from which users should be imported
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.importUsers = function ($scope, sourceUrl, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("users", {}), JSON.stringify({url: sourceUrl})).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Returns all users in single page
             * @param {type} $scope
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.getAll = function ($scope, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("users", {}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Returns users matching given username
             * @param {type} $scope
             * @param {String} username username
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.getMatching = function ($scope, username, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("users", {"user": username}), {"cache": true, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }

        return new UsersFct($http, $timeout);
    }]).factory("attachmentHttp", ["$http", "$timeout", function ($http, $timeout) {


        function AttachmentFct($http, $timeout) {


            this.getAttachmentsForSpecification = function ($scope, specificationId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http({method: "GET", url: $scope.SYNERGY.server.buildURL("attachments", {"id": specificationId})}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Deletes test run attachment
             * @param {type} $scope
             * @param {Number} attachmentId attachment ID
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.removeRunAttachment = function ($scope, attachmentId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http({method: "DELETE", url: $scope.SYNERGY.server.buildURL("run_attachment", {"id": attachmentId})}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Deletes specification attachment
             * @param {type} $scope
             * @param {Number} attachmentId attachment ID
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.removeSpecAttachment = function ($scope, attachmentId, specificationId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http({method: "DELETE", url: $scope.SYNERGY.server.buildURL("attachment", {"id": attachmentId, "specificationId": specificationId})}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }
        return new AttachmentFct($http, $timeout);
    }]).factory("labelHttp", ["$http", "$timeout", function ($http, $timeout) {

        function LabelFct($http, $timeout) {

            /**
             * Returns all cases with given label (paginated, first page is 1)
             * @param {type} $scope
             * @param {String} label
             * @param {Number} page
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.findCases = function ($scope, label, page, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("label", {"page": page, "label": encodeURIComponent(label)}), {"cache": true, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Adds label to test case
             * @param {type} $scope
             * @param {type} label {"label": "", "testCaseId": 1}
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.create = function ($scope, label, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("label", {}), JSON.stringify(label)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Removes label from test case
             * @param {type} $scope
             * @param {type} label {"label": "", "testCaseId": 1}
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.remove = function ($scope, label, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("label", {}), JSON.stringify(label)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }

        return new LabelFct($http, $timeout);
    }]).factory("labelsHttp", ["$http", "$timeout", function ($http, $timeout) {

        function LabelsFct($http, $timeout) {

            /**
             * Returns all matching labels (paginated, first page is 1)
             * @param {type} $scope
             * @param {String} label
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.getMatching = function ($scope, label, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("labels", {"label": label}), {"cache": true, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Returns all labels
             * @param {type} $scope     
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.getAll = function ($scope, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("labels", {"all": 1}), {"cache": true, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Adds labels to each test case in given suite
             * @param {type} $scope
             * @param {Number} suiteId
             * @param {String} label
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.createForSuite = function ($scope, suiteId, label, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("labels", {"id": suiteId}), JSON.stringify({"label": label})).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
            /**
             * Removes labels to each test case in given suite
             * @param {type} $scope
             * @param {Number} suiteId
             * @param {String} label
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.removeFromSuite = function ($scope, suiteId, label, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("labels", {"id": suiteId}), JSON.stringify({"label": label})).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

        }

        return new LabelsFct($http, $timeout);
    }]).factory("imageHttp", ["$http", "$timeout", function ($http, $timeout) {

        function ImageFct($http, $timeout) {


            this.getImagesForCase = function ($scope, caseId, suiteId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http({method: "GET", url: $scope.SYNERGY.server.buildURL("images", {"id": caseId, "suiteId": suiteId})}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Removes image
             * @param {type} $scope
             * @param {Number} imageId image id
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.remove = function ($scope, imageId, suiteId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http({method: "DELETE", url: $scope.SYNERGY.server.buildURL("image", {"id": imageId, "suiteId": suiteId})}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }
        return new ImageFct($http, $timeout);
    }]).factory("tribeHttp", ["$http", "$timeout", function ($http, $timeout) {

        function TribeFct($http, $timeout) {

            /**
             * Returns tribe
             * @param {type} $scope
             * @param {Number} tribeId image id
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.get = function ($scope, useCache, tribeId, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("tribe", {"id": tribeId}), {"cache": useCache, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Removes user from tribe
             * @param {type} $scope
             * @param {String} username
             * @param {Number} tribeId
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.revokeMembership = function ($scope, username, tribeId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("tribe", {"id": tribeId, "username": username, "action": "removeMember"})).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Adds user to tribe
             * @param {type} $scope
             * @param {Object} user
             * @param {Number} tribeId
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.newMembership = function ($scope, user, tribeId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("tribe", {"id": tribeId, "action": "addMember"}), JSON.stringify(user)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };


            /**
             * Creates a new tribe
             * @param {type} $scope
             * @param {Synergy.model.Tribe} tribe
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.create = function ($scope, tribe, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("tribe", {}), JSON.stringify(tribe)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Removes tribe
             * @param {type} $scope
             * @param {Number} tribeId
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.remove = function ($scope, tribeId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http({method: "DELETE", url: $scope.SYNERGY.server.buildURL("tribe", {"id": tribeId})}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Updates tribe
             * @param {type} $scope
             * @param {Synergy.model.Tribe} tribe
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.edit = function ($scope, tribe, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("tribe", {"id": tribe.id, "action": "editTribe"}), JSON.stringify(tribe)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Adds specification to tribe
             * @param {type} $scope
             * @param {type} tribeId
             * @param {type} specificationId
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.addSpecification = function ($scope, tribeId, specificationId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("tribe_specification", {"id": tribeId, "specificationId": specificationId}), JSON.stringify({})).success(function (result) {
                    $timeout(function () {
                        onSuccess(result, specificationId);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Removes specification from tribe
             * @param {type} $scope
             * @param {type} tribeId
             * @param {type} specificationId
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.removeSpecification = function ($scope, tribeId, specificationId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http({method: "DELETE", url: $scope.SYNERGY.server.buildURL("tribe_specification", {"id": tribeId, "specificationId": specificationId})}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result, parseInt(specificationId, 10));
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }
        return new TribeFct($http, $timeout);
    }]).factory("platformsHttp", ["$http", "$timeout", function ($http, $timeout) {

        function PlatformsFct($http, $timeout) {

            /**
             * Returns platforms
             * @param {type} $scope
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.get = function ($scope, onSuccess, onFail, cache) {
                cache = (typeof cache === "undefined") ? false : cache;
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("platforms", {}), {"timeout": $scope.SYNERGY.httpTimeout, "cache": cache}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };


            /**
             * Returns platforms that matches to given parameter
             * @param {type} $scope
             * @param {String} filter 
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.getMatching = function ($scope, filter, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("platforms", {"query": filter, "mode": "filter"}), {"cache": true, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }
        return new PlatformsFct($http, $timeout);
    }]).factory("platformHttp", ["$http", "$timeout", function ($http, $timeout) {

        function PlatformFct($http, $timeout) {

            /**
             * Removes platform
             * @param {type} $scope
             * @param {Number} platformId description
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.remove = function ($scope, platformId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http({method: "DELETE", url: $scope.SYNERGY.server.buildURL("platform", {"id": platformId})}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Updates platform
             * @param {type} $scope
             * @param {Synergy.model.Platform} platform
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.edit = function ($scope, platform, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("platform", {"id": platform.id}), JSON.stringify(platform)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Creates platform
             * @param {type} $scope
             * @param {Synergy.model.Platform} platform
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.create = function ($scope, platform, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("platform", {}), JSON.stringify(platform)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }

        return new PlatformFct($http, $timeout);
    }]).factory("tribesHttp", ["$http", "$timeout", function ($http, $timeout) {

        function TribesFct($http, $timeout) {
            /**
             * Returns list of tribes
             * @param {type} $scope
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.get = function ($scope, onSuccess, onFail, cache) {
                $scope.busyModeOn();
                cache = (typeof cache === "undefined") ? false : cache;
                $http.get($scope.SYNERGY.server.buildURL("tribes", {}), {"timeout": $scope.SYNERGY.httpTimeout, "cache": cache}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
            /**
             * Returns tribes with full information (users, specifications)
             * @param {type} $scope
             * @param {type} username Username of user that requested given data. Only tribes with leader that is the same as username will be returned
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.getDetailed = function ($scope, username, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("tribes", {"mode": "full", "leader": username}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
            /**
             * Returns tribes with full information (users, specifications)
             * @param {type} $scope
             * @param {type} username Username of user that requested given data. Only tribes with leader that is the same as username will be returned
             * @param {type} testRunId test run id
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.getTribesForRun = function ($scope, username, testRunId, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("run_tribes", {"leader": username, "testRunId": testRunId}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Imports tribes from given URL
             * @param {type} $scope
             * @param {String} sourceUrl import URL
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.importTribes = function ($scope, sourceUrl, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("tribes", {}), JSON.stringify({url: sourceUrl})).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

        }
        return new TribesFct($http, $timeout);
    }]).factory("sessionHttp", ["$http", "$timeout", function ($http, $timeout) {

        function SessionFct($http, $timeout) {
            /**
             * Returns session information
             * @param {type} $scope
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.get = function ($scope, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("session", {"login": 1}), {"cache": false, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            this.info = function ($scope, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("session", {}), {"cache": false, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            this.infoConditional = function ($scope, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("session", {"return": 1}), {"cache": false, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
            this.infoConditionalPromise = function (url) {
                return $http.get(url, {"cache": false});
            };

            this.infoConditionalCookie = function ($scope, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("session", {"return": 2}), {"cache": false, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Submits credentials to server
             * @param {type} $scope
             * @param {Object} credentials
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.login = function ($scope, credentials, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("session", {}), "username=" + credentials.username + "&password=" + credentials.password, {
                    headers: {"Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"}
                }).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            this.resetPassword = function ($scope, username, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("session", {}), JSON.stringify({username: username}))
                        .success(function (result) {
                            $timeout(function () {
                                onSuccess(result);
                            }, 0);
                        }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Destroys session at server
             * @param {type} $scope
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.logout = function ($scope, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http({method: "DELETE", url: $scope.SYNERGY.server.buildURL("session", {})}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }
        return new SessionFct($http, $timeout);
    }]).factory("settingsHttp", ["$http", "$timeout", function ($http, $timeout) {
        function SettingsFct($http, $timeout) {
            /**
             * Returns server"s settings
             * @param {type} $scope
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.get = function ($scope, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("configuration", {}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Edits server settings
             * @param {type} $scope
             * @param {Object} configuration description
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.edit = function ($scope, configuration, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("configuration", {}), JSON.stringify(configuration)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }
        return new SettingsFct($http, $timeout);
    }]).factory("calendarHttp", ["$http", "$timeout", function ($http, $timeout) {

        function CalendarFct($http, $timeout) {
            /**
             * Returns events from server (test runs etc.)
             * @param {type} $scope
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.getEvents = function ($scope, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("events", {}), {"cache": true, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }
        return new CalendarFct($http, $timeout);
    }]).factory("aboutHttp", ["$http", "$timeout", function ($http, $timeout) {
        function AboutFct($http, $timeout) {

            /**
             * Retrieves list of statistics about Synergy
             * @param {Function} onSuccess
             * @param {Function} onFail
             */
            this.get = function ($scope, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("about", {}), {"cache": true, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }
        return new AboutFct($http, $timeout);
    }]).factory("searchHttp", ["$http", "$timeout", function ($http, $timeout) {

        function SearchFct($http, $timeout) {

            /**
             * Retrieves list of search results
             * @param {Function} onSuccess
             * @param {Function} onFail
             */
            this.get = function ($scope, term, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("search", {"search": encodeURIComponent(term)}), {"cache": true, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
            /**
             * Retrieves list of search results that contains only up to 15 specifications
             */
            this.getFewSpecifications = function ($scope, term, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("search", {"search": encodeURIComponent(term), "specifications": 15, "suites": 0}), {"cache": true, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }

        return new SearchFct($http, $timeout);
    }]).factory("productsHttp", ["$http", "$timeout", function ($http, $timeout) {

        function ProductsFct($http, $timeout) {

            /**
             * Retrieves list of products
             * @param {Function} onSuccess
             * @param {Function} onFail
             */
            this.get = function ($scope, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("products", {}), {"cache": true, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }

        return new ProductsFct($http, $timeout);
    }]).factory("assignmentsHttp", ["$http", "$timeout", function ($http, $timeout) {

        function AssignmentsFct($http, $timeout) {

            this.createForTribes = function ($scope, data, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("tribe_assignments", {}), JSON.stringify(data)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
            this.createForUsers = function ($scope, data, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("assignments", {"mode": "user"}), JSON.stringify(data)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Returns test assignment
             * @param {type} $scope
             * @param {Number} assignmentId assignment ID
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.create = function ($scope, data, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("assignments", {"mode": "matrix"}), JSON.stringify(data)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }
        return new AssignmentsFct($http, $timeout);
    }]).factory("logHttp", ["$http", "$timeout", function ($http, $timeout) {

        function LogFct($http, $timeout) {

            /**
             * loads error log (need to be signed in)
             * @param {type} $scope
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.get = function ($scope, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("log", {})).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Clears log
             */
            this.remove = function ($scope, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http({method: "DELETE", url: $scope.SYNERGY.server.buildURL("log", {})}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }

        return new LogFct($http, $timeout);
    }]).factory("databaseHttp", ["$http", "$timeout", function ($http, $timeout) {

        function DatabaseFct($http, $timeout) {

            /**
             * Retrieves array of tables names
             */
            this.getTables = function ($scope, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("db", {"what": "tables"}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
            /**
             * Retrieves list of columns in given table
             * @param {type} $scope
             * @param {String} table table name
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.getColumns = function ($scope, table, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("db", {"what": "table", "table": table}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
            /**
             * Retrieves records from given table
             * @param {type} $scope
             * @param {type} table table name
             * @param {type} limit number of records to be retrieved
             * @param {type} order type of sorting (DESC or ASC)
             * @param {type} orderBy by which column it should be sorted
             * @param {type} onSuccess 
             * @param {type} onFail
             */
            this.listTable = function ($scope, table, limit, order, orderBy, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("db", {"what": "list", "table": table, "limit": limit, "order": order, "orderBy": orderBy}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }
        return new DatabaseFct($http, $timeout);
    }]).factory("jobHttp", ["$http", "$timeout", function ($http, $timeout) {
        function JobFct($http, $timeout) {

            /**
             * Loads information about job"s status from ci server
             * @param {type} $scope
             * @param {string} jobUrl
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.resolve = function ($scope, job, onSuccess, onFail) {
                $http({
                    method: "JSONP",
                    url: job.jobUrl + "JSON_CALLBACK",
                    cache: false
                }).success(function (data) {
                    data.id = job.id; // so ID from Synergy is preserved
                    onSuccess(data);
                }).error(function (data) {
                    onFail({
                        result: "ABORTED",
                        fullDisplayName: "Job not found",
                        id: job.id,
                        url: job.jobUrl
                    });
                }
                );
            };

            /**
             * Adds job to specification
             * @param {type} $scope
             * @param {Synergy.model.Job} job
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.create = function ($scope, job, onSuccess, onFail) {
                $scope.busyModeOn();
                window.document.body.style.cursor = "wait";
                $http.post($scope.SYNERGY.server.buildURL("job", {}), JSON.stringify(job)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Removes job from specification
             * @param {type} $scope
             * @param {type} jobId
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.remove = function ($scope, jobId, specificationId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http({method: "DELETE", url: $scope.SYNERGY.server.buildURL("job", {"id": jobId, "specificationId": specificationId})}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

        }

        return new JobFct($http, $timeout);
    }]).factory("issueHttp", ["$http", "$timeout", function ($http, $timeout) {

        function IssueFct($http, $timeout) {

            /**
             * Adds given issue to test case
             * @param {type} $scope
             * @param {Number} caseId
             * @param {Object} issue
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.create = function ($scope, issue, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("issue", {}), JSON.stringify(issue)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Removes issue from test case
             * @param {type} $scope
             * @param {type} issue {testCaseId:1, id: 1}
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.remove = function ($scope, issue, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("issue", {}), JSON.stringify(issue)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }
        return new IssueFct($http, $timeout);
    }]).factory("revisionsHttp", ["$http", "$timeout", function ($http, $timeout) {
        function RevisionsFct($http, $timeout) {

            /**
             * Returns list of revisions
             * @param {type} $scope
             * @param {type} specificationId
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.listRevisions = function ($scope, specificationId, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("revisions", {"id": specificationId, "mode": "list"}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Returns two particular revisions
             * @param {type} $scope
             * @param {type} idA revisionA id
             * @param {type} idB revisionB id
             * @param {type} specificationId
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.getRevisions = function ($scope, idA, idB, specificationId, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("revisions", {"specification": specificationId, "id1": idA, "id2": idB, "mode": "compare"}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

        }

        return new RevisionsFct($http, $timeout);
    }]).factory("sanitizerHttp", ["$http", "$timeout", function ($http, $timeout) {

        function SanitizerFct($http, $timeout) {

            /**
             * Sanitizes given text
             * @param {type} $scope
             * @param {String} data text to be sanitized
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.getSanitizedInput = function ($scope, data, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("sanitizer", {}), JSON.stringify({"data": data}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

        }

        return new SanitizerFct($http, $timeout);
    }]).factory("statisticsHttp", ["$http", "$timeout", function ($http, $timeout) {

        function StatisticsFct($http, $timeout) {

            this.get = function ($scope, testRunId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("statistics", {"id": testRunId}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
            this.getPeriod = function ($scope, testRunId, period, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("statistics_filter", {"id": testRunId}), JSON.stringify(period)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
            this.getArchive = function ($scope, testRunId, fallBackUrl, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                var url = (fallBackUrl) ? $scope.SYNERGY.server.buildURL("statistics_fallback", {}) + "run" + testRunId + "/statistics.json" : $scope.SYNERGY.server.buildURL("statistics_archived", {}) + "run" + testRunId + "/statistics.json";
                url += "?d=" + Date.now();
                $http.get(url, {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }
        return new StatisticsFct($http, $timeout);
    }]).factory("reviewHttp", ["$http", "$timeout", function ($http, $timeout) {
        function ReviewFct($http, $timeout) {



            /**
             * Submits review assignment results
             * @param {type} $scope
             * @param {Number} assignmentId assignment ID
             * @param {Synergy.model.ReviewAssignment} results
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.submitResults = function ($scope, assignmentId, results, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.defaults.headers.common["Synergy-Timestamp"] = encodeURIComponent(new Date().toMysqlFormat());
                $http.put($scope.SYNERGY.server.buildURL("review_assignment", {"id": assignmentId, "datetime": new Date().toMysqlFormat()}), JSON.stringify(results)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Creates a volunteer review assignment
             * @param {type} $scope
             * @param {Synergy.model.ReviewAssignment} assignment
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.createVolunteer = function ($scope, assignment, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("review_assignment", {"volunteer": true}), JSON.stringify(assignment)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };


            /**
             * Removes test assignment
             * @param {type} $scope
             * @param {Number} assignmentId assignment ID
             * @param {Function} onSuccess
             * @param {Function} onFail
             * @returns {undefined}
             */
            this.remove = function ($scope, assignmentId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http({method: "DELETE", url: $scope.SYNERGY.server.buildURL("review_assignment", {"id": assignmentId})}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            this.get = function ($scope, id, mode, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.defaults.headers.common["Synergy-Timestamp"] = encodeURIComponent(new Date().toMysqlFormat());
                $http.get($scope.SYNERGY.server.buildURL("review_assignment", {"id": id, "mode": mode, "datetime": new Date().toMysqlFormat()}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            this.list = function ($scope, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("reviews", {}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
            this.listNotStarted = function ($scope, testRunId, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("reviews", {"id": testRunId}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            this.importFromUrl = function ($scope, url, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("reviews", {}), JSON.stringify({"url": url})).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            this.create = function ($scope, newReview, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("review", {}), JSON.stringify(newReview)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }

        return new ReviewFct($http, $timeout);
    }]).factory("projectsHttp", ["$http", "$timeout", function ($http, $timeout) {

        function ProjectsFct($http, $timeout) {

            /**
             * Retrieves all projects
             */
            this.getAll = function ($scope, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("projects", {}), {"cache": false, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }

        return new ProjectsFct($http, $timeout);
    }]).factory("projectHttp", ["$http", "$timeout", function ($http, $timeout) {

        function ProjectFct($http, $timeout) {

            /**
             * Updates project
             * @param {type} $scope
             * @param {Synergy.model.Project} project object with 2 properties: id and name
             * @param {Function} onSuccess
             * @param {Function} onFail
             */
            this.edit = function ($scope, project, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("project", {}), JSON.stringify(project)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Removes project
             * @param {type} $scope
             * @param {Number} projectId project ID
             * @param {Function} onSuccess
             * @param {Function} onFail
             */
            this.remove = function ($scope, projectId, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http({method: "DELETE", url: $scope.SYNERGY.server.buildURL("project", {"id": projectId})}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            /**
             * Creates project
             * @param {type} $scope
             * @param {Synergy.model.Project} project new project to be created with property "name"
             * @param {Function} onSuccess
             * @param {Function} onFail
             */
            this.create = function ($scope, project, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("project", {}), JSON.stringify(project)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            this.get = function ($scope, id, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.get($scope.SYNERGY.server.buildURL("project", {"id": id}), {"cache": false, "timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }

        return new ProjectFct($http, $timeout);
    }]).factory("registerHttp", ["$http", "$timeout", function ($http, $timeout) {

        function RegisterFct($http, $timeout) {

            this.doRegister = function ($scope, registration, onSuccess, onFail) {
                window.document.body.style.cursor = "wait";
                $scope.busyModeOn();
                $http.post($scope.SYNERGY.server.buildURL("register", {}), JSON.stringify(registration)).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };
        }

        return new RegisterFct($http, $timeout);
    }]).factory("sessionService", [function () {

        var _session = null;

        return {
            setSession: function (s) {
                _session = s;
            },
            clearSession: function () {
                _session = null;
            },
            getToken: function () {
                if (_session !== null && _session.hasOwnProperty("token")) {
                    return _session.token;
                }
                return null;
            }
        };
    }]).factory("SynergyApp", [function () { // need for http interceptor
        var synergyApp = null;
        return {
            setApp: function (s) {
                synergyApp = s;
            },
            getApp: function () {
                return synergyApp;
            }
        };
    }]).factory("SessionRenewalFactory", ["SynergyApp", "$http", function (SynergyApp, $http) {

        var originalResponse = null;
        var intervalId = -1;
        var TOKEN_LENGTH = 32;
        var token = null;
        var counter = 0;
        var sessionRenewalHttp = {};//$injector.get("sessionRenewalHttp");
        function getToken() {
            var text = "";
            var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            for (var i = 0; i < TOKEN_LENGTH; i++) {
                text += possible.charAt(Math.floor(Math.random() * possible.length));
            }
            return text + new Date().getTime();
        }

        function reset() {
            window.clearInterval(intervalId);
            counter = 0;
            token = null;
            intervalId = -1;
            originalResponse = null;
        }

        function checkForNewLogin() {
            counter++;
            sessionRenewalHttp.get(SynergyApp.getApp().server.buildURL("sso", {"token": token}), function () {
                reset();
            }, function () {
                if (counter > 100) {
                    reset();
                }
            });
        }

        function getRedirectUrl() {
            var url = window.location.href.substring(0, window.location.href.indexOf("#"));
            url = url.endsWith(".html") ? url.substring(0, url.lastIndexOf("/") + 1) : url;
            token = getToken();
            url += "login.html?token=" + token;
            if (SynergyApp.getApp().useSSO) {
                return SynergyApp.getApp().getLoginRedirectUrl(SynergyApp.getApp().ssoLoginUrl, url);
            } else {
                // todo fix, std login page is synergy/client/app/#/login, which is not visible for server though...
                throw new Error("Not implemented");
            }
        }

        return {
            openLoginDialog: function (response) {
                originalResponse = response;
                $("#myModalLabel").text("Please login");
                $("#modal-body").html("<a href='" + getRedirectUrl() + "' target='_blank'>Click to login</a>");
                if (!$("#myModal").hasClass("in")) {
                    $("#myModal").modal("toggle");
                }
                intervalId = window.setInterval(checkForNewLogin, 2000);
            }
        };
    }]).factory("sessionRenewalHttp", ["$http", "$timeout", function ($http, $timeout) {

        function SessionRenewalHttp($http, $timeout) {

            /**
             * Sanitizes given text
             * @param {type} $scope
             * @param {String} data text to be sanitized
             * @param {type} onSuccess
             * @param {type} onFail
             * @returns {undefined}
             */
            this.test = function ($scope, onSuccess, onFail) {
                $scope.busyModeOn();
                $http.put($scope.SYNERGY.server.buildURL("refresh", {}), {"timeout": $scope.SYNERGY.httpTimeout}).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $scope.SYNERGY.logger.logHTTPError(data, status, headers, config);
                    $timeout(function () {
                        onFail(data, status);
                    }, 100);
                });
            };

            this.get = function (url, token, onSuccess, onFail) {
                $http.get(url).success(function (result) {
                    $timeout(function () {
                        onSuccess(result);
                    }, 0);
                }).error(function (data, status, headers, config) {
                    $timeout(function () {
                        onFail(data, status);
                    }, 10);
                });
            };

        }

        return new SessionRenewalHttp($http, $timeout);
    }]).factory("specificationCache", [function () {

        var currentSpecification = null;
        var currentProject = null;

        function getSuiteEstimation(suite) {
            var time = 0;
            for (var i = 0, max = suite.testCases.length; i < max; i++) {
                time += suite.testCases[i].duration;
            }
            return time;
        }


        return {
            getCurrentSpecificationId: function () {
                return currentSpecification !== null ? parseInt(currentSpecification.id, 10) : -1;
            },
            setCurrentSpecification: function (spec, project) {
                currentSpecification = spec;
                currentProject = project;
            },
            getCurrentSpecification: function () {
                return currentSpecification;
            },
            resetCurrentSpecification: function () {
                currentSpecification = null;
                currentProject = null;
            },
            getCurrentProjectName: function () {
                return currentProject !== null ? currentProject.name : null;
            },
            getCurrentProject: function () {
                return currentProject;
            },
            getCurrentSuite: function (suiteId) {
                if (currentSpecification) {
                    for (var i = 0, max = currentSpecification.testSuites.length; i < max; i++) {
                        if (currentSpecification.testSuites[i].id === suiteId) {
                            var s = currentSpecification.testSuites[i];
                            s.version = currentSpecification.version;
                            s.specificationTitle = currentSpecification.title;
                            s.estimation = getSuiteEstimation(s);
                            return s;
                        }
                    }
                }
                return null;
            },
            getCurrentCase: function (caseId, suiteId) {
                if (currentSpecification) {
                    for (var i = 0, max = currentSpecification.testSuites.length; i < max; i++) {
                        if (currentSpecification.testSuites[i].id === suiteId) {
                            var s = currentSpecification.testSuites[i];

                            for (var j = 0, max2 = s.testCases.length; j < max2; j++) {
                                if (s.testCases[j].id === caseId) {
                                    var c = s.testCases[j];
                                    c.version = currentSpecification.version;
                                    c.specificationTitle = currentSpecification.title;
                                    c.suiteTitle = s.title;
                                    c.specificationId = currentSpecification.id;
                                    return c;
                                }
                            }

                        }
                    }
                }
                return null;
            }
        };
    }]);