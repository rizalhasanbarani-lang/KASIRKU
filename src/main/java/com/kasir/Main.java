package com.kasir;

import com.kasir.view.MainFrame;

import javax.swing.*;

/**
 * Titik masuk utama (Entry Point) aplikasi kasir POS.
 */
public class Main {

    public static void main(String[] args) {
        // Terapkan Look and Feel sistem operasi agar antarmuka terlihat modern dan rapi
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Jika gagal, gunakan default Swing Look & Feel
        }

        // Jalankan UI di thread event dispatch Swing
        SwingUtilities.invokeLater(() -> {
            MainFrame app = new MainFrame();
            app.setVisible(true);
        });
    }
}
