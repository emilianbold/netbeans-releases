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



package org.netbeans.modules.uml.ui.products.ad.compartments;

import java.awt.Color;
import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.diagrams.ICoreRelationshipDiscovery;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.CollectionTranslator;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
import org.netbeans.modules.uml.core.support.umlutils.InvalidPointerException;
import org.netbeans.modules.uml.ui.controls.drawingarea.DiagramAreaEnumerations;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.ADRelationshipDiscovery;
import org.netbeans.modules.uml.ui.support.applicationmanager.IConnectedNode;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.MoveToFlags;
import org.netbeans.modules.uml.ui.support.helpers.GUIBlocker;
import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker;
import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker.GBK;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETPointEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETRectEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETTransformOwner;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PointConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.ISimplePresentationAction;
import org.netbeans.modules.uml.ui.swing.drawingarea.SimplePresentationAction;
import com.tomsawyer.drawing.TSDEdge;
import com.tomsawyer.drawing.TSDGraph;
import com.tomsawyer.drawing.TSDGraphManager;
import com.tomsawyer.drawing.TSDNode;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.layout.glt.TSLayoutEngine;
//import com.tomsawyer.layout.glt.TSLocalLayoutProxy;
import com.tomsawyer.service.layout.jlayout.client.TSLayoutProxy;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSPoint;
import com.tomsawyer.drawing.geometry.TSPoint;
//import com.tomsawyer.util.TSRect;
import com.tomsawyer.drawing.geometry.TSRect;


/**
 * @author Embarcadero Technologies Inc.
 *
 */

public class ETActivityPartitionsCompartment extends ETZonesCompartment implements IADActivityPartitionsCompartment
{

   /// Spacing used between contained presentation elements
   private static final int nSpacing = 20;

   //  Increase the size of the compartment to supply some buffer between the compartment edge
   //  and the internal presentation elements
   private static final int nPopulateSpacing = 10;

   public ETActivityPartitionsCompartment()
   {
      super();
   }

   ///////////////////////////////////////////////////////////////////////////////
   // ICompartment operations
   ///////////////////////////////////////////////////////////////////////////////

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#getCompartmentID()
    */
   public String getCompartmentID()
   {
      return "ADActivityPartitionsCompartment";
   }

