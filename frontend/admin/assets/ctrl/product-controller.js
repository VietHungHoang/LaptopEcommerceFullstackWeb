app.controller("product-ctrl", function ($scope, $location, $http, productService, HOST) {
    var url = `${HOST}/api/product`;
    var urlcate = `${HOST}/api/category`;
    var urlcompany = `${HOST}/api/company`;
    var urluser = `${HOST}/api/user`;
    $scope.items = [];
    $scope.cate = [];
    $scope.user = [];
    $scope.company = [];
    $scope.productdata = productService.get();
    $scope.getuser = "";
    $scope.getcate = {};
    $scope.getcompany = {};

    ////Error : Cannot set properties of null (setting 'innerHTML')

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

    // load category
    $http.get(urlcate).then(resp => {
        $scope.cate = resp.data;
    });

    // load company
    $http.get(urlcompany).then(resp => {
        $scope.company = resp.data;
    });

    // load data product
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

    // filter user
    $http.get(urluser).then(resp => {
        $scope.user = resp.data;
    });
    $scope.findUser = function () {
        $scope.productdata.user = $scope.user.filter(function (item) {
            return item.username === $scope.productdata.user.username;
        })[0];
    }

    // xoa form
    $scope.reset = function () {
        $scope.productdata = {};
        var item = {};
        productService.set(item);
        setTimeout(() => {
            const getdiv = document.getElementById("myDIV");
            getdiv.innerHTML = "";
        }, 1000)
    }

    // hien thi len form
    $scope.edit = function (item) {
        productService.set(item);
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
            const outImg = document.createElement("div");
            outImg.setAttribute("class", "outImg")
            const img = document.createElement("img");
            img.setAttribute("src", listImg[index]);
            img.setAttribute("ondblclick", `angular.element(this).scope().deleteImg(` + index + `)`);
            img.setAttribute("referrerpolicy", "no-referrer");
            img.style.width = '300px';
            outImg.appendChild(img);
            document.getElementById("myDIV").appendChild(outImg);
        }
    }

    $scope.deleteImg = (index) => {
        var arrImg = document.getElementById("imgs").value.split(',');
        arrImg.splice(index, 1);
        document.getElementById("imgs").value = arrImg;
        // * Delete all element in class "myDIV"
        setTimeout(() => {
            const getdiv = document.getElementById("myDIV");
            getdiv.innerHTML = "";
        }, 1000)
        // * After 1s, function updateImg() is execute
        setTimeout(() => { $scope.updateImg() }, 2000)
    }

    //them san pham moi
    $scope.create = function () {
        $scope.productdata.createdAt = moment().format('YYYY-MM-DD HH:mm');
        $scope.productdata.updatedAt = moment().format('YYYY-MM-DD HH:mm');
        $scope.productdata.company = $scope.company[$scope.productdata.company - 1];
        $scope.productdata.category = $scope.cate[$scope.productdata.category - 1];

        var item = angular.copy($scope.productdata);
        $http.post(`${url}`, item).then(resp => {
            $scope.items.push(resp.data);
            $scope.reset();
            sweetalert_success("Thêm mới thành công!");
            $location.path('product-list');
        }).catch(error => {
            sweetalert_error("Lỗi thêm mới sản phẩm!");
            console.log("Error", error);
        });
    }

    //cap nhat san pham
    $scope.update = function () {
        $scope.productdata.updatedat = new Date().toJSON();
        $scope.productdata.company = $scope.company[$scope.productdata.company - 1];
        $scope.productdata.category = $scope.cate[$scope.productdata.category - 1];

        // * Get img
        $scope.productdata.image = document.getElementById("imgs").value;

        var item = angular.copy($scope.productdata);
        console.log(item);
        $http.put(`${url}/${item.id}`, item).then(resp => {
            var index = $scope.items.findIndex(p => p.id == item.id);
            $scope.items[index] = item;
            $scope.reset();
            sweetalert_success("Cập nhật sản phẩm thành công!");
            $location.path('product-list');
        }).catch(error => {
            sweetalert_error("Lỗi cập nhật sản phẩm!");
            console.log("Error", error);
        });
    }

    // xoa san pham
    $scope.delete = function (item) {
        $http.delete(`${url}/${$scope.productdata.id}`).then(resp => {
            var index = $scope.items.findIndex(p => p.id == $scope.productdata.id);
            $scope.items.splice(index, 1);
            $scope.reset();
            sweetalert_success("Xóa sản phẩm thành công!");
            $location.path('product-list');
        }).catch(error => {
            sweetalert_error("Lỗi xóa sản phẩm!");
            console.log("Error", error);
        });
    }

    // Export excel
    $(document).ready(function () {
        $("#saveAsExcel").click(function () {
            var workbook = XLSX.utils.book_new();
            var worksheet_data = document.getElementById("table");
            var worksheet = XLSX.utils.table_to_sheet(worksheet_data);

            workbook.SheetNames.push("Product");
            workbook.Sheets["Product"] = worksheet;
            exportExcelFile(workbook);
        });
    })
    function exportExcelFile(workbook) {
        return XLSX.writeFile(workbook, "Product_List.xlsx");
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
                        const outImg = document.createElement("div");
                        outImg.setAttribute("class", "outImg")
                        const img = document.createElement("img");
                        img.setAttribute("src", cut.slice(1, cut.length - 1));
                        img.setAttribute("ondblclick", `angular.element(this).scope().deleteImg(` + index + `)`);
                        img.setAttribute("referrerpolicy", "no-referrer");
                        img.style.width = '300px';
                        outImg.appendChild(img);
                        document.getElementById("myDIV").appendChild(outImg);
                    });
                }
                document.getElementById("imgs").value = arr.toString().split(',')
            }
        });
    });
});