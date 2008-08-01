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

package org.netbeans.modules.bpel.mapper.cast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.support.BpelExternalModelResolver;
import org.netbeans.modules.soa.ui.tree.SoaTreeExtensionModel;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.TreeItemActionsProvider;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;
import org.netbeans.modules.soa.ui.tree.TreeStructureProvider;
import org.netbeans.modules.soa.ui.schema.SchemaTreeInfoProvider;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xpath.ext.schema.TypeInheritanceUtil;
import org.netbeans.modules.xml.xpath.ext.spi.ExternalModelResolver;

/**
 * 
 * @author nk160297
 */
public class TypeCastTreeModel implements SoaTreeModel, 
        TreeStructureProvider, TreeItemInfoProvider {

    private GlobalType mBaseType;
    private Map<GlobalType, GlobalType> mDerivationMap;

    public TypeCastTreeModel(GlobalType baseType, BpelModel bpelModel) {
        mBaseType = baseType;
        //
        ExternalModelResolver modelResolver = 
                new BpelExternalModelResolver(bpelModel);
        mDerivationMap = TypeInheritanceUtil.populateDerivationMap(modelResolver);
    }
    
    public Object getRoot() {
        return mBaseType;
    }

    public List<SoaTreeExtensionModel> getExtensionModelList() {
        return null;
    }

    public TreeStructureProvider getTreeStructureProvider() {
        return this;
    }

    public TreeItemInfoProvider getTreeItemInfoProvider() {
        return this;
    }

    public TreeItemActionsProvider getTreeItemActionsProvider() {
        return null;
    }

    //--------------------------------------------------------------------------
    
    public List<Object> getChildren(TreeItem treeItem) {
        List<Object> result = new ArrayList<Object>();
        //
        Object dataObj = treeItem.getDataObject();
        if (dataObj instanceof GlobalType) {
            assert dataObj instanceof GlobalType;
            Set<GlobalType> subtypes = TypeInheritanceUtil.
                    getDirectSubtypes((GlobalType)dataObj, mDerivationMap);
            result.addAll(subtypes);
        }
        //
        return result;
    }

    public Boolean isLeaf(TreeItem treeItem) {
        List childrent = getChildren(treeItem);
        return childrent == null || childrent.isEmpty();
    }

    public String getDisplayName(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        //
        String result = null;
        if (dataObj instanceof SchemaComponent) {
            result = SchemaTreeInfoProvider.getInstance().getDisplayName(treeItem);
        }
        //
        if (result == null) {
            result = dataObj.toString();
        }
        //
        return result;
    }

    public Icon getIcon(TreeItem treeItem) {
        Object dataObject = treeItem.getDataObject();
        //
        if (dataObject instanceof SchemaComponent) {
            Icon result = SchemaTreeInfoProvider.getInstance().getIcon(treeItem);
            if (result != null) {
                return result;
            }
        }
        //
        return null;
    }

    public String getToolTipText(TreeItem treeItem) {
        String result = SchemaTreeInfoProvider.getInstance().getToolTipText(treeItem);
        if (result != null) {
            return result;
        }
        //
        return null;
    }

}
