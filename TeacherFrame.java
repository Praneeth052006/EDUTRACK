
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

/**
 * Teacher Dashboard Frame - Student Management
 */
public class TeacherFrame extends JFrame {
    
    private int teacherId;
    private String teacherName;
    private String[] teacherClasses;
    private String currentClass = "10A";
    
    private JTabbedPane tabbedPane;
    private JComboBox<String> classSelector;
    
    // Statistics labels
    private JLabel totalStudentsLabel, presentTodayLabel, avgMarksLabel, feePendingLabel;
    
    // Tables
    private JTable studentsTable, attendanceTable, marksTable, feesTable;
    private DefaultTableModel studentsModel, attendanceModel, marksModel, feesModel;
    
    public TeacherFrame(int teacherId) {
        this.teacherId = teacherId;
        loadTeacherInfo();
        initializeUI();
        loadStatistics();
        loadStudents();
    }
    
    private void loadTeacherInfo() {
        try {
            String query = "SELECT full_name, classes FROM teachers WHERE teacher_id = ?";
            PreparedStatement pstmt = DatabaseConfig.prepareStatement(query);
            pstmt.setInt(1, teacherId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                teacherName = rs.getString("full_name");
                Array classesArray = rs.getArray("classes");
                if (classesArray != null) {
                    teacherClasses = (String[]) classesArray.getArray();
                    if (teacherClasses.length > 0) {
                        currentClass = teacherClasses[0];
                    }
                }
            }
            
            DatabaseConfig.closeResources(rs, pstmt);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void initializeUI() {
        setTitle("EduTrack - Teacher Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        
        // Main container
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        container.add(headerPanel, BorderLayout.NORTH);
        
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.setBackground(new Color(249, 250, 251));
        
        // Class selector panel
        JPanel classSelectorPanel = createClassSelectorPanel();
        contentPanel.add(classSelectorPanel, BorderLayout.NORTH);
        
        // Stats panel
        JPanel statsPanel = createStatsPanel();
        
        // Tabbed pane for different sections
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));
        
        tabbedPane.addTab("👥 Students", createStudentsPanel());
        tabbedPane.addTab("📅 Attendance", createAttendancePanel());
        tabbedPane.addTab("📊 Marks", createMarksPanel());
        tabbedPane.addTab("💰 Fees", createFeesPanel());
        tabbedPane.addTab("📚 Materials", createMaterialsPanel());
        
        // Center panel with stats and tabs
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(statsPanel, BorderLayout.NORTH);
        centerPanel.add(tabbedPane, BorderLayout.CENTER);
        
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        container.add(contentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(102, 126, 234));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // Left side
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("EduTrack Teacher Portal");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel welcomeLabel = new JLabel("Welcome, " + teacherName);
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        welcomeLabel.setForeground(new Color(220, 220, 255));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(welcomeLabel);
        
        leftPanel.add(titlePanel);
        
        // Right side
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(new Color(102, 126, 234));
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> logout());
        
        rightPanel.add(logoutButton);
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createClassSelectorPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel label = new JLabel("Select Class:");
        label.setFont(new Font("Arial", Font.BOLD, 14));
        
        classSelector = new JComboBox<>(teacherClasses);
        classSelector.setPreferredSize(new Dimension(150, 35));
        classSelector.setFont(new Font("Arial", Font.PLAIN, 14));
        classSelector.addActionListener(e -> {
            currentClass = (String) classSelector.getSelectedItem();
            refreshAllData();
        });
        
        panel.add(label);
        panel.add(classSelector);
        
        return panel;
    }
    
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(new Color(249, 250, 251));
        
        JPanel totalCard = createStatCard("Total Students", "0", new Color(59, 130, 246));
        JPanel presentCard = createStatCard("Present Today", "0", new Color(16, 185, 129));
        JPanel marksCard = createStatCard("Avg. Marks", "0%", new Color(139, 92, 246));
        JPanel feeCard = createStatCard("Fee Pending", "0", new Color(239, 68, 68));
        
        statsPanel.add(totalCard);
        statsPanel.add(presentCard);
        statsPanel.add(marksCard);
        statsPanel.add(feeCard);
        
