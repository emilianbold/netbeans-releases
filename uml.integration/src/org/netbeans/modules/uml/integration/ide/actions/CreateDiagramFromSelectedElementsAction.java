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

package org.netbeans.modules.uml.integration.ide.actions;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationship;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IDerivationClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.structure.ISourceFileArtifact;
import org.netbeans.modules.uml.core.reverseengineering.reintegration.IUMLParsingIntegrator;
import org.netbeans.modules.uml.core.reverseengineering.reintegration.UMLParsingIntegrator;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.netbeans.modules.uml.ui.addins.diagramcreator.DiagCreatorAddIn;


import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public final class CreateDiagramFromSelectedElementsAction extends CookieAction
{
    
    protected void performAction(Node[] activatedNodes)
    {
        final ETArrayList < IElement > elements = new ETArrayList < IElement >();
        for(Node curNode : activatedNodes)
        {
            IElement curElement = (IElement)curNode.getCookie(IElement.class);
            if(curElement != null)
            {
                elements.add(curElement);
            }
        }
        
        Thread creatorThread = new Thread(new Runnable()
        {
            public void run()
            {
                DiagCreatorAddIn creator = new DiagCreatorAddIn();
                creator.guiCreateDiagramFromElements(elements, null, null);
            }
        });
        creatorThread.run();
    }
    
    protected int mode()
    {
        //return CookieAction.MODE_ANY;
        return CookieAction.MODE_ALL;
    }
    
    public String getName()
    {
        return NbBundle.getMessage(
            CreateDiagramFromSelectedElementsAction.class, 
            "CTL_CreateDiagramFromSelectedElementsAction"); // NOI18N
    }
    
    protected Class[] cookieClasses()
    {
        return new Class[] {
            IElement.class
        };
    }
    
    protected String iconResource()
    {
        return ImageUtil.IMAGE_FOLDER + "cdfs.png"; // NOI18N
    }
    
    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous()
    {
        return false;
    }
    
    protected boolean enable(Node[] nodes)
    {
        boolean retVal = false;
        
        for (Node curNode: nodes)
        {
            IElement curElement = (IElement)curNode.getCookie(IElement.class);

            // if an operation is selected, 
            // then there can only be one selected element
            if (curElement instanceof IOperation && nodes.length > 1)
                retVal = false;

            else if (canDiagramBeCreatedFromElement(curElement))
                retVal = true;
            
            
            // Basically if frist node that fails the test will cause the action
            // to be disabled.
            if (!retVal)
                break;
        } // for
        
        return retVal;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Helper Methods
    
    /**
     * Determines if CDFS is able to create a diagram from the element
     */
    protected boolean canDiagramBeCreatedFromElement(IElement pElement)
    {
        boolean canCreate = false;
        if (pElement != null)
        {
            // fix for CR 6417670
            // IDerivationClassifier is also an instance of IRelationship, so return false only if
            // pElement is not of type IDerivationClassifier.
            if (pElement instanceof IDiagram ||
                pElement instanceof ISourceFileArtifact ||
                (pElement instanceof IRelationship && !(pElement instanceof IDerivationClassifier)) )
            {
                return false;
            }
            
            
            // We shouldn't be able to select elements under an interaction and CDFS.
            // Therefore, we should disable the CDFS menu if you select any children
            // of the interaction.
            IElement owner = pElement.getOwner();
            if (owner == null)
                return false;
            if (owner instanceof IInteraction ||
                pElement instanceof IMessage ||
                pElement instanceof ICombinedFragment ||
                pElement instanceof IOperation ||
                pElement instanceof IAttribute)
            {
                // For operations, we have to make sure the operation can be REed
                // in order to support CDFS
                if (pElement instanceof IOperation)
                {
                    canCreate = false;
                    IUMLParsingIntegrator integrator = getUMLParsingIntegrator();
                    boolean canRE = integrator.canOperationBeREed((IOperation) pElement);
                    if (canRE)
                    {
                        canCreate = true;
                    }
                }
            }
            else
            {
                canCreate = true;
            }
        }
        return canCreate;
    }
    
    
    private IUMLParsingIntegrator getUMLParsingIntegrator()
    {
        return new UMLParsingIntegrator();
    }
}
