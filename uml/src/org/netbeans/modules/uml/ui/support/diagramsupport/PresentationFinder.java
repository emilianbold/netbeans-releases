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



package org.netbeans.modules.uml.ui.support.diagramsupport;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

//import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.support.ProductHelper;
//import org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

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
      if(product != null)
      {
         openDiagrams = product.getAllDrawingAreas();
         workspace    = product.getCurrentWorkspace();
         
         if((openDiagrams == null) ||
            (workspace    == null))
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
       for (long index = 0L; index < numOpenDiagrams; index++)
       {
           IDiagram curDiagram = openDiagrams.get((int) index);
           if (curDiagram != null)
           {
               ETList<IPresentationElement> presElements = curDiagram.getAllItems(element);

               long numPresentationItems = 0;
               if (presElements != null)
               {
                   numPresentationItems = presElements.size();
               }

               // Don't include labels in the list.
               for (int presIndex = 0; presIndex < numPresentationItems; presIndex++)
               {
                   IPresentationElement curElement = presElements.get(presIndex);
                   IPresentationTarget target = new PresentationTarget();
                   target.setPresentationID(curElement.getXMIID());
                   target.setDiagramFilename(curDiagram.getFilename());
                   target.setOpenDiagram(curDiagram);

                   retVal.add(target);
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
