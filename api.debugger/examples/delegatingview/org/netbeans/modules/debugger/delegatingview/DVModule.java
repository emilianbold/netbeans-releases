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

package org.netbeans.modules.debugger.delegatingview;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.*;


import org.netbeans.modules.debugger.*;
import org.netbeans.modules.debugger.GUIManager.*;

import org.openide.TopManager;
import org.netbeans.modules.debugger.support.DebuggerModule;
import org.netbeans.modules.debugger.support.DelegatingView2;
import org.netbeans.modules.debugger.support.View2;
import org.netbeans.modules.debugger.support.View2Support;
import org.netbeans.modules.debugger.support.nodes.DebuggerNode;


/**
* Module installation class for HtmlModule
*
* @author Jan Jancura
*/
public class DVModule extends org.openide.modules.ModuleInstall {

    DelegatingView2 breakpointsView;
    DelegatingView2 threadsView;
    DelegatingView2 callStackView;
    DelegatingView2 sessionsView;
    DelegatingView2 watchesView; 
    DelegatingView2 classesView; 
    View2Support variablesListView;
    
    // ModuleInstall implementation ............................................
    
    /** Module installed again. */
    public void installed () {
        restored ();
    }
    
    /** Module installed again. */
    public void restored () {
        // create new "delegating" views
        breakpointsView = 
            new DelegatingView2 (new View2 [] {
                DebuggerModule.BREAKPOINTS_VIEW,//JPDA debugger
                new View2Support (              //tools debugger
                    DebuggerNode.getLocalizedString ("CTL_Breakpoints_view"), // NOI18N
                    "/org/netbeans/modules/debugger/resources/breakpoints", // NOI18N
                    "org.netbeans.modules.debugger.delegatingview.BreakpointsView", // NOI18N
                    false, // has not splitter
                    true,  // is in toolbar
                    true,  // visible
                    false  // separated
                )
            });
        threadsView = 
            new DelegatingView2 (new View2 [] {
                DebuggerModule.THREADS_VIEW,//JPDA debugger
                new View2Support (              //tools debugger
                    DebuggerNode.getLocalizedString ("CTL_Threads_view"), // NOI18N
                    "/org/netbeans/core/resources/threads", // NOI18N
                    "org.netbeans.modules.debugger.delegatingview.ThreadsView", // NOI18N
                    false, // has not splitter
                    true,  // is in toolbar
                    false,  // visible
                    false  // separated
                )
            });
        callStackView = 
            new DelegatingView2 (new View2 [] {
                DebuggerModule.CALL_STACK_VIEW,//JPDA debugger
                new View2Support (              //tools debugger
                    DebuggerNode.getLocalizedString ("CTL_Call_stack_view"), // NOI18N
                    "/org/netbeans/core/resources/callstack", // NOI18N
                    "org.netbeans.modules.debugger.delegatingview.CallStackView", // NOI18N
                    false, // has not splitter
                    true,  // is in toolbar
                    true,  // visible
                    false  // separated
                )
            });
        final DelegatingView2 sessionsView = 
            new DelegatingView2 (new View2 [] {
                DebuggerModule.SESSIONS_VIEW,//JPDA debugger
                new View2Support (              //tools debugger
                    DebuggerNode.getLocalizedString ("CTL_Sessions_view"), // NOI18N
                    "/org/netbeans/modules/debugger/multisession/resources/sessions", // NOI18N
                    "org.netbeans.modules.debugger.delegatingview.SessionsView", // NOI18N
                    false, // has not splitter
                    true,  // is in toolbar
                    false,  // visible
                    false  // separated
                )
            });
        watchesView = 
            new DelegatingView2 (new View2 [] {
                DebuggerModule.WATCHES_VIEW,//JPDA debugger
                new View2Support (              //tools debugger
                    DebuggerNode.getLocalizedString ("CTL_Watches_view"), // NOI18N
                    "/org/netbeans/core/resources/watches", // NOI18N
                    "org.netbeans.modules.debugger.delegatingview.WatchesView", // NOI18N
                    false, // has not splitter
                    true,  // is in toolbar
                    true,  // visible
                    false  // separated
                )
            });
        classesView = 
            new DelegatingView2 (new View2 [] {
                DebuggerModule.CLASSES_VIEW,  //JPDA debugger
                null                          //tools debugger
            });
        variablesListView = 
            new View2Support (              // visibility test
                DebuggerModule.VARIABLES_VIEW.getDisplayName (), // NOI18N
                DebuggerModule.VARIABLES_VIEW.getIconBase (),
                "org.netbeans.modules.debugger.support.nodes.VariablesView", // NOI18N
                DebuggerModule.VARIABLES_VIEW.hasFixedSize (), // has not splitter
                DebuggerModule.VARIABLES_VIEW.canBeHidden (),  // is in toolbar
                true,  // visible
                DebuggerModule.VARIABLES_VIEW.isSeparated ()  // separated
            );
            
        // replace original views
        View[] vs = GUIManager.getDefault ().getViews ();
        int i, k = vs.length;
        View[] nvs = new View [k];
        for (i = 0; i < k; i++)
            if (vs [i] == DebuggerModule.WATCHES_VIEW) {
                nvs [i] = watchesView;
            } else
            if (vs [i] == DebuggerModule.SESSIONS_VIEW) {
                nvs [i] = sessionsView;
            } else
            if (vs [i] == DebuggerModule.CALL_STACK_VIEW) {
                nvs [i] = callStackView;
            } else
            if (vs [i] == DebuggerModule.THREADS_VIEW) {
                nvs [i] = threadsView;
            } else
            if (vs [i] == DebuggerModule.BREAKPOINTS_VIEW) {
                nvs [i] = breakpointsView;
            } else
            if (vs [i] == DebuggerModule.CLASSES_VIEW) {
                nvs [i] = classesView;
            } else
            if (vs [i] == DebuggerModule.VARIABLES_VIEW) {
                nvs [i] = variablesListView;
            } else
                nvs [i] = vs [i];
        GUIManager.getDefault ().setViews (nvs);

        // add listener 
        try {
            final CoreDebugger cd = (CoreDebugger) TopManager.getDefault().getDebugger();
            cd.addPropertyChangeListener (new PropertyChangeListener () {
                public void propertyChange (PropertyChangeEvent e) {
                    if (e.getPropertyName () == null) return;
                    if (!e.getPropertyName ().equals (CoreDebugger.PROP_CURRENT_DEBUGGER)) return;
                    AbstractDebugger d = cd.getCurrentDebugger ();
                    if (d == null) return;
                    if (d.getClass ().getName ().indexOf ("JPDADebugger") >= 0) {
                        // switch to debugger1 views (JPDA debugger)
                        SwingUtilities.invokeLater (new Runnable () {
                            public void run () {
                                breakpointsView.setCurrentView (0);
                                classesView.setCurrentView (0);
                                sessionsView.setCurrentView (0);
                                threadsView.setCurrentView (0);
                                callStackView.setCurrentView (0);
                                watchesView.setCurrentView (0);
                                watchesView.refreshViews ();
                            }
                        });
                    } else
                    if (d.getClass ().getName ().indexOf ("ToolsDebugger") >= 0) {                        
                        // switch to debugger2 views (Tools debugger)
                        SwingUtilities.invokeLater (new Runnable () {
                            public void run () {
                                breakpointsView.setCurrentView (1);
                                classesView.setCurrentView (1);
                                sessionsView.setCurrentView (1);
                                threadsView.setCurrentView (1);
                                callStackView.setCurrentView (1);
                                watchesView.setCurrentView (1);
                                watchesView.refreshViews ();
                            }
                        });
                    }
                }
            });
        } catch (org.openide.debugger.DebuggerNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}

