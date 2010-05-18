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

package org.netbeans.modules.xslt.tmap.model.api.completion.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.modules.xml.text.syntax.dom.Tag;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xslt.tmap.model.api.WsdlDataHolder;
import org.netbeans.modules.xslt.tmap.model.api.completion.TMapCompletionResultItem;
import org.netbeans.modules.xslt.tmap.model.api.completion.TMapCompletionUtil;
import org.netbeans.modules.xslt.tmap.model.api.completion.TMapEditorComponentHolder;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Alex Petrov (14.07.2008)
 */
public class HandlerTransformSourceResult extends BaseCompletionHandler {
    //<service name="Service1" portType="ns1:getloanwsdPortType">
    //    <operation opName="getloanwsdOperation" 
    //        inputVariable="inOpVar1" outputVariable="outOpVar1">
    //        <transform name="Transform0" file="newXSLFile1.xsl" 
    //            source="$inOpVar1.part1" result="$inInvokeVar1.part1"/>
    //    </operation>
    //</service>
    
    @Override
    public List<TMapCompletionResultItem> getResultItemList(
        TMapEditorComponentHolder editorComponentHolder) {
        initHandler(editorComponentHolder);
        return getVariableNameList(TAG_NAME_TRANSFORM, 
            new String[] {ATTRIBUTE_NAME_SOURCE, ATTRIBUTE_NAME_RESULT});
    }
    
    protected List<TMapCompletionResultItem> getVariableNameList(String requiredTagName,
        String[] requiredAttributeNames) {
        if ((surroundTag == null) || (attributeName == null) || (tmapModel == null)) 
            return Collections.emptyList();
        
        String tagName = surroundTag.getTagName();
        if (! tagName.contains(requiredTagName))
            return Collections.emptyList();
        if (! Arrays.asList(requiredAttributeNames).contains(attributeName))
            return Collections.emptyList();

        if ((tmapModel != null) && (tmapModel.getState().equals(State.NOT_WELL_FORMED))) {
            return getIncorrectDocumentResultItem();
        }

        List<String> requiredPartNames = getRequiredPartNames();
        return getVariableNameList(requiredPartNames);
    }
    
    protected List<TMapCompletionResultItem> getVariableNameList(
        List<String> requiredPartNames) {
        Tag operationTag = TMapCompletionUtil.getParentTag(surroundTag, TAG_NAME_OPERATION);
        if (operationTag == null) return Collections.emptyList();
        
        SortedSet<String> inputVariables = new TreeSet<String>(),
                          outputVariables = new TreeSet<String>();
        storeInputOutputVariables(operationTag, inputVariables, outputVariables);
        
        NodeList childTags = operationTag.getChildNodes();
        for (int i = 0; i < childTags.getLength(); ++i) {
            Node childNode = childTags.item(i);
            if (childNode instanceof Tag) { // null isn't instance of any class
                storeInputOutputVariables((Tag) childNode, inputVariables, outputVariables);
            }
        }
        return makeCompletionResultItemList(inputVariables, outputVariables, requiredPartNames);
    }

    protected List<TMapCompletionResultItem> makeCompletionResultItemList(
        SortedSet<String> inputVariables, SortedSet<String> outputVariables, 
        List<String> requiredPartNames) {
        if (attributeName.equals(ATTRIBUTE_NAME_SOURCE)) {
            return makeCompletionResultItemList("Input Variables", inputVariables, 
                "Output Variables", outputVariables, requiredPartNames);
        } else if (attributeName.equals(ATTRIBUTE_NAME_RESULT)) {
            return makeCompletionResultItemList("Output Variables", outputVariables, 
                "Input Variables", inputVariables, requiredPartNames);
        }
        return Collections.emptyList();
    }

    protected List<TMapCompletionResultItem> makeCompletionResultItemList(
        String firstTitle, SortedSet<String> firstVariableSet, 
        String lastTitle, SortedSet<String> lastVariableSet, 
        List<String> requiredPartNames) {
        
        List<TMapCompletionResultItem> 
            resultItemList = new ArrayList<TMapCompletionResultItem>();
        resultItemList.addAll(makeCompletionResultItemList(firstTitle, 
            firstVariableSet, requiredPartNames));
        resultItemList.addAll(makeCompletionResultItemList(lastTitle, 
            lastVariableSet, requiredPartNames));
        for (int i = 0; i < resultItemList.size(); ++i) {
            resultItemList.get(i).setSortPriority(i);
        }
        return resultItemList;
    }
    
    private List<TMapCompletionResultItem> makeCompletionResultItemList(
        String title, SortedSet<String> variableSet, List<String> requiredPartNames) {
        if ((title == null) || (variableSet == null) ||
            (variableSet.isEmpty())) return Collections.emptyList();
        
        List<TMapCompletionResultItem> 
            resultItemList = new ArrayList<TMapCompletionResultItem>();
        
        Iterator<String> iterator = variableSet.iterator();
        while (iterator.hasNext()) {
            String variableName = iterator.next();
            if (! requiredPartNames.isEmpty()) {
                for (String partName : requiredPartNames) {
                    resultItemList.add(new ResultItemTransformSourceResult(variableName, 
                        partName, document, caretOffset));
                }
            } else {
                resultItemList.add(new ResultItemTransformSourceResult(variableName, 
                    null, document, caretOffset));
            }
        }
        if (! resultItemList.isEmpty()) {
            resultItemList.add(0, new ResultItemSectionRootNode(title, document, caretOffset));
        }
        return resultItemList;
    }
    
