package com.atomgroups.progressbarsample

import com.atomgroups.progressbar.ProgressObjectValue

data class ModelState(
    private val label: String,
    private val value: Int,
    private val color: String
): ProgressObjectValue {
    override fun getLabel(): String {
        return label
    }

    override fun getValue(): Int {
        return value
    }

    override fun getSubLabel(): String {
        return "$value points"
    }
}