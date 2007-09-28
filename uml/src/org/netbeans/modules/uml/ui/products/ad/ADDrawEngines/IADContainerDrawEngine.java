/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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


