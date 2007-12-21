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

package org.netbeans.modules.xslt.mapper.model.nodes.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;

/**
 *
 * @author nk160297
 */
public abstract class XsltNodeAction extends AbstractAction {
    
    protected XsltMapper myXsltMapper;
    protected TreeNode myTreeNode;
    
    public XsltNodeAction(XsltMapper xsltMapper, TreeNode node) {
        super();
        myXsltMapper = xsltMapper;
        myTreeNode = node;
    }
    
    protected void postInit() {
        putValue(Action.NAME, getDisplayName());
        putValue(Action.SMALL_ICON, getIcon());
    }
    
    public String getDisplayName() {
        return "";
    }
    
    public Icon getIcon() {
        return null;
    }
    public XsltMapper getMapper(){
        return myXsltMapper;
    }
}
