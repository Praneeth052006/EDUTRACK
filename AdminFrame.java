import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

/**
 * Admin Dashboard Frame - Teacher Management
 */
public class AdminFrame extends JFrame {
    
    private JTable teacherTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> departmentFilter;
    private JLabel totalTeachersLabel, departmentsLabel, totalClassesLabel, activeLabel;
    
    public AdminFrame() {
        initializeUI();
        loadStatistics();
        loadTeachers();
    }
    
    private void initializeUI() {
        setTitle("EduTrack - Admin Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Main container
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        container.add(headerPanel, BorderLayout.NORTH);
        
        // Statistics Panel
        JPanel statsPanel = createStatisticsPanel();
        container.add(statsPanel, BorderLayout.CENTER);
        
        // Table Panel
        JPanel tablePanel = createTablePanel();
        container.add(tablePanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(79, 70, 229));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // Left side - Logo and title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("EduTrack Admin");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Teacher Management Portal");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(220, 220, 255));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        
        leftPanel.add(titlePanel);
        
        // Right side - User info and logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(new Color(79, 70, 229));
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> logout());
        
        rightPanel.add(logoutButton);
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createStatisticsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        statsPanel.setBackground(Color.WHITE);
        
        // Create stat cards
        JPanel totalTeachersCard = createStatCard("Total Teachers", "0", new Color(79, 70, 229));
        JPanel departmentsCard = createStatCard("Departments", "8", new Color(16, 185, 129));
        JPanel totalClassesCard = createStatCard("Total Classes", "45", new Color(245, 158, 11));
        JPanel activeCard = createStatCard("Active Today", "0", new Color(239, 68, 68));
        
        statsPanel.add(totalTeachersCard);
        statsPanel.add(departmentsCard);
        statsPanel.add(totalClassesCard);
        statsPanel.add(activeCard);
        
        return statsPanel;
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 32));
        valueLabel.setForeground(color);
        
        // Store reference for updating
        if (title.equals("Total Teachers")) {
            totalTeachersLabel = valueLabel;
        } else if (title.equals("Active Today")) {
            activeLabel = valueLabel;
        }
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        
        // Search and filter panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        
        searchField = new JTextField(20);
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                filterTeachers();
            }
        });
        
        departmentFilter = new JComboBox<>(new String[]{
            "All Departments", "Mathematics", "Science", "English", "History",
            "Computer Science", "Physics", "Chemistry", "Biology"
        });
        departmentFilter.addActionListener(e -> filterTeachers());
        
        JButton addButton = new JButton("Add Teacher");
        addButton.setBackground(new Color(79, 70, 229));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> showAddTeacherDialog());
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Department:"));
        searchPanel.add(departmentFilter);
        searchPanel.add(addButton);
        
        // Table
        String[] columnNames = {"ID", "Name", "Email", "Department", "Subject", "Classes", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        teacherTable = new JTable(tableModel);
        teacherTable.setRowHeight(40);
        teacherTable.setFont(new Font("Arial", Font.PLAIN, 12));
        teacherTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        teacherTable.getTableHeader().setBackground(new Color(243, 244, 246));
        
        JScrollPane scrollPane = new JScrollPane(teacherTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private void loadStatistics() {
        try {
            // Count total teachers
            ResultSet rs = DatabaseConfig.executeQuery(
                "SELECT COUNT(*) as count FROM teachers");
            if (rs.next()) {
                totalTeachersLabel.setText(String.valueOf(rs.getInt("count")));
            }
            rs.close();
            
            // Count active teachers
            rs = DatabaseConfig.executeQuery(
                "SELECT COUNT(*) as count FROM teachers WHERE status = 'Active'");
            if (rs.next()) {
                activeLabel.setText(String.valueOf(rs.getInt("count")));
            }
            rs.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading statistics: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadTeachers() {
        try {
            tableModel.setRowCount(0);
            
            String query = "SELECT teacher_code, full_name, " +
                          "(SELECT email FROM users WHERE user_id = teachers.user_id) as email, " +
                          "department, subject, classes, status " +
                          "FROM teachers ORDER BY teacher_code";
            
            ResultSet rs = DatabaseConfig.executeQuery(query);
            
            while (rs.next()) {
                String teacherCode = rs.getString("teacher_code");
                String name = rs.getString("full_name");
                String email = rs.getString("email");
                String department = rs.getString("department");
                String subject = rs.getString("subject");
                
                // Handle PostgreSQL array
                Array classesArray = rs.getArray("classes");
                String classes = "";
                if (classesArray != null) {
                    String[] classArr = (String[]) classesArray.getArray();
                    classes = String.join(", ", classArr);
                }
                
                String status = rs.getString("status");
                
                tableModel.addRow(new Object[]{
                    teacherCode, name, email, department, subject, classes, status
                });
            }
            
            rs.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading teachers: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filterTeachers() {
        String searchText = searchField.getText().toLowerCase();
        String selectedDept = (String) departmentFilter.getSelectedItem();
        
        try {
            tableModel.setRowCount(0);
            
            StringBuilder query = new StringBuilder(
                "SELECT teacher_code, full_name, " +
                "(SELECT email FROM users WHERE user_id = teachers.user_id) as email, " +
                "department, subject, classes, status FROM teachers WHERE 1=1"
            );
            
            if (!searchText.isEmpty()) {
                query.append(" AND (LOWER(full_name) LIKE '%").append(searchText)
                     .append("%' OR LOWER(teacher_code) LIKE '%").append(searchText)
                     .append("%' OR LOWER(subject) LIKE '%").append(searchText).append("%')");
            }
            
            if (!selectedDept.equals("All Departments")) {
                query.append(" AND department = '").append(selectedDept).append("'");
            }
            
            query.append(" ORDER BY teacher_code");
            
            ResultSet rs = DatabaseConfig.executeQuery(query.toString());
            
            while (rs.next()) {
                String teacherCode = rs.getString("teacher_code");
                String name = rs.getString("full_name");
                String email = rs.getString("email");
                String department = rs.getString("department");
                String subject = rs.getString("subject");
                
                Array classesArray = rs.getArray("classes");
                String classes = "";
                if (classesArray != null) {
                    String[] classArr = (String[]) classesArray.getArray();
                    classes = String.join(", ", classArr);
                }
                
                String status = rs.getString("status");
                
                tableModel.addRow(new Object[]{
                    teacherCode, name, email, department, subject, classes, status
                });
            }
            
            rs.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void showAddTeacherDialog() {
        JDialog dialog = new JDialog(this, "Add New Teacher", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(8, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> deptCombo = new JComboBox<>(new String[]{
            "Mathematics", "Physics", "Chemistry", "Biology",
            "Computer Science", "English", "History"
        });
        JTextField subjectField = new JTextField();
        JTextField classesField = new JTextField();
        
        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Department:"));
        panel.add(deptCombo);
        panel.add(new JLabel("Subject:"));
        panel.add(subjectField);
        panel.add(new JLabel("Classes (comma separated):"));
        panel.add(classesField);
        
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields!");
                return;
            }
            
            if (addTeacher(nameField.getText(), emailField.getText(),
                          new String(passwordField.getPassword()),
                          (String)deptCombo.getSelectedItem(),
                          subjectField.getText(), classesField.getText())) {
                dialog.dispose();
                loadTeachers();
                loadStatistics();
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        panel.add(saveButton);
        panel.add(cancelButton);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private boolean addTeacher(String name, String email, String password,
                              String dept, String subject, String classes) {
        try {
            // First, insert user
            String userQuery = "INSERT INTO users (email, password, role) VALUES (?, ?, 'teacher') RETURNING user_id";
            PreparedStatement pstmt = DatabaseConfig.prepareStatement(userQuery);
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            int userId = 0;
            if (rs.next()) {
                userId = rs.getInt("user_id");
            }
            rs.close();
            pstmt.close();
            
            // Generate teacher code
            String codeQuery = "SELECT 'T' || LPAD((COUNT(*) + 1)::text, 3, '0') as code FROM teachers";
            rs = DatabaseConfig.executeQuery(codeQuery);
            String teacherCode = "T001";
            if (rs.next()) {
                teacherCode = rs.getString("code");
            }
            rs.close();
            
            // Insert teacher
            String teacherQuery = "INSERT INTO teachers (user_id, teacher_code, full_name, " +
                                 "department, subject, classes, status) VALUES (?, ?, ?, ?, ?, ?, 'Active')";
            pstmt = DatabaseConfig.prepareStatement(teacherQuery);
            pstmt.setInt(1, userId);
            pstmt.setString(2, teacherCode);
            pstmt.setString(3, name);
            pstmt.setString(4, dept);
            pstmt.setString(5, subject);
            
            // Convert classes string to array
            String[] classArray = classes.split(",");
            for (int i = 0; i < classArray.length; i++) {
                classArray[i] = classArray[i].trim();
            }
            Array sqlArray = DatabaseConfig.getConnection().createArrayOf("text", classArray);
            pstmt.setArray(6, sqlArray);
            
            pstmt.executeUpdate();
            pstmt.close();
            
            JOptionPane.showMessageDialog(this, "Teacher added successfully!");
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error adding teacher: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        }
    }
    public static void main(String[] args) {
    javax.swing.SwingUtilities.invokeLater(() -> {
        new AdminFrame().setVisible(true);
    });
}

}