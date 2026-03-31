import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.table.DefaultTableModel;

public class CustomerDashboard extends JFrame {

    private java.util.List<OrderItem> cartList = new java.util.ArrayList<>();
    private int cartCount = 0;
    private JLabel cartCountLabel;
    private CardLayout cardLayout;
    private JPanel centerPanel;
    private DefaultTableModel cartTableModel;
    private JLabel subtotalLabel, deliveryLabel, totalLabel;
    private double subtotal = 0;
    private int deliveryFee = 30;
    private Customer currentUser;
    private Runnable refreshOrdersData;

    public CustomerDashboard(Customer user) {
        this.currentUser = user;
        // Set window properties
        setTitle("Customer Dashboard - SuperMart");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setLayout(new BorderLayout());

        // --- Top Navigation Panel (North) ---
        JPanel topNavPanel = new JPanel();
        topNavPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10)); // Horizontal flow with spacing

        JButton homeBtn = new JButton("Home");
        JButton cartBtn = new JButton("Cart");
        cartCountLabel = new JLabel("(0)");
        cartCountLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        cartCountLabel.setForeground(Color.BLUE);

        JButton ordersBtn = new JButton("Orders");
        JButton profileBtn = new JButton("Profile");
        JButton logoutBtn = new JButton("Logout");

        // Group Cart Button and Counter Label together visually
        JPanel cartContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        cartContainer.add(cartBtn);
        cartContainer.add(cartCountLabel);

        topNavPanel.add(homeBtn);
        topNavPanel.add(cartContainer);
        topNavPanel.add(ordersBtn);
        topNavPanel.add(profileBtn);
        topNavPanel.add(logoutBtn);

        // Logout Action
        logoutBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(CustomerDashboard.this, "Logging out...");
            dispose();
            new SuperMartMain().setVisible(true);
        });

        // --- Dynamic Home Panel Integration ---
        JPanel homePanelContainer = createHomePanel();

        // --- Cart Panel Initialization ---
        JPanel cartPanel = createCartPanel();

        // --- Setup CardLayout Center ---
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.add(homePanelContainer, "HOME");
        centerPanel.add(cartPanel, "CART");
        centerPanel.add(createOrdersPanel(), "ORDERS");
        centerPanel.add(createProfilePanel(), "PROFILE");

        // Map UI Actions for Navigation
        homeBtn.addActionListener(e -> {
            // refresh data whenever coming back to home
            if (gridPanel != null && searchProductsField != null) {
                renderProductGrid(gridPanel, searchProductsField.getText());
            }
            cardLayout.show(centerPanel, "HOME");
        });
        cartBtn.addActionListener(e -> cardLayout.show(centerPanel, "CART"));
        ordersBtn.addActionListener(e -> {
            if(refreshOrdersData != null) refreshOrdersData.run();
            cardLayout.show(centerPanel, "ORDERS");
        });
        profileBtn.addActionListener(e -> cardLayout.show(centerPanel, "PROFILE"));

        // Add panels to frame
        add(topNavPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel gridPanel;
    private JTextField searchProductsField;

    private JPanel createHomePanel() {
        JPanel homeOuter = new JPanel(new BorderLayout());

        // Top Search Toolbar
        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchProductsField = new JTextField(25);
        JButton clearSearchBtn = new JButton("Clear");

        searchBarPanel.add(new JLabel("Search Products:"));
        searchBarPanel.add(searchProductsField);
        searchBarPanel.add(clearSearchBtn);

        homeOuter.add(searchBarPanel, BorderLayout.NORTH);

        // Center Database.Product Grid (2 columns)
        gridPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JScrollPane scrollGrid = new JScrollPane(gridPanel);
        // Faster scrolling
        scrollGrid.getVerticalScrollBar().setUnitIncrement(16);

        homeOuter.add(scrollGrid, BorderLayout.CENTER);

        // Behavior Hooks
        clearSearchBtn.addActionListener(e -> {
            searchProductsField.setText("");
            renderProductGrid(gridPanel, "");
        });

        searchProductsField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                renderProductGrid(gridPanel, searchProductsField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                renderProductGrid(gridPanel, searchProductsField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                renderProductGrid(gridPanel, searchProductsField.getText());
            }
        });

        // Initial Load
        renderProductGrid(gridPanel, "");

        return homeOuter;
    }

    private void renderProductGrid(JPanel grid, String searchQuery) {
        grid.removeAll();
        String query = searchQuery.toLowerCase().trim();

        AdminDAO adminDAO = new AdminDAO();
        for (Product p : adminDAO.getAllProducts()) {
            // Only show products with stock
            if (p.getQuantity() > 0) {
                // Apply search filter if present
                if (query.isEmpty() || p.getName().toLowerCase().contains(query)) {
                    grid.add(createProductCard(p));
                }
            }
        }
        grid.revalidate();
        grid.repaint();
    }

    // Helper method to create individual product cards mapping to Database.Product object
    private JPanel createProductCard(Product p) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        card.setBackground(Color.WHITE);

        // Process Image
        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(150, 100));
        imageLabel.setMaximumSize(new Dimension(150, 100));
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(240, 240, 240));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        if (p.getImagePath() != null && !p.getImagePath().isEmpty()) {
            ImageIcon originalIcon = new ImageIcon(p.getImagePath());
            Image scaledImg = originalIcon.getImage().getScaledInstance(150, 100, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImg));
        } else {
            imageLabel.setText("No Image");
        }

        // Database.Product Details
        JLabel idLabel = new JLabel("ID: " + p.getId());
        idLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        idLabel.setForeground(Color.GRAY);
        idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(p.getName());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel priceLabel = new JLabel("₹" + String.format("%.2f", p.getPrice()));
        priceLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        priceLabel.setForeground(new Color(0, 128, 0)); // Green color for price
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel qtyLabel = new JLabel("Qty: " + p.getQuantity());
        qtyLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        qtyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add to Cart Button
        JButton addToCartBtn = new JButton("Add to Cart");
        addToCartBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addToCartBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean found = false;
                for (OrderItem item : cartList) {
                    if (item.product.getId() == p.getId()) {
                        if (item.qty < p.getQuantity()) {
                            item.qty++;
                        } else {
                            JOptionPane.showMessageDialog(card, "Max stock reached!");
                            return;
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    cartList.add(new OrderItem(p, 1));
                }
                updateCartBadge();
                renderCartTable();
                JOptionPane.showMessageDialog(card, p.getName() + " added to cart!");
            }
        });

        // Add Spacing and Components to Card
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(idLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(imageLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(nameLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(priceLabel);
        card.add(Box.createRigidArea(new Dimension(0, 2)));
        card.add(qtyLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(addToCartBtn);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        return card;
    }

    // Builder method for the Cart Panel layout
    private JPanel createCartPanel() {
        JPanel cartOuter = new JPanel(new BorderLayout());
        cartOuter.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Cart Table
        String[] columns = { "Product", "Qty +/-", "Price(₹)", "Total(₹)", "Remove" };
        cartTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1 || column == 4;
            }
        };

        JTable cartTable = new JTable(cartTableModel);
        cartTable.setRowHeight(40);
        
        cartTable.getColumnModel().getColumn(1).setCellRenderer(new QtyCellRenderer());
        cartTable.getColumnModel().getColumn(1).setCellEditor(new QtyCellEditor(new JCheckBox()));
        cartTable.getColumnModel().getColumn(4).setCellRenderer(new RemoveCellRenderer());
        cartTable.getColumnModel().getColumn(4).setCellEditor(new RemoveCellEditor(new JCheckBox()));

        JScrollPane tableScroll = new JScrollPane(cartTable);
        tableScroll.setPreferredSize(new Dimension(800, 200));

        // 2. Checkout Container underneath Table
        JPanel checkoutPanel = new JPanel();
        checkoutPanel.setLayout(new BoxLayout(checkoutPanel, BoxLayout.Y_AXIS));
        checkoutPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // 2A. Pricing Breakdown
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        subtotalLabel = new JLabel("Subtotal: ₹0.00");
        deliveryLabel = new JLabel("Delivery: ₹" + deliveryFee);
        totalLabel = new JLabel("Grand Total: ₹0.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        pricePanel.add(subtotalLabel);
        pricePanel.add(deliveryLabel);
        pricePanel.add(totalLabel);
        checkoutPanel.add(pricePanel);

        // 2B. Shipping Speed
        JPanel shippingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        shippingPanel.setBorder(BorderFactory.createTitledBorder("Delivery Speed"));
        JRadioButton sameDayBtn = new JRadioButton("Same Day (+₹50)");
        JRadioButton nextDayBtn = new JRadioButton("Next Day (+₹30)", true);
        ButtonGroup shippingGroup = new ButtonGroup();
        shippingGroup.add(sameDayBtn);
        shippingGroup.add(nextDayBtn);
        shippingPanel.add(sameDayBtn);
        shippingPanel.add(nextDayBtn);

        ActionListener deliveryListener = e -> {
            if (sameDayBtn.isSelected()) deliveryFee = 50;
            else if (nextDayBtn.isSelected()) deliveryFee = 30;
            renderCartTable(); // Update total calculation
        };
        sameDayBtn.addActionListener(deliveryListener);
        nextDayBtn.addActionListener(deliveryListener);
        checkoutPanel.add(shippingPanel);

        // 2C. Address & Maps
        JPanel mapPanel = new JPanel(new BorderLayout(10, 0));
        mapPanel.setBorder(BorderFactory.createTitledBorder("Delivery Address"));
        JTextArea addressArea = new JTextArea(5, 40);
        mapPanel.add(new JScrollPane(addressArea), BorderLayout.CENTER);
        checkoutPanel.add(mapPanel);

        // 2D. Payment & Place Order Layout
        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        paymentPanel.add(new JLabel("Payment Method:"));
        String[] paymentTypes = { "UPI" };
        JComboBox<String> paymentCombo = new JComboBox<>(paymentTypes);
        paymentPanel.add(paymentCombo);

        JButton placeOrderBtn = new JButton("Place Order");
        placeOrderBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        placeOrderBtn.setBackground(new Color(46, 204, 113));
        placeOrderBtn.setForeground(Color.WHITE);

        placeOrderBtn.addActionListener(e -> {
            if (cartList.isEmpty()) {
                JOptionPane.showMessageDialog(cartOuter, "Your cart is empty!", "Cart Empty", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (addressArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(cartOuter, "Please enter a delivery address.", "Missing Address", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String fakeOrderId = "ORD-" + (int) (Math.random() * 1000000);
            
            StringBuilder summary = new StringBuilder();
            for(OrderItem item : cartList) {
                summary.append(item.product.getName()).append(" (x").append(item.qty).append("), ");
            }
            if(summary.length() > 2) summary.setLength(summary.length() - 2);

            String status = "Pending";
            String date = java.time.LocalDate.now().toString();
            String delType = sameDayBtn.isSelected() ? "Same Day" : "Next Day";
            
            Order order = new Order(fakeOrderId, date, status, subtotal + deliveryFee, delType, addressArea.getText().trim(), (String)paymentCombo.getSelectedItem(), summary.toString(), currentUser.getEmail());
            OrderDAO orderDAO = new OrderDAO();
            boolean success = orderDAO.placeOrder(order);

            if (success) {
                JOptionPane.showMessageDialog(cartOuter, "Order " + fakeOrderId + " placed successfully!", "Order Placed", JOptionPane.INFORMATION_MESSAGE);
                cartList.clear();
                addressArea.setText("");
                updateCartBadge();
                renderCartTable();
                cardLayout.show(centerPanel, "HOME");
            } else {
                JOptionPane.showMessageDialog(cartOuter, "Failed to place order. Database error.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        paymentPanel.add(placeOrderBtn);
        checkoutPanel.add(paymentPanel);

        cartOuter.add(tableScroll, BorderLayout.CENTER);
        cartOuter.add(checkoutPanel, BorderLayout.SOUTH);

        return cartOuter;
    }

    private void updateCartBadge() {
        cartCount = 0;
        for(OrderItem item : cartList) {
            cartCount += item.qty;
        }
        cartCountLabel.setText("(" + cartCount + ")");
    }

    private void renderCartTable() {
        if(cartTableModel == null) return;
        cartTableModel.setRowCount(0);
        subtotal = 0;
        for (OrderItem item : cartList) {
            double itemTotal = item.product.getPrice() * item.qty;
            subtotal += itemTotal;
            cartTableModel.addRow(new Object[] { 
                item.product.getName(), 
                item.qty, 
                "₹" + String.format("%.2f", item.product.getPrice()), 
                "₹" + String.format("%.2f", itemTotal), 
                "Remove" 
            });
        }
        subtotalLabel.setText("Subtotal: ₹" + String.format("%.2f", subtotal));
        deliveryLabel.setText("Delivery: ₹" + deliveryFee);
        totalLabel.setText("Grand Total: ₹" + String.format("%.2f", (subtotal + deliveryFee)));
    }

    // --- Custom Renderers & Editors ---
    class QtyPanel extends JPanel {
        JButton minusBtn = new JButton("-");
        JButton plusBtn = new JButton("+");
        JLabel qtyLabel = new JLabel("0", SwingConstants.CENTER);
        public QtyPanel() {
            setLayout(new BorderLayout());
            minusBtn.setMargin(new Insets(2, 5, 2, 5));
            plusBtn.setMargin(new Insets(2, 5, 2, 5));
            qtyLabel.setPreferredSize(new Dimension(30, 20));
            add(minusBtn, BorderLayout.WEST);
            add(qtyLabel, BorderLayout.CENTER);
            add(plusBtn, BorderLayout.EAST);
        }
    }

    class QtyCellRenderer extends QtyPanel implements javax.swing.table.TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value != null) qtyLabel.setText(value.toString());
            return this;
        }
    }

    class QtyCellEditor extends DefaultCellEditor {
        private QtyPanel panel;
        private int currentRow;
        public QtyCellEditor(JCheckBox checkBox) {
            super(checkBox);
            panel = new QtyPanel();
            panel.minusBtn.addActionListener(e -> {
                if (currentRow >= 0 && currentRow < cartList.size()) {
                    OrderItem item = cartList.get(currentRow);
                    if (item.qty > 1) {
                        item.qty--;
                        fireEditingStopped();
                        renderCartTable();
                        updateCartBadge();
                    } else {
                        fireEditingStopped();
                    }
                } else {
                    fireEditingStopped();
                }
            });
            panel.plusBtn.addActionListener(e -> {
                if (currentRow >= 0 && currentRow < cartList.size()) {
                    OrderItem item = cartList.get(currentRow);
                    if (item.qty < item.product.getQuantity()) {
                        item.qty++;
                        fireEditingStopped();
                        renderCartTable();
                        updateCartBadge();
                    } else {
                        fireEditingStopped();
                        JOptionPane.showMessageDialog(null, "Max stock reached for " + item.product.getName());
                    }
                } else {
                    fireEditingStopped();
                }
            });
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = table.convertRowIndexToModel(row);
            if (value != null) panel.qtyLabel.setText(value.toString());
            return panel;
        }
        @Override
        public Object getCellEditorValue() {
            return panel.qtyLabel.getText();
        }
    }

    class RemoveCellRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public RemoveCellRenderer() {
            setText("Remove");
            setForeground(Color.RED);
            setFocusPainted(false);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class RemoveCellEditor extends DefaultCellEditor {
        private JButton button;
        private int currentRow;
        public RemoveCellEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Remove");
            button.setForeground(Color.RED);
            button.setFocusPainted(false);
            button.addActionListener(e -> {
                if (currentRow >= 0 && currentRow < cartList.size()) {
                    cartList.remove(currentRow);
                    fireEditingStopped();
                    renderCartTable();
                    updateCartBadge();
                } else {
                    fireEditingStopped();
                }
            });
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = table.convertRowIndexToModel(row);
            return button;
        }
        @Override
        public Object getCellEditorValue() {
            return "Remove";
        }
    }

    // Builder method for the Orders Panel
    private JPanel createOrdersPanel() {
        JPanel ordersOuter = new JPanel(new BorderLayout(0, 10));
        ordersOuter.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh Orders");
        topPnl.add(refreshBtn);
        ordersOuter.add(topPnl, BorderLayout.NORTH);

        String[] columns = { "Order ID", "Date", "Status", "Total(₹)", "Delivery" };
        DefaultTableModel ordersModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable ordersTable = new JTable(ordersModel);
        ordersTable.setRowHeight(30);

        refreshOrdersData = () -> {
            ordersModel.setRowCount(0);
            OrderDAO orderDAO = new OrderDAO();
            java.util.List<Order> orders = orderDAO.getOrdersByCustomer(currentUser.getEmail());
            for(Order o : orders) {
                ordersModel.addRow(new Object[]{ o.getOrderId(), o.getDate(), o.getStatus(), "₹" + String.format("%.2f", o.getTotal()), o.getDeliveryType() });
            }
        };
        refreshOrdersData.run();
        refreshBtn.addActionListener(e -> refreshOrdersData.run());

        // Double-click to view details
        ordersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && ordersTable.getSelectedRow() != -1) {
                    int row = ordersTable.convertRowIndexToModel(ordersTable.getSelectedRow());
                    String targetId = ordersModel.getValueAt(row, 0).toString();
                    
                    OrderDAO orderDAO = new OrderDAO();
                    Order targetOrder = null;
                    for(Order o : orderDAO.getOrdersByCustomer(currentUser.getEmail())) {
                        if(o.getOrderId().equals(targetId)) {
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

        ordersOuter.add(new JScrollPane(ordersTable), BorderLayout.CENTER);
        return ordersOuter;
    }

    private void showOrderDetailsDialog(Order order) {
        JDialog dialog = new JDialog(this, "Order Details - " + order.getOrderId(), true);
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
        JLabel statusLbl = new JLabel("<html><b>Status:</b> <font color='blue'>" + order.getStatus() + "</font></html>");

        panel.add(titleLbl);
        panel.add(Box.createVerticalStrut(15));
        panel.add(itemsLbl);
        panel.add(Box.createVerticalStrut(10));
        panel.add(addressLbl);
        panel.add(Box.createVerticalStrut(10));
        panel.add(paymentLbl);
        panel.add(Box.createVerticalStrut(10));
        panel.add(statusLbl);
        panel.add(Box.createVerticalGlue());

        JButton closeBtn = new JButton("Close");
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.addActionListener(e -> dialog.dispose());
        panel.add(closeBtn);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private JPanel createProfilePanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("SansSerif", Font.BOLD, 14);
        Font valueFont = new Font("SansSerif", Font.PLAIN, 14);

        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLbl = new JLabel("Name:"); nameLbl.setFont(labelFont);
        outer.add(nameLbl, gbc);
        gbc.gridx = 1;
        JLabel nameVal = new JLabel(currentUser.getName()); nameVal.setFont(valueFont);
        outer.add(nameVal, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel emailLbl = new JLabel("Email:"); emailLbl.setFont(labelFont);
        outer.add(emailLbl, gbc);
        gbc.gridx = 1;
        JLabel emailVal = new JLabel(currentUser.getEmail()); emailVal.setFont(valueFont);
        outer.add(emailVal, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel phoneLbl = new JLabel("Phone:"); phoneLbl.setFont(labelFont);
        outer.add(phoneLbl, gbc);
        gbc.gridx = 1;
        JLabel phoneVal = new JLabel(currentUser.getPhone()); phoneVal.setFont(valueFont);
        outer.add(phoneVal, gbc);

        // Separator
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        outer.add(new JSeparator(), gbc);

        // Password Update Form
        gbc.gridy = 4; gbc.gridwidth = 1;
        JLabel curPwdLbl = new JLabel("Current Password:"); curPwdLbl.setFont(labelFont);
        outer.add(curPwdLbl, gbc);
        gbc.gridx = 1;
        JPasswordField curPwdField = new JPasswordField(15);
        outer.add(curPwdField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        JLabel newPwdLbl = new JLabel("New Password:"); newPwdLbl.setFont(labelFont);
        outer.add(newPwdLbl, gbc);
        gbc.gridx = 1;
        JPasswordField newPwdField = new JPasswordField(15);
        outer.add(newPwdField, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        JLabel confirmPwdLbl = new JLabel("Confirm New:"); confirmPwdLbl.setFont(labelFont);
        outer.add(confirmPwdLbl, gbc);
        gbc.gridx = 1;
        JPasswordField confirmPwdField = new JPasswordField(15);
        outer.add(confirmPwdField, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        JButton updateBtn = new JButton("Update Password");
        updateBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        outer.add(updateBtn, gbc);

        updateBtn.addActionListener(e -> {
            String current = new String(curPwdField.getPassword());
            String newPwd = new String(newPwdField.getPassword());
            String confirm = new String(confirmPwdField.getPassword());

            if (current.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) {
                JOptionPane.showMessageDialog(outer, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!current.equals(currentUser.getPassword())) {
                JOptionPane.showMessageDialog(outer, "Current password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!newPwd.equals(confirm)) {
                JOptionPane.showMessageDialog(outer, "New passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            currentUser.setPassword(newPwd);
            JOptionPane.showMessageDialog(outer, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
            curPwdField.setText("");
            newPwdField.setText("");
            confirmPwdField.setText("");
        });

        return outer;
    }
}

