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
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.WindowManager;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class GoToAction extends BpelNodeAction {

    private static final long serialVersionUID = 1L;

    private static ActSubMenuModel model = new ActSubMenuModel(null);
    private static BpelNodeAction[] GO_TO_ACTIONS = new BpelNodeAction[] {
            (BpelNodeAction)SystemAction.get(GoToDiagrammAction.class),
            (BpelNodeAction)SystemAction.get(GoToSourceAction.class),
            (BpelNodeAction)SystemAction.get(GoToLoggingAction.class)
    };

    public GoToAction() {
    }

    public static KeyStroke getKeyStroke(Class<? extends BpelNodeAction> clazz) {
        if (clazz == null) {
            return null;
        }
        SystemAction action = SystemAction.get(clazz);
        KeyStroke key = null;
        if (action != null) {
            Object keyObj = action.getValue(ACCELERATOR_KEY);
            if (keyObj instanceof KeyStroke) {
                key = (KeyStroke)keyObj;
            }
        }

        return key;
    }

    public final String getBundleName() {
        return NbBundle.getMessage(BpelNodeAction.class, "CTL_GoToAction"); // NOI18N
    }

    @Override
    public String getName() {
        return model.getCount() == 1 ?  super.getName() + " " +model.getLabel(0): super.getName(); // NOI18N
    }

    public ActionType getType() {
        return ActionType.GO_TO;
    }

    @Override
    public boolean enable(Node[] nodes) {
        model = new ActSubMenuModel(nodes);
        return super.enable(nodes);
    }

    @Override
    public void performAction(Node[] nodes) {
        if (! enable(nodes)) {
            return;
        }
        performAction(nodes, 0);
    }

    private static final void performAction(Node[] nodes, int index) {
        SystemAction[] gotoActions = getGoToActions(nodes);
        if (gotoActions == null || index < 0 || index > gotoActions.length) {
            return;
        }
        performAction(nodes, gotoActions[index]);
    }

    private static final void performAction(Node[] nodes, SystemAction gotoAction) {
        if (gotoAction instanceof BpelNodeAction) {
            ((BpelNodeAction)gotoAction).performAction(nodes);
        }
    }

    // TODO m
    public static final BpelNodeAction[] getGoToActions(Node[] nodes) {
        List<BpelNodeAction> availableGotoActions = new ArrayList<BpelNodeAction>();
        if (nodes != null && nodes.length > 0) {
//System.out.println();
            for (BpelNodeAction gotoAction : GO_TO_ACTIONS) {
//System.out.println("see: " + gotoAction);
                if (gotoAction.enable(nodes)) {
//System.out.println("     yes");
                    availableGotoActions.add(gotoAction);
                }
            }
            return availableGotoActions.toArray(new BpelNodeAction[availableGotoActions.size()]);
        }
        return null;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return new Actions.SubMenu(this, model, true);
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return new Actions.SubMenu(this, model, false);
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    private static final BpelNodeAction[] getGoToActions() {
        return GO_TO_ACTIONS;
    }

    private static final Node[] getCurrentNodes() {
        return WindowManager.getDefault().getRegistry().getCurrentNodes();
    }

    /** Implementation of ActSubMenuInt */
    private static class ActSubMenuModel extends EventListenerList implements Actions.SubMenuModel {
        static final long serialVersionUID = -4273674308662494596L;

        private Node[] nodes;

        ActSubMenuModel(Node[] nodes) {
            this.nodes = nodes;
        }

        private Node[] getNodes() {
            return nodes == null ? getCurrentNodes() : nodes;
        }

        public int getCount() {
            return getGoToActions(getNodes()).length;
        }

        public String getLabel(int index) {
            BpelNodeAction[] gotoActions = getGoToActions(getNodes());
            if (gotoActions != null && index >= 0 && index < gotoActions.length) {
                return gotoActions[index].getName();
            }
            return null;
        }

        public HelpCtx getHelpCtx(int index) {
            BpelNodeAction[] gotoActions = getGoToActions(getNodes());
            if (gotoActions != null && index > 0 && index < gotoActions.length) {
                return gotoActions[index].getHelpCtx();
            }
            return HelpCtx.DEFAULT_HELP;
        }

        public void performActionAt(int index) {
            BpelNodeAction[] gotoActions = getGoToActions(getNodes());
            if (gotoActions != null && index >= 0 && index < gotoActions.length) {
                performAction(nodes,index);
            }
        }

        /** Adds change listener for changes of the model.
        */
        public void addChangeListener(ChangeListener l) {
            add(ChangeListener.class, l);
        }

        /** Removes change listener for changes of the model.
        */
        public void removeChangeListener(ChangeListener l) {
            remove(ChangeListener.class, l);
        }
    }

    @Override
    protected void performAction(BpelEntity[] bpelEntities) {
        // do nothing
    }
     // end of ActSubMenuModel
}
