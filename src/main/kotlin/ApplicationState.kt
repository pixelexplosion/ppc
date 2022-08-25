import androidx.compose.runtime.*
import ui.PPCWindowState
import data.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import java.io.File
import java.nio.file.Path
import kotlin.io.path.*

@Composable
fun rememberApplicationState() = remember {
    ApplicationState().apply {
        newWindow()
    }
}

class ApplicationState {


    val settings = Settings()

    private val _windows = mutableStateListOf<PPCWindowState>()
    val windows: List<PPCWindowState> get() = _windows
    private val workingDirectoryPath = System.getProperty("user.home") + "/ppc/"

    fun saveDocument(document: Document, path: Path) {
        path.writeBytes(
            Cbor.encodeToByteArray(document)
        )
    }

    fun loadDocument(docInfo: DocumentInformation): Document? {
        return loadDocument(Path.of(docInfo.path))
    }



    fun loadDocument(docPath: Path): Document? {
        return try {
            val file = docPath.readBytes()
            Cbor.decodeFromByteArray<Document>(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun loadDocumentInformation(
        path: String = workingDirectoryPath,
        depth: Int = 3,
        loadedDoc: LoadedDoc
    ): List<DocumentInformation> {
        val folders = mutableListOf<DocumentInformation>()
        File(path).listFiles()?.forEach {
            if (it.isDirectory) {
                folders.add(
                    DocumentInformation(
                        it.name,
                        DocumentInformationType.Folder,
                        it.path
                    )
                )
            }
        }

        addChildrenToFolder(folders, depth - 1)

        updateLoadedDoc(folders, loadedDoc)
        return folders
    }

    fun addChildrenToFolder(folders: List<DocumentInformation>, remainingDepth: Int) {
        folders.forEach {
            if (it.type == DocumentInformationType.Folder) {

                File(it.path).listFiles()?.forEach { file ->
                    if (file.isFile && file.name.endsWith(".ppc")) {
                        it.children.add(
                            DocumentInformation(
                                file.name.substringBefore('.'),
                                DocumentInformationType.Document,
                                file.path
                            )
                        )
                    } else if (file.isDirectory) {
                        it.children.add(DocumentInformation(file.name, DocumentInformationType.Folder, file.path))
                    }
                }
                if (remainingDepth > 0) addChildrenToFolder(it.children, remainingDepth - 1)
            }
        }
    }

    private fun updateLoadedDoc(folders: List<DocumentInformation>, loadedDoc: LoadedDoc) {
        loadedDoc.workbook.value = folders.firstOrNull {
            it.path == loadedDoc.workbook.value?.path
        }
        loadedDoc.folder.value = loadedDoc.workbook.value?.children?.firstOrNull {
            it.path == loadedDoc.folder.value?.path
        }

    }

    fun newFolder(doc: DocumentInformation?, name: String) {
        val parentPath = doc?.path ?: workingDirectoryPath
        runCatching {
            Path(parentPath, name).createDirectory()
        }
    }

    fun newFile(doc: DocumentInformation?, name: String) {
        val parentPath = doc?.path ?: workingDirectoryPath
        runCatching {
            Path(parentPath, "$name.ppc").createFile()
        }
    }

    fun newWindow() {
        // TODO: New Window
        _windows.add(
            PPCWindowState(
                application = this,
                exit = _windows::remove
            )
        )
    }

    suspend fun exit() {
        val windowsCopy = windows.reversed()
        for (window in windowsCopy) {
            if (!window.exit()) {
                break
            }
        }
    }

}


