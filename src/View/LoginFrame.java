package View;

import View.FutsalGO;
import View.DashboardAdmin;
import Connection.Koneksi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginFrame() {
        setTitle("Login - FutsalGO");
        setSize(400, 320);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();
                Color color1 = new Color(0, 153, 255);
                Color color2 = new Color(0, 255, 204);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, height, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, width, height);
            }
        };
        panel.setLayout(null);

        JLabel titleLabel = new JLabel("FutsalGO Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(110, 20, 200, 30);
        panel.add(titleLabel);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setBounds(50, 80, 80, 25);
        panel.add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(140, 80, 180, 25);
        panel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setBounds(50, 120, 80, 25);
        panel.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(140, 120, 180, 25);
        panel.add(passwordField);

        loginButton = new JButton("Login");
        loginButton.setBounds(140, 170, 100, 30);
        loginButton.setBackground(new Color(0, 102, 204));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(loginButton);

        registerButton = new JButton("Buat Akun");
        registerButton.setBounds(140, 210, 100, 25);
        registerButton.setFont(new Font("Arial", Font.PLAIN, 12));
        registerButton.setBackground(new Color(0, 204, 102));
        registerButton.setForeground(Color.WHITE);
        panel.add(registerButton);

        add(panel);

        loginButton.addActionListener((ActionEvent e) -> login());
        registerButton.addActionListener((ActionEvent e) -> register());
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = String.valueOf(passwordField.getPassword()).trim();

        try {
            Connection conn = Koneksi.getConnection();
            String query = "SELECT * FROM user WHERE username=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                JOptionPane.showMessageDialog(this, "Login berhasil sebagai " + role);
                dispose();

                if ("admin".equalsIgnoreCase(role)) {
                    new DashboardAdmin();
                } else {
                    new FutsalGO(username).setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Username / Password salah!");
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void register() {
        String username = usernameField.getText().trim();
        String password = String.valueOf(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password tidak boleh kosong!");
            return;
        }

        try {
            Connection conn = Koneksi.getConnection();

            // Cek apakah username sudah ada
            String checkQuery = "SELECT * FROM user WHERE username=?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Username sudah digunakan, pilih yang lain.");
            } else {
                String insertQuery = "INSERT INTO user (username, password, role) VALUES (?, ?, 'user')";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Akun berhasil dibuat! Silakan login.");
            }

            rs.close();
            checkStmt.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saat registrasi: " + ex.getMessage());
        }
    }
}
