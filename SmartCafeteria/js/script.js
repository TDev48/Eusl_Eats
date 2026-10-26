

document.addEventListener("DOMContentLoaded", function () {

    
    // ==============================
    // HOMEPAGE FOOD SEARCH
    // ==============================

    const homeSearchInput = document.getElementById("homeFoodSearch");
    const homeSearchButton = document.getElementById("homeSearchButton");

    function goToFoodSearch() {
        if (!homeSearchInput) return;

        const searchTerm = homeSearchInput.value.trim();

        window.location.href =
            "student-order.html?search=" +
            encodeURIComponent(searchTerm);
    }

    if (homeSearchButton && homeSearchInput) {
        homeSearchButton.addEventListener("click", goToFoodSearch);

        homeSearchInput.addEventListener("keydown", function (event) {
            if (event.key === "Enter") {
                event.preventDefault();
                goToFoodSearch();
            }
        });
    }



// ==============================
// STUDENT REGISTRATION
// ==============================

const registerForm = document.getElementById("registerForm");

if (registerForm) {

    registerForm.addEventListener("submit", async function (event) {

        event.preventDefault();

        const universityId =
            document.getElementById("studentID").value.trim();

        const fullName =
            document.getElementById("fullName").value.trim();

        const email =
            document.getElementById("registerEmail").value.trim();

        const phone =
            document.getElementById("phone").value.trim();

        const faculty =
            document.getElementById("faculty").value;

        const password =
            document.getElementById("registerPassword").value;

        const confirmPassword =
            document.getElementById("confirmPassword").value;

        if (password !== confirmPassword) {

            alert("Passwords do not match!");
            return;

        }

        const studentData = {

            fullName: fullName,
            email: email,
            password: password,
            phone: phone,
            universityId: universityId,
            faculty: faculty

        };

        try {

            const response = await fetch(
                API_BASE + "/students/register",
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify(studentData)
                }
            );

            const result = await response.text();

            if (response.ok) {

                alert("Student registered successfully!");

                registerForm.reset();

                window.location.href = "login.html";

            } else {

                alert("Registration failed: " + result);

            }

        } catch (error) {

            console.error("Registration Error:", error);

            alert(
                "Could not connect to the server. " +
                "Please make sure the Spring Boot backend is running."
            );

        }

    });

}


// ==============================
// STUDENT LOGIN
// ==============================

const loginForm = document.getElementById("loginForm");

if (loginForm) {

    loginForm.addEventListener("submit", async function (event) {

        event.preventDefault();

        const email =
            document.getElementById("email").value.trim();

        const password =
            document.getElementById("password").value;

        const loginData = {

            email: email,
            password: password

        };

        try {

            const response = await fetch(
                API_BASE + "/students/login",
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify(loginData)
                }
            );

            const result = await response.text();

            if (response.ok) {

    const student = JSON.parse(result);

    saveSession("student", student);
                    window.location.href = "student-dashboard.html";

} else {

                alert("Login failed: " + result);

            }

        } catch (error) {

            console.error("Login Error:", error);

            alert(
                "Could not connect to the server. " +
                "Please make sure the Spring Boot backend is running."
            );

        }

    });

}
// ==============================
// STUDENT DASHBOARD
// ==============================

function setText(id, value) {
    const element = document.getElementById(id);

    if (element) {
        element.textContent = value ?? "";
    }
}

if (window.location.pathname.includes("student-dashboard.html")) {

    const student = getSession("student");

    if (!student) {
        window.location.href = "login.html";
        return;
    }

    setText("welcomeName", student.fullName || "Student");

    apiFetch("/students/me", {}, "student")
        .then(response => {
            if (!response.ok) {
                throw new Error("Could not load student profile");
            }
            return response.json();
        })
        .then(profile => {
            setText("studentId", profile.universityId);
            setText("studentName", profile.fullName);
            setText("studentEmail", profile.email);
            setText("studentPhone", profile.phone);
            setText("studentFaculty", profile.faculty);
            setText("welcomeName", profile.fullName);
        })
        .catch(error => {
            console.error("Profile Error:", error);
            ["studentId", "studentName", "studentEmail",
                "studentPhone", "studentFaculty"]
                .forEach(id => setText(id, "Unable to load"));
        });

    loadCurrentOrder(student.studentId);
}

async function loadCurrentOrder(studentId) {
    const currentOrderElement = document.getElementById("currentOrder");

    if (!currentOrderElement) return;

    currentOrderElement.textContent = "Loading your latest order...";

    try {
        const response = await apiFetch(
            "/orders/student/" + encodeURIComponent(studentId),
            {},
            "student"
        );

        if (!response.ok) {
            throw new Error("Could not load orders");
        }

        const orders = await response.json();

        if (!Array.isArray(orders) || orders.length === 0) {
            currentOrderElement.textContent =
                "You don't have any orders yet. Browse the food menu to get started.";
            return;
        }

        // The server returns the newest order first. Cancelled orders are
        // not "current" any more.
        const latestOrder = orders.find(order => order.status !== "Cancelled");

        if (!latestOrder) {
            currentOrderElement.textContent =
                "You have no active orders. Browse the food menu to order something.";
            return;
        }

        currentOrderElement.textContent =
            "Order #" + (
    latestOrder.displayOrderNumber ?? latestOrder.orderId
) +
            " from " + latestOrder.storeName +
            " — " + latestOrder.status;

        const ordersLink = document.createElement("a");
        ordersLink.href = "student-orders.html";
        ordersLink.textContent = " View all orders";
        currentOrderElement.appendChild(ordersLink);

    } catch (error) {
        console.error("Current Order Error:", error);

        currentOrderElement.textContent =
            "Unable to load your latest order. Please visit My Orders to check.";
    }
}

// ==============================
// HOME PAGE SIDEBAR (menus depend on who is logged in)
// ==============================
const sidebar = document.getElementById("appSidebar");
const sidebarToggle = document.getElementById("sidebarToggle");
const userRoleLabel = document.getElementById("userRoleLabel");
const activeRole = currentRole();

[
    ["studentMenu", "student"],
    ["storeMenu", "store"],
    ["adminMenu", "admin"]
].forEach(([id, menuRole]) => {
    const menu = document.getElementById(id);

    if (menu) {
        menu.hidden = activeRole !== menuRole;
    }
});

const guestMenu = document.getElementById("guestMenu");

if (guestMenu) {
    guestMenu.hidden = activeRole !== null;
}

if (userRoleLabel) {
    userRoleLabel.textContent =
        activeRole === "student" ? "Student Account"
            : activeRole === "store" ? "Store Account"
                : activeRole === "admin" ? "Admin Account"
                    : "Not logged in";
}

if (sidebarToggle && sidebar) {
    sidebarToggle.addEventListener("click", function (event) {
        event.stopPropagation();

        sidebar.classList.toggle("collapsed");

        const isOpen = !sidebar.classList.contains("collapsed");
        sidebarToggle.setAttribute("aria-expanded", String(isOpen));
    });

    // Close the sidebar when clicking anywhere outside it
    document.addEventListener("click", function (event) {
        const isOpen = !sidebar.classList.contains("collapsed");

        if (
            isOpen &&
            !sidebar.contains(event.target) &&
            !sidebarToggle.contains(event.target)
        ) {
            sidebar.classList.add("collapsed");
            sidebarToggle.setAttribute("aria-expanded", "false");
        }
    });
}

// ==============================
// STORE REGISTRATION
// ==============================

const storeRegisterForm =
    document.getElementById("storeRegisterForm");

