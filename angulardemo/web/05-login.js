
var app = angular.module("app", []);

app.factory("appService", ["$http", function($http) {
        return {
            login: function(email, password, successHandler, errorHandler) { // TODO
                successHandler({
                    email: email, 
                    name: email
                });
            }
        }
    }]);

app.controller("appController", ["$scope", "appService",
    function($scope, appService) {
        console.log("appController init");
        $scope.userEmail = null;
        $scope.login = function() {
            appService.login($scope.email, $scope.password, $scope.loggedIn, $scope.loginError);
        };
        $scope.loggedIn = function(data) {
            $scope.userEmail = data.email;
            $scope.userDisplayName = data.name;
        };
        $scope.loginError = function(data) {
            console.log("loginError", data);
        };
    }]);

