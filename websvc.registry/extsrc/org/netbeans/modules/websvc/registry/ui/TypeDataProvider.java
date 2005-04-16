/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.ui;

import org.netbeans.swing.outline.RenderDataProvider;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import com.sun.xml.rpc.processor.model.java.JavaType;
import com.sun.xml.rpc.processor.model.java.JavaEnumerationType;
import com.sun.xml.rpc.processor.model.java.JavaEnumerationEntry;
import com.sun.xml.rpc.processor.model.java.JavaSimpleType;
import com.sun.xml.rpc.processor.model.java.JavaStructureMember;
import com.sun.xml.rpc.processor.model.java.JavaStructureType;
import javax.swing.tree.*;

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
