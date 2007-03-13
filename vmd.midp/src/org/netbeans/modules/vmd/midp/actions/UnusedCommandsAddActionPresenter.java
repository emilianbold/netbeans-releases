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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.midp.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.AddActionItem;
import org.netbeans.modules.vmd.api.model.presenters.actions.AddActionPresenter;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.openide.util.NbBundle;

/**
 * @author Karol Harezlak
 */
abstract class UnusedCommandsAddActionPresenter extends AddActionPresenter {
    //TODO Because of havy use of this class created actions should be somehow cashed and reuse!!
    public static final String DISPLAY_NAME_ADD = NbBundle.getMessage(UnusedCommandsAddActionPresenter.class, "NAME_UnusedCommandsAddActionPresenter"); //NOI18N
    
    public static final Presenter createForDisplayable(String displayName, int order){
        return new UnusedCommandsAddActionPresenter(displayName, order) {
            protected void insideActionPerformed(DesignComponent unusedCommandComponent) {
                MidpDocumentSupport.attachCommandToDisplayable(getComponent(), unusedCommandComponent);
            }
        };
    }
    
    public static final Presenter createForItem(String displayName, int order){
        return new UnusedCommandsAddActionPresenter(displayName, order) {
            protected void insideActionPerformed(DesignComponent unusedCommandComponent) {
                MidpDocumentSupport.attachCommandToItem(getComponent(), unusedCommandComponent);
            }
        };
    }
    
    private Integer order;
    private String displayName;
    private List<Action> newAddActions;
    
    private UnusedCommandsAddActionPresenter(String displayName, int order) {
        this.order = order;
        this.displayName = displayName;
    }
    
    protected abstract void insideActionPerformed(DesignComponent unusedCommandComponent);
    
    public String getName() {
        return displayName;
    }
    
    public Integer getOrder() {
        return order;
    }
    
    public AddActionItem[] getAddActionItems() {
        if (newAddActions == null)
            newAddActions = new ArrayList<Action>();
        else
            newAddActions.clear();
        Collection<DesignComponent> unusedCommands = MidpDocumentSupport.getAvailableCommandsForComponent(getComponent());
        if (unusedCommands == null)
            return null;
        for (final DesignComponent unusedCommand : unusedCommands) {
            final InfoPresenter infoPresenter = unusedCommand.getPresenter(InfoPresenter.class);
            if (infoPresenter == null)
                throw new IllegalStateException("No Info Presenter for component: " + unusedCommand); //NOI18N
            //newAddActions.add(getInstance(unusedCommand, infoPresenter, this));
            AddActionItem item = createUnusedCommandAction(unusedCommand, infoPresenter, this);
            item.resolveAction(getComponent());
            newAddActions.add(item);
        }
        return newAddActions.toArray(new AddActionItem[newAddActions.size()]);
    }
    
    //private static Map<DesignComponent, AddActionItem> instances = new WeakHashMap<DesignComponent, AddActionItem>();
    
//    private static final AddActionItem getInstance(DesignComponent unusedCommandComponent,
//                                                   InfoPresenter infoPresenter,
//                                                   UnusedCommandsAddActionPresenter presenter) {
//        
//        AddActionItem action = instances.get(unusedCommandComponent);
//        if (action != null) {
//            action.resolveAction(unusedCommandComponent);
//            return action;
//        }
//        action = createUnusedCommandAction(unusedCommandComponent, infoPresenter, presenter);
//        instances.put(unusedCommandComponent, action);
//        
//        return action;
//    }
    
    private static final AddActionItem createUnusedCommandAction(final DesignComponent unusedCommandComponent,
                                                                 final InfoPresenter infoPresenter,
                                                                 final UnusedCommandsAddActionPresenter presenter) {
        
        return new AddActionItem(unusedCommandComponent.getType()) {
            private ImageIcon icon;
            public void actionPerformed(ActionEvent event) {
                unusedCommandComponent.getDocument().getTransactionManager().writeAccess(new Runnable() {
                    public void run() {
                        presenter.insideActionPerformed(unusedCommandComponent);
                    }
                });
            }
            
            public void resolveAction(DesignComponent component) {
                icon = icon == null ? icon = new ImageIcon(infoPresenter.getIcon(InfoPresenter.IconType.COLOR_16x16)) : icon;
                putValue(Action.NAME, infoPresenter.getDisplayName(InfoPresenter.NameType.PRIMARY));
                putValue(Action.SMALL_ICON, icon);
            }
        };
    }
}


