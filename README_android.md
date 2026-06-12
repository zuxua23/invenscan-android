<p align="center">
  <img src="https://img.shields.io/badge/Android-21%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Kotlin-1.9-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Hilt-DI-2196F3?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Room-DB-FF6F00?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Offline--First-✓-1D9E75?style=for-the-badge" />
</p>

<h1 align="center">InvenScan Android</h1>
<p align="center">Enterprise inventory management app for Android handheld terminals</p>
<p align="center">by <a href="https://github.com/zuxua23">Zuxlabs</a></p>

---

## Overview

InvenScan Android is a production-ready inventory management app designed for industrial Android handheld terminals (HT). It works fully offline and syncs automatically when internet is available.

### Key Features

- **Offline-First** — all operations work without internet, syncs via WorkManager
- **Abstract Scanner Interface** — plug in any RFID/barcode SDK (Zebra, Honeywell, Denso, etc.)
- **Camera Barcode Backup** — scan via camera if no hardware scanner available
- **Stock In** — receive items with RFID or barcode scanning
- **Stock Out** — record items leaving warehouse
- **Stock Taking** — physical inventory count with real-time found/missing/unknown counter
- **Stock Preparation** — picking list execution with progress tracking
- **Item Search** — instant lookup by scan with local cache
- **Tag Registration** — register and re-register RFID tags
- **RFID Settings** — configure trigger, power, session, Q factor
- **Battery Monitor** — display HT and RFID reader battery (separate or built-in)
- **Dark/Light Theme** — per-user theme preference
- **Activity Logging** — all actions logged and synced to server
- **Custom Dialogs & Toasts** — fully branded UI components

---

## Requirements

| Requirement | Minimum |
|-------------|---------|
| Android OS | 5.0 (API 21) |
| Android Studio | Hedgehog 2023.1.1+ |
| Kotlin | 1.9+ |
| Gradle | 8.0+ |
| InvenScan Backend | Running and accessible |

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/zuxua23/invenscan-android.git
```

### 2. Open in Android Studio

File → Open → select `invenscan-android` folder

### 3. Configure server URL

On first launch, go to **Settings** and enter your backend URL:

```
https://your-server-address:5001
```

Or set it directly in `PrefManager` defaults:
```kotlin
// app/src/main/java/com/invenscan/app/util/PrefManager.kt
const val DEFAULT_SERVER_URL = "https://your-server-address:5001"
```

### 4. Build and run

```bash
./gradlew assembleDebug
```

Or press **Run** in Android Studio.

---

## Default Credentials

Same as backend:

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| Operator | `operator` | `operator123` |

---

## Project Structure

```
app/src/main/java/com/invenscan/app/
├── InvenScanApp.kt              # Application class (Hilt)
├── scanner/
│   ├── ScannerContract.kt       # Abstract scanner interface ← KEY FILE
│   ├── ScannerManager.kt        # Singleton scanner holder
│   └── MockScanner.kt           # Default scanner for testing
├── ui/
│   ├── login/                   # LoginActivity + ViewModel
│   ├── home/                    # HomeActivity + ViewModel
│   ├── stockin/                 # StockInActivity + ViewModel
│   ├── stockout/                # StockOutActivity + ViewModel
│   ├── stocktaking/             # StockTakingActivity + Detail
│   ├── stockprep/               # StockPrepActivity + Detail
│   ├── search/                  # SearchItemActivity + ViewModel
│   ├── tagreg/                  # TagRegistrationActivity
│   ├── rfid/                    # RfidSettingsActivity
│   ├── settings/                # SettingsActivity
│   └── camera/                  # CameraActivity (barcode backup)
├── data/
│   ├── model/                   # Data classes / DTOs
│   ├── local/                   # Room entities + DAOs
│   ├── remote/                  # Retrofit API service
│   └── repository/              # Repository implementations
├── di/                          # Hilt modules
├── util/
│   ├── PrefManager.kt           # SharedPreferences wrapper
│   ├── AppLogger.kt             # Activity logging
│   ├── CustomDialog.kt          # Custom dialog component
│   ├── CustomToast.kt           # Custom toast component
│   └── WorkManagerUtil.kt       # WorkManager helpers
└── worker/
    └── SyncWorker.kt            # Background sync worker
```

---

## Scanner Integration Guide

This is the most important part for buyers. InvenScan uses an abstract `ScannerContract` interface — you only need to implement this interface for your hardware SDK.

### ScannerContract Interface

```kotlin
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

### Example: Zebra SDK Integration

```kotlin
class ZebraScanner : ScannerContract {
    private var emdk: EMDKManager? = null
    private var barcodeManager: BarcodeManager? = null
    private var listener: ScannerContract.ScanListener? = null

    override fun initialize(context: Context, listener: ScannerContract.ScanListener) {
        this.listener = listener
        EMDKManager.getEMDKManager(context, object : EMDKManager.EMDKListener {
            override fun onOpened(manager: EMDKManager) {
                emdk = manager
                barcodeManager = manager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE) 
                    as BarcodeManager
                // setup scanner here
            }
            override fun onClosed() {}
        })
    }

    override fun startScan() {
        // enable scanner
    }

    override fun stopScan() {
        // disable scanner
    }

    override fun release() {
        emdk?.release()
        listener = null
    }

    override fun isReady(): Boolean = barcodeManager != null
}
```

### Example: Honeywell SDK Integration