   ///////////////////////////////////////////////////////////////////////////////
   // IADActivityPartitionsCompartment operations
   ///////////////////////////////////////////////////////////////////////////////

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADActivityPartitionsCompartment#PopulatePartition(int)
    */
   public boolean populatePartition(int nIndex)
   {
      boolean bItemsAdded = false;

      if ((nIndex >= 0) && (nIndex < getNumCompartments()))
      {
         ICompartment compartment = getCompartment(nIndex);
         if (compartment != null)
         {
            // Populate this partition with the presentation elements that 
            // represent the model elements in the partition.
            bItemsAdded = populateWithChildren(compartment);
         }
      }

      return bItemsAdded;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADActivityPartitionsCompartment#PopulateAllPartitions()
    */
   public boolean populateAllPartitions()
   {
      boolean bItemsAdded = false;

      final int cnt = this.getNumCompartments();

      if (cnt > 0)
      {
         for (int i = 0; i < cnt; i++)
         {
            boolean bThisCompartmentAddItems = populatePartition(i);
            if (bThisCompartmentAddItems)
            {
               bItemsAdded = true;
            }
         }
      }
      else
      {
         // Populate the zones compartment itself
         bItemsAdded = populateWithChildren(this);
      }

      return bItemsAdded;
   }

   ///////////////////////////////////////////////////////////////////////////////
   // CADZonesCompartment virtual overrides
   ///////////////////////////////////////////////////////////////////////////////

   /**
   * Creates the proper element for the inserting into the table
   */
   protected IElement createNewElement()
   {
      return createNewPartition();
   }

   /**
    * Specifiy the proper buttons to create
    */
   protected void createZonesButtons(IMenuManager manager)
   {
      addActivityPartionsButtons(manager, m_zonedividers.getOrientation());
   }

   /**
    * Creates an IActivityPartition for the inserting into the table
    */
   protected IActivityPartition createNewPartition()
   {
      IActivityPartition partition = null;

      IElement element = getModelElement();
      if (element instanceof IActivityPartition)
      {
         IActivityPartition parentPartition = (IActivityPartition)element;

         TypedFactoryRetriever < IActivityPartition > ret = new TypedFactoryRetriever < IActivityPartition > ();
         partition = ret.createType("ActivityPartition");
         if (partition != null)
         {
            parentPartition.addSubPartition(partition);
         }
      }

      return partition;
   }

   /**
    * Lowerlevel routine to populate the partition
    */
   protected boolean populateWithChildren(ICompartment compartment)
   {
      if (null == compartment)
         throw new IllegalArgumentException();

      boolean bItemsAdded = false;

      IGUIBlocker blocker = null;

      try
      {
         blocker = new GUIBlocker();
         blocker.setKind(GBK.DIAGRAM_CONTAINMENT);

         IActivityPartition partition = null;
         {
            IElement element = compartment.getModelElement();
            if (element instanceof IActivityPartition)
            {
               partition = (IActivityPartition)element;
            }
         }

         IDrawingAreaControl control = getDrawingArea();
         ICoreRelationshipDiscovery coreRelationshipDiscovery = null;

         if (control != null)
         {
            coreRelationshipDiscovery = control.getRelationshipDiscovery();
         }

         IETRect rectBounding = compartment.getLogicalBoundingRect();

         if ((partition != null) && (control != null) && (coreRelationshipDiscovery != null) && (rectBounding != null))
         {
            // Get the contents of the partition
            ETList < IActivityNode > partitionContents = partition.getNodeContents();

            // Now that we have a list of all the nodes that should be
            // there get a list of those that are there that shouldn't be.
            ETList < IPresentationElement > currentlyContained = getContained(compartment);

            long numPartitionContents = (partitionContents != null) ? partitionContents.size() : 0;
            long numCurrentlyContained = (currentlyContained != null) ? currentlyContained.size() : 0;

            // Now break the currently contained presentation elements into those that
            // should be there and those that shouldn't
            ETList < IPresentationElement > containedThatShouldBeContained = new ETArrayList < IPresentationElement > ();
            ETList < IPresentationElement > containedThatShouldNotBeContained = new ETArrayList < IPresentationElement > ();
            ETList < IPresentationElement > pesThatWereCreated = new ETArrayList < IPresentationElement > ();

            if (currentlyContained != null)
            {
               for (Iterator iter = currentlyContained.iterator(); iter.hasNext();)
               {
                  IPresentationElement thisItem = (IPresentationElement)iter.next();

                  // See if it should be contained
                  if (isInList(partitionContents, thisItem))
                  {
                     containedThatShouldBeContained.add(thisItem);
                  }
                  else
                  {
                     containedThatShouldNotBeContained.add(thisItem);
                  }
               }
            }

            // Now remove the presentation elements that shouldn't be there
            for (Iterator iterator = containedThatShouldNotBeContained.iterator(); iterator.hasNext();)
            {
               IPresentationElement thisItem = (IPresentationElement)iterator.next();
               control.postDeletePresentationElement(thisItem);
            }

            // Now go through the elements that are contained and remove them from the
            // list of contained items.  The remainder is the list of new presentation elements
            // that should be created.
            ETList < IActivityNode > partitionContentsToCreate = getListOfNewPEs(partitionContents, containedThatShouldBeContained);

            // Go through the list of guys to create and create presentation elements
            IETPoint etPoint = PointConversions.newETPoint(rectBounding.getTopLeft());

            if (partitionContentsToCreate != null)
            {
               for (Iterator iterator = partitionContentsToCreate.iterator(); iterator.hasNext();)
               {
                  IActivityNode thisItem = (IActivityNode)iterator.next();
                  IPresentationElement createPE = coreRelationshipDiscovery.createNodePresentationElement(thisItem, etPoint);

                  if (createPE != null)
                  {
                     pesThatWereCreated.add(createPE);

                     // Tell our caller that we've added some stuff to the compartment
                     bItemsAdded = true;
                  }
               }

               // Relayout the partitions compartment and discover relationships
               relayoutContents(control, coreRelationshipDiscovery, compartment, pesThatWereCreated);
            }
         }
      }
      finally
      {
         if (blocker != null)
         {
            blocker.clearBlockers();
         }
      }

      return bItemsAdded;
   }

   /**
    * Is the argument element in the argument list of contents
    */
   protected boolean isInList(ETList < IActivityNode > partitionContents, IPresentationElement pe)
   {
      boolean bIsInList = false;

      if ((partitionContents != null) && (pe != null))
      {
         IElement firstSubject = pe.getFirstSubject();
         if (firstSubject instanceof IActivityNode)
         {
            IActivityNode activityNode = (IActivityNode)firstSubject;

            bIsInList = partitionContents.isInList(activityNode);
         }
      }

      return bIsInList;
   }

   /**
    * Create a list of activity nodes that are not currently being contained by this 
    * compartment.
    */
   protected ETList < IActivityNode > getListOfNewPEs(ETList < IActivityNode > partitionContents, ETList < IPresentationElement > existingPEs)
   {
      if (null == existingPEs)
         throw new IllegalArgumentException();

      ETList < IActivityNode > partitionContentsToCreate = null;

      if (partitionContents != null)
      {
         ETList < IActivityNode > foundGuysToCreate = new ETArrayList < IActivityNode > ();

         for (Iterator iter = partitionContents.iterator(); iter.hasNext();)
         {
            IActivityNode thisNode = (IActivityNode)iter.next();

            boolean bIsRepresentedInList = ADRelationshipDiscovery.isRepresentedInList(existingPEs, thisNode);
            if (!bIsRepresentedInList)
            {
               foundGuysToCreate.add(thisNode);
            }
         }

         partitionContentsToCreate = foundGuysToCreate;
      }

      return partitionContentsToCreate;
   }

   /**
    * Relayout the presentation elements in the compartment
    */
   protected void relayoutContents(IDrawingAreaControl control, ICoreRelationshipDiscovery coreRelationshipDiscovery, ICompartment partitionCompartment, ETList < IPresentationElement > newOnes)
   {
      if (null == control)
         throw new IllegalArgumentException();
      if (null == coreRelationshipDiscovery)
         throw new IllegalArgumentException();
      if (null == partitionCompartment)
         throw new IllegalArgumentException();
      if (null == newOnes)
         throw new IllegalArgumentException();

      control.pumpMessages(false);

      // Now remove the presentation elements that are on the diagram, but not in
      // graphical containment
      removeUncontainedPresentationElements(control, partitionCompartment, newOnes);

      control.pumpMessages(false);

      // Current contents
      ETList < IPresentationElement > currentlyContained = new ETArrayList < IPresentationElement > ();

      // Need this because getContained might return null instead of an empty list, but we still need to add "newOnes"
      ETList < IPresentationElement > tmpContained = getContained(partitionCompartment);
      if (tmpContained != null)
      {
         currentlyContained.addThese(tmpContained);
      }

      if (currentlyContained != null)
      {
         // Combine the new ones to the currently contained to create a master list of items that
         // should be in the compartment
         currentlyContained.addThese(newOnes);
         final int nCurrentlyContained = currentlyContained.size();

         if (nCurrentlyContained > 0)
         {
            // Discover the relationships
            discoverRelationships(control, partitionCompartment, currentlyContained);

            control.pumpMessages(false);

            TSDGraph pDigraph = null;
            TSDGraphManager pGraphManager = new TSDGraphManager();

            if (pGraphManager != null)
            {
               pDigraph = (TSDGraph)pGraphManager.addGraph();
               //pDigraph.setLayoutStyle(TSDGraph.TREE);
            }

            if (pDigraph != null)
            {
               // Add the nodes to the temp graph
               //Map < IPresentationElement, TSDNode > nodeNodeMap; // The PE to TSDNode map

               Map nodeNodeMap = new HashMap(); // The PE to TSDNode map

               // Add nodes to the offscreen graph
               addNodesToOffscreenGraph(pDigraph, currentlyContained, nodeNodeMap);

               // Get the edges and add those to the offscreen graph
               addEdgesToOffscreenGraph(pDigraph, currentlyContained, nodeNodeMap);

               //if (!TSLayoutEngine.isGLTInitialized())
               //{
               //  TSLayoutEngine.initializeGLT();
               //}

               // Layout that graph
               //TSLocalLayoutProxy layoutServer = new TSLocalLayoutProxy();
			   TSLayoutProxy layoutProxy = new TSLayoutProxy();
               try
               {
                   performLayout(control.getDiagram(), true);
               }
               catch (Exception e)
               {
                  e.printStackTrace();
               }

               // Move the presentation elements and resize our parent compartment
               movePresentationElements(partitionCompartment, nodeNodeMap, currentlyContained);

               // Resize the compartment
               IETRect rectOfCurrentPEs = TypeConversions.getLogicalBoundingRect(currentlyContained, false);
               rectOfCurrentPEs.setRight(rectOfCurrentPEs.getRight() + nPopulateSpacing);
               rectOfCurrentPEs.setBottom(rectOfCurrentPEs.getBottom() - nPopulateSpacing);

               resizeToContain(partitionCompartment, rectOfCurrentPEs);
               
            }
         }
      }
   }

   ///////////////////////////////////////////////////////////////////////////////////////////
    // TEST TEST TEST TEST
    ///////////////////////////////////////////////////////////////////////////////////////////
   
//   private void printPositions(ICompartment partitionCompartment, Map nodeNodeMap)
//   {
//       System.out.println("************************************");
//       System.out.println("**  Print Locations After Resize ***");
//       System.out.println("************************************");
//       System.out.println("====================================");
//       System.out.println("== Partition Compartmedt          ==");
//       TSENode node = (TSENode)TypeConversions.getTSObject(partitionCompartment);
//       System.out.println("Left = " + node.getLeft());
//       System.out.println("Top = " + node.getTop());
//       System.out.println("Right = " + node.getRight());
//       System.out.println("Bottom = " + node.getBottom());
//
//       IPresentationElement element = TypeConversions.getPresentationElement(partitionCompartment);
//       IETRect rectCompartment = TypeConversions.getLogicalBoundingRect(element);
//
//       System.out.println("------------------------------------");
//
//       System.out.println("Left = " + rectCompartment.getLeft());
//       System.out.println("Top = " + rectCompartment.getTop());
//       System.out.println("Right = " + rectCompartment.getRight());
//       System.out.println("Bottom = " + rectCompartment.getBottom());
//       System.out.println("====================================");
//       
//       for(Object tsObject : nodeNodeMap.values())
//       {
//           System.out.println("====================================");
//           TSDNode tsdNode = (TSDNode)tsObject;
//           System.out.println("Left = " + tsdNode.getLeft());
//           System.out.println("Top = " + tsdNode.getTop());
//           System.out.println("Right = " + tsdNode.getRight());
//           System.out.println("Bottom = " + tsdNode.getBottom());
//
//           element = TypeConversions.getPresentationElement(node);
//           rectCompartment = TypeConversions.getLogicalBoundingRect(element);
//
//           System.out.println("------------------------------------");
//
//           System.out.println("Left = " + rectCompartment.getLeft());
//           System.out.println("Top = " + rectCompartment.getTop());
//           System.out.println("Right = " + rectCompartment.getRight());
//           System.out.println("Bottom = " + rectCompartment.getBottom());
//           System.out.println("====================================");
//       }
//   }
   
   /**
	 * Prepare the diagram for layout, and layout the presentation elements on the diagram
	 *
	 * @param pDiagram [in] The diagram to perform layout on.
	 * @param bIgnoreContainment [in] Should we ignore containment?
	 */
	private void performLayout(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram pDiagram, boolean ignoreContainment)
	{
		if (pDiagram != null)
		{
			// Deselect everything
			pDiagram.selectAll(false);
			
			// Set the default mode to be selection
			pDiagram.enterMode(org.netbeans.modules.uml.core.metamodel.diagrams.IDrawingToolKind.DTK_SELECTION);
			
			// Layout the diagram
			int diaKind = pDiagram.getDiagramKind();
			
			// Let the diagram m decide its layout kind.
			//int layoutKind = getLayoutKind(diaKind);
			
			// Do a layout using our action which will automatically create
			// a blocker for containment if necessary
			if (pDiagram instanceof org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram)
			{
				org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram dia = (org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram) pDiagram;
				IDrawingAreaControl cpControl = dia.getDrawingArea();
				if (cpControl != null)
				{					
					{
						// Perform normal layout routines
						org.netbeans.modules.uml.ui.controls.drawingarea.ITopographyChangeAction changeAction = new org.netbeans.modules.uml.ui.controls.drawingarea.TopographyChangeAction();
						if (ignoreContainment)
						{
							changeAction.setKind(DiagramAreaEnumerations.TAK_LAYOUTCHANGE_IGNORECONTAINMENT_SILENT);
						}
						else
						{
							changeAction.setKind(DiagramAreaEnumerations.TAK_LAYOUTCHANGE_SILENT);
						}
						
						changeAction.setLayoutStyle(true, true, pDiagram.getLayoutStyle());
						changeAction.execute(cpControl);
						//dia.postDelayedAction(changeAction);
					}
					cpControl.refresh(false);
				}
			}
		}
	}
    ///////////////////////////////////////////////////////////////////////////////////////////
    // TEST TEST TEST TEST
    ///////////////////////////////////////////////////////////////////////////////////////////
   /**
    * Discovers relationships among all the contained items
    */
   protected void discoverRelationships(IDrawingAreaControl control, ICompartment partitionCompartment, ETList < IPresentationElement > currentlyContained)
   {
      if (null == control)
         throw new IllegalArgumentException();
      if (null == partitionCompartment)
         throw new IllegalArgumentException();
      if (null == currentlyContained)
         throw new IllegalArgumentException();

      // Get all the contained elements
      long nCurrentlyContained = currentlyContained.size();

      if (nCurrentlyContained > 0)
      {
         // First discover inside the compartment we're populating
         control.postSimplePresentationDelayedAction(currentlyContained, DiagramAreaEnumerations.SPAK_DISCOVER_RELATIONSHIPS);

         // Now discover between this compartment and the other compartments
         final int cnt = getNumCompartments();
         for (int i = 0; i < cnt; i++)
         {
            ICompartment otherCompartment = getCompartment(i);
            if (otherCompartment != partitionCompartment)
            {
               ETList < IPresentationElement > otherCompartmentContained = getContained(otherCompartment);
               if (otherCompartmentContained != null)
               {
                  final int nOtherCount = otherCompartmentContained.size();
                  if (nOtherCount > 0)
                  {
                     ISimplePresentationAction simplePEAction = new SimplePresentationAction();
                     if (simplePEAction != null)
                     {
                        simplePEAction.setKind(DiagramAreaEnumerations.SPAK_DISCOVER_RELATIONSHIPS);
                        simplePEAction.setPresentationElements(currentlyContained);
                        simplePEAction.setSecondaryPresentationElements(otherCompartmentContained);

                        control.postDelayedAction(simplePEAction);
                     }
                  }
               }
            }
         }
      }
   }

   /**
    * Now remove the presentation elements that are on the diagram, but not in graphical containment
    */
   protected void removeUncontainedPresentationElements(IDrawingAreaControl control, ICompartment partitionCompartment, ETList < IPresentationElement > newOnes)
   {
      if (null == control)
         throw new IllegalArgumentException();
      if (null == partitionCompartment)
         throw new IllegalArgumentException();

      // Current contents
      ETList < IPresentationElement > currentlyContained = new ETArrayList < IPresentationElement > ();

      // Need this because getContained might return null instead of an empty list, but we still need to add "newOnes"
      ETList < IPresentationElement > tmpContained = getContained(partitionCompartment);
      if (tmpContained != null)
      {
         currentlyContained.addThese(tmpContained);
      }

      int numCurrentlyContained = 0;

      currentlyContained.addThese(newOnes);
      numCurrentlyContained = currentlyContained.size();

      // Go over the list and remove any duplicate presentation elements
      for (int i = 0; i < numCurrentlyContained; i++)
      {
         IPresentationElement cpThisPE = currentlyContained.get(i);

         if (cpThisPE != null)
         {
            IElement pFirstSubject = cpThisPE.getFirstSubject();

            if (pFirstSubject != null)
            {
               // Get all the PEs for this element on the diagram and remove all but
               // the one being contained.
               ETList < IPresentationElement > pAllCurrentlyOnDiagram = null;
               int numOnDiagram = 0;

               pAllCurrentlyOnDiagram = control.getAllItems2(pFirstSubject);

               if (pAllCurrentlyOnDiagram != null)
               {
                  numOnDiagram = pAllCurrentlyOnDiagram.size();

                  if (numOnDiagram > 1)
                  {
                     // Remove the one that's not contained.
                     for (int j = 0; j < numOnDiagram; j++)
                     {
                        IPresentationElement pOnDiagram = pAllCurrentlyOnDiagram.get(j);

                        if (pOnDiagram != null)
                        {
                           boolean bInList = false;

                           bInList = currentlyContained.isInList(pOnDiagram);

                           if (!bInList)
                           {
//                              control.postDeletePresentationElement(pOnDiagram);
                               control.postDelayedAction(new org.netbeans.modules.uml.ui.swing.drawingarea.PresentationElementToDeleteAction(pOnDiagram));                               
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      // Now we need to make sure the contain items don't appear twice
      ETList < IElement > allFirstSubjects = new ETArrayList < IElement > ();

      for (Iterator iter = currentlyContained.iterator(); iter.hasNext();)
      {
         IPresentationElement thisPE = (IPresentationElement)iter.next();
         if (thisPE != null)
         {

            IElement firstSubject = thisPE.getFirstSubject();
            if (firstSubject != null)
            {
               boolean bInList = allFirstSubjects.isInList(firstSubject);
               if (bInList)
               {
                  // We have a duplicate contained item, remove it.
                  control.postDeletePresentationElement(thisPE);
               }
               else
               {
                  allFirstSubjects.add(firstSubject);
               }
            }
         }
      }
   }

   /**
    * Ensure that the zone model elements and the zone compartments match up
    */
   protected void validateZoneCompartments(boolean attachElements)
   {
      IElement element = getModelElement();
      if (element instanceof IActivityPartition)
      {
         IActivityPartition parentPartition = (IActivityPartition)element;

         ETList < IActivityPartition > partitions = parentPartition.getSubPartitions();
         if (partitions != null)
         {
            // Copy the partitions to the elements
            ETList < IElement > elements = null;
            elements = (new CollectionTranslator < IActivityPartition, IElement > ()).copyCollection(partitions);

            super.validateZoneCompartments(elements, 
                                           IETZoneDividers.DMO_VERTICAL, 
                                           attachElements);
         }
      }
   }

   /// Adds nodes to the offcreen graph
   protected void addNodesToOffscreenGraph(TSDGraph pDigraph, ETList < IPresentationElement > pNodesToAdd, Map nodeNodeMap)
   {
      int nNodesToAdd = pNodesToAdd.size();

      for (int i = 0; i < nNodesToAdd; i++)
      {
         IPresentationElement pPE = pNodesToAdd.get(i);

         INodePresentation pNodePE = (pPE instanceof INodePresentation) ? (INodePresentation)pPE : null;

         if (pNodePE != null)
         {
            pNodePE.sizeToContents();

            int nWidth = 0;
            int nHeight = 0;

            IETRect pETRect = pNodePE.getBoundingRect();

            if (pETRect != null)
            {
               nWidth = pETRect.getIntWidth();

               nHeight = pETRect.getIntHeight();

               TSDNode pNewNode = null;

               // add the tom sawyer node to the graph
               pNewNode = (TSDNode)pDigraph.addNode();

               if (pNewNode != null)
               {
                  pNewNode.setSize(nWidth, nHeight);
                  pNewNode.setLocalSize(nWidth, nHeight);
                  pNewNode.setOriginalSize(nWidth, nHeight);

                  nodeNodeMap.put(pNodePE, pNewNode);
               }
            }
         }
      }
   }

   /// Get the edges and add those to the offscreen graph
   protected void addEdgesToOffscreenGraph(TSDGraph pDigraph, ETList < IPresentationElement > pAddedNodes, Map nodeNodeMap)
   {
      int nAddedNodes = 0;

      ETList < IPresentationElement > pFoundEdgePEs = new ETArrayList < IPresentationElement > ();

      if (pAddedNodes != null)
      {
         nAddedNodes = pAddedNodes.size();
      }

      // Create the edges.  Need to find the edges between the presentation elements
      // above and reproduce them on the temporary graph.
      for (int i = 0; i < nAddedNodes; i++)
      {
         IPresentationElement pPE;

         pPE = (IPresentationElement)pAddedNodes.get(i);

         INodePresentation pNodePE = (pPE instanceof INodePresentation) ? (INodePresentation)pPE : null;

         if (pNodePE != null)
         {
            // See if there are any edges connecting this node to another node
            // in the list.
            ETList < IConnectedNode > pConnectedNodes = null;

            int count = 0;

            pConnectedNodes = pNodePE.getEdgeConnectedNodes();

            if (pConnectedNodes != null)
            {
               count = pConnectedNodes.size();
            }

            for (int j = 0; j < count; j++)
            {
               IConnectedNode pConnectedNode = (IConnectedNode)pConnectedNodes.get(j);

               if (pConnectedNode != null)
               {
                  IEdgePresentation pConnectingEdge = pConnectedNode.getIntermediateEdge();
                  INodePresentation pNodeAtOtherEnd = pConnectedNode.getNodeAtOtherEnd();

                  //ATLASSERT(pConnectingEdge && pNodeAtOtherEnd);

                  if (pConnectingEdge != null && pNodeAtOtherEnd != null)
                  {
                     boolean bFound = false;

                     bFound = pAddedNodes.isInList(pNodeAtOtherEnd);

                     if (bFound)
                     {
                        pFoundEdgePEs.addIfNotInList(pConnectingEdge);
                     }
                  }
               }
            }
         }
      }

      int nAddedEdges = pFoundEdgePEs.size();

      // Create the edges
      for (int i = 0; i < nAddedEdges; i++)
      {
         IPresentationElement pPE = (IPresentationElement)pFoundEdgePEs.get(i);
         IEdgePresentation pEdgePE = (pPE instanceof IEdgePresentation) ? (IEdgePresentation)pPE : null;

         if (pEdgePE != null)
         {
            ETPairT < INodePresentation, INodePresentation > result = pEdgePE.getEdgeFromAndToPresentationElement();
            INodePresentation pNode1 = (result != null) ? result.getParamOne() : null;
            INodePresentation pNode2 = (result != null) ? result.getParamTwo() : null;

            if (pNode1 != null && pNode2 != null)
            {
               TSDNode pNewNode1 = null;
               TSDNode pNewNode2 = null;
               pNewNode1 = (TSDNode)nodeNodeMap.get(pNode1);
               pNewNode2 = (TSDNode)nodeNodeMap.get(pNode2);

               if (pNewNode1 != null && pNewNode2 != null)
               {
                  TSDEdge pNewEdge = (TSDEdge)pDigraph.addEdge(pNewNode1, pNewNode2);
               }
            }
         }
      }
   }

   /// Move the presentation elements and resize our parent compartment
   protected void movePresentationElements(ICompartment pPartitionCompartment, 
                                           Map nodeNodeMap, 
                                           ETList < IPresentationElement > contained )
   {
      // Get the locations of the nodes
      Iterator iter = nodeNodeMap.keySet().iterator();

      IETRect bounding = TypeConversions.getLogicalBoundingRect(contained, false);
      IETRect rectCompartment = TypeConversions.getLogicalBoundingRect(pPartitionCompartment);   
      
      TSENode tempNode = (TSENode)TypeConversions.getTSObject(pPartitionCompartment);
      double top = tempNode.getTop();
      double left = tempNode.getLeft();
      
      while (iter.hasNext())
      {
         IPresentationElement pTempPE = (IPresentationElement)iter.next();

         TSDNode pThisNode = (TSDNode)nodeNodeMap.get(pTempPE);

         INodePresentation pNodePE = (pTempPE instanceof INodePresentation) ? (INodePresentation)pTempPE : null;

         if (pThisNode != null && pNodePE != null)
         {
            int spacing = 8 * nPopulateSpacing;

            IETRect mfcRect = new ETRectEx(pThisNode.getBounds());
            IETRect peRect = TypeConversions.getLogicalBoundingRect(pTempPE);
            int diffX = Math.abs(peRect.getLeft() - bounding.getLeft());
            int diffY = Math.abs(bounding.getTop() - peRect.getTop());
            pNodePE.moveTo(rectCompartment.getLeft() + diffX + spacing, 
                           rectCompartment.getTop() - diffY - spacing);
         }
      }
   }

   /**
    * Calculate the origin for new presentation elements based on the current rect, and current PEs
    */
   protected IETPoint calculateNewElementsOrigin(IETRect rectCompartment, IETRect rectContainedPEs)
   {
      IETPoint ptOrigin = new ETPoint(0, 0);

      // Remember to account for vertical vs horizontal partitions
      // We need to layout the new presentation elements starting from the upper left
      if (rectContainedPEs != null && (rectContainedPEs.getWidth() != 0 && rectContainedPEs.getHeight() != 0))
      {
         if (m_zonedividers.getOrientation() == IETZoneDividers.DMO_HORIZONTAL)
         {
            ptOrigin.setX(rectContainedPEs.getRight());
            ptOrigin.setY(rectContainedPEs.getTop());
         }
         else
         {
            ptOrigin.setX(rectContainedPEs.getLeft());
            ptOrigin.setY(rectContainedPEs.getBottom());
         }
      }
      else if (rectCompartment != null)
      {
         ptOrigin = PointConversions.newETPoint(rectCompartment.getTopLeft());
      }

      if (m_zonedividers.getOrientation() == IETZoneDividers.DMO_HORIZONTAL)
      {
         // Offset the top to be just below the top of the partition
         ptOrigin.setY(ptOrigin.getY() - nSpacing);
      }
      else
      {
         // Offset the left to put the new PEs in a line just to the right of the left side
         // of the partition
         ptOrigin.setX(ptOrigin.getX() + nSpacing);
      }

      return ptOrigin;
   }

   //////////////////////////////////////////////////////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////////////////////////////////////////////////////
   ////TODO The following should be throw away code once the Transform is initialized properly in the base classes

   public void setLogicalOffsetInDrawEngineRect(IETPoint value)
   {
      super.setLogicalOffsetInDrawEngineRect(value);
      this.setAbsoluteOwnerOrigin(value);

      final int compartmentCnt = getNumCompartments();
      if (compartmentCnt > 0)
      {
         ensureProperDividerCount();

         IETRect pBoundingRect = getLogicalBoundingRect();

         IETRect rectLocalBounding = (IETRect)getLogicalBoundingRect().clone();

         final double dZoom = this.getZoomLevel();

         int orientation = m_zonedividers.getOrientation();
         switch (orientation)
         {
            case IETZoneDividers.DMO_HORIZONTAL :
               {
                  IETPoint ptDrawOffset = m_zonedividers.getDrawOffset(pBoundingRect);

                  final int iOffset = ptDrawOffset.getY() - pBoundingRect.getTop();

                  int previousBottom = pBoundingRect.getTop();

                  for (int compartmentIndx = 0; compartmentIndx < compartmentCnt; compartmentIndx++)
                  {
                     rectLocalBounding.setTop(previousBottom);

                     previousBottom =
                        (compartmentIndx < m_zonedividers.getDividerCnt())
                           ? pBoundingRect.getTop() + iOffset + (int) (m_zonedividers.getDividerOffset(compartmentIndx) * dZoom)
                           : pBoundingRect.getBottom();

                     rectLocalBounding.setBottom(previousBottom);

                     ICompartment compartment = getCompartment(compartmentIndx);
                     if (compartment != null)
                     {
                        compartment.setLogicalOffsetInDrawEngineRect(new ETPoint(value.getX(), rectLocalBounding.getBottom()));
                        compartment.setTransformSize(rectLocalBounding.getIntWidth(), rectLocalBounding.getIntHeight());
                     }
                  }
               }
               break;

            case IETZoneDividers.DMO_VERTICAL :
               {
                  final int iOffset = m_zonedividers.getDrawOffset(pBoundingRect).getX() - pBoundingRect.getLeft();

                  int previousRight = pBoundingRect.getLeft();

                  for (int compartmentIndx = 0; compartmentIndx < compartmentCnt; compartmentIndx++)
                  {
                     rectLocalBounding.setLeft(previousRight);

                     previousRight =
                        (compartmentIndx < (int) (m_zonedividers.getDividerCnt()))
                           ? pBoundingRect.getLeft() + (int) (iOffset + m_zonedividers.getDividerOffset(compartmentIndx) * dZoom)
                           : pBoundingRect.getRight();

                     rectLocalBounding.setRight(previousRight);

                     ICompartment compartment = getCompartment(compartmentIndx);
                     if (compartment != null)
                     {
                        compartment.setLogicalOffsetInDrawEngineRect(new ETPoint(rectLocalBounding.getRight(), value.getY()));
                        compartment.setTransformSize(rectLocalBounding.getIntWidth(), rectLocalBounding.getIntHeight());
                     }
                  }

               }
               break;

            default :
               break;
         }

      }
   }

   // Messing around for the workaround
   //   public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
   //   {
   //      IETSize retValue = super.calculateOptimumSize(pDrawInfo, bAt100Pct);
   //
   //      Iterator < ICompartment > iterator = this.getCompartments().iterator();
   //
   //      while (iterator.hasNext())
   //      {
   //         ICompartment zoneCompartment = iterator.next();
   //         zoneCompartment.setTransformSize(retValue);
   //      }
   //      return retValue;
   //   }

   //   protected IETPoint calculateNewElementsOrigin2(IETRect rectCompartment)
   //   {
   //      IETPoint ptOrigin = new ETPoint(0, 0);
   //
   //      // Remember to account for vertical vs horizontal partitions
   //      // We need to layout the new presentation elements starting from the upper left
   //      if (rectCompartment != null)
   //      {
   //         if (m_zonedividers.getOrientation() == IETZoneDividers.DMO_HORIZONTAL)
   //         {
   //            ptOrigin.setX(rectCompartment.getLeft());
   //            ptOrigin.setY(rectCompartment.getBottom());
   //            ptOrigin.setY(ptOrigin.getY() - nSpacing);
   //         }
   //         else
   //         {
   //            ptOrigin.setX(rectCompartment.getRight());
   //            ptOrigin.setY(rectCompartment.getTop());
   //            ptOrigin.setX(ptOrigin.getX() + nSpacing);
   //         }
   //      }
   //
   //      return ptOrigin;
   //   }
   //
   //   public void setLogicalOffsetInDrawEngineRect(IETPoint value)
   //   {
   //      super.setLogicalOffsetInDrawEngineRect(value);
   //      this.setAbsoluteOwnerOrigin(value);
   //
   //      Iterator < ICompartment > iterator = this.getCompartments().iterator();
   //
   //      ICompartment prevCompartment = null;
   //      IETPoint newOffset = null;
   //
   //      while (iterator.hasNext())
   //      {
   //         ICompartment zoneCompartment = iterator.next();
   //
   //         if (prevCompartment != null)
   //         {
   //
   //            newOffset = calculateNewElementsOrigin2(prevCompartment.getLogicalBoundingRect());
   //         }
   //         else
   //         {
   //            newOffset = new ETPoint(value.getX(), value.getY());
   //         }
   //
   //         zoneCompartment.setLogicalOffsetInDrawEngineRect(newOffset);
   //
   //         prevCompartment = zoneCompartment;
   //      }
   //
   //   }

   //	public void setLogicalOffsetInDrawEngineRect(IETPoint value)
   //	{
   //		super.setLogicalOffsetInDrawEngineRect(value);
   //		this.setAbsoluteOwnerOrigin(value);
   //
   //		Iterator < ICompartment > iterator = this.getCompartments().iterator();
   //
   //		while (iterator.hasNext())
   //		{
   //			ICompartment zoneCompartment = iterator.next();
   //
   //			zoneCompartment.setLogicalOffsetInDrawEngineRect(value);
   //
   //		}
   //
   //	}

   /*
            
   	protected IETPoint calculateNewElementsOrigin2(IETRect rectCompartment)
   	{
   		IETPoint ptOrigin = new ETPoint(0, 0);
   
   		// Remember to account for vertical vs horizontal partitions
   		// We need to layout the new presentation elements starting from the upper left
   		if (rectCompartment != null)
   		{
   			if (m_zonedividers.getOrientation() == IETZoneDividers.DMO_HORIZONTAL)
   			{
   				ptOrigin.setX(rectCompartment.getLeft());
   				ptOrigin.setY(rectCompartment.getBottom());
   				ptOrigin.setY(ptOrigin.getY() - nSpacing);
   			}
   			else
   			{
   				ptOrigin.setX(rectCompartment.getRight());
   				ptOrigin.setY(rectCompartment.getTop());
   				ptOrigin.setX(ptOrigin.getX() + nSpacing);
   			}
   		}
   
   		return ptOrigin;
   	}
   
      public void setLogicalOffsetInDrawEngineRect(IETPoint value)
      {
         super.setLogicalOffsetInDrawEngineRect(value);
         this.setAbsoluteOwnerOrigin(value);
   
         Iterator < ICompartment > iterator = this.getCompartments().iterator();
   
         ICompartment prevCompartment = null;
         IETPoint newOffset = null;
   
         while (iterator.hasNext())
         {
            ICompartment zoneCompartment = iterator.next();
   
            if (prevCompartment != null)
            {
   
               newOffset = calculateNewElementsOrigin2(prevCompartment.getLogicalBoundingRect());
            }
            else
            {
               newOffset = new ETPoint(value.getX(), value.getY());
            }
   
            zoneCompartment.setLogicalOffsetInDrawEngineRect(newOffset);
   
            prevCompartment = zoneCompartment;
         }
   
      }
   */

   //   public void setLogicalOffsetInDrawEngineRect(IETPoint value)
   //   {
   //      super.setLogicalOffsetInDrawEngineRect(value);
   //		this.setAbsoluteOwnerOrigin(value);
   //
   //		ICompartment prevCompartment = null;
   //		Iterator < ICompartment > iterator = this.getCompartments().iterator();
   //		
   //		while (iterator.hasNext())
   //		{
   //			ICompartment zoneCompartment = iterator.next();	
   //			
   //			IETPoint newOffset = null;
   //			IETSize prevSize = null;
   //									
   //			if (prevCompartment != null){
   //				
   //				prevSize = (((ETTransformOwner)prevCompartment).getAbsoluteSize() != null)? ((ETTransformOwner)prevCompartment).getAbsoluteSize(): new ETSize(0,0);
   //				newOffset = new ETPoint((value.getX() + prevSize.getWidth()+ nSpacing*2),value.getY());				
   //				
   //			}else{
   //				newOffset = new ETPoint(value.getX(),value.getY());				
   //			}
   //					 
   //			zoneCompartment.setLogicalOffsetInDrawEngineRect(newOffset);
   //			prevCompartment = zoneCompartment;
   //		}
   //		
   //   }

   //   public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
   //   {
   //		IETSize retValue =  super.calculateOptimumSize(pDrawInfo, bAt100Pct);
   //
   //		Iterator < ICompartment > iterator = this.getCompartments().iterator();
   //		
   //		while (iterator.hasNext())
   //		{
   //			ICompartment zoneCompartment = iterator.next();
   //			//IETSize compSize = zoneCompartment.calculateOptimumSize(pDrawInfo, bAt100Pct);
   //			zoneCompartment.setTransformSize(retValue);
   //		}
   //		
   //		return retValue;
   //   }

   //	//TODO The above would be throw away code once the Transform is initialized properly

}
