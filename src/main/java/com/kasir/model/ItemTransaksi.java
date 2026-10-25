package com.kasir.model;

/**
 * Merepresentasikan satu baris item belanjaan di keranjang kasir.
 * Memanfaatkan POLIMORFISME: Atribut produk bertipe referensi abstrak 'Produk',
 * sehingga method hitungDiskon() dipanggil secara dinamis (runtime polymorphism)
 * sesuai kelas riil dari objek (ProdukMakanan atau ProdukElektronik).
 */
public class ItemTransaksi {

    // Enkapsulasi: private fields
    private final Produk produk;
    private int jumlah;

    public ItemTransaksi(Produk produk, int jumlah) {
        if (produk == null) {
            throw new IllegalArgumentException("Produk tidak boleh null.");
        }
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah beli minimal 1.");
        }
        this.produk = produk;
        this.jumlah = jumlah;
    }

    public Produk getProduk() {
        return produk;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah beli minimal 1.");
        }
        this.jumlah = jumlah;
    }

    public void tambahJumlah(int tambahan) {
        if (tambahan <= 0) {
            throw new IllegalArgumentException("Jumlah tambahan harus > 0.");
        }
        this.jumlah += tambahan;
    }

    public double getHargaSatuan() {
        return produk.getHarga();
    }

    /**
     * CONTOH POLIMORFISME RUNTIME:
     * Method hitungDiskon() yang dipanggil bergantung pada objek riil
     * (apakah ProdukMakanan atau ProdukElektronik), tanpa perlu percabangan manual if/else instanceof.
     */
    public double getDiskon() {
        return produk.hitungDiskon(this.jumlah);
    }

    public double getSubtotalSebelumDiskon() {
        return produk.getHarga() * this.jumlah;
    }

    public double getSubtotalAkhir() {
        return getSubtotalSebelumDiskon() - getDiskon();
    }
}
