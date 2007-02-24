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

import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.project.UMLProjectHelper;
import org.netbeans.modules.uml.project.ui.customizer.ImportElementListener;
import javax.swing.Action;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
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
        setIconBaseWithExtension(
            "org/netbeans/modules/uml/project/ui/resources/import_elements.png");
    }
    
    public String getDisplayName () {
        return (String)NbBundle.getMessage(ImportProjectRootNode.class, "ImportedNode_Name"); 
    }

    public String getName () {
        return this.getDisplayName();
    } 

    ////////////////////////////////////////////////////////////////////////////
    // ImportElementListener methods 
    
    public void elementImported(UMLProject project, 
                                IElement element, 
                                IElementImport importElement)
    {
        ImportedProjectChildren children = (ImportedProjectChildren)getChildren();
        children.addNewImportedElement(project, element, importElement, true);
    }
    
    public void packageImported(UMLProject project, 
                                IElement element, 
                                IPackageImport importElement)
    {
        ImportedProjectChildren children = (ImportedProjectChildren)getChildren();
        children.addNewImportedPackage(project, element, importElement, true);
    }
    
    public void elementDeleted(UMLProject project, IElement element)
    {
        this.project.removeElementImport(element);
        ImportedProjectChildren children = (ImportedProjectChildren)getChildren();
        children.removeImportElement(project, element);
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
        return new Action[] {null};
    }
    
}
