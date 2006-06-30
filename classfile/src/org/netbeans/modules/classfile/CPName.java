/*
 * CPName.java
 *
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
 *
 * Contributor(s): Thomas Ball
 *
 * Version: $Revision$
 */

package org.netbeans.modules.classfile;

/**
 * The base class for all constant pool types which store strings.
 *
 * @author Thomas Ball
 */
abstract class CPName extends CPEntry {

    final static int INVALID_INDEX = -1;

    int index;
    private String name;

    CPName(ConstantPool pool,int index) {
	super(pool);
        this.index = index;
    }

    CPName(ConstantPool pool) {
	super(pool);
	index = INVALID_INDEX;
    }

    public String getName() {
	if (index == INVALID_INDEX) {
	    return null;
        }            
        if (name == null) {
            name = ((CPName)pool.cpEntries[index]).getName();
        }
        return name;
    }
    
    public Object getValue() {
        return getName();
    }

    void setNameIndex(int index) {
	this.index = index;
        name = null;
    }

    public String toString() {
	return getClass().getName() + ": name=" + 
	    (index == INVALID_INDEX ? "<unresolved>" :  //NOI18N
	     ((CPName)pool.cpEntries[index]).getName());
    }
}
