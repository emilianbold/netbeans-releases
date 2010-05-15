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
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperPredicate;
import org.netbeans.modules.soa.xpath.mapper.lsm.PredicateManager;
import org.netbeans.modules.soa.xpath.mapper.lsm.ui.predicate.PredicateEditor;
import org.netbeans.modules.soa.xpath.mapper.lsm.ui.predicate.PredicateEditorFactory;
import org.netbeans.modules.soa.xpath.mapper.model.XPathMapperModel;
import org.netbeans.modules.soa.xpath.mapper.spi.MapperSpi;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.openide.util.NbBundle;

/**
 * Shows the Expression editor dialog in order to create a new predicate.
 *
 * @author nk160297
 */
public class EditPredicateAction extends MapperAction<TreeItem> {
    
    private static final long serialVersionUID = 1L;
    
    private TreePath mSubjectTPath;
    
    public EditPredicateAction(MapperStaticContext stContext,
            boolean inLeftTree, TreePath subjectTPath,
            TreeItem treeItem) {
        super(stContext, treeItem, inLeftTree);
        mSubjectTPath = subjectTPath;
        postInit();
        // putValue(DeleteAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(MapperAction.class, "EDIT_PREDICATE"); // NOI18N
    }

    @Override
    protected void actionPerformed(XPathMapperModel mapperModel,
            final MapperSwingTreeModel treeModel, SoaTreeModel sourceMModel,
            XPathDesignContext dContext) {
        //
        TreeItem treeItem = getActionSubject();
        Object dataObj = treeItem.getDataObject();
        assert dataObj instanceof MapperPredicate;
        final MapperPredicate oldPred = (MapperPredicate)dataObj;
        //
        MapperSpi mapperSpi = getSContext().getMapperSpi();
        PredicateEditorFactory psf = mapperSpi.getPredicateEditorFactory();
        final PredicateEditor editor = psf.constructPredEditor(
                getSContext(), oldPred, mInLeftTree);
        //
        // Prepare Ok processor for the PseudoComp editor
        Callable<Boolean> okProcessor = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                XPathPredicateExpression[] newPredArr =
                        editor.calculatePredicateExperArr();
                //
                PredicateManager pm = treeModel.getExtManagerHolder().
                        getPredicateManager();
                return pm.modifyPredicateCmd(oldPred, newPredArr,
                        mSubjectTPath, mInLeftTree, mStaticContext);
            }
        };
        //
        PredicateEditor.showDlg(editor, okProcessor);
    }

}
