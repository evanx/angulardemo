
var app = angular.module("app", ["ngTouch", "ngRoute", "ngSanitize", "ui.bootstrap"]);

app.factory("appService", ["$http", function($http) {
        return {
            load: function(url, successHandler) {
                console.log("load", url);
                $http.get(url).success(successHandler);
            }
        }
    }]);

app.controller("appController", ["$scope", "$location", "appService",
    function($scope, $location, appService) {
        console.log("appController");
        $scope.isCollapsed = true;
        $scope.state = {};
        $scope.userEmail = null;
        $scope.isActive = function(route) {
            return route === $location.path();
        };
    }]);

app.config(["$locationProvider", '$routeProvider', function($locationProvider, $routeProvider) {
        $locationProvider.html5Mode(false);
        $routeProvider.
                when("/sections", {
                    templateUrl: "sections.html", 
                    controller: "sectionsController"}).
                when("/section/:section", {
                    templateUrl: "section.html", 
                    controller: "sectionController"}).
                when(":date/article/:articleId", {
                    templateUrl: "article.html", 
                    controller: "articleController"}).
                when("/article/:articleId", {
                    templateUrl: "article.html", 
                    controller: "articleController"}).
                otherwise({redirectTo: "/section/News"});
    }]);

app.controller("sectionsController", ["$scope", "$window", "appService",
    function($scope, $window, appService) {
        $scope.sections = ["News", "Sport", "Business", "SciTech", "Motoring", "Tonight"];
    }]);

app.controller("sectionController", ["$scope", "$location", "$routeParams", "$window", "appService",
    function($scope, $location, $routeParams, $window, appService) {
        console.log("sectionController", $routeParams);
        $scope.sectionResult = function(data) {
            $scope.articles = data;            
        };
        var jsonPath = $routeParams.section.toLowerCase() + ".json";
        appService.load(jsonPath, $scope.sectionResult);
        $scope.selected = function(article) {
            console.log("selected", article.articleId);
            $location.path("/article/" + article.articleId);
        };
    }]);

app.controller("articleController", ["$scope", "$location", "$routeParams", "$window", "appService",
    function($scope, $location, $routeParams, $window, appService) {
        $scope.articleResult = function(data) {
            console.log("articleResult", data);
            $scope.article = data;
        };
        var jsonPath = "articles/" + $routeParams.articleId + ".json";
        console.log("articleController", $routeParams.articleId, jsonPath);
        appService.load(jsonPath, $scope.articleResult);
    }]);
