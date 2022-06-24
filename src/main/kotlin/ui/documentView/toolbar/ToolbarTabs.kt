package ui.documentView.toolbar

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.Document
import data.DocumentViewControlState
import data.Tool
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ui.documentView.toolbar.colorpicker.ColorSlider


val Tab1 = @Composable { documentViewControlState: DocumentViewControlState -> Tab1(documentViewControlState) }
val Tab2 = @Composable { documentViewControlState: DocumentViewControlState -> Tab2(documentViewControlState) }

@Composable
fun Tab1(documentViewControlState: DocumentViewControlState) {
    Row(modifier = Modifier.height(70.dp)) {
        ToolSelectButton(
            documentViewControlState.documentController.value.state.document.value.selectedTool.value == Tool.Eraser,
            "eraser.svg"
        ) {
            documentViewControlState.documentController.value.state.document.value.selectedTool.value = Tool.Eraser
        }
        BarDivider()

        ToolSelectButton(
            documentViewControlState.documentController.value.state.document.value.selectedTool.value == Tool.Pen,
            "coloredPen.svg"
        ) {
            documentViewControlState.documentController.value.state.document.value.selectedTool.value = Tool.Pen
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(false, "pen.svg", 20.dp, Modifier.height(40.dp)) {
            }
            IconButton(false, "add.svg", 20.dp, Modifier.height(40.dp)) {
            }
        }

        BarDivider()


        ToolSelectButton(
            documentViewControlState.documentController.value.state.document.value.selectedTool.value == Tool.Selector,
            "selection.svg"
        ) {
            documentViewControlState.documentController.value.state.document.value.selectedTool.value = Tool.Selector
        }
    }


}

@Composable
fun Tab2(documentViewControlState: DocumentViewControlState) {
    Row {

        Button(onClick = { documentViewControlState.documentController.value.newPage() }) {
            Text("new Page")
        }
        Button(onClick = {
            documentViewControlState.activeDialog.value =
                @Composable { ColorSlider(documentViewControlState.documentController.value) }
        }) {
            Text("color picker")
        }
        Button(onClick = {

            documentViewControlState.activeDialog.value =
                @Composable { ConnectDialog(documentViewControlState.documentController.value.connectionController) }

        }) {
            Text("verbinden")
        }

        Button(onClick = {
            val a = documentViewControlState.documentController.value.state.document.value.toJSON()
            val b = Json.decodeFromString<Document>(a)
            println(b.toJSON())
        }) {
            Text("connect")
        }

    }
}