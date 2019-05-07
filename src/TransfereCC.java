import java.awt.*;
import java.io.BufferedReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.CRC32;

import static java.lang.Integer.parseInt;

public class TransfereCC  extends Thread {
    static final int MAX_PRIORITY=10;
    ReentrantLock lock = new ReentrantLock();
    private int portaEntrada;
    private Estado estado;
    private Conexoes conexoes;
    private DatagramPacketBuffer recebe;
    private DatagramPacketBuffer envia;



    public TransfereCC(int portaEntrada,Conexoes conexoes) {
        this.portaEntrada = portaEntrada;
        this.estado = new Estado();
        this.conexoes=conexoes;
        this.recebe = new DatagramPacketBuffer();
        this.envia = new DatagramPacketBuffer();
    }




    public void accept(int id,Transferencia t,int tamanho){
        if(!t.isPedido())// transferencia nao pode ser um pedido deste utilizador mas sim de outro
            t.setConexaoEstabelecida(true);
      if(t.getTipo()==1)// download
      {
              PacoteAck pAck = new PacoteAck(5,id,1,tamanho);
              byte[] enviar = pAck.gerarPacote();
              DatagramPacket envia =  new DatagramPacket(enviar,enviar.length,t.getIp(),t.getPortaDestino());
              this.envia.add(envia);
      }
      else{
          PacoteAck pAck = new PacoteAck(5,id,1,0);
          byte[] enviar = pAck.gerarPacote();
          DatagramPacket envia =  new DatagramPacket(enviar,enviar.length,t.getIp(),t.getPortaDestino());
          this.envia.add(envia);
      }


    }

   /* public void reject(int id,Transferencia t){
        PacoteAck pAck = new PacoteAck(5,id,2);
        byte[] enviar = pAck.gerarPacote();
        DatagramPacket envia =  new DatagramPacket(enviar,enviar.length,t.getIp(),t.getPortaDestino());
        this.envia.add(envia);
    }
*/

    public void pacotesRecebidos(){

        List<DatagramPacket> pacotes = recebe.getAll();
        for(DatagramPacket p : pacotes  ){
            analisa(p);
        }
    }




    public void analisa(DatagramPacket p){
                byte[] dados = p.getData();
                byte[] checksum;
                int opcode = getOpcode(dados);
                int id,numSeq,portaDestino,resposta,chunk;
                String file;
        InetAddress ip;

        if(opcode==1) {//pedido para download-> entao faço upload
            System.out.println("o  cliente com endereco " + p.getAddress() + " e porta " + p.getPort() + " quer fazer download");
            id = getIdTrans(dados);
            System.out.println("id da transacao" + id);
            numSeq=getNumSeq(dados);// neste caso representa a tamanho da janela e nao o numseq;

            ip = p.getAddress();
            portaDestino = p.getPort();
            file = getFile(dados,p.getLength());
            System.out.println(file);

            Transferencia t = new Transferencia(2, id, ip, portaDestino, file,false);
            t.setCap_socket_recetor(numSeq);
            estado.addTransferencia(t);
        }




        else if (opcode==2){// pedido para upload-> entao faço download
            System.out.println("o  cliente com endereco "+p.getAddress()+" e porta "+p.getPort()+" quer fazer upload");
            id = getIdTrans(dados);
            System.out.println("id da transacao " +id);

            id = getIdTrans(dados);
            ip = p.getAddress();
            System.out.println(ip);
            portaDestino=p.getPort();
            file=getFile(dados,p.getLength());
            String[] parts = file.split("/");
            String ficheiro = parts[parts.length-1];
            System.out.println(file);
            System.out.println(ficheiro);

                Transferencia t = new Transferencia(1,id,ip,portaDestino,ficheiro,false);
                estado.addTransferencia(t);


        }
        else if (opcode==3){// recebi dados
            checksum = getCheckSum(dados);
            byte[] checksumBytes1 = Arrays.copyOfRange(dados, 0, 16);
            byte[] checksumBytes2 = Arrays.copyOfRange(dados, 24, p.getLength());
            CRC32 check = new CRC32();
            check.update(checksumBytes1);
            check.update(checksumBytes2);
            byte[] resultChecksum = ByteBuffer.allocate(8).putLong(check.getValue()).array();
            if(Arrays.equals(resultChecksum,checksum)){
                id = getIdTrans(dados);
		chunk= getChunk(dados);
                numSeq= getNumSeq(dados);
                byte[] x = getDados(dados,p.getLength());
                PacoteDados pd = new PacoteDados(3,id,numSeq,chunk,x);
                System.out.println("numSeq"+numSeq);
                estado.setPacote(pd,id,numSeq);
                PacoteAck ack = new PacoteAck(4,id,numSeq,chunk);
                byte [] ackdados = ack.gerarPacote();
                    this.envia.add(new DatagramPacket(ackdados,ackdados.length,p.getAddress(),p.getPort()));
            }
            else System.out.println("Ups tive erro de checksuuuuum");
        }

        else if (opcode==4){// recebi um ack
            id = getIdTrans(dados);
            numSeq= getNumSeq(dados);
	chunk= getChunk(dados);
            System.out.println("recebi ack com "+numSeq);
            int x=estado.setAck(id,numSeq,chunk);// se devolver 1 ja recebeu os ack todos,2 se recebeu todos os ack do chunk, 3 se nao recebeu os ack todos do chunk ainda
            if(x==1 || x==2){
                PacoteAck ack = new PacoteAck(6,id,numSeq,0);
                byte [] ackdados = ack.gerarPacote();
                this.envia.add(new DatagramPacket(ackdados,ackdados.length,p.getAddress(),p.getPort()));
            }

        }

        else if (opcode ==5){// resposta a pedido de conexão
            id = getIdTrans(dados);
            System.out.println("recebi um 5 E O id É "+id);
            int r = getNumSeq(dados);
            int janela=getTamJanela(dados);
                    if(r==1)// aceitou
            estado.alterarconexao(id,janela);
        }
        else if (opcode==6){// pacote final
            id = getIdTrans(dados);
            System.out.println("recebi um 6 E O id É "+id);
            Transferencia t =estado.getTransferencia(id);
            t.escreveFicheiro();

        }

    }



