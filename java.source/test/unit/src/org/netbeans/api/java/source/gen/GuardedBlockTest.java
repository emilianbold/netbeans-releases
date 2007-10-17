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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.guards.JavaGuardedSectionsFactory;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.GuardedSectionsFactory;
import org.netbeans.spi.editor.guards.GuardedSectionsProvider;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.SaveAsCapable;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableOpenSupport;
import static org.netbeans.api.java.source.JavaSource.Phase.*;

/**
 * Regression tests for guarded exceptions.
 * 
 * @author Pavel Flaska
 */
public class GuardedBlockTest extends GeneratorTestMDRCompat {

    public GuardedBlockTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(GuardedBlockTest.class);
//        suite.addTest(new GuardedBlockTest("testAddMethodAfterVariables"));
//        suite.addTest(new GuardedBlockTest(""));
        return suite;
    }
    
    /**
     * We need our own data loader to use guarded blocks.
     */
    @Override
    protected void setUp() throws Exception {
        XMLFileSystem system = new XMLFileSystem();
        assert GeneratorTestMDRCompat.class.getResource("/org/netbeans/modules/java/guards/layer.xml") != null;
        system.setXmlUrls(new URL[] {
            GeneratorTestMDRCompat.class.getResource("/org/netbeans/modules/java/source/resources/layer.xml"),
            GeneratorTestMDRCompat.class.getResource("/org/netbeans/modules/java/guards/layer.xml"),
            GeneratorTestMDRCompat.class.getResource("/org/netbeans/modules/java/editor/resources/layer.xml")
        });
        Repository repository = new Repository(system);
        ClassPathProvider cpp = new ClassPathProvider() {
            public ClassPath findClassPath(FileObject file, String type) {
                if (type.equals(ClassPath.SOURCE))
                    return ClassPathSupport.createClassPath(new FileObject[] {FileUtil.toFileObject(getDataDir())});
                    if (type.equals(ClassPath.COMPILE))
                        return ClassPathSupport.createClassPath(new FileObject[0]);
                    if (type.equals(ClassPath.BOOT))
                        return createClassPath(System.getProperty("sun.boot.class.path"));
                    return null;
            }
        };
//        MockServices.setServices(GuardedDataLoader.class);
        GuardedDataLoader loader = GuardedDataLoader.findObject(GuardedDataLoader.class, true);
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[] {repository, loader, cpp, new MimeDataProvider() {
            
            public Lookup getLookup(MimePath mimePath) {
                return Lookups.fixed(new JavaGuardedSectionsFactory(), new JavaKit());
            }
        }});
        JEditorPane.registerEditorKitForContentType("text/x-java", "org.netbeans.modules.editor.java.JavaKit");
        File cacheFolder = new File(getWorkDir(), "var/cache/index");
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
    }
    
    /**
     * #90424: Guarded Exception
     */
    public void testAddMethodAfterVariables() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication5;\n" +
            "\n" +
            "import java.awt.event.ActionEvent;\n" +
            "import java.awt.event.ActionListener;\n" +
            "\n" +
            "public class Guarded1 implements ActionListener {\n" +
            "    \n" +
            "    public Guarded1() {\n" +
            "    }\n" +
            "    \n" +
            "    // Variables declaration - do not modify//GEN-BEGIN:variables\n" +
            "    private javax.swing.JButton jButton1;\n" +
            "    // End of variablesdeclaration//GEN-END:variables\n" +
            "}\n"
        );
        DataObject dataObject = DataObject.find(FileUtil.toFileObject(testFile));
        EditorCookie editorCookie = ((GuardedDataObject) dataObject).getCookie(EditorCookie.class);
        System.err.println(editorCookie.openDocument());
        String golden = 
            "package javaapplication5;\n" +
            "\n" +
            "import java.awt.event.ActionEvent;\n" +
            "import java.awt.event.ActionListener;\n" +
            "\n" +
            "public class Guarded1 implements ActionListener {\n" +
            "    \n" +
            "    public Guarded1() {\n" +
            "    }\n" +
            "    \n" +
            "    // Variables declaration - do not modify//GEN-BEGIN:variables\n" +
            "    private javax.swing.JButton jButton1;\n" +
            "    // End of variables declaration//GEN-END:variables\n" +
            "\n" +
            "    public void actionPerformed(ActionEvent e) {\n" +
            "    }\n" +
            "}\n";
        
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree newMethod = make.Method(
                        make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)),
                        "actionPerformed",
                        make.PrimitiveType(TypeKind.VOID),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>singletonList(
                            make.Variable(
                                make.Modifiers(Collections.<Modifier>emptySet()),
                                "e", 
                                make.Identifier("ActionEvent"), 
                            null)
                        ),
                        Collections.<ExpressionTree>emptyList(),
                        make.Block(Collections.<StatementTree>emptyList(), false),
                        null // default value - not applicable
                ); 
                ClassTree copy = make.addClassMember(clazz, newMethod);
                workingCopy.rewrite(clazz, copy);
            }
        };
        src.runModificationTask(task).commit();
        editorCookie.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    @Override
    String getGoldenPckg() {
        return "";
    }

    @Override
    String getSourcePckg() {
        return "";
    }


