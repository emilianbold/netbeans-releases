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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 * @author sumitabhk
 *
 */
public class ElementLifeTimeEventsSink implements IElementLifeTimeEventsSink{

	/**
	 *
	 */
	public ElementLifeTimeEventsSink() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreCreate(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementPreCreate(String ElementType, IResultCell cell) {
		ETSystem.out.println("Got onElementPreCreate");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementCreated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementCreated(IVersionableElement element, IResultCell cell) {
		ETSystem.out.println("Got onElementCreated");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreDelete(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementPreDelete(IVersionableElement element, IResultCell cell) {
		ETSystem.out.println("Got onElementPreDelete");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementDeleted(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementDeleted(IVersionableElement element, IResultCell cell) {
		ETSystem.out.println("Got onElementDeleted");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreDuplicated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementPreDuplicated(IVersionableElement element, IResultCell cell) {
		ETSystem.out.println("Got onElementPreDuplicated");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementDuplicated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementDuplicated(IVersionableElement element, IResultCell cell) {
		ETSystem.out.println("Got onElementDuplicated");
	}
}



