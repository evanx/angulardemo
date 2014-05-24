
var jsonpCallbacks = {};

function jsonpCallback(path, data) {
   var callbackInfo = jsonpCallbacks[path];
   if (!callbackInfo) {
      console.error("jsonp info missing", path);
   } else {
      var callbackFunction = jsonpCallbacks[path].callback;
      if (!callbackFunction) {
         console.error("jsonp function missing", path);
      } else if (typeof (callbackFunction) !== 'function') {
         console.warn("jsonp not function", path, typeof (callbackFunction));
      } else {
         console.log("jsonp", path, typeof (data), data.length);
         var currentTime = new Date().getTime();
         if (callbackInfo.duration) {
            console.error("jsonp has duration", path, callbackInfo.duration);
         }
         callbackInfo.duration = currentTime - callbackInfo.timestamp;
         if (callbackInfo.timedOut) {
            console.log("jsonp timed out", path, callbackInfo.duration);
         } else {
            if (currentTime >= callbackInfo.timestamp + callbackInfo.timeout + 1000) {
               console.error("jsonp expired", callbackInfo);
            } else {
               console.info("jsonp ok", path, callbackInfo.duration);
            }
         }
         callbackFunction(data);
         if (callbackInfo.alsoCallback) {
            callbackInfo.alsoCallback(data);
         }
      }
   }
}

