
var appData = {
   sectionList: [
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
   ]};

var app = angular.module("app", ["ngTouch", "ngRoute", "ngSanitize", "ui.bootstrap"]);

app.filter('sliceFrom', function() {
   return function(array, start) {
      console.log("sliceFrom", array, start);
      return array.slice(start, array.length - start);
   };
});

app.factory("appService", ["$q", "$http", function($q, $http) {
      var defer = $q.defer();
      var sectionList = appData.sectionList;
      var articleMap = {};
      var sectionArticleList = {};
      var service = {
         getSectionList: function() {
            return sectionList;
         },
         putArticle: function(article) {
            articleMap[article.articleId] = article;
         },
         putArticles: function(articles) {
            for (var i = 0; i < articles.length; i++) {
               service.putArticle(articles[i]);
            }
         },
         isArticle: function(articleId) {
            return articleMap[articleId] !== undefined;
         },
         getArticle: function(articleId) {
            return articleMap[articleId];
         },
         putSectionArticles: function(section, articles) {
            console.log("putSectionArticles", section, articles);
            if (!sectionArticleList[section]) {
               sectionArticleList[section] = [];
            }
            if (!articles || articles.length === 0) {
               console.warn("empty articles", section);
            } else if (section === 'multimedia') {
               sectionArticleList[section] = [];
               for (var i = 0; i < articles.length; i++) {
                  if (articles[i].imageList && articles[i].imageList.length > 0) {
                     sectionArticleList[section].push(articles[i]);
                     service.putArticle(articles[i]);
                  }
               }
            } else if (section === 'videos') {
               sectionArticleList[section] = [];
               for (var i = 0; i < articles.length; i++) {
                  if (articles[i].youtubeList && articles[i].youtubeList.length > 0) {
                     sectionArticleList[section].push(articles[i]);
                     service.putArticle(articles[i]);
                  }
               }
            } else {
               sectionArticleList[section] = articles;
               service.putArticles(articles);
            }
            return sectionArticleList[section];
         },
         isSectionArticles: function(section) {
            return sectionArticleList[section] && sectionArticleList[section].length;
         },
         getSectionArticles: function(section) {
            if (!sectionArticleList[section]) {
               sectionArticleList[section] = [];
            }
            return sectionArticleList[section];
         },
         getSectionLabel: function(name) {
            for (var i = 0; i < sectionList.length; i++) {
               if (sectionList[i].name === name) {
                  return sectionList[i].label;
               }
               return name;
            }
         },
         load: function(url, successHandler, errorHandler) {
            console.log("load", url);
            $http.get(url).success(successHandler).error(errorHandler);
         },
         loadSection: function(section) {
            var jsonPath = section + "/articles.json";
            $http.get(jsonPath).success(function(data) {
               console.log("loadSection", jsonPath);
               defer.resolve(data);
            });
            return defer.promise;
         },
         init: function() {
            for (var i = 0; i < sectionList.length; i++) {
               var section = sectionList[i].name.toLowerCase();
               service.loadSection(section).then(function(data) {
                  service.putSectionArticles(section, data);
               });
            }
         }
      };
      return service;
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
      if (true) {
         setTimeout(function() {
            appService.init();
         }, 2000);
      }
   }]);

app.controller("sectionsController", ["$scope", "$location", "$window", "appService",
   function($scope, $location, $window, appService) {
      $scope.state.title = "My Independent";
      $scope.sections = appService.getSectionList();
      $scope.selected = function(section) {
         console.log("selected", section);
         $location.path("section/" + section);
      };
   }]);

