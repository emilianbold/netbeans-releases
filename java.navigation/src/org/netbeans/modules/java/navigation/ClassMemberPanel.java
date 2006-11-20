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

package org.netbeans.modules.java.navigation;

import javax.swing.JComponent;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
public class ClassMemberPanel implements NavigatorPanel {

    private ClassMemberPanelUI component;

    public ClassMemberPanel() {
    }

    public void panelActivated(Lookup context) {
        assert context != null;
        // System.out.println("Panel Activated");
        ClassMemberNavigatorJavaSourceFactory.getInstance().setLookup(context, getClassMemberPanelUI());
        getClassMemberPanelUI().showWaitNode();
    }

    public void panelDeactivated() {
        getClassMemberPanelUI().showWaitNode(); // To clear the ui
        ClassMemberNavigatorJavaSourceFactory.getInstance().setLookup(Lookup.EMPTY, null);
    }

    public Lookup getLookup() {
        return this.getClassMemberPanelUI().getLookup();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ClassMemberPanel.class,"LBL_members");
    }

    public String getDisplayHint() {
        return NbBundle.getMessage(ClassMemberPanel.class,"HINT_members");
    }

    public JComponent getComponent() {
        return getClassMemberPanelUI();
    }

    private synchronized ClassMemberPanelUI getClassMemberPanelUI() {
        if (this.component == null) {
            this.component = new ClassMemberPanelUI();
        }
        return this.component;
    }
        
}
