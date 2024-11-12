package kmp.project.schedule.db

import kmp.project.schedule.database.ScheduleDatabase

internal class Database (databaseDriverFactory: DatabaseDriverFactory){
    private val database = ScheduleDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.scheduleQueries

}