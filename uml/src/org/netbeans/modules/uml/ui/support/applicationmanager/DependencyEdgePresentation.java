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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IDelegate;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPort;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext;

/*
 * 
 * @author KevinM
 *
 */
public class DependencyEdgePresentation extends EdgePresentation implements IDependencyEdgePresentation
{

   /**
    * 
    */
   public DependencyEdgePresentation()
   {
      super();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#reconnectLink(org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext)
    */
   public boolean reconnectLink(IReconnectEdgeContext pContext)
   {
      if (pContext == null)
         return false;

      boolean bSuccessfullyReconnected = false;
      try
      {
         IETNode pOldNode = pContext.getPreConnectNode(); // The node that is being disconnected
         IETNode pNewNode = pContext.getProposedEndNode(); // The proposed, new node to take its place
         IETNode pAnchoredNode = pContext.getAnchoredNode(); // The node that's not being moved - at the other end of the link

         // Allow the target draw engine to determine if connectors should be created
         setReconnectConnectorFlag(pContext);

         if (pOldNode != null && pNewNode != null && pAnchoredNode != null && this.isDependency())
         {
            IElement pEdgeElement = getModelElement();
            IElement pFromNodeElement = TypeConversions.getElement(pOldNode);
            IElement pToNodeElement = TypeConversions.getElement(pNewNode);

            // A delegate must have at least one end being a port
            if (isDelegate())
            {
               if (!(pAnchoredNode instanceof IPort || pToNodeElement instanceof IPort))
               {
                  // The nodes are not valid.
                  pFromNodeElement = null;
                  pToNodeElement = null;
               }
            }

            if (pEdgeElement != null && pFromNodeElement != null && pToNodeElement != null)
            {
               INamedElement pToNodeNamedElement = pToNodeElement instanceof INamedElement ? (INamedElement) pToNodeElement : null;
               IDependency pDependency = pEdgeElement instanceof IDependency ? (IDependency) pEdgeElement : null;

               if (pToNodeNamedElement != null && pDependency != null)
               {
                  INamedElement pClient = pDependency.getClient();
                  INamedElement pSupplier = pDependency.getSupplier();

                  if (pClient != null && pSupplier != null)
                  {
                     boolean bFromNodeClient = pClient.isSame(pFromNodeElement);
                     boolean bFromNodeSupplier = pSupplier.isSame(pFromNodeElement);

                     // See if the node we're disconnecting is the implementing classifier or Supplier

                     if (bFromNodeClient)
                     {
                        pDependency.setClient(pToNodeNamedElement);

                        // Verify the change took place								
                        bSuccessfullyReconnected = pToNodeNamedElement.isSame(pDependency.getClient());
                     }
                     if (bFromNodeSupplier)
                     {
                        pDependency.setSupplier(pToNodeNamedElement);

                        // Verify the change took place								
                        bSuccessfullyReconnected = pToNodeNamedElement.isSame(pDependency.getSupplier());
                     }
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         bSuccessfullyReconnected = false;
      }

      return bSuccessfullyReconnected;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#reconnectLinkToValidNodes()
    */
   public boolean reconnectLinkToValidNodes()
   {
      return isDependency() && reconnectSimpleLinkToValidNodes();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#validateLinkEnds()
    */
   public boolean validateLinkEnds()
   {
      return super.validateLinkEnds() && isDependency() && validateSimpleLinkEnds();
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

   /**
    * Verify this guy is a Dependency, but not an implementation
    *
    * @returns TRUE if the element we represent is a dependency, but not an implementation.  Implementation
    * has its own presentation element.
    */
   protected boolean isDependency()
   {
      IElement pEdgeElement = TypeConversions.getElement(getETGraphObject());
      return pEdgeElement instanceof IDependency && !(pEdgeElement instanceof IImplementation);
   }
   
	/**
	 * Is this guy a delegate
	 *
	 * @param TRUE if the element we represent is a delegate
	 */
	protected boolean isDelegate()
	{
		return TypeConversions.getElement(this.getETGraphObject()) instanceof IDelegate;
	}
}
