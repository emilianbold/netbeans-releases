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
package org.netbeans.modules.debugger.ui;

import java.beans.PropertyChangeEvent;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;

import org.openide.awt.ToolbarPool;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;


/**
 * This listener notifies about changes in the
 * {@link DebuggerManager}.
 *
 * @author Jan Jancura
 */
public class DebuggerManagerListener extends DebuggerManagerAdapter {
    
    private boolean isOpened = false;
    
    public void propertyChange (PropertyChangeEvent evt) {
        if ( (DebuggerManager.getDebuggerManager ().getCurrentEngine () 
               != null) &&
             (!isOpened)
        ) {
            // Open debugger TopComponentGroup.
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    TopComponentGroup group = WindowManager.getDefault ().
                        findTopComponentGroup ("debugger"); // NOI18N
                    if (group != null) {
                        group.open ();
                        if (ToolbarPool.getDefault ().
                            getConfiguration ().equals 
                            (ToolbarPool.DEFAULT_CONFIGURATION)
                        )
                            ToolbarPool.getDefault ().setConfiguration 
                                ("Debugging"); // NOI18N
                    }
                }
            });
            isOpened = true;
        }
        if ( (evt.getPropertyName () == DebuggerManager.PROP_DEBUGGER_ENGINES) 
                &&
             ((DebuggerEngine[]) evt.getNewValue ()).length == 0
        ) {
            closeDebuggerUI();
            isOpened = false;
        }
    }
    
    static void closeDebuggerUI() {
        /*
        java.util.logging.Logger.getLogger("org.netbeans.modules.debugger.jpda").fine("CLOSING TopComponentGroup...");
        StringWriter sw = new StringWriter();
        new Exception("Stack Trace").fillInStackTrace().printStackTrace(new java.io.PrintWriter(sw));
        java.util.logging.Logger.getLogger("org.netbeans.modules.debugger.jpda").fine(sw.toString());
         */
        // Close debugger TopComponentGroup.
        if (SwingUtilities.isEventDispatchThread()) {
            doCloseDebuggerUI();
        } else {
            SwingUtilities.invokeLater(new Runnable () {
                public void run () {
                    doCloseDebuggerUI();
                }
            });
        }
        //java.util.logging.Logger.getLogger("org.netbeans.modules.debugger.jpda").fine("TopComponentGroup closed.");
    }
    
    private static void doCloseDebuggerUI() {
        TopComponentGroup group = WindowManager.getDefault ().
                findTopComponentGroup ("debugger"); // NOI18N
        if (group != null) {
            group.close ();
            if (ToolbarPool.getDefault().getConfiguration().equals("Debugging")) { // NOI18N
                ToolbarPool.getDefault().setConfiguration(ToolbarPool.DEFAULT_CONFIGURATION);
            }
        }
    }
    
    public String[] getProperties () {
        return new String [] {
            DebuggerManager.PROP_DEBUGGER_ENGINES,
            DebuggerManager.PROP_CURRENT_ENGINE
        };
    }
    
}
