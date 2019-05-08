import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class Estado {
    ReentrantLock lock;
    Map<Integer,Transferencia> transferencias;

    public Estado(){
        this.lock= new ReentrantLock();
        this.transferencias=new HashMap<>();
    }

    public void addTransferencia(Transferencia t){
        int x;
        this.lock.lock();
        if(t.isPedido())// caso em que é um pedido meu
        x= transferencias.size();
        else x= t.getId();// caso em que o id vem de fora
        t.setId(x);
        this.transferencias.put(x,t);
        System.out.println(this.transferencias.toString());
        this.lock.unlock();
    }

    public int getSize(){
        this.lock.lock();
        int x =this.transferencias.size();
        this.lock.unlock();
        return x;
    }

    public Transferencia getTransferencia(int i){
        this.lock.lock();
        Transferencia t = this.transferencias.get(i);
        this.lock.unlock();
        return t;
    }

    public Map<Integer, Transferencia> getTransferencias() {
        lock.lock();
        Map<Integer,Transferencia> aux = new HashMap<>();
        aux = transferencias;
        lock.unlock();
        return aux;

    }

    public void setPacote(Pacote p,int id,int numseq){
        lock.lock();
        Transferencia t = transferencias.get(id);
        t.setPacote(p,numseq);
        lock.unlock();
    }

    public int setAck(int id,int numseq,int chunk){
        int x;
        lock.lock();
        Transferencia t = transferencias.get(id);
        if(t!=null) {
            int check = t.getChunk();
            if (check == chunk) {
                t.setAck(numseq);
                if (t.isTodosAcks() && t.isCompleta()) {
                    x = 1;
                    System.out.println("ACABEI TRANSFERENCIA");
                    apagaTransferencia(id);
                } else if (t.isTodosAcks()) {
                    x = 2;
                    t.obtemDados();
                    System.out.println("TRANSMITI OS PACOTES TODOS");
                } else x = 3;
            } else {
                x = 3;
System.out.println("CHUNK JA PASSOU");
            }
        }
else{
    x=3;

            }
     lock.unlock();
        return x;
    }

    public void alterarconexao(int id,int tamanhojanela){
        lock.lock();
        Transferencia t = transferencias.get(id);
        t.setConexaoEstabelecida(true);
        t.setCap_socket_recetor(tamanhojanela);
        lock.unlock();
    }
    public Transferencia Accept(int i){
        Transferencia t;
        lock.lock();
         t = this.transferencias.get(i);
        if(t!=null) t.setConexaoEstabelecida(true);
        lock.unlock();
        return t;
    }

    public Transferencia reject(int i){
        Transferencia t = null;
        lock.lock();
        t = this.transferencias.get(i);
        lock.unlock();
        return t;
    }

    public void putRcvwindow(int tamanho){
    lock.lock();
        for(Transferencia t: transferencias.values()){
            if(t.getCap_socket_recetor()==0){// é sempre incializado a 0 e esta funçao serve para alterar esse avalor
                    t.setCap_socket_recetor(tamanho);
            }
        }
        lock.unlock();
    }


    public void apagaTransferencia (int id){
        this.transferencias.remove(id);
    }
}
