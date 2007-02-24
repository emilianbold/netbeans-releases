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
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IInteractionOccurrence extends IInteractionFragment, IBehavior
{
	/**
	 * Sets / Gets the Interaction this occurrence refers to.
	*/
	public IInteraction getInteraction();

	/**
	 * Sets / Gets the Interaction this occurrence refers to.
	*/
	public void setInteraction( IInteraction value );

	/**
	 * Adds an actual gate to this occurrence.
	*/
	public void addGate( IGate gate );

	/**
	 * Removes the specified gate from this occurrence.
	*/
	public void removeGate( IGate gate );

	/**
	 * Retrieves the collection of actual gates owned by this occurrence.
	*/
	public ETList<IGate> getGates();

	/**
	 * Sets / Gets the Behavior this occurrence refers to.
	*/
	public IBehavior getBehavior();

	/**
	 * Sets / Gets the Behavior this occurrence refers to.
	*/
	public void setBehavior( IBehavior value );
}