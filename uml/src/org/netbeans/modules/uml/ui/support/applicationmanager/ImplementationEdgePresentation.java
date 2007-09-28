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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Implementation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext;

/*
 * 
 * @author KevinM
 *
 */
public class ImplementationEdgePresentation extends EdgePresentation implements IImplementationEdgePresentation
{

   public boolean isImplementation()
   {
      return this.getModelElement() instanceof IImplementation;
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
         // Allow the target draw engine to determine if connectors should be created
         setReconnectConnectorFlag(pContext);

         if (pOldNode != null && pNewNode != null && isImplementation())
         {
            IElement pEdgeElement = getModelElement();
            IElement pFromNodeElement = TypeConversions.getElement(pOldNode);
            IElement pToNodeElement = TypeConversions.getElement(pNewNode);

            if (pEdgeElement != null && pFromNodeElement != null && pToNodeElement != null)
            {
               IClassifier pToNodeClassifier = pToNodeElement instanceof IClassifier ? (IClassifier) pToNodeElement : null;
               Implementation pImplementation = pEdgeElement instanceof Implementation ? (Implementation) pEdgeElement : null;
               if (pToNodeClassifier != null && pImplementation != null)
               {
                  IClassifier pImplementingClassifier = pImplementation.getImplementingClassifier();
                  IInterface pContract = pImplementation.getContract();
                  boolean bFromNodeImplementingClassifier = false;
                  boolean bFromNodeContract = false;

                  // See if the node we're disconnecting is the implementing classifier or contract
                  bFromNodeImplementingClassifier = pImplementingClassifier.isSame(pFromNodeElement);
                  bFromNodeContract = pContract.isSame(pFromNodeElement);

                  if (bFromNodeImplementingClassifier)
                  {
                     pImplementation.setImplementingClassifier(pToNodeClassifier);

                     // Verify that the change took place
                     IClassifier pCurrentElement = pImplementation.getImplementingClassifier();
                     bSuccessfullyReconnected = pToNodeClassifier.isSame(pCurrentElement);
                  }
                  if (bFromNodeContract)
                  {
                     IInterface pNewContract = pToNodeClassifier instanceof IInterface ? (IInterface) pToNodeClassifier : null;

                     if (pNewContract != null)
                     {

                        pImplementation.setContract(pNewContract);

                        // Verify that the change took place
                        IInterface pCurrentElement = pImplementation.getContract();
                        bSuccessfullyReconnected = pToNodeClassifier.isSame(pCurrentElement);
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
      return isImplementation() && reconnectSimpleLinkToValidNodes();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#validateLinkEnds()
    */
   public boolean validateLinkEnds()
   {
      try
      {
         // Call the baseclass first.  It removes the cached model element to verify that the
         // ME and PE are still hooked up
         return super.validateLinkEnds() && isImplementation() && validateSimpleLinkEnds();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
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
