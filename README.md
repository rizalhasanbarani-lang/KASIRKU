# Kasir Kedai Pos Kopi - Java Swing & Web POS
> **Platform Target**: **Apache NetBeans IDE (Java SE)** & **Web Browser (GitHub Pages)**

---

## 1. Struktur Folder Proyek
Proyek dirancang dengan pola arsitektur **MVC (Model - Service/Controller - View)** yang terpisah rapi serta dilengkapi versi Web untuk GitHub Pages:

```
Kasir/
├── pom.xml                                      <- Konfigurasi standar Maven (untuk dibuka di NetBeans)
├── index.html                                   <- Website POS Kasir (Deploy langsung ke GitHub Pages)
├── style.css                                    <- Styling modern antislop tema Kedai Kopi
├── app.js                                       <- Logika Web POS, Diskon Polimorfis, & LocalStorage
├── KasirKedaiPosKopi.jar                         <- File aplikasi Java siap klik (Standalone)
├── run.bat                                      <- Script sekali klik untuk kompilasi & jalankan Java GUI
├── run-test.bat                                 <- Script sekali klik untuk uji logika OOP via Terminal
├── push-ke-github.bat                           <- Script sekali klik untuk upload & deploy ke GitHub
├── README.md                                    <- Dokumentasi teknis proyek & penjelasan tugas
└── src/
    └── main/
        └── java/
            └── com/
                └── kasir/
                    ├── Main.java                <- Titik masuk aplikasi (Entry Point Swing)
                    ├── TestKasirOOP.java        <- Kelas uji coba otomatis konsep OOP
                    ├── model/                   <- Lapisan Data & Entitas (OOP Core)
                    │   ├── Produk.java          <- [Abstraksi & Enkapsulasi] Abstract Class dasar
                    │   ├── ProdukMakanan.java   <- [Pewarisan & Polimorfisme] Subclass produk makanan
                    │   ├── ProdukElektronik.java<- [Pewarisan & Polimorfisme] Subclass produk elektronik
                    │   ├── ItemTransaksi.java   <- Item keranjang (Polymorphic references)
                    │   └── Transaksi.java       <- Kalkulasi total, diskon, pembayaran & format struk
                    ├── service/                 <- Lapisan Logika Bisnis & Pengelolaan Data
                    │   ├── ProdukService.java   <- [CRUD & Overloading] Kelola Create, Read, Update, Delete
                    │   └── TransaksiService.java<- Manajemen POS, validasi stok & checkout
                    └── view/                    <- Lapisan Antarmuka Pengguna (Java Swing GUI)
                        ├── AppButton.java       <- Komponen tombol kontras tinggi (Anti-slop & Anti-fade)
                        ├── MainFrame.java       <- Window utama dengan navigasi TabbedPane
                        ├── PosPanel.java        <- Antarmuka Kasir POS (Katalog, Keranjang, Bayar)
                        ├── ProdukPanel.java     <- Antarmuka CRUD Produk (Form Input + JTable)
                        ├── RiwayatPanel.java    <- Antarmuka Riwayat Transaksi Penjualan
                        └── StrukDialog.java     <- Dialog modal cetak struk belanja monospaced
```

---

## 2. Pemenuhan Konsep Pemrograman Berorientasi Objek (PBO)

Aplikasi ini mengimplementasikan seluruh pilar PBO yang diminta:

### A. Atribut & Enkapsulasi (Encapsulation)
- Semua variabel anggota (atribut) pada kelas model dideklarasikan dengan modifier `private` untuk menyembunyikan data internal dari akses liar luar kelas (`information hiding`).
- Akses dan mutasi atribut dilakukan secara ketat melalui **Getter & Setter** yang disertai validasi bisnis:
  - `setHarga(double harga)`: Menolak harga negatif atau 0.
  - `setStok(int stok)`: Mencegah stok bernilai negatif.
  - `kurangiStok(int jumlah)`: Memastikan stok tidak berkurang jika kuantiti melebihi stok yang ada.

### B. Abstraksi (Abstraction)
- Diterapkan pada kelas dasar: **`public abstract class Produk`**.
- Kelas ini tidak dapat diinstansiasi secara langsung, melainkan berfungsi sebagai blueprint bagi entitas barang dagangan.
- Memiliki sejumlah *abstract method* yang wajib didefinisikan implementasinya oleh subclass:
  ```java
  public abstract double hitungDiskon(int kuantiti);
  public abstract String getKategori();
  public abstract String getInfoKhusus();
  public abstract void setInfoKhusus(String info);
  ```

### C. Pewarisan (Inheritance)
- Kelas turunan mewarisi atribut umum (`kode`, `nama`, `harga`, `stok`) serta metode dari `Produk` dengan kata kunci `extends`:
  - **`ProdukMakanan extends Produk`**: Menambahkan atribut spesifik `tanggalKadaluarsa`.
  - **`ProdukElektronik extends Produk`**: Menambahkan atribut spesifik `garansiBulan`.
- Menggunakan `super(...)` pada konstruktor subclass untuk menginisialisasi atribut superclass.

