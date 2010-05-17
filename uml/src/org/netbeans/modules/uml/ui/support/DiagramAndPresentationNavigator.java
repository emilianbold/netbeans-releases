/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.awt.Frame;
import java.util.Iterator;

import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.ui.support.commondialogs.INavigationDialog;
import org.netbeans.modules.uml.ui.support.diagramsupport.IPresentationTarget;

/**
 *
 * @author Trey Spiva
 */
public class DiagramAndPresentationNavigator implements IDiagramAndPresentationNavigator
{   
   private boolean m_ForceDialogDisplay = false;
   
   public boolean navigateToPresentationTarget(IElement             parentModelElement,
                                               ETList<IPresentationTarget> possibleTargets, 
                                               boolean              isShift)
   {
      boolean retVal = false;
     
      IProject project = null;
      if (parentModelElement instanceof IProject)
      {
         project = (IProject)parentModelElement;         
      }
           
      boolean didNavigate = navigateToAssociated(parentModelElement,
                                                 isShift,
                                                 project);
      
      if(didNavigate == false)
      {
         DiagramBuilder diagramBuilder = new DiagramBuilder();
         
        ETList < IProxyDiagram > diagrams = null;
        int numDiagrams  = 0;
		// get the number of scoped diagrams that this element has
        diagrams = diagramBuilder.getScopedDiagrams(parentModelElement);
        if(diagrams != null)
        {
           numDiagrams = diagrams.size();
        }
         
		ETList < IProxyDiagram > assocDiagrams = null;
		int numAssocDiagrams  = 0;
		// get the number of associated diagrams this element has
		assocDiagrams = diagramBuilder.getAssociatedDiagrams(parentModelElement);
		if(assocDiagrams != null)
		{
		   numAssocDiagrams = assocDiagrams.size();
		}

		// now get the number of presentation elements this element has, unless we have a project
         if (project == null)
         {
			ETList<IPresentationTarget> targets = possibleTargets;
			if(possibleTargets == null)
			{
			   targets = diagramBuilder.getPresentationTargets(parentModelElement);
			}
			
			int numPEs = 0;
			if (targets != null)
			{
				numPEs = targets.size(); 
			}
         	
         	ETList<IElement> pElements = diagramBuilder.getAssociatedElements(parentModelElement);
         	int numAssocElements = 0;
         	if (pElements != null)
         	{
         		numAssocElements = pElements.size();
         	}
         	
			int numTargets = numDiagrams + numAssocDiagrams + numPEs + numAssocElements;
         	
			if((numTargets == 1) && (isShift == false))
			{
			   if(numDiagrams > 0)
			   {
				  diagramBuilder.navigateToFirstDiagram(diagrams);
			   }            
			   else if(numPEs > 0)
			   {
				  diagramBuilder.navigateToFirstTarget(targets);
			   }
			   else if (numAssocDiagrams > 0)
			   {
			   	diagramBuilder.navigateToFirstDiagram(assocDiagrams);
			   }
			   else if (numAssocElements > 0)
			   {
			   		IElement pElem = pElements.get(0);
			   		diagramBuilder.navigateToElementInTree(pElem);
			   }
			}
			else if(numTargets > 1)
			{
				// Show the dialog.
				INavigationDialog dialog = UIFactory.createNavigationDialog();
			  	dialog.display(parentModelElement, diagrams, targets, assocDiagrams, pElements);
			}
         }
         else
         {
			// we have a project
         	if (numDiagrams == 1 && !isShift && !m_ForceDialogDisplay)
         	{
         		diagramBuilder.navigateToFirstDiagram(diagrams);
         	}
         	else if (numDiagrams != 0)
         	{
				// Show the dialog.
				INavigationDialog dialog = UIFactory.createNavigationDialog();
				dialog.display(parentModelElement, diagrams, null, assocDiagrams, null);
         	}
         }
      }
      
      return retVal;
   }
   
   /* (non-Javadoc)
    */
   public boolean navigateToPresentationTarget(int pParent, IElement pParentModelElement, ETList<IPresentationTarget> pPossibleTargets)
   {
   		return navigateToPresentationTarget(pParentModelElement, pPossibleTargets, false);
   }
   /* (non-Javadoc)
    */
   public boolean handleNavigation(int pParent, IElement pElement, boolean isShift)
   {
      return navigateToPresentationTarget(pElement, null, isShift);
   }
   
   /**
	* Bring up the project diagram dialog which shows all the closed diagrams in the project.
	*
	* @param pParent[in]
	* @param pCurrentProject[in]
	*/
   public void showScopedDiagrams(Frame pParent, IProject pCurrentProject)
   {
   		m_ForceDialogDisplay = true;
   		navigateToPresentationTarget(pCurrentProject, null, false);
   		m_ForceDialogDisplay = false;
   }
   
