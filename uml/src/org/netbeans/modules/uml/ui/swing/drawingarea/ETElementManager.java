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

import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.drawingarea.DiagramAreaEnumerations;
import org.netbeans.modules.uml.ui.controls.drawingarea.ModelElementSetter;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETEdge;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNode;
import org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr;
import org.netbeans.modules.uml.ui.support.helpers.GUIBlocker;
import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker;
import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker.GBK;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import com.tomsawyer.drawing.TSLabel;
import com.tomsawyer.editor.TSEGraph;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.graph.TSEdge;
import com.tomsawyer.graph.TSGraphObject;
import com.tomsawyer.graph.TSNode;
import com.tomsawyer.util.TSObject;

/**
 * @author sumitabhk
 *
 */
public class ETElementManager implements IGraphEventKind
{
   private IDrawingAreaControl m_Parent;

   /**
    * 
    */
   public ETElementManager(IDrawingAreaControl control)
   {
      super();
      m_Parent = control;
   }

   /**
    * Notify the IETGraphObjects that a delete gather is about to happen
    */
   public void onPreDeleteGatherSelected()
   {
      notifyETObjectsOfEvent(GEK_PRE_DELETEGATHERSELECTED);
   }

   /**
    * Fires the predelete to the IETGraphObjects
    */
   public void onPreDelete()
   {
      notifyETObjectsOfEvent(GEK_PRE_DELETE);
   }

   /**
    * Notify the IETGraphObjects that a delete was canceled.
    */
   public void onDeleteCancelled()
   {
      notifyETObjectsOfEvent(GEK_DELETECANCELED);
   }

   /**
    * Fires the preresize to the IETGraphObjects
    */
   public void onPreResize()
   {
      notifyETObjectsOfEvent(GEK_PRE_RESIZE);
   }
   /**
    * Fires the postresize to the IETGraphObjects
    */
   public void onPostResize()
   {
      notifyETObjectsOfEvent(GEK_POST_RESIZE);
   }

   /** 
    * Notifies the IETGraphObjects objects that an event occured.
    * 
    * @param kind On of the values in the IGraphEventKind interface.
    * @see IGraphEventKind
    */
   protected void notifyETObjectsOfEvent(int kind)
   {
      IDrawingAreaControl ctrl = getParent();
      if (ctrl != null)
      {
         dispatchToETGraphObjects(ctrl.getSelected(), kind);
      }
   }

