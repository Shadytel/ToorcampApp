package nerd.tuxmobil.fahrplan.congress.repositories

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

object AppExecutionContext : ExecutionContext {

    override val ui: CoroutineDispatcher = Main
    override val network: CoroutineDispatcher = IO
    override val database: CoroutineDispatcher = IO

}
