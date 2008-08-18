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

package org.netbeans.modules.uml.integration.netbeans.actions;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.requirementsframework.IRequirement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
//import org.netbeans.modules.uml.requirements.ADRequirementsManager;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


// TODO: meteora is this needed?
public final class AddToDiagramAction extends CookieAction
{
    
    protected void performAction(Node[] nodes)
    {
//        ArrayList<IElement> elements = new ArrayList<IElement>(nodes.length);
//        ArrayList<IProjectTreeItem> reqTreeItems = 
//            new ArrayList<IProjectTreeItem>(nodes.length);
//        
//        for (Node node: nodes)
//        {   
//            IElement ele = (IElement)node.getCookie(IElement.class);
//            
//            if (ele != null)
//                elements.add(ele);
//            
//	    IRequirement req = (IRequirement)node.getCookie(IRequirement.class);
//	    if (req != null) 
//            {
//		IProjectTreeItem item = 
//                    (IProjectTreeItem)node.getCookie(IProjectTreeItem.class);
//                
//		if (item != null) 
//		    reqTreeItems.add(item);
//	    }
//        }
//        
//	if (reqTreeItems.size() != 0) 
//        {
//            TopComponent topComp = org.openide.windows.WindowManager
//                .getDefault().findMode("editor").getSelectedTopComponent(); // NOI18N
//
//            if (topComp != null)
//            {
//                ADDrawingAreaControl daControl = (ADDrawingAreaControl)topComp
//                    .getLookup().lookup(ADDrawingAreaControl.class);
//
//                if (daControl != null)
//                {
//		    ETList<IPresentationElement> satisfiers = 
//                        daControl.getSelected();
//                    
//		    ADRequirementsManager reqMgr = 
//                        ADRequirementsManager.instance();
//		    
//                    if (reqMgr != null) 
//                    { 
//			reqMgr.addSatisfiers(satisfiers, reqTreeItems);		    		
//		    }
//		}
//            }
//	}
//
//        if (elements.size() != 0)
//        {
//            TopComponent topComp = WindowManager.getDefault().
//                  findMode("editor").getSelectedTopComponent(); // NOI18N
//            
//            if (topComp != null)
//            {
//                ADDrawingAreaControl daControl = (ADDrawingAreaControl)topComp
//                    .getLookup().lookup(ADDrawingAreaControl.class);
//
//                if (daControl != null)
//                {
//                   // Fixed issue 103231.
//                   // Check if the Project tab is currently actived. If yes,
//                   // process the actived node and deactivate any selected item on palette
//                   
//                   // Get the currently active top component
//                   TopComponent activeTopComp = WindowManager.getDefault().getRegistry().getActivated();
//                   if (activeTopComp != null && "Projects".equals(activeTopComp.getName()))
//                   {
//                      // if an item on platte was selected, deselected it
//                      String selectedPaletteItem = daControl.getSelectedPaletteButton();
//                      if (selectedPaletteItem != null && selectedPaletteItem.length() > 0 )
//                      {
//                         daControl.setSelectStateOnPalette();
//                         daControl.switchToDefaultState();
//                      }
//                      List<IETGraphObject> selitems = daControl.getSelected3();
//                      
//                      daControl.getGraphWindow().deselectAll(true);
//                      daControl.refresh(true);
//                      
//                      List<IETGraphObject> addedNodes =
//                            daControl.addNodeToCenter(elements);
//                      
//                      if (addedNodes == null || addedNodes.size() == 0)
//                         daControl.selectThese(selitems);
//                      
//                      else
//                         daControl.selectThese(addedNodes);
//                      
//                      daControl.setFocus();
//                      topComp.requestActive();
//                   }
//                }
//            }
//        }
    }
    
    protected int mode()
    {
        return CookieAction.MODE_ANY;
    }
    
    public String getName()
    {
        return "AddToDiagram"; // NOI118N
    }
    
    protected Class[] cookieClasses()
    {
        return new Class[] {
            IElement.class,
	    IRequirement.class
        };
    }
    
    protected void initialize()
    {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous()
    {
        return false;
    }
    
    protected IDiagram selectDiagram()
    {
        return null;
    }
//
//    private void addImportedElements(
//        ADDrawingAreaControl drawingAreaCtrl, ArrayList<IElement> elements)
//    {
//        for (IElement ele: elements)
//        {
//            drawingAreaCtrl.processOnDropElement(ele);
//        }
//    }
}

