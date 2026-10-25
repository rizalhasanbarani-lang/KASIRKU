package com.kasir.view;

import com.kasir.model.Transaksi;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog modal untuk menampilkan preview cetak struk kasir
 * dengan tipografi monospaced yang presisi dan rapi.
 */
public class StrukDialog extends JDialog {

    public StrukDialog(Frame parent, Transaksi transaksi) {
        super(parent, "Struk Pembayaran - " + transaksi.getNoNota(), true);
        initComponents(transaksi);
    }

    private void initComponents(Transaksi transaksi) {
        setLayout(new BorderLayout(10, 10));
        setSize(420, 560);
        setLocationRelativeTo(getParent());
        setResizable(false);

        // Header dialog
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 41, 59)); // Slate 800
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel lblTitle = new JLabel("Transaksi Berhasil!");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Nota: " + transaksi.getNoNota() + " | " + transaksi.getWaktuFormatted());
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(203, 213, 225)); // Slate 300

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Area teks struk (Monospaced agar tabel karakter sejajar)
        JTextArea txtStruk = new JTextArea();
        txtStruk.setEditable(false);
        txtStruk.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtStruk.setBackground(new Color(255, 255, 255));
        txtStruk.setForeground(new Color(15, 23, 42)); // Slate 900
        txtStruk.setText(transaksi.buatTeksStruk());
        txtStruk.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JScrollPane scrollPane = new JScrollPane(txtStruk);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        add(scrollPane, BorderLayout.CENTER);

        // Panel Tombol Bawah
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomPanel.setBackground(new Color(248, 250, 252));
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        AppButton btnTutup = AppButton.primary("Tutup / Transaksi Baru");
        btnTutup.addActionListener(e -> dispose());

        bottomPanel.add(btnTutup);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
