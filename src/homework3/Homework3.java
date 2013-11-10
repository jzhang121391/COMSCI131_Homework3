package homework3;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.*;
import java.util.zip.*;


public class Homework3 {

    public static void chunker_ti
            (int procNo, String fileDir, byte[] b, 
            String[] chunks, int KiB)
    {            
        int chunkIndex = 0;
        int charIndex = 0;
            
            /* Create first 128 KiB chunk without dictionary*/
            
        while(charIndex < b.length) {
            chunks[chunkIndex] += (char) b[charIndex];
            charIndex++;
            if(charIndex % KiB == 0) {
                chunkIndex++;
            }
        }
            
        //System.out.println(chunks[0]);
        //System.out.println(chunks[1]);
        //System.out.println(chunks[2]);
    }
    
    public static void chunker_fi
            (int procNo, String fileDir, byte[] b, 
            String[] chunks, int KiB, int dict)
    {            
        int chunkIndex = 0;
        int charIndex = 0;
            
            /* Create first 128 KiB chunk without dictionary*/
            
        while((charIndex < b.length) && (charIndex < KiB)) {
            chunks[chunkIndex] += (char) b[charIndex];
            charIndex++;
        }
            
        chunkIndex++;
            
            /* For subsequent chunks */
            
        if((charIndex - dict) > 0) {
            charIndex -= dict;
        }
            
        int i = 0; // iterator
            
        if (b.length > KiB) {
            while(charIndex < b.length) {
                chunks[chunkIndex] += (char) b[charIndex];
                charIndex++;
                i++;
                if (((charIndex + 1) % (double) KiB) == 0) {
                    chunkIndex++;
                    for (i = 0; i < dict; i++) {
                        chunks[chunkIndex] += 
                                (char) b[charIndex - dict + i];
                    }
                }
            }
        }
            
            //System.out.println(chunks[0]);
            //System.out.println(chunks[1]);
            //System.out.println(chunks[2]);
    }
    
    public static void main(String[] args)
    {   
        BufferedReader br = 
            new BufferedReader(new InputStreamReader(System.in));
        String fileDir = null;
        try {
            fileDir = br.readLine();
        } 
        catch (IOException ioe) {
        System.out.println("IO error trying to read your name!");
        System.exit(1);
        }
        
        int procNo = 0;
        boolean iParam = false;
        
        for(int j = 0; j < args.length; j++) {
            if (args[j].equals("-p")) {
                procNo = Integer.parseInt(args[j+1]);
            }
            else if (args[j].equals("-i")){
                iParam = true;
            }
        }
        
        File file = new File(fileDir);

        byte[] b = new byte[(int) file.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(b);
            String display = new String(b);
            //System.out.println(display);
        } 
        catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
        }
        catch (IOException e1) {
            System.out.println("Error Reading The File.");
        }
        
        int KiB = 128 * 1024;
        int dict = 32 * 1024;
        
        // 2D array to keep track of each 128 KiB chunk
        String[] chunks = new String[b.length/KiB + 1];
        for(int j = 0; j < (b.length/KiB + 1); j++) {
            chunks[j] = "";
        }
        //System.out.println(b.length/KiB + 1);
        
        if(iParam == true) {
            chunker_ti(procNo, fileDir, b, chunks, KiB);
        }
        else {
            chunker_fi(procNo, fileDir, b, chunks, KiB, dict); 
        }
             
        MyThread[] threadArr = new MyThread[procNo];
        
        for(int j = 0; j < procNo; j++) {
            threadArr[j] = new MyThread(null, null);
        }
        
        //System.out.println("srsly");
        
        int howMany = (b.length/KiB + 1)/procNo;
        //System.out.println(howMany);
        
        //System.out.println((b.length/KiB + 1)/procNo);
        if(b.length > KiB) {
            for(int j = 0; j < (b.length/KiB + 1)/procNo; j++) {
                for(int k = 0; k < procNo; k++) {
                    System.out.println(j+(k*procNo));
                    if (j+(k*howMany) < (b.length/KiB + 1)) {
                        threadArr[k].doCompressFile
                            (chunks[j+(k*howMany)], 
                                fileDir + "_chunk" + (j+(k*howMany)));
                    }
                }
            }
        }
        else {
            threadArr[0].doCompressFile(chunks[0], fileDir);
        }
    }
}