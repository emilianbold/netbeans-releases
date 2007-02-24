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


package org.netbeans.modules.uml.core.metamodel.common.commonactivities;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IObjectNode extends IActivityNode, ITypedElement
{
	/**
	 * property Ordering
	*/
	public int getOrdering();

	/**
	 * property Ordering
	*/
	public void setOrdering( /* ObjectNodeOrderingKind */ int value );

	/**
	 * property UpperBound
	*/
	public IValueSpecification getUpperBound();

	/**
	 * property UpperBound
	*/
	public void setUpperBound( IValueSpecification value );

	/**
	 * method AddInState
	*/
	public void addInState( IState pState );

	/**
	 * method RemoveInState
	*/
	public void removeInState( IState pState );

	/**
	 * property InStates
	*/
	public ETList<IState> getInStates();

	/**
	 * property Selection
	*/
	public IBehavior getSelection();

	/**
	 * property Selection
	*/
	public void setSelection( IBehavior value );

}
