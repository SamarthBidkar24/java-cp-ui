import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

public class SuperMartMain extends JFrame {

    public SuperMartMain() {
        // Pre-populate some dummy accounts
        if (Database.customers.isEmpty()) {
            Database.customers.add(new Database.Customer("Admin", "admin@supermart.com", "0000000000", "admin123"));
            Database.customers.add(new Database.Customer("Test Customer", "customer@test.com", "1234567890", "pass123"));
        }
        setTitle("SuperMart Online Ordering System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainContainer = new JPanel(new BorderLayout());

        // Top Title
        JLabel titleLabel = new JLabel("SuperMart Online Ordering", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        mainContainer.add(titleLabel, BorderLayout.NORTH);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 16));

        tabbedPane.addTab("Login", createLoginPanel());
        tabbedPane.addTab("Register", createRegisterPanel());

        mainContainer.add(tabbedPane, BorderLayout.CENTER);
        add(mainContainer);
    }

    private JPanel createLoginPanel() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Email
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel emailLbl = new JLabel("Email:");
        emailLbl.setFont(new Font("SansSerif", Font.PLAIN, 16));
        loginPanel.add(emailLbl, gbc);

        gbc.gridx = 1;
        JTextField emailField = new JTextField(20);
        emailField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        loginPanel.add(emailField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel pwdLbl = new JLabel("Password:");
        pwdLbl.setFont(new Font("SansSerif", Font.PLAIN, 16));
        loginPanel.add(pwdLbl, gbc);

        gbc.gridx = 1;
        JPasswordField pwdField = new JPasswordField(20);
        pwdField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        loginPanel.add(pwdField, gbc);

        // Separate Admin & Customer Login Buttons
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JPanel roleBtnOuter = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        JButton adminLoginBtn = new JButton("Admin Login");
        JButton customerLoginBtn = new JButton("Customer Login");

        adminLoginBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        customerLoginBtn.setFont(new Font("SansSerif", Font.BOLD, 14));

        roleBtnOuter.add(adminLoginBtn);
        roleBtnOuter.add(customerLoginBtn);

        loginPanel.add(roleBtnOuter, gbc);

        // --- Action Listeners ---
        // Helper to validate and return if success
        validateAndProceed(customerLoginBtn, emailField, pwdField, false);
        validateAndProceed(adminLoginBtn, emailField, pwdField, true);

        return loginPanel;
    }

    private void validateAndProceed(JButton button, JTextField emailField, JPasswordField pwdField, boolean isAdmin) {
        button.addActionListener(e -> {
            String email = emailField.getText().trim();
            String pwd = new String(pwdField.getPassword());

            if (email.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Email and Password are required.", "Login Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Invalid email format.", "Login Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Database.Customer matchedCustomer = null;
            for(Database.Customer c : Database.customers) {
                if(c.getEmail().equalsIgnoreCase(email)) {
                    matchedCustomer = c;
                    break;
                }
            }

            if (matchedCustomer != null && matchedCustomer.getPassword().equals(pwd)) {
                // Success
                if (isAdmin) {
                    new AdminDashboard(email).setVisible(true);
                } else {
                    new CustomerDashboard(matchedCustomer).setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials. Please try again or register.", "Login Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private JPanel createRegisterPanel() {
        JPanel registerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
        Font fieldFont = new Font("SansSerif", Font.PLAIN, 14);

        // Full Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLbl = new JLabel("Full Name:");
        nameLbl.setFont(labelFont);
        registerPanel.add(nameLbl, gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(20);
        nameField.setFont(fieldFont);
        registerPanel.add(nameField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel emailLbl = new JLabel("Email:");
        emailLbl.setFont(labelFont);
        registerPanel.add(emailLbl, gbc);
        gbc.gridx = 1;
        JTextField emailField = new JTextField(20);
        emailField.setFont(fieldFont);
        registerPanel.add(emailField, gbc);

        // Phone
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel phoneLbl = new JLabel("Phone:");
        phoneLbl.setFont(labelFont);
        registerPanel.add(phoneLbl, gbc);
        gbc.gridx = 1;
        JTextField phoneField = new JTextField(20);
        phoneField.setFont(fieldFont);
        registerPanel.add(phoneField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel pwdLbl = new JLabel("Password:");
        pwdLbl.setFont(labelFont);
        registerPanel.add(pwdLbl, gbc);
        gbc.gridx = 1;
        JPasswordField pwdField = new JPasswordField(20);
        pwdField.setFont(fieldFont);
        registerPanel.add(pwdField, gbc);

        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel confirmPwdLbl = new JLabel("Confirm Password:");
        confirmPwdLbl.setFont(labelFont);
        registerPanel.add(confirmPwdLbl, gbc);
        gbc.gridx = 1;
        JPasswordField confirmPwdField = new JPasswordField(20);
        confirmPwdField.setFont(fieldFont);
        registerPanel.add(confirmPwdField, gbc);

        // Register Button
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        JButton registerBtn = new JButton("Register");
        registerBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        registerPanel.add(registerBtn, gbc);

        registerBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String pwd = new String(pwdField.getPassword());
            String confirmPwd = new String(confirmPwdField.getPassword());

            // 1. Required fields
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pwd.isEmpty() || confirmPwd.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Registration Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Email format validation
            if (!isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Invalid email format.", "Registration Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. Password match
            if (!pwd.equals(confirmPwd)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Registration Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean exists = false;
            for(Database.Customer c : Database.customers) {
                if(c.getEmail().equalsIgnoreCase(email)) {
                    exists = true;
                    break;
                }
            }

            if (exists) {
                JOptionPane.showMessageDialog(this, "An account with this email already exists.", "Registration Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Success - add to map
            Database.customers.add(new Database.Customer(name, email, phone, pwd));

            // Clear fields
            nameField.setText("");
            emailField.setText("");
            phoneField.setText("");
            pwdField.setText("");
            confirmPwdField.setText("");

            JOptionPane.showMessageDialog(this, "Registration successful! You may now login.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        // Add padding around the panel
        JPanel outerPanel = new JPanel();
        outerPanel.add(registerPanel);
        return outerPanel;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pat = Pattern.compile(emailRegex);
        return email != null && pat.matcher(email).matches();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new SuperMartMain().setVisible(true));
    }
}
