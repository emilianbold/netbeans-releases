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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.wsdl.ui.schema.visitor.AbstractXSDVisitor;


public class MixedContentFinderVisitor extends AbstractXSDVisitor {
    
    private boolean isMixed = false;
    private Element element;
    
    public MixedContentFinderVisitor(Element element) {
        this.element = element;
    }

    public boolean hasMixedContent() {
        return isMixed;
    }

    
    @Override
    public void visit(GlobalElement ge) {
        if (ge != element) {
            return;
        }
        super.visit(ge);
    }

    @Override
    public void visit(LocalElement le) {
        if (element != le) {
            return;
        }
        super.visit(le);
    }

    @Override
    public void visit(GlobalComplexType gct) {
        if (gct.isMixed() != null && gct.isMixed().booleanValue()) {
            isMixed = true;
        }
            
        super.visit(gct);

    }

    @Override
    public void visit(LocalComplexType type) {
        if (type.isMixed() != null && type.isMixed().booleanValue()){
            isMixed = true;
        }
        super.visit(type);
        
    }

    @Override
    public void visit(ComplexContent cc) {
        if (cc.isMixed() != null && cc.isMixed().booleanValue()){
            isMixed = true;
        }
        super.visit(cc);
    }

    
    
    
    
    

}
