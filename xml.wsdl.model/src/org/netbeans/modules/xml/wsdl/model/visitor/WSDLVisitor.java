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
 * WSDLVisitor.java
 *
 * Created on November 15, 2005, 9:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.model.visitor;

import org.netbeans.modules.xml.wsdl.model.*;


/**
 *
 * @author rico
 */
public interface WSDLVisitor {
    void visit(Definitions definition);
    void visit(Types types);
    void visit(Documentation doc);
    void visit(Import importDef);
    void visit(Message message);
    void visit(Part part);
    void visit(PortType portType);
    void visit(OneWayOperation op);
    void visit(RequestResponseOperation op);
    void visit(NotificationOperation op);
    void visit(SolicitResponseOperation op);
    void visit(Input in);
    void visit(Output out);
    void visit(Binding binding);
    void visit(BindingInput bi);
    void visit(BindingOutput bo);
    void visit(BindingOperation bop);
    void visit(BindingFault bf);
    void visit(Service service);
    void visit(Port port);
    void visit(Fault fault);
    void visit(ExtensibilityElement ee);
}