        return statsPanel;
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setForeground(Color.GRAY);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setForeground(color);
        
        // Store references
        if (title.equals("Total Students")) {
            totalStudentsLabel = valueLabel;
        } else if (title.equals("Present Today")) {
            presentTodayLabel = valueLabel;
        } else if (title.equals("Avg. Marks")) {
            avgMarksLabel = valueLabel;
        } else if (title.equals("Fee Pending")) {
            feePendingLabel = valueLabel;
        }
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Top button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton addButton = new JButton("Add Student");
        addButton.setBackground(new Color(79, 70, 229));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> showAddStudentDialog());
        
        buttonPanel.add(addButton);
        
        // Table
        String[] columns = {"Roll No", "Name", "Age", "Father's Name", "Phone", "Email", "Actions"};
        studentsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only actions column
            }
        };
        
        studentsTable = new JTable(studentsModel);
        studentsTable.setRowHeight(50);
        studentsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        studentsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        studentsTable.getTableHeader().setBackground(new Color(249, 250, 251));
        
        // Add button renderer and editor for actions column
        studentsTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        studentsTable.getColumn("Actions").setCellEditor(new ButtonEditor(new JCheckBox(), studentsTable, this));
        
        JScrollPane scrollPane = new JScrollPane(studentsTable);
        
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Top panel with date and buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        
        JLabel dateLabel = new JLabel("Date:");
        JTextField dateField = new JTextField(LocalDate.now().toString());
        dateField.setPreferredSize(new Dimension(150, 30));
        
        JButton markAllPresentBtn = new JButton("Mark All Present");
        markAllPresentBtn.setBackground(new Color(16, 185, 129));
        markAllPresentBtn.setForeground(Color.WHITE);
        markAllPresentBtn.setFocusPainted(false);
        markAllPresentBtn.addActionListener(e -> markAllPresent());
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadAttendance());
        
        topPanel.add(dateLabel);
        topPanel.add(dateField);
        topPanel.add(markAllPresentBtn);
        topPanel.add(refreshBtn);
        
        // Table
        String[] columns = {"Roll No", "Name", "Status", "Actions"};
        attendanceModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };
        
        attendanceTable = new JTable(attendanceModel);
        attendanceTable.setRowHeight(50);
        attendanceTable.setFont(new Font("Arial", Font.PLAIN, 12));
        attendanceTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Add button renderer for attendance actions
        attendanceTable.getColumn("Actions").setCellRenderer(new AttendanceButtonRenderer());
        attendanceTable.getColumn("Actions").setCellEditor(
            new AttendanceButtonEditor(new JCheckBox(), attendanceTable, this));
        
        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createMarksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Top button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton saveButton = new JButton("Save All Marks");
        saveButton.setBackground(new Color(79, 70, 229));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> saveAllMarks());
        
        buttonPanel.add(saveButton);
        
        // Table
        String[] columns = {"Roll No", "Name", "Unit 1", "Unit 2", "Mid Term", "Final", "Total", "Grade"};
        marksModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 2 && column <= 5; // Mark columns editable
            }
        };
        
        marksTable = new JTable(marksModel);
        marksTable.setRowHeight(40);
        marksTable.setFont(new Font("Arial", Font.PLAIN, 12));
        marksTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(marksTable);
        
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFeesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Top panel with month selector
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        
        JLabel monthLabel = new JLabel("Month:");
        JComboBox<String> monthCombo = new JComboBox<>(new String[]{
            "January 2024", "February 2024", "March 2024", "April 2024"
        });
        
        topPanel.add(monthLabel);
        topPanel.add(monthCombo);
        
        // Table
        String[] columns = {"Roll No", "Name", "Amount", "Status", "Actions"};
        feesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };
        
        feesTable = new JTable(feesModel);
        feesTable.setRowHeight(50);
        feesTable.setFont(new Font("Arial", Font.PLAIN, 12));
        feesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Add button renderer for fee actions
        feesTable.getColumn("Actions").setCellRenderer(new FeeButtonRenderer());
        feesTable.getColumn("Actions").setCellEditor(new FeeButtonEditor(new JCheckBox(), feesTable, this));
        
        JScrollPane scrollPane = new JScrollPane(feesTable);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createMaterialsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JButton addMaterialBtn = new JButton("Add Material");
        addMaterialBtn.setBackground(new Color(79, 70, 229));
        addMaterialBtn.setForeground(Color.WHITE);
        addMaterialBtn.setFocusPainted(false);
        addMaterialBtn.addActionListener(e -> showAddMaterialDialog());
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(addMaterialBtn);
        
        JPanel materialsGrid = new JPanel(new GridLayout(0, 3, 15, 15));
        materialsGrid.setBackground(Color.WHITE);
        materialsGrid.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        loadMaterials(materialsGrid);
        
        JScrollPane scrollPane = new JScrollPane(materialsGrid);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Continued in Part 2...
    
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
    
    // Default no-arg constructor for quick testing
    public TeacherFrame() {
        // Use default test values for teacherId and class
        this(1, "Class A");
    }

    // Overloaded constructor to allow setting initial class explicitly
    public TeacherFrame(int teacherId, String initialClass) {
        this.teacherId = teacherId;
        loadTeacherInfo();
        // Override current class if an explicit value is provided (useful for tests)
        if (initialClass != null && !initialClass.isEmpty()) {
            this.currentClass = initialClass;
        }
        initializeUI();
        loadStatistics();
        loadStudents();
    }
    
    private void loadStatistics() {
        try {
            // Total students
            String query = "SELECT COUNT(*) as count FROM students WHERE class_name = ? AND teacher_id = ?";
            PreparedStatement pstmt = DatabaseConfig.prepareStatement(query);
            pstmt.setString(1, currentClass);
            pstmt.setInt(2, teacherId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                totalStudentsLabel.setText(String.valueOf(rs.getInt("count")));
            }
            rs.close();
            pstmt.close();

            // Present today
            query = "SELECT COUNT(*) as count FROM attendance a " +
                   "JOIN students s ON a.student_id = s.student_id " +
                   "WHERE s.class_name = ? AND s.teacher_id = ? " +
                   "AND a.attendance_date = CURRENT_DATE AND a.status = 'Present'";
            pstmt = DatabaseConfig.prepareStatement(query);
            pstmt.setString(1, currentClass);
            pstmt.setInt(2, teacherId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                presentTodayLabel.setText(String.valueOf(rs.getInt("count")));
            }
            rs.close();
            pstmt.close();

            // Average marks
            query = "SELECT AVG((unit1 + unit2 + midterm + final)/4.0) as avg " +
                   "FROM marks m JOIN students s ON m.student_id = s.student_id " +
                   "WHERE s.class_name = ? AND s.teacher_id = ?";
            pstmt = DatabaseConfig.prepareStatement(query);
            pstmt.setString(1, currentClass);
            pstmt.setInt(2, teacherId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                int avg = (int) rs.getDouble("avg");
                avgMarksLabel.setText(avg + "%");
            }
            rs.close();
            pstmt.close();

            // Fee pending
            query = "SELECT COUNT(*) as count FROM fees f " +
                   "JOIN students s ON f.student_id = s.student_id " +
                   "WHERE s.class_name = ? AND s.teacher_id = ? AND f.status = 'Pending'";
            pstmt = DatabaseConfig.prepareStatement(query);
            pstmt.setString(1, currentClass);
            pstmt.setInt(2, teacherId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                feePendingLabel.setText(String.valueOf(rs.getInt("count")));
            }
            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new TeacherFrame().setVisible(true);
            }
        });
    }
 
