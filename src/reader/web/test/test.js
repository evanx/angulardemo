
var app = angular.module("app", ["ngSanitize"]);

app.config(['$sceDelegateProvider', function($sceDelegateProvider) {
      $sceDelegateProvider.resourceUrlWhitelist([
         'self',
         'http://www.youtube.com/**',
      ]);
   }]);

app.factory("appService", ["$q", "$http", "$location", function($q, $http) {
      console.log("location", location);
      var geo = {city: "jhb", country: 'za', server: "localhost:8888"};
      var service = {
         getUrl: function(jsonPath) {
            if (true) {
               return "http://" + geo.server + "/" + jsonPath + "?callback=JSON_CALLBACK";
            } else {
               return jsonPath;
            }
         },
         loadSection: function(section) {
            var defer = $q.defer();
            var jsonPath = section + "/articles.json";
            var url = service.getUrl(jsonPath);
            $http.jsonp(url).success(function(data) {
               console.log("loadSection", url, data);
               defer.resolve({section: section, data: data});
            }).error(function(data, status) {
               console.error("loadSection error", url, data, status);
            });
            return defer.promise;
         },
         init: function() {
            service.loadSection("test").then(function(data) {
               console.log("loadSection", data);               
            });
         }
      };
      return service;
   }]);

app.controller("testController", ["$scope", "$location", "$sce", "appService",
   function($scope, $location, $sce, appService) {
      console.log("appController");
      appService.init();
   }]);



