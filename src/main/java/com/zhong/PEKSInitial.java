package com.zhong;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Random;


public class PEKSInitial {
    private BigInteger primeP;// prime p
    private BigInteger primeQ;// prime q
    private BigInteger primeM;// prime m
    private BigInteger generator;// 生成元G
    private BigInteger fainM;// Bluer function of prime m

    PEKSInitial() {
        primeP = BigInteger.probablePrime(512, new Random());// generate a 512
        // bit prime
        primeQ = BigInteger.probablePrime(512, new Random());// generate a 512
        // bit prime
        generator = new BigInteger(512, new Random());// a 512 bit number, may
        // not prime
        primeM = primeP.multiply(primeQ);// compute m=p*q
        fainM = (primeP.subtract(BigInteger.ONE)).multiply(primeQ.subtract(BigInteger.ONE));// eluer function of m
    }

    public static void main(String[] args) {
        PEKSInitial initial = new PEKSInitial();
        try {
            SecretKey key = KeyGenerator.getInstance("DES").generateKey();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("initial.data"));
            oos.writeObject(key);
            oos.writeObject(initial.getModules());
            oos.writeObject(initial.getGenerator());
            oos.writeObject(initial.getEulerFunction());
            oos.close();

            ObjectOutputStream oos2 = new ObjectOutputStream(new FileOutputStream("server.data"));
            oos2.writeObject(key);
            oos2.writeObject(initial.getModules());
            oos2.writeObject(initial.getGenerator());
            //oos1.writeObject();
            oos2.close();

            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("initial.data"));
            for (int i = 0; i < 4; i++) {
                System.out.println(ois.readObject().toString());
            }
            ObjectInputStream oos3 = new ObjectInputStream(new FileInputStream("server.data"));
            for (int i = 0; i < 3; i++) {
                System.out.println(oos3.readObject().toString());

            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException eio) {
            eio.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public BigInteger getGenerator() {
        return this.generator;
    }

    public BigInteger getModules() {
        return this.primeM;
    }

    public BigInteger getEulerFunction() {
        return this.fainM;
    }
}
