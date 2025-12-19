package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;

public class History extends JFrame {

    private static final String HISTORY_CSV = "history.csv";

    private final String username;

    private JTable table;
    private DefaultTableModel tableModel;

    private final DateTimeFormatter ID_DATE =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));

    // Constructor utama (dipakai kalau kamu mau kirim username dari Dashboard)
    public History(String username) {
        this.username = (username == null || username.isBlank()) ? "User" : username;

        setTitle("Riwayat Pembayaran");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        getContentPane().setBackground(Style.COLOR_BG);
        setLayout(new BorderLayout(20, 20));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        loadHistoryFromCsv();
        setupSorting();
    }

    // Constructor default (biar tidak error kalau ada pemanggilan new History() di kode lama)
    public History() {
        this("User");
    }

    // ===================== UI =====================

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 25, 0, 25));

        JLabel title = new JLabel("Riwayat Pembayaran");
        title.setFont(Style.FONT_TITLE);
        title.setForeground(Style.COLOR_PRIMARY);

        header.add(title, BorderLayout.WEST);
        return header;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(0, 25, 0, 25));

        // Container rounded (pakai Style.RoundedPanel yang sudah ada)
        Style.RoundedPanel card = new Style.RoundedPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Table model (sesuai kolom dari history.csv)
        tableModel = new DefaultTableModel(
                new Object[]{
                        "No",
                        "Nama Layanan",
                        "Tenggat Baru",
                        "Durasi",
                        "Total Bayar",
                        "Tanggal Bayar",
                        "Waktu Bayar"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(44);
        table.setFont(Style.FONT_INPUT);
        table.setForeground(Style.COLOR_TEXT);
        table.setBackground(Style.COLOR_WHITE);
        table.setGridColor(Style.COLOR_ACCENT);

        // Header styling (modern rapi)
        table.getTableHeader().setFont(Style.FONT_LABEL);
        table.getTableHeader().setForeground(Style.COLOR_TEXT);
        table.getTableHeader().setBackground(Style.COLOR_WHITE);

        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 10));
        table.setSelectionBackground(Style.COLOR_BG);
        table.setSelectionForeground(Style.COLOR_TEXT);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Style.COLOR_WHITE);

        card.add(sp, BorderLayout.CENTER);
        center.add(card, BorderLayout.CENTER);

        return center;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 25, 20, 25));

        // Tidak ada outline button di Style.java yang kamu kirim,
        // jadi pakai RoundedButton supaya konsisten dan tidak hardcode style.
        Style.RoundedButton btnBack = new Style.RoundedButton("Kembali");
        btnBack.setPreferredSize(new Dimension(140, 45));
        btnBack.addActionListener(e -> {
            dispose();
            new Dashboard(username).setVisible(true);
        });

        footer.add(btnBack, BorderLayout.WEST);
        return footer;
    }

    // ===================== CSV =====================

    private void loadHistoryFromCsv() {
        tableModel.setRowCount(0);

        File f = new File(HISTORY_CSV);
        if (!f.exists()) {
            JOptionPane.showMessageDialog(
                    this,
                    "File history.csv belum ditemukan.\nLetakkan di root project (sejajar users.csv).",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            int no = 1;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Urutan CSV:
                // Nama Layanan, Tenggat Baru, Durasi, Total Bayar, Tanggal Bayar, Waktu Bayar
                String[] v = line.split(",", -1);
                if (v.length < 6) continue;

                tableModel.addRow(new Object[]{
                        no++,
                        v[0].trim(),
                        v[1].trim(),
                        v[2].trim(),
                        v[3].trim(),
                        v[4].trim(),
                        v[5].trim()
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Gagal membaca history.csv",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ===================== SORTING =====================

    private void setupSorting() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

        // Comparator khusus untuk tanggal Indonesia: "dd MMMM yyyy"
        Comparator<String> indoDateComparator = (a, b) -> {
            LocalDate da = parseIndoDateSafe(a);
            LocalDate db = parseIndoDateSafe(b);

            // Kalau dua-duanya bisa diparse -> bandingin tanggal beneran
            if (da != null && db != null) return da.compareTo(db);

            // Kalau salah satu gagal parse, fallback ke compare string (biar tetap stabil)
            if (a == null) a = "";
            if (b == null) b = "";
            return a.compareToIgnoreCase(b);
        };

        sorter.setComparator(2, indoDateComparator); // Tenggat Baru
        sorter.setComparator(5, indoDateComparator); // Tanggal Bayar (wajib)

        table.setRowSorter(sorter);
        sorter.setSortable(0, false); // No tidak usah di-sort biar tidak bikin bingung
    }

    private LocalDate parseIndoDateSafe(String s) {
        try {
            if (s == null) return null;
            s = s.trim();
            if (s.isEmpty()) return null;
            return LocalDate.parse(s, ID_DATE);
        } catch (Exception ex) {
            return null;
        }
    }
}