if (storeRegisterForm) {

    storeRegisterForm.addEventListener(
        "submit",
        async function (event) {

            event.preventDefault();

            const fullName =
                document.getElementById(
                    "storeOwnerName"
                ).value.trim();

            const email =
                document.getElementById(
                    "storeEmail"
                ).value.trim();

            const phone =
                document.getElementById(
                    "storePhone"
                ).value.trim();

            const storeName =
                document.getElementById(
                    "storeName"
                ).value.trim();

            const location =
                document.getElementById(
                    "storeLocation"
                ).value.trim();

            const description =
                document.getElementById(
                    "storeDescription"
                ).value.trim();

            const password =
                document.getElementById(
                    "storePassword"
                ).value;

            const confirmPassword =
                document.getElementById(
                    "storeConfirmPassword"
                ).value;


            if (password !== confirmPassword) {

                alert("Passwords do not match!");

                return;
            }


            const storeData = {

                fullName: fullName,
                email: email,
                password: password,
                phone: phone,
                storeName: storeName,
                location: location,
                description: description

            };


            try {

                const response = await fetch(
                    API_BASE + "/stores/register",
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body:
                            JSON.stringify(storeData)
                    }
                );


                const result =
                    await response.text();


                if (response.ok) {

                    alert(
                        "Store registration submitted successfully! " +
                        "Waiting for admin approval."
                    );

                    storeRegisterForm.reset();

                } else {

                    alert(
                        "Store registration failed: " +
                        result
                    );

                }


            } catch (error) {

                console.error(
                    "Store Registration Error:",
                    error
                );

                alert(
                    "Could not connect to the server. " +
                    "Please make sure the Spring Boot backend is running."
                );

            }

        }
    );

}

// ==============================
// STORE LOGIN
// ==============================

const storeLoginForm =
    document.getElementById("storeLoginForm");

if (storeLoginForm) {

    storeLoginForm.addEventListener(
        "submit",
        async function (event) {

            event.preventDefault();

            const email =
                document.getElementById(
                    "storeLoginEmail"
                ).value.trim();

            const password =
                document.getElementById(
                    "storeLoginPassword"
                ).value;


            const loginData = {

                email: email,
                password: password

            };


            try {

                const response = await fetch(
                    API_BASE + "/stores/login",
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body:
                            JSON.stringify(loginData)
                    }
                );


                const result =
                    await response.text();


                if (response.ok) {

                    const store =
                        JSON.parse(result);

                    saveSession("store", store);
                    window.location.href = "store-dashboard.html";

                } else {

                    alert(
                        "Store login failed: " +
                        result
                    );

                }


            } catch (error) {

                console.error(
                    "Store Login Error:",
                    error
                );

                alert(
                    "Could not connect to the server. " +
                    "Please make sure the Spring Boot backend is running."
                );

            }

        }
    );

}

// ==============================
// STORE AREA: dashboard, menu and orders
// ==============================
const CATEGORY_NAMES = {
    1: "Rice & Curry",
    2: "Kottu",
    3: "Fast Food",
    4: "Short Eats",
    5: "Drinks",
    6: "Desserts"
};

const ORDER_STATUSES = [
    "Pending", "Accepted", "Preparing", "Ready", "Completed", "Cancelled"
];

const onStorePage = ["store-dashboard.html", "store-menu.html", "store-orders.html"]
    .some(page => window.location.pathname.endsWith("/" + page));

if (onStorePage) {

    const loggedInStore = getSession("store");

    if (!loggedInStore) {
        window.location.href = "store-login.html";
        return;
    }

    refreshStoreSession(loggedInStore).then(store => {
        showApprovalNotice(store);
        initStoreDashboard(store);
        initStoreMenu(store);
        initStoreOrders(store);
    });
}

// Reads the store's latest details (for example, the admin's approval).
async function refreshStoreSession(store) {
    try {
        const response = await apiFetch("/stores/me", {}, "store");

        if (!response.ok) {
            return store;
        }

        const fresh = await response.json();
        const updated = Object.assign({}, store, fresh, { token: store.token });

        saveSession("store", updated);
        return updated;

    } catch (error) {
        return store;
    }
}

function showApprovalNotice(store) {
    const main = document.querySelector("main");

    if (!main || store.approvalStatus === "APPROVED") {
        return;
    }

    const notice = document.createElement("div");
    notice.className = "notice notice-warning";
    notice.textContent = store.approvalStatus === "REJECTED"
        ? "Your store registration was rejected. Please contact the campus administrator."
        : "Your store is waiting for admin approval. Students cannot see it, and you can add food items once it is approved.";

    main.insertBefore(notice, main.firstChild);
}


function foodImageUrl(image) {
    if (!image) return "";

    if (/^https?:\/\//i.test(image)) {
        return image;
    }

    const serverBase = API_BASE.replace(/\/api\/?$/, "");
    return serverBase + (image.startsWith("/") ? image : "/" + image);
}

async function uploadFoodImage(inputId) {
    const input = document.getElementById(inputId);
    const file = input?.files?.[0];

    if (!file) return null;

    if (!file.type.startsWith("image/")) {
        throw new Error("Please select a valid image file.");
    }

    if (file.size > 5 * 1024 * 1024) {
        throw new Error("Image must be 5 MB or smaller.");
    }

    const store = getSession("store");

    if (!store?.token) {
        throw new Error("Please log in again.");
    }

    const formData = new FormData();
    formData.append("image", file);

    const response = await fetch(API_BASE + "/food-images/upload", {
        method: "POST",
        headers: {
            Authorization: "Bearer " + store.token
        },
        body: formData
    });

    if (!response.ok) {
        throw new Error(
            await responseMessage(response, "Image upload failed.")
        );
    }

    const result = await response.json();

    if (!result.imageUrl) {
        throw new Error("The server did not return the uploaded image path.");
    }

    return result.imageUrl;
}



async function responseMessage(response, fallback) {
    try {
        const text = (await response.text()).trim();
        return text || fallback;
    } catch (error) {
        return fallback;
    }
}

// ---------- store dashboard ----------
function initStoreDashboard(store) {
    if (!document.getElementById("storeWelcomeName")) {
        return;
    }

    setText("storeWelcomeName", store.fullName);
    setText("storeApprovalStatus", store.approvalStatus);
    setText("storeName", store.storeName);
    setText("storeOwner", store.fullName);
    setText("storeEmail", store.email);
    setText("storePhone", store.phone);
    setText("storeLocation", store.location);
    setText("storeDescription", store.description);
}

