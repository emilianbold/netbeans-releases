/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc.
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.templates;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Marek Fukala
 * @author Jaroslav Tulach
 */
public class ScriptingCreateFromTemplateTest extends NbTestCase {
    
    public ScriptingCreateFromTemplateTest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    @Override
    protected void setUp() throws Exception {
        MockLookup.setInstances(new SimpleLoader());
    }

    public void testCreateFromTemplateEncodingProperty() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo = FileUtil.createData(root, "simpleObject.txt");
        OutputStream os = fo.getOutputStream();
        os.write("${encoding}".getBytes());
        os.close();
        fo.setAttribute ("template", Boolean.TRUE);
        fo.setAttribute("javax.script.ScriptEngine", "freemarker");
        
        DataObject obj = DataObject.find(fo);
        DataFolder folder = DataFolder.findFolder(FileUtil.createFolder(root, "target"));
        
        Map<String,String> parameters = Collections.emptyMap();
        DataObject inst;
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader().getParent());
        try {
            inst = obj.createFromTemplate(folder, "complex", parameters);
        } finally {
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
        
        FileObject instFO = inst.getPrimaryFile();
        
        Charset targetEnc = FileEncodingQuery.getEncoding(instFO);
        assertNotNull("Template encoding is null", targetEnc);
        assertEquals("Encoding in template doesn't match", targetEnc.name(), instFO.asText());
    }
    
    //fix for this test was rolled back because of issue #120865
    public void XtestCreateFromTemplateDocumentCreated() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo = FileUtil.createData(root, "simpleObject.txt");
        OutputStream os = fo.getOutputStream();
        os.write("test".getBytes());
        os.close();
        fo.setAttribute ("template", Boolean.TRUE);
        fo.setAttribute("javax.script.ScriptEngine", "freemarker");

        MockServices.setServices(MockMimeLookup.class);
        MockMimeLookup.setInstances(MimePath.parse("content/unknown"), new TestEditorKit());
        
        DataObject obj = DataObject.find(fo);
        DataFolder folder = DataFolder.findFolder(FileUtil.createFolder(root, "target"));
        
        assertFalse(TestEditorKit.createDefaultDocumentCalled);
        DataObject inst = obj.createFromTemplate(folder, "test");
        assertTrue(TestEditorKit.createDefaultDocumentCalled);
        
        String exp = "test";
        assertEquals(exp, inst.getPrimaryFile().asText());
    }
    
    public static final class SimpleLoader extends MultiFileLoader {
        public SimpleLoader() {
            super(SimpleObject.class.getName());
        }
        protected String displayName() {
            return "SimpleLoader";
        }
        protected FileObject findPrimaryFile(FileObject fo) {
            if (fo.hasExt("prima")) {
                return fo;
            }
            return null;
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new SimpleObject(this, primaryFile);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FE(obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FileEntry(obj, secondaryFile);
        }
    }
    
    private static final class FE extends FileEntry {
        public FE(MultiDataObject mo, FileObject fo) {
            super(mo, fo);
        }

        @Override
        public FileObject createFromTemplate(FileObject f, String name) throws IOException {
            fail("I do not want to be called");
            return null;
        }
    }
    
    public static final class SimpleObject extends MultiDataObject {
        public SimpleObject(SimpleLoader l, FileObject fo) throws DataObjectExistsException {
            super(fo, l);
        }
        
        public String getName() {
            return getPrimaryFile().getNameExt();
        }
    }
    
    private static final class TestEditorKit extends DefaultEditorKit {
        
        static boolean createDefaultDocumentCalled;

        @Override
        public Document createDefaultDocument() {
            createDefaultDocumentCalled = true;
            return super.createDefaultDocument();
        }
        
    }

}
