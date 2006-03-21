/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.installer;

import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Pack200;
import java.util.jar.Pack200.Packer;
import java.util.jar.Pack200.Unpacker;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

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

    private static File inputDir;

    private static File outputFile;

    private static int inputDirLength;

    private static StringBuilder sb = new StringBuilder();

    private static long totalOriginalSize;

    private static long totalPackedSize;
    
    public PackJars () {
    }
    
    public static void main(String[] args) {
        /*System.out.println("args.length:" + args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.println("args[" + i + "]: " + args[i]);
        }*/

        if (args.length < 2) {
            System.out.println("Error: 2 input parameters are required.");
            return;
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
        scan.scanDir(inputDir);
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
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void init () {
        packer = Pack200.newPacker();

        Map<String, String> properties = packer.properties();

        /*properties.put(Packer.SEGMENT_LIMIT, "-1");
        properties.put(Packer.EFFORT, "5");
        properties.put(Packer.KEEP_FILE_ORDER, Packer.TRUE);
        properties.put(Packer.DEFLATE_HINT, Packer.KEEP);
        properties.put(Packer.MODIFICATION_TIME, Packer.KEEP);
        properties.put(Packer.UNKNOWN_ATTRIBUTE, Packer.PASS);*/
    }

    /** Recursive method. */
    private void scanDir (File dir) {
        File [] arr = dir.listFiles();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].isDirectory()) {
                scanDir(arr[i]);
            } else {
                if (arr[i].getName().endsWith(".jar")) {
                    //System.out.println("arr[" + i + "]:" + arr[i]);
                    String s = arr[i].getAbsolutePath();
                    s = s.substring(inputDirLength + 1, s.length());
                    //Check if jar is signed
                    if (isJarSigned(arr[i])) {
                        System.out.println("Skipping signed jar: " + s);
                    } else {
                        System.out.println("Packing: " + s);
                        sb.append("  ");
                        sb.append("<jar name=\"" + s + "\"");
                        sb.append(" time=\"" + arr[i].lastModified() + "\"");
                        sb.append(" original-size=\"" + arr[i].length() + "\"");
                        totalOriginalSize += arr[i].length();
                        File f = packFile(arr[i]);
                        sb.append(" packed-size=\"" + f.length() + "\"");
                        totalPackedSize += f.length();
                        sb.append(" />\n");
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
        }
        return false;
    }
    
    private File packFile (File file) {
        File f = new File(file.getAbsolutePath() + ".pack.gz");
        try {
            JarFile jarFile = new JarFile(file);
            FileOutputStream outputStream = new FileOutputStream(f);

            packer.pack(jarFile, outputStream);

            jarFile.close();
            outputStream.close();

            file.deleteOnExit();
        } catch (IOException exc) {
            exc.printStackTrace();
        }
        return f;
    }
    
}
