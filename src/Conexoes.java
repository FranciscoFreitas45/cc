
import java.awt.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.*;

    public class Conexoes {
        ReentrantLock lock;
        Queue<Point> ordens;// POINT sendo o x aceitar/rejetar e y o numero da transferencia
        public Conexoes() {
            this.lock=new ReentrantLock();
            this.ordens = new LinkedList<>();

        }
        public void addOrdens(Point p ) {
            this.lock.lock();
            this.ordens.add(p);
            this.lock.unlock();
        }

        public boolean isEmpty(){
            boolean x;
            this.lock.lock();
            x =this.ordens.isEmpty();
            this.lock.unlock();
            return x;
        }

        List<Point> getOrdens() {
            List<Point> aux = new ArrayList<>();
            this.lock.lock();
                while (!isEmpty()){
                    aux.add(ordens.poll());
                }
                this.lock.unlock();
                return aux;
            }
    }
