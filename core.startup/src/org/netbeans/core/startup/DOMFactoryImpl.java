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

package org.netbeans.core.startup;

import java.util.*;
import javax.xml.parsers.*;
import org.openide.*;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;

/**
 * A special DocumentBuilderFactory that delegates to other factories till
 * it finds one that can satisfy configured requirements.
 *
 * @author Petr Nejedly
 */
public class DOMFactoryImpl extends DocumentBuilderFactory {
    private static Class first;
    
    private Map attributes = new HashMap();
    
    /** The default property name according to the JAXP spec */
    private static final String Factory_PROP =
        "javax.xml.parsers.DocumentBuilderFactory"; // NOI18N

    public static void install() {
        System.getProperties().put(Factory_PROP,
                                   DOMFactoryImpl.class.getName());
    }

    static {
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        // Not app class loader. only ext and bootstrap
        try {
           Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader().getParent());
           first = DocumentBuilderFactory.newInstance().getClass();
        } finally {
           Thread.currentThread().setContextClassLoader(orig);            
        }
    }
    
    public java.lang.Object getAttribute(java.lang.String name) throws java.lang.IllegalArgumentException {
        return attributes.get(name);
    }
    
    public boolean getFeature (String name) {
        return Boolean.TRUE.equals (getAttribute (name));
    }
    
    public void setFeature(String name, boolean value) throws ParserConfigurationException {
        try {
            setAttribute (name, Boolean.valueOf (value));
        } catch (IllegalArgumentException ex) {
            ParserConfigurationException p = new ParserConfigurationException ();
            p.initCause (ex);
            throw p;
        }
    }
    
    

    public DocumentBuilder newDocumentBuilder() throws javax.xml.parsers.ParserConfigurationException {
        try {
            return tryCreate();
        } catch (IllegalArgumentException e) {
            ParserConfigurationException pce = new ParserConfigurationException(); // NOI18N
            ErrorManager.getDefault().annotate(pce, e);
            throw pce;
        }
    }


    public void setAttribute(java.lang.String name, java.lang.Object value) throws java.lang.IllegalArgumentException {
        attributes.put(name, value);
        try {
            tryCreate();
        } catch (ParserConfigurationException e) {
            IllegalArgumentException iae = new IllegalArgumentException(); // NOI18N
            ErrorManager.getDefault().annotate(iae, e);
            throw iae;
        }
    }
    
    private DocumentBuilder tryCreate() throws ParserConfigurationException, IllegalArgumentException {
        for (Iterator it = new LazyIterator(first, DocumentBuilderFactory.class, DOMFactoryImpl.class); it.hasNext(); ) {
            try {
                DocumentBuilder builder = tryCreate((Class)it.next());
                return builder;
            } catch (ParserConfigurationException e) {
                if (!it.hasNext()) throw e;
            } catch (IllegalArgumentException e) {
                if (!it.hasNext()) throw e;
            }
        }
        throw new IllegalStateException("Can't get here!"); // NOI18N
    }

    private DocumentBuilder tryCreate(Class delClass) throws ParserConfigurationException, IllegalArgumentException {
        Exception ex = null;
        try {
            DocumentBuilderFactory delegate = (DocumentBuilderFactory)delClass.newInstance();
            delegate.setNamespaceAware(isNamespaceAware());
            delegate.setValidating(isValidating());
            delegate.setIgnoringElementContentWhitespace(isIgnoringElementContentWhitespace());
            delegate.setExpandEntityReferences(isExpandEntityReferences());
            delegate.setIgnoringComments(isIgnoringComments());
            delegate.setCoalescing(isCoalescing());

            for (Iterator it = attributes.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry)it.next();
                delegate.setAttribute((String)entry.getKey(), entry.getValue());
            }
            return delegate.newDocumentBuilder();
        } catch (InstantiationException e) {
            ex = e;
        } catch (IllegalAccessException e) {
            ex = e;
        }
        
        ParserConfigurationException pce = new ParserConfigurationException("Broken factory"); // NOI18N
        ErrorManager.getDefault().annotate(pce, ex);
        throw pce;
    }    
}