   /**
    *
    * Called after an object has been added to the graph
    *
    * @param graphObject [in] The object added.
    * @param bResize [in] VARIANT_TRUE to resize the new element to its contents
    * @param handled [in] Did we handle this event?
    *
    */
   public void handlePostAddObject(TSGraphObject graphObj, boolean resize)
   {
      IETGraphObject pETElement = TypeConversions.getETGraphObject(graphObj);
      IPresentationTypesMgr presMgr = m_Parent.getPresentationTypesMgr();

      if (pETElement != null)
      {
         // Get some details off our parent

         // Get the engine controlling where the element should be created (what namespace)
         IDiagramEngine diaEng = m_Parent.getDiagramEngine();

         // 	Get the namespace from the parent
         INamespace pNamespace = diaEng != null ? diaEng.getNamespaceForCreatedElements() : null;

         // Get the type of the diagram
         int diaKind = m_Parent.getDiagramKind();

         // Get the model element in case we need to attach
         IElement modEle = m_Parent.getModelElement();

         // Get the correct init string.  Labels have no initstring right now.
         String initStr = pETElement.getETUI().getInitStringValue();

         // Set the drawing area backpointer
         IDiagram pDia = m_Parent.getDiagram();
         pETElement.setDiagram(pDia);
         IElement createdModEle = null;

         // Construct or attach to an IElement
         if (modEle != null)
         {
            pETElement.attach(modEle, initStr);
         }
         else
         {
            createdModEle = pETElement.create(pNamespace, initStr);
         }

         // Now verify that this graph object as created ok.  To do that get the model element
         // and presentation element.  If they exist then assume everything is ok, otherwise
         // post an event to remove the graph object
         IPresentationElement pPE = pETElement.getPresentationElement();
         boolean isValid = false;
         if (pPE != null)
         {
            if (pPE.getFirstSubject() != null)
            {
               isValid = true;
            }
            else if (createdModEle != null)
            {
               if (!createdModEle.isDeleted())
               {
                  // At this point we could have an element that was unloaded.  When unloaded
                  // the model element is moved to another xml file and the presentation element
                  // connections are lost.  Normally we get an event and eventually call 
                  // AxDrawingAreaControl::ReestablishPresentationElementOwnership to fix this, but
                  // we need to do this now, we can't wait for the event queue.
                  pPE.removeSubject(createdModEle);
                  pPE.addSubject(createdModEle);
                  isValid = true;
               }
            }
         }

         // Verify that the draw engine is allowed on this diagram
         if (presMgr != null)
         {
            IDrawEngine pEng = TypeConversions.getDrawEngine(pETElement);
            if (pEng != null)
            {
               if (!presMgr.isValidDrawEngine(diaKind, pEng.getDrawEngineID()))
               {
                  isValid = false;
               }
            }
         }

         if (isValid)
         {
            // Update the track bar with the new object if it's a node that represents a lifeline.
            // This update must be performed after the TS node is completely layed out.
            // So, we put the call on the delayed action stack.
               int kind =
                  ((pETElement != null && pETElement.isNode()) && (IDiagramKind.DK_SEQUENCE_DIAGRAM == diaKind))
                     ? DiagramAreaEnumerations.SPAK_ADDTOTRACKBAR // Sizes the node to its content, and adds the car to the track bar.
   : DiagramAreaEnumerations.SPAK_SIZETOCONTENTS; // Sizes the node to its content.

            // resize if told to do so or if SQD always resize
            if (resize || (diaKind == IDiagramKind.DK_SEQUENCE_DIAGRAM))
            {
               // Size the node view to the contents.
               // Do it as a delayed action because TS will crash in certain callstacks.
               IPresentationElement presEle = pETElement.getPresentationElement();
               if (presEle != null)
               {
                  m_Parent.postSimplePresentationDelayedAction(presEle, kind);
               }
            }

            if (pETElement.isEdge())
            {
               TSEdge pEdge = (TSEdge)pETElement;
               TSNode fromNode = pEdge.getSourceNode();
               TSNode toNode = pEdge.getTargetNode();

               // Tell both nodes that a new edge has been added.
               if (fromNode != null && toNode != null)
               {
                  IETGraphObject fromObj = TypeConversions.getETGraphObject(fromNode);
                  IETGraphObject toObj = TypeConversions.getETGraphObject(toNode);

                  if (fromObj != null && toObj != null)
                  {
                     // Notify these nodes that a new link has been attached.
                     fromObj.onPostAddLink(pETElement, true);
                     toObj.onPostAddLink(pETElement, false);
                  }
               }
            }

            // Tell the product element that we're done and it can initialize
            pETElement.onGraphEvent(IGraphEventKind.GEK_POST_CREATE);
         }
         else
         {
            IPresentationElement presEle = pETElement.getPresentationElement();
            if (presEle != null)
            {
               m_Parent.postDeletePresentationElement(presEle);
            }
            else
            {
               m_Parent.postDeletePresentationElement(graphObj);
            }
         }

         // Make sure the scroll bars are updated
         TSEGraph editor = m_Parent.getCurrentGraph();
         if (editor != null)
         {
            editor.updateBounds();
         }
      }
   }

   //**************************************************
   // Helper Methods
   //**************************************************

   /**
    * Retrieves the drawing area control that initialized the manager.
    */
   protected IDrawingAreaControl getParent()
   {
      return m_Parent;
   }

   protected void dispatchToETGraphObjects(ETList < IPresentationElement > elements, int kind)
   {
      if (elements != null)
      {
         for (Iterator < IPresentationElement > iter = elements.iterator(); iter.hasNext();)
         {
            dispatchToETGraphObject(iter.next(), kind);
         }
      }
   }

   protected void dispatchToETGraphObject(IPresentationElement element, int kind)
   {
      IETGraphObject obj = TypeConversions.getETGraphObject(element);
      if (obj != null)
      {
         obj.onGraphEvent(kind);
      }
   }

