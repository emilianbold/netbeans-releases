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
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.IComment;

/**
 * @author KevinM
 *
 * CommentEdgePresentation implements the ICommentEdgePresentation Comments.  It is
 * responsible for validating, reconnecting and transforming the link as necessary.
 */
public class CommentEdgePresentation extends EdgePresentation implements ICommentEdgePresentation
{

   /**
    * 
    */
   public CommentEdgePresentation()
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
         IETNode pOldNode; // The node that is being disconnected
         IETNode pNewNode; // The proposed, new node to take its place
         IETNode pAnchoredNode; // The node that's not being moved - at the other end of the link

         pOldNode = pContext.getPreConnectNode();
         pNewNode = pContext.getProposedEndNode();
         pAnchoredNode = pContext.getAnchoredNode();

         // Allow the target draw engine to determine if connectors should be created
         setReconnectConnectorFlag(pContext);
         if (pOldNode != null && pNewNode != null && pAnchoredNode != null && isComment())
         {
            // Get the elements for this PE's model element and the model element of
            // the from and to nodes.
            IElement pEdgeElement = getModelElement();
            IElement pFromNodeElement = TypeConversions.getElement(pOldNode);
            IElement pToNodeElement = TypeConversions.getElement(pNewNode);
            IElement pAnchoredNodeElement = TypeConversions.getElement(pAnchoredNode);

            // This element should be a comment
            IComment pComment = pEdgeElement instanceof IComment ? (IComment) pEdgeElement : null;

            INamedElement pNamedFromNodeElement = pFromNodeElement instanceof INamedElement ? (INamedElement) pFromNodeElement : null;
            INamedElement pNamedToNodeElement = pToNodeElement instanceof INamedElement ? (INamedElement) pToNodeElement : null;
            INamedElement pNamedAnchoredNodeElement = pAnchoredNodeElement instanceof INamedElement ? (INamedElement) pAnchoredNodeElement : null;
            if (pComment != null && pNamedFromNodeElement != null && pNamedToNodeElement != null)
            {
               // See if we are moving the end closest to the comment, moving the link
               // from one comment to another.
               boolean bCommentEndIsFromNode = pComment.isSame(pFromNodeElement);
               if (bCommentEndIsFromNode)
               {
                  IComment pNewComment = pToNodeElement instanceof IComment ? (IComment) pToNodeElement : null;

                  if (pNewComment != null)
                  {
                     // The Bridge link is connected to the IComment.  Change that.
                     IPresentationElement pETElement = getElement() instanceof IPresentationElement ? (IPresentationElement) getElement() : null;
                     if (pETElement != null && pNamedAnchoredNodeElement != null)
                     {
                        // Get the other end of the node
                        pComment.removeAnnotatedElement(pNamedAnchoredNodeElement);
                        pNewComment.addAnnotatedElement(pNamedAnchoredNodeElement);
                        setModelElement(pNewComment);

                        // Verify the change took place
                        bSuccessfullyReconnected = pNewComment.getIsAnnotatedElement(pNamedAnchoredNodeElement);
                     }
                  }
               }
               else
               {
                  // We're moving the annotated end.
                  pComment.removeAnnotatedElement(pNamedFromNodeElement);
                  pComment.addAnnotatedElement(pNamedToNodeElement);

                  // Verify the change took place
                  bSuccessfullyReconnected = pComment.getIsAnnotatedElement(pNamedToNodeElement);
               }
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return bSuccessfullyReconnected;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#reconnectLinkToValidNodes()
    */
   public boolean reconnectLinkToValidNodes()
   {
      boolean bSuccessfullyReconnected = false;

      try
      {
         if (isComment())
            bSuccessfullyReconnected = reconnectSimpleLinkToValidNodes();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      // Always return ok, if this routine throws then bSuccessfullyReconnected will be
      // false - that's our error condition
      return bSuccessfullyReconnected;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#validateLinkEnds()
    */
   public boolean validateLinkEnds()
   {
      boolean bIsValid = false;

      try
      {

         if (isComment())
         {
            IElement pEdgeElement;
            IElement pSourceNodeElement;
            IElement pTargetNodeElement;

            // Get the elements for this PE's model element and the model element of
            // the Source and to nodes.
            pEdgeElement = getModelElement();
            // Get the from and to node IElements
            ETPairT < IElement, IElement > pFromAndTo = getEdgeFromAndToElement(false);
            if (pFromAndTo != null)
            {
               pSourceNodeElement = pFromAndTo.getParamOne();
               pTargetNodeElement = pFromAndTo.getParamTwo();
            }
            else
            {
               pSourceNodeElement = null;
               pTargetNodeElement = null;
            }

            // One of the nodes should be the comment, the other an annotated comment.  The
            // edge should have the same model element as the comment.
            if (pEdgeElement != null && pSourceNodeElement != null && pTargetNodeElement != null)
            {
               // Get the comment
               IComment pComment = pSourceNodeElement instanceof IComment ? (IComment) pSourceNodeElement : null;
               INamedElement pAnnotatedElement;
               if (pComment == null)
               {
                  pComment = pTargetNodeElement instanceof IComment ? (IComment) pTargetNodeElement : null;
                  pAnnotatedElement = pSourceNodeElement instanceof INamedElement ? (INamedElement) pSourceNodeElement : null;
               }
               else
               {
                  pAnnotatedElement = pTargetNodeElement instanceof INamedElement ? (INamedElement) pTargetNodeElement : null;
               }

               if (pComment != null && pEdgeElement.isSame(pComment))
               {
                  // Make sure the other end is in the annotated list
                  if (pAnnotatedElement != null && pComment.getIsAnnotatedElement(pAnnotatedElement))
                  {
                     bIsValid = true;
                  }
               }
               else if((pAnnotatedElement instanceof IComment) && 
                       (pComment instanceof INamedElement))
               {
                   IComment temp = (IComment)pAnnotatedElement;
                   pAnnotatedElement = (INamedElement)pComment;
                   pComment = temp;
                   
                   if (pComment != null && pEdgeElement.isSame(pComment)) {
                       // Make sure the other end is in the annotated list
                       if (pAnnotatedElement != null && pComment.getIsAnnotatedElement(pAnnotatedElement)) {
                           bIsValid = true;
                       }
                   }
               }
            }
         }
         else
            bIsValid = super.validateLinkEnds();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      // Always return ok, if this routine throws then bIsValid will be 
      // false - that's our error condition
      return bIsValid;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement#transform(java.lang.String)
    */
   public IPresentationElement transform(String elemName)
   {
      if (elemName == null)
         return null;

      IPresentationElement newForm = null;

      try
      {
         IETGraphObject pETElement = this.getETGraphObject();
         if (pETElement != null)
            newForm = pETElement.transform(elemName);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      return newForm;
   }

   /// Verify this guy is a comment
   protected boolean isComment()
   {
      return getModelElement() instanceof IComment;
   }
}
