import java.io.*;
import java.util.*;

public class Ficheiro {
    static int MAX=1024*10;
    private String nome;
    private FileInputStream fis;
    private FileOutputStream fos ;// para o cliente que faz dowload;


    public Ficheiro(String nome){
        this.nome=nome;
    }


    public List<byte[]> getPacotes(){
        List<byte[]> pacotes= new ArrayList<>();
        byte[] dataBuffer = new byte[Transferencia.TAMANHO_PACOTE];
        int tamanhoAcc=0;
        try {
            while(tamanhoAcc<MAX) {
                int tamanho = fis.read(dataBuffer, 0, Transferencia.TAMANHO_PACOTE);
                    if(tamanho==-1) {// nao leu nada; para cobrir casos em que o tamanho do ficheiro é multiplo de 512;
                        break;
                    }

                byte[] dataBytes = Arrays.copyOfRange(dataBuffer, 0, tamanho);
                pacotes.add(dataBytes);
                tamanhoAcc+=dataBytes.length;
                if (tamanho < Transferencia.TAMANHO_PACOTE) {
                        fis.close();
                        System.out.println("li "+tamanhoAcc + " de bytes na classe ficheiro");
                        return pacotes;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return pacotes;

    }


    public void inicia_leitura(){
        try {
            this.fis= new FileInputStream(new File(this.nome));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void iniciaEscrita(){
            try {
                String[] parts = this.nome.split("/");
                String nome_ficheiro = parts[parts.length-1];
                File ficheiro= new File(nome_ficheiro);
                this.fos = new FileOutputStream(ficheiro);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }



    public void escreveFicheiro(Map<Integer,Pacote> pacotes){

        try {
            for(Pacote p :pacotes.values()) {
                PacoteDados pd =(PacoteDados)p;
                byte  [] dados = pd.gerarPacote();
                int tamanho=dados.length- Transferencia.CABECALHO*3;
                    fos.write(dados, Transferencia.CABECALHO*3, tamanho);
                   if(tamanho<Transferencia.TAMANHO_PACOTE) {
                       fos.close();
                       break;
                   }
                }
            } catch (IOException e) {
                 e.printStackTrace();
            }

        }

    }






