/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package test.dwarfclassview;

import com.sun.org.apache.xerces.internal.dom.ElementImpl;
import test.dwarfclassview.consts.KIND;
import test.dwarfclassview.consts.NodeATTR;

public class ClassViewElement extends ElementImpl {

    public ClassViewElement(ClassViewDocument document) {
        super(document, "node"); // NOI18N
    }
    
    public void setAttribute(NodeATTR attr, String value) {
        setAttribute(attr.value(), value);
    }

    public void setKind(KIND kind) {
        if (kind != null) {
            setAttribute(NodeATTR.TYPE.value(), kind.value());
        }
    }

    void setName(String name) {
        setAttribute(NodeATTR.NAME.value(), name);
    }


    String getAttribute(NodeATTR attr) {
        return getAttribute(attr.value());
    }

    public boolean equals(final ClassViewElement elem) {
        boolean result = attributeEluals(elem, NodeATTR.TYPE);
        result &= attributeEluals(elem, NodeATTR.QNAME);
        result &= attributeEluals(elem, NodeATTR.PARAMS);
        return result;
    }    
    
    private boolean attributeEluals(ClassViewElement elem, NodeATTR attr) {
        String val1 = getAttribute(attr);
        String val2 = elem.getAttribute(attr);
        return (val1.equals("") || val2.equals("") || val1.equals(val2)); // NOI18N
    }
  
}
