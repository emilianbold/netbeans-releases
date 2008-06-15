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
import org.netbeans.modules.bpel.mapper.predicates.AbstractPredicate;
import org.netbeans.modules.bpel.mapper.predicates.editor.PredicateUpdater;
import org.netbeans.modules.bpel.mapper.predicates.editor.PathConverter;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTcContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.openide.util.NbBundle;

/**
 *
 * @author nk160297
 */
public class DeletePredicateAction extends MapperAction<Iterable<Object>> {
    
    private static final long serialVersionUID = 1L;
    private boolean mInLeftTree;
    private TreePath mTreePath;
    
    public DeletePredicateAction(MapperTcContext mapperTcContext, 
            boolean inLeftTree, TreePath treePath, 
            Iterable<Object> doItrb) {
        super(mapperTcContext, doItrb);
        mTreePath = treePath;
        mInLeftTree = inLeftTree;
        postInit();
        // putValue(DeleteAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(MapperAction.class, "DELETE_PREDICATE"); // NOI18N
    }
    
    public void actionPerformed(ActionEvent e) {
        Iterable<Object> itrb = getActionSubject();
        Object nextObj = itrb.iterator().next();
        assert nextObj instanceof AbstractPredicate;
        AbstractPredicate pred = (AbstractPredicate)nextObj;
        //
        XPathSchemaContext sContext = pred.getSchemaContext();
        if (sContext == null) {
            sContext = PathConverter.constructContext(itrb, false);
        }
        //
        PredicateUpdater updater = new PredicateUpdater(mMapperTcContext, 
                null, pred, sContext, mInLeftTree, mTreePath);
        updater.deletePredicate();
    }
    
}
