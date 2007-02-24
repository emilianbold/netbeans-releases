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
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IActivityNode extends IRedefinableElement
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
	 * method AddOutgoingEdge
	*/
	public void addOutgoingEdge( IActivityEdge pEdge );

	/**
	 * method RemoveOutgoingEdge
	*/
	public void removeOutgoingEdge( IActivityEdge pEdge );

	/**
	 * property OutgoingEdges
	*/
	public ETList<IActivityEdge> getOutgoingEdges();

	/**
	 * method AddIncomingEdge
	*/
	public void addIncomingEdge( IActivityEdge pEdge );

	/**
	 * method RemoveIncomingEdge
	*/
	public void removeIncomingEdge( IActivityEdge pEdge );

	/**
	 * property IncomingEdges
	*/
	public ETList<IActivityEdge> getIncomingEdges();

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

}