   public boolean onKeyDown(int nKeyCode, int nShift)
   {
      boolean handled = false;
      ETList < IETGraphObject > selectedObjs = m_Parent.getSelected3();
      if (selectedObjs != null)
      {
         int count = selectedObjs.size();
         // Only pass the keydown if one product element is selected.
         if (count == 1)
         {
            IETGraphObject pObject = selectedObjs.get(0);
            handled = pObject.onKeydown(nKeyCode, nShift);
         }
      }
      return handled;
   }

   public boolean onCharTyped(char ch)
   {
      boolean handled = false;
      ETList < IETGraphObject > selectedObjs = m_Parent.getSelected3();
      if (selectedObjs != null)
      {
         int count = selectedObjs.size();
         // Only pass the keydown if one product element is selected.
         if (count == 1)
         {
            IETGraphObject pObject = selectedObjs.get(0);
            handled = pObject.onCharTyped(ch);
         }
      }
      return handled;
   }

   /**
   * Tells the IETGraphObject's about a PreCopy
   */
   public void onPreCopy()
   {
      // Get all the selected product elements
      ETList < IPresentationElement > pSelectedObjects = m_Parent.getSelected();

      dispatchToETGraphObjects(pSelectedObjects, IGraphEventKind.GEK_PRE_COPY);

   }

   /**
    * Tells the IETGraphObject's about a PostCopy
    */
   public void onPostCopy()
   {
      // Get all the selected product elements
      ETList < IPresentationElement > pSelectedObjects = m_Parent.getSelected();

      dispatchToETGraphObjects(pSelectedObjects, IGraphEventKind.GEK_POST_COPY);
   }

   /**
    * Tells the IETGraphObject's about a PostPaste
    */
   public boolean onPostPaste(List nodeList, List edgeList, List nodeLabelList, List edgeLabelList)
   {
      return handlePostPaste(nodeList, edgeList, nodeLabelList, edgeLabelList);
   }

