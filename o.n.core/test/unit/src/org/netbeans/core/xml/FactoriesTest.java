/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.xml;

import java.io.*;
import java.net.*;
import java.util.*; 
import javax.xml.parsers.*;
import javax.xml.parsers.SAXParserFactory;
import junit.framework.*;
import org.netbeans.core.startup.DOMFactoryImpl;
import org.netbeans.core.startup.SAXFactoryImpl;
import org.netbeans.junit.*;
import org.openide.util.*;
import org.xml.sax.*;



/**
 * A test verifying that by default, cheap JDK-default crimson parser is used,
 * but if you ask for some fancy features, possible better parser is provided.
 *
 * @author Nenik
 */
public class FactoriesTest extends NbTestCase {
    static SAXParserFactory origSAX;
    static DocumentBuilderFactory origDOM;
    static {
        origSAX = SAXParserFactory.newInstance();
        origDOM = DocumentBuilderFactory.newInstance();
        SAXFactoryImpl.install();
        DOMFactoryImpl.install();
        System.setSecurityManager (new org.netbeans.TopSecurityManager ());
    }
    /** Creates a new instance of FactoriesTest */
    public FactoriesTest(String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite(FactoriesTest.class);
        return suite;
    }

    
    protected void setUp () throws Exception {
        System.setProperty("org.openide.util.Lookup", "org.netbeans.core.xml.FactoriesTest$Lkp");
        assertNotNull ("ErrManager has to be in lookup", org.openide.util.Lookup.getDefault ().lookup (ErrManager.class));
        
        SAXParserFactory sax = SAXParserFactory.newInstance ();
        if (!(sax instanceof SAXFactoryImpl)) {
            fail ("We expect to see our factory, but was: " + sax);
        }
        
        DocumentBuilderFactory dom = DocumentBuilderFactory.newInstance ();
        if (!(dom instanceof DOMFactoryImpl)) {
            fail ("We expect to see our factory, but was: " + dom);
        }
        
    }

    
    /** Check whether factory provides cheap parser by default */
    public void testCreateCheapSAXParser() throws Exception {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        assertFalse("JDK-provided parser", testMap.containsKey(parser));
    }
    
    /** Check whether factory provides cheap parser by default */
    public void testCreateFeaturedSAXParser() throws Exception {
        String[] features = new String[] {
            SaxFactory1.class.getName(),
            SaxFactory2.class.getName(),
            SaxFactory3.class.getName(),
            SaxFactory4.class.getName(),
        };
        
        for (int i=0; i<features.length; i++) {
            String feature = features[i];
            SAXParserFactory fact = SAXParserFactory.newInstance();
            fact.setFeature(feature, true);
            
            SAXParser parser = fact.newSAXParser();
            assertEquals("Parser with feature " + feature, feature, testMap.get(parser));
        }
    }
    
    /** Check whether factory fails for unsupported feature */
    public void testCreateNonexistingSAXParser() throws Exception {
        SAXParserFactory fact = SAXParserFactory.newInstance();
        try {
            fact.setFeature("NoNeXiStInGfEaTuRe", true);
        } catch (Exception e) {
            return; // OK
        }
        
        fail ("Created parser with unsupported feature");
    }

