package np.com.madanpokharel.discovery.client

import akka.actor.AbstractActorWithTimers
import akka.actor.ActorRef
import akka.actor.Props
import akka.io.Udp
import akka.io.UdpMessage
import akka.japi.pf.ReceiveBuilder
import akka.util.ByteString
import java.net.InetSocketAddress
import java.time.Duration

class ServiceActor(private val localInet: InetSocketAddress,
                   private val remoteInet: InetSocketAddress) : AbstractActorWithTimers() {
    private val log = context.system.log()

    companion object {
        private const val SEND_MESSAGE_UDP = "SEND_MESSAGE_UDP"
        fun props(local: InetSocketAddress, remote: InetSocketAddress): Props = Props.create(ServiceActor::class.java) {
            ServiceActor(local, remote)
        }
    }

    override fun preStart() {
        super.preStart()
        val manager = Udp.get(context.system).manager
        manager.tell(UdpMessage.bind(self, localInet), self)
    }

    override fun createReceive(): Receive = ReceiveBuilder()
            .match(Udp.Bound::class.java) {
                context.become(ready(sender))
                timers().startPeriodicTimer(SEND_MESSAGE_UDP, SendMsg, Duration.ofSeconds(1)) //Starting a timer which sends message every seconds
            }
            .build()


    private fun ready(udpConnection: ActorRef): Receive = ReceiveBuilder()
            .match(Udp.Received::class.java) {
                log.info("Received message {}, from server: {}", it.data().utf8String(), it.sender())
                //We got reply from server, cancel timer
                timers().cancel(SEND_MESSAGE_UDP)

            }
            .match(SendMsg::class.java) {
                log.info("sending discovery message to server")
                udpConnection.tell(UdpMessage.send(ByteString.fromString("CLIENT_DISCOVERY"), remoteInet), self)
            }
            .matchEquals(UdpMessage.unbind()) {
                udpConnection.tell(it, self)
            }
            .match(Udp.Unbound::class.java) {
                context.stop(self)
            }
            .build()

    object SendMsg
}