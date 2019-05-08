import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.DataTruncation;
import java.util.*;

public class Transferencia {
    static final int CABECALHO = 4;
    static final int TAMANHO_PACOTE = 512;// (numSeq:4, dados=1000) Bytes : 1004 Bytes total
    static final int MAX_Tentativas=3;
    static final int MAX_PACOTES=20480; // corresponde ao numero de bytes;
    private int id;
    private int tipo;
    private InetAddress ip;
    private int portaDestino;
    private  String file ;
    private int ultimoEnviado;
    private int ultimoConfirmado;
    private boolean conexaoEstabelecida;
    private DatagramPacket conexao;
    private Map<Integer,Pacote> pacotes;
    private Map<Integer,Boolean>acks;
    private boolean completa;
    private int n_tentativas=0;
    private int cap_socket_recetor;
    private Janela janela;
    private boolean pedido;
    private int num_pacotes_enviados;
    private RTT rtt;
    private Ficheiro ficheiro;
//tentativa
private int chunk;




    public Transferencia(int tipo, String ip, int portaDestino, String file){
        this.tipo=tipo;
        this.id=0;
        try {
            this.ip = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.ultimoEnviado=-1;
        this.ultimoConfirmado=-1;
        this.conexaoEstabelecida=false;
        this.portaDestino = portaDestino;
        this.file = file;
        this.pacotes = new HashMap<>();
        this.acks =new HashMap<>();
        this.completa=false;
        this.pedido=true;
        this.cap_socket_recetor=0;
        this.num_pacotes_enviados=0;
        this.janela=new Janela();
        this.rtt=new RTT(this.ip);
        this.ficheiro=new Ficheiro(this.file);
this.chunk=-1;
    }


    public int getCap_socket_recetor() {
        return cap_socket_recetor;
    }



    public boolean isPedido() {
        return pedido;
    }

    public Transferencia(int tipo, int id, InetAddress ip, int portaDestino, String file, boolean pedido) {
        this.id = id;
        this.tipo = tipo;
        this.ip = ip;
        this.portaDestino = portaDestino;
        this.file=file;
        this.ultimoEnviado = -1;
        this.ultimoConfirmado=-1;
        this.conexaoEstabelecida = false;
        this.pacotes = new HashMap<>();
        this.acks = new HashMap<>();
        this.completa = false;
        this.pedido=pedido;
        this.cap_socket_recetor=0;
        this.num_pacotes_enviados=0;
        this.janela=new Janela();
        this.rtt=new RTT(this.ip);
        this.ficheiro=new Ficheiro(this.file);
this.chunk=-1;
        downORup();

    }



    public int getTipo() {
        return tipo;
    }

    public void setCap_socket_recetor(int cap_socket_recetor) {
        this.cap_socket_recetor = cap_socket_recetor;
        PacoteWRQ pWRQ = new PacoteWRQ(this.tipo,id,0,file);
        // System.out.println(file);
        byte []cone = pWRQ.gerarPacote();
        this.conexao = new DatagramPacket(cone, cone.length,this.ip,this.portaDestino);

    }


    public void obtemDados(){
        List<byte[]> dados = ficheiro.getPacotes();
        if(dados.size()==0){
            this.completa=true;
            return ;
        }
chunk++;
        this.pacotes.clear();
        this.acks.clear();
        ultimoEnviado=-1;
	    ultimoConfirmado=-1;
        int i=0;
        int tamanhoBytes=0;
        PacoteWRQ pWRQ = new PacoteWRQ(2,id,0,file);
        System.out.println("o titulo é "+file);
        byte []cone = pWRQ.gerarPacote();
        this.conexao = new DatagramPacket(cone, cone.length,this.ip,this.portaDestino);
        for(byte[] d : dados) {
            tamanhoBytes+=d.length;
            PacoteDados pd = new PacoteDados(3, this.id, i,chunk,d);
            this.pacotes.put(i, pd);
            this.acks.put(i, false);
            i++;
        }
        if(tamanhoBytes!=Ficheiro.MAX) {
            System.out.println("li  "+tamanhoBytes+ " bytes " );
            this.completa = true;
        }
    }

    public void downORup(){
        if(this.tipo==2){
           this.ficheiro.inicia_leitura();
           obtemDados();
        }
        else{
            this.ficheiro.iniciaEscrita();
        }
    }

    public List<DatagramPacket> getPackets(){
        int i =0,j=0;
        int max_pacotes= this.cap_socket_recetor/(TAMANHO_PACOTE+3*CABECALHO+8);
        int max_janela = this.janela.getTamanho();
        if(ultimoConfirmado!=ultimoEnviado) {
            this.janela.timeOut();
            max_janela = this.janela.getTamanho();
        }
        List<DatagramPacket> datagramPackets=new ArrayList<>();
        List<Integer> pacotes_enviados=new ArrayList<>();
            // retransmitir perdidos
        for(j=ultimoConfirmado+1;j<this.ultimoEnviado+1;j++) {
            if (i < max_janela) {
                if (!this.acks.get(j)) {// caso ainda nao tenha o ack, tem que retransmitir o pacote pois nao recebeu o ack de confirmação
                        System.out.println("restranmiti o pacote " + j);
                        Pacote p = pacotes.get(j);
                        PacoteDados pd = (PacoteDados) p;
                        byte[] pacote = pd.gerarPacote();
                        DatagramPacket datapacket = new DatagramPacket(pacote, pacote.length, this.ip, this.portaDestino);
                        datagramPackets.add(datapacket);
                        i++;
                        pacotes_enviados.add(j);
                }
            }
        }
            // enviar novos
        while(i<max_janela && ultimoEnviado<this.pacotes.size()) {
            Pacote p = pacotes.get(ultimoEnviado + 1);
            if (p != null) {
                PacoteDados pd = (PacoteDados) p;
                byte[] pacote = pd.gerarPacote();
                DatagramPacket datapacket = new DatagramPacket(pacote, pacote.length, this.ip, this.portaDestino);
                System.out.println("Foi enviado o pacote com id " + (ultimoEnviado + 1));
                datagramPackets.add(datapacket);
                ultimoEnviado++;
                i++;
                pacotes_enviados.add(j);
            }
        }
        this.num_pacotes_enviados=datagramPackets.size();
        this.rtt.start(pacotes_enviados);
        return datagramPackets;
    }


    public boolean isConexaoEstabelecida() {
        return conexaoEstabelecida;
    }

    public DatagramPacket getConexao() {
            n_tentativas++;
            if(n_tentativas>MAX_Tentativas)
                return null;
        return conexao;
    }

    public void setId(int id) {
        this.id = id;
        downORup();
    }

    public int getId() {
        return id;
    }


    public InetAddress getIp() {
        return ip;
    }

    public int getPortaDestino() {
        return portaDestino;
    }


    public void setPacote(Pacote p,int numseq){
        this.pacotes.put(numseq,p);

    }

    public void setConexaoEstabelecida(boolean conexaoEstabelecida) {
        this.conexaoEstabelecida = conexaoEstabelecida;
    }

    public void setAck(int numseq){
            rtt.atualizaTimeOut(numseq);
            num_pacotes_enviados--;
         boolean x=this.acks.get(numseq);
         if(x) {
             System.out.println("ACK DUPLICADO do pacote " + numseq);
             janela.ackDuplicado();
         }
         else
        this.acks.put(numseq,true);
        todosConfirmadosAte();
    }


    public boolean isCompleta() {
        return completa;
    }

    public boolean isTodosAcks(){
        boolean x= true;
        for(boolean b : this.acks.values()){
            if(!b)
                return false;
        }
        return x;
    }
/*
    public List<DatagramPacket> checkAcks(){
        int i =0;
        List<DatagramPacket> datagramPackets=new ArrayList<>();
        for(;i<this.ultimoEnviado;i++){
            if(!this.acks.get(i)){// caso ainda nao tenha o ack, tem que retransmitir o pacote pois nao recebeu o ack de confirmação
                System.out.println("restranmiti o pacote "+i);
                Pacote p = pacotes.get(i);
                PacoteDados pd = (PacoteDados) p;
                byte[] pacote = pd.gerarPacote();
                DatagramPacket datapacket = new DatagramPacket(pacote, pacote.length,this.ip,this.portaDestino);
                datagramPackets.add(datapacket);
            }
        }
        return datagramPackets;
    }

*/

public boolean possoTransmitir(){
    if(num_pacotes_enviados==0)
        return true;
    if(rtt.timedOut()) {
        janela.timeOut();
        return true;
    }
    else return false;
}

public int getChunk(){
	return this.chunk;
}


public void todosConfirmadosAte(){
    int i=ultimoConfirmado+1;
    for(;i<ultimoEnviado;i++){
            boolean ack=this.acks.get(i);
            if(ack)
                this.ultimoConfirmado=i;
            else{
                break;
            }
    }
}

    public void escreveFicheiro() {
        this.ficheiro.escreveFicheiro(this.pacotes);
        this.pacotes.clear();
        this.acks.clear();

    }
}
