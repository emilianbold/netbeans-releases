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

import java.util.Stack;

/**
 *
 * @author Vladimir Voskresensky
 */
public interface APTIncludeHandler {
    /*
     * save/restore state of handler
     */
    public State getState();
    public void setState(State state);
    public interface State {
        /**
         * clear cached restorable information of state
         * @return true if there were cleaned information which will need further restoring
         */
        public boolean cleanExceptIncludeStack();

        /**
         * get include stack and clean this information internally
         */
        public Stack/*<IncludeInfo>*/ cleanIncludeStack();
    
    };     
    
    /*
     * 
     * notify about inclusion
     * @param path included file absolute path
     * @param directiveLine line number of #include directive in original file (1-based)
     * @return false if inclusion is recursive and was prohibited
     */
    public boolean pushInclude(String path, int directiveLine);
    
    /*
     * notify about finished inclusion
     */
    public String popInclude();
    
    /**
     * get resolver for path
     */
    public APTIncludeResolver getResolver(String path);

    /**
     * returns the first file where include stack started
     */
    public String getStartFile();
    
    /**
     * include stack entry
     * - line where #include directive was
     * - resolved #include directive as absolute included path
     */
    public interface IncludeInfo {
        public String getIncludedPath();
        public int getIncludeDirectiveLine();
    } 
    
}
