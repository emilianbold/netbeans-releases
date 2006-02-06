/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.ui.diff;

import java.awt.*;
import javax.swing.*;
import org.openide.util.*;
import org.openide.windows.*;

/**
 * Diff TopComponent, synchronizing selected node and providing
 * diff setup source.
 *
 * @author Petr Kuzel
 */
public class DiffTopComponent extends TopComponent {
    
    private final DiffMainPanel panel;
    
    public DiffTopComponent(DiffMainPanel c) {
        setLayout(new BorderLayout());
        c.putClientProperty(TopComponent.class, this);
        add(c, BorderLayout.CENTER);
        panel = c;
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(DiffTopComponent.class, "ACSN_Diff_Top_Component"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DiffTopComponent.class, "ACSD_Diff_Top_Component"));
    }

    public int getPersistenceType(){
        return TopComponent.PERSISTENCE_NEVER;
    }

    protected void componentClosed() {
        panel.componentClosed();
        super.componentClosed();
    }

    protected String preferredID(){
        return "PERSISTENCE_NEVER-DiffTopComponent";    // NOI18N       
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    protected void componentActivated() {
        super.componentActivated();
        panel.requestActive();
    }

//        public Collection getSetups() {
//            DiffSetupSource mainPanel = ((DiffSetupSource) getComponent(0));
//            return mainPanel.getSetups();
//        }

//        public String getSetupDisplayName() {
//            DiffSetupSource mainPanel = ((DiffSetupSource) getComponent(0));
//            return mainPanel.getSetupDisplayName();
//        }
    
}
