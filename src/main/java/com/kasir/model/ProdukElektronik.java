package com.kasir.model;

/**
 * PEWARISAN (Inheritance) & POLIMORFISME (Polymorphism)
 * Subclass dari Produk khusus untuk produk Elektronik & Gadget.
 * Mewarisi atribut dan method dari class Produk, serta menambahkan atribut garansiBulan.
 */
public class ProdukElektronik extends Produk {

    // Atribut tambahan spesifik ProdukElektronik (Enkapsulasi)
    private int garansiBulan;

    public ProdukElektronik(String kode, String nama, double harga, int stok, int garansiBulan) {
        super(kode, nama, harga, stok); // Memanggil konstruktor superclass
        this.garansiBulan = Math.max(0, garansiBulan);
    }

    public int getGaransiBulan() {
        return garansiBulan;
    }

    public void setGaransiBulan(int garansiBulan) {
        this.garansiBulan = Math.max(0, garansiBulan);
    }

    // ================= POLIMORFISME: METHOD OVERRIDING =================

    /**
     * Logika diskon polimorfis untuk Elektronik:
     * Jika total belanja barang elektronik ini >= Rp 500.000,
     * pelanggan berhak mendapatkan diskon promo sebesar 5%.
     */
    @Override
    public double hitungDiskon(int kuantiti) {
        double subtotal = getHarga() * kuantiti;
        if (subtotal >= 500_000) {
            return subtotal * 0.05; // Diskon 5%
        }
        return 0.0;
    }

    @Override
    public String getKategori() {
        return "Elektronik/Gadget";
    }

    @Override
    public String getInfoKhusus() {
        return "Garansi: " + garansiBulan + " Bulan";
    }

    @Override
    public void setInfoKhusus(String info) {
        try {
            // Mengambil angka saja jika string berisi teks seperti "12 Bulan"
            String cleaned = info.replaceAll("[^0-9]", "");
            this.garansiBulan = cleaned.isEmpty() ? 0 : Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            this.garansiBulan = 0;
        }
    }
}
