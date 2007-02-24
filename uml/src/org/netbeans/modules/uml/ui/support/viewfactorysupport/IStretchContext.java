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


package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;

public interface IStretchContext
{
	/**
	 * Stretch context type.  The type will be one of the StretchContext values.
	*/
	public int getType();

	/**
	 * Stretch context type.
    * @param The new type. The type must be one of the StretchContext values.
	*/
	public void setType( /* StretchContextType */ int value );

	/**
	 * The area used to restrict movement during during the stretch operation, set during the start of the stretch
	*/
	public IETRect getRestrictedArea();

	/**
	 * The area used to restrict movement during during the stretch operation, set during the start of the stretch
	*/
	public void setRestrictedArea( IETRect value );

	/**
	 * location where the stretching began
	*/
	public IETPoint getStartPoint();

	/**
	 * location where the stretching began
	*/
	public void setStartPoint( IETPoint value );

	/**
	 * location where the stretching has finished
	*/
	public IETPoint getFinishPoint();

	/**
	 * location where the stretching has finished
	*/
	public void setFinishPoint( IETPoint value );

	/**
	 * the diference between the finish and start points
	*/
	public IETSize getStretchSize();

	/**
	 * The Compartment being stretched.
	*/
	public ICompartment getCompartment();

	/**
	 * The Compartment being stretched.
	*/
	public void setCompartment( ICompartment value );

}
