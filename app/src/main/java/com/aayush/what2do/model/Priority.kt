package com.aayush.what2do.model

enum class Priority(private val priority: String, val priorityNumber: Int) {
    LOW("Low", 0),
    MEDIUM("Medium", 1),
    HIGH("High", 2);

    override fun toString(): String {
        return priority
    }
}