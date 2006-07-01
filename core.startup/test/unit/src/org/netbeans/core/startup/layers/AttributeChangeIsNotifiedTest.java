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

package org.netbeans.core.startup.layers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.core.startup.layers.ModuleLayeredFileSystem;
import org.netbeans.core.startup.layers.SystemFileSystem;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 *
 * @author Jaroslav Tulach
 */
public class AttributeChangeIsNotifiedTest extends NbTestCase {
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
        File h = new File(getWorkDir(), "nb/installdir");
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
        File f1 = changeOfAnAttributeInLayerIsFiredgenerateLayer("Folder", "java.awt.List");
        File f2 = changeOfAnAttributeInLayerIsFiredgenerateLayer("Folder", "java.awt.Button");
        File f3 = changeOfAnAttributeInLayerIsFiredgenerateLayer("NoChange", "nochange");

        {
            ArrayList list = new ArrayList();
            list.add(f1.toURL());
            list.add(f3.toURL());
            fs.setURLs (list);
        }
        
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
        
        {
            ArrayList list = new ArrayList();
            list.add(f2.toURL());
            list.add(f3.toURL());
            fs.setURLs (list);
        }
        String v2 = (String) file.getAttribute("value");
        assertEquals("The second value is button", "java.awt.Button", v2);
        
        assertEquals("One change: " + l.events, 1, l.events.size());
        
        if (!(l.events.get(0) instanceof FileAttributeEvent)) {
            fail("Wrong event: " + l.events);
        }
        
        assertAttr("Imutable value is still nochange", nochange, "value", "nochange");
        assertEquals("No change in this attribute: "  + no.events, 0, no.events.size());
    }    
    
    private static void assertAttr(String msg, FileObject fo, String attr, String value) throws IOException {
        Object v = fo.getAttribute(attr);
        assertEquals(msg + "[" + fo + "]", value, v);
    }

    int cnt;
    private File changeOfAnAttributeInLayerIsFiredgenerateLayer(String folderName, String string) throws IOException {
        File f = new File(getWorkDir(), "layer" + (cnt++) + ".xml");
        FileWriter w = new FileWriter(f);
        w.write(
            "<filesystem>" +
            "<folder name='" + folderName + "'>" +
            "  <file name='empty.xml' >" +
            "    <attr name='value' stringvalue='" + string + "' />" +
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
