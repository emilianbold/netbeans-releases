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
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 *
 */
public class NamedElementEventsSink implements INamedElementEventsSink{

	/**
	 *
	 */
	public NamedElementEventsSink() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreNameModified(INamedElement element, String proposedName, IResultCell cell) {
		ETSystem.out.println("Got onPreNameModified");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onNameModified(INamedElement element, IResultCell cell) {
		ETSystem.out.println("Got onNameModified");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreVisibilityModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreVisibilityModified(INamedElement element, int proposedValue, IResultCell cell) {
		ETSystem.out.println("Got onPreVisibilityModified");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onVisibilityModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onVisibilityModified(INamedElement element, IResultCell cell) {
		ETSystem.out.println("Got onVisibilityModified");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreAliasNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreAliasNameModified(INamedElement element, String proposedName, IResultCell cell) {
		ETSystem.out.println("Got onPreAliasNameModified");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onAliasNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onAliasNameModified(INamedElement element, IResultCell cell) {
		ETSystem.out.println("Got onAliasNameModified");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreNameCollision(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String, org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreNameCollision(INamedElement element, String proposedName, ETList<INamedElement> collidingElements, IResultCell cell) {
		ETSystem.out.println("Got onPreNameCollision");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onNameCollision(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onNameCollision(INamedElement element, ETList<INamedElement> collidingElements, IResultCell cell) {
		ETSystem.out.println("Got onNameCollision");
		
	}

}



