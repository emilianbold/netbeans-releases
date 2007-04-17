/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.installer;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Pack200;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200.Packer;

/**
 *
 * This class is used to compress jars using pack200 from JDK 1.5 API.
 * All unsigned jars are packed and renamed to .pack.gz. Original unpacked
 * jars are deleted.
 *
 * @author Marek Slama
 *
 */

public class PackJars {
    
    private Pack200.Packer packer = null;
    
    private Pack200.Unpacker unpacker = null;

    private static File inputDir;

    private static File outputFile;

    private static int inputDirLength;

    private static StringBuilder sb = new StringBuilder();

    private static long totalOriginalSize;

    private static long totalPackedSize;
    
    public PackJars () {
    }
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Error: 2 input parameters are required.");
            System.exit(1);
        }

        inputDir = new File(args[0]);

        inputDirLength = inputDir.getAbsolutePath().length();

        PackJars scan = new PackJars();

        outputFile = new File(args[1]);

        File parent = outputFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

        scan.init();
        totalOriginalSize = 0L;
        totalPackedSize = 0L;
        //long startTime = System.currentTimeMillis();
        scan.scanDir(inputDir);
        //long endTime = System.currentTimeMillis();
        //System.out.println("Time: " + (endTime - startTime) + "ms");
        sb.append("</catalog>\n");
        //Insert at beginning
        sb.insert(0,"  <summary total-original-size=\"" + totalOriginalSize
        + "\" total-packed-size=\"" + totalPackedSize + "\" />\n");
        sb.insert(0,"<catalog>\n");
        Writer w = null;
        try {
            w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));
            w.write(sb.toString());
            w.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            System.exit(1);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
    
    private void init () {
        packer = Pack200.newPacker();

        Map<String, String> properties = packer.properties();
        properties.put(Packer.SEGMENT_LIMIT, "-1");
        properties.put(Packer.EFFORT, "5");
        /*properties.put(Packer.KEEP_FILE_ORDER, Packer.FALSE);
        properties.put(Packer.DEFLATE_HINT, Packer.KEEP);
        properties.put(Packer.MODIFICATION_TIME, Packer.KEEP);
        properties.put(Packer.UNKNOWN_ATTRIBUTE, Packer.PASS);*/
        
        unpacker = Pack200.newUnpacker();
    }

    /** Recursive method. */
    private void scanDir (File dir) {
        File [] arr = dir.listFiles();
        boolean useExt = false;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].isDirectory()) {
                scanDir(arr[i]);
            } else {
                if (arr[i].getName().endsWith(".jar")) {
                    long lastModified = arr[i].lastModified();
                    String s = arr[i].getAbsolutePath();
                    s = s.substring(inputDirLength + 1, s.length());
                    //Check if jar is signed
                    if (isJarSigned(arr[i])) {
                        System.out.println("Skipping signed jar: " + s);
                    } else {
                        System.out.println("Packing: " + s);
                        sb.append("  ");
                        sb.append("<jar name=\"" + s + "\"");
                        sb.append(" time=\"" + lastModified + "\"");
                        sb.append(" original-size=\"" + arr[i].length() + "\"");
                        totalOriginalSize += arr[i].length();
                        File f;
                        String md5;
                        if (useExt) {
                            f = packFileExt(arr[i]);
                            unpackFileExt(f);
                            md5 = generateKey(arr[i]);
                            f = packFileExt(arr[i]);
                        } else {
                            f = packFile(arr[i]);
                            unpackFile(f);
                            md5 = generateKey(arr[i]);
                            f = packFile(arr[i]);
                        }
                        sb.append(" packed-size=\"" + f.length() + "\"");
                        sb.append(" md5=\"" + md5 + "\"");
                        totalPackedSize += f.length();
                        sb.append(" />\n");
                        arr[i].delete();
                    }
                }
            }
        }
    }
   
    /** Detects if jar is signed or not. Signed jar will not be packed as it could break signature. */ 
    private boolean isJarSigned (File file) {
        try {
            JarFile jarFile = new JarFile(file);
            for (Enumeration en = jarFile.entries(); en.hasMoreElements(); ) {
                JarEntry je = (JarEntry) en.nextElement();
                //System.out.println("je.name: " + je.getName());
                String name = je.getName().toUpperCase(Locale.ENGLISH);
                if (name.startsWith("META-INF") && (name.endsWith(".DSA") || name.endsWith(".RSA") || name.endsWith(".SF"))) {
                    //System.out.println(file + " is probably signed.");
                    return true;
                }
            }
        } catch (IOException exc) {
            exc.printStackTrace();
            System.exit(1);
        }
        return false;
    }
    
    private File packFileExt (File file) {
        RunCommand r = new RunCommand();
        String javaHome = System.getProperty("java.home");
        String [] cmd = new String[6];
        cmd[0] = javaHome + "/bin/pack200";
        cmd[1] = "--segment-limit=-1";
        cmd[2] = "--effort=5";
        cmd[3] = "--no-gzip";
        cmd[4] = file.getAbsolutePath() + ".pack";
        cmd[5] = file.getAbsolutePath();
        r.execute(cmd);
        r.waitFor();
        int ret = r.getReturnStatus();
        if (ret != 0) {
            System.exit(ret);
        }
        return new File(file.getAbsolutePath() + ".pack");
    }
    
    private void unpackFileExt (File file) {
        RunCommand r = new RunCommand();
        File outFile = 
        new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - ".pack".length()));
        String javaHome = System.getProperty("java.home");
        String [] cmd = new String[3];
        cmd[0] = javaHome + "/bin/unpack200";
        cmd[1] = file.getAbsolutePath();
        cmd[2] = outFile.getAbsolutePath();
        r.execute(cmd);
        r.waitFor();
        int ret = r.getReturnStatus();
        if (ret != 0) {
            System.exit(ret);
        }
    }
    
    private void repackFileExt (File file) {
        RunCommand r = new RunCommand();
        String javaHome = System.getProperty("java.home");
        String [] cmd = new String[4];
        cmd[0] = javaHome + "/bin/pack200";
        cmd[1] = "--segment-limit=-1";
        cmd[2] = "--repack";
        cmd[3] = file.getAbsolutePath();
        r.execute(cmd);
        r.waitFor();
        int ret = r.getReturnStatus();
        if (ret != 0) {
            System.exit(ret);
        }
    }
    
    private File packFile (File file) {
        File f = new File(file.getAbsolutePath() + ".pack");
        try {
            JarFile jarFile = new JarFile(file);
            FileOutputStream outputStream = new FileOutputStream(f);

            packer.pack(jarFile, outputStream);

            jarFile.close();
            outputStream.close();
        } catch (IOException exc) {
            exc.printStackTrace();
            System.exit(1);
        }
        return f;
    }
    
    private void unpackFile (File file) {
        try {
            File outFile = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - ".pack".length()));
            JarOutputStream os = new JarOutputStream(new FileOutputStream(outFile));
            unpacker.unpack(file, os);
            os.close();
        } catch (IOException exc) {
            exc.printStackTrace();
            System.exit(1);
        }
    }
    
    /** 
     * Generate 32 byte long fingerprint of input file in string readable form
     * the same as produced by md5sum.
     */
    private String generateKey (File file) {
        String key = null;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5"); // NOI18N
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        ByteBuffer buff = null;
        try {
            buff = is.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        md.update(buff);
        byte [] md5sum = md.digest();
        
        try {
            is.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        
        StringBuffer keyBuff = new StringBuffer(32);
        //Convert byte array to hexadecimal string to be used as key
        for (int i = 0; i < md5sum.length; i++) {
            int val = md5sum[i];
            if (val < 0) {
                val = val + 256;
            }
            String s = Integer.toHexString(val);
            if (s.length() == 1) {
                keyBuff.append("0"); // NOI18N
            }
            keyBuff.append(Integer.toHexString(val));
        }
        key = keyBuff.toString();
        return key;
    }
    
}
