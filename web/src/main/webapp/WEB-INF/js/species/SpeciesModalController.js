function SpeciesModalController($log, $scope, $rootScope, $uibModalInstance, SpeciesService, StateService) {
    $scope.species = SpeciesService.getState().selectedSpecies;
    $scope.varieties = SpeciesService.getAllVarieties();
    $scope.user = StateService.getUser();
    $scope.addForm = {name: '', visible: false};
    $scope.selectSpecies = function (speciesId) {
        $log.info('Species from modal selected:' + speciesId);
        SpeciesService.selectSpecies(SpeciesService.getSpecies(speciesId));
        $uibModalInstance.dismiss('selected species');
    };
    $scope.close = function () {
        $uibModalInstance.dismiss('close');
    };
    $scope.addVariety = function (form, speciesId) {
        $log.log('Add Variety:', form);
        SpeciesService.addVariety(form.varietyName, speciesId);
        form.varietyName = '';
        form.visible = false;
    };
    $scope.toggleVariety = function (variety, species, event) {
        $log.log('Toggle variety:', [variety, species, event]);
        species.variety = species.variety == variety ? null : variety;
        event.currentTarget.blur();
    };
    $rootScope.$broadcast('species-modal-open', $scope.species);
    $scope.$on('modal.closing', function (a, b, c) {
        $rootScope.$broadcast('species-modal-close', $scope.species);
    });

}

angular.module('smigoModule').controller('SpeciesModalControvarietyControllerller', SpeciesModalController);