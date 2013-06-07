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
 * Portions Copyrighted 2007-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javadoc.hints;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.File;
import java.util.Enumeration;
import java.util.List;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.save.Reindenter;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.util.Enumerations;

/**
 *
 * @author Jan Pokorsky
 */
public abstract class JavadocTestSupport extends NbTestCase {
    
    private static File cache;
    private static FileObject cacheFO;
    protected StyledDocument doc;
    protected CompilationInfo info;

    public JavadocTestSupport(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
//        MockMimeLookup.setInstances(MimePath.parse("text/x-java"), new JavaKit());
        SourceUtilsTestUtil.prepareTest(new String[] {
            "org/netbeans/modules/java/editor/resources/layer.xml",
            "META-INF/generated-layer.xml"
        },
        new Object[] {
            new Pool(),
            new MockMimeLookup(),
        });
        MockMimeLookup.setInstances(MimePath.parse("text/x-java"), new Reindenter.Factory());
        FileUtil.setMIMEType("java", "text/x-java");
        
        if (cache == null) {
            cache = getWorkDir();
            cacheFO = FileUtil.toFileObject(cache);
            
            cache.deleteOnExit();
        }
    }
    
    protected void prepareTest(String code) throws Exception {
        clearWorkDir();
        
        FileObject workFO = FileUtil.toFileObject(getWorkDir());
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        
        FileObject data = FileUtil.createData(sourceRoot, "test/Test.java");
        
        TestUtilities.copyStringToFile(FileUtil.toFile(data), code);
        
        data.refresh();
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cacheFO);
        
        SourceUtilsTestUtil.compileRecursively(sourceRoot);
        
        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        
        assertNotNull(ec);
        
        doc = ec.openDocument();
        
        doc.putProperty(Language.class, JavaTokenId.language());
        
        JavaSource js = JavaSource.forFileObject(data);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
        assertTrue(info.getDiagnostics().toString(), info.getDiagnostics().isEmpty());
    }
    
    protected void doFixTest(String code, String expectation, TreePath tpath) throws Exception {
        doFixTest(code, expectation, tpath, false);
    }
    
    protected void doFixTest(String code, String expectation, TreePath tpath, boolean createJD) throws Exception {
        Analyzer an = new Analyzer(info, doc, tpath, Severity.WARNING, Access.PRIVATE, new Cancel() {

            @Override
            public boolean isCanceled() {
                return false;
            }
        });
        List<ErrorDescription> errs = an.analyze();
        assertNotNull(errs);
        assertFalse("none error found", errs.isEmpty());
        for (ErrorDescription edesc : errs) {
            Fix fix = edesc.getFixes().getFixes().get(0);
            fix.implement();
        }

        String result = doc.getText(0, doc.getLength());
        assertEquals("Source: \n" + code, expectation, result);
    }
    
    protected void doClassFixTest(String code, String expectation) throws Exception {
        prepareTest(code);
        
        ClassTree ct = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
        TreePath tpath = new TreePath(new TreePath(info.getCompilationUnit()), ct);
        
        doFixTest(code, expectation, tpath);
    }
    
    protected void doMemberFixTest(String code, String expectation) throws java.lang.Exception {
        doMemberFixTest(code, expectation, 1);
    }

    protected void doMemberFixTest(String code, String expectation, int position) throws Exception {
        prepareTest(code);
        
        ClassTree ct = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
        Tree mt = ct.getMembers().get(position); // skip constructor
        TreePath tpath = new TreePath(new TreePath(new TreePath(info.getCompilationUnit()), ct), mt);
        
        doFixTest(code, expectation, tpath);
    }
    
    protected void doConstructorFixTest(String code, String expectation) throws Exception {
        prepareTest(code);
        
        ClassTree ct = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
        Tree mt = ct.getMembers().get(0); // skip constructor
        TreePath tpath = new TreePath(new TreePath(new TreePath(info.getCompilationUnit()), ct), mt);
        
        doFixTest(code, expectation, tpath);
    }
    
    static class Pool extends DataLoaderPool {

        @Override
        protected Enumeration<? extends DataLoader> loaders() {
            return Enumerations.singleton(JavaDataLoader.findObject(JavaDataLoader.class, true));
        }
        
    }
    
}
