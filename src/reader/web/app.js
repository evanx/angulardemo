
var app = angular.module("app", ["ngTouch", "ngRoute"]);

app.factory("appService", ["$http", function($http) {
        return {
            load: function(url, successHandler) {
                console.log("load", url);
                $http.get(url).success(successHandler);
            }
        }
    }]);

app.controller("appController", ["$scope", "$locationProvider", "$location", "appService",
    function($scope, $locationProvider, $location, appService) {
        console.log("appController");
        $locationProvider.html5Mode(true);
        $scope.state = {};
        $scope.userEmail = null;
        $scope.isActive = function(route) {
            return route === $location.path();
        };
    }]);

app.config(['$routeProvider', function($routeProvider) {
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
        $scope.sections = ["News", "Sport", "Business"];
    }]);

app.controller("sectionController", ["$scope", "$location", "$routeParams", "$window", "appService",
    function($scope, $location, $routeParams, $window, appService) {
        console.log("sectionController", $routeParams);
        $scope.sectionResult = function(data) {
            $scope.articles = data;            
        };
        if ($routeParams.section === "News") {
            appService.load("news.json", $scope.sectionResult);
        } else if ($routeParams.section === "Sport") {
            appService.load("sport.json", $scope.sectionResult);
        } else if ($routeParams.section === "Business") {
            appService.load("business.json", $scope.sectionResult);
        }
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

