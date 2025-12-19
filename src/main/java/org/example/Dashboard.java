package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.LocalTime;
import java.util.*;
import java.util.List;

public class Dashboard extends JFrame {

    private static final String DATA_CSV = "subscriptions.csv";
    private static final String HISTORY_CSV = "history.csv";

    private final String username;

    private Style.RoundedTextField searchField;
    private JTable table;
    private DefaultTableModel tableModel;

    private final List<SubRow> allRows = new ArrayList<>();

    private final DateTimeFormatter ID_DATE =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));

    // --- ICON PATHS (resources) ---
    private static final String ICON_PAY = "/icons/pay.png";
    private static final String ICON_EDIT = "/icons/edit.png";
    private static final String ICON_DELETE = "/icons/delete.png";

    public Dashboard(String username) {
        this.username = username;

        setTitle("Sistem Manajemen Langganan - Dashboard");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        getContentPane().setBackground(Style.COLOR_BG);
        setLayout(new BorderLayout(20, 20));

        // Validasi icon biar jelas kalau path salah
        validateIconsOrWarn();

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        loadFromCsv();
        applyFilter("");
    }

    // ===================== UI =====================

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

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        Style.RoundedButton btnCreate = new Style.RoundedButton("Create Data");
        btnCreate.setPreferredSize(new Dimension(160, 45));
        btnCreate.addActionListener(e -> openCreateData());
        left.add(btnCreate);

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);

        searchField = new Style.RoundedTextField(25);
        searchField.setPreferredSize(new Dimension(420, 45));
        searchField.setPlaceholder("Search...");

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(searchField.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(searchField.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(searchField.getText()); }
        });

        right.add(searchField, BorderLayout.EAST);

        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private JPanel buildTableCard() {
        Style.RoundedPanelWithBorder card = new Style.RoundedPanelWithBorder();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 1. Setup Model Table
        // Kita override getColumnClass biar kolom "No" dibaca sebagai Angka (Integer), bukan Teks
        tableModel = new DefaultTableModel(
                new Object[]{"No", "Nama", "Link", "Harga", "Tenggat", "Aksi"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 5; // Cuma kolom aksi yang bisa diklik
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class; // PENTING: Biar sorting No (1, 2, 10) bener
                return super.getColumnClass(columnIndex);
            }
        };

        table = new JTable(tableModel);

        // --- LOGIC SORTING KHUSUS ---
        table.setAutoCreateRowSorter(true);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        javax.swing.table.DefaultTableCellRenderer leftRenderer = new javax.swing.table.DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);

        table.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);

        // A. Comparator Khusus Harga (String "Rp65.000" -> Angka 65000)
        java.util.Comparator<String> priceComparator = (s1, s2) -> {
            try {
                // Hapus "Rp", hapus titik, hapus spasi
                String clean1 = s1.replace("Rp", "").replace(".", "").trim();
                String clean2 = s2.replace("Rp", "").replace(".", "").trim();
                // Ubah jadi angka long
                long v1 = Long.parseLong(clean1);
                long v2 = Long.parseLong(clean2);
                return Long.compare(v1, v2);
            } catch (Exception e) {
                return s1.compareTo(s2); // Fallback kalau format salah
            }
        };

        // B. Terapkan Comparator ke Kolom Harga (Index 3)
        sorter.setComparator(3, priceComparator);

        // C. Atur Kolom Mana Saja yang Boleh Disort
        // List kolom: 0=No, 1=Nama, 2=Link, 3=Harga, 4=Tenggat, 5=Aksi
        sorter.setSortable(0, true);  // No (Boleh)
        sorter.setSortable(1, true);  // Nama (Boleh)
        sorter.setSortable(2, false); // Link (GAK BOLEH)
        sorter.setSortable(3, true);  // Harga (Boleh - Pake logic khusus tadi)
        sorter.setSortable(4, false); // Tenggat (GAK BOLEH)
        sorter.setSortable(5, false); // Aksi (GAK BOLEH)
        // -----------------------------

        // Styling Table (Sama kayak sebelumnya)
        table.setRowHeight(48);
        table.setFont(Style.FONT_INPUT);
        table.setForeground(Style.COLOR_TEXT);
        table.setBackground(Style.COLOR_WHITE);
        table.setGridColor(Style.COLOR_ACCENT);

        table.getTableHeader().setFont(Style.FONT_LABEL);
        table.getTableHeader().setForeground(Style.COLOR_TEXT);
        table.getTableHeader().setBackground(Style.COLOR_WHITE);
        table.setSelectionBackground(Style.COLOR_BG);
        table.setSelectionForeground(Style.COLOR_TEXT);

        table.getColumnModel().getColumn(5).setCellRenderer(new ActionCellRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionCellEditor());

        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 10));

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

        footer.add(history, BorderLayout.WEST);
        return footer;
    }

    // ===================== CSV =====================

    private void loadFromCsv() {
        allRows.clear();
        File f = new File(DATA_CSV);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] v = line.split(",", -1);
                if (v.length < 4) continue;

                allRows.add(new SubRow(v[0].trim(), v[1].trim(), v[2].trim(), v[3].trim()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToCsv() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_CSV))) {
            for (SubRow r : allRows) {
                pw.println(r.nama + "," + r.link + "," + r.harga + "," + r.tenggat);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===================== FILTER =====================

    private void applyFilter(String keyword) {
        String key = (keyword == null) ? "" : keyword.trim().toLowerCase();

        tableModel.setRowCount(0);
        int no = 1;

        for (SubRow r : allRows) {
            if (key.isEmpty() || r.matches(key)) {
                tableModel.addRow(new Object[]{
                        no++, r.nama, r.link, r.harga, r.tenggat, ""
                });
            }
        }
    }

    // ===================== NAV =====================

    private void openCreateData() {
        dispose();
        // Kirim username yang sekarang lagi login ke halaman Create
        new CreateData(this.username).setVisible(true);
    }

    private void openHistory() {
        dispose();
        new History(this.username).setVisible(true);
    }

    // ===================== ICON UTILS =====================

    private void validateIconsOrWarn() {
        List<String> missing = new ArrayList<>();
        if (getClass().getResource(ICON_PAY) == null) missing.add(ICON_PAY);
        if (getClass().getResource(ICON_EDIT) == null) missing.add(ICON_EDIT);
        if (getClass().getResource(ICON_DELETE) == null) missing.add(ICON_DELETE);

        if (!missing.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Icon tidak ditemukan di resources:\n" + String.join("\n", missing) +
                            "\n\nPastikan ikon ada di:\nsrc/main/resources/icons/",
                    "Resource Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private ImageIcon loadIcon(String path, int w, int h) {
        URL url = getClass().getResource(path);
        if (url == null) return null;

        ImageIcon raw = new ImageIcon(url);
        Image scaled = raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private JButton iconOnlyButton(String iconPath, String tooltip) {
        JButton b = new JButton();
        b.setIcon(loadIcon(iconPath, 22, 22));
        b.setToolTipText(tooltip);

        // PENTING: bikin ikon pasti keliatan (tanpa kotak default)
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);

        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setVerticalAlignment(SwingConstants.CENTER);
        b.setBorder(new EmptyBorder(2, 2, 2, 2));
        b.setPreferredSize(new Dimension(38, 32));
        return b;
    }

    // ===================== TABLE ACTIONS =====================

    private class ActionCellRenderer extends JPanel implements TableCellRenderer {
        private final JButton payBtn = iconOnlyButton(ICON_PAY, "Bayar / Perpanjang");
        private final JButton editBtn = iconOnlyButton(ICON_EDIT, "Edit");
        private final JButton delBtn = iconOnlyButton(ICON_DELETE, "Hapus");

        ActionCellRenderer() {
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.CENTER, 8, 6));
            add(payBtn);
            add(editBtn);
            add(delBtn);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            return this;
        }
    }

    private class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        private final JButton payBtn = iconOnlyButton(ICON_PAY, "Bayar / Perpanjang");
        private final JButton editBtn = iconOnlyButton(ICON_EDIT, "Edit");
        private final JButton delBtn = iconOnlyButton(ICON_DELETE, "Hapus");

        private int editingRow = -1;

        ActionCellEditor() {
            panel.setOpaque(false);

            payBtn.addActionListener(e -> onPay());
            editBtn.addActionListener(e -> onEdit());
            delBtn.addActionListener(e -> onDelete());

            panel.add(payBtn);
            panel.add(editBtn);
            panel.add(delBtn);
        }

        private SubRow getRowFromVisibleIndex(int visibleRowIndex) {
            if (visibleRowIndex < 0) return null;

            String nama = String.valueOf(table.getValueAt(visibleRowIndex, 1));
            String link = String.valueOf(table.getValueAt(visibleRowIndex, 2));
            String harga = String.valueOf(table.getValueAt(visibleRowIndex, 3));
            String tenggat = String.valueOf(table.getValueAt(visibleRowIndex, 4));

            for (SubRow r : allRows) {
                if (r.nama.equals(nama) && r.link.equals(link) && r.harga.equals(harga) && r.tenggat.equals(tenggat)) {
                    return r;
                }
            }
            return null;
        }

        private void onPay() {
            SubRow r = getRowFromVisibleIndex(editingRow);
            if (r == null) { fireEditingStopped(); return; }

            Integer[] pilihan = new Integer[12];
            for (int i = 0; i < 12; i++) pilihan[i] = i + 1;

            JComboBox<Integer> combo = new JComboBox<>(pilihan);

            int res = JOptionPane.showConfirmDialog(
                    Dashboard.this,
                    combo,
                    "Perpanjang berapa bulan?",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (res == JOptionPane.OK_OPTION) {
                try {
                    // 1) Update tenggat
                    LocalDate d = LocalDate.parse(r.tenggat, ID_DATE);
                    int bulan = (Integer) combo.getSelectedItem();
                    String tenggatBaru = d.plusMonths(bulan).format(ID_DATE);
                    r.tenggat = tenggatBaru;

                    // 2) Hitung total bayar (harga * bulan)
                    // Bersihin "Rp" dan titik, ambil digit saja
                    String hargaRaw = (r.harga == null) ? "" : r.harga.trim();
                    String angkaOnly = hargaRaw
                            .replace("Rp", "")
                            .replace("rp", "")
                            .replace(".", "")
                            .replace(" ", "")
                            .replaceAll("[^0-9]", "");

                    long hargaPerBulan = angkaOnly.isEmpty() ? 0L : Long.parseLong(angkaOnly);
                    long total = hargaPerBulan * bulan;

                    // Format balik Rupiah: Rp50.000
                    java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new Locale("id", "ID"));
                    String totalRupiah = "Rp" + nf.format(total);

                    // 3) Tanggal & waktu bayar sekarang
                    String tanggalBayar = LocalDate.now().format(ID_DATE);
                    String waktuBayar = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                    // 4) Save subscriptions.csv
                    saveToCsv();
                    applyFilter(searchField.getText());

                    // 5) Append ke history.csv
                    // Format: Nama Layanan, Tenggat Baru, Durasi (Bulan), Total Bayar, Tanggal Bayar, Waktu Bayar
                    try (PrintWriter pw = new PrintWriter(new FileWriter("history.csv", true))) {
                        pw.println(
                                r.nama + "," +
                                        tenggatBaru + "," +
                                        bulan + "," +
                                        totalRupiah + "," +
                                        tanggalBayar + "," +
                                        waktuBayar
                        );
                    } catch (IOException io) {
                        io.printStackTrace();
                        JOptionPane.showMessageDialog(
                                Dashboard.this,
                                "Tenggat berhasil diperpanjang, tapi gagal menulis history.csv",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }

                    JOptionPane.showMessageDialog(
                            Dashboard.this,
                            "Pembayaran sukses!\n" +
                                    "Durasi: " + bulan + " bulan\n" +
                                    "Total: " + totalRupiah + "\n" +
                                    "Tenggat baru: " + tenggatBaru,
                            "Sukses",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            Dashboard.this,
                            "Format tenggat harus: 08 Januari 2026",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

            fireEditingStopped();
        }


        private JLabel label(String text) {
            JLabel l = new JLabel(text);
            l.setFont(Style.FONT_LABEL);
            l.setForeground(Style.COLOR_TEXT);
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            return l;
        }

        private void onEdit() {
            SubRow r = getRowFromVisibleIndex(editingRow);
            if (r == null) { fireEditingStopped(); return; }

            Style.RoundedTextField namaF = new Style.RoundedTextField(20);
            Style.RoundedTextField linkF = new Style.RoundedTextField(20);
            Style.RoundedTextField hargaF = new Style.RoundedTextField(20);
            Style.RoundedTextField tenggatF = new Style.RoundedTextField(20);

            namaF.setText(r.nama);
            linkF.setText(r.link);
            hargaF.setText(r.harga);
            tenggatF.setText(r.tenggat);

            namaF.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            linkF.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            hargaF.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            tenggatF.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

            JPanel form = new JPanel();
            form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
            form.setBackground(Style.COLOR_WHITE);
            form.setBorder(new EmptyBorder(15, 15, 15, 15));

            form.add(label("Nama"));
            form.add(namaF);
            form.add(Box.createRigidArea(new Dimension(0, 10)));

            form.add(label("Link"));
            form.add(linkF);
            form.add(Box.createRigidArea(new Dimension(0, 10)));

            form.add(label("Harga"));
            form.add(hargaF);
            form.add(Box.createRigidArea(new Dimension(0, 10)));

            form.add(label("Tenggat (dd MMMM yyyy)"));
            form.add(tenggatF);

            int res = JOptionPane.showConfirmDialog(
                    Dashboard.this,
                    form,
                    "Edit Data Langganan",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (res == JOptionPane.OK_OPTION) {
                String nama = namaF.getText().trim();
                String link = linkF.getText().trim();
                String harga = hargaF.getText().trim();
                String tenggat = tenggatF.getText().trim();

                if (nama.isEmpty() || link.isEmpty() || harga.isEmpty() || tenggat.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            Dashboard.this,
                            "Semua field wajib diisi.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    fireEditingStopped();
                    return;
                }

                try { LocalDate.parse(tenggat, ID_DATE); }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            Dashboard.this,
                            "Format tenggat harus: 08 Januari 2026",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    fireEditingStopped();
                    return;
                }

                r.nama = nama;
                r.link = link;
                r.harga = harga;
                r.tenggat = tenggat;

                saveToCsv();
                applyFilter(searchField.getText());
            }

            fireEditingStopped();
        }

        private void onDelete() {
            SubRow r = getRowFromVisibleIndex(editingRow);
            if (r == null) { fireEditingStopped(); return; }

            int confirm = JOptionPane.showConfirmDialog(
                    Dashboard.this,
                    "Hapus data \"" + r.nama + "\" ?",
                    "Konfirmasi",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                allRows.remove(r);
                saveToCsv();
                applyFilter(searchField.getText());
            }

            fireEditingStopped();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.editingRow = row;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    // ===================== MODEL =====================

    private static class SubRow {
        String nama, link, harga, tenggat;

        SubRow(String nama, String link, String harga, String tenggat) {
            this.nama = nama;
            this.link = link;
            this.harga = harga;
            this.tenggat = tenggat;
        }

        boolean matches(String key) {
            return nama.toLowerCase().contains(key)
                    || link.toLowerCase().contains(key)
                    || harga.toLowerCase().contains(key)
                    || tenggat.toLowerCase().contains(key);
        }
    }
}
