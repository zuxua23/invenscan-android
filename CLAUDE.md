# CLAUDE.md — Multi-Agent Team Config

## 🧠 Agent Team

| Agent | Model | Role |
|-------|-------|------|
| 🟡 SONNET | `claude-sonnet-4-6` | UI, analyst, daily coding, boilerplate |
| 🔵 OPUS | `claude-opus-4-8` | Complex logic, review, debugging lintas layer |
| 🔴 FABLE | `claude-fable-5` | Architecture, last resort (sparingly!) |

### Ratio Target
- SONNET: ~80% tasks
- OPUS: ~15% tasks
- FABLE: ~5% tasks max

### Auto Escalation Rules
- SONNET gagal / output ga memuaskan 2x → eskalasi ke OPUS
- OPUS gagal 2x → eskalasi ke FABLE
- FABLE solve → handoff solution ke SONNET untuk finalisasi

---

## 🔄 Universal Workflow (semua stack)

```
START
  │
  ▼
[FABLE] System architecture + ERD + API design + modul breakdown
  │
  ▼
[SONNET] Project setup + base structure + boilerplate
  │
  ▼
[SONNET] Implement fitur (loop per fitur)
  │
  ├── Straightforward → done ✅
  └── Complex / stuck →
        [OPUS] Debug / implement complex part
          │
          ├── Solved → done ✅
          └── Still stuck →
                [FABLE] Solve → handoff ke SONNET → done ✅
  │
  ▼
[OPUS] Pre-commit review (security + quality check)
  │
  ▼
DONE ✅
```

---

## 🔒 Security Standards (WAJIB semua stack)

Setiap baris kode yang ditulis HARUS mengikuti rules ini tanpa terkecuali.
Jangan pernah skip security untuk alasan "nanti aja" atau "ini cuma dev".

### Input & Validation
- Semua input dari user/client WAJIB divalidasi di server side
- Whitelist approach: tolak semua yang tidak diizinkan secara eksplisit
- Jangan pernah trust data dari client: header, cookie, query param, body
- Sanitize semua input sebelum diproses (strip HTML, escape special chars)
- Validate tipe data, panjang, format, dan range di setiap field

### Injection Prevention
- WAJIB parameterized query / prepared statement — TIDAK BOLEH string concatenation untuk SQL
- WAJIB escape output ke HTML untuk mencegah XSS
- Hindari eval(), exec(), system() kecuali benar-benar diperlukan dan input sudah disanitasi
- Android: jangan gunakan WebView.loadUrl() dengan input user tanpa sanitasi

### Authentication & Authorization
- Password WAJIB di-hash dengan bcrypt / Argon2 (minimum cost factor 12)
- JANGAN simpan password plain text atau MD5/SHA1
- JWT: gunakan secret key yang kuat (min 256-bit), set expiry yang wajar
- Refresh token harus disimpan di httpOnly cookie, bukan localStorage
- Setiap endpoint WAJIB ada authorization check — jangan andalkan frontend
- Implement rate limiting di endpoint auth (login, register, forgot password)
- Multi-factor authentication untuk endpoint sensitif jika memungkinkan

### Data Protection
- Semua komunikasi WAJIB HTTPS / TLS 1.2+
- Data sensitif di database WAJIB dienkripsi (PII, nomor kartu, dll)
- Jangan log data sensitif (password, token, nomor kartu, dsb)
- Android: jangan simpan data sensitif di SharedPreferences tanpa enkripsi — gunakan EncryptedSharedPreferences
- API key / secret WAJIB di environment variable, TIDAK BOLEH hardcode di kode
- File .env WAJIB masuk .gitignore

### API Security
- Implement CORS yang ketat — whitelist origin yang diizinkan
- Semua response API jangan expose stack trace atau detail error internal ke client
- Gunakan response wrapper yang konsisten: `{ success, data, message }` — jangan expose nama tabel/kolom DB
- Implement request size limit untuk mencegah payload flooding
- Versioning API (/api/v1/) untuk backward compatibility
- Audit log untuk operasi sensitif (login, delete, update data penting)

### Dependency & Supply Chain
- Selalu gunakan versi library yang aktif di-maintain
- Jangan gunakan library yang sudah deprecated atau abandoned
- Lock versi dependency (package-lock.json, composer.lock, go.sum, Podfile.lock)
- Scan dependency vulnerability secara berkala

### Stack-Specific Security

**Android:**
- ProGuard / R8 wajib aktif di release build
- Jangan izinkan backup aplikasi jika menyimpan data sensitif (`android:allowBackup="false"`)
- Certificate pinning untuk API calls production
- Root detection jika aplikasi menangani data sensitif
- Jangan hardcode URL, API key, atau secret di kode — gunakan BuildConfig / local.properties
- Validasi deep link / intent data sebelum diproses

**Laravel:**
- CSRF protection wajib aktif
- Gunakan Laravel Policy untuk authorization, bukan cek manual di controller
- Mass assignment protection: selalu definisikan `$fillable` atau `$guarded`
- Query Eloquent: hindari `->where(request()->all())` langsung
- File upload: validasi MIME type di server, simpan di luar public/, rename file

**Next.js / React:**
- Semua secret WAJIB di server side — jangan expose ke client (NEXT_PUBLIC_ hanya untuk non-secret)
- Sanitasi input sebelum render ke DOM (DOMPurify jika perlu render HTML)
- Content Security Policy (CSP) header
- Validasi ulang di server action / API route meskipun sudah validasi di frontend

**ASP.NET Core:**
- Aktifkan HTTPS redirection middleware
- Gunakan Data Protection API untuk enkripsi data sensitif
- Anti-forgery token untuk form submission
- Aktifkan security headers: X-Frame-Options, X-Content-Type-Options, HSTS

**Golang:**
- Gunakan `html/template` bukan `text/template` untuk output HTML
- Validasi semua struct dari request menggunakan library validator
- Hindari goroutine leak — pastikan semua goroutine punya exit condition

**Flutter:**
- Jangan simpan token di plain storage — gunakan flutter_secure_storage
- Obfuscate release build (`--obfuscate --split-debug-info`)
- Validate SSL certificate — jangan bypass di production

---

## 🧹 Code Quality Standards (WAJIB semua stack)

