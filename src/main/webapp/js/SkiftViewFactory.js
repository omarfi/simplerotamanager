SRMApp.factory('SkiftView', function ($uibModal, Colors) {

    function show(events, newEvent) {
        return $uibModal.open({
            templateUrl: 'templates/skiftView.html',
            controller: function () {
                var vm = this;
                vm.events = events;
                vm.event = newEvent;
                vm.assignColor = function () {
                    var assignedColors = [];
                    var i;
                    for (i = 0; i < vm.events.length; i++) {
                        assignedColors.push(vm.events[i].color);
                        if (vm.events[i].title === newEvent.title && vm.events[i].calendarEventId !== newEvent.calendarEventId) {
                            newEvent.color = vm.events[i].color;
                            return;
                        }
                    }

                    // get new color if exists
                    for (i = 0; i < Colors.length; i++) {
                        if (assignedColors.indexOf(Colors[i]) === -1) {
                            newEvent.color = Colors[i];
                            return;
                        }
                    }

                    newEvent.color = Colors[0];
                };
            },
            controllerAs: 'vm'
        });
    }

    return {
        show: show
    };

});
