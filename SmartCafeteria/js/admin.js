// =====================================================
// EUSL EATS - ADMIN (login + store approval)
// =====================================================
document.addEventListener("DOMContentLoaded", function () {

    // ---------- admin login ----------
    const loginForm = document.getElementById("adminLoginForm");

    if (loginForm) {
        loginForm.addEventListener("submit", async function (event) {
            event.preventDefault();

            const credentials = {
                email: document.getElementById("adminEmail").value.trim(),
                password: document.getElementById("adminPassword").value
            };

            try {
                const response = await fetch(API_BASE + "/admin/login", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(credentials)
                });

                if (!response.ok) {
                    notify((await response.text()) || "Login failed.", "error");
                    return;
                }

                saveSession("admin", await response.json());
                window.location.href = "admin-dashboard.html";

            } catch (error) {
                console.error("Admin Login Error:", error);
                notify("Could not connect to the server. Please make sure the backend is running.", "error");
            }
        });
    }

    // ---------- admin dashboard ----------
    const list = document.getElementById("adminStoreList");

    if (!list) {
        return;
    }

    if (!getSession("admin")) {
        window.location.href = "admin-login.html";
        return;
    }

    const summary = document.getElementById("adminSummary");
    const filter = document.getElementById("adminStatusFilter");
    let stores = [];

    filter.addEventListener("change", render);
    document.getElementById("adminRefreshBtn").addEventListener("click", load);

    load();

    async function load() {
        list.innerHTML = "<p>Loading stores...</p>";

        try {
            const response = await apiFetch("/admin/stores", {}, "admin");

            if (!response.ok) {
                throw new Error((await response.text()) || "Could not load stores.");
            }

            stores = await response.json();

            // Show the waiting stores first; fall back to all if none wait.
            if (!stores.some(store => store.approvalStatus === "PENDING")
                && filter.value === "PENDING") {
                filter.value = "ALL";
            }

            render();

        } catch (error) {
            if (error.message !== "Session expired") {
                console.error("Load Stores Error:", error);
                list.innerHTML = "<p>" + escapeHtml(error.message) + "</p>";
            }
        }
    }

    function countOf(status) {
        return stores.filter(store => store.approvalStatus === status).length;
    }

    function render() {
        summary.innerHTML = `
            <div class="admin-stat"><strong>${countOf("PENDING")}</strong><span>Waiting</span></div>
            <div class="admin-stat"><strong>${countOf("APPROVED")}</strong><span>Approved</span></div>
            <div class="admin-stat"><strong>${countOf("REJECTED")}</strong><span>Rejected</span></div>
            <div class="admin-stat"><strong>${stores.length}</strong><span>Total</span></div>`;

        const visible = filter.value === "ALL"
            ? stores
            : stores.filter(store => store.approvalStatus === filter.value);

        if (visible.length === 0) {
            list.innerHTML = "<p>No stores in this list.</p>";
            return;
        }

        list.innerHTML = visible.map(store => {
            const status = store.approvalStatus;

            const approve = status !== "APPROVED"
                ? `<button type="button" data-status="APPROVED" data-id="${store.storeId}">✅ Approve</button>`
                : "";

            const reject = status !== "REJECTED"
                ? `<button type="button" class="secondary-button" data-status="REJECTED" data-id="${store.storeId}">${status === "APPROVED" ? "⛔ Revoke" : "❌ Reject"}</button>`
                : "";

            return `
                <article class="order-card">
                    <h3>${escapeHtml(store.storeName)}
                        <span class="status-pill status-${escapeHtml(status.toLowerCase())}">${escapeHtml(status)}</span>
                    </h3>
                    <p class="order-meta">📍 ${escapeHtml(store.location || "No location")}</p>
                    <p>${escapeHtml(store.description || "No description")}</p>
                    <p class="order-meta">
                        Owner: <strong>${escapeHtml(store.ownerName)}</strong>
                        · ${escapeHtml(store.ownerEmail)}
                        · ${escapeHtml(store.ownerPhone || "no phone")}
                    </p>
                    <div class="order-actions">${approve}${reject}</div>
                </article>`;
        }).join("");
    }

    list.addEventListener("click", async function (event) {
        const button = event.target.closest("button[data-status]");

        if (!button) {
            return;
        }

        const status = button.dataset.status;

        if (status === "REJECTED" && !confirm("Are you sure? The store will be hidden from students.")) {
            return;
        }

        button.disabled = true;

        try {
            const response = await apiJson(
                "/admin/stores/" + button.dataset.id + "/status",
                "PUT",
                { status: status },
                "admin"
            );

            if (!response.ok) {
                throw new Error((await response.text()) || "Could not update the store.");
            }

            notify("Store " + status.toLowerCase() + ".", "success");
            await load();

        } catch (error) {
            if (error.message !== "Session expired") {
                notify(error.message, "error");
            }
            button.disabled = false;
        }
    });
});
