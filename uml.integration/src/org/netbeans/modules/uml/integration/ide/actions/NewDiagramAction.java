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

package org.netbeans.modules.uml.integration.ide.actions;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.project.ui.nodes.UMLLogicalViewCookie;
import org.netbeans.modules.uml.project.ui.nodes.UMLPhysicalViewProvider;
import org.netbeans.modules.uml.project.ui.nodes.actions.NewDiagramType;
import java.io.IOException;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.ErrorManager;

public final class NewDiagramAction extends CookieAction
{
    
    protected void performAction(Node[] activatedNodes)
    {
        UMLLogicalViewCookie c = (UMLLogicalViewCookie)activatedNodes[0]
            .getCookie(UMLLogicalViewCookie.class);
        
        IProjectTreeItem item = (IProjectTreeItem)activatedNodes[0]
            .getCookie(IProjectTreeItem.class);
        
        if (c!=null) // UML project node
        {
            Node node = c.getModelRootNode();
            NewDiagramType newType = new NewDiagramType(node);
            try
            {
                newType.create();
                return;
            }
            catch (IOException e)
            {
                ErrorManager.getDefault().notify(e);
            }
        }
        
        if (item != null)
        {
            IElement element = item.getModelElement();
            if (element==null && item.isDiagram()) // could be diagram node
            {
                element = item.getDiagram().getProject();
            }
            NewDiagramType newType = new NewDiagramType(element);
            try
            {
                newType.create();
            }
            catch (IOException e)
            {
                ErrorManager.getDefault().notify(e);
            }
        }
        else  // UML model root node or diagrams root node
        {
            Object obj = activatedNodes[0].getLookup().lookup(UMLProject.class);
            if (obj!=null)
            {
                UMLPhysicalViewProvider provider = 
                    (UMLPhysicalViewProvider)((UMLProject)obj).getLookup()
                    .lookup(UMLPhysicalViewProvider.class);
                
                if (provider== null)
                    return;
                
                Node node = provider.getModelRootNode();
                NewDiagramType newType = new NewDiagramType(node);
                try
                {
                    newType.create();
                }
                catch (IOException e)
                {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
    }
    
    protected int mode()
    {
        return CookieAction.MODE_EXACTLY_ONE;
    }
    
    public String getName()
    {
        return NbBundle.getMessage(NewDiagramAction.class, "CTL_NewDiagramAction"); // NOI18N
    }
    
    protected Class[] cookieClasses()
    {
        return new Class[] {
            UMLProject.class,
            IProjectTreeItem.class
        };
    }
    
    protected String iconResource()
    {
        return ImageUtil.IMAGE_FOLDER + "new-diagram.png"; // NOI18N
    }
    
    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous()
    {
        return false;
    }
    
}
