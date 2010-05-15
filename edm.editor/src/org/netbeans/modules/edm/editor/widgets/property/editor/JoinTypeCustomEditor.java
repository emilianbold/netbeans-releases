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
package org.netbeans.modules.edm.editor.widgets.property.editor;

import java.beans.PropertyEditorSupport;

import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;

import org.netbeans.modules.edm.editor.widgets.property.JoinNode;
import org.netbeans.modules.edm.model.SQLConstants;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 *
 * @author Nithya
 */
public class JoinTypeCustomEditor extends PropertyEditorSupport implements
        ExPropertyEditor {

    private JoinNode node;
    private PropertyEnv env;
    private static final Logger mLogger = Logger.getLogger(JoinTypeCustomEditor.class.getName());

    public JoinTypeCustomEditor() {
        initialize();
    }

    @Override
    public String[] getTags() {
        String[] tags = {NbBundle.getMessage(JoinTypeCustomEditor.class, "LBL_INNER_JOIN"), NbBundle.getMessage(JoinTypeCustomEditor.class, "LBL_LEFT_OUTER_JOIN"),
            NbBundle.getMessage(JoinTypeCustomEditor.class, "LBL_RIGHT_OUTER_JOIN"), NbBundle.getMessage(JoinTypeCustomEditor.class, "LBL_FULL_OUTER_JOIN")
        };
        return tags;
    }

    @Override
    public Object getValue() {
        String type = "";
        if (node == null) {
            initialize();
        }
        int joinType = node.getJoinOperator().getJoinType();
        switch (joinType) {
            case SQLConstants.INNER_JOIN:
                type = "INNER JOIN";
                break;
            case SQLConstants.LEFT_OUTER_JOIN:
                type = "LEFT OUTER JOIN";
                break;
            case SQLConstants.RIGHT_OUTER_JOIN:
                type = "RIGHT OUTER JOIN";
                break;
            case SQLConstants.FULL_OUTER_JOIN:
                type = "FULL OUTER JOIN";
        }
        return type;
    }

    @Override
    public String getAsText() {
        return (String) getValue();
    }

    @Override
    public void setValue(Object object) {
        String type = (String) object;
        if (type.equals("INNER JOIN")) {
            node.getJoinOperator().setJoinType(SQLConstants.INNER_JOIN);
        } else if (type.equals("LEFT OUTER JOIN")) {
            node.getJoinOperator().setJoinType(SQLConstants.LEFT_OUTER_JOIN);
        } else if (type.equals("RIGHT OUTER JOIN")) {
            node.getJoinOperator().setJoinType(SQLConstants.RIGHT_OUTER_JOIN);
        } else if (type.equals("FULL OUTER JOIN")) {
            node.getJoinOperator().setJoinType(SQLConstants.FULL_OUTER_JOIN);
        }
        //node.getMashupDataObject().getMashupDataEditorSupport().synchDocument();
        node.getMashupDataObject().getModel().setDirty(true);
        node.getMashupDataObject().setModified(true);
    }

    @Override
    public void setAsText(String text) {
        if (node == null) {
            initialize();
        }
        setValue(text);
    }

    private void initialize() {
        Node[] nodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        for (Node node : nodes) {
            if (node instanceof JoinNode) {
                this.node = (JoinNode) node;
                break;
            }
        }
    }

    public void attachEnv(PropertyEnv env) {
        this.env = env;
    }
}