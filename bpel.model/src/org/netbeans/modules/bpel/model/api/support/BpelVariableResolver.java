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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bpel.model.api.support;

import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.xml.xpath.ext.spi.VariableResolver;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.Model.State;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.xpath.ext.spi.validation.XPathValidationContext;

/**
 * This object is intended to allow to an XPath model to resolve BPEL variables.
 * 
 * @author nk160297
 */
class BpelVariableResolver implements VariableResolver<XPathBpelVariable> {

    private BpelEntity mBpelEntity;
    private VisibilityScope mVisScope;
    private XPathValidationContext myVContext;
    
    public BpelVariableResolver(XPathValidationContext context, BpelEntity bpelEntity) {
        myVContext = context;
        mBpelEntity = bpelEntity;
    }
    
    private synchronized VisibilityScope getVisibilityScope() {
        if (mVisScope == null) {
            mVisScope = new VisibilityScope(mBpelEntity); 
        }
        return mVisScope;
    }
    
    public XPathBpelVariable resolveVariable(QName varQName) {
        Part resultPart = null;
        VariableDeclaration resultVariable = null;
        //
        String prefix = varQName.getPrefix();
        if (prefix != null && prefix.length() != 0) {
            BpelModel bpelModel = mBpelEntity.getBpelModel();
            if (bpelModel == null || bpelModel.getState() != State.VALID) {
                // Error: invalid bpel model
                return null;
            }
            Process process = bpelModel.getProcess();
            if (process == null) {
                // Error: invalid process
                return null;
            }
            //
            ExNamespaceContext nsContext = mBpelEntity.getNamespaceContext();
            //
            String namespaceUri = nsContext.getNamespaceURI(prefix);
            if (namespaceUri == null) {
                // Error: Unknown prefix
                if (myVContext != null) {
                    myVContext.addResultItem(ResultType.ERROR, NbBundle.getMessage(BpelVariableResolver.class, "UNKNOWN_NAMESPACE_PREFIX"), prefix); // NOI18N
                }
                return null;
            }
            //
            String processNsUri = process.getTargetNamespace();
            assert processNsUri != null;
            //
            if (!processNsUri.equals(namespaceUri)) {
                // Error: the variable is defined in a wrong namespace.
                String correctPrefix = nsContext.getPrefix(processNsUri);
                String varName = varQName.getLocalPart();
                if (myVContext != null) {
                    myVContext.addResultItem(ResultType.ERROR, NbBundle.getMessage(BpelVariableResolver.class, "WRONG_VARIABLE_PREFIX"), varName, 
                            prefix, correctPrefix); // NOI18N
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
        // Visibility rules are taken into consideration!
        resultVariable = getVisibilityScope().lookForVariable(varName);
        if (resultVariable == null) {
            // Error: Can't find the variable
            //
            // Don't show this error because of it is already detected by another
            // See: ReferencesValidator.FIX_VARIABLE
            return null;
        } 
        //
        // Check message type 
        WSDLReference<Message> messageTypeRef = resultVariable.getMessageType();
        Message message = null;
        if (messageTypeRef != null) {
            message = messageTypeRef.get();
            if (message == null) {
                // Error: impossible to get message by ref
                if (myVContext != null) {
                    myVContext.addResultItem(ResultType.ERROR, NbBundle.getMessage(BpelVariableResolver.class, "CANT_RETRIEVE_MESSAGE_TYPE"),
                            messageTypeRef.getRefString(), varName); // NOI18N
                }
                return null;
            }
        }
        //
        // Check part 
        if (partName == null || partName.length() == 0) {
            if (messageTypeRef != null) {
                //
                // Error: If the variable is of message type then a part has to be specified
                //
                if (myVContext != null) {
                    ArrayList<String> partNameList = getPartNames(message);
                    switch (partNameList.size()) {
                    case 0: 
                        myVContext.addResultItem(ResultType.ERROR, NbBundle.getMessage(BpelVariableResolver.class,
                                "MESSAGE_PART_REQUIRED"), varName); // NOI18N
                        break;
                    case 1:
                        String firstPartName = partNameList.get(0);
                        myVContext.addResultItem(ResultType.ERROR, NbBundle.getMessage(BpelVariableResolver.class,
                                "SPECIFIC_MESSAGE_PART_REQUIRED"), 
                                varName, firstPartName); // NOI18N
                        break;
                    default:
                        //
                        myVContext.addResultItem(ResultType.ERROR, NbBundle.getMessage(BpelVariableResolver.class,
                                "A_MESSAGE_PART_REQUIRED"), varName, 
                                partNameListToString(partNameList)); // NOI18N
                    }
                }
                //
                return null;
            }
        } else {
            // A message part is specified
            if (messageTypeRef == null) {
                // Error: if the part is specified then variable has to be of a message type
                if (myVContext != null) {
                    myVContext.addResultItem(ResultType.ERROR, NbBundle.getMessage(BpelVariableResolver.class,
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
                            myVContext.addResultItem(ResultType.ERROR, NbBundle.getMessage(BpelVariableResolver.class,
                                    "UNKNOWN_MESSAGE_PART"), partName, varName); // NOI18N
                            break;
                        case 1:
                            String firstPartName = partNameList.get(0);
                            myVContext.addResultItem(ResultType.ERROR, NbBundle.getMessage(BpelVariableResolver.class,
                                    "SPECIFIC_UNKNOWN_MESSAGE_PART"), 
                                    partName, varName, firstPartName); // NOI18N
                            break;
                        default:
                            //
                            myVContext.addResultItem(ResultType.ERROR, NbBundle.getMessage(BpelVariableResolver.class,
                                    "A_UNKNOWN_MESSAGE_PART"), 
                                    partName, varName, 
                                    partNameListToString(partNameList)); // NOI18N
                        }
                    }
                    return null;
                }
            }
        }
        return new XPathBpelVariable(resultVariable, resultPart);
    }

    public ReferenceableSchemaComponent resolveVariableType(XPathBpelVariable variable) {
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
}
