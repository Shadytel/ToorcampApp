package nerd.tuxmobil.fahrplan.congress.models

import info.metadude.android.eventfahrplan.database.contract.FahrplanContract

data class Alarm(

        val id: Int,
        val alarmTimeInMin: Int,
        val day: Int,
        val displayTime: Long,
        val sessionId: String,
        val sessionTitle: String,
        val startTime: Long,
        val timeText: String

) {

    constructor(
            alarmTimeInMin: Int,
            day: Int,
            displayTime: Long,
            sessionId: String,
            sessionTitle: String,
            startTime: Long,
            timeText: String
    ) : this(
            DEFAULT_VALUE_ID,
            alarmTimeInMin,
            day,
            displayTime,
            sessionId,
            sessionTitle,
            startTime,
            timeText
    )

    companion object {
        const val DEFAULT_VALUE_ID = FahrplanContract.AlarmsTable.Defaults.DEFAULT_VALUE_ID
    }

}
