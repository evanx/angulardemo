
var app = angular.module("app", ["ngSanitize"]);

app.config(['$sceDelegateProvider', function($sceDelegateProvider) {
        $sceDelegateProvider.resourceUrlWhitelist([
            'self',
            'http://www.youtube.com/**',
        ]);
    }]);

app.controller("testController", ["$scope", "$location", "$sce",
    function($scope, $location, $sce) {
        console.log("appController");
        //$sce.trustAsResourceUrl("http://www.youtube.com/embed/eH_WMLxmfA8");
        $scope.article = {
            youtubeList: [
                {
                    "width": "550",
                    "height": "440",
                    "url": "http://www.youtube.com/embed/eH_WMLxmfA8",
                    "title": "VIDEO: DA seeks gains in Free State"
                }
            ]
        };
    }]);

