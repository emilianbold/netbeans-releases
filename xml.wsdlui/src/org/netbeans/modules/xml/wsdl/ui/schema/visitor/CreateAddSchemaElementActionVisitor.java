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
 * CreateAddSchemaElementActionVisitor.java
 *
 * Created on April 10, 2006, 5:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.schema.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.TypeContainer;


/**
 *
 * @author radval
 */
public class CreateAddSchemaElementActionVisitor extends AbstractXSDVisitor {
    
    private List<Element> mElements = new ArrayList<Element>();
    
    private Stack<Element> mStack = new Stack<Element>();
    
    /** Creates a new instance of CreateAddSchemaElementActionVisitor */
    public CreateAddSchemaElementActionVisitor() {
    }
    
    public List<Element> getElements() {
        return this.mElements;
    }
    
    public void visit(GlobalElement ge) {
        createAction(ge);
        
        
    }
   
    public void visit(LocalElement le) {
         createAction(le);
    }
    
    private void createAction(Element e) {
        //empty is false meaning this is not top most element where we started this traversal
        //and there we are only one level down. (ie looking element inside element)
        if(!mStack.empty() && mStack.size() == 1) {
            this.mElements.add(e);
        } else {
            mStack.push(e);
            visitTypeContainer((TypeContainer) e);
            mStack.pop();
        }
    }
}
