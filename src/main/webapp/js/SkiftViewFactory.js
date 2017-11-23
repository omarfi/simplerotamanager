SRMApp.factory('SkiftView', function($uibModal) {

        function show(events, newEvent) {
            return $uibModal.open({
                templateUrl: 'templates/skiftView.html',
                controller: function() {
                    var vm = this;
                    vm.events = events;
                    vm.event = newEvent;
                },
                controllerAs: 'vm'
            });
        }

        return {
            show: show
        };

    });
