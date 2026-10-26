// =====================================================
// EUSL EATS - SHARED NAVIGATION AND LOGOUT
// Builds the header menu for whoever is logged in, so every page
// shows the same correct links. Load after config.js, before script.js.
// =====================================================
(function () {

    const MENUS = {
        guest: [
            ["Home", "index.html"],
            ["Browse Food", "student-order.html"],
            ["Browse Stores", "stores.html"],
            ["Student Login", "login.html"],
            ["Store Login", "store-login.html"]
        ],
        student: [
            ["Home", "index.html"],
            ["Dashboard", "student-dashboard.html"],
            ["Stores", "stores.html"],
            ["Food", "student-order.html"],
            ["My Orders", "student-orders.html"],
            ["Cart", "cart.html", "cart"],
            ["Notifications", "student-notifications.html", "notifications"]
        ],
        store: [
            ["Home", "index.html"],
            ["Dashboard", "store-dashboard.html"],
            ["Menu", "store-menu.html"],
            ["Orders", "store-orders.html"],
            ["Notifications", "store-notifications.html", "notifications"]
        ],
        admin: [
            ["Dashboard", "admin-dashboard.html"]
        ]
    };

    function buildLayout() {
        const role = currentRole();
        const header = document.querySelector("header");

        if (!header) {
            return;
        }

        const nav = header.querySelector("nav");
        const page = window.location.pathname.split("/").pop() || "index.html";

        if (nav) {
            nav.innerHTML = "";

            MENUS[role || "guest"].forEach(item => {
                const link = document.createElement("a");
                link.href = item[1];
                link.textContent = item[0];

                if (item[2] === "cart") {
                    const badge = document.createElement("span");
                    badge.className = "cart-count";
                    badge.setAttribute("data-cart-count", "");
                    badge.textContent = "0";
                    link.append(" ", badge);
                }

                if (item[2] === "notifications") {
                    const badge = document.createElement("span");
                    badge.className = "notification-count";
                    badge.setAttribute("data-notification-count", "");
                    badge.setAttribute("aria-label", "unread notifications");
                    badge.style.display = "none";
                    badge.textContent = "0";
                    link.append(" ", badge);
                }

                if (item[1] === page) {
                    link.setAttribute("aria-current", "page");
                }

                nav.appendChild(link);
            });
        }

        // Every logged-in page gets exactly one logout button.
        const logoutSelector = "#logoutBtn, #storeLogoutBtn, #adminLogoutBtn";
        let logout = header.querySelector(logoutSelector);

        if (role && !logout) {
            logout = document.createElement("button");
            logout.type = "button";
            logout.className = "login-btn";
            logout.textContent = "Logout";
            header.appendChild(logout);
        }

        if (logout) {
            logout.id = "logoutBtn";
            logout.type = "button";
            logout.style.display = role ? "" : "none";

            logout.addEventListener("click", function () {
                clearAllSessions();
                window.location.replace(LOGIN_PAGES[role] || "login.html");
            });
        }

        updateCartCount();
        refreshNotificationBadge();
    }

    // Keep the unread count fresh while a page stays open.
    window.setInterval(function () {
        if (!document.hidden) {
            refreshNotificationBadge();
        }
    }, 60000);

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", buildLayout);
    } else {
        buildLayout();
    }
})();
