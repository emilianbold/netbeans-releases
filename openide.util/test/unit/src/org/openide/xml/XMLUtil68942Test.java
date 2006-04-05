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

package org.openide.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public class XMLUtil68942Test extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.xml.XMLUtil68942Test$Lkp"); // NOI18N
        System.setProperty("jaxp.debug", "true");
    }
    
    public XMLUtil68942Test(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        Object lookup = Lkp.getDefault();
        assertNotNull(lookup);
        assertEquals("right class", Lkp.class, lookup.getClass());
        
//        System.setProperty("javax.xml.parsers.SAXParserFactory",
//                               "org.netbeans.core.startup.SAXFactoryImpl");
//
//        System.setProperty(
//            "javax.xml.parsers.DocumentBuilderFactory", 
//                                   "org.netbeans.core.startup.DOMFactoryImpl");

        URL u = XMLUtil.class.getProtectionDomain().getCodeSource().getLocation();
        URL core = new URL(u, "../core/core.jar");
        URLClassLoader loader = new MyLoader(core, XMLUtil.class.getClassLoader());

        Lkp l = (Lkp)Lkp.getDefault();
        l.set(Lookups.singleton(loader));

        // verify the class can be loaded
        Object c = loader.getResource("org/netbeans/core/startup/SAXFactoryImpl.class");
        assertNotNull("Class can be found", c);
    }

    public void testClassLoaderFoundFor68942() throws Exception {
        Object orig = Thread.currentThread().getContextClassLoader();
        String data =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<p>\n" +
                "</p>\n";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = XMLUtil.parse(new InputSource(new StringReader(data)), false, false, null, null);

        XMLUtil.write(doc, baos, "utf-8");
        
        assertEquals("Orig context classloader restored", orig, Thread.currentThread().getContextClassLoader());
        
        Object inst = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        assertNotNull("Instance is there", inst);
        
        MyLoader l = (MyLoader)Lookup.getDefault().lookup(MyLoader.class);
        assertNotNull(l);
        
        Iterator it = l.loadedClasses.iterator();
        while (it.hasNext()) {
            Class c = (Class)it.next();
            if (c.getName().startsWith("org.netbeans.core.startup.DOMFactoryImpl")) {
                // ok
                return;
            }
            if (c.getName().startsWith("org.netbeans.core.startup.SAXFactoryImpl")) {
                // ok
                return;
            }
        }

        fail("Expecting DOMFactoryImpl: " + l.loadedClasses);
    }
    
    public static final class Lkp extends ProxyLookup {
        public Lkp() {
            super(new Lookup[0]);
        }
        
        public void set(Lookup l) {
            setLookups(new Lookup[] { l });
        }
    }
    
    class MyLoader extends URLClassLoader {
        public Set loadedClasses = new HashSet();
        
        public MyLoader(URL u, ClassLoader parent) {
            super(new URL[] { u }, parent);
        }

        protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {

            Class retValue;
            
            retValue = super.loadClass(name, resolve);
            
            loadedClasses.add(retValue);
            return retValue;
        }
        
        
    }
}
