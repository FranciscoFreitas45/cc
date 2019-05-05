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
        this.lock.lock();
        this.transferencias.put(t.getId(),t);
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
    public int setAck(int id,int numseq){
        int x;
        lock.lock();
        Transferencia t = transferencias.get(id);
        t.setAck(numseq);
        if(t.isTodosAcks() && t.isCompleta())
            x=1;
        else if(t.isTodosAcks()){
            x=2;
            t.obtemDados();
        }
        else x=3;
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
}
