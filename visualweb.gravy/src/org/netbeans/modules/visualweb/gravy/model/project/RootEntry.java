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

import org.netbeans.modules.visualweb.gravy.Bundle;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.TreePath;

/**
 * Class for root entry of the Project.
 */

public class RootEntry implements ProjectEntry {

    private final static String bundle = "org.netbeans.modules.visualweb.gravy.model.project.Bundle";
    
    public final static String sourcePackagesName = Bundle.getStringTrimmed(
                                                  Bundle.getStringTrimmed(bundle, "WebProjectsBundle"),
                                                  Bundle.getStringTrimmed(bundle, "SourcePackages"));
    public final static String webPagesName = Bundle.getStringTrimmed(
                                                  Bundle.getStringTrimmed(bundle, "WebProjectsBundle"),
                                                  Bundle.getStringTrimmed(bundle, "WebPages"));
    
    /**
     * Project which the root entry created for.
     */
   Project project;
    
    /**
     * Child project entries of the root entry.
     */
   List childList = new ArrayList();
    
    /**
     * Creates a new instance of root entry.
     */
    public RootEntry(Project project) {
        this.project = project;
    }

    /**
     * Save root entry.
     */
    public void save() {
    }
    
    /**
     * Save root entry as root entry with specified name.
     */
    public void saveAs(String name) {
    }
    
    /**
     * @return Project which the root entry belong to.
     */
    public Project getProject() {
        return project;
    }
    
    /**
     * Get name of root entry.
     * @return Name of the root entry.
     */
    public String getName() {
        return null;
    }

    /**
     * @return TreePath of the root entry.
     */
    public TreePath getTreePath() {
        return null;
    }
    
    /**
     * @return Parent of the root entry.
     */
    public ProjectEntry getParent() {
        return null;
    }
    
    /**
     * @return Child project entries of the root entry.
     */
    public ProjectEntry[] getChildren() {
        return ((ProjectEntry[]) childList.toArray(new ProjectEntry[childList.size()]));
    }
    
    /**
     * @return Web Page root folder.
     */
    public WebPageFolder getWebPageRootFolder() {
        ProjectEntry[] prjEntries = getChildren();
        for (int i = 0; i < prjEntries.length ; i++) {
            if (prjEntries[i] instanceof WebPageFolder && 
                ((WebPageFolder) prjEntries[i]).getName().equals(webPagesName)) {
                return ((WebPageFolder) prjEntries[i]);
            }
        }
        return null;
    }
    
    /**
     * @return Source root folder.
     */
    public SourceFolder getSourceRootFolder() {
        ProjectEntry[] prjEntries = getChildren();
        for (int i = 0; i < prjEntries.length ; i++) {
            if (prjEntries[i] instanceof SourceFolder && 
                ((SourceFolder) prjEntries[i]).getName().equals(sourcePackagesName)) {
                return ((SourceFolder) prjEntries[i]);
            }
        }
        return null;
    }
    
    /**
     * Remove root entry.
     */
    public void delete() {
    }
}
