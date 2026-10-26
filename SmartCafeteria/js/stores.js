// ==============================
// EUSL EATS - STORE BROWSING
// ==============================

document.addEventListener("DOMContentLoaded", () => {
    const API = API_BASE;

    // Load all approved stores
    const storesList = document.getElementById("storesList");
    let allStores = [];

    if (storesList) {
        loadStores();

        const storeSearch = document.getElementById("storeSearch");

        if (storeSearch) {
            storeSearch.addEventListener("input", renderStores);
        }
    }

    function showMessage(className, title, text) {
        storesList.innerHTML = "";

        const box = document.createElement("div");
        box.className = className;

        const heading = document.createElement("h3");
        heading.textContent = title;

        const paragraph = document.createElement("p");
        paragraph.textContent = text;

        box.append(heading, paragraph);
        storesList.appendChild(box);

        return box;
    }

    async function loadStores() {
        storesList.innerHTML = '<p class="loading-state">Loading stores...</p>';

        try {
            const response = await fetch(`${API}/food-stores`);

            if (!response.ok) {
                throw new Error("Could not load food stores.");
            }

            const stores = await response.json();

            allStores = stores.filter(store =>
                String(store.approvalStatus || "").toUpperCase() === "APPROVED"
            );

            const count = document.getElementById("storeCount");

            if (count) {
                count.textContent = String(allStores.length);
            }

            renderStores();

        } catch (error) {
            console.error("Load Stores Error:", error);

            const box = showMessage(
                "error-state",
                "Could not load stores",
                "Make sure the backend is running, then try again."
            );

            const retry = document.createElement("button");
            retry.type = "button";
            retry.textContent = "Try again";
            retry.addEventListener("click", loadStores);
            box.appendChild(retry);
        }
    }

    function renderStores() {
        const searchInput = document.getElementById("storeSearch");
        const term = searchInput ? searchInput.value.trim().toLowerCase() : "";

        if (allStores.length === 0) {
            showMessage(
                "empty-state",
                "No stores available yet",
                "Approved food stores will appear here."
            );
            return;
        }

        const visible = allStores.filter(store =>
            [store.storeName, store.location, store.description]
                .join(" ")
                .toLowerCase()
                .includes(term)
        );

        if (visible.length === 0) {
            showMessage(
                "empty-state",
                "No matching stores",
                "Try a different name or location."
            );
            return;
        }

        storesList.innerHTML = "";

        const grid = document.createElement("div");
        grid.className = "food-grid";

        visible.forEach(store => {
            const card = document.createElement("article");
            card.className = "food-card store-card";

            const content = document.createElement("div");
            content.className = "food-card-content";

            // First letter of the store name instead of an emoji
            const icon = document.createElement("div");
            icon.className = "card-icon";
            icon.setAttribute("aria-hidden", "true");
            icon.textContent = (store.storeName || "F").trim().charAt(0).toUpperCase();

            const name = document.createElement("h3");
            name.textContent = store.storeName || "Food Store";

            const location = document.createElement("p");
            location.className = "store-location";
            location.textContent = store.location || "Not provided";

            const description = document.createElement("p");
            description.className = "store-description";
            description.textContent =
                store.description || "Explore this store's menu.";

            const button = document.createElement("a");
            button.className = "primary-link";
            button.textContent = "View menu";
            button.href =
                "student-store-menu.html?storeId=" +
                encodeURIComponent(store.storeId);

            content.append(icon, name, location, description, button);
            card.appendChild(content);
            grid.appendChild(card);
        });

        storesList.appendChild(grid);
    }

    // ==============================
    // LOAD FOOD FOR SELECTED STORE
    // ==============================

    const storeFoodList = document.getElementById("storeFoodList");

    if (storeFoodList) {
        loadStoreMenu();
    }

    async function loadStoreMenu() {
        const params = new URLSearchParams(window.location.search);
        const storeId = params.get("storeId");

        const nameElement = document.getElementById("storeName");
        const locationElement = document.getElementById("storeLocation");

        if (!storeId) {
            storeFoodList.textContent = "No store was selected.";
            return;
        }

        storeFoodList.innerHTML = "<p>Loading food items...</p>";

        try {
            // Get stores and confirm the selected store is approved
            const storesResponse = await fetch(`${API}/food-stores`);

            if (!storesResponse.ok) {
                throw new Error("Could not load food stores.");
            }

            const stores = await storesResponse.json();

            const store = stores.find(item =>
                String(item.storeId) === String(storeId) &&
                String(item.approvalStatus || "").toUpperCase() === "APPROVED"
            );

            if (!store) {
                throw new Error("This store was not found or is not approved.");
            }

            nameElement.textContent = store.storeName || "Store Menu";
            locationElement.textContent = store.location || "";

            // Get food belonging to this store only
            const response = await fetch(
                `${API}/food-items/store/${encodeURIComponent(storeId)}`
            );

            if (!response.ok) {
                throw new Error("Could not load this store's menu.");
            }

            const foods = await response.json();

            const availableFoods = foods.filter(food =>
                food.availability === true
            );

            if (availableFoods.length === 0) {
                storeFoodList.innerHTML =
                    "<p>No food items are currently available at this store.</p>";
                return;
            }

            storeFoodList.innerHTML = "";

            const grid = document.createElement("div");
            grid.className = "food-grid";

            availableFoods.forEach(food => {
                const card = document.createElement("article");
                card.className = "food-card";

                const imageWrap = document.createElement("div");
                imageWrap.className = "food-card-image";

                
const imagePath = String(food.image || "").trim();

if (imagePath) {
    const image = document.createElement("img");

    // Resolve uploaded images using the existing shared helper.
    // This ensures images load from Spring Boot, not Live Server.
    image.src = typeof foodImageUrl === "function"
        ? foodImageUrl(imagePath)
        : imagePath.startsWith("http://") ||
          imagePath.startsWith("https://")
            ? imagePath
            : API.replace(/\/api\/?$/, "") +
              (imagePath.startsWith("/") ? imagePath : "/" + imagePath);

    image.alt = food.foodName || "Food item";
    image.loading = "lazy";

    image.onerror = () => {
        image.remove();
        imageWrap.textContent = "🍽️";
        imageWrap.classList.add("food-image-unavailable");
    };

    imageWrap.appendChild(image);
} else {
    imageWrap.textContent = "🍽️";
    imageWrap.classList.add("food-image-unavailable");
}


                const content = document.createElement("div");
                content.className = "food-card-content";

                const title = document.createElement("h3");
                title.textContent = food.foodName || "Food item";

                const description = document.createElement("p");
                description.className = "food-description";
                description.textContent =
                    food.description || "Freshly prepared food.";

                const footer = document.createElement("div");
                footer.className = "food-card-footer";

                const price = document.createElement("strong");
                price.className = "food-price";
                price.textContent =
                    "LKR " + Number(food.price || 0).toLocaleString("en-LK", {
                        maximumFractionDigits: 2
                    });

                const addButton = document.createElement("button");
                addButton.type = "button";
                addButton.className = "add-to-cart-btn";
                addButton.textContent = "Add to cart";

                // Keep the data attributes used by the existing cart system
                addButton.dataset.foodId = food.foodId;
                addButton.dataset.storeId = store.storeId;
                addButton.dataset.foodName = food.foodName || "Food item";
                addButton.dataset.storeName = store.storeName || "Food store";
                addButton.dataset.price = food.price;
                addButton.dataset.image = food.image || "";
                addButton.dataset.description = food.description || "";

                footer.append(price, addButton);
                content.append(title, description, footer);
                card.append(imageWrap, content);
                grid.appendChild(card);
            });

            storeFoodList.appendChild(grid);

            // Refresh cart count if the existing function is available
            if (typeof updateCartCount === "function") {
                updateCartCount();
            }

        } catch (error) {
            console.error("Load Store Menu Error:", error);
            storeFoodList.innerHTML = "";

            const message = document.createElement("p");
            message.textContent = error.message;
            storeFoodList.appendChild(message);
        }
    }
});