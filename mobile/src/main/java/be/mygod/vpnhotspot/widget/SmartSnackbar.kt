package be.mygod.vpnhotspot.widget

import android.annotation.SuppressLint
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import be.mygod.vpnhotspot.App.Companion.app
import be.mygod.vpnhotspot.util.readableMessage
import com.google.android.material.snackbar.Snackbar
import com.topjohnwu.superuser.NoShellException

sealed class SmartSnackbar {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var holder: View? = null

        fun make(@StringRes text: Int): SmartSnackbar = make(app.getText(text))
        fun make(text: CharSequence = ""): SmartSnackbar {
            val holder = holder
            return if (holder == null) @SuppressLint("ShowToast") {
                if (Looper.myLooper() == null) Looper.prepare()
                ToastWrapper(Toast.makeText(app, text, Toast.LENGTH_LONG))
            } else SnackbarWrapper(Snackbar.make(holder, text, Snackbar.LENGTH_LONG))
        }
        fun make(e: Throwable) = make(when (e) {
            is NoShellException -> e.cause ?: e
            else -> e
        }.readableMessage)
    }

    class Register(lifecycle: Lifecycle, private val view: View) : DefaultLifecycleObserver {
        init {
            lifecycle.addObserver(this)
        }

        override fun onResume(owner: LifecycleOwner) {
            holder = view
        }
        override fun onPause(owner: LifecycleOwner) {
            if (holder === view) holder = null
        }
    }

    abstract fun show()
    open fun shortToast() = this
}

private class SnackbarWrapper(private val snackbar: Snackbar) : SmartSnackbar() {
    override fun show() = snackbar.show()
}

private class ToastWrapper(private val toast: Toast) : SmartSnackbar() {
    override fun show() = toast.show()

    override fun shortToast() = apply { toast.duration = Toast.LENGTH_SHORT }
}
