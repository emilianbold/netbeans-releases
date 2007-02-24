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
public interface IElementLifeTimeEventsSink
{
	/**
	 * Fired whenever an element is about to be created.
	*/
	public void onElementPreCreate( String ElementType, IResultCell cell );

	/**
	 * Fired whenever after an element is created.
	*/
	public void onElementCreated( IVersionableElement element, IResultCell cell );

	/**
	 * Fired whenever an element is about to be deleted.
	*/
	public void onElementPreDelete( IVersionableElement element, IResultCell cell );

	/**
	 * Fired after an element has been deleted.
	*/
	public void onElementDeleted( IVersionableElement element, IResultCell cell );

	/**
	 * Fired whenever an element is about to be duplicated.
	*/
	public void onElementPreDuplicated( IVersionableElement element, IResultCell cell );

	/**
	 * Fired after an element has been duplicated.
	*/
	public void onElementDuplicated( IVersionableElement element, IResultCell cell );
}
