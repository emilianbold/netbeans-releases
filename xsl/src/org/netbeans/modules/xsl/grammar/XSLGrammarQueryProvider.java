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
package org.netbeans.modules.xsl.grammar;

import java.beans.FeatureDescriptor;
import java.util.Enumeration;

import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import org.openide.filesystems.FileObject;
import org.openide.util.enum.EmptyEnumeration;
import org.openide.util.enum.SingletonEnumeration;

import org.netbeans.modules.xml.spi.model.GrammarEnvironment;
import org.netbeans.modules.xml.spi.model.GrammarQuery;
import org.netbeans.modules.xml.spi.model.GrammarQueryManager;

import org.netbeans.modules.xsl.XSLDataObject;

/**
 * Provide DTD grammar. It must be registered at layer.
 *
 * @author  Petr Kuzel <petr.kuzel@sun.com>
 */
public class XSLGrammarQueryProvider extends GrammarQueryManager {
    
    static final String PUBLIC = "!!! find it out";                             // NOI18N
    static final String SYSTEM = "!!! find it out";                             // NOI18N
    static final String NAMESPACE = "http://www.w3.org/1999/XSL/Transform";     // NOI18N
    
    private String prefix = null;
    
    public Enumeration enabled(GrammarEnvironment ctx) {
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
                if (tag.indexOf(":") == -1) {
                    if ("transformation".equals(tag) || "stylesheet".equals(tag)) {
                        String ns = element.getAttribute("xmlns");
                        if (NAMESPACE.equals(ns)) {
                            return new SingletonEnumeration(next);
                        }
                    }
                } else {
                    prefix = tag.substring(0, tag.indexOf(":"));
                    String local = tag.substring(tag.indexOf(":") + 1);
                    if ("transformation".equals(local) || "stylesheet".equals(local)) {
                        String ns = element.getAttribute("xmlns:" + prefix);
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
        return new XSLGrammarQuery(prefix == null ? "xsl" : prefix);
    }
    
}
