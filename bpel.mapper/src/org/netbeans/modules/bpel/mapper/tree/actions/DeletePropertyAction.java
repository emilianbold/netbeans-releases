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

package org.netbeans.modules.bpel.mapper.tree.actions;

import org.netbeans.modules.soa.xpath.mapper.tree.actions.MapperAction;
import java.awt.event.ActionEvent;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.mapper.properties.PropertiesConstants;
import org.netbeans.modules.bpel.mapper.tree.search.CorrelationPropertiesFinder;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.TreeItemFinder;
import org.netbeans.modules.soa.ui.tree.impl.TreeFinderProcessor;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class DeletePropertyAction extends MapperAction<TreeItem> {
    
    private static final long serialVersionUID = 1L;
    private TreePath treePath;
    
    public DeletePropertyAction(MapperTcContext mapperTcContext,
            boolean inLeftTree, TreePath treePath,
            TreeItem treeItem) 
    {
        super(mapperTcContext, treeItem, inLeftTree);
        this.treePath = treePath;
        postInit();
        // putValue(DeleteAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(DeletePropertyAction.class,
                "DELETE_NM_PROPERTY"); // NOI18N
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        CorrelationProperty correlationProperty = (CorrelationProperty) 
                getActionSubject().getDataObject();
        
        PropertyAlias propertyAlias = findPropertyAlias(correlationProperty);
        
        if (propertyAlias == null) {
            return;
        }
        
        BpelMapperModel bpelMapperModel =
                (BpelMapperModel) getSContext().getMapperModel();
        
        MapperSwingTreeModel leftTreeModel = bpelMapperModel.getLeftTreeModel();
        MapperSwingTreeModel rightTreeModel = bpelMapperModel.getRightTreeModel();
        
        TreeItemFinder finder = new CorrelationPropertiesFinder(
                correlationProperty);
        
        List<TreePath> leftResults = new TreeFinderProcessor(leftTreeModel)
                .findAllTreePaths(finder);
        
        List<TreePath> rightResults = new TreeFinderProcessor(rightTreeModel)
                .findAllTreePaths(finder);
        
        if (leftResults != null && !leftResults.isEmpty()) {
            Map<TreePath, Graph> graphsMap = bpelMapperModel.getGraphsInside(null);
            if (graphsMap != null) {
                for (TreePath graphTreePath : graphsMap.keySet()) {
                    for (TreePath path : leftResults) {
                        bpelMapperModel.removeIngoingLinks(graphTreePath, path);
                    }
                }
            }
            
        }
        
        if (rightResults != null && !rightResults.isEmpty()) {
            for (TreePath path : rightResults) {
                bpelMapperModel.removeNestedGraphs(path);
            }
        }
        
        Map<TreePath, Graph> map = bpelMapperModel.getGraphsInside(null);
        if (map != null) {
            Set<TreePath> updateSet = new HashSet<TreePath>(map.keySet());
            bpelMapperModel.fireGraphsChanged(updateSet);
        }
        
        WSDLModel wsdlModel1 = propertyAlias.getModel();
        WSDLModel wsdlModel2 = correlationProperty.getModel();
        Definitions definitions1 = (Definitions) propertyAlias.getParent();
        Definitions definitions2 = (Definitions) correlationProperty.getParent();
        
        if (wsdlModel1 != null && definitions1 != null 
                && wsdlModel1 == wsdlModel2 && definitions1 == definitions2) 
        {
            if (wsdlModel1.startTransaction()) {
                try {
                    definitions1.removeExtensibilityElement(correlationProperty);
                    definitions1.removeExtensibilityElement(propertyAlias);
                } finally {
                    wsdlModel1.endTransaction();
                }
            }
        }
        
        getSContext().getDesignContextController().reloadMapper();
    }
    
    public static boolean isApplicable(CorrelationProperty correlationProperty) 
    {
        return findPropertyAlias(correlationProperty) != null;
    }
    
    private static PropertyAlias findPropertyAlias(
                CorrelationProperty correlationProperty) 
    {
        WSDLModel wsdlModel = correlationProperty.getModel();
        if (wsdlModel == null) {
            return null;
        }
        
        Definitions definitions = wsdlModel.getDefinitions();
        if (definitions == null) {
            return null;
        }
        
        List<PropertyAlias> propertyAliases = definitions
                .getExtensibilityElements(PropertyAlias.class);
        
        if (propertyAliases == null) {
            return null;
        }
        
        PropertyAlias propertyAlias = null;
        
        for (PropertyAlias alias : propertyAliases) {
            NamedComponentReference<CorrelationProperty> propertyRef = alias
                    .getPropertyName();
            if (propertyRef == null) {
                continue;
            }
            
            CorrelationProperty property = propertyRef.get();
            if (property == null) {
                continue;
            }
            
            if (correlationProperty != property) {
                continue;
            }
            
            String nmProperty = alias
                    .getAnyAttribute(PropertiesConstants.NM_PROPERTY_QNAME);
            if (nmProperty == null) {
                return null;
            }
            
            if (propertyAlias == null) {
                propertyAlias = alias;
            } else {
                return null;
            }
        }
        
        return propertyAlias;
    }
}