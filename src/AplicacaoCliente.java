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
        try{
            this.streamSaida.writeObject(msg);
        }catch(Exception e){

        }
    }

    public Object recebeMensagem(){
        try{
            return this.streamEntrada.readObject();
        }catch(Exception e){
            return null;
        }
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
        int prot[] = new int[2];
        int op;
        Vector<File> files = new Vector<File>();
        Vector<String> nameFiles = new Vector<String>();
        
        InputStreamReader streamTeclado = new InputStreamReader(System.in);
        BufferedReader teclado0 = new BufferedReader(streamTeclado);

        Scanner teclado = new Scanner(System.in); 

        System.out.println("Digite seu nickname: ");
        nick = teclado0.readLine();
            
        AplicacaoCliente cliente = new AplicacaoCliente("192.168.2.15", 12345, nick);
        System.out.println(nick + " se conectou ao servidor!");
        
        do {
            System.out.println("1 - Listar arquivos disponíveis/n2 - Listar arquivos locais/nFIM - Finalizar");
            op = teclado.nextInt();
            if(op == 1){
                int k = 0;
                prot[0] = op;
                prot[1] = 0;
                cliente.enviaMensagem(prot);
                nameFiles = (Vector<String>) cliente.recebeMensagem();
                for(String index : nameFiles){
                    System.out.println(k + " - " + index);
                }
                System.out.println("Escolha um arquivo ou digite 0 para voltar: ");
                op = teclado.nextInt();
                if(op != 0){
                    prot[0] = 2;
                    prot[1] = op;
                    cliente.enviaMensagem(prot);
                    files.add( (File) cliente.recebeMensagem() );
                }

            }else if(op == 2){

            }else if (op == 0){
                fim = true;    
            }else{}             
                //cliente.enviaMensagem(msg, nick);
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

