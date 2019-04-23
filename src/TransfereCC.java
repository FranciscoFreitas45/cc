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

import static java.lang.Integer.parseInt;

public class TransfereCC  extends Thread {
    static final int MAX_PRIORITY=10;
    ReentrantLock lock = new ReentrantLock();
    private int portaEntrada;
    private Estado estado;
    private Conexoes conexoes;
    private DatagramPacketBuffer recebe;
    private DatagramPacketBuffer envia;

    public TransfereCC(Estado estado, int portaEntrada,Conexoes conexoes) {
        this.portaEntrada = portaEntrada;
        this.estado = estado;
        this.conexoes=conexoes;
        this.recebe = new DatagramPacketBuffer();
        this.envia = new DatagramPacketBuffer();
    }




    public void accept(int id,Transferencia t){
        if(t.isDownload())
            t.setConexaoEstabelecida(true);
        System.out.println("ola");
           PacoteAck pAck = new PacoteAck(5,id,1);
                byte[] enviar = pAck.gerarPacote();
                DatagramPacket envia =  new DatagramPacket(enviar,enviar.length,t.getIp(),t.getPortaDestino());
                this.envia.add(envia);


    }

    public void reject(int id,Transferencia t){
        PacoteAck pAck = new PacoteAck(5,id,2);
        byte[] enviar = pAck.gerarPacote();
        DatagramPacket envia =  new DatagramPacket(enviar,enviar.length,t.getIp(),t.getPortaDestino());
        this.envia.add(envia);
    }


    public void pacotesRecebidos(){

        List<DatagramPacket> pacotes = recebe.getAll();
        for(DatagramPacket p : pacotes  ){
            analisa(p);
        }
    }




    public void analisa(DatagramPacket p){
                byte[] dados = p.getData();

                int opcode = getOpcode(dados);
                int id,numSeq,portaDestino,resposta;
                String file;
        InetAddress ip;

        if(opcode==1) {//pedido para download-> entao faço upload
            System.out.println("o  cliente com endereco " + p.getAddress() + " e porta " + p.getPort() + " quer fazer download");
            id = getIdTrans(dados);
            System.out.println("id da transacao" + id);


            ip = p.getAddress();
            portaDestino = p.getPort();
            file = getFile(dados,p.getLength());
            System.out.println(file);

            Transferencia t = new Transferencia(2, id, ip, portaDestino, file,true);
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

                Transferencia t = new Transferencia(1,id,ip,portaDestino,ficheiro,true);
                estado.addTransferencia(t);


        }
        else if (opcode==3){// recebi dados
            id = getIdTrans(dados);
            numSeq= getNumSeq(dados);
            byte[] x = getDados(dados,p.getLength());
            PacoteDados pd = new PacoteDados(3,id,numSeq,x);
            System.out.println("numSeq"+numSeq);
            estado.setPacote(pd,id,numSeq);
            PacoteAck ack = new PacoteAck(4,id,numSeq);
            byte [] ackdados = ack.gerarPacote();
                this.envia.add(new DatagramPacket(ackdados,ackdados.length,p.getAddress(),p.getPort()));

        }

        else if (opcode==4){// recebi um ack
            System.out.println("recebi ack");
            id = getIdTrans(dados);
            numSeq= getNumSeq(dados);
            boolean x=estado.setAck(id,numSeq);
            if(x){
                PacoteAck ack = new PacoteAck(6,id,numSeq);
                byte [] ackdados = ack.gerarPacote();
                this.envia.add(new DatagramPacket(ackdados,ackdados.length,p.getAddress(),p.getPort()));
            }

        }

        else if (opcode ==5){// resposta a pedido de conexão
            id = getIdTrans(dados);
          //  System.out.println("recebi um 5 E O id É "+id);
            int r = getNumSeq(dados);
                    if(r==1)// aceitou
            estado.alterarconexao(id);
        }
        else if (opcode==6){// pacote final
            id = getIdTrans(dados);
            System.out.println("recebi um 6 E O id É "+id);
            Transferencia t =estado.getTransferencia(id);
            t.escreveFicheiro();

        }

    }



    public void veOrdens(){
        List<Point> ordens = this.conexoes.getOrdens();
          if(ordens.size()>0){
                    for(Point p : ordens){
                        int x = (int)p.getX();
                        int y =(int)p.getY();
                        if(x==3){
                         Transferencia t = estado.getTransferencia(y);
                         accept(y,t);
                        }
                    }
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

            while (true) {


                veOrdens();
                pacotesRecebidos();
                       for(Transferencia t : estado.getTransferencias().values()){
                                if(t.isConexaoEstabelecida() && t.getTipo()==2){
                                    List<DatagramPacket> retransPacotes= t.checkAcks();
                                    for(DatagramPacket d : retransPacotes) {
                                        this.envia.add(d);
                                    }

                         List<DatagramPacket> pacotes= t.getPackets();
                         for(DatagramPacket d : pacotes) {
                             this.envia.add(d);
                         }
                           }
                           else if (!t.isDownload()){
                                DatagramPacket d = t.getConexao();
                                    if(d!=null)
                                this.envia.add(d);
                                }
                       }
                //System.out.println("Phase 1");
                // interpret_received_packet();

                //System.out.println("Phase 2");
                sleep(10);
            }

        } catch (SocketException | InterruptedException e) {
            e.printStackTrace();
        }


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

    String getFile(byte[] pacote,int tamanho) {
        byte[] file = Arrays.copyOfRange(pacote, 8,tamanho);
        String s = new String(file);
        return s;
    }


     byte[]getDados (byte[] pacote,int tamanho) {
        byte[] dados = Arrays.copyOfRange(pacote, 12, tamanho);
        return dados;
    }




}
