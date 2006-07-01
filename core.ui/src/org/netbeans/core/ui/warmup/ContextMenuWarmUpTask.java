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

package org.netbeans.core.ui.warmup;

import java.awt.EventQueue;

import javax.swing.Action;
import javax.swing.JMenuItem;

import org.openide.actions.ToolsAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;


/**
 * Warm-up task for initing context menu
 *
 * @author  Tomas Pavek, Peter Zavadsky
 */
public final class ContextMenuWarmUpTask implements Runnable {

    public void run() {
        // For first context menu.
        org.openide.actions.ActionManager.getDefault().getContextActions();
        new javax.swing.JMenuItem();

        // #30676 ToolsAction popup warm up.
        try {
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    warmUpToolsPopupMenuItem();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Warms up tools action popup menu item. */
    private static void warmUpToolsPopupMenuItem() {
        SystemAction toolsAction = SystemAction.get(ToolsAction.class);
        if(toolsAction instanceof ContextAwareAction) {
            // Here is important to create proper lookup
            // to warm up Tools sub actions.
            Lookup lookup = new org.openide.util.lookup.ProxyLookup(
                new Lookup[] {
                    // This part of lookup causes warm up of Node (cookie) actions.
                    new AbstractNode(Children.LEAF).getLookup(),
                    // This part of lookup causes warm up of Callback actions.
                    new TopComponent().getLookup()
                }
            );
            
            Action action = ((ContextAwareAction)toolsAction)
                                .createContextAwareInstance(lookup);
            if(action instanceof Presenter.Popup) {
                JMenuItem toolsMenuItem = ((Presenter.Popup)action)
                                                .getPopupPresenter();
                if(toolsMenuItem instanceof Runnable) {
                    // This actually makes the warm up.
                    // See ToolsAction.Popup impl.
                    ((Runnable)toolsMenuItem).run();
                }
            }
        }
    }
}
