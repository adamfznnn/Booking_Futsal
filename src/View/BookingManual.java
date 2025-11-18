package View;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import Connection.Koneksi;

public class BookingManual extends JFrame {
    public JTextField fieldUsername;
    public JComboBox<Integer> fieldLapangan;
    public JTextField fieldTanggal;
    public JComboBox<String> fieldJam;

    private boolean isEditMode = false;
    private int bookingId;

    public BookingManual() {
        setTitle("Form Booking Manual");
        setSize(450, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();
                Color color1 = new Color(0, 102, 204);
                Color color2 = new Color(0, 204, 255);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, height, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, width, height);
            }
        };
        headerPanel.setPreferredSize(new Dimension(450, 60));
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel lblJudul = new JLabel("📋 Form Booking Manual");
        lblJudul.setFont(new Font("Arial", Font.BOLD, 22));
        lblJudul.setForeground(Color.WHITE);
        headerPanel.add(lblJudul);
        add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        fieldUsername = new JTextField();
        fieldLapangan = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        fieldTanggal = new JTextField(LocalDate.now().toString());
        fieldJam = new JComboBox<>();
        for (int i = 8; i <= 22; i++) {
            fieldJam.addItem(String.format("%02d:00", i));
        }

        int y = 0;
        addLabelAndField(formPanel, "👤 Nama Pemesan:", fieldUsername, gbc, y++);
        addLabelAndField(formPanel, "⚽ Lapangan:", fieldLapangan, gbc, y++);
        addLabelAndField(formPanel, "📅 Tanggal:", fieldTanggal, gbc, y++);
        addLabelAndField(formPanel, "⏰ Jam:", fieldJam, gbc, y++);

        JButton btnSubmit = new JButton("✔ Simpan Booking");
        btnSubmit.setBackground(new Color(0, 204, 102));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFont(new Font("Arial", Font.BOLD, 14));
        btnSubmit.addActionListener(e -> {
            if (isEditMode) {
                updateBooking();
            } else {
                simpanBaru();
            }
        });

        gbc.gridx = 1;
        gbc.gridy = y;
        formPanel.add(btnSubmit, gbc);

        add(formPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private void addLabelAndField(JPanel panel, String label, JComponent field, GridBagConstraints gbc, int y) {
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void simpanBaru() {
        try (Connection conn = Koneksi.getConnection()) {
            PreparedStatement check = conn.prepareStatement(
                "SELECT COUNT(*) FROM booking WHERE lapangan_id = ? AND tanggal = ? AND jam = ?");
            check.setInt(1, (int) fieldLapangan.getSelectedItem());
            check.setString(2, fieldTanggal.getText());
            check.setString(3, (String) fieldJam.getSelectedItem());
            ResultSet rs = check.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Slot sudah dibooking!", "Gagal", JOptionPane.WARNING_MESSAGE);
                return;
            }

            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO booking (username, lapangan_id, tanggal, jam) VALUES (?, ?, ?, ?)");
            stmt.setString(1, fieldUsername.getText());
            stmt.setInt(2, (int) fieldLapangan.getSelectedItem());
            stmt.setString(3, fieldTanggal.getText());
            stmt.setString(4, (String) fieldJam.getSelectedItem());
            stmt.executeUpdate();
            stmt.close();

            JOptionPane.showMessageDialog(this, "Booking berhasil!");
            dispose();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan booking!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateBooking() {
        try (Connection conn = Koneksi.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE booking SET username = ?, lapangan_id = ?, tanggal = ?, jam = ? WHERE id = ?");
            stmt.setString(1, fieldUsername.getText());
            stmt.setInt(2, (int) fieldLapangan.getSelectedItem());
            stmt.setString(3, fieldTanggal.getText());
            stmt.setString(4, (String) fieldJam.getSelectedItem());
            stmt.setInt(5, bookingId);
            stmt.executeUpdate();
            stmt.close();

            JOptionPane.showMessageDialog(this, "Booking berhasil diperbarui!");
            dispose();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui booking!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Dipanggil dari DashboardAdmin saat ingin mengedit
    public void setEditData(int id, String username, int lapanganId, String tanggal, String jam) {
        this.isEditMode = true;
        this.bookingId = id;
        fieldUsername.setText(username);
        fieldLapangan.setSelectedItem(lapanganId);
        fieldTanggal.setText(tanggal);
        fieldJam.setSelectedItem(jam);
    }
}