### D. Polimorfisme (Polymorphism)
1. **Dynamic Polymorphism (Method Overriding)**:
   - Method `hitungDiskon(int kuantiti)` di-override dengan logika diskon berbeda pada tiap subclass:
     - **`ProdukMakanan`**: Diskon grosir 10% jika membeli $\ge 5$ item.
     - **`ProdukElektronik`**: Diskon promo 5% jika total belanja barang tersebut $\ge$ Rp 500.000.
   - Saat item dimasukkan ke dalam keranjang belanja (`ItemTransaksi`), pemanggilan:
     ```java
     produk.hitungDiskon(jumlah);
     ```
     dieksekusi secara **polimorfis saat runtime** sesuai tipe objek aslinya tanpa memerlukan percabangan manual `if (produk instanceof ...)`.
2. **Static Polymorphism (Method Overloading)**:
   - Diterapkan pada `ProdukService`:
     - `cariProduk(String kode)`: Mencari berdasarkan kode unik.
     - `cariProduk(String keywordNama, String kategori)`: Mencari berdasarkan nama dan filter kategori.

---

## 3. Fitur Aplikasi

1. **Manajemen Produk (CRUD Lengkap)**:
   - **Create**: Tambah barang baru (pilihan kategori Makanan atau Elektronik yang mengubah kolom input secara dinamis).
   - **Read**: Tampil di `JTable` dengan fitur pencarian teks dan filter kategori.
   - **Update**: Klik baris di tabel $\rightarrow$ data otomatis masuk ke form $\rightarrow$ ubah data dan simpan.
   - **Delete**: Hapus produk dari sistem dengan dialog konfirmasi.
2. **Point of Sales (Kasir)**:
   - Menampilkan katalog barang dan jumlah stok yang tersedia.
   - Input kuantiti barang lalu masukkan ke keranjang belanja.
   - Otomatis menghitung subtotal, diskon polimorfis, dan Grand Total.
   - Input nominal pembayaran dengan kalkulasi kembalian secara *real-time*.
   - Validasi stok produk otomatis terpotong saat transaksi berhasil diselesaikan.
3. **Cetak Struk Pembayaran**:
   - Preview struk belanja nota bergaya thermal printer monospaced lengkap dengan nomor nota, tanggal, rincian produk, diskon, tunai, dan kembalian.
4. **Riwayat Penjualan**:
   - Menampung seluruh riwayat transaksi yang pernah dibuat dan dapat dibuka kembali struknya sewaktu-waktu.

---

## 4. Cara Menjalankan Proyek

### Opsi 1: Menggunakan Apache NetBeans IDE (Direkomendasikan)
1. Buka aplikasi **Apache NetBeans**.
2. Pilih menu **File** $\rightarrow$ **Open Project...**
3. Arahkan ke folder: `C:\Users\LENOVO\Documents\Kasir` (NetBeans akan otomatis mendeteksi icon cangkir kopi karena ada `pom.xml`).
4. Klik **Open Project**.
5. Klik kanan pada project $\rightarrow$ **Run** (atau tekan tombol **F6** di keyboard).

### Opsi 2: Sekali Klik di Windows (Tanpa NetBeans)
- Cukup double-click file **`run.bat`** di dalam folder `Kasir`. Aplikasi akan otomatis dikompilasi dan window kasir langsung terbuka.
- Untuk menguji logika OOP melalui terminal/command prompt, double-click **`run-test.bat`**.
- Untuk menjalankan file JAR executable langsung: double-click **`KasirKedaiPosKopi.jar`**.

### Opsi 3: Membuka Website POS di Browser
- Cukup double-click file **`index.html`** di browser manapun (Chrome, Edge, Firefox). Website POS siap digunakan secara instan tanpa perlu web server!

---

## 5. Cara Upload & Deploy Otomatis ke GitHub Pages

Repositori ini sudah dilengkapi konfigurasi standar GitHub dan **GitHub Actions CI/CD** (`.github/workflows/deploy.yml`) yang otomatis men-deploy **Website POS ke GitHub Pages** dan mengompilasi **Aplikasi Java**:

### Langkah Upload:
1. Buka [GitHub.com](https://github.com), buat repository baru (misal: `Kasir-Kedai-Pos-Kopi`). Biarkan kosong tanpa mencentang *Add README*.
2. Salin URL repository Anda (misal: `https://github.com/MFahriAr/Kasir-Kedai-Pos-Kopi.git`).
3. Di dalam folder `Kasir`, double-click file **`push-ke-github.bat`**.
4. Paste URL repository GitHub Anda saat diminta, lalu tekan **Enter**.
5. Tunggu proses selesai!

### Mengaktifkan Live Website di GitHub Pages:
1. Di repository GitHub Anda, buka menu **Settings** $\rightarrow$ **Pages** (di sidebar kiri).
2. Di bagian **Build and deployment** $\rightarrow$ **Source**, pilih **Deploy from a branch**.
3. Pilih Branch: **`main`** dan Folder: **`/ (root)`**, lalu klik **Save**.
4. Dalam 1-2 menit, website POS Anda akan aktif secara langsung di URL:
   `https://<username>.github.io/<nama-repo>/`
5. Aplikasi Java `KasirKedaiPosKopi.jar` juga otomatis tersedia untuk diunduh di tab **Actions**.