### Tidak Ada Komentar di Kode
- DILARANG menulis komentar `// ini untuk login`, `// ambil data`, dsb
- Kode harus self-explanatory melalui penamaan yang baik
- Satu-satunya komentar yang boleh ada:
  - Dokumentasi public API / fungsi publik (JSDoc, KDoc, XML doc)
  - Penjelasan algoritma non-obvious yang kompleks (bukan "apa", tapi "kenapa")
  - TODO yang sudah pasti ada tiket/issue-nya: `// TODO(#123): ...`

### Penamaan yang Jelas dan Konsisten
- Nama variabel, fungsi, class harus menjelaskan apa isinya / apa yang dilakukan
- DILARANG: `data`, `temp`, `val`, `x`, `y`, `str`, `obj`, `result` sebagai nama final
- Gunakan nama yang spesifik: `userAccessToken`, `fetchStockItemById`, `isRfidScannerReady`
- Konsisten dalam satu codebase: kalau pakai `fetch` jangan campurkan dengan `get` untuk hal yang sama
- Boolean: prefix `is`, `has`, `can`, `should` — contoh: `isLoading`, `hasPermission`
- Koleksi: nama plural — `users`, `stockItems`, `scannedTags`

### Struktur & Organisasi File
- Satu file = satu tanggung jawab utama
- Ukuran file: maksimal 300 baris — kalau lebih, pecah jadi modul/class terpisah
- Ukuran fungsi: maksimal 30 baris — kalau lebih, ekstrak ke fungsi helper
- Grouping yang konsisten dalam satu file: imports → constants → types → functions → exports
- Folder structure harus mencerminkan domain, bukan tipe file

