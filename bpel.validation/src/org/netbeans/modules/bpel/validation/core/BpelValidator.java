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
 * License. When distributing the software, include this License Header
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.bpel.validation.core;

import java.util.Collection;
import java.util.List;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.ui.XAMUtils;

import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SequenceDefinition;

import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.validation.core.Validator;

import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.CreateInstanceActivity;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.SimpleBpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.02.15
 */
public abstract class BpelValidator extends Validator {

    protected abstract SimpleBpelModelVisitor getVisitor();

    public synchronized ValidationResult validate(Model m, Validation validation, ValidationType type) {
        if (!(m instanceof BpelModel)) {
            return null;
        }
        BpelModel model = (BpelModel) m;

        if (model.getState() == Model.State.NOT_WELL_FORMED) {
            return null;
        }
        final Process process = model.getProcess();

        if (process == null) {
            return null;
        }
        init(validation, type);

        Runnable run = new Runnable() {
            public void run() {
                startTime();
                process.accept(getVisitor());
                endTime(getDisplayName());
            }
        };
        model.invoke(run);

        return createValidationResult(model);
    }

    protected final boolean isCreateInstanceYes(CreateInstanceActivity activity) {
        return activity != null && activity.getCreateInstance() == TBoolean.YES;
    }

    protected final CreateInstanceActivity getCreateInstanceActivity(Component component) {
        if (component instanceof CreateInstanceActivity) {
            return (CreateInstanceActivity) component;
        }
        if (component.getParent() instanceof CreateInstanceActivity) {
            return (CreateInstanceActivity) component.getParent();
        }
        return null;
    }

    protected final Component getVariableType(Variable variable) {
        Component type = getVariableDeclarationType(variable);

        if (type instanceof Message) {
            Collection<Part> parts = ((Message) type).getParts();

            if (parts != null && parts.size() == 1) {
                type = getType(parts.iterator().next());
            }
        }
        return XAMUtils.getBasedSimpleType(checkComplexType(checkTypeOfElement(checkElement(type))));
    }

    protected final Component getPartType(Part part) {
        Component type = getType(part);
        return XAMUtils.getBasedSimpleType(checkComplexType(checkTypeOfElement(checkElement(type))));
    }

    private Component checkElement(Component component) {
//out();
//out("CHECK E: " + component);
        if (component == null) {
            return null;
        }
//out("      E: " + component + " " + component.getClass().getName());
        if (component instanceof SimpleType) {
            return component;
        }
        if (component instanceof ComplexType) {
            return checkComplexType(component);
        }
        Component type = getTypeOfElement(component);
//out("   type: " + type);

        if (type != null && type instanceof GlobalSimpleType) {
//out("       return 1: ");
            return type;
        }
        List children = component.getChildren();

        if (children == null || children.size() != 1) {
            return component;
        }
//out("  children: " + children);
        Object object = children.get(0);
//out("  object: " + object);

        if (!(object instanceof Component)) {
            return component;
        }
        return checkComplexType((Component) object);
    }

    private Component checkComplexType(Component component) {
        Component type = getComplexType(component);

        if (type == null) {
            return component;
        }
        return type;
    }

    private Component getComplexType(Component component) {
//out("CHECK T: " + component);
        if (!(component instanceof ComplexType)) {
            return null;
        }
        List children = component.getChildren();
//out("  children: " + children);

        if (children == null || children.size() != 1) {
            return null;
        }
        Object object = children.get(0);
//out("  object: " + object);

        if (!(object instanceof Sequence)) {
            return null;
        }
        Component sequenceType = getSequenceType((Sequence) object);
//out("  sequenceType: " + sequenceType);

        if (sequenceType == null) {
            return null;
        }
        return sequenceType;
    }

    private Component getSequenceType(Sequence sequence) {
//out("CHECK S: " + sequence);
        List<SequenceDefinition> content = sequence.getContent();

        if (content == null || content.size() == 0) {
            return null;
        }
        Component type = null;

        for (SequenceDefinition component : content) {
//out("    see: " + component);
            type = checkElement(component);
//out("       : " + type);

            if ( !(type instanceof GlobalSimpleType)) {
                return null;
            }
        }
        return type;
    }

    protected final Component getVariableDeclarationType(VariableDeclaration declaration) {
        if (declaration == null) {
            return null;
        }
        // message type
        WSDLReference<Message> wsdlRef = declaration.getMessageType();

        if (wsdlRef != null) {
            Message message = wsdlRef.get();

            // # 130764
            if (message != null) {
                return message;
            }
        }
        // element
        SchemaReference<GlobalElement> elementRef = declaration.getElement();

        if (elementRef != null) {
            GlobalElement element = elementRef.get();

            if (element != null) {
                return element;
            }
        }
        // type
        SchemaReference<GlobalType> typeRef = declaration.getType();

        if (typeRef != null) {
            GlobalType type = typeRef.get();

            if (type != null) {
                return type;
            }
        }
        return null;
    }
}