```kotlin
class HoneywellScanner : ScannerContract {
    private var scanner: AidcManager? = null
    private var barcodeReader: BarcodeReader? = null
    private var listener: ScannerContract.ScanListener? = null

    override fun initialize(context: Context, listener: ScannerContract.ScanListener) {
        this.listener = listener
        AidcManager.create(context) { manager, _ ->
            scanner = manager
            barcodeReader = manager.createBarcodeReader()
            barcodeReader?.addBarcodeListener { barcodeReadEvent ->
                listener.onScanResult(
                    barcodeReadEvent.barcodeData ?: "",
                    ScannerContract.ScanType.BARCODE
                )
            }
        }
    }

    override fun startScan() { barcodeReader?.aim(true); barcodeReader?.decode(true) }
    override fun stopScan() { barcodeReader?.aim(false); barcodeReader?.decode(false) }
    override fun release() { barcodeReader?.close(); scanner?.close(); listener = null }
    override fun isReady(): Boolean = barcodeReader != null
}
```

### Example: Denso SDK Integration

```kotlin
class DensoScanner : ScannerContract {
    private var commScanner: CommScanner? = null
    private var listener: ScannerContract.ScanListener? = null

    override fun initialize(context: Context, listener: ScannerContract.ScanListener) {
        this.listener = listener
        // Connect via Bluetooth or USB
        // Refer to Denso SP1 / BHT-M80 SDK documentation
    }

    override fun startScan() {
        commScanner?.startScan()
    }

    override fun stopScan() {
        commScanner?.stopScan()
    }

    override fun release() {
        commScanner?.disconnect()
        listener = null
    }

    override fun isReady(): Boolean = commScanner?.isConnected == true
}
```

### Register your scanner with Hilt

```kotlin
// di/ScannerModule.kt
@Module
@InstallIn(SingletonComponent::class)
object ScannerModule {

    @Provides
    @Singleton
    fun provideScannerContract(): ScannerContract {
        // Replace MockScanner with your implementation:
        return ZebraScanner()       // for Zebra
        // return HoneywellScanner() // for Honeywell
        // return DensoScanner()     // for Denso
        // return MockScanner()      // for testing without hardware
    }
}
```

---

## Offline-First Architecture

InvenScan stores all scan data locally in Room DB first, then syncs to the backend automatically.

```
Scan item
    ↓
Save to Room DB (PENDING status)
    ↓
Try submit to API
    ↓ success          ↓ failed (no network)
Update status       Leave as PENDING
to SYNCED           ↓
                WorkManager retries
                every 15 minutes
                or when network available
```

### Sync Worker

The `SyncWorker` runs in background and handles:
- Stock In pending submissions
- Stock Out pending submissions
- Stock Taking pending submissions
- Stock Prep pending submissions
- Activity log pending uploads

---

## RFID Settings

Configure RFID reader behavior in **RFID Settings** screen:

| Setting | Options | Default |
|---------|---------|---------|
| Trigger Mode | Continuous / Key Press / Auto | Continuous |
| Default Power | 5 – 30 dBm | 27 dBm |
| Sensitivity | 1 – 10 | 7 |
| Session | S0 / S1 / S2 / S3 | S1 |
| Q Factor | 0 – 15 | 4 |

> These settings are stored in PrefManager. Apply them in your SDK implementation using the values from PrefManager.

---

## Battery Display

InvenScan supports two battery display modes. Configure in Settings:

**Mode A — Separate HT + RFID Reader (e.g., Denso BHT-M80 + SP1)**
- Shows two battery indicators in toolbar
- Implement `BatteryProvider` in your scanner class

**Mode B — Built-in RFID (single device)**
- Shows one battery indicator
- Standard Android `BatteryManager` API

```kotlin
// Implement in your scanner class:
interface BatteryProvider {
    fun getHtBattery(): Int          // 0-100
    fun getRfidBattery(): Int?       // null if not applicable
}
```

---

## Camera Barcode Scanning

Every scan screen has a FAB (floating action button) for camera-based barcode scanning as backup. Uses CameraX + ML Kit.

Supported barcode formats: QR Code, Code 128, Code 39, EAN-13, EAN-8, UPC-A, UPC-E, Data Matrix, PDF417.

Features:
- Full-screen camera preview
- Scan overlay frame
- Flash toggle (on/off)
- Auto-focus

---

## Tag Re-registration

RFID tags can be recycled and assigned to new items:

1. Go to **Tag Registration**
2. Scan a tag with status `OUT`
3. System detects previously used tag
4. Confirm re-registration
5. Tag status resets to `AVAILABLE`
6. Assign to a new item

---

## Tech Stack

| Component | Library |
|-----------|---------|
| Language | Kotlin |
| DI | Hilt |
| Local DB | Room |
| HTTP | Retrofit + OkHttp |
| Background sync | WorkManager |
| Camera | CameraX + ML Kit |
| Image loading | Glide |
| Async | Coroutines + Flow |
| Security | EncryptedSharedPreferences |

---

## Build Variants

```bash
./gradlew assembleDebug    # Debug build
./gradlew assembleRelease  # Release build (requires signing config)
```

For release build, configure signing in `build.gradle.kts`:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("your-keystore.jks")
        storePassword = "your-store-password"
        keyAlias = "your-key-alias"
        keyPassword = "your-key-password"
    }
}
```

---

## License

This is a commercial product by **Zuxlabs**. All rights reserved.

Purchased licenses include:
- **Regular License** — use in a single end product
- **Extended License** — use in multiple end products or SaaS

---

## Support

- GitHub Issues: [invenscan-android/issues](https://github.com/zuxua23/invenscan-android/issues)
- Email: support@zuxlabs.dev
