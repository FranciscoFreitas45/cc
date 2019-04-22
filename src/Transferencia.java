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
    private int id;
    private int tipo;
    private InetAddress ip;
    private int portaDestino;
    private  String file ;
    private int ultimoEnviado;
    private boolean conexaoEstabelecida;
    private DatagramPacket conexao;
    private Map<Integer,Pacote> pacotes;
    private Map<Integer,Boolean>acks;
    private boolean completa;
    private int n_tentativas=0;



    public Transferencia(int tipo,int id, String ip, int portaDestino, String file){
        this.tipo=tipo;
        this.id=id;
        try {
            this.ip = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.ultimoEnviado=0;
        this.conexaoEstabelecida=false;
        this.portaDestino = portaDestino;
        this.file = file;
        this.pacotes = new HashMap<>();
        this.acks =new HashMap<>();
        this.completa=false;
        downORup();
    }


    public Transferencia(int tipo, int id, InetAddress ip, int portaDestino, String file) {
        this.id = id;
        this.tipo = tipo;
        this.ip = ip;
        this.portaDestino = portaDestino;
        this.file = file;
        this.ultimoEnviado = 0;
        this.conexaoEstabelecida = false;
        this.pacotes = new HashMap<>();
        this.acks = new HashMap<>();
        this.completa = false;
        downORup();
    }

    public void downORup(){
        if(this.tipo==2){
            PacoteWRQ pWRQ = new PacoteWRQ(2,id,file);
           // System.out.println(file);
            byte []cone = pWRQ.gerarPacote();
            //System.out.println("tamaho do cone "+cone.length);
            this.conexao = new DatagramPacket(cone, cone.length,this.ip,this.portaDestino);
            try {
                boolean x= true;
                int i=0;
                FileInputStream fis = new FileInputStream(new File(this.file));
                while(x) {
                    byte[] dataBuffer = new byte[TAMANHO_PACOTE];
                     int tamanho = fis.read(dataBuffer, 0, TAMANHO_PACOTE);
                     if(tamanho<TAMANHO_PACOTE)
                         x=false;
                    byte[] dataBytes = Arrays.copyOfRange(dataBuffer, 0, tamanho);
                    PacoteDados pd = new PacoteDados(3,this.id,i,dataBytes);
                    this.pacotes.put(i,pd);
                    this.acks.put(i,false);
                    i++;
                }
                System.out.println("o tamanho do ficheiro e "+i+" pacotes" );
                fis.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<DatagramPacket> getPackets(){
            int i =0;
            List<DatagramPacket> datagramPackets=new ArrayList<>();
            while(i<5 && ultimoEnviado<this.pacotes.size()){
              Pacote p = pacotes.get(ultimoEnviado);
              PacoteDados pd = (PacoteDados) p;
                byte[] pacote = pd.gerarPacote();
                DatagramPacket datapacket = new DatagramPacket(pacote, pacote.length,this.ip,this.portaDestino);
                datagramPackets.add(datapacket);
                ultimoEnviado++;
        }
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


    public int getId() {
        return id;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPortaDestino() {
        return portaDestino;
    }

    public String getFile() {
        return file;
    }

    public Map<Integer,Pacote> getPacotes() {
        return pacotes;
    }

    public Map<Integer, Boolean> getAcks() {
        return acks;
    }
    public void setId(int id) {
        this.id = id;
    }

    public void setPacote(Pacote p,int numseq){
            this.pacotes.put(numseq,p);

    }

    public void setConexaoEstabelecida(boolean conexaoEstabelecida) {
        this.conexaoEstabelecida = conexaoEstabelecida;
    }

    public void setAck(int numseq){
        this.acks.replace(numseq,true);
    }


    public boolean isTodosAcks(){
        boolean x= true;
        for(boolean b : this.acks.values()){
            if(!b)
                return false;
        }
        return x;
    }

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



    public void escreveFicheiro(){
        try {
                File ficheiro= new File(this.file);
            FileOutputStream fos = new FileOutputStream(ficheiro);
        for(Pacote p : this.pacotes.values()) {
            PacoteDados pd =(PacoteDados)p;
            byte  [] dados = pd.gerarPacote();
            fos.write(dados, CABECALHO*3, dados.length- CABECALHO*3);
        }
        fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
