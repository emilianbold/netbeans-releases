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
package org.netbeans.modules.xml.text.syntax.javacc.lib;

import org.netbeans.editor.Syntax;

/**
 * State info holder enriched by jj substates.
 *
 * @author  Petr Kuzel
 * @version 0.9
 */

public final class JJStateInfo extends Syntax.BaseStateInfo {

    private int[] states;

    public void setSubStates(int[] states) {
        this.states = states;
    }

    public int[] getSubStates() {
        return states;
    }


    /** @return whether passed substates equals to this substates. */
    public int compareSubStates(int[] sub) {
        if (states == null) return Syntax.DIFFERENT_STATE;
        if (sub == null) return Syntax.DIFFERENT_STATE;
        if (states.length != sub.length) return Syntax.DIFFERENT_STATE;
        
        for (int i = states.length-1; i>=0; i--) {  //faster
            if (states[i] != sub[i]) return Syntax.DIFFERENT_STATE;
        }
        return Syntax.EQUAL_STATE;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("(JJ[").append("S:" + getState()); // NOI18N
        buf.append("P:" + getPreScan()).append("subS:");  // NOI18N
        for (int i=0; i<states.length; i++) {
            buf.append(states[i] + ","); // NOI18N
        }
        buf.append("]JJ)"); // NOI18N
        return buf.toString();
    }
}
