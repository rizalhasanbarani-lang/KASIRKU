package com.kasir.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Merepresentasikan sebuah transaksi kasir lengkap dengan daftar item,
 * kalkulasi total belanja, diskon polimorfis, pembayaran, dan pencetakan struk.
 */
public class Transaksi {

    private final String noNota;
    private final LocalDateTime waktu;
    private final List<ItemTransaksi> keranjang;
    private double bayar;
    private double kembali;

    public Transaksi(String noNota) {
        this.noNota = noNota;
        this.waktu = LocalDateTime.now();
        this.keranjang = new ArrayList<>();
    }

    public String getNoNota() {
        return noNota;
    }

    public LocalDateTime getWaktu() {
        return waktu;
    }

    public String getWaktuFormatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return waktu.format(formatter);
    }

    public List<ItemTransaksi> getKeranjang() {
        return Collections.unmodifiableList(keranjang);
    }

    public void tambahItem(Produk produk, int kuantiti) {
        // Cek apakah produk sudah ada di keranjang, jika ada tambahkan kuantitinya
        for (ItemTransaksi item : keranjang) {
            if (item.getProduk().getKode().equalsIgnoreCase(produk.getKode())) {
                item.tambahJumlah(kuantiti);
                return;
            }
        }
        keranjang.add(new ItemTransaksi(produk, kuantiti));
    }

    public void hapusItem(int index) {
        if (index >= 0 && index < keranjang.size()) {
            keranjang.remove(index);
        }
    }

    public void bersihkanKeranjang() {
        keranjang.clear();
        bayar = 0;
        kembali = 0;
    }

    public double hitungTotalSebelumDiskon() {
        double total = 0;
        for (ItemTransaksi item : keranjang) {
            total += item.getSubtotalSebelumDiskon();
        }
        return total;
    }

    public double hitungTotalDiskon() {
        double diskon = 0;
        for (ItemTransaksi item : keranjang) {
            diskon += item.getDiskon();
        }
        return diskon;
    }

    public double hitungGrandTotal() {
        return hitungTotalSebelumDiskon() - hitungTotalDiskon();
    }

    public double getBayar() {
        return bayar;
    }

    public double getKembali() {
        return kembali;
    }

    public void prosesPembayaran(double nominalBayar) {
        double grandTotal = hitungGrandTotal();
        if (nominalBayar < grandTotal) {
            throw new IllegalArgumentException("Uang pembayaran kurang! Dibutuhkan Rp " 
                    + String.format("%,.0f", grandTotal) + ", dibayar Rp " 
                    + String.format("%,.0f", nominalBayar));
        }
        this.bayar = nominalBayar;
        this.kembali = nominalBayar - grandTotal;
    }

    /**
     * Menghasilkan teks struk belanja berformat thermal printer yang rapi.
     */
    public String buatTeksStruk() {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("           KASIR KEDAI POS KOPI          \n");
        sb.append("         Spesialis Kopi & Santai         \n");
        sb.append("=========================================\n");
        sb.append(String.format("No. Nota  : %-25s\n", noNota));
        sb.append(String.format("Waktu     : %-25s\n", getWaktuFormatted()));
        sb.append("-----------------------------------------\n");
        sb.append(String.format("%-18s %4s %8s %7s\n", "Item", "Qty", "Harga", "Subtotal"));
        sb.append("-----------------------------------------\n");

        for (ItemTransaksi item : keranjang) {
            Produk p = item.getProduk();
            String namaItem = p.getNama();
            if (namaItem.length() > 18) {
                namaItem = namaItem.substring(0, 15) + "...";
            }
            sb.append(String.format("%-18s %4d %8.0f %7.0f\n",
                    namaItem,
                    item.getJumlah(),
                    item.getHargaSatuan(),
                    item.getSubtotalSebelumDiskon()));

            // Tampilkan diskon jika ada (hasil polimorfisme)
            if (item.getDiskon() > 0) {
                sb.append(String.format("  * Diskon (%s): -Rp %,.0f\n", 
                        p.getKategori(), item.getDiskon()));
            }
        }

        sb.append("-----------------------------------------\n");
        sb.append(String.format("Total Belanja : Rp %,14.0f\n", hitungTotalSebelumDiskon()));
        sb.append(String.format("Total Diskon  : Rp %,14.0f\n", hitungTotalDiskon()));
        sb.append(String.format("Grand Total   : Rp %,14.0f\n", hitungGrandTotal()));
        sb.append(String.format("Tunai/Bayar   : Rp %,14.0f\n", bayar));
        sb.append(String.format("Kembalian     : Rp %,14.0f\n", kembali));
        sb.append("=========================================\n");
        sb.append("       Terima Kasih Atas Kunjungan Anda   \n");
        sb.append("      Barang yang dibeli tidak dapat     \n");
        sb.append("              dikembalikan               \n");
        sb.append("=========================================\n");

        return sb.toString();
    }
}
