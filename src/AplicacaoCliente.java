import java.net.*;
import java.io.*;
import java.util.*;


public class AplicacaoCliente {

    private Socket servidor;
    private ObjectOutputStream streamSaida;
    private ObjectInputStream streamEntrada;
    private ThreadEntrada entrada; 
    private String nick;

    public AplicacaoCliente(String ip, int porta, String nick) throws UnknownHostException, IOException {
        this.nick = nick;
        servidor = new Socket(ip, porta);
        streamSaida = new ObjectOutputStream(servidor.getOutputStream());
        streamEntrada = new ObjectInputStream(servidor.getInputStream());
        /* entrada = new ThreadEntrada(streamEntrada);
        entrada.start(); */
    }

    public void enviaMensagem(Mensagem msg) throws IOException {
        streamSaida.writeObject(msg);
    }
    
    public void enviaMensagem(String msg) throws IOException {
        streamSaida.writeObject(new Mensagem(msg, nick));
    }
    
    public void enviaMensagem(String msg, String destinatario) throws IOException {
        streamSaida.writeObject(new Mensagem(msg, nick, destinatario));
    }

    public void enviaMensagem(Object msg){
        this.streamSaida.writeObject(msg);
    }

    public Object recebeMensagem(){
        return this.streamEntrada.readObject();
    }
    
    public String getNick() {
        return nick;
    }
    
    public void finaliza() throws IOException {
        entrada.finaliza();
        streamSaida.close();
        streamEntrada.close();
        servidor.close();
    }
    
    public static void main(String[] args) 
           throws UnknownHostException, IOException {

        String msg = "", nick = "";
        boolean fim = false;

        Vector<File> files;
        Vector<String> nameFiles;
        
        InputStreamReader streamTeclado = new InputStreamReader(System.in);
        BufferedReader teclado = new BufferedReader(streamTeclado);

        System.out.println("Digite seu nickname: ");
        nick = teclado.readLine();
            
        AplicacaoCliente cliente = new AplicacaoCliente("172.16.103.90", 12345, nick);
        System.out.println(nick + " se conectou ao servidor!");
        
        
        
        do {
            System.out.println("1 - Listar arquivos disponíveis/n2 - Listar arquivos locais/nFIM - Finalizar");
            msg = teclado.readLine();
            if(msg == "1"){
                int k = 0;
                cliente.enviaMensagem("100")
                nameFiles = (Vector<String>) cliente.recebeMensagem();
                for(String index : nameFiles){
                    System.out.println(k + " - " + index);
                }
                System.out.println("Escolha um arquivo ou digite c para voltar: ");
                msg = teclado.readLine();
                if(msg != "c"){
                    cliente.enviaMensagem("2"+msg+"0")
                }

            }else if(msg == "2"){

            }else if (msg.equals("fim") || msg.equals("FIM")){
                fim = true;    
            }else             
                cliente.enviaMensagem(msg, nick);
        } while (!fim);
                
        cliente.finaliza();
    }
    
}

class ThreadEntrada extends Thread {

    private ObjectInputStream streamEntrada;
    private boolean fim;

    public ThreadEntrada(ObjectInputStream streamEntrada) {
        this.streamEntrada = streamEntrada;
        this.fim = false;
    }
    
    public void finaliza() {
        fim = true;
    }

    public void run() {
        Mensagem msg;
        try {
            while(!fim) {
                msg = (Mensagem) streamEntrada.readObject();
                System.out.println(msg.getRemetente() + ": " + msg.getMensagem());
            }
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }
}

