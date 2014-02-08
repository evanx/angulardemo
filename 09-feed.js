
var app = angular.module("app", ["ngTouch"]);

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
            feed: function(feed, successHandler) {
                $http.post(feed).success(successHandler);
            }
        }
    }]);

app.controller("appController", ["$scope", "appService",
    function($scope, appService) {
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

app.controller("feedController", ["$scope", "$window", "appService",
    function($scope, $window, appService) {
        appService.feed("news.json", function(data) {
            $scope.articles = data;
        });
        $scope.selected = function() {
            console.log("selected", this.headline);
            $window.location.href = this.headline.link;
        }
    }]);
