/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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
