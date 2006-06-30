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

package org.netbeans.modules.j2ee.dd.api.ejb.j2ee1_3;

import org.netbeans.modules.schema2beans.Version;
import org.netbeans.modules.j2ee.dd.impl.common.ComponentBeanSingle;

abstract public class EntityBean extends ComponentBeanSingle {

    public EntityBean(java.util.Vector comps, Version version) {
	super(comps, version);
    }

    //
    // The 1.3 descriptor wants True or False instead of the boolean value
    //
    public static final String TRUE = "True";	//NOI18N
    public static final String FALSE = "False";	//NOI18N

    public void setReentrant(boolean value) {
	if (value) {
	    setReentrant(TRUE);
	} else {
	    setReentrant(FALSE);
	}
    }

    public boolean isReentrant() {
	String value = getReentrant();
	if ((value == null) || value.equals(FALSE)) {
	    return false;
	} else {
	    return true;
	}
    }

    abstract public void setReentrant(String value);
    abstract public String getReentrant();
}
