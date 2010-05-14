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

package org.netbeans.modules.bpel.debugger.ui.action;

import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.spi.debugger.ContextProvider;


/**
 * Represents the detach action, which un-attaches the debugger
 * from a BPEL service engine.
 *
 */
public class KillActionProvider extends BpelActionsProviderSupport {

    public KillActionProvider(ContextProvider lookupProvider) {
        super(lookupProvider, ActionsManager.ACTION_KILL);
    }

    public void doAction (Object action) {
        Runnable runner = new Runnable(){
            public void run() {
                getDebugger().finish();
            }
        };
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(runner);
        } else {
            runner.run();
        }
        
    }
}
