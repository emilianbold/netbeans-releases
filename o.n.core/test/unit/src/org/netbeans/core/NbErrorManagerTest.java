/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.MissingResourceException;
import org.netbeans.junit.*;
import junit.textui.TestRunner;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.xml.sax.SAXParseException;

/**
 * Test the core error manager impl.
 * @author Jesse Glick
 * @see "#18141"
 */
public class NbErrorManagerTest extends NbTestCase {
    
    public NbErrorManagerTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(NbErrorManagerTest.class));
    }
    
    private NbErrorManager err;
    private StringWriter w;
    protected void setUp() throws Exception {
        w = new StringWriter();
        err = new NbErrorManager(new PrintWriter(w));
    }
    
    public void testEMFound() throws Exception {
        assertEquals(NbErrorManager.class, Lookup.getDefault().lookup(ErrorManager.class).getClass());
    }
    
    public void testBasicNotify() throws Exception {
        assertTrue(err.isNotifiable(ErrorManager.EXCEPTION));
        NullPointerException npe = new NullPointerException("unloc msg");
        err.notify(ErrorManager.INFORMATIONAL, npe);
        String s = w.toString();
        assertTrue(s.indexOf("java.lang.NullPointerException: unloc msg") != -1);
        assertTrue(s.indexOf("testBasicNotify") != -1);
    }
    
    public void testLog() throws Exception {
        assertTrue(!err.isLoggable(ErrorManager.INFORMATIONAL));
        err.log("some msg");
        String s = w.toString();
        assertTrue(s.indexOf("some msg") == -1);
        assertTrue(err.isLoggable(ErrorManager.WARNING));
        err.log(ErrorManager.WARNING, "another msg");
        s = w.toString();
        assertTrue(s.indexOf("another msg") != -1);
        ErrorManager err2 = err.getInstance("foo.bar.baz");
        assertTrue(!err2.isLoggable(ErrorManager.INFORMATIONAL));
        err2.log("sub msg #1");
        s = w.toString();
        assertTrue(s.indexOf("sub msg #1") == -1);
        System.setProperty("quux.hoho", "0");
        err2 = err.getInstance("quux.hoho.yaya");
        assertTrue(err2.isLoggable(ErrorManager.INFORMATIONAL));
        err2.log("sub msg #2");
        s = w.toString();
        assertTrue(s.indexOf("sub msg #2") != -1);
        assertTrue(s.indexOf("quux.hoho.yaya") != -1);
    }
    
    /** @see "#15611" */
    public void testNestedThrowables() throws Exception {
        NullPointerException npe = new NullPointerException("unloc msg");
        ClassNotFoundException cnfe = new ClassNotFoundException("other msg", npe);
        err.notify(ErrorManager.INFORMATIONAL, cnfe);
        String s = w.toString();
        assertTrue(s.indexOf("java.lang.NullPointerException: unloc msg") != -1);
        // JDK 1.3.1 will not print the detail message "other msg", OK:
        assertTrue(s.indexOf("java.lang.ClassNotFoundException") != -1);
        npe = new NullPointerException("msg1");
        IOException ioe = new IOException("msg2");
        err.annotate(ioe, npe);
        InvocationTargetException ite = new InvocationTargetException(ioe, "msg3");
        IllegalStateException ise = new IllegalStateException("msg4");
        err.annotate(ise, ite);
        err.notify(ErrorManager.INFORMATIONAL, ise);
        s = w.toString();
        assertTrue(s.indexOf("java.lang.NullPointerException: msg1") != -1);
        assertTrue(s.indexOf("java.io.IOException: msg2") != -1);
        // Again passes on 1.4 but not 1.3:
        //assertTrue(s.indexOf("msg3") != -1);
        assertTrue(s.indexOf("java.lang.IllegalStateException: msg4") != -1);
    }
    
    public void testNotifyWithAnnotations() throws Exception {
        NullPointerException npe = new NullPointerException("unloc msg");
        err.annotate(npe, "loc msg #1");
        err.notify(ErrorManager.INFORMATIONAL, npe);
        String s = w.toString();
        assertTrue(s.indexOf("java.lang.NullPointerException: unloc msg") != -1);
        assertTrue(s.indexOf("loc msg #1") != -1);
        npe = new NullPointerException("unloc msg");
        err.annotate(npe, ErrorManager.UNKNOWN, "extra unloc msg", null, null, null);
        err.notify(ErrorManager.INFORMATIONAL, npe);
        s = w.toString();
        assertTrue(s.indexOf("extra unloc msg") != -1);
        npe = new NullPointerException("new unloc msg");
        IOException ioe = new IOException("something bad");
        err.annotate(ioe, npe);
        err.notify(ErrorManager.INFORMATIONAL, ioe);
        s = w.toString();
        assertTrue(s.indexOf("java.lang.NullPointerException: new unloc msg") != -1);
        assertTrue(s.indexOf("java.io.IOException: something bad") != -1);
    }
    
    public void testDeepAnnotations() throws Exception {
        Exception e1 = new Exception("msg1");
        // #19114: deeply nested loc msgs should be used
        err.annotate(e1, "some loc msg");
        Exception e2 = new Exception("msg2");
        err.annotate(e2, e1);
        Exception e3 = new Exception("msg3");
        err.annotate(e3, e2);
        Exception e4 = new Exception("msg4");
        err.annotate(e3, e4);
        err.notify(ErrorManager.INFORMATIONAL, e3);
        String s = w.toString();
        assertTrue(s.indexOf("java.lang.Exception: msg1") != -1);
        assertTrue(s.indexOf("java.lang.Exception: msg2") != -1);
        assertTrue(s.indexOf("java.lang.Exception: msg3") != -1);
        assertTrue(s.indexOf("java.lang.Exception: msg4") != -1);
        assertTrue(s.indexOf("some loc msg") != -1);
    }
    
    /** @see "#19487" */
    public void testLoops() throws Exception {
        Exception e1 = new Exception("msg1");
        Exception e2 = new Exception("msg2");
        err.annotate(e2, e1);
        Exception e3 = new Exception("msg3");
        err.annotate(e3, e2);
        err.annotate(e1, e3);
        err.notify(ErrorManager.INFORMATIONAL, e1);
        String s = w.toString();
        assertTrue(s.indexOf("java.lang.Exception: msg1") != -1);
        assertTrue(s.indexOf("java.lang.Exception: msg2") != -1);
        assertTrue(s.indexOf("java.lang.Exception: msg3") != -1);
        // warning from NBEM itself:
        assertTrue(s.indexOf("cyclic") != -1);
    }
    
    public void testAddedInfo() throws Exception {
        MissingResourceException mre = new MissingResourceException("msg1", "the.class.Name", "the-key");
        err.notify(ErrorManager.INFORMATIONAL, mre);
        String s = w.toString();
        assertTrue(s.indexOf("java.util.MissingResourceException: msg1") != -1);
        assertTrue(s.indexOf("the.class.Name") != -1);
        assertTrue(s.indexOf("the-key") != -1);
        SAXParseException saxpe = new SAXParseException("msg2", "pub-id", "sys-id", 313, 424);
        err.notify(ErrorManager.INFORMATIONAL, saxpe);
        s = w.toString();
        assertTrue(s.indexOf("org.xml.sax.SAXParseException: msg2") != -1);
        assertTrue(s.indexOf("pub-id") != -1);
        assertTrue(s.indexOf("sys-id") != -1);
        assertTrue(s.indexOf("313") != -1);
        assertTrue(s.indexOf("424") != -1);
    }
    
    /**
     * Actually just tests the same code used when running NE.
     */
    public void testNotifyException() throws Exception {
        IOException ioe = new IOException("unloc msg");
        err.annotate(ioe, "loc msg");
        NbErrorManager.Exc x = err.createExc(ioe, ErrorManager.USER);
        assertEquals(ErrorManager.USER, x.getSeverity());
        assertEquals("loc msg", x.getLocalizedMessage());
        assertTrue(x.isLocalized());
        // could do more here...
    }
    
}
