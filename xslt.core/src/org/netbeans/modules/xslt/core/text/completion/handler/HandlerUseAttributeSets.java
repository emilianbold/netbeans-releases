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

package org.netbeans.modules.xslt.core.text.completion.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xslt.core.text.completion.XSLTCompletionConstants;
import org.netbeans.modules.xslt.core.text.completion.XSLTCompletionResultItem;
import org.netbeans.modules.xslt.core.text.completion.XSLTCompletionUtil;
import org.netbeans.modules.xslt.core.text.completion.XSLTEditorComponentHolder;
import org.netbeans.modules.xslt.model.AttributeSet;
import org.netbeans.modules.xslt.model.Stylesheet;

/**
 * @author Alex Petrov (06.06.2008)
 */
public class HandlerUseAttributeSets extends BaseCompletionHandler implements 
    XSLTCompletionConstants {
    @Override
    public List<XSLTCompletionResultItem> getResultItemList(
        XSLTEditorComponentHolder editorComponentHolder) {
        initHandler(editorComponentHolder);
        return getAttributeSetNameList();
    }
    
    private List<XSLTCompletionResultItem> getAttributeSetNameList() {
        if ((attributeName == null) || (xslModel == null)) 
            return Collections.emptyList();

        if (! XSLTCompletionUtil.ignoreNamespace(attributeName).equals(
            ATTRIBUTE_NAME_USE_ATTRIBUTE_SETS)) return Collections.emptyList();

        if ((xslModel != null) && (xslModel.getState().equals(State.NOT_WELL_FORMED))) {
            return getIncorrectDocumentResultItem();
        }
        return findAttributeSetNames();
    }
    
    private List<XSLTCompletionResultItem> findAttributeSetNames() {
        Stylesheet stylesheet = xslModel.getStylesheet();
        List<AttributeSet> attributeSetList = stylesheet.getChildren(AttributeSet.class);
        if (attributeSetList.isEmpty()) return Collections.emptyList();
        
        List<XSLTCompletionResultItem> resultItemList = 
            new ArrayList<XSLTCompletionResultItem>();
        for (AttributeSet attributeSet : attributeSetList) {
            QName valueofAttributeName = attributeSet.getName();
            if (valueofAttributeName != null) {
                String atributeSetName = valueofAttributeName.toString();
                if ((atributeSetName != null) && (atributeSetName.length() > 0)) {
                    
                    XSLTCompletionResultItem resultItem = new XSLTCompletionResultItem(
                        atributeSetName, document, caretOffset);
                    resultItem.setSortPriority(resultItemList.size());
                    resultItemList.add(resultItem);
                }
            }   
        }
        return resultItemList;
    }
}