        /** Check whether factory provides cheap parser by default */
    public void testCreateCheapDOMBuilder() throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        assertFalse("JDK-provided builder", testMap.containsKey(builder));
    }
    
    /** Check whether factory provides cheap parser by default */
    public void testCreateFeaturedDOMBuilder() throws Exception {
        String[] features = new String[] {
            DOMFactory1.class.getName(),
            DOMFactory2.class.getName(),
            DOMFactory3.class.getName(),
            DOMFactory4.class.getName(),
        };
        
        for (int i=0; i<features.length; i++) {
            String feature = features[i];
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            fact.setAttribute(feature, "YES");
            
            DocumentBuilder builder = fact.newDocumentBuilder();
            assertEquals("Builder with feature " + feature, feature, testMap.get(builder));
        }
    }
    
    /** Check whether factory fails for unsupported feature */
    public void testCreateNonexistingDOMBuilder() throws Exception {
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        try {
            fact.setAttribute("NoNeXiStInGfEaTuRe", "YES");
        } catch (Exception e) {
            return; // OK
        }
        
        fail ("Created builder with unsupported feature");
    }
    
    /** For cases the factories are not on classpath we fallback to 
     * some implementation.
     */
    public void testFactoriesAreNotOnClassPath () throws Exception {
        ClassLoader parent = SAXFactoryImpl.class.getClassLoader ();
        assertNotNull ("We have a classloader", parent);
        parent = parent.getParent ();
        assertNotNull ("Still not null", parent);
        
        try {
            Class l = parent.loadClass (SAXFactoryImpl.class.getName ());
            fail ("The classloader " + parent + " should not be able to load: " + l);
        } catch (ClassNotFoundException ex) {
            // ok, satisfied
        }
        
        CL loader = new CL (parent, FactoriesRunnableHid.class.getName (), getClass ().getResource ("FactoriesRunnableHid.class"));
        Class runnableClass = loader.loadClass (FactoriesRunnableHid.class.getName ());
        assertNotNull (runnableClass);
        assertFalse ("Different class than our", FactoriesRunnableHid.class == runnableClass);
        
        Thread.currentThread ().setContextClassLoader (loader);
        Runnable run = (Runnable)runnableClass.newInstance ();
        run.run ();
        
        /** the runnable also implements map so we can get some values from it ;-) */
        Map map = (Map)run;
        
        Object dom = map.get ("dom");
        Object sax = map.get ("sax");
        
        assertNotNull ("Wants dom", dom);
        assertNotNull ("Wants sax", sax);
     
        assertEquals ("We should use orignal sax", origSAX.getClass (), sax.getClass ());
        assertEquals ("We should use orignal dom", origDOM.getClass (), dom.getClass ());
        
    }

    
    static Map testMap = new WeakHashMap();
    
    static class GenericSAXFactory extends SAXParserFactory {
        String supported;
        boolean value;
        
        GenericSAXFactory() {
            this.supported = getClass().getName();
        }
        
        public SAXParser newSAXParser() throws ParserConfigurationException, SAXException {
            SAXParser parser = origSAX.newSAXParser();
            testMap.put(parser, supported);
            return parser;
        }

        public boolean getFeature(String name) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
            if (supported.equals(name)) return value;
            return false;
        }


        public void setFeature(String name, boolean value) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
            if (supported.equals(name)) {
                this.value = value;
            } else {
                throw new ParserConfigurationException(name + " not supported"  );
            }
        }
        
    }
    
    public static class SaxFactory1 extends GenericSAXFactory {}
    public static class SaxFactory2 extends GenericSAXFactory {}
    public static class SaxFactory3 extends GenericSAXFactory {}
    public static class SaxFactory4 extends GenericSAXFactory {}
    
    static class GenericDOMFactory extends DocumentBuilderFactory {
        String supported;
        Object value;
        
        GenericDOMFactory() {
            this.supported = getClass().getName();
        }
        
        public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
            DocumentBuilder builder = origDOM.newDocumentBuilder();
            testMap.put(builder, supported);
            return builder;
        }
    
    
        public java.lang.Object getAttribute(java.lang.String name) throws java.lang.IllegalArgumentException {
            if (supported.equals(name)) return value;
            return null;
        }



        public void setAttribute(java.lang.String name, java.lang.Object value) throws java.lang.IllegalArgumentException {
            if (supported.equals(name)) {
                this.value = value;
            } else {
                throw new IllegalArgumentException(name + " not supported"  );
            }
        }

        public boolean getFeature(java.lang.String name) throws javax.xml.parsers.ParserConfigurationException {
            return false;
        }

        public void setFeature(java.lang.String name, boolean value) throws javax.xml.parsers.ParserConfigurationException {
        }
    }
    
    public static class DOMFactory1 extends GenericDOMFactory {}
    public static class DOMFactory2 extends GenericDOMFactory {}
    public static class DOMFactory3 extends GenericDOMFactory {}
    public static class DOMFactory4 extends GenericDOMFactory {}

    
    
    //
    // Our fake lookup
    //
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            ic.add (new ErrManager ());
            ic.add (new SaxFactory1());
            ic.add (new SaxFactory2());
            ic.add (new SaxFactory3());
            ic.add (new SaxFactory4());
            ic.add (new DOMFactory1());
            ic.add (new DOMFactory2());
            ic.add (new DOMFactory3());
            ic.add (new DOMFactory4());
        }
    }
    //
    // Logging support
    //
    public static final class ErrManager extends org.openide.ErrorManager {
        public static final StringBuffer messages = new StringBuffer ();
        
        private String prefix;
        
        public ErrManager () {
            this (null);
        }
        public ErrManager (String prefix) {
            this.prefix = prefix;
        }
        
        public Throwable annotate (Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, java.util.Date date) {
            return t;
        }
        
        public Throwable attachAnnotations (Throwable t, org.openide.ErrorManager.Annotation[] arr) {
            return t;
        }
        
        public org.openide.ErrorManager.Annotation[] findAnnotations (Throwable t) {
            return null;
        }
        
        public org.openide.ErrorManager getInstance (String name) {
            return new ErrManager ();
        }
        
        public void log (int severity, String s) {
            if (prefix != null) {
                messages.append (prefix);
                messages.append (s);
                messages.append ('\n');
            }
        }
        
        public void notify (int severity, Throwable t) {
            log (severity, t.getMessage ());
        }
        
        public boolean isNotifiable (int severity) {
            return prefix != null;
        }
        
        public boolean isLoggable (int severity) {
            return prefix != null;
        }
        
    } // end of ErrManager

    /** Our own classloader that loads FactoriesRunnableHid
     */
    public static final class CL extends ClassLoader {
        private String name;
        private URL url;
        
        public CL (ClassLoader parent, String className, URL url) {
            super (parent);
            this.name = className;
            this.url = url;
        } 

        protected Class findClass(String str) throws ClassNotFoundException {
            if (str.equals (name)) {
                try {
                    InputStream is = url.openStream ();
                    byte[] arr = new byte[4096];
                    int len = is.read (arr);
                    return defineClass (name, arr, 0, len);
                } catch (java.io.IOException ex) {
                    throw new ClassNotFoundException (ex.getMessage ());
                }
            }
            throw new ClassNotFoundException ();
        }
        
        
    } // end of CL
}
