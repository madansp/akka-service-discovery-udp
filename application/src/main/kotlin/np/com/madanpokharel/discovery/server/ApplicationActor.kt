package np.com.madanpokharel.discovery.server

import akka.actor.AbstractLoggingActor
import akka.actor.ActorRef
import akka.actor.Props
import akka.io.Udp
import akka.io.UdpMessage
import akka.japi.pf.ReceiveBuilder
import akka.util.ByteString
import java.net.InetAddress
import java.net.InetSocketAddress

class ApplicationActor(private val localInet: InetSocketAddress,
                       private val udpMulticastAddress: InetAddress) : AbstractLoggingActor() {

    companion object {
        fun props(localInet: InetSocketAddress, udpMulticastAddress: InetAddress): Props = Props.create(ApplicationActor::class.java) {
            ApplicationActor(localInet, udpMulticastAddress)
        }
    }

    override fun preStart() {
        super.preStart()
        val manager = Udp.get(context.system).manager

        //Need to pass options for Multicasting
        manager.tell(UdpMessage.bind(self, localInet, listOf(InetProtocolFamily(), MulticastGroup(udpMulticastAddress))), self)
    }

    override fun createReceive(): Receive = ReceiveBuilder()
            .match(Udp.Bound::class.java) {
                context.become(ready(sender))
            }
            .build()


    private fun ready(udpConnection: ActorRef): Receive = ReceiveBuilder()
            .match(Udp.Received::class.java) {
                log().info("Received message {} from client: {}", it.data().utf8String(), it.sender())
                udpConnection.tell(UdpMessage.send(ByteString.fromString("ACK_MESSAGE_FROM_SERVER"), it.sender()), self)
            }
            .matchEquals(UdpMessage.unbind()) {
                udpConnection.tell(it, self)
            }
            .match(Udp.Unbound::class.java) {
                context.stop(self)
            }
            .build()
}