// ---------- store menu ----------
function initStoreMenu(store) {
    const foodItemsList = document.getElementById("foodItemsList");

    if (!foodItemsList) {
        return;
    }

    let foods = [];

    const addFoodForm = document.getElementById("addFoodForm");
    const editFoodForm = document.getElementById("editFoodForm");
    const editSection = document.getElementById("editFoodSection");

    loadFoods();

    async function loadFoods() {
        foodItemsList.innerHTML = "<p>Loading food items...</p>";

        try {
            const response = await fetch(
                API_BASE + "/food-items/store/" + encodeURIComponent(store.storeId)
            );

            if (!response.ok) {
                throw new Error("Could not load food items");
            }

            foods = await response.json();
            renderFoods();

        } catch (error) {
            console.error("Food Items Error:", error);
            foodItemsList.innerHTML = "<p>Unable to load food items.</p>";
        }
    }

    
function renderFoods() {
    if (foods.length === 0) {
        foodItemsList.innerHTML = "<p>No food items added yet.</p>";
        return;
    }

    foodItemsList.innerHTML = foods.map(food => {
        const hasImage = Boolean(food.image);
        const sizes = Array.isArray(food.sizes) ? food.sizes : [];

        const sizeDisplay = sizes.length
            ? `<p><strong>Sizes:</strong> ${
                sizes.map(size =>
                    `${escapeHtml(size.sizeName)} — ${escapeHtml(formatMoney(size.price))}`
                ).join(", ")
            }</p>`
            : `<p><strong>Sizes:</strong> No size options</p>`;

        return `
            <div class="food-item-card">
                <div class="food-item-image">
                    ${hasImage
                        ? `<img src="${escapeHtml(foodImageUrl(food.image))}"
                                alt="${escapeHtml(food.foodName)}">`
                        : `<div class="no-image">🍽️</div>`}
                </div>

                <div class="food-item-details">
                    <h3>${escapeHtml(food.foodName)}</h3>
                    <p>${escapeHtml(food.description || "No description available")}</p>
                    <p><strong>Base price:</strong> ${escapeHtml(formatMoney(food.price))}</p>
                    ${sizeDisplay}
                    <p><strong>Category:</strong>
                        ${escapeHtml(CATEGORY_NAMES[food.categoryId] || "Unknown")}
                    </p>

                    <p><strong>Availability:</strong>
                        <span class="availability ${food.availability ? "is-on" : "is-off"}">
                            ${food.availability ? "Available" : "Unavailable"}
                        </span>
                    </p>

                    <div class="food-item-actions">
                        <button type="button" data-action="toggle" data-id="${food.foodId}">
                            ${food.availability ? "❌ Mark unavailable" : "✅ Mark available"}
                        </button>
                        <button type="button" data-action="edit" data-id="${food.foodId}">
                            Edit
                        </button>
                        <button type="button" data-action="delete" data-id="${food.foodId}">
                            Delete
                        </button>
                    </div>
                </div>
            </div>`;
    }).join("");
}

function populateSizeFields(prefix, sizes) {
    const configuredSizes = Array.isArray(sizes) ? sizes : [];

    const fields = [
        { name: "Small", checkbox: "addSmallEnabled", price: "addSmallPrice" },
        { name: "Medium", checkbox: "addMediumEnabled", price: "addMediumPrice" },
        { name: "Large", checkbox: "addLargeEnabled", price: "addLargePrice" }
    ];

    fields.forEach(field => {
        const isEdit = prefix === "editFood";

        const checkboxId = isEdit
            ? "edit" + field.name + "Enabled"
            : field.checkbox;

        const priceId = isEdit
            ? "edit" + field.name + "Price"
            : field.price;

        const checkbox = document.getElementById(checkboxId);
        const priceInput = document.getElementById(priceId);

        const configured = configuredSizes.find(
            size => size.sizeName === field.name
        );

        checkbox.checked = Boolean(configured);
        checkbox.dispatchEvent(new Event("change"));

        priceInput.value = configured ? configured.price : "";
    });
}


    // Edit / delete / toggle buttons
    foodItemsList.addEventListener("click", async function (event) {
        const button = event.target.closest("button[data-action]");

        if (!button) {
            return;
        }

        const foodId = Number(button.dataset.id);
        const food = foods.find(item => item.foodId === foodId);

        if (!food) {
            return;
        }

        if (button.dataset.action === "edit") {
            document.getElementById("editFoodId").value = food.foodId;
            document.getElementById("editFoodName").value = food.foodName || "";
            document.getElementById("editFoodDescription").value = food.description || "";
            document.getElementById("editFoodCategory").value = food.categoryId;
            document.getElementById("editFoodPrice").value = food.price;
            document.getElementById("editFoodImage").value = "";
document.getElementById("editFoodCurrentImage").value = food.image || "";
populateSizeFields("editFood", food.sizes);
const editPreview = document.getElementById("editFoodImagePreview");

if (food.image) {
    editPreview.src = foodImageUrl(food.image);
    editPreview.style.display = "block";
} else {
    editPreview.removeAttribute("src");
    editPreview.style.display = "none";
}
            document.getElementById("editFoodAvailability").checked = Boolean(food.availability);

            editSection.style.display = "block";
            editSection.scrollIntoView({ behavior: "smooth" });
            return;
        }

        button.disabled = true;

        try {
            let response;

            if (button.dataset.action === "delete") {
                if (!confirm("Are you sure you want to delete this food item?")) {
                    button.disabled = false;
                    return;
                }

                response = await apiFetch(
                    "/food-items/" + foodId + "/" + store.storeId,
                    { method: "DELETE" },
                    "store"
                );
            } else {
                response = await apiFetch(
                    "/food-items/availability/" + foodId + "/" + store.storeId,
                    { method: "PUT" },
                    "store"
                );
            }

            if (!response.ok) {
                throw new Error(await responseMessage(response, "The change could not be saved."));
            }

            if (button.dataset.action === "delete") {
    const message = await response.text();
    notify(message, "success");
} else {
    notify("Availability updated.", "success");
}

            await loadFoods();

        } catch (error) {
            if (error.message !== "Session expired") {
                console.error("Food action error:", error);
                notify(error.message, "error");
            }
            button.disabled = false;
        }
    });



    

function readFoodForm(prefix) {
    const isEdit = prefix === "editFood";

    const imageInput = document.getElementById(prefix + "Image");

    const currentImageInput = document.getElementById(
        isEdit ? "editFoodCurrentImage" : "foodCurrentImage"
    );

    const sizeFields = [
        { name: "Small", checkbox: "Small", price: "Small" },
        { name: "Medium", checkbox: "Medium", price: "Medium" },
        { name: "Large", checkbox: "Large", price: "Large" }
    ];

    const sizes = sizeFields
        .filter(size => {
            return document.getElementById(
                (isEdit ? "edit" : "add") + size.checkbox + "Enabled"
            ).checked;
        })
        .map(size => ({
            sizeName: size.name,
            price: Number(document.getElementById(
                (isEdit ? "edit" : "add") + size.price + "Price"
            ).value)
        }));

    return {
        categoryId: parseInt(
            document.getElementById(prefix + "Category").value, 10
        ),
        foodName: document.getElementById(prefix + "Name").value.trim(),
        description: document.getElementById(prefix + "Description").value.trim(),
        price: Number(document.getElementById(prefix + "Price").value),
        image: currentImageInput ? currentImageInput.value : "",
        imageFile: imageInput?.files?.[0] || null,
        availability: document.getElementById(prefix + "Availability").checked,
        sizes: sizes
    };
}


function foodFormProblem(food) {
    if (!food.foodName) return "Please enter a food name.";
    if (Number.isNaN(food.categoryId)) return "Please choose a category.";
    if (Number.isNaN(food.price) || food.price <= 0) {
        return "Please enter a price greater than zero.";
    }
    if (food.imageFile && !food.imageFile.type.startsWith("image/")) {
        return "Please select a valid image file.";
    }
    if (food.imageFile && food.imageFile.size > 5 * 1024 * 1024) {
        return "Image must be 5 MB or smaller.";
    }

    if (!Array.isArray(food.sizes)) {
        return "Invalid size options.";
    }

    for (const size of food.sizes) {
        if (
            !Number.isFinite(size.price) ||
            size.price <= 0 ||
            !/^\d+(\.\d{1,2})?$/.test(String(size.price))
        ) {
            return "Enter a valid price with up to two decimal places for every enabled size.";
        }
    }


    return "";
}


    if (addFoodForm) {
        addFoodForm.addEventListener("submit", async function (event) {
            event.preventDefault();

            const food = readFoodForm("food");
            const problem = foodFormProblem(food);

            if (problem) {
                notify(problem, "error");
                return;
            }

            try {

                
const uploadedImage = await uploadFoodImage("foodImage");

if (uploadedImage) {
    food.image = uploadedImage;
}

delete food.imageFile;

                const response = await apiJson("/food-items", "POST", food, "store");

                if (!response.ok) {
                    throw new Error(await responseMessage(response, "Could not add the food item."));
                }

                notify("Food item added.", "success");
                addFoodForm.reset();

populateSizeFields("food", []);

document.getElementById("foodAvailability").checked = true;
                await loadFoods();

            } catch (error) {
                if (error.message !== "Session expired") {
                    console.error("Add Food Item Error:", error);
                    notify(error.message, "error");
                }
            }
        });
    }

    if (editFoodForm) {
        editFoodForm.addEventListener("submit", async function (event) {
            event.preventDefault();

            const foodId = document.getElementById("editFoodId").value;
            const food = readFoodForm("editFood");
            const problem = foodFormProblem(food);

            if (problem) {
                notify(problem, "error");
                return;
            }

            try {
                
const imageInput = document.getElementById("editFoodImage");
const selectedFile = imageInput?.files?.[0];

if (selectedFile) {
    const uploadedImage = await uploadFoodImage("editFoodImage");

    if (!uploadedImage) {
        throw new Error("The new image was not uploaded.");
    }

    food.image = uploadedImage;
} else {
    food.image =
        document.getElementById("editFoodCurrentImage")?.value || null;
}

delete food.imageFile;



                const response = await apiJson(
                    "/food-items/" + foodId + "/" + store.storeId, "PUT", food, "store"
                );

                if (!response.ok) {
    const errorText = await response.text();

    console.error("Update response status:", response.status);
    console.error("Update response body:", errorText);

    throw new Error(
        errorText || "Could not update the food item."
    );
}

                notify("Food item updated.", "success");
                editSection.style.display = "none";
                await loadFoods();

            
} catch (error) {
    console.error("Update Food Item Error:", error);

    notify(
        error.message || "Failed to update food item.",
        "error"
    );

    alert(
        "Food update failed:\n" +
        (error.message || "Unknown error") +
        "\n\nCheck the VS Code browser console (F12)."
    );
}

        });
    }

    const cancelEditButton = document.getElementById("cancelEditBtn");

    if (cancelEditButton) {
        cancelEditButton.addEventListener("click", function () {
            editSection.style.display = "none";
        });
    }
}

