package np.com.madanpokharel.discovery.server

import akka.actor.ActorSystem
import akka.io.Inet
import java.net.*
import java.nio.channels.DatagramChannel

fun main() {
    val actorSystem = ActorSystem.create("AkkaUDPDiscoveryServer")
    val config = actorSystem.settings().config().getConfig("service-discovery-application")

    val localIntet = InetSocketAddress(config.getInt("udp-multicast-port"))
    val multicastAddress = InetAddress.getByName(config.getString("udp-multicast-address")!!)

    actorSystem.actorOf(ApplicationActor.props(localIntet, multicastAddress), ApplicationActor::class.java.simpleName)
}


class InetProtocolFamily : Inet.DatagramChannelCreator() {
    override fun create(): DatagramChannel {
        return DatagramChannel.open(StandardProtocolFamily.INET)
    }
}

class MulticastGroup(private val group: InetAddress) : Inet.AbstractSocketOptionV2() {

    override fun afterBind(s: DatagramSocket) {
        try {
            val networkInterface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost())
            s.channel.join(group, networkInterface)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}