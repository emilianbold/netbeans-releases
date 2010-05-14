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

import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.xpath.mapper.lsm.CastManager;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.xpath.mapper.context.MapperStaticContext;
import org.netbeans.modules.soa.xpath.mapper.context.XPathDesignContext;
import org.netbeans.modules.soa.xpath.mapper.lsm.ui.SubtypeChooser;
import org.netbeans.modules.soa.xpath.mapper.model.XPathMapperModel;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.openide.util.NbBundle;

/**
 * Shows the Subtype chooser dialog to create a new Custed item. 
 *
 * @author nk160297
 */
public class AddCastAction extends MapperAction<TreeItem> {
    
    private static final long serialVersionUID = 1L;
    
    private TreePath mCastedTPath; // Path to the casted object
    private GlobalType mCastedGType; // Type of the casted object

    /**
     *
     * @param castedGType the type of casted object
     * @param mapperTcContext
     * @param inLeftTree
     * @param castedTPath tree path of the casted object.
     * @param castedTreeItem
     */
    public AddCastAction(GlobalType castedGType, MapperStaticContext sContext,
            boolean inLeftTree, TreePath castedTPath,
            TreeItem castedTreeItem) {
        super(sContext, castedTreeItem, inLeftTree);
        assert castedTPath != null || castedTreeItem != null :
            "Either treePath or treeItem has to be specified"; // NOI18N
        //
        mCastedTPath = castedTPath;
        mCastedGType = castedGType;
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
        CastManager castManager = treeModel.getExtManagerHolder().getCastManager();
        if (castManager == null) {
            return ;
        }
        //
        TreeItem castedTreeItem = getActionSubject();
        SchemaComponent targetSComp = getSContext().getMapperSpi().
                getAssociatedSchemaComp(castedTreeItem.getDataObject());
        boolean simpleTypesOnly = targetSComp instanceof Attribute;
        //
        SubtypeChooser chooser = new SubtypeChooser(getSContext(),
                mCastedGType, simpleTypesOnly);
        if (!SubtypeChooser.showDlg(chooser)) {
            return; // The cancel is pressed
        }
        //
        GlobalType targetGType = chooser.getSelectedValue();
        //
        castManager.addCastCmd(targetGType, mCastedTPath, mInLeftTree, getSContext());
    }

}
