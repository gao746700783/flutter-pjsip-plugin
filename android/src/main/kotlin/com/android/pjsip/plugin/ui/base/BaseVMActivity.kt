package com.android.pjsip.plugin.ui.base

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBinding


abstract class BaseVMActivity<VB : ViewBinding> : AppCompatActivity() {
    private lateinit var loadingDialog: Dialog

    /* ==========================================================================================
     * View
     * ========================================================================================== */
    protected lateinit var views: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        views = getBinding()
        setContentView(views.root)
        setupUIStatus(isFullScreen())
        initUiAndData()
        setupToolbar()

    }

    abstract fun getBinding(): VB

    open fun initUiAndData() {}

    open fun setupToolbar() {}

    open fun isFullScreen(): Boolean = false

    private fun exitFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API 30) 及以上
            window.setDecorFitsSystemWindows(true)
            window.decorView.windowInsetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
            window.decorView.windowInsetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
            window.decorView.windowInsetsController?.show(WindowInsetsCompat.Type.statusBars())
            window.decorView.windowInsetsController?.show(WindowInsetsCompat.Type.navigationBars())
            // 可选：设置系统栏行为
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window.decorView.windowInsetsController?.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_DEFAULT
            }
        } else {
            // Android 7.1 (API 25) 及以下
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                        )
            }
        }
    }

    private fun setFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.decorView.windowInsetsController?.hide(WindowInsetsCompat.Type.statusBars())
            window.decorView.windowInsetsController?.hide(WindowInsetsCompat.Type.navigationBars())
            window.decorView.windowInsetsController?.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
            // 隐藏状态栏和导航栏
            windowInsetsController.hide(
                WindowInsetsCompat.Type.statusBars()
                        or WindowInsetsCompat.Type.navigationBars()
            )
            windowInsetsController.isAppearanceLightStatusBars = true
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }

    private fun setupUIStatus(fullscreen: Boolean) {
        if (fullscreen) {
            setFullScreen()
        } else {
            exitFullScreen()
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            // window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                val controller = window.insetsController
//                controller?.setSystemBarsAppearance(
//                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
//                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
//                )
//            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//            }
        }
    }

    companion object {
        val TAG: String = BaseVMActivity::class.java.simpleName
    }

}