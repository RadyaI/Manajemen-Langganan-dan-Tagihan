package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Dashboard extends JFrame {

    private static final String DATA_CSV = "subscriptions.csv";
    private static final String HISTORY_CSV = "history.csv";

    private final String username;

    private Style.RoundedTextField searchField;
    private JTable table;
    private DefaultTableModel tableModel;

    public final List<SubRow> allRows = new ArrayList<>();

    private final DateTimeFormatter ID_DATE =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));

    // Pastikan file icon tersedia di folder resources agar tidak error runtime
    private static final String ICON_PAY = "/icons/pay.png";
    private static final String ICON_EDIT = "/icons/edit.png";
    private static final String ICON_DELETE = "/icons/delete.png";

    public Dashboard(String username) {
        this.username = username;

        setTitle("Sistem Manajemen Langganan - Dashboard");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        getContentPane().setBackground(Style.COLOR_BG);
        setLayout(new BorderLayout(20, 20));

        validateIconsOrWarn();

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        loadFromCsv();
        applyFilter("");
    }

    // ===================== UI BUILDER =====================

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 25, 0, 25));

        JLabel greet = new JLabel("Selamat datang, " + username);
        greet.setFont(Style.FONT_SUBTITLE);
        greet.setForeground(Style.COLOR_TEXT);

        header.add(greet, BorderLayout.WEST);
        return header;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(15, 15));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(0, 25, 0, 25));

        center.add(buildTopControls(), BorderLayout.NORTH);
        center.add(buildTableCard(), BorderLayout.CENTER);

        return center;
    }

    private JPanel buildTopControls() {
        JPanel top = new JPanel(new BorderLayout(12, 12));
        top.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        Style.RoundedButton btnCreate = new Style.RoundedButton("Create Data");
        btnCreate.setPreferredSize(new Dimension(160, 45));
        btnCreate.addActionListener(e -> openCreateData());
        left.add(btnCreate);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        searchField = new Style.RoundedTextField(25);
        searchField.setPreferredSize(new Dimension(420, 45));
        searchField.setPlaceholder("Search...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(searchField.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(searchField.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(searchField.getText()); }
        });
        right.add(searchField);

        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private JPanel buildTableCard() {
        Style.RoundedPanelWithBorder card = new Style.RoundedPanelWithBorder();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Penting: Definisi kolom harus lengkap. Jika salah, bisa muncul error "Index 5 >= 0"
        tableModel = new DefaultTableModel(
                new Object[]{"No", "Nama", "Link", "Harga", "Tenggat", "Aksi"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 5; // Hanya kolom aksi yang bisa diklik
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Return Integer agar sorting nomor urut (1, 2, 10) benar, bukan string (1, 10, 2)
                if (columnIndex == 0) return Integer.class;
                return super.getColumnClass(columnIndex);
            }
        };

        table = new JTable(tableModel);

        table.setAutoCreateRowSorter(true);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Custom Comparator: Parsing format "Rp" ke Angka agar sorting harga akurat
        sorter.setComparator(3, (String s1, String s2) -> {
            try {
                long v1 = Long.parseLong(s1.replaceAll("[^0-9]", ""));
                long v2 = Long.parseLong(s2.replaceAll("[^0-9]", ""));
                return Long.compare(v1, v2);
            } catch (Exception e) { return s1.compareTo(s2); }
        });

        sorter.setSortable(2, false);
        sorter.setSortable(4, false);
        sorter.setSortable(5, false);

        table.setRowHeight(50);
        table.setFont(Style.FONT_INPUT);
        table.getTableHeader().setFont(Style.FONT_LABEL);
        table.setSelectionBackground(Style.COLOR_BG);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 10));

        javax.swing.table.DefaultTableCellRenderer leftRenderer = new javax.swing.table.DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        table.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);

        table.getColumnModel().getColumn(5).setCellRenderer(new ActionCellRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionCellEditor());

        // Wajib set lebar kolom agar 3 tombol muat sejajar dan tidak terpotong
        table.getColumnModel().getColumn(5).setMinWidth(180);
        table.getColumnModel().getColumn(5).setMaxWidth(180);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Style.COLOR_WHITE);

        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 25, 20, 25));

        Style.RoundedButtonSmall history = new Style.RoundedButtonSmall("History");
        history.setPreferredSize(new Dimension(120, 36));
        history.addActionListener(e -> openHistory());

        Style.RoundedButtonSmall btnLogout = new Style.RoundedButtonSmall("Logout");
        btnLogout.setPreferredSize(new Dimension(100, 36));
        btnLogout.setBackground(new Color(255, 82, 82));
        btnLogout.setForeground(Color.WHITE);

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    Dashboard.this,
                    "Yakin mau keluar?",
                    "Konfirmasi Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new Main().setVisible(true);
            }
        });

        footer.add(history, BorderLayout.WEST);
        footer.add(btnLogout, BorderLayout.EAST);

        return footer;
    }

    // ===================== LOGIC CSV & FILTER =====================

    private void loadFromCsv() {
        allRows.clear();
        File f = new File(DATA_CSV);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] v = line.split(",", -1);
                // Filter hanya memuat data milik user yang sedang login
                if (v.length >= 5 && v[0].trim().equals(username)) {
                    allRows.add(new SubRow(v[1].trim(), v[2].trim(), v[3].trim(), v[4].trim()));
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void saveToCsv() {
        List<String> otherUsersData = new ArrayList<>();
        File f = new File(DATA_CSV);
        if (f.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] v = line.split(",", -1);
                    // Simpan data user lain agar tidak tertimpa/hilang
                    if (v.length > 0 && !v[0].equals(username)) {
                        otherUsersData.add(line);
                    }
                }
            } catch (IOException e) { e.printStackTrace(); }
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_CSV))) {
            for (String line : otherUsersData) pw.println(line);
            for (SubRow r : allRows) {
                pw.println(username + "," + r.nama + "," + r.link + "," + r.harga + "," + r.tenggat);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void applyFilter(String keyword) {
        String key = (keyword == null) ? "" : keyword.trim().toLowerCase();
        tableModel.setRowCount(0);
        int no = 1;
        for (SubRow r : allRows) {
            if (key.isEmpty() || r.matches(key)) {
                tableModel.addRow(new Object[]{no++, r.nama, r.link, r.harga, r.tenggat, ""});
            }
        }
    }

    // ===================== NAVIGATION =====================

    private void openCreateData() {
        dispose();
        new CreateData(this.username).setVisible(true);
    }

    private void openHistory() {
        dispose();
        new History(this.username).setVisible(true);
    }

    // ===================== BUTTON RENDERER & EDITOR =====================

    private ImageIcon loadIcon(String path) {
        URL url = getClass().getResource(path);
        if (url == null) return null;
        ImageIcon raw = new ImageIcon(url);
        Image scaled = raw.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private void validateIconsOrWarn() {
        if (getClass().getResource(ICON_PAY) == null) System.err.println("WARNING: Icon pay.png not found!");
        if (getClass().getResource(ICON_EDIT) == null) System.err.println("WARNING: Icon edit.png not found!");
        if (getClass().getResource(ICON_DELETE) == null) System.err.println("WARNING: Icon delete.png not found!");
    }

    private JButton createIconButton(String iconPath, String tooltip) {
        JButton b = new JButton();
        ImageIcon icon = loadIcon(iconPath);
        if (icon != null) b.setIcon(icon);
        else b.setText(tooltip.substring(0, 1));

        b.setToolTipText(tooltip);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(40, 35));
        return b;
    }

    private class ActionCellRenderer extends JPanel implements TableCellRenderer {
        private final JButton btnPay = createIconButton(ICON_PAY, "Bayar");
        private final JButton btnEdit = createIconButton(ICON_EDIT, "Edit");
        private final JButton btnDel = createIconButton(ICON_DELETE, "Hapus");

        ActionCellRenderer() {
            setOpaque(true);
            setBackground(Style.COLOR_WHITE);
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            add(btnPay); add(btnEdit); add(btnDel);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : Style.COLOR_WHITE);
            return this;
        }
    }

    private class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        private final JButton btnPay = createIconButton(ICON_PAY, "Bayar");
        private final JButton btnEdit = createIconButton(ICON_EDIT, "Edit");
        private final JButton btnDel = createIconButton(ICON_DELETE, "Hapus");
        private int editingRow = -1;

        ActionCellEditor() {
            panel.setOpaque(true);
            panel.setBackground(Style.COLOR_WHITE);

            btnPay.addActionListener(e -> onPay());
            btnEdit.addActionListener(e -> onEdit());
            btnDel.addActionListener(e -> onDelete());

            panel.add(btnPay); panel.add(btnEdit); panel.add(btnDel);
        }

        private void onEdit() {
            SubRow r = getRowFromVisibleIndex(editingRow);
            if (r == null) { fireEditingStopped(); return; }

            Style.RoundedTextField fNama = new Style.RoundedTextField(20);
            fNama.setText(r.nama);
            fNama.setPlaceholder("Nama Layanan");

            Style.RoundedTextField fLink = new Style.RoundedTextField(20);
            fLink.setText(r.link);
            fLink.setPlaceholder("Link / URL");

            String cleanHarga = r.harga.replaceAll("[^0-9]", "");
            Style.RoundedTextField fHarga = new Style.RoundedTextField(20);
            fHarga.setText(cleanHarga);
            fHarga.setPlaceholder("Harga (Angka)");

            Style.RoundedTextField fTenggat = new Style.RoundedTextField(20);
            fTenggat.setText(r.tenggat);
            fTenggat.setPlaceholder("Contoh: 25 Januari 2026");

            // GridLayout(0, 1) memaksa layout vertikal (tumpuk ke bawah)
            JPanel p = new JPanel(new GridLayout(0, 1, 0, 2));
            p.setOpaque(false);

            p.add(createLabel("Nama Layanan:"));
            p.add(fNama);
            p.add(createLabel("Link:"));
            p.add(fLink);
            p.add(createLabel("Harga (Angka Saja):"));
            p.add(fHarga);
            p.add(createLabel("Tenggat (dd MMMM yyyy):"));
            p.add(fTenggat);

            // Loop validation: dialog tidak menutup jika input invalid (UX improvement)
            while (true) {
                int result = JOptionPane.showConfirmDialog(
                        Dashboard.this, p, "Edit Data Langganan",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
                );

                if (result != JOptionPane.OK_OPTION) break;

                String valNama = fNama.getText().trim();
                String valLink = fLink.getText().trim();
                String valHarga = fHarga.getText().trim();
                String valTenggat = fTenggat.getText().trim();

                if (valNama.isEmpty() || valHarga.isEmpty() || valTenggat.isEmpty()) {
                    JOptionPane.showMessageDialog(Dashboard.this, "Nama, Harga, dan Tenggat wajib diisi!", "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                try {
                    LocalDate d = LocalDate.parse(valTenggat, ID_DATE);
                    if (d.isBefore(LocalDate.now())) {
                        JOptionPane.showMessageDialog(Dashboard.this,
                                "Tanggal tenggat tidak boleh kurang dari hari ini!",
                                "Validasi Tanggal", JOptionPane.WARNING_MESSAGE);
                        continue;
                    }
                    r.tenggat = valTenggat;
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(Dashboard.this,
                            "Format tanggal salah!\nGunakan format: 25 Januari 2026\n(Bulan Bahasa Indonesia)",
                            "Error Format", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                try {
                    long h = Long.parseLong(valHarga);
                    r.harga = "Rp" + java.text.NumberFormat.getInstance(new Locale("id")).format(h);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(Dashboard.this, "Harga harus berupa angka valid!", "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                r.nama = valNama;
                r.link = valLink.isEmpty() ? "-" : valLink;

                saveToCsv();
                applyFilter(searchField.getText());
                JOptionPane.showMessageDialog(Dashboard.this, "Data berhasil diperbarui!");
                break;
            }

            fireEditingStopped();
        }

        private JLabel createLabel(String text) {
            JLabel l = new JLabel(text);
            l.setFont(new Font("SansSerif", Font.BOLD, 12));
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            return l;
        }

        private void onPay() {
            SubRow r = getRowFromVisibleIndex(editingRow);
            if (r == null) { fireEditingStopped(); return; }

            Integer[] opts = {1, 2, 3, 6, 7, 8, 9, 10, 11, 12};
            JComboBox<Integer> cb = new JComboBox<>(opts);

            JPanel p = new JPanel(new GridLayout(0, 1, 0, 10));
            p.setOpaque(false);
            JLabel lbl = new JLabel("Perpanjang berapa bulan?");
            lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
            p.add(lbl);
            p.add(cb);

            if (JOptionPane.showConfirmDialog(Dashboard.this, p, "Pembayaran", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                try {
                    int bulan = (Integer) cb.getSelectedItem();
                    LocalDate d = LocalDate.parse(r.tenggat, ID_DATE).plusMonths(bulan);
                    String tenggatBaru = d.format(ID_DATE);

                    long harga = Long.parseLong(r.harga.replaceAll("[^0-9]", ""));
                    String total = "Rp" + java.text.NumberFormat.getInstance(new Locale("id")).format(harga * bulan);

                    r.tenggat = tenggatBaru;
                    saveToCsv();

                    try (PrintWriter pw = new PrintWriter(new FileWriter(HISTORY_CSV, true))) {
                        pw.println(username + "," + r.nama + "," + r.tenggat + "," + bulan + "," + total + "," +
                                LocalDate.now().format(ID_DATE) + "," + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
                    }

                    applyFilter(searchField.getText());

                    String msg = "Pembayaran Berhasil! ✅\n\n" +
                            "Layanan      : " + r.nama + "\n" +
                            "Durasi       : " + bulan + " Bulan\n" +
                            "Total Bayar  : " + total + "\n" +
                            "Tenggat Baru : " + tenggatBaru;

                    JOptionPane.showMessageDialog(Dashboard.this, msg, "Sukses", JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) { ex.printStackTrace(); }
            }
            fireEditingStopped();
        }

        private void onDelete() {
            SubRow r = getRowFromVisibleIndex(editingRow);
            if (r != null && JOptionPane.showConfirmDialog(Dashboard.this, "Hapus " + r.nama + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                allRows.remove(r);
                saveToCsv();
                applyFilter(searchField.getText());
            }
            fireEditingStopped();
        }

        private SubRow getRowFromVisibleIndex(int row) {
            if (row < 0 || row >= table.getRowCount()) return null;
            String nama = (String) table.getValueAt(row, 1);
            return allRows.stream().filter(r -> r.nama.equals(nama)).findFirst().orElse(null);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.editingRow = row;
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() { return ""; }
    }

    public static class SubRow {
        public String nama;
        String link;
        public String harga;
        String tenggat;
        public SubRow(String n, String l, String h, String t) { nama = n; link = l; harga = h; tenggat = t; }
        public boolean matches(String k) { return nama.toLowerCase().contains(k) || link.toLowerCase().contains(k); }
    }
}