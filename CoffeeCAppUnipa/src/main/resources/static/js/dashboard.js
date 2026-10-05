document.addEventListener("DOMContentLoaded", function() {
    const urlParams = new URLSearchParams(window.location.search);
    const success = urlParams.get('success');
    const error = urlParams.get('error');

    if (success) {
        const alertBox = document.createElement('div');
        alertBox.style.backgroundColor = '#d4edda';
        alertBox.style.color = '#155724';
        alertBox.style.padding = '10px';
        alertBox.style.marginBottom = '15px';
        alertBox.style.borderRadius = '5px';
        alertBox.style.border = '1px solid #c3e6cb';
        alertBox.textContent = success;
        document.querySelector('main').prepend(alertBox);
        window.history.replaceState({}, document.title, window.location.pathname);
    }

    if (error) {
        const alertBox = document.createElement('div');
        alertBox.style.backgroundColor = '#f8d7da';
        alertBox.style.color = '#721c24';
        alertBox.style.padding = '10px';
        alertBox.style.marginBottom = '15px';
        alertBox.style.borderRadius = '5px';
        alertBox.style.border = '1px solid #f5c6cb';
        alertBox.textContent = error;
        document.querySelector('main').prepend(alertBox);
        window.history.replaceState({}, document.title, window.location.pathname);
    }

    // Mappa Leaflet
    var map = L.map('map').setView([38.1157, 13.3615], 13); // Palermo
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(map);

    var marker;

    map.on('click', function(e) {
        var lat = e.latlng.lat;
        var lng = e.latlng.lng;

        if (marker) {
            marker.setLatLng(e.latlng);
        } else {
            marker = L.marker(e.latlng).addTo(map);
        }

        document.getElementById('lat').value = lat;
        document.getElementById('lon').value = lng;
    });
});