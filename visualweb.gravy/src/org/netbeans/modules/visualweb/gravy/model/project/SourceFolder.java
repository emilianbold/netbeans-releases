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

import org.netbeans.modules.visualweb.gravy.TestUtils;
import org.netbeans.modules.visualweb.gravy.ProjectNavigatorOperator;

import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import javax.swing.tree.TreePath;

/**
 * Class for source folders included in Project.
 */

public class SourceFolder extends Folder {

    private final String popupNewPackage = "New|Java Package...";
    private final String dlgNewPackage = "New Java Package";
    private final String popupNewClass = "New|Java Class...";
    private final String dlgNewClass = "New Java Class";
    private final String btnFinish = "Finish";
    private final String btnCancel = "Cancel";
    
    SourceFolder(TreePath path, String name, ProjectEntry parent) {
        super(path, name, parent);
    }

    /**
     * Add new source subfolder to current folder.
     * @param name Name of new source subfolder.
     * @return Added source subfolder.
     */
    public SourceFolder addSourceSubFolder(String name) {
        if (getSourceSubFolder(name) == null) {
            String path_part1 = "", path_part2 = "", full_path = "", path_to_find = "";
            ProjectNavigatorOperator prjNav = ProjectNavigatorOperator.showProjectNavigator();
            TestUtils.wait(1000);
            full_path = this.path.toString().substring(1, this.path.toString().length() - 1) + "|" + this.name;
            path_to_find = full_path + "|" + name;
            if (full_path.indexOf("|", full_path.indexOf("|") + 1) != -1) {
                path_part1 = full_path.substring(0, full_path.indexOf("|", full_path.indexOf("|") + 1) + 1);
                path_part2 = full_path.substring(full_path.indexOf("|", full_path.indexOf("|") + 1) +1, full_path.length()).replace('|', '.');
                full_path = path_part1.concat(path_part2);
                path_to_find = full_path + "." + name;
            }
            try {
                prjNav.tree().findPath(path_to_find);
            }
            catch(TimeoutExpiredException tee) {
                try {
                    prjNav.pressPopupItemOnNode(full_path, popupNewPackage, new Operator.DefaultStringComparator(true, true));
                }
                catch(Exception e) {
                    throw new JemmyException(popupNewPackage + " item in popup menu of " + full_path + " node can't be found!", e);
                }
                TestUtils.wait(1000);
                try {
                    JDialogOperator jdo = new JDialogOperator(dlgNewPackage);
                    if (!path_part2.equals("")) path_part2 = path_part2 + ".";
                    new JTextFieldOperator(jdo, 0).setText(path_part2 + name);
                    TestUtils.wait(500);
                    new JButtonOperator(jdo, btnFinish).pushNoBlock();
                }
                catch(Exception e) {
                    throw new JemmyException("Error occurs in " + dlgNewPackage + " dialog!", e);
                }
                TestUtils.wait(1000);
            }
            SourceFolder newSourceFolder = new SourceFolder(new TreePath(full_path), name, this);
            childList.add(newSourceFolder);
            return newSourceFolder;
        }
        else return getSourceSubFolder(name);
    }
    
    /**
     * Get source subfolder with specified name.
     * @param name Name of source subfolder.
     * @return Source folder with specified name.
     */
    public SourceFolder getSourceSubFolder(String name) {
        for (int i = 0; i < childList.size(); i++) {
            if (childList.get(i) instanceof SourceFolder && ((SourceFolder) childList.get(i)).getName().equals(name))
                return ((SourceFolder) childList.get(i));
        }
        return null;
    }
    
    /**
     * Add Java file to folder.
     * @param name Name of Java file.
     * @return Added Java file.
     */
    public JavaFile addJavaFile(String name) {
        if (getJavaFile(name) == null) {
            String path_part1 = "", path_part2 = "", full_path = "";
            ProjectNavigatorOperator prjNav = ProjectNavigatorOperator.showProjectNavigator();
            TestUtils.wait(1000);
            full_path = this.path.toString().substring(1, this.path.toString().length() - 1) + "|" + this.name;
            if (full_path.indexOf("|", full_path.indexOf("|") + 1) != -1) {
                path_part1 = full_path.substring(0, full_path.indexOf("|", full_path.indexOf("|") + 1) + 1);
                path_part2 = full_path.substring(full_path.indexOf("|", full_path.indexOf("|") + 1) +1, full_path.length()).replace('|', '.');
                full_path = path_part1.concat(path_part2);
            }
            try {
                prjNav.tree().findPath(full_path + "|" + name + ".java");
            }
            catch(TimeoutExpiredException tee) {
                try {
                    prjNav.pressPopupItemOnNode(full_path, popupNewClass, new Operator.DefaultStringComparator(true, true));
                }
                catch(Exception e) {
                    throw new JemmyException(popupNewClass + " item in popup menu of " + full_path + " node can't be found!", e);
                }
                TestUtils.wait(1000);
                try {
                    JDialogOperator jdo = new JDialogOperator(dlgNewClass);
                    new JTextFieldOperator(jdo, 0).setText(name);
                    TestUtils.wait(500);
                    new JButtonOperator(jdo, btnFinish).pushNoBlock();
                }
                catch(Exception e) {
                    throw new JemmyException("Error occurs in " + dlgNewClass + " dialog!", e);
                }
                TestUtils.wait(1000);
            }
            JavaFile newJavaFile;
            if (getParent() instanceof RootEntry) newJavaFile = new JavaFile(new TreePath(full_path + "|<default package>"), name, this);
            else newJavaFile = new JavaFile(new TreePath(full_path), name, this);
            childList.add(newJavaFile);
            return newJavaFile;
        }
        else return getJavaFile(name);
    }
    
    /**
     * Get Java file with specified name.
     * @param name Name of Java file.
     * @return Java file with specified name.
     */
    public JavaFile getJavaFile(String name) {
        for (int i = 0; i < childList.size(); i++) {
            if (childList.get(i) instanceof JavaFile && ((JavaFile) childList.get(i)).getName().equals(name))
                return ((JavaFile) childList.get(i));
        }
        if ((getParent().getParent() instanceof RootEntry) && 
            (name.equals("ApplicationBean1") || name.equals("RequestBean1") || 
             name.equals("SessionBean1"))) {
            String str_path = this.path.toString().substring(1, this.path.toString().length() - 1);
            JavaFile newJavaFile = new JavaFile(new TreePath(str_path + "|" + this.name), name, this);
            childList.add(newJavaFile);
            return newJavaFile;
        }
        return null;
    }
}
