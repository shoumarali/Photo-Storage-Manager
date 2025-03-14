package com.alishoumar.androidstorage.di

import android.app.Application
import com.alishoumar.androidstorage.data.local.storage.ExternalStoragePhotoApi
import com.alishoumar.androidstorage.data.local.storage.InternalStoragePhotoApi
import com.alishoumar.androidstorage.data.utils.CryptoManager
import com.alishoumar.androidstorage.domain.repository.ExternalStorageRepository
import com.alishoumar.androidstorage.domain.repository.InternalStorageRepository
import com.alishoumar.androidstorage.domain.usecases.InternalStorage.InternalStorageUseCases
import com.alishoumar.androidstorage.domain.usecases.InternalStorage.DeletePhotoFromInternalStorageUseCase
import com.alishoumar.androidstorage.domain.usecases.InternalStorage.LoadPhotosFromInternalStorageUseCase
import com.alishoumar.androidstorage.domain.usecases.InternalStorage.SavePhotoInternalStorageUseCase
import com.alishoumar.androidstorage.domain.usecases.externalStorage.DeletePhotoFromExternalStorageUseCase
import com.alishoumar.androidstorage.domain.usecases.externalStorage.LoadPhotosFromExternalStorageUseCase
import com.alishoumar.androidstorage.domain.usecases.externalStorage.SavePhotoToExternalStorageUseCase
import com.alishoumar.androidstorage.domain.usecases.permissions.GetUnGrantedPermissionsUseCase
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
    fun provideCryptoManager(): CryptoManager{
        return CryptoManager()
    }

    @Singleton
    @Provides
    fun provideInternalStorageApi(
        application: Application,
        cryptoManager: CryptoManager
    ):InternalStoragePhotoApi {
        return InternalStoragePhotoApi(application = application, cryptoManager)
    }

    @Singleton
    @Provides
    fun provideExternalStorageApi(application: Application): ExternalStoragePhotoApi {
        return ExternalStoragePhotoApi(application)
    }

    @Singleton
    @Provides
    fun provideLoadPhotosFromInternalStorageUseCase(
        internalStorageRepository: InternalStorageRepository
    ): LoadPhotosFromInternalStorageUseCase {
        return LoadPhotosFromInternalStorageUseCase(
            internalStorageRepository
        )
    }

    @Singleton
    @Provides
    fun provideDeletePhotosFromInternalStorageUseCase(
        internalStorageRepository: InternalStorageRepository
    ): DeletePhotoFromInternalStorageUseCase {
        return DeletePhotoFromInternalStorageUseCase(internalStorageRepository)
    }

    @Singleton
    @Provides
    fun provideSavePhotoToInternalStorageUseCase(
        internalStorageRepository: InternalStorageRepository
    ): SavePhotoInternalStorageUseCase {
        return SavePhotoInternalStorageUseCase(internalStorageRepository)
    }

    @Singleton
    @Provides
    fun provideInternalStorageUseCases(
        loadPhotosFromInternalStorageUseCase: LoadPhotosFromInternalStorageUseCase,
        savePhotoInternalStorageUseCase: SavePhotoInternalStorageUseCase,
        deletePhotoFromInternalStorageUseCase: DeletePhotoFromInternalStorageUseCase
    ): InternalStorageUseCases {
        return InternalStorageUseCases(
            loadPhotosFromInternalStorageUseCase,
            savePhotoInternalStorageUseCase,
            deletePhotoFromInternalStorageUseCase
        )
    }

    @Singleton
    @Provides
    fun provideLoadPhotosFromExternalStorageUseCase(
        externalStorageRepository: ExternalStorageRepository
    ): LoadPhotosFromExternalStorageUseCase{
        return LoadPhotosFromExternalStorageUseCase(externalStorageRepository)
    }

    @Singleton
    @Provides
    fun provideSavePhotoToExternalStorageUseCase(
        externalStorageRepository: ExternalStorageRepository
    ):SavePhotoToExternalStorageUseCase{
        return SavePhotoToExternalStorageUseCase(externalStorageRepository)
    }

    @Singleton
    @Provides
    fun provideDeletePhotoFromExternalStorageUseCase(
        externalStorageRepository: ExternalStorageRepository
    ): DeletePhotoFromExternalStorageUseCase{
        return DeletePhotoFromExternalStorageUseCase(externalStorageRepository)
    }

    @Singleton
    @Provides
    fun provideGetUnGrantedPermissions(
        application: Application
    ):GetUnGrantedPermissionsUseCase{
        return GetUnGrantedPermissionsUseCase(application)
    }
}