package com.codewithkael.webrtcwithmlkit.utils.webrt

import org.webrtc.PeerConnection.IceServer

class IceServers {
    companion object {
        fun getIceServers() = listOf(
            IceServer.builder("stun:stun.relay.metered.ca:80").createIceServer(),
            IceServer.builder("turn:159.223.175.154:3478").setUsername("user")
                .setPassword("password").createIceServer(),
            IceServer.builder("turn:95.217.13.89:3478").setUsername("user")
                .setPassword("password").createIceServer()
        )
    }
}