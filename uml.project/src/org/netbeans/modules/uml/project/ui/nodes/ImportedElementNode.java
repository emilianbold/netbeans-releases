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

package org.netbeans.modules.uml.project.ui.nodes;

import java.io.IOException;
import java.text.Collator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageImport;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.project.ui.cookies.ImportedElementCookie;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;


public class ImportedElementNode extends FilterNode implements ImportedElementCookie, Comparable
{

    private IProject referencingProject;
    private IElement elementImport;

    public ImportedElementNode(IProject project, Node orig,
            IElementImport elementImport)
    {
        super(orig, new NestedImportElementChildren(orig));
        this.referencingProject = project;
        this.elementImport = elementImport;
        this.disableDelegation(DELEGATE_DESTROY);
    }

    public ImportedElementNode(IProject project, Node orig,
            IPackageImport elementImport)
    {
//        super(orig, Children.LEAF);
        super(orig,
                new NestedImportElementChildren(orig));
        this.referencingProject = project;
        this.elementImport = elementImport;
        this.disableDelegation(DELEGATE_DESTROY);
    }
    
//   public Action[] getActions(boolean context)
//    {
//       if (elementImport instanceof IElementImport)
//           return super.getActions(context);
//
//       ArrayList<Action> list = new ArrayList<Action>();
//       Action[] actions = getOriginal().getActions(context);
//       for (Action action: actions)
//       {
//           if (action == SystemAction.get(UMLNewAction.class))
//               continue;
//           list.add(action);
//       }
//
//       Action[] a = new Action[list.size()];
//       return list.toArray(a);
//    }

    public <T extends Node.Cookie> T getCookie(Class<T> type)
    {
        if (type.isInstance(this))
        {
            return type.cast(this);
        }
        return super.getCookie(type);
    }

    public boolean canDestroy()
    {
        return true;
    }

    public void destroy() throws IOException
    {
        destroy(true);
    }

    public void destroy(boolean fromOriginal) throws IOException
    {
        if (fromOriginal == true)
        {
            this.getOriginal().destroy();
        }
        super.destroy(); // calls Node.destroy(), not orig.destroy()
    }

    public void removeImportedElement()
    {
        IProjectTreeItem item = getOriginal().getCookie(IProjectTreeItem.class);
        if (item != null)
        {
            referencingProject.removeElementImport(elementImport);
        }
        try
        {
            destroy(false);
        } catch (IOException e)
        {
        }
    }

    public int compareTo(Object o)
    {
        return Collator.getInstance().compare(this.getName(), o.toString());
//        if (!(o instanceof ImportedElementNode))
//        {
//            return -1;
//        }
//        IProjectTreeItem item1 = getOriginal().getCookie(IProjectTreeItem.class);
//        IProjectTreeItem item2 = ((ImportedElementNode) o).getOriginal().getCookie(IProjectTreeItem.class);
//        if (item1 != null && item2 != null)
//        {
//            return ProjectTreeComparable.compareTo(item1, item2);
//        }
//        return -1;
    }

    public IProject getReferencingProject()
    {
        return referencingProject;
    }

    public String getElementXMIID()
    {
        IProjectTreeItem item = getOriginal().getCookie(IProjectTreeItem.class);
        return (item != null) ? item.getModelElementXMIID() : "";
    }

    /**
     *  override FilterNode.Children to provide custom logic for sub nodes of
     *  imported element. Delete action is disabled on those sub nodes to prevent
     *  user from removing model elements inadvertently from imported projects
     */
    public static class NestedImportElementChildren extends FilterNode.Children
    {

        public NestedImportElementChildren(Node or)
        {
            super(or);
        }

        protected Node[] createNodes(Node key)
        {
            NestedImportElementNode node = new NestedImportElementNode(key);
            return new Node[]{node};
        }
    }

    public static class NestedImportElementNode extends FilterNode
    {

        public NestedImportElementNode(Node orig)
        {
            super(orig, new NestedImportElementChildren(orig));
        }

        public boolean canDestroy()
        {
            return false;
        }
    }
}
