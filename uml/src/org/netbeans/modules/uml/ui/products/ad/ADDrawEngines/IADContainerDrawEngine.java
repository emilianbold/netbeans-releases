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



package org.netbeans.modules.uml.ui.products.ad.ADDrawEngines;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;

/**
 * @author sumitabhk
 *
 */
public interface IADContainerDrawEngine extends IADNodeDrawEngine
{
	// Returns true when there are contained presentation elements
	public boolean hasContained();

	// Retrieve the presentation elements contained within this container node
	public ETList <IPresentationElement> getContained();
        
        /** 
         * Retrieve the presentation elements contained within this container.
         * 
         * @param bNeedOnlyOne 
         * @param deep to recursively search for contained presentation elements.
         *             If a container contains a container the sub containers
         *             children should be returned if deep is true.
         * @param verify test if the list of children should be verified before
         *               being returned.
         * @return The list of children.
         */
        public ETList < IPresentationElement > getContained(boolean bNeedOnlyOne, 
                                                            boolean deep,
                                                            boolean verify);

	// Populates this container with what it's contents should be
	public boolean populate();

	// Tells the container that it should start containing the argument presentation element
	public void beginContainment(INodePresentation pPreviousContainer,
								IPresentationElement pPresentationElement );

	// Tells the container that it should start containing the argument presentation elements
	public void beginContainment(INodePresentation pPreviousContainer,
							 	 ETList<IPresentationElement> pPresentationElements );

	// Tells the container that it should stop containing the argument presentation element
	public void endContainment(ETList<IPresentationElement> pPresentationElements );

	// Sets the type of container this guy is.  The long is an OR value of the ContainmentType
	public void setContainmentType(int nContainmentType );
	
	// The type of container this guy is
	public int getContainmentType();

	// Allows you to turn on and off the containment mechanism of this node
	public void setIsGraphicalContainer(boolean bIsGraphicalContainer );
	
	// Allows you to turn on and off the containment mechanism of this node
	public boolean getIsGraphicalContainer();

	// Retrieves the model element from the compartment containing the presentation element
	public IElement getContainingModelElement(IPresentationElement pPresentationElement);
        
        
	public ETList < IPresentationElement > getDeepContained();
		
}


