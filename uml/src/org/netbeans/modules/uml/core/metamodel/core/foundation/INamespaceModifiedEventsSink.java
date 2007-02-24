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
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

public interface INamespaceModifiedEventsSink
{
	/**
	 * Fired whenever an element is about to be added to a namespace.
	*/
	public void onPreElementAddedToNamespace( INamespace space, INamedElement elementToAdd, IResultCell cell );

	/**
	 * Fired whenever an element is added to a namespace.
	*/
	public void onElementAddedToNamespace( INamespace space, INamedElement elementAdded, IResultCell cell );

}
