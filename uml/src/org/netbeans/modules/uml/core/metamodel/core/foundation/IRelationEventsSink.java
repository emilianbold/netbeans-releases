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

public interface IRelationEventsSink
{
	/**
	 * Fired before a relation meta type is modified. This includes Dependency, Generalization, and Associations.
	*/
	public void onPreRelationEndModified( IRelationProxy proxy, IResultCell cell );

	/**
	 * Fired after a relation meta type has been modified. This includes Dependency, Generalization, and Associations.
	*/
	public void onRelationEndModified( IRelationProxy Payload, IResultCell cell );

	/**
	 * Fired before a relation meta type is added to. This includes Dependency, Generalization, and Associations.
	*/
	public void onPreRelationEndAdded( IRelationProxy proxy, IResultCell cell );

	/**
	 * Fired after a relation meta type has been added to. This includes Dependency, Generalization, and Associations.
	*/
	public void onRelationEndAdded( IRelationProxy proxy, IResultCell cell );

	/**
	 * Fired before a relation meta type is removed from. This includes Dependency, Generalization, and Associations.
	*/
	public void onPreRelationEndRemoved( IRelationProxy proxy, IResultCell cell );

	/**
	 * Fired after a relation meta type has been removed from. This includes Dependency, Generalization, and Associations.
	*/
	public void onRelationEndRemoved( IRelationProxy proxy, IResultCell cell );

	/**
	 * Fired before a relation meta type is created.
	*/
	public void onPreRelationCreated( IRelationProxy proxy, IResultCell cell );

	/**
	 * Fired after a relation meta type has been created.
	*/
	public void onRelationCreated( IRelationProxy proxy, IResultCell cell );

	/**
	 * Fired before a relation meta type is deleted.
	*/
	public void onPreRelationDeleted( IRelationProxy proxy, IResultCell cell );

	/**
	 * Fired after a relation meta type has been deleted.
	*/
	public void onRelationDeleted( IRelationProxy proxy, IResultCell cell );

}
