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
package org.netbeans.core;

import java.io.File;

import org.netbeans.performance.DataDescriptor;
import org.openide.filesystems.multifs.MultiXMLFSTest.FileWrapper;

/**
 * Describes data
 */
final class MUCDataDescriptor extends DataDescriptor {

    private int classCount;
    private int fsCount;

    private DataDescriptor dd;

    /** New MFSDataDescriptor */
    public MUCDataDescriptor(int classCount, int fsCount) {
        this.classCount = classCount;
        this.fsCount = fsCount;
    }

    /** Getter for classCount */
    public int getClassCount() {
        return classCount;
    }
    
    /** Getter for xmlfsCount */
    public int getFsCount() {
        return fsCount;
    }
    
    /** Getter for dd */
    public DataDescriptor getDD() {
        return dd;
    }
    
    /** Setter for dd */
    public void setDD(DataDescriptor dd) {
        this.dd = dd;
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
        
        if (obj instanceof MUCDataDescriptor) {
            MUCDataDescriptor dd = (MUCDataDescriptor) obj;
            
            boolean flag = this.dd == dd.dd || this.dd == null || this.dd.equals(dd.dd);
            
            return flag && getClassName().equals(dd.getClassName()) && classCount == dd.classCount && fsCount == dd.fsCount;
        }
        
        return false;
    }
}
