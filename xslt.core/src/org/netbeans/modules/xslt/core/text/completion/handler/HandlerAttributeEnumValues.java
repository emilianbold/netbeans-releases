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
import org.netbeans.modules.xslt.core.text.completion.XSLTCompletionResultItem;
import org.netbeans.modules.xslt.core.text.completion.XSLTCompletionUtil;
import org.netbeans.modules.xslt.core.text.completion.XSLTEditorComponentHolder;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.Enumeration;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.NamedReferenceable;

/**
 * @author Alex Petrov (06.06.2008)
 */
public class HandlerAttributeEnumValues extends BaseCompletionHandler {
    @Override
    public List<XSLTCompletionResultItem> getResultItemList(
        XSLTEditorComponentHolder editorComponentHolder) {
        initHandler(editorComponentHolder);
        if (schemaModel == null) return Collections.emptyList();
        return getAttributeEnumValueList();
    }
    
    private List<XSLTCompletionResultItem> getAttributeEnumValueList() {
        if ((schemaModel == null) || (surroundTag == null) || (attributeName == null)) 
            return Collections.emptyList();
        
        NamedReferenceable<SchemaComponent> refSchemaComponent = 
            schemaModel.findByNameAndType(XSLTCompletionUtil.ignoreNamespace(
            surroundTag.getTagName()), GlobalElement.class);
        if (refSchemaComponent == null) return Collections.emptyList();    
        
        List<SchemaComponent> children = refSchemaComponent.getChildren();
        List<Attribute> attributes = XSLTCompletionUtil.collectChildrenOfType(
            children, Attribute.class);

        String attrTypeName = XSLTCompletionUtil.getAttributeType(
            attributes, attributeName);
        if (attrTypeName == null) return Collections.emptyList(); 

        attrTypeName = XSLTCompletionUtil.ignoreNamespace(attrTypeName);
        refSchemaComponent = schemaModel.findByNameAndType(
            attrTypeName, GlobalSimpleType.class);

        children = refSchemaComponent.getChildren();
        List enumerations = XSLTCompletionUtil.collectChildrenOfType(
            children, Enumeration.class);

        if ((enumerations == null) || (enumerations.isEmpty())) 
            return Collections.emptyList(); 
            
        List<XSLTCompletionResultItem> resultItemList = 
            new ArrayList<XSLTCompletionResultItem>();
        for (Object objEnum : enumerations) {
            String optionName = ((Enumeration) objEnum).getValue();
            resultItemList.add(new XSLTCompletionResultItem(optionName, 
                document, caretOffset));
        }
        return resultItemList;
    }    
}