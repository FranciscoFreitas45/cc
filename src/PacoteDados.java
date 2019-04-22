import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PacoteDados extends Pacote {
    private  int  numSeq;
    private byte[] dados;

    public PacoteDados(int opcode,int id,int numSeq,byte[]dados){
        super(opcode,id);
        this.numSeq=numSeq;
        this.dados=dados;
        System.out.println("tamanho de dados"+dados.length);
    }


    public byte [] gerarPacote(){
        byte[] code = ByteBuffer.allocate(Transferencia.CABECALHO).putInt(this.opcode).array();
        byte[] idtrans = ByteBuffer.allocate(Transferencia.CABECALHO).putInt(this.idTrans).array();
        byte[] block = ByteBuffer.allocate(Transferencia.CABECALHO).putInt(this.numSeq).array();
        ByteBuffer bufferPacote = ByteBuffer.allocate(Transferencia.CABECALHO*3 + this.dados.length);
        bufferPacote.put(code);
        bufferPacote.put(idtrans);
        bufferPacote.put(block);
        bufferPacote.put(this.dados);
        return bufferPacote.array();
    }
}


