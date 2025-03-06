app.controller("cart-ctrl", function ($scope, $cart) {
	$scope.removeItem = (item) => {
		$cart.removeItem(item.id);
	};
	
});
