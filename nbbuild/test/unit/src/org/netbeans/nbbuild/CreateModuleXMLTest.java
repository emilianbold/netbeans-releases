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
package org.netbeans.nbbuild;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import junit.framework.*;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.selectors.SelectorUtils;

import org.netbeans.junit.NbTestCase;

/** Check behaviour of ModuleSelector.
 *
 * @author Jaroslav Tulach
 */
public class CreateModuleXMLTest extends NbTestCase {
    
    public CreateModuleXMLTest(String testName) {
        super(testName);
    }

    public void testIncludesAllModulesByDefault() throws Exception {
        Manifest m = ModuleDependenciesTest.createManifest ();
        m.getMainAttributes().putValue("OpenIDE-Module", "org.my.module");
        File aModule = generateJar(new String[0], m);
        
        File output = new File(getWorkDir(), "output");
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"createmodulexml\" classname=\"org.netbeans.nbbuild.CreateModuleXML\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" + 
            "  <createmodulexml xmldir='" + output + "' >" +
            "    <hidden dir='" + aModule.getParent() + "' >" +
            "      <include name='" + aModule.getName() + "' />" +
            "    </hidden>" +
            "  </createmodulexml>" +
            "</target>" +
            "</project>"
        );
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { "-verbose" });
        
        assertTrue ("Output exists", output.exists ());
        assertTrue ("Output directory created", output.isDirectory());
        
        String[] files = output.list();
        assertEquals("It one file", 1, files.length);
        assertEquals("Its name reflects the code name of the module", "org-my-module.xml_hidden", files[0]);
        
    }
    
    
    private final File createNewJarFile () throws IOException {
        File dir = new File(this.getWorkDir(), "modules");
        dir.mkdirs();
        
        int i = 0;
        for (;;) {
            File f = new File (dir, i++ + ".jar");
            if (!f.exists ()) return f;
        }
    }
    
    protected final File generateJar (String[] content, Manifest manifest) throws IOException {
        File f = createNewJarFile ();
        
        JarOutputStream os;
        if (manifest != null) {
            os = new JarOutputStream (new FileOutputStream (f), manifest);
        } else {
            os = new JarOutputStream (new FileOutputStream (f));
        }
        
        for (int i = 0; i < content.length; i++) {
            os.putNextEntry(new JarEntry (content[i]));
            os.closeEntry();
        }
        os.closeEntry ();
        os.close();
        
        return f;
    }
    
}
