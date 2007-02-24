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


// Workfile. SmartDragHelper.java
// Revision. 1
//   Author. treys
//     Date. Feb 10, 2004 7:27:50 AM
//  Modtime. Feb 10, 2004 7:27:50 AM

package org.netbeans.modules.uml.ui.products.ad.compartments;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.ETLifelineCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.LifelineConnectorLocation;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.ConnectorPiece;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.LifelineCompartmentPiece;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.RecursiveHelper;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.SmartDragTool;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.graph.TSEdge;
import com.tomsawyer.graph.TSGraphObject;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import com.tomsawyer.util.TSObject;

/**
 
 @author Trey Spiva
 */
public class SmartDragHelper extends RecursiveHelper
{
   /**
    * @param name
    */
   public SmartDragHelper(String name)
   {
      super(name);
   }
   
   /**
    Adds the edges and nodes associated with this piece to
    the list of affected tsgraphobjects on the smart draw tool
    */
   public void layoutAffected( final String strName, TSConstPoint startPoint, LifelineCompartmentPiece pPiece )
   {  
      if (isOkToUsePiece( strName, pPiece ))
      {
         // Tell the smart drag tool about the change
         IDiagram pDiagram = pPiece.getDiagram();
         if (pDiagram != null)
         {               
            if(pDiagram instanceof IUIDiagram)
            {
               IUIDiagram pAxDiagram = (IUIDiagram)pDiagram;
               IDrawingAreaControl pControl = pAxDiagram.getDrawingArea();

               if (pControl != null)
               {
                  TSEGraphWindow pGraphEditor = pControl.getGraphWindow();
                  if (pGraphEditor != null)
                  {
                     SmartDragTool pSmartDragTool = new SmartDragTool(startPoint, pControl, true);
                     
                     // Get the node this piece is attached to
                     TSObject pTSObject = null;
                     IDrawEngine pDrawEngine = null;
                     ETLifelineCompartment pCompartmentImpl = pPiece.getParent();
                     if (pCompartmentImpl != null)
                     {
                        pDrawEngine = pCompartmentImpl.getEngine();
                        if (pDrawEngine != null)
                        {
                           pTSObject = TypeConversions.getTSObject(pDrawEngine);
                        }
                     }

                     TSGraphObject pTSOurNode = (TSGraphObject)pTSObject;
                     pSmartDragTool.addDraggingTSGraphObject(pTSOurNode);

                     // Now go over all 4 corners of the piece and get any attached edges
                     if (pPiece instanceof ConnectorPiece)
                     {
                        ConnectorPiece pConnectorPiece = (ConnectorPiece)pPiece;
                      
                        for (int i = 0; i <= LifelineConnectorLocation.LCL_BOTTOMLEFT; i++)
                        {
                           TSEdge pTSEdge = pConnectorPiece.getAttachedEdge(i);
                           if(pTSEdge != null)
                           {
                              pSmartDragTool.addDraggingTSGraphObject(pTSEdge);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
      
   }
}
