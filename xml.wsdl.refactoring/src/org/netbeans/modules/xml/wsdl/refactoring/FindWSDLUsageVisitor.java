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
package org.netbeans.modules.xml.wsdl.refactoring;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.refactoring.UsageGroup;
import org.netbeans.modules.xml.refactoring.spi.RefactoringEngine;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.visitor.ChildVisitor;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.ErrorManager;

/**
 *
 * @author Nam Nguyen
 */
public class FindWSDLUsageVisitor extends ChildVisitor {
    
    private Referenceable<WSDLComponent> referenced;
    private UsageGroup usage;
    private List<UsageGroup> usages;
    
    /** Creates a new instance of FindWSDLUsageVisitor */
    public FindWSDLUsageVisitor() {
    }
    
    public List<UsageGroup> findUsages(ReferenceableWSDLComponent referenced,
            Definitions wsdl, RefactoringEngine engine) {
        this.referenced = referenced;
        usage = new UsageGroup(engine, wsdl.getModel(), referenced);
        usages = new ArrayList<UsageGroup>();
        wsdl.accept(this);
        if (!usage.isEmpty()) {
            usages.add(usage);
        }
        return usages;
    }
    
    private <T extends ReferenceableWSDLComponent> void check(NamedComponentReference<T> ref, Component referencing) {
        if (ref == null || ! (referenced instanceof ReferenceableWSDLComponent)) {
            return;
        }
        
        try {
            if (ref.references(ref.getType().cast(referenced))) {
                usage.addItem(referencing);
            }
        } catch(Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            usage.addError(referencing, e.getMessage());
        }
    }
    
    private <T extends Referenceable<WSDLComponent>> void check(Reference<T> ref, Component referencing) {
        if (ref == null) return;
        try {
            if (ref.references(ref.getType().cast(referenced))) {
                usage.addItem(referencing);
            }
        } catch(Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            usage.addError(referencing, e.getMessage());
        }
    }
    
    private void check(OperationParameter referencing) {
        check(referencing.getMessage(), referencing);
    }
    
    public void visit(BindingOperation component) {
        check(component.getOperation(), component);
        super.visit(component);
    }
    
    public void visit(Input oparam) {
        check(oparam);
        super.visit(oparam);
    }
    
    public void visit(Output oparam) {
        check(oparam);
        super.visit(oparam);
    }
    
    public void visit(Fault oparam) {
        check(oparam);
        super.visit(oparam);
    }
    
    public void visit(Port port) {
        check(port.getBinding(), port);
        super.visit(port);
    }
    
    public void visit(BindingInput component) {
        check(component.getInput(), component);
        super.visit(component);
    }
    
    public void visit(BindingOutput component) {
        check(component.getOutput(), component);
        super.visit(component);
    }
    
    public void visit(BindingFault component) {
        check(component.getFault(), component);
        super.visit(component);
    }
    
    public void visit(Binding component) {
        check(component.getType(), component); 
        super.visit(component);
    }
}


