/*
 * ElementHeadersTest.java
 * JUnit based test
 *
 * Created on June 12, 2007, 6:13 PM
 */

package org.netbeans.api.java.source.ui;

import com.sun.source.util.TreePath;
import java.io.File;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.JavaDataLoader;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author phrebejk
 */
public class ElementHeadersTest extends NbTestCase {
    
    public ElementHeadersTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[] {JavaDataLoader.class});
    }
    
    private void prepareTest(String fileName, String code) throws Exception {
        clearWorkDir();
        
        FileObject workFO = FileUtil.toFileObject(getWorkDir());
        
        assertNotNull(workFO);
        
        sourceRoot = workFO.createFolder("src");
        
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        
        FileObject data = FileUtil.createData(sourceRoot, fileName);
        File dataFile = FileUtil.toFile(data);
        
        assertNotNull(dataFile);
        
        TestUtilities.copyStringToFile(dataFile, code);
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
        
        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        
        assertNotNull(ec);
        
        doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        
        JavaSource js = JavaSource.forFileObject(data);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
    }
    
    private FileObject sourceRoot;
    private CompilationInfo info;
    private Document doc;
    
    protected void performTest(String fileName, String code, int pos, String format, String golden) throws Exception {
        prepareTest(fileName, code);
        
        TreePath path = info.getTreeUtilities().pathFor(pos);
        
        assertEquals(golden, ElementHeaders.getHeader(path, info, format));
    }
    
    public void testField() throws Exception {
        performTest("test/Test.java", "package test; public class Test { int aa; }", 39, ElementHeaders.NAME, "aa");
        performTest("test/Test.java", "package test; public class Test { int aa; }", 39, ElementHeaders.NAME, "aa");
        performTest("test/Test.java", "package test; public class Test { int aa; }", 39, ElementHeaders.NAME, "aa");
        performTest("test/Test.java", "package test; public class Test { int aa; }", 39, ElementHeaders.NAME, "aa");
        performTest("test/Test.java", "package test; public class Test { int aa; }", 39, ElementHeaders.NAME, "aa");
        performTest("test/Test.java", "package test; public class Test { int aa; }", 39, ElementHeaders.NAME, "aa");
    }
}
