app.controller('order-ctrl', function ($scope, $location, $http, orderService, HOST, $cart) {
    var urlOrder = `${HOST}/api/order`;
    var urlOrderDetail = `${HOST}/api/orderdetail`;
    var urlDetailOfOrder = `${HOST}/api/orderdetail/pro`
    var urlOrderApproval = `${HOST}/api/orderdetail/approval`

    $scope.orderdata = orderService.get();
    $scope.items = [];
    $scope.item = {};
    $scope.orderFinish = [];
    $scope.orderCancel = [];
    $scope.itemDetail = [];
    $scope.orders = [];
    $scope.orderseller = [];
    $scope.isRowCollapsed = true;
    $scope.currentUser = localStorage.getItem('currentUser');
    $scope.itemsd = {};

    
    var sweetalert_success = function (text) {
        Swal.fire({
            icon: "success",
            title: text,
            showConfirmButton: false,
            timer: 2000,
        });
    }
    
    var sweetalert_error = function (text) {
        Swal.fire({
            icon: "error",
            title: text,
            showConfirmButton: false,
            timer: 2000,
        });
    }

    //load order detail by status 
    $scope.loadOrderDetailByStatus = function (status) {
        $http.get(`${urlOrderApproval}/${status}`).then(resp => {
            if (status == 3) {
                $scope.orderCancel = resp.data.filter(item => item.order.user.username == $scope.currentUser);
            } else if (status == 4) {
                $scope.orderFinish = resp.data.filter(item => item.order.user.username == $scope.currentUser);
            }

        });

    }

    //load order data 
    $scope.loadOrderData = function () {
        $http.get(urlOrder).then(resp => {
            $scope.items = resp.data.filter(item => item.user.username == $scope.currentUser);
            for (var i = 0; i < $scope.items.length; i++) {
                this.getDetailItem($scope.items[i].id);
            }
        });

    }

    //load order seller data 
    $scope.loadOrderSellerData = function () {
        $http.get(urlOrderDetail).then(resp => {
            $scope.orderseller = resp.data.filter(item => item.product.user.username == $scope.currentUser);
        });

    }

    //load detail for order
    $scope.getDetailItem = function (id) {
        var tempItem = {};
        $http.get(`${urlOrder}/${id}`).then(resp => {
            $scope.itemsd = resp.data;
            tempItem = null;
            tempItem = $scope.itemsd;
            $http.get(`${urlDetailOfOrder}/${tempItem.id}`).then(respose => {
                $scope.itemDetail = respose.data;
                var tempDetail = $scope.itemDetail;
                tempItem.detail = tempDetail;
                //get Total Price
                var TempPrice = 0;
                for (var i = 0; i < $scope.itemDetail.length; i++) {
                    TempPrice += $scope.itemDetail[i].price;
                    $scope.totalPrice = TempPrice;
                }
                tempItem.totalPrice = $scope.totalPrice;
                $scope.totalPrice = 0;
                tempItem.status = this.getOrderStatus(tempDetail);
            });
            $scope.itemsd = tempItem;
            $scope.orders.push($scope.itemsd);
        });
    }

    //get Order Status
    $scope.getOrderStatus = function (detail) {
        for (var k = 0; k < detail.length; k++) {
            var odetail = detail[k];
            if (odetail != undefined && odetail != null) {
                var status;
                var detailst = odetail.status;
                if (detailst == 1 || detailst == 2) {
                    return status = 1;
                } else {
                    status = 2;
                }
            } else if (odetail == undefined || odetail == null) {
                return status = 0;
            }
        }
        return status;
    }

    //reorder
    $scope.addCart = (product) => {
        $cart.addItem(product, 1);
    };


    //hien thi len chi tiết - chi tiết đơn hàng
    $scope.detail = function (item) {
        orderService.set(item);
    }

    //load detail data - chi tiết đơn hàng
    $scope.getDetail = function (id) {
        if (id == null) {
            sweetalert_error("Chọn đơn hàng để xem chi tiết");
            $location.path('my-account');
        } else {
            $http.get(`${urlDetailOfOrder}/${id}`).then(resp => {
                $scope.details = resp.data;

                //get Total Price
                var TempPrice = 0;
                for (var i = 0; i < $scope.details.length; i++) {
                    TempPrice += $scope.details[i].price;
                    $scope.totalPrice = TempPrice;
                }
            })
        }
    };

    $scope.filterSellerOrder = function (item) {
        return item.status == 1 || item.status == 2;
    }

      $scope.confirmOrderFinish = function(detail) {
        Swal.fire({
          text: 'Bạn chắc chắn đã nhận được hàng?',
          icon: 'warning',
          showCancelButton: true,
          confirmButtonText: 'Có, tôi đã nhận!',
          cancelButtonText: 'Không'
        }).then((result) => {
          if (result.value) {
            detail.status = 4;
            $scope.upadateDetail(detail);
            Swal.fire(
              'Đã xác nhận!',
              'Cám ơn bạn đã sử dụng dịch vụ của NghienDT!!!.',
              'success'
            );
            setTimeout(() => {
                location.reload();
              }, 5000); // Reload sau 3 giây
          } else if (result.dismiss === Swal.DismissReason.cancel) {
            Swal.close();
          }
        });
      }

    ////////////////////////////////////////////// review order ////////////////////////////////////////
    var urlReview = `${HOST}/api/review`;
    var reviewPU = angular.element("#myModal");
    var temppro = {};

    $scope.itemReview = {
        content: "",
        mark: 0,
        image: "anh1.png",
        createdAt: moment().format('YYYY-MM-DD HH:mm'),
        enable: true,
        orderDetail: {},
    }

    $scope.preLoadModal = function (detail) {
        temppro = detail.product;
        document.getElementById("detail-id").value = detail.id;
        document.getElementById("review-image").src = temppro.image;
        document.getElementById("review-proname").innerText = temppro.name;
        document.getElementById("review-proprice").innerText = temppro.price.toLocaleString('vn-VN');
        document.getElementById("review-proquantity").innerText = detail.quantity;
        document.getElementById("review-prototal").innerText = detail.price.toLocaleString('vn-VN');
        reviewPU.modal('show');
    }

    $scope.finish = function () {
        var did = document.getElementById("detail-id").value;
        $http.get(`${urlOrderDetail}/${did}`).then(respose => {
            $scope.itemReview.orderDetail = respose.data;
            $scope.itemReview.orderDetail.reviewed = true;
            var item = angular.copy($scope.itemReview);
            $http.post(`${urlReview}`, item).then(resp => {
                $scope.upadateDetail($scope.itemReview.orderDetail);
                reviewPU.modal('hide');
                location.reload();
                sweetalert_success("Cám ơn bạn đã góp ý!");
            }).catch(error => {
                sweetalert_error("Có lỗi xảy ra!");
                console.log("Error", error);
            });
        }).catch(error => {
            sweetalert_error("Có lỗi xảy ra!");
            console.log("Error", error);
        });
    };

    $scope.upadateDetail = function(item) {
        $http.post(`${urlOrderDetail}`, item).then(resp => {
            $scope.items.push(resp.data);
        }).catch(error => {
            console.log("Error", error);
        });
    };
})
    .directive('date', function (dateFilter) {
        return {
            require: 'ngModel',
            link: function (scope, elm, attrs, ctrl) {

                var dateFormat = attrs['date'] || 'yyyy-MM-dd';

                ctrl.$formatters.unshift(function (modelValue) {
                    return dateFilter(modelValue, dateFormat);
                });
            }
        };
    })
    .directive('dir', function () {
        return {
            link: function (scope, elem, attrs) {
                $(elem).on('click', function () {
                    $(this)
                        .parent('tr')
                        .next('tr')
                        .toggle();
                })
            }
        }
    })