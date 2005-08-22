/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.text.syntax.dom;

import org.w3c.dom.*;
import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.modules.xml.spi.dom.*;
import org.netbeans.editor.*;

/**
 * It should envolve in DocumentType implementation.
 *
 * @author Petr Kuzel
 */
public class DocumentTypeImpl extends SyntaxNode implements DocumentType, XMLTokenIDs {
        
    public DocumentTypeImpl(XMLSyntaxSupport syntax, TokenItem first, int to) {
        super (syntax, first, to);
    }
    
    public short getNodeType() {
        return Node.DOCUMENT_TYPE_NODE;
    }
        
    public String getPublicId() {
        String doctype = first.getImage();
        if (doctype.indexOf("PUBLIC") != -1) {                                  // NOI18N
            TokenItem next = first.getNext();
            if (next != null && next.getTokenID() == VALUE) {
                String publicId = next.getImage();
                return publicId.substring(1, publicId.length() - 1);
            }
        }
        return null;
    }
    
    public org.w3c.dom.NamedNodeMap getNotations() {
        return NamedNodeMapImpl.EMPTY;
    }
    
    public String getName() {
        //<!DOCTYPE id ...
        String docType = first.getImage();
        int idIndex = docType.indexOf(' ');
        if(idIndex > 0) {
            int idEndIndex = docType.indexOf(' ', idIndex + 1);
            if(idEndIndex > 0 && idEndIndex > idIndex) {
                return docType.substring(idIndex + 1, idEndIndex);
            }
        }
        return null;
    }
    
    public org.w3c.dom.NamedNodeMap getEntities() {
        return NamedNodeMapImpl.EMPTY;
    }
    
    public String getSystemId() {
        String doctype = first.getImage();
        if (doctype.indexOf("PUBLIC") != -1) {                                  // NOI18N
            TokenItem next = first.getNext();
            if (next != null && next.getTokenID() == VALUE) {
                next = next.getNext();
                if (next == null) return null;
                next = next.getNext();
                if (next != null && next.getTokenID() == VALUE) {
                    String system = next.getImage();
                    return system.substring(1, system.length() -1);
                }
            }
        } else if (doctype.indexOf("SYSTEM") != -1) {                           // NOI18N
            TokenItem next = first.getNext();
            if (next != null && next.getTokenID() == VALUE) {
                String system = next.getImage();
                return system.substring(1, system.length() - 1);
            }
        }        
        return null;
    }
    
    public String getInternalSubset() {
        return null;
    }
    
}

