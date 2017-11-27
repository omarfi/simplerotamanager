SRMApp.controller('MainController', function ($location, $http, moment, calendarConfig, SkiftView) {
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

    vm.generer = function () {
        console.log(vm.events);
        var url = $location.absUrl() + "genererTjenesteplan";

        var config = {
            headers: {
                'Accept': 'text/plain'
            }
        };

        $http.post(url, vm.events, config).then(function (response) {
            vm.postResultMessage = response.data;
        }, function error(response) {
            vm.postResultMessage = "Error with status: " + response.statusText;
        });

        console.log(vm.postResultMessage);
    }
});