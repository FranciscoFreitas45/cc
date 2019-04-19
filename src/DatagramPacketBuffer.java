import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DatagramPacketBuffer {
    private ReentrantLock lock;
    private Queue<DatagramPacket> pacotesDatagram;
    Condition vazio;


    public DatagramPacketBuffer(){
        this.lock= new ReentrantLock();
        this.pacotesDatagram= new LinkedList<>();
        this.vazio=lock.newCondition();
    }

    public void add(DatagramPacket p){
        lock.lock();
        pacotesDatagram.add(p);
        vazio.signalAll();
        lock.unlock();
    }
    public DatagramPacket get(){
        lock.lock();
        if(pacotesDatagram.size()==0) {
            try {
                vazio.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        DatagramPacket p = pacotesDatagram.poll();
        lock.unlock();
        return p;
    }


    public List<DatagramPacket> getAll(){
        List<DatagramPacket> packets = new ArrayList<>();
        this.lock.lock();
        while(!this.pacotesDatagram.isEmpty()){
            packets.add(pacotesDatagram.remove());
        }
        this.lock.unlock();
        return packets;
    }

}
