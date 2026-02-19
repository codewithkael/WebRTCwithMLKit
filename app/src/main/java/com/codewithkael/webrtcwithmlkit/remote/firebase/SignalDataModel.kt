package com.codewithkael.webrtcwithmlkit.remote.firebase

data class SignalDataModel(
    val participantId: String,
    val type:SignalDataModelTypes?=null,
    val data:String?=null
)

enum class SignalDataModelTypes {
    INCOMING_CALL,ACCEPT_CALL,OFFER,ANSWER,ICE
}