   /**
	* Navigates to the argument target.  If the diagram is already up 
	* the it makes sure to raise the window.
	*
	* @param pParent[in]
	* @param pCurrentProject[in]
	*/
   public void doNavigate(IPresentationTarget pTarget)
   {
   		DiagramBuilder builder = new DiagramBuilder();
   		builder.navigateToTarget(pTarget);
   }
   
   //**************************************************
   // Helper Methods
   //**************************************************
   protected boolean navigateToAssociated(IElement parentModelElement,
                                          boolean  isShift,
                                          IProject project)
   {
      boolean retVal = false;
      
      boolean onlyShowWhenShiftIsDown = false;
      boolean hasDefault              = false;
      String  xmid                    = "";
      UserSettings settings = new UserSettings();
      
      if(project == null)
      {  
         onlyShowWhenShiftIsDown = settings.isOnlyShowNavigateWhenShift(parentModelElement);
         hasDefault = settings.hasDefaultNavigationTarget(parentModelElement);    
         xmid       = settings.getDefaultNavigationTargetXMID(parentModelElement);   
      }
      
      if( (hasDefault == true) && (onlyShowWhenShiftIsDown == true) && (isShift == false) )
      {
		DiagramBuilder builder = new DiagramBuilder();

		// Navigate to the default target
		// are we navigating to a diagram
         if(settings.isDefaultNavigationTargetADiagram(parentModelElement) == true)
         {
			// now we find out if the passed in diagram id matches an id in the scoped
			// diagrams of the element
         	ETList<IProxyDiagram> diags = builder.getScopedDiagrams(parentModelElement);
         	if (diags != null)
         	{
         		IProxyDiagram proxyDia = getProxyDiagramWithID(diags, xmid);
         		if (proxyDia != null)
         		{
         			String filename = proxyDia.getFilename();
         			if (filename != null && filename.length() > 0)
         			{
         				builder.navigateToDiagram(filename, "", "", "");
         				retVal = true;
         			}
         		}
         	}
         	
			// must not have been a scoped diagram, so the other would be an associated
			// diagram
         	if (!retVal)
         	{
         		ETList<IProxyDiagram> pDiagrams = builder.getAssociatedDiagrams(parentModelElement);
         		if (pDiagrams != null)
         		{
					IProxyDiagram proxyDia = getProxyDiagramWithID(pDiagrams, xmid);
					if (proxyDia != null)
					{
						String filename = proxyDia.getFilename();
						if (filename != null && filename.length() > 0)
						{
							builder.navigateToDiagram(filename, "", "", "");
							retVal = true;
						}
					}
         		}
         	}
         }
         else
         {
			// the id that we are looking for is not a diagram, so is it among
			// the presentation elements for this element
            ETList<IPresentationTarget> targets = builder.getPresentationTargets(parentModelElement);
            if(targets != null)
            {
               IPresentationTarget thisTarget = getTargetWithID(targets, xmid);
               //IPresentationTarget thisTarget = targets.get(xmid);
               if(thisTarget != null)
               {
                  builder.navigateToTarget(thisTarget);
                  retVal = true;
               }
            }
            
			// not a presentation element either, so one final check to see if it
			// is an associated element
            if (!retVal)
            {
            	ETList<IElement> pElements = builder.getAssociatedElements(parentModelElement);
            	if (pElements != null)
            	{
            		int count = pElements.size();
            		IElement pElem = null;
            		for (int i=0; i<count; i++)
            		{
            			IElement tempEle = pElements.get(i);
            			String id = tempEle.getXMIID();
            			if (id != null && id.equals(xmid))
            			{
            				pElem = tempEle;
            				break;
            			}
            		}
            		if (pElem != null)
            		{
            			builder.navigateToElementInTree(pElem);
            			retVal = true;
            		}
            	}
            }
         }
         
         // If we navigated ok then don't bring up the dialog, else clear out 
         // the registry and bring up the dialog
         if(retVal == false)
         {
            settings.clearDefaultTarget(parentModelElement);
         }
      }
      return retVal;
   }
   
   protected IPresentationTarget getTargetWithID(ETList < IPresentationTarget > targets,
                                                 String id)
   {
      IPresentationTarget retVal = null;
      for (Iterator < IPresentationTarget > iter = targets.iterator();
           (iter.hasNext() == true) && (retVal == null);)
      {
         IPresentationTarget element = iter.next();
         if(id.equals(element.getPresentationID()))
         {
            retVal = element;
            break;
         }
      }
      return retVal;
   }
   
   protected IProxyDiagram getProxyDiagramWithID(ETList < IProxyDiagram > targets, String id)
   {
	  IProxyDiagram retVal = null;
	  for (Iterator < IProxyDiagram > iter = targets.iterator();
		   (iter.hasNext() == true) && (retVal == null);)
	  {
		 IProxyDiagram element = iter.next();
		 if (id != null && id.equals(element.getXMIID()))
		 {
			retVal = element;
			break;
		 }
	  }
	  return retVal;
   }
   
}
