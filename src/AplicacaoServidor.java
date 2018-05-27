import java.net.*;
import java.io.*;
import java.util.*;


public class AplicacaoServidor extends Thread{
    private ServerSocket servidor;
    private int porta;
    private Mensagem nick;
    private ObjectInputStream streamEntrada;

    private Map<String, Cliente> clientes;
    private Map<String, Socket> socks;

    private Vector<File> files;

    public AplicacaoServidor(int porta) throws IOException {
        this.porta = porta;
        servidor = new ServerSocket(porta); 
        this.socks = new HashMap<String, Socket>();
        this.clientes = new HashMap<String, Cliente>();
        this.files = new Vector<File>();
    }

    public Vector<File> getFiles(){
        return this.files;
    }

    public File getFile(int id){
        return this.files.get(id);
    }

    public void esperaConexoes() throws IOException {
        Socket auxS;

        while(true){
            try{
                auxS = servidor.accept();
                System.out.println("Nova conexão com o cliente " + auxS.getInetAddress().getHostAddress());
                this.socks.put(auxS.getInetAddress().getHostAddress(), auxS);
                this.clientes.put(auxS.getInetAddress().getHostAddress(), new Cliente(auxS, this, this.files));
                this.clientes.get(auxS.getInetAddress().getHostAddress()).start();
            }catch(Exception e){}
        }
    }

    public void run() {
        try{
            esperaConexoes();
        }catch(Exception e){}
    
    }

    public void finaliza() throws IOException  {
        
        for(String aux : this.clientes.keySet()){
            this.clientes.get(aux).finaliza();
        }
        for(String aux : this.socks.keySet()){
            this.socks.get(aux).close();
        }

        servidor.close();
    }
    
    public void enviaMensagemTodos(Mensagem msg) throws IOException  {

        for(String aux : this.clientes.keySet()){
            this.clientes.get(aux).saida().writeObject(msg);
        }
    }

    public void enviaArquivo(File file, String ip) throws IOException{
        this.clientes.get(ip).saida().writeObject(file);
    }
    
    public int getPorta() {
        return porta;
    }

    public static void listFilesForFolder(final File folder, Vector<File> files) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry, files);
            } else {
                files.add(fileEntry);
                System.out.println(fileEntry.getName());
            }
        }
    }

    public static void main(String[] args) throws IOException {
        boolean fim = false;
        AplicacaoServidor server = new AplicacaoServidor(12345);

        final File folder = new File("../files/");
        AplicacaoServidor.listFilesForFolder(folder, server.getFiles());

        System.out.println("Porta 12345 aberta!");
        
        System.out.println("Clientes conectando...");    
        server.start();
        
        Scanner teclado = new Scanner(System.in);
        String msg;
        
        do {
            System.out.println("Digite fim para sair: ");
            msg = teclado.nextLine();
            if (msg.equals("fim") || msg.equals("FIM"))
                fim = true;
        } while (!fim);
        
        teclado.close();
        server.finaliza();
        
    }
    
}

class Cliente extends Thread {
    private Socket sock;
    private boolean fim;
    private AplicacaoServidor server;
    private ObjectInputStream streamEntrada;
    private ObjectOutputStream streamSaida;
    private Vector<File> files;

    
    public Cliente(Socket sock, AplicacaoServidor server, Vector<File> files) throws IOException  {
        this.sock = sock;
        this.server = server;
        this.files = files;
        fim = false;
        streamSaida = new ObjectOutputStream(sock.getOutputStream());
        streamEntrada = new ObjectInputStream(sock.getInputStream());

    }
    
    public void finaliza() {
        fim = true;
    }
    
    
    public ObjectInputStream entrada() {
        return streamEntrada;
    }

    public ObjectOutputStream saida() {
        return streamSaida;
    }

    
    public void run() {
        try {
            Mensagem msg;
            while (!fim) { 
            
                msg = (Mensagem) streamEntrada.readObject();
                System.out.println("Preparando...");
                server.enviaArquivo(this.files.get(0), this.sock.getInetAddress().getHostAddress());
                System.out.println("Mensagem enviada para todos!");
            } 
            streamEntrada.close();
        } catch (Exception e) {
                System.err.println("Erro: " + e.getMessage());
            
        }
    }
}

