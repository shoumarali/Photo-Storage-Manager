package com.alishoumar.androidstorage.di

import com.alishoumar.androidstorage.data.repository.ExternalStorageRepositoryImpl
import com.alishoumar.androidstorage.data.repository.InternalStorageRepositoryImpl
import com.alishoumar.androidstorage.domain.repository.ExternalStorageRepository
import com.alishoumar.androidstorage.domain.repository.InternalStorageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn (SingletonComponent::class)
abstract class RepositoryModule {


    @Binds
    @Singleton
    abstract fun bindInternalStorageRepository(
        internalStorageRepositoryImpl: InternalStorageRepositoryImpl
    ):InternalStorageRepository

    @Binds
    @Singleton
    abstract fun bindExternalStorageRepository(
        externalStorageRepositoryImpl: ExternalStorageRepositoryImpl
    ): ExternalStorageRepository

}