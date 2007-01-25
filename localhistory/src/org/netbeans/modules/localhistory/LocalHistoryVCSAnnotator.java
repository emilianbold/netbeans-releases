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
package org.netbeans.modules.localhistory;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.localhistory.ui.actions.RevertDeletedAction;
import org.netbeans.modules.localhistory.ui.actions.ShowLocalHistoryAction;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * Provides the Local History Actions to the IDE
 * 
 * @author Tomas Stupka
 */
public class LocalHistoryVCSAnnotator extends VCSAnnotator {
    
    /** Creates a new instance of LocalHistoryVCSAnnotator */
    public LocalHistoryVCSAnnotator() {
    }
 
    public Image annotateIcon(Image icon, VCSContext context) {
        // not supported yet
        return super.annotateIcon(icon, context);
    }    
            
    public String annotateName(String name, VCSContext context) {
        // not supported yet
        return super.annotateName(name, context);
    }
    
    public Action[] getActions(VCSContext ctx, int destination) {
        Lookup context = Lookups.fixed(ctx.getNodes());
        List<Action> actions = new ArrayList<Action>();
        if (destination == VCSAnnotator.DEST_MAINMENU) {
            actions.add(SystemAction.get(ShowLocalHistoryAction.class));
            actions.add(SystemAction.get(RevertDeletedAction.class));
        } else {
            actions.add(SystemActionBridge.createAction(
                                            SystemAction.get(ShowLocalHistoryAction.class), 
                                            NbBundle.getMessage(LocalHistoryVCSAnnotator.class, "CTL_PopupMenuItem_ShowLocalHistory"), 
                                            context));
            actions.add(SystemActionBridge.createAction(
                                            SystemAction.get(RevertDeletedAction.class), 
                                            NbBundle.getMessage(LocalHistoryVCSAnnotator.class, "CTL_PopupMenuItem_RevertDeleted"), 
                                            context));           
        }
        return actions.toArray(new Action[actions.size()]);
    }    
    
}
