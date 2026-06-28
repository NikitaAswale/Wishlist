package com.example.wishlist.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WishRepository @Inject constructor(private val wishDao: WishDao) {

    val allWishes: Flow<List<Wish>> = wishDao.getAllWishes()
    val activeWishes: Flow<List<Wish>> = wishDao.getActiveWishes()
    val fulfilledWishes: Flow<List<Wish>> = wishDao.getFulfilledWishes()

    suspend fun getWishById(id: Long): Wish? = wishDao.getWishById(id)

    suspend fun insert(wish: Wish): Long = wishDao.insert(wish)

    suspend fun update(wish: Wish) = wishDao.update(wish)

    suspend fun delete(wish: Wish) = wishDao.delete(wish)

    suspend fun setFulfilled(id: Long, fulfilled: Boolean) = wishDao.setFulfilled(id, fulfilled)
}
