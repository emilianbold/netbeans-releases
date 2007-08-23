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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.netbeans.spi.navigator.NavigatorPanelWithUndo;
import org.openide.awt.UndoRedo;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;


/**
 * Implementation of <code>NavigatorPanel</code>, which provides the
 * outline component.
 *
 * @author Peter Zavadsky
 */
public class OutlinePanelProvider implements NavigatorPanelWithUndo {


    /** Current context to work on. */
    private Lookup.Result<DesignBean> currentContextResult;

    /** Listens on the retrieved <code>Lookup.Result</code>. */
    private /*final*/ LookupListener outlineLookupListener;// = new OutlineLookupListener();


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
        currentContextResult = lookup.lookup(new Lookup.Template<DesignBean>(DesignBean.class));

        if (isFine()) {
            fine("panelActivated lookup=" + lookup); // NOI18N
            if (lookup != null) {
                java.util.Collection col = lookup.lookup(new Lookup.Template<Object>(Object.class)).allInstances();
                for (java.util.Iterator it = col.iterator(); it.hasNext(); ) {
                    fine("item=" + it.next()); // NOI18N
                }
            }
        }

        Collection<? extends DesignBean> designBeans = currentContextResult.allInstances();
        OutlinePanel.getDefault().setActiveBeans(designBeans.toArray(new DesignBean[designBeans.size()]));

        // XXX Get new listener (in case there is lingering the old one).
        outlineLookupListener = new OutlineLookupListener();
        LookupListener weakLookupListener = WeakListeners.create(LookupListener.class, outlineLookupListener, currentContextResult);
        currentContextResult.addLookupListener(weakLookupListener);
    }

    public void panelDeactivated() {
//        if (currentContextResult == null) {
//            // #105327 There seems to be panelDeactivated called while there was missing panelActivated call before.
//            info(new IllegalStateException(
//                    "Called panelDeactivated method without previous correspondent panelActiavated method call on NavigatorPanel impl, "
//                    + "navigatorPanel=" + this)); // NOI18N
//        } else {
//            currentContextResult.removeLookupListener(outlineLookupListener);
//            currentContextResult = null;
//        }
        // XXX Removing weak listener.
        outlineLookupListener = null;
        
        // XXX #99299 Memory leak. Removing the tree when the panel is deactivated.
        OutlinePanel.getDefault().setActiveBeans(new DesignBean[0]);
    }

    public Lookup getLookup() {
        return OutlinePanel.getDefault().getLookup();
    }


    /** Listens on retrieved <code>Lookup.Result</code>. */
    private static class OutlineLookupListener implements LookupListener {
        public void resultChanged(LookupEvent evt) {
            Collection<? extends DesignBean> designBeans = ((Lookup.Result<DesignBean>)evt.getSource()).allInstances();
            if (isFine()) {
                fine("designBeans=" + designBeans); // NOI18N
            }

            OutlinePanel.getDefault().setActiveBeans(designBeans.toArray(new DesignBean[designBeans.size()]));
        }
    } // End of OultlineLookupListener.

    
    public UndoRedo getUndoRedo() {
        return getUndoRedoFromNodes(OutlinePanel.getDefault().getExplorerManager().getSelectedNodes());
    }
    
    private static UndoRedo getUndoRedoFromNodes(Node[] nodes) {
        if (nodes == null) {
            return UndoRedo.NONE;
        }

        for (Node node : nodes) {
            UndoRedo undoRedo = node.getLookup().lookup(UndoRedo.class);
            if (undoRedo != null) {
                return undoRedo;
            }
        }
        
        return UndoRedo.NONE;
    }

    
    private static Logger getLogger() {
        return Logger.getLogger(OutlinePanelProvider.class.getName());
    }
    
    private static boolean isFine() {
        return getLogger().isLoggable(Level.FINE);
    }
    
    private static void fine(String message) {
        getLogger().fine(message);
    }
    
    private static void info(Exception ex) {
        getLogger().log(Level.INFO, null, ex);
    }
}
