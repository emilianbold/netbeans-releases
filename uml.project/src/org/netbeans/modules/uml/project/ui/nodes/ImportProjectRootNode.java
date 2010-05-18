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

/*
 * ImportProjectRootNode.java
 *
 * Created on May 15, 2005, 6:58 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.uml.project.ui.nodes;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageImport;
import org.netbeans.modules.uml.project.UMLProjectHelper;
import org.netbeans.modules.uml.project.ui.customizer.ImportElementListener;
import javax.swing.Action;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.project.ProjectUtil;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.openide.nodes.AbstractNode;
import org.openide.util.NbBundle;


/**
 *
 * @author Administrator
 */
public class ImportProjectRootNode extends AbstractNode implements ImportElementListener
{

    IProject project = null;

    /** Creates a new instance of ImportProjectRootNode */
    public ImportProjectRootNode(UMLProjectHelper helper)
    {
        super(new ImportedProjectChildren(helper));
        this.project = helper.getProject();
        setIconBaseWithExtension(ImageUtil.IMAGE_FOLDER + "import-elements.png"); // NOI18N
    }

    public String getDisplayName()
    {
        return (String) NbBundle.getMessage(ImportProjectRootNode.class, "ImportedNode_Name"); // NOI18N
    }

    public String getName()
    {
        return this.getDisplayName();
    }
    ////////////////////////////////////////////////////////////////////////////
    // ImportElementListener methods

    public void elementImported(IProject project,
            IElement element,
            IElementImport importElement)
    {
        ImportedProjectChildren children = (ImportedProjectChildren) getChildren();
        children.addNewImportedElement(project, element, importElement, true);
    }

    public void packageImported(IProject project,
            IElement element,
            IPackageImport importElement)
    {
        ImportedProjectChildren children = (ImportedProjectChildren) getChildren();
        children.addNewImportedElement(project, element, importElement, true);
    }

    public void elementDeleted(IProject proj, IElement element)
    {
        // this is the case when original model element is deleted,
        // just remove it from imported element list if it exists, no need to operate on
        // the node, as filter node listens to original node events, it will be destroyed
        // once the original node is deleted.
        if (!(element instanceof IElementImport) && !(element instanceof IPackageImport))
        {
            project.removeElementImport(element);
            return;
        }

        // the event is triggered when imported element is deleted from diagram with
        // 'remove from import' option is selected, or an imported element is deleted in 
        // other projects.
        // filter out the event for deleting imported element in a different project 85134
        if (proj != project)
            return;
        
        IElement e = element;
        IProject ownerProject = proj;
        if (element instanceof IElementImport)
        {
            e = ((IElementImport) element).getImportedElement();
        } else if (element instanceof IPackageImport)
        {
            e = ((IPackageImport) element).getImportedPackage();
        }
//        ownerProject = e.getOwner() == null ? proj : e.getOwner().getProject();
        ownerProject = ProjectUtil.getOwningProjectOfImportedElement(e);
        ImportedProjectChildren children = (ImportedProjectChildren) getChildren();
        children.removeImportElement(ownerProject, element);
    }

    
    public boolean canCopy()
    {
        return false;
    }

    public boolean canCut()
    {
        return false;
    }

    public boolean canDestroy()
    {
        return false;
    }

    public boolean canRename()
    {
        return false;
    }

    public Action[] getActions(boolean context)
    {
        return new Action[]{null};
    }
}
