# Java SuperMart Online Ordering System 🛒

A complete, dual-interface desktop application built exclusively with **Vanilla Java Swing**. Designed for local storefronts merging dynamic customer-facing checkout tools with powerful administrative dashboard analytics. 

## Features

### 👤 Customer Experience
* **Native Authentication**: Standard log-ins and on-the-fly registration.
* **Store Product Grid**: View all populated inventory elements as generated UI cards.
* **Search & Filter**: Find specific products dynamically by query strings.
* **Shopping Cart Architecture**: Add products, alter quantities real-time, compute live Delivery & Subtotal fees, specify addresses, and initiate checkout flows natively via **UPI**. 
* **My Orders Timeline**: Double-click past transactions within the orders grid to monitor detailed invoices, order locations, and processing status from the backend.
* **Profile Settings**: Control passwords and read profile layouts seamlessly.
  
### ⚙️ Admin Dashboard
* **Dynamic Inventory Control**: Modify existing entries or insert new products with image support and price tracking across live user connections.
* **Logistics Routing**: Break down daily "Same Day" & "Next Day" queues seamlessly on separate grids. Track delivery statuses manually (Pending / Processing / Delivered) via interactive combo-box inputs and cell selections.
* **Customer Notifier Mock**: Simulate pings sent toward email channels when shipping progresses. 
* **Analytics Engine**: Generate isolated "Daily Reports" evaluating total orders, revenue volume metrics, and visual counts per generated report timeframe.
* **Global Order Master Tracker**: Review *all* cross-platform actions generated into one master grid. Double-click to load a detailed tracking overlay covering product counts and client locations.
  
## Codebase Structure
The backend drops slow, isolated databases in favor of lightning-fast **Static In-Memory Models**. All structural persistence handles locally via auto-increment IDs inside synchronized Java Data Structures.
- `Database.java` - Core unifying arrays holding abstracted `Customer`, `Product`, and `Order` instances routing cleanly across GUI panes. 
- `SuperMartMain.java` - Validates global email checks and establishes the core entry portal. 
- `AdminDashboard.java` - Powers detailed Split-pane tools mapping strictly to back-end administration features.
- `CustomerDashboard.java` - Builds elegant customer-centric views wrapping around the central inventory mappings smoothly. 

## How To Run

#### 1. Via Standard CLI 
Compile and run the application using the following commands:

**Compilation:**
```sh
javac --module-path "C:\Users\acer\Downloads\openjfx-26_windows-x64_bin-sdk\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml,javafx.web -cp "src;mysql-connector-j-9.6.0.jar" src/*.java
```

**Execution:**
```sh
java --module-path "C:\Users\acer\Downloads\openjfx-26_windows-x64_bin-sdk\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml,javafx.web -cp "src;mysql-connector-j-9.6.0.jar" SuperMartMain
```


#### 2. Intellij IDEA / Eclipse
* Copy all the `.java` files from this repo directly into your IDE's `src` folder. 
* Locate `SuperMartMain.java` and execute standard Run features to spawn the application UI!

#### 3. Visual Studio Code 
* Install the baseline **Extension Pack for Java**. 
* Access your project directory. 
* Locate `SuperMartMain.java` and click the floating `Run` button that appears above the `main` argument string.

---

### Recent Updates
- Integrated a new backend persistence layer for better data management.
- Improved the Admin Dashboard analytics for real-time reporting.
- Refactored core logic in `SuperMartMain.java` and `Admin.java` for better performance.

