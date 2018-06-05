import java.net.*;
import java.io.*;
import java.util.*;

public class Util {

    public void Util(){}

    public static void clearScreen() {  
        System.out.print("\033[H\033[2J");  
        System.out.flush();  
    }

    public static void listaArquivos(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listaArquivos(fileEntry);
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

    public static void listaArquivos(final File folder, Map<String, File> files) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listaArquivos(fileEntry, files);
            } else {
                files.put(fileEntry.getName(), fileEntry);
            }
        }
    }

    public static void gravaArquivo(byte[] aInput, String fileName){
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

    public static byte[] capturaArquivo(File file){
        byte[] result = new byte[(int)file.length()];
        try {
            InputStream input = null;
            try {
                int totalBytesRead = 0;
                input = new BufferedInputStream(new FileInputStream(file));
                while(totalBytesRead < result.length){
                    int bytesRemaining = result.length - totalBytesRead;
                    //input.read() returns -1, 0, or more :
                    int bytesRead = input.read(result, totalBytesRead, bytesRemaining); 
                    if (bytesRead > 0){
                        totalBytesRead = totalBytesRead + bytesRead;
                    }
                }
                // log("Num bytes read: " + totalBytesRead);
            }
            finally {
                // log("Closing input stream.");
                input.close();
            }
        }
        catch (FileNotFoundException ex) {
            // log("File not found.");
        }
        catch (IOException ex) {
            // log(ex);
        }
        return result;
    }
}