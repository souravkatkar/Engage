package com.abhishekjagushte.engage.ui.chat.screens.chat.fragments.chatscreen

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.abhishekjagushte.engage.database.views.MessageView
import com.abhishekjagushte.engage.utils.Constants
import com.abhishekjagushte.engage.utils.StringFormatting

@BindingAdapter("setTimeString")
fun TextView.setTimeString(message: MessageView){
    val dateTime = message.timeStamp
    dateTime!!.let {
        text = StringFormatting.getTime(it)
    }
}

@BindingAdapter("setSenderName")
fun TextView.setSenderName(message: MessageView){
    if(message.conType == Constants.CONVERSATION_TYPE_121)
        this.visibility = View.GONE
    else {

        if(message.nickname.isNullOrEmpty())
            text = message.senderID
        else
            text = message.nickname
    }
}

