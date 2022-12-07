package com.example.padec_device_info

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import android.util.Log
import org.junit.Rule
import java.util.concurrent.TimeUnit.*
import androidx.test.rule.ActivityTestRule
import java.util.concurrent.*


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
        @get:Rule
        var mMainActivityRule = ActivityTestRule(
            MainActivity::class.java
        )

        @Test
        fun devoceInfoTest() {
            Log.d(
                "#############",
                "Here is test"
            )
            val scheduler = Executors.newScheduledThreadPool(1)

            val runner = Runnable {
                mMainActivityRule.runOnUiThread {
                    kotlin.run {
                        Log.d(
                            "#############",
                            mMainActivityRule.activity.getDeviceInfo()
                        )
                    }
                }
            }

            val beeperHandle = scheduler.scheduleAtFixedRate(runner, 0, 10, SECONDS)
            scheduler.schedule({ beeperHandle.cancel(true) }, 60 * 60, SECONDS)

            val lock: CountDownLatch = CountDownLatch(1)
            lock.await(60*1000, TimeUnit.MILLISECONDS)
        }
}
