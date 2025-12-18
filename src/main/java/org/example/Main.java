package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main extends JFrame {

    private static final String CSV_FILE = "users.csv";

    public Main() {
        // Setup Window
        setTitle("Sistem Manajemen Langganan - Login");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        // Pakai warna background dari file Style
        getContentPane().setBackground(Style.COLOR_BG);

        // --- Panel Login ---
        Style.RoundedPanel loginCard = new Style.RoundedPanel();

        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setBorder(new EmptyBorder(40, 50, 40, 50));

        // --- Komponen UI (Ambil dari Style) ---

        // Judul
        JLabel titleLabel = new JLabel("LOGIN");
        titleLabel.setFont(Style.FONT_TITLE); // Ambil font dari Style
        titleLabel.setForeground(Style.COLOR_PRIMARY); // Ambil warna dari Style
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(Style.FONT_LABEL);
        userLabel.setForeground(Style.COLOR_TEXT);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Pake Custom Component dari Style
        Style.RoundedTextField userField = new Style.RoundedTextField(20);
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Password
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(Style.FONT_LABEL);
        passLabel.setForeground(Style.COLOR_TEXT);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        Style.RoundedPasswordField passField = new Style.RoundedPasswordField(20);
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Tombol Login
        Style.RoundedButton loginButton = new Style.RoundedButton("Masuk Sekarang");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        // --- Logic Tombol ---
        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            if (checkLogin(username, password)) {
                JOptionPane.showMessageDialog(this, "Login Berhasil! Welcome " + username);
                openDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Username / Password Salah!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- Tata Letak (Layouting) ---
        loginCard.add(titleLabel);
        loginCard.add(Box.createRigidArea(new Dimension(0, 30)));

        // Helper panel biar label rata kiri
        loginCard.add(createLeftPanel(userLabel));
        loginCard.add(Box.createRigidArea(new Dimension(0, 10)));
        loginCard.add(userField);
        loginCard.add(Box.createRigidArea(new Dimension(0, 20)));

        loginCard.add(createLeftPanel(passLabel));
        loginCard.add(Box.createRigidArea(new Dimension(0, 10)));
        loginCard.add(passField);
        loginCard.add(Box.createRigidArea(new Dimension(0, 30)));
        loginCard.add(loginButton);

        add(loginCard);
    }

    // Helper kecil buat layout label biar rata kiri tapi panelnya center
    private JPanel createLeftPanel(JComponent comp) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Style.COLOR_WHITE);
        p.add(comp, BorderLayout.WEST);
        return p;
    }

    // --- Logic Baca CSV ---
    private boolean checkLogin(String inputUser, String inputPass) {
        if (inputUser.isEmpty() || inputPass.isEmpty()) return false;

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 2) {
                    if (values[0].trim().equals(inputUser) && values[1].trim().equals(inputPass)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void openDashboard() {
        this.dispose();
        // new Dashboard().setVisible(true); // Nanti di uncomment pas dashboard jadi
        System.out.println("Masuk Dashboard...");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}