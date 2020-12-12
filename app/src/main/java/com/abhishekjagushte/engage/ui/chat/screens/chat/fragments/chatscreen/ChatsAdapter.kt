package com.abhishekjagushte.engage.ui.chat.screens.chat.fragments.chatscreen

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.abhishekjagushte.engage.database.views.MessageView
import com.abhishekjagushte.engage.databinding.ImageMessageContainerLeftBinding
import com.abhishekjagushte.engage.databinding.ImageMessageContainerRightBinding
import com.abhishekjagushte.engage.databinding.TextMessageContainerLeftBinding
import com.abhishekjagushte.engage.databinding.TextMessageContainerRightBinding
import com.abhishekjagushte.engage.datasource.remotedatasource.uploadmanager.UploadManager
import com.abhishekjagushte.engage.utils.Constants
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val MY_TEXT_MESSAGE = 1
const val OTHER_TEXT_MESSAGE = 2
const val MY_IMAGE_MESSAGE =3
const val OTHER_IMAGE_MESSAGE = 4

class ChatsAdapter(private val viewLifecycleOwner: LifecycleOwner) : PagedListAdapter<ChatDataItem, RecyclerView.ViewHolder>(ChatDiffCallBack()){

    private val adapterScope = CoroutineScope(Dispatchers.Default)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            MY_TEXT_MESSAGE -> MyTextMessageViewHolder.from(parent)
            OTHER_TEXT_MESSAGE -> OtherTextMessageViewHolder.from(parent)
            MY_IMAGE_MESSAGE -> MyImageMessageViewHolder.from(parent, viewLifecycleOwner)
            OTHER_IMAGE_MESSAGE -> OtherImageMessageViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is MyTextMessageViewHolder -> {
                val item = getItem(position) as ChatDataItem.MyTextMessage
                holder.bind(item.message)
            }

            is OtherTextMessageViewHolder -> {
                val item = getItem(position) as ChatDataItem.OtherTextMessage
                holder.bind(item.message)
            }

            is MyImageMessageViewHolder -> {
                val item = getItem(position) as ChatDataItem.MyImageMessage
                holder.bind(item.message)
            }

            is OtherImageMessageViewHolder -> {
                val item = getItem(position) as ChatDataItem.OtherImageMessage
                holder.bind(item.message)
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is ChatDataItem.MyTextMessage -> MY_TEXT_MESSAGE
            is ChatDataItem.OtherTextMessage -> OTHER_TEXT_MESSAGE
            is ChatDataItem.MyImageMessage -> MY_IMAGE_MESSAGE
            is ChatDataItem.OtherImageMessage -> OTHER_IMAGE_MESSAGE
            else -> throw IllegalStateException("Chat Adapter unknown adapter item type")
        }
    }

    fun updateList(list: PagedList<ChatDataItem>?) {
        adapterScope.launch {
            list?.let {
                withContext(Dispatchers.Main) {
                    submitList(list)
                }
            }
        }
    }
}

class MyTextMessageViewHolder private constructor(val binding: TextMessageContainerRightBinding): RecyclerView.ViewHolder(binding.root){

    fun bind(message: MessageView){
        binding.message = message
        binding.executePendingBindings()
    }

    companion object{
        fun from(parent: ViewGroup): MyTextMessageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = TextMessageContainerRightBinding.inflate(layoutInflater, parent, false)

            return MyTextMessageViewHolder(binding)
        }
    }
}

class OtherTextMessageViewHolder private constructor(val binding: TextMessageContainerLeftBinding): RecyclerView.ViewHolder(binding.root){

    fun bind(message: MessageView){
        binding.message = message
        binding.executePendingBindings()
    }

    companion object{
        fun from(parent: ViewGroup): OtherTextMessageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = TextMessageContainerLeftBinding.inflate(layoutInflater, parent, false)

            return OtherTextMessageViewHolder(binding)
        }
    }
}

class MyImageMessageViewHolder private constructor(
    val binding: ImageMessageContainerRightBinding,
    val viewLifecycleOwner: LifecycleOwner
): RecyclerView.ViewHolder(binding.root){

    private val TAG = "MyImageMessageViewHolder"

    fun bind(message: MessageView){
        binding.message = message
        Glide.with(binding.root.context)
            .load(message.local_uri)
            .apply(RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE))
            .into(binding.imageMsgIv)

        setUpProgressBar(message)

        binding.executePendingBindings()
    }

    private fun setUpProgressBar(message: MessageView){
        if(message.status == Constants.STATUS_UPLOADING) {
            UploadManager.getUploader(message.messageID)?.let{
                it.progress.observe( viewLifecycleOwner, Observer {
                    if(it < 100)
                        binding.progressBarImageRight.progress = it.toInt()
                    else
                        binding.progressBarImageRight.visibility = View.GONE

                    Log.d(TAG, "setUpProgressBar: $it")
                })}
        }
        else{
            binding.progressBarImageRight.visibility = View.GONE
        }
    }

    companion object{
        fun from(parent: ViewGroup, viewLifecycleOwner: LifecycleOwner): MyImageMessageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ImageMessageContainerRightBinding.inflate(layoutInflater, parent, false)

            return MyImageMessageViewHolder(binding, viewLifecycleOwner)
        }
    }

}

class OtherImageMessageViewHolder private constructor(val binding: ImageMessageContainerLeftBinding): RecyclerView.ViewHolder(binding.root){

    fun bind(message: MessageView){
        binding.message = message
        Glide.with(binding.root.context).load(message.local_uri).into(binding.imageMsgIv)
        binding.executePendingBindings()
    }

    companion object{
        fun from(parent: ViewGroup): OtherImageMessageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ImageMessageContainerLeftBinding.inflate(layoutInflater, parent, false)

            return OtherImageMessageViewHolder(binding)
        }
    }

}

class ChatDiffCallBack: DiffUtil.ItemCallback<ChatDataItem>() {
    override fun areItemsTheSame(oldItem: ChatDataItem, newItem: ChatDataItem): Boolean {
        return oldItem.id == newItem.id // this checks if message is deleted than changes it
    }

    override fun areContentsTheSame(oldItem: ChatDataItem, newItem: ChatDataItem): Boolean {
        return (oldItem.deleted == newItem.deleted)
    }

}

sealed class ChatDataItem{
    abstract val id: String
    abstract val deleted: Int //for deleted status we will show the message is deleted
    abstract val status: Int

    data class MyTextMessage(val message: MessageView): ChatDataItem() {
        override val id = message.messageID
        override val deleted: Int = message.deleted
        override val status: Int = message.status
    }

    data class OtherTextMessage(val message: MessageView): ChatDataItem(){
        override val id = message.messageID
        override val deleted: Int = message.deleted
        override val status: Int = message.status
    }

    data class MyImageMessage(val message: MessageView): ChatDataItem(){
        override val id = message.messageID
        override val deleted = message.deleted
        override val status = message.status
    }

    data class OtherImageMessage(val message: MessageView): ChatDataItem(){
        override  val id = message.messageID
        override val deleted = message.deleted
        override val status = message.deleted
    }

}