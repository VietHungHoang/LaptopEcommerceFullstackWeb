app.factory("myService", function () {
    var savedData = {};

    function set(data) {
        savedData = data;
    }

    function get() {
        return savedData;
    }
    return {
        set: set,
        get: get,
    };
});

app.factory("orderService", function () {
    var savedData = {};

    function set(data) {
        savedData = data;
    }

    function get() {
        return savedData;
    }
    return {
        set: set,
        get: get,
    };
});

app.factory("authInterceptor", function (HOST, authService) {
    return {
        request: function (config) {
            var token = authService.getToken();
            if (config.url.indexOf(HOST) === 0 && token) {
                config.headers.Authorization = "Bearer " + token;
            }
            return config;
        },
        response: function (res) {
            if (res.config.url.indexOf(HOST) === 0 && res.data.token) {
                authService.saveToken(res.data.token);
            }
            return res;
        }
    };
});

app.service("authService", function ($window, $location) {
    var vm = this;
    vm.parseJwt = function (token) {
        var base64Url = token.split(".")[1];
        var base64 = base64Url.replace("-", "+").replace("_", "/");
        return JSON.parse($window.atob(base64));
    };
    vm.getToken = function () {
        return $window.localStorage["jwtToken"];
    };
    vm.saveToken = function (token) {
        $window.localStorage["jwtToken"] = token;
    };
    vm.logout = function (token) {
        $window.localStorage.removeItem("currentUser");
        $window.localStorage.removeItem("jwtToken");
        $location.path("/login");
    };
    vm.isAuthed = function () {
        var token = vm.getToken();
        if (token) {
            var params = vm.parseJwt(token);
            return Math.round(new Date().getTime() / 1000) <= params.exp;
        } else {
            return false;
        }
    };
});

app.service("userService", function ($http, HOST) {
    var vm = this;
    vm.register = function (username, password, fullname, phone, email) {
        return $http.post(HOST + "/auth/register", {
            username: username,
            password: password,
            fullname: fullname,
            phone: phone,
            email: email
        });
    };
    vm.login = function (username, password) {
        return $http.post(HOST + "/auth/login", {
            username: username,
            password: password
        });

    };
});

