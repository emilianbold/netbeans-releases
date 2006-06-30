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

    @Override
    public Object getValue() {
        Element el = getElement();
        if (el == null) { // #9675
            return NbBundle.getMessage(AntProperty.class, "LBL_property_invalid_no_element");
        }
        return el.getAttribute(name);
    }
    
    @Override
    public boolean canRead() {
        return true;
    }
    
    @Override
    public boolean canWrite() {
        return false;
    }
    
    @Override
    public void setValue(Object val) throws IllegalArgumentException{
        throw new IllegalArgumentException();
    }
    
}
