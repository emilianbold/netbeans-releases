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
package org.netbeans.modules.bpel.nodes.actions;

import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.mapper.logging.multiview.LoggingDesignContextFactory;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.openide.loaders.DataNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class GoToLoggingAction extends BpelNodeAction {

    private static final long serialVersionUID = 1L;
//    public static final KeyStroke GOTOLOGGING_KEYSTROKE = KeyStroke.getKeyStroke(
//            NbBundle.getMessage(GoToLoggingAction.class,"ACT_GoToLoggingAction"));// NOI18N

    public GoToLoggingAction() {
        super();
//        putValue(GoToLoggingAction.ACCELERATOR_KEY, GOTOLOGGING_KEYSTROKE);
    }

    protected String getBundleName() {
        return NbBundle.getMessage(GoToLoggingAction.class,
                "CTL_GoToLoggingAction"); // NOI18N
    }

    public ActionType getType() {
        return ActionType.GO_TO_LOGGING;
    }

    //TODO m
    @Override
    public boolean enable(final Node[] nodes) {
        if (nodes == null || nodes.length == 0) {
            return false;
        }
        boolean isEnable = false;

        DataNode dataNode = null;
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] instanceof BpelNode) {
                Object ref = ((BpelNode)nodes[i]).getReference();

                isEnable = ref instanceof ExtensibleElements
                        && LoggingDesignContextFactory.canExtend((ExtensibleElements)ref);
                if (isEnable) {
                    break;
                }
            }
            if (nodes[i] instanceof DataNode) {
                dataNode = (DataNode)nodes[i];
            }
        }

        // temporary hack, tc doesn't have nested mv tc activated nodes
        if (dataNode != null) {
            TopComponent activatedTc = WindowManager.getDefault().getRegistry().getActivated();
            Node[] activatedNodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
            BpelEntity[] entities = getBpelEntities(activatedNodes);
            isEnable = entities != null && entities.length > 0
                    && entities[0] instanceof ExtensibleElements
                    && LoggingDesignContextFactory.canExtend((ExtensibleElements)entities[0]);
        }
        return isEnable;
    }

    private boolean isDataNode(Node[] nodes) {
        boolean isDataNode = true;
        DataNode dataNode = null;
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] instanceof BpelNode) {
                isDataNode = false;
            }
            if (nodes[i] instanceof DataNode) {
                dataNode = (DataNode)nodes[i];
            }
        }
        isDataNode = isDataNode && dataNode != null;
        return isDataNode;
    }

    @Override
    public void performAction(Node[] nodes) {
        if (!enable(nodes)) {
            return;
        }

        if (isDataNode(nodes)) {
            nodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        }

        BpelEntity[] entities = getBpelEntities(nodes);
        if (entities != null && entities.length > 0) {
            performAction(entities);
        }
    }

    protected void performAction(BpelEntity[] bpelEntities) {
        EditorUtil.goToLoggingAlerting(bpelEntities[0]);
    }


    @Override
    public boolean isChangeAction() {
        return false;
    }
}
