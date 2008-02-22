/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


    // #123003 Removed memory leak.
//    /** Current context to work on. */
//    private Lookup.Result<DesignBean> currentContextResult;

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
        Lookup.Result<DesignBean> currentContextResult = lookup.lookup(new Lookup.Template<DesignBean>(DesignBean.class));

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
