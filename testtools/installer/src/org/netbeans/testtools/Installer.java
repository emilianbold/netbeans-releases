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

package org.netbeans.testtools;

/*
 * Installer.java
 *
 * Created on February 7, 2003, 9:46 AM
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Test Tools Installer performs extraction of Jemmy, JellyTools libraries and
 * XTest distribution from zip of NBMs into defined places for automated testing.
 * It is designed to be included in zipped bundle of test tools NBMs and declared
 * as a Main-Class in Manifest.
 * <p>
 * Destination NetBeans root directory ("nb_all") is taken from first command-line
 * argument or else from "nbroot".
 * <p>
 * Installer checks if destination is a part of CVS repository to avoid later
 * conflicts and may asks for confirmation. Property "ignoreCVS" set to true
 * ensures automatic confiramtion.
 * <p>
 * Usage can be: java -jar last_TTNBMs.zip /space/myrepository/nb_all
 * -DignoreCVS=true
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0
 */
public class Installer {
    
    private static boolean ignoreCVS = Boolean.getBoolean("ignoreCVS");

    private static String targetFolder = System.getProperty("nbroot", ".");
    
    private static final String jemmyJAR = "netbeans/modules/ext/jemmy.jar";
    private static final String jellyJAR = "netbeans/modules/ext/jelly-nb.jar";
    private static final String jelly2JAR = "netbeans/modules/ext/jelly2-nb.jar";
    private static final String xtestFolder = "netbeans/xtest-distribution/";
    
    private static final String jemmyTarget = "jemmy/builds/jemmy.jar";
    private static final String jellyTarget = "jellytools/builds/jelly-nb.jar";
    private static final String jelly2Target = "jellytools/builds/jelly2-nb.jar";
    private static final String xtestTarget = "xtest/";
    
    private static final ZipInputStream jemmyNBM = getStream("jemmy.nbm");
    private static final ZipInputStream jellyNBM = getStream("jellytools.nbm");
    private static final ZipInputStream xtestNBM = getStream("xtest.nbm");
    
    private static final byte buff[] = new byte[65536];
    
    private static void err(String message) {
        System.err.println("TestTools Installer error: "+message);
        if (jemmyNBM!=null) try {jemmyNBM.close();} catch (IOException ioe) {}
        if (jellyNBM!=null) try {jemmyNBM.close();} catch (IOException ioe) {}
        if (xtestNBM!=null) try {xtestNBM.close();} catch (IOException ioe) {}
        System.exit(-1);
    }
    
    private static ZipInputStream getStream(String fileName) {
        InputStream is=Installer.class.getClassLoader().getResourceAsStream(fileName);
        if (is==null) {
            is=Installer.class.getClassLoader().getResourceAsStream("nb/"+fileName);
            if (is==null) err("Missing "+fileName+" !");
        }
        return new ZipInputStream(is);
    }
    
    private static void testTarget(String target) {
        try {
            File test=new File(targetFolder, target).getCanonicalFile();
            if (test.isFile()) test=test.getParentFile();
            if (new File(test, "CVS").isDirectory()) {
                System.err.println("Folder "+test.getAbsolutePath()+" contains CVS information !");
                if (!ignoreCVS) {
                    System.err.println("Overriding files from CVS repository may cause collisions during next update !");
                    System.err.println("Do you want to continue (Y/n) ?");
                    if (Character.toUpperCase((char)System.in.read())=='Y') ignoreCVS=true;
                    else err("Installation interrupted !");
                }
            }
        } catch  (IOException ioe) {
            err(ioe.getMessage());
        }
    }
    
    private static void unzipFile(ZipInputStream in, String target) {
        FileOutputStream out=null;
        File file=new File(targetFolder, target);
        try {
            file=file.getCanonicalFile();
            createFolder(file.getParentFile());
            out=new FileOutputStream(file);
            int i;
            while ((i=in.read(buff))>0) {
                out.write(buff, 0, i);
            }
            System.out.println(file.getAbsolutePath());
        } catch (FileNotFoundException fnfe) {
            err("Error creating "+file.getAbsolutePath()+" "+fnfe.getMessage());
        } catch (IOException ioe) {
            err("IOException during extraction of "+file.getAbsolutePath()+" "+ioe.getMessage());
        } finally {
            if (out!=null)  try {out.close();} catch (IOException ioe) {}
        }
    }
    
