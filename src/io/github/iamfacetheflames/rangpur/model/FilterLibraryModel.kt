package io.github.iamfacetheflames.rangpur.model

import io.github.iamfacetheflames.rangpur.repository.Database
import io.github.iamfacetheflames.rangpur.data.Directory

class FilterLibraryModel(val database: Database) {
    fun getDateList(): List<String> = database.getDateList()
    fun getYears(): List<String> = database.getYears()
    fun getMonths(year: String): List<String> = database.getMonths(year)
    fun getDirectories(): List<Directory> = database.getRootDirectories()
    fun getDirectories(root: Directory): List<Directory> = database.getDirectories(root)
}