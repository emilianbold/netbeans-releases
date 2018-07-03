"use strict";

var synergyModule = angular.module("synergyLogin", [])
        .config(function($provide, $routeProvider) {
            $routeProvider.when("/", {templateUrl: "partials/public/login/home.html?v=1405687647278", controller: "HomeCtrl"});
            $routeProvider.otherwise({redirectTo: "/"});
        }).controller("HomeCtrl", ["$scope", "$http", "$timeout", function($scope, $http, $timeout) {

        $scope.isOK = true;
        $scope.err = "";

        if (window.location.href.indexOf("?token=") > 0) {
            var t = window.location.href.substring(window.location.href.indexOf("?token=") + 7);
            if (t.indexOf("#/") > -1) {
                t = t.substring(0, t.indexOf("#/"));
            }
            $http.post($scope.SYNERGY.server.buildURL("refresh"), JSON.stringify({"token": t})).success(function() {
                $scope.msg = "Finished, this window will close in 2 seconds. Please wait...";
                $timeout(function() {
                    window.close();
                }, 2000);
            }).error(function(data, status, headers, config) {
                $scope.isOK = false;
                $scope.err = data;
            });
        }
    }]).controller("SynergyCtrl", ["$scope", function($scope) {
        $scope.SYNERGY = new Synergy();
    }]);