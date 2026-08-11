document.addEventListener("DOMContentLoaded", function () {

    const filter =
        document.getElementById("statusFilter");

    const rows =
        document.querySelectorAll("#ecrTable tbody tr");

    filter.addEventListener("change", function () {

        const selectedStatus = filter.value;

        rows.forEach(function (row) {

            const rowStatus =
                row.getAttribute("data-status");

            if (
                selectedStatus === "ALL" ||
                rowStatus === selectedStatus
            ) {

                row.style.display = "";

            } else {

                row.style.display = "none";
            }
        });
    });
});