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

    private Map<String, File> files;

    public AplicacaoServidor(int porta) throws IOException {
        this.porta = porta;
        servidor = new ServerSocket(porta, 5, null); 
        this.socks = new HashMap<String, Socket>();
        this.clientes = new HashMap<String, Cliente>();
        this.files = new HashMap<String, File>();
    }

    public Map<String, File> getFiles(){
        return this.files;
    }

    public File getFile(String name){
        return this.files.get(name);
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

                Mensagem aux = new Mensagem(0,0,auxS.getInetAddress().getHostAddress());
                this.clientes.get(auxS.getInetAddress().getHostAddress()).saida().writeObject(aux);
                
            }catch(Exception e){}
        }
    }

    public void deletaCliente(String client){
        this.clientes.remove(client);
        this.socks.remove(client);
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
        this.stop();
        servidor.close();
    }
    
    public void enviaMensagemTodos(Mensagem msg) throws IOException  {

        for(String aux : this.clientes.keySet()){
            this.clientes.get(aux).saida().writeObject(msg);
        }
    }

    public void enviaArquivo(File file, String ip) throws IOException{
        byte[] result = Util.capturaArquivo(file);
        Mensagem aux = new Mensagem(0, 0, file.getName(), result );
        this.clientes.get(ip).saida().writeObject(aux);
    }

    public void enviaNomes( String ip ) throws IOException{
        Map<String, Integer> nomes = new HashMap<String, Integer>();
        for(String index : this.files.keySet()){
            nomes.put(index, (int)this.files.get(index).length());
        }
        Mensagem aux = new Mensagem(0, 0, nomes );
        this.clientes.get(ip).saida().writeObject(aux);
    }

    public void enviaClientes( String ip ) throws IOException{
        Vector<String> aux = new Vector<String>();
        for(String index : this.clientes.keySet()){
            aux.add(index);
        }

        Mensagem data = new Mensagem(0, 0, aux);
        this.clientes.get(ip).saida().writeObject(data);
    }
    
    public int getPorta() {
        return porta;
    }

    

    public static void main(String[] args) throws IOException {
        File serverFolder = new File("../serverFiles/");
        if(!serverFolder.isDirectory()){
            serverFolder.mkdirs();
        }
        
        boolean fim = false;
        AplicacaoServidor server = new AplicacaoServidor(12975);

        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while(e.hasMoreElements()){
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements()){
                InetAddress i = (InetAddress) ee.nextElement();
                String serverIP = i.getHostAddress();
                if(!serverIP.contains(":") && !serverIP.contains("127.0.0.1")){
                    System.out.println("Servidor iniciado com sucesso!\nEndereço: "+ serverIP+"\nPorta: 12975");
                }
            }
        }
        
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
    private Map<String, File> files;

    
    public Cliente(Socket sock, AplicacaoServidor server, Map<String, File> files) throws IOException  {
        this.sock = sock;
        this.server = server;
        this.files = files;
        fim = false;
        streamSaida = new ObjectOutputStream(sock.getOutputStream());
        streamEntrada = new ObjectInputStream(sock.getInputStream());

    }
    
    public void finaliza() {
        fim = true;
        this.stop();
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
                msg = ( Mensagem ) streamEntrada.readObject();
                if(msg.getOprc() == 1 ){
                    final File folder = new File("../serverFiles/");
                    Util.listaArquivos(folder, server.getFiles());
                    System.out.println("Preparando...");
                    server.enviaNomes(this.sock.getInetAddress().getHostAddress());
                    System.out.println("Mensagem enviada para: "+ this.sock.getInetAddress().getHostAddress());
                }else if(msg.getOprc() == 2 ){
                    System.out.println("Preparando...");
                    server.enviaArquivo(this.files.get( msg.getName() ), this.sock.getInetAddress().getHostAddress());
                    System.out.println("Mensagem enviada para: "+ this.sock.getInetAddress().getHostAddress());
                }else if(msg.getOprc() == 3){
                    System.out.println("Preparando...");
                    server.enviaClientes(this.sock.getInetAddress().getHostAddress());
                    System.out.println("Mensagem enviada para: "+ this.sock.getInetAddress().getHostAddress());
                }else if(msg.getOprc() == -1){
                    this.server.deletaCliente((String)msg.getData());
                    System.out.println("Cliente " +(String)msg.getData() +" desconectou.");
                    this.stop();
                }
                
            } 
            streamEntrada.close();
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }
}
