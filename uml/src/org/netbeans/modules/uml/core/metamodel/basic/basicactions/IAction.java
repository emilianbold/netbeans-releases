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


package org.netbeans.modules.uml.core.metamodel.basic.basicactions;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IAction extends IElement
{
	/**
	 *
	*/
	public boolean getIsReadOnly();

	/**
	 *
	*/
	public void setIsReadOnly( boolean value );

	/**
	 *
	*/
	public void addSuccessor( IAction pAction );

	/**
	 *
	*/
	public void removeSuccessor( IAction pAction );

	/**
	 * 
	*/
	public ETList <IAction> getSuccessors();

	/**
	 * 
	*/
	public void addPredecessor( IAction pAction );

	/**
	 * 
	*/
	public void removePredecessor( IAction pAction );

	/**
	 * 
	*/
	public ETList <IAction> getPredecessors();

	/**
	 * 
	*/
	public void addOutput( IOutputPin pPin );

	/**
	 * 
	*/
	public void removeOutput( IOutputPin pPin );

	/**
	 * 
	*/
	public ETList <IOutputPin> getOutputs();

	/**
	 * 
	*/
	public void addInput( IValueSpecification pPin );

	/**
	 * 
	*/
	public void removeInput( IValueSpecification pPin );

	/**
	 * 
	*/
	public ETList <IValueSpecification> getInputs();

	/**
	 * 
	*/
	public void addJumpHandler( IJumpHandler pHandler );

	/**
	 * 
	*/
	public void removeJumpHandler( IJumpHandler pHandler );

	/**
	 * 
	*/
	public ETList <IJumpHandler> getJumpHandlers();

}
