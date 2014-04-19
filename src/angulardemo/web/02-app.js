
var app = angular.module("app", []);

app.controller("appController", ["$scope",
    function($scope) {
        console.log("appController init");
        $scope.userEmail = "evan.summers@gmail.com";
    }]);

