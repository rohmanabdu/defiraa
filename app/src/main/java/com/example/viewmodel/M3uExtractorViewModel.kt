package com.example.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SavedPlaylistEntity
import com.example.extractor.ApkExtractorEngine
import com.example.extractor.StreamTester
import com.example.model.CategoryInfo
import com.example.model.CategorySortMode
import com.example.model.ChannelItem
import com.example.model.ExtractionLog
import com.example.model.ExtractionResult
import com.example.model.LogLevel
import com.example.model.StreamStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class M3uExtractorViewModel(application: Application) : AndroidViewModel(application) {

    private val extractorEngine = ApkExtractorEngine(application)
    private val streamTester = StreamTester()
    private val playlistDao = AppDatabase.getDatabase(application).playlistDao()

    val savedPlaylists = playlistDao.getAllPlaylists().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _inputUrl = MutableStateFlow("https://raw.githubusercontent.com/rohmanabdu/rama-ana/refs/heads/main/DiktaTVV%20Versi%20Terbaru%20(SFILE.MOBI)_2.apk")
    val inputUrl: StateFlow<String> = _inputUrl.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isExtracting = MutableStateFlow(false)
    val isExtracting: StateFlow<Boolean> = _isExtracting.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _progressText = MutableStateFlow("")
    val progressText: StateFlow<String> = _progressText.asStateFlow()

    private val _logs = MutableStateFlow<List<ExtractionLog>>(emptyList())
    val logs: StateFlow<List<ExtractionLog>> = _logs.asStateFlow()

    private val _extractionResult = MutableStateFlow<ExtractionResult?>(null)
    val extractionResult: StateFlow<ExtractionResult?> = _extractionResult.asStateFlow()

    private val _channels = MutableStateFlow<List<ChannelItem>>(emptyList())
    val channels: StateFlow<List<ChannelItem>> = _channels.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _customCategoryOrder = MutableStateFlow<List<String>>(emptyList())
    val customCategoryOrder: StateFlow<List<String>> = _customCategoryOrder.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _rawM3uText = MutableStateFlow("")
    val rawM3uText: StateFlow<String> = _rawM3uText.asStateFlow()

    private val _currentlyPlayingChannel = MutableStateFlow<ChannelItem?>(null)
    val currentlyPlayingChannel: StateFlow<ChannelItem?> = _currentlyPlayingChannel.asStateFlow()

    private val _isTestingStreams = MutableStateFlow(false)
    val isTestingStreams: StateFlow<Boolean> = _isTestingStreams.asStateFlow()

    fun setInputUrl(url: String) {
        _inputUrl.value = url
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun setSelectedCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCurrentlyPlayingChannel(channel: ChannelItem?) {
        _currentlyPlayingChannel.value = channel
    }

    fun setRawM3uText(text: String) {
        _rawM3uText.value = text
    }

    fun addLog(message: String, level: LogLevel = LogLevel.INFO) {
        _logs.value = _logs.value + ExtractionLog(message = message, level = level)
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun startExtraction(url: String = _inputUrl.value) {
        if (url.isBlank()) {
            Toast.makeText(getApplication(), "Masukkan link URL APK atau playlist terlebih dahulu!", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            _isExtracting.value = true
            _progress.value = 0.05f
            _progressText.value = "Memulai ekstraksi..."
            _logs.value = emptyList()

            try {
                val result = extractorEngine.extractFromUrl(
                    inputUrl = url,
                    onProgress = { p, text ->
                        _progress.value = p
                        _progressText.value = text
                    },
                    onLog = { log ->
                        _logs.value = _logs.value + log
                    }
                )

                _extractionResult.value = result
                _channels.value = result.channels
                _rawM3uText.value = result.rawM3u
                _selectedCategory.value = "Semua"

                if (result.channels.isNotEmpty()) {
                    _selectedTab.value = 1 // Switch to channels screen
                }

                // Automatically save to local history
                saveCurrentPlaylistToDb(
                    title = result.metadata.fileName.ifBlank { "Playlist Siaran APK" }
                )

            } catch (e: Exception) {
                addLog("Terjadi kesalahan: ${e.localizedMessage ?: e.message}", LogLevel.ERROR)
                Toast.makeText(getApplication(), "Gagal ekstrak: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                _isExtracting.value = false
            }
        }
    }

    fun startExtractionFromUri(uri: Uri, fileName: String) {
        viewModelScope.launch {
            _isExtracting.value = true
            _progress.value = 0.05f
            _progressText.value = "Membaca file lokal..."
            _logs.value = emptyList()

            try {
                val result = extractorEngine.extractFromUri(
                    uri = uri,
                    fileName = fileName,
                    onProgress = { p, text ->
                        _progress.value = p
                        _progressText.value = text
                    },
                    onLog = { log ->
                        _logs.value = _logs.value + log
                    }
                )

                _extractionResult.value = result
                _channels.value = result.channels
                _rawM3uText.value = result.rawM3u
                _selectedCategory.value = "Semua"

                if (result.channels.isNotEmpty()) {
                    _selectedTab.value = 1
                }

                saveCurrentPlaylistToDb(
                    title = fileName.ifBlank { "File APK Lokal" }
                )
            } catch (e: Exception) {
                addLog("Gagal ekstrak file: ${e.localizedMessage ?: e.message}", LogLevel.ERROR)
                Toast.makeText(getApplication(), "Gagal: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                _isExtracting.value = false
            }
        }
    }

    fun toggleChannelSelection(id: String) {
        _channels.value = _channels.value.map {
            if (it.id == id) it.copy(isSelected = !it.isSelected) else it
        }
        regenerateRawM3u()
    }

    fun selectAllChannels(select: Boolean) {
        _channels.value = _channels.value.map { it.copy(isSelected = select) }
        regenerateRawM3u()
    }

    fun updateChannel(updated: ChannelItem) {
        _channels.value = _channels.value.map {
            if (it.id == updated.id) updated else it
        }
        regenerateRawM3u()
    }

    fun addManualChannel(name: String, url: String, group: String, logo: String) {
        val newChannel = ChannelItem(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            streamUrl = url.trim(),
            group = group.trim().ifBlank { "Umum" },
            logoUrl = logo.trim(),
            status = StreamStatus.UNKNOWN
        )
        _channels.value = listOf(newChannel) + _channels.value
        regenerateRawM3u()
        Toast.makeText(getApplication(), "Channel berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
    }

    fun deleteChannel(id: String) {
        val target = _channels.value.find { it.id == id }
        _channels.value = _channels.value.filterNot { it.id == id }
        regenerateRawM3u()
        Toast.makeText(getApplication(), "Siaran '${target?.name ?: "Channel"}' berhasil dihapus", Toast.LENGTH_SHORT).show()
    }

    fun deleteSelectedChannels() {
        val toDeleteCount = _channels.value.count { it.isSelected }
        if (toDeleteCount == 0) {
            Toast.makeText(getApplication(), "Tidak ada siaran yang dipilih untuk dihapus", Toast.LENGTH_SHORT).show()
            return
        }
        _channels.value = _channels.value.filterNot { it.isSelected }
        regenerateRawM3u()
        Toast.makeText(getApplication(), "$toDeleteCount siaran terpilih berhasil dihapus!", Toast.LENGTH_SHORT).show()
    }

    fun getOrderedCategories(): List<String> {
        val existingGroups = _channels.value.map { it.group.trim().ifBlank { "Umum" } }.distinct()
        val customOrder = _customCategoryOrder.value.filter { it in existingGroups }
        val remaining = existingGroups.filterNot { it in customOrder }
        return customOrder + remaining
    }

    fun renameCategory(oldCategoryName: String, newCategoryName: String) {
        val cleanNew = newCategoryName.trim().ifBlank { "Umum" }
        if (oldCategoryName == cleanNew) return

        _channels.value = _channels.value.map { ch ->
            if (ch.group.trim().equals(oldCategoryName.trim(), ignoreCase = false)) {
                ch.copy(group = cleanNew)
            } else {
                ch
            }
        }

        // Update custom category order
        _customCategoryOrder.value = _customCategoryOrder.value.map {
            if (it == oldCategoryName) cleanNew else it
        }.distinct()

        // If selected category was renamed, update it
        if (_selectedCategory.value == oldCategoryName) {
            _selectedCategory.value = cleanNew
        }

        regenerateRawM3u()
        Toast.makeText(getApplication(), "Kategori '$oldCategoryName' diubah menjadi '$cleanNew'", Toast.LENGTH_SHORT).show()
    }

    fun deleteCategory(
        categoryName: String,
        deleteChannels: Boolean,
        targetMoveCategory: String = "Umum"
    ) {
        val targetMove = targetMoveCategory.trim().ifBlank { "Umum" }
        val channelsInCategory = _channels.value.filter { it.group.trim() == categoryName.trim() }

        if (deleteChannels) {
            _channels.value = _channels.value.filterNot { it.group.trim() == categoryName.trim() }
            Toast.makeText(getApplication(), "${channelsInCategory.size} siaran dalam kategori '$categoryName' telah dihapus", Toast.LENGTH_SHORT).show()
        } else {
            _channels.value = _channels.value.map { ch ->
                if (ch.group.trim() == categoryName.trim()) {
                    ch.copy(group = targetMove)
                } else {
                    ch
                }
            }
            Toast.makeText(getApplication(), "${channelsInCategory.size} siaran dipindahkan ke '$targetMove'", Toast.LENGTH_SHORT).show()
        }

        _customCategoryOrder.value = _customCategoryOrder.value.filterNot { it == categoryName }

        if (_selectedCategory.value == categoryName) {
            _selectedCategory.value = "Semua"
        }

        regenerateRawM3u()
    }

    fun moveCategory(fromIndex: Int, toIndex: Int, syncToPlaylist: Boolean = false) {
        val currentCategories = getOrderedCategories().toMutableList()
        if (fromIndex !in currentCategories.indices || toIndex !in currentCategories.indices || fromIndex == toIndex) {
            return
        }

        val item = currentCategories.removeAt(fromIndex)
        currentCategories.add(toIndex, item)
        _customCategoryOrder.value = currentCategories

        if (syncToPlaylist) {
            applyCategoryOrderToChannels(currentCategories)
        }
    }

    fun sortCategories(mode: CategorySortMode, applyToChannelsPlaylist: Boolean = true) {
        val currentCategories = getOrderedCategories()
        val sortedCategories = when (mode) {
            CategorySortMode.ALPHABETICAL_ASC -> currentCategories.sortedWith(String.CASE_INSENSITIVE_ORDER)
            CategorySortMode.ALPHABETICAL_DESC -> currentCategories.sortedWith(String.CASE_INSENSITIVE_ORDER.reversed())
            CategorySortMode.CHANNEL_COUNT_DESC -> currentCategories.sortedByDescending { cat ->
                _channels.value.count { it.group.trim() == cat.trim() }
            }
            CategorySortMode.CHANNEL_COUNT_ASC -> currentCategories.sortedBy { cat ->
                _channels.value.count { it.group.trim() == cat.trim() }
            }
            CategorySortMode.CUSTOM -> currentCategories
        }

        _customCategoryOrder.value = sortedCategories

        if (applyToChannelsPlaylist) {
            applyCategoryOrderToChannels(sortedCategories)
        } else {
            Toast.makeText(getApplication(), "Kategori berhasil diurutkan!", Toast.LENGTH_SHORT).show()
        }
    }

    fun applyCategoryOrderToChannels(order: List<String> = getOrderedCategories()) {
        val orderMap = order.withIndex().associate { it.value to it.index }
        _channels.value = _channels.value.sortedWith(
            compareBy<ChannelItem> { orderMap[it.group.trim()] ?: Int.MAX_VALUE }
        )
        regenerateRawM3u()
        Toast.makeText(getApplication(), "Playlist diurutkan berdasarkan kategori!", Toast.LENGTH_SHORT).show()
    }

    fun writeM3uToUri(uri: Uri, customContent: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val textToWrite = customContent ?: _rawM3uText.value
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(textToWrite.toByteArray(Charsets.UTF_8))
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "File .m3u hasil edit berhasil disimpan ke memori!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Gagal menyimpan file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun regenerateRawM3u() {
        _rawM3uText.value = extractorEngine.generateM3uString(_channels.value)
    }

    fun parseAndApplyRawM3u(rawText: String) {
        val parsed = extractorEngine.parseM3uContent(rawText)
        if (parsed.isNotEmpty()) {
            _channels.value = parsed
            _rawM3uText.value = rawText
            Toast.makeText(getApplication(), "${parsed.size} channel berhasil dimuat dari M3U!", Toast.LENGTH_SHORT).show()
        }
    }

    fun testSingleChannel(id: String) {
        viewModelScope.launch {
            val current = _channels.value.find { it.id == id } ?: return@launch
            _channels.value = _channels.value.map {
                if (it.id == id) it.copy(status = StreamStatus.CHECKING) else it
            }
            val tested = streamTester.testStream(current)
            _channels.value = _channels.value.map {
                if (it.id == id) tested else it
            }
        }
    }

    fun testAllChannels() {
        if (_isTestingStreams.value) return
        viewModelScope.launch {
            _isTestingStreams.value = true
            Toast.makeText(getApplication(), "Memeriksa status semua stream...", Toast.LENGTH_SHORT).show()
            
            val updated = _channels.value.toMutableList()
            for (i in updated.indices) {
                val item = updated[i]
                updated[i] = item.copy(status = StreamStatus.CHECKING)
                _channels.value = updated.toList()

                val tested = streamTester.testStream(item)
                updated[i] = tested
                _channels.value = updated.toList()
            }
            _isTestingStreams.value = false
            Toast.makeText(getApplication(), "Pemeriksaan stream selesai!", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyM3uToClipboard(customText: String? = null) {
        val textToCopy = customText ?: _rawM3uText.value
        if (textToCopy.isBlank()) {
            Toast.makeText(getApplication(), "Teks M3U kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        // Android Binder max size is ~1MB for the whole process. Prevent TransactionTooLargeException
        if (textToCopy.length > 300_000) {
            Toast.makeText(
                getApplication(),
                "Ukuran playlist terlalu besar (${textToCopy.length / 1024} KB) untuk clipboard sistem. Gunakan tombol 'Unduh .M3U' untuk menyimpan ke memori!",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        try {
            val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("M3U Playlist", textToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(getApplication(), "Teks M3U berhasil disalin ke clipboard!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(getApplication(), "Gagal menyalin ke clipboard. Gunakan tombol 'Unduh .M3U'!", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportAndShareM3u(fileName: String = "playlist_siaran_edit.m3u", customContent: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val textToShare = customContent ?: _rawM3uText.value
                val actualName = if (fileName.endsWith(".m3u", ignoreCase = true) || fileName.endsWith(".m3u8", ignoreCase = true)) fileName else "$fileName.m3u"
                val exportFile = File(context.cacheDir, actualName)
                FileOutputStream(exportFile).use {
                    it.write(textToShare.toByteArray(Charsets.UTF_8))
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    exportFile
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "audio/x-mpegurl"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Playlist M3U Siaran")
                    // Do not put huge text in EXTRA_TEXT to avoid TransactionTooLargeException
                    putExtra(Intent.EXTRA_TEXT, "File playlist M3U ($actualName)")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                val chooser = Intent.createChooser(shareIntent, "Bagikan File .M3U").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Gagal membagikan: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun saveM3uToPublicDownloads(fileName: String = "playlist_siaran_edit.m3u", customContent: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val textToSave = customContent ?: _rawM3uText.value
            if (textToSave.isBlank()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Konten M3U kosong!", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val actualFileName = if (fileName.endsWith(".m3u", ignoreCase = true) || fileName.endsWith(".m3u8", ignoreCase = true)) fileName else "$fileName.m3u"

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, actualFileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "audio/x-mpegurl")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        context.contentResolver.openOutputStream(uri)?.use { os ->
                            os.write(textToSave.toByteArray(Charsets.UTF_8))
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "File .m3u berhasil diunduh ke folder Downloads:\n$actualFileName", Toast.LENGTH_LONG).show()
                        }
                        return@launch
                    }
                }

                // Fallback for direct storage access
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                
                val targetFile = File(downloadsDir, actualFileName)
                FileOutputStream(targetFile).use {
                    it.write(textToSave.toByteArray(Charsets.UTF_8))
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "File .m3u berhasil diunduh ke folder Downloads:\n${targetFile.name}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    exportAndShareM3u(actualFileName, textToSave)
                }
            }
        }
    }

    fun saveCurrentPlaylistToDb(title: String) {
        viewModelScope.launch {
            if (_rawM3uText.value.isBlank()) return@launch
            val entity = SavedPlaylistEntity(
                title = title.ifBlank { "Playlist Siaran" },
                sourceUrl = _inputUrl.value,
                channelCount = _channels.value.size,
                rawM3u = _rawM3uText.value
            )
            playlistDao.insertPlaylist(entity)
        }
    }

    fun loadSavedPlaylist(saved: SavedPlaylistEntity) {
        val parsed = extractorEngine.parseM3uContent(saved.rawM3u)
        _channels.value = parsed
        _rawM3uText.value = saved.rawM3u
        _inputUrl.value = saved.sourceUrl
        _selectedCategory.value = "Semua"
        _selectedTab.value = 1 // Switch to channel list
        Toast.makeText(getApplication(), "Playlist '${saved.title}' dimuat (${parsed.size} channel)", Toast.LENGTH_SHORT).show()
    }

    fun deleteSavedPlaylist(id: Long) {
        viewModelScope.launch {
            playlistDao.deleteById(id)
            Toast.makeText(getApplication(), "Playlist dihapus dari riwayat", Toast.LENGTH_SHORT).show()
        }
    }
}
