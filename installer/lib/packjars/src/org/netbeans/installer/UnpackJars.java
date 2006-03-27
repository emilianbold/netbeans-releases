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

import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * This class is used to unpack jars using Pack200.Unpacker from JDK 1.5 API.
 * All packed jars with extension ".pack.gz" are unpacked and suffix is removed.
 * Original packed jars are deleted. This class is used to verify packed jars
 * during installer build.
 *
 * @author Marek Slama
 *
 */

public class UnpackJars {
    
    private Pack200.Unpacker unpacker = null;

    private static File inputDir;

    private static int inputDirLength;

    public UnpackJars () {
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Error: 1 input parameter is required.");
            System.exit(1);
        }

        inputDir = new File(args[0]);

        inputDirLength = inputDir.getAbsolutePath().length();

        UnpackJars scan = new UnpackJars();
        scan.init();
        scan.scanDir(inputDir);
    }
    
    private void init () {
        unpacker = Pack200.newUnpacker();
    }
    
    private void scanDir (File dir) {
        File [] arr = dir.listFiles();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].isDirectory()) {
                scanDir(arr[i]);
            } else {
                if (arr[i].getName().endsWith(".pack.gz")) {
                    String s = arr[i].getAbsolutePath();
                    s = s.substring(inputDirLength + 1, s.length());
                    System.out.println("Unpacking: " + s);
                    unpackFile(arr[i]);
                }
            }
        }
    }
    
    private void unpackFile (File file) {
        try {
            File outFile = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - ".pack.gz".length()));
            JarOutputStream os = new JarOutputStream(new FileOutputStream(outFile));
            unpacker.unpack(file, os);
            os.close();
            file.deleteOnExit();
        } catch (IOException exc) {
            exc.printStackTrace();
            System.exit(1);
        }
    }
    
}
