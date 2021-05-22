package io.github.iamfacetheflames.rangpur.presenter

import io.github.iamfacetheflames.rangpur.data.Directory
import io.github.iamfacetheflames.rangpur.model.Models
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

const val DATE_ALL = "All"
const val DIRECTORY_ALL = "All"

class FilterPresenter(val scope: CoroutineScope, val models: Models) {

//    var actionsDateList: Subject<Action<String>> =
//        PublishSubject.create()
//    var actionsDirectories: Subject<Action<Directory>> =
//        PublishSubject.create()

    private fun requestFilterDateList(year: String? = null)  {
        val list = if (year == null) {
            models.filterLibrary.getYears()
        } else {
            models.filterLibrary.getMonths(year)
        }
//        observableFilterDateList.onNext(list)
    }

    private fun requestFilterFullDateList()  {
        scope.launch {
            val dates = mutableListOf<String>().apply {
                addAll(models.filterLibrary.getDateList())
            }
            flowFullDateList.value = dates
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

//    var observableFilterDateList: Subject<List<String>> =
//        BehaviorSubject.create()
//    var observableFilterDirectories: Subject<List<Directory>> =
//        BehaviorSubject.create()

    private val flowFullDateList = MutableStateFlow<List<String>>(emptyList())
    private val flowDirectories = MutableStateFlow<List<Directory>>(emptyList())

    fun observableFullDateList(): StateFlow<List<String>> = flowFullDateList
    fun observableDirectories(): StateFlow<List<Directory>> = flowDirectories

    fun requestData() {
        requestFilterDateList()
        requestFilterFullDateList()
        requestFilterDirectories()
    }

    init {
//        actionsDateList.observeOn(Schedulers.io())
//            .subscribe { action ->
//                when (action) {
//                    is ActionSelect -> {
//                        if (action.state == Select.CHECKED) {
//                            checked(action.item)
//                        } else {
//                            unchecked(action.item)
//                        }
//                    }
//                    is ActionClick -> requestFilterDateList(action.item)
//                }
//            }
//        actionsDirectories.observeOn(Schedulers.io())
//            .subscribe { action ->
//                when (action) {
//                    is ActionSelect -> {
//                        if (action.state == Select.CHECKED) {
//                            checked(action.item)
//                        } else {
//                            unchecked(action.item)
//                        }
//                    }
//                    else -> {}
//                }
//            }
    }

//    fun checked(date: String) {
//        models.audioLibraryModel.filter.dateList.add(date)
//    }
//
//    fun checked(directory: Directory) {
//        models.audioLibraryModel.filter.directories.add(directory)
//    }
//
//    fun unchecked(date: String) {
//        models.audioLibraryModel.filter.dateList.remove(date)
//    }
//
//    fun unchecked(directory: Directory) {
//        models.audioLibraryModel.filter.directories.remove(directory)
//    }
//
//    fun isCheckedFilter(date: String): Boolean {
//        return models.audioLibraryModel.filter.dateList.contains(date)
//    }
//
//    fun isCheckedFilter(directory: Directory): Boolean {
//        return models.audioLibraryModel.filter.directories.contains(directory)
//    }

}