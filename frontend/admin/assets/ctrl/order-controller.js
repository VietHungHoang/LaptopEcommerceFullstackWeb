app.controller("order-ctrl", function ($scope, $timeout, $http, $route, orderService, $location, HOST) {
    var url = `${HOST}/api/order`;
    var urlOrderApproval = `${HOST}/api/orderdetail/approval`;
    var urlOrderDetail = `${HOST}/api/orderdetail`;
    var urlDetailOfOrder = `${HOST}/api/orderdetail/pro`;

    $scope.orderdata = orderService.get();
    $scope.items = [];
    $scope.itemsApproval = [];
    $scope.details = [];
    $scope.selectedList = {};
    $scope.itemSelected = {};
    $scope.status = '1';
    $scope.isRowCollapsed = true;

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

    // load data order approval - Xử lí đơn hàng
    $scope.loadOrderApproval = function () {
        $http.get(`${urlOrderApproval}/${$scope.status}`).then(resp => {
            $scope.itemsApproval = resp.data;
            // paginate
            $scope.curPage = 1;
            $scope.itemsPerPage = 10;
            $scope.maxSize = 5;
            this.items = $scope.itemsApproval;
            $scope.numOfPages = function () {
                return Math.ceil($scope.itemsApproval.length / $scope.itemsPerPage);
            };
            $scope.$watch('curPage + numPerPage', function () {
                var begin = (($scope.curPage - 1) * $scope.itemsPerPage);
                var end = begin + $scope.itemsPerPage;
                $scope.pagingItemsApproval = $scope.itemsApproval.slice(begin, end);
            });
        });
    }

    // load order by status - Xử lí đơn hàng
    $scope.loadOrderbyStatus = function () {
        $scope.loadOrderApproval();
    }

    // load data order - Tất cả đơn hàng
    $http.get(url).then(resp => {
        $scope.items = resp.data;
        // paginate
        $scope.curPage = 1;
        $scope.itemsPerPage = 10;
        $scope.maxSize = 5;
        this.items = $scope.items;
        $scope.numOfPages = function () {
            return Math.ceil($scope.items.length / $scope.itemsPerPage);
        };
        $scope.$watch('curPage + numPerPage', function () {
            var begin = (($scope.curPage - 1) * $scope.itemsPerPage);
            var end = begin + $scope.itemsPerPage;
            $scope.pagingItems = $scope.items.slice(begin, end);
        });
    });

    // Duyệt đơn - Xử lí đơn hàng
    $scope.submit = function (flag) {
        angular.forEach($scope.selectedList, function (selected, itemid) {
            if (selected) {
                $http.get(`${urlOrderDetail}/${itemid}`).then(resp => {
                    $scope.itemSelected = resp.data;
                    var item = $scope.itemSelected;
                    item.status = flag;
                    item.order.updatedat = moment().valueOf();
                    $http.put(`${urlOrderDetail}/${itemid}`, item).then(resp => {
                        if (flag == 2) {
                            sweetalert_success("Duyệt đơn thành công!");
                        } else {
                            sweetalert_success("Hủy đơn thành công!");
                        }
                        $http.put(`${url}/${item.order.id}`, item.order).then(resp => {
                        })
                        $timeout($route.reload, 2000);
                    })
                })
            }
        });
    };

    // load detail data - chi tiết đơn hàng
    $scope.getDetail = function (id) {
        if (id == null) {
            sweetalert_error("Chọn đơn hàng để xem chi tiết!");
            $location.path('order-list');
        } else {
            $http.get(`${urlDetailOfOrder}/${id}`).then(resp => {
                $scope.details = resp.data;
            })
        }
    };

    // hien thi len chi tiết - chi tiết đơn hàng
    $scope.detail = function (item) {
        orderService.set(item);

    }

    // Export excel
    $(document).ready(function () {
        $("#saveAsExcel").click(function () {
            var workbook = XLSX.utils.book_new();
            var worksheet_data = document.getElementById("table");
            var worksheet = XLSX.utils.table_to_sheet(worksheet_data);
            workbook.SheetNames.push("order");
            workbook.Sheets["order"] = worksheet;
            exportExcelFile(workbook);
        });
    })
    function exportExcelFile(workbook) {
        return XLSX.writeFile(workbook, "order_List.xlsx");
    }
});
