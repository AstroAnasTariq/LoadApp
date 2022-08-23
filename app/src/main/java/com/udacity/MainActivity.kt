package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.udacity.Constants.CHANNEL_ID
import com.udacity.Constants.CHANNEL_NAME
import com.udacity.Constants.FILE_NAME_KEY
import com.udacity.Constants.GLIDE_URL
import com.udacity.Constants.NOTIFICATION_ID
import com.udacity.Constants.RETROFIT_URL
import com.udacity.Constants.STATUS_KEY
import com.udacity.Constants.UDACITY_URL
import com.udacity.databinding.ActivityMainBinding
import com.udacity.databinding.ContentMainBinding


class MainActivity : AppCompatActivity() {

    private var mDownloadID: Long = 0

    private lateinit var binding: ActivityMainBinding
    private lateinit var contentMainBinding: ContentMainBinding
    private lateinit var mNotificationManager: NotificationManager
    private lateinit var mPendingIntent: PendingIntent
    private lateinit var mFileName: String
    private lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        contentMainBinding = binding.contentMain
        setSupportActionBar(binding.toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        contentMainBinding.customButton.setOnClickListener {
            if (::url.isInitialized) {
                contentMainBinding.customButton.buttonState = ButtonState.Loading
                download()
            } else
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.you_should_select_from_list),
                    Toast.LENGTH_SHORT
                ).show()
        }
        binding.contentMain.radioGroup.setOnCheckedChangeListener { _, index ->
            when (index) {
                R.id.radio_glide -> {
                    url = GLIDE_URL
                    mFileName = getString(R.string.glide)
                }
                R.id.radio_load_app -> {
                    url = UDACITY_URL
                    mFileName = getString(R.string.load_app)
                }
                R.id.radio_retrofit -> {
                    url = RETROFIT_URL
                    mFileName = getString(R.string.retrofit)
                }

            }

        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val intentId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (intentId == -1L)
                return
            intentId?.let { id ->
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val query = DownloadManager.Query()
                query.setFilterById(id)
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val downloadStatus =
                        if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(index))
                            getString(R.string.success_status)
                        else
                            getString(R.string.failed_status)
                    sendNotifications(downloadStatus)
                    contentMainBinding.customButton.buttonState = ButtonState.Completed

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    private fun download() {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(getString(R.string.app_name))
            .setDescription(String.format(getString(R.string.app_description), mFileName))
            .setRequiresCharging(false)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        // enqueue puts the download request in the queue.
        mDownloadID = downloadManager.enqueue(request)
    }

    private fun sendNotifications(status: String) {

        createChannel()

        intent = Intent(applicationContext, DetailActivity::class.java)
        intent.putExtra(STATUS_KEY, status)
        intent.putExtra(FILE_NAME_KEY, mFileName)
        mPendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText(getString(R.string.loading_completed))
            .setAutoCancel(true)
            .addAction(
                R.drawable.abc_vector_test,
                getString(R.string.see_result),
                mPendingIntent
            )

        mNotificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager
        mNotificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun createChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            channel.enableVibration(true)
            channel.enableLights(true)

            channel.description = getString(R.string.loading_completed)
            val notificationManager = this.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)

        }
    }
}
