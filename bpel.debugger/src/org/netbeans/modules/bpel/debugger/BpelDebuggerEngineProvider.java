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

package org.netbeans.modules.bpel.debugger;

import java.awt.Component;
import java.beans.beancontext.BeanContextChildComponentProxy;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.openide.windows.WindowManager;

/**
 *
 * @author Alexander Zgursky
 */
public class BpelDebuggerEngineProvider extends DebuggerEngineProvider {

    public static final String ID = "netbeans-BpelEngine"; // NOI18N

    private DebuggerEngine.Destructor   myDesctuctor;
    private Session                     mySession;

    /**
     * Creates a new instance of BpelEngineProvider.
     *
     * @param contextProvider provides context for the BPEL debugger engine
     */
    public BpelDebuggerEngineProvider(ContextProvider contextProvider) {
        mySession = contextProvider.lookupFirst(null, Session.class);
    }
    
    public String[] getLanguages() {
        return new String[] {"BPEL"}; // NOI18N
    }
    
    public String getEngineTypeID() {
        return ID;
    }
    
    public Object[] getServices() {
        return getUiComponentProxies();
    }
    
    public void setDestructor(DebuggerEngine.Destructor desctuctor) {
        myDesctuctor = desctuctor;
    }
    
    /**
     * Returns destructor to be used when finishing debug session.
     * @return destructor
     */
    public DebuggerEngine.Destructor getDestructor() {
        return myDesctuctor;
    }
    
    /**
     * Returns the debug session in the context of which this debugger engine
     * operates.
     * 
     * @return debug session
     */
    public Session getSession() {
        return mySession;
    }
    
    private static Object[] getUiComponentProxies() {
        
        class ComponentProxy implements BeanContextChildComponentProxy {
            
            private String name;
            
            public ComponentProxy(final String name) {
                this.name = name;
            }
            
            public Component getComponent() {
                return WindowManager.getDefault().findTopComponent(name);
            }
            
        }
        
        return new Object[] {
            new ComponentProxy("localsView"),
            new ComponentProxy("watchesView"),
            new ComponentProxy("breakpointsView"),
            new ComponentProxy("BPELPLinksView"),
            new ComponentProxy("ProcessExecutionView"),
            new ComponentProxy("ProcessView")
        };
        
    }
}
