
/**
 *
 * @authors Catarina Ribeiro, Leonardo Cavalcante, Leonardo Portugal, Victor
 * Meireles
 *
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.Arrays;

public class Servidor {
    static final int CABECALHO = 4;
    static final int TAMANHO_PACOTE = 512;
        private DatagramSocket entrada;
        private DatagramSocket saida;
        private int portaEntrada;
        private int portaDestino;
        private String caminho;
        private  boolean transferenciacompleta;

    //construtor
    public Servidor(int portaEntrada,int portaDestino,String caminho) {

        this.caminho = caminho;
        this.portaEntrada = portaEntrada;
        this.portaDestino = portaDestino;
        this.transferenciacompleta = false;
        try {
            this.saida = new DatagramSocket();
            this.entrada = new DatagramSocket(1234);
            try {
                FileOutputStream fos = null;
                    while(!transferenciacompleta) {
                        byte[] recebeDados = new byte[TAMANHO_PACOTE];
                        DatagramPacket recebePacote = new DatagramPacket(recebeDados, recebeDados.length);


                        this.entrada.receive(recebePacote);
                        InetAddress enderecoIP = recebePacote.getAddress();
                        int numSeq = ByteBuffer.wrap(Arrays.copyOfRange(recebeDados, 0, CABECALHO)).getInt();
                        System.out.println("Servidor: Numero de pacote recebido " + numSeq);
                        byte[] pacoteAck=gerarPacote(numSeq);
                            this.saida.send(new DatagramPacket(pacoteAck, pacoteAck.length, enderecoIP, 4321));

                        if(recebePacote.getLength()<512){
                            transferenciacompleta=true;
                        }
                        System.out.println(recebePacote.getLength());
                        if(numSeq==1) {
                            File arquivo = new File(caminho);
                            if (!arquivo.exists()) {
                                arquivo.createNewFile();
                            }
                            fos = new FileOutputStream(arquivo);
                        }
                        fos.write(recebeDados, CABECALHO, recebePacote.getLength()-CABECALHO);
                    }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            } finally {
                this.entrada.close();
                //this.saida.close();
                System.out.println("Servidor: Socket de entrada fechado!");
                System.out.println("Servidor: Socket de saida fechado!");
            }
        } catch (SocketException e1) {
            e1.printStackTrace();
        }
    }

    //gerar pacote de ACK
    public byte[] gerarPacote(int numAck) {
        byte[] numAckBytes = ByteBuffer.allocate(CABECALHO).putInt(numAck).array();
        ByteBuffer bufferPacote = ByteBuffer.allocate(CABECALHO);
        bufferPacote.put(numAckBytes);
        return bufferPacote.array();
    }






    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        System.out.println("----------------------------------------------SERVIDOR----------------------------------------------");
        System.out.print("Digite o diretorio do arquivo a ser criado. (Ex: C:/Users/Diego/Documents/): ");
        String diretorio = teclado.nextLine();
        System.out.print("Digite o nome do arquivo a ser criado: (Ex: letra.txt): ");
        String nome = teclado.nextLine();

        Servidor servidor = new Servidor(8010,8011,diretorio + nome);
    }
}