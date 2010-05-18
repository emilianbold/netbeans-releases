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

package org.netbeans.modules.uml.drawingarea.actions;

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
import org.netbeans.modules.uml.drawingarea.ui.addins.diagramcreator.DiagCreatorAddIn;
import org.netbeans.modules.uml.project.ui.nodes.UMLModelRootNode;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel;
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
            IElement curElement = curNode.getCookie(IElement.class);
            if(curElement != null)
            {  
                elements.add(curElement);
            }
        }
        
//        Thread creatorThread = new Thread(new Runnable()
//        {
//            public void run()
//            {
                DiagCreatorAddIn creator = new DiagCreatorAddIn(); 
                IProjectTreeModel treeModel = UMLModelRootNode.getProjectTreeModel();
                creator.guiCreateDiagramFromElements(elements, null, treeModel);
//            }
//        });
//        creatorThread.run();
//        Call to run is just a direct execution, commented unnecessary thread creation
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

            // if an operation is selected, then use re operation with the same funtionality
            // in releases <=6.5 cdfs and re operation was there if only one operation was selected.
            if (curElement instanceof IOperation)
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
//                    canCreate = false;
//                    IUMLParsingIntegrator integrator = getUMLParsingIntegrator();
//                    boolean canRE = integrator.canOperationBeREed((IOperation) pElement);
//                    if (canRE)
//                    {
                        canCreate = true;
//                    }
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
