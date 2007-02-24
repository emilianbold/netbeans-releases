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

import java.util.Map;

import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.ICoreRelationshipDiscovery;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.drawing.TSDGraph;

public interface IADActivityPartitionsCompartment extends IADZonesCompartment
{
   /**
    * Populates a particular partition with its presentation elements
    * 
    * @param  nPos the position of the partition to populate
    * @return true when items were added to the partition's presentation element
    */
   public boolean populatePartition(int nPos);

   /**
    * Populates all partitions
    * 
    * @return true when items were added to the partition's presentation element
    */
   public boolean populateAllPartitions();

/*
   /// Creates the proper element for the inserting into the table
   IElement createNewElement();

   /// Specifiy the proper buttons to create
   void createZonesButtons(IMenuManager manager);

   /// Creates an IActivityPartition for the inserting into the table
   IActivityPartition createNewPartition();

   /// Lowerlevel routine to populate the partition
   boolean populateWithChildren(ICompartment compartment);

   /// Is the argument element in the argument list of contents
   boolean isInList(ETList < IActivityNode > partitionContents, IPresentationElement pe);

   /// Create a list of activity nodes that are not currently being contained by this compartment.
   ETList < IActivityNode > getListOfNewPEs(ETList < IActivityNode > partitionContents, ETList < IPresentationElement > existingPEs);

   /// Relayout the presentation elements in the compartment
   void relayoutContents(IDrawingAreaControl control, ICoreRelationshipDiscovery coreRelationshipDiscovery, ICompartment partitionCompartment, ETList < IPresentationElement > newOnes);

   /// Calculate the origin for new presentation elements based on the current rect, and current PEs
   IETPoint calculateNewElementsOrigin(final IETRect rectCompartment, final IETRect rectContainedPEs);

   /// Discovers relationships among all the contained items
   void discoverRelationships(IDrawingAreaControl control, ICompartment partitionCompartment, ETList < IPresentationElement > currentlyContained);

   /// Now remove the presentation elements that are on the diagram, but not in graphical containment
   void removeUncontainedPresentationElements(IDrawingAreaControl control, ICompartment partitionCompartment, ETList < IPresentationElement > newOnes);

   /// Ensure that the zone model elements and the zone compartments match up
   void validateZoneCompartments();

   /// Adds nodes to the offcreen graph
   void addNodesToOffscreenGraph(TSDGraph pDigraph, ETList <IPresentationElement> pNodesToAdd, Map nodeNodeMap);

   /// Get the edges and add those to the offscreen graph
   void addEdgesToOffscreenGraph(TSDGraph pDigraph, ETList <IPresentationElement> pAddedNodes, Map nodeNodeMap);

   /// Move the presentation elements and resize our parent compartment
   void movePresentationElements(ICompartment pPartitionCompartment, Map nodeNodeMap);
  */ 
}
