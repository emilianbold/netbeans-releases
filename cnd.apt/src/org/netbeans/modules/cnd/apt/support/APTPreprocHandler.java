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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.apt.support;

import java.util.Stack;

/**
 * composition of include handler and macro map for parsing file phase
 * @author Vladimir Voskresensky
 */
public interface APTPreprocHandler {
    /*
     * save/restore state of handler
     */
    public State getState();
    public void setState(State state);
    
    /** immutable state object of preprocessor handler */
    public interface State {
        /**
         * check whether state has correct flag or not;
         * the flag is "correct" when state was created for source file or 
         * for header included from source file
         */ 
        public boolean isStateCorrect();
        
        /**
         * check whether state has cached information or cleaned
         */
        public boolean isCleaned();        
    };   
    
    public APTMacroMap getMacroMap();
    public APTIncludeHandler getIncludeHandler();
    
    public boolean isStateCorrect();
}
