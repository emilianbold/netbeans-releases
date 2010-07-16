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

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */
package org.netbeans.modules.vmd.inspector;

import java.awt.BorderLayout;
import java.awt.Color;
import org.openide.util.Lookup;
import javax.swing.*;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * @author Karol Harezlak
 */

final class InspectorPanel implements NavigatorPanel {

    private static InspectorPanel INSTANCE;

    private JPanel panel;
    private Lookup lookup;
    private final InstanceContent ic;
    
    static InspectorPanel getInstance() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        synchronized (InspectorPanel.class) {
            if (INSTANCE == null) {
                INSTANCE = new InspectorPanel();
            }
            return INSTANCE;
        }
    }
    
    private InspectorPanel() {
        this.ic = new InstanceContent();
        this.lookup = new AbstractLookup(ic);
        this.panel = new JPanel(new BorderLayout());
        this.panel.setBackground(Color.WHITE);
    }

    public String getDisplayName() {
        return NbBundle.getMessage(InspectorPanel.class, "LBL_InspectorPanelDisplayName"); //NOI18N
    }

    public String getDisplayHint() {
        return NbBundle.getMessage(InspectorPanel.class, "LBL_InspectorPanelHint"); //NOI18N
    }

    public synchronized JComponent getComponent() {
        return panel;
    }

    public void panelActivated(Lookup lookup) {
    }

    public void panelDeactivated() {
    }

    public Lookup getLookup() {
        return lookup;
    }

    InstanceContent getInstanceContent() {
        return ic;
    }

}
