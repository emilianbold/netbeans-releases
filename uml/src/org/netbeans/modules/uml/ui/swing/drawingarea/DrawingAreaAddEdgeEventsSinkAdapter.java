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

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 * An adapter class for the IDrawAreaEdgeEventsSink.
 *
 * @author Trey Spiva
 * @see IDrawAreaEdgeEventsSink
 */
public class DrawingAreaAddEdgeEventsSinkAdapter implements IDrawingAreaAddEdgeEventsSink
{

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddEdgeEventsSink#onDrawingAreaStartingEdge(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaStartingEdge(IDiagram pParentDiagram, IEdgeCreateContext pContext, IResultCell cell)
   {

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddEdgeEventsSink#onDrawingAreaEdgeShouldCreateBend(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateBendContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaEdgeShouldCreateBend(IDiagram pParentDiagram, IEdgeCreateBendContext pContext, IResultCell cell)
   {

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddEdgeEventsSink#onDrawingAreaEdgeMouseMove(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeMouseMoveContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaEdgeMouseMove(IDiagram pParentDiagram, IEdgeMouseMoveContext pContext, IResultCell cell)
   {

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddEdgeEventsSink#onDrawingAreaFinishEdge(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaFinishEdge(IDiagram pParentDiagram, IEdgeFinishContext pContext, IResultCell cell)
   {

   }

}
