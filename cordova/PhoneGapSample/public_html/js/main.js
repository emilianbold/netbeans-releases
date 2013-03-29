var map;
var marker;
var infoWindow;
var watchID;

$(document).ready(function() {
    document.addEventListener("deviceready", onDeviceReady, false);
    //for testing in Chrome browser uncomment
    //onDeviceReady();
});

function onDeviceReady() {
    $(window).unbind();
    $(window).bind('pageshow resize orientationchange', function(e) {
        maxHeight();
    });
    maxHeight();
    google.load("maps", "3.8", {"callback": map, other_params: "sensor=true&language=en"});
}

function maxHeight() {
    var h = $('div[data-role="header"]').outerHeight(true);
    var f = $('div[data-role="footer"]').outerHeight(true);
    var w = $(window).height();
    var c = $('div[data-role="content"]');
    var c_h = c.height();
    var c_oh = c.outerHeight(true);
    var c_new = w - h - f - c_oh + c_h;
    var total = h + f + c_oh;
    if (c_h < c.get(0).scrollHeight) {
        c.height(c.get(0).scrollHeight);
    } else {
        c.height(c_new);
    }
}

function map() {
    var latlng = new google.maps.LatLng(55.17, 23.76);
    var myOptions = {
        zoom: 6,
        center: latlng,
        streetViewControl: true,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        zoomControl: true
    };
    map = new google.maps.Map(document.getElementById("map"), myOptions);

    google.maps.event.addListenerOnce(map, 'tilesloaded', function() {
        watchID = navigator.geolocation.watchPosition(geo_success, geo_error, {maximumAge: 10000, timeout: 10000, enableHighAccuracy: true});
    });
}

function geo_error(error) {
    $('h1[id="header"]').html("Searching for GPS");
}

function geo_success(position) {
    $('h1[id="header"]').html("You Are Here");
    map.setCenter(new google.maps.LatLng(position.coords.latitude, position.coords.longitude));
    map.setZoom(15);

    var info =
            ('Latitude: ' + position.coords.latitude + '<br>' +
                    'Longitude: ' + position.coords.longitude + '<br>' +
                    'Altitude: ' + position.coords.altitude + '<br>' +
                    'Accuracy: ' + position.coords.accuracy + '<br>' +
                    'Altitude Accuracy: ' + position.coords.altitudeAccuracy + '<br>' +
                    'Heading: ' + position.coords.heading + '<br>' +
                    'Speed: ' + position.coords.speed + '<br>' +
                    'Timestamp: ' + new Date(position.timestamp));

    var point = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
    if (!marker) {
        marker = new google.maps.Marker({
            position: point,
            map: map
        });
    } else {
        marker.setPosition(point);
    }
    if (!infoWindow) {
        infoWindow = new google.maps.InfoWindow({
            content: info
        });
    } else {
        infoWindow.setContent(info);
    }
    google.maps.event.addListener(marker, 'click', function() {
        infoWindow.open(map, marker);
    });
}
