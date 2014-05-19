
var jsonpCallbacks = {};

function jsonpCallback(path, data) {
   console.log("jsonpCallback", path, data);
   jsonpCallbacks[path](data);   
};

function jsonLoad(path) {
   console.log("jsonLoad", path);
   var json = document.getElementsById(path);
   var data = JSON.parse(json[json.length - 1].textContent); 
   console.log("jsonLoad", path, data);
   jsonpCallbacks[path](data);   
};

var app = angular.module("app", ["ngSanitize"]);

app.factory("appService", ["$q", "$http", "$location", function($q, $http) {
      console.log("location", location);
      var geo = {city: "jhb", country: 'za', server: "chronica.co"};
      var service = {
         getJson: function(jsonPath, successHandler) {
            var url = "http://" + geo.server + "/" + jsonPath + "?time=" + new Date().getTime();
            console.log("getJson", url);
            jsonpCallbacks[jsonPath] = successHandler;
            var scriptElement = document.createElement("script");
            //scriptElement.id = "script-top";
            scriptElement.type = "application/json";
            scriptElement.src = url;
            scriptElement.onreadystatechange = scriptElement.onload = function() {
               console.log("onload");
            };
            document.head.appendChild(scriptElement);
         },
         getJsonp: function(jsonPath, successHandler) {
            var url = "http://" + geo.server + "/" + jsonPath + "p?time=" + new Date().getTime();
            console.log("getJsonp", url);
            jsonpCallbacks[jsonPath] = successHandler;
            var scriptElement = document.createElement("script");
            scriptElement.type = "text/javascript";
            scriptElement.src = url;
            document.head.appendChild(scriptElement);
         },
         load: function(jsonPath, successHandler) {
            service.getJson(jsonPath, successHandler);
         },
         loadSection: function(section, sectionHandler) {
            var jsonPath = section + "/articles.json";
            service.load(jsonPath, function(data) {
               sectionHandler(section, data);
            });
         },
         init: function() {
            service.loadSection("top", function(section, data) {
               console.log("loadSection success", section, data);
            });
         }
      };
      return service;
   }]);

app.controller("testController", ["$scope", "$location", "appService",
   function($scope, $location, appService) {
      console.log("appController");
      appService.init();
   }]);



