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

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;

/**
 *
 * @author Edgar
 */
public class BloomFilter<T> {

    private BitSet bitset;
    private int size; //tamaño del bitset
    private int elementosEsperados; // maxima cantidad de elementos esperados 
    private int cant; // cantidad actual de elementos almacenados 
    private int k; //numero de funciones de hash a aplicar 
    static final Charset charset = Charset.forName("UTF-8"); // Clave utilizada para guardar los strings como bytes 
    static final String hashName = "MD5"; //Algoritmo para hashear que sera utilizado -> Si se necesita mayor precision esta SHA1
    static final MessageDigest digestFunction;
    static { // prueba que el metodo de hasheo en hashName exista 
        MessageDigest tmp;
        try {
            tmp = java.security.MessageDigest.getInstance(hashName);
        } catch (NoSuchAlgorithmException e) {
            tmp = null;
        }
        digestFunction = tmp;
    }

    /**
     * @param c bits por elemento
     * @param n numero de elementos a almacenar
     * @param k numero de funciones de hash a utilizar
     */
    public BloomFilter(){ // constructor vacio por si solo te interesa saber el tamaño minimo 
        
    }
 
    public BloomFilter(double c, int n, int k) {
        this.elementosEsperados = n;
        this.k = k;
        this.size = (int) Math.ceil(c * n);
        cant = 0;
        this.bitset = new BitSet(size);
    }

    /**
     * @param porcentajeFalsosPositivos porcentaje deseado de falsos positivos
     * @param n numero de elementos a almacenar
     */
    public BloomFilter(double porcentajeFalsosPositivos, int n) {
        this(Math.ceil(-(Math.log(porcentajeFalsosPositivos) / Math.log(2))) / Math.log(2), // c = k / ln(2)
                n,
                (int) Math.ceil(-(Math.log(porcentajeFalsosPositivos) / Math.log(2)))); // k = ceil(-log_2(false prob.))
    }
    public int tamañoMinimo(double porcentajeFalsosPositivos, int n) {
        int res=1;
        int k=2; //se elige el valor de k arbitrariamente, pues en la tarea se deja libre 
        BloomFilter<String> bloom;
        boolean ban =true;
        int i=1;
        
        while(ban){
            i++;
            bloom= new BloomFilter<>(i,n,k);
            ban= bloom.probabilidadDeFalsosPositivosEsperada()>=porcentajeFalsosPositivos;
        }
        return n*i; 
        
    }

    public void inserta(T element) {
        inserta(element.toString().getBytes(charset));
    }

    public void inserta(byte[] bytes) {
        int[] hashes = createHashes(bytes, k);
        for (int hash : hashes) {
            bitset.set(Math.abs(hash % size), true);
        }
        cant++;
    }

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
                System.out.println(Arrays.toString(digest));
            }

            for (int i = 0; i < digest.length / 4 && k < hashes; i++) {
                int h = 0;
                for (int j = (i * 4); j < (i * 4) + 4; j++) {
                    h <<= 8;
                    h |= ((int) digest[j]) & 0xFF;
                }
                result[k] = h;
                k++;
            }
        }
        return result;
    }

    public double probabilidadDeFalsosPositivosEsperada() {
        return Math.pow((1 - Math.exp(-k * (double) elementosEsperados / (double) size)), k);
    }

    public double probabilidadDeFalsosPositivos() {
        // (1 - e^(-k * n / m)) ^ k
        return Math.pow((1 - Math.exp(-k * (double) cant / (double) size)), k);
    }

    public boolean contains(T element) {
        return contains(element.toString().getBytes(charset));
    }

    public boolean contains(byte[] bytes) {
        int[] hashes = createHashes(bytes, k);
        for (int hash : hashes) {
            if (!bitset.get(Math.abs(hash % size))) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        
        int l = 0;
        boolean ban;
        Scanner sc = null;
        String[] lista = new String[10000];
        int[] coincidencias = new int[35];

        try {
            File ent = new File("/Users/elisaortizloyola/Desktop/BloomFilter/palabras.txt");
            sc = new Scanner(new FileReader(ent));

        } catch (FileNotFoundException e) {
            System.out.println("Input file not found");
            System.exit(1);
        }

        while (l < 10000) {
            String text = sc.nextLine().toLowerCase();
            ban = true;
            if ((text != null) && (!text.equals("")) && (text.matches("^[a-zA-Z]*$"))) {
                lista[l] = text;
                l++;
            }

        }
        //Prueba de la distribucion en un arreglo de 10000 palabras utilizando la funcion de hasheo standard de java 
        for (String pal : lista) {
            coincidencias[Math.abs(pal.hashCode() % 35)]++;

        }
        System.out.println(Arrays.toString(coincidencias));
        
        //prueba para ver que regresaba el getBytes
        System.out.println(Arrays.toString("gato".toString().getBytes(charset)));
        System.out.println(Arrays.toString(createHashes("gato".toString().getBytes(charset), 4)));
        
        
        //prueba para diseñar el algoritmo de TamañoMinimo
        BloomFilter<String> bloom= new BloomFilter<>(5,10,2);
        System.out.println(bloom.probabilidadDeFalsosPositivosEsperada());
        
        System.out.println("Prueba");
        for(int i=0;i<10;i++){
            bloom= new BloomFilter<>(i,10,2);
            System.out.println(bloom.probabilidadDeFalsosPositivosEsperada()+"   "+i);
        }
        BloomFilter<String> bloom2= new BloomFilter<>();
        System.out.println(bloom2.tamañoMinimo(.35, 10));
        System.out.println("-----------------------------------------------");
        //Obtencion de datos de como cambian los falsos positivos si cambia k
        for(int y=1;y<50;y++){
            bloom= new BloomFilter<>(5,10,y);
         System.out.println(bloom.probabilidadDeFalsosPositivosEsperada() );
        }
        
        
        



    }
}
