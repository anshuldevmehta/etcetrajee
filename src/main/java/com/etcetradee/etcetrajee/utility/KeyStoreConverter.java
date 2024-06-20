package com.etcetradee.etcetrajee.utility;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class KeyStoreConverter
{

        /**
         * Converts a Java KeyStore (JKS) to PEM format.
         *
         * @param jksFilePath Path to the JKS file.
         * @param pemFilePath Path where the converted PEM file should be saved.
         * @throws IOException If an I/O error occurs.
         */
        public static void jksToPem(String jksFilePath, String pemFilePath) throws IOException, KeyStoreException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException {
            // Load the JKS keystore
            KeyStore jks = loadJks(jksFilePath);

            // Convert the JKS keystore to a byte array
            byte[] jksBytes = toByteArray(jks);

            // Save the byte array as a PEM file
            Files.write(Paths.get(pemFilePath), jksBytes);
        }

        /**
         * Loads a JKS keystore from a file.
         *
         * @param jksFilePath Path to the JKS file.
         * @return Loaded JKS keystore.
         * @throws IOException If an I/O error occurs.
         * @throws KeyStoreException If the keystore cannot be loaded.
         * @throws NoSuchAlgorithmException If the algorithm cannot be found.
         * @throws CertificateException If the certificate cannot be found.
         * @throws UnrecoverableKeyException If the key cannot be recovered.
         */
        public static KeyStore loadJks(String jksFilePath) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
            KeyStore jks = KeyStore.getInstance("JKS");
            try (FileInputStream fis = new FileInputStream(jksFilePath)) {
                jks.load(fis, "yourKeystorePassword".toCharArray());
            }
            return jks;
        }

        /**
         * Converts a KeyStore to a byte array.
         *
         * @param keyStore The KeyStore to convert.
         * @return Byte array representation of the KeyStore.
         * @throws IOException If an I/O error occurs.
         * @throws KeyStoreException If the keystore cannot be converted.
         */
        public static byte[] toByteArray(KeyStore keyStore) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            keyStore.store(baos, "yourKeystorePassword".toCharArray());
            return baos.toByteArray();
        }

}
