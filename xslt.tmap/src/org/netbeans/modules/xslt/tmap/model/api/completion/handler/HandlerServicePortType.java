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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xslt.tmap.model.api.WsdlDataHolder;
import org.netbeans.modules.xslt.tmap.model.api.completion.TMapCompletionResultItem;
import org.netbeans.modules.xslt.tmap.model.api.completion.TMapCompletionUtil;
import org.netbeans.modules.xslt.tmap.model.api.completion.TMapEditorComponentHolder;

/**
 * @author Alex Petrov (03.07.2008)
 */
public class HandlerServicePortType extends BaseCompletionHandler {
    //<service name="Service1" portType="ns1:getloanwsdPortType">
    
    @Override
    public List<TMapCompletionResultItem> getResultItemList(
        TMapEditorComponentHolder editorComponentHolder) {
        initHandler(editorComponentHolder);
        return getPortTypeList(TAG_NAME_SERVICE);
    }
    
    protected List<TMapCompletionResultItem> getPortTypeList(String requiredTagName) {
        if ((surroundTag == null) || (attributeName == null) || (tmapModel == null)) 
            return Collections.emptyList();
        
        String tagName = surroundTag.getTagName(); //getLocalName();
        if (! tagName.contains(requiredTagName))
            return Collections.emptyList();
        if (! attributeName.equals(ATTRIBUTE_NAME_PORTTYPE))
            return Collections.emptyList();

        if ((tmapModel != null) && (tmapModel.getState().equals(State.NOT_WELL_FORMED))) {
            return getIncorrectDocumentResultItem();
        }
        return getPortTypeList();
    }
    
    private List<TMapCompletionResultItem> getPortTypeList() {
        List<WsdlDataHolder> wsdlHolders = WsdlDataHolder.getImportedWsdlList(tmapModel);
        if ((wsdlHolders == null) || (wsdlHolders.isEmpty())) 
            return Collections.emptyList();
        
        List<TMapCompletionResultItem> 
            resultItemList = new ArrayList<TMapCompletionResultItem>();
        for (WsdlDataHolder wsdlHolder : wsdlHolders) {
            try {
                Collection<PortType> portTypes = 
                    wsdlHolder.getWsdlModel().getDefinitions().getPortTypes();                                    
                if (portTypes.size() < 1) continue;
                
                List<TMapCompletionResultItem> 
                    sectionResultItems = makeSectionResultItemList(wsdlHolder, portTypes);
                for (TMapCompletionResultItem resultItem : sectionResultItems) {
                    resultItem.setSortPriority(resultItemList.size());
                    resultItemList.add(resultItem);
                }
            } catch(Exception e) {
                TMapCompletionUtil.logExceptionInfo(e);
            }
        }
        return resultItemList;
    }
    
    private List<TMapCompletionResultItem> makeSectionResultItemList(
        WsdlDataHolder wsdlHolder, Collection<PortType> portTypes) {
        Iterator<PortType> iterator = portTypes.iterator();
        List<TMapCompletionResultItem> sectionResultItems = 
            new ArrayList<TMapCompletionResultItem>();
        while (iterator.hasNext()) {
            PortType portType = iterator.next();
            String namePortType = portType.getName();
            if (namePortType == null) continue;
                
            TMapCompletionResultItem resultItem = new ResultItemServicePortType(
                namePortType, wsdlHolder.getQNamePrefix(), document, caretOffset);
            sectionResultItems.add(resultItem);
        }
        if (sectionResultItems.size() > 0) {
            sectionResultItems.add(0, new ResultItemSectionRootNode(
                wsdlHolder.getLocation(), document, caretOffset));
        }
        return sectionResultItems;
    }
}