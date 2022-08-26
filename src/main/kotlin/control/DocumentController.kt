package control

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import data.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import util.Point
import kotlin.math.abs

class DocumentController(document: Document) : Controller {
    val mouse = MouseInputHandler(this)
    val state = mutableStateOf( DocumentControlState(this, document))

    val strokeController = StrokeController()
    private var canvasSize = Point(0, 0)
    var selection: MutableState<Selection?> = mutableStateOf(null)
    var selectedPage: Page? = null
        private set

    var localCenter = Point(0, 0)
        private set

    var selectedTool: MutableState<Tool> = mutableStateOf(Eraser())
    var selectedColor: Color = Color.Red





    fun toolDraggedEnded() {
        when (selectedTool.value) {
            is Selector -> selection.value?.selectionComplete = true
            is TPen -> if (selectedPage != null) strokeController.endStroke()

            else -> {}

        }
    }

    fun toolDragged(point: Point) {

        val globalPoint = localCoordsToGlobal(point)

        when (selectedTool.value) {

            is TPen -> strokeAddPoint(globalPoint)
            is Eraser -> eraserMoved(globalPoint)
            is Selector -> selectorMoved(globalPoint)
        }

    }

    fun toolDown() {
        when (selectedTool.value) {
            is Selector -> {
                if (selection.value?.selectionComplete!!)
                    selection.value = null
            }
            is TPen -> if (selectedPage != null) newStroke()

            else -> {}
        }
    }

    fun toolClicked() {

    }

    private fun selectorMoved(globalPoint: Point) {

        getPageByPoint(globalPoint).let {
            if (it != selectedPage) {
                selectedPage = it

            }
        }
        if (selectedPage != null) {

            if (selection.value == null) {
                selection.value = Selection(globalPoint)
            }
            selection.value?.end?.value = globalPoint

            selection.value!!.selectedStrokes.clear()
            selection.value!!.addStroke(
                selectedPage!!.strokes.filter {
                    BoundingBox(
                        selection.value!!.start,
                        selection.value!!.end.value!!
                    ) in it.mainBoundingBox
                }
            )
        }

    }

    private fun strokeAddPoint(globalPoint: Point) {
        getPageByPoint(globalPoint).let {
            if (it != selectedPage) {
                selectedPage = it

                newStroke()
            }
        }
        if (selectedPage != null) {
            strokeController.addPoint(globalPoint)
        }
    }


    private fun eraserMoved(globalPoint: Point) {

        getPageByPoint(globalPoint).let {
            if (it != selectedPage) {
                selectedPage = it
            }

            // needed to avoid java.util.ConcurrentModificationException
            val erasedStrokes = mutableListOf<Stroke>()

            val eraserBoundingBox = BoundingBox(globalPoint - Point(5, 5), globalPoint + Point(5, 5))

            selectedPage?.strokes?.forEach { s ->
                if (eraserBoundingBox !in s.mainBoundingBox) return@forEach

                run bb@{
                    s.boundingBoxes.forEach { bb ->
                        if (eraserBoundingBox in bb) {

                            if ((abs((globalPoint - bb.point0).cross(bb.point1 - bb.point0))) / (bb.point1 - bb.point0).length < 10f * state.value.document.value.zoomFactor) {
                                println((abs((globalPoint - bb.point0).cross(bb.point1 - bb.point0))) / (bb.point1 - bb.point0).length)
                                erasedStrokes.add(s)
                                return@bb
                            }

                        }
                    }
                }
            }

            selectedPage?.strokes?.removeAll(erasedStrokes)

        }


    }

    fun newStroke() {
        val stroke = selectedPage?.newStroke((selectedTool.value as TPen).pen.color, (selectedTool.value as TPen).pen.width)
        if (stroke != null)
            strokeController.newStroke(stroke)

    }

//    fun newStroke() {
//        val stroke = Stroke()
//        //state.value.strokes.add(stroke)
//       // strokeController.newStroke(stroke)
//
//    }

    fun getPageByPoint(point: Point): Page? {

        return try {
            state.value.document.value.pages.first {
                point.x in it.topLeft.x..(it.topLeft.x + state.value.document.value.pageSize.width) &&
                        point.y in it.topLeft.y..(it.topLeft.y + state.value.document.value.pageSize.height)
            }
        } catch (e: NoSuchElementException) {
            null
        }
    }

    fun resize(newXDim: Int, newYDim: Int, localCenter: Point) {
        canvasSize = Point(newXDim, newYDim)
        this.localCenter = localCenter
    }

    fun scrollY(scrollDelta: Float) {
        if (state.value.document.value.scrollY + scrollDelta < 0) return
        state.value.document.value.centerPoint.value += Point(0, scrollDelta.toDouble())
        state.value.document.value.scrollY += scrollDelta
        println(state.value.document.value.centerPoint.value)

    }

    fun scrollX(scrollDelta: Float) {
        println("X: $scrollDelta")
        state.value.document.value.centerPoint.value += Point(scrollDelta.toDouble(), 0)
        state.value.document.value.scrollX += scrollDelta

    }

    fun newPage() {
        val document = state.value.document.value

        document.newPage(
            PageType.Dotted,
            Point(-document.pageSize.width / 2.0, ((document.pageSize.height + 10) * document.pageCount).toDouble())
        )
    }

    fun zoom(zoomDelta: Float, localMousePos: Point) {
        if (state.value.document.value.zoomFactor + zoomDelta <= 0) return

        val zoomFactor = 1 / (state.value.document.value.zoomFactor + zoomDelta)
        val newPos = localMousePos * zoomFactor
        val delta = localMousePos * (1 / state.value.document.value.zoomFactor) - newPos
        val newCenterPoint = state.value.document.value.centerPoint.value + delta
        state.value.document.value.updateZoom(zoomDelta, newCenterPoint)
    }

    fun setColor(color: Color) {
        selectedColor = color
        strokeController.stroke?.color = color
    }

    private fun localCoordsToGlobal(localPoint: Point): Point {
        val zoomFactor = 1 / state.value.document.value.zoomFactor
        return localPoint * zoomFactor + state.value.document.value.centerPoint.value - localCenter
    }

    fun deleteSelection() {
        selection.value?.selectedStrokes?.let { selectedPage?.strokes?.removeAll(it) }
        selection.value = null
    }

    fun moveSelection() {

        // todo ... fix (:
        selection.value?.selectedStrokes?.forEach {
            it.move(Point(30.0, 30.0))
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun docToBytes(): ByteArray = Cbor.encodeToByteArray(state.value.document.value)

    @OptIn(ExperimentalSerializationApi::class)
    fun loadDocFromBytes(bytes: ByteArray) {
        try {
            state.value.document.value = Cbor.decodeFromByteArray(bytes)
        } catch (e: Exception) {
            println("Could not open file: $e")
        }
    }

    fun onRender() {
        if (state.value.document.value.pages.size == 0)
            newPage()
    }



}