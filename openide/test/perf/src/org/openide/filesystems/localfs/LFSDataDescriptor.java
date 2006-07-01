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
package org.openide.filesystems.localfs;

import org.netbeans.performance.DataDescriptor;

import java.io.File;

/**
 * Describes data
 */
final class LFSDataDescriptor extends DataDescriptor {

    // non persistent data!
    private transient File rootDir;
    private int foCount;

    /** New LFSDataDescriptor */
    public LFSDataDescriptor(int count) {
        this.foCount = count;
    }

    /** Sette for rootDir */
    final void setFile(File root) {
        this.rootDir = root;
    }

    /** @return foCount */
    final int getFileNo() {
        return foCount;
    }
    
    /** @return rootDir */
    final File getRootDir() {
        return rootDir;
    }
    
    /** @return hashCode */
    public int hashCode() {
        return getClassName().hashCode();
    }
    
    /** @return boolean iff obj equals this */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        
        if (obj instanceof LFSDataDescriptor) {
            LFSDataDescriptor dd = (LFSDataDescriptor) obj;
            return getClassName().equals(dd.getClassName()) && foCount == dd.foCount;
        }
        
        return false;
    }
    
    public String toString() {
        return super.toString() + " root: " + rootDir + " foCount: " + foCount + " " + System.identityHashCode(this);
    }
}