// Add these methods to TeacherFrame class
    
    
    
    private void loadStudents() {
        try {
            studentsModel.setRowCount(0);
            
            String query = "SELECT * FROM students WHERE class_name = ? AND teacher_id = ? ORDER BY roll_no";
            PreparedStatement pstmt = DatabaseConfig.prepareStatement(query);
            pstmt.setString(1, currentClass);
            pstmt.setInt(2, teacherId);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                studentsModel.addRow(new Object[]{
                    rs.getString("roll_no"),
                    rs.getString("full_name"),
                    rs.getInt("age"),
                    rs.getString("father_name"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    "Actions"
                });
            }
            
            DatabaseConfig.closeResources(rs, pstmt);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadAttendance() {
        try {
            attendanceModel.setRowCount(0);
            
            String query = "SELECT s.student_id, s.roll_no, s.full_name, " +
                          "COALESCE(a.status, 'Absent') as status " +
                          "FROM students s " +
                          "LEFT JOIN attendance a ON s.student_id = a.student_id " +
                          "AND a.attendance_date = CURRENT_DATE " +
                          "WHERE s.class_name = ? AND s.teacher_id = ? " +
                          "ORDER BY s.roll_no";
            
            PreparedStatement pstmt = DatabaseConfig.prepareStatement(query);
            pstmt.setString(1, currentClass);
            pstmt.setInt(2, teacherId);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                attendanceModel.addRow(new Object[]{
                    rs.getString("roll_no"),
                    rs.getString("full_name"),
                    rs.getString("status"),
                    "Actions"
                });
            }
            
            DatabaseConfig.closeResources(rs, pstmt);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadMarks() {
        try {
            marksModel.setRowCount(0);
            
            String query = "SELECT s.student_id, s.roll_no, s.full_name, " +
                          "COALESCE(m.unit1, 0) as unit1, COALESCE(m.unit2, 0) as unit2, " +
                          "COALESCE(m.midterm, 0) as midterm, COALESCE(m.final, 0) as final " +
                          "FROM students s " +
                          "LEFT JOIN marks m ON s.student_id = m.student_id " +
                          "WHERE s.class_name = ? AND s.teacher_id = ? " +
                          "ORDER BY s.roll_no";
            
            PreparedStatement pstmt = DatabaseConfig.prepareStatement(query);
            pstmt.setString(1, currentClass);
            pstmt.setInt(2, teacherId);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int unit1 = rs.getInt("unit1");
                int unit2 = rs.getInt("unit2");
                int midterm = rs.getInt("midterm");
                int finalMark = rs.getInt("final");
                int total = unit1 + unit2 + midterm + finalMark;
                String grade = calculateGrade((int)(total / 4.0));
                
                marksModel.addRow(new Object[]{
                    rs.getString("roll_no"),
                    rs.getString("full_name"),
                    unit1,
                    unit2,
                    midterm,
                    finalMark,
                    total + "/400",
                    grade
                });
            }
            
            DatabaseConfig.closeResources(rs, pstmt);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadFees() {
        try {
            feesModel.setRowCount(0);
            
            String query = "SELECT s.student_id, s.roll_no, s.full_name, " +
                          "COALESCE(f.amount, 5000.00) as amount, " +
                          "COALESCE(f.status, 'Pending') as status " +
                          "FROM students s " +
                          "LEFT JOIN fees f ON s.student_id = f.student_id " +
                          "AND f.month = 'January' AND f.year = 2024 " +
                          "WHERE s.class_name = ? AND s.teacher_id = ? " +
                          "ORDER BY s.roll_no";
            
            PreparedStatement pstmt = DatabaseConfig.prepareStatement(query);
            pstmt.setString(1, currentClass);
            pstmt.setInt(2, teacherId);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                feesModel.addRow(new Object[]{
                    rs.getString("roll_no"),
                    rs.getString("full_name"),
                    "₹" + rs.getDouble("amount"),
                    rs.getString("status"),
                    "Actions"
                });
            }
            
            DatabaseConfig.closeResources(rs, pstmt);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadMaterials(JPanel grid) {
        try {
            grid.removeAll();
            
            String query = "SELECT * FROM study_materials " +
                          "WHERE teacher_id = ? AND class_name = ? " +
                          "ORDER BY upload_date DESC";
            
            PreparedStatement pstmt = DatabaseConfig.prepareStatement(query);
            pstmt.setInt(1, teacherId);
            pstmt.setString(2, currentClass);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                JPanel materialCard = createMaterialCard(
                    rs.getInt("material_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("material_type"),
                    rs.getString("upload_date")
                );
                grid.add(materialCard);
            }
            
            DatabaseConfig.closeResources(rs, pstmt);
            
            grid.revalidate();
            grid.repaint();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private JPanel createMaterialCard(int materialId, String title, String description,
                                     String type, String date) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JTextArea descArea = new JTextArea(description);
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setOpaque(false);
        descArea.setFont(new Font("Arial", Font.PLAIN, 11));
        
        JLabel typeLabel = new JLabel(type.toUpperCase());
        typeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        typeLabel.setForeground(Color.GRAY);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        buttonPanel.setOpaque(false);
        
        JButton downloadBtn = new JButton("Download");
        JButton shareBtn = new JButton("Share");
        JButton deleteBtn = new JButton("Delete");
        
        downloadBtn.setPreferredSize(new Dimension(90, 25));
        shareBtn.setPreferredSize(new Dimension(90, 25));
        deleteBtn.setPreferredSize(new Dimension(90, 25));
        
        deleteBtn.addActionListener(e -> deleteMaterial(materialId));
        
        buttonPanel.add(downloadBtn);
        buttonPanel.add(shareBtn);
        buttonPanel.add(deleteBtn);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(descArea, BorderLayout.CENTER);
        topPanel.add(typeLabel, BorderLayout.SOUTH);
        
        card.add(topPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.SOUTH);
        
        return card;
    }
    
    public void showAddStudentDialog() {
        JDialog dialog = new JDialog(this, "Add New Student", true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(9, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextField nameField = new JTextField();
        JTextField rollNoField = new JTextField();
        JTextField ageField = new JTextField();
        JTextField fatherNameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JTextArea addressArea = new JTextArea(3, 20);
        
        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Roll Number:"));
        panel.add(rollNoField);
        panel.add(new JLabel("Age:"));
        panel.add(ageField);
        panel.add(new JLabel("Father's Name:"));
        panel.add(fatherNameField);
        panel.add(new JLabel("Phone:"));
        panel.add(phoneField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Address:"));
        panel.add(new JScrollPane(addressArea));
        
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        
        saveBtn.addActionListener(e -> {
            if (addStudent(nameField.getText(), rollNoField.getText(),
                          Integer.parseInt(ageField.getText()),
                          fatherNameField.getText(), phoneField.getText(),
                          emailField.getText(), addressArea.getText())) {
                dialog.dispose();
                refreshAllData();
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        panel.add(saveBtn);
        panel.add(cancelBtn);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private boolean addStudent(String name, String rollNo, int age,
                               String fatherName, String phone, String email, String address) {
        try {
            String query = "INSERT INTO students (roll_no, full_name, age, class_name, " +
                          "father_name, phone, email, address, teacher_id) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = DatabaseConfig.prepareStatement(query);
            pstmt.setString(1, rollNo);
            pstmt.setString(2, name);
            pstmt.setInt(3, age);
            pstmt.setString(4, currentClass);
            pstmt.setString(5, fatherName);
            pstmt.setString(6, phone);
            pstmt.setString(7, email);
            pstmt.setString(8, address);
            pstmt.setInt(9, teacherId);
            
            pstmt.executeUpdate();
            pstmt.close();
            
            JOptionPane.showMessageDialog(this, "Student added successfully!");
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error adding student: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public void editStudent(int row) {
        String rollNo = (String) studentsModel.getValueAt(row, 0);
        // Implementation similar to add student, but load existing data
        JOptionPane.showMessageDialog(this, "Edit student: " + rollNo);
    }
    
    public void viewStudent(int row) {
        String rollNo = (String) studentsModel.getValueAt(row, 0);
        String name = (String) studentsModel.getValueAt(row, 1);
        
        JDialog dialog = new JDialog(this, "Student Details", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setText("Student Details\n\n" +
                           "Roll No: " + rollNo + "\n" +
                           "Name: " + name + "\n" +
                           "Class: " + currentClass);
        
        dialog.add(new JScrollPane(detailsArea));
        dialog.setVisible(true);
    }
    
    public void markAttendance(int row, String status) {
        try {
            String rollNo = (String) attendanceModel.getValueAt(row, 0);
            
            // Get student ID
            String query = "SELECT student_id FROM students WHERE roll_no = ?";
            PreparedStatement pstmt = DatabaseConfig.prepareStatement(query);
            pstmt.setString(1, rollNo);
            ResultSet rs = pstmt.executeQuery();
            
            int studentId = 0;
            if (rs.next()) {
                studentId = rs.getInt("student_id");
            }
            rs.close();
            pstmt.close();
            
            // Insert or update attendance
            query = "INSERT INTO attendance (student_id, attendance_date, status, marked_by) " +
                   "VALUES (?, CURRENT_DATE, ?, ?) " +
                   "ON CONFLICT (student_id, attendance_date) " +
                   "DO UPDATE SET status = ?, marked_by = ?";
            
            pstmt = DatabaseConfig.prepareStatement(query);
            pstmt.setInt(1, studentId);
            pstmt.setString(2, status);
            pstmt.setInt(3, teacherId);
            pstmt.setString(4, status);
            pstmt.setInt(5, teacherId);
            
            pstmt.executeUpdate();
            pstmt.close();
            
            loadAttendance();
            loadStatistics();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void markAllPresent() {
        try {
            String query = "INSERT INTO attendance (student_id, attendance_date, status, marked_by) " +
                          "SELECT s.student_id, CURRENT_DATE, 'Present', ? " +
                          "FROM students s WHERE s.class_name = ? AND s.teacher_id = ? " +
                          "ON CONFLICT (student_id, attendance_date) " +
                          "DO UPDATE SET status = 'Present', marked_by = ?";
            
            PreparedStatement pstmt = DatabaseConfig.prepareStatement(query);
            pstmt.setInt(1, teacherId);
            pstmt.setString(2, currentClass);
            pstmt.setInt(3, teacherId);
            pstmt.setInt(4, teacherId);
            
            pstmt.executeUpdate();
            pstmt.close();
            
            loadAttendance();
            loadStatistics();
            
            JOptionPane.showMessageDialog(this, "All students marked present!");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void saveAllMarks() {
        try {
            for (int i = 0; i < marksModel.getRowCount(); i++) {
                String rollNo = (String) marksModel.getValueAt(i, 0);
                int unit1 = Integer.parseInt(marksModel.getValueAt(i, 2).toString());
                int unit2 = Integer.parseInt(marksModel.getValueAt(i, 3).toString());
                int midterm = Integer.parseInt(marksModel.getValueAt(i, 4).toString());
                int finalMark = Integer.parseInt(marksModel.getValueAt(i, 5).toString());
                
                // Get student ID
                String query = "SELECT student_id FROM students WHERE roll_no = ?";
                PreparedStatement pstmt = DatabaseConfig.prepareStatement(query);
                pstmt.setString(1, rollNo);
                ResultSet rs = pstmt.executeQuery();
                
                int studentId = 0;
                if (rs.next()) {
                    studentId = rs.getInt("student_id");
                }
                rs.close();
                pstmt.close();
                
                // Insert or update marks
                query = "INSERT INTO marks (student_id, unit1, unit2, midterm, final, updated_by) " +
                       "VALUES (?, ?, ?, ?, ?, ?) " +
                       "ON CONFLICT (student_id) " +
                       "DO UPDATE SET unit1 = ?, unit2 = ?, midterm = ?, final = ?, updated_by = ?";
                
                pstmt = DatabaseConfig.prepareStatement(query);
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, unit1);
                pstmt.setInt(3, unit2);
                pstmt.setInt(4, midterm);
                pstmt.setInt(5, finalMark);
                pstmt.setInt(6, teacherId);
                pstmt.setInt(7, unit1);
                pstmt.setInt(8, unit2);
                pstmt.setInt(9, midterm);
                pstmt.setInt(10, finalMark);
                pstmt.setInt(11, teacherId);
                
                pstmt.executeUpdate();
                pstmt.close();
            }
            
            JOptionPane.showMessageDialog(this, "All marks saved successfully!");
            loadMarks();
            loadStatistics();
            
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error saving marks: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void toggleFeeStatus(int row) {
        try {
            String rollNo = (String) feesModel.getValueAt(row, 0);
            String currentStatus = (String) feesModel.getValueAt(row, 3);
            String newStatus = currentStatus.equals("Paid") ? "Pending" : "Paid";
            
            // Get student ID
            String query = "SELECT student_id FROM students WHERE roll_no = ?";
            PreparedStatement pstmt = DatabaseConfig.prepareStatement(query);
            pstmt.setString(1, rollNo);
            ResultSet rs = pstmt.executeQuery();
            
            int studentId = 0;
            if (rs.next()) {
                studentId = rs.getInt("student_id");
            }
            rs.close();
            pstmt.close();
            
            // Update fee status
            query = "INSERT INTO fees (student_id, month, year, status, payment_date) " +
                   "VALUES (?, 'January', 2024, ?, ?) " +
                   "ON CONFLICT (student_id, month, year) " +
                   "DO UPDATE SET status = ?, payment_date = ?";
            
            Date paymentDate = newStatus.equals("Paid") ? new Date(System.currentTimeMillis()) : null;
            
            pstmt = DatabaseConfig.prepareStatement(query);
            pstmt.setInt(1, studentId);
            pstmt.setString(2, newStatus);
            pstmt.setDate(3, paymentDate);
            pstmt.setString(4, newStatus);
            pstmt.setDate(5, paymentDate);
            
            pstmt.executeUpdate();
            pstmt.close();
            
            loadFees();
            loadStatistics();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void showAddMaterialDialog() {
        JDialog dialog = new JDialog(this, "Add Study Material", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextField titleField = new JTextField();
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{
            "notes", "assignment", "previous_paper", "reference"
        });
        JTextArea descArea = new JTextArea(3, 20);
        
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Type:"));
        panel.add(typeCombo);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(descArea));
        
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        
        saveBtn.addActionListener(e -> {
            if (addMaterial(titleField.getText(), (String)typeCombo.getSelectedItem(),
                          descArea.getText())) {
                dialog.dispose();
                JPanel materialsGrid = (JPanel)((JScrollPane)((JPanel)tabbedPane.getComponentAt(4))
                    .getComponent(1)).getViewport().getView();
                loadMaterials(materialsGrid);
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        panel.add(saveBtn);
        panel.add(cancelBtn);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private boolean addMaterial(String title, String type, String description) {
        try {
            String query = "INSERT INTO study_materials (teacher_id, title, description, " +
                          "material_type, class_name) VALUES (?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = DatabaseConfig.prepareStatement(query);
            pstmt.setInt(1, teacherId);
            pstmt.setString(2, title);
            pstmt.setString(3, description);
            pstmt.setString(4, type);
            pstmt.setString(5, currentClass);
            
            pstmt.executeUpdate();
            pstmt.close();
            
            JOptionPane.showMessageDialog(this, "Material added successfully!");
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void deleteMaterial(int materialId) {
        try {
            String query = "DELETE FROM study_materials WHERE material_id = ?";
            PreparedStatement pstmt = DatabaseConfig.prepareStatement(query);
            pstmt.setInt(1, materialId);
            pstmt.executeUpdate();
            pstmt.close();
            
            JPanel materialsGrid = (JPanel)((JScrollPane)((JPanel)tabbedPane.getComponentAt(4))
                .getComponent(1)).getViewport().getView();
            loadMaterials(materialsGrid);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void refreshAllData() {
        loadStatistics();
        loadStudents();
        loadAttendance();
        loadMarks();
        loadFees();
    }
    
    private String calculateGrade(int marks) {
        if (marks >= 90) return "A+";
        if (marks >= 80) return "A";
        if (marks >= 70) return "B+";
        if (marks >= 60) return "B";
        if (marks >= 50) return "C";
        return "F";
    }

}

// Additional renderer and editor classes for Attendance and Fees

class AttendanceButtonRenderer extends JPanel implements TableCellRenderer {
    private JButton presentBtn;
    private JButton absentBtn;
    
    public AttendanceButtonRenderer() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        presentBtn = new JButton("Present");
        absentBtn = new JButton("Absent");
        
        presentBtn.setBackground(new Color(16, 185, 129));
        presentBtn.setForeground(Color.WHITE);
        absentBtn.setBackground(new Color(239, 68, 68));
        absentBtn.setForeground(Color.WHITE);
        
        presentBtn.setPreferredSize(new Dimension(80, 30));
        absentBtn.setPreferredSize(new Dimension(80, 30));
        
        add(presentBtn);
        add(absentBtn);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        return this;
    }
}

class AttendanceButtonEditor extends DefaultCellEditor {
    private JPanel panel;
    private JButton presentBtn;
    private JButton absentBtn;
    private JTable table;
    private TeacherFrame parent;
    
    public AttendanceButtonEditor(JCheckBox checkBox, JTable table, TeacherFrame parent) {
        super(checkBox);
        this.table = table;
        this.parent = parent;
        
        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        presentBtn = new JButton("Present");
        absentBtn = new JButton("Absent");
        
        presentBtn.setBackground(new Color(16, 185, 129));
        presentBtn.setForeground(Color.WHITE);
        absentBtn.setBackground(new Color(239, 68, 68));
        absentBtn.setForeground(Color.WHITE);
        
        presentBtn.setPreferredSize(new Dimension(80, 30));
        absentBtn.setPreferredSize(new Dimension(80, 30));
        
        presentBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                parent.markAttendance(row, "Present");
            }
            fireEditingStopped();
        });
        
        absentBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                parent.markAttendance(row, "Absent");
            }
            fireEditingStopped();
        });
        
        panel.add(presentBtn);
        panel.add(absentBtn);
    }
    
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        return panel;
    }
}

class FeeButtonRenderer extends JPanel implements TableCellRenderer {
    private JButton toggleBtn;
    
    public FeeButtonRenderer() {
        setLayout(new FlowLayout(FlowLayout.CENTER));
        toggleBtn = new JButton("Toggle Status");
        toggleBtn.setPreferredSize(new Dimension(120, 30));
        add(toggleBtn);
    }
    
      @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        return this;
    }
}

class FeeButtonEditor extends DefaultCellEditor {
    private JPanel panel;
    private JButton toggleBtn;
    private JTable table;
    private TeacherFrame parent;

    public FeeButtonEditor(JCheckBox checkBox, JTable table, TeacherFrame parent) {
        super(checkBox);
        this.table = table;
        this.parent = parent;

        panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        toggleBtn = new JButton("Toggle Status");
        toggleBtn.setPreferredSize(new Dimension(120, 30));

        toggleBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                parent.toggleFeeStatus(row);
            }
            fireEditingStopped();
        });

        panel.add(toggleBtn);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        return panel;
    }
    
}

// Button Renderer class for Students table
class ButtonRenderer extends JPanel implements TableCellRenderer {
    private JButton editButton;
    private JButton viewButton;
    
    public ButtonRenderer() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        editButton = new JButton("Edit");
        viewButton = new JButton("View");
        
        editButton.setPreferredSize(new Dimension(70, 30));
        viewButton.setPreferredSize(new Dimension(70, 30));
        
        add(editButton);
        add(viewButton);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        return this;
    }
}

// Button Editor class for Students table  
class ButtonEditor extends DefaultCellEditor {
    private JPanel panel;
    private JButton editButton;
    private JButton viewButton;
    private JTable table;
    private TeacherFrame parent;
    
    public ButtonEditor(JCheckBox checkBox, JTable table, TeacherFrame parent) {
        super(checkBox);
        this.table = table;
        this.parent = parent;
        
        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        editButton = new JButton("Edit");
        viewButton = new JButton("View");
        
        editButton.setPreferredSize(new Dimension(70, 30));
        viewButton.setPreferredSize(new Dimension(70, 30));
        
        editButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                parent.editStudent(row);
            }
            fireEditingStopped();
        });
        
        viewButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                parent.viewStudent(row);
            }
            fireEditingStopped();
        });
        
        panel.add(editButton);
        panel.add(viewButton);
    }
    
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        return panel;
    }
}