    private void storeInputOutputVariables(Tag tag, SortedSet<String> inputVariables,
        SortedSet<String> outputVariables) {
        if ((tag == null) || (inputVariables == null) || (outputVariables == null))
            return;
        
        String inputVarName = tag.getAttribute(ATTRIBUTE_NAME_INPUT_VARIABLE);
        if ((inputVarName != null) && (inputVarName.length() > 0)) {
            inputVariables.add(inputVarName);
        }
        String outputVarName = tag.getAttribute(ATTRIBUTE_NAME_OUTPUT_VARIABLE);
        if ((outputVarName != null) && (outputVarName.length() > 0)) {
            outputVariables.add(outputVarName);
        }
    }
    
    protected List<String> getRequiredPartNames() {
        String requiredOperationName = getRequiredOperationName(surroundTag);
        if (requiredOperationName == null) return null;

        String requiredPortType = getRequiredPortType(surroundTag);
        if (requiredPortType == null) return null;

        WsdlDataHolder wsdlHolder = getWsdlHolder(requiredPortType);
        if (wsdlHolder == null) return null;
        try {
            WSDLModel wsdlModel = wsdlHolder.getWsdlModel();
            Collection<PortType> portTypes = wsdlModel.getDefinitions().getPortTypes();
            if (portTypes == null) return null;
            
            List<Message> messageList = getMessageList(portTypes, requiredOperationName);
            if ((messageList == null) || (messageList.isEmpty())) return null;
            
            List<String> messagePartNames = getMessagePartNameList(messageList);
            if ((messagePartNames == null) || (messagePartNames.isEmpty())) return null;

            SortedSet<String> partNames = new TreeSet<String>(messagePartNames);
            return (new ArrayList(partNames));
        } catch(Exception e) {
            TMapCompletionUtil.logExceptionInfo(e);
            return null;
        }
    }

    private List<Message> getMessageList(Collection<PortType> portTypes, 
        String requiredOperationName) {
        if ((portTypes == null) || (requiredOperationName == null)) 
            return Collections.emptyList();
        
        List<Message> messageList = new ArrayList<Message>();
        
        Iterator<PortType> iterator = portTypes.iterator();
        while (iterator.hasNext()) {
            PortType portType = iterator.next();
            Collection<Operation> operations = portType.getOperations();
            List<Message> operationMessages = getMessageList(requiredOperationName,
                operations);
            if (operationMessages != null) {
                messageList.addAll(operationMessages);
            }
        }
        return messageList;
    }

    private List<Message> getMessageList(String requiredOperationName,
        Collection<Operation> operations) {
        if ((operations == null) || (requiredOperationName == null)) 
            return Collections.emptyList();
        
        List<Message> messageList = new ArrayList<Message>();
        
        Iterator<Operation> iterator = operations.iterator();
        while (iterator.hasNext()) {
            Operation operation = iterator.next();
            String operationName = operation.getName();
            if (operationName == null) continue;
            
            if (operationName.equals(requiredOperationName)) {
                Input input = operation.getInput();
                Message message = input.getMessage().get();
                if (message != null) messageList.add(message);
                
                Output output = operation.getOutput();
                message = output.getMessage().get();
                if (message != null) messageList.add(message);
            }
        }
        return messageList;
    }

    private List<String> getMessagePartNameList(List<Message> messageList) {
        if (messageList == null) return null;
        
        List<String> messagePartNames = new ArrayList<String>();
        for (Message message : messageList) {
            Collection<Part> messageParts = message.getParts();
            if (messageParts == null) continue;
            
            Iterator<Part> partIterator = messageParts.iterator();
            while (partIterator.hasNext()) {
                Part messagePart = partIterator.next();
                if (messagePart == null) continue;
                
                String partName = messagePart.getName();
                if ((partName != null) && (partName.length() > 0) && 
                    (! messagePartNames.contains(partName))) {
                    messagePartNames.add(partName);
                }
            }
        }
        return messagePartNames;
    }
    
    protected String getRequiredOperationName(Tag requiredTag) {
        if (requiredTag == null) return null;
        // get parent tag <operation ...>
        requiredTag = TMapCompletionUtil.getParentTag(requiredTag, TAG_NAME_OPERATION);
        if (requiredTag == null) {
            TMapCompletionUtil.showMsgParentTagNotFound(TAG_NAME_OPERATION);
            return null;
        }
        String requiredOperationName = requiredTag.getAttribute(
            ATTRIBUTE_NAME_OPERATION_NAME);
        if (requiredOperationName == null) {
            TMapCompletionUtil.showMsgAttributeNotDefined(TAG_NAME_OPERATION, 
                ATTRIBUTE_NAME_OPERATION_NAME);
            return null;
        }
        return requiredOperationName;
    }
    
    @Override
    public String getRequiredPortType(Tag requiredTag) {
        if (requiredTag == null) return null;
        try {
            // get parent tag <service ...>
            requiredTag = TMapCompletionUtil.getParentTag(requiredTag, TAG_NAME_SERVICE);
            if (requiredTag == null) throw new NullPointerException();
            return super.getRequiredPortType(requiredTag);
        } catch(Exception e) {
            TMapCompletionUtil.showMsgParentTagNotFound(TAG_NAME_SERVICE);
            return null;
        }
    }
}