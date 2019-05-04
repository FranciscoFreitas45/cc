import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PacoteWRQ extends Pacote {
    private String file;
    private int rcvwindow ;//  para o caso de enviar um pedido de download ja informa o outro cliente que o tamanho do buffer do socket que vai receber dados

    public PacoteWRQ(int opcode,int idtrans,int rcvwindow,String file){
        super(opcode,idtrans);
        this.rcvwindow=rcvwindow;
        this.file=file;
    }

    public byte [] gerarPacote(){
            byte[] code = ByteBuffer.allocate(Transferencia.CABECALHO).putInt(this.opcode).array();
            byte[] idtrans = ByteBuffer.allocate(Transferencia.CABECALHO).putInt(this.idTrans).array();
            byte[] window = ByteBuffer.allocate(Transferencia.CABECALHO).putInt(this.rcvwindow).array();
            byte[] filebyte = this.file.getBytes();
            ByteBuffer bufferPacote = ByteBuffer.allocate(Transferencia.CABECALHO*3 + filebyte.length);
            bufferPacote.put(code);
            bufferPacote.put(idtrans);
            bufferPacote.put(window);
            bufferPacote.put(filebyte);
            return bufferPacote.array();
        }
    }

