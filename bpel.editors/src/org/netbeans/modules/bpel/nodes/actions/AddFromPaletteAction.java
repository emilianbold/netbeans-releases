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
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.xml.xam.ui.category.AbstractCategory;
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
public class AddFromPaletteAction extends BpelNodeAction {
    private static final long serialVersionUID = 1L;
    private static ActSubMenuModel model = new ActSubMenuModel(null);
    private static BpelNodeAction[] PALETTE_CATEGORY_ACTIONS = new BpelNodeAction[] {
            (BpelNodeAction)SystemAction.get(AddWebServiceActivitiesAction.class),
            (BpelNodeAction)SystemAction.get(AddBasicActivitiesAction.class),
            (BpelNodeAction)SystemAction.get(AddStructuredActivitiesAction.class)
        };
    
    public AddFromPaletteAction() {
    }

    public final String getBundleName() {
        return NbBundle.getMessage(BpelNodeAction.class, "CTL_AddFromPaletteAction"); // NOI18N    
    }

//    public String getName() {
//        return model.getCount() == 1 ?  super.getName() + " " +model.getLabel(0): super.getName(); // NOI18N
//    }

    public ActionType getType() {
        return ActionType.ADD_FROM_PALETTE;
    }

    protected boolean enable(BpelEntity[] bpelEntities) {
        model = new ActSubMenuModel(bpelEntities);
        if (bpelEntities == null || bpelEntities.length < 0 ) {
            return false;
        }
        // TODO m
        if (bpelEntities[0] instanceof BpelContainer) {
            
            BpelNodeAction[] categories = getCategoriesActions(bpelEntities);
            if (categories == null || categories.length < 1) {
                return false;
            }
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
        SystemAction[] categoryActions = getCategoriesActions(bpelEntities);
        if (categoryActions == null || index < 0 || index > categoryActions.length) {
            return;
        }
        performAction(bpelEntities, categoryActions[index]);
    }
    
    private static final void performAction(BpelEntity[] bpelEntities, SystemAction wrapAction) {
        if (wrapAction instanceof BpelNodeAction) {
            ((BpelNodeAction)wrapAction).performAction(bpelEntities);
        } 
    }

    // TODO m
    public static final BpelNodeAction[] getCategoriesActions(BpelEntity[] bpelEntities) {
        List<BpelNodeAction> availableCategoriesActions = new ArrayList<BpelNodeAction>();
        if (bpelEntities != null && bpelEntities.length > 0) {
            for (BpelNodeAction categoryAction : PALETTE_CATEGORY_ACTIONS) {
                if (categoryAction.enable(bpelEntities)) {
                    availableCategoriesActions.add(categoryAction);
                }
            }
            return availableCategoriesActions.toArray(new BpelNodeAction[availableCategoriesActions.size()]);
        }
        return null;

////        return PALETTE_CATEGORY_ACTIONS;
    }
    
    public static final BpelNodeAction[] getCategoriesAction(Node[] nodes) {
        return getCategoriesActions(getBpelEntities(nodes));
    }
    
    // TODO m
    public JMenuItem getPopupPresenter() {
        if (!enable(model.getEntities())) {
            return new Actions.SubMenu(this, model, true);
        }

        JMenu submenu = new JMenu(this.getBundleName());

        BpelNodeAction[] categoryActions = getCategoriesActions(model.getEntities());
        for (BpelNodeAction paletteCategory : categoryActions) {
            
            AddPaletteActivityAction[] paletteActions = null;
            if (paletteCategory instanceof AddBasicActivitiesAction) {
                paletteActions = ((AddBasicActivitiesAction)paletteCategory)
                .getPaletteActions(model.getEntities());
            } else if (paletteCategory instanceof AddStructuredActivitiesAction) {
                paletteActions = ((AddStructuredActivitiesAction)paletteCategory)
                .getPaletteActions(model.getEntities());
            } else if (paletteCategory instanceof AddWebServiceActivitiesAction) {
                paletteActions = ((AddWebServiceActivitiesAction)paletteCategory)
                .getPaletteActions(model.getEntities());
            }
            
            if (paletteActions != null && paletteActions.length == 1) {
                submenu.add(new Actions.MenuItem(paletteCategory, false));
            } else if (paletteActions != null ) {
                JMenu subsubmenu = new JMenu(((BpelNodeAction)paletteCategory).getName());
                for (AddPaletteActivityAction paletteElem : paletteActions) {
                    subsubmenu.add(new Actions.MenuItem(paletteElem, false));
                }
                submenu.add(subsubmenu);
            }
        }

////        return new Actions.SubMenu(this, model, true);
        return submenu;
    }

    public JMenuItem getMenuPresenter() {
        return new Actions.SubMenu(this, model, false);
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    private static final BpelNodeAction[] getCategoryActions() {
        return PALETTE_CATEGORY_ACTIONS;
    }

    private static final BpelEntity[] getCurrentEntities() {
        return getBpelEntities(WindowManager.getDefault().getRegistry().getCurrentNodes());
    }
    
    /** Implementation of ActSubMenuInt */
    private static class ActSubMenuModel extends EventListenerList implements Actions.SubMenuModel {
        static final long serialVersionUID = -4273674308662497796L;

        private BpelEntity[] entities;
        
        ActSubMenuModel(BpelEntity[] entities) {
            this.entities = entities;
        }
        
        private BpelEntity[] getEntities() {
            return entities == null ? getCurrentEntities() : entities;
        }

        public int getCount() {
            return getCategoriesActions(getEntities()).length;
        }

        public String getLabel(int index) {
            BpelNodeAction[] categoryActions = getCategoriesActions(getEntities());
            if (categoryActions != null && index >= 0 && index < categoryActions.length) {
                return categoryActions[index].getName();
            }
            return null;
        }

        public HelpCtx getHelpCtx(int index) {
            BpelNodeAction[] categoryActions = getCategoriesActions(getEntities());
            if (categoryActions != null && index > 0 && index < categoryActions.length) {
                return categoryActions[index].getHelpCtx();
            }
            return HelpCtx.DEFAULT_HELP;
        }

        public void performActionAt(int index) {
            BpelNodeAction[] categoryActions = getCategoriesActions(getEntities());
            if (categoryActions != null && index >= 0 && index < categoryActions.length) {
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
