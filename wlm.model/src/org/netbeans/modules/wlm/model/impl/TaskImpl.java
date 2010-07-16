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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.model.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.wlm.model.api.TAction;
import org.netbeans.modules.wlm.model.api.TAssignment;
import org.netbeans.modules.wlm.model.api.TEscalation;
import org.netbeans.modules.wlm.model.api.TImport;
import org.netbeans.modules.wlm.model.api.TInit;
import org.netbeans.modules.wlm.model.api.TKeywords;
import org.netbeans.modules.wlm.model.api.TNotification;
import org.netbeans.modules.wlm.model.api.TPriority;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.TTimeout;
import org.netbeans.modules.wlm.model.api.TTitle;
import org.netbeans.modules.wlm.model.api.VariableDeclaration;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMComponentFactory;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.api.WLMVisitor;
import org.netbeans.modules.wlm.model.api.WSDLReference;
import org.netbeans.modules.wlm.model.spi.OperationReference;
import org.netbeans.modules.wlm.model.spi.PortTypeReference;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

public class TaskImpl extends WLMComponentBase implements TTask {

    private VariableDeclaration mInputVariable;
    private VariableDeclaration mOutputVariable;

    public TaskImpl(WLMModel model, Element e) {
        super(model, e);
    }

    public void addImport(TImport toAdd) {
        if (!contains(toAdd)) {
            addAfter(IMPORT_TYPE_PROPERTY, toAdd, IMPORT_POSITION);
        }
    }

    public Collection<TImport> getImports() {
        return getChildren(TImport.class);
    }

    public void removeImport(TImport toRemove) {
        removeChild(IMPORT_TYPE_PROPERTY, toRemove);
    }

    public String getTargetNamespace() {
        return getAttribute(WLMAttribute.TARGET_NAME_SPACE);
    }

    public void setTargetNamespace(String value) {
        setAttribute(TARGET_NAMESPACE_PROPERTY,
                WLMAttribute.TARGET_NAME_SPACE, value);
    }

    public TTitle getTitle() {
        return getChild(TTitle.class);
    }

    public void setTitle(TTitle value) {
        setChild(TTitle.class, TITLE_PROPERTY, value,
                TITLE_POSITON);
    }
    
    public void removeTitle(TTitle title) {
        removeChild(TITLE_PROPERTY, title);
    }

    public void removePriority(TPriority priority) {
        removeChild(PRIORITY_PROPERTY, priority);
    }
    
    public TPriority getPriority() {
        return getChild(TPriority.class);
    }

    public void setPriority(TPriority value) {
        setChild(TPriority.class, PRIORITY_PROPERTY, value,
                PRIORITY_POSITION);
    }

    public void addEscalation(TEscalation escalation) {
        addAfter(ESCALATION_PROPERTY, escalation, ESCALATION_POSITION);
    }

    public void addNotification(TNotification notification) {
        addAfter(NOTIFICATION_PROPERTY, notification, NOTIFICATION_POSITION);
    }

    public void addTimeOut(TTimeout timeout) {
        addAfter(TIMEOUT_PROPERTY, timeout, TIMEOUT_POSITION);
    }

    public TInit getInit() {
        return getChild(TInit.class);
    }

    public TAssignment getAssignment() {
        return getChild(TAssignment.class);
    }

    public List<TEscalation> getEscalations() {
        return getChildren(TEscalation.class);
    }

    public List<TNotification> getNotifications() {
        return getChildren(TNotification.class);
    }

    public boolean hasActions() {
        return (getChild(TAction.class) != null);
    }

    public boolean hasEscalations() {
        return (getChild(TEscalation.class) != null);
    }

    public boolean hasTimeouts() {
        return (getChild(TTimeout.class) != null);
    }

    public boolean hasNotifications() {
        return (getChild(TNotification.class) != null);
    }

    public boolean hasImports() {
        return (getChild(TImport.class) != null);
    }

