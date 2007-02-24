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



package org.netbeans.modules.uml.ui.products.ad.drawengines;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNodeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext;
import com.tomsawyer.editor.TSEConnector;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.graph.TSNode;

import java.awt.Color;

public interface INodeDrawEngine extends IDrawEngine
{
   /**
    * Resizes this node, optionally keeping the upper left corner fixed.
   */
   public void resize(int nWidth, int nHeight, boolean bKeepUpperLeftPoint);

   /**
    * Resizes this node, optionally keeping the upper left corner fixed.
   */
   public void resize(IETSize size, boolean bKeepUpperLeftPoint);

   /**
    * Resizes this node to ensure that the given compartment is completely visible.
   */
   public void resizeToFitCompartment(ICompartment pCompartment, boolean bKeepUpperLeftPoint, boolean bIgnorePreferences);

   /**
    * Force the node to the given size in logical coordinates, upper left corner stays fixed
   */
   public void resizeTo(IETSize pSizeNew);

   /**
    * Place a specific type of decoration on the node at the specified location
   */
   public void addDecoration(String sDecorationType, IETPoint pLocation);

   /**
    * Add a connector to the node at the specified offset.
   */
   public TSEConnector addConnector();

   /**
    * Indicate to the draw engine that it is being stretched
   */
   public void stretch(IStretchContext pStretchContext);

   /**
    * Set the shape of this node/edge
   */
   public void setNodeShape(IDrawInfo pDrawInfo);

   /**
    * Access the internal TS node
   */
   public TSENode getNode();

   /**
    * Gets the parent IClassifier if the view implements that interface.
   */
   public IClassifier getParentClassifier();

   /**
    * Returns the attached qualifiers.
   */
   public ETList < IPresentationElement > getAttachedQualifiers();

   /**
    * Selects all the qualifiers.
   */
   public void selectAllAttachedQualifiers(boolean bSelect);

   /**
    * Relocates the qualifiers to the side of this node.
   */
   public void relocateQualifiers(boolean bAutoRouteAssociationEdge);

   /**
    * Hides all the qualifiers.
   */
   public void hideAllAttachedQualifiers(boolean bSelect);

   /**
    * Should the reconnect create a new connector or default to pointing to the center of the node?
   */
   public int getReconnectConnector(IPresentationElement pEdgePE);

   /**
    * Locks editing so double clicks dont activate the edit control.
   */
   public boolean getLockEdit();

   /**
    * Locks editing so double clicks dont activate the edit control.
   */
   public void setLockEdit(boolean value);

   /**
    * Collapses the compartment and resizes the draw engine if necessary
    */
   public boolean collapseCompartment(ICompartment pCompartmentToCollapse, boolean bCollapse);

   /// Helper for those draw engines that use the INameListCompartment.
   public long handleNameListModelElementHasChanged(INotificationTargets pTargets);

   public IETNodeUI getNodeUI();
   public Color getFillColor();

   /*
    * Retuns the Color used to draw the border.
    */
   public Color getBorderColor();
}
