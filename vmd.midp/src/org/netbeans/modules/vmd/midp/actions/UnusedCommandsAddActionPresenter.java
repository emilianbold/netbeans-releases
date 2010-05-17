/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
