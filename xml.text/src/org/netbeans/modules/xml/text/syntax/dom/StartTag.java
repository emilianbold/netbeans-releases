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

import java.util.*;

import org.w3c.dom.*;
import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.editor.*;

public class StartTag extends Tag {
    
    public StartTag(XMLSyntaxSupport support, TokenItem from, int to, String name, Collection attribs) {
        super( support, from, to, name, attribs );
    }
    
    public boolean hasChildNodes() {
        SyntaxElement next = getNext();
        if (next == null) return false;
        // if not well-formed
        if (next instanceof StartTag && ((StartTag)next).getEndTag() == null) return false;
        if (next instanceof EndTag) return false;
        return true;
    }
    
    public NodeList getChildNodes() {
        
        List list = new ArrayList();
        Node next = hasChildNodes() ? findNext(this) : null;
        
        while (next != null) {
            list.add(next);
            next = next.getNextSibling();
        }
        
        return new NodeListImpl(list);
    }
    
    protected Tag getStartTag() {
        return this;
    }
    
    protected Tag getEndTag() {
        
        SyntaxNode next = findNext();
        
        while (next != null) {
            if (next instanceof EndTag) {
                // check well-formedness
                EndTag endTag = (EndTag) next;
                if (endTag.getTagName().equals(getTagName())) {
                    return endTag;
                } else {
                    return null;
                }
            } else if (next instanceof StartTag) {
                Tag startTag = (Tag) next;
                next = startTag.getEndTag();
                if (next == null) return null;
                next = next.findNext();
            } else {
                next = next.findNext();
            }
        }
        
        return null;
    }
    
    public String toString() {
        StringBuffer ret = new StringBuffer( "StartTag(\"" + name + "\" " );
        ret.append(getAttributes().toString());
        return ret.toString() + " " + first + ")";
    }
    
}

