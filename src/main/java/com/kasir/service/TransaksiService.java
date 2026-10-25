package com.kasir.service;

import com.kasir.model.ItemTransaksi;
import com.kasir.model.Produk;
import com.kasir.model.Transaksi;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service untuk mengelola transaksi POS (Point of Sales).
 * Menangani penambahan item ke keranjang, validasi stok, checkout, dan pencatatan riwayat transaksi.
 */
public class TransaksiService {

    private Transaksi transaksiAktif;
    private final List<Transaksi> riwayatTransaksi;
    private int nomorUrutNota = 1;

    public TransaksiService() {
        this.riwayatTransaksi = new ArrayList<>();
        bukaTransaksiBaru();
    }

    public Transaksi getTransaksiAktif() {
        return transaksiAktif;
    }

    public List<Transaksi> getRiwayatTransaksi() {
        return new ArrayList<>(riwayatTransaksi);
    }

    public void bukaTransaksiBaru() {
        String tanggal = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String noNota = String.format("TRX-%s-%03d", tanggal, nomorUrutNota++);
        this.transaksiAktif = new Transaksi(noNota);
    }

    public void tambahKeKeranjang(Produk produk, int kuantiti) {
        if (produk == null) {
            throw new IllegalArgumentException("Silakan pilih produk terlebih dahulu.");
        }
        if (kuantiti <= 0) {
            throw new IllegalArgumentException("Kuantiti harus lebih dari 0.");
        }

        // Hitung total kuantiti yang sudah ada di keranjang untuk produk ini
        int kuantitiDiKeranjang = 0;
        for (ItemTransaksi item : transaksiAktif.getKeranjang()) {
            if (item.getProduk().getKode().equalsIgnoreCase(produk.getKode())) {
                kuantitiDiKeranjang += item.getJumlah();
            }
        }

        if (kuantitiDiKeranjang + kuantiti > produk.getStok()) {
            throw new IllegalStateException(
                    "Stok tidak mencukupi! Stok tersedia: " + produk.getStok() + 
                    ", sudah di keranjang: " + kuantitiDiKeranjang + 
                    ", mau ditambah: " + kuantiti
            );
        }

        transaksiAktif.tambahItem(produk, kuantiti);
    }

    public void hapusItemDariKeranjang(int index) {
        transaksiAktif.hapusItem(index);
    }

    public void batalkanTransaksi() {
        transaksiAktif.bersihkanKeranjang();
    }

    /**
     * Memproses checkout akhir:
     * 1. Validasi isi keranjang
     * 2. Validasi nominal uang pembayaran
     * 3. Pengurangan stok produk secara otomatis
     * 4. Simpan ke riwayat transaksi
     * 5. Buat transaksi baru untuk pelanggan berikutnya
     */
    public Transaksi selesaikanTransaksi(double nominalBayar) {
        if (transaksiAktif.getKeranjang().isEmpty()) {
            throw new IllegalStateException("Keranjang belanja masih kosong! Tidak dapat melakukan checkout.");
        }

        // Validasi uang bayar & hitung kembalian
        transaksiAktif.prosesPembayaran(nominalBayar);

        // Kurangi stok riil dari setiap produk
        for (ItemTransaksi item : transaksiAktif.getKeranjang()) {
            item.getProduk().kurangiStok(item.getJumlah());
        }

        // Catat ke riwayat
        Transaksi transaksiSelesai = transaksiAktif;
        riwayatTransaksi.add(transaksiSelesai);

        // Siapkan nota baru untuk transaksi berikutnya
        bukaTransaksiBaru();

        return transaksiSelesai;
    }
}
