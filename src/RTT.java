import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RTT {
    private InetAddress ip;
    private long estimatedRTT;
    private long devRTT;
    private long timeout;
    private long ultimoenviado;
    private Map<Long, List<Integer>> pacotes; // key ->tempo em milisegundo em que o conjunto de pacotes foi enviado, value-> conjunt de pacotes enviados


    public RTT(InetAddress ip){
        this.ip=ip;
        estimatedRTT=0;
        devRTT=0;
        timeout=0;
        ultimoenviado=0;
        pacotes= new HashMap<>();
        firsRTT();
    }

    private void firsRTT() {
        try {
            long end = 0;
            long begin = System.currentTimeMillis();
            if (ip.isReachable(500)) {
                end = System.currentTimeMillis();
                this.estimatedRTT = end - begin;
                this.devRTT = end - begin;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void start(List<Integer> pacotes){
        long aux=System.currentTimeMillis();
        this.pacotes.put(aux,pacotes);
        this.timeout = pacotes.size() * (this.estimatedRTT + 4*devRTT);
        this.ultimoenviado=aux;
    }

    public boolean atualizaTimeOut(int sequence){
        List<Integer> aux = this.pacotes.get(ultimoenviado);
        if (aux.contains(sequence)) {
            long now = System.currentTimeMillis();
            this.estimatedRTT = (long) Math.ceil(this.estimatedRTT * 0.875 + 0.125 * (now - ultimoenviado));
            this.devRTT = (long) Math.ceil(0.75 * this.devRTT + 0.25 * Math.abs(now - ultimoenviado - this.estimatedRTT));
            return true;
        }

        return false;
    }


    public boolean timedOut() {
        long now = System.currentTimeMillis();
        if ((now - ultimoenviado) >= timeout)
                return true;
        else
                return false;
    }

    }



