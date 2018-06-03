import java.io.*;

public class Mensagem implements Serializable {
    private int oprc;
    private int fileId;
    private String fileName;
    private Object data;
    
    public Mensagem() {
        this.oprc = 0;
        this.fileId = 0;
        this.fileName = new String();
        this.data = null;
    }

    public Mensagem(int oprc, int fileId){
        this.oprc = oprc;
        this.fileId = fileId;
        this.fileName = new String();
        this.data = null;
    }

    public Mensagem(int oprc, String fileName){
        this.oprc = oprc;
        this.fileId = 0;
        this.fileName = fileName;
        this.data = null;
    }

    public Mensagem(int oprc, int fileId, Object data){
        this.oprc = oprc;
        this.fileId = fileId;
        this.fileName = new String();
        this.data = data;
    }

    public Mensagem(int oprc, int fileId, String fileName, Object data){
        this.oprc = oprc;
        this.fileId = fileId;
        this.fileName = fileName;
        this.data = data;
    }

    public int getOprc(){
        return this.oprc;
    }

    public int getFileId(){
        return this.fileId;
    }

    public Object getData(){
        return this.data;
    }

    public String getName(){
        return this.fileName;
    }
    
    

}