### Clean Code Principles
- DRY (Don't Repeat Yourself): kalau kode yang sama muncul 2x, ekstrak jadi fungsi/helper
- Single Responsibility: satu fungsi melakukan satu hal
- Fail fast: validasi dan return error di awal fungsi, bukan nested if berlapis
- Hindari nested callback / deeply nested if — gunakan early return pattern
- Jangan biarkan dead code (kode yang tidak pernah dipanggil) — hapus saja
- Magic number DILARANG — semua angka dan string literal ke constant

### Error Handling
- Setiap error WAJIB di-handle secara eksplisit — jangan swallow exception dengan catch kosong
- Error message harus informatif untuk developer, tapi generik untuk user
- Gunakan custom exception / error types untuk domain error yang spesifik
- Log semua error dengan context yang cukup (request id, user id, timestamp)

### Maintainability
- Setiap fungsi publik WAJIB punya unit test minimal happy path + 1 edge case
- Interface / contract didefinisikan sebelum implementasi
- Dependency injection — jangan instantiate dependency langsung di dalam class
- Konfigurasi environment di satu tempat (config file / env), bukan tersebar di mana-mana
- Breaking change di API selalu diversion (`/v2/`) — jangan ubah contract yang sudah ada

---

## 📋 Prompt Templates

### ── ANDROID (Kotlin/Compose) ──────────────────────────────

```
[NEW APP — ANDROID]

App: [nama aplikasi]
Deskripsi: [apa yang dilakukan app ini]

Fitur:
- [fitur 1]
- [fitur 2]
- [fitur 3]

Tech Stack:
- Android: Kotlin + Jetpack Compose
- Backend: [ASP.NET Core / Laravel / Node.js / dst]
- Database lokal: Room DB
- Networking: Retrofit
- Auth: [JWT / Firebase Auth / dst]
- Extra: [RFID Denso SP1 / CameraX ML Kit / Firebase / dst]

Jalankan workflow ini TANPA nunggu konfirmasi, berurutan:

PHASE 1 — ARCHITECTURE
1. System architecture overview (layer diagram)
2. ERD lengkap semua tabel
3. API endpoint list (method, path, request, response)
4. Breakdown modul + urutan pengerjaan
5. Simpan ke docs/architecture.md

PHASE 2 — PROJECT SETUP
1. Struktur folder MVVM + Repository pattern
2. Base classes: BaseActivity, BaseViewModel, BaseRepository
3. Setup Retrofit + Auth interceptor + error handler + certificate pinning
4. Setup Room DB + TypeConverters
5. Setup Hilt (Dependency Injection)
6. Setup Compose Navigation
7. Setup EncryptedSharedPreferences untuk data sensitif
8. ProGuard rules untuk release
9. Simpan ke docs/setup.md

PHASE 3 — IMPLEMENT
Implement fitur pertama dari breakdown secara end-to-end:
- UI (Jetpack Compose)
- ViewModel + StateFlow
- Repository + Room DAO
- API endpoint (jika fullstack)

Pastikan semua kode mengikuti Security Standards dan Code Quality Standards di CLAUDE.md.
Mulai sekarang.
```

---

### ── ANDROID + HARDWARE SDK ─────────────────────────────────

```
[NEW APP — ANDROID + HARDWARE]

App: [nama aplikasi]
Hardware: [Denso SP1 / BHT-M80 / Zebra / Honeywell / dst]
SDK: [nama SDK + versi jika ada]

Fitur:
- [fitur 1]
- [fitur 2]

Tech Stack:
- Android: Kotlin + [Compose / XML]
- Hardware SDK: [nama SDK]
- Backend: [stack backend]
- Database lokal: Room DB
- Sync: WorkManager (offline-first)

Jalankan workflow ini TANPA nunggu konfirmasi:

PHASE 1 — ARCHITECTURE
1. Hardware integration architecture (SDK flow diagram)
2. Offline-first sync strategy (local DB → queue → API)
3. ERD + API endpoint list
4. Breakdown modul + urutan pengerjaan
5. Simpan ke docs/architecture.md

PHASE 2 — PROJECT SETUP
1. Struktur folder + base classes
2. SDK singleton setup ([NamaSDK]Manager)
3. Setup Room DB + WorkManager sync
4. Setup Retrofit + interceptor + certificate pinning
5. EncryptedSharedPreferences untuk token/config sensitif
6. ProGuard rules
7. Simpan ke docs/setup.md

PHASE 3 — IMPLEMENT
Implement fitur scan pertama end-to-end:
- SDK init + scan callback
- Simpan hasil scan ke Room
- Queue sync ke API via WorkManager
- UI feedback (banner / snackbar)

Pastikan semua kode mengikuti Security Standards dan Code Quality Standards di CLAUDE.md.
Mulai sekarang.
```

---

### ── WEB FULLSTACK (Laravel) ────────────────────────────────

```
[NEW APP — LARAVEL FULLSTACK]

App: [nama aplikasi]
Deskripsi: [deskripsi singkat]

Fitur:
- [fitur 1]
- [fitur 2]
- [fitur 3]

Tech Stack:
- Backend: Laravel [versi]
- Frontend: [Blade + Alpine.js / Inertia + Vue / Livewire]
- Database: [MySQL / PostgreSQL]
- Auth: [Breeze / Sanctum / Passport]
- Extra: [Queue / Scheduler / Storage / dst]

Jalankan workflow ini TANPA nunggu konfirmasi:

PHASE 1 — ARCHITECTURE
1. System architecture + MVC flow
2. ERD lengkap
3. Route list (method, URI, controller, middleware)
4. Breakdown modul + urutan pengerjaan
5. Simpan ke docs/architecture.md

PHASE 2 — PROJECT SETUP
1. Setup Laravel project + env config
2. Migration semua tabel dari ERD
3. Model + relationship + $fillable (mass assignment protection)
4. Base Controller + API response wrapper
5. Auth setup + rate limiting pada auth routes
6. CORS config yang ketat
7. Global error handler (jangan expose stack trace ke client)
8. Simpan ke docs/setup.md

PHASE 3 — IMPLEMENT
Implement modul pertama end-to-end:
- Migration + Model + Seeder
- Controller (index, store, show, update, destroy)
- Form Request validation
- Policy untuk authorization
- Route + middleware

Pastikan semua kode mengikuti Security Standards dan Code Quality Standards di CLAUDE.md.
Mulai sekarang.
```

---

### ── WEB FULLSTACK (Next.js) ────────────────────────────────

```
[NEW APP — NEXT.JS FULLSTACK]

App: [nama aplikasi]
Deskripsi: [deskripsi singkat]

Fitur:
- [fitur 1]
- [fitur 2]
- [fitur 3]

Tech Stack:
- Frontend: Next.js [versi] + TypeScript
- Styling: [Tailwind CSS / shadcn/ui / MUI]
- Backend: [Next.js API Routes / terpisah]
- Database: [PostgreSQL / MySQL / MongoDB]
- ORM: [Prisma / Drizzle]
- Auth: [NextAuth / Clerk / Supabase Auth]
- State: [Zustand / Redux Toolkit / React Query]
- Extra: [Supabase / Firebase / Cloudinary / dst]

Jalankan workflow ini TANPA nunggu konfirmasi:

PHASE 1 — ARCHITECTURE
1. App architecture (App Router structure)
2. ERD / data model
3. API routes list (path, method, request, response)
4. Component tree overview
5. Breakdown modul + urutan pengerjaan
6. Simpan ke docs/architecture.md

PHASE 2 — PROJECT SETUP
1. Struktur folder (app/, components/, lib/, hooks/, types/)
2. Setup Prisma / Drizzle + database connection
3. Setup Auth
4. Base components: Layout, Navbar, Sidebar, Loading, Error
5. Setup API handler + error middleware
6. Security headers (next.config.js): CSP, X-Frame-Options, HSTS
7. Input validation library (zod) setup
8. Simpan ke docs/setup.md

PHASE 3 — IMPLEMENT
Implement modul pertama end-to-end:
- Database schema + migration
- API route dengan validation (zod) + authorization check
- React hooks / server actions
- UI page + components
- Loading + error state

Pastikan semua kode mengikuti Security Standards dan Code Quality Standards di CLAUDE.md.
Mulai sekarang.
```

---

### ── WEB FULLSTACK (ASP.NET Core) ───────────────────────────

```
[NEW APP — ASP.NET CORE]

App: [nama aplikasi]
Deskripsi: [deskripsi singkat]

Fitur:
- [fitur 1]
- [fitur 2]
- [fitur 3]

Tech Stack:
- Backend: ASP.NET Core [versi] (API / MVC)
- Database: [SQL Server / PostgreSQL]
- ORM: Entity Framework Core
- Auth: [JWT Bearer / Identity / Azure AD]
- Frontend: [React / Next.js / Razor + Alpine.js]
- Extra: [SignalR / Hangfire / dst]

Jalankan workflow ini TANPA nunggu konfirmasi:

PHASE 1 — ARCHITECTURE
1. Clean architecture layer diagram
2. ERD + database schema
3. API endpoint list (controller, action, method, route)
4. Breakdown modul + urutan pengerjaan
5. Simpan ke docs/architecture.md

PHASE 2 — PROJECT SETUP
1. Struktur folder clean architecture (API, Application, Domain, Infrastructure)
2. DbContext + Entity base classes
3. Migration awal dari ERD
4. Base repository + unit of work pattern
5. JWT auth middleware + refresh token
6. Global error handler (ProblemDetails, jangan expose stack trace)
7. Response wrapper konsisten
8. Rate limiting middleware
9. Security headers middleware
10. Simpan ke docs/setup.md

PHASE 3 — IMPLEMENT
Implement modul pertama end-to-end:
- Entity + Migration
- Repository + Service
- Controller + DTOs
- Validation (FluentValidation)

Pastikan semua kode mengikuti Security Standards dan Code Quality Standards di CLAUDE.md.
Mulai sekarang.
```

---

### ── BACKEND API ONLY (Golang) ──────────────────────────────

```
[NEW APP — GOLANG REST API]

App: [nama service]
Deskripsi: [deskripsi singkat]

Endpoints:
- [resource 1]: CRUD
- [resource 2]: [operasi spesifik]

Tech Stack:
- Go [versi]
- Framework: [Gin / Echo / Fiber]
- Database: [PostgreSQL / MySQL]
- ORM: [GORM / sqlx]
- Auth: JWT
- Extra: [Redis / RabbitMQ / gRPC / dst]

Jalankan workflow ini TANPA nunggu konfirmasi:

PHASE 1 — ARCHITECTURE
1. Service architecture
2. Database schema / ERD
3. API endpoint list lengkap
4. Breakdown + urutan pengerjaan
5. Simpan ke docs/architecture.md

PHASE 2 — PROJECT SETUP
1. Struktur folder (cmd/, internal/, pkg/)
2. Database connection + migration
3. Base handler + middleware (auth, logger, recovery, rate limiter, CORS)
4. Router setup
5. Config management (.env)
6. Input validation setup (go-playground/validator)
7. Simpan ke docs/setup.md

PHASE 3 — IMPLEMENT
Implement resource pertama end-to-end:
- Model / entity
- Repository (parameterized query wajib)
- Service (business logic)
- Handler (HTTP) + validation
- Route registration

Pastikan semua kode mengikuti Security Standards dan Code Quality Standards di CLAUDE.md.
Mulai sekarang.
```

---

### ── MOBILE CROSS-PLATFORM (Flutter) ────────────────────────

```
[NEW APP — FLUTTER]

App: [nama aplikasi]
Platform: [Android / iOS / both]
Deskripsi: [deskripsi singkat]

Fitur:
- [fitur 1]
- [fitur 2]

Tech Stack:
- Flutter [versi] + Dart
- State: [Riverpod / Bloc / Provider]
- Backend: [REST API / Firebase / Supabase]
- Local storage: [Hive / Isar / SQLite]
- Auth: [Firebase Auth / JWT]
- Extra: [FCM / Google Maps / Camera / dst]

Jalankan workflow ini TANPA nunggu konfirmasi:

PHASE 1 — ARCHITECTURE
1. App architecture (feature-first / layer-first)
2. Data model + API contract
3. Screen flow diagram
4. Breakdown fitur + urutan pengerjaan
5. Simpan ke docs/architecture.md

PHASE 2 — PROJECT SETUP
1. Struktur folder
2. Base: AppTheme, AppRouter, AppColors, AppTypography
3. Network layer: Dio + interceptor + error handler + SSL pinning
4. flutter_secure_storage untuk token sensitif
5. State management setup
6. Auth flow skeleton
7. Obfuscation config di build
8. Simpan ke docs/setup.md

PHASE 3 — IMPLEMENT
Implement fitur pertama end-to-end:
- Data model + repository
- State/provider/bloc
- UI screens + widgets
- Navigation

Pastikan semua kode mengikuti Security Standards dan Code Quality Standards di CLAUDE.md.
Mulai sekarang.
```

---

### ── AI / ML PROJECT ─────────────────────────────────────────

```
[NEW PROJECT — AI/ML]

Project: [nama project]
Deskripsi: [apa yang dilakukan]
Problem type: [classification / NLP / computer vision / RAG / LLM agent / dst]

Input: [deskripsi data input]
Output: [deskripsi output yang diinginkan]

Tech Stack:
- Python
- Framework: [PyTorch / TensorFlow / HuggingFace / Scikit-learn]
- LLM: [Anthropic / OpenAI / Gemini / Ollama]
- Vector DB: [ChromaDB / FAISS / Pinecone] (jika RAG)
- Serving: [FastAPI / Gradio / Streamlit]
- Extra: [LangChain / LlamaIndex / dst]

Jalankan workflow ini TANPA nunggu konfirmasi:

PHASE 1 — ARCHITECTURE
1. ML system architecture (data flow diagram)
2. Model selection rationale
3. Pipeline breakdown (data → preprocess → train/infer → eval → serve)
4. Urutan pengerjaan
5. Simpan ke docs/architecture.md

PHASE 2 — PROJECT SETUP
1. Struktur folder (data/, models/, src/, notebooks/, api/)
2. requirements.txt / pyproject.toml
3. Config management (.env / yaml) — semua API key ke env
4. Data loading + EDA skeleton
5. Logging setup (bukan print)
6. Input validation (Pydantic) untuk serving endpoint
7. Simpan ke docs/setup.md

PHASE 3 — IMPLEMENT
Implement pipeline pertama end-to-end:
- Data preprocessing
- Model / chain / agent setup
- Training / inference
- Basic evaluation
- Serving endpoint dengan validation + rate limiting

Pastikan semua kode mengikuti Security Standards dan Code Quality Standards di CLAUDE.md.
Mulai sekarang.
```

---

### ── MICRO SAAS / SIDE PROJECT ──────────────────────────────

```
[NEW APP — MICRO SAAS]

App: [nama]
Tagline: [1 kalimat value proposition]
Target user: [siapa yang pakai]

Core features (MVP only):
- [fitur 1]
- [fitur 2]
- [fitur 3]

Tech Stack:
- Frontend: [Next.js / React / Vue]
- Backend: [Next.js API / Laravel / dst]
- Database: [Supabase / PlanetScale / Neon]
- Auth: [Clerk / NextAuth / Supabase Auth]
- Payment: [Stripe] (opsional)
- Deploy: [Vercel / Railway / Fly.io]
- Cost target: [gratis tier / < $X/bulan]

Jalankan workflow ini TANPA nunggu konfirmasi:

PHASE 1 — ARCHITECTURE
1. MVP scope (in/out tegas)
2. Tech stack justification (cost-optimized)
3. Data model minimal
4. API/route list
5. Breakdown + urutan pengerjaan
6. Simpan ke docs/architecture.md

PHASE 2 — PROJECT SETUP
1. Project init + folder structure
2. Database setup + schema
3. Auth setup
4. Base layout + design system minimal
5. Security headers + CORS config
6. Deploy config + env management
7. Simpan ke docs/setup.md

PHASE 3 — IMPLEMENT
Implement core feature pertama:
- Data layer
- Business logic
- UI

Pastikan semua kode mengikuti Security Standards dan Code Quality Standards di CLAUDE.md.
Mulai sekarang.
```

---

## 📐 General Coding Standards

### Universal
- No magic number → constant/enum/config
- Error handling eksplisit di setiap layer — no empty catch
- Naming deskriptif, spesifik, dan konsisten
- No dead code — hapus kode yang tidak dipakai
- No TODO tanpa tiket/issue reference
- Production-ready dari hari pertama, no placeholder
- Satu fungsi satu tanggung jawab (max 30 baris)
- Satu file satu concern (max 300 baris)
- DRY — duplikasi 2x → ekstrak ke fungsi/helper

### Komentar
- Kode harus self-explanatory — penamaan yang baik lebih baik dari komentar
- Komentar HANYA untuk: dokumentasi API publik, algoritma non-obvious (tulis KENAPA bukan APA), TODO dengan issue reference
- DILARANG komentar yang menjelaskan apa yang dilakukan kode (itu tugas penamaan)

### Android (Kotlin)
- MVVM + Repository, Coroutines + Flow
- Jetpack Compose untuk UI baru
- Offline-first: Room → WorkManager → API
- Edge-to-edge insets wajib

### Laravel
- PSR-12, Form Request untuk validation
- Policy untuk authorization
- Response wrapper: `{ success, data, message, errors }`

### Next.js / React
- TypeScript strict mode
- Functional component + hooks only
- Server Component default, Client Component kalau perlu interaktivitas

### ASP.NET Core
- Clean architecture, async/await semua endpoint
- FluentValidation, response wrapper konsisten

### Golang
- Error handling eksplisit, no panic di production
- Clean arch: handler → service → repository

### Flutter
- Null safety wajib
- Widget kecil (max ~50 baris)
- const constructor wherever possible

### Python / AI
- Type hints wajib di semua fungsi
- Pydantic untuk data model
- Logging bukan print

---

## ⚡ Quick Commands

```bash
# Model switching
/model claude-sonnet-4-6    # default harian
/model claude-opus-4-8      # complex task / security review
/model claude-fable-5       # architecture / last resort

# Mid-session prompts
"Lanjut ke fitur berikutnya dari breakdown"
"Review semua perubahan session ini — cek security + code quality"
"Gw stuck di [masalah], ini kodenya: [paste]"
"Security audit bagian ini: [paste]"
"Refactor bagian ini biar lebih clean: [paste]"
"Generate unit test untuk: [paste]"
"Explain arsitektur yang udah dibangun sejauh ini"
"Cek ada vulnerability di kode ini: [paste]"
```

---

# InvenScan — Commercial Inventory System Product

## Overview
Commercial source code product untuk dijual di CodeCanyon/Gumroad.
Dibangun ulang dari scratch berdasarkan real enterprise project (RFID Inventory Control System).
Target buyer: developer/enterprise yang punya hardware scanner (Zebra, Honeywell, Denso, dll).

**Package 1 — Android Only ($69-99)**
**Package 2 — Full Stack Android + ASP.NET Core MVC ($179-249)**

---

## Tech Stack

### Android
- Language: **Kotlin**
- Min SDK: 21, Target SDK: 34
- Pattern: **MVVM + Repository**
- DI: **Hilt**
- Async: **Coroutines + Flow**
- Local DB: **Room**
- Background Sync: **WorkManager**
- HTTP: **Retrofit + OkHttp**
- UI: **XML View-based** (bukan Compose — lebih compatible dengan HT device lama)

### Backend
- Framework: **ASP.NET Core MVC 8**
- Database: **SQL Server**
- ORM: **Entity Framework Core**
- Auth: **JWT Bearer**
- Pattern: **Controller → Service Interface → Service Implementation → DbContext**

---

## Abstract Scanner Interface — USP UTAMA PRODUCT INI

**SEMUA scanner interaction WAJIB melalui interface ini.**
**DILARANG ada SDK-specific import di luar class implementasi.**

```kotlin
// ScannerContract.kt
interface ScannerContract {
    fun initialize(context: Context, listener: ScanListener)
    fun startScan()
    fun stopScan()
    fun release()
    fun isReady(): Boolean

    interface ScanListener {
        fun onScanResult(code: String, type: ScanType)
        fun onScanError(message: String)
        fun onScannerDisconnected()
    }

    enum class ScanType { RFID, BARCODE }
}
```

```kotlin
// MockScanner.kt — DEFAULT, included di product untuk testing tanpa hardware
class MockScanner : ScannerContract {
    private var listener: ScannerContract.ScanListener? = null
    private var isRunning = false

    override fun initialize(context: Context, listener: ScannerContract.ScanListener) {
        this.listener = listener
    }

    override fun startScan() { isRunning = true }
    override fun stopScan() { isRunning = false }
    override fun release() { listener = null }
    override fun isReady() = true

    // Untuk testing: simulasi scan result
    fun simulateScan(code: String, type: ScannerContract.ScanType) {
        if (isRunning) listener?.onScanResult(code, type)
    }
}
```

```kotlin
// ScannerManager.kt — Singleton holder, hardware-agnostic
@Singleton
class ScannerManager @Inject constructor() {
    private var scanner: ScannerContract = MockScanner()

    fun setScanner(scanner: ScannerContract) {
        this.scanner = scanner
    }

    fun getScanner(): ScannerContract = scanner
    fun isReady(): Boolean = scanner.isReady()
}
```

**Dokumentasi untuk buyer:**
```
// Buyer implement untuk hardware mereka sendiri:
class ZebraScanner : ScannerContract { /* plug Zebra SDK */ }
class HoneywellScanner : ScannerContract { /* plug Honeywell SDK */ }
class DensoScanner : ScannerContract { /* plug Denso CommScanner */ }
```

---

## Database Schema (SQL Server — SIMPLIFIED dari original)

### Yang DIPERTAHANKAN dari project original
```
tb_User         → Users (auth)
tb_Role         → Roles
tb_User_Role    → UserRoles
tb_Item         → Items (master data)
tb_Location     → Locations
tb_Stock_Taking → StockTakings
tb_StockTakingDetail → StockTakingDetails
tb_Transaction  → Transactions (stock in history)
tb_TransactionDetail → TransactionDetails
```

### Yang DIHAPUS (terlalu domain-specific)
```
tb_DO           → Delivery Order (Sato-specific)
tb_DO_Detail    → DO Detail
tb_DO_Detail_Tag → DO Detail Tag
tb_Reader       → Impinj Reader config (hardware-specific)
tb_ReaderSettings → Reader settings
tb_HistoryPrint → Print history (SATO printer specific)
tb_Module       → Permission module (overkill untuk product)
tb_Permission   → Simplify ke role-based aja
tb_Role_Permission → Simplify
```

### Schema Baru yang Simplified

```sql
-- Users
tb_User: Id, UserId, FullName, PasswordHash, Role (ADMIN/OPERATOR), 
         IsActive, CreatedAt

-- Items (master data)  
tb_Item: Id, ItemCode, ItemName, Description, Unit, MinStock,
         CreatedBy, CreatedAt, UpdatedAt, IsDelete

-- Locations
tb_Location: Id, LocationCode, LocationName, Description,
             CreatedBy, CreatedAt, IsDelete

-- Tags/Items yang sudah di-register RFID-nya
tb_Tag: Id, TagId, EpcTag, ItemId(FK), LocationId(FK), 
        Status (IN_STOCK/OUT/UNKNOWN), CreatedAt, UpdatedAt

-- Stock Taking Session
tb_StockTaking: Id, SessionCode, Remark, Status (OPEN/CLOSED),
                CreatedBy, CreatedAt, ClosedAt

-- Stock Taking Detail (hasil scan per item)
tb_StockTakingDetail: Id, SttId(FK), TagId(FK), ItemId(FK),
                      Action (SYSTEM/SCAN/MISSING),
                      ScannedAt, CreatedBy

-- Stock In (penerimaan barang)
tb_StockIn: Id, DocNumber, LocationId(FK), Notes,
            CreatedBy, CreatedAt, Status (PENDING/SYNCED)

-- Stock In Detail
tb_StockInDetail: Id, StockInId(FK), TagId(FK), ItemId(FK),
                  ScannedCode, ScanType (RFID/BARCODE), CreatedAt

-- Stock Preparation / Picking List
tb_StockPrep: Id, DocNumber, Notes, Status (OPEN/IN_PROGRESS/DONE),
              CreatedBy, CreatedAt

-- Stock Prep Detail
tb_StockPrepDetail: Id, StockPrepId(FK), ItemId(FK), LocationId(FK),
                    RequestedQty, PickedQty, Status (PENDING/PICKED),
                    ScannedCode, CreatedBy, UpdatedAt
```

---

## Android — Screen List

```
1. LoginActivity
2. HomeActivity (dashboard menu utama)
3. StockInActivity
4. StockTakingActivity
5. StockTakingDetailActivity (scan per lokasi)
6. StockPrepActivity (list picking list)
7. StockPrepDetailActivity (scan item per picking list)
8. SearchItemActivity
9. SettingsActivity (server URL, device ID, scanner type)
```

### Flow per Feature (dari original project — dipertahankan logicnya)

**Stock In:**
```
Pilih lokasi → Switch RFID/Barcode → Scan item
→ Item muncul di list (resolve dari server atau cache)
→ Save → offline queue di Room → WorkManager sync
```

**Stock Taking:**
```
Cek active session dari server → Kalau ada, join session
→ Scan semua item → item match/missing/unknown
→ Submit hasil ke server
→ Offline: queue di tb_scan_queue, sync saat online
```

**Stock Preparation (Picking List):**
```
Fetch list dari server → Pilih DO/PickingList
→ Scan item satu per satu → compare dengan requested
→ Submit picked quantity → sync ke server
→ Offline: queue di tb_pending_submit
```

**Search Item:**
```
Scan barcode/RFID → Lookup ke cache lokal dulu
→ Kalau miss, hit API → Tampil detail item + lokasi + status
```

---

## Android — Room Entities yang Dibutuhkan

```kotlin
// Sama dengan original, tapi rename + simplify:
ScanQueueEntity     → untuk offline stock taking queue
PendingSubmitEntity → untuk offline stock prep queue  
StockInScanEntity   → untuk offline stock in queue
SearchItemEntity    → cache item untuk search
TagCacheEntity      → cache tag lookup (EPC → item info)
AppLogEntity        → activity log (pertahankan dari original)
```

---

## Backend — Controller List

```
AuthController          → POST /api/auth/login, /api/auth/refresh
ItemApiController       → GET /api/item, /api/item/{id}
LocationApiController   → GET /api/location
TagApiController        → GET /api/tag/{id}, POST /api/tag/register
StockInController       → GET/POST /api/stockin, POST /api/stockin/bulk-info
StockTakingController   → GET/POST /api/stock-taking, /api/stock-taking/active
                          GET /api/stock-taking/tags/{id}
                          POST /api/stock-taking/operator-submit
StockPrepController     → GET /api/stockprep, POST /api/stockprep/bulk
                          GET /api/stockprep/{id}
SearchItemController    → GET /api/search-item, /api/search-item/{code}
UserApiController       → GET/POST/PUT /api/user (admin only)
PingController          → GET /api/ping (health check)
```

### Web Controllers (dashboard admin)
```
HomeController (Web)         → Dashboard summary
ItemWebController            → CRUD items
LocationWebController        → CRUD locations  
StockTakingWebController     → List + detail + close session
StockPrepWebController       → Create picking list + detail
TransactionWebController     → Stock in history
UserWebController            → Manage users
AuthWebController            → Login web
```

---

## Offline-First Rules (dari original — pertahankan)

```
1. Semua transaksi WAJIB bisa dilakukan tanpa internet
2. Room DB = source of truth untuk data lokal
3. WorkManager sync tiap 15 menit + trigger saat koneksi ada
4. Conflict resolution:
   - Master data (item, location, tag): server wins
   - Transaksi (scan results): device wins, queue retry kalau gagal
5. SyncStatus: PENDING → SYNCED / FAILED → RETRY
```

---

## Hal yang DIUBAH dari Original

### Android
```
Java → Kotlin (rewrite penuh)
Direct API call di Activity → ViewModel + Repository pattern
ScannerManager (Denso hardcoded) → Abstract ScannerContract
No DI → Hilt
Callback-based network → Coroutines + Flow
```

### Backend
```
HAPUS: ImpinjHelper (Impinj RFID reader — hardware specific)
HAPUS: PrinterHelper (SATO label printer — hardware specific)
HAPUS: DO/DeliveryOrder flow (Sato domain specific)
HAPUS: Reader/ReaderSettings entity
HAPUS: HistoryPrint entity
HAPUS: Module/Permission granular → simplify ke Role (ADMIN/OPERATOR)
PERTAHANKAN: Service Interface pattern
PERTAHANKAN: JWT auth
PERTAHANKAN: DailyFileLogger
PERTAHANKAN: DTO pattern
```

---

## Code Quality Rules — WAJIB karena ini COMMERCIAL PRODUCT

```
Android:
- Semua ViewModel pakai StateFlow/LiveData untuk UI state
- Setiap screen: Loading state, Empty state, Error state, wajib ada
- No hardcoded strings → strings.xml
- No hardcoded URLs → BuildConfig atau SettingsActivity
- KDoc comment di semua public method
- Error handling di semua network call + Room operation

Backend:
- XML docs comment di semua public method Controller + Service
- Try-catch di semua Service implementation
- Return proper HTTP status codes (200, 201, 400, 401, 403, 404, 500)
- No business logic di Controller — semua di Service layer
- No raw SQL — pakai LINQ/EF query
```

---

## Packaging untuk Dijual

```
/android          → Android Studio project (Kotlin)
/backend          → ASP.NET Core MVC 8 project
/docs
  README.md                    → Setup guide lengkap
  SCANNER_INTEGRATION.md       → Cara implement ScannerContract
                                  untuk Zebra, Honeywell, Denso
  API_DOCUMENTATION.md         → Semua endpoint + request/response
  DATABASE_SETUP.md            → Migration + seed script
/postman
  InvenScan.postman_collection.json
/sql
  migration_script.sql
  seed_data.sql
/screenshots
  android/                     → Screenshot semua screen
  web/                         → Screenshot web dashboard
```

---

## 5-Day Execution Plan

```
Day 1 → Backend: Setup project + DB schema + Auth + Item + Location API
Day 2 → Backend: StockIn + StockTaking + StockPrep + Search API + Web Controllers
Day 3 → Android: Project setup + Hilt + Room + Retrofit + ScannerContract + WorkManager
Day 4 → Android: Semua Activity/ViewModel + UI (semua screen)
Day 5 → Integration test + Bug fix + Dokumentasi + Packaging
```

---

## Daily Prompts

### Day 1 — Backend Foundation
```
Buat ASP.NET Core MVC 8 project bernama "InvenScan" sesuai CLAUDE.md.

Task:
1. Setup project structure:
   Controllers/, Controllers/Web/, Service/Interfaces/, 
   Service/Implementations/, Entity/, DTO/, Database/, 
   Utility/, Models/, Routes/, Views/

2. Buat semua Entity sesuai simplified schema di CLAUDE.md:
   User, Role, UserRole, Item, Location, Tag, 
   StockTaking, StockTakingDetail, StockIn, StockInDetail,
   StockPrep, StockPrepDetail

3. Setup AppDBContext dengan semua relasi dan index

4. Setup EF Core + SQL Server di Program.cs
   Connection string via appsettings.json

5. Buat Initial Migration + Seeder:
   - 2 roles: ADMIN, OPERATOR
   - 1 default admin user (admin/admin123)
   - 5 sample items
   - 3 sample locations

6. Implement JWT Auth:
   - JwtTokenHelper.cs (generate + validate)
   - AuthController: POST /api/auth/login → return JWT token
   - Middleware JWT validation di Program.cs

7. Implement ItemApiController + IItemService + ItemService:
   GET /api/item → list semua item (non-deleted)
   GET /api/item/{id} → detail item

8. Implement LocationApiController + ILocationService + LocationService:
   GET /api/location → list semua lokasi

9. GET /api/ping → health check (no auth required)

10. Setup Routes/Api.cs untuk route mapping

Deliver: dotnet run berhasil, login API working, item + location API working.
Test dengan curl atau Postman.
```

### Day 2 — Backend Features + Web Dashboard
```
Lanjut InvenScan backend sesuai CLAUDE.md. Backend sudah running dari Day 1.

Task:
1. TagApiController + ITagService + TagService:
   GET /api/tag/{id} → detail tag by EPC atau TagId
   POST /api/tag/register → register list tag baru

2. StockInController + IStockInService + StockInService:
   GET /api/stockin?code={code}&scannerType={type} → lookup tag by scan
   POST /api/stockin → submit stock in (single)
   POST /api/stockin/bulk-info → lookup multiple tags sekaligus

3. StockTakingController + IStockTakingService + StockTakingService:
   POST /api/stock-taking → create session baru
   GET /api/stock-taking → list semua session
   GET /api/stock-taking/active → get active session (status OPEN)
   GET /api/stock-taking/tags/{sttId} → get session items
   GET /api/stock-taking/available-tags/{sttId} → tags available untuk session
   POST /api/stock-taking/operator-submit → submit scan results dari HT

4. StockPrepController + IStockPrepService + StockPrepService:
   GET /api/stockprep → list picking list (status OPEN/IN_PROGRESS)
   GET /api/stockprep/{id} → detail picking list
   POST /api/stockprep/bulk → submit picked items dari HT

5. SearchItemController:
   GET /api/search-item → list semua item + tag info untuk cache
   GET /api/search-item/{code} → detail by barcode atau EPC

6. Web Controllers + Views (pakai Bootstrap 5 + DataTables):
   - AuthWebController → Login page
   - HomeController → Dashboard (summary card: total item, total lokasi, 
     active stock taking, pending prep)
   - ItemWebController → CRUD items (DataTables)
   - LocationWebController → CRUD lokasi
   - StockTakingWebController → List session, detail, close session
   - StockPrepWebController → Create picking list, list, detail
   - UserWebController → Manage users (admin only)

7. Buat Routes/Api.cs dan Routes/Web.cs untuk semua route

8. Export Postman collection semua API endpoint

Deliver: Semua API working, web dashboard bisa login + navigate semua menu.
```

### Day 3 — Android Foundation
```
Buat Android project "InvenScan" di Kotlin sesuai CLAUDE.md.

Task:
1. Setup project baru:
   - Package: com.invenscan.app
   - Min SDK 21, Target SDK 34
   - Tambah dependencies: Hilt, Retrofit, OkHttp, Room, 
     WorkManager, Coroutines, Glide, Material Components

2. Setup Hilt di Application class + semua module:
   - NetworkModule (Retrofit, OkHttp dengan JWT interceptor)
   - DatabaseModule (Room AppDatabase)
   - RepositoryModule

3. Implement ScannerContract interface + MockScanner sesuai CLAUDE.md
   Setup ScannerManager singleton via Hilt

4. Buat Room Database:
   - AppDatabase.kt
   - Semua Entity: ScanQueueEntity, PendingSubmitEntity, 
     StockInScanEntity, SearchItemEntity, TagCacheEntity, AppLogEntity
   - AppDao.kt dengan semua query (mirror dari original Java DAO)

5. Setup Retrofit:
   - ApiService.kt (semua endpoint sesuai CLAUDE.md)
   - AuthInterceptor.kt (inject Bearer token otomatis)
   - ApiClient.kt

6. Buat semua data class / model:
   - AuthModel, ItemModel, LocationModel, TagModel,
     StockTakingModel, StockPrepModel

7. Implement WorkManager SyncWorker:
   - Sync PendingSubmitEntity (stock prep offline)
   - Sync StockInScanEntity (stock in offline)
   - Retry logic: Result.retry() kalau gagal

8. PrefManager.kt (SharedPreferences wrapper):
   token, userId, serverUrl, deviceId

9. Setup base classes:
   - BaseActivity.kt
   - BaseViewModel.kt
   - Resource.kt (sealed class: Loading, Success, Error)

Deliver: Project compile sukses, Room database created, 
WorkManager terdaftar, MockScanner bisa simulasi scan.
```

### Day 4 — Android UI (Semua Screen)
```
Lanjut Android InvenScan sesuai CLAUDE.md. Foundation sudah selesai dari Day 3.
Gunakan MockScanner untuk semua testing — JANGAN ada SDK hardware spesifik.

Buat semua Activity + ViewModel + Layout XML:

1. LoginActivity + LoginViewModel
   - Input: server URL, username, password
   - Validate → hit API → save token → navigate ke HomeActivity

2. HomeActivity + HomeViewModel  
   - Grid menu: Stock In, Stock Taking, Stock Prep, Search Item, Settings
   - Username display + logout button
   - Bottom atau top app bar

3. StockInActivity + StockInViewModel
   - Spinner: pilih lokasi
   - Switch: RFID / Barcode mode
   - Tombol Start/Stop scan (pakai ScannerContract)
   - RecyclerView: list item yang ter-scan
   - Resolve item dari cache (TagCacheEntity) atau API
   - Save ke StockInScanEntity → WorkManager sync
   - Loading, empty, error state wajib ada

4. StockTakingActivity + StockTakingViewModel
   - Fetch active session dari API
   - Kalau tidak ada: tampil "Tidak ada sesi aktif"
   - Kalau ada: tampil info sesi + tombol mulai scan
   - Navigate ke StockTakingDetailActivity

5. StockTakingDetailActivity + StockTakingDetailViewModel
   - Scan mode (MockScanner)
   - Real-time counter: Found / Missing / Unknown
   - List item scan result
   - Tombol Submit → POST ke API
   - Offline queue ke ScanQueueEntity kalau gagal

6. StockPrepActivity + StockPrepViewModel
   - Fetch list picking list dari API
   - RecyclerView list picking list
   - Tap item → navigate ke StockPrepDetailActivity

7. StockPrepDetailActivity + StockPrepDetailViewModel  
   - Tampil list item yang harus dipick
   - Scan item → match dengan requested list
   - Update picked qty
   - Submit → POST ke API
   - Offline queue ke PendingSubmitEntity kalau gagal

8. SearchItemActivity + SearchItemViewModel
   - Scan barcode/RFID (MockScanner)
   - Lookup ke SearchItemEntity cache dulu
   - Kalau miss → hit API /api/search-item/{code}
   - Tampil: nama item, kode, lokasi, status, qty

9. SettingsActivity
   - Input server URL (simpan ke PrefManager)
   - Device ID (auto-generate UUID, bisa diedit)
   - Scanner type selector (Mock/Zebra/Honeywell/Denso)
     → untuk dokumentasi buyer, bukan actual SDK
   - App version info

Semua screen WAJIB punya:
- Loading state (ProgressBar atau Shimmer)
- Empty state (ilustrasi + teks)
- Error state (snackbar atau dialog dengan retry button)

Deliver: Semua screen bisa dinagivasi end-to-end dengan MockScanner.
Flow lengkap: Login → Home → tiap feature → back ke Home.
```

### Day 5 — Integration, Bug Fix, Dokumentasi, Packaging
```
Finalisasi InvenScan product sesuai CLAUDE.md.

Task:
1. Integration test end-to-end:
   - Android connect ke backend lokal
   - Test flow: Login → Stock In → submit → cek di web dashboard
   - Test flow: Stock Taking → scan → submit → cek hasil di web
   - Test flow: Stock Prep → scan pick → submit → status berubah
   - Test flow: Search Item → scan → tampil detail
   - Test offline mode: matikan server → scan → nyalakan → auto sync

2. Fix semua bug yang ditemukan

3. Buat dokumentasi:

   README.md:
   - Requirements (Android Studio, .NET 8, SQL Server)
   - Backend setup (clone, connection string, migration, run)
   - Android setup (clone, change server URL, build, run)
   - Default credentials

   SCANNER_INTEGRATION.md:
   - Penjelasan ScannerContract interface
   - Step by step cara implement untuk:
     * Zebra (contoh class ZebraScanner)
     * Honeywell (contoh class HoneywellScanner)
     * Denso (contoh class DensoScanner)
   - Cara register scanner di Hilt module
   - Cara switch scanner di runtime

   API_DOCUMENTATION.md:
   - Semua endpoint: method, URL, request body, response
   - Auth flow
   - Error codes

4. Export Postman collection final

5. Buat SQL seed script final (dengan sample data yang proper)

6. Screenshot semua screen:
   Android: Login, Home, StockIn, StockTaking, StockPrep, SearchItem, Settings
   Web: Dashboard, Items, Locations, StockTaking, StockPrep, Users

7. Zip packaging:
   /invenscan-android/    → Android project
   /invenscan-backend/    → ASP.NET Core project  
   /docs/                 → Semua dokumentasi
   /postman/              → Collection
   /sql/                  → Migration + seed
   /screenshots/          → Semua screenshot

Deliver: Product siap dijual. Semua fitur working, dokumentasi lengkap,
packaging rapi sesuai standar CodeCanyon.
```

---

## Notes Penting

```
1. INI COMMERCIAL PRODUCT — kode harus clean, well-documented,
   production-ready. Buyer adalah developer profesional.

2. Abstract ScannerContract adalah USP utama — jangan pernah ada
   SDK-specific code yang bocor ke luar implementasi class.

3. UI harus simple dan functional — bukan fancy.
   HT device layarnya kecil, operatornya pakai sarung tangan.
   Tombol besar, font readable, contrast tinggi.

4. Jangan copy code dari project internship — rewrite dari scratch
   berdasarkan logic dan arsitektur yang sama.

5. MockScanner WAJIB berfungsi penuh untuk demo dan testing
   tanpa hardware apapun.
```
