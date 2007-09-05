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
    
    /** immutable state object of include handler */    
    public interface State {
    };
    
    /*
     * 
     * notify about inclusion
     * @param path included file absolute path
     * @param directiveLine line number of #include directive in original file (1-based)
     * @param resolvedDirIndex index of resolved directory in lists of include paths
     * @return false if inclusion is recursive and was prohibited
     */
    public boolean pushInclude(String path, int directiveLine, int resolvedDirIndex);
    
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
    public StartEntry getStartEntry();
    
    /**
     * include stack entry
     * - line where #include directive was
     * - resolved #include directive as absolute included path
     */
    public interface IncludeInfo {
        public String getIncludedPath();
        public int getIncludeDirectiveLine();
        public int getIncludedDirIndex();
    } 
    
}
