/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wsdlextensions.jdbc.refactoring;

import java.util.List;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCAddress;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCBinding;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCComponent;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCOperation;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCOperationInput;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCOperationOutput;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.visitor.ChildVisitor;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author skini
 */
public class JDBCReferenceFinderVisitor extends ChildVisitor implements WSDLVisitor, JDBCComponent.Visitor {

    private Referenceable referenced;
    private List<Component> components;

    public JDBCReferenceFinderVisitor(Referenceable referenced, List<Component> components) {
        this.referenced = referenced;
        this.components = components;
    }

    @Override
    public void visit(ExtensibilityElement ee) {
        if (ee instanceof JDBCOperationOutput) {
            visit((JDBCOperationOutput) ee);
        }
        visitComponent(ee);
    }

    public void visit(JDBCAddress target) {
    }

    public void visit(JDBCBinding target) {
    }

    public void visit(JDBCOperation target) {
    }

    public void visit(JDBCOperationInput target) {
    }

    public void visit(JDBCOperationOutput target) {
        //Uncomment whole thing, since returnPartName seems to return -1.
        //Dont know whether this is related to wsdl part name or not.
//        if (referenced instanceof Part) {
//            Part part = (Part) referenced;
//            
//            if (part.getName().equals(target.getReturnPartName())) {
//                WSDLComponent parent = target.getParent();
//
//                if (parent != null) {
//                    NamedComponentReference<Message> messageRef = null;
//                    if (parent instanceof BindingInput) {
//                        Reference<Input> input = ((BindingInput) parent).getInput();
//                        if (input != null && input.get() != null) {
//                            messageRef = input.get().getMessage();
//                        }
//                    } else if (parent instanceof BindingOutput) {
//                        Reference<Output> output = ((BindingOutput) parent).getOutput();
//                        if (output != null && output.get() != null) {
//                            messageRef = output.get().getMessage();
//                        }
//                    }
//
//                    if (messageRef != null && messageRef.get() != null) {
//                        if (messageRef.get().equals(part.getParent())) {
//                            components.add(target);
//                        }
//                    }
//                }
//            }
//        }
    }

}
