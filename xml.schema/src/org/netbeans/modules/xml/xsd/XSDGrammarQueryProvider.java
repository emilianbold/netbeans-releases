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

package org.netbeans.modules.xml.xsd;

import java.beans.FeatureDescriptor;
import java.util.Enumeration;
import java.util.WeakHashMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.enum.SingletonEnumeration;
import org.netbeans.modules.xml.api.model.*;
import org.netbeans.api.xml.services.UserCatalog;

/**
 * Provide XSD grammar. It must be registered at layer.
 *
 * @author  Ales Novak <ales.novak@sun.com>
 */
public class XSDGrammarQueryProvider extends GrammarQueryManager {

    private static final String XMLNS_ATTR = "xmlns"; //NOI18N
    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance"; //NOI18N
    private static final String XSI_LOCATION = "schemaLocation"; //NOI18N
    private static final String XSI_NO_NAMESPACE_LOCATION = "noNamespaceSchemaLocation"; //NOI18N
    
    private static final WeakHashMap schemas = new WeakHashMap();
 
    public XSDGrammarQueryProvider() {
    }
    
    public Enumeration enabled(GrammarEnvironment ctx) {
        
        if (ctx.getFileObject() == null) {
            return null;
        }
        
        Enumeration en = ctx.getDocumentChildren();
        while (en.hasMoreElements()) {
            Node next = (Node) en.nextElement();
            if (next.getNodeType() == next.ELEMENT_NODE) {
                org.w3c.dom.Element element = (org.w3c.dom.Element) next;
                String xmlns = element.getAttribute(XMLNS_ATTR);
                if (xmlns != null)  { //NOI18N
                    String xsi = findXSINamespace(element);
                    String schema = element.getAttribute(xsi + ":" + XSI_LOCATION);
                    if (schema == null) {
                        schema = element.getAttribute(xsi + ":" + XSI_NO_NAMESPACE_LOCATION);
                        if (schema == null) {
                            // bail out
                            ErrorManager.getDefault().log(ErrorManager.WARNING, "SCHEMA is null: " + xmlns + " " + element.getLocalName());
                            continue;
                        }
                    }

                    // remember this schema
                    schemas.put(ctx.getFileObject(), schema);
                    
                    return new SingletonEnumeration(next);
                }
	    }
	}

	return null;
    }
    
    public FeatureDescriptor getDescriptor() {
        return new FeatureDescriptor();
    }
    
    public GrammarQuery getGrammar(GrammarEnvironment env) {
        try {
            InputSource inputSource = findSchema(env.getFileObject());
            if (inputSource != null) {
               return new XSDParser().parse(inputSource);
            }
        } catch (java.io.IOException e) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.EXCEPTION, e);
        }

        return null;
    }

    /**
     * @param fileObject of type FileObject
     * @return InputSource for corresponding schema of this file
     */
    private static InputSource findSchema(FileObject fileObject) throws java.io.IOException {

        assert fileObject != null;

        String schema = (String) schemas.remove(fileObject);
        int idx = schema.indexOf(' ');
        if (idx >= 0) {
            schema = schema.substring(idx + 1);
        }
        
        // first try std way
        try {
            UserCatalog catalog = UserCatalog.getDefault();
            if (catalog != null) {
                EntityResolver resolver = catalog.getEntityResolver();
                if (resolver != null) {
                    InputSource inputSource = resolver.resolveEntity(schema, null);
                    if (inputSource != null) {
                        return inputSource;
                    }
                }
            }
        } catch (org.xml.sax.SAXException e) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.EXCEPTION, e);
        }
        
        // try an URL first
        try {
            java.net.URL url = new java.net.URL(schema);
            return new InputSource(url.openStream());
        } catch (java.net.MalformedURLException e) { // sort of expected
            // debug only
            // ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "URL not found: " + schema);
        }
        
        // try files
        FileObject fo = fileObject.getParent().getFileObject(schema);

        if (fo == null) {
            // debug only
            // ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "File not found: " + schema);
            return null;
        }

        return new InputSource(fo.getInputStream());
    }
    
    private static final String findXSINamespace(org.w3c.dom.Element element) {
        /*
        org.w3c.dom.NamedNodeMap map = element.getAttributes();
        for (int i = 0; i < map.getLength(); i++) {
            org.w3c.dom.Node attr = map.item(i);
            if (attr.getNodeValue().compareToIgnoreCase(XSI_NAMESPACE) == 0) {
                return attr.getLocalName().substring(XMLNS_ATTR.length());
            }
        }
        */
        return "xsi";
    }
}
