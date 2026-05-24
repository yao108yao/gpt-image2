package com.example.gptimage2.domain.model

enum class Quality(val label: String, val apiValue: String) {
    AUTO("自动", "auto"),
    LOW("低", "low"),
    MEDIUM("中", "medium"),
    HIGH("高", "high")
}
