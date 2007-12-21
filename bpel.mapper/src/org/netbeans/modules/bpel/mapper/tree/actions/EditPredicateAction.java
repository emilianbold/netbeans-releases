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

package org.netbeans.modules.bpel.mapper.tree.actions;

import java.awt.event.ActionEvent;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.predicates.AbstractPredicate;
import org.netbeans.modules.bpel.mapper.predicates.editor.PredicateEditor;
import org.netbeans.modules.bpel.mapper.predicates.editor.PredicateMapperModelFactory;
import org.netbeans.modules.bpel.mapper.predicates.editor.PredicateUpdater;
import org.netbeans.modules.bpel.mapper.predicates.editor.PathConverter;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTcContext;
import org.netbeans.modules.bpel.mapper.tree.spi.RestartableIterator;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContext;
import org.openide.util.NbBundle;

/**
 * Shows the Expression editor dialog in order to create a new predicate.
 *
 * @author nk160297
 */
public class EditPredicateAction extends MapperAction<RestartableIterator<Object>> {
    
    private static final long serialVersionUID = 1L;
    
    private TreePath mTreePath;
    private boolean mInLeftTree;
    
    public EditPredicateAction(MapperTcContext mapperTcContext, 
            boolean inLeftTree, TreePath treePath, 
            RestartableIterator<Object> doItr) {
        super(mapperTcContext, doItr);
        mTreePath = treePath;
        mInLeftTree = inLeftTree;
        postInit();
        // putValue(DeleteAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(MapperAction.class, "EDIT_PREDICATE"); // NOI18N
    }
    
    public void actionPerformed(ActionEvent e) {
        RestartableIterator<Object> itr = getActionSubject();
        itr.restart();
        Object nextObj = itr.next();
        assert nextObj instanceof AbstractPredicate;
        AbstractPredicate pred = (AbstractPredicate)nextObj;
        //
        XPathSchemaContext sContext = pred.getContext();
        if (sContext == null) {
            sContext = PathConverter.constructContext(itr);
        }
        //
        // Create new mapper TC context
        MapperTcContext wrapper = new MapperTcContext.Wrapper(mMapperTcContext);
        //
        PredicateMapperModelFactory modelFactory = new PredicateMapperModelFactory();
        BpelMapperModel predMModel = modelFactory.constructModel(
                wrapper, sContext, pred);
        //
        PredicateEditor editor = new PredicateEditor(sContext, pred, predMModel);
        wrapper.setMapper(editor.getMapper());
        //
        if (PredicateEditor.showDlg(editor)) {
            PredicateUpdater updater = new PredicateUpdater(mMapperTcContext, 
                    predMModel, pred, sContext, mInLeftTree, mTreePath);
            updater.updateOnChanges();
        }
    }
    
}