// ---------- store orders ----------
function initStoreOrders(store) {
    const list = document.getElementById("storeOrdersList");

    if (!list) {
        return;
    }

    let orders = [];
    let statusFilter = "All";

    const toolbar = document.createElement("div");
    toolbar.className = "orders-toolbar";
    toolbar.innerHTML = `
    <label>
        <input type="checkbox" id="selectAllOrders">
        Select all clearable orders
    </label>

    <button type="button" id="clearSelectedOrders" class="clear-orders-btn">
        Clear selected orders
    </button>

    <label for="orderStatusFilter">Show</label>
    <select id="orderStatusFilter">
        <option value="All">All orders</option>
        ${ORDER_STATUSES.map(status =>
            `<option value="${status}">${status}</option>`
        ).join("")}
    </select>

    <button type="button" id="refreshOrdersBtn" class="secondary-button">
        ↻ Refresh
    </button>
`;
    list.parentNode.insertBefore(toolbar, list);

    toolbar.querySelector("#orderStatusFilter").addEventListener("change", function (event) {
        statusFilter = event.target.value;
        render();
    });

    toolbar.querySelector("#refreshOrdersBtn").addEventListener("click", load);

    load();

    async function load() {
        list.innerHTML = "<p>Loading orders...</p>";

        try {
            const response = await apiFetch(
                "/orders/store/" + encodeURIComponent(store.storeId), {}, "store"
            );

            if (!response.ok) {
                throw new Error(await responseMessage(response, "Could not load orders."));
            }

            orders = await response.json();
updateOrderSummary();
render();

        } catch (error) {
            if (error.message !== "Session expired") {
                console.error("Load Orders Error:", error);
                list.innerHTML = "<p>Could not load orders. Please check the backend.</p>";
            }
        }
    }

    function updateOrderSummary() {
    const total = orders.length;
    const pending = orders.filter(
        order => order.status === "Pending"
    ).length;

    const progress = orders.filter(
        order => ["Accepted", "Preparing", "Ready"].includes(order.status)
    ).length;

    const completed = orders.filter(
        order => order.status === "Completed"
    ).length;

    const setSummary = (id, value) => {
        const element = document.getElementById(id);
        if (element) element.textContent = value;
    };

    setSummary("summaryTotal", total);
    setSummary("summaryPending", pending);
    setSummary("summaryProgress", progress);
    setSummary("summaryCompleted", completed);
}

    function render() {
        if (orders.length === 0) {
            list.innerHTML = "<p>No orders received yet.</p>";
            return;
        }

        const visible = statusFilter === "All"
            ? orders
            : orders.filter(order => order.status === statusFilter);

        if (visible.length === 0) {
            list.innerHTML = "<p>No " + escapeHtml(statusFilter.toLowerCase()) + " orders.</p>";
            return;
        }

        list.innerHTML = visible
    .map(order => orderCard(order))
    .join("");

highlightLinkedOrder();
    }

    function highlightLinkedOrder() {
        const target = window.location.hash
            ? document.getElementById(window.location.hash.slice(1))
            : null;

        if (target) {
            target.classList.add("order-highlight");
            target.scrollIntoView({ behavior: "smooth", block: "center" });
        }
    }

    
function orderCard(order) {
    const displayNumber =
        order.displayOrderNumber ?? order.orderId;
    const items = (order.items || []).map(item => {
        const imageUrl = item.image
    ? (item.image.startsWith("http")
        ? item.image
        : `${API_BASE.replace(/\/api\/?$/, "")}${item.image.startsWith("/") ? "" : "/"}${item.image}`)
    : "";

const image = imageUrl
    ? `<img class="order-food-image"
            src="${escapeHtml(imageUrl)}"
            alt="${escapeHtml(item.foodName)}"
            loading="lazy"
            onerror="this.style.display='none'">`
    : `<div class="order-food-image order-food-image-placeholder">
            No image
       </div>`;

        return `
            <li class="order-item-row">
                ${image}
               
<div class="order-item-details">
    <span class="order-food-name">${escapeHtml(item.foodName)}</span>
    ${item.selectedSize
        ? `<span class="order-food-size">Size: ${escapeHtml(item.selectedSize)}</span>`
        : ""}
    <span class="order-food-quantity">Quantity: ${escapeHtml(item.quantity)}</span>
</div>

                <span class="order-item-price">${escapeHtml(formatMoney(item.lineTotal))}</span>
            </li>`;
    }).join("");

    const allowed = order.nextStatuses || [];
    const isFinal = allowed.length === 0;

    const options = ORDER_STATUSES.map(status => {
        const isCurrent = status === order.status;
        const disabled = !isCurrent && !allowed.includes(status);

        return `<option value="${escapeHtml(status)}"${isCurrent ? " selected" : ""}${disabled ? " disabled" : ""}>${escapeHtml(status)}</option>`;
    }).join("");

    const actions = `
        <div class="order-actions">
            <label for="status-${order.orderId}">Move to</label>
            <select id="status-${order.orderId}"
                    class="order-status-select"
                    data-current="${escapeHtml(order.status)}"
                    ${isFinal ? "disabled" : ""}>
                ${options}
            </select>
            <button type="button"
                    class="update-order-status-btn"
                    data-order-id="${order.orderId}"
                    disabled>
                Update status
            </button>
        </div>
        ${isFinal
            ? `<p class="order-final">This order is ${escapeHtml(String(order.status).toLowerCase())} and can no longer be changed.</p>`
            : ""}`;

    const cancelled = order.status === "Cancelled";

    return `
        <article class="order-card${cancelled ? " order-card-cancelled" : ""}"
                 id="order-${order.orderId}">

            <div class="order-card-top">
                <label class="select-order-label">
    <input
        type="checkbox"
        class="select-order-checkbox"
        value="${order.orderId}"
        aria-label="Select order ${order.orderId}"
        ${!["Completed", "Cancelled"].includes(order.status)
            ? "disabled"
            : ""}
    >
    Select order
</label>

                <span class="status-pill status-${escapeHtml(String(order.status).toLowerCase())}">
                    ${escapeHtml(order.status)}
                </span>
            </div>
            

            <div class="order-card-heading">
    <h3>Order #${displayNumber}</h3>

    <p class="order-meta">
        <strong>Student:</strong>
        ${escapeHtml(order.studentName || "Unknown student")}
        <span class="order-date">
            · ${escapeHtml(formatDate(order.orderDate))}
        </span>
    </p>

    <div class="order-customer-details">
        <p>
            <strong>Phone:</strong>
            ${escapeHtml(order.studentPhone || order.phone || "Not available")}
        </p>

        <p>
            <strong>Order location:</strong>
            ${escapeHtml(order.orderLocation || order.location || "Not available")}
        </p>
    </div>
</div>

            <ul class="order-items">${items}</ul>

            <div class="order-card-bottom">
                <p class="order-total">
                    <span>Total amount</span>
                    <strong>${escapeHtml(formatMoney(order.totalAmount))}</strong>
                </p>
                ${actions}
            </div>
        </article>`;
}


    // Enable "Update status" only when a different status is chosen.
    list.addEventListener("change", function (event) {
        const select = event.target.closest(".order-status-select");

        if (!select) {
            return;
        }

        const button = select.closest(".order-card")
            .querySelector(".update-order-status-btn");

        button.disabled = select.value === select.dataset.current;
    });

    list.addEventListener("click", async function (event) {
        const button = event.target.closest(".update-order-status-btn");

        if (!button) {
            return;
        }

        const orderId = Number(button.dataset.orderId);
const select = document.getElementById("status-" + orderId);
const status = select.value;

const currentOrder = orders.find(
    order => Number(order.orderId) === orderId
);

const displayOrderNumber =
    currentOrder?.displayOrderNumber ?? orderId;

        if (status === select.dataset.current) {
            return;
        }

        

        button.disabled = true;
        select.disabled = true;

        try {
            const response = await apiJson(
                "/orders/" + orderId + "/store/" + store.storeId + "/status",
                "PUT",
                { status: status },
                "store"
            );

            if (!response.ok) {
                throw new Error(await responseMessage(response, "Could not update the order status."));
            }

           if (status === "Cancelled") {
    notify(
        "Order #" + displayOrderNumber +
        " cancelled. The student has been notified.",
        "success"
    );
} else {
    notify(
        "Order #" + displayOrderNumber +
        " is now " + status + ".",
        "success"
    );
}
            await load();

        } catch (error) {
            if (error.message !== "Session expired") {
                console.error("Update Order Status Error:", error);
                notify(error.message, "error");

                // Put the dropdown back to the saved status.
                select.value = select.dataset.current;
                select.disabled = false;
                button.disabled = true;
            }
        }
    });

    // Select all visible, clearable store orders.
toolbar.querySelector("#selectAllOrders").addEventListener(
    "change",
    function () {
        list.querySelectorAll(".select-order-checkbox:not(:disabled)")
            .forEach(checkbox => {
                checkbox.checked = this.checked;
            });
    }
);

// Keep Select all synchronized with clearable orders.
list.addEventListener("change", function (event) {
    if (!event.target.matches(".select-order-checkbox")) {
        return;
    }

    const checkboxes = [
        ...list.querySelectorAll(
            ".select-order-checkbox:not(:disabled)"
        )
    ];

    const selectAll = toolbar.querySelector("#selectAllOrders");

    selectAll.checked =
        checkboxes.length > 0 &&
        checkboxes.every(checkbox => checkbox.checked);
});

// Permanently delete selected orders.
toolbar.querySelector("#clearSelectedOrders").addEventListener(
    "click",
    async function () {
        const selected = [
            ...list.querySelectorAll(".select-order-checkbox:checked")
        ];

        if (selected.length === 0) {
            notify("Please select at least one order.", "error");
            return;
        }

        if (!confirm(
            "Are you sure that you want to clear all selected orders"
        )) {
            return;
        }

        const orderIds = selected.map(
            checkbox => Number(checkbox.value)
        );

        const button = this;
        button.disabled = true;

        try {
            const response = await apiFetch(
                "/orders/store/" +
                    encodeURIComponent(store.storeId) +
                    "/selected",
                {
                    method: "DELETE",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(orderIds)
                },
                "store"
            );

            if (!response.ok) {
                throw new Error(
                    await responseMessage(
                        response,
                        "Could not delete selected orders."
                    )
                );
            }

            notify("Selected orders cleared from store orders.", "success");

            toolbar.querySelector("#selectAllOrders").checked = false;

            await load();

        } catch (error) {
            if (error.message !== "Session expired") {
                console.error("Delete Selected Orders Error:", error);
                notify(error.message, "error");
            }
        } finally {
            button.disabled = false;
        }
    }
);
}