    private static void createFolder(String folder) {
        try {
            createFolder(new File(targetFolder, folder).getCanonicalFile());
        } catch (IOException ioe) {
            err(ioe.getMessage());
        }
    }
    
    private static void createFolder(File dir) {
        if (!dir.exists()) {
            if (dir.mkdirs()) System.out.println(dir.getAbsolutePath());
            else err("Could not create directory "+dir.getAbsolutePath()+" !");
        } else if (!dir.isDirectory()) err(dir.getAbsolutePath()+" is not a directory !");
    }
    
    /** performs installation
     * @param args NetBeans root directory (nb_all) - optional
     */
    public static void main(String[] args) {
        if (args.length>0) {
            targetFolder=args[0];
        } else {
            System.out.println("NetBeans root directory (\"nb_all\") is not defined as command-line argument!");
            System.out.println();
            System.out.println("This installer is designed to extract fresh libraries and XTest harness into test repository for usage in automated process.");
            System.out.println("Usage: java -jar <zip file> <root directory>");
            System.out.println("Example: java -jar testtools_nbms.zip C:\\space\\test-repository\\nb_all");
            System.out.println("To install modules into running IDE for test developmnet purpose use Update Center wizard on extracted NBMs.");
            System.exit(-1);
        }
        testTarget(jemmyTarget);
        testTarget(jellyTarget);
        testTarget(jelly2Target);
        testTarget(xtestTarget);
        try {
            ZipEntry entry=jemmyNBM.getNextEntry();
            while (entry!=null && !entry.getName().equals(jemmyJAR)) entry=jemmyNBM.getNextEntry();
            if (entry==null) err("Missing "+jemmyJAR+" in jemmy.nbm !");
            unzipFile(jemmyNBM, jemmyTarget);
            entry=jellyNBM.getNextEntry();
            while (entry!=null && !entry.getName().equals(jellyJAR) && !entry.getName().equals(jelly2JAR)) entry=jellyNBM.getNextEntry();
            if (entry==null) err("Missing "+jellyJAR+" and "+jelly2JAR+" in jellytools.nbm !");
            String next;
            if (entry.getName().equals(jellyJAR)) {
                next=jelly2JAR;
                unzipFile(jellyNBM, jellyTarget);
            } else {
                next=jellyJAR;
                unzipFile(jellyNBM, jelly2Target);
            }
            while (entry!=null && !entry.getName().equals(next)) entry=jellyNBM.getNextEntry();
            if (entry==null) err("Missing "+next+" in jellytools.nbm !");
            if (next.equals(jellyJAR)) {
                unzipFile(jellyNBM, jellyTarget);
            } else {
                unzipFile(jellyNBM, jelly2Target);
            }
            boolean found=false;
            while ((entry=xtestNBM.getNextEntry())!=null) {
                if (entry.getName().startsWith(xtestFolder)) {
                    found=true;
                    if (entry.isDirectory()) {
                        createFolder(xtestTarget+entry.getName().substring(xtestFolder.length()));
                    } else {
                        unzipFile(xtestNBM, xtestTarget+entry.getName().substring(xtestFolder.length()));
                    }
                }
            }
            if (!found) err("Missing "+xtestFolder+" in xtest.nbm !");
            jemmyNBM.close();
            jellyNBM.close();
            xtestNBM.close();
            System.out.println("Finished.");
//            System.out.println("Warning: Several files were also installed into parent directory structure ("+new File(targetFolder, "../nbextra").getCanonicalPath()+").");
        } catch (IOException ioe) {
            err(ioe.getMessage());
        }
    }
    
}
