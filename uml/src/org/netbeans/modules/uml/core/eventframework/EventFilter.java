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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.uml.core.eventframework;

/**
 * @author sumitabhk
 *
 */
public class EventFilter implements IEventFilter{

	/**
	 *
	 */
	public EventFilter() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.eventframework.IEventFilter#validateEvent(java.lang.String, java.lang.Object)
	 */
	public boolean validateEvent(String triggerName, Object payLoad) {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.eventframework.IEventFilter#getFilterID()
	 */
	public String getFilterID() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.eventframework.IEventFilter#setFilterID(java.lang.String)
	 */
	public void setFilterID(String value) {
		// TODO Auto-generated method stub
		
	}

}



