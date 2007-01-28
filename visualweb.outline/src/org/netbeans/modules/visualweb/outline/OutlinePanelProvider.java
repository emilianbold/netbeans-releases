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

package org.netbeans.modules.visualweb.outline;

import com.sun.rave.designtime.DesignBean;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.nodes.Node;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;


/**
 * Implementation of <code>NavigatorPanel</code>, which provides the
 * outline component.
 *
 * @author Peter Zavadsky
 */
public class OutlinePanelProvider implements NavigatorPanel {

    /** Debugging flag. */
    private static final boolean DEBUG = ErrorManager.getDefault()
            .getInstance(OutlinePanelProvider.class.getName()).isLoggable(ErrorManager.INFORMATIONAL);


    /** Current context to work on. */
    private Lookup.Result currentContextResult;

    /** Listens on the retrieved <code>Lookup.Result</code>. */
    private final LookupListener outlineLookupListener = new OutlineLookupListener();


    /** Creates a new instance of OutlinePanel */
    public OutlinePanelProvider() {
    }


    public String getDisplayName() {
        return NbBundle.getMessage(OutlinePanel.class, "LBL_OutlinePanel");
    }

    public String getDisplayHint() {
        return NbBundle.getMessage(OutlinePanel.class, "HINT_OutlinePanel");
    }

    public JComponent getComponent() {
        return OutlinePanel.getDefault();
    }

    public void panelActivated(Lookup lookup) {
        currentContextResult = lookup.lookup(new Lookup.Template(DesignBean.class));

        if (DEBUG) {
            debugLog("panelActivated lookup=" + lookup); // NOI18N
            if (lookup != null) {
                java.util.Collection col = lookup.lookup(new Lookup.Template(Object.class)).allInstances();
                for (java.util.Iterator it = col.iterator(); it.hasNext(); ) {
                    debugLog("item=" + it.next()); // NOI18N
                }
            }
        }

        Collection<DesignBean> designBeans = currentContextResult.allInstances();
        OutlinePanel.getDefault().setActiveBeans(designBeans.toArray(new DesignBean[designBeans.size()]));
        currentContextResult.addLookupListener(outlineLookupListener);
    }

    public void panelDeactivated() {
        currentContextResult.removeLookupListener(outlineLookupListener);
        currentContextResult = null;
    }

    public Lookup getLookup() {
        return null;
    }


    /** Logs debug message. Use only after checking <code>DEBUG</code> flag. */
    private static void debugLog(String message) {
        ErrorManager.getDefault().getInstance(OutlinePanelProvider.class.getName()).log(message);
    }


    /** Listens on retrieved <code>Lookup.Result</code>. */
    private static class OutlineLookupListener implements LookupListener {
        public void resultChanged(LookupEvent evt) {
            Collection<DesignBean> designBeans = ((Lookup.Result)evt.getSource()).allInstances();
            if (DEBUG) {
                debugLog("designBeans=" + designBeans); // NOI18N
            }

            OutlinePanel.getDefault().setActiveBeans(designBeans.toArray(new DesignBean[designBeans.size()]));
        }
    } // End of OultlineLookupListener.

}
