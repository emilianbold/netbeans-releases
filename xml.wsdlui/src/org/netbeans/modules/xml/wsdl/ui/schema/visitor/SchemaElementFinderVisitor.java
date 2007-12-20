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
 * SchemaElementFinderVisitor.java
 *
 * Created on April 10, 2006, 1:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.schema.visitor;

import java.util.Stack;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author radval
 */
public class SchemaElementFinderVisitor extends AbstractXSDVisitor {
    
    private Element mElement;
    
    private String mSuccessorElement;
    
    private Stack mStack = new Stack();
    
    /** Creates a new instance of SchemaElementFinderVisitor */
    public SchemaElementFinderVisitor(String successorElement) {
        this.mSuccessorElement = successorElement;
    }
    
    public Element getSuccessorElement() {
        return mElement;
    }
    
    public void visit(GlobalElement ge) {
        //empty is false meaning this is not top most element where we started this traversal
        //if(!mStack.empty()) {
            if(this.mSuccessorElement.equals(ge.getName())) {
                this.mElement = ge;
                return;
            }
        //}
        
        mStack.push(ge);
        visitTypeContainer(ge);
        mStack.pop();
    }
   
    public void visit(LocalElement le) {
        if(this.mSuccessorElement.equals(le.getName())) {
            this.mElement = le;
            return;
        }
    visitTypeContainer(le);
    }
    
}
