package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;

public class History extends JFrame {

    private String username;
    private JTable table;
    private DefaultTableModel tableModel;

    public History(String username) {
        this.username = username;

        setTitle("Riwayat Pembayaran - " + username);
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Style.COLOR_BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Style.COLOR_BG);
        header.setBorder(new EmptyBorder(30, 30, 20, 30));

        JLabel title = new JLabel("Riwayat Pembayaran");
        title.setFont(Style.FONT_TITLE);
        title.setForeground(Style.COLOR_PRIMARY);

        Style.RoundedButtonOutline btnBack = new Style.RoundedButtonOutline("Kembali");
        btnBack.setPreferredSize(new Dimension(100, 40));
        btnBack.addActionListener(e -> {
            dispose();
            new Dashboard(this.username).setVisible(true);
        });

        header.add(title, BorderLayout.WEST);
        header.add(btnBack, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        add(buildTableCard(), BorderLayout.CENTER);

        loadData();
    }

    private JPanel buildTableCard() {
        Style.RoundedPanelWithBorder card = new Style.RoundedPanelWithBorder();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] columns = {"Nama Layanan", "Tenggat Baru", "Durasi (Bln)", "Total Bayar", "Tanggal Bayar", "Waktu"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);

        // Custom Sorter untuk Tanggal (Kolom Index 4) agar sorting akurat secara kalender
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        Comparator<String> dateComparator = (s1, s2) -> {
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
                LocalDate d1 = LocalDate.parse(s1, fmt);
                LocalDate d2 = LocalDate.parse(s2, fmt);
                return d1.compareTo(d2);
            } catch (Exception e) { return s1.compareTo(s2); }
        };
        sorter.setComparator(4, dateComparator);

        table.setRowHeight(40);
        table.setFont(Style.FONT_INPUT);
        table.getTableHeader().setFont(Style.FONT_LABEL);
        table.getTableHeader().setBackground(Style.COLOR_WHITE);
        table.setShowVerticalLines(false);
        table.setGridColor(Style.COLOR_ACCENT);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Style.COLOR_WHITE);

        card.add(sp, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 30, 30, 30));
        wrapper.add(card);

        return wrapper;
    }

    private void loadData() {
        File file = new File("history.csv");
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                // Filter hanya history milik user yang sedang login
                if (parts.length >= 7 && parts[0].equals(this.username)) {
                    tableModel.addRow(new Object[]{
                            parts[1], // Nama
                            parts[2], // Tenggat
                            parts[3], // Durasi
                            parts[4], // Total
                            parts[5], // Tgl Bayar
                            parts[6]  // Waktu
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}