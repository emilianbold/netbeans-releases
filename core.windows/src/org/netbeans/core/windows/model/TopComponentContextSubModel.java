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
        //tab index
        int index = -1;
        SplitConstraint[] constraints;
    } // end of Context

    /** Mapping <TopComponentID, Context> between top component and context holding
     its previous location */
    private final Map<String, Context> tcID2Contexts = new HashMap<String, Context> (10);
    
    public TopComponentContextSubModel() {
    }

    public void setTopComponentPreviousConstraints(String tcID, SplitConstraint[] constraints) {
        Context context = tcID2Contexts.get(tcID);
        if (context == null) {
            context = new Context();
            tcID2Contexts.put(tcID, context);
        }
        context.constraints = constraints;
    }
    
    public void setTopComponentPreviousMode(String tcID, ModeImpl mode, int index) {
        Context context = tcID2Contexts.get(tcID);
        if (context == null) {
            context = new Context();
            tcID2Contexts.put(tcID, context);
        }
        context.mode = mode;
        context.index = index;
    }
    
    public SplitConstraint[] getTopComponentPreviousConstraints(String tcID) {
        Context context = tcID2Contexts.get(tcID);
        return context == null ? null : context.constraints;
    }
    
    public ModeImpl getTopComponentPreviousMode(String tcID) {
        Context context = tcID2Contexts.get(tcID);
        return context == null ? null : context.mode;
    }
    
    public int getTopComponentPreviousIndex(String tcID) {
        Context context = tcID2Contexts.get(tcID);
        return context == null ? -1 : context.index;
    }
}
