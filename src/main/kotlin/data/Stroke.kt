package data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import util.Point

@Serializable

//todo: color serialization
// todo: currently the strokes get created on mouserelease therefore the color must be set after initialization -> change in future to create on mouse press
class Stroke(@Transient var color: Color = Color.Red) {
    @Serializable(with = SnapshotListSerializer::class)
    val spline = mutableStateListOf<Point>()

    @Serializable(with = SnapshotListSerializer::class)
    val pendingPoints = mutableStateListOf<Point>()

    @Transient
    val boundingBoxes = mutableListOf<BoundingBox>()

    @Transient
    var mainBoundingBox = BoundingBox(Point(0.0, 0.0), Point(0.0, 0.0))


    fun isEmpty() = spline.isEmpty() && pendingPoints.isEmpty()

    fun move(offset: Point) {
        spline.forEach { it + offset }
        pendingPoints.forEach { it + offset }
        // todo: update bounding boxes
    }
}

