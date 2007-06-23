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

package org.netbeans.modules.cnd.modelimpl.repository;

import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Key.Behavior;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;


/*package*/
// have to be public or UID factory does not work
public abstract class AbstractKey implements Key, SelfPersistent {
    /**
     * must be implemented in child
     */
    public abstract String toString();
    
    /**
     * must be implemented in child
     */
    public abstract int hashCode();
    
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null || (this.getClass() != obj.getClass())) {
	    return false;
	}
	return true;
    }
    
    public Key.Behavior getBehavior() {
	return Behavior.Default;
    }
    
    public abstract int getSecondaryAt(int level);
    
    public abstract String getAt(int level);
    
    public abstract String getUnit();
    
    public abstract int getSecondaryDepth();
    
    public abstract int getDepth();
    
}
