package com.v2box.mobiletina.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.net.TrafficStats
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.os.SystemClock
import android.text.format.Formatter
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import com.v2box.mobiletina.AppConfig
import com.v2box.mobiletina.R
import com.v2box.mobiletina.core.CoreServiceManager
import com.v2box.mobiletina.databinding.ActivityMainBinding
import com.v2box.mobiletina.databinding.LayoutV2boxGroupCardBinding
import com.v2box.mobiletina.dto.GroupMapItem
import com.v2box.mobiletina.dto.TestServiceMessage
import com.v2box.mobiletina.enums.EConfigType
import com.v2box.mobiletina.enums.PermissionType
import com.v2box.mobiletina.extension.toast
import com.v2box.mobiletina.extension.toastError
import com.v2box.mobiletina.handler.AngConfigManager
import com.v2box.mobiletina.handler.MmkvManager
import com.v2box.mobiletina.handler.SettingsChangeManager
import com.v2box.mobiletina.handler.SettingsManager
import com.v2box.mobiletina.handler.SubscriptionUpdater
import com.v2box.mobiletina.handler.V2BoxSubscriptionInfo
import com.v2box.mobiletina.util.LogUtil
import com.v2box.mobiletina.util.InstagramLink
import com.v2box.mobiletina.util.MessageUtil
import com.v2box.mobiletina.util.Utils
import com.v2box.mobiletina.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import java.text.DateFormat
import java.util.Date
import java.util.UUID

