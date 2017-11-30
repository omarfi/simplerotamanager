SRMApp.controller('MainController', function ($location, $http, $window, moment, calendarConfig, SkiftView) {
    var vm = this;

    vm.calendarView = 'month';
    vm.viewDate = new Date();
    var actions = [{
        label: '<i class=\'glyphicon glyphicon-pencil\'></i>',
        onClick: function (args) {
        }
    }, {
        label: '<i class=\'glyphicon glyphicon-remove\'></i>',
        onClick: function (args) {
        }
    }];
    vm.events = [];

    vm.cellIsOpen = true;

    vm.slideboxScope = {
        addEvent: function (date) {
            var newEvent = {
                title: '',
                startsAt: new Date(date),
                endsAt: new Date(date),
                draggable: true,
                resizable: true,
                actions: [
                    {
                        label: '<i class=\'glyphicon glyphicon-pencil\'></i>',
                        onClick: vm.eventEdited
                    },
                    {
                        label: '<i class=\'glyphicon glyphicon-remove\'></i>',
                        onClick: vm.eventDeleted
                    }
                ]
            };
            vm.events.push(newEvent);
            SkiftView.show(vm.events, newEvent);
        },
        formatSkiftTitle: function (event) {
            return event.title + ' (' + moment(event.startsAt).format('HH:mm') + ' - ' + moment(event.endsAt).format('HH:mm') + ')';
        }
    };

    vm.eventClicked = function (event) {
        SkiftView.show(vm.events, event);
    };

    vm.eventEdited = function (args) {
        SkiftView.show(vm.events, args.calendarEvent);
    };

    vm.eventDeleted = function (args) {
        vm.events.splice(vm.events.indexOf(args.calendarEvent), 1);
    };

    vm.timespanClicked = function (date) {
        if (vm.calendarView === 'month') {
            if (vm.viewDate.getDate() !== date.getDate()) {
                vm.viewDate = date;
                vm.cellIsOpen = true;
            } else {
                vm.cellIsOpen = !vm.cellIsOpen;
            }
        }
    };

    function createRequestData() {
        var data = angular.copy(vm.events);

        var options = {
            hour12: false, year: 'numeric', month: "2-digit", day: "2-digit",
            hour: "2-digit", minute: "2-digit", second: "2-digit"
        };

        for (var i = 0; i < vm.events.length; i++) {
            data[i].startsAt = vm.events[i].startsAt.toLocaleString({}, options);
            data[i].endsAt = vm.events[i].endsAt.toLocaleString({}, options);
        }
        return data;
    }

    vm.generer = function () {
        var url = $location.absUrl() + "genererTjenesteplan";

        var config = {
            headers: {
                'Accept': 'text/plain'
            }
        };

        $http.post(url, createRequestData(), config).then(function (response) {
            if (response.status === 200) {
                $window.location.href = $location.absUrl() + "/lastned";
            }

        }, function error(response) {
            vm.postResultMessage = "Feil med status: " + response.statusText;
        });


    }
});