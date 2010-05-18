/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.xml.wsdl.refactoring;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
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
import org.netbeans.modules.xml.wsdl.refactoring.spi.WSDLExtensibilityElementRefactoringSupport;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.ErrorManager;
import org.openide.util.Lookup;

/**
 *
 * @author Nam Nguyen
 */
public class FindWSDLUsageVisitor extends ChildVisitor implements WSDLVisitor {
    
    private ReferenceableWSDLComponent referenced;
    private List<WSDLRefactoringElement> elements;
    private Definitions wsdl;
    
    /** Creates a new instance of FindWSDLUsageVisitor */
    public FindWSDLUsageVisitor() {
    }
    
       
    public List<WSDLRefactoringElement> findUsages(ReferenceableWSDLComponent referenced,Definitions wsdl) {
        this.referenced = referenced;
        this.wsdl = wsdl;
        elements = new ArrayList<WSDLRefactoringElement>();
        wsdl.accept(this);
        return elements;
    }
    
    private <T extends ReferenceableWSDLComponent> void check(NamedComponentReference<T> ref, Component referencing) {
        if (ref == null || ! ref.getType().isAssignableFrom(referenced.getClass())) {
            return;
        }
        
        try {
            if (ref.references(ref.getType().cast(referenced))) {
                elements.add(new WSDLRefactoringElement(referencing.getModel(), (Referenceable)referenced, referencing));
            }
        } catch(Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            
        }
    }
    
    private <T extends ReferenceableWSDLComponent> void check(Reference<T> ref, Component referencing) {
        if (ref == null || ! ref.getType().isAssignableFrom(referenced.getClass())) {
            return;
        }
        try {
            if (ref.references(ref.getType().cast(referenced))) {
                elements.add(new WSDLRefactoringElement(referencing.getModel(), referenced, referencing));
            }
        } catch(Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            
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
        } else {
            QName qname = ee.getQName();
            if (qname != null && qname.getNamespaceURI() != null) {
                for (WSDLExtensibilityElementRefactoringSupport support : Lookup.getDefault().lookupAll(WSDLExtensibilityElementRefactoringSupport.class)) {
                    if (support.getNamespace().equals(qname.getNamespaceURI())) {
                        List<Component> components= new ArrayList<Component>();
                        ee.accept(support.getReferenceFinderVisitor(referenced, components));
                        for (Component component : components) {
                            elements.add(new WSDLRefactoringElement(component.getModel(), (Referenceable)referenced, component));
                        }
                    }
                }
            }
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


