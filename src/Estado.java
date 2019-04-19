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
    public void setAck(int id,int numseq){
        lock.lock();
        Transferencia t = transferencias.get(id);
        t.setAck(numseq);
        lock.unlock();

    }

    public void alterarconexao(int id){
        lock.lock();
        Transferencia t = transferencias.get(id);
        t.setConexaoEstabelecida(true);
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
}
