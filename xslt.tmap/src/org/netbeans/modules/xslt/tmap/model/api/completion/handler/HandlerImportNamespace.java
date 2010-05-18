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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xslt.tmap.model.api.completion.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xslt.tmap.model.api.completion.TMapCompletionResultItem;
import org.netbeans.modules.xslt.tmap.model.api.completion.TMapCompletionUtil;
import org.netbeans.modules.xslt.tmap.model.api.completion.TMapEditorComponentHolder;

/**
 * @author Alex Petrov (06.06.2008)
 */
public class HandlerImportNamespace extends BaseCompletionHandler {
    //<import namespace="http://j2ee.netbeans.org/wsdl/commentWSDL" location="commentWSDL.wsdl"/>
    protected static final String
        TAG_NAME_IMPORT = "import",
        ATTRIBUTE_NAME_NAMESPACE = "namespace",
        ATTRIBUTE_NAME_LOCATION = "location";
    
    @Override
    public List<TMapCompletionResultItem> getResultItemList(
        TMapEditorComponentHolder editorComponentHolder) {
        initHandler(editorComponentHolder);
        return getImportNamespaceList();
    }
    
    private List<TMapCompletionResultItem> getImportNamespaceList() {
        if ((surroundTag == null) || (attributeName == null) || (tmapModel == null)) 
            return Collections.emptyList();
        
        String tagName = surroundTag.getTagName(); //getLocalName();
        if (! tagName.contains(TAG_NAME_IMPORT))
            return Collections.emptyList();
        if (! attributeName.equals(ATTRIBUTE_NAME_NAMESPACE))
            return Collections.emptyList();

        if ((tmapModel != null) && (tmapModel.getState().equals(State.NOT_WELL_FORMED))) {
            return getIncorrectDocumentResultItem();
        }

        String targetNamspace = getTargetNamespace();
        if ((targetNamspace == null) || 
            (targetNamspace.length() < 1)) return Collections.emptyList();
        
        List<TMapCompletionResultItem> resultItemList = 
            new ArrayList<TMapCompletionResultItem>();
        TMapCompletionResultItem resultItem = new TMapCompletionResultItem(
            targetNamspace, document, caretOffset);
        resultItem.setSortPriority(resultItemList.size());
        resultItemList.add(resultItem);
        
        return resultItemList;
    }
    
    protected String getTargetNamespace() {
        if (surroundTag == null) return null;
        String attributeLocation = TMapCompletionUtil.valueofAttribute(surroundTag, 
            ATTRIBUTE_NAME_LOCATION);
        if (attributeLocation == null) return null;
        
        WSDLModel wsdlModel = TMapCompletionUtil.getWsdlModel(tmapModel, attributeLocation, false);    
        if (wsdlModel == null) return null;
        
        String targetNamspace = wsdlModel.getDefinitions().getTargetNamespace();
        return targetNamspace;
    }
}