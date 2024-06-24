package nerd.tuxmobil.fahrplan.congress.details

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import org.toorcamp.app.android.R
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity
import nerd.tuxmobil.fahrplan.congress.utils.showWhenLockedCompat

class SessionDetailsActivity : BaseActivity(R.layout.detail_frame) {

    companion object {

        const val REQUEST_CODE = 2

        fun start(activity: Activity) {
            activity.startActivity(createIntent(activity))
        }

        fun startForResult(activity: Activity) {
            activity.startActivityForResult(createIntent(activity), REQUEST_CODE)
        }

        private fun createIntent(activity: Activity) =
            Intent(activity, SessionDetailsActivity::class.java)

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        showWhenLockedCompat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val actionBarColor = ContextCompat.getColor(this, R.color.colorActionBar)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(actionBarColor))

        val intent = this.intent
        if (intent == null) {
            finish()
        }
        if (intent != null && findViewById<View?>(R.id.detail) != null) {
            SessionDetailsFragment.replace(supportFragmentManager, R.id.detail)
        }
    }

}
