/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows.model;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.SplitConstraint;
import org.openide.windows.TopComponent;

/**
 * Model which stores context of TopComponents in one mode. Context consists
 * of mode and constraints info of previous container TopComponent was part of.
 *
 * This sub model is not thread safe. It is supposed to be just part of DefaultModeModel
 * which is responsible for the synch.
 *
 * @author  Dafe Simonek
 */
final class TopComponentContextSubModel {
    
    private static final class Context {
        // XXX we should use weak reference for holding mode, to let it vanish
        ModeImpl mode;
        SplitConstraint[] constraints;
    } // end of Context

    /** Mapping <TopComponent, Context> between top component and context holding
     its previous location */
    private final Map tcs2Contexts = new HashMap(10);

    
    public TopComponentContextSubModel() {
    }

    public void setTopComponentPreviousConstraints(TopComponent tc, SplitConstraint[] constraints) {
        Context context = (Context)tcs2Contexts.get(tc);
        if (context == null) {
            context = new Context();
        }
        context.constraints = constraints;
    }
    
    public void setTopComponentPreviousMode(TopComponent tc, ModeImpl mode) {
        Context context = (Context)tcs2Contexts.get(tc);
        if (context == null) {
            context = new Context();
            tcs2Contexts.put(tc, context);
        }
        context.mode = mode;
    }
    
    public SplitConstraint[] getTopComponentPreviousConstraints(TopComponent tc) {
        Context context = (Context)tcs2Contexts.get(tc);
        return context == null ? null : context.constraints;
    }
    
    public ModeImpl getTopComponentPreviousMode(TopComponent tc) {
        Context context = (Context)tcs2Contexts.get(tc);
        return context == null ? null : context.mode;
    }
    
}
