import java.net.*;
import java.io.*;
import java.util.*;

public class AplicacaoCliente {

    private Socket servidor;
    private ObjectOutputStream streamSaida;
    private ObjectInputStream streamEntrada;
    private String nick;

    public AplicacaoCliente(String ip, int porta, String nick) throws UnknownHostException, IOException {
        this.nick = nick;
        servidor = new Socket(ip, porta);
        streamSaida = new ObjectOutputStream(servidor.getOutputStream());
        streamEntrada = new ObjectInputStream(servidor.getInputStream());
    }

    public String getIP(){
        return this.servidor.getInetAddress().getHostAddress();
    }

    public void enviaMensagem(Object msg) throws IOException{
        this.streamSaida.writeObject(msg);
    }

    public Object recebeMensagem() throws IOException{
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
        streamSaida.close();
        streamEntrada.close();
        servidor.close();
    }

    public void gravaArquivo(byte[] aInput, String fileName){
        final File file = new File("../received/"+fileName);
        try {
            OutputStream output = null;
            try {
                output = new BufferedOutputStream(new FileOutputStream(file));
                output.write(aInput);
            }
            finally {
                output.close();
            }
        }
        catch(FileNotFoundException ex){
            // log("File not found.");
        }
        catch(IOException ex){
            // log(ex);
        }
    }

    public static void listFilesForFolder(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                if(fileEntry.length() < 1000 ){
                    System.out.println(fileEntry.getName() + " ------------ " + fileEntry.length()+"B");
                }
                else{
                    System.out.println(fileEntry.getName() + " ------------ " + fileEntry.length()/1000+"KB");
                }
            }
        }
        
    }

    public void clearScreen() {  
        System.out.print("\033[H\033[2J");  
        System.out.flush();  
    }  
    
    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
        Mensagem msg;
        String nick = "";
        boolean fim = false;
        int op, auX=1;

        Map<String, Integer> nameFiles = new HashMap<String, Integer>();
        Map<Integer, String> idFiles;

        InputStreamReader streamTeclado = new InputStreamReader(System.in);
        BufferedReader teclado0 = new BufferedReader(streamTeclado);

        Scanner teclado = new Scanner(System.in); 

        File receiveFolder = new File("../received/");

        if(!receiveFolder.isDirectory()){
            receiveFolder.mkdirs();
        }

        System.out.println("Digite seu nickname: ");
        nick = teclado0.readLine();
            
        AplicacaoCliente cliente = new AplicacaoCliente("25.79.218.143", 12975, nick);
        System.out.println(nick + " se conectou ao servidor!");
        
        do {
            cliente.clearScreen();
            System.out.println("SERVIDOR DE ARQUIVOS\n\n");
            System.out.println("1 - Listar arquivos disponíveis\n2 - Listar arquivos locais\n3 - Listar clientes conectados\n0 - Finalizar");
            op = teclado.nextInt();
            if(op == 1){
                cliente.clearScreen();
                System.out.println("ARQUIVOS DO SERVIDOR\n");
                int k = 1;
                idFiles = new HashMap<Integer, String>();
                msg = new Mensagem(1, 0);
                cliente.enviaMensagem(msg);
                msg = (Mensagem) cliente.recebeMensagem();
                nameFiles = (Map<String, Integer>) msg.getData();
                for(String index : nameFiles.keySet()){
                    if(nameFiles.get(index) < 1000 ){
                        System.out.println(k + " - " + index + " ------------ " + nameFiles.get(index)+"B");
                    }
                    else{
                        System.out.println(k + " - " + index + " ------------ " + nameFiles.get(index)/1000+"KB");
                    }
                    idFiles.put(k, index);
                    k = k+1;
                }
                System.out.print("\n\nEscolha um arquivo ou digite 0 para voltar: ");
                op = teclado.nextInt();
                if(op != 0){
                    msg = new Mensagem(2, idFiles.get(op));
                    cliente.enviaMensagem(msg);
                    cliente.clearScreen();
                    System.out.println("Baixando arquivo...");
                    msg = (Mensagem) cliente.recebeMensagem();
                    cliente.gravaArquivo((byte[])msg.getData(), msg.getName());
                    System.out.println("Arquivo recebido com sucesso!");
                    System.out.print("\n\nPressione enter para voltar:"); teclado0.readLine();
                }

            }else if(op == 2){
                cliente.clearScreen();
                System.out.println("ARQUIVOS BAIXADOS\n");
                final File folder = new File("../received/");
                AplicacaoCliente.listFilesForFolder(folder);
                System.out.print("\n\nPressione enter para voltar:"); teclado0.readLine();
            }else if(op == 3){
                cliente.clearScreen();
                System.out.println("CLIENTES CONECTADOS\n");
                msg = new Mensagem(3);
                cliente.enviaMensagem(msg);
                msg = (Mensagem)cliente.recebeMensagem();

                Vector<String> clientes = (Vector<String>)msg.getData();

                for(String index : clientes){
                    System.out.println(index);
                }

                System.out.print("\n\nPressione enter para voltar:"); teclado0.readLine();

            }else if (op == 0){
                msg = new Mensagem(-1, 0, cliente.getIP());
                cliente.enviaMensagem(msg);
                fim = true;    
            }else{}
        } while (!fim);
                
        cliente.finaliza();
    }
}