var sectionController = app.controller("sectionController", [
   "$scope", "$location", "$routeParams", "$window", "appService",
   function($scope, $location, $routeParams, $window, appService) {
      $scope.sectionLabel = appService.getSectionLabel($routeParams.section);
      $scope.section = $routeParams.section.toLowerCase();
      $scope.state.section = $scope.section;
      $scope.state.mobile = ($window.innerWidth < 560);
      console.log("sectionController", $scope.section);
      $scope.state.title = appService.getSectionLabel($routeParams.section);
      var jsonPath = $scope.section + "/articles.json";
      $scope.resultHandler = function(data) {
         $scope.statusMessage = "Loaded";
         $scope.articles = appService.putSectionArticles($scope.section, data);
      };
      $scope.errorHandler = function() {
         $scope.statusMessage = "Failed";
      };
      if (appService.isSectionArticles($scope.section)) {
         $scope.articles = appService.getSectionArticles($scope.section);
      } else {
         $scope.statusMessage = "Loading " + jsonPath;
         appService.load(jsonPath, $scope.resultHandler, $scope.errorHandler);
      }
      $scope.selected = function(article) {
         console.log("selected", article.articleId);
         $location.path("article/" + article.articleId);
      };
   }]);

sectionController.resolve = [
   '$routeParams', 'appService',
   function($routeParams, appService) {
      console.log("resolve", $routeParams.section);
   }];

app.config(["$locationProvider", '$routeProvider', function($locationProvider, $routeProvider) {
      $locationProvider.html5Mode(false);
      $routeProvider.
              when("/sections", {
                 templateUrl: "sections.html",
                 controller: "sectionsController"}).
              when("/section/:section", {
                 templateUrl: "section.html",
                 controller: "sectionController",
                 resolve: sectionController.resolve}).
              when(":date/article/:articleId", {
                 templateUrl: "article.html",
                 controller: "articleController"}).
              when("/article/:articleId", {
                 templateUrl: "article.html",
                 controller: "articleController"}).
              otherwise({redirectTo: "/section/Top"});
   }]);

app.controller("articleController", ["$scope", "$location", "$window", "$routeParams", "$window", "$sce", "$timeout", "appService",
   function($scope, $location, $window, $routeParams, $window, $sce, $timeout, appService) {
      var jsonPath = "article/" + $routeParams.articleId + ".json";
      //$sce.trustAsResourceUrl("http://www.youtube.com/embed/5VWDIlSMTMc");
      $scope.state.mobile = ($window.innerWidth < 560);
      $scope.develInfo = "" + $window.innerWidth + "x" + $window.innerHeight;
      console.log("article", $window);
      $scope.addThisVisible = false;
      $scope.galleryStyle = "";
      $scope.setStyle = function() {
         $scope.galleryStyle = {};
         if ($scope.article.maxHeight) {
            var height = 2 + $scope.article.maxHeight;
            if ($window.innerWidth < $scope.article.maxWidth) {
               height = 2 + ($scope.article.maxHeight * $window.innerWidth / $scope.article.maxWidth);
               $scope.galleryStyle["width"] = "100%";
            } else {
               $scope.galleryStyle["width"] = "" + $scope.article.maxWidth + "px";
               $scope.galleryStyle["margin-left"] = "1em";
            }
            $scope.galleryStyle["min-height"] = "" + height + "px";
         }
         console.log("galleryStyle", $scope.galleryStyle);
      };
      $scope.scheduleAddThis = function() {
         if (typeof addthis !== 'undefined') {
            $timeout(function() {
               console.log("addthis", addthis);
               addthis.toolbox(".addthis_toolbox");
               $scope.addThisVisible = true;
            }, 200);
         }
      };
      $scope.processArticle = function(article) {
         if (article.imageList) {
            for (var i = 0; i < article.imageList.length; i++) {
            }
         }
         return article;
      };
      $scope.resultHandler = function(data) {
         console.log("articleResult", data);
         $scope.statusMessage = "Loaded";
         $scope.article = data;
         $scope.setStyle();
         $scope.state.title = $scope.article.title;
         appService.putArticle($scope.article);
         $scope.scheduleAddThis();
      };
      $scope.errorHandler = function() {
         $scope.statusMessage = "Failed";
      };
      console.log("articleController", $routeParams.articleId, jsonPath);
      if (appService.isArticle($routeParams.articleId)) {
         $scope.article = appService.getArticle($routeParams.articleId);
         $scope.state.title = $scope.article.title;
         $scope.scheduleAddThis();
      } else {
         $scope.statusMessage = "Loading " + jsonPath;
         appService.load(jsonPath, $scope.resultHandler, $scope.errorHandler);
      }
   }]);
