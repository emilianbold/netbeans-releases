/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.xml.text.syntax.dom;

import java.util.*;

import org.w3c.dom.*;
import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.modules.xml.spi.dom.*;
import org.netbeans.editor.*;

/**
 * End element of ELEMENT_NODE.
 * //??? should it be implementing Node?
 */
public class EndTag extends Tag {

    public EndTag(XMLSyntaxSupport support, TokenItem from, int to, String name) {
        super( support, from, to, name, null );
        this.name = name;
    }

    /**
     * Create properly bound attributes
     */
    public synchronized org.w3c.dom.NamedNodeMap getAttributes() {
        Tag start = getStartTag();
        if (start != null) {
            return start.getAttributes();
        } else {
            return NamedNodeMapImpl.EMPTY;
        }
    }
    
    public boolean hasChildNodes() {
        SyntaxElement prev = getPrevious();
        if (prev == null) return false;
        if (prev instanceof EndTag && ((EndTag)prev).getStartTag() == null) return false;
        if (prev instanceof StartTag) return false;
        return true;
    }
    
    public NodeList getChildNodes() {
        
        List list = new ArrayList();
        Node prev = hasChildNodes() ? findPrevious(this) : null;
        
        while (prev != null) {
            list.add(0, prev);
            prev = prev.getPreviousSibling();
        }
        
        return new NodeListImpl(list);
    }
    
    protected Tag getStartTag() {
        
        SyntaxNode prev = findPrevious();
        
        while (prev != null) {
            if (prev instanceof StartTag) {
                // check well-formedness
                StartTag startTag = (StartTag) prev;
                if (startTag.getNodeName().equals(getNodeName())) {
                    return startTag;
                } else {
                    return null;
                }
            } else if (prev instanceof EndTag) {
                EndTag endTag = (EndTag) prev;
                prev = endTag.getStartTag();
                if (prev == null) return null;
                prev = prev.findPrevious();
            } else {
                prev = prev.findPrevious();
            }
        }
        
        return null;
    }
    
    protected Tag getEndTag() {
        return this;
    }
    
    public String toString() {
        return "EndTag(\"" + name + "\") " + first;
    }
    
}

