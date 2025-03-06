app.controller("index-ctrl", ($scope, $http, $cart, $utility, authService) => {
	const { $owlSlick } = $utility;
	var cate = "http://157.245.157.128/v1/api/category";
	var brand = "http://157.245.157.128/v1/api/category";
	$scope.cates = [];
	$scope.brands = [];
	$scope.currentUser = localStorage.getItem("currentUser");
	
    $scope.logout = function () {
        authService.logout && authService.logout();
    };

    $scope.isAuthed = function () {
        return authService.isAuthed ? authService.isAuthed() : false;
    };

	$http.get(cate).then(resp => {
		$scope.cates = resp.data;
	});

	$http.get(brand).then(resp => {
		$scope.brands = resp.data;
	});

	$owlSlick.configIndex($scope);

	$scope.removeCartItem = (item) => {
		$cart.removeItem(item.id);
	};
});
