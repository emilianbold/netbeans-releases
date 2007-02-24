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

package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
public interface IOperationEventsSink
{
	/**
	 * Fired whenever a pre or post condition is about to be added to an operation.
	*/
	public void onConditionPreAdded( IOperation oper, IConstraint condition, boolean isPreCondition, IResultCell cell );

	/**
	 * Fired whenever  a pre or post condition has been added to an operation.
	*/
	public void onConditionAdded( IOperation oper, IConstraint condition, boolean isPreCondition, IResultCell cell );

	/**
	 * Fired whenever a pre or post condition is about to be removed from an operation.
	*/
	public void onConditionPreRemoved( IOperation oper, IConstraint condition, boolean isPreCondition, IResultCell cell );

	/**
	 * Fired whenever a pre or post condition is about to be removed from an operation.
	*/
	public void onConditionRemoved( IOperation oper, IConstraint condition, boolean isPreCondition, IResultCell cell );

	/**
	 * Fired whenever the query flag on an operation is about to be modified.
	*/
	public void onPreQueryModified( IOperation oper, boolean proposedValue, IResultCell cell );

	/**
	 * Fired whenever the query flag on an operation has been modified.
	*/
	public void onQueryModified( IOperation oper, IResultCell cell );

	/**
	 * Fired whenever a RaisedException is about to be added to an operation.
	*/
	public void onRaisedExceptionPreAdded( IOperation oper, IClassifier pException, IResultCell cell );

	/**
	 * Fired whenever a RaisedException has been added to an operation.
	*/
	public void onRaisedExceptionAdded( IOperation oper, IClassifier pException, IResultCell cell );

	/**
	 * Fired whenever a RaisedException is about to be removed from an operation.
	*/
	public void onRaisedExceptionPreRemoved( IOperation oper, IClassifier pException, IResultCell cell );

	/**
	 * Fired whenever a RaisedException is about to be removed from an operation.
	*/
	public void onRaisedExceptionRemoved( IOperation oper, IClassifier pException, IResultCell cell );
 
    /**
     * Fired when a property is about to be changed on the operation.
    */
    public void onPreOperationPropertyModified( IOperation oper, /* OperationPropertyKind */ int nKind, boolean proposedValue, IResultCell cell );

    /**
     * Fired when a property changes on the operation.
    */
    public void onOperationPropertyModified( IOperation oper, /* OperationPropertyKind */ int nKind, IResultCell cell );
}