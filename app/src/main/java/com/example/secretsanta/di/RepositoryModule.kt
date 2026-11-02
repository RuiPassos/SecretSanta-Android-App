package com.example.secretsanta.di

import com.example.secretsanta.data.repository.OfflineGroupRepository
import com.example.secretsanta.domain.repository.GroupRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindGroupRepository(impl: OfflineGroupRepository): GroupRepository
}
