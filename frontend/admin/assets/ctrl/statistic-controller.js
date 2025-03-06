app.controller('statistic-ctrl', function ($scope, $http, HOST) {
    const orderUrl = `${HOST}/api/order`;
    const categoryUrl = `${HOST}/api/category`;
    const companyUrl = `${HOST}/api/company`;
    const productUrl = `${HOST}/api/product`;
    const orderDetailUrl = `${HOST}/api/orderdetail`;;

    $http.get(orderUrl).then((resp) => {
        $scope.orders = resp.data
        $scope.orders.forEach(
            (od) => (od.year = new Date(od.createdat).getFullYear())
        )
        $scope.getYears($scope.orders)
        $scope.getProducts()
    }).catch((err) => console.error(err))

    $scope.getYears = (orders) => {
        const years = orders.map((od) => {
            return od.year
        })
        $scope.years = createArray(Math.min(...years), Math.max(...years))
    }

    function createArray(min, max) {
        const arr = new Array(max - min + 1)
        let count = 0
        while (count < arr.length) {
            arr[count] = min + count++
        }
        return arr
    }

    $scope.getProducts = () => {
        $http.get(productUrl).then((resp) => {
            $scope.products = resp.data
            $scope.getOrderDetails()
            $scope.getCategories($scope.products)
            $scope.getCompanies($scope.products)
        }).catch((err) => console.log(err))
    }
    $scope.getCategories = (products) => {
        $http.get(categoryUrl).then((resp) => {
            $scope.categories = resp.data
            const map = new Map($scope.categories.map((item) => [item.id, item]))
            products.forEach((prod) => (prod.category = map.get(prod.category.id)))
        }).catch((err) => console.error(err))
    }

    $scope.getCompanies = (products) => {
        $http.get(companyUrl).then((resp) => {
            $scope.companies = resp.data
            const map = new Map($scope.companies.map((item) => [item.id, item]))
            products.forEach((prod) => (prod.company = map.get(prod.company.id)))
        }).catch((err) => console.error(err))
    }

    $scope.getOrderDetails = () => {
        $http.get(orderDetailUrl).then((resp) => {
            $scope.orderDetails = resp.data
            $scope.orderDetails = resp.data
            $scope.getProductSold()
        })
    }

    $scope.getProductSold = () => {
        const { orderDetails, years, products } = $scope
        if (!orderDetails || !years || !products) {
            return
        }
        const productSold = $scope.years.map((year) =>
            $scope.getProductSoldByYear(year)
        )
        $scope.productSold = [].concat(...productSold)
    }

    $scope.getProductSoldByYear = (year) => {
        const filteredOrders = $scope.orders.filter((od) => od.year === year)
        const orderMap = new Map(filteredOrders.map((od) => [od.id, od]))

        const filteredDetails = $scope.orderDetails.filter((detail) =>
            orderMap.has(detail.order.id)
        )
        const soldCountMap = new Map(
            filteredDetails.map((detail) => [
                detail.product.id,
                {
                    quantity: detail.quantity,
                    year: orderMap.get(detail.order.id).year,
                },
            ])
        )
        return $scope.products.map((prod) => {
            const info = soldCountMap.get(prod.id)
            return {
                ...prod,
                soldCount: info ? info.quantity : 0,
                year: year,
            }
        })
    }

    $scope.initSearch = () => {
        $scope.search = {
            name: undefined,
            year: undefined,
            category: undefined,
            company: undefined,
        }
        Object.keys($scope.search).forEach((key) => {
            $scope.$watch(`search.${key}`, function () {
                if ($scope.search[key] == null) $scope.search[key] = undefined
            })
        })
    }
    $scope.initSearch()

    // Export excel
    $(document).ready(function () {
        $("#saveAsExcel").click(function () {
            var workbook = XLSX.utils.book_new();
            var worksheet_data = document.getElementById("table");
            var worksheet = XLSX.utils.table_to_sheet(worksheet_data);

            workbook.SheetNames.push("Statistic");
            workbook.Sheets["Statistic"] = worksheet;
            exportExcelFile(workbook);
        });
    })
    function exportExcelFile(workbook) {
        return XLSX.writeFile(workbook, "Statistic_List.xlsx");
    }
})

function distinctArray(arr) {
    const map = new Map(arr.map((item) => [item.id, item]))
    return [...map.values()]
}
