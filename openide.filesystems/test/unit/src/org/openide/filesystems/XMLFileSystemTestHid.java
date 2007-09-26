/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.openide.filesystems;

import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class XMLFileSystemTestHid extends TestBaseHid {

    private String[] resources = new String[] {"a/b/c"};
    XMLFileSystem xfs = null;

    public XMLFileSystemTestHid(String testName) {
        super(testName);
    }

    protected String[] getResources (String testName) {
        return resources;
    }


    public void testReset () throws Exception {        
        FileObject a = xfs.findResource("a");
        assertNotNull(a);
        

        FileChangeAdapter fcl = new FileChangeAdapter();
        a.addFileChangeListener(fcl);
        
        resources = new String[] {"a/b/c","a/b1/c"};        
        xfs.setXmlUrl(createXMLLayer().toURL());
        
        FileObject b1 = xfs.findResource("a/b1");
        assertNotNull(b1);                
        assertTrue(b1.isFolder());        
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        File f = createXMLLayer();
        xfs = new XMLFileSystem ();
        xfs.setXmlUrl(f.toURL());
        this.testedFS = xfs;
    }

    private File createXMLLayer() throws IOException {
        String testName = getName();
        File f = TestUtilHid.createXMLLayer(testName, getResources(testName));
        return f;
    }

    public void testChangesAreFiredOnSetXMLUrlsIssue59160() throws Exception {
        File f = writeFile ("layer1.xml", 
"<filesystem>\n" +
    "<folder name='TestModule'>\n" +
        "<file name='sample.txt' >Ahoj</file>\n" +
    "</folder>\n" +
"</filesystem>\n"
        );
        
        File f2 = writeFile ("layer2.xml", 
"<filesystem>\n" +
    "<folder name='TestModule'>\n" +
        "<file name='sample.txt' >Hello!</file>\n" +
    "</folder>\n" +
"</filesystem>\n"
        );

        
        
        
        xfs = new XMLFileSystem (f.toURL());
        
        FileObject fo = xfs.findResource ("TestModule/sample.txt");
        assertEquals ("Four bytes there", 4, fo.getSize ());
        registerDefaultListener (fo);
        
        xfs.setXmlUrl (f2.toURL ());
        
        assertEquals ("Six bytes there", 6, fo.getSize ());
        
        fileChangedAssert ("Change in the content", 1);
    }

    public void testChangesAreFiredOnSetXMLUrlsWithURLsIssue59160() throws Exception {
        File u1 = writeFile("u1.txt", "Ahoj");
        File u2 = writeFile("u2.txt", "Hello!");
        
        File f = writeFile ("layer1.xml", 
"<filesystem>\n" +
    "<folder name='TestModule'>\n" +
        "<file name='sample.txt' url='u1.txt' />\n" +
    "</folder>\n" +
"</filesystem>\n"
        );
        
        File f2 = writeFile ("layer2.xml", 
"<filesystem>\n" +
    "<folder name='TestModule'>\n" +
        "<file name='sample.txt' url='u2.txt' />\n" +
    "</folder>\n" +
"</filesystem>\n"
        );

        
        
        
        xfs = new XMLFileSystem (f.toURL());
        
        FileObject fo = xfs.findResource ("TestModule/sample.txt");
        assertEquals ("Four bytes there", 4, fo.getSize ());
        registerDefaultListener (fo);
        
        xfs.setXmlUrl (f2.toURL ());
        
        assertEquals ("Six bytes there", 6, fo.getSize ());
        
        fileChangedAssert ("Change in the content", 1);
    }
    
    public void testChangesAreFiredOnSetXMLUrlsWithAttributesIssue21204() throws Exception {
        File f = writeFile ("layer1.xml", 
"<filesystem>\n" +
    "<folder name='TestModule'>\n" +
        "<file name='sample.txt' >\n" +
        "  <attr name='value' stringvalue='old' />\n" +
        "</file>\n" +
    "</folder>\n" +
"</filesystem>\n"
        );
        
        File f2 = writeFile ("layer2.xml", 
"<filesystem>\n" +
    "<folder name='TestModule'>\n" +
        "<file name='sample.txt' >\n" +
        "  <attr name='value' stringvalue='new' />\n" +
        "</file>\n" +
    "</folder>\n" +
"</filesystem>\n"
        );

        
        
        
        xfs = new XMLFileSystem (f.toURL());
        
        FileObject fo = xfs.findResource ("TestModule/sample.txt");
        assertEquals("Old value is in the attribute", "old", fo.getAttribute("value"));
        registerDefaultListener (fo);
        
        xfs.setXmlUrl (f2.toURL ());

        assertEquals("New value is in the attribute", "new", fo.getAttribute("value"));
        fileAttributeChangedAssert("Change in the content", 1);
    }
    
    public void testChangesAreFiredOnSetXMLUrlsEvenWhenRemoved() throws Exception {
        File u1 = writeFile("u1.txt", "Ahoj");
        
        File f = writeFile("layer1.xml",
                "<filesystem>\n" +
                "<folder name='TestModule'>\n" +
                "<file name='sample.txt' url='u1.txt' />\n" +
                "</folder>\n" +
                "</filesystem>\n"
                );
        
        File f2 = writeFile("layer2.xml",
                "<filesystem>\n" +
                "</filesystem>\n"
                );
        
        
        
        
        xfs = new XMLFileSystem(f.toURL());
        
        FileObject fo = xfs.findResource("TestModule/sample.txt");
        assertEquals("Four bytes there", 4, fo.getSize());
        registerDefaultListener(fo);
        
        xfs.setXmlUrl(f2.toURL());
        
        assertFalse("Valid no more", fo.isValid());
        assertEquals("Empty now", 0, fo.getSize());
        
        fileDeletedAssert("Change in the content", 1);
    }
    
    public void testIssue62570() throws Exception {
        File f = writeFile("layer3.xml",
                "<filesystem>\n" +
                "<folder name='TestModule'>\n" +
                "<file name='sample.txt' >Ahoj</file>\n" +
                "<file name='sample2.txt' url='sample2.txt'/>\n" +
                "</folder>\n" +
                "</filesystem>\n"
                );
              
        File f2 = new File(f.getParentFile(), "sample2.txt");        
        if (!f2.exists()) {
            Thread.sleep(3000);
            assertTrue(f2.createNewFile());
        }
        xfs = new XMLFileSystem(f.toURL());
        FileObject fo = xfs.findResource ("TestModule/sample.txt");
        assertNotNull(fo);
        assertEquals(fo.lastModified().getTime(), f.lastModified());        
        
        FileObject fo2 = xfs.findResource ("TestModule/sample2.txt");
        assertNotNull(fo2);
        assertEquals(fo2.lastModified().getTime(), f2.lastModified());        
        assertFalse(fo2.lastModified().equals(fo.lastModified()));        
        
    }

    
    
    private File writeFile(String name, String content) throws IOException {
        File f = new File (getWorkDir (), name);
        java.io.FileWriter w = new java.io.FileWriter (f);
        w.write(content);
        w.close();
        return f;
    }
    
    
    public void testChangeOfAnAttributeInLayerIsFiredIfThereIsRealChange() throws Exception {
        XMLFileSystem fs = new XMLFileSystem();
        
        File f1 = changeOfAnAttributeInLayerIsFiredgenerateLayer("Folder", "java.awt.List");
        File f2 = changeOfAnAttributeInLayerIsFiredgenerateLayer("Folder", "java.awt.Button");
        File f3 = changeOfAnAttributeInLayerIsFiredgenerateLayer("NoChange", "nochange");

        fs.setXmlUrls (new URL[] { f1.toURL(), f3.toURL() } );
        
        FileObject file = fs.findResource("Folder/empty.xml");
        assertNotNull("File found in layer", file);
        
        FSListener l = new FSListener();
        file.addFileChangeListener(l);
        
        FileObject nochange = fs.findResource("NoChange/empty.xml");
        assertNotNull("File found in layer", nochange);
        FSListener no = new FSListener();
        nochange.addFileChangeListener(no);
        
        assertAttr("The first value is list", file, "value", "java.awt.List");
        assertAttr("Imutable value is nochange", nochange, "value", "nochange");
        
        fs.setXmlUrls (new URL[] { f2.toURL(), f3.toURL() } );
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
        public List<FileEvent> events = new ArrayList<FileEvent>();
        public List<FileEvent> change = new ArrayList<FileEvent>();
        
        
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
