/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.awt.Dialog;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.templates.FileBuilder;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.ReformatTask;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Enumerations;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Jaroslav Tulach
 */
public class IndentEngineIntTest extends NbTestCase {
    
    public IndentEngineIntTest(String testName) {
        super(testName);
    }
    
    protected boolean runInEQ() {
        return true;
    }
    

    @SuppressWarnings("deprecation")
    protected void setUp() throws Exception {
        MockLookup.setLookup(Lookups.fixed(new DD(), new Pool()), Lookups.metaInfServices(getClass().getClassLoader()));
        MockMimeLookup.setInstances(MimePath.get("text/jarda"), new IEImpl2());
        FileUtil.setMIMEType("txt", "text/jarda");
        clearWorkDir();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    /**
     * Checks that the templating works without IndentEngine available
     * @throws Exception 
     */
    public void testWithoutEditorIndent() throws Exception {
        MockLookup.setLookup(Lookups.fixed(new DD(), new Pool()), 
                Lookups.exclude(Lookups.metaInfServices(getClass().getClassLoader()),
                        Class.forName("org.netbeans.modules.editor.indent.IndentScriptEngineHack$Factory")
                        ));
        FileObject dataRoot = FileUtil.toFileObject(getDataDir());
        FileObject template = dataRoot.getFileObject("templates/ClassWithoutReplacements.java");
        template.setAttribute("template", Boolean.TRUE);
        FileObject workRoot = FileUtil.toFileObject(getWorkDir());
        FileObject result = FileBuilder.createFromTemplate(template, workRoot, "NoReplacements", null, FileBuilder.Mode.FORMAT);
        FileObject pass = dataRoot.getFileObject("golden/ClassWithoutReplacements.java");
        assertFile(FileUtil.toFile(result), FileUtil.toFile(pass));
    }

    public void testCreateFromTemplateUsingFreemarker() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo = FileUtil.createData(root, "simpleObject.txt");
        OutputStream os = fo.getOutputStream();
        String txt = "print('<html><h1>'); print(title); print('</h1></html>');";
        os.write(txt.getBytes());
        os.close();
        fo.setAttribute(ScriptingCreateFromTemplateHandler.SCRIPT_ENGINE_ATTR, "JavaScript");
        
        
        DataObject obj = DataObject.find(fo);
        
        DataFolder folder = DataFolder.findFolder(FileUtil.createFolder(root, "target"));
        
        Map<String,String> parameters = Collections.singletonMap("title", "Nazdar");
        DataObject n = obj.createFromTemplate(folder, "complex", parameters);
        
        assertEquals("Created in right place", folder, n.getFolder());
        assertEquals("Created with right name", "complex.txt", n.getName());
        
        String exp = ">lmth/<>1h/<radzaN>1h<>lmth<";
        assertEquals(exp, stripNewLines(readFile(n.getPrimaryFile())));
        
    }
    
    static String stripNewLines(String str) {
        return str.replace("\n", "").replace("\r", "");
    }
    
    private static String readFile(FileObject fo) throws IOException {
        return fo.asText();
    }
    
    public static final class DD extends DialogDisplayer {
        public Object notify(NotifyDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Dialog createDialog(final DialogDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    public static final class Pool extends DataLoaderPool {
        protected Enumeration<DataLoader> loaders() {
            return Enumerations.<DataLoader>singleton(SimpleLoader.getLoader(SimpleLoader.class));
        }
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
    
    public static final class IEImpl2 implements ReformatTask, ReformatTask.Factory {
        private Context context;

        public IEImpl2(Context context) {
            this.context = context;
        }

        public IEImpl2() {
        }

        @Override
        public void reformat() throws BadLocationException {
            int from = context.startOffset();
            int to = context.endOffset();
            int len = to - from;
            String s = context.document().getText(from, len);
            StringBuilder sb = new StringBuilder(s.length());
            for (int i = s.length() - 1; i >= 0; i--) {
                sb.append(s.charAt(i));
            }
            context.document().insertString(from, sb.toString(), null);
            context.document().remove(from + len, len);
        }

        @Override
        public ExtraLock reformatLock() {
            return null;
        }

        @Override
        public ReformatTask createTask(Context context) {
            return new IEImpl2(context);
        }
    }

}
