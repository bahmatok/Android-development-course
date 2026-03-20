package com.example.timer.presentation.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Timer : Screen("timer/{sequenceId}") {
        fun createRoute(id: String) = "timer/$id"
    }
    object Edit : Screen("edit?sequenceId={sequenceId}") {
        // sequenceId опциональный: если null — создаем новый, если есть — редактируем
        fun createRoute(id: String?) = if (id != null) "edit?sequenceId=$id" else "edit"
    }
    object Settings : Screen("settings")
}