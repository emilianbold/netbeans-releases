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

