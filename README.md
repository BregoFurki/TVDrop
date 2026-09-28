# TVDrop — Send files and APKs to Android TV

TVDrop lets you transfer files from a phone or computer to an Android TV on the same local network. Scan the QR code shown on the TV, then upload through a browser. No companion phone app or account is needed.

## Features

- Send any file type up to 2 GB per file. APK, video, audio, image, subtitle, document and archive files are supported. Opening a file on the TV requires a suitable app.
- Install a received APK using Android's system installer after granting the required permission.
- Browse, open and delete received files using the TV remote.
- TV and phone interfaces each follow their own device language automatically, with separate manual language selectors. Available languages: English, Turkish, Brazilian Portuguese, Simplified Chinese, Japanese, German, Indonesian, Russian, Spanish and French.
- Files are saved in `Download/TVDrop` when storage permission allows it; otherwise they are saved in the app's private `TVDrop` folder.

## Install and use

Download the signed APK from [GitHub Releases](../../releases), copy it to your Android TV and install it. Open TVDrop, scan the QR code with your phone, and choose a file. Both devices must be on the same local network. Use the compact language selector on the TV or at the top right of the web page to override automatic language detection.

Transfers use unencrypted HTTP on the local network; use TVDrop only on networks you trust. The TV needs roughly three times the uploaded file's size in free space during transfer. The server creates a new pairing token when its service restarts, so an old QR link can expire.

## Build

Requirements: JDK 17 and Android SDK 34. To build a local test APK, run `./gradlew assembleDebug`. The output is `app/build/outputs/apk/debug/app-debug.apk`.

For a distributable APK, create `release-signing/tvdrop-release.jks` and `release-signing/signing.properties` with `storePassword`, `keyAlias`, and `keyPassword`. Run `./gradlew assembleRelease`. The signing directory is excluded from Git. **Back up the keystore and password securely:** Android updates must use the same signing key. Never publish a debug APK or the signing files. Use version `v1.1.1` for the release tag and attach the signed APK and its SHA-256 checksum.

Licensed under [MIT](LICENSE). The Turkish project notes follow below.

---

# TVDrop - Android TV Yerel Ağ (LAN) Dosya & APK Yöneticisi

**TVDrop**, Android TV kullanıcılarının telefon veya bilgisayarlarından televizyonlarına aynı yerel ağ üzerinden, telefona ek uygulama kurmadan dosya ve APK aktarmasını sağlayan bir Android TV aracıdır.

---

## 🌟 Öne Çıkan Özellikler

1. **Sıfır İstemci Kurulumu (Zero-Install):**
   * TV ekranında beliren **QR kodu** telefon veya tablet kamerasıyla taratmak yeterlidir. Kod, o servis oturumuna özel bir yükleme anahtarı içerir.
   * Telefondaki standart web tarayıcısında açılan şık sürükle-bırak paneli üzerinden dosyalar doğrudan TV'ye akar.
2. **Akıllı APK Ayrıştırıcı & 1-Tık Kurulum:**
   * TV'ye gelen `.apk` dosyalarının uygulama simgesi, adı ve sürüm numarası okunur.
   * TV kumandasından sistem kurulum ekranı başlatılır (`FileProvider`). Kurulum için Android'in bilinmeyen uygulama izni ve kullanıcı onayı gerekebilir.
3. **Depolama:**
   * İzin verilirse desteklenen Android sürümlerinde dosyalar ortak `Download/TVDrop` klasörüne kaydedilir. Android 10'da ve ortak depolama izni verilmediğinde uygulamaya özel `TVDrop` klasörü kullanılır.
   * İzin sonradan verilse de uygulamaya özel klasördeki önceki dosyalar listelenmeye devam eder.
4. **Kumanda (D-Pad) Odaklı Arayüz:**
   * QR alanı odaklandığında genişler; OK ile QR tam ekran açılır. Dosyalara geçildiğinde listeye daha çok yer açılır.
   * TV'nin ağ adresi değiştiğinde bağlantı adresi ve QR kod otomatik güncellenir.
5. **Arka Plan Servisi (Foreground Service):**
   * Servis arka planda çalışır; kısmi CPU uyandırma kilidini yalnızca aktif aktarımda, en fazla iki saat kullanır. TV'nin ekranı veya ağ bağlantısı için kesintisiz çalışma garantisi vermez.
6. **Dil Desteği:**
   * TV ve telefon kendi cihaz dillerini kullanır. İkisinde de elle dil seçilebilir.

## Güvenlik ve sınırlar

* Yükleme için QR kodundaki oturum anahtarı gerekir; anahtar servis yeniden başladığında değişir. Yükleme adları doğrulanır ve aynı adlı dosyalar ayrı kaydedilir.
* Yerel bağlantı HTTP kullanır ve şifreli değildir. Uygulamayı yalnızca güvendiğiniz ağlarda kullanın.
* NanoHTTPD'nin çok parçalı yükleme işleme biçimi nedeniyle aktarım önce geçici dosyalara alınır, ardından hedef klasöre kopyalanır. Yaklaşık üç dosya boyutu kadar boş alan gerekir. Web arayüzü dosya başına 2 GB sınırı uygular; sunucunun istek üst sınırı 2 GiB'nin biraz altındadır.
* Telefondaki ilerleme çubuğu ağ gönderimini, TV'deki ilerleme çubuğu geçici dosyanın hedefe kaydedilmesini gösterir.

---

## 📁 Proje Mimarisi

* `app/src/main/assets/web/index.html`: Telefondan bağlanan kullanıcıya sunulan yerel web yükleme sayfası.
* `app/src/main/java/com/tvdrop/app/server/`:
  * `TVHttpServer.kt`: TV üzerinde çalışan hafif HTTP streaming sunucusu (NanoHTTPD).
  * `TransferService.kt`: Aktarımı arka planda canlı tutan Foreground Service.
* `app/src/main/java/com/tvdrop/app/utils/`:
  * `NetworkUtils.kt`: Yerel Wi-Fi / Ethernet IPv4 tespit motoru.
  * `QrCodeGenerator.kt`: ZXing ile dinamik TV QR kod üretimi.
  * `ApkParser.kt`: Kurulum öncesi APK ikon, başlık ve sürüm okuyucu.
  * `PackageInstallerHelper.kt`: Güvenli APK kurulum ve dosya açma yöneticisi.
  * `StorageUtils.kt`: Depolama klasörü (`/Download/TVDrop`) ve izin kontrolcüsü.
* `app/src/main/java/com/tvdrop/app/ui/`:
  * `MainActivity.kt`: Kumanda kontrollü ana kontrol paneli.
  * `TransferAdapter.kt`: D-Pad animasyonlu aktarım listesi.

---

## 🚀 Derleme ve Test

Hata ayıklama APK'sı derlemek için:
```bash
./gradlew assembleDebug
```
Çıktı konumu: `app/build/outputs/apk/debug/app-debug.apk`

Cihaza veya emülatöre yüklemek için:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

GitHub Releases için hata ayıklama APK’sı yerine aynı anahtarla imzalanmış sürüm APK’sı kullanılmalıdır. İmzalama dosyaları `release-signing/` klasöründedir ve Git’e eklenmez.
