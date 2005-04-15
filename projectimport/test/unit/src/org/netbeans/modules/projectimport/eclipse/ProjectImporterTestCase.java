/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.projectimport.eclipse;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.junit.NbTestCase;

/**
 * Provides basic functionality for ProjectImporter tests.
 *
 * @author mkrauskopf
 */
class ProjectImporterTestCase extends NbTestCase {
    
    private static final int BUFFER = 2048;
    
    /*
     * If true a lot of information about parsed project will be written to a
     * console.
     */
    private static boolean verbose;
    
    private File jars;
    private File workDir;
    
    /** Creates a new instance of ProjectImporterTestCase */
    public ProjectImporterTestCase(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        /* comment this out to see verbose info */
        // setVerbose(true);
        clearWorkDir();
        workDir = getWorkDir();
        jars = new File(ProjectImporterTestCase.class.getResource("jars").getFile());
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        clearWorkDir();
    }
    
    protected void setVerbose(boolean verbose) {
        ProjectImporterTestCase.verbose = verbose;
    }
    
    /*
     * XXX - doesn't similar method already exist somewhere in the API?
     * XXX - If not replace with JarFileSystem as hinted by Radek :)
     */
    protected File extractJARToWorkDir(String jarFile) throws IOException {
        ZipInputStream zis = null;
        BufferedOutputStream dest = null;
        try {
            FileInputStream fis = new FileInputStream(new File(jars, jarFile));
            zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null) {
                byte data[] = new byte[BUFFER];
                File entryFile = new File(workDir, entry.getName());
                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    FileOutputStream fos = new FileOutputStream(entryFile);
                    dest = new BufferedOutputStream(fos, BUFFER);
                    int count;
                    while ((count = zis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                }
            }
        } finally {
            if (zis != null) { zis.close(); }
            if (dest != null) { dest.close(); }
        }
        // return the directory (without ".jar" - convention used here)
        return new File(workDir, jarFile.substring(0, jarFile.length() - 4));
    }
    
    protected static void printMessage(String message, boolean newLine) {
        if (verbose) {
            if (newLine) {
                System.out.println(message);
            } else {
                System.out.print(message);
            }
        }
    }
    
    protected static void printMessage(String message) {
        printMessage(message, true);
    }
    
    protected static void printCollection(String name, Collection col) {
        if (col != null && !col.isEmpty()) {
            printMessage("  " + name + ":");
            for (Iterator it = col.iterator(); it.hasNext(); ) {
                ClassPathEntry entry = (ClassPathEntry) it.next();
                printMessage("    \"" + entry.getRawPath() + "\" ", false);
                if (entry.getAbsolutePath() != null) {
                    printMessage("converted to \"" + entry.getAbsolutePath() + "\"");
                } else {
                    printMessage("cannot be resolved !!!!");
                }
            }
        }
    }
}
