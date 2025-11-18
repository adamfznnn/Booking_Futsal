package View;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import Connection.Koneksi;

public class DashboardAdmin extends JFrame {
    private final JTable tableBooking;
    private final DefaultTableModel model;

    public DashboardAdmin() {
        setTitle("Dashboard Admin - Booking Futsal");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel lblJudul = new JLabel("\uD83D\uDCCB Data Booking Lapangan", JLabel.CENTER);
        lblJudul.setFont(new Font("Arial", Font.BOLD, 28));
        lblJudul.setForeground(Color.WHITE);
        lblJudul.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

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
        headerPanel.setLayout(new BorderLayout());
        headerPanel.add(lblJudul, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        model = new DefaultTableModel();
        tableBooking = new JTable(model);
        model.addColumn("ID");
        model.addColumn("Username");
        model.addColumn("Lapangan");
        model.addColumn("Tanggal");
        model.addColumn("Jam");

        JScrollPane scrollPane = new JScrollPane(tableBooking);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelBawah = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnRefresh = new JButton("\uD83D\uDD04 Refresh Data");
        JButton btnBookingManual = new JButton("\u2795 Booking Manual");
        JButton btnEdit = new JButton("✏ Edit");
        JButton btnHapus = new JButton("❌ Hapus");
        JButton btnDeleteAll = new JButton("❎ Delete All");
        JButton btnKeluar = new JButton("\uD83D\uDEAA Logout");

        btnRefresh.setBackground(new Color(0, 153, 255));
        btnRefresh.setForeground(Color.WHITE);
        btnBookingManual.setBackground(new Color(0, 204, 102));
        btnBookingManual.setForeground(Color.WHITE);
        btnEdit.setBackground(new Color(255, 204, 0));
        btnEdit.setForeground(Color.BLACK);
        btnHapus.setBackground(new Color(255, 51, 51));
        btnHapus.setForeground(Color.WHITE);
        btnDeleteAll.setBackground(new Color(153, 0, 0));
        btnDeleteAll.setForeground(Color.WHITE);
        btnKeluar.setBackground(new Color(102, 102, 102));
        btnKeluar.setForeground(Color.WHITE);

        btnRefresh.addActionListener(e -> loadData());
        btnBookingManual.addActionListener(e -> new BookingManual());
        btnEdit.addActionListener(e -> editBooking());
        btnHapus.addActionListener(e -> hapusBooking());
        btnDeleteAll.addActionListener(e -> hapusSemuaBooking());
        btnKeluar.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        panelBawah.add(btnRefresh);
        panelBawah.add(btnBookingManual);
        panelBawah.add(btnEdit);
        panelBawah.add(btnHapus);
        panelBawah.add(btnDeleteAll);
        panelBawah.add(btnKeluar);

        add(panelBawah, BorderLayout.SOUTH);

        loadData();
        setVisible(true);
    }

    private void loadData() {
        model.setRowCount(0);
        try (Connection conn = Koneksi.getConnection()) {
            String query = "SELECT * FROM booking";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getInt("lapangan_id"),
                        rs.getDate("tanggal"),
                        rs.getString("jam")
                });
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data booking.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void hapusBooking() {
        int selectedRow = tableBooking.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data yang ingin dihapus.");
            return;
        }
        int id = (int) model.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus booking ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = Koneksi.getConnection()) {
                String query = "DELETE FROM booking WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, id);
                stmt.executeUpdate();
                loadData();
                JOptionPane.showMessageDialog(this, "Booking berhasil dihapus.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus data.", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void hapusSemuaBooking() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Yakin ingin menghapus SEMUA data booking?",
                "Konfirmasi",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = Koneksi.getConnection()) {
                String query = "DELETE FROM booking";
                PreparedStatement stmt = conn.prepareStatement(query);
                int rowsDeleted = stmt.executeUpdate();
                loadData();
                JOptionPane.showMessageDialog(this, rowsDeleted + " data booking berhasil dihapus.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus semua data.", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void editBooking() {
        int selectedRow = tableBooking.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data yang ingin diedit.");
            return;
        }

        int id = (int) model.getValueAt(selectedRow, 0);
        String username = (String) model.getValueAt(selectedRow, 1);
        int lapanganId = (int) model.getValueAt(selectedRow, 2);
        String tanggal = model.getValueAt(selectedRow, 3).toString();
        String jam = (String) model.getValueAt(selectedRow, 4);

        BookingManual formEdit = new BookingManual();
        formEdit.setTitle("Edit Booking");

        // Set mode edit dan isi data ke form
        formEdit.setEditData(id, username, lapanganId, tanggal, jam);

        // Reload tabel setelah form ditutup
        formEdit.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                loadData();
            }
        });
    }
}
