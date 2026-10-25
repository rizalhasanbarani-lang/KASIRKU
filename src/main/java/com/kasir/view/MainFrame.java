package com.kasir.view;

import com.kasir.service.ProdukService;
import com.kasir.service.TransaksiService;

import javax.swing.*;
import java.awt.*;

/**
 * Window Utama Aplikasi Kasir (Point of Sales).
 * Mengintegrasikan Tab POS, Tab CRUD Produk, dan Tab Riwayat Transaksi.
 */
public class MainFrame extends JFrame {

    private final ProdukService produkService;
    private final TransaksiService transaksiService;

    private JTabbedPane tabbedPane;
    private PosPanel posPanel;
    private ProdukPanel produkPanel;
    private RiwayatPanel riwayatPanel;

    public MainFrame() {
        super("Kasir Kedai Pos Kopi");
        this.produkService = new ProdukService();
        this.transaksiService = new TransaksiService();

        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 720);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);

        // Header Aplikasi
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(15, 23, 42)); // Slate 900
        headerPanel.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel lblTitle = new JLabel("KASIR KEDAI POS KOPI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Badge Status Kasir
        JPanel statusBadge = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        statusBadge.setOpaque(false);
        JLabel lblUser = new JLabel("Kasir: Standar (Admin)");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(new Color(226, 232, 240));
        statusBadge.add(lblUser);
        headerPanel.add(statusBadge, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Inisialisasi Panel Tab
        Runnable refreshCallback = this::sinkronisasiSemuaData;

        posPanel = new PosPanel(produkService, transaksiService, this, refreshCallback);
        produkPanel = new ProdukPanel(produkService, refreshCallback);
        riwayatPanel = new RiwayatPanel(transaksiService, this);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.addTab("  Point of Sales (Kasir)  ", posPanel);
        tabbedPane.addTab("  Manajemen Produk (CRUD)  ", produkPanel);
        tabbedPane.addTab("  Riwayat Penjualan  ", riwayatPanel);

        // Event pergantian tab untuk memastikan data selalu sinkron
        tabbedPane.addChangeListener(e -> sinkronisasiSemuaData());

        add(tabbedPane, BorderLayout.CENTER);

        // Footer / Status Bar
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(241, 245, 249)); // Slate 100
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));

        JLabel lblFooter = new JLabel("Ready | Platform: Apache NetBeans / Java Swing");
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFooter.setForeground(new Color(100, 116, 139));
        footerPanel.add(lblFooter, BorderLayout.WEST);

        add(footerPanel, BorderLayout.SOUTH);
    }

    /**
     * Memastikan ketika ada perubahan pada data stok / CRUD produk,
     * seluruh tab (POS, CRUD, dan Riwayat) ter-update secara serentak.
     */
    private void sinkronisasiSemuaData() {
        if (posPanel != null) posPanel.muatKatalogProduk();
        if (produkPanel != null) produkPanel.muatDataTabel();
        if (riwayatPanel != null) riwayatPanel.muatDataRiwayat();
    }
}
