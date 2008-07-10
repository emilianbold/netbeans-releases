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
import org.netbeans.modules.visualweb.gravy.TestUtils;
import org.netbeans.modules.visualweb.gravy.ProjectNavigatorOperator;

import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import javax.swing.tree.TreePath;

/**
 * Class for web page folders included in Project.
 */

public class WebPageFolder extends Folder {

    private final static String bundle = "org.netbeans.modules.visualweb.gravy.model.project.Bundle";
    private final static String popupNew = Bundle.getStringTrimmed(bundle, "New");
    private final static String popupFolder = Bundle.getStringTrimmed(bundle, "Folder");
    private final static String popupNewFolder = popupNew + "|" + popupFolder + "...";
    private final static String dlgNewFolder = popupNew + " " + popupFolder;
    private final static String popupPage = Bundle.getStringTrimmed(
                                               Bundle.getStringTrimmed(bundle, "JSFResBundle"),
                                               Bundle.getStringTrimmed(bundle, "Page"));
    private final static String popupNewPage = popupNew + "|" + popupPage + "...";
    private final static String dlgNewPage = popupNew + " " + popupPage;
    private final static String btnFinish = Bundle.getStringTrimmed(bundle, "FinishButton");
    private final static String btnCancel = Bundle.getStringTrimmed(bundle, "CancelButton");
    
    private SourceFolder sf;
    
    WebPageFolder(TreePath path, String name, ProjectEntry parent) {
        super(path, name, parent);
        if (parent instanceof RootEntry) {
            RootEntry root = (RootEntry) parent;
            SourceFolder srf = new SourceFolder(new TreePath(root.getProject().getName()), root.sourcePackagesName, root);
            SourceFolder sf = new SourceFolder(new TreePath(root.getProject().getName() + "|" + root.sourcePackagesName),  root.getProject().getName().toLowerCase(), srf);
            srf.childList.add(sf);
            root.childList.add(srf);
            this.sf = sf;
        }
        else this.sf = ((WebPageFolder) parent).getSourceFolder().addSourceSubFolder(name);
    }

    /**
     * Add new web page to folder.
     * @param name Name of new web page.
     * @return Added web page.
     */
    public WebPage addWebPage(String name) {
        if (getWebPage(name) == null) {
            ProjectNavigatorOperator prjNav = ProjectNavigatorOperator.showProjectNavigator();
            TestUtils.wait(4000);
            String full_path = this.path.toString().substring(1, this.path.toString().length() - 1) + "|" + this.name;
            try {
                prjNav.pressPopupItemOnNode(full_path, popupNewPage, new Operator.DefaultStringComparator(true, true));
            }
            catch(Exception e) {
                throw new JemmyException(popupNewPage + " item in popup menu of " + full_path + " node can't be found!", e);
            }
            TestUtils.wait(1000);
            try {
                JDialogOperator jdo = new JDialogOperator(dlgNewPage);
                new JTextFieldOperator(jdo, 0).setText(name);
                TestUtils.wait(500);
                new JButtonOperator(jdo, btnFinish).pushNoBlock();
            }
            catch(Exception e) {
                throw new JemmyException("Error occurs in " + dlgNewPage + " dialog!", e);
            }
            TestUtils.wait(1000);
            WebPage newWebPage = new WebPage(new TreePath(full_path), name, this);
            childList.add(newWebPage);
            return newWebPage;
        }
        else return getWebPage(name);
    }
    
    /**
     * Get web page with specified name.
     * @param name Name of web page.
     * @return Web page with specified name.
     */
    public WebPage getWebPage(String name) {
        for (int i = 0; i < childList.size(); i++) {
            if ((childList.get(i) instanceof WebPage) && ((WebPage) childList.get(i)).getName().equals(name))
                return ((WebPage) childList.get(i));
        }
        if (getParent() instanceof RootEntry && name.equals("Page1")) {
            String full_path = this.path.toString().substring(1, this.path.toString().length() - 1) + "|" + this.name;
            WebPage newWebPage = new WebPage(new TreePath(full_path), name, this);
            childList.add(newWebPage);
            return newWebPage;
        }
        return null;
    }
    
    /**
     * Add web page folder to folder.
     * @param name Name of web page folder.
     * @return Added web page folder.
     */
    public WebPageFolder addWebPageSubFolder(String name) {
        ProjectNavigatorOperator prjNav = ProjectNavigatorOperator.showProjectNavigator();
        TestUtils.wait(1000);
        String full_path = this.path.toString().substring(1, this.path.toString().length() - 1) + "|" + this.name;
        try {
            prjNav.pressPopupItemOnNode(full_path, popupNewFolder, new Operator.DefaultStringComparator(true, true));
        }
        catch(Exception e) {
            throw new JemmyException(popupNewFolder + " item in popup menu of " + full_path + " node can't be found!", e);
        }
        TestUtils.wait(1000);
        try {
            JDialogOperator jdo = new JDialogOperator(dlgNewFolder);
            new JTextFieldOperator(jdo, 0).setText(name);
            TestUtils.wait(500);
            new JButtonOperator(jdo, btnFinish).pushNoBlock();
        }
        catch(Exception e) {
            throw new JemmyException("Error occurs in " + dlgNewFolder + " dialog!", e);
        }
        TestUtils.wait(1000);
        WebPageFolder newWebPageFolder = new WebPageFolder(new TreePath(full_path), name, this);
        childList.add(newWebPageFolder);
        return newWebPageFolder;
    }
    
    /**
     * Get web page subfolder with specified name.
     * @param name Name of web page subfolder.
     * @return Web page folder with specified name.
     */
    public WebPageFolder getWebPageSubFolder(String name) {
        for (int i = 0; i < childList.size(); i++) {
            if (childList.get(i) instanceof WebPageFolder && ((WebPageFolder) childList.get(i)).getName().equals(name))
                return ((WebPageFolder) childList.get(i));
        }
        return null;
    }
    
    /**
     * Get source folder for this web page folder.
     * @return Source folder for this web page folder.
     */
    public SourceFolder getSourceFolder() {
        return sf;
    }
}
