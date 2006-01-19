/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.text.indent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import javax.swing.JTextArea;
import junit.framework.*;
import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.AbstractFormatLayer;
import org.netbeans.editor.ext.FormatSupport;
import org.netbeans.editor.ext.FormatWriter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.xml.text.syntax.XMLKit;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;

import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomasz Slota
 */
public class XMLFormatterTest extends NbTestCase {
    /**
     * Used for testing dynamic indentation ("smart enter")
     */
    public final static String LINE_BREAK_METATAG = "|";
    
    public XMLFormatterTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(XMLFormatterTest.class);
        
        return suite;
    }

    public void testReformatSample1(){
        testReformat("web.xml");
    }
    
    public void testReformatSample2(){
        testReformat("netbeans_build.xml");
    }
    
    public void testSmartEnterSample1() throws Exception{
        testSmartEnter("test1");
    }
    
    public void testSmartEnterSample2() throws Exception{
        testSmartEnter("test2");
    }
    
    private void testSmartEnter(String testFileName) throws Exception {
        System.out.println("testSmartEnter(" + testFileName + ")");
        XMLFormatter formatter = new XMLFormatter(XMLKit.class);
        BaseDocument doc = createEmptyBaseDocument();
        XMLSyntaxSupport sup = new XMLSyntaxSupport(doc);
        
        File dirDataFiles = new File(getTestFilesDir(), "testSmartEnter");
        File fileInputFile = new File(dirDataFiles, testFileName + ".in");
        File fileTargetFile = new File(dirDataFiles, testFileName + ".out");
        
        String txtRawSample = readStringFromFile(fileInputFile);
        
        doc.insertString(0, extractCRMetatag(txtRawSample), null);
        int originalCaretPos = txtRawSample.indexOf(LINE_BREAK_METATAG);
        assertTrue(originalCaretPos > -1);
        doc.insertString(originalCaretPos, "\n", null);
        
        int newCaretPos = formatter.smartEnter(doc, originalCaretPos + 1, sup);
        
        String txtRawReference = readStringFromFile(fileTargetFile);
        assertEquals(extractCRMetatag(txtRawReference), doc.getText(0, doc.getLength()));
        int refCaretPos = txtRawReference.indexOf(LINE_BREAK_METATAG);
        assertEquals(newCaretPos, refCaretPos);
    }
    
    private String extractCRMetatag(String text){
        int indexOfMetatag = text.indexOf(LINE_BREAK_METATAG);
        
        if (indexOfMetatag == -1){
            return text;
        }
        
        String firstPart = text.substring(0, indexOfMetatag);
        String secondPart = text.substring(indexOfMetatag + LINE_BREAK_METATAG.length());
        
        if (secondPart.indexOf(LINE_BREAK_METATAG) > -1){
            throw new IllegalArgumentException("text contains more than one line break tag");
        }
        
        return firstPart + secondPart;
    }
    
    private void testReformat(String testFileName) {
        System.out.println("testReformat(" + testFileName + ")");
        XMLFormatter formatter = new XMLFormatter(XMLKit.class);
        BaseDocument doc = createEmptyBaseDocument();
         
        try{
            String txtRawXML = readStringFromFile(new File(new File(
                getTestFilesDir(), "testReformat"), testFileName));
                    
            doc.insertString(0, txtRawXML, null);
            formatter.reformat(doc, 0, doc.getLength(), false);
            getRef().print(doc.getText(0, doc.getLength()));
        }
        catch (Exception e){
            fail(e.getMessage()); // should never happen
        }
        
        compareReferenceFiles();
    }

    private String readStringFromFile(File file) throws IOException {
        StringBuffer buff = new StringBuffer();
        
        BufferedReader rdr = new BufferedReader(new FileReader(file));
        
        String line;
        
        try{
            while ((line = rdr.readLine()) != null){
                buff.append(line + "\n");
            }
        } finally{
            rdr.close();
        }
        
        return buff.toString();
    }
    
    private File getTestFilesDir(){
        return new File(new File(getDataDir(), "input"), "XMLFormatterTest");
    }
    
    private BaseDocument createEmptyBaseDocument(){
        return new BaseDocument(XMLKit.class, false);
    }
}
