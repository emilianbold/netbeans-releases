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

/*
 * SchemaAttributeTypeFinderVisitor.java
 *
 * Created on April 14, 2006, 7:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.schema.visitor;

import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author radval
 */
public class SchemaAttributeTypeFinderVisitor extends AbstractXSDVisitor {
    
    private SimpleType mGst;
    
    /** Creates a new instance of SchemaAttributeTypeFinderVisitor */
    public SchemaAttributeTypeFinderVisitor() {
    }
    
    public SimpleType getSimpleType() {
        return this.mGst;
    }
    
    public void visit(LocalAttribute la) {
        NamedComponentReference<GlobalSimpleType> gstRef = la.getType();
        if(gstRef != null && gstRef.get() != null) {
            this.mGst = gstRef.get();
        } else {
            mGst = la.getInlineType();
        }
    }
    
    public void visit(GlobalAttribute ga) {
        NamedComponentReference<GlobalSimpleType> gstRef = ga.getType();
        if(gstRef != null && gstRef.get() != null) {
            this.mGst = gstRef.get();
        } else {
            mGst = ga.getInlineType();
        }
        
    }
    
    
}
