public class Janela {
    private int tamanho;
    private boolean ackDuplicado;

  // Tou aplicar o slow start(TCP Reno)
    public Janela() {
        tamanho = 1;
        ackDuplicado = false;

    }

    public void  timeOut(){
        tamanho=1;
    }

    public void ackDuplicado(){
        tamanho=tamanho/2;
        if(tamanho<=0)
                tamanho=1;
        ackDuplicado=true;
    }

    public int getTamanho() {
        int size = tamanho;
        if(ackDuplicado){
            tamanho++;
        }
        else{
            tamanho=tamanho*2;
        }
        return size;
    }
}