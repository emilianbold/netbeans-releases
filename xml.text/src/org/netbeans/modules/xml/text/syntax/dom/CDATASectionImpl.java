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
 * CDATA section representation.
 */
public class CDATASectionImpl extends TextImpl {

    /**
     * Create content text node.
     */
    public CDATASectionImpl(XMLSyntaxSupport support, TokenItem from, int to) {
        super( support, from, to );
    }
    
    /**
     * Create attribute text node.
     */
    CDATASectionImpl(XMLSyntaxSupport syntax, TokenItem from, AttrImpl parent) {
        super( syntax, from, parent);
    }
    
}

