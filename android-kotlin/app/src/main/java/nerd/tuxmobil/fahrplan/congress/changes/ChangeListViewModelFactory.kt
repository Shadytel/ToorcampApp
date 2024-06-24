package nerd.tuxmobil.fahrplan.congress.changes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.metadude.android.eventfahrplan.commons.logging.Logging
import org.toorcamp.app.android.Repositories.AppExecutionContext
import org.toorcamp.app.android.Repositories.AppRepository

class ChangeListViewModelFactory(

    private val appRepository: AppRepository

) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val logging = Logging.get()
        @Suppress("UNCHECKED_CAST")
        return ChangeListViewModel(
            repository = appRepository,
            executionContext = AppExecutionContext,
            logging = logging
        ) as T
    }
}
