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



package org.netbeans.modules.uml.ui.support.diagramsupport;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;

/**
 * 
 * @author Trey Spiva
 */
public class PresentationFinder implements IPresentationFinder
{

   /**
    * Returns a detailed list of where the presentation elements reside that 
    * represent the argument model element.
    *
    * @param element The model element to look for in the diagram files
    * @return The resulting presentation targets, one for each found element in
    *         a diagram file
    */
   public ETList<IPresentationTarget> getPresentationTargets(IElement element)
   {
      ETList<IPresentationTarget> retVal = new ETArrayList<IPresentationTarget>();
      
      IProduct product = ProductHelper.getProduct();
      
      ETList<IDiagram> openDiagrams  = null;
      IWorkspace         workspace    = null;
      IProductDiagramManager manager  = null;
      if(product != null)
      {
         openDiagrams = product.getAllDrawingAreas();
         workspace    = product.getCurrentWorkspace();
         manager  = ProductHelper.getProductDiagramManager();
         
         if((openDiagrams == null) ||
            (workspace    == null) ||
            (manager      == null))
         {
            // TODO: Add Error Message.
         }
      }
      else
      {
         // TODO: Add Error Message.
      }
      
      long numOpenDiagrams = 0;
      if(openDiagrams != null)
      {
         numOpenDiagrams = openDiagrams.size();
      }
      
      // Now that we have the open diagrams ask them for the presentation elements that make up
      // the input model elements.
      for(long index = 0L; index < numOpenDiagrams; index++)
      {
         IDiagram curDiagram = openDiagrams.get((int)index);
         if((curDiagram != null) && (curDiagram instanceof IUIDiagram))
         {
            IUIDiagram axDiagram = (IUIDiagram)curDiagram;
            IDrawingAreaControl control = axDiagram.getDrawingArea();
            
            if(control != null)
            {
               ETList<IPresentationElement> presElements = curDiagram.getAllItems2(element);
               
               long numPresentationItems = 0;
               if(presElements != null)
               {
                  numPresentationItems = presElements.size();
               }
               
               // Don't include labels in the list.
               for(int presIndex = 0; presIndex < numPresentationItems; presIndex++)
               {
                  IPresentationElement curElement = presElements.get(presIndex);
                  if( !(curElement instanceof ILabelPresentation) )
                  {
                     IPresentationTarget target = new PresentationTarget();
                     target.setPresentationID(curElement.getXMIID());
                     target.setDiagramFilename(control.getFilename());
                     target.setOpenDiagram(curDiagram);
                     
                     retVal.add(target);
                  }
               }
            }
         }
      }
      
      // Now go through the closed diagrams looking for the model element id.
      // TS (ISSUE 6274443) The following line of code was making sure that we  
      //             had a valid workspace before checking the closed diagrams.  
      //             After moving to the NetBeans project system we no longer
      //             use the Describe workspace management mechanisms.  So, 
      //             we will never have an IWorkspace instance.
      //if(workspace != null)
      {
         IProxyDiagramManager proxyManager = ProxyDiagramManager.instance();
         ETList<IProxyDiagram> diagrams = proxyManager.getDiagramsInWorkspace();
         if (diagrams != null)
         {
         	int count = diagrams.size();
         	for (int i=0; i<count; i++)
         	{
         		IProxyDiagram proxyDia = diagrams.get(i);
         		IDiagram dia = proxyDia.getDiagram();
         		if (dia == null)
         		{
         			String filename = proxyDia.getFilename();
         			ETList<IPresentationTarget> targets = getPresentationTargetsFromClosedDiagram(element, filename);
         			if (targets != null)
         			{
						retVal.addAll((Collection)targets);
         			}
         		}
         	}
         }
         
      }
      
      return retVal;
   }

   /**
	* Looks into the .etlp file for presentation elements that represent the queried model element.
	*
	* @param pModelElement [in] The model element to look for in the diagram files
	* @param tomFilename [in] The full patch filename for the diagram file
	* @param pVal [in,out] The resulting presentation targets, one for each found element in a diagram file
	*/
	public ETList<IPresentationTarget> getPresentationTargetsFromClosedDiagram(IElement pModelEle, String filename)
	{
		ETList<IPresentationTarget> retObj = null;
		if (filename != null && filename.length() > 0)
		{
			IProxyDiagramManager mgr = ProxyDiagramManager.instance();
			retObj = mgr.getPresentationTargetsFromClosedDiagram(pModelEle, filename);
		}
		return retObj;
	}
}
