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
package org.netbeans.performance;

import java.io.Serializable;

/**
 * Describes data
 */
public class DataDescriptor implements Serializable {
    private String klassName;
    private String testName;
    private Object arg;
    
    /** Sets all data */
    final void set(String klassName, String testName, Object arg) {
        this.klassName = klassName;
        this.testName = testName;
        this.arg = arg;
    }
    
    /** @getter for klassName for that this DD was created */
    protected final String getClassName() {
        return klassName;
    }
    
    /** @getter for testName for that this DD was created */
    protected final String getTestName() {
        return testName;
    }
    
    /** @getter for arg for that this DD was created */
    protected final Object getArgument() {
        return arg;
    }
    
    /** @return hashCode */
    public int hashCode() {
        return klassName.hashCode() ^ testName.hashCode() ^ arg.hashCode();
    }
    
    /** @return boolean iff obj equals this */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        
        if (obj instanceof DataDescriptor) {
            DataDescriptor dd = (DataDescriptor) obj;
            return klassName.equals(dd.klassName) && testName.equals(dd.testName) && arg.equals(dd.arg);
        }
        
        return false;
    }
}
