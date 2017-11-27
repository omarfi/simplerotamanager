var SRMApp = angular
    .module('simplerotamanager', ['mwl.calendar', 'ngAnimate', 'ui.bootstrap', 'ngTouch'])
    .config(['calendarConfig', function (calendarConfig, $touchProvider) {
        calendarConfig.dateFormatter = 'moment';
        calendarConfig.templates.calendarSlideBox = '/templates/calendarSlidebox.html';
        calendarConfig.i18nStrings.weekNumber = 'Uke {week}';
        calendarConfig.allDateFormats.moment.date.hour = 'HH:mm';

        moment.locale('nb', {
            week: {
                dow: 1
            }
        });
        moment.locale('nb');

    }])
    .config(['$touchProvider', function ($touchProvider) {
        $touchProvider.ngClickOverrideEnabled(true);
    }]);
