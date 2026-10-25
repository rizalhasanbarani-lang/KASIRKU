package com.kasir.service;

import com.kasir.model.Produk;
import com.kasir.model.ProdukElektronik;
import com.kasir.model.ProdukMakanan;

import java.util.ArrayList;
import java.util.List;

/**
 * Service untuk mengelola data master produk (CRUD).
 * Menyediakan data sampel awal agar aplikasi langsung siap digunakan saat dijalankan.
 */
public class ProdukService {

    private final List<Produk> daftarProduk;

    public ProdukService() {
        this.daftarProduk = new ArrayList<>();
        inisialisasiDataAwal();
    }

    /**
     * Memuat beberapa data produk realistis untuk kebutuhan demo / tugas.
     */
    private void inisialisasiDataAwal() {
        // Produk Makanan / Minuman Kedai Kopi (dengan info tanggal kadaluarsa)
        daftarProduk.add(new ProdukMakanan("M01", "Kopi Susu Pos Aren", 18000, 25, "2026-11-15"));
        daftarProduk.add(new ProdukMakanan("M02", "Espresso Double Shot", 21000, 30, "2026-11-20"));
        daftarProduk.add(new ProdukMakanan("M03", "Croissant Cokelat Keju", 35000, 15, "2026-11-10"));
        daftarProduk.add(new ProdukMakanan("M04", "Roti Toast Srikaya", 45000, 12, "2026-11-12"));

        // Produk Elektronik & Alat Kopi (dengan info garansi dalam bulan)
        daftarProduk.add(new ProdukElektronik("E01", "Electric Milk Frother", 125000, 10, 12));
        daftarProduk.add(new ProdukElektronik("E02", "Digital Coffee Scale", 450000, 8, 12));
        daftarProduk.add(new ProdukElektronik("E03", "Mesin Espresso Portable", 650000, 6, 24));
        daftarProduk.add(new ProdukElektronik("E04", "Manual Coffee Grinder", 85000, 20, 36));
    }

    // ================= CREATE (C) =================
    public void tambahProduk(Produk produk) {
        if (produk == null) {
            throw new IllegalArgumentException("Data produk tidak boleh kosong.");
        }
        if (cariProduk(produk.getKode()) != null) {
            throw new IllegalArgumentException("Produk dengan kode '" + produk.getKode() + "' sudah terdaftar!");
        }
        daftarProduk.add(produk);
    }

    // ================= READ (R) =================
    public List<Produk> getSemuaProduk() {
        return new ArrayList<>(daftarProduk);
    }

    /**
     * POLIMORFISME STATIS (Method Overloading) #1:
     * Mencari produk berdasarkan kode uniknya.
     */
    public Produk cariProduk(String kode) {
        if (kode == null) return null;
        for (Produk p : daftarProduk) {
            if (p.getKode().equalsIgnoreCase(kode.trim())) {
                return p;
            }
        }
        return null;
    }

    /**
     * POLIMORFISME STATIS (Method Overloading) #2:
     * Mencari produk berdasarkan nama dan kategori.
     */
    public List<Produk> cariProduk(String keywordNama, String kategori) {
        List<Produk> hasil = new ArrayList<>();
        String kw = (keywordNama == null) ? "" : keywordNama.toLowerCase().trim();
        boolean filterKategori = (kategori != null && !kategori.equalsIgnoreCase("Semua"));

        for (Produk p : daftarProduk) {
            boolean cocokNama = p.getNama().toLowerCase().contains(kw) || p.getKode().toLowerCase().contains(kw);
            boolean cocokKategori = !filterKategori || p.getKategori().equalsIgnoreCase(kategori);

            if (cocokNama && cocokKategori) {
                hasil.add(p);
            }
        }
        return hasil;
    }

    // ================= UPDATE (U) =================
    public void updateProduk(String kode, String namaBaru, double hargaBaru, int stokBaru, String infoKhususBaru) {
        Produk p = cariProduk(kode);
        if (p == null) {
            throw new IllegalArgumentException("Produk dengan kode '" + kode + "' tidak ditemukan.");
        }

        p.setNama(namaBaru);
        p.setHarga(hargaBaru);
        p.setStok(stokBaru);
        p.setInfoKhusus(infoKhususBaru);
    }

    // ================= DELETE (D) =================
    public boolean hapusProduk(String kode) {
        Produk p = cariProduk(kode);
        if (p != null) {
            return daftarProduk.remove(p);
        }
        return false;
    }
}
