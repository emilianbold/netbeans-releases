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
import org.netbeans.modules.xml.spi.dom.*;
import org.netbeans.editor.*;

public class EmptyTag extends Tag {
    
    public EmptyTag(XMLSyntaxSupport support, TokenItem from, int to, String name, Collection attribs) {
        super( support, from, to, name, attribs );
    }
    
    public boolean hasChildNodes() {
        return false;
    }
    
    public NodeList getChildNodes() {
        return NodeListImpl.EMPTY;
    }
    
    protected Tag getEndTag() {
        return this;
    }
    
    protected Tag getStartTag() {
        return this;
    }
    
    public String toString() {
        StringBuffer ret = new StringBuffer( "EmptyTag(\"" + name + "\" " );
        ret.append(getAttributes().toString());
        return ret.toString() + " " + first + ")";
    }
    
}

