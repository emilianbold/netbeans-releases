/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
