
var app = angular.module("app", ['']);

app.factory("appService", ["$http", function($http) {
        return {
            login: function(email, password) {
                return {
                    name: email
                }
            }
        }
    }]);

app.controller("appController", ["$scope", "appService",
    function($scope, appService) {
        $scope.email = "evan.summers@gmail.com";
        $scope.login = function() {
            appService.login($scope.email, $scope.password);
        };
    }]);

