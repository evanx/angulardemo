
var app = angular.module("app", []);

app.factory("appService", ["$http", function($http) {
        return {
            login: function(email, password, successHandler) {
                console.warn("TODO appService login", email);
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
        $scope.userEmail = "evan.summers@gmail.com";
    }]);

