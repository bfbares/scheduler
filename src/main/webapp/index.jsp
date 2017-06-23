<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="utf-8">
    <title>Planificador con AG</title>

    <link rel="stylesheet" href="<c:url value="/resources/css/style.css" />" type="text/css">
    <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/fullcalendar/3.1.0/fullcalendar.min.css" type="text/css">
    <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/fullcalendar/3.1.0/fullcalendar.print.css" type="text/css" media='print'>
    <link rel="stylesheet" href="<c:url value="/resources/css/scheduler.min.css" />" type="text/css">

    <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/moment.js/2.9.0/moment.min.js"></script>
    <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
    <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/fullcalendar/3.1.0/fullcalendar.min.js"></script>
    <script type="text/javascript" src="//cdn.jsdelivr.net/sockjs/1/sockjs.min.js"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/lib/stomp.min.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/lib/scheduler.min.js" />"></script>
</head>

<body>
    <div>
        <p id="show-form" class="btn pull-right">Mostrar formulario de cita</p>
        <p id="clean" class="btn pull-right">Limpiar</p>
        <div style="clear: both"></div>
        <div id="form" style="display: none">
            <table>
                <thead>
                <tr>
                    <th><input id="select-all" type="checkbox"></th>
                    <th>Nombre</th>
                    <th>Duracion (m.)</th>
                </tr>
                </thead>
                <tbody id="table-body">

                </tbody>
            </table>
            <p id="operations-empty" style="display: none" class="error">Debe seleccionar por lo menos una operacion</p>
            <p id="events-empty" style="display: none" class="error">Debe marcar al menos un hueco como reservado haciendo una seleccion sobre el calendario</p>
            <p id="send-form" class="btn pull-right">Planificar</p>
            <div style="clear: both"></div>
        </div>
    </div>
    <div id="calendar">
    </div>

    <script type="text/javascript" src="<c:url value="/resources/js/index.js" />"></script>
</body>
</html>