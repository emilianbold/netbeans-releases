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

package org.netbeans.modules.versioning;

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.netbeans.modules.versioning.spi.VersioningSupport;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * View menu action that shows/hides textual Versioning annotations.
 *
 * @author Maros Sandor
 */
public class ShowTextAnnotationsAction extends SystemAction implements DynamicMenuContent {

    private JCheckBoxMenuItem [] menuItems;
    
    public JComponent[] getMenuPresenters() {
        createItems();
        updateState();
        return menuItems;
    }

    public JComponent[] synchMenuPresenters(JComponent[] items) {
        updateState();
        return items;
    }
    
    private void updateState() {
        boolean tav = VersioningSupport.getPreferences().getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);
        menuItems[0].setSelected(tav);
    }
    
    private void createItems() {
        if (menuItems == null) {
            menuItems = new JCheckBoxMenuItem[1];
            menuItems[0] = new JCheckBoxMenuItem(this);
            menuItems[0].setIcon(null);
            Mnemonics.setLocalizedText(menuItems[0], NbBundle.getMessage(ShowTextAnnotationsAction.class, "CTL_MenuItem_ShowTextAnnotations"));
        }
    }

    public String getName() {
        return NbBundle.getMessage(ShowTextAnnotationsAction.class, "CTL_MenuItem_ShowTextAnnotations");
    }

    public boolean isEnabled() {
        return true;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ShowTextAnnotationsAction.class);
    }

    public void actionPerformed(ActionEvent e) {
        boolean tav = VersioningSupport.getPreferences().getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);
        VersioningSupport.getPreferences().putBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, !tav); 
    }
}
