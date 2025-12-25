import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Login Frame for EduTrack
 * Authenticates users from the database and opens respective dashboards.
 */
public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JButton loginButton;

    public LoginFrame() {
        setTitle("EduTrack - Login");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initializeUI();
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Header
        JLabel titleLabel = new JLabel("EduTrack Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(79, 70, 229));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Center form
        JPanel formPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        formPanel.setBackground(Color.WHITE);

        emailField = new JTextField();
        passwordField = new JPasswordField();

        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);

        loginButton = new JButton("Login");
        loginButton.setBackground(new Color(79, 70, 229));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> login());

        formPanel.add(loginButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Status bar
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(Color.RED);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void login() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both email and password.");
            return;
        }

        try {
            String query = "SELECT user_id, role FROM users WHERE email = ? AND password = ?";
            PreparedStatement stmt = DatabaseConfig.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                int userId = rs.getInt("user_id");

                statusLabel.setText("Login successful!");
                dispose(); // Close login window

                // Open dashboard based on role
                SwingUtilities.invokeLater(() -> {
                    if ("admin".equalsIgnoreCase(role)) {
                        new AdminFrame().setVisible(true);
                    } else if ("teacher".equalsIgnoreCase(role)) {
                        // You may fetch teacher_class dynamically here if needed
                        new TeacherFrame(userId, "Class A").setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(null, "Unknown user role: " + role);
                    }
                });

            } else {
                statusLabel.setText("Invalid email or password!");
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Database error: " + e.getMessage(),
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
