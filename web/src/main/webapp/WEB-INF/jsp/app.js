"use strict";
var app = angular.module('speciesModule', ['ngRoute']);

app.config(function ($routeProvider) {
    $routeProvider.
        when('/test', {
            templateUrl: 'test.html',
            controller: 'TestController'
        }).
        when('/help', {
            templateUrl: 'help.html',
            controller: 'AboutController'
        }).
        when('/login', {
            templateUrl: 'login.html',
            controller: 'LoginController'
        }).
        when('/register', {
            templateUrl: 'login.html',
            controller: 'RegisterController'
        }).
        when('/garden', {
            templateUrl: 'garden.html',
            controller: 'GardenController'
        }).
        otherwise({redirectTo: '/garden'});

});
app.controller('TestController', function ($scope) {
    $scope.doSomething = function () {
        alert('Submitted!');
    }
});
app.run(function ($rootScope) {
    console.log("App run");
    $rootScope.currentUser = '${pageContext.request.remoteUser}';
    $rootScope.getObjectLength = function (obj) {
        return Object.keys(obj).length;
    }

});
app.filter('translate', function () {
    var msg = <c:out escapeXml="false" value="${f:toJson(messages)}" />;
    return function (messageObject, param) {
        if (!messageObject) {
            console.error('Can not translate', messageObject);
            return 'n/a';
        }
        if (messageObject.messageKey) {
            return msg[messageObject.messageKey];
        }
        if (!msg[messageObject]) {
            console.error('Could not translate:' + messageObject);
            return messageObject;
        }
        return msg[messageObject].replace('{0}', param);
    };
});
app.directive('equals', function () {
    return {
        restrict: 'A', // only activate on element attribute
        require: '?ngModel', // get a hold of NgModelController
        link: function (scope, elem, attrs, ngModel) {
            console.log('Equals', [scope, elem, attrs, ngModel]);
            if (!ngModel) return; // do nothing if no ng-model

            // watch own value and re-validate on change
            scope.$watch(attrs.ngModel, function () {
                validate();
            });

            // observe the other value and re-validate on change
            attrs.$observe('equals', function (val) {
                validate();
            });

            var validate = function () {
                // values
                var val1 = ngModel.$viewValue;
                var val2 = attrs.equals;

                // set validity
                ngModel.$setValidity('equals', val1 === val2);
            };
        }
    }
});
app.directive('rememberScroll', function ($timeout) {
    return {
        restrict: 'A',
        link: function (scope, $elm, attr) {
            scope.$watch('selectedYear', function (newYear, oldYear, wtf) {
                $timeout(function () {
                    $elm[0].scrollLeft = 99999;
                    $elm[0].scrollLeft = $elm[0].scrollLeft / 2;
                    $elm[0].scrollTop = 99999;
                    $elm[0].scrollTop = $elm[0].scrollTop / 2;
                }, 0, false);
            }, true);
        }
    }
});
app.factory('plantService', function ($http) {
    console.log('plantService');
    var garden = <c:out escapeXml="false" value="${f:toJson(garden)}"/>;
    var selectedYear = +Object.keys(garden.squares).sort().slice(-1).pop();
    var selectedSpecies = garden.species["28"];

    function PlantData(plant) {
        this.year = plant.location.year;
        this.y = plant.location.y;
        this.x = plant.location.x;
        this.speciesId = plant.species.id;
    }

    function fillGarden() {
        angular.forEach(garden.squares, function (squareList) {
            angular.forEach(squareList, function (square) {
                angular.forEach(square.plants, function (plant) {
                });
            });
        });
    }

    function reloadGarden() {
        var promise = $http.get('rest/garden');
        promise.success(function (data) {
            console.log('Garden reloaded', data);
            garden = data;
        });
        promise.error(function (data, status, headers, config) {
            console.error('Could not reload garden', [data, status, headers, config]);
        });
        return promise;
    }

    function getBounds(year) {
        var axisLength = 9999;
        var ret = {
            xmax: -axisLength,
            ymax: -axisLength,
            xmin: axisLength,
            ymin: axisLength
        };
        angular.forEach(garden.squares[year], function (square, index) {
            ret.xmax = Math.max(square.location.x, ret.xmax);
            ret.ymax = Math.max(square.location.y, ret.ymax);
            ret.xmin = Math.min(square.location.x, ret.xmin);
            ret.ymin = Math.min(square.location.y, ret.ymin);
        });
        return ret;
    }

    return {
        getBounds: getBounds,
        getSelectedSpecies: function () {
            return selectedSpecies;
        },
        setSelectedSpecies: function (species) {
            selectedSpecies = species;
        },
        getSelectedYear: function () {
            return selectedYear;
        },
        setSelectedYear: function (year) {
            if (!garden.squares.hasOwnProperty(year)) {
                garden.squares[year] = [];
            }
            selectedYear = year;
        },
        getGarden: function () {
            return garden;
        },
        reloadGarden: reloadGarden,
        addYear: function (year) {
            var mostRecentYear = Object.keys(garden.squares).sort().slice(-1).pop();
            var newYearSquareArray = [];
            var copyFromSquareArray = garden.squares[mostRecentYear];

            angular.forEach(copyFromSquareArray, function (square) {
                angular.forEach(square.plants, function (plant) {
                    if (!plant.species.annual) {
                        var newLocation = {year: year, x: plant.location.x, y: plant.location.y};
                        var newSquare = {location: newLocation, plants: {}};
                        newSquare.plants[plant.species.id] = {species: plant.species, location: newLocation, add: true};
                        newYearSquareArray.push(newSquare);
                    }
                });
            });

            //add verge
            var bounds = getBounds(mostRecentYear);
            for (var x = bounds.xmin - 1; x <= bounds.xmax + 1; x++) {
                newYearSquareArray.push({location: {year: year, x: x, y: bounds.ymin - 1}, plants: {}, verge: true});
                newYearSquareArray.push({location: {year: year, x: x, y: bounds.ymax + 1}, plants: {}, verge: true});
            }
            for (var y = bounds.ymin; y <= bounds.ymax; y++) {
                newYearSquareArray.push({location: {year: year, x: bounds.xmax + 1, y: y}, plants: {}, verge: true});
                newYearSquareArray.push({location: {year: year, x: bounds.xmin - 1, y: y}, plants: {}, verge: true});
            }

            garden.squares[year] = newYearSquareArray;
            selectedYear = year;
            console.log('Year added:' + year, garden.squares);
        },
        getAvailableYears: function () {
            var sortedYearsArray = Object.keys(garden.squares).sort();
            var firstYear = +sortedYearsArray[0];
            var lastYear = +sortedYearsArray.slice(-1)[0];
            var ret = [];
            for (var i = firstYear; i <= lastYear; i++) {
                ret.push(i);
            }
            console.log('AvailableYears', ret);
            return ret;
        },
        addSquare: function (year, x, y) {
            var newSquare = {
                location: {
                    x: x,
                    y: y,
                    year: +year
                },
                plants: {}
            };
            garden.squares[year].push(newSquare);
            console.log('Square added', newSquare);
            return newSquare;
        },
        removePlant: function (square) {
            angular.forEach(square.plants, function (plant, key) {
                plant.remove = true;
                if (plant.add) {//undo add
                    delete square.plants[key];
                }
            });
            console.log('Species removed', square);
        },
        addPlant: function (species, square) {
            if (!square.plants[species.id]) {
                square.plants[species.id] = { species: species, location: square.location};
            }
            square.plants[species.id].add = true;
            console.log('Species added', square);
        },
        save: function () {
            var update = { addList: [], removeList: [] };
            angular.forEach(garden.squares, function (squareList) {
                angular.forEach(squareList, function (square) {
                    angular.forEach(square.plants, function (plant) {
                        if (plant.add && !plant.remove) {
                            update.addList.push(new PlantData(plant));
                            delete plant.add;
                            delete plant.remove;
                        } else if (plant.remove && !plant.add) {
                            update.removeList.push(new PlantData(plant));
                            delete square.plants[plant.species.id];
                        }
                    });
                });
            });
            console.log('Sending to server', update);
            $http.post('rest/garden', update);
        }
    };
});
app.factory('userService', function ($rootScope, $http, $location, plantService) {
    return {
        loginOrRegister: function (form, scope, action) {
            scope.objectErrors = [];
            if (form.$invalid) {
                console.log('Form is invalid:' + action);
                //set all values to trigger dirty, so validation messages become visible
                angular.forEach(form, function (value) {
                    if (value.$setViewValue) {
                        value.$setViewValue(value.$viewValue);
                    }
                });
                return;
            }

            scope.formModel['remember-me'] = true;

            $http({
                method: 'POST',
                url: action,
                data: $.param(scope.formModel),
                headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            }).success(function (data, status, headers, config) {
                $rootScope.currentUser = scope.formModel.username;
                plantService.reloadGarden().then(function () {
                    console.log("Redirecting to garden");
                    $location.path('/garden');
                });
                console.log('Performed successfully action:' + action, [data, status, headers, config]);
            }).error(function (data, status, headers, config) {
                scope.objectErrors = data;
                delete $rootScope.currentUser;
                console.log('Performed unsuccessfully action:' + action, [form, data, status, headers, config]);
            });
        }};
});
app.controller('GardenController', function ($scope, $rootScope, $http, plantService) {
    console.log('GardenController', $scope);

    //Garden
    $scope.$watch(plantService.getGarden, function (newVal, oldVal, scope) {
        $scope.garden = plantService.getGarden();
        $scope.forwardYear = +Object.keys(plantService.getGarden().squares).sort().slice(-1).pop() + 1;
        $scope.backwardYear = +Object.keys(plantService.getGarden().squares).sort()[0] - 1;
        console.log("Garden changed", [$scope, scope]);
    }, true);

    //Year
    $scope.$watch(plantService.getSelectedYear, function (newVal, oldVal, scope) {
        $scope.selectedYear = plantService.getSelectedYear();
        $scope.availableYears = plantService.getAvailableYears();
    });
    $scope.addYear = plantService.addYear;
    $scope.setSelectedYear = plantService.setSelectedYear;

    //Species
    $scope.$watch(plantService.getSelectedSpecies, function (newVal, oldVal, scope) {
        $scope.selectedSpecies = plantService.getSelectedSpecies();
    });
    $scope.setSelectedSpecies = plantService.setSelectedSpecies;

    //Action
    //todo

    $scope.onSquareClick = function (clickEvent, square) {
        console.log('Square clicked', [clickEvent, square, $scope.selectedSpecies]);
        if (clickEvent.shiftKey) {
            plantService.removePlant(square);
        } else if (clickEvent.ctrlKey) {
            console.log('Copy species');
        } else {
            plantService.addPlant($scope.selectedSpecies, square);
        }
        plantService.save();
        clickEvent.stopPropagation();
    };

    $scope.onGridClick = function (clickEvent) {
        var x = Math.floor((clickEvent.offsetX - 100000) / 48);
        var y = Math.floor((clickEvent.offsetY - 100000) / 48);
        var addedSquare = plantService.addSquare($scope.selectedYear, x, y);
        plantService.addPlant($scope.selectedSpecies, addedSquare);
        plantService.save();
        clickEvent.stopPropagation();
    };

    $scope.getGridSizeCss = function (year) {
        var bounds = plantService.getBounds(year);
//        console.log('Grid size', ymin, xmax, ymax, xmin);
        var margin = 48 * 2;
        return {
            'margin-top': (-100000 + -bounds.ymin * 48 + margin) + 'px',
            'width': (100000 + 47 + bounds.xmax * 48 + margin) + 'px',
            'height': (100000 + 47 + bounds.ymax * 48 + margin) + 'px',
            'margin-left': (-100000 + -bounds.xmin * 48 + margin) + 'px'
        };
    };

    $scope.getSquarePositionCss = function (square) {
        return {
            top: square.location.y * 48 + 100000 + 'px',
            left: square.location.x * 48 + 100000 + 'px'
        };
    };
});
app.controller('LoginController', function ($scope, userService) {
    $scope.viewModel = {
        login: true,
        usernameMin: 0,
        usernameMax: 999,
        usernamePattern: /.+/,
        passwordMin: 0,
        pageMessageKey: 'account.login'
    };
    $scope.formModel = {
        username: 'user7389327855123',
        password: 'testreg17'
    };
    $scope.submitLoginOrRegisterForm = function (form) {
        console.log('loginOrRegister', [form, $scope]);
        userService.loginOrRegister(form, $scope, 'login');
    }
});
app.controller('RegisterController', function ($scope, userService) {
    $scope.viewModel = {
        register: true,
        usernameMin: 5,
        usernameMax: 40,
        usernamePattern: /^[\w]+$/,
        passwordMin: 6,
        pageMessageKey: 'msg.account.register'
    };

    /*
     var newName = 'testreg' + Math.floor(Math.random() * 999999999);
     $scope.formModel = {
     username: newName,
     password: newName,
     passwordAgain: newName
     };
     */

    $scope.submitLoginOrRegisterForm = function (form) {
        userService.loginOrRegister(form, $scope, 'register');
    }
});
app.controller('AboutController', function () {
});
app.controller('MainMenuController', function ($http, $scope, $rootScope, plantService, $route) {
    $scope.logout = function () {
        $http({
            method: 'GET',
            url: 'logout'
        }).success(function (data, status, headers, config) {
            delete $rootScope.currentUser;
            plantService.reloadGarden().then(function () {
                $route.reload();
            });
        }).error(function (data, status, headers, config) {
            delete $rootScope.currentUser;
        });

    };
});