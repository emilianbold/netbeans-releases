/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.struts.editor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.xml.text.syntax.XMLKit;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

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

    /** Test when the cursor is inside of declaration <action ..... />
     */
    public void testGetActionPath() {
        BaseDocument doc = createBaseDocument(new File(testDir, "struts-config.xml"));
        String path = null;
        String text = null;
        try {
            text = doc.getText(0, doc.getLength() - 1);
        } catch (BadLocationException ex) {
            fail(ex.toString());
        }
        int where;
        
        where = text.indexOf("/login");
        path = StrutsEditorUtilities.getActionPath(doc, where);
        assertEquals("/login", path);
        path = StrutsEditorUtilities.getActionPath(doc, where+1);
        assertEquals("/login", path);
        path = StrutsEditorUtilities.getActionPath(doc, where+6);
        assertEquals("/login", path);
        
        where = text.indexOf("action type=\"com");
        path = StrutsEditorUtilities.getActionPath(doc, where);
        assertEquals("/login", path);
        path = StrutsEditorUtilities.getActionPath(doc, where-1);
        assertEquals("/login", path);
        path = StrutsEditorUtilities.getActionPath(doc, where+7);
        assertEquals("/login", path);
        path = StrutsEditorUtilities.getActionPath(doc, where+10);
        assertEquals("/login", path);
    }

    /** Test when the cursor is inside of declaration <form-bean ..... />
     */
    public void testGetActionFormBeanName() {
        BaseDocument doc = createBaseDocument(new File(testDir, "struts-config.xml"));
        String path = null;
        String text = null;
        try {
            text = doc.getText(0, doc.getLength() - 1);
        } catch (BadLocationException ex) {
            fail(ex.toString());
        }
        int where;
        
        where = text.indexOf("name=\"FirstBean\"");
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where);
        assertEquals("FirstBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+5);
        assertEquals("FirstBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+10);
        assertEquals("FirstBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+16);
        assertEquals("FirstBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where-1);
        assertEquals("FirstBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where - 20);
        assertEquals("FirstBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where-35);
        assertEquals("FirstBean", path);
        
        where = text.indexOf("initial=\"33\"");
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where);
        assertEquals("SecondBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+5);
        assertEquals("SecondBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+10);
        assertEquals("SecondBean", path);
        
        where = text.indexOf("name=\"name\"");
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where);
        assertEquals("SecondBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+5);
        assertEquals("SecondBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+10);
        assertEquals("SecondBean", path);
        
        where = text.indexOf("/form-bean>");
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where);
        assertEquals("SecondBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+5);
        assertEquals("SecondBean", path);
        
        where = text.indexOf("name=\"SecondBean\">");
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+10);
        assertEquals("SecondBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+15);
        assertEquals("SecondBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+17);
        assertEquals("SecondBean", path);
        path = StrutsEditorUtilities.getActionFormBeanName(doc, where+18);
        assertEquals("SecondBean", path);
    }
    
    private BaseDocument createBaseDocument(File file){
        BaseDocument doc = new BaseDocument(XMLKit.class, false);
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
