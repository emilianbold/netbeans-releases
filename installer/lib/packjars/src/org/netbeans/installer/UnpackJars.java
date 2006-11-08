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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
                if (arr[i].getName().endsWith(".pack")) {
                    String s = arr[i].getAbsolutePath();
                    s = s.substring(inputDirLength + 1, s.length());
                    System.out.println("Unpacking: " + s);
                    unpackFile(arr[i]);
                    arr[i].deleteOnExit();
                }
            }
        }
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
    
}
