import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Scanner;
import java.util.List;

public class Cliente {
    static final int CABECALHO = 4;
    static final int TAMANHO_PACOTE = 512;
    private String caminho;
    private boolean transferenciaCompleta;
    private DatagramSocket saida;
    private DatagramSocket entrada;
    private int portaEntrada;
    private int portaSaida;
    private InetAddress enderecoIP;
    private  int numPacote;
    List<byte[]> listaPacotes;

    public Cliente(String caminho, boolean transferenciaCompleta,int portaEntrada, int portaSaida) throws UnknownHostException {
        this.caminho = caminho;
        this.transferenciaCompleta = transferenciaCompleta;
        this.portaEntrada = portaEntrada;
        this.portaSaida = portaSaida;
        this.enderecoIP=InetAddress.getLocalHost();
        this.numPacote=1;
        try {
            this.saida = new DatagramSocket();
            this.entrada= new DatagramSocket(4321);
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }



    public void run(){
        try {
            FileInputStream fis = new FileInputStream(new File(caminho));
            try{
                while(!transferenciaCompleta) {
                    byte[] enviaDados = new byte[TAMANHO_PACOTE];
                    byte[] dataBuffer = new byte[TAMANHO_PACOTE];
                    int tamanhoDados = fis.read(dataBuffer, 0, TAMANHO_PACOTE);
                    if(tamanhoDados<TAMANHO_PACOTE)
                        transferenciaCompleta=true;
                    byte[] dataBytes = Arrays.copyOfRange(dataBuffer, 0, tamanhoDados);
                      enviaDados = gerarPacote(numPacote, dataBytes);
                        int aux= numPacote;
                        numPacote++;
                        boolean pacoteEnviado=false;
                       while(!pacoteEnviado) {
                           //enviando pacote
                           this.saida.send(new DatagramPacket(enviaDados, enviaDados.length, enderecoIP, 1234));
                           byte[] recebeDados = new byte[CABECALHO];
                           DatagramPacket recebePacote = new DatagramPacket(recebeDados, recebeDados.length);
                           entrada.receive(recebePacote);
                           int numAck = getnumAck(recebeDados);
                           if(aux==numAck)
                               pacoteEnviado=true;
                           System.out.println("Cliente: Ack recebido " + numAck);
                       }
                    System.out.println("Cliente: Numero de pacote enviado "+numPacote);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                this.saida.close();
                fis.close();
                System.out.println("Cliente: Socket de saida fechado!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }


    int getnumAck(byte[] pacote) {
        byte[] numAckBytes = Arrays.copyOfRange(pacote, 0, CABECALHO);
        return ByteBuffer.wrap(numAckBytes).getInt();
    }



    //cria o pacote com numero de sequencia e os dados
    public byte[] gerarPacote(int numSeq, byte[] dadosByte) {
        byte[] numSeqByte = ByteBuffer.allocate(CABECALHO).putInt(numSeq).array();
        ByteBuffer bufferPacote = ByteBuffer.allocate(CABECALHO + dadosByte.length);
        bufferPacote.put(numSeqByte);
        bufferPacote.put(dadosByte);
        return bufferPacote.array();
    }





    public static void main(String[] args) throws UnknownHostException  {
            Scanner teclado = new Scanner(System.in);
            System.out.println("----------------------------------------------CLIENTE-----------------------------------------------");
          //  System.out.print("Digite o endereco do servidor: ");
           // String enderecoIP = teclado.nextLine();
            System.out.println("1-Download");
            System.out.println("2-Upload");
            System.out.print("Digite a diretoria do arquivo a ser enviado. (Ex: C:/Users/Diego/Documents/): ");
            String diretoria = teclado.nextLine();
            System.out.print("Digite o nome do arquivo a ser enviado: (Ex: letra.txt): ");
            String nome = teclado.nextLine();
            Cliente cliente = new Cliente(diretoria+nome,false,8010,8011);
            cliente.run();
        }
    }