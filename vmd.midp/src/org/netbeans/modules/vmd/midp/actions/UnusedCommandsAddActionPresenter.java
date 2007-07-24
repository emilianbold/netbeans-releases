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

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.AddActionItem;
import org.netbeans.modules.vmd.api.model.presenters.actions.AddActionPresenter;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Karol Harezlak
 */
abstract class UnusedCommandsAddActionPresenter extends AddActionPresenter {
    
    //TODO Because of havy use of this class created actions should be somehow cashed and reuse!!
    
    public static final String DISPLAY_NAME_ADD = NbBundle.getMessage(UnusedCommandsAddActionPresenter.class, "NAME_UnusedCommandsAddActionPresenter"); //NOI18N
    
    public static Presenter createForDisplayable(String displayName, int order) {
        return new UnusedCommandsAddActionPresenter(displayName, order) {
            private DesignComponent selectedCommandSource;
            protected synchronized void insideActionPerformed(final DesignComponent unusedCommandComponent) {
                final DesignDocument document = unusedCommandComponent.getDocument();
                document.getTransactionManager().writeAccess(new Runnable() {
                    public void run() {
                        selectedCommandSource = MidpDocumentSupport.attachCommandToDisplayable(getComponent(), unusedCommandComponent);
                    }
                });
                selectComponent(selectedCommandSource);
                selectedCommandSource = null;
            }
        };
    }
    
    public static Presenter createForItem(String displayName, int order) {
        return new UnusedCommandsAddActionPresenter(displayName, order) {
            private DesignComponent selectedCommandSource;
            protected synchronized void insideActionPerformed(final DesignComponent unusedCommandComponent) {
                final DesignDocument document = unusedCommandComponent.getDocument();
                document.getTransactionManager().writeAccess(new Runnable() {
                    public void run() {
                        selectedCommandSource = MidpDocumentSupport.attachCommandToItem(getComponent(), unusedCommandComponent);
                    }
                });
                selectComponent(selectedCommandSource);
                selectedCommandSource = null;
            }
        };
    }
    
    private static void selectComponent(final DesignComponent selectedCommandSource) {
        final DesignDocument document = selectedCommandSource.getDocument();
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                 document.setSelectedComponents(null, Collections.singleton(selectedCommandSource));
            }
        });
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
            AddActionItem item = createUnusedCommandAction(new WeakReference<DesignComponent>(unusedCommand),
                    new WeakReference<InfoPresenter>(infoPresenter),
                    new WeakReference<UnusedCommandsAddActionPresenter>(this));
            item.resolveAction(getComponent());
            newAddActions.add(item);
        }
        return newAddActions.toArray(new AddActionItem[newAddActions.size()]);
    }
    
    private static AddActionItem createUnusedCommandAction(final WeakReference<DesignComponent> unusedCommandComponent,
            final WeakReference<InfoPresenter> infoPresenter,
            final WeakReference<UnusedCommandsAddActionPresenter> presenter) {
        
        return new AddActionItem(unusedCommandComponent.get().getType()) {
            private ImageIcon icon;
            public void actionPerformed(ActionEvent event) {
                presenter.get().insideActionPerformed(unusedCommandComponent.get());
            }
            
            public void resolveAction(DesignComponent component) {
                InfoPresenter presenter = infoPresenter.get();
                icon = icon == null ? icon = new ImageIcon(presenter.getIcon(InfoPresenter.IconType.COLOR_16x16)) : icon;
                putValue(Action.NAME, presenter.getDisplayName(InfoPresenter.NameType.PRIMARY));
                putValue(Action.SMALL_ICON, icon);
            }
        };
    }
}
