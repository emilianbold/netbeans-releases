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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.hints.infrastructure;

import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import javax.swing.text.Document;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.java.source.TestUtil;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class ErrorHintsProviderTest extends NbTestCase {
    
    public ErrorHintsProviderTest(String testName) {
        super(testName);
    }

//    public static Test suite() {
//        TestSuite suite = new TestSuite(JavaHintsProviderTest.class);
//        
//        return suite;
//    }
    
    private FileObject testSource;
    private JavaSource js;
    private CompilationInfo info;
    
    private static File cache;
    private static FileObject cacheFO;
    
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[0]);
        
        if (cache == null) {
            cache = TestUtil.createWorkFolder();
            cacheFO = FileUtil.toFileObject(cache);
            
            cache.deleteOnExit();
        }
    }
    
    private void prepareTest(String capitalizedName) throws Exception {
        FileObject workFO = makeScratchDir(this);
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
//        FileObject cache = workFO.createFolder("cache");
        FileObject packageRoot = FileUtil.createFolder(sourceRoot, "javahints");
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cacheFO);
        
        String testPackagePath = "javahints/";
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
    
    /**Copied from org.netbeans.api.project.
     * Create a scratch directory for tests.
     * Will be in /tmp or whatever, and will be empty.
     * If you just need a java.io.File use clearWorkDir + getWorkDir.
     */
    public static FileObject makeScratchDir(NbTestCase test) throws IOException {
        test.clearWorkDir();
        File root = test.getWorkDir();
        assert root.isDirectory() && root.list().length == 0;
        FileObject fo = FileUtil.toFileObject(root);
        if (fo != null) {
            // Presumably using masterfs.
            return fo;
        } else {
            // For the benefit of those not using masterfs.
            LocalFileSystem lfs = new LocalFileSystem();
            try {
                lfs.setRootDirectory(root);
            } catch (PropertyVetoException e) {
                assert false : e;
            }
            Repository.getDefault().addFileSystem(lfs);
            return lfs.getRoot();
        }
    }
    
    private void performTest(String name) throws Exception {
        prepareTest(name);
        
        DataObject testData = DataObject.find(testSource);
        EditorCookie ec = (EditorCookie) testData.getCookie(EditorCookie.class);
        Document doc = ec.openDocument();
        
        BaseDocument bdoc = new NbEditorDocument(JavaKit.class);
        
        bdoc.putProperty("mimeType", "text/x-java");
        bdoc.putProperty(Document.StreamDescriptionProperty, testData);
        bdoc.insertString(0, doc.getText(0, doc.getLength()), null);
        
        for (ErrorDescription ed : new ErrorHintsProvider(testSource).computeErrors(info, doc))
            ref(ed.toString());
        
        compareReferenceFiles();
    }
    
    public void testShortErrors1() throws Exception {
        performTest("TestShortErrors1");
    }
    
    public void testShortErrors2() throws Exception {
        performTest("TestShortErrors2");
    }
    
    public void testShortErrors3() throws Exception {
        performTest("TestShortErrors3");
    }

    public void testShortErrors4() throws Exception {
        performTest("TestShortErrors4");
    }
    
    public void testShortErrors5() throws Exception {
        performTest("TestShortErrors5");
    }
    
    public void testShortErrors6() throws Exception {
        performTest("TestShortErrors6");
    }
    
    public void testShortErrors7() throws Exception {
        performTest("TestShortErrors7");
    }
    
    public void testShortErrors8() throws Exception {
        performTest("TestShortErrors8");
    }
    
    public void testShortErrors9() throws Exception {
        performTest("TestShortErrors9");
    }
    
}
