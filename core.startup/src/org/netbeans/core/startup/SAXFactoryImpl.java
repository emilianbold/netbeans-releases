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
import org.xml.sax.*;

/**
 * A special SAXParserFactory that delegates to other factories till it finds
 * one that can satisfy configured requirements.
 *
 * @author Petr Nejedly
 */
public class SAXFactoryImpl extends SAXParserFactory {
    private static Class first;
    
    private Map features = new HashMap();
    
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
    }
    
    public boolean getFeature(java.lang.String name) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        return ((Boolean)features.get(name)).booleanValue();
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
        
        ParserConfigurationException pce = new ParserConfigurationException("Broken factory"); // NOI18N
        ErrorManager.getDefault().annotate(pce, ex);
        throw pce;
    }
}