    public WSDLReference<PortType> getPortType() {
        String ptStr = getPortTypeAsString();
        if (ptStr == null || ptStr.length() == 0) {
            return null;
        }
        //
        PortTypeReference ptRef = new PortTypeReference(this, ptStr);
        return ptRef;
    }

    public String getPortTypeAsString() {
        String ptStr = getAttribute(WLMAttribute.PORT_TYPE);
        return ptStr;
    }

    public WSDLReference<Operation> getOperation() {
        String ptStr = getPortTypeAsString();
        if (ptStr == null || ptStr.length() == 0) {
            return null;
        }
        //
        String oprStr = getOperationAsString();
        if (oprStr == null || oprStr.length() == 0) {
            return null;
        }
        OperationReference optRef = new OperationReference(this, oprStr);
        return optRef;
    }

    public List<TTimeout> getTimeouts() {
        return getChildren(TTimeout.class);
    }

    public void removeInit(TInit value) {
        removeChild(INIT_PROPERTY, value);
    }

    public void removeAssignment(TAssignment value) {
        removeChild(ASSIGNMENT_PROPERTY, value);
    }

    public void removeEscalation(TEscalation escalation) {
        removeChild(ESCALATION_PROPERTY, escalation);
    }

    public void removeNotification(TNotification notification) {
        removeChild(NOTIFICATION_PROPERTY, notification);
    }

    public void removeTimeOut(TTimeout timeout) {
        removeChild(TIMEOUT_PROPERTY, timeout);
    }

    public void setInit(TInit value) {
        setChild(TInit.class, INIT_PROPERTY, value, INIT_POSITION);
    }

    public void setAssignment(TAssignment value) {
        setChild(TAssignment.class, ASSIGNMENT_PROPERTY, value, 
                ASSIGNMENT_POSITION);
    }

    public TKeywords getKeywords() {
        return getChild(TKeywords.class);
    }

    public void removeKeywords(TKeywords keywords) {
        removeChild(KEYWORDS_PROPERTY, keywords);
    }

    public void setKeywords(TKeywords keywords) {
        setChild(TKeywords.class, KEYWORDS_PROPERTY, keywords,
                KEYWORDS_POSITION);
    }

    public void setOperation(WSDLReference<Operation> ref) {
        Operation opt = ref.get();
        if (opt == null) {
            return;
        }
        //
        WSDLComponent parent = opt.getParent();
        assert parent instanceof PortType;
        if (parent instanceof PortType) {
            setPortType(createPortTypeReference((PortType) parent));
        }
        //
        String val = opt.getName(); //ref.getRefString();
        setAttribute(OPERATION_PROPERTY, WLMAttribute.OPERATION, val);
        //
        // Add import
        if (!getOperation().isResolved()) {
            WLMComponentFactory factory = getModel().getFactory();
            TImport importEl = factory.createImport(getModel());
            importEl.setWSDL(opt.getModel());
            getModel().getTask().addImport(importEl);
        }
    }

    public void setOperation(WSDLReference<Operation> ref, String wsdlCatalogId) {
        Operation opt = ref.get();
        if (opt == null) {
            return;
        }
        //
        WSDLComponent parent = opt.getParent();
        assert parent instanceof PortType;
        if (parent instanceof PortType) {
            setPortType(createPortTypeReference((PortType) parent));
        }
        //
        String val = opt.getName(); //ref.getRefString();
        setAttribute(OPERATION_PROPERTY, WLMAttribute.OPERATION, val);
        //
        // Add import
        if (!getOperation().isResolved()) {
            WLMComponentFactory factory = getModel().getFactory();
            TImport importEl = factory.createImport(getModel());
            importEl.setWSDL(opt.getModel(), wsdlCatalogId);
            getModel().getTask().addImport(importEl);
        }
    }

    public void setPortType(WSDLReference<PortType> portTypeRef) {
        String refStr = portTypeRef.getRefString();
        setAttribute(PORT_TYPE_PROPERTY, WLMAttribute.PORT_TYPE, refStr);
        //
        discardVariables();
    }

