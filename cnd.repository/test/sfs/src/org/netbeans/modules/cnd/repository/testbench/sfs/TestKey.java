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

package org.netbeans.modules.cnd.repository.testbench.sfs;

import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;


class TestKey implements Key {
    
    private String key;
    
    public Behavior getBehavior() {
	return Behavior.Default;
    }
    
    public TestKey(String key) {
	this.key = key;
    }
    
    public String getAt(int level) {
	return key;
    }
    
    public int getDepth() {
	return 1;
    }
    
    public PersistentFactory getPersistentFactory() {
	return TestFactory.instance();
    }
    
    public int getSecondaryAt(int level) {
	return 0;
    }
    
    public int getSecondaryDepth() {
	return 0;
    }
    
    public int hashCode() {
	return key.hashCode();
    }
    
    public boolean equals(Object obj) {
	if (obj != null && TestKey.class == obj.getClass()) {
	    return this.key.equals(((TestKey) obj).key);
	}
	return false;
    }    

    public String toString() {
	return key;
    }

    public String getUnit() {
	return "Test"; // NOI18N
    }
    
}
