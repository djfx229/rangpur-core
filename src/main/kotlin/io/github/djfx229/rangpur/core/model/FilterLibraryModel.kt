package io.github.djfx229.rangpur.core.model

import io.github.djfx229.rangpur.core.repository.database.Database
import io.github.djfx229.rangpur.core.data.Directory

class FilterLibraryModel(private val database: Database) {
    fun getDateList(): List<String> = database.calendar.getDateList()
    fun getYears(): List<String> = database.calendar.getYears()
    fun getMonths(year: String): List<String> = database.calendar.getMonths(year)
    fun getDirectories(onlyFromRoot: Boolean = true): List<Directory> {
        return if (onlyFromRoot) {
            database.directories.getOnlyRoot()
        } else {
            database.directories.getAll()
        }
    }
}