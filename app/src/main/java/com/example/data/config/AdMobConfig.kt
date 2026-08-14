package com.example.data.config

import com.example.BuildConfig

object AdMobConfig {
    /**
     * AdMob Application ID (configured in AndroidManifest.xml)
     */
    const val APP_ID = "ca-app-pub-4198915485397168~4031510238"

    /**
     * Google's official Native Advanced Test Ad Unit ID
     */
    const val TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

    /**
     * Live Production Native Advanced Ad Unit ID
     */
    const val PRODUCTION_NATIVE_AD_UNIT_ID = "ca-app-pub-4198915485397168/3145803976"

    /**
     * Returns the active Native Ad Unit ID.
     * Uses [TEST_NATIVE_AD_UNIT_ID] in Debug builds to prevent invalid traffic policy violations,
     * and switches to [PRODUCTION_NATIVE_AD_UNIT_ID] in Release builds.
     */
    val NATIVE_AD_UNIT_ID: String
        get() = if (BuildConfig.DEBUG) TEST_NATIVE_AD_UNIT_ID else PRODUCTION_NATIVE_AD_UNIT_ID
}
