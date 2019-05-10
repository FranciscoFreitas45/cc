
import java.awt.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.*;

    public class Conexoes {
        ReentrantLock lock;
        Queue<Point> ordens;// POINT sendo o x aceitar/rejetar e y o numero da transferencia
        Queue<Transferencia> pedidos;
        public Conexoes() {
            this.lock=new ReentrantLock();
            this.ordens = new LinkedList<>();
            this.pedidos = new LinkedList<>();

        }
        public void addOrdens(Point p ) {
            this.lock.lock();
            this.ordens.add(p);
            this.lock.unlock();
        }

        public void addTransferencias(Transferencia t ) {
            this.lock.lock();
            this.pedidos.add(t);
            this.pedidos.size();
            this.lock.unlock();
        }




        public boolean isEmpty(){
            boolean x;
            x =this.ordens.isEmpty();
            return x;
        }

        public boolean isPedidos(){
            boolean x;
            x =this.pedidos.isEmpty();
            return x;
        }

       public List<Point> getOrdens() {
            List<Point> aux = new ArrayList<>();
            this.lock.lock();
                while (this.ordens.size()!=0){
                    aux.add(ordens.poll());
                }
                this.lock.unlock();
                return aux;
            }

     public  List<Transferencia> getTransferencias() {
            List<Transferencia> aux = new ArrayList<>();
            this.lock.lock();
            while (this.pedidos.size()!=0){
                aux.add(pedidos.remove());
            }
            this.lock.unlock();
            return aux;
        }

    }