class MainActivity : HelperBaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    val mainViewModel: MainViewModel by viewModels()
    private lateinit var groupPagerAdapter: GroupPagerAdapter
    private var tabMediator: TabLayoutMediator? = null
    private var smartConnectJob: Job? = null
    private var statsJob: Job? = null
    private var connectedAt = 0L
    private var uploadedAt = 0L
    private var downloadedAt = 0L

    private val requestVpnPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            startV2Ray()
        } else {
            applyRunningState(false, false)
        }
    }
    private val requestActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (SettingsChangeManager.consumeRestartService() && mainViewModel.isRunning.value == true) {
            restartV2Ray()
        }
        if (SettingsChangeManager.consumeSetupGroupTab()) {
            setupGroupTab()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupToolbar(binding.toolbar, false, getString(R.string.app_name))

        // setup viewpager and tablayout
        groupPagerAdapter = GroupPagerAdapter(this, emptyList())
        binding.viewPager.adapter = groupPagerAdapter
        binding.viewPager.isUserInputEnabled = true
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (groupPagerAdapter.groups.isNotEmpty()) {
                    renderGroupCards(groupPagerAdapter.groups)
                    refreshSubscriptionInfo()
                }
            }
        })

        // setup navigation drawer
        setupNavigationDrawer()

        binding.fab.setOnClickListener { handleFabAction() }
        binding.btnConnect.setOnClickListener { handleFabAction() }
        binding.layoutTest.setOnClickListener { handleLayoutTestClick() }
        binding.switchSmart.isChecked = MmkvManager.decodeSettingsBool(AppConfig.PREF_SMART_CONNECT, true)
        binding.switchSmart.setOnCheckedChangeListener { _, enabled ->
            MmkvManager.encodeSettings(AppConfig.PREF_SMART_CONNECT, enabled)
            binding.switchSmartSettings.isChecked = enabled
            if (mainViewModel.isRunning.value != true) updateConnectButtonLabel()
            if (!enabled) {
                smartConnectJob?.cancel()
                MessageUtil.sendMsg2TestService(this, TestServiceMessage(key = AppConfig.MSG_MEASURE_CONFIG_CANCEL))
            }
        }
        binding.switchAutoSort.isChecked = MmkvManager.decodeSettingsBool(AppConfig.PREF_AUTO_SORT_AFTER_TEST, true)
        binding.switchAutoSort.setOnCheckedChangeListener { _, enabled ->
            MmkvManager.encodeSettings(AppConfig.PREF_AUTO_SORT_AFTER_TEST, enabled)
            if (enabled) lifecycleScope.launch(Dispatchers.IO) {
                mainViewModel.sortByTestResults()
                withContext(Dispatchers.Main) { mainViewModel.reloadServerList() }
            }
        }
        binding.switchSmartSettings.isChecked = binding.switchSmart.isChecked
        binding.switchSmartSettings.setOnCheckedChangeListener { _, enabled -> binding.switchSmart.isChecked = enabled }
        binding.switchSubUpdate.isChecked = MmkvManager.decodeSettingsBool(AppConfig.PREF_SUB_UPDATE_ON_START, true)
        binding.switchSubUpdate.setOnCheckedChangeListener { _, enabled ->
            MmkvManager.encodeSettings(AppConfig.PREF_SUB_UPDATE_ON_START, enabled)
        }
        binding.btnPingAll.setOnClickListener { mainViewModel.testAllRealPing() }
        binding.btnSortPing.setOnClickListener { sortByTestResults() }
        binding.rowLanguage.setOnClickListener { openAdvancedSettings() }
        binding.rowSubscriptionInfo.setOnClickListener {
            requestActivityLauncher.launch(Intent(this, SubSettingActivity::class.java))
        }
        binding.rowTunnel.setOnClickListener { openAdvancedSettings() }
        binding.rowDns.setOnClickListener { openAdvancedSettings() }
        binding.rowRoute.setOnClickListener {
            requestActivityLauncher.launch(Intent(this, RoutingSettingActivity::class.java))
        }
        binding.rowSubSettings.setOnClickListener {
            requestActivityLauncher.launch(Intent(this, SubSettingActivity::class.java))
        }
        binding.rowSpeed.setOnClickListener { openAdvancedSettings() }
        binding.rowAbout.setOnClickListener { startActivity(Intent(this, AboutActivity::class.java)) }
        val deviceId = MmkvManager.decodeSettingsString("v2box_device_id")
            ?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString().also {
                MmkvManager.encodeSettings("v2box_device_id", it)
            }
        binding.tvDeviceId.text = "${getString(R.string.v2box_device_id)}: $deviceId"
        binding.tvDeviceId.setOnClickListener { Utils.setClipboard(this, deviceId) }
        binding.btnRouting.setOnClickListener {
            requestActivityLauncher.launch(Intent(this, RoutingSettingActivity::class.java))
        }
        binding.btnInstagram.setOnClickListener { InstagramLink.open(this) }
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    binding.homeContent.isVisible = true
                    binding.configContent.isVisible = false
                    binding.settingsContent.isVisible = false
                    binding.topAppBar.isVisible = false
                    binding.layoutTest.isVisible = true
                    binding.tvHomeStatus.isVisible = true
                    binding.tvTestState.isVisible = false
                    binding.toolbar.title = getString(R.string.app_name)
                    invalidateOptionsMenu()
                    true
                }
                R.id.nav_configs -> {
                    binding.homeContent.isVisible = false
                    binding.configContent.isVisible = true
                    binding.settingsContent.isVisible = false
                    binding.topAppBar.isVisible = true
                    binding.layoutTest.isVisible = true
                    binding.tvHomeStatus.isVisible = true
                    binding.tvTestState.isVisible = false
                    binding.toolbar.title = ""
                    invalidateOptionsMenu()
                    true
                }
                R.id.nav_settings -> {
                    binding.homeContent.isVisible = false
                    binding.configContent.isVisible = false
                    binding.settingsContent.isVisible = true
                    binding.topAppBar.isVisible = false
                    binding.layoutTest.isVisible = true
                    binding.tvHomeStatus.isVisible = true
                    binding.tvTestState.isVisible = false
                    invalidateOptionsMenu()
                    true
                }
                else -> false
            }
        }
        binding.bottomNav.selectedItemId = R.id.nav_home

        setupGroupTab()
        setupViewModel()
        SubscriptionUpdater.sync()
        mainViewModel.reloadServerList()
        refreshSubscriptionInfo()
        if (MmkvManager.decodeSettingsBool(AppConfig.PREF_SUB_UPDATE_ON_START, true) &&
            MmkvManager.decodeSubscriptions().any { it.subscription.enabled && it.subscription.url.isNotBlank() }
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                AngConfigManager.updateConfigViaSubAll()
                withContext(Dispatchers.Main) {
                    setupGroupTab()
                    mainViewModel.reloadServerList()
                    refreshSubscriptionInfo()
                }
            }
        }

        checkAndRequestPermission(PermissionType.POST_NOTIFICATIONS) {
        }
    }

    private fun setupNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

    private fun openAdvancedSettings() {
        requestActivityLauncher.launch(Intent(this, SettingsActivity::class.java))
    }

    private fun updateConnectButtonLabel() {
        binding.btnConnect.setText(if (binding.switchSmart.isChecked) R.string.v2box_smart_button else R.string.v2box_connect)
    }

    private fun setupViewModel() {
        mainViewModel.updateTestResultAction.observe(this) { setTestState(it) }
        mainViewModel.isRunning.observe(this) { isRunning ->
            applyRunningState(false, isRunning)
        }
        mainViewModel.startListenBroadcast()
        mainViewModel.initAssets(assets)
    }

    private fun setupGroupTab() {
        val groups = mainViewModel.getSubscriptions(this)
        groupPagerAdapter.update(groups)

        tabMediator?.detach()
        tabMediator = TabLayoutMediator(binding.tabGroup, binding.viewPager) { tab, position ->
            groupPagerAdapter.groups.getOrNull(position)?.let {
                tab.text = it.remarks
                tab.tag = it.id
            }
        }.also { it.attach() }

        val preferred = mainViewModel.subscriptionId.ifEmpty { AppConfig.DEFAULT_SUBSCRIPTION_ID }
        val targetIndex = groups.indexOfFirst { it.id == preferred }.takeIf { it >= 0 } ?: 0
        binding.viewPager.setCurrentItem(targetIndex, false)

        binding.tabGroup.isVisible = false
        refreshGroupTabTitles(true)
    }

    private fun renderGroupCards(groups: List<GroupMapItem>) {
        binding.groupCards.removeAllViews()
        groups.forEachIndexed { index, group ->
            val card = LayoutV2boxGroupCardBinding.inflate(layoutInflater, binding.groupCards, false)
            val count = if (group.id.isEmpty()) MmkvManager.decodeAllServerList().size
                else MmkvManager.decodeServerList(group.id).size
            card.groupTitle.text = "${group.remarks} ($count)"
            card.groupIcon.text = if (group.id == AppConfig.DEFAULT_SUBSCRIPTION_ID) "⌄" else "↻"
            val sub = MmkvManager.decodeSubscription(group.id)
            val details = mutableListOf<String>()
            sub?.let { item ->
                val total = item.trafficTotalBytes ?: 0L
                val remaining = V2BoxSubscriptionInfo.remainingBytes(item)
                if (total > 0L && remaining != null) {
                    val used = total - remaining
                    card.groupProgress.isVisible = true
                    card.groupProgress.progress = ((used.toDouble() / total) * 100).toInt().coerceIn(0, 100)
                    details += getString(R.string.v2box_group_usage,
                        Formatter.formatShortFileSize(this, used), Formatter.formatShortFileSize(this, total))
                }
                item.expireEpochSeconds?.takeIf { it > 0L }?.let { epoch ->
                    val days = ((epoch - System.currentTimeMillis() / 1_000L)
                        .coerceAtLeast(0L) + 86_399L) / 86_400L
                    details += getString(R.string.v2box_days_remaining, days)
                }
            }
            card.groupSummary.text = details.joinToString("  ·  ").ifBlank { getString(R.string.v2box_no_info) }
            card.root.strokeWidth = if (index == binding.viewPager.currentItem) resources.displayMetrics.density.toInt().coerceAtLeast(1) else 0
            card.root.strokeColor = ContextCompat.getColor(this, R.color.v2box_accent)
            card.root.setOnClickListener {
                binding.viewPager.setCurrentItem(index, false)
                mainViewModel.subscriptionIdChanged(group.id)
                refreshSubscriptionInfo()
                renderGroupCards(groups)
            }
            card.groupPing.setOnClickListener {
                binding.viewPager.setCurrentItem(index, false)
                mainViewModel.subscriptionIdChanged(group.id)
                mainViewModel.testAllRealPing()
                renderGroupCards(groups)
            }
            card.groupMore.setOnClickListener {
                requestActivityLauncher.launch(Intent(this, SubSettingActivity::class.java))
            }
            binding.groupCards.addView(card.root)
        }
    }

    fun refreshGroupTabTitles(refreshAll: Boolean = false) {
        val groupsToRefresh = if (refreshAll || mainViewModel.subscriptionId.isEmpty()) {
            groupPagerAdapter.groups
        } else {
            groupPagerAdapter.groups.filter { it.id == mainViewModel.subscriptionId }
        }

        groupsToRefresh.forEach { group ->
            if (group.id.isEmpty()) {
                return@forEach
            }
            val tabIndex = groupPagerAdapter.groups.indexOfFirst { it.id == group.id }
            if (tabIndex >= 0) {
                val count = MmkvManager.decodeServerList(group.id).size
                binding.tabGroup.getTabAt(tabIndex)?.text = "${group.remarks} ($count)"
            }
        }
        renderGroupCards(groupPagerAdapter.groups)
    }

    private fun handleFabAction() {
        if (smartConnectJob?.isActive == true) {
            smartConnectJob?.cancel()
            MessageUtil.sendMsg2TestService(this, TestServiceMessage(key = AppConfig.MSG_MEASURE_CONFIG_CANCEL))
            return
        }
        if (mainViewModel.isRunning.value == true) {
            CoreServiceManager.stopVService(this)
        } else if (MmkvManager.decodeSettingsBool(AppConfig.PREF_SMART_CONNECT, true)) {
            smartConnectAndStart()
        } else {
            startSelectedServer()
        }
    }

    private fun startSelectedServer() {
        if (MmkvManager.getSelectServer().isNullOrEmpty()) {
            toast(R.string.title_file_chooser)
            return
        }
        applyRunningState(isLoading = true, isRunning = false)
        if (SettingsManager.isVpnMode()) {
            val intent = VpnService.prepare(this)
            if (intent == null) {
                startV2Ray()
            } else {
                requestVpnPermission.launch(intent)
            }
        } else {
            startV2Ray()
        }
    }

    /** Use the upstream real ping service, then connect only to a positive result. */
    private fun smartConnectAndStart() {
        val guids = MmkvManager.decodeAllServerList().distinct()
        if (guids.isEmpty()) {
            toast(R.string.title_file_chooser)
            return
        }
        if (guids.size == 1) {
            MmkvManager.setSelectServer(guids.first())
            startSelectedServer()
            return
        }
        MmkvManager.clearAllTestDelayResults(guids)
        MessageUtil.sendMsg2TestService(this, TestServiceMessage(key = AppConfig.MSG_MEASURE_CONFIG_CANCEL))
        MessageUtil.sendMsg2TestService(this, TestServiceMessage(
            key = AppConfig.MSG_MEASURE_CONFIG_START, serverGuids = guids
        ))
        smartConnectJob = lifecycleScope.launch {
            binding.btnConnect.setText(R.string.v2box_choosing)
            var started = false
            try {
                var firstPositiveAt = 0L
                withTimeoutOrNull(25_000L) {
                    while (isActive) {
                        val delays = withContext(Dispatchers.IO) {
                            guids.map { MmkvManager.decodeServerAffiliationInfo(it)?.testDelayMillis ?: 0L }
                        }
                        val now = SystemClock.elapsedRealtime()
                        if (delays.any { it > 0L } && firstPositiveAt == 0L) firstPositiveAt = now
                        if (delays.all { it != 0L } ||
                            (firstPositiveAt > 0L && now - firstPositiveAt > 3_000L)) break
                        delay(250L)
                    }
                }
                val best = withContext(Dispatchers.IO) {
                    guids.mapNotNull { guid ->
                        (MmkvManager.decodeServerAffiliationInfo(guid)?.testDelayMillis ?: 0L)
                            .takeIf { it > 0L }?.let { guid to it }
                    }.minByOrNull { it.second }
                }
                if (best == null) toast(R.string.v2box_smart_unavailable)
                else {
                    MmkvManager.setSelectServer(best.first)
                    refreshSelectedServer()
                    startSelectedServer()
                    started = true
                }
            } finally {
                MessageUtil.sendMsg2TestService(this@MainActivity,
                    TestServiceMessage(key = AppConfig.MSG_MEASURE_CONFIG_CANCEL))
                smartConnectJob = null
                if (!started && mainViewModel.isRunning.value != true) applyRunningState(false, false)
            }
        }
    }

    private fun handleLayoutTestClick() {
        if (mainViewModel.isRunning.value == true) {
            setTestState(getString(R.string.connection_test_testing))
            mainViewModel.testCurrentServerRealPing()
        } else {
            // service not running: keep existing no-op (could show a message if desired)
        }
    }

    private fun startV2Ray() {
        if (MmkvManager.getSelectServer().isNullOrEmpty()) {
            toast(R.string.title_file_chooser)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN && MmkvManager.decodeSettingsBool(AppConfig.PREF_PROXY_SHARING)) {
            checkAndRequestPermission(PermissionType.ACCESS_LOCAL_NETWORK) {}
        }

        CoreServiceManager.startVService(this)
    }

    fun restartV2Ray() {
        if (mainViewModel.isRunning.value == true) {
            CoreServiceManager.stopVService(this)
        }
        lifecycleScope.launch {
            delay(500)
            startV2Ray()
        }
    }

    private fun setTestState(content: String?) {
        binding.tvTestState.text = content
    }

    private fun applyRunningState(isLoading: Boolean, isRunning: Boolean) {
        refreshSelectedServer()
        if (isLoading) {
            binding.fab.setImageResource(R.drawable.ic_fab_check)
            binding.tvHomeStatus.setText(R.string.connection_test_testing)
            return
        }

        if (isRunning) {
            if (connectedAt == 0L) {
                connectedAt = SystemClock.elapsedRealtime()
                uploadedAt = readUidBytes(true)
                downloadedAt = readUidBytes(false)
            }
            binding.btnConnect.setText(R.string.v2box_disconnect)
            binding.tvHomeStatus.setText(R.string.connection_connected)
            binding.fab.setImageResource(R.drawable.ic_stop_24dp)
            binding.fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_fab_active))
            binding.fab.contentDescription = getString(R.string.action_stop_service)
            setTestState(getString(R.string.connection_connected))
            binding.layoutTest.isFocusable = true
        } else {
            connectedAt = 0L
            updateConnectButtonLabel()
            binding.tvHomeStatus.setText(R.string.connection_not_connected)
            binding.fab.setImageResource(R.drawable.ic_play_24dp)
            binding.fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_fab_inactive))
            binding.fab.contentDescription = getString(R.string.tasker_start_service)
            setTestState(getString(R.string.connection_not_connected))
            binding.layoutTest.isFocusable = false
        }
    }

    override fun onResume() {
        super.onResume()
        refreshSelectedServer()
        binding.switchSmart.isChecked = MmkvManager.decodeSettingsBool(AppConfig.PREF_SMART_CONNECT, true)
        binding.switchAutoSort.isChecked = MmkvManager.decodeSettingsBool(AppConfig.PREF_AUTO_SORT_AFTER_TEST, true)
        binding.switchSubUpdate.isChecked = MmkvManager.decodeSettingsBool(AppConfig.PREF_SUB_UPDATE_ON_START, true)
        refreshSubscriptionInfo()
        statsJob?.cancel()
        statsJob = lifecycleScope.launch {
            while (isActive) {
                updateConnectionStats()
                delay(1_000L)
            }
        }
    }

    override fun onPause() {
        statsJob?.cancel()
        statsJob = null
        super.onPause()
    }

    private fun readUidBytes(upload: Boolean): Long {
        val count = if (upload) TrafficStats.getUidTxBytes(Process.myUid())
                    else TrafficStats.getUidRxBytes(Process.myUid())
        return count.coerceAtLeast(0L)
    }

    private fun updateConnectionStats() {
        val seconds = if (connectedAt == 0L) 0L
                      else (SystemClock.elapsedRealtime() - connectedAt) / 1_000L
        binding.tvDuration.text = if (connectedAt == 0L) "-" else
            String.format(java.util.Locale.US, "%02d:%02d:%02d",
                seconds / 3_600, seconds / 60 % 60, seconds % 60)
        val up = if (connectedAt == 0L) 0L else (readUidBytes(true) - uploadedAt).coerceAtLeast(0L)
        val down = if (connectedAt == 0L) 0L else (readUidBytes(false) - downloadedAt).coerceAtLeast(0L)
        binding.tvUpload.text = "${getString(R.string.v2box_upload)}: ${Formatter.formatShortFileSize(this, up)}"
        binding.tvDownload.text = "${getString(R.string.v2box_download)}: ${Formatter.formatShortFileSize(this, down)}"
    }

    private fun refreshSubscriptionInfo() {
        val subscriptions = MmkvManager.decodeSubscriptions()
        val selectedSubscriptionId = MmkvManager.getSelectServer()
            ?.let { MmkvManager.decodeServerConfig(it)?.subscriptionId }
        val active = V2BoxSubscriptionInfo.selectForDisplay(
            subscriptions, listOfNotNull(selectedSubscriptionId, mainViewModel.subscriptionId)
        )
        val parts = mutableListOf<String>()
        active?.let { item ->
            V2BoxSubscriptionInfo.remainingBytes(item)?.let { remaining ->
                parts += getString(R.string.v2box_remaining,
                    Formatter.formatShortFileSize(this, remaining))
            }
            item.expireEpochSeconds?.takeIf { it > 0L }?.let { epoch ->
                val date = DateFormat.getDateInstance(DateFormat.MEDIUM, SettingsManager.getLocale())
                    .format(Date(epoch.coerceAtMost(Long.MAX_VALUE / 1_000) * 1_000))
                parts += getString(R.string.v2box_expiration, date)
                val days = ((epoch - System.currentTimeMillis() / 1_000)
                    .coerceAtLeast(0L) + 86_399L) / 86_400L
                parts += getString(R.string.v2box_days_remaining, days)
            }
        }
        binding.tvSubscription.isVisible = parts.isNotEmpty()
        binding.tvSubscription.text = parts.joinToString("\n")
    }

    private fun refreshSelectedServer() {
        val selected = MmkvManager.getSelectServer()?.let { MmkvManager.decodeServerConfig(it) }
        binding.tvSelectedServer.text = selected?.remarks?.takeIf { it.isNotBlank() }
            ?: getString(R.string.v2box_no_server)
        binding.tvSelectedServer.isVisible = selected != null
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val showConfigActions = binding.configContent.isVisible
        for (index in 0 until menu.size()) menu.getItem(index).isVisible = showConfigActions
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.search_view)
        if (searchItem != null) {
            val searchView = searchItem.actionView as SearchView
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = false

                override fun onQueryTextChange(newText: String?): Boolean {
                    mainViewModel.filterConfig(newText.orEmpty())
                    return false
                }
            })

            searchView.setOnCloseListener {
                mainViewModel.filterConfig("")
                false
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.import_qrcode -> {
            importQRcode()
            true
        }

        R.id.import_clipboard -> {
            importClipboard()
            true
        }

        R.id.import_local -> {
            importConfigLocal()
            true
        }

        R.id.import_manually_policy_group -> {
            importManually(EConfigType.POLICYGROUP.value)
            true
        }

        R.id.import_manually_proxy_chain -> {
            importManually(EConfigType.PROXYCHAIN.value)
            true
        }

        R.id.import_manually_vmess -> {
            importManually(EConfigType.VMESS.value)
            true
        }

        R.id.import_manually_vless -> {
            importManually(EConfigType.VLESS.value)
            true
        }

        R.id.import_manually_ss -> {
            importManually(EConfigType.SHADOWSOCKS.value)
            true
        }

        R.id.import_manually_socks -> {
            importManually(EConfigType.SOCKS.value)
            true
        }

        R.id.import_manually_http -> {
            importManually(EConfigType.HTTP.value)
            true
        }

        R.id.import_manually_trojan -> {
            importManually(EConfigType.TROJAN.value)
            true
        }

        R.id.import_manually_wireguard -> {
            importManually(EConfigType.WIREGUARD.value)
            true
        }

        R.id.import_manually_hysteria2 -> {
            importManually(EConfigType.HYSTERIA2.value)
            true
        }

        R.id.export_all -> {
            exportAll()
            true
        }

        R.id.real_ping_all -> {
            toast(getString(R.string.connection_test_testing_count, mainViewModel.serversCache.count()))
            mainViewModel.testAllRealPing()
            true
        }

        R.id.service_restart -> {
            restartV2Ray()
            true
        }

        R.id.del_all_config -> {
            delAllConfig()
            true
        }

        R.id.del_duplicate_config -> {
            delDuplicateConfig()
            true
        }

        R.id.del_invalid_config -> {
            delInvalidConfig()
            true
        }

        R.id.sort_by_test_results -> {
            sortByTestResults()
            true
        }

        R.id.sub_update -> {
            importConfigViaSub()
            true
        }

        R.id.locate_selected_config -> {
            locateSelectedServer()
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun importManually(createConfigType: Int) {
        if (createConfigType == EConfigType.POLICYGROUP.value) {
            startActivity(
                Intent()
                    .putExtra("subscriptionId", mainViewModel.subscriptionId)
                    .setClass(this, ServerGroupActivity::class.java)
            )
        } else if (createConfigType == EConfigType.PROXYCHAIN.value) {
            startActivity(
                Intent()
                    .putExtra("subscriptionId", mainViewModel.subscriptionId)
                    .setClass(this, ServerProxyChainActivity::class.java)
            )
        } else {
            startActivity(
                Intent()
                    .putExtra("createConfigType", createConfigType)
                    .putExtra("subscriptionId", mainViewModel.subscriptionId)
                    .setClass(this, ServerActivity::class.java)
            )
        }
    }

    /**
     * import config from qrcode
     */
    private fun importQRcode(): Boolean {
        launchQRCodeScanner { scanResult ->
            if (scanResult != null) {
                importBatchConfig(scanResult)
            }
        }
        return true
    }

    /**
     * import config from clipboard
     */
    private fun importClipboard()
            : Boolean {
        try {
            val clipboard = Utils.getClipboard(this)
            importBatchConfig(clipboard)
        } catch (e: Exception) {
            LogUtil.e(AppConfig.TAG, "Failed to import config from clipboard", e)
            return false
        }
        return true
    }

    private fun importBatchConfig(server: String?) {
        showLoading()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val (count, countSub) = AngConfigManager.importBatchConfig(server, mainViewModel.subscriptionId, true)
                delay(500L)
                withContext(Dispatchers.Main) {
                    when {
                        count > 0 -> {
                            toast(getString(R.string.title_import_config_count, count))
                            mainViewModel.reloadServerList()
                            refreshGroupTabTitles()
                        }

                        countSub > 0 -> setupGroupTab()
                        else -> toastError(R.string.toast_failure)
                    }
                    hideLoading()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    toastError(R.string.toast_failure)
                    hideLoading()
                }
                LogUtil.e(AppConfig.TAG, "Failed to import batch config", e)
            }
        }
    }

    /**
     * import config from local config file
     */
    private fun importConfigLocal(): Boolean {
        try {
            showFileChooser()
        } catch (e: Exception) {
            LogUtil.e(AppConfig.TAG, "Failed to import config from local file", e)
            return false
        }
        return true
    }


    /**
     * import config from sub
     */
    fun importConfigViaSub(): Boolean {
        showLoading()

        lifecycleScope.launch(Dispatchers.IO) {
            val result = mainViewModel.updateConfigViaSubAll()
            delay(500L)
            launch(Dispatchers.Main) {
                if (result.successCount + result.failureCount + result.skipCount == 0) {
                    toast(R.string.title_update_subscription_no_subscription)
                } else if (result.successCount > 0 && result.failureCount + result.skipCount == 0) {
                    toast(getString(R.string.title_update_config_count, result.configCount))
                } else {
                    toast(
                        getString(
                            R.string.title_update_subscription_result,
                            result.configCount, result.successCount, result.failureCount, result.skipCount
                        )
                    )
                }
                if (result.configCount > 0) {
                    mainViewModel.reloadServerList()
                    refreshGroupTabTitles()
                }
                hideLoading()
            }
        }
        return true
    }

    private fun exportAll() {
        showLoading()
        lifecycleScope.launch(Dispatchers.IO) {
            val ret = mainViewModel.exportAllServer()
            launch(Dispatchers.Main) {
                if (ret > 0)
                    toast(getString(R.string.title_export_config_count, ret))
                else
                    toastError(R.string.toast_failure)
                hideLoading()
            }
        }
    }

    private fun delAllConfig() {
        AlertDialog.Builder(this).setMessage(R.string.del_config_comfirm)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                showLoading()
                lifecycleScope.launch(Dispatchers.IO) {
                    val ret = mainViewModel.removeAllServer()
                    launch(Dispatchers.Main) {
                        mainViewModel.reloadServerList()
                        refreshGroupTabTitles()
                        toast(getString(R.string.title_del_config_count, ret))
                        hideLoading()
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                //do noting
            }
            .show()
    }

    private fun delDuplicateConfig() {
        AlertDialog.Builder(this).setMessage(R.string.del_config_comfirm)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                showLoading()
                lifecycleScope.launch(Dispatchers.IO) {
                    val ret = mainViewModel.removeDuplicateServer()
                    launch(Dispatchers.Main) {
                        mainViewModel.reloadServerList()
                        refreshGroupTabTitles()
                        toast(getString(R.string.title_del_duplicate_config_count, ret))
                        hideLoading()
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                //do noting
            }
            .show()
    }

    private fun delInvalidConfig() {
        AlertDialog.Builder(this).setMessage(R.string.del_invalid_config_comfirm)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                showLoading()
                lifecycleScope.launch(Dispatchers.IO) {
                    val ret = mainViewModel.removeInvalidServer()
                    launch(Dispatchers.Main) {
                        mainViewModel.reloadServerList()
                        refreshGroupTabTitles()
                        toast(getString(R.string.title_del_config_count, ret))
                        hideLoading()
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                //do noting
            }
            .show()
    }

    private fun sortByTestResults() {
        showLoading()
        lifecycleScope.launch(Dispatchers.IO) {
            mainViewModel.sortByTestResults()
            launch(Dispatchers.Main) {
                mainViewModel.reloadServerList()
                hideLoading()
            }
        }
    }

    /**
     * show file chooser
     */
    private fun showFileChooser() {
        launchFileChooser { uri ->
            if (uri == null) {
                return@launchFileChooser
            }

            readContentFromUri(uri)
        }
    }

    /**
     * read content from uri
     */
    private fun readContentFromUri(uri: Uri) {
        try {
            contentResolver.openInputStream(uri).use { input ->
                importBatchConfig(input?.bufferedReader()?.readText())
            }
        } catch (e: Exception) {
            LogUtil.e(AppConfig.TAG, "Failed to read content from URI", e)
        }
    }

    /**
     * Locates and scrolls to the currently selected server.
     * If the selected server is in a different group, automatically switches to that group first.
     */
    private fun locateSelectedServer() {
        val targetSubscriptionId = mainViewModel.findSubscriptionIdBySelect()
        if (targetSubscriptionId.isNullOrEmpty()) {
            toast(R.string.title_file_chooser)
            return
        }

        val targetGroupIndex = groupPagerAdapter.groups.indexOfFirst { it.id == targetSubscriptionId }
        if (targetGroupIndex < 0) {
            toast(R.string.toast_server_not_found_in_group)
            return
        }

        // Switch to target group if needed, then scroll to the server
        if (binding.viewPager.currentItem != targetGroupIndex) {
            binding.viewPager.setCurrentItem(targetGroupIndex, true)
            binding.viewPager.postDelayed({ scrollToSelectedServer(targetGroupIndex) }, 1000)
        } else {
            scrollToSelectedServer(targetGroupIndex)
        }
    }

    /**
     * Scrolls to the selected server in the specified fragment.
     * @param groupIndex The index of the group/fragment to scroll in
     */
    private fun scrollToSelectedServer(groupIndex: Int) {
        val itemId = groupPagerAdapter.getItemId(groupIndex)
        val fragment = supportFragmentManager.findFragmentByTag("f$itemId") as? GroupServerFragment

        if (fragment?.isAdded == true && fragment.view != null) {
            fragment.scrollToSelectedServer()
        } else {
            toast(R.string.toast_fragment_not_available)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BUTTON_B) {
            moveTaskToBack(false)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.sub_setting -> requestActivityLauncher.launch(Intent(this, SubSettingActivity::class.java))
            R.id.per_app_proxy_settings -> requestActivityLauncher.launch(Intent(this, PerAppProxyActivity::class.java))
            R.id.routing_setting -> requestActivityLauncher.launch(Intent(this, RoutingSettingActivity::class.java))
            R.id.user_asset_setting -> requestActivityLauncher.launch(Intent(this, UserAssetActivity::class.java))
            R.id.settings -> requestActivityLauncher.launch(Intent(this, SettingsActivity::class.java))
            R.id.promotion -> InstagramLink.open(this)
            R.id.logcat -> startActivity(Intent(this, LogcatActivity::class.java))
            R.id.check_for_update -> startActivity(Intent(this, CheckUpdateActivity::class.java))
            R.id.backup_restore -> requestActivityLauncher.launch(Intent(this, BackupActivity::class.java))
            R.id.about -> startActivity(Intent(this, AboutActivity::class.java))
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onDestroy() {
        tabMediator?.detach()
        super.onDestroy()
    }
}
