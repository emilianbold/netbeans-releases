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

import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IInteractionOperand extends IInteractionFragment
{
	/**
	 * Adds a fragment to the collection of owned fragments.
	*/
	public void addFragment( IInteractionFragment frag );

	/**
	 * Removes a fragment from this operand.
	*/
	public void removeFragment( IInteractionFragment frag );

	/**
	 * Retrieves the collection of fragments this operand owns.
	*/
	public ETList<IInteractionFragment> getFragments();

	/**
	 * Create and attach the constraint of the operand.
	*/
	public IInteractionConstraint createGuard();

	/**
	 * Sets / Gets the constraint of the operand.
	*/
	public IInteractionConstraint getGuard();

	/**
	 * Sets / Gets the constraint of the operand.
	*/
	public void setGuard( IInteractionConstraint value );

	/**
	 * Helper function to get all the UML:Messages contained within this InteractionOperand
	*/
	public ETList<IMessage> getCoveredMessages();
}