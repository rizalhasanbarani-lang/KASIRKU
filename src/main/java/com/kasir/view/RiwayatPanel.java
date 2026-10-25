package com.kasir.view;

import com.kasir.model.Transaksi;
import com.kasir.service.TransaksiService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Panel Riwayat Transaksi:
 * Menampilkan seluruh daftar transaksi penjualan yang telah berhasil diselesaikan.
 */
public class RiwayatPanel extends JPanel {

    private final TransaksiService transaksiService;
    private final Frame parentFrame;
    private JTable tabelRiwayat;
    private DefaultTableModel modelRiwayat;
    private final NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));

    public RiwayatPanel(TransaksiService transaksiService, Frame parentFrame) {
        this.transaksiService = transaksiService;
        this.parentFrame = parentFrame;
        initComponents();
        muatDataRiwayat();
    }

    private void initComponents() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Header Panel
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Riwayat Transaksi Penjualan");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(30, 41, 59));

        AppButton btnLihatStruk = AppButton.primary("Lihat Ulang Struk Nota");
        btnLihatStruk.addActionListener(e -> bukaStrukTerpilih());

        panelHeader.add(lblTitle, BorderLayout.WEST);
        panelHeader.add(btnLihatStruk, BorderLayout.EAST);
        add(panelHeader, BorderLayout.NORTH);

        // Tabel Riwayat
        String[] kolom = {"No. Nota", "Waktu Transaksi", "Jumlah Item", "Total Belanja", "Diskon", "Grand Total"};
        modelRiwayat = new DefaultTableModel(kolom, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabelRiwayat = new JTable(modelRiwayat);
        tabelRiwayat.setRowHeight(28);
        tabelRiwayat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelRiwayat.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelRiwayat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tabelRiwayat.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        tabelRiwayat.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tabelRiwayat.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tabelRiwayat.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(tabelRiwayat);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void muatDataRiwayat() {
        modelRiwayat.setRowCount(0);
        List<Transaksi> list = transaksiService.getRiwayatTransaksi();
        for (Transaksi t : list) {
            modelRiwayat.addRow(new Object[]{
                    t.getNoNota(),
                    t.getWaktuFormatted(),
                    t.getKeranjang().size() + " Item",
                    formatRupiah.format(t.hitungTotalSebelumDiskon()),
                    formatRupiah.format(t.hitungTotalDiskon()),
                    formatRupiah.format(t.hitungGrandTotal())
            });
        }
    }

    private void bukaStrukTerpilih() {
        int row = tabelRiwayat.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Silakan pilih salah satu baris transaksi dari tabel.", "Pilih Transaksi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<Transaksi> list = transaksiService.getRiwayatTransaksi();
        if (row < list.size()) {
            Transaksi t = list.get(row);
            StrukDialog dialog = new StrukDialog(parentFrame, t);
            dialog.setVisible(true);
        }
    }
}
