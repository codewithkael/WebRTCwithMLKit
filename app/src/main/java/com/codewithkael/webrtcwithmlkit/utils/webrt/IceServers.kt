package com.codewithkael.webrtcwithmlkit.utils.webrt

import org.webrtc.PeerConnection.IceServer

class IceServers {
    companion object {
        fun getIceServers() = listOf(
            IceServer.builder("turn:95.217.13.89:3478").setUsername("user").setPassword("password")
                .createIceServer(),
            IceServer.builder("stun:95.217.13.89:3478").createIceServer(),
            IceServer.builder("stun:stun.relay.metered.ca:80").createIceServer(),
            IceServer.builder("turn:164.92.142.241:3478?transport=udp").setUsername("username1")
                .setPassword("key1").createIceServer(),
            IceServer.builder("turns:164.92.142.241:5349?transport=tcp").setUsername("username1")
                .setPassword("key1").createIceServer(),
            IceServer.builder("turn:164.92.142.251:3478").setUsername("username1").setPassword("key1")
                .createIceServer(),
            IceServer.builder("turn:global.relay.metered.ca:80").setUsername("0da9dc3f3ca0b8aef7388ca9")
                .setPassword("KuuHVTmXU80Q1WMO").createIceServer(),
            IceServer.builder("turn:global.relay.metered.ca:80?transport=tcp")
                .setUsername("0da9dc3f3ca0b8aef7388ca9").setPassword("KuuHVTmXU80Q1WMO")
                .createIceServer(),
            IceServer.builder("turn:global.relay.metered.ca:443")
                .setUsername("0da9dc3f3ca0b8aef7388ca9").setPassword("KuuHVTmXU80Q1WMO")
                .createIceServer(),
            IceServer.builder("turns:global.relay.metered.ca:443?transport=tcp")
                .setUsername("0da9dc3f3ca0b8aef7388ca9").setPassword("KuuHVTmXU80Q1WMO")
                .createIceServer(),
            IceServer.builder("turn:13.250.13.83:3478?transport=udp")
                .setUsername("YzYNCouZM1mhqhmseWk6").setPassword("YzYNCouZM1mhqhmseWk6")
                .createIceServer(),
            IceServer.builder("turn:numb.viagenie.ca").setUsername("webrtc@live.com")
                .setPassword("muazkh").createIceServer(),
            IceServer.builder("turn:freestun.net:3478").setUsername("free").setPassword("free")
                .createIceServer()
        )

    }
}