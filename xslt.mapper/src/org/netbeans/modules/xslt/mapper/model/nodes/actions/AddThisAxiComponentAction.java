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

import java.awt.event.ActionEvent;
import org.netbeans.modules.xslt.mapper.model.BranchConstructor;
import org.netbeans.modules.xslt.mapper.model.targettree.SchemaNode;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.netbeans.modules.xslt.model.XslModel;
import org.openide.util.NbBundle;

/**
 * Adds the specified schema component to the XSLT.
 *
 * @author nk160297
 */
public class AddThisAxiComponentAction extends XsltNodeAction {
    
    private static final long serialVersionUID = 1L;
    
    public AddThisAxiComponentAction(XsltMapper xsltMapper, SchemaNode node) {
        super(xsltMapper, node);
        postInit();
        // putValue(DeleteAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(ActionConst.class, "ADD_THIS"); // NOI18N
    }
    
    public void actionPerformed(ActionEvent e) {
        XslModel model = myXsltMapper.getContext().getXSLModel();
                Object dataObject = myTreeNode.getDataObject();
 
        new BranchConstructor((SchemaNode)myTreeNode, super.getMapper()).construct();
    }
    
}
