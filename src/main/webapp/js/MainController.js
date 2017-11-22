SRMApp.controller('MainController', function (moment, calendarConfig, SkiftView) {
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
                title: 'Ola Nordmann',
                startsAt: new Date(date),
                endsAt: new Date(date),
                color: calendarConfig.colorTypes.important,
                draggable: true,
                resizable: true
            };
            vm.events.push(newEvent);
            SkiftView.show(newEvent.title, newEvent);
        }
    };

    vm.eventClicked = function (event) {
        SkiftView.show(event.title, event);
    };

    vm.eventEdited = function (event) {
        SkiftView.show(event.title, event);
    };

    vm.eventDeleted = function (event) {
    };

    vm.timespanClicked = function (date, cell) {
        if (vm.calendarView === 'month') {
            if (vm.viewDate !== date) {
                vm.viewDate = date;
                vm.cellIsOpen = true;
            } else {
                vm.cellIsOpen = !vm.cellIsOpen;
            }
        }
    };
});