// ==============================
// STUDENT FOOD BROWSING
// Load approved stores first, then each store's menu.
// ==============================
const studentFoodList = document.getElementById("studentFoodList");

if (studentFoodList) {
    // Public browsing: login is not required to view food.
    loadAvailableFood();

    async function loadAvailableFood() {
        studentFoodList.innerHTML = '<p class="loading-state">Finding available food near you…</p>';
        try {
            const storesResponse = await fetch(API_BASE + "/food-stores");
            if (!storesResponse.ok) throw new Error("Could not load approved food stores.");
            const stores = await storesResponse.json();

            const menus = await Promise.all(stores.map(async store => {
                const response = await fetch(API_BASE + "/food-items/store/" + encodeURIComponent(store.storeId));
                if (!response.ok) throw new Error("Could not load the menu for " + store.storeName + ".");
                const foods = await response.json();
                return foods
                    .filter(food => food.availability === true)
                    .map(food => ({ ...food, storeName: store.storeName, storeLocation: store.location }));
            }));

            const foods = menus.flat();
            if (!foods.length) {
                studentFoodList.innerHTML = '<div class="empty-state"><h3>No food available right now</h3><p>Please check again later or visit another time.</p></div>';
                return;
            }

            studentFoodList.innerHTML = "";
            const toolbar = document.createElement("div");
            toolbar.className = "food-list-toolbar";
            
const student = getSession("student");

const isStudentLoggedIn = Boolean(student && student.studentId);

toolbar.innerHTML = `
    <p>Choose a meal from an approved canteen or nearby store.</p>
    ${
        isStudentLoggedIn
            ? '<a class="secondary-link" href="cart.html">🛒 View cart <span class="cart-count" data-cart-count>0</span></a>'
            : '<a class="secondary-link" href="login.html">🔐 Login to order food</a>'
    }
`;

            studentFoodList.appendChild(toolbar);

            const grid = document.createElement("div");
            grid.className = "food-grid";
            foods.forEach(food => {
                const card = document.createElement("article");
                card.className = "food-card";
                card.dataset.foodName = food.foodName || "";
                card.dataset.description = food.description || "";
                card.dataset.storeName = food.storeName || "";
                card.dataset.categoryId = String(food.categoryId ?? "");
                console.log("Food category check:", {
    foodName: food.foodName,
    categoryId: food.categoryId,
    categoryName: food.categoryName
});
                card.dataset.categoryName =
    food.categoryName ||
    food.category?.categoryName ||
    food.category?.name ||
    "";

                const imageWrap = document.createElement("div");
                imageWrap.className = "food-card-image";
                if (food.image) {
                    const image = document.createElement("img");
                    image.src = foodImageUrl(food.image);
                    image.alt = food.foodName || "Food item";
                    image.loading = "lazy";
                    image.onerror = () => { imageWrap.innerHTML = '<span aria-hidden="true">🍽️</span>'; };
                    imageWrap.appendChild(image);
                } else {
                    imageWrap.innerHTML = '<span aria-hidden="true">🍽️</span>';
                }

                const details = document.createElement("div");
                details.className = "food-card-content";
                const store = document.createElement("p");
                store.className = "food-store-name";
                store.textContent = food.storeName + (food.storeLocation ? " · " + food.storeLocation : "");
                const title = document.createElement("h3");
                title.textContent = food.foodName;
                const description = document.createElement("p");
                description.className = "food-description";
                description.textContent = food.description || "Freshly prepared and ready to order.";
                const footer = document.createElement("div");
                footer.className = "food-card-footer";
                
const basePrice = Number(food.price || 0);

const price = document.createElement("strong");
price.className = "food-price";
price.textContent = formatMoney(basePrice);

// Only show a size selector if this food has configured sizes.
const sizes = Array.isArray(food.sizes) ? food.sizes : [];
let sizeSelect = null;

if (sizes.length > 0) {
    sizeSelect = document.createElement("select");
    sizeSelect.className = "food-size-select";
    sizeSelect.setAttribute("aria-label", "Choose food size");
    sizeSelect.required = true;

    const placeholder = document.createElement("option");
    placeholder.value = "";
    placeholder.textContent = "Choose a size";
    placeholder.selected = true;
    placeholder.disabled = true;
    sizeSelect.appendChild(placeholder);

    sizes.forEach(size => {
        const option = document.createElement("option");
        option.value = size.sizeName;
        option.dataset.price = String(Number(size.price));
        option.textContent =
            size.sizeName + " — " + formatMoney(size.price);

        sizeSelect.appendChild(option);
    });

    sizeSelect.addEventListener("change", function () {
        const selectedOption =
            sizeSelect.options[sizeSelect.selectedIndex];

        if (selectedOption && selectedOption.dataset.price) {
            price.textContent = formatMoney(
                Number(selectedOption.dataset.price)
            );
        }
    });
}

const addButton = document.createElement("button");
addButton.type = "button";
addButton.className = "add-to-cart-btn";
addButton.textContent = "Add to cart";
addButton.dataset.foodId = food.foodId;
addButton.dataset.storeId = food.storeId;
addButton.dataset.foodName = food.foodName || "Food item";
addButton.dataset.storeName = food.storeName || "Food store";
addButton.dataset.price = String(basePrice);
addButton.dataset.image = food.image || "";
addButton.dataset.description = food.description || "";

if (sizeSelect) {
    footer.append(price, sizeSelect, addButton);
} else {
    footer.append(price, addButton);
}

                details.append(store, title, description, footer);
                card.append(imageWrap, details);
                grid.appendChild(card);
            });
            
studentFoodList.appendChild(grid);
updateCartCount();



            // ==============================
            // FOOD MENU SEARCH AND FILTER
            // ==============================

            
            const clearSearchButton = document.getElementById("clearMenuSearch");
            const searchStatus = document.getElementById("menuSearchHint");
            const searchInput = document.getElementById("menuSearchInput");
            const categoryButtons = document.querySelectorAll(".menu-category");
            const params = new URLSearchParams(window.location.search);


            // Restore the search term passed from the homepage.
if (searchInput) {
    searchInput.value = params.get("search") || "";
}

            // Search food as the user types.
if (searchInput) {
    searchInput.addEventListener("input", filterFoodCards);
}

// Clear the search without resetting the selected category.
if (clearSearchButton) {
    clearSearchButton.addEventListener("click", function () {
        if (searchInput) {
            searchInput.value = "";
            searchInput.focus();
        }

        filterFoodCards();
    });
}

            let selectedCategory = "all";

            // Normalize text for reliable case- and whitespace-insensitive search.
            function normalizeSearchText(value) {
                return String(value || "")
                    .normalize("NFD")
                    .replace(/[\u0300-\u036f]/g, "")
                    .toLowerCase()
                    .replace(/\s+/g, " ")
                    .trim();
            }

            // Fallback only: these IDs must match your database.
            const categoryIds = {
                "rice & curry": "1",
                "kottu": "2",
                "fast food": "3",
                "short eats": "4",
                "drinks": "5",
                "desserts": "6"
            };

            // Add a message without removing the food cards.
            const noResults = document.createElement("div");
            noResults.className = "empty-state";
            noResults.hidden = true;
            noResults.innerHTML =
                "<h3>No matching food found</h3>" +
                "<p>Try another food name or choose a different category.</p>";

            studentFoodList.appendChild(noResults);

            

function filterFoodCards() {
    const searchTerm = normalizeSearchText(
        searchInput ? searchInput.value : ""
    );

    const categoryName = normalizeSearchText(selectedCategory);
    const selectedCategoryId = categoryIds[categoryName];
    const foodCards = grid.querySelectorAll(".food-card");

    let visibleCount = 0;

    foodCards.forEach(card => {
        const searchableText = normalizeSearchText([
            card.dataset.foodName,
            card.dataset.description,
            card.dataset.storeName
        ].join(" "));

        const matchesSearch =
            !searchTerm || searchableText.includes(searchTerm);

        const cardCategoryId = String(
            card.dataset.categoryId || ""
        ).trim();

        const matchesCategory =
            categoryName === "all" ||
            (
                selectedCategoryId !== undefined &&
                cardCategoryId === String(selectedCategoryId)
            );

        const isVisible = matchesSearch && matchesCategory;

        card.hidden = !isVisible;

        if (isVisible) {
            visibleCount++;
        }
    });

    noResults.hidden = visibleCount > 0;

    if (clearSearchButton) {
        clearSearchButton.disabled = !searchTerm;
    }

    if (searchStatus) {
        if (searchTerm) {
            searchStatus.textContent =
                `${visibleCount} matching food item${visibleCount === 1 ? "" : "s"} found.`;
        } else {
            searchStatus.textContent =
                "Search by food name, description or store name.";
        }
    }
}


            function selectCategory(category) {
                selectedCategory = category || "all";

                categoryButtons.forEach(button => {
                    const isSelected =
                        normalizeSearchText(button.dataset.category) ===
                        normalizeSearchText(selectedCategory);

                    button.setAttribute(
                        "aria-pressed",
                        String(isSelected)
                    );

                    button.style.background =
                        isSelected ? "#105536" : "";
                    button.style.color =
                        isSelected ? "#ffffff" : "";
                    button.style.borderColor =
                        isSelected ? "#105536" : "";
                });

                filterFoodCards();
            }

            categoryButtons.forEach(button => {
                button.addEventListener("click", function () {
                    selectCategory(button.dataset.category || "all");
                });
            });

            // Apply a category passed from the homepage.
            const initialCategory = params.get("category");

            if (
                initialCategory &&
                Array.from(categoryButtons).some(button =>
                    normalizeSearchText(button.dataset.category) ===
                    normalizeSearchText(initialCategory)
                )
            ) {
                selectCategory(initialCategory);
            } else {
                selectCategory("all");
            }


            filterFoodCards();

        } catch (error) {
            console.error("Load Available Food Error:", error);
            studentFoodList.innerHTML = '<div class="error-state"><h3>Food menu could not be loaded</h3><p>' + escapeHtml(error.message) + '</p><button type="button" id="retryFoodLoad">Try again</button></div>';
            const retryButton = document.getElementById("retryFoodLoad");
            if (retryButton) retryButton.addEventListener("click", loadAvailableFood);
        }
    }
}

