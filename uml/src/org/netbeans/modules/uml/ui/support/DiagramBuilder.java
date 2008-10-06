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



package org.netbeans.modules.uml.ui.support;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IReference;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.IPresentationTarget;
import org.netbeans.modules.uml.ui.support.diagramsupport.PresentationFinder;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;

/**
 * Theres lots of common code among the various dialogs.  This class helps the 
 * dialogs build and maintain their various lists.  It also can perform some 
 * various actions.
 * 
 * @author Trey Spiva
 */
public class DiagramBuilder
{
   /**
    * Retrieve the presentation targets for the element.
    * 
    * @param element The element to display.
    * @return The presentation targets.
    */
   public ETList < IPresentationTarget > getPresentationTargets(IElement element)
   {
      ETList < IPresentationTarget > retVal = new ETArrayList < IPresentationTarget > ();

      PresentationFinder finder = new PresentationFinder();

      // Projects do not have presentation element.  So, make sure that we
      // do not have a project.
      if (!(element instanceof IProject))
      {
         retVal = finder.getPresentationTargets(element);
      }
      return retVal;
   }

   /**
    * Navigates to the presentation element.
    * 
    * @param thisTarget The presentation element to navigate to.
    */
   public void navigateToTarget(IPresentationTarget target)
   {
      IProductDiagramManager manager = ProductHelper.getProductDiagramManager();
      if (manager != null && target != null)
      {
         IDiagram diagram = target.getOpenDiagram();
         if (diagram != null)
         {
            manager.raiseWindow(diagram);
         }
         else
         {
            diagram = manager.openDiagram(target.getDiagramFilename(), true, null);
         }

         if (diagram != null)
         {
            String presentationId = target.getPresentationID();
            String toplevelId = target.getTopLevelID();
            String meid = target.getModelElementID();
            if (presentationId != null && presentationId.length() > 0)
            {
               diagram.centerPresentationElement(target.getPresentationID(), true, true);
            }
         }
      }
   }

   /**
   * Get the scoped diagrams for this element
   */
   public ETList < IProxyDiagram > getScopedDiagrams(IElement element)
   {
      IProxyDiagramManager manager = ProxyDiagramManager.instance();

      ETList < IProxyDiagram > diagrams = null;
      if (element instanceof IProject)
      {
         IProject project = (IProject) element;
         diagrams = manager.getDiagramsInProject(project);
      }
      else if (element instanceof INamespace)
      {
         INamespace space = (INamespace) element;
         diagrams = manager.getDiagramsInNamespace(space);
      }

      //      ETList < IProxyDiagram > retVal = null;
      //      if((diagrams != null) && (diagrams.length > 0))
      //      {
      //         retVal = new ETArrayList < IProxyDiagram >();
      //         retVal.addAll(Arrays.asList(diagrams));
      //      }

      return diagrams;
   }

   /**
   * Navigate to the first PE
   */
   public void navigateToFirstTarget(ETList < IPresentationTarget > targets)
   {
      if (targets != null && targets.size() > 0)
      {
         IPresentationTarget pTarget = targets.get(0);
         navigateToTarget(pTarget);
      }
   }

   /**
   * Navigate to the first diagram
   */
   public void navigateToFirstDiagram(ETList < IProxyDiagram > diagrams)
   {
      if (diagrams != null && diagrams.size() > 0)
      {
         IProxyDiagram pDiagram = diagrams.get(0);
         IProductDiagramManager pDiaMgr = ProductHelper.getProductDiagramManager();
         if (pDiaMgr != null)
         {
            IDiagram openedDia = pDiaMgr.openDiagram2(pDiagram, true, null);
            if (openedDia != null)
            {
               pDiaMgr.raiseWindow(openedDia);
            }
         }
      }
   }

   /**
   * Navigates to the diagram.
   *
   * If a presentation element xmiid is provided then that pe will be selected in the diagram
   * If a toplevel and modelelement xmiid is provided then the first PE for that ME is selected.
   *
   * @param sDefaultDiagramFilename [in] The full filename for the diagram to open
   * @param sPresentationXMIID [in] Optional presentation element xmiid.
   * @param sTopLevelXMIID [in] Optional toplevel element xmiid.
   * @param sModelElementXMIID [in] Optional model element xmiid.
   */
   public void navigateToDiagram(String fileName, String presId, String topLevelId, String meid)
   {
      if (fileName != null && fileName.length() > 0)
      {
         IProductDiagramManager diaMgr = ProductHelper.getProductDiagramManager();
         if (diaMgr != null)
         {
            IDiagram openDia = diaMgr.openDiagram(fileName, true, null);
            if (openDia != null)
            {
               diaMgr.raiseWindow(openDia);
               if (presId != null && presId.length() > 0)
               {
                  //openDia.centerPresentationElement(presId, true, true);
               }
            }
         }
      }
   }

   /**
    * Navigate to the element in the project tree
    */
   public void navigateToElementInTree(IElement pElement)
   {
      IProduct prod = ProductHelper.getProduct();
      if (prod != null)
      {
         IProjectTreeControl pTree = prod.getProjectTree();
         if (pTree != null)
         {
            pTree.findAndSelectInTree(pElement);
         }
      }
   }

   /**
    * Get the associated elements for this element
    */
   public ETList < IElement > getAssociatedElements(IElement pElement)
   {
      if (pElement != null)
      {
         ETList < IElement > retElems = new ETArrayList < IElement > ();

         ETList < IReference > pReferences = pElement.getReferencingReferences();
         if (pReferences != null)
         {
            int count = pReferences.size();
            for (int i = 0; i < count; i++)
            {
               IReference pRef = pReferences.get(i);
               IElement pEle = pRef.getReferredElement();
               if (pEle != null)
               {
                  retElems.add(pEle);
               }
            }
         }
         return retElems.size() > 0 ? retElems : null;
      }
      return null;
   }

   /**
    * Get the associated diagrams for this element
    */
   public ETList < IProxyDiagram > getAssociatedDiagrams(IElement pElement)
   {
      ETList < IProxyDiagram > retDias = null;
      if (pElement != null)
      {
         IProxyDiagramManager diaMgr = ProxyDiagramManager.instance();
         retDias = diaMgr.getAssociatedDiagramsForElement(pElement);
      }
      return retDias;
   }

}
