app.controller("checkout-ctrl", function ($scope, HOST, $cart, $http, $window, authService) {
    var urluser = `${HOST}/api/user`;
    var urlprod = `${HOST}/api/product`;
    var urlorder = `${HOST}/api/order`;
    var urlorderdetail = `${HOST}/api/orderdetail`;

    $scope.users = [];
    $scope.orderdetails = [];
    $scope.orders = [];

    $scope.orderdetail = {};
    $scope.order = {};
    $scope.userid = {};

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
            timer: 3000,
        });
    }

    $scope.isAuthed = function () {
        return authService.isAuthed ? authService.isAuthed() : false;
    };

    $http.get(HOST + "/api/user").then((resp) => {
        $scope.users = resp.data;
    });

    $http.get(HOST + "/api/orderdetail").then((resp) => {
        $scope.orderdetails = resp.data;
    });

    $http.get(HOST + "/api/order").then((resp) => {
        $scope.orders = resp.data;
    });

    $scope.checkout = async () => {
        if ($scope.check == false || $scope.check == undefined) {
            sweetalert_error("Vui lòng đọc các điều khoản dịch vụ và tích vào ô để thực hiện thanh toán")
        } else {
            // * POST Order
            var datetime = new Date();
            $scope.order.createdat = datetime.getTime();
            $scope.order.updatedat = datetime.getTime();
            // * GET user
            $scope.order.user = $scope.userid;

            var item = angular.copy($scope.order);
            console.log(item);
            await $http.post(`${urlorder}`, item).then((resp) => {
                $scope.orders.push(resp.data);
                $scope.order.id = resp.data.id;
            });


            // * Recover Order
            var item2 = angular.copy($scope.order);
            // * POST orderdetail
            for (const cart of $cart.values) {
                $scope.orderdetail.price = cart.price;
                $scope.orderdetail.quantity = cart.quantity;
                $scope.orderdetail.status = '1';
                $scope.orderdetail.order = item2;
                $scope.orderdetail.product = cart;
                $scope.orderdetail.reviewed = false;

                var itemoddt = angular.copy($scope.orderdetail);
                await $http.post(`${urlorderdetail}`, itemoddt).then(response => {
                    $scope.orderdetails.push(response.data);
                })
                //subtract quantity prod
                var product = {}
                await $http.get(urlprod + '/' + cart.id).then(resp => {
                    product = resp.data;
                })
                product.quantity = (product.quantity - cart.quantity);
                var subtracted = product;
                await $http.put(urlprod + '/' + cart.id, subtracted).then(resp => {
                    console.log(resp.data);
                })
            }
            sweetalert_success("Thanh toán thành công")
            localStorage.removeItem("cart");
            setTimeout(() => {
                $window.location.href = '#!my-account';
                window.location.reload();
            }, 2500)
        }
    }

    // Get info user
    $scope.findUserDetail = () => {
        var getNameUser = localStorage.getItem("currentUser");
        $http.get(urluser).then(resp => {
            $scope.userid = resp.data.filter(item => item.username === getNameUser)[0];
        })
    }
});