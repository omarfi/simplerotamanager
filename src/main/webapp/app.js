var SRMApp = angular
    .module('simplerotamanager', ['mwl.calendar', 'ngAnimate', 'ui.bootstrap'])
    .config(['calendarConfig', function (calendarConfig) {
        calendarConfig.templates.calendarSlideBox = '/templates/calendarSlidebox.html';
    }]);
