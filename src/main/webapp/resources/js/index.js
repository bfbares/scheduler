$(document).ready(function () {
    new Scheduler();
});

function Scheduler() {
    var _th = this;
    this.calendar = $('#calendar');
    this.tableBody = $('#table-body');
    this.operationsEmpty = $('#operations-empty');
    this.eventsEmpty = $('#events-empty');
    this.form = $('#form');

    $('#show-form').click(function () {
        _th.form.slideToggle('slow');
    });

    $('#select-all').change(function () {
        var headInput = $('thead').find('input');

        _th.tableBody.find('input').each(function () {
            $(this).prop('checked', headInput.is(':checked'));
        });
    });

    $('#clean').click(function () {
        _th.clear();
    });

    $('#send-form').click(_th.sendForm.bind(this));
    $.get('init', function (data) {
        _th.initForm(data.operations);
        _th.initCalendar(data.units, data.events);
        _th.initWebSockets();
    })
}

Scheduler.prototype.initForm = function (operations) {
    var tableBody = $('#table-body');
    $.each(operations, function (i, operation) {
        tableBody.append('<tr><td><input type="checkbox" value="' + operation.id + '"></td><td>' + operation.name + '</td><td>' + operation.duration / 60000 + '</td></tr>')
    });
};

Scheduler.prototype.initCalendar = function (units, events) {
    var _th = this;

    this.calendar.fullCalendar({
        schedulerLicenseKey: 'GPL-My-Project-Is-Open-Source',
        defaultView: 'agendaDay',
        editable: true,
        selectable: true,
        eventLimit: true,
        allDaySlot: false,
        header: {
            left: '',
            center: 'title',
            right: ''
        },
        resources: units,
        events: [],
        select: _th.onSelect.bind(_th),
        eventResize: _th.onChange.bind(_th),
        eventDrop: _th.onChange.bind(_th)
    });

    $.each(events, function (i, event) {
        _th.addEvent(event);
    });
};

Scheduler.prototype.onChange = function (event, delta, revertFunc) {
    this.stompClient.send("/app/add", {}, JSON.stringify(event));
};

Scheduler.prototype.onSelect = function (start, end, jsEvent, view, resource) {
    var uid = moment().valueOf();
    var event = {
        id: uid,
        title: 'Reservado',
        start: start,
        end: end,
        type: 1,
        resourceId: resource.id,
        uid: uid
    };
    this.calendar.fullCalendar('renderEvent', event);

    this.stompClient.send("/app/add", {}, JSON.stringify(event));
};

Scheduler.prototype.initWebSockets = function () {
    var _th = this;
    var socket = new SockJS('/scheduler/scheduler');
    _th.stompClient = Stomp.over(socket);
    _th.stompClient.connect({}, function (frame) {
        _th.stompClient.subscribe('/response/add', function (messageOutput) {
            var events = JSON.parse(messageOutput.body);
            events.forEach(function (newEvent) {
                var oldEvent = _th.calendar.fullCalendar('clientEvents', function (event) {
                    return event.uid === newEvent.id
                });
                if (oldEvent.length === 0) {
                    _th.addEvent(newEvent);
                } else {
                    _th.modifyEvent(oldEvent[0], newEvent);
                }
            });
        });
        _th.stompClient.subscribe('/response/clear', function (messageOutput) {
            _th.calendar.fullCalendar('removeEvents');
        });
    });
};

Scheduler.prototype.modifyEvent = function (oldEvent, newEvent) {
    oldEvent.start = $.fullCalendar.moment.parseZone(newEvent.start);
    oldEvent.end = $.fullCalendar.moment.parseZone(newEvent.end);
    oldEvent.color = newEvent.type == 1 ? 'grey' : 'blue';
    oldEvent.resourceId = newEvent.resourceId.toString();
    this.calendar.fullCalendar('updateEvent', oldEvent);
};

Scheduler.prototype.addEvent = function (event) {
    event.start = $.fullCalendar.moment.parseZone(event.start);
    event.end = $.fullCalendar.moment.parseZone(event.end);
    event.color = event.type == 1 ? 'grey' : 'blue';
    event.editable = event.type == 1;
    event.resourceId = event.resourceId.toString();
    event.uid = Number(event.id);
    delete event.id;
    this.calendar.fullCalendar('renderEvent', event);
};

Scheduler.prototype.sendForm = function () {
    var _th = this;
    var errors = false;
    var checks = this.tableBody.find('input:checked');
    var operations = [];

    if (checks.length == 0) {
        _th.operationsEmpty.slideDown('slow');
        errors = true;
    } else {
        checks.each(function () {
            operations.push($(this).val());
        });
        _th.operationsEmpty.slideUp('slow');
    }

    if (_th.calendar.fullCalendar('clientEvents').length === 0) {
        _th.eventsEmpty.slideDown('slow');
        errors = true;
    } else {
        _th.eventsEmpty.slideUp('slow');
    }

    if (!errors) {
        var scheduleRequest = {
            operations: operations,
            day: _th.calendar.fullCalendar('getDate').toISOString()
        };
        _th.stompClient.send("/app/schedule", {}, JSON.stringify(scheduleRequest));
        _th.form.slideUp('slow');
    }
};

Scheduler.prototype.clear = function () {
    this.stompClient.send('/app/clear', {}, {});
};