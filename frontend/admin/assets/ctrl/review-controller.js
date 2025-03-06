app.controller("review-ctrl", function ($scope, $location, $http, reviewService, HOST) {
    var url = `${HOST}/api/review`;
    var urlorder = `${HOST}/api/orderdetail`;
    $scope.items = [];
    $scope.reviewdata = reviewService.get();

     //Error : Incomplete upload image of review

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

    // load orderDetails
    $http.get(urlorder).then(resp => {
        $scope.cate = resp.data;
    });

    // load data review
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

    // xoa form
    $scope.reset = function () {
        $scope.reviewdata = {};
    }

    // hien thi len form
    $scope.edit = function (item) {
        reviewService.set(item);
    }

    // cap nhat review
    $scope.update = function () {
        var item = angular.copy($scope.reviewdata);
        $http.put(`${url}/${item.id}`, item).then(resp => {
            var index = $scope.items.findIndex(p => p.id == item.id);
            $scope.items[index] = item;
            $scope.reset();
            sweetalert_success("Cập nhật đánh giá thành công!");
            $location.path('review-list');
        }).catch(error => {
            sweetalert_error("Lỗi cập nhật đánh giá!");
            console.log("Error", error);
        });
    }

    // xoa review
    $scope.delete = function (item) {
        $http.delete(`${url}/${$scope.reviewdata.id}`).then(resp => {
            var index = $scope.items.findIndex(p => p.id == $scope.reviewdata.id);
            $scope.items.splice(index, 1);
            $scope.reset();
            sweetalert_success("Xóa đánh giá thành công!");
            $location.path('review-list');
        }).catch(error => {
            sweetalert_error("Lỗi xóa đánh giá!");
            console.log("Error", error);
        });
    }

    // Export excel
    $(document).ready(function () {
        $("#saveAsExcel").click(function () {
            var workbook = XLSX.utils.book_new();
            var worksheet_data = document.getElementById("table");
            var worksheet = XLSX.utils.table_to_sheet(worksheet_data);

            workbook.SheetNames.push("review");
            workbook.Sheets["review"] = worksheet;
            exportExcelFile(workbook);
        });
    })
    function exportExcelFile(workbook) {
        return XLSX.writeFile(workbook, "review_List.xlsx");
    }
});