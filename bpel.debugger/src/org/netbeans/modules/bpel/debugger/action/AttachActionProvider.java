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


package org.netbeans.modules.bpel.debugger.action;

import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.AttachingCookie;
import org.netbeans.modules.bpel.debugger.api.DebugException;
import org.netbeans.modules.bpel.debugger.BpelDebuggerImpl;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.RequestProcessor;


/**
 * Starts the debugger by attaching to a BPEL service engine.
 */
public class AttachActionProvider extends ActionsProviderSupport {

    protected ContextProvider mLookupProvider;
    private BpelDebuggerImpl mDebugger;
    
    
    public AttachActionProvider(ContextProvider lookupProvider) {
        mLookupProvider = lookupProvider;
        mDebugger = 
            (BpelDebuggerImpl) mLookupProvider.lookupFirst(null, BpelDebugger.class);
        setEnabled(ActionsManager.ACTION_START, true);
    }
    
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_START);
    }

    public void doAction (Object action) {
        // awt version used instead - postAction
    }
    
    public void postAction(Object action, Runnable actionPerformedNotifier) {
        AttachingCookie cookie = 
            mLookupProvider.lookupFirst(null, AttachingCookie.class);
        final String host = cookie.getHost();
        final int port = cookie.getPort();
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                mDebugger.setStartingThread(Thread.currentThread());
                try {
                    mDebugger.setRunning(host, port);
                } catch (Exception ex) {
                    mDebugger.setException(new DebugException(ex));
                } finally {
                    mDebugger.unsetStartingThread();
                }
            }
        });
        if (actionPerformedNotifier != null) {
            actionPerformedNotifier.run();
        }
    }
}
