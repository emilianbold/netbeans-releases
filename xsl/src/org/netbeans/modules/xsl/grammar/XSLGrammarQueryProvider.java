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
package org.netbeans.modules.xsl.grammar;

import java.beans.FeatureDescriptor;
import java.util.Enumeration;
import org.netbeans.modules.xml.api.model.GrammarEnvironment;
import org.netbeans.modules.xml.api.model.GrammarQuery;
import org.netbeans.modules.xml.api.model.GrammarQueryManager;
import org.openide.filesystems.FileObject;
import org.openide.util.enum.EmptyEnumeration;
import org.openide.util.enum.SingletonEnumeration;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import org.openide.filesystems.FileObject;
import org.openide.util.enum.EmptyEnumeration;
import org.openide.util.enum.SingletonEnumeration;

import org.netbeans.modules.xsl.XSLDataObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Provide DTD grammar. It must be registered at layer.
 *
 * @author  Petr Kuzel
 */
public class XSLGrammarQueryProvider extends GrammarQueryManager {
    
    static final String PUBLIC = "!!! find it out";                             // NOI18N
    static final String SYSTEM = "!!! find it out";                             // NOI18N
    static final String NAMESPACE = XSLGrammarQuery.XSLT_NAMESPACE_URI;
    
    private String prefix = null;
    
    public Enumeration enabled(GrammarEnvironment ctx) {

        if (ctx.getFileObject() == null) return null;
        
        prefix = null;
        Enumeration en = ctx.getDocumentChildren();
        while (en.hasMoreElements()) {
            Node next = (Node) en.nextElement();
            if (next.getNodeType() == next.DOCUMENT_TYPE_NODE) {
//                DocumentType doctype = (DocumentType) next;
//                if (PUBLIC.equals(doctype.getPublicId()) || SYSTEM.equals(doctype.getSystemId())) {
//                    return new SingletonEnumeration(next);
//                }
            } else if (next.getNodeType() == next.ELEMENT_NODE) {
                Element element = (Element) next;
                String tag = element.getTagName();
                if (tag.indexOf(":") == -1) {  // NOI18N
                    if ("transformation".equals(tag) || "stylesheet".equals(tag)) { // NOI18N
                        String ns = element.getAttribute("xmlns"); // NOI18N
                        if (NAMESPACE.equals(ns)) {
                            return new SingletonEnumeration(next);
                        }
                    }
                } else {
                    prefix = tag.substring(0, tag.indexOf(":"));  // NOI18N
                    String local = tag.substring(tag.indexOf(":") + 1); // NOI18N
                    if ("transformation".equals(local) || "stylesheet".equals(local)) { // NOI18N
                        String ns = element.getAttribute("xmlns:" + prefix); // NOI18N
                        if (NAMESPACE.equals(ns)) {
                            return new SingletonEnumeration(next);
                        }
                    }
                }
            }
        }
        
        // try mime type
        FileObject fo = ctx.getFileObject();
        if (fo != null) {
            if (XSLDataObject.MIME_TYPE.equals(fo.getMIMEType())) {
                // almost forever, until client uses its own invalidation
                // rules based e.g. on new node detection at root level
                // or MIME type listening
                return EmptyEnumeration.EMPTY;
            }
        }
        
        return null;
    }    
    
    public FeatureDescriptor getDescriptor() {
        return new FeatureDescriptor();
    }
    
    public GrammarQuery getGrammar(GrammarEnvironment input) {
        try {
            FileObject fo = input.getFileObject();
            if (fo == null) throw new IllegalStateException("GrammarEnvironment has changed between enabled() and getGrammar()!"); // NOI18N     // NOI18N
            DataObject dataObj = DataObject.find(fo);
            return new XSLGrammarQuery(dataObj);
            
        } catch (DataObjectNotFoundException e) {
            throw new IllegalStateException("Missing DataObject " + e.getFileObject().getPath() + "!"); // NOI18N
        }
    }
    
}
