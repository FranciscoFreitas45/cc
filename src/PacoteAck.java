import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PacoteAck extends Pacote {
    private int numSeq;

    public PacoteAck(int opcode, int id, int numSeq){
        super(opcode,id);
        this.numSeq=numSeq;
    }


    public byte [] gerarPacote(){
        byte[] code = ByteBuffer.allocate(Transferencia.CABECALHO).putInt(this.opcode).array();
        byte[] idtrans = ByteBuffer.allocate(Transferencia.CABECALHO).putInt(this.idTrans).array();
        byte[] block = ByteBuffer.allocate(Transferencia.CABECALHO).putInt(this.numSeq).array();

        ByteBuffer bufferPacote = ByteBuffer.allocate(Transferencia.CABECALHO*3);
        bufferPacote.put(code);
        bufferPacote.put(idtrans);
        bufferPacote.put(block);
        return bufferPacote.array();
    }


}
