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
package org.netbeans.modules.bpel.debugger.api;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.ContextProvider;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.10.14
 */
public final class TracerAccess {

    private TracerAccess() {}

    /**
     * Returns tracer for given context provider.
     * @param provider given provider
     * @return tracer for given context provider
     */
    public static Tracer getTracer(ContextProvider provider) {
      if (provider == null) {
        throw new IllegalArgumentException("Can't return tracer for null");// NOI18N
      }
      return getTracer (provider.lookupFirst(null, Session.class));
    }

    /**
     * Returns tracer for given session.
     * @param session given session
     * @return tracer for given session
     */
    public static Tracer getTracer(Session session) {
        return getTracerFactory ().getTracer(session);
    }

    private static TracerFactory getTracerFactory() {
        if (ourTracerFactory == null) {
            ourTracerFactory =
              DebuggerManager.getDebuggerManager().lookupFirst(null, TracerFactory.class);
            assert ourTracerFactory != null : "Can't find Tracer Factory"; // NOI18N
        }
        return ourTracerFactory;
    }

    private static TracerFactory ourTracerFactory;
}
