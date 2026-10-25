package com.kasir.view;

import com.kasir.model.ItemTransaksi;
import com.kasir.model.Produk;
import com.kasir.model.Transaksi;
import com.kasir.service.ProdukService;
import com.kasir.service.TransaksiService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Panel Point of Sales (POS) / Antarmuka Kasir.
 * Menangani pemilihan barang, kuantiti, keranjang belanja,
 * kalkulasi diskon polimorfis, nominal pembayaran, kembalian, dan cetak nota.
 */
public class PosPanel extends JPanel {

    private final ProdukService produkService;
    private final TransaksiService transaksiService;
    private final Frame parentFrame;
    private final Runnable dataChangeListener;

    // Komponen Katalog Produk (Kiri)
    private JTable tabelKatalog;
    private DefaultTableModel modelKatalog;
    private JTextField txtCariProduk;
    private JSpinner spnKuantiti;
    private AppButton btnTambahKeranjang;

    // Komponen Keranjang Belanja (Kanan)
    private JTable tabelKeranjang;
    private DefaultTableModel modelKeranjang;
    private AppButton btnHapusItem;

    // Komponen Ringkasan Pembayaran
    private JLabel lblNoNota;
    private JLabel lblTotalBelanja;
    private JLabel lblTotalDiskon;
    private JLabel lblGrandTotal;
    private JTextField txtBayar;
    private JLabel lblKembalian;
    private AppButton btnBayar;
    private AppButton btnBatal;

