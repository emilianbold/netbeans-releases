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

import com.sun.org.apache.xml.internal.utils.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.xml.text.syntax.dom.Tag;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xslt.tmap.model.api.WsdlDataHolder;
import org.netbeans.modules.xslt.tmap.model.api.completion.TMapCompletionResultItem;
import org.netbeans.modules.xslt.tmap.model.api.completion.TMapCompletionUtil;
import org.netbeans.modules.xslt.tmap.model.api.completion.TMapEditorComponentHolder;

/**
 * @author Alex Petrov (11.07.2008)
 */
public class HandlerOperationOpName extends BaseCompletionHandler {
    //<operation opName="getloanwsdOperation" inputVariable="inOpVar1" 
    //           outputVariable="outOpVar1">
    
    @Override
    public List<TMapCompletionResultItem> getResultItemList(
        TMapEditorComponentHolder editorComponentHolder) {
        initHandler(editorComponentHolder);
        return getOperationNameList(TAG_NAME_OPERATION);
    }
    
    protected List<TMapCompletionResultItem> getOperationNameList(String requiredTagName) {
        if ((surroundTag == null) || (attributeName == null) || (tmapModel == null)) 
            return Collections.emptyList();
        
        String tagName = surroundTag.getTagName(); //getLocalName();
        if (! tagName.contains(requiredTagName))
            return Collections.emptyList();
        if (! attributeName.equals(ATTRIBUTE_NAME_OPERATION_NAME))
            return Collections.emptyList();

        if ((tmapModel != null) && (tmapModel.getState().equals(State.NOT_WELL_FORMED))) {
            return getIncorrectDocumentResultItem();
        }
        String requiredPortType = getRequiredPortType(surroundTag);
        WsdlDataHolder wsdlHolder = getWsdlHolder(requiredPortType);
        return getOperationNameList(wsdlHolder, requiredPortType);
    }
    
    private List<TMapCompletionResultItem> getOperationNameList(
        WsdlDataHolder wsdlHolder, String requiredPortType) {
        if (wsdlHolder == null) return Collections.emptyList();         

        try {
            Collection<PortType> portTypes = 
                wsdlHolder.getWsdlModel().getDefinitions().getPortTypes();                                    
            if (portTypes.size() < 1) Collections.emptyList();

            String portTypeName = QName.getLocalPart(requiredPortType);
            List<TMapCompletionResultItem> resultItemList = getOperationNameList(
               portTypes, portTypeName);
            return resultItemList;
        } catch(Exception e) {
            TMapCompletionUtil.logExceptionInfo(e);
        }
        return Collections.emptyList();
    }

    private List<TMapCompletionResultItem> getOperationNameList(
        Collection<PortType> portTypes, String requiredPortTypeName) {
        if ((portTypes == null) || (requiredPortTypeName == null)) 
            return Collections.emptyList();
        
        Iterator<PortType> iterator = portTypes.iterator();
        List<TMapCompletionResultItem> resultItemList = new ArrayList<TMapCompletionResultItem>();
        while (iterator.hasNext()) {
            PortType portType = iterator.next();
            String namePortType = portType.getName();
            if ((namePortType == null) || (! requiredPortTypeName.equals(namePortType))) 
                continue;
            
            Collection<Operation> operations = portType.getOperations();
            if ((operations == null) || (operations.size() < 1)) continue;
            
            Iterator<Operation> iteratorOperation = operations.iterator();
            while (iteratorOperation.hasNext()) {
                Operation operation = iteratorOperation.next();
                if (operation == null) continue;
                
                String nameOperation = operation.getName();
                if ((nameOperation == null) || (nameOperation.length() < 1)) continue;
                
                TMapCompletionResultItem resultItem = new TMapCompletionResultItem(
                    nameOperation, document, caretOffset);
                resultItemList.add(resultItem);
            }
            return resultItemList;
        }
        return Collections.emptyList();
    }
    
    @Override
    public String getRequiredPortType(Tag requiredTag) {
        if (requiredTag == null) return null;
        if (requiredTag.getTagName().contains(TAG_NAME_OPERATION)) {
            // get parent tag <service ...>
            requiredTag = (Tag) requiredTag.getParentNode();
            if (requiredTag == null) {
                TMapCompletionUtil.showMsgParentTagNotFound(TAG_NAME_SERVICE);
                return null;
            }
        }
        return super.getRequiredPortType(requiredTag);
    }
}