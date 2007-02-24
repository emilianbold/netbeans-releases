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
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IOperation extends IBehavioralFeature,
									IParameterableElement
{
	/**
	 * property IsQuery
	*/
	public boolean getIsQuery();

	/**
	 * property IsQuery
	*/
	public void setIsQuery( boolean value );

	/**
	 * method AddPostCondition
	*/
	public void addPostCondition( IConstraint cond );

	/**
	 * method RemovePostCondition
	*/
	public void removePostCondition( IConstraint cond );

	/**
	 * property PostConditions
	*/
	public ETList<IConstraint> getPostConditions();

	/**
	 * method AddPreCondition
	*/
	public void addPreCondition( IConstraint cond );

	/**
	 * method RemovePreCondition
	*/
	public void removePreCondition( IConstraint cond );

	/**
	 * property PreConditions
	*/
	public ETList<IConstraint> getPreConditions();

	/**
	 * Indicates that this Operation can potentially raise the passed in Classifier in an Exception.
	*/
	public void addRaisedException( IClassifier exc );

	/**
	 * Removes the passed in Classifier from the list of Classifiers that can be raised as an exception.
	*/
	public void removeRaisedException( IClassifier exc );

	/**
	 * Retrieves the collection of Classifiers that can be raised in Exceptions from this Operation.
	*/
	public ETList<IClassifier> getRaisedExceptions();

	/**
	 * Adds an exception by name.
	*/
	public void addRaisedException2( String classifierName );

	/**
	 * Determines whether or not this Operation is a constructor.
	*/
	public boolean getIsConstructor();

	/**
	 * Determines whether or not this Operation is a constructor.
	*/
	public void setIsConstructor( boolean value );

    /**
     * Determines whether or not this Operation is a destructor.
    */
    public boolean getIsDestructor();
	
	/**
     * Determines whether or not this Operation is a destructor.
    */
    public void setIsDestructor(boolean value);
	/**
	 * Determines whether or not this Operation represents a property.
	*/
	public boolean getIsProperty();

	/**
	 * Determines whether or not this Operation represents a property.
	*/
	public void setIsProperty( boolean value );

	/**
	 * Determines whether or not this Operation represents a friend operation to the enclosing Classifier.
	*/
	public boolean getIsFriend();

	/**
	 * Determines whether or not this Operation represents a friend operation to the enclosing Classifier.
	*/
	public void setIsFriend( boolean value );

	public boolean getIsSubroutine();
	public void setIsSubroutine( boolean value );

	public boolean getIsVirtual();
	public void setIsVirtual( boolean value );

	public boolean getIsOverride();
	public void setIsOverride( boolean value );

	public boolean getIsDelegate();
	public void setIsDelegate( boolean value );

	public boolean getIsIndexer();
	public void setIsIndexer( boolean value );
    
    public String getRaisedExceptionsAsString();
}
