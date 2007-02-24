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
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IActivity extends IBehavior
{
	/**
	 * property Kind
	*/
	public int getKind();

	/**
	 * property Kind
	*/
	public void setKind( /* ActivityKind */ int value );

	/**
	 * property IsSingleCopy
	*/
	public boolean getIsSingleCopy();

	/**
	 * property IsSingleCopy
	*/
	public void setIsSingleCopy( boolean value );

	/**
	 * method AddNode
	*/
	public void addNode( IActivityNode pNode );

	/**
	 * method RemoveNode
	*/
	public void removeNode( IActivityNode pNode );

	/**
	 * property Nodes
	*/
	public ETList<IActivityNode> getNodes();

	/**
	 * method AddEdge
	*/
	public void addEdge( IActivityEdge pEdge );

	/**
	 * method RemoveEdge
	*/
	public void removeEdge( IActivityEdge pEdge );

	/**
	 * property Edges
	*/
	public ETList<IActivityEdge> getEdges();

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
	 * method AddPartition
	*/
	public void addPartition( IActivityPartition pPartition );

	/**
	 * method RemovePartition
	*/
	public void removePartition( IActivityPartition pPartition );

	/**
	 * property Partitions
	*/
	public ETList<IActivityPartition> getPartitions();

}
