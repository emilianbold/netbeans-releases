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
import java.util.Map;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.DebugException;
import org.netbeans.modules.bpel.debugger.BpelDebuggerImpl;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.RequestProcessor;


/**
 * Starts the debugger by attaching to a BPEL service engine.
 */
public class DebugProjectActionProvider extends ActionsProviderSupport {

    protected ContextProvider mLookupProvider;
    private BpelDebuggerImpl mDebugger;
    
    
    public DebugProjectActionProvider(ContextProvider lookupProvider) {
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
    
    @Override
    public void postAction(Object action, Runnable actionPerformedNotifier) {
        final Map params = mLookupProvider.lookupFirst(null, Map.class);
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                mDebugger.setStartingThread(Thread.currentThread());
                try {
                    Map bpelseParams = null;
                    for (Object objEntry : params.entrySet()) {
                        final Map.Entry entry = (Map.Entry)objEntry;
                        final String key = (String)entry.getKey();
                        
                        if ("sun-bpel-engine".equals(key)) {
                            bpelseParams = (Map)entry.getValue();
                            break;
                        }
                    }
                    if (bpelseParams == null) {
                        return;
                    }
                    
                    final String j2eeServerInstance = 
                            (String)params.get("j2eeServerInstance");
                    final String host = 
                            getServerInstanceHost(j2eeServerInstance);
                    final Integer port = 
                            new Integer((String) bpelseParams.get("port"));
                    
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
    
    private static String getServerInstanceHost(String j2eeServerInstance) {
        //the j2eeServerInstance has the following format:
        //"[C:\Sun\glassfish_b32]deployer:Sun:AppServer::localhost:4848"
        //so we can obtain a host from the string itself
        //TODO:is there more reliable way to obtain a host?
        int to = j2eeServerInstance.lastIndexOf(":");
        int from = j2eeServerInstance.lastIndexOf(":", to - 1);
        return j2eeServerInstance.substring(from + 1, to);
    }
}
