/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.ant.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;

/**
 * Listens on DebuggerManager and:
 * - loads all breakpoints & watches on startup
 * - listens on all changes of breakpoints and watches (like breakoint / watch
 *     added / removed, or some property change) and saves a new values
 *
 */
public class PersistenceManager implements LazyDebuggerManagerListener {
    
    @Override
    public Breakpoint[] initBreakpoints () {
        Properties p = Properties.getDefault ().getProperties ("debugger").
            getProperties (DebuggerManager.PROP_BREAKPOINTS);
        Breakpoint[] breakpoints = (Breakpoint[]) p.getArray (
            "ant", 
            new Breakpoint [0]
        );
        for (int i = 0; i < breakpoints.length; i++) {
            if (breakpoints[i] == null) {
                Breakpoint[] b2 = new Breakpoint[breakpoints.length - 1];
                System.arraycopy(breakpoints, 0, b2, 0, i);
                if (i < breakpoints.length - 1) {
                    System.arraycopy(breakpoints, i + 1, b2, i, breakpoints.length - i - 1);
                }
                breakpoints = b2;
                i--;
                continue;
            }
            breakpoints[i].addPropertyChangeListener(this);
        }
        return breakpoints;
    }
    
    @Override
    public void initWatches () {
    }
    
    @Override
    public String[] getProperties () {
        return new String [] {
            DebuggerManager.PROP_BREAKPOINTS_INIT,
            DebuggerManager.PROP_BREAKPOINTS,
        };
    }
    
    @Override
    public void breakpointAdded (Breakpoint breakpoint) {
        if (breakpoint instanceof AntBreakpoint) {
            Properties p = Properties.getDefault ().getProperties ("debugger").
                getProperties (DebuggerManager.PROP_BREAKPOINTS);
            p.setArray (
                "ant", 
                getBreakpoints ()
            );
            breakpoint.addPropertyChangeListener(this);
        }
    }

    @Override
    public void breakpointRemoved (Breakpoint breakpoint) {
        if (breakpoint instanceof AntBreakpoint) {
            Properties p = Properties.getDefault ().getProperties ("debugger").
                getProperties (DebuggerManager.PROP_BREAKPOINTS);
            p.setArray (
                "ant", 
                getBreakpoints ()
            );
            breakpoint.removePropertyChangeListener(this);
        }
    }
    @Override
    public void watchAdded (Watch watch) {
    }
    
    @Override
    public void watchRemoved (Watch watch) {
    }
    
    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getSource() instanceof Breakpoint) {
            Properties.getDefault ().getProperties ("debugger").
                getProperties (DebuggerManager.PROP_BREAKPOINTS).setArray (
                    "ant",
                    getBreakpoints ()
                );
        }
    }
    
    @Override
    public void sessionAdded (Session session) {}
    @Override
    public void sessionRemoved (Session session) {}
    @Override
    public void engineAdded (DebuggerEngine engine) {}
    @Override
    public void engineRemoved (DebuggerEngine engine) {}
    
    
    private static Breakpoint[] getBreakpoints () {
        Breakpoint[] bs = DebuggerManager.getDebuggerManager ().
            getBreakpoints ();
        int i, k = bs.length;
        ArrayList bb = new ArrayList ();
        for (i = 0; i < k; i++)
            // Don't store hidden breakpoints
            if (bs[i] instanceof AntBreakpoint)
                bb.add (bs [i]);
        bs = new Breakpoint [bb.size ()];
        return (Breakpoint[]) bb.toArray (bs);
    }
}

