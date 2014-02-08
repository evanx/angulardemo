
var app = angular.module("app", ['']);

app.factory("appService", ["$http", function($http) {
        return {
            login: function(email, password) {
            }
        }
    }]);

app.controller("appController", ["$scope", "appService",
    function($scope, appService) {
        $scope.email = null;
        $scope.login = function() {
            appService.login();
        };
    }]);

