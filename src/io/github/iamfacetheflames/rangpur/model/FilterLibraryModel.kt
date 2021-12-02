package io.github.iamfacetheflames.rangpur.model

import io.github.iamfacetheflames.rangpur.module.database.Database
import io.github.iamfacetheflames.rangpur.data.Directory

class FilterLibraryModel(val database: Database) {
    fun getDateList(): List<String> = database.calendar.getDateList()
    fun getYears(): List<String> = database.calendar.getYears()
    fun getMonths(year: String): List<String> = database.calendar.getMonths(year)
    fun getDirectories(): List<Directory> = database.getRootDirectories()
    fun getDirectories(root: Directory): List<Directory> = database.getDirectories(root)
}