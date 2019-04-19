import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import static java.lang.Integer.parseInt;

public class Cliente {
    public static void main(String[] args) {
        try {
            int portaEntrada, portaDestino, tipo;
            BufferedReader sin = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Qual é a porta de entrada: ");

            portaEntrada = parseInt(sin.readLine());
            Conexoes conexoes = new Conexoes();
            Estado estado = new Estado();
            TransfereCC transfere = new TransfereCC(estado, portaEntrada,conexoes);
            Thread t1 = new Thread((transfere));
            t1.start();
            while (true) {
                System.out.println("----------------------------------------------CLIENTE-----------------------------------------------");
                System.out.println("1-Download");
                System.out.println("2-Upload");
                System.out.println("3-Aceitar conexao -- Id de transferencia");
                System.out.println("4-Rejeitar conexão --Id de tarnsferencia");
                System.out.println("0-Sair");


                tipo = parseInt(sin.readLine());
                if (tipo == 0)
                    System.exit(0);
                else if(tipo==3) {
                    System.out.println("Id da Transferencia");
                    int id = parseInt(sin.readLine());
                    Point p = new Point(3,id);
                    conexoes.addOrdens(p);


                }
                else if(tipo==4) {
                    System.out.println("Id da Transferencia");
                    int id = parseInt(sin.readLine());
                    Point p = new Point(4,id);
                    conexoes.addOrdens(p);

                    //Transferencia t = estado.reject(id);
                    //if(t!=null)
                    //transfere.reject(id,t);
                }



                else if(tipo==1 || tipo==2) {
                    System.out.println("Endereco do servidor: ");
                    String enderecoIP = sin.readLine();
                    System.out.println("Porta Destino");
                    portaDestino = parseInt(sin.readLine());
                    System.out.print("Diretoria do ficheiro + nome do ficheiro a ser enviado. (Ex: C:/Users/Diego/Documents/): ");
                    String file = sin.readLine();

                    int size = estado.getSize();
                    Transferencia t = new Transferencia(tipo, size, enderecoIP, portaDestino, file);
                    estado.addTransferencia(t);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
