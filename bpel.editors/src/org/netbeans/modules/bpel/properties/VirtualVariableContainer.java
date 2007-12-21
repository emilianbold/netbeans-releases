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

package org.netbeans.modules.bpel.properties;

import java.util.Iterator;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.spi.FindHelper;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.xam.Reference;
import org.openide.util.Lookup;

/**
 * This class is intended to keep information about a variable.
 * The variable can be existing or not.
 * If the variable is existing then it is specified with a direct instance.
 * Otherwise a preparatory information is specified.
 *
 * @author nk160297
 */
public class VirtualVariableContainer {
    
    private String myName;
    private TypeContainer myType;
    private BaseScope myScope;
    
    private VariableDeclaration myVarDecl;
    private Lookup myLookup;
    
    public VirtualVariableContainer(String name, TypeContainer type, BaseScope scope) {
        myName = name;
        myType = type;
        myScope = scope;
    }
    
    public VirtualVariableContainer(VariableDeclaration varDecl, Lookup lookup) {
        myVarDecl = varDecl;
    }
    
    public String getName() {
        if (myVarDecl == null) {
            return myName;
        } else {
            return myVarDecl.getVariableName();
        }
    }
    
    public TypeContainer getType() {
        if (myVarDecl == null) {
            return myType;
        } else {
            Reference typeRef = myVarDecl.getMessageType();
            if (typeRef == null) {
                typeRef = myVarDecl.getType();
                if (typeRef == null) {
                    typeRef = myVarDecl.getElement();
                }
            }
            if (typeRef != null) {
                return new TypeContainer(typeRef);
            }
        }
        //
        return null;
    }
    
    public BaseScope getScope() {
        if (myVarDecl == null) {
            return myScope;
        } else {
            FindHelper findHelper = (FindHelper)myLookup.lookup(FindHelper.class);
            if (findHelper != null) {
                Iterator<BaseScope> itr = findHelper.scopeIterator(myVarDecl);
                if (itr.hasNext()) {
                    return itr.next();
                }
            }
        }
        //
        return null;
    }
    
    public VariableDeclaration getVariableDeclaration() {
        return myVarDecl;
    }
    
    public boolean isExisting() {
        return myVarDecl != null;
    }
    
    /**
     * Creates a new variable if the container keeps not existing description.
     * This method has to be called inside of transaction.
     */
    public VariableDeclaration createNewVariable() throws VetoException {
        if (myVarDecl != null) {
            return myVarDecl;
        }
        //
        BpelModel model = myScope.getBpelModel();
        BPELElementsBuilder builder = model.getBuilder();
        //
        VariableContainer variableContainer = myScope.getVariableContainer();
        if (variableContainer == null) {
            variableContainer = builder.createVariableContainer();
            myScope.setVariableContainer(variableContainer);
            variableContainer = myScope.getVariableContainer();
        }
        //
        Variable resultVar = builder.createVariable();
        variableContainer.insertVariable(resultVar, 0);
        resultVar = variableContainer.getVariable(0);
        //
        resultVar.setName(myName);
        //
        if (myType != null) {
            switch (myType.getStereotype()) {
                case MESSAGE:
                    Message msg = myType.getMessage();
                    WSDLReference<Message> msgRef =
                            resultVar.createWSDLReference(msg, Message.class);
                    resultVar.setMessageType(msgRef);
                    break;
                case PRIMITIVE_TYPE:
                case GLOBAL_TYPE:
                case GLOBAL_SIMPLE_TYPE:
                case GLOBAL_COMPLEX_TYPE:
                    GlobalType type = myType.getGlobalType();
                    SchemaReference<GlobalType> typeRef =
                            resultVar.createSchemaReference(type, GlobalType.class);
                    resultVar.setType(typeRef);
                    break;
                case GLOBAL_ELEMENT:
                    GlobalElement element = myType.getGlobalElement();
                    SchemaReference<GlobalElement> elementRef =
                            resultVar.createSchemaReference(element, GlobalElement.class);
                    resultVar.setElement(elementRef);
                    break;
            }
        }
        //
        return resultVar;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VirtualVariableContainer) {
            boolean retValue = false;
            //
            VirtualVariableContainer otherVvc = (VirtualVariableContainer)obj;
            boolean thisIsExisting = this.isExisting();
            boolean otherIsExisting = otherVvc.isExisting();
            //
            if (thisIsExisting && otherIsExisting) {
                retValue = this.myVarDecl.equals(otherVvc.getVariableDeclaration());
            } else if (!thisIsExisting && !otherIsExisting) {
                retValue = this.getName().equals(otherVvc.getName()) &&
                        this.getScope().equals(otherVvc.getScope()) &&
                        this.getType().equals(otherVvc.getType());
            }
            //
            return retValue;
        }
        return false;
    }
    
}