    public void setName(String name) {
        // TODO Auto-generated method stub
        setAttribute(NAME_PROPERTY, WLMAttribute.NAME, name);
    }

    public String getName() {
        // TODO Auto-generated method stub
        return getAttribute(WLMAttribute.NAME);
    }

    public void accept(WLMVisitor visitor) {
        // TODO Auto-generated method stub
        visitor.visitTask(this);
    }

    public WLMComponent createChild(Element childEl) {
        // TODO Auto-generated method stub
        WLMComponent child = null;
        if (childEl != null) {
            String localName = childEl.getLocalName();
            if (localName == null || localName.length() == 0) {
                localName = childEl.getTagName();
            }
            //
            if (IMPORT_TYPE_PROPERTY.equals(localName)) {
                child = new ImportImpl(getModel(), childEl);
            } else if (TITLE_PROPERTY.equals(localName)) {
                child = new TitleImpl(getModel(), childEl);
            } else if (PRIORITY_PROPERTY.equals(localName)) {
                child = new PriorityImpl(getModel(), childEl);
            } else if (INIT_PROPERTY.equals(localName)) {
                child = new InitImpl(getModel(), childEl);
            } else if (ASSIGNMENT_PROPERTY.equals(localName)) {
                child = new AssignmentImpl(getModel(), childEl);
            } else if (TIMEOUT_PROPERTY.equals(localName)) {
                child = new TimeoutImpl(getModel(), childEl);
            } else if (ESCALATION_PROPERTY.equals(localName)) {
                child = new EscalationImpl(getModel(), childEl);
            } else if (NOTIFICATION_PROPERTY.equals(localName)) {
                child = new NotificationImpl(getModel(), childEl);
            } else if (ACTION_PROPERTY.equals(localName)) {
                child = new ActionImpl(getModel(), childEl);
            } else if (KEYWORDS_PROPERTY.equals(localName)) {
                child = new KeywordsImpl(getModel(), childEl);
            }
        }
        return child;
    }

    public String getOperationAsString() {
        // TODO Auto-generated method stub
        String optStr = getAttribute(WLMAttribute.OPERATION);
        return optStr;
    }

    public OperationReference createOperationReference(Operation referenced) {
        OperationReference ref = new OperationReference(referenced, this);
        return ref;
    }

    public PortTypeReference createPortTypeReference(PortType referenced) {
        PortTypeReference ref = new PortTypeReference(referenced, this);
        return ref;
    }

    private boolean contains(TImport imp) {
        boolean result = false;
        String ns = imp.getNamespace();
        String loc = imp.getLocation();

        Collection<TImport> imports = getImports();
        Iterator<TImport> it = imports.iterator();

        while (it.hasNext()) {
            TImport i = it.next();
            String namespace = i.getNamespace();
            String location = i.getLocation();

            boolean matchedNamespace = false;
            boolean matchedLocation = false;

            if (ns != null && ns.equals(namespace)) {
                matchedNamespace = true;
            }

            if (loc != null && loc.equals(location)) {
                matchedLocation = true;
            }

            if (ns != null) {
                result = matchedNamespace;
            }

            if (loc != null) {
                result &= matchedLocation;
            }

            if (result) {
                break;
            }

        }
        return result;
    }

    public List<TAction> getActions() {
        return getChildren(TAction.class);
    }

    public void addAction(TAction action) {
        addAfter(ACTION_PROPERTY, action, ACTION_POSITION);
    }

    public void removeAction(TAction action) {
        removeChild(ACTION_PROPERTY, action);
    }

