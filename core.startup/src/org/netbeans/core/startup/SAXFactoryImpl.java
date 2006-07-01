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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * A special SAXParserFactory that delegates to other factories till it finds
 * one that can satisfy configured requirements.
 *
 * @author Petr Nejedly
 */
public class SAXFactoryImpl extends SAXParserFactory {
    private static Class first;
    
    private Map<String,Boolean> features = new HashMap<String,Boolean>();
    
    /** The default property name according to the JAXP spec */
    private static final String SAXParserFactory_PROP =
        "javax.xml.parsers.SAXParserFactory"; // NOI18N

    public static void install() {
            System.getProperties().put(SAXParserFactory_PROP,
                                   SAXFactoryImpl.class.getName());
    }

    static {
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        // Not app class loader. only ext and bootstrap
        try {
           Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader().getParent());
           first = SAXParserFactory.newInstance().getClass();
        } finally {
           Thread.currentThread().setContextClassLoader(orig);            
        }
        DOMFactoryImpl.install();
        SAXFactoryImpl.install();
    }
    
    public boolean getFeature(String name) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        return features.get(name);
    }

    public javax.xml.parsers.SAXParser newSAXParser() throws ParserConfigurationException, SAXException {
        SAXParser parser = tryCreate();
        return parser;
    }

    public void setFeature(java.lang.String name, boolean value) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        features.put(name, Boolean.valueOf(value));
        tryCreate();
    }

    private SAXParser tryCreate() throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        for (Iterator it = new LazyIterator(first, SAXParserFactory.class, SAXFactoryImpl.class); it.hasNext(); ) {
            try {
                SAXParser parser = tryCreate((Class)it.next());
                return parser;
            } catch (ParserConfigurationException e) {
                if (!it.hasNext()) throw e;
            } catch (SAXNotRecognizedException e) {
                if (!it.hasNext()) throw e;
            } catch (SAXNotSupportedException e) {
                if (!it.hasNext()) throw e;
            } catch (SAXException e) {
                if (!it.hasNext()) throw new ParserConfigurationException();
            }
        }
        throw new IllegalStateException("Can't get here!"); // NOI18N
    }

    private SAXParser tryCreate(Class delClass) throws ParserConfigurationException, SAXException {
        Exception ex = null;
        try {
            SAXParserFactory delegate = (SAXParserFactory)delClass.newInstance();
            delegate.setValidating(isValidating());
            delegate.setNamespaceAware(isNamespaceAware());
            for (Iterator it = features.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry)it.next();
                delegate.setFeature((String)entry.getKey(), ((Boolean)entry.getValue()).booleanValue());
            }
            return delegate.newSAXParser();
        } catch (InstantiationException e) {
            ex = e;
        } catch (IllegalAccessException e) {
            ex = e;
        }
        throw (ParserConfigurationException) new ParserConfigurationException("Broken factory").initCause(ex); // NOI18N
    }
}
