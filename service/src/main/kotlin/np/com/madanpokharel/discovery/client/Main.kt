package np.com.madanpokharel.discovery.client

import akka.actor.ActorSystem
import java.net.InetAddress
import java.net.InetSocketAddress


fun main() {
    val actorSystem = ActorSystem.create("AkkaUDPDiscoveryClient")
    val config = actorSystem.settings().config().getConfig("service-discovery-client")
    val remoteInet = InetSocketAddress(
            InetAddress.getByName(config.getString("udp-multicast-address")!!),
            config.getInt("udp-multicast-port")
    )
    val localIntet = InetSocketAddress(0)

    actorSystem.actorOf(ServiceActor.props(localIntet, remoteInet), ServiceActor::class.java.simpleName)
}