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

package org.netbeans.modules.xsl.grammar;

import org.w3c.dom.*;
/**
 *
 * @author  asgeir@dimonsoftware.com
 */
public class ResultAttr extends ResultNode implements Attr {
    
    private Attr attr;
    
    /** Creates a new instance of ResultAttr */
    public ResultAttr(Attr peer, String ignorePrefix, String onlyUsePrefix) {
        super(peer, ignorePrefix, onlyUsePrefix);
        attr = peer;
    }
    
    public String getName() {
        return attr.getName();
    }
    
    public Element getOwnerElement() {
        return new ResultElement(attr.getOwnerElement(), ignorePrefix, onlyUsePrefix);
    }
    
    public boolean getSpecified() {
        return attr.getSpecified();
    }
    
    public String getValue() {
        return attr.getValue();
    }
    
    public void setValue(String value) throws DOMException {
        attr.setValue(value);
    }
}
