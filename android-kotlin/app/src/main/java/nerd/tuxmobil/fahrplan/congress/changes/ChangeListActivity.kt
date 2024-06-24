package nerd.tuxmobil.fahrplan.congress.changes

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import org.toorcamp.app.android.R
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment.OnSessionListClick
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsActivity
import org.toorcamp.app.android.Repositories.AppRepository

class ChangeListActivity :
    BaseActivity(),
    OnSessionListClick {

    companion object {

        fun start(activity: Activity) {
            val intent = Intent(activity, ChangeListActivity::class.java)
            activity.startActivity(intent)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_list)
        //initToolbar()
        if (savedInstanceState == null) {
            val fragment = ChangeListFragment.newInstance(sidePane = false)
            addFragment(R.id.container, fragment, ChangeListFragment.FRAGMENT_TAG)
        }
    }

    private fun initToolbar() {
        val toolbar = requireViewByIdCompat<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBarColor = ContextCompat.getColor(this, R.color.colorActionBar)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(actionBarColor))
    }

    override fun onSessionListClick(sessionId: String) {
        if (AppRepository.updateSelectedSessionId(sessionId)) {
            SessionDetailsActivity.start(this)
        }
    }

}
