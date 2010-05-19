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

package org.netbeans.modules.wlm.model.xpath;

import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.VariableDeclaration;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.utl.Util;
import org.netbeans.modules.xml.xpath.ext.spi.VariableResolver;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.xpath.ext.spi.validation.XPathValidationContext;

/**
 * This object is intended to allow to an XPath model to resolve BPEL variables.
 * 
 * @author nk160297
 */
class WlmVariableResolver implements VariableResolver<XPathWlmVariable> {

    private WLMComponent mContextComp;
    private XPathValidationContext myVContext;
    
    public WlmVariableResolver(XPathValidationContext context, WLMComponent contextComp) {
        myVContext = context;
        mContextComp = contextComp;
    }
    
    public XPathWlmVariable resolveVariable(QName varQName) {
        Part resultPart = null;
        VariableDeclaration resultVariable = null;
        //
        String prefix = varQName.getPrefix();
        if (prefix != null && prefix.length() != 0) {
            WLMModel wlmModel = mContextComp.getModel();
            if (wlmModel == null || wlmModel.getState() != State.VALID) {
                // Error: invalid bpel model
                return null;
            }
            TTask task = wlmModel.getTask();
            if (task == null) {
                // Error: invalid process
                return null;
            }
            //
            ExNamespaceContext nsContext = 
                    new WlmXPathNamespaceContext(mContextComp);
            //
            String namespaceUri = nsContext.getNamespaceURI(prefix);
            if (namespaceUri == null) {
                // Error: Unknown prefix
                if (myVContext != null) {
                    myVContext.addResultItem(ResultType.ERROR, NbBundle.getMessage(WlmVariableResolver.class, "UNKNOWN_NAMESPACE_PREFIX"), prefix); // NOI18N
                }
                return null;
            }
            //
            String processNsUri = task.getTargetNamespace();
            assert processNsUri != null;
            //
            if (!processNsUri.equals(namespaceUri)) {
                // Error: the variable is defined in a wrong namespace.
                String correctPrefix = nsContext.getPrefix(processNsUri);
                String varName = varQName.getLocalPart();
                if (myVContext != null) {
                    myVContext.addResultItem(ResultType.ERROR, NbBundle.getMessage(WlmVariableResolver.class, "WRONG_VARIABLE_PREFIX"), varName, prefix, correctPrefix); // NOI18N
                }
                return null;
            }
        }
        //
        // Extract part
        String varName = varQName.getLocalPart();
        int partIndex = varName.lastIndexOf('.');
        String partName = null;
        if (partIndex != -1) {
            partName = varName.substring(partIndex + 1);
            varName = varName.substring(0, partIndex);
        }
        //
        // Try to find the variable in the model.
        resultVariable = findVariable(varName);
        if (resultVariable == null) {
            // Error: Can't find the variable
            //
            // Don't show this error because of it is already detected by another
            // See: ReferencesValidator.FIX_VARIABLE
            return null;
        } 
        //
        Class varType = resultVariable.getTypeClass();
        NamedComponentReference typeRef = resultVariable.getTypeRef();
        //
        // Check message type
        Message message = null;
        if (varType == Message.class) {
            if (typeRef != null) {
                message = (Message)typeRef.get();
                if (message == null) {
                    // Error: impossible to get message by ref
                    if (myVContext != null) {
                        myVContext.addResultItem(ResultType.ERROR,
                                NbBundle.getMessage(WlmVariableResolver.class,
                                "CANT_RETRIEVE_MESSAGE_TYPE"),
                                typeRef.getRefString(), varName); // NOI18N
                    }
                    return null;
                }
            }
        }
        //
        // Check part 
        if (partName == null || partName.length() == 0) {
            if (typeRef != null) {
                //
                // Error: If the variable is of message type then a part has to be specified
                //
                if (myVContext != null) {
                    ArrayList<String> partNameList = getPartNames(message);
                    switch (partNameList.size()) {
                    case 0: 
                        myVContext.addResultItem(ResultType.ERROR, NbBundle.getMessage(WlmVariableResolver.class,
                                "MESSAGE_PART_REQUIRED"), varName); // NOI18N
                        break;
                    case 1:
                        String firstPartName = partNameList.get(0);
                        myVContext.addResultItem(ResultType.ERROR, NbBundle.getMessage(WlmVariableResolver.class,
                                "SPECIFIC_MESSAGE_PART_REQUIRED"), 
                                varName, firstPartName); // NOI18N
                        break;
                    default:
                        //
                        myVContext.addResultItem(ResultType.ERROR, NbBundle.getMessage(WlmVariableResolver.class,
                                "A_MESSAGE_PART_REQUIRED"), varName, 
                                partNameListToString(partNameList)); // NOI18N
                    }
                }
                //
                return new XPathWlmVariable(resultVariable, null); // anjeleevich: prev result null.
            }
        } else {
            // A message part is specified
            if (typeRef == null) {
                // Error: if the part is specified then variable has to be of a message type
                if (myVContext != null) {
                    myVContext.addResultItem(ResultType.ERROR, NbBundle.getMessage(WlmVariableResolver.class,
                            "VARIABLE_MESSAGE_TYPE_REQUIRED"), varName); // NOI18N
                }
                return null;
            } else {
                Collection<Part> parts = message.getParts();
                for (Part aPart : parts) {
                    if (aPart.getName().equals(partName)) {
                        resultPart = aPart;
                        break;
                    }
                }
                //
                if (resultPart == null) {
                    // Error: the specified part is not found
                    //
                    if (myVContext != null) {
                        ArrayList<String> partNameList = getPartNames(message);
                        switch (partNameList.size()) {
                        case 0: 
                            myVContext.addResultItem(ResultType.ERROR, NbBundle.getMessage(WlmVariableResolver.class,
                                    "UNKNOWN_MESSAGE_PART"), partName, varName); // NOI18N
                            break;
                        case 1:
                            String firstPartName = partNameList.get(0);
                            myVContext.addResultItem(ResultType.ERROR, NbBundle.getMessage(WlmVariableResolver.class,
                                    "SPECIFIC_UNKNOWN_MESSAGE_PART"), 
                                    partName, varName, firstPartName); // NOI18N
                            break;
                        default:
                            //
                            myVContext.addResultItem(ResultType.ERROR, NbBundle.getMessage(WlmVariableResolver.class,
                                    "A_UNKNOWN_MESSAGE_PART"), 
                                    partName, varName, 
                                    partNameListToString(partNameList)); // NOI18N
                        }
                    }
                    return null;
                }
            }
        }
        return new XPathWlmVariable(resultVariable, resultPart);
    }

