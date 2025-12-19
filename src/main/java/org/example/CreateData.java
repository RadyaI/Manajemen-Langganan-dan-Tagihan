package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CreateData extends JFrame {

    // Kita butuh username biar pas balik ke Dashboard, namanya tetep ada
    private String currentUsername;

    // Komponen Input
    private Style.RoundedTextField namaField;
    private Style.RoundedTextField linkField;
    private Style.RoundedTextField hargaField;
    private Style.RoundedTextField tenggatField;

    // Constructor menerima username dari halaman sebelumnya
    public CreateData(String username) {
        this.currentUsername = username;

        setTitle("Tambah Langganan Baru");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout()); // Biar Form ada di tengah
        getContentPane().setBackground(Style.COLOR_BG);

        // --- Container Form (Kartu Putih) ---
        Style.RoundedPanel formCard = new Style.RoundedPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(new EmptyBorder(40, 50, 40, 50));
        // Ukuran preferensi biar gak kekecilan
        formCard.setPreferredSize(new Dimension(500, 600));

        // --- 1. Judul Form ---
        JLabel titleLabel = new JLabel("Tambah Data");
        titleLabel.setFont(Style.FONT_TITLE);
        titleLabel.setForeground(Style.COLOR_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- 2. Input Fields ---
        // Helper method biar kodingan rapi (cek di bawah)
        JPanel inputNama = createInputGroup("Nama Aplikasi", "Contoh: Netflix Premium");
        JPanel inputLink = createInputGroup("Link / URL", "Contoh: https://netflix.com");
        JPanel inputHarga = createInputGroup("Harga Langganan (Angka saja)", "Contoh: 186000");
        JPanel inputTenggat = createInputGroup("Tenggat Pembayaran", "Format: 25 Januari 2026");

        // Ambil referensi field dari panel helper tadi
        namaField = (Style.RoundedTextField) inputNama.getComponent(2);
        linkField = (Style.RoundedTextField) inputLink.getComponent(2);
        hargaField = (Style.RoundedTextField) inputHarga.getComponent(2);
        tenggatField = (Style.RoundedTextField) inputTenggat.getComponent(2);

        // --- 3. Tombol Action ---
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0)); // Grid 2 kolom, gap 20px
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Tombol Batal (Outline Style)
        Style.RoundedButtonOutline btnBatal = new Style.RoundedButtonOutline("Batal");
        btnBatal.addActionListener(e -> backToDashboard());

        // Tombol Simpan (Solid Style)
        Style.RoundedButton btnSimpan = new Style.RoundedButton("Simpan Data");
        btnSimpan.addActionListener(e -> saveData());

        buttonPanel.add(btnBatal);
        buttonPanel.add(btnSimpan);

        // --- MENYUSUN LAYOUT ---
        formCard.add(titleLabel);
        formCard.add(Box.createRigidArea(new Dimension(0, 30))); // Spacer
        formCard.add(inputNama);
        formCard.add(Box.createRigidArea(new Dimension(0, 15)));
        formCard.add(inputLink);
        formCard.add(Box.createRigidArea(new Dimension(0, 15)));
        formCard.add(inputHarga);
        formCard.add(Box.createRigidArea(new Dimension(0, 15)));
        formCard.add(inputTenggat);
        formCard.add(Box.createRigidArea(new Dimension(0, 40))); // Spacer agak jauh ke tombol
        formCard.add(buttonPanel);

        add(formCard);
    }

    private JPanel createInputGroup(String labelText, String placeholder) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // [PENTING!] Ganti ini jadi CENTER_ALIGNMENT
        // Kenapa? Biar panel pembungkusnya mau melebar penuh ngikutin container utama
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(Style.FONT_LABEL);
        label.setForeground(Style.COLOR_TEXT);

        Style.RoundedTextField field = new Style.RoundedTextField(20);
        field.setPlaceholder(placeholder);

        // Pastikan width-nya MAX biar dia mau melar
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        // Fieldnya tetep rata KIRI (biar teks mulai dari kiri)
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Label Wrapper
        JPanel labelWrap = new JPanel(new BorderLayout());
        labelWrap.setOpaque(false);
        labelWrap.add(label, BorderLayout.WEST);
        labelWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        // Wrapper label juga rata KIRI
        labelWrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(labelWrap);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(field);

        return panel;
    }

    // --- LOGIC SIMPAN DATA ---
    // --- LOGIC SIMPAN DATA (REVISI) ---
    private void saveData() {
        String nama = namaField.getText().trim();
        String link = linkField.getText().trim(); // Link boleh kosong
        String hargaRaw = hargaField.getText().trim();
        String tenggat = tenggatField.getText().trim();

        // 1. Validasi Kosong (Link dihapus dari sini, jadi optional)
        if (nama.isEmpty() || hargaRaw.isEmpty() || tenggat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama, Harga, dan Tenggat wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Kalau Link kosong, isi dengan strip "-" biar CSV tetap rapi
        if (link.isEmpty()) {
            link = "-";
        }

        // 2. Validasi Tanggal & Format (Gak boleh tanggal masa lalu)
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
            LocalDate inputDate = LocalDate.parse(tenggat, formatter);

            // Cek apakah tanggal input < hari ini?
            if (inputDate.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this,
                        "Tanggal tenggat tidak boleh kurang dari hari ini!",
                        "Validasi Tanggal", JOptionPane.WARNING_MESSAGE);
                return; // Stop proses simpan
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Format tanggal salah!\nGunakan format: 25 Januari 2026\n(Pastikan nama bulan Bahasa Indonesia)",
                    "Format Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Formatting Harga (50000 -> Rp50.000)
        String hargaFormatted = hargaRaw;
        if (!hargaRaw.startsWith("Rp")) {
            try {
                // Bersihkan titik/koma dulu jaga-jaga user iseng ngetik manual
                long val = Long.parseLong(hargaRaw.replace(".", "").replace(",", "").replace("Rp", "").trim());
                hargaFormatted = String.format("Rp%,d", val).replace(',', '.');
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Harga harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // 4. Tulis ke CSV (Dipastiin aman)
        // Kita pake try-with-resources biar file otomatis ditutup rapi sebelum pindah layar
        try (PrintWriter pw = new PrintWriter(new FileWriter("subscriptions.csv", true))) {
            pw.println(nama + "," + link + "," + hargaFormatted + "," + tenggat);
            pw.flush(); // Paksa tulis ke disk sekarang juga
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data: " + e.getMessage());
            return; // Jangan pindah dashboard kalau gagal simpan
        }

        // 5. Kalau sukses, kasih info & pindah
        JOptionPane.showMessageDialog(this, "Data Berhasil Disimpan!");
        backToDashboard();
    }

    private void backToDashboard() {
        this.dispose();
        // Oper balik username ke Dashboard
        new Dashboard(currentUsername).setVisible(true);
    }
}