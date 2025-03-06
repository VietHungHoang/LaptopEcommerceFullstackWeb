app.controller("dashboard-ctrl", function ($scope, $http, $rootScope, HOST) {
    var url = `${HOST}/api/product/getTopProduct`;
    var url1 = `${HOST}/api/product/getTopCustomer`;
    var url2 = `${HOST}/api/product/getDailyRevenue`;
    
    $scope.items = [];
    $scope.items1 = [];
    $scope.items2 = [];

    // load data
    $http.get(url).then(resp => {
        $scope.items = resp.data;
    });

    // load data
    $http.get(url1).then(resp => {
        feather.replace();
        $scope.items1 = resp.data;
    });

    // load data
    $http.get(url2).then(resp => {
        $scope.items2 = resp.data;
    });
});