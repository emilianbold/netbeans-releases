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
package org.openide.filesystems.multifs;

import java.io.File;

import org.netbeans.performance.DataDescriptor;
import org.openide.filesystems.multifs.MultiXMLFSTest.FileWrapper;

/**
 * Describes data
 */
final class MFSDataDescriptor extends DataDescriptor {
    
    private int foCount;
    private int fsCount;
    
    private FileWrapper[] wrappers;
    
    /** New MFSDataDescriptor */
    public MFSDataDescriptor(int foCount, int fsCount) {
        this.foCount = foCount;
        this.fsCount = fsCount;
    }
    
    /** Setter for wrappers */
    void setFileWrappers(FileWrapper[] wrappers) {
        this.wrappers = wrappers;
    }
    
    /** Getter for wrappers */
    public FileWrapper[] getFileWrappers() {
        return wrappers;
    }
    
    /** Getter for foCount */
    public int getFoCount() {
        return foCount;
    }
    
    /** Getter for xmlfsCount */
    public int getFsCount() {
        return fsCount;
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
        
        if (obj instanceof MFSDataDescriptor) {
            MFSDataDescriptor dd = (MFSDataDescriptor) obj;
            return getClassName().equals(dd.getClassName()) && foCount == dd.foCount && fsCount == dd.fsCount;
        }
        
        return false;
    }
}
