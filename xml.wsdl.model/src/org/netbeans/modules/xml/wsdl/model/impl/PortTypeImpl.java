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

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author nn136682
 */
public class PortTypeImpl extends NamedImpl implements PortType {
    
    /** Creates a new instance of PortTypeImpl */
    public PortTypeImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    public PortTypeImpl(WSDLModel model) {
        this(model, createNewElement(WSDLQNames.PORTTYPE.getQName(), model));
    }

    public Collection<Operation> getOperations() {
        return getChildren(Operation.class);
    }

    public void removeOperation(Operation operation) {
        removeChild(OPERATION_PROPERTY, operation);
    }

    public void addOperation(Operation operation) {
        appendChild(OPERATION_PROPERTY, operation);
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
}
