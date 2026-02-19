package com.codewithkael.webrtcwithmlkit.remote.firebase

import android.util.Log
import com.codewithkael.webrtcwithmlkit.utils.MyApplication
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseClient @Inject constructor(
    private val database: DatabaseReference, private val gson: Gson
) {
    //  Unify all coroutines into a single CoroutineScope
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val userId = MyApplication.UserID

    fun observeIncomingSignals(callback: (SignalDataModel) -> Unit) {
        database.child(FirebaseFieldNames.USERS).child(userId).child(FirebaseFieldNames.DATA)
            .addValueEventListener(object : MyValueEventListener() {
                override fun onDataChange(snapshot: DataSnapshot) {
                    super.onDataChange(snapshot)
                    runCatching {
                        gson.fromJson(snapshot.value.toString(), SignalDataModel::class.java)
                    }.onSuccess {
                        if (it != null) callback(it)
                    }.onFailure {
                        Log.d(MyApplication.TAG, "onDataChange: ${it.message}")
                    }
                }
            })
    }

    suspend fun updateParticipantDataModel(participantId: String, data: SignalDataModel) {
        database.child(FirebaseFieldNames.USERS).child(participantId).child(FirebaseFieldNames.DATA)
            .setValue(gson.toJson(data)).await()
    }

    suspend fun removeSelfData() {
        database.child(FirebaseFieldNames.USERS).child(userId).child(FirebaseFieldNames.DATA)
            .removeValue().await()
    }

    // Cleanup function to cancel all running coroutines
    fun clear() {
        coroutineScope.cancel()
    }
}
