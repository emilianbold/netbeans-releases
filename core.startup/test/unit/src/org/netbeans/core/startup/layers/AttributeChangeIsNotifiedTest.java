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

package org.netbeans.core.startup.layers;

import junit.framework.*;
import org.openide.util.WeakListeners;
import java.awt.Image;
import java.awt.Toolkit;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jaroslav Tulach
 */
public class AttributeChangeIsNotifiedTest extends org.netbeans.junit.NbTestCase {
    SystemFileSystem sfs;
    
    public AttributeChangeIsNotifiedTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        
        File u = new File(getWorkDir(), "userdir");
        File uc = new File(u, "config");
        uc.mkdirs();
        System.setProperty("netbeans.user", u.toString());
        File h = new File(getWorkDir(), "installdir");
        new File(h, "config").mkdirs();
        System.setProperty("netbeans.home", h.toString());
        
        
        sfs = (SystemFileSystem)Repository.getDefault().getDefaultFileSystem();
        
        File f = FileUtil.toFile(sfs.getRoot());
        
        assertEquals("Root is really on disk", uc, f);
        
    }

    protected void tearDown() throws Exception {
    }
    
    protected ModuleLayeredFileSystem getTheLayer(SystemFileSystem sfs) {
        return sfs.getUserLayer();
    }

    public void testChangeOfAnAttributeInLayerIsFired() throws Exception {
        doChangeOfAnAttributeInLayerIsFired(getTheLayer(sfs));
    }
    
    private void doChangeOfAnAttributeInLayerIsFired(ModuleLayeredFileSystem fs) throws Exception {
        File f1 = changeOfAnAttributeInLayerIsFiredgenerateLayer("java.awt.List");
        File f2 = changeOfAnAttributeInLayerIsFiredgenerateLayer("java.awt.Button");
        
        fs.setURLs (Collections.singletonList(f1.toURL()));
        
        FileObject file = sfs.findResource("Folder/empty.xml");
        assertNotNull("File found in layer", file);
        
        FSListener l = new FSListener();
        file.addFileChangeListener(l);
        
        FileObject nochange = sfs.findResource("NoChange/empty.xml");
        assertNotNull("File found in layer", nochange);
        FSListener no = new FSListener();
        nochange.addFileChangeListener(no);
        
        assertAttr("The first value is list", file, "value", "java.awt.List");
        assertAttr("Imutable value is nochange", nochange, "value", "nochange");
        
        fs.setURLs (Collections.singletonList(f2.toURL()));
        String v2 = (String) file.getAttribute("value");
        assertEquals("The second value is button", "java.awt.Button", v2);
        
        assertEquals("One change: " + l.events, 1, l.events.size());
        
        if (!(l.events.get(0) instanceof FileAttributeEvent)) {
            fail("Wrong event: " + l.events);
        }
        
        assertAttr("Imutable value is still nochange", nochange, "value", "nochange");
        // DOES NOT WORK RIGHT NOW 
        // WOULD FIX 67163
        // assertEquals("No change in this attribute: "  + no.events, 0, no.events.size());
    }    
    
    private static void assertAttr(String msg, FileObject fo, String attr, String value) throws IOException {
        Object v = fo.getAttribute(attr);
        assertEquals(msg + "[" + fo + "]", value, v);
    }

    int cnt;
    private File changeOfAnAttributeInLayerIsFiredgenerateLayer(String string) throws IOException {
        File f = new File(getWorkDir(), "layer" + (cnt++) + ".xml");
        FileWriter w = new FileWriter(f);
        w.write(
            "<filesystem>" +
            "<folder name=\"Folder\">" +
            "  <file name='empty.xml' >" +
            "    <attr name='value' stringvalue='" + string + "' />" +
            "  </file>" +
            "</folder>" +
            "<folder name=\"NoChange\">" +
            "  <file name='empty.xml' >" +
            "    <attr name='value' stringvalue='nochange' />" +
            "  </file>" +
            "</folder>" +
            "</filesystem>"
        );
        w.close();
        return f;
    }
    
    private static class FSListener extends FileChangeAdapter {
        public List events = new ArrayList();
        public List change = new ArrayList();
        
        
        public void fileRenamed(FileRenameEvent fe) {
            events.add(fe);
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            events.add(fe);
        }

        public void fileFolderCreated(FileEvent fe) {
            events.add(fe);
        }

        public void fileDeleted(FileEvent fe) {
            events.add(fe);
        }

        public void fileDataCreated(FileEvent fe) {
            events.add(fe);
        }

        public void fileChanged(FileEvent fe) {
            change.add(fe);
        }
        
    }
}
