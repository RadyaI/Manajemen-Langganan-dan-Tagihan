# Manajemen Subscription dan Langganan

Aplikasi berbasis Desktop (Java Swing) untuk membantu pengguna mengelola daftar langganan layanan digital (seperti Netflix, Spotify, Youtube Premium, dll), memantau tenggat waktu pembayaran, dan mencatat riwayat pengeluaran.

## 👥 Anggota Kelompok
* **Muhammad Radya Iftikhar** - 202410370110370
* **Nur Aini** - 202410370110381

---

## ✨ Fitur Utama
Aplikasi ini menerapkan konsep CRUD (Create, Read, Update, Delete) dengan penyimpanan berbasis File (CSV).

1.  **Login System**
    * Validasi username dan password menggunakan data dari `users.csv`.
    * Setiap user memiliki data langganan yang terisolasi (Data Radya tidak terlihat oleh Aini, dan sebaliknya).

2.  **Dashboard & Manajemen Data (CRUD)**
    * **Create:** Menambah data langganan baru dengan validasi input (Format Rupiah otomatis, validasi tanggal masa depan).
    * **Read (Search):** Mencari dan memfilter daftar langganan berdasarkan nama atau link.
    * **Update (Edit):** Mengubah detail langganan (Nama, Link, Harga, Tenggat).
    * **Delete:** Menghapus langganan yang sudah tidak aktif.

3.  **Sistem Pembayaran (Perpanjangan)**
    * Fitur untuk memperpanjang durasi langganan (1 bulan, 3 bulan, dst).
    * Otomatis menghitung total biaya yang harus dibayar.
    * Otomatis memperbarui tanggal tenggat (*deadline*) berikutnya.

4.  **Riwayat (History)**
    * Mencatat setiap transaksi pembayaran/perpanjangan ke dalam `history.csv`.
    * Menampilkan log aktivitas pembayaran beserta waktu transaksi.

5.  **Unit Testing**
    * Menggunakan **JUnit 5** untuk menguji logika bisnis (Validasi Input, Kalkulasi Harga, Format Tanggal, dan Logika Pencarian).

---

## 🛠️ Teknologi yang Digunakan
* **Bahasa Pemrograman:** Java (JDK 25)
* **GUI Framework:** Java Swing
* **Build Tool:** Maven
* **Database:** CSV (Comma Separated Values)
* **Testing:** JUnit 5 (Jupiter API)

---

## 🚀 Cara Menjalankan Program

### Prasyarat
Pastikan komputer kamu sudah terinstall:
1.  Java Development Kit (JDK) versi 25 atau terbaru.
2.  IDE (Disarankan menggunakan IntelliJ IDEA).

### Langkah-Langkah
1.  **Clone** repository ini melalui terminal:
    ```bash
    git clone https://github.com/RadyaI/Manajemen-Langganan-dan-Tagihan.git
    ```
2.  Buka folder project tersebut menggunakan **IntelliJ IDEA**.
3.  Tunggu proses **Maven Sync** selesai (untuk mengunduh library JUnit otomatis).
4.  Pastikan file data berikut ada di folder root project (sejajar dengan `pom.xml`):
    * `users.csv`
    * `subscriptions.csv`
    * `history.csv`
5.  Buka file `src/main/java/org/example/Main.java`.
6.  Klik tombol **Run** (Icon Play Hijau).

### Akun Demo (Untuk Login)
Gunakan kredensial berikut untuk masuk ke aplikasi:

| Username | Password | Keterangan |
| :--- | :--- | :--- |
| **Radya** | **370** | User 1 |
| **Aini** | **381** | User 2 |

---

## 🧪 Cara Menjalankan Testing (JUnit)
Proyek ini dilengkapi dengan Unit Test untuk memastikan logika aplikasi berjalan benar.

1.  Buka folder `src/test/java`.
2.  Klik kanan pada file `LogicTest.java`.
3.  Pilih **Run 'LogicTest'**.
4.  Pastikan semua indikator tes berwarna **HIJAU (Passed)**.

---

## 📂 Struktur Project

```text
Manajemen-Langganan/
├── .idea/
├── src/
│   ├── main/
│   │   ├── java/org/example/
│   │   │   ├── Main.java          # Halaman Login
│   │   │   ├── Dashboard.java     # Menu Utama (CRUD)
│   │   │   ├── CreateData.java    # Form Tambah Data
│   │   │   ├── History.java       # Halaman Riwayat
│   │   │   └── Style.java         # Konfigurasi UI (Warna/Font)
│   │   └── resources/icons/       # Aset Gambar (edit.png, delete.png, pay.png)
│   └── test/
│       └── java/
│           └── LogicTest.java     # Unit Testing
├── users.csv                      # Data Akun
├── subscriptions.csv              # Data Langganan
├── history.csv                    # Data Riwayat
├── pom.xml                        # Konfigurasi Maven
└── README.md                      # Dokumentasi Project