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
import org.netbeans.modules.bpel.mapper.cast.CastManager;
import org.netbeans.modules.bpel.mapper.cast.PseudoCompManager;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.predicates.editor.PredicateEditor;
import org.netbeans.modules.bpel.mapper.predicates.editor.PredicateMapperModelFactory;
import org.netbeans.modules.bpel.mapper.predicates.editor.PredicateUpdater;
import org.netbeans.modules.bpel.mapper.predicates.editor.PathConverter;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTcContext;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.openide.util.NbBundle;

/**
 * Shows the Expression editor dialog in order to create a new predicate.
 *
 * @author nk160297
 */
public class AddPredicateAction extends MapperAction<Iterable<Object>> {
    
    private static final long serialVersionUID = 1L;
    private boolean mInLeftTree;
    private TreePath mTreePath;
    
    public AddPredicateAction(MapperTcContext mapperTcContext,
            boolean inLeftTree, TreePath treePath, 
            Iterable<Object> dataObjectPathItrb) {
        super(mapperTcContext, dataObjectPathItrb);
        mTreePath = treePath;
        mInLeftTree = inLeftTree;
        postInit();
        // putValue(DeleteAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(MapperAction.class, "ADD_PREDICATE"); // NOI18N
    }
    
    public void actionPerformed(ActionEvent e) {
        Iterable<Object> itrb = getActionSubject();
        //
        // Construct a new Predicate Context by the current element
        XPathSchemaContext sContext = PathConverter.constructContext(itrb, false);
        if (sContext == null) {
            return;
        }
        //
        // Create new mapper TC context
        MapperTcContext wrapper = new MapperTcContext.Wrapper(mMapperTcContext);
        //
        // Obtain CastManager and PseudoCompManager from the main mapper's left tree
        MapperModel mm = getContext().getMapper().getModel();
        CastManager castManager = CastManager.getCastManager((BpelMapperModel)mm, true);
        PseudoCompManager pseudoCompManager = 
                PseudoCompManager.getPseudoCompManager((BpelMapperModel)mm, true);
        //
        PredicateMapperModelFactory modelFactory = new PredicateMapperModelFactory();
        BpelMapperModel predMModel = modelFactory.constructEmptyModel(
                wrapper, castManager, pseudoCompManager);
        //
        PredicateEditor editor = new PredicateEditor(sContext, null, predMModel);
        wrapper.setMapper(editor.getMapper());
        //
        if (PredicateEditor.showDlg(editor)) {
            PredicateUpdater updater = new PredicateUpdater(mMapperTcContext, 
                    predMModel, null, sContext, mInLeftTree, mTreePath);
            updater.addPredicate(itrb);
        }
    }
    
}
