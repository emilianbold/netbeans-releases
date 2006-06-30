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

package org.netbeans.modules.xml.wsdl.model.visitor;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.ReferenceableExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.xam.Referenceable;

/**
 *
 * @author Nam Nguyen
 */
public class FindReferencedVisitor<T extends ReferenceableWSDLComponent> extends DefaultVisitor {
    private Class<T> type;
    private String localName;
    private T referenced = null;
    private Definitions root;
    
    /** Creates a new instance of FindReferencedVisitor */
    public FindReferencedVisitor(Definitions root) {
        this.root = root;
    }
    
    public T find(String localName, Class<T> type) {
        this.type = type;
        this.localName = localName;
        visitChildren(root);
        return referenced;
    }

    public void visit(Binding c) {
        checkReference(c, true); //extensible
    }

    public void visit(Message c) { //extensible
        checkReference(c, true);
    }

    public void visit(PortType c) { //extensible
        checkReference(c, true);
    }

    public void visit(ExtensibilityElement c) {
        if (c instanceof Referenceable) {
            checkReference(ReferenceableExtensibilityElement.class.cast(c), true);
        }
        visitChildren(c);
    }

    private void checkReference(ReferenceableWSDLComponent c, boolean checkChildren) {
         if (type.isAssignableFrom(c.getClass()) && c.getName().equals(localName)) {
             referenced = type.cast(c);
             return;
         } else if (checkChildren) {
             visitChildren(c);
         }
    }
    
    private void visitChildren(WSDLComponent c) {
        for (WSDLComponent child : c.getChildren()) {
           if (referenced != null) { // before start each visit
                return;
           }
            child.accept(this);
        }
    }
    
}
