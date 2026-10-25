package com.kasir.view;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Komponen Tombol Kustom dengan Kontras Tinggi (High-Contrast Button).
 * Menggunakan BasicButtonUI agar warna latar belakang (background) dan warna teks (foreground)
 * dijamin 100% tampil konsisten di semua sistem operasi dan Look & Feel (Windows, macOS, Linux),
 * sehingga teks tombol tidak pernah hilang / memudar (menghindari bug Windows L&F default).
 */
public class AppButton extends JButton {

    private final Color bgNormal;
    private final Color bgHover;
    private final Color bgPressed;
    private final Color fgColor;
    private final Color borderColor;

    public AppButton(String text, Color bgNormal, Color fgColor, Color borderColor) {
        super(text);
        this.bgNormal = bgNormal;
        this.fgColor = fgColor;
        this.borderColor = borderColor;
        this.bgHover = calculateHoverColor(bgNormal);
        this.bgPressed = calculatePressedColor(bgNormal);

        // KUNCI: BasicButtonUI menjamin Swing merender background secara manual
        // dan tidak ditimpa oleh Windows Theme engine (uxtheme).
        setUI(new BasicButtonUI());

        setOpaque(true);
        setContentAreaFilled(true);
        setFocusPainted(false);
        setBackground(bgNormal);
        setForeground(fgColor);
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Border solid untuk ketegasan visual
        if (borderColor != null) {
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor, 1, true),
                    BorderFactory.createEmptyBorder(8, 16, 8, 16)
            ));
        } else {
            setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        }

        // Efek Hover & Pressed interaktif
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(bgHover);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(bgNormal);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(bgPressed);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(bgHover);
                }
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            setBackground(bgNormal);
            setForeground(fgColor);
            if (borderColor != null) {
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(borderColor, 1, true),
                        BorderFactory.createEmptyBorder(8, 16, 8, 16)
                ));
            }
        } else {
            // Tampilan saat tombol dinonaktifkan (tetap jelas terbaca, tidak hilang)
            setBackground(new Color(226, 232, 240)); // Slate 200
            setForeground(new Color(148, 163, 184)); // Slate 400
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                    BorderFactory.createEmptyBorder(8, 16, 8, 16)
            ));
        }
    }

    private static Color calculateHoverColor(Color c) {
        int r = Math.max(0, c.getRed() - 22);
        int g = Math.max(0, c.getGreen() - 22);
        int b = Math.max(0, c.getBlue() - 22);
        return new Color(r, g, b);
    }

    private static Color calculatePressedColor(Color c) {
        int r = Math.max(0, c.getRed() - 40);
        int g = Math.max(0, c.getGreen() - 40);
        int b = Math.max(0, c.getBlue() - 40);
        return new Color(r, g, b);
    }

    // ================= FACTORY METHODS (PRESET WARNA) =================

    /**
     * Tombol Utama (Primary): Biru Tegas dengan Teks Putih Kontras Tinggi.
     */
    public static AppButton primary(String text) {
        return new AppButton(text, new Color(29, 78, 216), Color.WHITE, new Color(30, 64, 175)); // Blue 700 / Blue 800
    }

    /**
     * Tombol Sukses/Bayar (Success): Hijau Emerald dengan Teks Putih Kontras Tinggi.
     */
    public static AppButton success(String text) {
        return new AppButton(text, new Color(4, 120, 87), Color.WHITE, new Color(6, 95, 70)); // Emerald 700 / Emerald 800
    }

    /**
     * Tombol Update (Teal/Cyan): Warna Cyan-Teal Gelap dengan Teks Putih.
     */
    public static AppButton update(String text) {
        return new AppButton(text, new Color(15, 118, 110), Color.WHITE, new Color(17, 94, 89)); // Teal 700 / Teal 800
    }

    /**
     * Tombol Bahaya/Hapus (Danger): Merah Crimson dengan Teks Putih.
     */
    public static AppButton danger(String text) {
        return new AppButton(text, new Color(185, 28, 28), Color.WHITE, new Color(153, 27, 27)); // Red 700 / Red 800
    }

    /**
     * Tombol Sekunder/Netral (Secondary/Outline): Abu-abu Bersih dengan Teks Hitam Pekat & Border Jelas.
     */
    public static AppButton secondary(String text) {
        return new AppButton(text, new Color(241, 245, 249), new Color(15, 23, 42), new Color(148, 163, 184)); // Slate 100, Slate 900, Slate 400
    }
}