   /**
    * Handles post paste ( it used to handle duplicate as well until we deprecated that feature)
    *
    * @param nodeList [in] The nodes that were pasted
    * @param edgeList [in] The nodes that were pasted
    * @param labelList [in] The nodes that were pasted
    */
   public boolean handlePostPaste(List nodeList, List edgeList, List nodeLabelList, List edgeLabelList)
   {
      boolean bHandled = false;

      // During post paste/duplicate we need to change the presentation elements of the pasted items otherwise
      // we'll have two tomsawyer objects pointing to the same presentation element.  If any labels have
      // been pasted then whack them.

      // We need to block containment or otherwise a contained element could query for its container
      // before the container has been initialized.

		IGUIBlocker cpBlocker = null;
      try
      {
         cpBlocker = new GUIBlocker();
         cpBlocker.setKind(GBK.DIAGRAM_CONTAINMENT);

         // A list of objects we need to send post paste events to
         ETList < IETGraphObject > pETElements = new ETArrayList < IETGraphObject > ();

         /// Node List
         if (nodeList != null)
         {
            for (IteratorT < IETGraphObject > iter = new IteratorT < IETGraphObject > (nodeList); iter.hasNext();)
            {
               IETGraphObject cpObject = iter.next();

               IElement pElement = TypeConversions.getElement(cpObject);

               IETGraphObject pETElement = cpObject;

               if (pElement != null && pETElement != null)
               {
                  // The model element setter will make sure that the postaddobject event
                  // attaches the node/edge to this element rather then create a new one.
                  // It's a IETElement::Attach rather then an IETElement::Create.
                  ModelElementSetter modelElementSetter = new ModelElementSetter(m_Parent, pElement);

                  // Add to our list of guys to send post paste events to
                  pETElements.add(pETElement);

                  // Send the event to the element so it can prepare for the paste
                  pETElement.onGraphEvent(IGraphEventKind.GEK_POST_PASTE_VIEW);

                  // call parent but don't resize
                  m_Parent.postAddObject(pETElement, false);

                  m_Parent.setModelElement(null);

               }
            }

         }

         // W2526 We don't allow pasting of links or labels.  For links we'd need to figure out
         // what nodes to attach to and then create a new relationship.  Lots of work which we're not
         // going to do.
         // Edge List
         if (edgeList != null)
         {

            for (IteratorT < IETGraphObject > iter = new IteratorT < IETGraphObject > (edgeList); iter.hasNext();)
            {
               IETGraphObject cpObject = iter.next();

               IElement pElement = TypeConversions.getElement(cpObject);

               IETGraphObject pETElement = cpObject;

               if (pElement != null && pETElement != null)
               {
                  // The model element setter will make sure that the postaddobject event
                  // attaches the node/edge to this element rather then create a new one.
                  // It's a IETElement::Attach rather then an IETElement::Create.
                  ModelElementSetter modelElementSetter = new ModelElementSetter(m_Parent, pElement);

                  // Add to our list of guys to send post paste events to
                  pETElements.add(pETElement);

                  // Send the event to the element so it can prepare for the paste
                  pETElement.onGraphEvent(IGraphEventKind.GEK_POST_PASTE_VIEW);

                  // call parent but don't resize
                  m_Parent.postAddObject(pETElement, false);
                  m_Parent.setModelElement(null);
               }
            }
         }

         // Label List - these are not allowed to be pasted.
         if (nodeLabelList != null)
         {
            for (IteratorT < IETGraphObject > iter = new IteratorT < IETGraphObject > (nodeLabelList); iter.hasNext();)
            {
               IETGraphObject cpObject = iter.next();

               IElement pElement = TypeConversions.getElement(cpObject);

               IETGraphObject pETElement = cpObject;

               if (pElement != null && pETElement != null)
               {
                  // The model element setter will make sure that the postaddobject event
                  // attaches the node/edge to this element rather then create a new one.
                  // It's a IETElement::Attach rather then an IETElement::Create.
                  ModelElementSetter modelElementSetter = new ModelElementSetter(m_Parent, pElement);

                  // Add to our list of guys to send post paste events to
                  pETElements.add(pETElement);

                  // Send the event to the element so it can prepare for the paste
                  pETElement.onGraphEvent(IGraphEventKind.GEK_POST_PASTE_VIEW);

                  // call parent but don't resize
                  m_Parent.postAddObject(pETElement, false);
                  m_Parent.setModelElement(null);
               }
            }

         }
         // Label List - these are not allowed to be pasted.
         if (edgeLabelList != null)
         {
            for (IteratorT < IETGraphObject > iter = new IteratorT < IETGraphObject > (edgeLabelList); iter.hasNext();)
            {
               IETGraphObject cpObject = iter.next();

               IElement pElement = TypeConversions.getElement(cpObject);

               IETGraphObject pETElement = cpObject;

               if (pElement != null && pETElement != null)
               {
                  // The model element setter will make sure that the postaddobject event
                  // attaches the node/edge to this element rather then create a new one.
                  // It's a IETElement::Attach rather then an IETElement::Create.
                  ModelElementSetter modelElementSetter = new ModelElementSetter(m_Parent, pElement);

                  // Add to our list of guys to send post paste events to
                  pETElements.add(pETElement);

                  // Send the event to the element so it can prepare for the paste
                  pETElement.onGraphEvent(IGraphEventKind.GEK_POST_PASTE_VIEW);

                  // call parent but don't resize
                  m_Parent.postAddObject(pETElement, false);
                  m_Parent.setModelElement(null);
               }
            }
         }

         // Send out the events now that all the objects have been reattached

         for (IteratorT < IETGraphObject > iter = new IteratorT < IETGraphObject > (pETElements); iter.hasNext();)
         {
            IETGraphObject pETElement = iter.next();

            if (pETElement != null)
            {
               pETElement.onGraphEvent(IGraphEventKind.GEK_POST_PASTE_ALL);
            }
         }
      }
      finally
      {
         if (cpBlocker != null)
         {
				cpBlocker.clearBlockers();
         }
      }

      return bHandled;
   }
   /**
    *
    * Tells the IETGraphObject's about a PostMove
    *
    *
    * @return 
    *
    */
   public void onPostMove(ETList < IPresentationElement > pSelectedList)
   {

      // Now tell each product element that the move has finished
      dispatchToETGraphObjects(pSelectedList, IGraphEventKind.GEK_POST_MOVE);

   }

   /**
    *
    * Tells the IETGraphObject's about a PreMove
    *
    *
    * @return 
    *
    */
   public void onPreMove(ETList < IPresentationElement > pSelectedList)
   {

      // Now tell each product element that the move has finished
      dispatchToETGraphObjects(pSelectedList, IGraphEventKind.GEK_PRE_MOVE);

   }

}
