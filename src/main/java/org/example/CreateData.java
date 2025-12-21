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

    private String currentUsername;
    private Style.RoundedTextField namaField;
    private Style.RoundedTextField linkField;
    private Style.RoundedTextField hargaField;
    private Style.RoundedTextField tenggatField;

    public CreateData(String username) {
        this.currentUsername = username;

        setTitle("Tambah Langganan Baru");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(Style.COLOR_BG);

        Style.RoundedPanel formCard = new Style.RoundedPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(new EmptyBorder(40, 50, 40, 50));
        formCard.setPreferredSize(new Dimension(500, 600));

        JLabel titleLabel = new JLabel("Tambah Data");
        titleLabel.setFont(Style.FONT_TITLE);
        titleLabel.setForeground(Style.COLOR_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Input Groups
        JPanel inputNama = createInputGroup("Nama Aplikasi", "Contoh: Netflix Premium");
        JPanel inputLink = createInputGroup("Link / URL", "Contoh: https://netflix.com");
        JPanel inputHarga = createInputGroup("Harga Langganan (Angka saja)", "Contoh: 186000");
        JPanel inputTenggat = createInputGroup("Tenggat Pembayaran", "Format: 25 Januari 2026");

        // Mengambil referensi field dari dalam panel helper
        namaField = (Style.RoundedTextField) inputNama.getComponent(2);
        linkField = (Style.RoundedTextField) inputLink.getComponent(2);
        hargaField = (Style.RoundedTextField) inputHarga.getComponent(2);
        tenggatField = (Style.RoundedTextField) inputTenggat.getComponent(2);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        Style.RoundedButtonOutline btnBatal = new Style.RoundedButtonOutline("Batal");
        btnBatal.addActionListener(e -> backToDashboard());

        Style.RoundedButton btnSimpan = new Style.RoundedButton("Simpan Data");
        btnSimpan.addActionListener(e -> saveData());

        buttonPanel.add(btnBatal);
        buttonPanel.add(btnSimpan);

        // Compose Form
        formCard.add(titleLabel);
        formCard.add(Box.createRigidArea(new Dimension(0, 30)));
        formCard.add(inputNama);
        formCard.add(Box.createRigidArea(new Dimension(0, 15)));
        formCard.add(inputLink);
        formCard.add(Box.createRigidArea(new Dimension(0, 15)));
        formCard.add(inputHarga);
        formCard.add(Box.createRigidArea(new Dimension(0, 15)));
        formCard.add(inputTenggat);
        formCard.add(Box.createRigidArea(new Dimension(0, 40)));
        formCard.add(buttonPanel);

        add(formCard);
    }

    private JPanel createInputGroup(String labelText, String placeholder) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(Style.FONT_LABEL);
        label.setForeground(Style.COLOR_TEXT);

        Style.RoundedTextField field = new Style.RoundedTextField(20);
        field.setPlaceholder(placeholder);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel labelWrap = new JPanel(new BorderLayout());
        labelWrap.setOpaque(false);
        labelWrap.add(label, BorderLayout.WEST);
        labelWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        labelWrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(labelWrap);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(field);

        return panel;
    }

    private void saveData() {
        String nama = namaField.getText().trim();
        String link = linkField.getText().trim();
        String hargaRaw = hargaField.getText().trim();
        String tenggat = tenggatField.getText().trim();

        // 1. Validasi Dasar
        if (nama.isEmpty() || hargaRaw.isEmpty() || tenggat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama, Harga, dan Tenggat wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (link.isEmpty()) link = "-";

        // 2. Validasi Tanggal
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
            LocalDate inputDate = LocalDate.parse(tenggat, formatter);
            if (inputDate.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Tanggal tenggat tidak boleh kurang dari hari ini!", "Validasi Tanggal", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Format tanggal salah!\nGunakan format: 25 Januari 2026", "Format Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Formatting Harga
        String hargaFormatted = hargaRaw;
        if (!hargaRaw.startsWith("Rp")) {
            try {
                long val = Long.parseLong(hargaRaw.replace(".", "").replace(",", "").replace("Rp", "").trim());
                hargaFormatted = String.format("Rp%,d", val).replace(',', '.');
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Harga harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // 4. Simpan ke CSV
        try (PrintWriter pw = new PrintWriter(new FileWriter("subscriptions.csv", true))) {
            pw.println(currentUsername + "," + nama + "," + link + "," + hargaFormatted + "," + tenggat);
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data: " + e.getMessage());
            return;
        }

        JOptionPane.showMessageDialog(this, "Data Berhasil Disimpan!");
        backToDashboard();
    }

    private void backToDashboard() {
        this.dispose();
        new Dashboard(currentUsername).setVisible(true);
    }
}