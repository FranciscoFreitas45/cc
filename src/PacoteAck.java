import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PacoteAck extends Pacote {
    private int numSeq;
    private int rcvWindow;

    public PacoteAck(int opcode, int id, int numSeq,int rcvWindow){
        super(opcode,id);
        this.numSeq=numSeq;
        this.rcvWindow=rcvWindow;
    }


    public byte [] gerarPacote(){
        byte[] code = ByteBuffer.allocate(Transferencia.CABECALHO).putInt(this.opcode).array();
        byte[] idtrans = ByteBuffer.allocate(Transferencia.CABECALHO).putInt(this.idTrans).array();
        byte[] block = ByteBuffer.allocate(Transferencia.CABECALHO).putInt(this.numSeq).array();
        byte[] window = ByteBuffer.allocate(Transferencia.CABECALHO).putInt(this.rcvWindow).array();
        ByteBuffer bufferPacote = ByteBuffer.allocate(Transferencia.CABECALHO*4);
        bufferPacote.put(code);
        bufferPacote.put(idtrans);
        bufferPacote.put(block);
        bufferPacote.put(window);
        return bufferPacote.array();
    }


}
