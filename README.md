# Janji
Saya Razan Raihan Malik dengan NIM 2508838 mengerjakan Tugas Praktikum 6 pada Mata Kuliah Desain dan Pemrograman Berorientasi Objek (DPBO) untuk keberkahan-Nya maka saya tidak melakukan kecurangan seperti yang telah dispesifikasikan. Aamiin
# Struktur File

# Desain / Fungsi Program
## Arsitektur
Program memakai pemisahan **Logic** (aturan dan data permainan) dan **View** (tampilan), mirip pola MVC sederhana.
| Kelas | Peran |
|---|---|
| `App` | Titik masuk program. Membuat JFrame, merangkai semua komponen, dan berpindah layar dengan `CardLayout` (Menu ↔ Game). |
| `MainMenu` | Layar menu awal dengan tombol **MULAI** dan **KELUAR**. |
| `Logic` | Otak permainan: timer, gravitasi, tabrakan, skor, status, dan input keyboard. Mengimplementasikan `ActionListener` dan `KeyListener`. |
| `View` | Layar permainan (`JPanel`). Menggambar latar, pipa, burung, `JLabel` skor, pesan, dan efek Game Over. |
| `Player` | Data burung: posisi, ukuran, kecepatan vertikal, gambar, dan sudut rotasi. |
| `Pipe` | Data pipa: posisi, ukuran, gambar, dan status `passed` (sudah dilewati atau belum). |
| `Sound` | Pengelola audio: BGM berulang dan efek suara. Kalau tidak ada perangkat audio, game tetap jalan tanpa suara. |
## Hubungan antar kelas
```
App ──membuat──► Logic ◄──────► View (JPanel)
 │                 │  ▲            ▲
 │                 │  └ setView()  └ addKeyListener(logic)
 │                 ├── Player (1)
 │                 ├── ArrayList<Pipe> (banyak)
 │                 └── Sound
 └──CardLayout──► MainMenu  /  View
```
- `Logic` memegang `Player`, daftar `Pipe`, dan `Sound`. `View` hanya membaca data itu lewat getter, lalu menggambarnya.
- `Logic` memberi tahu `View` lewat `setScore()`, `showMessage()`, `startGameOverEffect()`, dan `clearGameOverEffect()`.
- `Logic` tidak tahu soal CardLayout. Kembali ke menu dilakukan lewat `Runnable menuListener` yang dipasang `App`.
## Status permainan (State)
Permainan punya tiga status di `Logic`:
```
 READY ──SPASI──► PLAYING ──nabrak / jatuh──► GAME_OVER
   ▲                                              │
   └───────────────── tombol R ───────────────────┘
                       (tombol M → kembali ke Main Menu)
```
| Status | Kondisi |
|---|---|
| `READY` | Burung melayang diam, menunggu SPASI pertama. |
| `PLAYING` | Gravitasi, pipa, tabrakan, dan skor aktif. |
| `GAME_OVER` | Semua berhenti. Hanya R dan M yang direspons. |
Dengan status ini, `move()` cukup mengecek `if (state != PLAYING) return;` untuk membekukan permainan saat Game Over.
## Dua timer
- **`gameLoop`** berjalan tiap 1000/60 ms (60 FPS). Ia memanggil `move()` lalu `repaint()`. Timer ini tetap hidup saat Game Over supaya animasi flash merah dan fade abu-abu tetap tampil.
- **`pipesCooldown`** berjalan tiap 1500 ms dan memanggil `placePipes()`. Timer ini hanya hidup selama permainan berlangsung.
## Rincian fitur
- **Tabrakan:** hitbox burung dibuat sebagai `Rectangle`, dikecilkan 3px di tiap sisi supaya adil. Dicek dengan `intersects()` terhadap setiap pipa. Jatuh ke bawah dicek lewat `posY + tinggi >= frameHeight`.
- **Skor:** sepasang pipa (atas dan bawah) hanya bernilai 1 poin. Poin dihitung dari pipa atas saja, saat `posX burung > posX pipa + lebar pipa`. Flag `passed` mencegah poin terhitung dua kali.
- **Rotasi burung:** sudut target dihitung dari kecepatan (`velocityY × 4.5`, dibatasi −30° sampai 80°). Sudut sekarang bergerak 20% mendekati target tiap frame, sehingga terlihat mengayun halus. Saat digambar, `Graphics2D.rotate()` memutar burung di sekitar titik tengahnya.
- **Efek Game Over:** saat Game Over, adegan terakhir difoto ke `BufferedImage`. Dari foto itu dibuat versi abu-abu dengan rumus luminansi `0,299R + 0,587G + 0,114B` (saturasi 0). Tiap frame, `View` menggambar foto berwarna, menumpuk foto abu-abu dengan alpha naik 0→1 (fade in), lalu menumpuk lapisan merah yang alpha-nya turun dari 0,75→0 (flash).
- **Audio:** BGM berulang dengan `Clip.loop`. Efek suara untuk lompat, skor, tabrakan, dan game over memakai `Clip` yang diputar ulang dari awal. Tiap kelipatan 10 skor, efek skor diganti efek spesial.
# Alur Program
## Alur program dari awal
1. `App.main()` membuat `Logic` dan `View`, lalu menghubungkan keduanya dengan `setView()`.
2. `App` membuat JFrame berisi `CardLayout` dengan dua kartu: `menu` dan `game`.
3. **Main Menu tampil.** Pemain memilih:
   - **KELUAR** → `System.exit(0)`.
   - **MULAI** → berpindah ke kartu `game`, memanggil `logic.startGame()`, lalu `View` meminta fokus keyboard.
