import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

import java.util.ArrayList;

public class AdminDashboard extends JFrame {

    
    

    private JLabel mainTitleLabel;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;

    private JLabel globalStatsLabel;

    public AdminDashboard(String email) {
        // Set window properties
        setTitle("Admin Dashboard - SuperMart");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setLayout(new BorderLayout());

        // --- Left Sidebar (West) ---
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setPreferredSize(new Dimension(200, getHeight()));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // Array of button names for the sidebar
        String[] sidebarButtons = {
                "Inventory",
                "Orders Today",
                "Orders Tomorrow",
                "Daily Reports",
                "Order Status",
                "Logout"
        };

        // Add buttons to sidebar with spacing
        for (String btnName : sidebarButtons) {
            JButton btn = new JButton(btnName);
            btn.setMaximumSize(new Dimension(180, 40));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Add action listener to update the main title when clicked
            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (btnName.equals("Logout")) {
                        JOptionPane.showMessageDialog(AdminDashboard.this, "Logging out...");
                        dispose(); // Close dashboard
                        new SuperMartMain().setVisible(true);
                    } else if (btnName.equals("Inventory") || btnName.equals("Order Status") || btnName.equals("Orders Today") || btnName.equals("Orders Tomorrow") || btnName.equals("Daily Reports")) {
                        if (cardLayout != null) {
                            // On switch, we might want to refresh, but for now just show
                            cardLayout.show(mainContentPanel, btnName);
                        }
                    } else {
                        // For under construction panels,make dummy ones on the fly or show
                        JPanel tempPanel = new JPanel(new GridBagLayout());
                        JLabel tempLabel = new JLabel(btnName + " - Under Construction");
                        tempLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
                        tempPanel.add(tempLabel);
                        mainContentPanel.add(tempPanel, btnName);
                        cardLayout.show(mainContentPanel, btnName);
                    }
                }
            });

            sidebarPanel.add(btn);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10))); // 10px spacing
        }

        // --- Main Content Area (Center) ---
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel();
        mainContentPanel.setLayout(cardLayout);

        // Home Panel
        JPanel homePanel = new JPanel(new GridBagLayout());
        JLabel welcomeLabel = new JLabel("Welcome Admin - " + email);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        homePanel.add(welcomeLabel);

        mainContentPanel.add(homePanel, "Home");
        mainContentPanel.add(createInventoryPanel(), "Inventory");
        mainContentPanel.add(createOrderStatusPanel(), "Order Status");
        mainContentPanel.add(createOrdersPeriodPanel("Same Day", "Orders Today"), "Orders Today");
        mainContentPanel.add(createOrdersPeriodPanel("Next Day", "Orders Tomorrow"), "Orders Tomorrow");
        mainContentPanel.add(createDailyReportsPanel(), "Daily Reports");

        // Start with Home
        cardLayout.show(mainContentPanel, "Home");

        // --- Bottom Area (South) ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout()); // Align left and right
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        globalStatsLabel = new JLabel("Total Orders Today: 0 | Revenue: ₹0");
        globalStatsLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        bottomPanel.add(globalStatsLabel, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(AdminDashboard.this, "Data Refreshed.");
            }
        });

        bottomPanel.add(refreshBtn, BorderLayout.EAST);

        // Add components to the frame
        add(sidebarPanel, BorderLayout.WEST);
        add(mainContentPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createInventoryPanel() {
        JPanel inventoryPanel = new JPanel(new BorderLayout());

        mainTitleLabel = new JLabel("Inventory", SwingConstants.CENTER);
        mainTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        mainTitleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        inventoryPanel.add(mainTitleLabel, BorderLayout.NORTH);

        // Top Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addProductBtn = new JButton("Add Product");
        JButton deleteSelectedBtn = new JButton("Delete Selected");
        JButton refreshBtn = new JButton("Refresh");
        JTextField searchField = new JTextField(15);
        searchField.setToolTipText("Search Product...");

        controlPanel.add(addProductBtn);
        controlPanel.add(deleteSelectedBtn);
        controlPanel.add(refreshBtn);
        controlPanel.add(new JLabel("Search Product:"));
        controlPanel.add(searchField);

        // Table Model
        String[] columns = { "ID", "Name", "Quantity", "Price(₹)", "Category", "Photo" };
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) {
                    return ImageIcon.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };

        JTable inventoryTable = new JTable(tableModel);
        inventoryTable.setRowHeight(60);
        inventoryTable.getColumnModel().getColumn(5).setPreferredWidth(60);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        inventoryTable.setRowSorter(sorter);

        // Filter Hook
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void updateFilter() {
                String text = searchField.getText();
                if (text.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFilter();
            }
        });

        // Context Menu via Right-Click
        inventoryTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int r = inventoryTable.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < inventoryTable.getRowCount()) {
                        inventoryTable.setRowSelectionInterval(r, r);
                    } else {
                        inventoryTable.clearSelection();
                    }

                    int rowindex = inventoryTable.getSelectedRow();
                    if (rowindex < 0)
                        return;

                    JPopupMenu popup = new JPopupMenu();
                    JMenuItem editItem = new JMenuItem("Edit");
                    JMenuItem deleteItem = new JMenuItem("Delete");

                    editItem.addActionListener(event -> {
                        int modelRow = inventoryTable.convertRowIndexToModel(rowindex);
                        showEditProductDialog(tableModel, modelRow);
                    });

                    deleteItem.addActionListener(event -> {
                        int modelRow = inventoryTable.convertRowIndexToModel(rowindex);
                        int id = (int) tableModel.getValueAt(modelRow, 0);
                        Database.products.removeIf(p -> p.getId() == id);
                        refreshInventoryTable(tableModel);
                    });

                    popup.add(editItem);
                    popup.add(deleteItem);
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(inventoryTable);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.add(controlPanel, BorderLayout.NORTH);
        centerWrapper.add(scrollPane, BorderLayout.CENTER);

        inventoryPanel.add(centerWrapper, BorderLayout.CENTER);

        // Button Actions
        addProductBtn.addActionListener(e -> showAddProductDialog(tableModel));

        refreshBtn.addActionListener(e -> refreshInventoryTable(tableModel));

        deleteSelectedBtn.addActionListener(e -> {
            int[] selectedRows = inventoryTable.getSelectedRows();
            if (selectedRows.length > 0) {
                int confirm = JOptionPane.showConfirmDialog(inventoryPanel,
                        "Delete the selected product(s)?", "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    for (int i = selectedRows.length - 1; i >= 0; i--) {
                        int modelRow = inventoryTable.convertRowIndexToModel(selectedRows[i]);
                        int id = (int) tableModel.getValueAt(modelRow, 0);
                        Database.products.removeIf(p -> p.getId() == id);
                    }
                    refreshInventoryTable(tableModel);
                }
            } else {
                JOptionPane.showMessageDialog(inventoryPanel, "Please select at least one product.", "No Selection",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        refreshInventoryTable(tableModel); // Initial data load
        return inventoryPanel;
    }

    private void refreshInventoryTable(DefaultTableModel tableModel) {
        tableModel.setRowCount(0); // clear
        for (Database.Product p : Database.products) {
            ImageIcon icon = null;
            if (p.getImagePath() != null && !p.getImagePath().isEmpty()) {
                ImageIcon originalIcon = new ImageIcon(p.getImagePath());
                Image scaledImg = originalIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaledImg);
            } else {
                icon = new ImageIcon(); // Empty space if no photo
            }
            tableModel.addRow(
                    new Object[] { p.getId(), p.getName(), p.getQuantity(), p.getPrice(), p.getCategory(), icon });
        }
    }

    private void showAddProductDialog(DefaultTableModel tableModel) {
        JDialog dialog = new JDialog(this, "Add Product", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(15);
        dialog.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("Price(₹):"), gbc);
        gbc.gridx = 1;
        JTextField priceField = new JTextField(15);
        dialog.add(priceField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        JTextField quantityField = new JTextField(15);
        dialog.add(quantityField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        String[] categories = { "Fruits", "Vegetables", "Dairy", "Other" };
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        dialog.add(categoryCombo, gbc);

        // Photo Upload Component
        gbc.gridx = 0;
        gbc.gridy = 4;
        dialog.add(new JLabel("Photo:"), gbc);
        gbc.gridx = 1;
        JPanel photoPanel = new JPanel(new BorderLayout(5, 0));
        JButton uploadPhotoBtn = new JButton("Upload...");
        JLabel photoLabel = new JLabel("No file selected");
        photoPanel.add(uploadPhotoBtn, BorderLayout.WEST);
        photoPanel.add(photoLabel, BorderLayout.CENTER);
        dialog.add(photoPanel, gbc);

        final String[] finalImagePath = new String[] { "" };
        uploadPhotoBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(dialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                finalImagePath[0] = fileChooser.getSelectedFile().getAbsolutePath();
                photoLabel.setText(fileChooser.getSelectedFile().getName());
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = 5; // Adjusted gridy
        gbc.gridwidth = 2;
        dialog.add(btnPanel, gbc);

        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                int qty = Integer.parseInt(quantityField.getText().trim());
                String cat = (String) categoryCombo.getSelectedItem();

                int maxId = 0;
                for (Database.Product p : Database.products) {
                    if (p.getId() > maxId)
                        maxId = p.getId();
                }

                Database.Product newProd = new Database.Product(maxId + 1, name, qty, price, cat, finalImagePath[0]);
                Database.products.add(newProd);
                refreshInventoryTable(tableModel);
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid number format for price or quantity.", "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private void showEditProductDialog(DefaultTableModel tableModel, int modelRow) {
        int targetId = (int) tableModel.getValueAt(modelRow, 0);
        Database.Product targetProduct = null;
        for (Database.Product p : Database.products) {
            if (p.getId() == targetId) {
                targetProduct = p;
                break;
            }
        }
        if (targetProduct == null)
            return;

        JDialog dialog = new JDialog(this, "Edit Product", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(15);
        nameField.setText(targetProduct.getName());
        dialog.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("Price(₹):"), gbc);
        gbc.gridx = 1;
        JTextField priceField = new JTextField(15);
        priceField.setText(String.valueOf(targetProduct.getPrice()));
        dialog.add(priceField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        JTextField quantityField = new JTextField(15);
        quantityField.setText(String.valueOf(targetProduct.getQuantity()));
        dialog.add(quantityField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        String[] categories = { "Fruits", "Vegetables", "Dairy", "Other" };
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        categoryCombo.setSelectedItem(targetProduct.getCategory());
        dialog.add(categoryCombo, gbc);

        // Photo Upload Component for Edit
        gbc.gridx = 0;
        gbc.gridy = 4;
        dialog.add(new JLabel("Photo:"), gbc);
        gbc.gridx = 1;
        JPanel photoPanel = new JPanel(new BorderLayout(5, 0));
        JButton uploadPhotoBtn = new JButton("Update...");
        JLabel photoLabel = new JLabel((targetProduct.getImagePath() == null || targetProduct.getImagePath().isEmpty())
                ? "No file selected"
                : new java.io.File(targetProduct.getImagePath()).getName());
        photoPanel.add(uploadPhotoBtn, BorderLayout.WEST);
        photoPanel.add(photoLabel, BorderLayout.CENTER);
        dialog.add(photoPanel, gbc);

        final String[] editImagePath = new String[] {
                targetProduct.getImagePath() != null ? targetProduct.getImagePath() : "" };
        uploadPhotoBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(dialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                editImagePath[0] = fileChooser.getSelectedFile().getAbsolutePath();
                photoLabel.setText(fileChooser.getSelectedFile().getName());
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = 5; // Adjusted gridy
        gbc.gridwidth = 2;
        dialog.add(btnPanel, gbc);

        cancelBtn.addActionListener(e -> dialog.dispose());

        final Database.Product toUpdate = targetProduct;
        saveBtn.addActionListener(e -> {
            try {
                toUpdate.setName(nameField.getText().trim());
                toUpdate.setPrice(Double.parseDouble(priceField.getText().trim()));
                toUpdate.setQuantity(Integer.parseInt(quantityField.getText().trim()));
                toUpdate.setCategory((String) categoryCombo.getSelectedItem());
                toUpdate.setImagePath(editImagePath[0]);

                refreshInventoryTable(tableModel);
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid number format for price or quantity.", "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private JPanel createDailyReportsPanel() {
        JPanel reportPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Daily Reports", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        reportPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel centerSplit = new JPanel(new GridLayout(1, 2, 20, 0));
        centerSplit.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // LEFT: Report Generator
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Orders Summary"));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Date (yyyy-MM-dd): "));
        JTextField dateField = new JTextField(10);
        dateField.setText(java.time.LocalDate.now().toString());
        JButton generateBtn = new JButton("Generate Report");
        inputPanel.add(dateField);
        inputPanel.add(generateBtn);

        String[] columns = { "Total Orders", "Total Revenue", "Same Day", "Next Day" };
        DefaultTableModel reportModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable reportTable = new JTable(reportModel);
        reportTable.setRowHeight(40);
        
        leftPanel.add(inputPanel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(reportTable), BorderLayout.CENTER);

        generateBtn.addActionListener(e -> {
            String targetDate = dateField.getText().trim();
            int ordersCount = 0;
            double revenue = 0;
            int sameDay = 0;
            int nextDay = 0;

            for(Database.Order o : Database.orders) {
                if(o.getDate().equals(targetDate)) {
                    ordersCount++;
                    revenue += o.getTotal();
                    if("Same Day".equals(o.getDeliveryType())) sameDay++;
                    if("Next Day".equals(o.getDeliveryType())) nextDay++;
                }
            }

            reportModel.setRowCount(0);
            reportModel.addRow(new Object[]{
                ordersCount,
                "₹" + String.format("%.2f", revenue),
                sameDay,
                nextDay
            });
        });
        
        // Mock a click to load default date
        generateBtn.doClick();

        // RIGHT: Top Products
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Top 5 Products Sold"));
        DefaultListModel<String> topListModel = new DefaultListModel<>();
        topListModel.addElement("1. Apples (Fuji) - 120 units");
        topListModel.addElement("2. Milk (1L) - 85 units");
        topListModel.addElement("3. Bread (Whole Wheat) - 64 units");
        topListModel.addElement("4. Fresh Eggs (Dozen) - 50 units");
        topListModel.addElement("5. Orange Juice - 30 units");
        
        JList<String> topProductsList = new JList<>(topListModel);
        topProductsList.setFont(new Font("SansSerif", Font.PLAIN, 16));
        rightPanel.add(new JScrollPane(topProductsList), BorderLayout.CENTER);

        centerSplit.add(leftPanel);
        centerSplit.add(rightPanel);

        reportPanel.add(centerSplit, BorderLayout.CENTER);

        return reportPanel;
    }

    private JPanel createOrdersPeriodPanel(String deliveryTypeFilter, String title) {
        JPanel orderPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        orderPanel.add(titleLabel, BorderLayout.NORTH);

        // Top Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton updateStatusBtn = new JButton("Update Status");
        JButton notifyBtn = new JButton("Notify Customer");
        JButton refreshBtn = new JButton("Refresh");

        controlPanel.add(refreshBtn);
        controlPanel.add(updateStatusBtn);
        controlPanel.add(notifyBtn);

        String[] columns = { "Order ID", "Customer Email", "Time", "Address", "Total(₹)", "Status", "Update Status" };
        DefaultTableModel orderModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only "Update Status" combobox is editable
            }
        };

        JTable orderTable = new JTable(orderModel);
        orderTable.setRowHeight(30);

        // Combo Box for Status Column
        String[] statusOptions = { "Pending", "Processing", "Delivered" };
        JComboBox<String> statusCombo = new JComboBox<>(statusOptions);
        orderTable.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(statusCombo));

        Runnable refreshOrders = () -> {
            orderModel.setRowCount(0);
            for(Database.Order o : Database.orders) {
                if (o.getDeliveryType() != null && o.getDeliveryType().equals(deliveryTypeFilter)) {
                    orderModel.addRow(new Object[] {
                            o.getOrderId(),
                            (o.getCustomerEmail() != null ? o.getCustomerEmail() : "N/A"),
                            o.getDate(),
                            o.getAddress().replace("\n", " "), // Flatten the address to show clearly as a row
                            "₹" + String.format("%.2f", o.getTotal()),
                            o.getStatus(),
                            o.getStatus() // Sets default combo box option implicitly visually
                    });
                }
            }
        };

        refreshBtn.addActionListener(e -> refreshOrders.run());

        // Update Button Logic
        updateStatusBtn.addActionListener(e -> {
            if (orderTable.isEditing()) {
                orderTable.getCellEditor().stopCellEditing(); // Commit combo box first
            }
            int selectedRow = orderTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = orderTable.convertRowIndexToModel(selectedRow);
                String orderId = orderModel.getValueAt(modelRow, 0).toString();
                String newStatus = orderModel.getValueAt(modelRow, 6).toString();
                
                // Write into our backend DB
                for(Database.Order o : Database.orders) {
                    if (o.getOrderId().equals(orderId)) {
                        o.setStatus(newStatus);
                        break;
                    }
                }
                refreshOrders.run();
                JOptionPane.showMessageDialog(orderPanel, "Status updated to " + newStatus + " for Order: " + orderId);
            } else {
                JOptionPane.showMessageDialog(orderPanel, "Please select an order to update data.");
            }
        });

        // Notify Customer Dialog Mock
        notifyBtn.addActionListener(e -> {
            int selectedRow = orderTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = orderTable.convertRowIndexToModel(selectedRow);
                String email = orderModel.getValueAt(modelRow, 1).toString();
                if(email.equals("N/A")) {
                    JOptionPane.showMessageDialog(orderPanel, "No email linked to this order!");
                } else {
                    JOptionPane.showMessageDialog(orderPanel, "Email Notification sent successfully to:\n" + email);
                }
            } else {
                JOptionPane.showMessageDialog(orderPanel, "Please select an order to ping customer.");
            }
        });

        // Add standard panel refresh hook here maybe
        refreshOrders.run();

        // Double click details hook
        orderTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && orderTable.getSelectedRow() != -1) {
                    int modelRow = orderTable.convertRowIndexToModel(orderTable.getSelectedRow());
                    String targetId = orderModel.getValueAt(modelRow, 0).toString();
                    Database.Order targetOrder = null;
                    for(Database.Order o : Database.orders) {
                        if (o.getOrderId().equals(targetId)) {
                            targetOrder = o;
                            break;
                        }
                    }
                    if(targetOrder != null) {
                        showOrderDetailsDialog(targetOrder);
                    }
                }
            }
        });

        // Assemble Sub-Components
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.add(controlPanel, BorderLayout.NORTH);
        centerWrapper.add(new JScrollPane(orderTable), BorderLayout.CENTER);

        orderPanel.add(centerWrapper, BorderLayout.CENTER);

        return orderPanel;
    }

    private JPanel createOrderStatusPanel() {
        JPanel orderPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Order Status", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        orderPanel.add(titleLabel, BorderLayout.NORTH);

        // Top Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] statusOptions = { "Pending", "Processing", "Delivered" };
        JComboBox<String> updateStatusCombo = new JComboBox<>(statusOptions);
        JButton updateStatusBtn = new JButton("Update Status");

        controlPanel.add(new JLabel("Update Status:"));
        controlPanel.add(updateStatusCombo);
        controlPanel.add(updateStatusBtn);

        String[] columns = { "Order ID", "Date", "Status", "Total", "Delivery Type" };
        DefaultTableModel orderModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable orderTable = new JTable(orderModel);
        orderTable.setRowHeight(30);

        Runnable refreshHook = () -> {
            orderModel.setRowCount(0);
            for(Database.Order o : Database.orders) {
                orderModel.addRow(new Object[] { o.getOrderId(), o.getDate(), o.getStatus(), "₹" + String.format("%.2f", o.getTotal()), o.getDeliveryType() });
            }
        };
        refreshHook.run();

        // Update logic map
        updateStatusBtn.addActionListener(e -> {
            int selectedRow = orderTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = orderTable.convertRowIndexToModel(selectedRow);
                String orderId = orderModel.getValueAt(modelRow, 0).toString();
                String newStatus = (String)updateStatusCombo.getSelectedItem();
                
                for(Database.Order o : Database.orders) {
                    if(o.getOrderId().equals(orderId)) {
                        o.setStatus(newStatus);
                        break;
                    }
                }
                refreshHook.run();
                JOptionPane.showMessageDialog(AdminDashboard.this, "Order " + orderId + " updated to " + newStatus);
            } else {
                JOptionPane.showMessageDialog(orderPanel, "Please select an order to update.");
            }
        });

        // Double click details hook
        orderTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && orderTable.getSelectedRow() != -1) {
                    int modelRow = orderTable.convertRowIndexToModel(orderTable.getSelectedRow());
                    String targetId = orderModel.getValueAt(modelRow, 0).toString();
                    Database.Order targetOrder = null;
                    for(Database.Order o : Database.orders) {
                        if (o.getOrderId().equals(targetId)) {
                            targetOrder = o;
                            break;
                        }
                    }
                    if(targetOrder != null) {
                        showOrderDetailsDialog(targetOrder);
                    }
                }
            }
        });

        // Assemble Sub-Components
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.add(controlPanel, BorderLayout.NORTH);
        centerWrapper.add(new JScrollPane(orderTable), BorderLayout.CENTER);

        orderPanel.add(centerWrapper, BorderLayout.CENTER);

        return orderPanel;
    }

    private void showOrderDetailsDialog(Database.Order order) {
        JDialog dialog = new JDialog(this, "Admin Order View - " + order.getOrderId(), true);
        dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLbl = new JLabel("Order Details: " + order.getOrderId());
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel itemsLbl = new JLabel("<html><b>Items:</b><br>" + order.getItemsSummary() + "</html>");
        JLabel addressLbl = new JLabel("<html><b>Address:</b><br>" + order.getAddress().replace("\n", "<br>") + "</html>");
        JLabel paymentLbl = new JLabel("<html><b>Payment Method:</b> " + order.getPaymentMethod() + "</html>");
        JLabel statusLbl = new JLabel("<html><b>Current Status:</b> " + order.getStatus() + "</html>");

        panel.add(titleLbl);
        panel.add(Box.createVerticalStrut(20));
        panel.add(itemsLbl);
        panel.add(Box.createVerticalStrut(15));
        panel.add(addressLbl);
        panel.add(Box.createVerticalStrut(15));
        panel.add(paymentLbl);
        panel.add(Box.createVerticalStrut(15));
        panel.add(statusLbl);
        panel.add(Box.createVerticalGlue());

        JButton closeBtn = new JButton("Close");
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.addActionListener(e -> dialog.dispose());
        panel.add(closeBtn);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        // Enforce default Swing cross-platform logic (Metal)
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new AdminDashboard("admin@test.com").setVisible(true);
            }
        });
    }
}

