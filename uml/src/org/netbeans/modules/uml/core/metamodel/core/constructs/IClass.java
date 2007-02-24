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

package org.netbeans.modules.uml.core.metamodel.core.constructs;

import org.netbeans.modules.uml.core.metamodel.infrastructure.IEncapsulatedClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IReception;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IClass extends IEncapsulatedClassifier
{
	/**
	 * method AddReception
	*/
	public void addReception( IReception rec );

	/**
	 * method RemoveReception
	*/
	public void removeReception( IReception rec );

	/**
	 * property Receptions
	*/
	public ETList<IReception> getReceptions();

	/**
	 * Determines whether an object specified by this class is active or not. An active object is an object that, as a direct concsequence of its creation, commences to execute its behavior specification, and does not cease until either the complete specification is ?É?
	*/
	public boolean getIsActive();

	/**
	 * Determines whether an object specified by this class is active or not. An active object is an object that, as a direct concsequence of its creation, commences to execute its behavior specification, and does not cease until either the complete specification is ?É?
	*/
	public void setIsActive( boolean value );

	/**
	 * Determines whether an object specified by this class is represented in code by a structure.
	*/
	public boolean getIsStruct();

	/**
	 * Determines whether an object specified by this class is represented in code by a structure.
	*/
	public void setIsStruct( boolean value );

	/**
	 * Determines whether an object specified by this class is represented in code by a union.
	*/
	public boolean getIsUnion();

	/**
	 * Determines whether an object specified by this class is represented in code by a union.
	*/
	public void setIsUnion( boolean value );
}
