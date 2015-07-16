package pl.qla.network;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import pl.qla.detector.PacketSentListener;

/**
 * Created by przemek on 04/07/15.
 */
public class CommunicationProtocol {
    private static final String TAG = CommunicationProtocol.class.getName();
    private InetAddress receiverAddress;
    private int port;
    private DatagramSocket datagramSocket;
    private int numberOfSentPackets;
    private PacketSentListener packetSentListener;

   public enum MESSAGE_TYPE {
        PING((byte) 1),
        ALARM((byte) 2),
        TERMINATE((byte) 3),
        INIT((byte) 4);

        MESSAGE_TYPE(byte value) {
            this.value = value;
        }

        private byte value;
    }

    public CommunicationProtocol(String receiverIP, int ping) {
        try {
            receiverAddress = InetAddress.getByName(receiverIP);
            this.port = ping;
            numberOfSentPackets = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(MESSAGE_TYPE messageType) throws IOException {
        byte[] statusMsg = new byte[1];
        statusMsg[0] = messageType.value;
        Log.i(TAG, String.format("Datagram packet has been sent %s", statusMsg[0]));
        if (!datagramSocket.isClosed()) {
            datagramSocket.send(new DatagramPacket(statusMsg, 1, receiverAddress, port));
        }
        numberOfSentPackets++;
        packetSentListener.onPacketSentListener(numberOfSentPackets);
    }

    public void setPacketSentListener(PacketSentListener packetSentListener) {
        this.packetSentListener = packetSentListener;
    }

    public void open() {
        try {
            datagramSocket = new DatagramSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        datagramSocket.close();
        numberOfSentPackets = 0;
    }
}
