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

package org.netbeans.modules.web.taglib;
import java.util.Enumeration;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.*;
import org.netbeans.modules.xml.api.model.DTDUtil;
import org.netbeans.api.xml.services.UserCatalog;

/** Taglib Grammar provided code completion for jsptaglibrary specified by XML schema.
 *
 * @author  mk115033
 */
public class TaglibGrammarQueryManager extends org.netbeans.modules.xml.api.model.GrammarQueryManager
{
    private static final String XMLNS_ATTR="xmlns"; //NOI18N
    private static final String TAGLIB_TAG="taglib"; //NOI18N
       
    public java.util.Enumeration enabled(org.netbeans.modules.xml.api.model.GrammarEnvironment ctx) {
        if (ctx.getFileObject() == null) return null;
        Enumeration en = ctx.getDocumentChildren();
        while (en.hasMoreElements()) {
            Node next = (Node) en.nextElement();
            if (next.getNodeType() == next.DOCUMENT_TYPE_NODE) {
                return null; // null for taglibs specified by DTD
            } else if (next.getNodeType() == next.ELEMENT_NODE) {
                Element element = (Element) next;
                String tag = element.getTagName();
                if (TAGLIB_TAG.equals(tag)) {  // NOI18N
                    String xmlns = element.getAttribute(XMLNS_ATTR);
                    if (xmlns!=null && TaglibCatalog.J2EE_NS.equals(xmlns)) //NOI18N
                            return org.openide.util.Enumerations.singleton (next);
                    }
                }
        }
        
        return null;
    }
    
    public java.beans.FeatureDescriptor getDescriptor() {
        return new java.beans.FeatureDescriptor();
    }
    
    /** Returns pseudo DTD for code completion
    */
    public org.netbeans.modules.xml.api.model.GrammarQuery getGrammar(org.netbeans.modules.xml.api.model.GrammarEnvironment ctx) {
        UserCatalog catalog = UserCatalog.getDefault();
        if (catalog != null) {
            EntityResolver resolver = catalog.getEntityResolver();
            if (resolver != null) {
                try {
                    InputSource inputSource = resolver.resolveEntity(TaglibCatalog.TAGLIB_2_0_ID, null);
                    if (inputSource!=null) {
                        return DTDUtil.parseDTD(true, inputSource);
                    }
                } catch(SAXException e) {
                } catch(java.io.IOException e) {
                }
            }
        }
        return null;
    }
    
}
