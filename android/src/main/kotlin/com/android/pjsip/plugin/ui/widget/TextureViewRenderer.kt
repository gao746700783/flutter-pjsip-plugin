package com.android.pjsip.plugin.ui.widget

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.os.Looper
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import net.gotev.sipservice.Logger

class TextureViewRenderer : TextureView, TextureView.SurfaceTextureListener {

    companion object {
        private const val TAG = "TextureViewRenderer"
    }

    // Cached resource name.
    private val resourceName: String

    // Accessed only on the main thread.
    private var rotatedFrameWidth = 1280
    private var rotatedFrameHeight = 960

    private var initialized = false

    //    var surfaceHolder: SurfaceHolder? = null
    var surfaceRenderer: Surface? = null

    /**
     * Standard View constructor. In order to render something, you must first call init().
     */
    constructor(context: Context) : super(context) {
        resourceName = getResourceName()
        surfaceTextureListener = this
        init()
    }

    /**
     * Standard View constructor. In order to render something, you must first call init().
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        resourceName = getResourceName()
        surfaceTextureListener = this
        init()
    }

    fun init() {
        checkIsOnMainThread()
        if (initialized) return
        initialized = true
    }

    private fun getResourceName(): String {
        return try {
            resources.getResourceEntryName(id)
        } catch (e: NotFoundException) {
            ""
        }
    }

    fun checkIsOnMainThread() {
        check(
            Thread.currentThread() === Looper.getMainLooper().thread
        ) { "Not on main thread!" }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        Logger.debug(TAG, "onSurfaceTextureAvailable(). New size: " + width + "x" + height)
        checkIsOnMainThread()

        this.surfaceRenderer = Surface(surface)
        this.rendererListener?.onRendererReady()
        updateSurfaceSize()
    }

    override fun onSurfaceTextureSizeChanged(
        surface: SurfaceTexture,
        width: Int,
        height: Int
    ) {
        checkIsOnMainThread()
        Logger.debug(TAG, "onSurfaceTextureSizeChanged(). New size: " + width + "x" + height)
        this.surfaceRenderer = Surface(surface)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        checkIsOnMainThread()
        this.surfaceRenderer = null
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        checkIsOnMainThread()
        val width = MeasureSpec.getSize(widthSpec)
        val height = MeasureSpec.getSize(heightSpec)
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        checkIsOnMainThread()
        updateSurfaceSize()
    }

    private fun updateSurfaceSize() {
        checkIsOnMainThread()
        if (rotatedFrameWidth != 0 && rotatedFrameHeight != 0 && width != 0 && height != 0) {
            adjustAspectRatio(rotatedFrameWidth, rotatedFrameHeight, width, height)
        }
    }

    /**
     * Sets the TextureView transform to preserve the aspect ratio of the video.
     */
    private fun adjustAspectRatio(
        videoWidth: Int, videoHeight: Int, viewWidth: Int, viewHeight: Int
    ) {
        val viewWidth = viewWidth
        val viewHeight = viewHeight
        val aspectRatio = videoHeight.toFloat() / videoWidth
        val newWidth: Int
        val newHeight: Int
        if (viewHeight > (viewWidth * aspectRatio).toInt()) {
            // limited by narrow width  restrict height
            newWidth = viewWidth
            newHeight = (viewWidth * aspectRatio).toInt()
        } else {
            // limited by short height  restrict width
            newWidth = (viewHeight / aspectRatio).toInt()
            newHeight = viewHeight
        }
        // 计算偏移量：居中显示
        val xOffSet = (viewWidth - newWidth) / 2
        val yOffSet = (viewHeight - newHeight) / 2
        Logger.debug(
            TAG,
            "video=" + videoWidth + "x" + videoHeight
                    + " view=" + viewWidth + "x" + viewHeight
                    + " newView=" + newWidth + "x" + newHeight
                    + " off=" + xOffSet + "," + yOffSet,
        )
        val matrix = getTransform(matrix)

        // 计算缩放比例：完整显示视频
        matrix.setScale(newWidth.toFloat() / viewWidth, newHeight.toFloat() / viewHeight)
//        matrix.postRotate(10F)           // just for fun
        matrix.postTranslate(xOffSet.toFloat(), yOffSet.toFloat())
        setTransform(matrix)
    }

    fun setMirror() {
        if (initialized) {
            val matrix = Matrix()
            matrix.setScale(-1.0f, 1.0f)  // 水平翻转
            matrix.postTranslate(this.width.toFloat(), 0.0f) // 平移回可见区域
            this.setTransform(matrix)
        }
    }

    fun onFrameResolutionChanged(videoWidth: Int, videoHeight: Int, rotation: Int) {
        val rotatedWidth = if (rotation == 0 || rotation == 180) videoWidth else videoHeight
        val rotatedHeight = if (rotation == 0 || rotation == 180) videoHeight else videoWidth
        // run immediately if possible for ui thread tests
        postOrRun {
            rotatedFrameWidth = rotatedWidth
            rotatedFrameHeight = rotatedHeight
            updateSurfaceSize()
            requestLayout()
        }
    }

    fun setOnSurfaceListener(rendererListener: OnRendererListener) {
        this.rendererListener = rendererListener
    }

    private fun postOrRun(r: Runnable) {
        if (Thread.currentThread() === Looper.getMainLooper().thread) {
            r.run()
        } else {
            post(r)
        }
    }

    private var rendererListener: OnRendererListener? = null

    interface OnRendererListener {
        fun onRendererReady()
    }

}