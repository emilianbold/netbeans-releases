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



package org.netbeans.modules.uml.ui.support;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IReference;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.IPresentationTarget;
import org.netbeans.modules.uml.ui.support.diagramsupport.PresentationFinder;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import java.util.Arrays;

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
               diagram.centerPresentationElement2(target.getPresentationID(), true, true);
            }
            else if (meid != null && meid.length() > 0 && toplevelId != null && toplevelId.length() > 0)
            {
               centerOnME(diagram, toplevelId, meid);
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
                  openDia.centerPresentationElement2(presId, true, true);
               }
               else if (topLevelId != null && topLevelId.length() > 0 && meid != null && meid.length() > 0)
               {
                  centerOnME(openDia, meid, topLevelId);
               }
            }
         }
      }
   }

   /**
    * Centers on the first PE of a model element
    */
   private void centerOnME(IDiagram openDia, String meid, String topLevelId)
   {
      if (topLevelId != null && topLevelId.length() > 0 && meid != null && meid.length() > 0)
      {
         // The presentation target comes from a stub diagram so the presentation elements were
         // not available - only the model element.
         IDrawingAreaControl control = null;
         if (openDia != null && openDia instanceof IUIDiagram)
         {
            control = ((IUIDiagram) openDia).getDrawingArea();
         }
         if (control != null)
         {
            ETList < IPresentationElement > pPEs = control.getAllItems(topLevelId, meid);
            if (pPEs != null)
            {
               int count = pPEs.size();
               if (count > 0)
               {
                  IPresentationElement pPE = pPEs.get(0);
                  control.centerPresentationElement(pPE, true, true);
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
