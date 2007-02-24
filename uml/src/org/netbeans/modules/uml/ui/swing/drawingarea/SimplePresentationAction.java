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



package org.netbeans.modules.uml.ui.swing.drawingarea;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.ICoreRelationshipDiscovery;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.drawingarea.DelayedAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.DiagramAreaEnumerations;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar;

import java.util.Iterator;

import javax.swing.SwingUtilities;

/**
 * @author KevinM
 *
 */
public class SimplePresentationAction extends DelayedAction implements ISimplePresentationAction
{

   protected ETList < IPresentationElement > m_PresentationElements = new ETArrayList < IPresentationElement > ();
   protected ETList < IPresentationElement > m_SecondaryPresentationElements = null;
   protected int m_Kind = -1; // Unknown.
   protected Object m_data = null;
   protected IETRect m_InvalidateBeforeRect;

   /**
    * 
    */
   public SimplePresentationAction()
   {
      super();
   }

   public SimplePresentationAction(int DiagramAreaEnumerationsKind)
   {
      super();
      this.setKind(DiagramAreaEnumerationsKind);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.ISimplePresentationAction#add(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
    */
   public void add(IPresentationElement pVal)
   {
      m_PresentationElements.addIfNotInList(pVal);

      if (getKind() == DiagramAreaEnumerations.SPAK_INVALIDATE)
      {
         setPreInvalidateRect();
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.ISimplePresentationAction#getPresentationElements()
    */
   public ETList < IPresentationElement > getPresentationElements()
   {
      return m_PresentationElements;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.ISimplePresentationAction#setPresentationElements()
    */
   public void setPresentationElements(ETList < IPresentationElement > pVal)
   {
      m_PresentationElements = pVal;

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.ISimplePresentationAction#setSecondaryPresentationElements()
    */
   public void setSecondaryPresentationElements(ETList < IPresentationElement > pVal)
   {
      m_SecondaryPresentationElements = pVal;
   }

   /*
    * 
    */
   public ETList < IPresentationElement > getSecondaryPresentationElements()
   {
      return m_SecondaryPresentationElements;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.ISimplePresentationAction#getKind()
    */
   public int getKind()
   {
      return m_Kind;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.ISimplePresentationAction#setKind(int)
    */
   public void setKind(int simplePresentationActionKind)
   {
      m_Kind = simplePresentationActionKind;
      if (getKind() == DiagramAreaEnumerations.SPAK_INVALIDATE)
      {
         setPreInvalidateRect();
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.ISimplePresentationAction#getData()
    */
   public Object getData()
   {
      return m_data;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.ISimplePresentationAction#setData(java.lang.Object)
    */
   public void setData(Object newVal)
   {
      m_data = newVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDelayedAction#getDescription()
    */
   public String getDescription()
   {
      try
      {
         String message = new String("SimplePresentationAction : ");

         switch (getKind())
         {
            case DiagramAreaEnumerations.SPAK_SIZETOCONTENTS :
               message += "SPAK_SIZETOCONTENTS";
               break;
            case DiagramAreaEnumerations.SPAK_DISCARDALLBENDS :
               message += "SPAK_DISCARDALLBENDS";
               break;
            case DiagramAreaEnumerations.SPAK_ADDTOTRACKBAR :
               message += "SPAK_ADDTOTRACKBAR";
               break;
            case DiagramAreaEnumerations.SPAK_UPDATE_TRACKBAR :
               message += "SPAK_UPDATE_TRACKBAR";
               break;
            case DiagramAreaEnumerations.SPAK_LIFELINE_MAKECREATEHORIZONTAL :
               message += "SPAK_LIFELINE_MAKECREATEHORIZONTAL";
               break;
            case DiagramAreaEnumerations.SPAK_REPOSITIONTOCONTENTS :
               message += "SPAK_REPOSITIONTOCONTENTS";
               break;
            case DiagramAreaEnumerations.SPAK_INVALIDATE :
               message += "SPAK_INVALIDATE";
               break;
            case DiagramAreaEnumerations.SPAK_EDITLABEL :
               message += "SPAK_EDITLABEL";
               break;
            case DiagramAreaEnumerations.SPAK_VALIDATENODE :
               message += "SPAK_VALIDATENODE";
               break;
            case DiagramAreaEnumerations.SPAK_MOVEBEHINDCONTAINED :
               message += "SPAK_MOVEBEHINDCONTAINED";
               break;
            case DiagramAreaEnumerations.SPAK_DELETEANDREINITIALIZEALLLABELS :
               message += "SPAK_DELETEANDREINITIALIZEALLLABELS";
               break;
            case DiagramAreaEnumerations.SPAK_RESIZETOCONTAIN :
               message += "SPAK_RESIZETOCONTAIN";
               break;
            case DiagramAreaEnumerations.SPAK_SELECT :
               message += "SPAK_SELECT";
               break;
            case DiagramAreaEnumerations.SPAK_DESELECT :
               message += "SPAK_DESELECT";
               break;
            case DiagramAreaEnumerations.SPAK_DISCOVER_RELATIONSHIPS :
               message += "SPAK_DISCOVER_RELATIONSHIPS";
               break;
            case DiagramAreaEnumerations.SPAK_RELAYOUTALLLABELS :
               message += "SPAK_RELAYOUTALLLABELS";
               break;

            default :
               message += "Unknown";
               break;
         }

         /* CString temp;
         	temp.Format("%x", (long)(m_PresentationElements.p));
         	message += ", m_PresentationElements = ";
         	message += temp;
         	*/

         return message;
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return "";
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.drawingarea.IExecutableAction#execute(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl)
    */
   public void execute(final IDrawingAreaControl pControl)
   {
      try
      {
         Object pDispTrackBar = pControl != null ? pControl.getTrackBar() : null;
         long count = m_PresentationElements != null ? m_PresentationElements.getCount() : 0;
         boolean bDoRefresh = false;

         ITrackBar pTrackBar = pDispTrackBar instanceof ITrackBar ? (ITrackBar)pDispTrackBar : null;

         // Only allow editing of the first label
         boolean didEdit = false;
         for (int i = 0; pControl != null && i < count; i++)
         {
            final IPresentationElement pThisPE = m_PresentationElements.item(i);

            if (pThisPE == null)
            {
               continue;
            }

            switch (getKind())
            {
               case DiagramAreaEnumerations.SPAK_SIZETOCONTENTS :
                  {
                     if (pControl != null)
                     {
                        pControl.sizeToContentsWithTrackBar(pThisPE);
                     }
                  }
                  break;
               case DiagramAreaEnumerations.SPAK_DISCARDALLBENDS :
                  {
                     if (pThisPE instanceof IEdgePresentation)
                     {
                        ((IEdgePresentation)pThisPE).discardAllBends();
                        bDoRefresh = true;
                     }
                  }
                  break;
               case DiagramAreaEnumerations.SPAK_ADDTOTRACKBAR :
                  {
                     if (pThisPE instanceof INodePresentation)
                     {
                        // Fix J1794:  Adding the track car needs to be delayed
                        //             because the compartments have not been created
                        //             by the time this is normally called when the
                        //             user is placing a lifeline on the SQD.
                        SwingUtilities.invokeLater(new Runnable()
                        {
                           public void run()
                           {
                              // Make sure the TS node is sized properly
                               ((INodePresentation)pThisPE).sizeToContents();

                              // Add the car to the track bar
                              pControl.addPresentationElementToTrackBar(pThisPE);
                           }
                        });
                     }
                  }
                  break;
               case DiagramAreaEnumerations.SPAK_UPDATE_TRACKBAR :
                  {
                     if (pTrackBar != null)
                     {
                        // If the update is for a label it has to be handled differently
                        if (pThisPE instanceof ILabelPresentation)
                        {
                           pTrackBar.expandAssociatedCoupling((ILabelPresentation)pThisPE);
                        }
                        else if (pControl != null)
                        {
                           // Tell the track about the resize
                           boolean bObjectResized = false;
                           pTrackBar.preResize(pThisPE);
                           bObjectResized = pTrackBar.resize(pThisPE);

                           // Also, tell the track bar about the name change
                           pTrackBar.updateName(pThisPE);
                        }
                     }

                  }
                  break;
               case DiagramAreaEnumerations.SPAK_INVALIDATE :
                  {
                     doInvalidate(pControl, pThisPE);
                  }
                  break;
               case DiagramAreaEnumerations.SPAK_EDITLABEL :
                  {
                     if (!didEdit)
                     {
                        if (pThisPE instanceof ILabelPresentation && pControl != null)
                        {
                           //pControl.pumpMessages();
                            ((ILabelPresentation)pThisPE).beginEdit();
                           didEdit = true;
                        }
                     }
                  }
                  break;
               case DiagramAreaEnumerations.SPAK_VALIDATENODE :
                  {
                     IDrawEngine cpEngine = TypeConversions.getDrawEngine(pThisPE);
                     if (cpEngine != null)
                     {
                        boolean bValid = cpEngine.validateNode();
                     }
                  }
                  break;
               case DiagramAreaEnumerations.SPAK_MOVEBEHINDCONTAINED :
               case DiagramAreaEnumerations.SPAK_LIFELINE_MAKECREATEHORIZONTAL :
               case DiagramAreaEnumerations.SPAK_REPOSITIONTOCONTENTS :
                  // Don't do anything.  The diagram engines should handle these.
                  break;
               case DiagramAreaEnumerations.SPAK_DELETEANDREINITIALIZEALLLABELS :
                  {
                     // Now get the draw engine's label manager and discard labels, then
                     // recreate.
                     IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(pThisPE);
                     if (pThisPE instanceof IProductGraphPresentation && pDrawEngine != null)
                     {
                        ILabelManager pLabelManager = ((IProductGraphPresentation)pThisPE).getLabelManager();

                        if (pLabelManager != null)
                        {
                           pLabelManager.discardAllLabels();
                           pLabelManager.createInitialLabels();
                           pLabelManager = null;
                        }
                     }
                  }
                  break;
               case DiagramAreaEnumerations.SPAK_RESIZETOCONTAIN :
                  {
                     // TODO: Impl DiagramAreaEnumerations.SPAK_RESIZETOCONTAIN.
                     /*             if( VT_DISPATCH == m_varData.vt )
                     			   {
                     				  xxx instanceof INodePresentation > cpOtherPE( m_varData.punkVal );
                     				  if( cpOtherPE )
                     				  {
                     					 xxx instanceof INodePresentation > cpThisNode(pThisPE);
                     					 if( cpThisNode )
                     					 {
                     						 cpThisNode.resizeToContain( cpOtherPE );
                     					 }
                     				  }
                     			   }
                     */
                  }
                  break;
                  /*					case DiagramAreaEnumerations.SPAK_SELECT :
                  					case DiagramAreaEnumerations.SPAK_DESELECT :
                  						{
                  							if (pThisPE instanceof IGraphPresentation) {
                  								((IGraphPresentation) pThisPE).setSelected(getKind() == DiagramAreaEnumerations.SPAK_SELECT ? true : false);
                  							}
                  						}
                  						break;
                  */
               case DiagramAreaEnumerations.SPAK_DISCOVER_RELATIONSHIPS :
                  {
                     if (i == 0)
                     {
                        // Perform relationship discovery on all the elements
                        ICoreRelationshipDiscovery cpRelationshipDiscovery = pControl.getRelationshipDiscovery();

                        if (cpRelationshipDiscovery != null)
                        {
                           ETList < IElement > pAllFirstSubjects = getAllFirstSubjects(m_PresentationElements);
                           ETList < IElement > pAllSecondaryPEFirstSubjects = getAllFirstSubjects(m_SecondaryPresentationElements);

                           if (pAllFirstSubjects != null && pAllSecondaryPEFirstSubjects != null)
                           {
                              ETList < IPresentationElement > pPresentationElements = cpRelationshipDiscovery.discoverCommonRelations(true, pAllFirstSubjects, pAllSecondaryPEFirstSubjects);

                           }
                           else if (pAllFirstSubjects != null)
                           {
                              ETList < IPresentationElement > pPresentationElements = cpRelationshipDiscovery.discoverCommonRelations(true, pAllFirstSubjects);

                           }
                        }
                     }
                  }
                  break;
               case DiagramAreaEnumerations.SPAK_RELAYOUTALLLABELS :
                  {
                     // Now get the draw engine's label manager and discard labels, then
                     // recreate.
                     IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(pThisPE);
                     if (pThisPE instanceof IProductGraphPresentation && pDrawEngine != null)
                     {
                        IProductGraphPresentation pGraphPE = (IProductGraphPresentation)pThisPE;
                        ILabelManager pLabelManager = pGraphPE.getLabelManager();
                        if (pLabelManager != null)
                        {
                           pLabelManager.relayoutLabels();
                           pLabelManager = null;
                        }
                     }
                  }
                  break;

               default :
                  //assert("Unknown Type");
                  break;
            }
         }

         if (pControl != null)
         {
            if (bDoRefresh)
            {
               pControl.refresh(true);
            }

            if (count > 0)
            {
               pControl.setIsDirty(true);
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /*
    * 
    */
   protected void setPreInvalidateRect()
   {

      try
      {
         if (getPresentationElements() != null && !getPresentationElements().isEmpty())
         {
            m_InvalidateBeforeRect = null;
            IPresentationElement pFirstPE = getPresentationElements().item(0);

            if (pFirstPE instanceof IProductGraphPresentation)
            {
               m_InvalidateBeforeRect = ((IProductGraphPresentation)pFirstPE).getBoundingRect();
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    Invalidates this presentation element
    *
    @param pControl [in] The current drawing area
    @param pElement [in] The guy to do the invalidation on
    */
   protected void doInvalidate(IDrawingAreaControl pControl, IPresentationElement pElement)
   {
      try
      {
         // We have a before rectangle we keep.  Union it with the current
         // retangle to get the new invalidate rect.
         if (pElement instanceof IProductGraphPresentation)
         {
            IProductGraphPresentation pGraphPresentation = (IProductGraphPresentation)pElement;
            if (m_InvalidateBeforeRect == null || pControl == null)
            {
               pGraphPresentation.invalidate();
            }
            else
            {
               IETRect pCurrentRect = pGraphPresentation.getBoundingRect();
               if (pCurrentRect != null && m_InvalidateBeforeRect != null && pControl != null)
               {
                  pCurrentRect.unionWith(m_InvalidateBeforeRect);

                  // RefreshRect is expecting the y-axis to be inverted.
                  pCurrentRect.normalizeRect();

                  pControl.refreshRect(pCurrentRect);
               }
               else
               {
                  pGraphPresentation.invalidate();
               }
            }
         }

      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
   * Builds a list of first subject elements.
   **/
   public static ETList < IElement > getAllFirstSubjects(ETList < IPresentationElement > pPES)
   {
      if (pPES == null || pPES.isEmpty())
         return null;

      ETList < IElement > firstSubjects = new ETArrayList < IElement > ();
      Iterator < IPresentationElement > iter = pPES.iterator();
      while (iter.hasNext())
      {
         IElement pElement = iter.next().getFirstSubject();
         if (pElement != null)
            firstSubjects.add(pElement);
      }
      return firstSubjects;
   }
}
