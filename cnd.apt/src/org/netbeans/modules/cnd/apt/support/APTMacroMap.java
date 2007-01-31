/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.apt.support;

import antlr.Token;
import java.util.Collection;
import java.util.List;

/**
 * interface to support contextual macro definitions map
 * @author Vladimir Voskresensky
 */
public interface APTMacroMap extends APTMacroCallback {
    /*
     * save/restore state of map
     */
    public State getState();
    public void setState(State state);
    public interface State {
        /**
         * clear cached restorable information of state
         * @return true if there were cleaned information which will need further restoring
         */
        public boolean clean();
    };    
    
    /** 
     * APTWalker context methods to (un)define macros 
     */    
    public void define(Token name, List value);
    public void define(Token name, Collection params, List value);
    public void undef(Token name);       
}
