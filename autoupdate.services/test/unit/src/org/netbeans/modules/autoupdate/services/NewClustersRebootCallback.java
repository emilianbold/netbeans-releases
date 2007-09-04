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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.autoupdate.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Radek Matous
 */
public class NewClustersRebootCallback {
    public static final String NAME_OF_NEW_CLUSTER = "newcluster";//NOI18N
    public static void main(String[] args) throws IOException {
//        System.out.println("NewClustersRebootCallback executed with params: "+Arrays.toString(args));
        String nbdirs = System.getProperty("netbeans.dirs");        
//        System.out.println("NewClustersRebootCallback netbeans.dirs: "+nbdirs);
        File udir = new File(System.getProperty("netbeans.user"));
        File newCluster = new File(udir, NAME_OF_NEW_CLUSTER);
        if (!newCluster.exists()) {
            createFileWithNewClustersForNbexec(newCluster, nbdirs, udir);
            createAtLeastOneNbm(newCluster, udir);
        } 
    }
    
    public static void copy(InputStream is, OutputStream os)
    throws IOException {
        final byte[] BUFFER = new byte[4096];
        int len;

        for (;;) {
            len = is.read(BUFFER);

            if (len == -1) {
                return;
            }

            os.write(BUFFER, 0, len);
        }
    }
    
    private static void createAtLeastOneNbm(File newCluster, File udir) throws FileNotFoundException, FileNotFoundException, IOException, IOException {
        File nbm = new File(newCluster, "update"+File.separatorChar+"download"+File.separatorChar+"whatever.nbm");
        nbm.getParentFile().mkdirs();
        nbm.createNewFile();
        InputStream is2 = new FileInputStream(new File(udir, "nbmfortest"));
        OutputStream os2 = new FileOutputStream(nbm);
        copy(is2, os2);
        os2.close();
    }

    private static void createFileWithNewClustersForNbexec(File newCluster, String nbdirs, File udir) throws FileNotFoundException, IOException {
        newCluster.mkdirs();
        nbdirs = nbdirs + File.pathSeparatorChar + newCluster.getAbsolutePath();
        File fileWithClustersForShell = new File(udir, "update"+File.separatorChar+"download"+File.separatorChar+"netbeans.dirs");
        fileWithClustersForShell.getParentFile().mkdirs();
        fileWithClustersForShell.createNewFile();
        OutputStream os = new FileOutputStream(fileWithClustersForShell);
        os.write(nbdirs.getBytes());
        os.close();
    }
}