app.factory("$utility", ($window, $http, $routeParams, HOST) => {
    return {
        get $http() {
            return $http;
        },
        get $params() {
            return $routeParams;

        },
        get $message() {
            return {
                product: {
                    error: {
                        OVER_QUANTITY() {
                            return "Đã vượt quá số lượng hàng trong kho!";
                        },
                    },
                },
                user: {
                    success: {
                        CHANGE_PASSWORD() {
                            return "Cập nhật mật khẩu thành công!";
                        },
                    },
                    error: {
                        CHANGE_PASSWORD() {
                            return "Cập nhật mật khẩu thất bại!";
                        },
                    },
                },
                mail: {
                    success: {
                        RESET_PASSWORD() {
                            return "Chúng tôi đã gửi một liên kết đặt lại mật khẩu đến email của bạn. Nếu bạn không thấy email, hãy kiểm tra thư rác của bạn.";
                        },
                    },
                    error: {
                        RESET_PASSWORD(code, message) {
                            switch (code) {
                                case -1:
                                    return "Không thể kết nối đến server. Vui lòng kiểm tra lại server!";
                                case 500:
                                    return message ? message : "Không tìm thấy địa chỉ email.";
                                default:
                                    return "Không thể gửi mail.";
                            }
                        },
                    },
                },
            };
        },
        get $templateUrl() {
            return {
                getHomeTemplates: () => {
                    const templatePath = "pages/home";
                    return {
                        slider: `${templatePath}/slider.html`,
                        features: `${templatePath}/features.html`,
                        products: `${templatePath}/products.html`,
                        featuredProducts: `${templatePath}/featured-product.html`,
                        productsByCategory: `${templatePath}/product-by-category.html`,
                        productItem: `${templatePath}/product-item.html`,
                        bannerTop: `${templatePath}/banner-top.html`,
                        bannerMiddle: `${templatePath}/banner-middle.html`,
                        bannerBottom: `${templatePath}/banner-bottom.html`,
                    };
                },
                getProductTemplates: () => {
                    const templatePath = "pages/product";
                    return {
                        breadcrumb: `${templatePath}/breadcrumb.html`,
                        details: `${templatePath}/details.html`,
                        reviews: `${templatePath}/reviews.html`,
                        related: `${templatePath}/related.html`,
                    };
                },
            };
        },
        get $storage() {
            const local = $window.localStorage;
            const session = $window.sessionStorage;
            const json = {
                string: (value) => {
                    return JSON.stringify(value);
                },
                parse: (value) => {
                    try {
                        return JSON.parse(value);
                    } catch (err) {
                        console.log(err);
                    }
                },
            };
            class Local {
                set(key, value) {
                    local.setItem(key, json.string(value));
                }
                get(key) {
                    let value = json.parse(local.getItem(key));
                    return value ? value : this.remove(key);
                }
                remove(key) {
                    local.removeItem(key);
                }
                clear() {
                    local.clear();
                }
            }
            class Session {
                set(key, value) {
                    session.setItem(key, json.string(value));
                }
                get(key) {
                    let value = json.parse(session.getItem(key));
                    return value ? value : this.remove(key);
                }
                remove(key) {
                    session.removeItem(key);
                }
                clear() {
                    session.clear();
                }
            }
            return {
                local: new Local(),
                session: new Session(),
            };
        },
        get $url() {
            class UrlService {
                redirect(url) {
                    $window.location.href = url;
                }
                redirectToProductPage() {
                    this.redirect("/#!product");
                }
            }
            return new UrlService();
        },
        get $serverUrl() {
            const featuredProductsUrl = `${HOST}/api/product/getFeaturedProducts`;
            const categoriesUrl = `${HOST}/api/category`;
            const productsUrl = `${HOST}/api/product`;
            const bannersUrl = `${HOST}/api/banner`;
            const usersUrl = `${HOST}/api/user`;
            const forgotPasswordUrl = `${HOST}/auth/forgot-password`;
            const resetPasswordUrl = `${HOST}/auth/reset-password`;
            return {
                apiUrls: {
                    featuredProducts: featuredProductsUrl,
                    categories: categoriesUrl,
                    products: productsUrl,
                    banners: bannersUrl,
                    users: usersUrl,
                },
                forgotPasswordUrl: forgotPasswordUrl,
                resetPasswordUrl: resetPasswordUrl,
            };
        },
        get $url() {
            class UrlService {
                redirect(url) {
                    $window.location.href = url;
                }
                redirectToProductPage() {
                    this.redirect("/#!product");
                }
                redirectToHomePage() {
                    this.redirect("/");
                }
                redirectToLoginPage() {
                    this.redirect("/#!login");
                }
            }
            return new UrlService();
        },
        get $data() {
            const apiUrls = this.$serverUrl.apiUrls;
            return {
                fetch($scope, { name, url }) {
                    if (!url) url = apiUrls[name];
                    $http.get(url).then((resp) => {
                        $scope[name] = resp.data;
                    }).catch((err) => console.log(err));
                },
            };
        },
        get $owlSlick() {
            return {
                configIndex: ($scope) => {
                    $scope.slickConfig = {
                        arrows: false,
                        autoplay: false,
                        autoplaySpeed: 5000,
                        dots: true,
                        pauseOnFocus: false,
                        pauseOnHover: false,
                        fade: true,
                        infinite: true,
                        slidesToShow: 1,
                        responsive: [
                            {
                                breakpoint: 767,
                                settings: {
                                    dots: true,
                                },
                            },
                        ],
                    };

                    $scope.owlOptions = {
                        loop: true,
                        dots: false,
                        margin: 30,
                        nav: true,
                        navText: [
                            '<i class="lnr lnr-arrow-left"></i>',
                            '<i class="lnr lnr-arrow-right"></i>',
                        ],
                        autoplay: false,
                        stagePadding: 0,
                        smartSpeed: 700,
                        responsive: {
                            0: {
                                items: 1,
                                nav: false,
                            },
                            480: {
                                items: 2,
                                nav: false,
                            },
                            768: {
                                items: 3,
                            },
                            992: {
                                items: 4,
                            },
                            1024: {
                                items: 4,
                            },
                            1600: {
                                items: 7,
                            },
                        },
                    };

                    $scope.owlOptionss = {
                        items: 3,
                        loop: true,
                        dots: false,
                        margin: 30,
                        nav: true,
                        navText: [
                            '<i class="lnr lnr-arrow-left"></i>',
                            '<i class="lnr lnr-arrow-right"></i>',
                        ],
                        autoplay: false,
                        stagePadding: 0,
                        smartSpeed: 700,
                        responsive: {
                            0: {
                                items: 1,
                                nav: false,
                            },
                            480: {
                                items: 1,
                                nav: false,
                            },
                            768: {
                                items: 2,
                            },
                            992: {
                                items: 3,
                            },
                            1024: {
                                items: 3,
                            },
                            1600: {
                                items: 4,
                            },
                        },
                    };

                    $scope.owlOptionsss = {
                        loop: true,
                        margin: 30,
                        dots: false,
                        autoplay: false,
                        nav: true,
                        navText: [
                            '<i class="lnr lnr-arrow-left"></i>',
                            '<i class="lnr lnr-arrow-right"></i>',
                        ],
                        stagePadding: 0,
                        smartSpeed: 700,
                        responsive: {
                            0: {
                                items: 1,
                            },
                            480: {
                                items: 2,
                            },
                            768: {
                                items: 3,
                            },
                            992: {
                                items: 4,
                            },
                            1024: {
                                items: 5,
                            },
                            1600: {
                                items: 6,
                            },
                        },
                    };
                },
                configProduct: ($scope) => {
                    $scope.slickConfigs = {
                        slidesToShow: 1,
                        slidesToScroll: 1,
                        fade: true,
                        arrows: false,
                        asNavFor: ".pro-nav",
                    };

                    $scope.slickConfigss = {
                        slidesToShow: 5,
                        slidesToScroll: 1,
                        prevArrow:
                            '<button type="button" class="arrow-prev"><i class="fa fa-long-arrow-left"></i></button>',
                        nextArrow:
                            '<button type="button" class="arrow-next"><i class="fa fa-long-arrow-right"></i></button>',
                        asNavFor: ".product-large-slider",
                        centerMode: true,
                        arrows: true,
                        centerPadding: 0,
                        focusOnSelect: true,
                    };

                    // product view mode change js
                    $(".product-view-mode a").on("click", function (e) {
                        e.preventDefault();

                        var shopProductWrap = $(".shop-product-wrap");
                        var viewMode = $(this).data("target");

                        $(".product-view-mode a").removeClass("active");
                        $(this).addClass("active");
                        shopProductWrap
                            .removeClass("grid list column_3")
                            .addClass(viewMode);
                    });

                    // modal fix
                    $(".modal").on("shown.bs.modal", function (e) {
                        $(".pro-nav").resize();
                    });
                },
            };
        },
    };
});

