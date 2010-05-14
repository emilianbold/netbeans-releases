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
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xslt.tmap.model.api.completion.TMapCompletionResultItem;
import org.netbeans.modules.xslt.tmap.model.api.completion.TMapEditorComponentHolder;

/**
 * @author Alex Petrov (16.07.2008)
 */
public class HandlerParamTypeValue extends HandlerTransformSourceResult {
    //<service name="Service1" portType="ns1:getloanwsdPortType">
    //    <operation opName="getloanwsdOperation" 
    //        inputVariable="inOpVar1" outputVariable="outOpVar1">
    //        <transform name="Transform0" file="newXSLFile1.xsl" 
    //            source="$inOpVar1.part1" result="$inInvokeVar1.part1">
    //            <param name="param1" type="literal" value="..."/>
    //            <param name="param2" type="part" value="$someVariableName.part1"/>
    //        </transform>
    //    </operation>
    //</service>
    
    private static final String
        ATTRIBUTE_TYPE_VALUE_LITERAL = "literal", // NOI18N
        ATTRIBUTE_TYPE_VALUE_PART = "part"; // NOI18N
    
    @Override
    public List<TMapCompletionResultItem> getResultItemList(
        TMapEditorComponentHolder editorComponentHolder) {
        initHandler(editorComponentHolder);
        return getResultItemList();
    }
    
    protected List<TMapCompletionResultItem> getResultItemList() {
        if ((surroundTag == null) || (attributeName == null) || (tmapModel == null)) 
            return Collections.emptyList();
        
        String tagName = surroundTag.getTagName();
        if (! tagName.contains(TAG_NAME_PARAM))
            return Collections.emptyList();

        if ((tmapModel != null) && (tmapModel.getState().equals(State.NOT_WELL_FORMED))) {
            return getIncorrectDocumentResultItem();
        }

        if (attributeName.contains(ATTRIBUTE_NAME_TYPE)) {
            return getAvailableTypeList();
        } else if (attributeName.contains(ATTRIBUTE_NAME_VALUE)) {
            List<String> requiredPartNames = getRequiredPartNames();
            return getVariableNameList(requiredPartNames);
        }
        return Collections.emptyList();
    }
    
    private List<TMapCompletionResultItem> getAvailableTypeList() {
        List<TMapCompletionResultItem> resultItemList = 
            new ArrayList<TMapCompletionResultItem>(2);
        resultItemList.add(new TMapCompletionResultItem(ATTRIBUTE_TYPE_VALUE_LITERAL, 
            document, caretOffset));
        resultItemList.add(new TMapCompletionResultItem(ATTRIBUTE_TYPE_VALUE_PART, 
            document, caretOffset));
        return resultItemList;
    }

    @Override
    protected List<TMapCompletionResultItem> makeCompletionResultItemList(
        SortedSet<String> inputVariables, SortedSet<String> outputVariables, 
        List<String> requiredPartNames) {
        return makeCompletionResultItemList("Input Variables", inputVariables, 
            "Output Variables", outputVariables, requiredPartNames);
    }
}