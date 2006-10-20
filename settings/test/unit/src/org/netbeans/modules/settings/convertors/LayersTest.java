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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.settings.convertors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import junit.framework.TestCase;
import junit.framework.*;
import org.netbeans.*;
import org.netbeans.core.startup.ModuleSystem;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 *
 * @author radim
 */
public class LayersTest extends NbTestCase {
    
    public LayersTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(LayersTest.class);
        
        return suite;
    }
    
    public void testFastParsingOfXMLFiles() throws Exception {
        CharSequence chars = Log.enable(XMLSettingsSupport.class.getName(), Level.FINE);
        int len = 0;
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
        FileObject dir = sfs.getRoot();
        Enumeration/*<? extends FileObject>*/ en = dir.getChildren(true);
        while (en.hasMoreElements()) {
            FileObject fo = (FileObject)en.nextElement();
            if (fo.isFolder())
                continue;
            if (!"settings".equals(fo.getExt())) {
                continue;
            }
            // check only settings files woithout convertors
            String loc = fo.getURL().toExternalForm();
            Document doc = XMLUtil.parse(new InputSource(loc), false, true, null, EntityCatalog.getDefault());
            if (!"-//NetBeans//DTD Session settings 1.0//EN".equals(doc.getDoctype().getPublicId()))
                continue;
            
            log("checking "+fo.getPath());
            try {
                XMLSettingsSupport.SettingsRecognizer sr = new XMLSettingsSupport.SettingsRecognizer(true, fo);
                sr.parse();
            }
            catch (IOException ioe) {
                log("IOException was thrown: "+ioe.getMessage());
            }
            if (chars.length() > len) {
                log("quickParse fails");
                len = chars.length();
            }
        }
        if (chars.length() > 0) {
            fail("fast parsing of .settings files fails :"+chars.toString());
        }
    }
    
    /*
        
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        assertNotNull ("In the IDE mode, there always should be a classloader", l);
        
        List<URL> urls = new ArrayList<URL>();
        boolean atLeastOne = false;
        Enumeration<URL> en = l.getResources("META-INF/MANIFEST.MF");
        while (en.hasMoreElements ()) {
            URL u = en.nextElement();
            InputStream is = u.openStream();
            Manifest mf;
            try {
                mf = new Manifest(is);
            } finally {
                is.close();
            }
            String module = mf.getMainAttributes ().getValue ("OpenIDE-Module");
            if (module == null) continue;
            String layer = mf.getMainAttributes ().getValue ("OpenIDE-Module-Layer");
            if (layer == null) continue;
            
            atLeastOne = true;
            URL layerURL = new URL(u, "../" + layer);
            urls.add(layerURL);
        }
        
        File cacheDir;
        File workDir = getWorkDir();
        int i = 0;
        do {
            cacheDir = new File(workDir, "layercache"+i);
        } while (!cacheDir.mkdir());

        assertEquals("No errors or warnings during layer parsing: "+h.errors().toString(), 0, h.errors().size());
    }
     */

}
