package com.v2box.mobiletina.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.v2box.mobiletina.AppConfig
import com.v2box.mobiletina.core.CoreNativeManager
import com.v2box.mobiletina.dto.RealPingEvent
import com.v2box.mobiletina.dto.TestServiceMessage
import com.v2box.mobiletina.extension.serializable
import com.v2box.mobiletina.handler.MmkvManager
import com.v2box.mobiletina.util.LogUtil
import com.v2box.mobiletina.util.MessageUtil
import java.util.Collections
import java.util.concurrent.atomic.AtomicLong

class CoreTestService : Service() {

    // manage active batch workers so each batch is independent and cancellable
    private val activeWorkers = Collections.synchronizedList(mutableListOf<RealPingWorkerService>())
    private val batchGeneration = AtomicLong(0L)

    /**
     * Initializes the V2Ray environment.
     */
    override fun onCreate() {
        super.onCreate()
        CoreNativeManager.initCoreEnv(this)
    }

    /**
     * Binds the service.
     * @param intent The intent.
     * @return The binder.
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Cleans up resources when the service is destroyed.
     */
    override fun onDestroy() {
        batchGeneration.incrementAndGet()
        LogUtil.i(AppConfig.TAG, "CoreTestService is being destroyed, cancelling ${activeWorkers.size} active workers")
        // cancel any active workers
        val snapshot = ArrayList(activeWorkers)
        snapshot.forEach { it.cancel() }
        activeWorkers.clear()
        super.onDestroy()
    }

    /**
     * Handles the start command for the service.
     * @param intent The intent.
     * @param flags The flags.
     * @param startId The start ID.
     * @return The start mode.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val message = intent?.serializable<TestServiceMessage>("content")
        if (message == null) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        when (message.key) {
            AppConfig.MSG_MEASURE_CONFIG_START -> handleMeasureStart(message, startId)
            AppConfig.MSG_MEASURE_CONFIG_CANCEL -> handleMeasureCancel()
            else -> stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    @Synchronized
    private fun handleMeasureStart(message: TestServiceMessage, startId: Int) {
        LogUtil.i(AppConfig.TAG, "CoreTestService starting worker   subscription ${message.subscriptionId}")
        // A fresh test supersedes callbacks from every previous test.
        val generation = batchGeneration.incrementAndGet()
        ArrayList(activeWorkers).forEach { it.cancel() }
        activeWorkers.clear()

        val guidsList = when {
            message.serverGuids.isNotEmpty() -> message.serverGuids
            message.subscriptionId.isNotEmpty() -> MmkvManager.decodeServerList(message.subscriptionId)
            else -> MmkvManager.decodeAllServerList()
        }

        if (guidsList.isNotEmpty()) {
            MmkvManager.clearAllTestDelayResults(guidsList)
            lateinit var worker: RealPingWorkerService
            worker = RealPingWorkerService(
                context = this,
                guids = guidsList,
                onEvent = { event -> handleWorkerEvent(generation, worker, event) }
            )
            activeWorkers.add(worker)
            worker.start()
        } else {
            stopSelf(startId)
        }
    }

    @Synchronized
    private fun handleWorkerEvent(generation: Long, worker: RealPingWorkerService, event: RealPingEvent) {
        if (generation != batchGeneration.get()) return
        when (event) {
            is RealPingEvent.Progress -> {
                MessageUtil.sendMsg2UI(this, AppConfig.MSG_MEASURE_CONFIG_NOTIFY, event.text)
            }

            is RealPingEvent.Result -> {
                MmkvManager.encodeServerTestDelayMillis(event.guid, event.delayMillis)
                MessageUtil.sendMsg2UI(this, AppConfig.MSG_MEASURE_CONFIG_SUCCESS, event.guid)
            }

            is RealPingEvent.Finish -> {
                if (event.status == "0" &&
                    MmkvManager.decodeSettingsBool(AppConfig.PREF_AUTO_SORT_AFTER_TEST, true)) {
                    MmkvManager.sortServerListsByPing()
                }
                MessageUtil.sendMsg2UI(this, AppConfig.MSG_MEASURE_CONFIG_FINISH, event.status)
                activeWorkers.remove(worker)
                if (activeWorkers.isEmpty()) {
                    stopSelf()
                }
            }
        }
    }

    @Synchronized
    private fun handleMeasureCancel() {
        batchGeneration.incrementAndGet()
        LogUtil.i(AppConfig.TAG, "CoreTestService received cancel message, cancelling ${activeWorkers.size} active workers")
        val snapshot = ArrayList(activeWorkers)
        snapshot.forEach { it.cancel() }
        activeWorkers.clear()
        stopSelf()
    }
}
