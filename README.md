# ifs25059-p5-pbo2026-springboot-cashflow

Aplikasi web **Cashflow (Catatan Keuangan)** berbasis **Spring Boot 4.1.2**, **Maven**, **Vaadin 25.3.4**, dan **JDBC (PostgreSQL)**, dengan arsitektur **Clean Architecture** (domain, usecase, adapter, framework).

| | |
| --- | --- |
| Folder | `ifs25059-p5-pbo2026-springboot-cashflow` |
| Artifact Maven | `ifs25059-p5-pbo2026-springboot-cashflow` |
| Package | `org.delcom.ifs25059springbootcashflow` |
| Database | `pbo2026_cashflow` |

## Prasyarat

- JDK 25
- PostgreSQL — buat database terlebih dahulu:

```sql
CREATE DATABASE pbo2026_cashflow;
```

- Maven Wrapper (`mvnw` / `mvnw.cmd`)

## Konfigurasi `.env`

Salin `.env.example` menjadi `.env` (file `.env` diabaikan git):

```env
APP_PORT=8080

DB_CONNECTION=pgsql
DB_HOST=localhost
DB_PORT=5432
DB_DATABASE=pbo2026_cashflow
DB_USERNAME=postgres
DB_PASSWORD=postgres

ROOT_USERNAME=admin
ROOT_PASSWORD=admin

UPLOAD_DIR=uploads
```

Akun admin di-seed otomatis saat start dari `ROOT_USERNAME` / `ROOT_PASSWORD`. Upload disimpan di `UPLOAD_DIR` dan dilayani di `/uploads/**`.

`DotEnv` hanya mengisi system property jika variabel lingkungan / property dengan nama yang sama **belum** ada. Di server/cloud, nilai `DB_*` dari environment platform yang dipakai.

**Env DB yang didukung (selaras platform deploy):**

| Variabel | Keterangan |
| --- | --- |
| `DB_CONNECTION` | `pgsql` (informasional; driver tetap PostgreSQL) |
| `DB_HOST` | host Postgres/PgBouncer |
| `DB_PORT` | port (contoh cloud: `6432`) |
| `DB_DATABASE` | nama database (utama; di cloud: `db_<website_id>` dengan `-` diganti `_`) |
| `DB_USERNAME` | user DB |
| `DB_PASSWORD` | password DB |

`application.yaml` memakai `${DB_DATABASE}` tanpa default — wajib diisi lewat `.env` / environment.

## Fitur

- Registrasi, login, logout (`AuthSession`: cookie `cashflow_uid` + flag `LOGGED_OUT_KEY`); sesi bertahan setelah refresh
- Transaksi per pengguna: keterangan, jumlah (`long`), tipe **Pemasukan (INCOME)** / **Pengeluaran (EXPENSE)**, bukti gambar opsional (`proofPath`), waktu dicatat
- Kartu ringkasan: total transaksi, total pemasukan, total pengeluaran, saldo
- Toolbar: cari keterangan (case-insensitive), urutkan (`SortOption`: jumlah terkecil/terbesar, pemasukan dulu, pengeluaran dulu, terbaru, terlama), muat ulang, tambah pemasukan, tambah pengeluaran
- Grid: bukti (thumbnail), ID, keterangan, jumlah, tipe, waktu, tindakan (ubah / hapus dengan konfirmasi); header kolom bisa diurutkan
- Klik bukti → modal zoom (`ImagePreviewDialog`)
- Format uang lewat `FinancePresenter`: `Rp 1,000,000.00`, baris `[<id>] <keterangan> | Rp 1,000,000.00 | [Pemasukan]`, `Saldo: Rp 4,975,000.00`
- Input jumlah menerima `1000000`, `1.000.000`, atau `1,000,000`; jumlah kosong / non-angka / ≤ 0 ditolak dengan notifikasi
- Profil: nama, password, foto
- Role **ADMIN**: daftar pengguna + lihat transaksi user lain (`/user-transactions/{id}`, read-only); admin punya cashflow sendiri
- Role **USER**: hanya transaksi sendiri; `/users` dan `/user-transactions` dialihkan ke `/`

## Rute & hak akses

| Route | View | Akses |
| --- | --- | --- |
| `/login`, `/register` | `LoginView`, `RegisterView` | Publik (dialihkan ke `/` bila sudah login) |
| `/uploads/**`, `/icons/**`, `styles.css` | static | Publik |
| `/` | `FinanceView` | USER & ADMIN |
| `/profile` | `ProfileView` | USER & ADMIN |
| `/users` | `UsersView` | ADMIN |
| `/user-transactions/{id}` | `UserTransactionsView` | ADMIN (read-only) |

## Struktur singkat

```text
src/main/java/org/delcom/ifs25059springbootcashflow/
├── domain/          # entity (Transaction, TransactionType, Balance, User, Role, SortOption),
│                    # exception, repository port (IRepository, ITransactionRepository, IUserRepository), PasswordHasher
├── usecase/         # AuthUseCase, FinanceUseCase, UserUseCase
├── adapter/         # Jdbc*/InMemory*, SchemaInitializer, FileStorage, FinancePresenter
├── framework/       # auth (AuthSession), view (Vaadin)
├── config/          # AppConfig (composition root)
├── DotEnv.java
└── App.java
```

Runtime memakai **JDBC + PostgreSQL**. Unit test repository memakai **InMemory**; tes JDBC memakai **H2**.

## Menjalankan

Dari direktori proyek `ifs25059-p5-pbo2026-springboot-cashflow`:

```powershell
cd ifs25059-p5-pbo2026-springboot-cashflow  # jika berada di root workspace praktikum
.\mvnw.cmd spring-boot:run
```

Buka [http://localhost:8080](http://localhost:8080) (atau port `APP_PORT`).

Login admin: sesuai `ROOT_USERNAME` / `ROOT_PASSWORD` di `.env`.

### Build JAR (profil production Vaadin)

Jalankan dari **root proyek** agar `.env` terbaca (jika tidak di-override environment):

```powershell
.\mvnw.cmd clean package -Pproduction
java -jar target\ifs25059-p5-pbo2026-springboot-cashflow-0.0.1-SNAPSHOT.jar
```

Tanpa `-Pproduction`, menjalankan JAR biasanya gagal (`vaadin-dev-server not found`).

## Uji & coverage

```powershell
.\mvnw.cmd clean verify
```

- Jacoco mengecualikan `framework/view` dan `framework/auth`
- Laporan: `target/site/jacoco/index.html`

## Troubleshooting JAR di server/cloud

Jika log berisi `Gagal menginisialisasi schema database` / `Connection ... refused`, **JAR sudah berhasil di-build**; yang gagal adalah koneksi database.

1. Pastikan PostgreSQL (atau PgBouncer) hidup dan menerima TCP dari host aplikasi.
2. Cocokkan `DB_HOST`, `DB_PORT`, `DB_DATABASE`, `DB_USERNAME`, `DB_PASSWORD` (environment server mengalahkan `.env`).
3. Pastikan database sudah ada / dibuat di platform.
4. Contoh error `Connection to delcom-deployment-pgbouncer:6432 refused` berarti host/port DB tidak reachable — perbaiki layanan DB/PgBouncer atau jaringan, bukan rebuild Vaadin.