// ==============================
// CART HELPERS AND ADD-TO-CART
// The backend creates one order per store, so one cart is limited to one store.
// ==============================
function renderCart() {
    const cartContainer = document.getElementById("cartItems");
    if (!cartContainer) return;

    const cart = readCart();
    const totalElement = document.getElementById("cartTotal");
    const checkoutButton = document.getElementById("checkoutButton");
    const emptyMessage = document.getElementById("emptyCartMessage");
    const summary = document.getElementById("cartSummary");

    cartContainer.innerHTML = "";
    const isEmpty = cart.length === 0;
    if (emptyMessage) emptyMessage.hidden = !isEmpty;
    if (summary) summary.hidden = isEmpty;
    if (checkoutButton) checkoutButton.disabled = isEmpty;

    
    let itemCount = 0;
    let total = 0;
    cart.forEach((item, index) => {
        const quantity = Math.max(1, Number(item.quantity || 1));
        const price = Number(item.price || 0);
        total += price * quantity;
        itemCount += quantity;

        const row = document.createElement("article");
        row.className = "cart-item";
                const image = document.createElement("div");
        image.className = "cart-item-image";
        const initial = (item.foodName || "F").trim().charAt(0).toUpperCase();
        if (item.image) {
            const img = document.createElement("img");
            img.src = item.image;
            img.alt = item.foodName || "Food item";
            img.onerror = () => { img.remove(); image.textContent = initial; };
            image.appendChild(img);
        } else {
            image.textContent = initial;
        }
        const info = document.createElement("div");
        info.className = "cart-item-info";
        
info.innerHTML =
    '<h3>' + escapeHtml(item.foodName || "Food item") + '</h3>' +
    '<p>' + escapeHtml(item.storeName || "Food store") + '</p>' +
    (
        item.selectedSize
            ? '<p class="cart-item-size">Size: ' +
                escapeHtml(item.selectedSize) + '</p>'
            : ''
    ) +
    '<strong>' + formatMoney(price) + '</strong>';

        const controls = document.createElement("div");
        controls.className = "cart-item-controls";
        controls.innerHTML = '<label>Qty <input class="cart-quantity" type="number" min="1" max="99" step="1" value="' + quantity + '" data-index="' + index + '"></label><button type="button" class="remove-cart-item-btn secondary-button" data-index="' + index + '">Remove</button>';
        const lineTotal = document.createElement("strong");
        lineTotal.className = "cart-line-total";
        lineTotal.textContent = "LKR " + (price * quantity).toLocaleString("en-LK", { maximumFractionDigits: 2 });
        row.append(image, info, controls, lineTotal);
        cartContainer.appendChild(row);
    });

        const countElement = document.getElementById("cartItemCount");
    if (countElement) countElement.textContent = String(itemCount);

    if (totalElement) totalElement.textContent = "LKR " + total.toLocaleString("en-LK", { maximumFractionDigits: 2 });
    updateCartCount();
}

