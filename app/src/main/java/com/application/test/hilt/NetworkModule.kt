package com.application.test.hilt

import android.app.Application
import android.content.Context
import com.application.test.repositories.api.AppService

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.SSLContext

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }

    @Singleton
    @Provides
    fun getRetrofit(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://router.project-osrm.org")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(gsonConverterFactory)
            .build()
    }

//    @Singleton
//    @Provides
//    @Named("search")
//    fun getSearchRetrofit(
//        okHttpClient: OkHttpClient,
//        gsonConverterFactory: GsonConverterFactory
//    ): Retrofit {
//        return Retrofit.Builder()
//            .client(okHttpClient)
//            .baseUrl(Constants.BASE_URL_SEARCH)
//            .addConverterFactory(ScalarsConverterFactory.create())
//            .addConverterFactory(gsonConverterFactory)
//            .build()
//    }

    @Singleton
    @Provides
    @Named("searchAppService")
    fun provideSearchAppService(@Named("search") retrofit: Retrofit): AppService {
        return retrofit
            .create(AppService::class.java)
    }

    @Singleton
    @Provides
    fun provideAppService(retrofit: Retrofit): AppService {
        return retrofit
            .create(AppService::class.java)
    }


    @Singleton
    @Provides
    fun httpLoggingInterceptor(): HttpLoggingInterceptor {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
//        httpLoggingInterceptor.level =
//            if (DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
        return httpLoggingInterceptor
    }


    @Singleton
    @Provides
    fun gsonConverterFactory(gson: Gson): GsonConverterFactory {
        return GsonConverterFactory.create(gson)
    }


    @Singleton
    @Provides
    fun gson(): Gson {
        return Gson()
    }


    @Singleton
    @Provides
    fun okHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        provideContext: Context
    ): OkHttpClient {
//        trustEveryone()
        initializeSSLContext()
        return OkHttpClient()
            .newBuilder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val builder = original.newBuilder()
//                if (Hawk.get<String>(Constants.HawKData.TOKEN) != null) {
//                    builder.header(
//                        "x-auth-token",
//                        Hawk.get<String>(
//                            Constants.HawKData.TOKEN
//                        ) as String
//                    )
//                }
                val request = builder.build()
                chain.proceed(request)
            }
            .addInterceptor { chain ->
                val original = chain.request()
                val builder = original.newBuilder()
                    .header(
                        "x-platform", "1"

                    )
                val request = builder.build()
                chain.proceed(request)
            }
            .addInterceptor(httpLoggingInterceptor)
//            .addInterceptor(ChuckInterceptor(provideContext))
            .cache(null)
            .build()
    }



    fun initializeSSLContext() {
        try {
            SSLContext.getInstance("TLSv1.2")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }




}