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
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
import org.netbeans.modules.xml.wsdl.model.visitor.ChildVisitor;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.ErrorManager;

/**
 *
 * @author Nam Nguyen
 */
public class FindWSDLUsageVisitor extends ChildVisitor implements WSDLVisitor {
    
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
    
    private <T extends ReferenceableWSDLComponent> void check(NamedComponentReference<T> ref, Component referencing) {
        if (ref == null || ! ref.getType().isAssignableFrom(referenced.getClass())) {
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
    
    private <T extends ReferenceableWSDLComponent> void check(Reference<T> ref, Component referencing) {
        if (ref == null || ! ref.getType().isAssignableFrom(referenced.getClass())) {
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
    
    public void visit(ExtensibilityElement ee) {
        if (ee instanceof SOAPComponent) {
            ((SOAPComponent) ee).accept(new SOAPVisitor());
        }
        super.visit(ee);
    }
    
    // SOAPComponent.Visitor
    class SOAPVisitor implements SOAPComponent.Visitor {
        public void visit(SOAPFault component) {
            check(component.getFault(), component);
            visitChildren(component);
        }

        public void visit(SOAPHeader component) {
            if (referenced instanceof Message) {
                check(component.getMessage(), component);
            } else if (referenced instanceof Part) {
                check(component.getPartRef(), component);
            }
            visitChildren(component);
        }
        
        public void visit(SOAPHeaderFault component) {
            if (referenced instanceof Message) {
                check(component.getMessage(), component);
            } else if (referenced instanceof Part) {
                check(component.getPartRef(), component);
            }
            visitChildren(component);
        }
        
        public void visit(SOAPOperation component) {
            //no references
            visitChildren(component);
        }
        
        public void visit(SOAPBinding component) {
            //no references
            visitChildren(component);
        }
        
        public void visit(SOAPBody component) {
            if (component.getParts() != null) {
                for (Reference<Part> ref : component.getPartRefs()) {
                    check(ref, component);
                }
            }
            visitChildren(component);
        }
        
        public void visit(SOAPAddress component) {
            //no references
            visitChildren(component);
        }
        
        protected void visitChildren(SOAPComponent component) {
            for (SOAPComponent c : component.getChildren(SOAPComponent.class)) {
                c.accept(this);
            }
        }
    }
}


