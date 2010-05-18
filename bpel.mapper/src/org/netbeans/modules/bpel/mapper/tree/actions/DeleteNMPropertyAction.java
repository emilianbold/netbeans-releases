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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.mapper.properties.DeleteNMPropertyCallable;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.ext.editor.api.NMProperty;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class DeleteNMPropertyAction extends MapperAction<TreeItem> {
    private static final long serialVersionUID = 1L;
    private TreePath treePath;
    
    public DeleteNMPropertyAction(MapperTcContext mapperTcContext,
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
        return NbBundle.getMessage(DeleteNMPropertyAction.class,
                "DELETE_NM_PROPERTY_SHORTCUT"); // NOI18N
    }
    
    public void actionPerformed(ActionEvent e) {
        TreeItem treeItem = getActionSubject();

        Object dataObj = treeItem.getDataObject();
        
        if (!(dataObj instanceof NMProperty)) {
            return;
        }
        
        NMProperty nmProperty = (NMProperty) dataObj;

        //ExtensibleElements variable = findVariableDeclaration();
        
        BpelMapperModel bpelMapperModel =
                (BpelMapperModel) getSContext().getMapperModel();
        
        if (mInLeftTree) {
            Map<TreePath, Graph> graphsMap = bpelMapperModel.getGraphsInside(null);
            if (graphsMap != null) {
                for (TreePath graphTreePath : graphsMap.keySet()) {
                    bpelMapperModel.removeIngoingLinks(graphTreePath, treePath);
                }
            }
        } else {
            bpelMapperModel.removeNestedGraphs(treePath);
        }
        
        BpelModel bpelModel = nmProperty.getBpelModel();
        
        Boolean ok = Boolean.FALSE;
        
        try {
            ok = bpelModel.invoke(new DeleteNMPropertyCallable(nmProperty), 
                    getSContext());
        } catch (Exception ex) {
//            ex.printStackTrace();
        }
        
        MapperSwingTreeModel treeModel = (mInLeftTree)
                ? bpelMapperModel.getLeftTreeModel()
                : bpelMapperModel.getRightTreeModel();
        
        treeModel.remove(treePath);
        
        Map<TreePath, Graph> map = bpelMapperModel.getGraphsInside(null);
        if (map != null) {
            Set<TreePath> updateSet = new HashSet<TreePath>(map.keySet());
            bpelMapperModel.fireGraphsChanged(updateSet);
        }
    }
}
