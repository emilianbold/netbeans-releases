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

import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IPresentationElement extends IElement {

	/*
	 * Retrieves the collection of Elements this element is associated with.
	 */
	public ETList < IElement > getSubjects();

	/*
	 * Returns the first element on the subjects collection.
	 */
	public IElement getFirstSubject();

	/*
	 * Associates the passed in Element with this PresentationElement.
	 */	
	public IElement addSubject(IElement elem);

	/*
	 * Removes the Element with the matching ID.
	 */	
	public void removeSubject(IElement elem);

	/*
	 * Determines whether or not the passed in element is a subject of this PresentationElement.
	 */	
	public boolean isSubject(IElement elem);

	/*
	 * Determines whether or not the passed in element is the first subject of this PresentationElement.
	 */ 	
	public boolean isFirstSubject2(String elementXMIID);
	
	/*
	 * Determines whether or not the passed in element is the first subject of this PresentationElement.
	 */ 		
	public boolean isFirstSubject(IElement pElement);

	/*
	 * Gets the ID of the physical display element associated with this PresentationElement.
	 */	
	public String getDisplayElementID();

	/*
	 * Sets the ID of the physical display element associated with this PresentationElement.
	 */
	public void setDisplayElementID(String id);

	/*
	 * Transforms this presentation element into another, such as an AssocationEdge into an AggregationEdge.
	 */
	public IPresentationElement transform(String elemName);

	/*
	 * Returns the number of subjects.
	 */
	public long getSubjectCount();

}