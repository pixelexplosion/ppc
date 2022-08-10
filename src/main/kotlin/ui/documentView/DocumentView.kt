package ui.documentView

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import data.DocumentViewControlState
import ui.PPCCanvas
import ui.PPCWindowState
import ui.documentView.toolbar.SideBar
import ui.documentView.toolbar.ToolDialog
import ui.documentView.toolbar.Toolbar


val DocumentView: @Composable (PPCWindowState, DocumentViewControlState) -> Unit =
    @Composable { windowState, windowControlState ->
        DocumentView(windowState, windowControlState)
    }

@Composable
fun DocumentView(windowState: PPCWindowState, documentViewControlState: DocumentViewControlState) {
    val documentController = documentViewControlState.documentController.value
    remember { documentViewControlState }
    Box(modifier = Modifier.background(Color(0xFFF8FCFF))) {
            PPCCanvas(documentController)
        Column {
        Toolbar(documentViewControlState)
            SideBar(documentViewControlState, windowState)
        }
        if (documentController.selection.value != null && documentController.selection.value!!.end.value != null) {

            val test = IntOffset(
                documentController.selection.value!!.end.value!!.x.toInt(),
                documentController.selection.value!!.end.value!!.y.toInt()
            )
            SelectionMenu(test, documentController)
        }

        documentViewControlState.activeDialog.value?.let {
            ToolDialog(documentViewControlState, "title") {
                it(
                    documentController
                )
            }
        }

    }


}