function json_callback(path, data) {
   jsonpCallback(path, data);
}

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
   ],
   definedHosts: {
      "cf.chronica.co": {hostType: "cors", geoDisabled: true},
      "chronica.co": {hostType: "cors", geoDisabled: true},
      "de.chronica.co": {hostType: "jsonp", geoDisabled: true},
      "do.chronica.co": {hostType: "cors"},
      "lh": {hostType: "jsonp"},
      "de.lh": {hostType: "cors"},
      "za.lh": {hostType: "cors"},
      "localhost": {hostType: "cors"},
      "za.chronica.co": {hostType: "jsonp"}
   },
   preferredHosts: {
      local: ["localhost:8888", "lh:8000"],
      other: ["do.chronica.co", "cf.chronica.co"],
      de: ["de.chronica.co", "cf.chronica.co"],
      us: ["do.chronica.co", "cf.chronica.co"],
      za: ["za.chronica.co", "do.chronica.co"]
   }
};

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
      hostIndex: 0,
      hostName: 'origin',
      hostType: 'origin',
      city: "jhb",
      country: 'za',
      preferredHosts: appData.preferredHosts,
      definedHosts: appData.definedHosts,
      location: {
         path: $location.path(),
         host: $location.host()
      },
   };
   var sectionList = appData.sectionList;
   var articleMap = {};
   var sectionArticleList = {};
   var service = {
      initHost: function() {
         geo.initialHost = geo.definedHosts[geo.location.host];
         if (!geo.initialHost) {
            console.warn("initHost not defined", geo.location.host);
         } else if (geo.initialHost.geoDisabled) {
            geo.enabled = false;
         }
         console.log("initHost", geo.location.host, geo);
         if (geo.enabled) {
            console.warn("geo enabled");
         }
      },
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
         console.log("putSectionArticles", section, typeof (articles), articles.length);
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
         $http.get("storage/" + jsonPath).success(successHandler).error(errorHandler);
      },
      getCors: function(jsonPath, successHandler, errorHandler) {
         var url = "http://" + geo.hostName + "/storage/" + jsonPath;
         console.log("getCors", url);
         $http.get(url).success(successHandler).error(errorHandler);
      },
      getJsonp: function(jsonPath, successHandler, errorHandler, timeout) {
         var hostName = geo.hostName.toLowerCase();
         var url = "http://" + geo.hostName + "/" + jsonPath + "p?time=" + new Date().getTime();
         console.log("getJsonp", url);
         if (jsonpCallbacks[jsonPath]) {
            if (!jsonpCallbacks[jsonPath].duration) {
               if (!jsonpCallbacks[jsonPath].timedOut) {
                  var duration = new Date().getTime() - jsonpCallbacks[jsonPath].timestamp;
                  console.warn("getJsonp active", jsonPath, hostName, duration);
                  errorHandler();
                  return;
               }
            }
         }
         jsonpCallbacks[jsonPath] = {
            timedOut: false,
            callback: successHandler,
            timestamp: new Date().getTime(),
            timeout: timeout
         };
         var scriptElement = document.createElement("script");
         scriptElement.type = "text/javascript";
         scriptElement.src = url;
         document.head.appendChild(scriptElement);
         $timeout(function() {
            if (!jsonpCallbacks[jsonPath].duration) {
               jsonpCallbacks[jsonPath].timedOut = true;
               console.warn("getJsonp timeout", jsonPath, hostName);
               errorHandler();
            }
         }, timeout);
      },
      loadType: function(jsonPath, successHandler, errorHandler, timeout) {
         console.log("loadType", jsonPath, geo.hostType, geo.hostName);
         if (!geo.enabled) {
            service.getOrigin(jsonPath, successHandler, errorHandler);
         } else if (geo.hostType === 'origin') {
            service.getOrigin(jsonPath, successHandler, errorHandler);
         } else if (geo.hostType === 'jsonp') {
            service.getJsonp(jsonPath, successHandler, errorHandler, timeout);
         } else if (geo.hostType === 'cors') {
            service.getCors(jsonPath, successHandler, errorHandler);
         } else {
            service.getOrigin(jsonPath, successHandler, errorHandler);
         }
      },
      load: function(jsonPath, successHandler, errorHandler, initialTimeout, timeout) {
         service.loadType(jsonPath, successHandler, function() {
            console.warn("load", jsonPath, geo.hostType, geo.hostName);
            if (geo.enabled) {
               service.changeServer();
            }
            service.loadType(jsonPath, successHandler, function() {
               console.warn("load", jsonPath, geo.hostType, geo.hostName);
               if (geo.enabled) {
                  service.changeServer();
               }
               service.loadType(jsonPath, successHandler, errorHandler, timeout);
            }, timeout);
         }, initialTimeout);
      },
      loadSection: function(section, initialTimeout, timeout) {
         var deferred = $q.defer();
         var jsonPath = section + "/articles.json";
         service.load(jsonPath, function(data) {
            console.log("loadSection", section, typeof (data), data.length);
            deferred.resolve({section: section, data: data});
         }, function() {
            console.warn("loadSection", section);
            deferred.reject(section);
         }, initialTimeout, timeout);
         return deferred.promise;
      },
      initData: function() {
         var timeout = 4000;
         console.log("initData", geo);
         var firstSection = sectionList[0].name.toLowerCase();
         service.loadSection(firstSection, timeout, timeout).then(function(result) {
            service.putSectionArticles(firstSection, result.data);
            for (var i = 1; i < sectionList.length; i++) {
               var section = sectionList[i].name.toLowerCase();
               service.loadSection(section, timeout + i * 500, timeout).then(function(result) {
                  console.log("initData result", result);
                  service.putSectionArticles(result.section, result.data);
               });
            }
         });
      },
      changeCountry: function(country) {
         if (!geo.preferredHosts[country]) {
            console.warn("changeCountry", country);
            geo.country = "other";
         } else {
            console.log("changeCountry", country);
            geo.country = country;
         }
         geo.hostIndex = 0;
         service.setServer();
      },
      setServer: function() {
         console.log("setServer", geo.country, geo.hostIndex);
         if (!geo.preferredHosts[geo.country]) {
            console.warn("setServer country", geo.country);
            geo.country = "other";
         }
         geo.hostName = geo.preferredHosts[geo.country][geo.hostIndex];
         if (!geo.hostName) {
            console.error("setServer: no hostName");
            geo.hostType = "origin";
         } else if (!geo.definedHosts[geo.hostName]) {
            console.error("setServer: no host", geo.hostName);
            geo.hostType = "jsonp";
         } else {
            if (!geo.definedHosts[geo.hostName].hostType) {
               console.error("setServer: no hostType", geo.definedHosts[geo.hostName]);
               geo.hostType = "jsonp";
            } else if (geo.definedHosts[geo.hostName].hostType === "jsonp") {
               geo.hostType = "jsonp";
            } else {
               geo.hostType = "cors";
            }
         }
         console.log("setServer done", geo.country, geo.hostName, geo.hostType, geo.definedHosts[geo.hostName]);
      },
      changeServer: function() {
         console.log("changeServer", geo.country, geo.hostName, geo.definedHosts[geo.hostName]);
         if (geo.hostIndex === 0) {
            geo.hostIndex = 1;
         } else {
            geo.hostIndex = 0;
         }
         service.setServer();
      },
      queryGeo: function() {
         $http.get("http://ipinfo.io/json").success(function(data) {
            geo.ipinfo = data;
            if (data.city === "Cape Town") {
               geo.city = "cpt";
            }
            if (data.country) {
               geo.country = data.country.toLowerCase();
            }
            if (!geo.preferredHosts[geo.country]) {
               console.log("initGeo country", geo.country);
               geo.country = "other";
            }
            console.log("initGeo", geo);
            service.setServer();
         }).error(function() {
            console.warn("initGeo");
         });
      },
      init: function() {
         service.initHost();
         if (geo.enabled) {
            if (geo.overrideCountry) {
               console.warn("overrideCountry", geo.overrideCountry);
               service.changeCountry(geo.overrideCountry);
            } else {
               service.queryGeo();
            }
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
      appService.loadSection($scope.section, 4000, 4000).then($scope.resultHandler, $scope.errorHandler);
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
         appService.load(jsonPath, $scope.resultHandler, $scope.errorHandler, 4000, 4000);
      }
   }]);
