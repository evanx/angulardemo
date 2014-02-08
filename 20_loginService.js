
app.factory("loginService", ["$http", function($http) {
        return {
            login: function(email, password, success) {
                $http.post("login.json", {
                    email: email,
                    password: password
                }).success(success);
            },
            logout: function(email, success) {
                $http.post("logout.json", {
                    email: email
                }).success(success);
            }
        };
    }]);
