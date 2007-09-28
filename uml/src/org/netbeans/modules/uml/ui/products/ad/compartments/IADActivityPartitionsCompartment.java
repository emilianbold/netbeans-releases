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
