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


package org.netbeans.modules.websvc.manager.ui;

import javax.swing.tree.DefaultMutableTreeNode;
import org.netbeans.swing.outline.RenderDataProvider;

/**
 *
 * @author  David Botterill
 */
public class TypeDataProvider implements RenderDataProvider {
    
    /** Creates a new instance of TypeDataProvider */
    public TypeDataProvider() {
    }
    
    public java.awt.Color getBackground(Object o) {
        
        return null;
    }
    
    public String getDisplayName(Object inNode) {
        if(null == inNode) return null;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)inNode;
        if(null == node.getUserObject()) return null;
        NodeData data = (NodeData)node.getUserObject();
        return data.getNodeType().getFormalName();
        
    }
    
    public java.awt.Color getForeground(Object o) {
        return null;
    }
    
    public javax.swing.Icon getIcon(Object o) {
        return null;
    }
    
    public String getTooltipText(Object o) {
        return null;
    }
    
    public boolean isHtmlDisplayName(Object o) {
        return false;
    }
    
}
