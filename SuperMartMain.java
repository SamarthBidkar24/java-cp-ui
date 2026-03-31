import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

public class SuperMartMain extends JFrame {

    public SuperMartMain() {
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

            if (isAdmin) {
                AdminDAO adminDAO = new AdminDAO();
                Admin matchedAdmin = adminDAO.loginAdmin(email, pwd);
                if (matchedAdmin != null) {
                    new AdminDashboard(email).setVisible(true);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid admin credentials.", "Login Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                CustomerDAO customerDAO = new CustomerDAO();
                Customer matchedCustomer = customerDAO.login(email, pwd);
                if (matchedCustomer != null) {
                    new CustomerDashboard(matchedCustomer).setVisible(true);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid customer credentials.", "Login Error",
                            JOptionPane.ERROR_MESSAGE);
                }
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

        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        registerPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(20);
        registerPanel.add(nameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 1;
        registerPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        JTextField emailField = new JTextField(20);
        registerPanel.add(emailField, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 2;
        registerPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        JTextField phoneField = new JTextField(20);
        registerPanel.add(phoneField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 3;
        registerPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField pwdField = new JPasswordField(20);
        registerPanel.add(pwdField, gbc);

        // Confirm
        gbc.gridx = 0; gbc.gridy = 4;
        registerPanel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField confirmPwdField = new JPasswordField(20);
        registerPanel.add(confirmPwdField, gbc);

        // Role
        gbc.gridx = 0; gbc.gridy = 5;
        registerPanel.add(new JLabel("Register As:"), gbc);
        gbc.gridx = 1;
        String[] roles = {"Customer", "Admin"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);
        registerPanel.add(roleCombo, gbc);

        // Register Button
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JButton registerBtn = new JButton("Register");
        registerBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        registerPanel.add(registerBtn, gbc);

        registerBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String pwd = new String(pwdField.getPassword());
            String confirmPwd = new String(confirmPwdField.getPassword());
            String selectedRole = (String) roleCombo.getSelectedItem();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pwd.isEmpty() || confirmPwd.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Invalid email format.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!pwd.equals(confirmPwd)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean registered = false;
            if ("Admin".equals(selectedRole)) {
                AdminDAO adminDAO = new AdminDAO();
                if (adminDAO.emailExists(email)) {
                    JOptionPane.showMessageDialog(this, "Admin email already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                registered = adminDAO.registerAdmin(name, email, phone, pwd);
            } else {
                CustomerDAO customerDAO = new CustomerDAO();
                if (customerDAO.emailExists(email)) {
                    JOptionPane.showMessageDialog(this, "Customer email already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                registered = customerDAO.registerCustomer(name, email, phone, pwd);
            }

            if (registered) {
                JOptionPane.showMessageDialog(this, "Registration successful!");
                nameField.setText("");
                emailField.setText("");
                phoneField.setText("");
                pwdField.setText("");
                confirmPwdField.setText("");
                roleCombo.setSelectedIndex(0);
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

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
