/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package aplikasi;

import java.awt.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class APLIKASI extends JFrame {

    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";
    private static final Path FILE = Paths.get("karyawan.txt");

    // Data karyawan: {id, nama, jabatan, username, password}
    private final List<String[]> karyawan = new ArrayList<>();

    private final CardLayout card = new CardLayout();
    private final JPanel root = new JPanel(card);
    private final JLabel lblSambutan = new JLabel("", SwingConstants.CENTER);
    private final DefaultTableModel model =
            new DefaultTableModel(new String[]{"ID", "Nama", "Jabatan", "Username"}, 0) {
                @Override
                public boolean isCellEditable(int r, int c) { return false; }
            };

    public APLIKASI() {
        super("Sistem Absensi dan Gaji");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        muatData();

        root.add(buatHalamanUtama(), "utama");
        root.add(buatLogin(false), "loginUser");
        root.add(buatLogin(true), "loginAdmin");
        root.add(buatDashboardUser(), "dashUser");
        root.add(buatDashboardAdmin(), "dashAdmin");
        add(root);

        setSize(760, 480);
        setLocationRelativeTo(null);
    }

    // ================= Halaman Utama =================
    private JPanel buatHalamanUtama() {
        JLabel judul = new JLabel("SISTEM ABSENSI DAN GAJI", SwingConstants.CENTER);
        judul.setFont(judul.getFont().deriveFont(Font.BOLD, 22f));
        JButton btnUser = new JButton("LOGIN USER");
        JButton btnAdmin = new JButton("LOGIN ADMIN");
        btnUser.addActionListener(e -> card.show(root, "loginUser"));
        btnAdmin.addActionListener(e -> card.show(root, "loginAdmin"));

        JPanel isi = new JPanel(new GridLayout(0, 1, 12, 12));
        isi.setBorder(BorderFactory.createEmptyBorder(60, 220, 60, 220));
        isi.add(judul);
        isi.add(btnUser);
        isi.add(btnAdmin);
        return isi;
    }

    // ================= Login (User / Admin) =================
    private JPanel buatLogin(boolean admin) {
        JTextField usr = new JTextField();
        JPasswordField pwd = new JPasswordField();
        JButton btnLogin = new JButton("LOGIN");
        JButton btnKembali = new JButton("KEMBALI");

        JLabel judul = new JLabel(admin ? "LOGIN ADMIN" : "LOGIN USER", SwingConstants.CENTER);
        judul.setFont(judul.getFont().deriveFont(Font.BOLD, 18f));

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Username"));
        form.add(usr);
        form.add(new JLabel("Password"));
        form.add(pwd);
        form.add(btnKembali);
        form.add(btnLogin);

        btnKembali.addActionListener(e -> {
            usr.setText("");
            pwd.setText("");
            card.show(root, "utama");
        });

        btnLogin.addActionListener(e -> {
            String u = usr.getText().trim();
            String p = new String(pwd.getPassword());
            if (admin) {
                if (u.equals(ADMIN_USER) && p.equals(ADMIN_PASS)) {
                    card.show(root, "dashAdmin");
                } else {
                    pesan("Username atau password admin salah.");
                    return;
                }
            } else {
                String[] k = cariLogin(u, p);
                if (k == null) {
                    pesan("Username atau password salah.");
                    return;
                }
                lblSambutan.setText("Selamat datang, " + k[1]);
                card.show(root, "dashUser");
            }
            usr.setText("");
            pwd.setText("");
        });

        JPanel p = new JPanel(new BorderLayout(10, 20));
        p.setBorder(BorderFactory.createEmptyBorder(80, 200, 80, 200));
        p.add(judul, BorderLayout.NORTH);
        p.add(form, BorderLayout.CENTER);
        return p;
    }

    // ================= Dashboard User (sementara) =================
    private JPanel buatDashboardUser() {
        lblSambutan.setFont(lblSambutan.getFont().deriveFont(Font.BOLD, 18f));
        JButton logout = new JButton("LOGOUT");
        logout.addActionListener(e -> card.show(root, "utama"));

        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        p.add(lblSambutan, BorderLayout.CENTER);
        p.add(logout, BorderLayout.SOUTH);
        return p;
    }

    // ================= Dashboard Admin: Data Karyawan =================
    private JPanel buatDashboardAdmin() {
        JTextField nama = new JTextField();
        JTextField jabatan = new JTextField();
        JTextField user = new JTextField();
        JTextField pass = new JTextField();
        JButton tambah = new JButton("Tambah Karyawan");
        JButton logout = new JButton("LOGOUT");

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Nama"));
        form.add(nama);
        form.add(new JLabel("Jabatan"));
        form.add(jabatan);
        form.add(new JLabel("Username"));
        form.add(user);
        form.add(new JLabel("Password"));
        form.add(pass);
        form.add(logout);
        form.add(tambah);

        tambah.addActionListener(e -> {
            String err = tambahKaryawan(nama.getText(), jabatan.getText(), user.getText(), pass.getText());
            if (err != null) {
                pesan(err);
                return;
            }
            nama.setText("");
            jabatan.setText("");
            user.setText("");
            pass.setText("");
            isiTabel();
        });
        logout.addActionListener(e -> card.show(root, "utama"));

        isiTabel();
        JTable tabel = new JTable(model);
        tabel.setRowHeight(24);

        JLabel judul = new JLabel("DATA KARYAWAN");
        judul.setFont(judul.getFont().deriveFont(Font.BOLD, 16f));

        JPanel atas = new JPanel(new BorderLayout(6, 6));
        atas.add(judul, BorderLayout.NORTH);
        atas.add(form, BorderLayout.CENTER);

        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        p.add(atas, BorderLayout.NORTH);
        p.add(new JScrollPane(tabel), BorderLayout.CENTER);
        return p;
    }

    // ================= Logika data =================
    private String[] cariLogin(String u, String p) {
        for (String[] k : karyawan)
            if (k[3].equals(u) && k[4].equals(p)) return k;
        return null;
    }

    /** @return pesan error, atau null jika berhasil */
    private String tambahKaryawan(String nama, String jabatan, String user, String pass) {
        nama = bersih(nama);
        jabatan = bersih(jabatan);
        user = bersih(user);
        pass = bersih(pass);
        if (nama.isEmpty() || jabatan.isEmpty() || user.isEmpty() || pass.isEmpty())
            return "Semua field wajib diisi.";
        if (user.equalsIgnoreCase(ADMIN_USER))
            return "Username tidak boleh sama dengan admin.";
        for (String[] k : karyawan)
            if (k[3].equalsIgnoreCase(user)) return "Username sudah dipakai.";

        int max = 0;
        for (String[] k : karyawan) max = Math.max(max, Integer.parseInt(k[0].substring(1)));
        karyawan.add(new String[]{String.format("K%03d", max + 1), nama, jabatan, user, pass});
        simpanData();
        return null;
    }

    private void isiTabel() {
        model.setRowCount(0);
        for (String[] k : karyawan) model.addRow(new Object[]{k[0], k[1], k[2], k[3]});
    }

    private static String bersih(String s) {
        return s.replace("|", " ").trim();
    }

    private void muatData() {
        try {
            if (Files.exists(FILE)) {
                for (String l : Files.readAllLines(FILE)) {
                    String[] p = l.split("\\|", -1);
                    if (p.length >= 5) karyawan.add(p);
                }
            }
        } catch (IOException e) {
            pesan("Gagal membaca data: " + e.getMessage());
        }
        if (karyawan.isEmpty()) { // data contoh
            karyawan.add(new String[]{"K001", "Budi Santoso", "Staff Produksi", "budi", "123"});
            simpanData();
        }
    }

    private void simpanData() {
        List<String> baris = new ArrayList<>();
        for (String[] k : karyawan) baris.add(String.join("|", k));
        try {
            Files.write(FILE, baris);
        } catch (IOException e) {
            pesan("Gagal menyimpan data: " + e.getMessage());
        }
    }

    private void pesan(String m) {
        JOptionPane.showMessageDialog(this, m);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new APLIKASI().setVisible(true));
    }
}
