import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.Buffer;

public class ThreadRecebe  extends Thread  {

    private DatagramPacketBuffer packets;
    private DatagramSocket socket;

    public ThreadRecebe(DatagramPacketBuffer dpf,DatagramSocket socket){
        this.packets = dpf;
        this.socket=socket;
    }


    public void run(){
        try{
            while(true){
                byte[] recebeDados = new byte[Transferencia.TAMANHO_PACOTE];
                DatagramPacket recebePacote = new DatagramPacket(recebeDados,recebeDados.length);
                socket.receive(recebePacote);
                System.out.println(recebeDados.length);
                packets.add(recebePacote);
            }
        }
        catch (IOException e) {
            System.out.println("Erro no receiver");
        }
    }

}