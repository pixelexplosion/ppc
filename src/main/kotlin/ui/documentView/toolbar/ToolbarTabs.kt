package ui.documentView.toolbar

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.DocumentViewControlState
import data.Tool
import ui.documentView.toolbar.colorpicker.ColorSlider
import x


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
        IconButton(false, "addPage.svg", 60.dp, Modifier.height(60.dp).padding(10.dp)) {
            documentViewControlState.documentController.value.newPage()
        }

        IconButton(false, "colorPicker.svg", 60.dp, Modifier.height(60.dp).padding(10.dp)) {
            documentViewControlState.activeDialog.value =
                @Composable { ColorSlider(documentViewControlState.documentController.value) }
        }

        IconButton(false, "connect.svg", 60.dp, Modifier.height(60.dp).padding(10.dp)) {
            documentViewControlState.activeDialog.value =
                @Composable { ConnectDialog(documentViewControlState.documentController.value) }
        }



    }
}