    public synchronized VariableDeclaration getInputVariable() {
        if (mInputVariable == null) {
            Part firstPart = null;
            WSDLReference<Operation> operationRef = getOperation();
            if (operationRef != null) {
                Operation operation = operationRef.get();
                if (operation != null) {
                    Input input = operation.getInput();
                    if (input != null) {
                        NamedComponentReference<Message> msgRef = input.getMessage();
                        mInputVariable = new PredefinedVariable(INPUT_VAR_NAME, msgRef);
//                        if (msgRef != null) {
//                            Message msg = msgRef.get();
//                            if (msg != null) {
//                                Collection<Part> parts = msg.getParts();
//                                if (parts != null && !parts.isEmpty()) {
//                                    firstPart = parts.iterator().next();
//                                }
//                            }
//                        }
                    }
                }
            }
//            //
//            if (firstPart != null) {
//                mInputVariable = constructVariableDecl(INPUT_VAR_NAME, firstPart);
//            }
        }
        return mInputVariable;
    }

    public synchronized VariableDeclaration getOutputVariable() {
        if (mOutputVariable == null) {
            Part firstPart = null;
            WSDLReference<Operation> operationRef = getOperation();
            if (operationRef != null) {
                Operation operation = operationRef.get();
                if (operation != null) {
                    Output output = operation.getOutput();
                    if (output != null) {
                        NamedComponentReference<Message> msgRef = output.getMessage();
                        mOutputVariable = new PredefinedVariable(OUTPUT_VAR_NAME, msgRef);
//                        if (msgRef != null) {
//                            Message msg = msgRef.get();
//                            if (msg != null) {
//                                Collection<Part> parts = msg.getParts();
//                                if (parts != null && !parts.isEmpty()) {
//                                    firstPart = parts.iterator().next();
//                                }
//                            }
//                        }
                    }
                }
            }
//            //
//            if (firstPart != null) {
//                mOutputVariable = constructVariableDecl(OUTPUT_VAR_NAME, firstPart);
//            }
        }
        return mOutputVariable;
    }

    private synchronized void discardVariables() {
        mInputVariable = null;
        mOutputVariable = null;
    }

    private static VariableDeclaration constructVariableDecl(String name, Part part) {
        if (name == null || name.length() == 0) {
            return null;
        }
        //
        NamedComponentReference<GlobalElement> elemRef = part.getElement();
        if (elemRef != null) {
            return new PredefinedVariable(name, elemRef);
        }
        //
        NamedComponentReference<GlobalType> typeRef = part.getType();
        if (typeRef != null) {
            return new PredefinedVariable(name, typeRef);
        }
        //
        return null;
    }

    private static class PredefinedVariable implements VariableDeclaration {

        private String mName;
        private NamedComponentReference mRef;

        public PredefinedVariable(String name, NamedComponentReference ref) {
            assert name != null && name.length() != 0;
            assert ref != null;
            //
            mName = name;
            //
            mRef = ref;
        }

        public String getVariableName() {
            return mName;
        }

        public NamedComponentReference getTypeRef() {
            return mRef;
        }

        public Class getTypeClass() {
            return mRef.getType();
        }
    }

    private static final ElementPosition IMPORT_POSITION
            = new ElementPosition(TImport.class);

    private static final ElementPosition TITLE_POSITON
            = new ElementPosition(IMPORT_POSITION, TTitle.class);
    
    private static final ElementPosition PRIORITY_POSITION
            = new ElementPosition(TITLE_POSITON, TPriority.class);

    private static final ElementPosition INIT_POSITION
            = new ElementPosition(PRIORITY_POSITION, TInit.class);

    private static final ElementPosition ASSIGNMENT_POSITION 
            = new ElementPosition(INIT_POSITION, TAssignment.class);
    
    private static final ElementPosition TIMEOUT_POSITION 
            = new ElementPosition(ASSIGNMENT_POSITION, TTimeout.class);
    
    private static final ElementPosition ESCALATION_POSITION
            = new ElementPosition(TIMEOUT_POSITION, TEscalation.class);
    
    private static final ElementPosition NOTIFICATION_POSITION
            = new ElementPosition(ESCALATION_POSITION, TNotification.class);

    private static final ElementPosition ACTION_POSITION
            = new ElementPosition(NOTIFICATION_POSITION, TAction.class);

    private static final ElementPosition KEYWORDS_POSITION
            = new ElementPosition(ACTION_POSITION, TKeywords.class);
}
