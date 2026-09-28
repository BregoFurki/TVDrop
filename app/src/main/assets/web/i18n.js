// Keep keys in the same order in every language. The TV and phone choose languages independently.
(() => {
    const keys = `pageTitle hero1 hero2 hero3 uploadTitle checking uploadArea dropFiles chooseFiles typesLabel typesBody limitLabel limitBody transferProgress preparing file footer languageLabel autoLanguage busy oversized pairRequired scanToSend oneComplete manyComplete connected transferFailed sentToTv pairExpired tooLargeOrSpace anotherTransfer pairAgain stopped disconnected connectionLost connectionRetry tvNoResponse`.split(' ');
    const rows = {
        en: [
            'TVDrop — Send files to your TV', 'Choose.', 'Send.', 'On TV.',
            'Send files to your TV', 'Checking connection', 'File upload area', 'Drop files here', 'Choose files',
            'Types', 'Any file type, including APK, MP4, MKV, AVI, MP3, WAV, FLAC, JPG, PNG, WEBP, PDF, TXT, SRT, ASS and ZIP. A suitable app is needed to open it on TV.',
            'Limit', 'Up to 2 GB per file. The TV needs about three times the file size in free space.',
            'Transfer progress', 'Preparing', 'File', 'Transfers use HTTP on your local network. Use only on networks you trust.',
            'Language', 'Auto (phone language)', 'A transfer is in progress. Choose another file when it finishes.',
            '“{0}” exceeds the 2 GB limit.', 'Pairing required: scan the QR code on your TV.', 'Scan the QR code on your TV to send files.',
            'File sent to TV.', '{0} files sent to TV.', 'Connected to TV', 'Transfer failed.', 'Sent to TV',
            'Pairing expired. Scan the QR code on your TV again.', 'The 2 GB limit was exceeded or the TV has insufficient free space.',
            'Another transfer is in progress.', 'Pair again', 'Transfer stopped', 'TV disconnected', 'Connection lost',
            'Connection to TV lost. Check your network and try again.', 'TV did not respond.'
        ],
        tr: [
            'TVDrop — TV’ye dosya gönder', 'Seç.', 'Gönder.', 'TV’nde.',
            'TV’ye dosya gönder', 'Bağlantı kontrol ediliyor', 'Dosya yükleme alanı', 'Dosyaları buraya bırak', 'Cihazdan dosya seç',
            'Türler', 'Her dosya türü; APK, MP4, MKV, AVI, MP3, WAV, FLAC, JPG, PNG, WEBP, PDF, TXT, SRT, ASS ve ZIP dahil. TV’de açılması için uygun uygulama gerekir.',
            'Sınır', 'Dosya başına en fazla 2 GB. TV’de dosya boyutunun yaklaşık 3 katı boş alan gerekir.',
            'Dosya aktarımı', 'Hazırlanıyor', 'Dosya', 'Aktarım yerel ağda HTTP ile yapılır. Yalnızca güvendiğin ağlarda kullan.',
            'Dil', 'Otomatik (telefon dili)', 'Aktarım sürüyor. Bittiğinde yeni dosya seçebilirsin.',
            '“{0}” 2 GB sınırını aşıyor.', 'Eşleştirme gerekli: TV’deki QR kodu okut.', 'Dosya göndermek için TV’deki QR kodu okut.',
            'Dosya TV’ye ulaştı.', '{0} dosya TV’ye ulaştı.', 'TV ile bağlantı hazır', 'Aktarım başarısız oldu.', 'TV’ye aktarıldı',
            'Eşleştirme süresi doldu. TV’deki QR kodu yeniden okut.', '2 GB sınırı aşıldı veya TV’de yeterli boş alan yok.',
            'Başka bir aktarım sürüyor.', 'Yeniden eşleştir', 'Aktarım durdu', 'TV bağlantısı kesildi', 'Bağlantı kesildi',
            'TV bağlantısı kesildi. Ağı kontrol edip tekrar dene.', 'TV yanıt vermedi.'
        ],
        'pt-BR': [
            'TVDrop — Envie arquivos para a TV', 'Escolha.', 'Envie.', 'Na TV.',
            'Envie arquivos para a TV', 'Verificando conexão', 'Área de envio de arquivos', 'Solte os arquivos aqui', 'Escolher arquivos',
            'Tipos', 'Qualquer tipo de arquivo, incluindo APK, MP4, MKV, AVI, MP3, WAV, FLAC, JPG, PNG, WEBP, PDF, TXT, SRT, ASS e ZIP. É necessário um app compatível para abrir na TV.',
            'Limite', 'Até 2 GB por arquivo. A TV precisa de cerca de três vezes o tamanho do arquivo em espaço livre.',
            'Progresso da transferência', 'Preparando', 'Arquivo', 'A transferência usa HTTP na rede local. Use apenas em redes confiáveis.',
            'Idioma', 'Automático (idioma do celular)', 'Há uma transferência em andamento. Escolha outro arquivo quando terminar.',
            '“{0}” excede o limite de 2 GB.', 'Conexão necessária: leia o QR na TV.', 'Leia o QR na TV para enviar arquivos.',
            'Arquivo enviado à TV.', '{0} arquivos enviados à TV.', 'Conectado à TV', 'Falha na transferência.', 'Enviado à TV',
            'A conexão expirou. Leia novamente o QR na TV.', 'Limite de 2 GB excedido ou espaço livre insuficiente na TV.',
            'Outra transferência está em andamento.', 'Conectar novamente', 'Transferência interrompida', 'TV desconectada', 'Conexão perdida',
            'Conexão com a TV perdida. Verifique a rede e tente novamente.', 'A TV não respondeu.'
        ],
        'zh-CN': [
            'TVDrop — 向电视发送文件', '选择。', '发送。', '电视上。',
            '向电视发送文件', '正在检查连接', '文件上传区域', '将文件拖到这里', '选择文件',
            '类型', '支持所有文件类型，包括 APK、MP4、MKV、AVI、MP3、WAV、FLAC、JPG、PNG、WEBP、PDF、TXT、SRT、ASS 和 ZIP。电视上需要合适的应用才能打开。',
            '限制', '每个文件最多 2 GB。电视可用空间约需文件大小的三倍。',
            '传输进度', '正在准备', '文件', '文件通过局域网 HTTP 传输。请仅在可信网络上使用。',
            '语言', '自动（手机语言）', '文件正在传输。完成后可选择其他文件。',
            '“{0}”超过 2 GB 限制。', '需要配对：扫描电视上的二维码。', '扫描电视上的二维码以发送文件。',
            '文件已发送到电视。', '{0} 个文件已发送到电视。', '已连接电视', '传输失败。', '已发送到电视',
            '配对已过期。请重新扫描电视上的二维码。', '超过 2 GB 限制或电视可用空间不足。',
            '其他传输正在进行。', '重新配对', '传输已停止', '电视连接已断开', '连接已断开',
            '与电视的连接已断开。请检查网络后重试。', '电视无响应。'
        ],
        ja: [
            'TVDrop — テレビにファイルを送信', '選ぶ。', '送る。', 'テレビへ。',
            'テレビにファイルを送信', '接続を確認中', 'ファイル送信エリア', 'ここにファイルをドロップ', 'ファイルを選択',
            '種類', 'APK、MP4、MKV、AVI、MP3、WAV、FLAC、JPG、PNG、WEBP、PDF、TXT、SRT、ASS、ZIPなど、すべてのファイル形式に対応。テレビで開くには対応アプリが必要です。',
            '上限', '1ファイル最大2 GB。テレビにはファイルサイズの約3倍の空き容量が必要です。',
            '転送状況', '準備中', 'ファイル', '転送にはローカルネットワークのHTTPを使用します。信頼できるネットワークでのみ利用してください。',
            '言語', '自動（スマートフォンの言語）', '転送中です。完了してから次のファイルを選択してください。',
            '「{0}」は2 GBの上限を超えています。', '接続が必要です。テレビのQRコードを読み取ってください。', 'テレビのQRコードを読み取ってファイルを送信してください。',
            'ファイルをテレビに送信しました。', '{0}件のファイルをテレビに送信しました。', 'テレビに接続済み', '転送に失敗しました。', 'テレビに送信しました',
            '接続の有効期限が切れました。テレビのQRコードを再度読み取ってください。', '2 GBの上限を超えたか、テレビの空き容量が不足しています。',
            '別の転送が進行中です。', '再接続が必要', '転送を停止しました', 'テレビとの接続が切れました', '接続が切れました',
            'テレビとの接続が切れました。ネットワークを確認して再試行してください。', 'テレビが応答しませんでした。'
        ],
        de: [
            'TVDrop — Dateien an den TV senden', 'Wählen.', 'Senden.', 'Am TV.',
            'Dateien an den TV senden', 'Verbindung wird geprüft', 'Datei-Upload-Bereich', 'Dateien hier ablegen', 'Dateien auswählen',
            'Typen', 'Alle Dateitypen, darunter APK, MP4, MKV, AVI, MP3, WAV, FLAC, JPG, PNG, WEBP, PDF, TXT, SRT, ASS und ZIP. Zum Öffnen auf dem TV ist eine passende App nötig.',
            'Limit', 'Maximal 2 GB pro Datei. Der TV benötigt etwa das Dreifache der Dateigröße als freien Speicher.',
            'Übertragungsfortschritt', 'Vorbereitung', 'Datei', 'Die Übertragung erfolgt per HTTP im lokalen Netzwerk. Nur in vertrauenswürdigen Netzwerken verwenden.',
            'Sprache', 'Automatisch (Handysprache)', 'Eine Übertragung läuft. Wähle danach eine weitere Datei.',
            '„{0}“ überschreitet das 2-GB-Limit.', 'Kopplung erforderlich: QR-Code auf dem TV scannen.', 'Scanne den QR-Code auf dem TV, um Dateien zu senden.',
            'Datei an TV gesendet.', '{0} Dateien an TV gesendet.', 'Mit TV verbunden', 'Übertragung fehlgeschlagen.', 'An TV gesendet',
            'Kopplung abgelaufen. QR-Code auf dem TV erneut scannen.', '2-GB-Limit überschritten oder zu wenig freier Speicher auf dem TV.',
            'Eine weitere Übertragung läuft.', 'Erneut koppeln', 'Übertragung gestoppt', 'TV getrennt', 'Verbindung verloren',
            'Verbindung zum TV verloren. Netzwerk prüfen und erneut versuchen.', 'TV antwortet nicht.'
        ],
        id: [
            'TVDrop — Kirim file ke TV', 'Pilih.', 'Kirim.', 'Di TV.',
            'Kirim file ke TV', 'Memeriksa koneksi', 'Area unggah file', 'Letakkan file di sini', 'Pilih file',
            'Jenis', 'Semua jenis file, termasuk APK, MP4, MKV, AVI, MP3, WAV, FLAC, JPG, PNG, WEBP, PDF, TXT, SRT, ASS, dan ZIP. Aplikasi yang sesuai diperlukan untuk membukanya di TV.',
            'Batas', 'Maksimal 2 GB per file. TV membutuhkan ruang kosong sekitar tiga kali ukuran file.',
            'Progres transfer', 'Menyiapkan', 'File', 'Transfer menggunakan HTTP di jaringan lokal. Gunakan hanya pada jaringan tepercaya.',
            'Bahasa', 'Otomatis (bahasa ponsel)', 'Transfer sedang berlangsung. Pilih file lain setelah selesai.',
            '“{0}” melebihi batas 2 GB.', 'Perlu penyandingan: pindai QR di TV.', 'Pindai QR di TV untuk mengirim file.',
            'File terkirim ke TV.', '{0} file terkirim ke TV.', 'Terhubung ke TV', 'Transfer gagal.', 'Terkirim ke TV',
            'Penyandingan kedaluwarsa. Pindai ulang QR di TV.', 'Batas 2 GB terlampaui atau ruang kosong TV tidak cukup.',
            'Transfer lain sedang berlangsung.', 'Sandingi lagi', 'Transfer berhenti', 'TV terputus', 'Koneksi terputus',
            'Koneksi ke TV terputus. Periksa jaringan dan coba lagi.', 'TV tidak merespons.'
        ],
        ru: [
            'TVDrop — Отправка файлов на ТВ', 'Выберите.', 'Отправьте.', 'На ТВ.',
            'Отправить файлы на ТВ', 'Проверка соединения', 'Область загрузки файлов', 'Перетащите файлы сюда', 'Выбрать файлы',
            'Типы', 'Любые типы файлов, включая APK, MP4, MKV, AVI, MP3, WAV, FLAC, JPG, PNG, WEBP, PDF, TXT, SRT, ASS и ZIP. Для открытия на ТВ нужно подходящее приложение.',
            'Лимит', 'До 2 ГБ на файл. На ТВ требуется примерно втрое больше свободного места, чем размер файла.',
            'Ход передачи', 'Подготовка', 'Файл', 'Передача идёт по HTTP в локальной сети. Используйте только доверенные сети.',
            'Язык', 'Автоматически (язык телефона)', 'Идёт передача. Выберите другой файл после завершения.',
            '«{0}» превышает лимит 2 ГБ.', 'Нужно сопряжение: отсканируйте QR-код на ТВ.', 'Отсканируйте QR-код на ТВ, чтобы отправить файл.',
            'Файл отправлен на ТВ.', 'Файлов отправлено на ТВ: {0}.', 'Подключено к ТВ', 'Не удалось передать файл.', 'Отправлено на ТВ',
            'Срок сопряжения истёк. Снова отсканируйте QR-код на ТВ.', 'Превышен лимит 2 ГБ или на ТВ недостаточно места.',
            'Уже идёт другая передача.', 'Повторить сопряжение', 'Передача остановлена', 'ТВ отключён', 'Соединение потеряно',
            'Связь с ТВ потеряна. Проверьте сеть и повторите попытку.', 'ТВ не отвечает.'
        ],
        es: [
            'TVDrop — Envía archivos al TV', 'Elige.', 'Envía.', 'En el TV.',
            'Envía archivos al TV', 'Comprobando conexión', 'Área de carga de archivos', 'Suelta archivos aquí', 'Elegir archivos',
            'Tipos', 'Cualquier tipo de archivo, incluidos APK, MP4, MKV, AVI, MP3, WAV, FLAC, JPG, PNG, WEBP, PDF, TXT, SRT, ASS y ZIP. Se necesita una app compatible para abrirlo en el TV.',
            'Límite', 'Hasta 2 GB por archivo. El TV necesita unas tres veces el tamaño del archivo como espacio libre.',
            'Progreso de transferencia', 'Preparando', 'Archivo', 'La transferencia usa HTTP en la red local. Úsalo solo en redes de confianza.',
            'Idioma', 'Automático (idioma del teléfono)', 'Hay una transferencia en curso. Elige otro archivo cuando termine.',
            '«{0}» supera el límite de 2 GB.', 'Vinculación necesaria: escanea el QR del TV.', 'Escanea el QR del TV para enviar archivos.',
            'Archivo enviado al TV.', '{0} archivos enviados al TV.', 'Conectado al TV', 'Falló la transferencia.', 'Enviado al TV',
            'La vinculación caducó. Escanea de nuevo el QR del TV.', 'Se superó el límite de 2 GB o no hay suficiente espacio en el TV.',
            'Ya hay otra transferencia en curso.', 'Volver a vincular', 'Transferencia detenida', 'TV desconectado', 'Conexión perdida',
            'Se perdió la conexión con el TV. Comprueba la red y vuelve a intentarlo.', 'El TV no respondió.'
        ],
        fr: [
            'TVDrop — Envoyer des fichiers à la TV', 'Choisissez.', 'Envoyez.', 'Sur la TV.',
            'Envoyer des fichiers à la TV', 'Vérification de la connexion', 'Zone d’envoi de fichiers', 'Déposez les fichiers ici', 'Choisir des fichiers',
            'Types', 'Tous les types de fichiers, dont APK, MP4, MKV, AVI, MP3, WAV, FLAC, JPG, PNG, WEBP, PDF, TXT, SRT, ASS et ZIP. Une application adaptée est nécessaire pour les ouvrir sur la TV.',
            'Limite', 'Jusqu’à 2 Go par fichier. La TV a besoin d’environ trois fois la taille du fichier en espace libre.',
            'Progression du transfert', 'Préparation', 'Fichier', 'Le transfert utilise HTTP sur le réseau local. Utilisez uniquement un réseau de confiance.',
            'Langue', 'Automatique (langue du téléphone)', 'Un transfert est en cours. Choisissez un autre fichier une fois terminé.',
            '« {0} » dépasse la limite de 2 Go.', 'Association requise : scannez le QR sur la TV.', 'Scannez le QR sur la TV pour envoyer des fichiers.',
            'Fichier envoyé à la TV.', '{0} fichiers envoyés à la TV.', 'Connecté à la TV', 'Échec du transfert.', 'Envoyé à la TV',
            'Association expirée. Scannez à nouveau le QR sur la TV.', 'Limite de 2 Go dépassée ou espace libre insuffisant sur la TV.',
            'Un autre transfert est en cours.', 'Associer de nouveau', 'Transfert arrêté', 'TV déconnectée', 'Connexion perdue',
            'Connexion à la TV perdue. Vérifiez le réseau et réessayez.', 'La TV ne répond pas.'
        ]
    };
    const result = {};
    for (const [locale, values] of Object.entries(rows)) {
        if (values.length !== keys.length) throw new Error(`Incomplete ${locale} translations`);
        result[locale] = Object.fromEntries(keys.map((key, index) => [key, values[index]]));
    }
    window.TVDropI18n = result;
})();
