
var app = angular.module("app", []);

app.factory("appService", ["$http", function($http) {
        return {
            login: function(email, password, successHandler) {
                console.warn("TODO appService login", email);
                successHandler({
                    email: email, 
                    name: "Judge Jules" // TODO
                });
            }
        }
    }]);

app.controller("appController", ["$scope", "appService",
    function($scope, appService) {
        console.log("appController init");
        $scope.userEmail = null;
        $scope.login = function() {
            console.log("login", $scope.email);
            appService.login($scope.email, $scope.password, $scope.loggedIn);
        };
        $scope.loggedIn = function(data) {
            $scope.userEmail = data.email;
            $scope.userDisplayName = data.name;
        };
    }]);

