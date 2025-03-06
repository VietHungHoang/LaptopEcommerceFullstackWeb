app.controller("user-ctrl", function ($scope, $location, $http, userService, HOST) {
    var url = `${HOST}/api/user`;
    $scope.items = [];
    $scope.userdata = userService.get();

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

    //load data
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

    //xoa form
    $scope.reset = function () {
        $scope.userdata = {};
    }

    //hien thi len form
    $scope.edit = function (item) {
        userService.set(item);
        // * Delete all element in class "myDIV"
        setTimeout(() => {
            const getdiv = document.getElementById("myDIV");
            getdiv.innerHTML = "";
        }, 1000)
        // * After 1s, function updateImg() is execute
        setTimeout(() => { $scope.updateImg() }, 1000)
    }

    // list IMG
    $scope.updateImg = () => {
        var listImg = document.getElementById("imgs").value.split(',');
        for (let index = 0; index < listImg.length; index++) {
            const para = document.createElement("img");
            para.setAttribute("src", listImg[index]);
            para.setAttribute("referrerpolicy", "no-referrer");
            para.style.width = '300px';
            para.style.marginRight = '10px';
            document.getElementById("myDIV").appendChild(para);
        }
    }

    //them sp moi
    $scope.create = function () {
        var datetime = new Date();
        $scope.userdata.createdat = moment(datetime).format("YYYY-MM-DD HH:mm");
        $scope.userdata.updatedat = moment(datetime).format("YYYY-MM-DD HH:mm");
        $scope.userdata.token = "null";
        var item = angular.copy($scope.userdata);
        $http.post(`${url}`, item).then(resp => {
            $scope.items.push(resp.data);
            $scope.reset();
            sweetalert_success("Thêm mới thành công!");
            $location.path('user-list');
        }).catch(error => {
            sweetalert_error("Lỗi thêm mới tài khoản!");
            console.log("Error", error);
        });
    }

    //cap nhat sp
    $scope.update = function () {
        var datetime = new Date();
        $scope.userdata.updatedat = moment(datetime).format("YYYY-MM-DD HH:mm");

        // * Get img
        $scope.userdata.image = document.getElementById("imgs").value;

        var item = angular.copy($scope.userdata);
        $http.put(`${url}/${item.id}`, item).then(resp => {
            var index = $scope.items.findIndex(p => p.id == item.id);
            $scope.items[index] = item;
            $scope.reset();
            sweetalert_success("Cập nhật tài khoản thành công!");
            $location.path('user-list');
        }).catch(error => {
            sweetalert_error("Lỗi cập nhật tài khoản!");
            console.log("Error", error);
        });
    }

    //xoa sp
    $scope.delete = function () {
        $http.delete(`${url}/${$scope.userdata.id}`).then(resp => {
            var index = $scope.items.findIndex(p => p.id == $scope.userdata.id);
            $scope.items.splice(index, 1);
            $scope.reset();
            sweetalert_success("Xóa tài khoản thành công!");
            $location.path('user-list');
        }).catch(error => {
            sweetalert_error("Lỗi xóa tài khoản!");
            console.log("Error", error);
        });
    }

    // Export excel
    $(document).ready(function () {
        $("#saveAsExcel").click(function () {
            var workbook = XLSX.utils.book_new();
            var worksheet_data = document.getElementById("table");
            var worksheet = XLSX.utils.table_to_sheet(worksheet_data);

            workbook.SheetNames.push("User");
            workbook.Sheets["User"] = worksheet;
            exportExcelFile(workbook);
        });
    })
    function exportExcelFile(workbook) {
        return XLSX.writeFile(workbook, "User_List.xlsx");
    }

    // Upload IMG to imgur api
    $('document').ready(function () {
        $('input[type=file]').on('change', function () {
            var $files = $(this).get(0).files;
            const getdiv = document.getElementById("myDIV");
            getdiv.innerHTML = "";
            if ($files.length) {
                for (let index = 0; index < $files.length; index++) {
                    if ($files[index].size > $(this).data('max-size') * 1024) {
                        return false;
                    }
                }
                document.getElementById("imgs").value = '';
                const arr = [];
                for (let index = 0; index < $files.length; index++) {
                    var apiUrl = 'https://api.imgur.com/3/image';
                    var apiKey = '146def7f79c7a87';
                    var settings = {
                        async: false,
                        crossDomain: true,
                        processData: false,
                        contentType: false,
                        type: 'POST',
                        url: apiUrl,
                        headers: {
                            Authorization: 'Client-ID ' + apiKey,
                            Accept: 'application/json',
                        },
                        mimeType: 'multipart/form-data',
                    };
                    var formData = new FormData();
                    formData.append('image', $files[index]);
                    settings.data = formData;

                    $.ajax(settings).done(function (response) {
                        var obj = JSON.parse(response);
                        var cut = JSON.stringify(obj.data.link);
                        arr.push(cut.slice(1, cut.length - 1));
                        const para = document.createElement("img");
                        para.setAttribute("src", cut.slice(1, cut.length - 1));
                        para.setAttribute("id", index);
                        para.setAttribute("referrerpolicy", "no-referrer");
                        para.style.width = '300px';
                        document.getElementById("myDIV").appendChild(para);
                    });
                }
                document.getElementById("imgs").value = arr.toString().split(',')
            }
        });
    });
});