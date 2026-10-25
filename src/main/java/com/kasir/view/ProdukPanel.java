package com.kasir.view;

import com.kasir.model.Produk;
import com.kasir.model.ProdukElektronik;
import com.kasir.model.ProdukMakanan;
import com.kasir.service.ProdukService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Panel Manajemen Produk yang mengimplementasikan operasi CRUD:
 * - Create: Tambah produk makanan atau elektronik
 * - Read: Menampilkan daftar produk dengan filter dan pencarian
 * - Update: Mengubah data produk yang dipilih
 * - Delete: Menghapus produk dari sistem
 */
public class ProdukPanel extends JPanel {

    private final ProdukService produkService;
    private final Runnable dataChangeListener;

    // Komponen Tabel
    private JTable tabelProduk;
    private DefaultTableModel modelTabel;
    private JTextField txtCari;
    private JComboBox<String> cmbFilterKategori;

    // Komponen Form Input (CRUD)
    private JComboBox<String> cmbKategori;
    private JTextField txtKode;
    private JTextField txtNama;
    private JTextField txtHarga;
    private JTextField txtStok;
    private JLabel lblInfoKhusus;
    private JTextField txtInfoKhusus;

    // Tombol CRUD
    private AppButton btnTambah;
    private AppButton btnUpdate;
    private AppButton btnHapus;
    private AppButton btnReset;

