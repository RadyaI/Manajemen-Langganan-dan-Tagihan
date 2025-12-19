package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class Style {

    // --- 1. DEFINISI WARNA (PALETTE) ---
    public static final Color COLOR_BG = Color.decode("#EBF4DD");      // Cream
    public static final Color COLOR_ACCENT = Color.decode("#90AB8B");  // Sage Green
    public static final Color COLOR_PRIMARY = Color.decode("#5A7863"); // Forest Green
    public static final Color COLOR_TEXT = Color.decode("#3B4953");    // Dark Slate
    public static final Color COLOR_WHITE = Color.WHITE;

    // --- 2. DEFINISI FONT ---
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 32);
    public static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_INPUT = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD, 16);

    // --- 3. CUSTOM COMPONENTS (Rounded Styles) ---

    // A. Input Text Biasa (Rounded)
    // A. Input Text Biasa (Rounded + Placeholder Support)
    public static class RoundedTextField extends JTextField {
        private String placeholder = "";

        public RoundedTextField(int columns) {
            super(columns);
            setOpaque(false);
            setFont(FONT_INPUT);
            setBorder(new EmptyBorder(10, 15, 10, 15));
        }

        // Method buat set text placeholder
        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. Gambar Background Putih
            g2.setColor(COLOR_WHITE);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);

            // 2. Gambar Border Hijau
            g2.setColor(COLOR_ACCENT);
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);

            // 3. Gambar Text Asli (Inputan User)
            super.paintComponent(g);

            // 4. Gambar Placeholder (Kalau teks kosong & tidak lagi diketik)
            if (getText().isEmpty() && !placeholder.isEmpty()) {
                // Set warna placeholder (abu-abu transparan biar mirip browser)
                g2.setColor(new Color(150, 150, 150));
                g2.setFont(getFont().deriveFont(Font.ITALIC)); // Opsional: bikin miring dikit

                // Hitung posisi biar pas di tengah vertikal
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

                // Gambar teks (padding kiri 17 biar gak nempel border)
                g2.drawString(placeholder, 17, y);
            }
        }
    }

    // B. Input Password (Rounded)
    public static class RoundedPasswordField extends JPasswordField {
        public RoundedPasswordField(int columns) {
            super(columns);
            setOpaque(false);
            setFont(FONT_INPUT);
            setBorder(new EmptyBorder(10, 15, 10, 15));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COLOR_WHITE);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            g2.setColor(COLOR_ACCENT);
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            super.paintComponent(g);
        }
    }

    // C. Tombol (Rounded + Hover Effect)
    public static class RoundedButton extends JButton {
        private boolean isHovered = false;

        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(FONT_BUTTON);
            setForeground(COLOR_WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                @Override
                public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Ganti warna saat hover
            g2.setColor(isHovered ? COLOR_TEXT : COLOR_PRIMARY);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            super.paintComponent(g);
        }
    }

    // --- Buka Style.java, taruh ini di paling bawah sebelum tutup kurung terakhir ---

    // D. Panel (Kotak Kontainer) Rounded
    public static class RoundedPanel extends JPanel {
        public RoundedPanel() {
            // Penting biar background di luar sudut rounded jadi transparan
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            // Biar garisnya halus (anti-aliasing)
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Warna background panel (PUTIH)
            g2.setColor(COLOR_WHITE);
            // Gambar kotak rounded. Angka 40, 40 itu tingkat kelengkungannya
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);

            super.paintComponent(g);
        }
    }

    // --- 4. TAMBAHAN FONT (untuk Dashboard) ---
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);

    // Tombol kecil (Rounded, cocok untuk "History", "Bayar", "Hapus")
    public static class RoundedButtonSmall extends JButton {
        private boolean isHovered = false;

        public RoundedButtonSmall(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(FONT_LABEL);
            setForeground(COLOR_WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                @Override
                public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isHovered ? COLOR_TEXT : COLOR_PRIMARY);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
            super.paintComponent(g);
        }
    }

    // Panel rounded dengan border tipis (buat container tabel biar modern)
    public static class RoundedPanelWithBorder extends JPanel {
        public RoundedPanelWithBorder() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(COLOR_WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

            g2.setColor(COLOR_ACCENT);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);

            super.paintComponent(g);
        }
    }

    public static class RoundedButtonOutline extends JButton {
        private boolean isHovered = false;

        public RoundedButtonOutline(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(FONT_BUTTON);
            setForeground(COLOR_PRIMARY); // Teks Hijau
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                @Override
                public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Background
            g2.setColor(isHovered ? new Color(240, 240, 240) : COLOR_WHITE);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);

            // Border/Garis Pinggir
            g2.setColor(COLOR_PRIMARY);
            g2.setStroke(new BasicStroke(2)); // Ketebalan garis 2px
            g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 30, 30);

            super.paintComponent(g);
        }
    }

}