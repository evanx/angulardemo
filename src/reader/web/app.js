
var jsonpCallbacks = {};

function jsonpCallback(path, data) {
   var callback = jsonpCallbacks[path];
   console.log("jsonpCallback", path, typeof(data), typeof (callback));
   if (callback) {
      delete jsonpCallbacks[path];
   }
   if (!callback) {
      console.warn("jsonpCallback missing", path);      
   } else if (typeof (callback) !== 'function') {
      console.warn("jsonpCallback type", path, typeof (callback));
   } else {
      callback(data);
   }
}

function json_callback(path, data) {
   jsonpCallback(path, data);
}

var appData = {
   servers: {
      other: ["cf.chronica.co", "chronica.co"],
      de: ["de.chronica.co", "chronica.co"],
      us: ["us.chronica.co", "chronica.co"],
      za: ["za.chronica.co", "chronica.co"],
      local: ["localhost:8000", "localhost:8000"],
      "de.chronica.co": {type: "jsonp"},
      "za.chronica.co": {type: "jsonp"}
   },
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

app.config(["$locationProvider", '$routeProvider', function($locationProvider, $routeProvider) {
      $locationProvider.html5Mode(false);
      $routeProvider.
              when("/sections", {
                 templateUrl: "sections.html",
                 controller: "sectionsController"}).
              when("/country/:country", {
                 controller: "countryController"}).
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

app.config(['$httpProvider', function($httpProvider) {
      if (!$httpProvider.defaults.headers.get) {
         $httpProvider.defaults.headers.get = {};
      }
      $httpProvider.defaults.headers.get['If-Modified-Since'] = '0';
   }]);

app.factory("appService", function($q, $http, $location, $timeout) {
   var geo = {
      enabled: true,
      server: 'origin',
      serverType: 'origin',
      city: "jhb",
      country: 'za',
      servers: appData.servers,
      path: $location.path(),
      host: $location.host()
   };
   geo.host = $location.host();
   if (geo.host === "chronica.co") {
      geo.enabled = false;
   }
   if (geo.host === "localhost") {
      geo.enabled = true;
   }
   console.log("geo init", geo);
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
         console.log("putSectionArticles", section, typeof(articles), articles.length);
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
      getOrigin: function(jsonPath, successHandler, errorHandler) {
         $http.get(jsonPath).success(successHandler).error(errorHandler);
      },
      getCors: function(jsonPath, successHandler, errorHandler) {
         var url = "http://" + geo.server + "/" + jsonPath;
         console.log("getCors", url);
         $http.get(url).success(successHandler).error(errorHandler);
      },
      getJsonp: function(jsonPath, successHandler, errorHandler) {
         var url = "http://" + geo.server + "/" + jsonPath + "p?time=" + new Date().getTime();
         console.log("getJsonp", url);
         jsonpCallbacks[jsonPath] = successHandler;
         var scriptElement = document.createElement("script");
         scriptElement.type = "text/javascript";
         scriptElement.src = url;
         document.head.appendChild(scriptElement);
         $timeout(function() {
            if (jsonpCallbacks[jsonPath]) {
               errorHandler();
            }
         }, 2000);
      },
      loadType: function(jsonPath, successHandler, errorHandler) {
         console.log("loadType", geo.server, geo.serverType, jsonPath);
         if (!geo.enabled) {
            service.getOrigin(jsonPath, successHandler, errorHandler);
         } else if (geo.server === 'origin') {
            service.getOrigin(jsonPath, successHandler, errorHandler);
         } else if (geo.serverType === 'jsonp') {
            service.getJsonp(jsonPath, successHandler, errorHandler);
         } else if (geo.serverType === 'cors') {
            service.getCors(jsonPath, successHandler, errorHandler);
         } else {
            service.getOrigin(jsonPath, successHandler, errorHandler);
         }
      },
      load: function(jsonPath, successHandler, errorHandler) {
         service.loadType(jsonPath, successHandler, function() {
            console.warn("load: geo", jsonPath, geo.enabled, geo.server, geo.serverType);
            if (geo.enabled) {
               if (geo.server === 'origin') {
                  service.setGeo();
               } else {
                  service.changeServer();
               }
            }
            service.loadType(jsonPath, successHandler, function() {
               console.warn("load: geo", jsonPath, geo.enabled, geo.server, geo.serverType);
               if (geo.enabled) {
                  service.changeServer();
               }
               service.loadType(jsonPath, successHandler, errorHandler);
            });
         });
      },
      loadSection: function(section) {
         var deferred = $q.defer();
         var jsonPath = section + "/articles.json";
         service.load(jsonPath, function(data) {
            console.log("loadSection", section, typeof(data), data.length);
            deferred.resolve({section: section, data: data});
         }, function() {
            console.warn("loadSection", section);
            deferred.reject(section);
         });
         return deferred.promise;
      },
      initData: function() {
         var firstSection = sectionList[0].name.toLowerCase();
         service.loadSection(firstSection).then(function(result) {
            service.putSectionArticles(firstSection, result.data);
            for (var i = 1; i < sectionList.length; i++) {
               var section = sectionList[i].name.toLowerCase();
               service.loadSection(section).then(function(result) {
                  console.log("initData", result);
                  service.putSectionArticles(result.section, result.data);
               });
            }
         });
      },
      setCountry: function(country) {
         geo.country = country;
      },
      setGeo: function() {
         geo.serverIndex = 0;
         if (!geo.servers[geo.country]) {
            geo.country = "other";
         }
         service.setServer();
      },
      setServer: function() {
         geo.server = geo.servers[geo.country][geo.serverIndex];
         console.log("setServer", geo.country, geo.server, geo.servers[geo.server]);
         if (geo.servers[geo.server] && geo.servers[geo.server].type === "jsonp") {
            geo.serverType = "jsonp";
         } else {
            geo.serverType = "cors";
         }
         console.log("server", geo.server, geo.serverType);
      },
      changeServer: function() {
         if (geo.serverIndex === 0) {
            geo.serverIndex = 1;
         } else {
            geo.serverIndex = 0;
         }
         service.setServer();
      },
      initGeo: function() {
         $http.get("http://ipinfo.io/json").success(function(data) {
            geo.ipinfo = data;
            if (data.city === "Cape Town") {
               geo.city = "cpt";
            }
            if (data.country) {
               geo.country = data.country.toLowerCase();
            }
            service.setGeo();
            console.log("initGeo", geo);
         }).error(function() {
            console.warn("initGeo");            
         });
      },
      init: function() {
         if (geo.enabled && geo.path.indexOf("/country/") !== 0) {
            service.initGeo();
         }
      }
   };
   return service;
});

app.config(['$sceDelegateProvider', function($sceDelegateProvider) {
      $sceDelegateProvider.resourceUrlWhitelist([
         'self',
         'http://www.youtube.com/**',
      ]);
   }]);

app.controller("appController", function($scope, $location, $timeout, appService) {
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
   appService.init();
   if (true) {
      $timeout(function() {
         appService.initData();
      }, 1000);
   }
});

app.controller("countryController", function(
        $scope, $location, $routeParams, $location, appService) {
   console.log("countryController", $routeParams.country);
   appService.setCountry($routeParams.country);
   $location.path("section/Top");
});

app.controller("sectionsController", function(
        $scope, $location, $window, appService) {
   $scope.state.title = "My Independent";
   $scope.sections = appService.getSectionList();
   $scope.selected = function(section) {
      console.log("selected", section);
      $location.path("section/" + section);
   };
});

var sectionController = app.controller("sectionController", function(
        $scope, $location, $routeParams, $window, $timeout, appService) {
   $scope.sectionLabel = appService.getSectionLabel($routeParams.section);
   $scope.section = $routeParams.section.toLowerCase();
   $scope.state.section = $scope.section;
   $scope.state.mobile = ($window.innerWidth < 560);
   console.log("sectionController", $scope.section);
   $scope.state.title = appService.getSectionLabel($routeParams.section);
   var jsonPath = $scope.section + "/articles.json";
   $scope.resultHandler = function(result) {
      $scope.statusMessage = "Loaded";
      console.log('section apply result', result);
      $scope.articles = appService.putSectionArticles(result.section, result.data);
   };
   $scope.errorHandler = function() {
      $scope.statusMessage = "Failed";
   };
   if (appService.isSectionArticles($scope.section)) {
      $scope.articles = appService.getSectionArticles($scope.section);
   } else {
      $scope.statusMessage = "Loading " + jsonPath;
      appService.loadSection($scope.section).then($scope.resultHandler, $scope.errorHandler);
   }
   $scope.selected = function(article) {
      console.log("selected", article.articleId);
      $location.path("article/" + article.articleId);
   };
});

sectionController.resolve = [
   '$routeParams', 'appService',
   function($routeParams, appService) {
      console.log("resolve", $routeParams.section);
   }];

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
