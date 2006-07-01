/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A special DocumentBuilderFactory that delegates to other factories till
 * it finds one that can satisfy configured requirements.
 *
 * @author Petr Nejedly
 */
public class DOMFactoryImpl extends DocumentBuilderFactory {
    private static Class first;

    private Map<String, Object> attributes = new HashMap<String, Object>();
    
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
        
        DOMFactoryImpl.install();
        SAXFactoryImpl.install();
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
            throw (ParserConfigurationException) new ParserConfigurationException(e.toString()).initCause(e);
        }
    }


    public void setAttribute(java.lang.String name, java.lang.Object value) throws java.lang.IllegalArgumentException {
        attributes.put(name, value);
        try {
            tryCreate();
        } catch (ParserConfigurationException e) {
            throw (IllegalArgumentException) new IllegalArgumentException(e.toString()).initCause(e);
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
        throw (ParserConfigurationException) new ParserConfigurationException("Broken factory").initCause(ex); // NOI18N
    }    
}
