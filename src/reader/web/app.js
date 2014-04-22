
var app = angular.module("app", ["ngTouch", "ngRoute", "ngSanitize", "ui.bootstrap"]);

var articles = {};

function putArticles(articles) {
    for (var i = 0; i < articles.length; i++) {
        putArticle(articles[i]);
    }
}

function putArticle(article) {
    articles[article.articleId] = article;
}

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

app.controller("sectionController", ["$scope", "$location", "$routeParams", "$window", "appService",
    function($scope, $location, $routeParams, $window, appService) {
        console.log("sectionController", $routeParams);
        var section = $routeParams.section.toLowerCase();
        var jsonPath = section + "/articles.json";
        $scope.resultHandler = function(data) {
            $scope.statusMessage = undefined;
            $scope.articles = data;
            sections[section] = { articles: data };
            putArticles($scope.articles);
        };
        $scope.errorHandler = function() {
            $scope.statusMessage = undefined;
        };
        console.log("sections", sections, sections[section]);
        if (sections[section] && sections[section].articles) {
            $scope.articles = sections[section].articles;
            putArticles($scope.articles);
        } else {
            $scope.statusMessage = "Loading " + jsonPath;
            appService.load(jsonPath, $scope.resultHandler, $scope.errorHandler);
        }
        $scope.selected = function(article) {
            console.log("selected", article.articleId);
            $location.path("article/" + article.articleId);
        };
    }]);

app.controller("articleController", ["$scope", "$location", "$routeParams", "$window", "appService",
    function($scope, $location, $routeParams, $window, appService) {
        var jsonPath = "articles/" + $routeParams.articleId + ".json";
        $scope.resultHandler = function(data) {
            console.log("articleResult", data);
            $scope.statusMessage = undefined;
            $scope.article = data;
            putArticle($scope.article);
        };
        $scope.errorHandler = function() {
            $scope.statusMessage = undefined;
        };
        console.log("articleController", $routeParams.articleId, jsonPath);
        if (articles[$routeParams.articleId]) {
            $scope.article = articles[$routeParams.articleId];
        } else {
            $scope.statusMessage = "Loading " + jsonPath;
            appService.load(jsonPath, $scope.resultHandler, $scope.errorHandler);
        }
    }]);
