package com.example.wishlist.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Calendar : Screen("calendar")
    data object AddWish : Screen("add_wish")
    data object EditWish : Screen("edit_wish/{wishId}") {
        fun createRoute(wishId: Long) = "edit_wish/$wishId"
    }
    data object WishDetail : Screen("wish_detail/{wishId}") {
        fun createRoute(wishId: Long) = "wish_detail/$wishId"
    }
}
