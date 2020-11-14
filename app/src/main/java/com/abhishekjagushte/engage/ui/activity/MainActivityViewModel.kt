package com.abhishekjagushte.engage.ui.activity

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.abhishekjagushte.engage.database.entities.Message
import com.abhishekjagushte.engage.listeners.One21Listener
import com.abhishekjagushte.engage.network.MessageNetwork
import com.abhishekjagushte.engage.repository.DataRepository
import com.abhishekjagushte.engage.sync.M2MChatsSynchronizer
import com.abhishekjagushte.engage.sync.One21Synchronizer
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.*

class MainActivityViewModel(
    private val dataRepository: DataRepository
): ViewModel(){

    private val TAG = "MainActivityViewModel"
    private var listener121: ListenerRegistration?=null


    val job: Job = Job()
    val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Main + job)

    fun printPanda(){
        Log.d(TAG, "printPanda: Printed")
    }

    fun getUnsentMessages(): LiveData<List<Message>> {
        return dataRepository.getUnsentMessages()
    }

    override fun onCleared() {
        super.onCleared()

        //Removes 121 listener
        listener121?.remove()
        Log.d(TAG, "onCleared: MainActivity ViewModel onCleared")
    }

    fun sendMessages(unsentMessages: List<Message>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                for(m in unsentMessages){
                    dataRepository.pushMessage(m)
                }
            }
        }
    }

    fun set121MessageListener(){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val details = dataRepository.getMydetails()
                details?.let {
                    listener121 =  One21Listener(dataRepository, it.username).set121Listener(it.username)
                }
            }
        }
    }

    fun testSyncFunction(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                val m2MChatsSynchronizer = M2MChatsSynchronizer(dataRepository, context)
                m2MChatsSynchronizer.synchronize()
                val one21Synchronizer = One21Synchronizer(dataRepository, context)
                one21Synchronizer.synchronize()
            }
        }
    }
}

//    private fun createNewChat121(con: Conversation) {
//        val myUsername = dataRepository.getMydetails()!!.username
//
//        dataRepository.createNewChat121(hashMapOf(
//            "myID" to myUsername,
//            "otherID" to con.username!!,
//            "conversationID" to con.networkID
//        )).addOnSuccessListener {
//            it.data?.let{
//                val res = (it as HashMap<*, *>).get("conversationID") as String
//                Log.d(TAG, "createTestNewChat: $res is the new conversationID")
//            }
//
//            viewModelScope.launch {
//                withContext(Dispatchers.IO){
//                    con.needs_push = Constants.CONVERSATION_NEEDS_PUSH_NO
//                    dataRepository.updateConversation(con)
//                }
//            }
//
//        }

//        response.data?.let {
//            val res = (it as HashMap<*, *>).get("conversationID")
//            Log.d(TAG, "createTestNewChat: $res is the new conversationID")
//            conversationID = res as String
//            dataRepository.addConversation121(username, res)
//        }
//    }

//    fun pushConversations(it: List<Conversation>) {
//        viewModelScope.launch {
//            withContext(Dispatchers.IO){
//                for(con in it){
//                    createNewChat121(con)
//                }
//            }
//        }
//    }