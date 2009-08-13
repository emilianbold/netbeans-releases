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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.debugger.ui.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.viewmodel.CheckNodeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @author   Jan Jancura
 */
public class BreakpointsNodeModel implements CheckNodeModel  {

    public static final String BREAKPOINT_GROUP =
        "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint";

    private final Map<Breakpoint, Boolean> breakpointsBeingEnabled = new IdentityHashMap<Breakpoint, Boolean>();
    private RequestProcessor rp;
    private final Collection modelListeners = new ArrayList();

    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return NbBundle.getBundle(BreakpointsNodeModel.class).getString("CTL_BreakpointModel_Column_Name_Name");
        } else
        if (o instanceof String) {
            return (String) o;
        } else
        throw new UnknownTypeException (o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return TreeModel.ROOT;
        } else
        if (o instanceof String) {
            return NbBundle.getBundle(BreakpointsNodeModel.class).getString("CTL_BreakpointModel_Column_GroupName_Desc");
        } else
        throw new UnknownTypeException (o);
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return BREAKPOINT_GROUP;
        } else
        if (o instanceof String) {
            return BREAKPOINT_GROUP;
        } else
        throw new UnknownTypeException (o);
    }

    /**
     * Registers given listener.
     *
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.add(l);
        }
    }

    /**
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.remove(l);
        }
    }

    private void fireModelEvent(ModelEvent ev) {
        Collection listeners;
        synchronized (modelListeners) {
            listeners = new ArrayList(modelListeners);
        }
        for (Iterator it = listeners.iterator(); it.hasNext(); ) {
            ModelListener l = (ModelListener) it.next();
            l.modelChanged(ev);
        }
    }

    public boolean isCheckable(Object node) throws UnknownTypeException {
        return true;
    }

    public boolean isCheckEnabled(Object node) throws UnknownTypeException {
        return true;
    }

    public Boolean isSelected(Object node) throws UnknownTypeException {
        if (node instanceof Breakpoint) {
            return ((Breakpoint) node).isEnabled();
        } else if (node instanceof String) {
            String groupName = (String) node;
            Breakpoint[] bs = DebuggerManager.getDebuggerManager ().
                getBreakpoints ();
            Boolean enabled = null;
            for (int i = 0; i < bs.length; i++) {
                if (bs [i].getGroupName ().equals (groupName)) {
                    if (enabled == null) {
                        enabled = Boolean.valueOf (bs[i].isEnabled ());
                    } else {
                        if (enabled.booleanValue() != bs[i].isEnabled ()) {
                            return null; // Some are enabled, some disabled
                        }
                    }
                }
            }
            return enabled;
        }
        throw new UnknownTypeException (node);
    }

    public void setSelected(Object node, Boolean selected) throws UnknownTypeException {
        if (selected != null) {
            if (node instanceof Breakpoint) {
                Breakpoint bp = (Breakpoint) node;
                synchronized (breakpointsBeingEnabled) {
                    // Keep the original value until we change the BP state...
                    breakpointsBeingEnabled.put(bp, Boolean.valueOf(bp.isEnabled()));
                    if (rp == null) {
                        rp = new RequestProcessor("Enable Breakpoints RP", 1); // NOI18N
                    }
                }
                rp.post(new BreakpointEnabler(bp, selected.booleanValue ()));
            } else if (node instanceof String) {
                String groupName = (String) node;
                Breakpoint[] bs = DebuggerManager.getDebuggerManager ().
                    getBreakpoints ();
                ArrayList breakpoints = new ArrayList();
                for (int i = 0; i < bs.length; i++) {
                    if (bs [i].getGroupName ().equals (groupName)) {
                        breakpoints.add(bs[i]);
                    }
                }
                if (breakpoints.size() > 0) {
                    synchronized (breakpointsBeingEnabled) {
                        // Keep the original value until we change the BP state...
                        for (Iterator it = breakpoints.iterator(); it.hasNext(); ) {
                            Breakpoint bp = (Breakpoint) it.next();
                            breakpointsBeingEnabled.put(bp, Boolean.valueOf(bp.isEnabled()));
                        }
                        if (rp == null) {
                            rp = new RequestProcessor("Enable Breakpoints RP", 1); // NOI18N
                        }
                        for (Iterator it = breakpoints.iterator(); it.hasNext(); ) {
                            Breakpoint bp = (Breakpoint) it.next();
                            rp.post(new BreakpointEnabler(bp, selected.booleanValue ()));
                        }
                    }
                }
            }
        }
    }
    

    private class BreakpointEnabler extends Object implements Runnable {

        private Breakpoint bp;
        private boolean enable;

        public BreakpointEnabler(Breakpoint bp, boolean enable) {
            this.bp = bp;
            this.enable = enable;
        }

        public void run() {
            if (enable)
                bp.enable ();
            else
                bp.disable ();
            synchronized (breakpointsBeingEnabled) {
                breakpointsBeingEnabled.remove(bp);
            }
            fireModelEvent(new ModelEvent.NodeChanged(
                    BreakpointsNodeModel.this,
                    bp));
            // re-calculate the enabled state of the BP group
            String groupName = bp.getGroupName();
            if (groupName != null) {
                fireModelEvent(new ModelEvent.NodeChanged(
                    BreakpointsNodeModel.this,
                    groupName));
            }
        }
    }

}
