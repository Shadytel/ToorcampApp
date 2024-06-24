package nerd.tuxmobil.fahrplan.congress.utils

import android.util.Patterns
import androidx.annotation.StringRes
import org.toorcamp.app.android.R

class UrlValidator(

        private val url: String

) : Validation {

    @StringRes
    private var errorMessage: Int? = null

    @StringRes
    override fun getErrorMessage(): Int? = errorMessage

    override fun isValid(): Boolean {
        val matches = Patterns.WEB_URL.matcher(url).matches()
        errorMessage = if (matches) null else R.string.validation_error_invalid_url
        return matches
    }

}
