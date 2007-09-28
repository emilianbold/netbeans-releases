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
