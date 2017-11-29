package adamczewski.marcin.callswatcher

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CallsMonitorService: Service() {

    companion object {
        val ACTION_START = "start"
        val ACTION_STOP = "stop"
    }

    private lateinit var callsReceiver: BroadcastReceiver
    private val callsDao = CallsDao()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        callsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val state = intent.extras.getString(TelephonyManager.EXTRA_STATE)
                val stateCode = if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                    TelephonyManager.CALL_STATE_RINGING
                } else {
                    null
                }

                if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED && stateCode != null) {
                    val number = intent.extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
                    Log.d("lol", "Incoming call detected from number:  " + number)
                    makeIncomingCallRequest(number)
                }
            }
        }

        val intentFilter = IntentFilter().apply {
            priority = 2147483647
            addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        }

        registerReceiver(callsReceiver, intentFilter)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            ACTION_START -> {
                val notification = createNotification()
                startForeground(1, notification)
            }
            ACTION_STOP -> stopSelf()
        }

        return Service.START_STICKY
    }

    private fun makeIncomingCallRequest(number: String) {
        callsDao.sendPhoneNumber(IncomingCallRequest(number), object: Callback<Void> {
            override fun onFailure(call: Call<Void>?, t: Throwable?) {
                Log.e("lol", "Couldn't send number: " + number)
                Toast.makeText(applicationContext, "An error occurred: " + t?.message, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<Void>?, response: Response<Void>?) {
                response?.apply {
                    if (isSuccessful) {
                        Log.d("lol", "Successfully sent number: " + number)
                    } else {
                        Toast.makeText(applicationContext, "An error occurred: ", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            val notificationManager = getSystemService (Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel("1", "a", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "descirption"
            }
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, "1")
                .setContentTitle("Monitor połączeń włączony")
                .setTicker("Monitor połączeń włączony")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setChannelId("1")
                .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(callsReceiver)
    }
}