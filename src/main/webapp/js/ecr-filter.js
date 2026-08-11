document.addEventListener("DOMContentLoaded", function () {

    const statusFilter = document.getElementById("statusFilter");

    const rows = document.querySelectorAll(".ecr-row");


    statusFilter.addEventListener("change", function () {

        const selectedStatus = statusFilter.value;


        rows.forEach(function (row) {

            const rowStatus = row.getAttribute("data-status");


            if (selectedStatus === "ALL" ||
                rowStatus === selectedStatus) {

                row.style.display = "";

            } else {

                row.style.display = "none";

            }

        });

    });

});