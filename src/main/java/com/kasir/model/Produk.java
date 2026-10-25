package com.kasir.model;

/**
 * ABSTRAKSI (Abstraction) & ENKAPSULASI (Encapsulation)
 * Kelas abstrak dasar yang mendefinisikan atribut umum dan perilaku produk.
 * Memiliki abstract method yang wajib diimplementasikan oleh kelas turunannya.
 */
public abstract class Produk {

    // ATRIBUT (Encapsulation: modifier private)
    private String kode;
    private String nama;
    private double harga;
    private int stok;

    // Konstruktor
    public Produk(String kode, String nama, double harga, int stok) {
        if (kode == null || kode.trim().isEmpty()) {
            throw new IllegalArgumentException("Kode produk tidak boleh kosong.");
        }
        if (nama == null || nama.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama produk tidak boleh kosong.");
        }
        if (harga < 0) {
            throw new IllegalArgumentException("Harga tidak boleh negatif.");
        }
        if (stok < 0) {
            throw new IllegalArgumentException("Stok awal tidak boleh negatif.");
        }

        this.kode = kode.trim().toUpperCase();
        this.nama = nama.trim();
        this.harga = harga;
        this.stok = stok;
    }

    // ================= ENKAPSULASI: GETTER & SETTER =================

    public String getKode() {
        return kode;
    }

    public void setKode(String kode) {
        if (kode == null || kode.trim().isEmpty()) {
            throw new IllegalArgumentException("Kode produk tidak boleh kosong.");
        }
        this.kode = kode.trim().toUpperCase();
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        if (nama == null || nama.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama produk tidak boleh kosong.");
        }
        this.nama = nama.trim();
    }

    public double getHarga() {
        return harga;
    }

    public void setHarga(double harga) {
        if (harga < 0) {
            throw new IllegalArgumentException("Harga tidak boleh negatif.");
        }
        this.harga = harga;
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        if (stok < 0) {
            throw new IllegalArgumentException("Stok tidak boleh negatif.");
        }
        this.stok = stok;
    }

    // Metode mutator dengan logika bisnis
    public void kurangiStok(int jumlah) {
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah pengurangan stok harus lebih dari 0.");
        }
        if (this.stok < jumlah) {
            throw new IllegalStateException("Stok tidak mencukupi! Stok saat ini: " + this.stok);
        }
        this.stok -= jumlah;
    }

    public void tambahStok(int jumlah) {
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah penambahan stok harus lebih dari 0.");
        }
        this.stok += jumlah;
    }

    // ================= ABSTRAKSI: ABSTRACT METHODS =================
    
    /**
     * Menghitung besaran diskon berdasarkan jenis produk dan kuantiti pembelian.
     * Akan diimplementasikan secara spesifik (Polimorfisme) oleh masing-masing subclass.
     */
    public abstract double hitungDiskon(int kuantiti);

    /**
     * Mengembalikan nama kategori produk (misal: "Makanan", "Elektronik").
     */
    public abstract String getKategori();

    /**
     * Mengembalikan informasi spesifik subclass (misal: tanggal kadaluarsa atau masa garansi).
     */
    public abstract String getInfoKhusus();

    /**
     * Memperbarui informasi spesifik subclass.
     */
    public abstract void setInfoKhusus(String info);

    @Override
    public String toString() {
        return "[" + kode + "] " + nama + " (Rp " + String.format("%,.0f", harga) + ") - Stok: " + stok;
    }
}
