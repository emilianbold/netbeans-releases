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

package org.netbeans.modules.xslt.mapper.model.nodes.actions;

import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author nk160297
 */
public class DeleteAction extends XsltNodeAction {

    private static final long serialVersionUID = 1L;
    private static final String DELETE_KEYSTROKE = "DELETE"; // NOI18N
    
    public DeleteAction(XsltMapper xsltMapper, TreeNode node) {
        super(xsltMapper, node);
        putValue(DeleteAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(DELETE_KEYSTROKE));
    }
    
    public ActionType getType() {
        return ActionType.REMOVE;
    }

    public void actionPerformed(ActionEvent e) {
        // TODO delete
        System.out.println("DeleteAction"); 
    }

}
