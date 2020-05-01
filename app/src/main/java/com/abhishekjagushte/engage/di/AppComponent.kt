package com.abhishekjagushte.engage.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.abhishekjagushte.engage.EngageApplication
import com.abhishekjagushte.engage.repository.DataRepository
import com.abhishekjagushte.engage.ui.setup.fragments.login.di.LoginComponent
import com.abhishekjagushte.engage.ui.setup.fragments.setusername.di.SetUsernameComponent
import com.abhishekjagushte.engage.ui.setup.fragments.signup.di.SignUpComponent
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton


//TODO if this doent work use the support library dagger-android-support

@Singleton
@Component(modules =
        [LocalStorageModule::class,
        NetworkModule::class, ViewModelBuilderModule::class,
        SubcomponentsModule::class])
interface AppComponent: AndroidInjector<EngageApplication>{

    @Component.Factory
    interface Factory{
        fun create(@BindsInstance applicationContext: Context): AppComponent
    }

    fun addLoginComponent(): LoginComponent.Factory
    fun addSignUpComponent(): SignUpComponent.Factory
    fun addSetUsernameComponent(): SetUsernameComponent.Factory
    fun getDataRepository(): DataRepository
}

@Module
abstract class ViewModelBuilderModule {

    @Binds
    abstract fun bindViewModelFactory(
        factory: AppViewModelFactory
    ): ViewModelProvider.Factory
}


@Module(subcomponents = [LoginComponent::class, SignUpComponent::class, SetUsernameComponent::class])
object SubcomponentsModule




//    @Component.Builder
//    interface Builder{
//        @BindsInstance
//        fun application(application: Application)
//        fun build(): AppComponent
//    }