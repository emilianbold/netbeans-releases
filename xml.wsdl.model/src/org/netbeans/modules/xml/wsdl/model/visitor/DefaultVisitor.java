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

/*
 * DefaultVisitor.java
 *
 * Created on November 17, 2005, 9:59 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.model.visitor;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.NotificationOperation;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 *
 * @author nn136682
 */
public class DefaultVisitor implements WSDLVisitor {
    
    /** Creates a new instance of DefaultVisitor */
    public DefaultVisitor() {
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Types types) {
        visitComponent(types);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Port port) {
        visitComponent(port);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Definitions definition) {
        visitComponent(definition);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.BindingInput bi) {
        visitComponent(bi);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.BindingOutput bo) {
        visitComponent(bo);
    }

    public void visit(OneWayOperation op) {
        visitComponent(op);
    }

    public void visit(RequestResponseOperation op) {
        visitComponent(op);
    }
    
    public void visit(NotificationOperation op) {
        visitComponent(op);
    }

    public void visit(SolicitResponseOperation op) {
        visitComponent(op);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Part part) {
        visitComponent(part);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Documentation doc) {
        visitComponent(doc);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.BindingOperation bop) {
        visitComponent(bop);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Binding binding) {
        visitComponent(binding);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Message message) {
        visitComponent(message);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Service service) {
        visitComponent(service);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.BindingFault bf) {
        visitComponent(bf);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Import importDef) {
        visitComponent(importDef);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Output out) {
        visitComponent(out);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.PortType portType) {
        visitComponent(portType);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Input in) {
        visitComponent(in);
    }
    
    public void visit(org.netbeans.modules.xml.wsdl.model.Fault fault) {
        visitComponent(fault);
    }
    
    public void visit(ExtensibilityElement ee) {
        visitComponent(ee);
    }
    
    protected void visitComponent(WSDLComponent component) {
    }
}
