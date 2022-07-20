package com.example.roomdemo

import android.content.Context
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.*
import com.example.roomdemo.db.Subscriber
import com.example.roomdemo.db.SubscriberRepository
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class SubscriberViewModel(private val repository: SubscriberRepository) : ViewModel() {

    val subscribers = repository.subscribers
    private var isUpdateOrDelete = false
    private lateinit var subscriberToUpdateOrDelete : Subscriber

    val inputName = MutableLiveData<String>()

    val inputEmail = MutableLiveData<String>()

    val saveOrUpdateButtonText = MutableLiveData<String>()

    val clearAllOrDeleteButtonText = MutableLiveData<String>()

    private val statusMessage =  MutableLiveData<Event<String>>()

    val message : LiveData<Event<String>>
    get() = statusMessage

    init {
        saveOrUpdateButtonText.value = "save"
        clearAllOrDeleteButtonText.value = "Clear All"
    }

    fun saveOrUpdate() {

        if(inputName.value == null){
            statusMessage.value = Event("Please Enter Name")
        }else if(inputEmail.value == null){
            statusMessage.value = Event("Please Enter Email")
        }else if(!Patterns.EMAIL_ADDRESS.matcher(inputEmail.value!!).matches()){
            statusMessage.value = Event("Please Enter Correct Email Address")
        }else{
            if(isUpdateOrDelete){
                subscriberToUpdateOrDelete.name = inputName.value!!
                subscriberToUpdateOrDelete.email = inputEmail.value!!
                update(subscriberToUpdateOrDelete)
            }else{
                val name = inputName.value!!
                val email = inputEmail.value!!
                insert(Subscriber(0,name,email))
                inputName.value = ""
                inputEmail.value = ""
            }
        }
    }

    fun clearAllOrDelete() {
        if(isUpdateOrDelete){
            delete(subscriberToUpdateOrDelete)
        }else{
            clearAll()
        }

    }

    fun insert(subscriber: Subscriber) = viewModelScope.launch {
         val newRowId:Long = repository.insert(subscriber)
        if(newRowId>-1) {
            statusMessage.value = Event("Subscriber Inserted Successfully $newRowId")
        }else{
            statusMessage.value = Event("Error Occurred")
        }
    }

    fun update(subscriber: Subscriber) = viewModelScope.launch {
        repository.update(subscriber)
        inputName.value = ""
        inputEmail.value = ""
        isUpdateOrDelete = false
        saveOrUpdateButtonText.value = "save"
        clearAllOrDeleteButtonText.value ="clear all"
        statusMessage.value =Event("Subscriber Updated  Successfully")
    }

    fun delete(subscriber: Subscriber) = viewModelScope.launch {
        repository.delete(subscriber)
        inputName.value = ""
        inputEmail.value = ""
        isUpdateOrDelete = false
        saveOrUpdateButtonText.value = "save"
        clearAllOrDeleteButtonText.value ="clear all"
        statusMessage.value =Event("Subscriber Deleted Successfully")
    }

    fun clearAll() = viewModelScope.launch {
        repository.deleteAll()
        statusMessage.value =Event("All Subscriber Deleted Successfully")
    }

    fun initUpdateAndDelete(subscriber: Subscriber){
        inputName.value = subscriber.name
        inputEmail.value = subscriber.email
        isUpdateOrDelete = true
        subscriberToUpdateOrDelete = subscriber
        saveOrUpdateButtonText.value = "Update"
        clearAllOrDeleteButtonText.value ="Delete"

    }

    fun getSaveSubscribers() = liveData {
        repository.subscribers.collect{
            emit(it)
        }
    }

}