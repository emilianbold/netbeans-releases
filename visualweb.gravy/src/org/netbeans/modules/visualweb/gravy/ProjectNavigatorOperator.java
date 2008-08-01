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

package org.netbeans.modules.visualweb.gravy;

import javax.swing.tree.TreePath;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.*;
import org.netbeans.jellytools.*;
import org.netbeans.modules.visualweb.gravy.actions.ShowFilesAction;
import org.netbeans.modules.visualweb.gravy.actions.ShowProjectsAction;
import org.netbeans.modules.visualweb.gravy.actions.BuildProjectAction;

import java.awt.*;

/** Provides access to Projects window and it's components
 *  @author Alexey Butenko
 * 
 */
public class ProjectNavigatorOperator extends org.netbeans.jellytools.ProjectsTabOperator {

    public ProjectNavigatorOperator() {
            super();
    }
    
    /** Select Path
     *  @param treePath - String path to select
     */
    public void selectPath(String treePath) {
        makeComponentVisible();
        TreePath path = tree().findPath(treePath);
        tree().selectPath(path);
    }

    /** Opens Projects window
     */
    public static ProjectNavigatorOperator showProjectNavigator() {
        new ShowProjectsAction().perform();
        new QueueTool().waitEmpty();
        Util.wait(500);
        return(new ProjectNavigatorOperator());
    }
    /**
     *  Select Node
     *  @param node - Node to select
     */
    public static void selectNode(String node){
        ProjectNavigatorOperator projectNav = ProjectNavigatorOperator.showProjectNavigator();
        projectNav.makeComponentVisible();
        JTreeOperator tree = projectNav.tree();
        tree.selectPath(tree.findPath(node));
    }
    
    /** Build Project
     * @param prjName - Name of the project to build
     */
    public void buildProject(String prjName) {
        makeComponentVisible();
        pressPopupItemOnNode(prjName, Bundle.getStringTrimmed("com.sun.rave.project.actions.Bundle", "LBL_BuildProject"));
    }
    
    /** Opens Files Tab
     */
    public static JTreeOperator switchToFiles() {
        new ShowFilesAction().perform();

        ContainerOperator containerOperator = new ContainerOperator(
            waitTopComponent(null,
            Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle",
            "LBL_projectTab_tc"), 0,
            new ComponentChooser() {
                public boolean checkComponent(Component comp) {
                    return comp.getClass().getName().endsWith("ProjectTab");
                }
                public String getDescription() {
                    return "org.netbeans.modules.projects.ui.ProjectTab";
                }
            }));
        Util.wait(1000);
        JTreeOperator treeOperator = new JTreeOperator(containerOperator);
        return (treeOperator);
    }

    /** Opens Projects Tab
     */
    public static ProjectNavigatorOperator switchToProjects() {
        new ShowProjectsAction().perform();
        return(new ProjectNavigatorOperator());
    }

    /** Press popup item on node
     * @param path - click for popup on
     * @param menuItem - item to click in popup window
     * @param comparator - StringComparator
     */
    public static ProjectNavigatorOperator pressPopupItemOnNode(String path, String menuItem, 
        StringComparator comparator) {
        try{
            ProjectNavigatorOperator projectNav = ProjectNavigatorOperator.showProjectNavigator();
            TestUtils.wait(4000);
            //projectNav.makeComponentVisible();
            JTreeOperator tree = projectNav.tree();
            JPopupMenuOperator popup;
            if (path.equals("ROOT")) {
                popup = new JPopupMenuOperator(tree.callPopupOnPath(new TreePath(tree.getRoot())));
            } else {
                popup = new JPopupMenuOperator(tree.callPopupOnPath(tree.findPath(path, "|")));
            }
            new QueueTool().waitEmpty();
            Util.wait(1000);

            if (comparator != null) {
                popup.setComparator(comparator);
            }

            popup.pushMenuNoBlock(menuItem, "|");
            new QueueTool().waitEmpty();
            Util.wait(1000);
            return projectNav;
        } catch(Exception e) {
            System.out.println("Exception occured in pressPopupItemOnPath function");
            e.printStackTrace();
            return null;
        }
    }

    /** Press popup item on node
     * @param path - click for popup on
     * @param menuItem - item to click in popup window
     */
    public static ProjectNavigatorOperator pressPopupItemOnNode(String path, String menuItem) {
            return pressPopupItemOnNode(path, menuItem, null);
    }
    
    /**
     * Add a new page
     * @param projectName project that a page will be created at
     * @param pageName page will be created
     */
    public void addWebPage(String projectName, String pageName){
        String treeNodePath = projectName + "|" + Bundle.getStringTrimmed("org.netbeans.modules.visualweb.gravy.Bundle",
            "ProjectNode_WebPages");
        String popupMenuItem = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.gravy.Bundle","ProjectMenuItem_New")
        +"|"+ Bundle.getStringTrimmed("org.netbeans.modules.visualweb.gravy.Bundle","ProjectMenuItem_Page");
        pressPopupItemOnNode(treeNodePath, popupMenuItem);
        
        JDialogOperator dialog = new JDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.visualweb.gravy.Bundle","Dialog_NewPage"));
        Util.wait(1000);
         
        JTextFieldOperator textfieldFolderName = new JTextFieldOperator(dialog, 0);
        textfieldFolderName.setText(pageName);
        new JButtonOperator(dialog, Bundle.getStringTrimmed("org.netbeans.modules.visualweb.gravy.Bundle","Button_Finish")).pushNoBlock();
        //dialog.waitClosed();
        Util.wait(1000);
        
    }
    
    /**
     * Open a jsp page of current project
     * @param webPageName
     */
     public void openWebPage(String projectName, String webPageName) {
        String treeNodePath = projectName + "|" + Bundle.getStringTrimmed("org.netbeans.modules.visualweb.gravy.Bundle", "ProjectNode_WebPages") + "|" +
                webPageName + Bundle.getStringTrimmed("org.netbeans.modules.visualweb.gravy.Bundle", "WebPage_extension");
        String popupMenuItem = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.gravy.Bundle", "ProjectMenuItem_Open");
        pressPopupItemOnNode(treeNodePath, popupMenuItem);
    }
}
