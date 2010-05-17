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

package org.netbeans.modules.bpel.mapper.tree.actions;

import org.netbeans.modules.soa.xpath.mapper.tree.actions.MapperAction;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.model.BpelMapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.mapper.properties.AddEditNMPropertyPane;
import org.netbeans.modules.bpel.mapper.properties.CreateNMPropertyCallable;
import org.netbeans.modules.bpel.mapper.properties.PropertiesNode;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.ext.editor.api.NMProperty;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperTreeNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class AddNMPropertyAction extends MapperAction<TreeItem> {
    
    private static final long serialVersionUID = 1L;
    private TreePath treePath;
    
    public AddNMPropertyAction(MapperTcContext mapperTcContext,
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
        return NbBundle.getMessage(AddNMPropertyAction.class,
                "ADD_NM_PROPERTY_SHORTCUT"); // NOI18N
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        TreeItem treeItem = getActionSubject();
        
        AddEditNMPropertyPane addEditPanel = new AddEditNMPropertyPane();
        
        DialogDescriptor dialogDescriptor = new DialogDescriptor(addEditPanel, 
                NbBundle.getMessage(AddNMPropertyAction.class,
                "ADD_NM_PROPERTY_SHORTCUT_DIALOG_TITLE")); // NOI18N
        
        addEditPanel.setDialogDescriptor(dialogDescriptor);
        
        DialogDisplayer.getDefault().createDialog(dialogDescriptor)
                .setVisible(true);

        addEditPanel.setDialogDescriptor(null);
        
        if (dialogDescriptor.getValue() != DialogDescriptor.OK_OPTION) {
            return;
        }
        
        ExtensibleElements variable = findVariableDeclaration();
        
        if (variable == null) {
            return;
        }
        
        BpelModel bpelModel = variable.getBpelModel();
        BPELElementsBuilder builder = bpelModel.getBuilder();
        
        String nmPropertyName = addEditPanel.getNMPropeprtyName();
        String displayName = addEditPanel.getDisplayName();
        
        NMProperty nmProperty = null;
        try {
            nmProperty = bpelModel.invoke(new CreateNMPropertyCallable(
                    builder, variable, nmPropertyName, displayName, 
                    mInLeftTree), getSContext());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        if (nmProperty == null) {
            return;
        }
        
        MapperModel mapperModel = getSContext().getMapperModel();
        BpelMapperModel bpelMm = BpelMapperModel.class.cast(mapperModel);
        BpelMapperSwingTreeModel treeModel = (mInLeftTree)
                ? bpelMm.getLeftTreeModel() : bpelMm.getRightTreeModel();
                
        TreePath propertiesTreePath = findPropertiesTreePath(treePath);

        treeModel.insertChild(propertiesTreePath, 0, 
                nmProperty);

        TreePath treePathToSelect = propertiesTreePath.pathByAddingChild(
                treeModel.getChild(propertiesTreePath
                .getLastPathComponent(), 0));
        
        Mapper mapper = getSContext().getMapper();
        if (mapper != null) {
            if (mInLeftTree) {
                mapper.getLeftTree().setSelectionPath(treePathToSelect);
            } else {
                List<TreePath> list = new ArrayList<TreePath>();
                TreePath path = treePathToSelect.getParentPath();
                while (path != null) {
                    list.add(path);
                    path = path.getParentPath();
                }
                mapper.applyExpandedPathes(list);
                mapper.setSelected(treePathToSelect);
            }
        }
    }
    
    private List<FileObject> getFileObjects() {
        List<FileObject> result = new ArrayList<FileObject>();
        TreePath path = this.treePath;
        
        while (path != null) {
            MapperTreeNode node = (MapperTreeNode) path.getLastPathComponent();
            Object dataObj = node.getDataObject();
            
            if (dataObj instanceof FileObject) {
                result.add(0, (FileObject) dataObj);
            }  else if (dataObj instanceof PropertiesNode) {
                break;
            }
            
            path = path.getParentPath();
        }
        
        return result;
    }
    
    private ExtensibleElements findVariableDeclaration() {
        TreePath path = treePath;
        while (path != null) {
            Object node = path.getLastPathComponent();
            if (node instanceof MapperTreeNode) {
                Object dataObject = ((MapperTreeNode) node).getDataObject();
                if (dataObject instanceof ExtensibleElements) {
                    return (ExtensibleElements) dataObject;
                }
            }
            path = path.getParentPath();
        }
        return null;
    }
    
    private TreePath findPropertiesTreePath(TreePath treePath) {
        while (treePath != null) {
            MapperTreeNode node = (MapperTreeNode) treePath
                    .getLastPathComponent();
            
            if (node.getDataObject() instanceof PropertiesNode) {
                return treePath;
            }
            
            treePath = treePath.getParentPath();
        }
        
        return null;
    }
}
