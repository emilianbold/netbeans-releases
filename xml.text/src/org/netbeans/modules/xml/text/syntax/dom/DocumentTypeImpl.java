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
import org.netbeans.editor.*;

/**
 * It should envolve in DocumentType implementation.
 *
 * @author Petr Kuzel
 */
public class DocumentTypeImpl extends SyntaxNode implements DocumentType {
        
    DocumentTypeImpl(XMLSyntaxSupport syntax, TokenItem first, int to) {
        super (syntax, first, to);
    }
    
    public short getNodeType() {
        return Node.DOCUMENT_TYPE_NODE;
    }
    
    public Node getPreviousSibling() {
        return null;  //??? we may miss comment or PI
    }

    //!!! skip internal DTD
    public Node getNextSibling() {
        SyntaxElement next = getNext();

        // skip internal DTD
        while (next != null && next instanceof Declaration);

        if (next instanceof SyntaxNode) return (Node) next;
        if (next != null) return findNext(next);
        return null;
    }        
    
    public String getPublicId() {
        return null;  //!!! parse for it
    }
    
    public org.w3c.dom.NamedNodeMap getNotations() {
        return NamedNodeMapImpl.EMPTY;
    }
    
    public String getName() {
        return null;  //!!! parse for it
    }
    
    public org.w3c.dom.NamedNodeMap getEntities() {
        return NamedNodeMapImpl.EMPTY;
    }
    
    public String getSystemId() {
        return null;  //!!! parse for it
    }
    
    public String getInternalSubset() {
        return null;
    }
    
}

