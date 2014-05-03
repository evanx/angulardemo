
var articles = {};

var sectionList = [
    {
        name: "Top",
        label: "Frontpage"
    }, 
    {
        name: "News",
        label: "News"
    }, 
    {
        name: "Sport", 
        label: "Sport"
    }, 
    {
        name: "Business", 
        label: "Business"
    }, 
    { 
        name: "SciTech", 
        label: "Science & Technology"
    }, 
    { 
        name: "Motoring", 
        label: "Motoring"
    }, 
    { 
        name: "Lifestyle", 
        label: "Lifestyle"
    }, 
    { 
        name: "Tonight", 
        label: "Tonight"
    }, 
    { 
        name: "Travel", 
        label: "Travel"
    }, 
    { 
        name: "Backpage", 
        label: "Backpage"
    }, 
    { 
        name: "Multimedia", 
        label: "Galleries"
    }, 
    { 
        name: "Videos",
        label: "Videos"
    }
];

var app = angular.module("app", ["ngTouch", "ngRoute", "ngSanitize", "ui.bootstrap"]);

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

app.config(['$sceDelegateProvider', function($sceDelegateProvider) {
        $sceDelegateProvider.resourceUrlWhitelist([
            'self',
            'http://www.youtube.com/**',
        ]);
    }]);

app.controller("appController", ["$scope", "$location", "appService",
    function($scope, $location, appService) {
        console.log("appController");
        $scope.isCollapsed = true;
        $scope.state = {};
        $scope.userEmail = null;
        $scope.state.title = "My Independent";
        $scope.isActive = function(route) {
            return route === $location.path();
        };
        $scope.personalize = function() {
            //console.log("personalize", $location.path());
            if ($location.path().indexOf("/section/") === 0) {
                return $location.path().substring(1);
            }
            return null;
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
        $scope.state.title = "My Independent";
        $scope.sections = sectionList;
        $scope.selected = function(section) {
            console.log("selected", section);
            $location.path("section/" + section);
        };
    }]);

app.controller("sectionController", ["$scope", "$location", "$routeParams", "$window", "appService",
    function($scope, $location, $routeParams, $window, appService) {
        $scope.section = $routeParams.section.toLowerCase();
        console.log("sectionController", $scope.section);
        $scope.state.title = $routeParams.section;
        var jsonPath = $scope.section + "/articles.json";
        $scope.resultHandler = function(data) {
            $scope.statusMessage = undefined;
            $scope.articles = data;
            if (data && data.length > 0) {
                sections[$scope.section] = { articles: data };
                putArticles($scope.articles);
            }
        };
        $scope.errorHandler = function() {
            $scope.statusMessage = undefined;
        };
        console.log("sections", sections, sections[$scope.section]);
        if (sections[$scope.section] && sections[$scope.section].articles && 
                sections[$scope.section].articles.length) {
            $scope.articles = sections[$scope.section].articles;
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

app.controller("articleController", ["$scope", "$location", "$window", "$routeParams", "$window", "$sce", "$timeout", "appService",
    function($scope, $location, $window, $routeParams, $window, $sce, $timeout, appService) {
        var jsonPath = "article/" + $routeParams.articleId + ".json";
        //$sce.trustAsResourceUrl("http://www.youtube.com/embed/5VWDIlSMTMc");
        $scope.develInfo = "" + $window.innerWidth + "x" + $window.innerHeight;
        console.log("article", $window);
        $scope.scheduleAddThis = function() {
            $timeout(function() {
                console.log("addthis", addthis);
                addthis.toolbox(".addthis_toolbox");
            }, 200);
        };
        $scope.resultHandler = function(data) {
            console.log("articleResult", data);
            $scope.statusMessage = undefined;
            $scope.article = data;
            $scope.state.title = $scope.article.title;
            putArticle($scope.article);
            $scope.scheduleAddThis();
        };
        $scope.errorHandler = function() {
            $scope.statusMessage = undefined;
        };
        $scope.galleryHeight = function(article) {
            if ($window.innerWidth < article.maxWidth) {
                return 4 + article.maxHeight*$window.innerWidth/article.maxWidth;
            }
            return article.maxHeight;
        };
        console.log("articleController", $routeParams.articleId, jsonPath);
        if (articles[$routeParams.articleId]) {
            $scope.article = articles[$routeParams.articleId];
            $scope.state.title = $scope.article.title;
            $scope.scheduleAddThis();
        } else {
            $scope.statusMessage = "Loading " + jsonPath;
            appService.load(jsonPath, $scope.resultHandler, $scope.errorHandler);
        }
    }]);
