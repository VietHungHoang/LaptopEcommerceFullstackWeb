app.controller('shopsearch-ctrl', function ($scope, $filter, $http, HOST, $cart, $product, $utility) {
    const { $data, $url, $templateUrl } = $utility;

    $scope.templateUrl = $templateUrl.getHomeTemplates();
    // Paging
    $scope.products = [];
    $scope.categories = [];
    $scope.companies = [];

    $http.get(HOST + "/api/product/search/" + $scope.searchN).then((resp) => {
        $scope.products = resp.data;
    }).catch((err) => {
        console.log(err);
    });

    $http.get(HOST + "/api/category").then((resp) => {
        $scope.categories = resp.data;
    });

    $http.get(HOST + "/api/company").then((resp) => {
        $scope.companies = resp.data;
    });

    $scope.addCart = (product) => {
        $cart.addItem(product);
    };

    $scope.viewProduct = (event, product) => {
        event.preventDefault();
        $product.current = product;
        $url.redirectToProductPage();
    };

    $scope.propertyName = '';
    $scope.reverse = true;

    $scope.sortBy = function (propertyName) {
        $scope.reverse = ($scope.propertyName === propertyName) ? !$scope.reverse : false;
        $scope.propertyName = propertyName;
    };

    $scope.filter = (name) => {
        $scope.q = name;
    }

    $scope.currentPage = 0;
    $scope.pageSize = 12;
    $scope.q = '';

    $scope.getData = function () {
        return $filter('filter')($scope.products, $scope.q)
    }

    $scope.numberOfPages = function () {
        return Math.ceil($scope.products.length / $scope.pageSize);
    }

    $scope.showLabel = (val) => {
        var date1 = new Date();
        var date2 = new Date(val);
        var diffMonths = date1.getMonth() - date2.getMonth();
        var diffDays = date1.getDate() - date2.getDate();
        var diffYears = date1.getYear() - date2.getYear();
        
        console.log(diffDays, diffMonths, diffYears)
        if (diffDays < 0) {
            diffMonths -= 1;
            var daysInMonth = new Date(date2.getYear(), date2.getMonth() - 1, 0).getDate();
            diffDays = daysInMonth + diffDays;
        }
        
        if (diffMonths <= 0 && diffDays <= 7) {
            return true;
        } else {
            return false;
        }
    }

    $scope.searchName = () => {
        $http.get(HOST + "/api/product/search" + $scope.searchN).then((resp) => {
            $scope.products = resp.data;
        });
    }
});