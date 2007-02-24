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

package org.netbeans.modules.uml.core.scm;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;

/**
 * Provides versioning information for an IElement.
 */
public interface ISCMElementItem extends ISCMItem
{
	/**
	 * Retrieves the IElement assocated with the SCM item.
	*/
	public IElement getElement();

	/**
	 * Sets the IElement assocated with the SCM item.
	*/
	public void setElement( IElement value );

	/**
	 * Determines whether or not the IElement this item represents is actually an IProject.
	*/
	public boolean getIsProject();

	/**
	 * Determines whether or not to gather any associated Artifacts along with this element.
	*/
	public void setGatherArtifacts( boolean value );

	/**
	 * Determines whether or not to gather any associated Artifacts along with this element.
	*/
	public boolean getGatherArtifacts();

	/**
	 * Determines whether or not to gather any associated SourceFileArtifacts along with this element.
	*/
	public void setGatherSourceFileArtifacts( boolean value );

	/**
	 * Determines whether or not to gather any associated SourceFileArtifacts along with this element.
	*/
	public boolean getGatherSourceFileArtifacts();

}
