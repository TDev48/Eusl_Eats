// =====================================================
// EUSL EATS - SHARED SETTINGS AND HELPERS
// Load this file BEFORE every other script.
// =====================================================

// Where the back end lives.
//  - Opened with VS Code Live Server (port 5500/5501): the back end on your PC.
//  - Anywhere else (the real website): the same address as the site itself,
//    because the web server forwards /api and /uploads to the back end.
// To use a different back end, replace the whole expression with its address,
// for example "https://api.example.com/api".
const API_BASE = (
    ["localhost", "127.0.0.1"].includes(window.location.hostname) &&
    ["5500", "5501"].includes(window.location.port)
) ? "http://localhost:8080/api" : "/api";

// Address that uploaded food photos are loaded from.
const SERVER_ORIGIN = API_BASE.replace(/\/api\/?$/, "") || window.location.origin;

// On the free GitHub Pages preview there is no back end, so say so clearly.
if (window.location.hostname.endsWith(".github.io")) {
    document.addEventListener("DOMContentLoaded", function () {
        const banner = document.createElement("div");
        banner.textContent =
            "Design preview only: the back end is not connected here, so login, " +
            "stores and orders will not work. See the README to run the full app.";
        banner.style.cssText =
            "position:fixed;left:0;right:0;bottom:0;z-index:99999;padding:10px 16px;" +
            "background:#fff8e6;color:#6b4e00;border-top:1px solid #f5d98b;" +
            "font:600 13px Inter,Arial,sans-serif;text-align:center;";
        document.body.appendChild(banner);
    });
}

const SESSION_KEYS = {
    student: "loggedInStudent",
    store: "loggedInStore",
    admin: "loggedInAdmin"
};

const LOGIN_PAGES = {
    student: "login.html",
    store: "store-login.html",
    admin: "admin-login.html"
};

// Returns the saved login for a role, or null if not logged in.
// A login without a token (an old session) counts as not logged in.
function getSession(role) {
    try {
        const session = JSON.parse(
            sessionStorage.getItem(SESSION_KEYS[role]) || "null"
        );
        return session && session.token ? session : null;
    } catch (error) {
        return null;
    }
}

function saveSession(role, data) {
    // Only one kind of account can be logged in at a time.
    Object.keys(SESSION_KEYS).forEach(key => {
        if (key !== role) {
            sessionStorage.removeItem(SESSION_KEYS[key]);
        }
    });
    sessionStorage.setItem(SESSION_KEYS[role], JSON.stringify(data));
}

function currentRole() {
    return ["admin", "store", "student"].find(role => getSession(role)) || null;
}

function clearAllSessions() {
    Object.values(SESSION_KEYS).forEach(key => sessionStorage.removeItem(key));
    localStorage.removeItem("cart");
}

// fetch() for the API. Adds the login token automatically and sends the
// person back to the login page if the token is missing or has expired.
async function apiFetch(path, options = {}, role = currentRole()) {
    const session = role ? getSession(role) : null;
    const headers = Object.assign({}, options.headers);

    if (session) {
        headers["Authorization"] = "Bearer " + session.token;
    }

    const response = await fetch(
        API_BASE + path,
        Object.assign({}, options, { headers })
    );

    if (response.status === 401 && role) {
        sessionStorage.removeItem(SESSION_KEYS[role]);
        alert("Your session has expired. Please log in again.");
        window.location.href = LOGIN_PAGES[role];
        throw new Error("Session expired");
    }

    return response;
}

// Sends JSON with apiFetch.
function apiJson(path, method, body, role) {
    return apiFetch(path, {
        method: method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
    }, role);
}

// ---------- small shared helpers ----------
function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    })[character]);
}

function formatMoney(value) {
    return "LKR " + Number(value || 0).toLocaleString("en-LK", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function formatDate(value) {
    const date = value ? new Date(value) : null;
    return date && !isNaN(date) ? date.toLocaleString() : "Not available";
}

// A short message that fades away (replaces most alert() pop-ups).
function notify(message, type = "info") {
    const toast = document.createElement("div");
    toast.className = "toast toast-" + type;
    toast.setAttribute("role", "status");
    toast.textContent = message;
    document.body.appendChild(toast);

    window.setTimeout(() => toast.classList.add("toast-hide"), 3200);
    window.setTimeout(() => toast.remove(), 3800);
}

// ---------- cart (kept in the browser, one store at a time) ----------
function readCart() {
    try {
        const value = JSON.parse(localStorage.getItem("cart") || "[]");
        return Array.isArray(value) ? value : [];
    } catch (error) {
        return [];
    }
}

function saveCart(cart) {
    localStorage.setItem("cart", JSON.stringify(cart));
    updateCartCount();
}

function updateCartCount() {
    const count = readCart().reduce(
        (total, item) => total + Number(item.quantity || 0), 0
    );
    document.querySelectorAll("[data-cart-count]").forEach(element => {
        element.textContent = String(count);
    });
}

// ---------- unread notification badge (students and stores) ----------
async function refreshNotificationBadge() {
    const role = currentRole();

    if (role !== "student" && role !== "store") {
        return;
    }

    try {
        const response = await apiFetch("/notifications/unread-count", {}, role);

        if (!response.ok) {
            return;
        }

        const data = await response.json();
        const count = Number(data.unread || 0);

        document.querySelectorAll("[data-notification-count]").forEach(element => {
            element.textContent = String(count);
            element.style.display = count > 0 ? "" : "none";
        });
    } catch (error) {
        // The badge is only a convenience; ignore network problems.
    }
}
