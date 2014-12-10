function RegisterController($scope, $timeout, UserService) {
    $scope.viewModel = {
        register: true,
        usernameMin: 5,
        usernameMax: 40,
        usernamePattern: /^[\w]+$/,
        passwordMin: 6,
        pageMessageKey: 'msg.account.register'
    };

//     var newName = 'testreg' + Math.floor(Math.random() * 999999999);
//     $scope.passwordAgain = newName;
//     $scope.formModel = {
//     username: newName,
//     password: newName
//     };

    $scope.submitLoginOrRegisterForm = UserService.register;
    $scope.requestFeature = UserService.requestFeature;


    var renderCaptchaPromise = $timeout(function renderCaptcha() {
        $scope.widgetId = grecaptcha.render('recaptcha', {
            sitekey: '6LeO6_4SAAAAACgz20mK-j47nP8wJULuMci06Cej'
        });
    }, 2000);

    $scope.$on('$destroy', function () {
        $timeout.cancel(renderCaptchaPromise);
    });


}

angular.module('smigoModule').controller('RegisterController', RegisterController);