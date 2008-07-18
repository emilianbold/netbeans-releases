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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package org.netbeans.lib.uihandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.XMLFormatter;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import org.netbeans.junit.NbTestCase;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.CallbackSystemAction;

/**
 *
 * @author Jindrich Sedek
 */
public class LogFormatterTest extends NbTestCase {
    
    public LogFormatterTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testFormat() throws IOException {
        LogRecord rec = new LogRecord(Level.SEVERE, "PROBLEM");
        Throwable thrown = new NullPointerException("TESTING");
        thrown.initCause(new AssertionError("CAUSE PROBLEM"));
        rec.setThrown(thrown);
        String result = new LogFormatter().format(rec);
        assertTrue(result.contains("java.lang.NullPointerException: TESTING"));
        assertTrue(result.contains("<level>SEVERE</level>"));
        assertTrue(result.contains("<method>testFormat</method>"));
        assertTrue(result.contains("<message>java.lang.AssertionError: CAUSE PROBLEM</message>"));
        assertTrue(result.contains("<more>19</more>"));
        assertTrue(result.contains(" <class>junit.framework.TestSuite</class>"));
        assertTrue(result.contains("<class>sun.reflect.NativeMethodAccessorImpl</class>"));
        assertFalse(result.contains("<more>20</more>"));
    }
        
    
    public void testEasy() throws IOException {
        Throwable thrown = new NullPointerException("TESTING");
        thrown.initCause(new AssertionError("CAUSE PROBLEM"));
        formatAndScan(thrown);
    }
    
    public void testManyCausesFormat() throws IOException{
        try{
            generateIOException();
        }catch(IOException exc){
            formatAndScan(exc);
        }
    }
    
    public void testDontPrintLocalizedMessage() throws IOException{
        LogRecord log = new LogRecord(Level.INFO, "test_msg");
        log.setResourceBundleName("org.netbeans.lib.uihandler.TestBundle");
        log.setResourceBundle(ResourceBundle.getBundle("org.netbeans.lib.uihandler.TestBundle"));
        log.setParameters(new Object[] { new Integer(1), "Ahoj" });
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        LogRecords.write(os, log);
        assertFalse("no localized message is printed" + os.toString(), os.toString().contains(" and "));
        assertTrue("key", os.toString().contains("<key>test_msg</key>"));
        assertTrue("no localized message", os.toString().contains("<message>test_msg</message>"));
    }

    /**
     * test whether the result of LogFormatter is the same as XMLFormatter 
     * if there is no nested exception
     */
    public void testXMLFormatterDifference(){
        LogRecord rec = new LogRecord(Level.SEVERE, "PROBLEM");
        LogFormatter logFormatter = new LogFormatter();
        XMLFormatter xmlFormatter = new XMLFormatter();
        String logResult = logFormatter.format(rec);
        String xmlResult = xmlFormatter.format(rec);
        assertEquals("WITHOUT THROWABLE", xmlResult, logResult);
        rec.setThrown(new NullPointerException("TESTING EXCEPTION"));
        rec.setResourceBundleName("MUJ BUNDLE");
        logResult = logFormatter.format(rec);
        //remove file names
        logResult = logResult.replaceAll("      <file>.*</file>\n", "");
        xmlResult = xmlFormatter.format(rec);
        assertEquals("WITH THROWABLE", xmlResult, logResult);
    }
    
    private void formatAndScan(Throwable thr) throws IOException{
        ByteArrayOutputStream oStream = new ByteArrayOutputStream(1000);
        LogRecord rec = new LogRecord(Level.SEVERE, "PROBLEM");
        rec.setThrown(thr);
        LogRecords.write(oStream, rec);//write to stream
        ByteArrayInputStream iStream = new ByteArrayInputStream(oStream.toByteArray());
        Formatter formatter = new LogFormatter();
        LogRecord readFromStream = new TestHandler(iStream).read();// read from stream
        //read by handler equals the writen by formatter
        assertEquals(formatter.format(readFromStream), formatter.format(rec));    
        oStream.reset();
        thr.printStackTrace(new PrintStream(oStream));
        String writen = oStream.toString();
        oStream.reset();
        rec.getThrown().printStackTrace(new PrintStream(oStream));
        String read = oStream.toString();
        assertEquals(writen, read);//both stacktraces are the same        
    }
    
