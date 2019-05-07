import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

public class PacoteDados extends Pacote {
    private  int  numSeq;
    private byte[] dados;
private int chunk;

    public PacoteDados(int opcode,int id,int numSeq,int chunk,byte[]dados){
        super(opcode,id);
        this.numSeq=numSeq;
	this.chunk=chunk;
        this.dados=dados;
    }


    public byte [] gerarPacote(){
        CRC32 checksum = new CRC32();
        byte[] checksumBytes;
        byte[] code = ByteBuffer.allocate(Transferencia.CABECALHO).putInt(this.opcode).array();
        byte[] idtrans = ByteBuffer.allocate(Transferencia.CABECALHO).putInt(this.idTrans).array();
        byte[] block = ByteBuffer.allocate(Transferencia.CABECALHO).putInt(this.numSeq).array();
        byte[] chunkbloco = ByteBuffer.allocate(Transferencia.CABECALHO).putInt(this.chunk).array();

        ByteBuffer bufferPacote = ByteBuffer.allocate(Transferencia.CABECALHO*4 + this.dados.length + 8);

        checksum.update(code);
        checksum.update(idtrans);
        checksum.update(block);
	checksum.update(chunkbloco);
        checksum.update(this.dados);

        checksumBytes = ByteBuffer.allocate(8).putLong(checksum.getValue()).array();

        bufferPacote.put(code);
        bufferPacote.put(idtrans);
        bufferPacote.put(block);
        bufferPacote.put(chunkbloco);
        bufferPacote.put(checksumBytes);
        bufferPacote.put(this.dados);
        return bufferPacote.array();
    }
}


