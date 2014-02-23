
var app = angular.module("app", ["ngTouch", "ngRoute"]);

app.factory("appService", ["$http", function($http) {
        return {
            loadFeed: function(feed, successHandler) {
                $http.post(feed).success(successHandler);
            }
        }
    }]);

app.controller("appController", ["$scope", "appService",
    function($scope, appService) {
        $scope.state = {title: "Demo"};
        $scope.userEmail = null;
    }]);

app.config(['$routeProvider', function($routeProvider) {
        $routeProvider.
                when("/sections", {templateUrl: "15-sections.html", controller: "sectionsController"}).
                when("/section/:section", {templateUrl: "16-section.html", controller: "sectionController"}).
                otherwise({redirectTo: "/sections"});
    }]);

app.controller("sectionsController", ["$scope", "$window", "appService",
    function($scope, $window, appService) {
        $scope.state.title = "Demo";
        $scope.sections = ["News", "Sport", "Business"];
    }]);

app.controller("sectionController", ["$scope", "$routeParams", "$window", "appService",
    function($scope, $routeParams, $window, appService) {
        console.log("sectionController", $routeParams);
        $scope.state.title = $routeParams.section;
        $scope.feedResult = function(data) {
            $scope.articles = data;
        };
        if ($routeParams.section === "News") {
            appService.loadFeed("news.json", $scope.feedResult);
        } else if ($routeParams.section === "Sport") {
            appService.loadFeed("sport.json", $scope.feedResult);
        } else if ($routeParams.section === "Business") {
            appService.loadFeed("business.json", $scope.feedResult);
        }
        $scope.selected = function() {
            $window.location.href = this.article.link;
        };
    }]);

