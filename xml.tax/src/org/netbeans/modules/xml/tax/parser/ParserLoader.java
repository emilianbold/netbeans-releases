/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tax.parser;

import org.openide.util.Lookup;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * A filtering classloader (isolator) ensuring that a particuler version of
 * Xerces2 parser is used. The filtering rule is inlined to perform well. 
 * <p>
 * If the rule match it loads data from isolated resource otherwise delegates
 * to a parent (TopManager.getSystemClassLoader()).
 * <p>
 * Use getInstance() followed by loadClass() method for obtaining the parser.
 * 
 * @author  Petr Kuzel
 * @version 
 */
public final class ParserLoader extends URLClassLoader {
    
    // filtering "rules"
    private static final String PARSER_PACKAGE = "org.apache.xerces";  // NOI18N
    private static final String USER_PREFIXES[] = new String[] {
        "org.netbeans.tax.io.XNIBuilder", // NOI18N
        "org.netbeans.modules.xml.tools.action.XMLCompiler" // NOI18N
    };

    // parser library relative to module directory
    // library itself can not have a jar extension 
    // to avoid loading it by module classloader    
    private static final String PARSER_MODULES_LIB = "/modules/autoload/ext/xerces2.jar"; // NOI18N
    
    // delegating classloader
    private ClassLoader parentLoader;

    // the only instance of this classloader
    private static ParserLoader instance = null;
    
    /** Creates new ParserLoader */
    private ParserLoader(URL library) {
        super(new URL[] { library });
        parentLoader = (ClassLoader) Lookup.getDefault().lookup(ClassLoader.class);
    }

    /**
     * Bootstrapping method.
     * @return ParserLoader or null if library can not be located
     */
    public static synchronized ParserLoader getInstance() {
                        
        if (instance != null) return instance;
        
        try {
            String prop;
            URL xer2url;

            prop = System.getProperty("netbeans.user"); // NOI18N
            xer2url = new URL("file:" + prop + PARSER_MODULES_LIB); // NOI18N

            try {
                // just skip netbeans home
                xer2url.openStream();
            } catch (IOException ex) {
                prop = System.getProperty("netbeans.home"); // NOI18N
                xer2url = new URL("file:" + prop + PARSER_MODULES_LIB); // NOI18N
            }

            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Isolated jar URL=" + xer2url); // NOI18N

            instance = new ParserLoader(xer2url);
//              URL module = instance.getClass().getProtectionDomain().getCodeSource().getLocation();            
            URL module = org.netbeans.tax.io.XNIBuilder.class.getProtectionDomain().getCodeSource().getLocation();

            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Isolated module URL=" + module); // NOI18N

            instance.addURL(module);
        } catch (MalformedURLException ex) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug (ex);
        }
               
        return instance;
    }
    
    /**
     * Use simple filtering rule for distingvishing
     * of classes that must be loaded from particular
     * library version.
     * ASSUMPTION parentLoader see bootstrap resources
     */
    public Class loadClass(String name) throws ClassNotFoundException {
        Class clazz = null;
        
        if (name.startsWith(PARSER_PACKAGE)) {
                
            clazz = super.findLoadedClass(name);  //no class duplication allowed
            if (clazz == null) {
                clazz = super.findClass(name);    //define new class
            }
            
        } else {
            
            // all potential users of this class loader (using isolated classes) must
            // be known in compile time to eliminate multiple loaded classes by
            // multiple instance of this classloader
            
            for (int i = 0; i<USER_PREFIXES.length; i++) {
                if (name.startsWith(USER_PREFIXES[i])) {
           
                    synchronized (this) {                    
                        clazz = super.findLoadedClass(name);  //no class duplication allowed
                        if (clazz == null) {
                            clazz = super.findClass(name);    //define new class
                        }
                    }
                }
            }
            
            // delegate to parent
            
            if (clazz == null) {
                clazz = parentLoader.loadClass(name);
            }
        }
        
        return clazz;
    }

    
    
    /*
     * Prefer isolated library when looking for resource.
     * ASSUMPTION parentLoader see bootstrap resources
     */
    public URL getResource(String name) {
        URL in = super.getResource(name);
        if (in == null) {
            in = parentLoader.getResource(name);
        }

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Resource: " + name + " =>" + in); // NOI18N

        return in;
    }

    /*
     * Prefer isolated library when looking for resource stream.
     * ASSUMPTION parentLoader see bootstrap resources
     */
    public InputStream getResourceAsStream(String name) {
        try {
            URL url = this.getResource(name);
            if (url == null) {
                return null;
            } else {
                return url.openStream();
            }
        } catch (IOException ex) {
            return null;
        }
    }

    /*
     * Prefer isolated library when looking for resources.
     * It is defacto implementation of getResources() that is final.
     * //!!! this inplemetation does not isolate bootstrap resources
     */
    public Enumeration findResources(String name) throws IOException {
        Enumeration en1 = super.findResources(name);
        Enumeration en2 = parentLoader.getResources(name);

        return new org.openide.util.enum.SequenceEnumeration(en1, en2);
    }

    /**
     * Perform basic self test.
     */
    public static void main(String args[]) throws Exception {
        
        ParserLoader me = ParserLoader.getInstance();
        
        Class apache = me.loadClass("org.apache.xerces.util.QName"); // NOI18N
        Class java = me.loadClass("java.lang.String"); // NOI18N
        Class netbeans = me.loadClass("org.openide.util.Mutex"); // NOI18N

        System.err.println("apache " + apache.getClassLoader()); // NOI18N
        System.err.println("netbeans " + netbeans.getClassLoader()); // NOI18N
        System.err.println("java " + java.getClassLoader()); // NOI18N
        
    }
    
}
