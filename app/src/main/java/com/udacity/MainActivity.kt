package com.udacity

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.database.Cursor
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.content_main.*
import java.io.File
import java.net.URL


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var selRepository: String? = null
    private var selFileName: String? = null
    lateinit var loadingButton: LoadingButton

    private lateinit var notificationManager: NotificationManager
//    private lateinit var pendingIntent: PendingIntent
//    private lateinit var action: NotificationCompat.Action

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        loadingButton = findViewById(R.id.custom_button)
        loadingButton.setCustomButtonState(ButtonState.Completed)
        loadingButton.setOnClickListener {
            download()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val action = intent.action

            if (downloadID == id) {
                if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    val query = DownloadManager.Query()
                    query.setFilterById(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
                    val manager = context!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val cursor: Cursor = manager.query(query)
                    if (cursor.moveToFirst()) {
                        if (cursor.count > 0) {
                            val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                loadingButton.setCustomButtonState(ButtonState.Completed)
                                notificationManager.sendNotification(selFileName.toString(), applicationContext, "Success")
                            } else {
                                loadingButton.setCustomButtonState(ButtonState.Completed)
                                notificationManager.sendNotification(selFileName.toString(), applicationContext, "Failed")
                            }
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun download() {
        loadingButton.setCustomButtonState(ButtonState.Clicked)

        if (selRepository != null) {
            loadingButton.setCustomButtonState(ButtonState.Loading)
            notificationManager = ContextCompat.getSystemService(applicationContext, NotificationManager::class.java) as NotificationManager
            createChannel(getString(R.string.repository_channel_id), getString(R.string.githubRepo_notification_channel_name))

            var file = File(getExternalFilesDir(null), "/repos")

            if (!file.exists()) {
                file.mkdirs()
            }

            val request =
                DownloadManager.Request(Uri.parse(selRepository))
                    .setTitle(getString(R.string.app_name))
                    .setDescription(getString(R.string.app_description))
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/repos/repository.zip")


            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadID =
                downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        } else {
            loadingButton.setCustomButtonState(ButtonState.Completed)
            showToast(getString(R.string.choose_file))
        }
    }

    companion object {
        private const val CHANNEL_ID = "channelId"
    }

    private fun ObjectAnimator.disableViewDuringAnimation(view: View) {
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                view.isEnabled = false
            }
            override fun onAnimationEnd(animation: Animator?) {
                view.isEnabled = true
            }
        })
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            val isChecked = view.isChecked
            when (view.getId()) {
                R.id.glide_button ->
                    if (isChecked) {
                        selRepository = getString(R.string.glide_url)
                        selFileName = getString(R.string.glide_text)
                    }

                R.id.load_app_button ->
                    if (isChecked) {
                        selRepository = getString(R.string.loadApp_url)
                        selFileName = getString(R.string.load_app_text)
                    }

                R.id.retrofit_button -> {
                    if (isChecked) {
                        selRepository = getString(R.string.retrofit_url)
                        selFileName = getString(R.string.retrofit_text)
                    }
                }
            }
        }
    }

    private fun showToast(text: String) {
        val toast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Download completed!"

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}