    private void generateIOException()throws IOException{
        try{
            generateSQL();
        }catch(SQLException error){
            IOException except = new IOException("IO EXCEPTION");
            except.initCause(error);
            throw except;
        }
    }
            
    private void generateSQL() throws SQLException{
        try{
            generateClassNotFoundException();
        }catch(ClassNotFoundException exception){
            SQLException except = new SQLException("SQL TESTING EXCEPTION");
            except.initCause(exception);
            throw except;
        }
    }
    
    private void generateClassNotFoundException() throws ClassNotFoundException{
        java.lang.Class.forName("unknown name");
    }                  
    
    public void testFormatterDoesNotIncludeHashOnButton() throws ClassNotFoundException {
        LogRecord r = new LogRecord(Level.INFO, "BUTTON");
        r.setParameters(new Object[] { new JButton("kuk") });
        Formatter formatter = new LogFormatter();
        String s = formatter.format(r);
        assertEquals("No @\n" + s, -1, s.indexOf("@"));
        if (s.indexOf("kuk") == -1) {
            fail("kuk should be there:\n" + s);
        }
    }
    public void testFormatterDoesNotIncludeHashOnActions() throws ClassNotFoundException {
        LogRecord r = new LogRecord(Level.INFO, "ACTION");
        SA sa = SA.get(SA.class);
        r.setParameters(new Object[] { sa });
        Formatter formatter = new LogFormatter();
        String s = formatter.format(r);
        assertEquals("No @\n" + s, -1, s.indexOf("@"));
        if (s.indexOf("SomeName") == -1) {
            fail("SomeName should be there:\n" + s);
        }
        if (s.indexOf("LogFormatterTest$SA") == -1) {
            fail("LogFormatterTest$SA should be there:\n" + s);
        }
    }
    public void testFormatterDoesNotIncludeHashOnActionsClone() throws ClassNotFoundException {
        LogRecord r = new LogRecord(Level.INFO, "ACTION_CLONE");
        SA sa = SA.get(SA.class);
        r.setParameters(new Object[] { sa.createContextAwareInstance(Lookup.EMPTY) });
        Formatter formatter = new LogFormatter();
        String s = formatter.format(r);
        assertEquals("No @\n" + s, -1, s.indexOf("@"));
        if (s.indexOf("SomeName") == -1) {
            fail("SomeName should be there:\n" + s);
        }
        if (s.indexOf("LogFormatterTest$SA") == -1) {
            fail("LogFormatterTest$SA should be there:\n" + s);
        }
    }
    public void testFormatterDoesNotIncludeHashOnMenu() throws ClassNotFoundException {
        LogRecord r = new LogRecord(Level.INFO, "MENU");
        SA sa = SA.get(SA.class);
        r.setParameters(new Object[] { new JMenuItem(sa) });
        Formatter formatter = new LogFormatter();
        String s = formatter.format(r);
        assertEquals("No @\n" + s, -1, s.indexOf("@"));
        if (s.indexOf("SomeName") == -1) {
            fail("SomeName should be there:\n" + s);
        }
        if (s.indexOf("LogFormatterTest$SA") == -1) {
            fail("LogFormatterTest$SA should be there:\n" + s);
        }
    }
    public void testFormatterDoesNotIncludeHashOnEditor() throws ClassNotFoundException {
        LogRecord r = new LogRecord(Level.INFO, "EDIT");
        JEditorPane ep = new javax.swing.JEditorPane();
        ep.setName("SomeName");
        r.setParameters(new Object[] { ep });
        Formatter formatter = new LogFormatter();
        String s = formatter.format(r);
        assertEquals("No @\n" + s, -1, s.indexOf("@"));
        if (s.indexOf("SomeName") == -1) {
            fail("SomeName should be there:\n" + s);
        }
    }
    
    public static class SA extends CallbackSystemAction {

        public String getName() {
            return "SomeName";
        }

        public HelpCtx getHelpCtx() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}


