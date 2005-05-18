/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xsd;
import java.util.Enumeration;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.*;
import org.netbeans.modules.xml.dtd.grammar.DTDParser;
import org.netbeans.api.xml.services.UserCatalog;

/** XML Schema Grammar provided code completion for XML Schema file.
 *
 * @author  mk115033
 */
public class SchemaGrammarQueryManager extends org.netbeans.modules.xml.api.model.GrammarQueryManager
{
    // actually code completion works only for xsd: and xs: prefixes
    private static final String SCHEMA_ROOT_XSD="xsd:schema"; //NOI18N
    private static final String SCHEMA_ROOT_XS="xs:schema"; //NOI18N
    private String prefix;
       
    public java.util.Enumeration enabled(org.netbeans.modules.xml.api.model.GrammarEnvironment ctx) {
        if (ctx.getFileObject() == null) return null;
        Enumeration en = ctx.getDocumentChildren();
        while (en.hasMoreElements()) {
            Node next = (Node) en.nextElement();
            if (next.getNodeType() == next.DOCUMENT_TYPE_NODE) {
                return null; // null for XML Documents specified by DTD
            } else if (next.getNodeType() == next.ELEMENT_NODE) {
                Element element = (Element) next;
                String tagName = element.getTagName();
                if (SCHEMA_ROOT_XSD.equals(tagName)) {  // NOI18N
                    prefix = SCHEMA_ROOT_XSD;
                    return org.openide.util.Enumerations.singleton (next);
                } else if (SCHEMA_ROOT_XS.equals(tagName)) {  // NOI18N
                    prefix = SCHEMA_ROOT_XS;
                    return org.openide.util.Enumerations.singleton (next);
                }
                
            }
        }
        
        return null;
    }
    
    public java.beans.FeatureDescriptor getDescriptor() {
        return new java.beans.FeatureDescriptor();
    }
    
    /** Returns DTD for code completion
    */
    public org.netbeans.modules.xml.api.model.GrammarQuery getGrammar(org.netbeans.modules.xml.api.model.GrammarEnvironment ctx) {
        InputSource inputSource = null;
        if (SCHEMA_ROOT_XSD.equals(prefix))
            inputSource = new InputSource("nbres:/org/netbeans/modules/xml/schema/resources/XMLSchema_xsd.dtd"); //NOI18N
        else if (SCHEMA_ROOT_XS.equals(prefix))
            inputSource = new InputSource("nbres:/org/netbeans/modules/xml/schema/resources/XMLSchema_xs.dtd"); //NOI18N
        
        if (inputSource!=null) {
            DTDParser dtdParser = new DTDParser(true);
            return dtdParser.parse(inputSource);
        }
        return null;
    }
    
}
