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

import org.netbeans.modules.bpel.nodes.actions.BpelNodeAction;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import javax.swing.KeyStroke;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContextFactory;
import org.netbeans.modules.bpel.model.api.BpelEntity;
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
public class ShowBpelMapperAction extends BpelNodeAction {

    private static final long serialVersionUID = 1L;
//    public static final KeyStroke GOTOMAPPER_KEYSTROKE = KeyStroke.getKeyStroke(
//            NbBundle.getMessage(ShowBpelMapperAction.class,"ACT_GoToMapperAction"));// NOI18N

    public ShowBpelMapperAction() {
        super();
//        putValue(ShowBpelMapperAction.ACCELERATOR_KEY, GOTOMAPPER_KEYSTROKE);
    }

    protected String getBundleName() {
        return NbBundle.getMessage(ShowBpelMapperAction.class,
                "CTL_ShowBpelMapperAction"); // NOI18N
    }

    public ActionType getType() {
        return ActionType.SHOW_BPEL_MAPPER;
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

                isEnable = ref instanceof BpelEntity
                        && BpelDesignContextFactory.getInstance().isMappableEntity((BpelEntity)ref);
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
                        && BpelDesignContextFactory.getInstance().isMappableEntity(entities[0]);
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
        EditorUtil.goToBusinessRules(bpelEntities[0]);
/**        TopComponent mapperTC = WindowManager.getDefault().
                findTopComponent(BpelMapperTopComponent.ID);
        if (mapperTC == null) {
            return;
        }

        if (!(mapperTC.isOpened())) {
            mapperTC.open();
        }
        mapperTC.requestVisible();
        mapperTC.requestActive();
 */
    }


    @Override
    public boolean isChangeAction() {
        return false;
    }
}
