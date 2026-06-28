package com.example.wishlist.di

import android.content.Context
import androidx.room.Room
import com.example.wishlist.data.WishDao
import com.example.wishlist.data.WishDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWishDatabase(@ApplicationContext context: Context): WishDatabase {
        return Room.databaseBuilder(
            context,
            WishDatabase::class.java,
            "wishlist_database"
        ).build()
    }

    @Provides
    fun provideWishDao(database: WishDatabase): WishDao {
        return database.wishDao()
    }
}
