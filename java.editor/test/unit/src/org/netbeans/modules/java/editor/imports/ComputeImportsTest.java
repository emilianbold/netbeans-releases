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
package org.netbeans.modules.java.editor.imports;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.test.support.MemoryValidator;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.editor.overridden.IsOverriddenAnnotationCreatorTest;
import org.netbeans.modules.java.editor.imports.ComputeImports.Pair;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class ComputeImportsTest extends NbTestCase {
    
    private static final Set<String> JDK16_MASKS = new HashSet(Arrays.asList(new String[] {
        "com.sun.xml.bind.v2.schemagen.xmlschema.List",
        "com.sun.xml.txw2.Document",
    }));
    
    private static final Set<String> NO_MASKS = new HashSet();
    
    private FileObject testSource;
    private JavaSource js;
    private CompilationInfo info;
    
    private static File cache;
    private static FileObject cacheFO;
    
    public ComputeImportsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[0]);
        
        if (cache == null) {
            cache = TestUtil.createWorkFolder();
            cacheFO = FileUtil.toFileObject(cache);
            
            cache.deleteOnExit();
            
            IndexUtil.setCacheFolder(cache);
            
            for (URL u : SourceUtilsTestUtil.getBootClassPath()) {
                final ClassIndexImpl ci = ClassIndexManager.getDefault().createUsagesQuery(u, false);
                ProgressHandle handle = ProgressHandleFactory.createHandle("cache creation");
                ci.getBinaryAnalyser().analyse(u, handle);
            }
        }
    }
    
    public static Test suite() {
        return MemoryValidator.wrap(new TestSuite(ComputeImportsTest.class));
    }
    
    public void testSimple() throws Exception {
        doTest("TestSimple", JDK16_MASKS, JDK16_MASKS);
    }
    
    public void testFilterDeclaration() throws Exception {
        doTest("TestFilterDeclaration", JDK16_MASKS, NO_MASKS);
    }
    
    public void testFilterTypedInitializator() throws Exception {
        doTest("TestFilterTypedInitializator", JDK16_MASKS, NO_MASKS);
    }
    
    public void testFilterWithMethods() throws Exception {
        doTest("TestFilterWithMethods", NO_MASKS, NO_MASKS);
    }
    
    public void testGetCookie() throws Exception {
        doTest("TestGetCookie", JDK16_MASKS, JDK16_MASKS);
    }
    
    public void testNew() throws Exception {
        doTest("TestNew", NO_MASKS, NO_MASKS);
    }
    
    public void testException() throws Exception {
        doTest("TestException", NO_MASKS, NO_MASKS);
    }
    
    public void testEmptyCatch() throws Exception {
        doTest("TestEmptyCatch", NO_MASKS, NO_MASKS);
    }
    
    public void testUnfinishedMethod() throws Exception {
        doTest("TestUnfinishedMethod", NO_MASKS, NO_MASKS);
    }
    
    public void testUnsupportedOperation1() throws Exception {
        doTest("TestUnsupportedOperation1", NO_MASKS, NO_MASKS);
    }
    
    public void testPackageDoesNotExist() throws Exception {
        doTest("TestPackageDoesNotExist", NO_MASKS, NO_MASKS);
    }

    public void testUnfinishedMethod2() throws Exception {
        doTest("TestUnfinishedMethod2", NO_MASKS, NO_MASKS);
    }
    
    private void prepareTest(String capitalizedName) throws Exception {
        FileObject workFO = IsOverriddenAnnotationCreatorTest.makeScratchDir(this);
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
//        FileObject cache = workFO.createFolder("cache");
        FileObject packageRoot = FileUtil.createFolder(sourceRoot, "org/netbeans/modules/java/editor/imports/data");
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cacheFO);
        
        String testPackagePath = "org/netbeans/modules/java/editor/imports/data/";
        File   testPackageFile = new File(getDataDir(), testPackagePath);
        
        String[] names = testPackageFile.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.endsWith(".java"))
                    return true;
                
                return false;
            }
        });
        
        String[] files = new String[names.length];
        
        for (int cntr = 0; cntr < files.length; cntr++) {
            files[cntr] = testPackagePath + names[cntr];
        }
        
        TestUtil.copyFiles(getDataDir(), FileUtil.toFile(sourceRoot), files);
        
        packageRoot.refresh();
        
        testSource = packageRoot.getFileObject(capitalizedName + ".java");
        
        assertNotNull(testSource);
        
        js = JavaSource.forFileObject(testSource);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
    }
    
    private void dump(PrintStream out, Map<String, List<TypeElement>> set, Set<String> masks) {
        for (Map.Entry<String, List<TypeElement>> e : set.entrySet()) {
            List<String> fqns = new ArrayList<String>();
            
            for (TypeElement t : e.getValue()) {
                String fqn = t.getQualifiedName().toString();
                
                if (!masks.contains(fqn))
                    fqns.add(fqn);
            }
            
            out.println(e.getKey() + ":" + fqns.toString());
        }
    }
    
    private void doTest(String name, Set<String> unfilteredMasks, Set<String> filteredMasks) throws Exception {
        prepareTest(name);
        
        DataObject testDO = DataObject.find(testSource);
        EditorCookie ec = (EditorCookie) testDO.getCookie(EditorCookie.class);
        
        assertNotNull(ec);
        
        Document doc = ec.openDocument();
        
        Pair<Map<String, List<TypeElement>>, Map<String, List<TypeElement>>> candidates = new ComputeImports().computeCandidates(info);
        
        for (List<TypeElement> cand : candidates.b.values()) {
            Collections.sort(cand, new Comparator<TypeElement>() {
                public int compare(TypeElement t1, TypeElement t2) {
                    return t1.getQualifiedName().toString().compareTo(t2.getQualifiedName().toString());
                }
            });
        }
        
        for (List<TypeElement> cand : candidates.a.values()) {
            Collections.sort(cand, new Comparator<TypeElement>() {
                public int compare(TypeElement t1, TypeElement t2) {
                    return t1.getQualifiedName().toString().compareTo(t2.getQualifiedName().toString());
                }
            });
        }
        
        dump(getLog(getName() + "-unfiltered.ref"), candidates.b, unfilteredMasks);
        dump(getLog(getName() + "-filtered.ref"), candidates.a, filteredMasks);
        
        compareReferenceFiles(getName() + "-unfiltered.ref", getName() + "-unfiltered.pass", getName() + "-unfiltered.diff");
        compareReferenceFiles(getName() + "-filtered.ref", getName() + "-filtered.pass", getName() + "-filtered.diff");
    }
}
