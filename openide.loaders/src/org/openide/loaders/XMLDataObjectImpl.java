/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import java.io.IOException;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.openide.loaders.RuntimeCatalog;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Class that hide implementations details of deprecated utility
 * methods provided at XMLDataObject.
 *
 * @author  Petr Kuzel
 */
class XMLDataObjectImpl extends Object {


    /** Create DOM builder using JAXP libraries. */
    static DocumentBuilder makeBuilder(boolean validate) throws IOException, SAXException {
        
        DocumentBuilder builder;
        DocumentBuilderFactory factory;

        //create factory according to javax.xml.parsers.SAXParserFactory property 
        //or platform default (i.e. com.sun...)
        try {
            factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(validate);
            factory.setNamespaceAware(false);
        } catch (FactoryConfigurationError err) {
            notifyFactoryErr(err, "javax.xml.parsers.DocumentBuilderFactory"); //NOI18N
            throw err;
        }

        try {
            builder = factory.newDocumentBuilder();                
        } catch (ParserConfigurationException ex) {
            SAXException sex = new SAXException("Configuration exception."); // NOI18N
            ErrorManager emgr = ErrorManager.getDefault();
            emgr.annotate(sex, ex);
            emgr.annotate(sex, "Can not create a DOM builder!\nCheck javax.xml.parsers.DocumentBuilderFactory property and the builder library presence on classpath."); // NOI18N
            throw sex;
        }
        
        return builder;
    }
    
    static Parser makeParser(boolean validate) {
        
        try {
            return new org.xml.sax.helpers.XMLReaderAdapter (XMLUtil.createXMLReader(validate));
        } catch (SAXException ex) {
            notifyNewSAXParserEx(ex);
            return null;
        }
        
    }

    /** Return XML reader or null if no provider exists. */
    static XMLReader makeXMLReader(boolean validating, boolean namespaces) {

        try {
            return XMLUtil.createXMLReader(validating,namespaces);
        } catch (SAXException ex) {
            notifyNewSAXParserEx(ex);
            return null;
        }
        
    }
    
    /** Annotate & notify the exception. */
    private static void notifyNewSAXParserEx (Exception ex) {
        ErrorManager emgr = ErrorManager.getDefault();
        emgr.annotate(ex, "Can not create a SAX parser!\nCheck javax.xml.parsers.SAXParserFactory property features and the parser library presence on classpath."); // NOI18N
        emgr.notify(ex);
    }

    /** Annotate & notify the error. */
    private static void notifyFactoryErr(Error err, String property) {
        ErrorManager emgr = ErrorManager.getDefault();
        emgr.annotate(err, "Can not create a factory!\nCheck " + property + "  property and the factory library presence on classpath."); // NOI18N
        emgr.notify(err);
    }

    // warning back compatability code!!!    
    static synchronized void registerCatalogEntry(String publicId, String uri) {
        Iterator it = Lookup.getDefault().lookupAll(EntityCatalog.class).iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof RuntimeCatalog) {
                ((RuntimeCatalog) o).registerCatalogEntry(publicId, uri);
                return;
            }
        }
        assert false;
    }
    
}
