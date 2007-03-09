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
package org.netbeans.modules.compapp.casaeditor.model.casa;

import org.openide.util.NbBundle;

/**
 *
 * @author jqian
 */


public enum CasaPortBindingState {
            
    BOUND("bound"),                             // NOI18N
    UNBOUND("unbound");                         // NOI18N
    
    CasaPortBindingState(String state) {
        this.state = state;
    }
    
    public String getState() {
        return state;
    }
    
    public static CasaPortBindingState getCasaPortBindingState(String state) {
        if (state == null) {
            return BOUND;   // be default
        } else if (state.equals("bound")) {     // NOI18N
            return BOUND;
        } else if (state.equals("unbound")) {   // NOI18N
            return UNBOUND;
        } else {
            throw new RuntimeException(NbBundle.getMessage(CasaPortBindingState.class, "Error_Illegal_connection_state") + state);  // NOI18N
        }
    }
    
    private final String state;
}
