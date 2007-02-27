/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.identity.profile.ui;

import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.openide.nodes.Node;

/**
 * Represents a security view in the web service attribute editor.
 *
 * Created on April 18, 2006, 1:27 PM
 *
 * @author ptliu
 */
public abstract class SecurityView extends SectionNodeView {
    
    public SecurityView() {
        super(null);
    }
    
    public void dataModelPropertyChange(Object source, String propertyName,
            Object oldValue, Object newValue) {
        /**
         * if (oldValue instanceof EnterpriseBeans || newValue instanceof EnterpriseBeans) {
         * scheduleRefreshView();
         * }
         * super.dataModelPropertyChange(source, propertyName, oldValue, newValue);
         */
    }
    
    public XmlMultiViewDataSynchronizer getModelSynchronizer() {
        return null;
    }
    
    public void refreshView() {
        super.refreshView();
    }
    
    protected ToolBarDesignEditor getToolBarDesignEditor() {
        return null;
    }
    
    public void selectNode(Node node) {
        //
        // RESOLVE:
        // This method is overridden to work around the problem
        // where the select node does not belong to the rootContext.
        // Need to investigate further.
        //
        // super.selectNode(node);
    }
    
    public void save() {
        SecuritySectionNode node = (SecuritySectionNode) this.getRootNode();
        node.save();
    }
    
    public void cancel() {
        SecuritySectionNode node = (SecuritySectionNode) this.getRootNode();
        node.cancel();
    }
}
