package com.kasir.model;

/**
 * PEWARISAN (Inheritance) & POLIMORFISME (Polymorphism)
 * Subclass dari Produk khusus untuk produk Makanan & Minuman.
 * Mewarisi atribut dan method dari class Produk, serta menambahkan atribut tanggalKadaluarsa.
 */
public class ProdukMakanan extends Produk {

    // Atribut tambahan spesifik ProdukMakanan (Enkapsulasi)
    private String tanggalKadaluarsa;

    public ProdukMakanan(String kode, String nama, double harga, int stok, String tanggalKadaluarsa) {
        super(kode, nama, harga, stok); // Memanggil konstruktor superclass
        this.tanggalKadaluarsa = (tanggalKadaluarsa != null && !tanggalKadaluarsa.trim().isEmpty()) 
                ? tanggalKadaluarsa.trim() 
                : "-";
    }

    public String getTanggalKadaluarsa() {
        return tanggalKadaluarsa;
    }

    public void setTanggalKadaluarsa(String tanggalKadaluarsa) {
        this.tanggalKadaluarsa = tanggalKadaluarsa;
    }

    // ================= POLIMORFISME: METHOD OVERRIDING =================

    /**
     * Logika diskon polimorfis untuk Makanan:
     * Pembelian grosir (kuantiti >= 5 item) mendapat potongan 10% dari subtotal barang tersebut.
     */
    @Override
    public double hitungDiskon(int kuantiti) {
        if (kuantiti >= 5) {
            double totalHarga = getHarga() * kuantiti;
            return totalHarga * 0.10; // Diskon 10%
        }
        return 0.0;
    }

    @Override
    public String getKategori() {
        return "Makanan/Minuman";
    }

    @Override
    public String getInfoKhusus() {
        return "Exp: " + tanggalKadaluarsa;
    }

    @Override
    public void setInfoKhusus(String info) {
        this.tanggalKadaluarsa = info;
    }
}
