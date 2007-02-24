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


package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface INotificationTargets
{
	/**
	 * The kind of event that trigger this change
	*/
	public int getKind();

	/**
	 * The kind of event that trigger this change
	*/
	public void setKind( /* ModelElementChangedKind */ int value );

	/**
	 * This was the model element that was changed
	*/
	public IElement getChangedModelElement();

	/**
	 * This was the model element that was changed
	*/
	public void setChangedModelElement( IElement value );

	/**
	 * This was the model element that was changed
	*/
	public IElement getSecondaryChangedModelElement();

	/**
	 * This was the model element that was changed
	*/
	public void setSecondaryChangedModelElement( IElement value );

	/**
	 * A list of presentation elements to notify
	*/
	public ETList < IPresentationElement > getPresentationElementsToNotify();

	/**
	 * A list of presentation elements to notify
	*/
	public void setPresentationElementsToNotify( ETList < IPresentationElement > value );

	/**
	 * Adds an IPresentationElement to our list of elements to notify (ChangedModelElement)
	*/
	public void addNotifiedElement( IPresentationElement pElementToNotify );

	/**
	 * Adds these IPresentationElements to our list of elements to notify (ChangedModelElement)
	*/
	public void addNotifiedElements( ETList < IPresentationElement > pElementsToNotify );

	/**
	 * Adds the IPresentationElements of this IElement to our list of elements to notify (ChangedModelElement)
	*/
	public void addElementsPresentationElements( IDiagram pDiagram, IElement pElementToGetPEsFrom );


}
