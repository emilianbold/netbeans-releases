/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
