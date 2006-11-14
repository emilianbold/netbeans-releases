/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.xml.xsd;
import java.util.Enumeration;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.netbeans.modules.xml.api.model.DTDUtil;
import java.io.*;

/** XML Schema Grammar provided code completion for XML Schema file.
 *
 * @author  Milan Kuchtiak
 */
public class SchemaGrammarQueryManager extends org.netbeans.modules.xml.api.model.GrammarQueryManager
{
    // actually code completion works only for xsd: and xs: prefixes
    private static final String SCHEMA="schema"; //NOI18N
    private static final String PUBLIC_JAXB="http://java.sun.com/xml/ns/jaxb"; //NOI18N
    
    private String prefix, ns_jaxb;
       
    public java.util.Enumeration enabled(org.netbeans.modules.xml.api.model.GrammarEnvironment ctx) {
        if (ctx.getFileObject() == null) return null;
        Enumeration en = ctx.getDocumentChildren();
        prefix=null;
        ns_jaxb=null;
        while (en.hasMoreElements()) {
            Node next = (Node) en.nextElement();
            if (next.getNodeType() == next.DOCUMENT_TYPE_NODE) {
                return null; // null for XML Documents specified by DTD
            } else if (next.getNodeType() == next.ELEMENT_NODE) {
                Element element = (Element) next;
                String tagName = element.getTagName();
                if (tagName.endsWith(":"+SCHEMA)) { //NOI18N
                    prefix = tagName.substring(0,tagName.indexOf(":"+SCHEMA));
                } else if (tagName.equals(SCHEMA)) {
                    prefix = "";
                }
                if (prefix==null) return null;
                NamedNodeMap map = element.getAttributes();
                for (int i=0; i<map.getLength();i++) {
                    Attr attr = (Attr)map.item(i);
                    String name = attr.getName();
                    if (PUBLIC_JAXB.equals(attr.getValue())) {
                        if (name.startsWith("xmlns:")) ns_jaxb=name.substring(6); //NOI18N
                    }
                }
                return org.openide.util.Enumerations.singleton (next);
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
        if (prefix==null) return null;
        InputSource inputSource = null;
        StringBuffer buffer=new StringBuffer(512);
        if (prefix.length()==0) {
            buffer.append("<!ENTITY % p ''><!ENTITY % s ''>"); //NOI18N
        } else {
            buffer.append("<!ENTITY % p '"+prefix+":'><!ENTITY % s ':"+prefix+"'>"); //NOI18N
        }
        java.io.InputStream is = null;
        if (ns_jaxb==null) {
            is = getClass().getResourceAsStream("/org/netbeans/modules/xml/schema/resources/XMLSchema.dtd"); //NOI18N
        } else {
            is = getClass().getResourceAsStream("/org/netbeans/modules/xml/schema/resources/XMLSchema_jaxb.dtd"); //NOI18N
            buffer.append("<!ENTITY % jaxb '"+ns_jaxb+"'>"); //NOI18N
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        try {
            while ((line=br.readLine())!=null) {
                buffer.append(line);
            }
            br.close();
        } catch (IOException ex) {
            return null;
        }
        inputSource = new InputSource(new StringReader(buffer.toString()));
        if (ns_jaxb==null)
            inputSource.setSystemId("nbres:/org/netbeans/modules/xml/schema/resources/XMLSchema.dtd"); //NOI18N
        else
            inputSource.setSystemId("nbres:/org/netbeans/modules/xml/schema/resources/XMLSchema_jaxb.dtd"); //NOI18N
        if (inputSource!=null) {
            return DTDUtil.parseDTD(true, inputSource);
        }
        return null;
    }    
}
