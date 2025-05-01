package io.github.djfx229.rangpur.common.presentation

class CheckItemInAdapter<Item>(
    val onSelect: (List<Item>) -> Unit,
    val onAll: () -> Unit,
) {

    var checkedItems: MutableList<Item> = mutableListOf()

    var isChecked: ((Item) -> Boolean) = {
        checkedItems.contains(it)
    }

    var onChecked: ((Item, Boolean) -> Unit) = { item, isChecked ->
        if (isChecked) {
            checkedItems.add(item)
        } else {
            checkedItems.remove(item)
        }
        if (checkedItems.isEmpty()) {
            onAll()
        } else {
            onSelect(checkedItems)
        }
    }

    fun reset() {
        checkedItems = mutableListOf()
        onAll()
    }

    fun invert(allItems: List<Item>) {
        val newCheckedItems = mutableListOf<Item>()
        allItems.forEach { item ->
            if (!checkedItems.contains(item)) {
                newCheckedItems.add(item)
            }
        }
        checkedItems = newCheckedItems
        onSelect(checkedItems)
    }

}