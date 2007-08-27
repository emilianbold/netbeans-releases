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
 * @author Alexander Simon
 */
public final class ResolvedPath {
    private final String folder;
    private final String path;
    private final boolean isDefaultSearchPath;
    private final int index;
    
    public ResolvedPath(String folder, String path, boolean isDefaultSearchPath, int index) {
        this.folder = folder;
        this.path = path;
        this.isDefaultSearchPath = isDefaultSearchPath;
        this.index = 0;
    }
    /**
     * Resolved file path
     */
    public String getPath(){
        return path;
    }

    /**
     * Include path used for resolving file path
     */
    public String getFolder(){
        return folder;
    }

    /**
     * Returns true if path resolved from default path
     */
    public boolean isDefaultSearchPath(){
        return isDefaultSearchPath;
    }

    /**
     * Returns index of resolved path in user and system include paths
     */
    public int getIndex(){
        return index;
    }
    
    public String toString(){
        return path+" in "+folder; // NOI18N
    }
}
