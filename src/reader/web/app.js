
var app = angular.module("app", ["ngTouch", "ngRoute", "ngSanitize", "ui.bootstrap"]);

app.factory("appService", ["$http", function($http) {
        return {
            load: function(url, successHandler, errorHandler) {
                console.log("load", url);
                $http.get(url).success(successHandler).error(errorHandler);
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
                when("/subsection/:subsection", {
                    templateUrl: "section.html", 
                    controller: "subSectionsController"}).
                when(":date/article/:articleId", {
                    templateUrl: "article.html", 
                    controller: "articleController"}).
                when("/article/:articleId", {
                    templateUrl: "article.html", 
                    controller: "articleController"}).
                otherwise({redirectTo: "/section/Top"});
    }]);

app.controller("sectionsController", ["$scope", "$location", "$window", "appService",
    function($scope, $location, $window, appService) {
        $scope.sections = ["Top", "News", "Sport", "Business", "SciTech", "Motoring", "Tonight"];
        $scope.selected = function(section) {
            console.log("selected", section);
            $location.path("section/" + section);
        };        
    }]);

app.controller("subSectionController", ["$scope", "$location", "$routeParams", "$window", "appService",
    function($scope, $location, $routeParams, $window, appService) {
        console.log("sectionController", $routeParams);
        $scope.resultHandler = function(data) {
            $scope.statusMessage = undefined;
            $scope.articles = data;            
        };
        $scope.errorHandler = function() {
            $scope.statusMessage = undefined;
        };
        var jsonPath = $routeParams.section.toLowerCase() + "/articles.json";
        appService.load(jsonPath, $scope.resultHandler, $scope.errorHandler);
        $scope.selected = function(article) {
            console.log("selected", article.articleId);
            $location.path("article/" + article.articleId);
        };
    }]);

app.controller("sectionController", ["$scope", "$location", "$routeParams", "$window", "appService",
    function($scope, $location, $routeParams, $window, appService) {
        console.log("sectionController", $routeParams);
        $scope.resultHandler = function(data) {
            $scope.statusMessage = undefined;
            $scope.articles = data;            
        };
        $scope.errorHandler = function() {
            $scope.statusMessage = undefined;
        };
        var jsonPath = $routeParams.section.toLowerCase() + "/articles.json";
        $scope.statusMessage = "Loading " + jsonPath;
        appService.load(jsonPath, $scope.resultHandler, $scope.errorHandler);
        $scope.selected = function(article) {
            console.log("selected", article.articleId);
            $location.path("article/" + article.articleId);
        };
    }]);

app.controller("articleController", ["$scope", "$location", "$routeParams", "$window", "appService",
    function($scope, $location, $routeParams, $window, appService) {
        $scope.imageWidth = 300;
        $scope.resultHandler = function(data) {
            console.log("articleResult", data);
            $scope.statusMessage = undefined;
            $scope.article = data;
        };
        $scope.errorHandler = function() {
            $scope.statusMessage = undefined;
        };
        var jsonPath = "articles/" + $routeParams.articleId + ".json";
        console.log("articleController", $routeParams.articleId, jsonPath);
        $scope.statusMessage = "Loading " + jsonPath;
        appService.load(jsonPath, $scope.resultHandler, $scope.errorHandler);
    }]);
