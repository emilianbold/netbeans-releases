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
package org.netbeans.modules.soa.xpath.mapper.tree.models;

import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;
import org.netbeans.modules.soa.ui.tree.TreeItemActionsProvider;
import org.openide.util.ImageUtilities;

/**
 * The implementation of the TreeItemInfoProvider for target tree 
 * with simple content (Wait, For, If, ...).
 * 
 * @author nk160297
 */
public class SimpleTreeInfoProvider 
        implements TreeItemInfoProvider, TreeItemActionsProvider {

    public static Icon RESULT_IMAGE = new ImageIcon(ImageUtilities.loadImage(
            "org/netbeans/modules/soa/xpath/mapper/tree/models/RESULT.png"));
    
    private static SimpleTreeInfoProvider singleton = new SimpleTreeInfoProvider();
    
    public static SimpleTreeInfoProvider getInstance() {
        return singleton;
    }
    
    public String getDisplayName(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        return dataObj.toString();
    }

    public Icon getIcon(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        //
        if (dataObj instanceof String) {
            return RESULT_IMAGE;
        }
        //
        return TreeItemInfoProvider.UNKNOWN_IMAGE;
    }

    public List<Action> getMenuActions(TreeItem treeItem, 
            Object context, TreePath treePath) {
        return null;
    }

    public String getToolTipText(TreeItem treeItem) {
        return null;
    }
}
