import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Main extends JFrame {
    JTextField tfRuang, tfPemesan, tfTanggal, tfJam, tfCari;
    JTable table;
    DefaultTableModel model;
    int selectedId = -1;

    Connection conn;

    public Main() {
        setTitle("Reservasi Ruang Kelas");
        setSize(750, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        connectDB();
        createTable();

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        tfRuang = new JTextField();
        tfPemesan = new JTextField();
        tfTanggal = new JTextField();
        tfJam = new JTextField();

        formPanel.add(new JLabel("Nama Ruang"));
        formPanel.add(tfRuang);
        formPanel.add(new JLabel("Nama Pemesanan"));
        formPanel.add(tfPemesan);
        formPanel.add(new JLabel("Tanggal"));
        formPanel.add(tfTanggal);
        formPanel.add(new JLabel("Jam"));
        formPanel.add(tfJam);

        JPanel buttonPanel = new JPanel();
        JButton btnTambah = new JButton("Tambah");
        JButton btnUbah = new JButton("Ubah");
        JButton btnHapus = new JButton("Hapus");
        JButton btnReset = new JButton("Reset");

        buttonPanel.add(btnTambah);
        buttonPanel.add(btnUbah);
        buttonPanel.add(btnHapus);
        buttonPanel.add(btnReset);

        JPanel searchPanel = new JPanel(new BorderLayout());
        tfCari = new JTextField();
        searchPanel.add(new JLabel("Cari (Ruang/Pemesan): "), BorderLayout.WEST);
        searchPanel.add(tfCari, BorderLayout.CENTER);

        model = new DefaultTableModel(
                new String[]{"ID", "Ruang", "Pemesan", "Tanggal", "Jam"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(searchPanel, BorderLayout.SOUTH);

        loadData("");

        btnTambah.addActionListener(e -> tambahData());
        btnUbah.addActionListener(e -> ubahData());
        btnHapus.addActionListener(e -> hapusData());
        btnReset.addActionListener(e -> resetForm());

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                selectedId = Integer.parseInt(model.getValueAt(row, 0).toString());
                tfRuang.setText(model.getValueAt(row, 1).toString());
                tfPemesan.setText(model.getValueAt(row, 2).toString());
                tfTanggal.setText(model.getValueAt(row, 3).toString());
                tfJam.setText(model.getValueAt(row, 4).toString());
            }
        });

        tfCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                loadData(tfCari.getText());
            }
        });
    }


    void connectDB() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:reservasi.db");
        } catch (Exception e) {
            showError(e, "Koneksi database gagal");
        }
    }

    void createTable() {
        if (conn == null) return;
        String sql = "CREATE TABLE IF NOT EXISTS reservasi (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "ruang TEXT," +
                "pemesan TEXT," +
                "tanggal TEXT," +
                "jam TEXT)";
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (Exception e) {
            showError(e, "Gagal membuat tabel");
        }
    }

    void tambahData() {
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Tidak terhubung ke database", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String ruang = tfRuang.getText().trim();
        String pemesan = tfPemesan.getText().trim();
        if (ruang.isEmpty() || pemesan.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama ruang dan pemesan tidak boleh kosong", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO reservasi (ruang, pemesan, tanggal, jam) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ruang);
            ps.setString(2, pemesan);
            ps.setString(3, tfTanggal.getText().trim());
            ps.setString(4, tfJam.getText().trim());
            ps.executeUpdate();
            loadData("");
            resetForm();
        } catch (Exception e) {
            showError(e, "Gagal menambah data");
        }
    }

    void ubahData() {
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Tidak terhubung ke database", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (selectedId == -1) return;
        String ruang = tfRuang.getText().trim();
        String pemesan = tfPemesan.getText().trim();
        if (ruang.isEmpty() || pemesan.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama ruang dan pemesan tidak boleh kosong", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String sql = "UPDATE reservasi SET ruang=?, pemesan=?, tanggal=?, jam=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ruang);
            ps.setString(2, pemesan);
            ps.setString(3, tfTanggal.getText().trim());
            ps.setString(4, tfJam.getText().trim());
            ps.setInt(5, selectedId);
            ps.executeUpdate();
            loadData("");
            resetForm();
        } catch (Exception e) {
            showError(e, "Gagal mengubah data");
        }
    }

    void hapusData() {
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Tidak terhubung ke database", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (selectedId == -1) return;
        String sql = "DELETE FROM reservasi WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            loadData("");
            resetForm();
        } catch (Exception e) {
            showError(e, "Gagal menghapus data");
        }

    }

    void loadData(String keyword) {
        if (conn == null) return;
        model.setRowCount(0);
        String sql = "SELECT * FROM reservasi WHERE ruang LIKE ? OR pemesan LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("ruang"),
                            rs.getString("pemesan"),
                            rs.getString("tanggal"),
                            rs.getString("jam")

                    });
                }
            }
        } catch (Exception e) {
            showError(e, "Gagal memuat data");
        }
    }

    void resetForm() {
        tfRuang.setText("");
        tfPemesan.setText("");
        tfTanggal.setText("");
        tfJam.setText("");
        selectedId = -1;
        table.clearSelection();
    }

    private void showError(Exception e, String context) {
        // Tulis ke stderr untuk debugging dan tampilkan dialog yang ramah
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, context + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}