app.factory("$cart", ($utility) => {
    const $message = $utility.$message;
    const $local = $utility.$storage.local;

    var sweetalert_topPU_success = function (text) {
        const Toast = Swal.mixin({
            toast: true,
            position: 'bottom-end',
            showConfirmButton: false,
            timer: 3000,
            timerProgressBar: true,
            didOpen: (toast) => {
                toast.addEventListener('mouseenter', Swal.stopTimer)
                toast.addEventListener('mouseleave', Swal.resumeTimer)
            }
        })

        Toast.fire({
            icon: 'success',
            title: text,
        })
    }

    var sweetalert_warning = function (text) {
        Swal.fire({
            icon: "warning",
            title: text,
            showConfirmButton: false,
            timer: 2000,
        });
    }
    class Cart {
        #items;
        #cart_local = "cart";
        constructor() {
            this.#items = new Map();
            this.getFromLocal();
        }

        get size() {
            return this.#items.size;
        }

        get total() {
            let total = 0;
            this.#items.forEach((item) => {
                total += item.price * item.quantity;
            });
            return total;
        }

        get values() {
            return [...this.#items.values()];
        }

        getItem(id) {
            return this.#items.get(id);
        }

        getItemQuantity(id) {
            const item = this.getItem(id);
            return item ? item.quantity : 0;
        }

        contains(productId) {
            return this.#items.has(productId);
        }

        addItem(product, quantityToAdd) {
            let item = this.getItem(product.id);
            let quantity;
            quantityToAdd = quantityToAdd ? quantityToAdd : quantity;
            if (item) {
                quantity = item.quantity + quantityToAdd;
                if (quantity = 1) {
                    console.log($message.product.error.OVER_QUANTITY);
                    sweetalert_warning("Chỉ được mua sản phẩm với số lượng là 1 nếu muốn mua thêm thì liên hệ với người đăng sản phẩm");
                    return;
                }
            } else {
                quantity = quantityToAdd;
            }
            this.#items.set(product.id, { ...product, quantity: 1 });
            this.saveToLocal();
            sweetalert_topPU_success('Đã thêm sản phẩm vào giỏ hàng');

        }

        removeItem(productId) {
            this.#items.delete(productId);
            this.saveToLocal();
        }

        saveToLocal() {
            $local.set(this.#cart_local, this.values);
        }

        getFromLocal() {
            const items = $local.get(this.#cart_local);
            if (items) items.forEach((item) => this.#items.set(item.id, item));
        }
    }
    return new Cart();
});

app.factory("$product", ($utility) => {
    const $session = $utility.$storage.session;
    return {
        set current(product) {
            $session.set("product", product);
        },
        get current() {
            return $session.get("product");
        },
    };
});

app.filter('startFrom', function () {
    return function (input, start) {
        start = +start; //parse to int
        return input.slice(start);
    }
});
