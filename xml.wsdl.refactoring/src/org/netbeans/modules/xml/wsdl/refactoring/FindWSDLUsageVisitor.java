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
import org.netbeans.modules.xml.wsdl.model.Documentation;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.visitor.ChildVisitor;
import org.openide.ErrorManager;

/**
 *
 * @author Nam Nguyen
 */
public class FindWSDLUsageVisitor extends ChildVisitor {
    
    private ReferenceableWSDLComponent referenced;
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
    
    public void visit(BindingOperation bo) {
        try {
            if (referenced instanceof Operation) {
                Operation op = (Operation) referenced;
                if (op.getName().equals(bo.getOperation().get())) {
                    usage.addItem(bo);
                }
            }
        } catch(Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            usage.addError(bo, e.getMessage());
        }
        super.visit(bo);
    }

    private void check(OperationParameter oparam) {
        //TODO NamedComponentReference<Message> getMessage
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
        //TODO getBinding
        super.visit(port);
    }

    public void visit(BindingInput bi) {
        //TODO getInput
        super.visit(bi);
    }

    public void visit(BindingOutput bo) {
        //TODO getOutput
        super.visit(bo);
    }

    public void visit(BindingFault bf) {
        //TODO getFault()
        super.visit(bf);
    }

    public void visit(Binding binding) {
        //TODO NamedComponentReference<PortType> getType();
        super.visit(binding);
    }

}
