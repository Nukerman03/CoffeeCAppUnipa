document.addEventListener("DOMContentLoaded", function() {
    const urlParams = new URLSearchParams(window.location.search);
    const msg = urlParams.get('msg');
    if (msg) {
        alert(msg);
        window.history.replaceState({}, document.title, window.location.pathname);
    }

    function pollUserCredit() {
        fetch('/api/user/status')
            .then(response => {
                if (response.ok) return response.json();
                throw new Error('Network response was not ok');
            })
            .then(data => {
                const creditSpan = document.getElementById('userCredit');
                if (creditSpan) {
                    // Formatta come valuta italiana (es. 4,50 €)
                    creditSpan.innerText = new Intl.NumberFormat('it-IT', { style: 'currency', currency: 'EUR' }).format(data.credit);
                }
            })
            .catch(error => console.error('Polling error:', error));
    }

    // Polling ogni 3 secondi
    setInterval(pollUserCredit, 3000);
});