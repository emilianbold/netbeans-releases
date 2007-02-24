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

package org.netbeans.modules.uml.integration.netbeans.actions;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.requirementsframework.IRequirement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.requirements.ADRequirementsManager;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADDrawingAreaControl;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.windows.TopComponent;

public final class AddToDiagramAction extends CookieAction
{
    
    protected void performAction(Node[] nodes)
    {
        ArrayList<IElement> elements = new ArrayList<IElement>(nodes.length);
        
        ArrayList<IProjectTreeItem> reqTreeItems = 
            new ArrayList<IProjectTreeItem>(nodes.length);
        
        for (Node node: nodes)
        {
            IElement ele = (IElement)node.getCookie(IElement.class);
            
            if (ele != null)
                elements.add(ele);
            
	    IRequirement req = (IRequirement)node.getCookie(IRequirement.class);
	    if (req != null) 
            {
		IProjectTreeItem item = 
                    (IProjectTreeItem)node.getCookie(IProjectTreeItem.class);
                
		if (item != null) 
		    reqTreeItems.add(item);
	    }

        }
        
	if (reqTreeItems.size() != 0) 
        {

            TopComponent topComp = org.openide.windows.WindowManager
                .getDefault().findMode("editor").getSelectedTopComponent(); // NOI18N

            if (topComp != null)
            {
                ADDrawingAreaControl daControl = (ADDrawingAreaControl)topComp
                    .getLookup().lookup(ADDrawingAreaControl.class);

                if (daControl != null)
                {
		    ETList<IPresentationElement> satisfiers = 
                        daControl.getSelected();
                    
		    ADRequirementsManager reqMgr = 
                        ADRequirementsManager.instance();
		    
                    if (reqMgr != null) 
                    { 
			reqMgr.addSatisfiers(satisfiers, reqTreeItems);		    		
		    }
		}
            }
	}

        if (elements.size() != 0)
        {
            TopComponent topComp = org.openide.windows.WindowManager
                .getDefault().findMode("editor").getSelectedTopComponent(); // NOI18N

            if (topComp != null)
            {
                ADDrawingAreaControl daControl = (ADDrawingAreaControl)topComp
                    .getLookup().lookup(ADDrawingAreaControl.class);

                if (daControl != null)
                {
                    // addImportedElements mehtod does nothting but calls the method 
                    // processOnDropElement(IElement) for each element.
                    // A better place to call processOnDropElement(IElement) is right before 
                    // we add the element on the diagram. Hence commenting this line.
                     
                    //addImportedElements(daControl, elements);

                    List<IETGraphObject> selitems = daControl.getSelected3();

                    daControl.getGraphWindow().deselectAll(true);
                    daControl.refresh(true);
                    
                    List<IETGraphObject> addedNodes = 
                        daControl.addNodeToCenter(elements);
                    
                    if (addedNodes == null || addedNodes.size() == 0)
                        daControl.selectThese(selitems);
                    
                    else
                        daControl.selectThese(addedNodes);

                    daControl.refresh(true);
                    daControl.setFocus();
                }
            }
        }
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

    private void addImportedElements(
        ADDrawingAreaControl drawingAreaCtrl, ArrayList<IElement> elements)
    {
        for (IElement ele: elements)
        {
            drawingAreaCtrl.processOnDropElement(ele);
        }
    }
}

