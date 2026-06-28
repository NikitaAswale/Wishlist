package com.example.wishlist.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WishDao {

    @Query("SELECT * FROM wishes ORDER BY targetDate ASC")
    fun getAllWishes(): Flow<List<Wish>>

    @Query("SELECT * FROM wishes WHERE id = :id")
    suspend fun getWishById(id: Long): Wish?

    @Query("SELECT * FROM wishes WHERE isFulfilled = 0 ORDER BY targetDate ASC")
    fun getActiveWishes(): Flow<List<Wish>>

    @Query("SELECT * FROM wishes WHERE isFulfilled = 0 ORDER BY targetDate ASC")
    suspend fun getActiveWishesList(): List<Wish>

    @Query("SELECT * FROM wishes WHERE isFulfilled = 1 ORDER BY targetDate DESC")
    fun getFulfilledWishes(): Flow<List<Wish>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wish: Wish): Long

    @Update
    suspend fun update(wish: Wish)

    @Delete
    suspend fun delete(wish: Wish)

    @Query("UPDATE wishes SET isFulfilled = :fulfilled WHERE id = :id")
    suspend fun setFulfilled(id: Long, fulfilled: Boolean)
}
