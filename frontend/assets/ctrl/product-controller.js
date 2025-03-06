app.controller("product-ctrl", function ($scope, $product, $cart, $utility, HOST, $http) {
  const { $message, $templateUrl, $owlSlick } = $utility;

  $scope.templateUrl = $templateUrl.getProductTemplates();
  $owlSlick.configProduct($scope);

  const message = $message.product;

  var sweetalert_error = function (text) {
    Swal.fire({
      icon: "error",
      title: text,
      showConfirmButton: false,
      timer: 2000,
    });
  }

  $scope.info = {
    currentProduct: $product.current,
    quantity: 0,
    get cartQuantity() {
      return $cart.getItemQuantity(this.currentProduct.id);
    },
    get maxQuantity() {
      return this.currentProduct.quantity - this.cartQuantity;
    },
  };

  $scope.addToCart = () => {
    const { currentProduct, quantity } = $scope.info;
    $cart.addItem(currentProduct, quantity);
    $scope.info.quantity = 0;
  };

  $scope.$watch("info.quantity", (newVal, oldVal) => {
    const { maxQuantity } = $scope.info;
    if (newVal == 0 && maxQuantity != 0) $scope.info.quantity = 1;
    if (newVal != oldVal && newVal > maxQuantity) {
      $scope.info.quantity = oldVal;
      console.log(message.error.OVER_QUANTITY());
      sweetalert_error("Đã vượt quá sản phẩm tồn kho!")
    }
  });

  /////////////////////////////////////////////Review ///////////////////////////////////////
  var urlReview = `${HOST}/api/review`;

  $scope.currentProduct = $product.current;
  $scope.reviews = [];
  $scope.mark = 0;

  $scope.rv = [];
  $http.get(urlReview).then(resp => {
    $scope.reviews = resp.data.filter(item => item.orderDetail.product.id == $scope.currentProduct.id);
    var mark = 0;
    for(var i = 0; i < $scope.reviews.length; i++) {
      var review = $scope.reviews[i];
      $scope.checkEnableofReview(review);
      mark += review.mark;
    }
    $scope.mark = (mark/$scope.reviews.length).toFixed();
  });



  $scope.checkEnableofReview = function(review) {
    if(review.enable === true){
      $scope.rv.push(review);
    }else{
      return;
    }
    $scope.countReviews = $scope.rv.length;   
  }
});
