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

package org.netbeans.modules.mercurial.ui.diff;

import java.awt.*;
import java.util.*;
import org.openide.util.*;
import org.openide.windows.*;
import org.openide.awt.UndoRedo;

/**
 * Diff TopComponent, synchronizing selected node and providing
 * diff setup source.
 *
 * @author Petr Kuzel
 */
public class DiffTopComponent extends TopComponent implements DiffSetupSource {

    private final DiffMainPanel panel;

    public DiffTopComponent(DiffMainPanel c) {
        setLayout(new BorderLayout());
        c.putClientProperty(TopComponent.class, this);
        add(c, BorderLayout.CENTER);
        panel = c;
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(DiffTopComponent.class, "ACSN_Diff_Top_Component")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DiffTopComponent.class, "ACSD_Diff_Top_Component")); // NOI18N
    }

    public UndoRedo getUndoRedo() {
        return panel.getUndoRedo();
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

    public Collection getSetups() {
        DiffSetupSource mainPanel = ((DiffSetupSource) getComponent(0));
        return mainPanel.getSetups();
    }

    public String getSetupDisplayName() {
        DiffSetupSource mainPanel = ((DiffSetupSource) getComponent(0));
        return mainPanel.getSetupDisplayName();
    }
    
}
