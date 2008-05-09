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
public class WrapAction extends BpelNodeAction {
    private static ActSubMenuModel model = new ActSubMenuModel(null);
    private static AbstractWrapWithAction[] WRAP_ACTIONS = new AbstractWrapWithAction[] {
            (AbstractWrapWithAction)SystemAction.get(WrapWithSequenceAction.class),
            (AbstractWrapWithAction)SystemAction.get(WrapWithScopeAction.class),
            (AbstractWrapWithAction)SystemAction.get(WrapWithFlowAction.class),
            (AbstractWrapWithAction)SystemAction.get(WrapWithForeachAction.class),
            (AbstractWrapWithAction)SystemAction.get(WrapWithRepeatUntilAction.class),
            (AbstractWrapWithAction)SystemAction.get(WrapWithWhileAction.class)
        };
    
    public WrapAction() {
    }

    public final String getBundleName() {
        return NbBundle.getMessage(BpelNodeAction.class, "CTL_WrapAction"); // NOI18N    
    }

    public String getName() {
        return model.getCount() == 1 ?  super.getName() + " " +model.getLabel(0): super.getName(); // NOI18N
    }

    public ActionType getType() {
        return ActionType.WRAP;
    }

    protected boolean enable(BpelEntity[] bpelEntities) {
        model = new ActSubMenuModel(bpelEntities);
        if (bpelEntities == null || bpelEntities.length < 0 ) {
            return false;
        }
        // TODO m
        if (bpelEntities[0] instanceof Activity) {
            return true;
        }
        
        return false;
    }

    protected void performAction(BpelEntity[] bpelEntities) {
        if (! enable(bpelEntities)) {
            return;
        }
        performAction(bpelEntities, 0);
    }
    
    private static final void performAction(BpelEntity[] bpelEntities, int index) {
        SystemAction[] wrapActions = getWrapActions(bpelEntities);
        if (wrapActions == null || index < 0 || index > wrapActions.length) {
            return;
        }
        performAction(bpelEntities, wrapActions[index]);
    }
    
    private static final void performAction(BpelEntity[] bpelEntities, SystemAction wrapAction) {
        if (wrapAction instanceof BpelNodeAction) {
            ((BpelNodeAction)wrapAction).performAction(bpelEntities);
        } 
    }

    // TODO m
    public static final AbstractWrapWithAction[] getWrapActions(BpelEntity[] bpelEntities) {
        return getWrapActions();
        // TODO r
//        List<AbstractWrapWithAction> availableWrapActions = new ArrayList<AbstractWrapWithAction>();
//        if (bpelEntities != null && bpelEntities.length > 0) {
//            for (AbstractWrapWithAction wrapAction : WRAP_ACTIONS) {
//                if (wrapAction.enable(bpelEntities)) {
//                    availableWrapActions.add(wrapAction);
//                }
//            }
//            return availableWrapActions.toArray(new AbstractWrapWithAction[availableWrapActions.size()]);
//        }
//        return null;
    }
    
    public static final AbstractWrapWithAction[] getWrapActions(Node[] nodes) {
        return getWrapActions(getBpelEntities(nodes));
    }
    
    public JMenuItem getPopupPresenter() {
        return new Actions.SubMenu(this, model, true);
    }

    public JMenuItem getMenuPresenter() {
        return new Actions.SubMenu(this, model, false);
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    private static final AbstractWrapWithAction[] getWrapActions() {
        return WRAP_ACTIONS;
    }

    private static final BpelEntity[] getCurrentEntities() {
        return getBpelEntities(WindowManager.getDefault().getRegistry().getCurrentNodes());
    }
    
    /** Implementation of ActSubMenuInt */
    private static class ActSubMenuModel extends EventListenerList implements Actions.SubMenuModel {
        static final long serialVersionUID = -4273674308662494596L;

        private BpelEntity[] entities;
        
        ActSubMenuModel(BpelEntity[] entities) {
            this.entities = entities;
        }
        
        private BpelEntity[] getEntities() {
            return entities == null ? getCurrentEntities() : entities;
        }

        public int getCount() {
            return getWrapActions(getEntities()).length;
        }

        public String getLabel(int index) {
            AbstractWrapWithAction[] wrapActions = getWrapActions(getEntities());
            if (wrapActions != null && index >= 0 && index < wrapActions.length) {
                return wrapActions[index].getName();
            }
            return null;
        }

        public HelpCtx getHelpCtx(int index) {
            AbstractWrapWithAction[] wrapActions = getWrapActions(getEntities());
            if (wrapActions != null && index > 0 && index < wrapActions.length) {
                return wrapActions[index].getHelpCtx();
            }
            return HelpCtx.DEFAULT_HELP;
        }

        public void performActionAt(int index) {
            AbstractWrapWithAction[] wrapActions = getWrapActions(getEntities());
            if (wrapActions != null && index >= 0 && index < wrapActions.length) {
                performAction(entities,index);
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
     // end of ActSubMenuModel
}
