SRMApp.factory('SkiftView', function($uibModal) {

        function show(action, event) {
            return $uibModal.open({
                templateUrl: 'templates/skiftView.html',
                controller: function() {
                    var vm = this;
                    vm.action = action;
                    vm.event = event;
                },
                controllerAs: 'vm'
            });
        }

        return {
            show: show
        };

    });
