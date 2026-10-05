document.addEventListener("DOMContentLoaded", function() {
    const form = document.querySelector("form");
    if (form) {
        form.addEventListener("submit", function(e){
            const p1 = document.getElementById("password").value;
            const p2 = document.getElementById("password-check").value;
            if(p1 !== p2){
                e.preventDefault();
                alert("Le password non coincidono!");
            }
        });
    }
});