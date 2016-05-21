function AccountController($anchorScroll, $scope, $http, $log, $location, UserService) {
    'use strict';
    $scope.userBean = angular.copy(UserService.getState().currentUser);
    $scope.goTo = function (id) {
        $log.log('Scrolling to ', id);
        $location.hash(id);
        $anchorScroll();
    };

    $http.get('locales', {cache: true}).then(function (resopnse) {
        $log.log('Locales retrieved', resopnse);
        $scope.locales = resopnse.data;
    });

    $scope.submitAccountDetailsForm = function (form, userBean) {
        $log.log('Submit ', [form, userBean]);
        form.pendingSave = true;
        $scope.updateSuccessful = false;
        $scope.objectErrors = [];
        if (form.$invalid) {
            $log.log('Form is invalid', form);
            return;
        }
        UserService.updateUser(userBean)
            .then(function () {
                form.pendingSave = false;
                $scope.updateSuccessful = true;
                $scope.$emit('current-user-changed', userBean);
            })
            .catch(function (response) {
                $log.error('Update user failed', response);
                $scope.objectErrors = response.data;
                form.pendingSave = false;
            });
    };
}

angular.module('smigoModule').controller('AccountController', AccountController);