// other top level classes used for getting guarded section initialized
public static class GuardedDataLoader extends MultiFileLoader {
    private final String JAVA_EXTENSION = "java";

    public GuardedDataLoader() {
        super("org.netbeans.api.java.source.gen.GuardedDataObject"); // NOI18N
    }

    @Override
    protected FileObject findPrimaryFile(FileObject fo) {
        if (fo.getExt().equals(JAVA_EXTENSION)) {
            return fo;
        }
        return null;
    }

    @Override
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        if (primaryFile.getExt().equals(JAVA_EXTENSION))
            return new GuardedDataObject(primaryFile, this);
        return null;
    }

    @Override
    protected Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
        return new FileEntry(obj, primaryFile);
    }

    @Override
    protected Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
        return null;
    }

} // GuardedDataLoader

public static class GuardedDataObject extends MultiDataObject {

    private MyGuardedEditorSupport fes = null;

    public GuardedDataObject(FileObject primaryFile, GuardedDataLoader loader) throws DataObjectExistsException {
        super(primaryFile, loader);
        getCookieSet().add(createMyGuardedEditorSupport());
        getCookieSet().assign(SaveAsCapable.class, new SaveAsCapable() {

            public void saveAs(FileObject folder, String fileName) throws IOException {
                createMyGuardedEditorSupport().saveAs(folder, fileName);
            }
        });
    }

    private synchronized MyGuardedEditorSupport createMyGuardedEditorSupport() {
        if (fes == null) {
            fes = new MyGuardedEditorSupport (this);
        }
        return fes;
    }

    private static class MyGuardedEditorSupport extends DataEditorSupport
        implements OpenCookie, EditCookie, EditorCookie, PrintCookie, EditorCookie.Observable
    {

        private static final class Environment extends DataEditorSupport.Env {

            private static final long serialVersionUID = -1;

            private transient SaveSupport saveCookie = null;

            private final class SaveSupport implements SaveCookie {

                public void save() throws IOException {
                    ((MyGuardedEditorSupport) findCloneableOpenSupport()).saveDocument();
                    getDataObject().setModified(false);
                }
            }

            public Environment(GuardedDataObject obj) {
                super(obj);
            }

            protected FileObject getFile() {
                return this.getDataObject().getPrimaryFile();
            }

            protected FileLock takeLock() throws java.io.IOException {
                return ((MultiDataObject)this.getDataObject()).getPrimaryEntry().takeLock();
            }

            public @Override CloneableOpenSupport findCloneableOpenSupport() {
                return (CloneableEditorSupport) ((GuardedDataObject)this.getDataObject()).getCookie(EditorCookie.class);
            }


            public void addSaveCookie() {
                GuardedDataObject javaData = (GuardedDataObject) this.getDataObject();
                if (javaData.getCookie(SaveCookie.class) == null) {
                    if (this.saveCookie == null)
                        this.saveCookie = new SaveSupport();
                    javaData.getCookieSet().add(this.saveCookie);
                    javaData.setModified(true);
                }
            }

            public void removeSaveCookie() {
                GuardedDataObject javaData = (GuardedDataObject) this.getDataObject();
                if (javaData.getCookie(SaveCookie.class) != null) {
                    javaData.getCookieSet().remove(this.saveCookie);
                    javaData.setModified(false);
                }
            }
        }

        public MyGuardedEditorSupport(GuardedDataObject dataObject) {
            super(dataObject, new Environment(dataObject));
            setMIMEType("text/x-java"); // NOI18N
        }

        @Override
        protected boolean notifyModified() {
            if (!super.notifyModified())
                return false;
            ((Environment) this.env).addSaveCookie();
            return true;
        }

        @Override
        protected void notifyUnmodified() {
            super.notifyUnmodified();
            ((Environment) this.env).removeSaveCookie();
        }

        @Override 
        protected CloneableEditor createCloneableEditor() {
            return new CloneableEditor(this);
        }

        @Override
        public boolean close(boolean ask) {
            return super.close(ask);
        }

        private final class FormGEditor implements GuardedEditorSupport {

            StyledDocument doc = null;

            public StyledDocument getDocument() {
                return FormGEditor.this.doc;
            }
        }

        private FormGEditor guardedEditor;
        private GuardedSectionsProvider guardedProvider;

        @Override
        protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
            if (guardedEditor == null) {
                guardedEditor = new FormGEditor();
                GuardedSectionsFactory gFactory = GuardedSectionsFactory.find("text/x-java");
                if (gFactory != null) {
                    guardedProvider = gFactory.create(guardedEditor);
                }
            }

            if (guardedProvider != null) {
                guardedEditor.doc = doc;
                Charset c = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
                Reader reader = guardedProvider.createGuardedReader(stream, c);
                try {
                    kit.read(reader, doc, 0);
                } finally {
                    reader.close();
                }
            } else {
                super.loadFromStreamToKit(doc, stream, kit);
            }
        }

        @Override
        protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
            if (guardedProvider != null) {
                Charset c = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
                Writer writer = guardedProvider.createGuardedWriter(stream, c);
                try {
                    kit.write(writer, doc, 0, doc.getLength());
                } finally {
                    writer.close();
                }
            } else {
                super.saveFromKitToStream(doc, kit, stream);
            }
        }
    }
} // GuardedDataObject

}