4. `startGame()` memanggil `resetGame()`, menyalakan BGM, dan menjalankan `gameLoop`. Status menjadi `READY`.
## Alur saat bermain
1. **Menunggu (`READY`):** muncul pesan "Tekan SPASI untuk mulai".
2. **SPASI pertama:**
   - Status menjadi `PLAYING`.
   - Pipa pertama langsung dibuat, lalu `pipesCooldown` dinyalakan.
   - Burung loncat (`velocityY = -10`) dan efek suara lompat berbunyi.
3. **Setiap frame (60 kali per detik)** di `move()`:
   1. `velocityY += gravity`, lalu `posY += velocityY`.
   2. Hitung sudut rotasi burung.
   3. Cek jatuh: kalau menyentuh batas bawah → **Game Over**.
   4. Untuk tiap pipa:
      - Geser ke kiri sebesar `pipeVelocityX`.
      - Cek tabrakan dengan burung → kalau kena, **Game Over**.
      - Cek apakah sudah dilewati → kalau ya, `score++`, update `JLabel` skor, lalu putar efek suara. Kelipatan 10 memakai efek spesial.
      - Hapus pipa yang sudah keluar layar dari daftar.
   5. `repaint()` → `View` menggambar latar, pipa, lalu burung yang sudah dirotasi.
4. **Setiap 1,5 detik:** `placePipes()` membuat sepasang pipa dengan celah 160px di posisi Y acak, dengan margin 80px dari tepi atas dan bawah.
## Alur Game Over
1. `gameOver()` dipanggil dari `move()`, karena burung menabrak pipa atau jatuh.
2. Yang dilakukan:
   - Status menjadi `GAME_OVER`, sehingga `move()` langsung berhenti.
   - `pipesCooldown` dimatikan dan BGM dihentikan.
   - Efek suara tabrakan dan game over diputar.
   - Skor terbaik diperbarui.
   - `view.startGameOverEffect()` memotret layar dan menyiapkan versi abu-abu.
   - Muncul pesan "GAME OVER", skor, dan skor terbaik.
3. `gameLoop` tetap berjalan, jadi `View` terus menggambar flash merah yang cepat hilang dan fade ke abu-abu.
4. Pemain memilih:
   - **R** → `resetGame()`: pipa dihapus, burung kembali ke posisi awal, skor jadi 0, efek dihapus, status kembali `READY`, BGM nyala lagi.
   - **M** → `gameLoop` dihentikan, lalu kembali ke Main Menu lewat `menuListener`.
Saat Game Over, SPASI tidak ada efeknya, karena handler `keyPressed` memeriksa status lebih dulu.
## Ringkasan alur
```
Mulai program → Main Menu ─ KELUAR → selesai
                    │
                  MULAI
                    ▼
              READY (tunggu SPASI)
                    ▼
              PLAYING ──► tiap frame: gravitasi → cek jatuh → geser pipa
                    │                  → cek tabrakan → cek skor → gambar
                    ▼
              GAME_OVER ── R → READY (ulang)
                    └───── M → Main Menu
```

# Dokumentasi
[![YouTube](https://img.shields.io/badge/YouTube-FF0000?style=for-the-badge&logo=youtube&logoColor=white)](https://www.youtube.com/watch?v=4gRAuA6A8bM)

klik untuk melihat video
