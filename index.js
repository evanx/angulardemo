
var app = angular.module("app", []);

app.factory("appService", ["$http", function($http) {
        return {
        }
    }]);

app.controller("appController", ["$scope", "appService",
    function($scope, appService) {
    }]);

app.controller("linksController", ["$scope", "$window", "appService",
    function($scope, $window, appService) {
        $scope.links = [
            "01-bootstrap.html",
            "02-app.js",
            "03-angular.html",
            "04-submit.html",
            "05-login.js",
            "06-login.js",
            "07-login.html",
            "10-feed.html",
            "11-feed.js",
            "12-feeds.js",
            "13-routes.html",
            "14-routes.js",
            "15-sections.html",
            "16-section.html"
        ];
        console.log("links", $scope.links);
        $scope.open = function() {
            $window.location.href = this.link;
        }
        $scope.openGithub = function() {
            $window.location.href = 'https://raw.github.com/evanx/angulardemo/master/' + link;
        }
    }]);