    public ReferenceableSchemaComponent resolveVariableType(XPathWlmVariable variable) {
        if (variable != null) {
            return variable.getType();
        } else {
            return null;
        }
    }

    public ReferenceableSchemaComponent resolveVariableType(QName variableName) {
        return resolveVariableType(resolveVariable(variableName));
    }

    //==========================================================================
    
    private ArrayList<String> getPartNames(Message message) {
        ArrayList<String> partNameList = new ArrayList<String>();
        Collection<Part> parts = message.getParts();
        for (Part aPart : parts) {
            String aPartName = aPart.getName();
            partNameList.add(aPartName);
        }
        return partNameList;
    }
    
    private String partNameListToString(List<String> partNameList) {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (String aPartName : partNameList) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(" | "); // NOI18N
            }
            //
            sb.append(aPartName);
        }
        return sb.toString();
    }

    public List<VariableDeclaration> getAllVariables() {
        // Add predefined input and output variables
        WLMModel model = mContextComp.getModel();
        if (model != null) {
            return Util.getAllVariables(model);
        }

        List<VariableDeclaration> result = Collections.emptyList();
        return result;
    }

    public VariableDeclaration findVariable(String soughtName) {
        List<VariableDeclaration> varList = getAllVariables();
        for (VariableDeclaration var : varList) {
            String varName = var.getVariableName();
            if (varName != null && varName.equals(soughtName)) {
                return var;
            }
        }
        return null;
    }
}
