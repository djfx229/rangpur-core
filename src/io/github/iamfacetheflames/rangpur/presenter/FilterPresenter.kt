package io.github.iamfacetheflames.rangpur.presenter

import io.github.iamfacetheflames.rangpur.data.Directory
import io.github.iamfacetheflames.rangpur.model.Models
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

const val DATE_ALL = "All"
const val DIRECTORY_ALL = "All"

class FilterPresenter(val scope: CoroutineScope, private val models: Models) {

    private val flowDateList = MutableStateFlow<List<String>>(emptyList())
    private val flowDirectories = MutableStateFlow<List<Directory>>(emptyList())

    fun observableDateList(): StateFlow<List<String>> = flowDateList
    fun observableDirectories(): StateFlow<List<Directory>> = flowDirectories

    fun requestData(isDateOnlyYears: Boolean = false) {
        if (isDateOnlyYears) {
            requestFilterDateList()
        } else {
            requestFilterFullDateList()
        }
        requestFilterDirectories()
    }

    private fun requestFilterDateList(year: String? = null)  {
        val list = if (year == null) {
            models.filterLibrary.getYears()
        } else {
            models.filterLibrary.getMonths(year)
        }
        flowDateList.value = list
    }

    private fun requestFilterFullDateList()  {
        scope.launch(Dispatchers.IO) {
            val dates = mutableListOf<String>().apply {
                addAll(models.filterLibrary.getDateList())
            }
            flowDateList.value = dates
        }
    }

    private fun requestFilterDirectories(root: Directory? = null) {
        val list = if (root == null) {
            models.filterLibrary.getDirectories()
        } else {
            models.filterLibrary.getDirectories(root)
        }
        flowDirectories.value = list
    }

}