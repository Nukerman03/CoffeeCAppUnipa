document.addEventListener("DOMContentLoaded", function() {

    const machineIdElement = document.getElementById('machineId');
    if (!machineIdElement) return;

    const machineId = machineIdElement.innerText;
    const POLLING_INTERVAL = 2000;
    const HEARTBEAT_INTERVAL = 60000;
    const MONITORING_URL = "http://localhost:8081/monitoring/heartbeat";
    
    let currentLocation = null;

    function pollBackend() {
        fetch(`/api/machine/${machineId}/poll`)
            .then(res => {
                if (!res.ok) throw new Error("Backend Error");
                return res.json();
            })
            .then(data => updateUI(data))
            .catch(err => console.error("Polling error:", err));
    }

    function updateUI(data) {
        const header = document.getElementById('HeaderName');
        const badge = document.getElementById('Balance');
        const status = document.getElementById('ConnectionStatus');
        const panel = document.getElementById('productPanel');
        
        if (data.location) {
            currentLocation = data.location;
        }

        console.log("Stato connessione:", data.status);

        if (data.status === 'CONNECTED') {
            if(header) header.innerText = `Benvenuto, ${data.username}`;
            if(badge) badge.innerText = `€ ${data.userCredit.toFixed(2)}`;
            if(status) {
                status.innerText = "CONNESSO";
                status.className = "status-badge status-connected";
            }
            if(panel) panel.classList.remove('disabled');
        } else {
            if(header) header.innerText = "In attesa di cliente...";
            if(badge) badge.innerText = "€ 0,00";
            if(status) {
                status.innerText = "DISCONNESSO";
                status.className = "status-badge status-idle";
            }
            if(panel) panel.classList.add('disabled');
        }
    }

    function sendHeartbeat() {
        if (!currentLocation) return;

        const payload = {
            locationLabel: currentLocation,
            status: "ATTIVA"
        };

        fetch(MONITORING_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        })
        .then(response => {
            if (!response.ok) {
                console.warn(`Heartbeat failed with status: ${response.status}`);
            }
        })
        .catch(err => {
            console.warn("Heartbeat connection error (Monitoring server might be down):", err.message);
        });
    }

    const buyForm = document.getElementById('buyForm');
    if (buyForm) {
        buyForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const bevanda = document.getElementById('bevanda').value;
            const zucchero = document.getElementById('zucchero').value;

            fetch(`/api/machine/${machineId}/buy`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    product: bevanda,
                    sugar: zucchero
                })
            })
            .then(async response => {
                const text = await response.text();
                if (response.ok) {
                    alert("Erogazione in corso... Prelevare il prodotto!");
                } else {
                    alert("Errore: " + text);
                }
            })
            .catch(err => {
                alert("Errore di comunicazione con il server");
                console.error(err);
            });
        });
    }

    setInterval(pollBackend, POLLING_INTERVAL);
    setInterval(sendHeartbeat, HEARTBEAT_INTERVAL);
    
    pollBackend();
    sendHeartbeat();
});