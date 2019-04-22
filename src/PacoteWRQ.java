import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PacoteWRQ extends Pacote {
    private String file;

    public PacoteWRQ(int opcode,int idtrans,String file){
        super(opcode,idtrans);
        this.file=file;
    }

    public byte [] gerarPacote(){
            byte[] code = ByteBuffer.allocate(Transferencia.CABECALHO).putInt(this.opcode).array();
            byte[] idtrans = ByteBuffer.allocate(Transferencia.CABECALHO).putInt(this.idTrans).array();
            byte[] filebyte = this.file.getBytes();
            System.out.println("tamanho do ficheiro"+filebyte.length);
            ByteBuffer bufferPacote = ByteBuffer.allocate(Transferencia.CABECALHO*2 + filebyte.length);
            bufferPacote.put(code);
            bufferPacote.put(idtrans);
            bufferPacote.put(filebyte);
            return bufferPacote.array();
        }
    }