// Handle add-to-cart, cart quantity changes, removal, and checkout.
document.addEventListener("click", async function (event) {
    const addButton = event.target.closest(".add-to-cart-btn");

    if (addButton) {
        // Require student login only when adding food to cart.
        const student = getSession("student");

        if (!student || !student.studentId) {
            alert("Please log in as a student to order food.");
            window.location.href = "login.html";
            return;
        }

        
const foodId = Number(addButton.dataset.foodId || addButton.dataset.id);
const storeId = Number(addButton.dataset.storeId || addButton.dataset.store);

const foodCard = addButton.closest(".food-card");
const sizeSelect = foodCard
    ? foodCard.querySelector(".food-size-select")
    : null;

let selectedSize = null;
let selectedPrice = Number(addButton.dataset.price || 0);

if (sizeSelect) {
    if (!sizeSelect.value) {
        alert("Please choose a size before adding this food to your cart.");
        sizeSelect.focus();
        return;
    }

    selectedSize = sizeSelect.value;

    const selectedOption =
        sizeSelect.options[sizeSelect.selectedIndex];

    selectedPrice = Number(selectedOption.dataset.price);

    if (!Number.isFinite(selectedPrice) || selectedPrice <= 0) {
        alert("The selected size has an invalid price. Please try again.");
        return;
    }
}

const cart = readCart();

const differentStore = cart.length &&
    cart.some(item => Number(item.storeId) !== storeId);

if (differentStore) {
    const replace = confirm(
        "Your cart contains items from another store. Clear it and add this item instead?"
    );

    if (!replace) return;

    cart.length = 0;
}

// Match both food ID and size, so Small and Large are separate lines.
const existing = cart.find(item =>
    Number(item.foodId) === foodId &&
    (item.selectedSize || null) === selectedSize
);

if (existing) {
    const newQuantity = Number(existing.quantity || 1) + 1;

    if (newQuantity > 99) {
        alert("You can order a maximum of 99 of the same food size.");
        return;
    }

    existing.quantity = newQuantity;
    existing.price = selectedPrice;
} else {
    cart.push({
        foodId,
        storeId,
        storeName: addButton.dataset.storeName || "Food store",
        foodName: addButton.dataset.foodName || "Food item",
        description: addButton.dataset.description || "",
        image: addButton.dataset.image || "",
        selectedSize,
        price: selectedPrice,
        quantity: 1
    });
}

saveCart(cart);
updateCartCount();

const oldText = addButton.textContent;
addButton.textContent = "Added ✓";


window.setTimeout(() => {
    if (addButton.isConnected) {
        addButton.textContent = oldText;
    }
}, 1000);

return;
}

// Handle removing a cart item.
const removeButton = event.target.closest(".remove-cart-item-btn");

    if (removeButton) {
        const cart = readCart();
        cart.splice(Number(removeButton.dataset.index), 1);
        saveCart(cart);
        renderCart();
        return;
    }

    if (event.target.closest("#clearCartButton")) {
        if (readCart().length && confirm("Remove all items from your cart?")) {
            saveCart([]);
            renderCart();
        }
        return;
    }

    const checkoutButton = event.target.closest("#checkoutButton");
    if (!checkoutButton) return;

    const student = getSession("student");
    if (!student || !student.studentId) {
        alert("Please log in as a student before checking out.");
        window.location.href = "login.html";
        return;
    }
    const cart = readCart();
if (!cart.length) return;

// Get the student's pickup location.
const orderLocationInput = document.getElementById("orderLocation");
const orderLocation = orderLocationInput?.value.trim() || "";

if (!orderLocation) {
    alert("Please enter your pickup location.");
    orderLocationInput?.focus();
    return;
}

checkoutButton.disabled = true;
checkoutButton.textContent = "Placing order…";
    try {
        const response = await apiFetch("/orders", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
    studentId: student.studentId,
    storeId: cart[0].storeId,
    orderLocation: orderLocation,
    items: cart.map(item => ({
        foodId: Number(item.foodId),
        quantity: Number(item.quantity),
        selectedSize: item.selectedSize || null
    }))
})
        });
        const responseText = await response.text();
        let result;
        try { result = JSON.parse(responseText); } catch { result = responseText; }
        if (!response.ok) {
            throw new Error(typeof result === "string" ? result : "The order could not be placed. Please check item availability and try again.");
        }
        saveCart([]);
        renderCart();
        
        const confirmation = document.getElementById("checkoutMessage");

const displayOrderNumber =
    result.displayOrderNumber ?? result.orderId;

if (confirmation) {
    confirmation.hidden = false;
    confirmation.innerHTML =
        '<h3>Order placed successfully!</h3>' +
        '<p>Order #' + escapeHtml(displayOrderNumber) +
        ' · Total: ' + escapeHtml(formatMoney(result.totalAmount)) +
        '</p>' +
        '<a class="primary-link" href="student-orders.html">Track your order</a>';
} else {
    alert(
        "Order placed successfully! Order #" +
        displayOrderNumber
    );
}
    } catch (error) {
        console.error("Checkout Error:", error);
        if (error.message !== "Session expired") {
            alert("Checkout failed: " + error.message);
        }
    } finally {
        checkoutButton.disabled = readCart().length === 0;
        checkoutButton.textContent = "Place order";
    }
});

// Cart page setup
const cartPage = document.getElementById("cartItems");
if (cartPage) {
    const student = getSession("student");
    if (!student) {
        window.location.href = "login.html";
    } else {
        renderCart();
        document.addEventListener("change", function (event) {
            if (!event.target.matches(".cart-quantity")) return;
            const index = Number(event.target.dataset.index);
            const quantity = Number(event.target.value);
            if (!Number.isInteger(quantity) || quantity < 1 || quantity > 99) {
                alert("Please choose a quantity between 1 and 99.");
                renderCart();
                return;
            }
            const cart = readCart();
            if (!cart[index]) return;
            cart[index].quantity = quantity;
            saveCart(cart);
            renderCart();
        });
    }
}

// ==============================
// STUDENT MY ORDERS
// ==============================
const studentOrdersList = document.getElementById("studentOrdersList");

