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
