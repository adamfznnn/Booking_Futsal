package View;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import Connection.Koneksi;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class FutsalGO extends JFrame {
    private JButton selectedDateButton = null;
    private JButton selectedTimeButton = null;
    private final JPanel panelTanggal;
    private final JPanel panelJam;
    private JComboBox<String> comboBulan;
    private JComboBox<Integer> comboTahun;
    private JComboBox<Integer> comboLapangan;
    private String selectedJamText = null;
    private int selectedTanggal = -1;

    private final String username;

    public FutsalGO(String username) {
        this.username = username;
        setTitle("Booking - FutsalGO");
        setSize(950, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header dengan logo dan teks
        JPanel panelHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelHeader.setBackground(new Color(0, 102, 204));
        panelHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Logo
        ImageIcon logoIcon = new ImageIcon("src/assets/logo.png");
        Image img = logoIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        JLabel lblLogo = new JLabel(new ImageIcon(img));

        // Teks
        JLabel lblNama = new JLabel("FutsalGO");
        lblNama.setFont(new Font("Arial", Font.BOLD, 32));
        lblNama.setForeground(Color.WHITE);
        lblNama.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        panelHeader.add(lblLogo);
        panelHeader.add(lblNama);
        add(panelHeader, BorderLayout.NORTH);

        // Panel konten utama
        JPanel panelKonten = new JPanel() {
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
        panelKonten.setLayout(new BoxLayout(panelKonten, BoxLayout.Y_AXIS));
        panelKonten.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // Bulan & Tahun
        JPanel filterTanggal = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterTanggal.setOpaque(false);
        String[] bulan = {"Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli",
                "Agustus", "September", "Oktober", "November", "Desember"};
        comboBulan = new JComboBox<>(bulan);
        comboTahun = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 1; i <= currentYear + 2; i++) {
            comboTahun.addItem(i);
        }
        comboBulan.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        comboTahun.setSelectedItem(currentYear);
        filterTanggal.add(new JLabel("Bulan:"));
        filterTanggal.add(comboBulan);
        filterTanggal.add(new JLabel("Tahun:"));
        filterTanggal.add(comboTahun);
        panelKonten.add(filterTanggal);

        panelTanggal = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTanggal.setBorder(BorderFactory.createTitledBorder("Pilih Tanggal"));
        panelTanggal.setOpaque(false);
        ((TitledBorder) panelTanggal.getBorder()).setTitleColor(Color.WHITE);
        panelKonten.add(panelTanggal);

        comboBulan.addActionListener(e -> generateTanggal());
        comboTahun.addActionListener(e -> generateTanggal());

        generateTanggal();

        // Lapangan
        JPanel panelLapangan = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelLapangan.setBorder(BorderFactory.createTitledBorder("Pilih Lapangan"));
        panelLapangan.setOpaque(false);
        ((TitledBorder) panelLapangan.getBorder()).setTitleColor(Color.WHITE);
        comboLapangan = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        comboLapangan.addActionListener(e -> {
            if (selectedTanggal != -1) {
                generateJam();
            }
        });
        panelLapangan.add(comboLapangan);
        panelKonten.add(panelLapangan);

        panelJam = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelJam.setBorder(BorderFactory.createTitledBorder("Pilih Jam Booking"));
        panelJam.setOpaque(false);
        ((TitledBorder) panelJam.getBorder()).setTitleColor(Color.WHITE);
        panelKonten.add(panelJam);

        // Tombol Booking
        JButton btnBooking = new JButton("✅ Booking Sekarang");
        btnBooking.setFont(new Font("Arial", Font.BOLD, 18));
        btnBooking.setBackground(new Color(0, 204, 102));
        btnBooking.setForeground(Color.WHITE);
        btnBooking.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnBooking.addActionListener(e -> {
            if (selectedTanggal == -1 || selectedJamText == null) {
                JOptionPane.showMessageDialog(this, "Harap pilih tanggal dan jam!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            } else {
                try {
                    Connection conn = Koneksi.getConnection();
                    String tanggalFull = String.format("%d-%02d-%02d",
                            comboTahun.getSelectedItem(), comboBulan.getSelectedIndex() + 1, selectedTanggal);

                    PreparedStatement checkStmt = conn.prepareStatement(
                            "SELECT COUNT(*) FROM booking WHERE lapangan_id = ? AND tanggal = ? AND jam = ?");
                    checkStmt.setInt(1, (int) comboLapangan.getSelectedItem());
                    checkStmt.setString(2, tanggalFull);
                    checkStmt.setString(3, selectedJamText);
                    ResultSet checkRs = checkStmt.executeQuery();
                    checkRs.next();
                    int count = checkRs.getInt(1);
                    checkStmt.close();

                    if (count > 0) {
                        JOptionPane.showMessageDialog(this, "Jam sudah dibooking di tanggal tersebut!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    } else {
                        PreparedStatement stmt = conn.prepareStatement(
                                "INSERT INTO booking (username, lapangan_id, tanggal, jam) VALUES (?, ?, ?, ?)");
                        stmt.setString(1, username);
                        stmt.setInt(2, (int) comboLapangan.getSelectedItem());
                        stmt.setString(3, tanggalFull);
                        stmt.setString(4, selectedJamText);
                        stmt.executeUpdate();
                        stmt.close();

                        JOptionPane.showMessageDialog(this, "Booking berhasil!\nTanggal: " + tanggalFull + "\nJam: " + selectedJamText);
                        //dispose();
                    }

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat booking.", "Gagal", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        panelKonten.add(Box.createRigidArea(new Dimension(0, 20)));
        panelKonten.add(btnBooking);

        // Navigasi
        JPanel panelNavigasi = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelNavigasi.setOpaque(false);
        JButton btnLogout = new JButton("🔓 Logout");
        btnLogout.setBackground(new Color(204, 0, 0));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("Arial", Font.BOLD, 14));

        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        panelNavigasi.add(btnLogout);
        panelKonten.add(Box.createRigidArea(new Dimension(0, 20)));
        panelKonten.add(panelNavigasi);

        add(panelKonten, BorderLayout.CENTER);
        setVisible(true);
    }

    private void generateTanggal() {
        panelTanggal.removeAll();
        int selectedMonth = comboBulan.getSelectedIndex() + 1;
        int selectedYear = (int) comboTahun.getSelectedItem();
        YearMonth yearMonth = YearMonth.of(selectedYear, selectedMonth);
        int daysInMonth = yearMonth.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            JButton btnTanggal = new JButton(String.valueOf(day));
            btnTanggal.setPreferredSize(new Dimension(50, 35));
            btnTanggal.setBackground(Color.BLUE);
            btnTanggal.setForeground(Color.WHITE);
            btnTanggal.setFont(new Font("Arial", Font.PLAIN, 14));

            btnTanggal.addActionListener(e -> {
                if (selectedDateButton != null) {
                    selectedDateButton.setBackground(Color.BLUE);
                    selectedDateButton.setForeground(Color.WHITE);
                }
                selectedDateButton = btnTanggal;
                selectedTanggal = Integer.parseInt(btnTanggal.getText());
                btnTanggal.setBackground(Color.YELLOW);
                btnTanggal.setForeground(Color.BLACK);
                generateJam();
            });

            panelTanggal.add(btnTanggal);
        }

        panelTanggal.revalidate();
        panelTanggal.repaint();
    }

    private void generateJam() {
        panelJam.removeAll();
        selectedTimeButton = null;
        selectedJamText = null;

        String tanggalFull = String.format("%d-%02d-%02d",
                comboTahun.getSelectedItem(), comboBulan.getSelectedIndex() + 1, selectedTanggal);

        try (Connection conn = Koneksi.getConnection()) {
            for (int i = 8; i <= 22; i++) {
                String jam = String.format("%02d:00", i);
                JButton btnJam = new JButton(jam);
                btnJam.setPreferredSize(new Dimension(70, 35));
                btnJam.setBackground(Color.BLUE);
                btnJam.setForeground(Color.WHITE);
                btnJam.setFont(new Font("Arial", Font.PLAIN, 14));

                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT COUNT(*) FROM booking WHERE lapangan_id = ? AND tanggal = ? AND jam = ?");
                stmt.setInt(1, (int) comboLapangan.getSelectedItem());
                stmt.setString(2, tanggalFull);
                stmt.setString(3, jam);
                ResultSet rs = stmt.executeQuery();
                rs.next();
                boolean booked = rs.getInt(1) > 0;
                rs.close();
                stmt.close();

                if (booked) {
                    btnJam.setEnabled(false);
                    btnJam.setBackground(Color.GRAY);
                } else {
                    btnJam.addActionListener(e -> {
                        if (selectedTimeButton != null) {
                            selectedTimeButton.setBackground(Color.BLUE);
                            selectedTimeButton.setForeground(Color.WHITE);
                        }
                        selectedTimeButton = btnJam;
                        selectedJamText = jam;
                        btnJam.setBackground(Color.YELLOW);
                        btnJam.setForeground(Color.BLACK);
                    });
                }

                panelJam.add(btnJam);
            }

            if (panelJam.getComponentCount() == 0) {
                panelJam.add(new JLabel("Tidak ada jam tersedia."));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        panelJam.revalidate();
        panelJam.repaint();
}
}