package za.co.varsitycollege.st10215473.pank.decorator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat

class SpacesItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.left = space
        outRect.right = space
        outRect.bottom = space
        if (parent.getChildLayoutPosition(view) == 0) {
            outRect.top = space
        }
    }
}