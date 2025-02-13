package com.alishoumar.androidstorage.di

import android.app.Application
import com.alishoumar.androidstorage.data.local.storage.InternalStoragePhotoApi
import com.alishoumar.androidstorage.domain.repository.InternalStorageRepository
import com.alishoumar.androidstorage.domain.usecases.DeletePhotoFromInternalStorageUseCase
import com.alishoumar.androidstorage.domain.usecases.LoadPhotosFromInternalStorageUseCase
import com.alishoumar.androidstorage.domain.usecases.SavePhotoInternalStorageUseCase
import com.alishoumar.androidstorage.domain.usecases.StorageUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideInternalStorageApi(application: Application):InternalStoragePhotoApi {
        return InternalStoragePhotoApi(application = application)
    }

    @Singleton
    @Provides
    fun provideLoadPhotosFromInternalStorageUseCase(
        internalStorageRepository: InternalStorageRepository
    ):LoadPhotosFromInternalStorageUseCase{
        return LoadPhotosFromInternalStorageUseCase(
            internalStorageRepository
        )
    }

    @Singleton
    @Provides
    fun provideDeletePhotosFromInternalStorageUseCase(
        internalStorageRepository: InternalStorageRepository
    ): DeletePhotoFromInternalStorageUseCase{
        return DeletePhotoFromInternalStorageUseCase(internalStorageRepository)
    }

    @Singleton
    @Provides
    fun provideSavePhotoToInternalStorageUseCase(
        internalStorageRepository: InternalStorageRepository
    ):SavePhotoInternalStorageUseCase{
        return SavePhotoInternalStorageUseCase(internalStorageRepository)
    }

    @Singleton
    @Provides
    fun provideStorageUseCases(
        loadPhotosFromInternalStorageUseCase: LoadPhotosFromInternalStorageUseCase,
        savePhotoInternalStorageUseCase: SavePhotoInternalStorageUseCase,
        deletePhotoFromInternalStorageUseCase: DeletePhotoFromInternalStorageUseCase
    ):StorageUseCases{
        return StorageUseCases(
            loadPhotosFromInternalStorageUseCase,
            savePhotoInternalStorageUseCase,
            deletePhotoFromInternalStorageUseCase
        )
    }
}