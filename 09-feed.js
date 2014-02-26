
var app = angular.module("app", []);

app.factory("appService", ["$http", function($http) {
        return {
            login: function(email, password, successHandler) {
                $http.post("login.json", {
                    email: email,
                    password: password
                }).success(successHandler);
            },
            logout: function(email, successHandler) {
                $http.post("logout.json", {
                    email: email
                }).success(successHandler);
            },
            loadFeed: function(feed, successHandler) {
                $http.post(feed).success(successHandler);
            }
        }
    }]);

app.controller("feedController", ["$scope", "$window", "appService",
    function($scope, $window, appService) {
        appService.loadFeed("sport.json", function(data) {
            $scope.articles = data;
        });
        $scope.selected = function() {
            console.log("article", this.article);
            $window.location.href = this.article.link;
        }
    }]);

app.controller("appController", ["$scope", "appService",
    function($scope, appService) {
        $scope.title = "Sport";
        $scope.userEmail = null;
        $scope.login = function() {
            appService.login($scope.email, $scope.password, $scope.loggedIn);
            $scope.email = null;
            $scope.password = null;
        };
        $scope.loggedIn = function(data) {
            $scope.userEmail = data.email;
            $scope.userDisplayName = data.name;
        };
        $scope.logout = function() {
            appService.logout($scope.email, $scope.loggedOut);
        };
        $scope.loggedOut = function() {
            $scope.userEmail = null;
            $scope.userDisplayName = null;
        };
    }]);

