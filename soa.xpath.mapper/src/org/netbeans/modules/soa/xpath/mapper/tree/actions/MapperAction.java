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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.xpath.mapper.context.MapperStaticContext;
import org.netbeans.modules.soa.xpath.mapper.context.XPathDesignContext;
import org.netbeans.modules.soa.xpath.mapper.model.XPathMapperModel;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;

/**
 *
 * @author nk160297
 */
public abstract class MapperAction<SubjectType> extends AbstractAction {
    
    protected MapperStaticContext mStaticContext;
    protected SubjectType mActionSubject;
    protected boolean mInLeftTree;
    
    public MapperAction(MapperStaticContext sContext,
            SubjectType actionSubject, boolean inLeftTree) {
        super();
        mStaticContext = sContext;
        mActionSubject = actionSubject;
        mInLeftTree = inLeftTree;
    }
    
    protected void postInit() {
        putValue(Action.NAME, getDisplayName());
        putValue(Action.SMALL_ICON, getIcon());
    }
    
    public SubjectType getActionSubject() {
        return mActionSubject;
    }
    
    public String getDisplayName() {
        return "";
    }
    
    public Icon getIcon() {
        return null;
    }
    
    public MapperStaticContext getSContext() {
        return mStaticContext;
    }
    
    public XPathDesignContext getDesignContext() {
        return getSContext().getDesignContextController().getContext();
    }

    public void actionPerformed(ActionEvent e) {
        //
        // Add the new type cast to the CastManager
        MapperModel mm = mStaticContext.getMapperModel();
        assert mm instanceof XPathMapperModel;
        XPathMapperModel mapperModel = (XPathMapperModel)mm;

        MapperSwingTreeModel treeModel = null;
        if (mInLeftTree) {
            treeModel = mapperModel.getLeftTreeModel();
        } else {
            treeModel = mapperModel.getRightTreeModel();
        }
        //
        SoaTreeModel sourceMModel = treeModel.getSourceModel();
        //
        XPathDesignContext dContext =
                mStaticContext.getDesignContextController().getContext();
        assert dContext != null;
        //
        actionPerformed(mapperModel, treeModel, sourceMModel, dContext);
    }

    protected void actionPerformed(XPathMapperModel mapperModel,
            MapperSwingTreeModel treeModel, SoaTreeModel sourceMModel,
            XPathDesignContext dContext) {
        // It is implied to be redefined in descendant classes
    }
}
