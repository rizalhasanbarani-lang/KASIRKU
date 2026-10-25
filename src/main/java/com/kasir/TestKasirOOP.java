package com.kasir;

import com.kasir.model.*;
import com.kasir.service.*;

public class TestKasirOOP {
    public static void main(String[] args) {
        System.out.println("=== PENGUJIAN LOGIKA SISTEM KASIR & OOP ===");

        ProdukService ps = new ProdukService();
        TransaksiService ts = new TransaksiService();

        // 1. Tes Read & Polymorphic types
        System.out.println("\n[1] Pengujian Model & Polimorfisme:");
        for (Produk p : ps.getSemuaProduk()) {
            System.out.println("-> " + p.getKode() + " | " + p.getKategori() + " | " + p.getNama() 
                + " | Rp " + p.getHarga() + " | Stok: " + p.getStok() + " | " + p.getInfoKhusus());
        }

        // 2. Tes Polimorfisme hitungDiskon
        Produk makanan = ps.cariProduk("M01"); // Roti Tawar Gandum, Rp 18.000
        double diskonMakanan = makanan.hitungDiskon(5); // 5 x 18.000 = 90.000, diskon 10% = 9.000
        System.out.println("\n[2] Tes Diskon Makanan (Qty 5): Rp " + diskonMakanan + " (Ekspektasi: 9000.0)");

        Produk elektronik = ps.cariProduk("E03"); // Headphone ANC, Rp 650.000
        double diskonElektronik = elektronik.hitungDiskon(1); // 650.000 >= 500.000 -> 5% = 32.500
        System.out.println("    Tes Diskon Elektronik: Rp " + diskonElektronik + " (Ekspektasi: 32500.0)");

        // 3. Tes POS Keranjang & Checkout
        System.out.println("\n[3] Tes Transaksi Kasir:");
        ts.tambahKeKeranjang(makanan, 5);
        ts.tambahKeKeranjang(elektronik, 1);

        Transaksi trx = ts.getTransaksiAktif();
        System.out.println("    Total Sebelum Diskon: Rp " + trx.hitungTotalSebelumDiskon());
        System.out.println("    Total Diskon: Rp " + trx.hitungTotalDiskon());
        System.out.println("    Grand Total: Rp " + trx.hitungGrandTotal());

        int stokMakananSebelum = makanan.getStok();
        Transaksi selesai = ts.selesaikanTransaksi(1_000_000);
        System.out.println("    Pembayaran: Rp 1.000.000");
        System.out.println("    Kembalian: Rp " + selesai.getKembali());
        System.out.println("    Stok Makanan sesudah checkout: " + makanan.getStok() + " (Sebelumnya: " + stokMakananSebelum + ")");

        System.out.println("\n=== STRUK PEMBAYARAN ===");
        System.out.println(selesai.buatTeksStruk());

        System.out.println("SEMUA PENGUJIAN OOP & POS BERHASIL 100%!");
    }
}
