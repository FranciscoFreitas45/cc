import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ThreadEnvia extends Thread {

    private DatagramPacketBuffer packets;
    private DatagramSocket socket;

    public ThreadEnvia(DatagramPacketBuffer dpf, DatagramSocket socket) {
        this.packets = dpf;
        this.socket = socket;
    }


    public void run() {
        byte [] data = new byte[Transferencia.TAMANHO_PACOTE];

        try {
            while (true) {
                DatagramPacket p = packets.get();
                socket.send(p);
            }
        } catch (IOException e) {
            System.out.println("Erro no sender");
        }
    }


}