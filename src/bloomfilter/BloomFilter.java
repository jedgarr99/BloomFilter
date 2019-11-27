/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bloomfilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Scanner;
import java.lang.Math;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;
import java.util.Collection;


/**
 *
 * @author Edgar
 */
public class BloomFilter {
    static final Charset charset = Charset.forName("UTF-8"); // Clave utilizada para guardar los strings como bytes 
    static final String hashName = "MD5"; //Algoritmo para hashear que sera utilizado -> Si se necesita mayor precision esta SHA1
    static final MessageDigest digestFunction;
     static { // The digest method is reused between instances
        MessageDigest tmp;
        try {
            tmp = java.security.MessageDigest.getInstance(hashName);
        } catch (NoSuchAlgorithmException e) {
            tmp = null;
        }
        digestFunction = tmp;
    }

    /**
     * @param args the command line arguments
     */
    public static int[] createHashes(byte[] data, int hashes) {
        int[] result = new int[hashes];

        int k = 0;
        byte salt = 0;
        while (k < hashes) {
            byte[] digest;
            synchronized (digestFunction) { // solo se puede ejecutar una a la vez 
                digestFunction.update(salt);
                salt++;
                digest = digestFunction.digest(data); //devuelve 16 bytes segun el objeto hasheado
               System.out.println( Arrays.toString(digest));                
            }
        
            for (int i = 0; i < digest.length/4 && k < hashes; i++) {
                int h = 0;
                for (int j = (i*4); j < (i*4)+4; j++) {
                    h <<= 8;
                    h |= ((int) digest[j]) & 0xFF;
                }
                result[k] = h;
                k++;
            }
        }
        return result;
    }
    public static void main(String[] args) {
        
        /* int i = 0;
        boolean ban;
        Scanner sc = null;
        String[] lista = new String[10000];
        int[] coincidencias= new int[35];
        
        try {
            File ent = new File("C:\\Users\\Edgar\\Desktop\\Heap\\palabras.txt");
            sc = new Scanner(new FileReader(ent));

        } catch (FileNotFoundException e) {
            System.out.println("Input file not found");
            System.exit(1);
        }

        while (i < 10000) {
            String text = sc.nextLine().toLowerCase();
            ban = true;    
            if ((text != null) && (!text.equals("")) && (text.matches("^[a-zA-Z]*$"))) {
                lista[i] = text;
                i++;
            }

        }
        
        for(String pal : lista){
            coincidencias[Math.abs(pal.hashCode()%35)]++;
            
        }
        System.out.println(Arrays.toString(coincidencias));*/
        System.out.println( Arrays.toString("gato".toString().getBytes(charset)));
         System.out.println( Arrays.toString(createHashes("asasasasas".toString().getBytes(charset),4)));


  
    }
    
}
