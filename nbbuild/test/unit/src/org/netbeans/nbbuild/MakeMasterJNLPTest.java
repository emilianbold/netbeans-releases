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

package org.netbeans.nbbuild;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.junit.*;


/** Is generation of Jnlp files correct?
 *
 * @author Jaroslav Tulach
 */
public class MakeMasterJNLPTest extends NbTestCase {
    public MakeMasterJNLPTest (String name) {
        super (name);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
    }
    
    
    public void testGenerateRefenrenceFilesOnce() throws Exception {
        doGenerateRefenrenceFiles(1);
    }
    public void testGenerateRefenrenceFilesThree() throws Exception {
        doGenerateRefenrenceFiles(3);
    }
    
    private void doGenerateRefenrenceFiles(int cnt) throws Exception {
        Manifest m;
        
        m = ModuleDependenciesTest.createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.my.module/3");
        File simpleJar = generateJar (new String[0], m);

        m = ModuleDependenciesTest.createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.second.module/3");
        File secondJar = generateJar (new String[0], m);
        
        File parent = simpleJar.getParentFile ();
        assertEquals("They are in the same folder", parent, secondJar.getParentFile());
        
        File output = new File(parent, "output");
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeMasterJNLP\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" + 
            "  <jnlp dir='" + output + "'  >" +
            "    <modules dir='" + parent + "' >" +
            "      <include name='" + simpleJar.getName() + "' />" +
            "      <include name='" + secondJar.getName() + "' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        while (cnt-- > 0) {
            PublicPackagesInProjectizedXMLTest.execute (f, new String[] { "-verbose" });
        }
        
        assertTrue ("Output exists", output.exists ());
        assertTrue ("Output directory created", output.isDirectory());
        
        String[] files = output.list();
        assertEquals("It has two files", 2, files.length);

        java.util.Arrays.sort(files);
        
        assertEquals("The res1 file: " + files[0], "org-my-module.ref", files[0]);
        assertEquals("The res2 file: "+ files[1], "org-second-module.ref", files[1]);
        
        File r1 = new File(output, "org-my-module.ref");
        String res1 = ModuleDependenciesTest.readFile (r1);

        File r2 = new File(output, "org-second-module.ref");
        String res2 = ModuleDependenciesTest.readFile (r2);
        
        assertExt(res1, "org.my.module");
        assertExt(res2, "org.second.module");
    }
    
    private static void assertExt(String res, String module) {
        int ext = res.indexOf("<extension");
        if (ext == -1) {
            fail ("<extension tag shall start there: " + res);
        }
        
        assertEquals("Just one extension tag", -1, res.indexOf("<extension", ext + 1));

        int cnb = res.indexOf(module);
        if (cnb == -1) {
            fail("Cnb has to be there: " + module + " but is " + res);
        }
        assertEquals("Just one cnb", -1, res.indexOf(module, cnb + 1));
        
        String dashcnb = module.replace('.', '-');
        
        int dcnb = res.indexOf(dashcnb);
        if (dcnb == -1) {
            fail("Dash Cnb has to be there: " + dashcnb + " but is " + res);
        }
        assertEquals("Just one dash cnb", -1, res.indexOf(dashcnb, dcnb + 1));
    }

    private final File createNewJarFile () throws IOException {
        int i = 0;
        for (;;) {
            File f = new File (this.getWorkDir(), i++ + ".jar");
            if (!f.exists ()) return f;
        }
    }
    
    protected final File generateJar (String[] content, Manifest manifest) throws IOException {
        File f = createNewJarFile ();
        
        JarOutputStream os = new JarOutputStream (new FileOutputStream (f), manifest);
        
        for (int i = 0; i < content.length; i++) {
            os.putNextEntry(new JarEntry (content[i]));
            os.closeEntry();
        }
        os.closeEntry ();
        os.close();
        
        return f;
    }

}
