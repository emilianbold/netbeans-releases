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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.ui.schema.visitor;

import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.Attribute.Use;

public class OptionalAttributeFinderVisitor extends AbstractXSDVisitor {
    private boolean isOptional = false;
    private String mAttrName = null;
    public OptionalAttributeFinderVisitor(String attrName) {
        mAttrName = attrName;
    }
    @Override
    public void visit(AttributeReference reference) {
        if (reference.getRef() != null && reference.getRef().get() != null) {
            if (reference.getRef().get().getName().equals(mAttrName)) {
                isOptional = reference.getUse() == null || reference.getUse().equals(Use.OPTIONAL);
            }
        }
        
    }
    
    @Override
    public void visit(LocalAttribute la) {
        if (la.getName().equals(mAttrName)) {
            isOptional = la.getUse() == null || la.getUse().equals(Use.OPTIONAL);
        }
    }
    
    public boolean isOptional () {
        return isOptional;
    }
}
