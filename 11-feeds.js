
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
            loadFeed: function(feed, successHandler) {
                console.log("loadFeed", feed);
                $http.post(feed).success(successHandler);
            }
        }
    }]);

app.controller("appController", ["$scope", "appService",
    function($scope, appService) {
        $scope.userEmail = null;
        $scope.view = "News";
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
        $scope.setView = function(view) {
            if ($scope.view !== view) {
                $scope.view = view;
                $scope.$broadcast("setView", view);
            }
        };
    }]);

app.controller("feedController", ["$scope", "$window", "appService",
    function($scope, $window, appService) {
        $scope.$on("setView", function(event, view) {
            console.log("feedController setView", view);
            if (view === "News") {
                $scope.loadFeed("news.json");
            } else if (view === "Sport") {
                $scope.loadFeed("sport.json");
            } else if (view === "Business") {
                $scope.loadFeed("business.json");
            }
        });
        $scope.loadFeed = function(feed) {
            appService.loadFeed(feed,  $scope.feedResult);
        };
        $scope.feedResult = function(data) {
            console.log("feedResult", data);
            $scope.articles = data;
        };
        $scope.selected = function() {
            console.log("article", this.article);
            $window.location.href = this.article.link;
        };
        $scope.loadFeed("news.json");
    }]);