if (studentOrdersList) {

    const student = getSession("student");

    if (!student) {
        window.location.href = "login.html";
        return;
    }

    let studentOrders = [];
    const cancelling = new Set();   // orders with a cancel request in progress

    // Student history has its own independent clear controls.
const studentOrdersToolbar = document.createElement("div");
studentOrdersToolbar.className = "orders-toolbar student-orders-toolbar";

studentOrdersToolbar.innerHTML = `
    <label>
        <input type="checkbox" id="selectAllStudentOrders">
        Select all clearable orders
    </label>

    <button
        type="button"
        id="clearSelectedStudentOrders"
        class="clear-orders-btn"
    >
        Clear selected orders
    </button>
`;

studentOrdersList.parentNode.insertBefore(
    studentOrdersToolbar,
    studentOrdersList
);


    loadStudentOrders();

    // Pick up status changes made by the store while this page stays open.
    window.setInterval(function () {
        if (!document.hidden && cancelling.size === 0) {
            loadStudentOrders(true);
        }
    }, 30000);

    async function loadStudentOrders(quiet) {
        if (quiet !== true) {
            studentOrdersList.innerHTML = '<p class="loading-state">Loading your orders…</p>';
        }

        try {
            const response = await apiFetch(
                "/orders/student/" + encodeURIComponent(student.studentId), {}, "student"
            );

            if (!response.ok) {
                throw new Error(await responseMessage(response, "Could not load your orders."));
            }

            // The status always comes from the database, never from a saved copy.
            studentOrders = await response.json();
            renderStudentOrders();

        } catch (error) {
            if (error.message === "Session expired" || quiet === true) {
                return;
            }

            console.error("Load Student Orders Error:", error);
            studentOrdersList.innerHTML = '<div class="error-state"><h3>Could not load orders</h3><p>' + escapeHtml(error.message) + '</p><button type="button" id="retryStudentOrders">Try again</button></div>';

            const retry = document.getElementById("retryStudentOrders");
            if (retry) retry.addEventListener("click", loadStudentOrders);
        }
    }

        function updateOrderSummary() {
        const countOf = status =>
            studentOrders.filter(order => order.status === status).length;

        const show = (id, value) => {
            const element = document.getElementById(id);
            if (element) element.textContent = String(value);
        };

        show("summaryActive", studentOrders.filter(order =>
            order.status !== "Cancelled" && order.status !== "Completed").length);
        show("summaryPending", countOf("Pending"));
        show("summaryCompleted", countOf("Completed"));
    }

    // Cancelled orders are removed from this list (the store keeps them).
    function renderStudentOrders() {
    updateOrderSummary();

    if (studentOrders.length === 0) {
        studentOrdersList.innerHTML = `
            <div class="empty-state">
                <h3>No orders in your history</h3>
                <p>Your food orders will appear here.</p>
                <a class="primary-link" href="student-order.html">
                    Browse available food
                </a>
            </div>
        `;

        studentOrdersToolbar.querySelector(
            "#selectAllStudentOrders"
        ).checked = false;

        return;
    }

    studentOrdersList.innerHTML =
        studentOrders.map(studentOrderCard).join("");

    studentOrdersToolbar.querySelector(
        "#selectAllStudentOrders"
    ).checked = false;
}

        const ORDER_STEPS = ["Pending", "Accepted", "Preparing", "Ready", "Completed"];

    function studentOrderCard(order) {
    const items = (order.items || []).map(item => `
        <li>
            <span>
                ${escapeHtml(item.foodName)} ×
                ${escapeHtml(item.quantity)}
            </span>
            <span>${escapeHtml(formatMoney(item.lineTotal))}</span>
        </li>
    `).join("");

    const isCancelled = order.status === "Cancelled";
    const canClear = ["Completed", "Cancelled"].includes(order.status);

    const current = order.status === "Completed"
        ? ORDER_STEPS.length
        : ORDER_STEPS.indexOf(order.status);

    const tracker = isCancelled
        ? `<p class="order-final">
               This order was cancelled. It remains in your order history.
           </p>`
        : `
            <ol class="order-tracker" aria-label="Order progress">
                ${ORDER_STEPS.map((step, index) => `
                    <li class="${
                        index < current
                            ? "is-done"
                            : index === current
                                ? "is-current"
                                : ""
                    }">
                        <span class="order-tracker-dot"></span>
                        <span class="order-tracker-label">
                            ${escapeHtml(step)}
                        </span>
                    </li>
                `).join("")}
            </ol>
        `;

    const canCancel = order.status === "Pending";

    return `
        <article
            class="order-card${isCancelled ? " order-card-cancelled" : ""}"
            id="order-${order.orderId}"
        >
            <div class="order-card-top">
                <label class="select-order-label">
                    <input
                        type="checkbox"
                        class="student-select-order-checkbox"
                        value="${order.orderId}"
                        aria-label="Select order ${order.orderId}"
                        ${canClear ? "" : "disabled"}
                    >
                    Select order
                </label>

                <span class="status-pill status-${
                    escapeHtml(String(order.status).toLowerCase())
                }">
                    ${escapeHtml(order.status)}
                </span>
            </div>

            <h3>Order #${escapeHtml(
    order.displayOrderNumber ?? order.orderId
)}</h3>

            <p class="order-meta">
                ${escapeHtml(order.storeName)}
                · ${escapeHtml(formatDate(order.orderDate))}
            </p>

            ${tracker}

            <ul class="order-items">${items}</ul>

            <p class="order-total">
                <strong>Total</strong>
                ${escapeHtml(formatMoney(order.totalAmount))}
            </p>

            ${canCancel ? `
                <button
                    type="button"
                    class="cancel-order-btn secondary-button"
                    data-order-id="${order.orderId}"
                >
                    Cancel order
                </button>
            ` : ""}
        </article>
    `;
}

// Select all visible, clearable student orders.
studentOrdersToolbar.querySelector(
    "#selectAllStudentOrders"
).addEventListener("change", function () {
    studentOrdersList.querySelectorAll(
        ".student-select-order-checkbox:not(:disabled)"
    ).forEach(checkbox => {
        checkbox.checked = this.checked;
    });
});

// Synchronize Select all with individual selections.
studentOrdersList.addEventListener("change", function (event) {
    if (!event.target.matches(".student-select-order-checkbox")) {
        return;
    }

    const checkboxes = [
        ...studentOrdersList.querySelectorAll(
            ".student-select-order-checkbox:not(:disabled)"
        )
    ];

    studentOrdersToolbar.querySelector(
        "#selectAllStudentOrders"
    ).checked =
        checkboxes.length > 0 &&
        checkboxes.every(checkbox => checkbox.checked);
});

// Clear selected orders from this student's history only.
studentOrdersToolbar.querySelector(
    "#clearSelectedStudentOrders"
).addEventListener("click", async function () {
    const selected = [
        ...studentOrdersList.querySelectorAll(
            ".student-select-order-checkbox:checked:not(:disabled)"
        )
    ];

    if (selected.length === 0) {
        notify(
            "Select at least one completed or cancelled order.",
            "error"
        );
        return;
    }

    if (!confirm(
        "Clear the selected completed or cancelled orders from your history? " +
        "They will remain in the store's order list."
    )) {
        return;
    }

    const orderIds = selected.map(
        checkbox => Number(checkbox.value)
    );

    const button = this;
    button.disabled = true;

    try {
        const response = await apiFetch(
            "/orders/student/" +
                encodeURIComponent(student.studentId) +
                "/selected",
            {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(orderIds)
            },
            "student"
        );

        if (!response.ok) {
            throw new Error(
                await responseMessage(
                    response,
                    "Could not clear the selected orders."
                )
            );
        }

        notify(
            "Selected orders cleared from your history.",
            "success"
        );

        studentOrdersToolbar.querySelector(
            "#selectAllStudentOrders"
        ).checked = false;

        await loadStudentOrders(true);

    } catch (error) {
        if (error.message !== "Session expired") {
            console.error("Clear Student Orders Error:", error);
            notify(error.message, "error");
        }
    } finally {
        button.disabled = false;
    }
});

    studentOrdersList.addEventListener("click", async function (event) {
        const button = event.target.closest(".cancel-order-btn");

        if (!button) {
            return;
        }

        const orderId = Number(button.dataset.orderId);

        // One request at a time per order (no double clicks).
        if (cancelling.has(orderId) || !confirm("Cancel this order?")) {
            return;
        }

        cancelling.add(orderId);
        button.disabled = true;

        try {
            const response = await apiFetch(
                "/orders/" + orderId + "/student/" + student.studentId + "/cancel",
                { method: "PUT" },
                "student"
            );

            if (!response.ok) {
                throw new Error(await responseMessage(response, "Could not cancel the order."));
            }

            // The server confirmed: take the order off this page right away.
            const order = studentOrders.find(item => item.orderId === orderId);

            if (order) {
                order.status = "Cancelled";
            }

            renderStudentOrders();
            notify("Order cancelled. The store has been notified.", "success");
            refreshNotificationBadge();

        } catch (error) {
            if (error.message !== "Session expired") {
                notify(error.message, "error");

                // The order may have changed (for example the store accepted
                // it) - show the real status again.
                if (/already|no longer/i.test(error.message)) {
                    await loadStudentOrders(true);
                } else {
                    button.disabled = false;
                }
            }
        } finally {
            cancelling.delete(orderId);
        }
    });
}

updateCartCount();
});
