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
    public static class RoundedTextField extends JTextField {
        public RoundedTextField(int columns) {
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

}