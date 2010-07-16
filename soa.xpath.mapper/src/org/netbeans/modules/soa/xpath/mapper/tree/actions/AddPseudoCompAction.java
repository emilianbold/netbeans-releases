/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.xpath.mapper.tree.actions;

import java.util.concurrent.Callable;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.xpath.mapper.context.MapperStaticContext;
import org.netbeans.modules.soa.xpath.mapper.context.XPathDesignContext;
import org.netbeans.modules.soa.xpath.mapper.lsm.DetachedPseudoComp;
import org.netbeans.modules.soa.xpath.mapper.lsm.PseudoCompManager;
import org.netbeans.modules.soa.xpath.mapper.lsm.ui.PseudoCompEditor;
import org.netbeans.modules.soa.xpath.mapper.model.XPathMapperModel;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.openide.util.NbBundle;

/**
 * Shows the Type Chooser dialog to create a new Pseudo schema component based on 
 * Any or AnyAttribute. 
 *
 * @author nk160297
 */
public class AddPseudoCompAction extends MapperAction<TreeItem> {
    
    private static final long serialVersionUID = 1L;
    
    private TreePath mXsdAnyTreePath;
    
    private AnyElement mAnyElement;
    private AnyAttribute mAnyAttr;
    private boolean mIsAttribute;
    
    public AddPseudoCompAction(AnyElement anyElement, 
            MapperStaticContext sContext,
            boolean inLeftTree, TreePath xsdAnyTreePath,
            TreeItem xsdAnyTreeItem) {
        super(sContext, xsdAnyTreeItem, inLeftTree);
        mXsdAnyTreePath = xsdAnyTreePath;
        mAnyElement = anyElement;
        mIsAttribute = false;
        postInit();
    }
    
    public AddPseudoCompAction(AnyAttribute anyAttr, 
            MapperStaticContext sContext,
            boolean inLeftTree, TreePath treePath, 
            TreeItem treeItem) {
        super(sContext, treeItem, inLeftTree);
        mXsdAnyTreePath = treePath;
        mAnyAttr = anyAttr;
        mIsAttribute = true;
        postInit();
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(MapperAction.class, "ADD_CAST"); // NOI18N
    }
    
    @Override
    protected void actionPerformed(XPathMapperModel mapperModel,
            MapperSwingTreeModel treeModel, SoaTreeModel sourceMModel,
            XPathDesignContext dContext) {
        //
        // The  iterator points to an xsd:any or xsd:anyAttribute component
        final TreeItem xsdAnyTreeItem = getActionSubject();
        //
        final PseudoCompManager pcm =
                treeModel.getExtManagerHolder().getPseudoCompManager();
        if (pcm == null) {
            return ;
        }
        //
        PseudoCompEditor editor = null;
        if (mIsAttribute) {
            editor = new PseudoCompEditor(getSContext(), xsdAnyTreeItem,
                    mAnyAttr, mapperModel, mInLeftTree);
        } else {
            editor = new PseudoCompEditor(getSContext(), xsdAnyTreeItem,
                    mAnyElement, mapperModel, mInLeftTree);
        }
        //
        final PseudoCompEditor finalEditor = editor;
        //
        // Prepare Ok processor for the PseudoComp editor
        Callable<Boolean> okProcessor = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                DetachedPseudoComp newDPC = finalEditor.getSelectedValue();
                return pcm.addPseudoCompCmd(newDPC, mXsdAnyTreePath,
                        mInLeftTree, mStaticContext);
            }
        };
        //
        PseudoCompEditor.showDlg(editor, okProcessor);
    }

}
