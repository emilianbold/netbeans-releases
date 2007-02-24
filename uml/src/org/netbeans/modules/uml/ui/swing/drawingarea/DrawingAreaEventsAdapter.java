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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData;


/**
 *
 * @author Trey Spiva
 */
public class DrawingAreaEventsAdapter implements IDrawingAreaEventsSink
{

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreCreated(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaPreCreated(IDrawingAreaControl pDiagramControl, IResultCell cell)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostCreated(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaPostCreated(IDrawingAreaControl pDiagramControl, IResultCell cell)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaOpened(com.embarcadero.describe.diagrams.IDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaOpened(IDiagram pParentDiagram, IResultCell cell)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaClosed(com.embarcadero.describe.diagrams.IDiagram, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaClosed(IDiagram pParentDiagram, boolean bDiagramIsDirty, IResultCell cell)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreSave(com.embarcadero.describe.diagrams.IProxyDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaPreSave(IProxyDiagram pParentDiagram, IResultCell cell)
   {
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostSave(com.embarcadero.describe.diagrams.IProxyDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaPostSave(IProxyDiagram pParentDiagram, IResultCell cell)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaKeyDown(com.embarcadero.describe.diagrams.IDiagram, int, boolean, boolean, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaKeyDown(IDiagram pParentDiagram, int nKeyCode, boolean bControlIsDown, boolean bShiftIsDown, boolean bAltIsDown, IResultCell cell)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPrePropertyChange(com.embarcadero.describe.diagrams.IProxyDiagram, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaPrePropertyChange(IProxyDiagram pProxyDiagram, int nPropertyKindChanged, IResultCell cell)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostPropertyChange(com.embarcadero.describe.diagrams.IProxyDiagram, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaPostPropertyChange(IProxyDiagram pProxyDiagram, int nPropertyKindChanged, IResultCell cell)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaTooltipPreDisplay(com.embarcadero.describe.diagrams.IDiagram, com.embarcadero.describe.foundation.IPresentationElement, org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaTooltipPreDisplay(IDiagram pParentDiagram, IPresentationElement pPE, IToolTipData pTooltip, IResultCell cell)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaActivated(com.embarcadero.describe.diagrams.IDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaActivated(IDiagram pParentDiagram, IResultCell cell)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreDrop(com.embarcadero.describe.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaPreDrop(IDiagram pParentDiagram, IDrawingAreaDropContext pContext, IResultCell cell)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostDrop(com.embarcadero.describe.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaPostDrop(IDiagram pParentDiagram, IDrawingAreaDropContext pContext, IResultCell cell)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreFileRemoved(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaPreFileRemoved(String sFilename, IResultCell cell)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaFileRemoved(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaFileRemoved(String sFilename, IResultCell cell)
   {
      
   }

}