    public void veOrdens(int tamanho){
        List<Point> ordens = this.conexoes.getOrdens();
          if(ordens.size()>0){
                    for(Point p : ordens){
                        int x = (int)p.getX();
                        int y =(int)p.getY();
                        if(x==3){
                         Transferencia t = estado.getTransferencia(y);
                         accept(y,t,tamanho);
                        }
                    }
                }
    }
    public void vePedidos(){
        List<Transferencia> pedidos = this.conexoes.getTransferencias();
        for(Transferencia t : pedidos){
            estado.addTransferencia(t);
        }
    }


    public void run ()

    {
        try {
            DatagramSocket socket = new DatagramSocket(this.portaEntrada);
            Thread t1 = new Thread(new ThreadEnvia(this.envia, socket));
            Thread t2 = new Thread(new ThreadRecebe(this.recebe, socket));
            t1.start();
            t2.start();
            int tamanho =socket.getReceiveBufferSize();

            while (true) {

                estado.putRcvwindow(tamanho);
                veOrdens(tamanho);
                vePedidos();
                pacotesRecebidos();
                       for(Transferencia t : estado.getTransferencias().values()){
                                if(t.isConexaoEstabelecida() && t.getTipo()==2 && t.possoTransmitir()){

                         List<DatagramPacket> pacotes= t.getPackets();
                         for(DatagramPacket d : pacotes) {
                             this.envia.add(d);
                         }
                           }
                           else if (t.isPedido()){
                                DatagramPacket d = t.getConexao();
                                    if(d!=null){
                                        this.envia.add(d);
                                        System.out.println("eeee");
                                    }
                                  // else System.out.println("null");
                                }
                       }
                //System.out.println("Phase 1");
                // interpret_received_packet();

                //System.out.println("Phase 2");
            }

        } catch (SocketException e) {
            e.printStackTrace();
            System.out.println("erro");
        }


    }

    byte[] getCheckSum(byte[] pacote) {
        byte[] checksum = Arrays.copyOfRange(pacote, 16, 24);
        return checksum;
    }

    int getOpcode(byte[] pacote) {
        byte[] getOpcode = Arrays.copyOfRange(pacote, 0, Transferencia.CABECALHO);
        return ByteBuffer.wrap(getOpcode).getInt();
    }

    int getIdTrans(byte[] pacote) {
        byte[] getId = Arrays.copyOfRange(pacote, 4, 8);
        return ByteBuffer.wrap(getId).getInt();
    }

    int getNumSeq(byte[] pacote) {
        byte[] getNum = Arrays.copyOfRange(pacote, 8, 12);
        return ByteBuffer.wrap(getNum).getInt();
    }
    int getTamJanela(byte[] pacote) {
        byte[] getNum = Arrays.copyOfRange(pacote, 12, 16);
        return ByteBuffer.wrap(getNum).getInt();
    }

    String getFile(byte[] pacote,int tamanho) {
        byte[] file = Arrays.copyOfRange(pacote, 12,tamanho);
        String s = new String(file);
        return s;
    }


     int getChunk (byte[] pacote) {
        byte[] dados = Arrays.copyOfRange(pacote, 12, 16);
      return ByteBuffer.wrap(dados).getInt();
    }


     byte[]getDados (byte[] pacote,int tamanho) {
        byte[] dados = Arrays.copyOfRange(pacote, 24, tamanho);
        return dados;
    }




}
