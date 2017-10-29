import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

class SimpleHTTPServer{
    public static void main(String[] args) throws Exception {

            int port = 8080;
            ServerSocket serverSocket = new ServerSocket(port);
            System.err.println("Server Running at port: " + port);
            

            while (true) {
                Socket clientSocket = serverSocket.accept();
                OutputStream out = clientSocket.getOutputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String s=in.readLine();
                HashMap header = new HashMap<String,String>();
                String[] first = s.split(" ");
                header.put("Method",first[0]);
                header.put("Page",first[1]);
                header.put("Protocol",first[2]);
                
                while (!s.isEmpty()) {
                    s = in.readLine();
                    String[] hd=s.split(":");
                    try {header.put(hd[0],hd[1]);} 
                    catch(Exception e) {}  
                }
                GetDetails inst = new GetDetails();
                
                String extn = inst.getExtn(header.get("Page").toString());
                String cntype = inst.getContentType(extn);
                System.out.println(cntype);
                
                byte[] data = inst.fromFile(header.get("Page").toString());
                out.write("HTTP/1.1 200 OK\r\n".getBytes());
                out.write(("Content-Length: "+data.length+"\r\n").getBytes());
                System.out.println(extn);
                
                if(inst.enableDownload(extn)){
                    out.write("Content-Type: application/force-download\r\n".getBytes());    
                } else {
                    out.write(("Content-Type: "+cntype+"\r\n").getBytes());
                }
                out.write("\r\n".getBytes());
                out.write(data);
                in.close();
                out.close();
                socket.close();
           }
           
           
     }
}

class GetDetails{

    byte[] fromFile(String p) {
        if(p.equals("/")) {
            p="/index.html";
        }
        Path path = Paths.get(p.substring(1));
        try {
            byte[] data = Files.readAllBytes(path); 
            return data;
        } catch(Exception e) {
            byte[] data= new byte[0];
            return data;
        }
        
    }
    
    /* For Supporting more types just add a case of that extension and add corresponding Content-Type Header in return  */
    
    String getContentType(String ext){
        switch(ext){    
            case "html":    
            return "text/html";  
            case "mp3":    
            return "audio/mpeg";
            case "mp4":
            return "video/mp4";
            case "jpg":
            return "image/jpg";
            default: return "text/plain";        
        }    
    }
    
    
    String getExtn(String path) {
        if (path.equals("/"))return "html";
        String[] pth = path.split("\\.");
        String extn = pth[pth.length-1];
        return extn;
    }
    
    /* For Supporting Downloading for a type of file just add a case of that extension and add true in return  */ 
    
    boolean enableDownload(String ext) {
        switch(ext){    
            case "html":    
            return false;  
            case "mp3":    
            return true;
            case "mp4":
            return true;
            case "jpg":
            return true;
            default: return false;        
        }   
    } 
}
