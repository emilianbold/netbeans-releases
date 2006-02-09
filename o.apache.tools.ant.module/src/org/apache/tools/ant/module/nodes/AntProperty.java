/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.nodes;

import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;

class AntProperty extends Node.Property {
    
    private Element el;
    private String name;
    
    public AntProperty(Element el, String name) {
        this(name);
        this.el = el;
    }
    
    protected AntProperty(String name) {
        super(String.class);
        setName(name);
        this.name = name;
    }
    
    protected Element getElement() {
        return el;
    }
    
    public Object getValue() {
        Element el = getElement();
        if (el == null) { // #9675
            return NbBundle.getMessage(AntProperty.class, "LBL_property_invalid_no_element");
        }
        return el.getAttribute(name);
    }
    
    public boolean canRead() {
        return true;
    }
    
    public boolean canWrite() {
        return false;
    }
    
    public void setValue(Object val) throws IllegalArgumentException{
        throw new IllegalArgumentException();
    }
    
}
