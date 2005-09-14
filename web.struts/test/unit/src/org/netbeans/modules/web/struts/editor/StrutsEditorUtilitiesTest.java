/*
 * StrutsEditorUtilitiesTest.java
 * JUnit based test
 *
 * Created on September 9, 2005, 11:23 AM
 */

package org.netbeans.modules.web.struts.editor;

import java.io.File;
import java.io.FileReader;
import junit.framework.*;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.junit.*;

/**
 *
 * @author petr
 */
public class StrutsEditorUtilitiesTest extends NbTestCase {
    
    File testDir;
    FileObject testDirFO;
    
    public StrutsEditorUtilitiesTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        testDir = new File (this.getDataDir().getPath());
        assertTrue("have a dir " + testDir, testDir.isDirectory());
        testDirFO = FileUtil.toFileObject(testDir);
        assertNotNull("testDirFO is null", testDirFO);
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(StrutsEditorUtilitiesTest.class);
        
        return suite;
    }
    
    /** Test when the cursor is inside of declaration <action ..... />
     */
    public void testGetActionPath() {
        BaseDocument doc = createBaseDocument(new File(testDir, "struts-config.xml"));
        String path = StrutsEditorUtilities.getActionPath(doc, 659);
        assertEquals("/login", path);
        path = StrutsEditorUtilities.getActionPath(doc, 657);
        assertEquals("/login", path);
        path = StrutsEditorUtilities.getActionPath(doc, 652);
        assertEquals("/login", path);
        path = StrutsEditorUtilities.getActionPath(doc, 665);
        assertEquals("/login", path);
    }

    private BaseDocument createBaseDocument(File file){
        BaseDocument doc = new BaseDocument(org.netbeans.modules.xml.text.syntax.XMLKit.class, false);
        File strutsConfig = new File(testDir, "struts-config.xml");
        StringBuffer buffer = new StringBuffer();
        try {
            FileReader reader = new FileReader (strutsConfig);
            char[] buf = new char [100];
            int count = -1;
            while ((count = reader.read(buf)) != -1){
                buffer.append(buf, 0, count);
            } 
            reader.close();
            doc.insertString(0, buffer.toString(), null); 
            //System.out.println("where: " + buffer.toString().indexOf("/login"));
            return doc;
        } catch (IOException ex) {
            fail("Exception occured during createBaseDocument: " + ex.toString());
        }
        catch (BadLocationException ex) {
            fail("Exception occured during createBaseDocument: " + ex.toString());
        } 
        return null;
    }
    
}
