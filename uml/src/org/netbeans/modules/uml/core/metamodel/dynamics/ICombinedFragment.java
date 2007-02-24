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


package org.netbeans.modules.uml.core.metamodel.dynamics;

import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IInteractionOperator;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface ICombinedFragment extends IInteractionFragment
{
	/**
	 * Creates a new interaction operand and adds it to this combined fragment.
	*/
	public IInteractionOperand createOperand();

	/**
	 * Inserts an interaction operand (possibly creating it) before another interation operand.
	*/
	public void insertOperand( IInteractionOperand pOperand, IInteractionOperand pBeforeOperand );

	/**
	 * Adds an operand to this combined fragment.
	*/
	public void addOperand( IInteractionOperand op );

	/**
	 * Removes the specified operand from this fragment.
	*/
	public void removeOperand( IInteractionOperand op );

	/**
	 * Retrieves the collection of operands this fragment owns.
	*/
	public ETList<IInteractionOperand> getOperands();

	/**
	 * Adds an expression gate to this fragment.
	*/
	public void addGate( IGate gate );

	/**
	 * Removes the specified gate from this fragment.
	*/
	public void removeGate( IGate gate );

	/**
	 * Retrieves the collection of gates this fragment owns.
	*/
	public ETList<IGate> getGates();

	/**
	 * Sets / Gets the operator kind of this fragment.
	*/
	public int getOperator();

	/**
	 * Sets / Gets the operator kind of this fragment.
	*/
	public void setOperator( int value );
}
