/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package homework3;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author DNCeSWiTHBLLeTS
 */
public class MyThread extends Thread {
    
    private String text;
    private String fileDir;
    
    public MyThread(String text, String fileDir) {
        this.text = text;
        this.fileDir = fileDir;
    }
    
    public static void doCompressFile(String text, String fileDir){
        try{
            FileOutputStream fos = new FileOutputStream(fileDir + ".gz");
            GZIPOutputStream gzos = new GZIPOutputStream(fos);
            gzos.write(text.getBytes());
            gzos.close();
            FileInputStream fis = new FileInputStream(fileDir + ".gz");
            GZIPInputStream gzis = new GZIPInputStream(fis);
            byte[] buffer = new byte[1024];
            gzis.read(buffer);
            text = new String(buffer);
            System.out.println(text);
        }
        catch(IOException e){
            System.out.println("Exception is" + e);
        }
        //return text;
    }
    
    public void run() {
        doCompressFile(text, fileDir);
    }
}
