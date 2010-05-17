/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.wsdlextensions.jms.refactoring;

import java.util.List;
import org.netbeans.modules.wsdlextensions.jms.JMSAddress;
import org.netbeans.modules.wsdlextensions.jms.JMSBinding;
import org.netbeans.modules.wsdlextensions.jms.JMSComponent;
import org.netbeans.modules.wsdlextensions.jms.JMSJCAOptions;
import org.netbeans.modules.wsdlextensions.jms.JMSJNDIEnv;
import org.netbeans.modules.wsdlextensions.jms.JMSJNDIEnvEntry;
import org.netbeans.modules.wsdlextensions.jms.JMSMapMessage;
import org.netbeans.modules.wsdlextensions.jms.JMSMapMessagePart;
import org.netbeans.modules.wsdlextensions.jms.JMSMessage;
import org.netbeans.modules.wsdlextensions.jms.JMSOperation;
import org.netbeans.modules.wsdlextensions.jms.JMSOption;
import org.netbeans.modules.wsdlextensions.jms.JMSOptions;
import org.netbeans.modules.wsdlextensions.jms.JMSProperties;
import org.netbeans.modules.wsdlextensions.jms.JMSProperty;
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
public class JMSReferenceFinderVisitor extends ChildVisitor implements WSDLVisitor, JMSComponent.Visitor {

    private Referenceable referenced;
    private List<Component> components;

    public JMSReferenceFinderVisitor(Referenceable referenced, List<Component> components) {
        this.referenced = referenced;
        this.components = components;
    }

    @Override
    public void visit(ExtensibilityElement ee) {
        if (ee instanceof JMSMapMessagePart) {
            visit((JMSMapMessagePart) ee);
        } else if (ee instanceof JMSMessage) {
            visit((JMSMessage) ee);
        } else if (ee instanceof JMSProperty) {
            visit((JMSProperty) ee);
        }
        visitComponent(ee);
    }


    public void visit(JMSMessage target) {
        if (referenced instanceof Part) {
            Part part = (Part) referenced;
            String partName = part.getName();
            if (partName.equals(target.getBytesPart()) || partName.equals(target.getCorrelationIdPart()) ||
                    partName.equals(target.getDeliveryModePart()) || partName.equals(target.getMessageIDPart()) ||
                    partName.equals(target.getPriorityPart()) || partName.equals(target.getRedeliveredPart()) ||
                    partName.equals(target.getTimestampPart()) || partName.equals(target.getTextPart()) ||
                    partName.equals(target.getTypePart())) {
                 check(target, part);
            }
        }
    }
    
    private void check(JMSComponent target, Part part) {
        WSDLComponent parent = target.getParent();
        while(parent != null) {
            if (parent instanceof ExtensibilityElement) {
                parent = parent.getParent();
            } else {
                break;
            }
        }

        if (parent != null) {
            NamedComponentReference<Message> messageRef = null;
            if (parent instanceof BindingInput) {
                Reference<Input> input = ((BindingInput) parent).getInput();
                if (input != null && input.get() != null) {
                    messageRef = input.get().getMessage();
                }
            } else if (parent instanceof BindingOutput) {
                Reference<Output> output = ((BindingOutput) parent).getOutput();
                if (output != null && output.get() != null) {
                    messageRef = output.get().getMessage();
                }
            }

            if (messageRef != null && messageRef.get() != null) {
                if (messageRef.get().equals(part.getParent())) {
                    components.add(target);
                }
            }
        }
    }
    
    public void visit(JMSProperty target) {
        if (referenced instanceof Part) {
            if (((Part)referenced).getName().equals(target.getPart())) {
                check(target, (Part) referenced);
            }
        }
    }
    
    public void visit(JMSMapMessagePart target) {
        if (referenced instanceof Part) {
            if (((Part)referenced).getName().equals(target.getPart())) {
                check(target, (Part) referenced);
            }
        }
    }

    public void visit(JMSAddress target) {
        //no refactoring
    }

    public void visit(JMSBinding target) {
        //no refactoring
    }

    public void visit(JMSOperation target) {
        //no refactoring
    }

    public void visit(JMSJNDIEnv target) {
        //no refactoring
    }

    public void visit(JMSJNDIEnvEntry target) {
        //no refactoring
    }

    public void visit(JMSJCAOptions target) {
        //no refactoring
    }

    public void visit(JMSOptions target) {
        //no refactoring
    }

    public void visit(JMSOption target) {
        //no refactoring
    }

    public void visit(JMSProperties target) {
        //no refactoring
    }

    public void visit(JMSMapMessage target) {
        //no refactoring
    }


}
