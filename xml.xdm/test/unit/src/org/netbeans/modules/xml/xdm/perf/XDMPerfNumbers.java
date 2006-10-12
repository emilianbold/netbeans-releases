/*
 * XDMPerfNumbers.java
 *
 * Created on November 10, 2005, 11:17 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.xdm.perf;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.XMLKit;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * This test class is meant to be used for obtaining performance numbers on XDM.
 * NOTE: The numbers may vary on each run and on each machine. Also please note
 * the following before running these tests.
 *
 * 1. Do not run testReadUsingSyntaxParser and testReadUsingXDM together.
 * 2. Avoid insertString for each line. This is BAD. See usage for 'readLine'
 * and 'insertEachLine'.
 *
 * @author Samaresh
 */
public class XDMPerfNumbers extends TestCase {
    
    /**
     * Performance numbers are run on this giant schema.
     */
    static final String SCHEMA_FILE = "J1_TravelItinerary.xsd";
        
    /**
     * Line separator.
     */
    static String lineSeparator = System.getProperty("line.separator");
    
    /**
     * boolean flag, to indicate whether to call insertString() for each line
     * or not. Applicable only when 'readLine' is true. When true, the performance
     * is really BAD.
     */
    boolean insertEachLine = false;
    
    /**
     * boolean flag, to indicate whether to read line-by-line or
     * character-by-character. This flag does NOT make any major
     * difference.
     */
    boolean readLine = false;
    
    public XDMPerfNumbers(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(XDMPerfNumbers.class);
        
        return suite;
    }
            
    public void testReadUsingDOM() {
        System.out.println("testReadUsingDOM");
        long start = System.currentTimeMillis();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream is = getClass().getResourceAsStream(SCHEMA_FILE);
            org.w3c.dom.Document document = builder.parse(is);
            this.assertNotNull("DOM model didn't get created!!!", document);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("Time taken to parse using DOM: " + (end-start) + "ms.\n");
    }
        
    public void testReadUsingSyntaxParser() {
        System.out.println("testReadUsingSyntaxParser");
        try {
            java.net.URL url = getClass().getResource(SCHEMA_FILE);            
            // prepare document
            BaseDocument basedoc = new BaseDocument(XMLKit.class, false);
            insertStringInDocument(new InputStreamReader(url.openStream(),"UTF-8"), basedoc);

            long start = System.currentTimeMillis();
            //org.w3c.dom.Document doc = new XMLSyntaxParser(basedoc).parse();
            ExtSyntaxSupport sup = (ExtSyntaxSupport)basedoc.getSyntaxSupport();
            TokenItem token = sup.getTokenChain(0, basedoc.getLength());
            long end = System.currentTimeMillis();
            System.out.println("Time taken to parse using Syntax Parser: " + (end-start) + "ms.\n");
            this.assertNotNull("Syntax parser model didn't get created!!!", sup);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }    
    
    public void testReadUsingXDM() {
        long start = System.currentTimeMillis();
        try {
            javax.swing.text.Document sd = new BaseDocument(XMLKit.class, false);
            XDMModel model = null;

            InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(SCHEMA_FILE),"UTF-8");
            insertStringInDocument(reader, sd);
            
            start = System.currentTimeMillis();
	    Lookup lookup = Lookups.singleton(sd);
	    ModelSource ms = new ModelSource(lookup, true);
            model = new XDMModel(ms);
            model.sync();
            this.assertNotNull("XDM model didn't get created!!!", model);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("Time taken to parse using XDM: " + (end-start) + "ms.\n");
    }
    
    
    /**
     * Reads data in a buffer reader and then accumulates them in a
     * string buffer
     */
    private void insertStringInDocument(InputStreamReader inputStreamReader,
            javax.swing.text.Document document) {
        
        BufferedReader reader = null;
        System.out.println("insertStringInDocument() called...");
        long start = System.currentTimeMillis();
        try {
            reader = new BufferedReader(inputStreamReader);
            StringBuffer sbuf = new StringBuffer();
            if(readLine) {
                insertLines(reader, document);
            } else {
                insertCharacters(reader, document);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("Time taken in insertStringInDocument(): " + (end-start) + "ms.");        
    }
    
    
    /**
     * Inserts text into the swing document line by line.
     * If 'insertEachLine' is true, inserts them directly, otherwise
     * keeps them in a buffer and inserts at the end.
     * The former is BAD. Do NOT insert each line.
     */
    private void insertLines(BufferedReader reader,
            javax.swing.text.Document document) throws Exception {
        StringBuffer buffer = new StringBuffer();
        String line = null;
        while ((line = reader.readLine()) != null) {
            //do not ever insert each line into the doc. This is a killer.
            if(insertEachLine)
                document.insertString(document.getLength(), line+lineSeparator, null);
            else
                buffer.append(line+lineSeparator);
        }
        if(!insertEachLine) {
            document.insertString(0, buffer.toString(), null);
        }
    }
    
    /**
     * Inserts text into the swing document character by character.
     */
    private void insertCharacters(BufferedReader reader,
            javax.swing.text.Document document) throws Exception {
        StringBuffer buffer = new StringBuffer();
        int c = 0;
        while((c = reader.read()) != -1) {
            buffer.append((char)c);
        }
        
        //finally one insertString
        document.insertString(0, buffer.toString(), null);
    }
}
