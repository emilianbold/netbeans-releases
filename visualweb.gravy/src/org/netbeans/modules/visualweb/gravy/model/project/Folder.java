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

package org.netbeans.modules.visualweb.gravy.model.project;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.TreePath;

/**
 * Class for folders included in Project.
 */

public class Folder implements ProjectEntry {

    TreePath path;
    String name;
    
    /**
     * Parent of the folder.
     */
    private ProjectEntry parent;
    
    /**
     * Child project entries of the folder.
     */
    List childList = new ArrayList();

    /**
     * Creates a new instance of folder.
     * @param path Path to folder in project.
     * @param name Name of folder.
     */
    Folder(TreePath path, String name, ProjectEntry parent) {
        this.path = path;
        this.name = name;
        setParent(parent);
    }

    /**
     * Save folder.
     */
    public void save() {
    }
    
    /**
     * Save folder as folder with specified name.
     */
    public void saveAs(String name) {
    }
    
    /**
     * Get name of folder.
     * @return Name of the folder.
     */
    public String getName() {
        return name;
    }

    /**
     * @return TreePath of the folder.
     */
    public TreePath getTreePath() {
        return path;
    }
    
    /**
     * @return Parent of the folder.
     */
    public ProjectEntry getParent() {
        return parent;
    }
    
    /**
     * Set parent of the folder.
     */
    protected void setParent(ProjectEntry parent) {
        this.parent = parent;
    }
    
    /**
     * @return Child project entries of the folder.
     */
    public ProjectEntry[] getChildren() {
        return ((ProjectEntry[]) childList.toArray(new ProjectEntry[childList.size()]));
    }
    
    
    
    /**
     * Remove folder.
     */
    public void delete() {
    }
}
