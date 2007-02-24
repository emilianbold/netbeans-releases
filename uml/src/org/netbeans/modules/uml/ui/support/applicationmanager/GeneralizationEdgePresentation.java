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