    private final NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));

    public PosPanel(ProdukService produkService, TransaksiService transaksiService, Frame parentFrame, Runnable dataChangeListener) {
        this.produkService = produkService;
        this.transaksiService = transaksiService;
        this.parentFrame = parentFrame;
        this.dataChangeListener = dataChangeListener;

        initComponents();
        muatKatalogProduk();
        perbaruiTampilanKeranjang();
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(new Color(248, 250, 252)); // Slate 50
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ================= HEADER KASIR =================
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(new Color(255, 255, 255));
        panelHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        lblNoNota = new JLabel("Transaksi Aktif: " + transaksiService.getTransaksiAktif().getNoNota());
        lblNoNota.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNoNota.setForeground(new Color(15, 23, 42));

        JLabel lblPetunjuk = new JLabel("Pilih barang dari katalog, masukkan jumlah, lalu tekan Tambah");
        lblPetunjuk.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblPetunjuk.setForeground(new Color(100, 116, 139));

        panelHeader.add(lblNoNota, BorderLayout.WEST);
        panelHeader.add(lblPetunjuk, BorderLayout.EAST);
        add(panelHeader, BorderLayout.NORTH);

        // ================= SPLIT BODY: KATALOG & KERANJANG =================
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.48);
        splitPane.setBorder(null);

        // 1. Panel Katalog Produk (Kiri)
        JPanel panelKatalog = new JPanel(new BorderLayout(8, 8));
        panelKatalog.setBackground(Color.WHITE);
        panelKatalog.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JPanel panelCari = new JPanel(new BorderLayout(6, 6));
        panelCari.setOpaque(false);
        JLabel lblCari = new JLabel("Cari Produk:");
        lblCari.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtCariProduk = new JTextField();
        txtCariProduk.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtCariProduk.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterKatalog(); }
            public void removeUpdate(DocumentEvent e) { filterKatalog(); }
            public void changedUpdate(DocumentEvent e) { filterKatalog(); }
        });
        panelCari.add(lblCari, BorderLayout.WEST);
        panelCari.add(txtCariProduk, BorderLayout.CENTER);
        panelKatalog.add(panelCari, BorderLayout.NORTH);

        // Tabel Katalog
        String[] colKatalog = {"Kode", "Kategori", "Nama Produk", "Harga", "Stok"};
        modelKatalog = new DefaultTableModel(colKatalog, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelKatalog = new JTable(modelKatalog);
        tabelKatalog.setRowHeight(26);
        tabelKatalog.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelKatalog.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelKatalog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tabelKatalog.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tabelKatalog.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        JScrollPane scrollKatalog = new JScrollPane(tabelKatalog);
        scrollKatalog.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panelKatalog.add(scrollKatalog, BorderLayout.CENTER);

        // Kontrol Penambahan ke Keranjang
        JPanel panelAksiKatalog = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        panelAksiKatalog.setOpaque(false);

        JLabel lblQty = new JLabel("Jumlah:");
        lblQty.setFont(new Font("Segoe UI", Font.BOLD, 12));
        spnKuantiti = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spnKuantiti.setPreferredSize(new Dimension(70, 34));
        spnKuantiti.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btnTambahKeranjang = AppButton.primary("+ Masukkan ke Keranjang");
        btnTambahKeranjang.setPreferredSize(new Dimension(210, 36));
        btnTambahKeranjang.addActionListener(e -> tambahBarangKeKeranjang());

        panelAksiKatalog.add(lblQty);
        panelAksiKatalog.add(spnKuantiti);
        panelAksiKatalog.add(btnTambahKeranjang);
        panelKatalog.add(panelAksiKatalog, BorderLayout.SOUTH);

        splitPane.setLeftComponent(panelKatalog);

        // 2. Panel Keranjang & Pembayaran (Kanan)
        JPanel panelKeranjang = new JPanel(new BorderLayout(8, 8));
        panelKeranjang.setBackground(Color.WHITE);
        panelKeranjang.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        // Header Keranjang
        JPanel panelHeaderKeranjang = new JPanel(new BorderLayout());
        panelHeaderKeranjang.setOpaque(false);
        JLabel lblTitleKeranjang = new JLabel("Keranjang Belanja");
        lblTitleKeranjang.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitleKeranjang.setForeground(new Color(30, 41, 59));

        btnHapusItem = AppButton.danger("✕ Hapus Item");
        btnHapusItem.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnHapusItem.addActionListener(e -> hapusItemTerpilih());

        panelHeaderKeranjang.add(lblTitleKeranjang, BorderLayout.WEST);
        panelHeaderKeranjang.add(btnHapusItem, BorderLayout.EAST);
        panelKeranjang.add(panelHeaderKeranjang, BorderLayout.NORTH);

        // Tabel Keranjang
        String[] colKeranjang = {"Kode", "Nama Produk", "Harga", "Qty", "Diskon", "Subtotal"};
        modelKeranjang = new DefaultTableModel(colKeranjang, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelKeranjang = new JTable(modelKeranjang);
        tabelKeranjang.setRowHeight(26);
        tabelKeranjang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelKeranjang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        tabelKeranjang.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        tabelKeranjang.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tabelKeranjang.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tabelKeranjang.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

        JScrollPane scrollKeranjang = new JScrollPane(tabelKeranjang);
        scrollKeranjang.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panelKeranjang.add(scrollKeranjang, BorderLayout.CENTER);

        // Panel Pembayaran & Checkout (Bawah Kanan)
        JPanel panelCheckout = new JPanel(new BorderLayout(8, 8));
        panelCheckout.setOpaque(false);
        panelCheckout.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(8, 0, 0, 0)
        ));

        // Kalkulasi Rincian
        JPanel panelRincian = new JPanel(new GridLayout(5, 2, 8, 6));
        panelRincian.setOpaque(false);

        JLabel lbl1 = new JLabel("Subtotal:");
        lbl1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTotalBelanja = new JLabel("Rp 0", SwingConstants.RIGHT);
        lblTotalBelanja.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel lbl2 = new JLabel("Diskon (Polimorfis):");
        lbl2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTotalDiskon = new JLabel("Rp 0", SwingConstants.RIGHT);
        lblTotalDiskon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalDiskon.setForeground(new Color(13, 148, 136)); // Teal

        JLabel lbl3 = new JLabel("Grand Total:");
        lbl3.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl3.setForeground(new Color(15, 23, 42));
        lblGrandTotal = new JLabel("Rp 0", SwingConstants.RIGHT);
        lblGrandTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblGrandTotal.setForeground(new Color(37, 99, 235)); // Blue 600

        JLabel lbl4 = new JLabel("Nominal Bayar (Rp):");
        lbl4.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtBayar = new JTextField();
        txtBayar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtBayar.setHorizontalAlignment(SwingConstants.RIGHT);
        txtBayar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { hitungKembalian(); }
            public void removeUpdate(DocumentEvent e) { hitungKembalian(); }
            public void changedUpdate(DocumentEvent e) { hitungKembalian(); }
        });

        JLabel lbl5 = new JLabel("Kembalian:");
        lbl5.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKembalian = new JLabel("Rp 0", SwingConstants.RIGHT);
        lblKembalian.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblKembalian.setForeground(new Color(5, 150, 105)); // Emerald

        panelRincian.add(lbl1);
        panelRincian.add(lblTotalBelanja);
        panelRincian.add(lbl2);
        panelRincian.add(lblTotalDiskon);
        panelRincian.add(lbl3);
        panelRincian.add(lblGrandTotal);
        panelRincian.add(lbl4);
        panelRincian.add(txtBayar);
        panelRincian.add(lbl5);
        panelRincian.add(lblKembalian);

        panelCheckout.add(panelRincian, BorderLayout.CENTER);

        // Tombol Checkout & Batal
        JPanel panelTombolCheckout = new JPanel(new GridLayout(1, 2, 10, 0));
        panelTombolCheckout.setOpaque(false);
        panelTombolCheckout.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        btnBatal = AppButton.secondary("↺ Reset Transaksi");
        btnBatal.setPreferredSize(new Dimension(0, 44));
        btnBatal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBatal.addActionListener(e -> batalkanTransaksi());

        btnBayar = AppButton.success("✓ Bayar & Cetak Struk");
        btnBayar.setPreferredSize(new Dimension(0, 44));
        btnBayar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBayar.addActionListener(e -> prosesPembayaran());

        panelTombolCheckout.add(btnBatal);
        panelTombolCheckout.add(btnBayar);
        panelCheckout.add(panelTombolCheckout, BorderLayout.SOUTH);

        panelKeranjang.add(panelCheckout, BorderLayout.SOUTH);

        splitPane.setRightComponent(panelKeranjang);
        add(splitPane, BorderLayout.CENTER);
    }

    public void muatKatalogProduk() {
        modelKatalog.setRowCount(0);
        List<Produk> list = produkService.getSemuaProduk();
        for (Produk p : list) {
            modelKatalog.addRow(new Object[]{
                    p.getKode(),
                    p.getKategori(),
                    p.getNama(),
                    formatRupiah.format(p.getHarga()),
                    p.getStok()
            });
        }
    }

    private void filterKatalog() {
        String keyword = txtCariProduk.getText().trim();
        modelKatalog.setRowCount(0);
        List<Produk> list = produkService.cariProduk(keyword, "Semua");
        for (Produk p : list) {
            modelKatalog.addRow(new Object[]{
                    p.getKode(),
                    p.getKategori(),
                    p.getNama(),
                    formatRupiah.format(p.getHarga()),
                    p.getStok()
            });
        }
    }

    private void tambahBarangKeKeranjang() {
        int row = tabelKatalog.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Silakan pilih salah satu produk dari tabel di kiri!", "Pilih Produk", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String kode = (String) modelKatalog.getValueAt(row, 0);
        Produk p = produkService.cariProduk(kode);
        if (p == null) return;

        int qty = (int) spnKuantiti.getValue();

        try {
            transaksiService.tambahKeKeranjang(p, qty);
            perbaruiTampilanKeranjang();
            spnKuantiti.setValue(1); // Reset spinner ke 1
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Gagal Menambahkan", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void hapusItemTerpilih() {
        int row = tabelKeranjang.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Silakan pilih baris item di keranjang yang ingin dihapus.", "Pilih Item", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        transaksiService.hapusItemDariKeranjang(row);
        perbaruiTampilanKeranjang();
    }

    private void batalkanTransaksi() {
        int konfirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin mengosongkan keranjang belanja?", "Reset Keranjang", JOptionPane.YES_NO_OPTION);
        if (konfirm == JOptionPane.YES_OPTION) {
            transaksiService.batalkanTransaksi();
            txtBayar.setText("");
            perbaruiTampilanKeranjang();
        }
    }

    private void perbaruiTampilanKeranjang() {
        Transaksi trx = transaksiService.getTransaksiAktif();
        lblNoNota.setText("Transaksi Aktif: " + trx.getNoNota());

        modelKeranjang.setRowCount(0);
        for (ItemTransaksi item : trx.getKeranjang()) {
            Produk p = item.getProduk();
            modelKeranjang.addRow(new Object[]{
                    p.getKode(),
                    p.getNama(),
                    formatRupiah.format(item.getHargaSatuan()),
                    item.getJumlah(),
                    formatRupiah.format(item.getDiskon()), // POLIMORFISME
                    formatRupiah.format(item.getSubtotalAkhir())
            });
        }

        lblTotalBelanja.setText(formatRupiah.format(trx.hitungTotalSebelumDiskon()));
        lblTotalDiskon.setText(formatRupiah.format(trx.hitungTotalDiskon()));
        lblGrandTotal.setText(formatRupiah.format(trx.hitungGrandTotal()));

        hitungKembalian();
    }

    private void hitungKembalian() {
        try {
            String bayarStr = txtBayar.getText().trim().replaceAll("[^0-9]", "");
            if (bayarStr.isEmpty()) {
                lblKembalian.setText("Rp 0");
                return;
            }
            double bayar = Double.parseDouble(bayarStr);
            double grandTotal = transaksiService.getTransaksiAktif().hitungGrandTotal();
            double kembalian = bayar - grandTotal;

            if (kembalian >= 0) {
                lblKembalian.setText(formatRupiah.format(kembalian));
                lblKembalian.setForeground(new Color(5, 150, 105)); // Emerald hijau
            } else {
                lblKembalian.setText("-" + formatRupiah.format(Math.abs(kembalian)));
                lblKembalian.setForeground(new Color(220, 38, 38)); // Merah jika kurang
            }
        } catch (Exception ignored) {
            lblKembalian.setText("Rp 0");
        }
    }

    private void prosesPembayaran() {
        Transaksi trx = transaksiService.getTransaksiAktif();
        if (trx.getKeranjang().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keranjang belanja masih kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String bayarStr = txtBayar.getText().trim().replaceAll("[^0-9]", "");
            if (bayarStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Silakan masukkan nominal uang pembayaran!", "Validasi Pembayaran", JOptionPane.WARNING_MESSAGE);
                txtBayar.requestFocus();
                return;
            }

            double bayar = Double.parseDouble(bayarStr);
            double grandTotal = trx.hitungGrandTotal();

            if (bayar < grandTotal) {
                JOptionPane.showMessageDialog(this, "Nominal uang pembayaran kurang dari Grand Total!", "Uang Kurang", JOptionPane.ERROR_MESSAGE);
                txtBayar.requestFocus();
                return;
            }

            // Eksekusi checkout transaksi (potong stok, rekam riwayat)
            Transaksi transaksiSelesai = transaksiService.selesaikanTransaksi(bayar);

            // Buka Dialog Struk
            StrukDialog dialog = new StrukDialog(parentFrame, transaksiSelesai);
            dialog.setVisible(true);

            // Reset UI
            txtBayar.setText("");
            muatKatalogProduk();
            perbaruiTampilanKeranjang();

            // Beritahu observer agar tab CRUD juga mengupdate data stok terbaru
            if (dataChangeListener != null) {
                dataChangeListener.run();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Kesalahan Transaksi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
