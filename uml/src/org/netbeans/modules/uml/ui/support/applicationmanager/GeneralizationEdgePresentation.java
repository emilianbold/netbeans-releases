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



package org.netbeans.modules.uml.ui.support.applicationmanager;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationValidator;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext;

/*
 * 
 * @author KevinM
 *
 */
public class GeneralizationEdgePresentation extends EdgePresentation implements IGeneralizationEdgePresentation
{

   /**
    * 
    */
   public GeneralizationEdgePresentation()
   {
      super();
   }

   /**
    * Verify this guy is a generalization.
    */
   public boolean isGeneralization()
   {
      return this.getModelElement() instanceof IGeneralization;
   }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#reconnectLink(org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext)
	 */
	public boolean reconnectLink(IReconnectEdgeContext context)
	{
		if ( context == null )
		{
			return false;
		}

		boolean successfullyReconnected = false;
      
		// Sun issue 6175453:  The original code here allowed circular generalizations,
		// which was the same as the C++ code that was not working properly in C++.
		// However, in Java allowing this connection was causing the application to hang.
		// 
		// So, the real solution is to not allow circular generalizations by asking the
		// relationship validator to validate the generalization before reconnecting the link.
		// This process is similar to what is done when a generalization is created originally.
      
		IElement edgeElement = getModelElement();
		if ( edgeElement instanceof IGeneralization )
		{
			IGeneralization generalization = (IGeneralization)edgeElement;
         
			IETNode proposedNode = context.getProposedEndNode(); // The proposed, new node to take its place
			IETNode anchoredNode = context.getAnchoredNode(); // The node that's not being moved - at the other end of the link
			if( proposedNode != null && anchoredNode != null )
			{
				IElement proposedElement = TypeConversions.getElement( proposedNode );
				IElement anchoredElement = TypeConversions.getElement( anchoredNode );
            
				if (proposedElement instanceof IClassifier)
				{
					IClassifier proposedClassifier = (IClassifier)proposedElement;
               
					// Determine which end of the generalization remains the same
					ETPairT < IElement, IElement > elements = getEdgeFromAndToElement(true);
					if ( elements != null )
					{
						IElement specific = elements.getParamOne();
						if( specific != null )
						{
							// Verify the relation
							class GeneralizationValidator extends RelationValidator
							{
								public boolean isValid( IClassifier classifier, IElement elementToMatch )
								{
									return !matchesSuper( classifier, elementToMatch );
								}
							}
                     
							GeneralizationValidator validator = new GeneralizationValidator();
                     
							boolean anchoredIsSpecific = specific.isSame( anchoredElement );
							if( anchoredIsSpecific )
							{
								if( validator.isValid( proposedClassifier, anchoredElement ))
								{
									generalization.setGeneral( proposedClassifier );
									// Verify that the change took place
									successfullyReconnected = proposedClassifier.isSame( generalization.getGeneral() );
								}
							}
							else
							{
								if( validator.isValid( (IClassifier)anchoredElement, proposedClassifier ))
								{
									generalization.setSpecific( proposedClassifier );
									// Verify that the change took place
									successfullyReconnected = proposedClassifier.isSame( generalization.getSpecific() );
								}
							}
						}
					}
				}
			}
		}

		return successfullyReconnected;
	}
	
	/* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#reconnectLinkToValidNodes()
    */
   public boolean reconnectLinkToValidNodes()
   {
      return isGeneralization() ? reconnectSimpleLinkToValidNodes() : false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#validateLinkEnds()
    */
   public boolean validateLinkEnds()
   {
      if (super.validateLinkEnds() && isGeneralization())
      {
         return validateSimpleLinkEnds();
      }
      else
         return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement#transform(java.lang.String)
    */
   public IPresentationElement transform(String elemName)
	{
		try
		{
			//	Call our base class which clears out the cached model element
			IPresentationElement pe = super.transform(elemName);
			IETGraphObject pETElement = this.getETGraphObject();
			if (pETElement != null)
			{
				pe = pETElement.transform(elemName);
			}

			return pe;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

}