    private final NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));

    public ProdukPanel(ProdukService produkService, Runnable dataChangeListener) {
        this.produkService = produkService;
        this.dataChangeListener = dataChangeListener;
        initComponents();
        muatDataTabel();
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(new Color(248, 250, 252)); // Slate 50
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ================= BAGIAN KIRI: TABEL & PENCARIAN (READ) =================
        JPanel panelTabel = new JPanel(new BorderLayout(8, 8));
        panelTabel.setBackground(new Color(248, 250, 252));

        // Panel Filter & Cari
        JPanel panelFilter = new JPanel(new BorderLayout(8, 8));
        panelFilter.setBackground(new Color(255, 255, 255));
        panelFilter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JPanel panelInputCari = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelInputCari.setOpaque(false);

        JLabel lblCari = new JLabel("Cari Barang:");
        lblCari.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtCari = new JTextField(15);
        txtCari.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel lblFilter = new JLabel("Kategori:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cmbFilterKategori = new JComboBox<>(new String[]{"Semua", "Makanan/Minuman", "Elektronik/Gadget"});
        cmbFilterKategori.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        AppButton btnFilterCari = AppButton.primary("Cari");
        btnFilterCari.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFilterCari.addActionListener(e -> terapkanFilterCari());

        AppButton btnResetFilter = AppButton.secondary("Reset");
        btnResetFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnResetFilter.addActionListener(e -> {
            txtCari.setText("");
            cmbFilterKategori.setSelectedIndex(0);
            muatDataTabel();
        });

        panelInputCari.add(lblCari);
        panelInputCari.add(txtCari);
        panelInputCari.add(lblFilter);
        panelInputCari.add(cmbFilterKategori);
        panelInputCari.add(btnFilterCari);
        panelInputCari.add(btnResetFilter);

        panelFilter.add(panelInputCari, BorderLayout.CENTER);
        panelTabel.add(panelFilter, BorderLayout.NORTH);

        // Tabel Data Produk
        String[] kolom = {"Kode", "Kategori", "Nama Produk", "Harga Satuan", "Stok", "Info Khusus"};
        modelTabel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabel read-only secara visual, diedit via form
            }
        };

        tabelProduk = new JTable(modelTabel);
        tabelProduk.setRowHeight(28);
        tabelProduk.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelProduk.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelProduk.getTableHeader().setBackground(new Color(241, 245, 249)); // Slate 100
        tabelProduk.getTableHeader().setForeground(new Color(51, 65, 85));
        tabelProduk.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Rata kanan untuk kolom harga dan stok
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tabelProduk.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tabelProduk.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        // Event klik baris tabel untuk mengisi form (Update / Delete)
        tabelProduk.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                isiFormDariTabel();
            }
        });

        JScrollPane scrollTabel = new JScrollPane(tabelProduk);
        scrollTabel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panelTabel.add(scrollTabel, BorderLayout.CENTER);

        add(panelTabel, BorderLayout.CENTER);

        // ================= BAGIAN KANAN: FORM INPUT & AKSI CRUD =================
        JPanel panelForm = new JPanel(new BorderLayout(10, 10));
        panelForm.setPreferredSize(new Dimension(360, 0));
        panelForm.setBackground(Color.WHITE);
        panelForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel lblFormTitle = new JLabel("Kelola Data Produk");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblFormTitle.setForeground(new Color(30, 41, 59));
        lblFormTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        // Isian Form
        JPanel panelField = new JPanel(new GridLayout(6, 2, 8, 12));
        panelField.setOpaque(false);

        JLabel lblKategori = new JLabel("Kategori:");
        cmbKategori = new JComboBox<>(new String[]{"Makanan/Minuman", "Elektronik/Gadget"});
        cmbKategori.addActionListener(e -> sesuaikanInfoKhusus());

        JLabel lblKode = new JLabel("Kode Produk:");
        txtKode = new JTextField();

        JLabel lblNama = new JLabel("Nama Produk:");
        txtNama = new JTextField();

        JLabel lblHarga = new JLabel("Harga (Rp):");
        txtHarga = new JTextField();

        JLabel lblStok = new JLabel("Stok Awal:");
        txtStok = new JTextField();

        lblInfoKhusus = new JLabel("Tgl Kadaluarsa:");
        txtInfoKhusus = new JTextField("2026-12-31");

        panelField.add(lblKategori);
        panelField.add(cmbKategori);
        panelField.add(lblKode);
        panelField.add(txtKode);
        panelField.add(lblNama);
        panelField.add(txtNama);
        panelField.add(lblHarga);
        panelField.add(txtHarga);
        panelField.add(lblStok);
        panelField.add(txtStok);
        panelField.add(lblInfoKhusus);
        panelField.add(txtInfoKhusus);

        // Panel Tombol Aksi (CRUD)
        JPanel panelTombol = new JPanel(new GridLayout(2, 2, 10, 10));
        panelTombol.setOpaque(false);
        panelTombol.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        btnTambah = AppButton.primary("+ Tambah Baru");
        btnTambah.setPreferredSize(new Dimension(0, 38));
        btnTambah.addActionListener(e -> tambahProduk());

        btnUpdate = AppButton.update("✓ Simpan Update");
        btnUpdate.setPreferredSize(new Dimension(0, 38));
        btnUpdate.setEnabled(false);
        btnUpdate.addActionListener(e -> updateProduk());

        btnHapus = AppButton.danger("✕ Hapus Produk");
        btnHapus.setPreferredSize(new Dimension(0, 38));
        btnHapus.setEnabled(false);
        btnHapus.addActionListener(e -> hapusProduk());

        btnReset = AppButton.secondary("↺ Bersihkan Form");
        btnReset.setPreferredSize(new Dimension(0, 38));
        btnReset.addActionListener(e -> resetForm());

        panelTombol.add(btnTambah);
        panelTombol.add(btnUpdate);
        panelTombol.add(btnHapus);
        panelTombol.add(btnReset);

        // Satukan Title, Fields, dan Tombol di container atas agar selalu tampak utuh
        JPanel containerAtas = new JPanel(new BorderLayout(0, 10));
        containerAtas.setOpaque(false);
        containerAtas.add(lblFormTitle, BorderLayout.NORTH);
        containerAtas.add(panelField, BorderLayout.CENTER);
        containerAtas.add(panelTombol, BorderLayout.SOUTH);

        panelForm.add(containerAtas, BorderLayout.NORTH);

        add(panelForm, BorderLayout.EAST);
    }

    private void sesuaikanInfoKhusus() {
        String kategori = (String) cmbKategori.getSelectedItem();
        if ("Elektronik/Gadget".equals(kategori)) {
            lblInfoKhusus.setText("Garansi (Bulan):");
            if (txtInfoKhusus.getText().contains("-") || txtInfoKhusus.getText().isEmpty()) {
                txtInfoKhusus.setText("12");
            }
        } else {
            lblInfoKhusus.setText("Tgl Kadaluarsa:");
            if (!txtInfoKhusus.getText().contains("-")) {
                txtInfoKhusus.setText("2026-12-31");
            }
        }
    }

    public void muatDataTabel() {
        modelTabel.setRowCount(0);
        List<Produk> list = produkService.getSemuaProduk();
        for (Produk p : list) {
            modelTabel.addRow(new Object[]{
                    p.getKode(),
                    p.getKategori(),
                    p.getNama(),
                    formatRupiah.format(p.getHarga()),
                    p.getStok(),
                    p.getInfoKhusus()
            });
        }
    }

    private void terapkanFilterCari() {
        String keyword = txtCari.getText().trim();
        String kategori = (String) cmbFilterKategori.getSelectedItem();

        modelTabel.setRowCount(0);
        List<Produk> list = produkService.cariProduk(keyword, kategori);
        for (Produk p : list) {
            modelTabel.addRow(new Object[]{
                    p.getKode(),
                    p.getKategori(),
                    p.getNama(),
                    formatRupiah.format(p.getHarga()),
                    p.getStok(),
                    p.getInfoKhusus()
            });
        }
    }

    private void isiFormDariTabel() {
        int selectedRow = tabelProduk.getSelectedRow();
        if (selectedRow >= 0) {
            String kode = (String) modelTabel.getValueAt(selectedRow, 0);
            Produk p = produkService.cariProduk(kode);
            if (p != null) {
                txtKode.setText(p.getKode());
                txtKode.setEditable(false); // Kode kunci primer tidak diubah saat update
                txtNama.setText(p.getNama());
                txtHarga.setText(String.format(Locale.US, "%.0f", p.getHarga()));
                txtStok.setText(String.valueOf(p.getStok()));

                if (p instanceof ProdukElektronik) {
                    cmbKategori.setSelectedItem("Elektronik/Gadget");
                    ProdukElektronik pe = (ProdukElektronik) p;
                    txtInfoKhusus.setText(String.valueOf(pe.getGaransiBulan()));
                } else if (p instanceof ProdukMakanan) {
                    cmbKategori.setSelectedItem("Makanan/Minuman");
                    ProdukMakanan pm = (ProdukMakanan) p;
                    txtInfoKhusus.setText(pm.getTanggalKadaluarsa());
                }
                cmbKategori.setEnabled(false);

                btnTambah.setEnabled(false);
                btnUpdate.setEnabled(true);
                btnHapus.setEnabled(true);
            }
        }
    }

    private void tambahProduk() {
        try {
            String kode = txtKode.getText().trim();
            String nama = txtNama.getText().trim();
            String strHarga = txtHarga.getText().trim();
            String strStok = txtStok.getText().trim();
            String infoKhusus = txtInfoKhusus.getText().trim();
            String kategori = (String) cmbKategori.getSelectedItem();

            if (kode.isEmpty() || nama.isEmpty() || strHarga.isEmpty() || strStok.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Validasi Form", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double harga = Double.parseDouble(strHarga);
            int stok = Integer.parseInt(strStok);

            Produk produkBaru;
            if ("Elektronik/Gadget".equals(kategori)) {
                int garansi = Integer.parseInt(infoKhusus.replaceAll("[^0-9]", ""));
                produkBaru = new ProdukElektronik(kode, nama, harga, stok, garansi);
            } else {
                produkBaru = new ProdukMakanan(kode, nama, harga, stok, infoKhusus);
            }

            produkService.tambahProduk(produkBaru);
            JOptionPane.showMessageDialog(this, "Produk berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            
            resetForm();
            muatDataTabel();
            if (dataChangeListener != null) dataChangeListener.run();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Harga, stok, atau garansi harus berupa angka valid!", "Kesalahan Input", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateProduk() {
        try {
            String kode = txtKode.getText().trim();
            String nama = txtNama.getText().trim();
            double harga = Double.parseDouble(txtHarga.getText().trim());
            int stok = Integer.parseInt(txtStok.getText().trim());
            String infoKhusus = txtInfoKhusus.getText().trim();

            produkService.updateProduk(kode, nama, harga, stok, infoKhusus);
            JOptionPane.showMessageDialog(this, "Data produk berhasil diupdate!", "Sukses", JOptionPane.INFORMATION_MESSAGE);

            resetForm();
            muatDataTabel();
            if (dataChangeListener != null) dataChangeListener.run();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Harga dan stok harus berupa angka!", "Kesalahan Input", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapusProduk() {
        String kode = txtKode.getText().trim();
        if (kode.isEmpty()) return;

        int konfirmasi = JOptionPane.showConfirmDialog(
                this, 
                "Apakah Anda yakin ingin menghapus produk [" + kode + "]?", 
                "Konfirmasi Hapus", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE
        );

        if (konfirmasi == JOptionPane.YES_OPTION) {
            boolean berhasil = produkService.hapusProduk(kode);
            if (berhasil) {
                JOptionPane.showMessageDialog(this, "Produk berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                resetForm();
                muatDataTabel();
                if (dataChangeListener != null) dataChangeListener.run();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus produk.", "Gagal", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void resetForm() {
        txtKode.setText("");
        txtKode.setEditable(true);
        txtNama.setText("");
        txtHarga.setText("");
        txtStok.setText("");
        cmbKategori.setEnabled(true);
        cmbKategori.setSelectedIndex(0);
        sesuaikanInfoKhusus();
        tabelProduk.clearSelection();

        btnTambah.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnHapus.setEnabled(false);
    }
}
