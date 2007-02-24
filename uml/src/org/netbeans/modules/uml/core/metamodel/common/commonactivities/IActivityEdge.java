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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IActivityEdge extends IRedefinableElement
{
	/**
	 * property Activity
	*/
	public IActivity getActivity();

	/**
	 * property Activity
	*/
	public void setActivity( IActivity value );

	/**
	 * property Source
	*/
	public IActivityNode getSource();

	/**
	 * property Source
	*/
	public void setSource( IActivityNode value );

	/**
	 * property Target
	*/
	public IActivityNode getTarget();

	/**
	 * property Target
	*/
	public void setTarget( IActivityNode value );

	/**
	 * property Guard
	*/
	public IValueSpecification getGuard();

	/**
	 * property Guard
	*/
	public void setGuard( IValueSpecification value );

	/**
	 * method AddGroup
	*/
	public void addGroup( IActivityGroup pGroup );

	/**
	 * method RemoveGroup
	*/
	public void removeGroup( IActivityGroup pGroup );

	/**
	 * property Groups
	*/
	public ETList<IActivityGroup> getGroups();

	/**
	 * property Weight
	*/
	public IValueSpecification getWeight();

	/**
	 * property Weight
	*/
	public void setWeight( IValueSpecification value );
    
}
