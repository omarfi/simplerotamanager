SRMApp.factory('SkiftView', function ($uibModal, Colors, moment) {

    function show(events, newEvent) {
        return $uibModal.open({
            templateUrl: 'templates/skiftView.html',
            controller: function () {
                var vm = this;
                vm.events = events;
                vm.event = newEvent;
                vm.erHelgeskift = vm.event.startsAt.getDay() === 0 || vm.event.startsAt.getDay() === 6;
                vm.heading = moment(vm.event.startsAt).format("dddd, Do MMMM YYYY");

                vm.startTimeChanged = function () {
                    if (vm.event.startsAt > vm.event.endsAt) {
                        vm.event.endsAt = vm.event.startsAt;
                    }
                };

                vm.getDagPrefix = function () {
                    switch (vm.event.startsAt.getDay()) {
                        case 1:
                            prefix = "man";
                            break;
                        case 2:
                            prefix = "tirs";
                            break;
                        case 3:
                            prefix = "ons";
                            break;
                        case 4:
                            prefix = "tors";
                            break;
                        case 5:
                            prefix = "fre";
                            break;
                        case 6:
                            prefix = "lør";
                            break;
                        case 0:
                            prefix = "søn";
                            break;
                    }
                    return prefix;
                };

                function assignColor() {
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

                function kopierSkiftTil(ukedager) {
                    var date;
                    for (date = moment(vm.event.startsAt).startOf('month');
                         date.month() === vm.event.startsAt.getMonth();
                         date.add(1, 'd')) {

                        if (date.date() === vm.event.startsAt.getDate()) {
                            continue
                        }

                        if (ukedager.indexOf(date.day()) !== -1) {
                            var copiedEvent = angular.copy(vm.event);
                            copiedEvent.startsAt.setDate(date.date());
                            copiedEvent.endsAt.setDate(date.date());
                            vm.events.push(copiedEvent);
                        }
                    }
                }

                vm.lagre = function () {
                    if (vm.sluttSkiftNesteDag === true) {
                        vm.event.endsAt.setDate(vm.event.endsAt.getDate() + 1);
                    }
                    assignColor();
                    if (vm.skiftFrekvens === 'ukentlig') {
                        kopierSkiftTil([vm.event.startsAt.getDay()]);
                    } else if (vm.skiftFrekvens === 'hverdager') {
                        kopierSkiftTil([1, 2, 3, 4, 5]);
                    } else if (vm.skiftFrekvens === 'helg') {
                        kopierSkiftTil([0, 6]);
                    } else if (vm.skiftFrekvens === 'daglig') {
                        kopierSkiftTil([0, 1, 2, 3, 4, 5, 6]);
                    }
                };

            },
            controllerAs: 'vm',
            backdrop: 'static'

        });
    }

    return {
        show: show
    };

});
