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
package org.netbeans.modules.xml.wsdl.model.extensions.soap.impl;

import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;
import org.netbeans.modules.xml.xam.ComponentUpdater.Query;

/**
 *
 * @author Nam Nguyen
 */
public class SOAPComponentUpdater implements ComponentUpdater<SOAPComponent>, Query<SOAPComponent>, SOAPComponent.Visitor {
    private SOAPComponent parent;
    private Operation operation;
    private boolean canAdd;
    
    /** Creates a new instance of SOAPComponentUpdater */
    public SOAPComponentUpdater() {
    }
    
    public boolean canAdd(SOAPComponent target, Component child) {
        if (!(child instanceof SOAPComponent)) return false;
        update(target, (SOAPComponent) child, null);
        return canAdd;
    }

    public void update(SOAPComponent target, SOAPComponent child, Operation operation) {
        update(target, child, -1, operation);
    }

    
    public void update(SOAPComponent target, SOAPComponent child, int index, Operation operation) {
        parent = target;
        this.operation = operation;
        child.accept(this);
    }

    public void visit(SOAPOperation child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(SOAPBinding child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(SOAPHeader child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(SOAPBody child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(SOAPFault child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(SOAPHeaderFault child) {
        SOAPHeader target = (SOAPHeader) parent;
        if (operation == Operation.ADD) {
            target.addSOAPHeaderFault(child);
        } else if (operation == Operation.REMOVE) {
            target.removeSOAPHeaderFault(child);
        } else if (operation == null) {
            canAdd = true;
        }
    }

    public void visit(SOAPAddress child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }
    
}
