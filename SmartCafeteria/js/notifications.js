// =====================================================
// EUSL EATS - NOTIFICATIONS PAGE (students and stores)
// =====================================================

document.addEventListener("DOMContentLoaded", function () {

    const list = document.getElementById("notificationsList");

    if (!list) {
        return;
    }

    const role = list.dataset.requiredRole;

    if (!getSession(role)) {
        window.location.href = LOGIN_PAGES[role];
        return;
    }

    const markAllButton = document.getElementById("markAllReadBtn");
    const refreshButton = document.getElementById("refreshNotificationsBtn");

    if (!markAllButton || !refreshButton) {
        console.error("Notification page controls are missing.");
        return;
    }

    // =====================================================
    // SELECT ALL NOTIFICATIONS
    // =====================================================

    const selectAllLabel = document.createElement("label");

    selectAllLabel.style.display = "inline-flex";
    selectAllLabel.style.alignItems = "center";
    selectAllLabel.style.gap = "6px";
    selectAllLabel.style.marginRight = "12px";

    const selectAllCheckbox = document.createElement("input");

    selectAllCheckbox.type = "checkbox";
    selectAllCheckbox.id = "selectAllNotifications";

    selectAllLabel.appendChild(selectAllCheckbox);
    selectAllLabel.appendChild(
        document.createTextNode("Select All")
    );

    markAllButton.parentElement.insertBefore(
        selectAllLabel,
        markAllButton
    );

    // =====================================================
    // CLEAR SELECTED NOTIFICATIONS BUTTON
    // =====================================================

    const clearSelectedButton = document.createElement("button");

    clearSelectedButton.type = "button";
    clearSelectedButton.id = "clearSelectedNotificationsBtn";
    clearSelectedButton.className = "secondary-button";
    clearSelectedButton.textContent = "🗑 Clear Selected";
    clearSelectedButton.disabled = true;

    markAllButton.parentElement.appendChild(
        clearSelectedButton
    );

    // =====================================================
    // STATE
    // =====================================================

    let notifications = [];

    const selectedNotificationIds = new Set();

    // =====================================================
    // SELECT ALL CHECKBOX
    // =====================================================

    selectAllCheckbox.addEventListener("change", function () {

        const checkboxes = list.querySelectorAll(
            ".notification-select"
        );

        checkboxes.forEach(checkbox => {

            checkbox.checked = selectAllCheckbox.checked;

            const id = Number(checkbox.dataset.id);

            if (checkbox.checked) {
                selectedNotificationIds.add(id);
            } else {
                selectedNotificationIds.delete(id);
            }
        });

        updateClearButton();
    });

    // =====================================================
    // REFRESH NOTIFICATIONS
    // =====================================================

    refreshButton.addEventListener("click", function () {
        load();
    });

    // =====================================================
    // MARK ALL NOTIFICATIONS AS READ
    // =====================================================

    markAllButton.addEventListener("click", async function () {

        markAllButton.disabled = true;

        try {

            const response = await apiFetch(
                "/notifications/read-all",
                { method: "PUT" },
                role
            );

            if (!response.ok) {
                throw new Error(
                    "Could not mark notifications as read."
                );
            }

            notifications.forEach(item => {
                item.read = true;
            });

            render();
            refreshNotificationBadge();

        } catch (error) {

            if (error.message !== "Session expired") {
                notify(error.message, "error");
            }

        } finally {
            markAllButton.disabled =
                !notifications.some(item => !item.read);
        }
    });

    // =====================================================
    // CLEAR SELECTED NOTIFICATIONS
    // =====================================================

    clearSelectedButton.addEventListener(
        "click",
        async function () {

            const selectedIds = [
                ...selectedNotificationIds
            ];

            if (selectedIds.length === 0) {
                notify(
                    "Please select at least one notification.",
                    "error"
                );
                return;
            }

            if (!confirm(
                "Are you sure that you want to permanently delete all selected notifications?"
            )) {
                return;
            }

            clearSelectedButton.disabled = true;

            try {

                const response = await apiFetch(
                    "/notifications/selected",
                    {
                        method: "DELETE",
                        headers: {
                            "Content-Type": "application/json"
                        },
                        body: JSON.stringify(selectedIds)
                    },
                    role
                );

                if (!response.ok) {

                    const details = await response.text();

                    throw new Error(
                        "Delete failed (HTTP " +
                        response.status +
                        "): " +
                        (details || response.statusText)
                    );
                }

                const result = await response.json();

                selectedNotificationIds.clear();

                notify(
                    (result.deleted ?? selectedIds.length) +
                    " notification(s) permanently deleted.",
                    "success"
                );

                await load();

                refreshNotificationBadge();

            } catch (error) {

                if (error.message !== "Session expired") {
                    notify(error.message, "error");
                }

            } finally {
                updateClearButton();
            }
        }
    );

    // =====================================================
    // UPDATE SELECTION CONTROLS
    // =====================================================

    function updateClearButton() {

        clearSelectedButton.disabled =
            selectedNotificationIds.size === 0;

        const checkboxes = list.querySelectorAll(
            ".notification-select"
        );

        const checkedCount = [
            ...checkboxes
        ].filter(checkbox => checkbox.checked).length;

        selectAllCheckbox.checked =
            checkboxes.length > 0 &&
            checkedCount === checkboxes.length;

        selectAllCheckbox.indeterminate =
            checkedCount > 0 &&
            checkedCount < checkboxes.length;
    }

    // =====================================================
    // INDIVIDUAL NOTIFICATION SELECTION
    // =====================================================

    list.addEventListener("change", function (event) {

        const checkbox = event.target.closest(
            ".notification-select"
        );

        if (!checkbox) {
            return;
        }

        const id = Number(checkbox.dataset.id);

        if (checkbox.checked) {
            selectedNotificationIds.add(id);
        } else {
            selectedNotificationIds.delete(id);
        }

        updateClearButton();
    });

    // =====================================================
    // AUTOMATIC REFRESH
    // =====================================================

    window.setInterval(function () {

        if (!document.hidden) {
            load(true);
        }

    }, 30000);

    // =====================================================
    // LOAD NOTIFICATIONS
    // =====================================================

    async function load(quiet) {

        if (quiet !== true) {
            list.innerHTML =
                "<p>Loading notifications...</p>";
        }

        try {

            const response = await apiFetch(
                "/notifications",
                {},
                role
            );

            if (!response.ok) {
                throw new Error(
                    "Could not load notifications."
                );
            }

            notifications = await response.json();

            render();

            refreshNotificationBadge();

        } catch (error) {

            if (
                error.message === "Session expired" ||
                quiet === true
            ) {
                return;
            }

            list.innerHTML =
                '<div class="error-state">' +
                    '<p>' +
                        escapeHtml(error.message) +
                    '</p>' +
                    '<button type="button" id="retryNotifications">' +
                        'Try again' +
                    '</button>' +
                '</div>';

            document
                .getElementById("retryNotifications")
                .addEventListener("click", function () {
                    load();
                });
        }
    }

    // =====================================================
    // RENDER NOTIFICATIONS
    // =====================================================

    function render() {

        const unread = notifications.filter(
            item => !item.read
        ).length;

        markAllButton.disabled = unread === 0;

        // Remove selections for notifications that no longer exist.
        const existingIds = new Set(
            notifications.map(
                item => Number(item.notificationId)
            )
        );

        selectedNotificationIds.forEach(id => {

            if (!existingIds.has(Number(id))) {
                selectedNotificationIds.delete(id);
            }
        });

        if (notifications.length === 0) {

            list.innerHTML =
                '<div class="empty-state">' +
                    '<h3>No notifications yet</h3>' +
                    '<p>' +
                        (
                            role === "store"
                                ? "New orders and cancellations will appear here."
                                : "Updates about your orders will appear here."
                        ) +
                    '</p>' +
                '</div>';

            updateClearButton();

            return;
        }

        list.innerHTML = notifications.map(item => {

            // IMPORTANT:
            // orderId is the real database ID.
            // It must remain unchanged for links and API operations.
            const orderId = item.orderId;

            // Display the persistent store-assigned order number.
            // Fall back to orderId for older API responses.
            const displayOrderNumber =
                item.displayOrderNumber ?? orderId;

            const target = role === "store"
                ? "store-orders.html#order-" + orderId
                : "student-orders.html";

            const viewLink = orderId != null
                ? (
                    '<a class="notification-link" href="' +
                    escapeHtml(target) +
                    '" data-id="' +
                    escapeHtml(item.notificationId) +
                    '">View order</a>'
                )
                : "";

            const readButton = item.read
                ? ""
                : (
                    '<button type="button" ' +
                    'class="secondary-button notification-read-btn" ' +
                    'data-id="' +
                    escapeHtml(item.notificationId) +
                    '">' +
                    'Mark as read' +
                    '</button>'
                );

            const isSelected = selectedNotificationIds.has(
                Number(item.notificationId)
            );

            const orderMetadata = orderId != null
                ? (
                    " · Order #" +
                    escapeHtml(displayOrderNumber)
                )
                : "";

            return `
                <article class="notification-item ${
                    item.read ? "" : "notification-unread"
                }">

                    <label class="notification-select-label">
                        <input
                            type="checkbox"
                            class="notification-select"
                            data-id="${escapeHtml(item.notificationId)}"
                            aria-label="Select notification"
                            ${isSelected ? "checked" : ""}
                        >
                    </label>

                    <div class="notification-body">

                        <p class="notification-message">
                            ${
                                item.read
                                    ? ""
                                    : '<span class="notification-new">New</span> '
                            }${escapeHtml(item.message)}
                        </p>

                        <p class="notification-time">${
                            escapeHtml(formatDate(item.createdAt))
                        }${orderMetadata}</p>

                    </div>

                    <div class="notification-actions">
                        ${viewLink}
                        ${readButton}
                    </div>

                </article>
            `;

        }).join("");

        updateClearButton();
    }

    // =====================================================
    // MARK A SINGLE NOTIFICATION AS READ
    // =====================================================

    async function markRead(id, keepalive) {

        const response = await apiFetch(
            "/notifications/" + id + "/read",
            {
                method: "PUT",
                keepalive: keepalive === true
            },
            role
        );

        if (!response.ok) {
            throw new Error(
                "Could not mark the notification as read."
            );
        }

        const item = notifications.find(
            entry =>
                Number(entry.notificationId) === Number(id)
        );

        if (item) {
            item.read = true;
        }
    }

    // =====================================================
    // NOTIFICATION ACTIONS
    // =====================================================

    list.addEventListener("click", async function (event) {

        const readButton = event.target.closest(
            ".notification-read-btn"
        );

        const viewLink = event.target.closest(
            ".notification-link"
        );

        // Mark an individual notification as read.
        if (readButton) {

            readButton.disabled = true;

            try {

                await markRead(readButton.dataset.id);

                render();

                refreshNotificationBadge();

            } catch (error) {

                if (error.message !== "Session expired") {
                    notify(error.message, "error");
                }

                readButton.disabled = false;
            }
        }

        // Opening an order also marks its notification as read.
        // Keep the actual notification ID for this API request.
        if (viewLink) {

            markRead(
                viewLink.dataset.id,
                true
            ).catch(function () { });
        }
    });

    // =====================================================
    // INITIAL LOAD
    // =====================================================

    load();

});