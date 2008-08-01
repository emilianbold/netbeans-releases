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

import java.awt.*;
import java.util.*;
import com.sun.rave.designtime.DesignBean;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.*;
import javax.swing.tree.*;

/**
 * Operator for Outline window.
 */
public class DocumentOutlineOperator extends JComponentOperator {
    private JTreeOperator structureTree;

    public DocumentOutlineOperator(ContainerOperator contOper) {
        super(contOper, new DocumentOutlineChooser());
    }

    /** Performs verification by accessing all sub-components */
    public void verify() {
        getStructTreeOperator();
    }

    /**
     * Find specified tree path.
     * @param strTreePath String which represents tree path.
     * @return TreePath Specified path.
     */
    public TreePath findPath(String strTreePath) {
        verify();
        return (structureTree.findPath(strTreePath));
    }

    /**
     * Find specified tree path with specified comparator.
     * @param strTreePath String which represents tree path.
     * @param nodeIndexes Array of nodes indexes.
     * @param comparator Comparator to compare tree path with specified string.
     * @return TreePath Specified path.
     */
    public TreePath findPath(String strTreePath, int[] nodeIndexes, 
                             Operator.StringComparator comparator) {
        verify();
        String[] nodeNames = new Operator.DefaultPathParser("|").parse(strTreePath);        
        if (nodeIndexes == null) {
            nodeIndexes = new int[nodeNames.length]; // fill the array in with zero values
        }
        System.out.println("+++ DocumentOutlineOperator.findPath(...): nodeNames = " + 
            Arrays.asList(nodeNames));
        System.out.print("+++ DocumentOutlineOperator.findPath(...): nodeIndexes = [");
        for (int i = 0; i < nodeIndexes.length; ++i) {
            System.out.print(nodeIndexes[i] + 
                (i < nodeIndexes.length - 1 ? ", " : ""));
        }
        System.out.println("]");        
        TreePath treePath = structureTree.findPath(nodeNames, nodeIndexes, 
            comparator);
        return treePath;
    }

    /**
     * Click on specified tree path.
     * @param strTreePath String which represents tree path.
     */
    public void clickOnPath(String strTreePath) {
        structureTree.clickOnPath(findPath(strTreePath));
    }

    /**
     * Expand specified tree path.
     * @param strTreePath String which represents tree path.
     */
    public void expandPath(String strTreePath) {
        structureTree.expandPath(findPath(strTreePath));
    }

    /**
     * Select specified tree path.
     * @param strTreePath String which represents tree path.
     */
    public void selectPath(String strTreePath) {
        structureTree.selectPath(findPath(strTreePath));
    }

    /**
     * Click for popup with specified mouse button.
     * @param i Number of mouse button.
     */
    public void clickForPopup(int i) {
        verify();
        structureTree.clickForPopup(i);
    }

    /**
     * Get tree which represents structure.
     * @return JTreeOperator JTree which represents structure.
     */
    public JTreeOperator getStructTreeOperator() {
        if (structureTree == null) {
            structureTree = new JTreeOperator(this);
        }
        return structureTree;
    }

    /**
     * Press button with given title.
     * @param btName Name of button.
     */
    public void pressButton(String btName){
        new JButtonOperator(this, btName).pushNoBlock();
    }

    /**
     * Find child with specified name in specified parent.
     * @param parent Bean where child will be searched.
     * @param componentName Name of child.
     * @return DesignBean Found child.
     */
    private DesignBean findChild(DesignBean parent, String componentName){
        DesignBean lb;
        if (parent.getInstanceName().equals(componentName)){
            return parent;
        }
        for (int i = 0; i < parent.getChildBeanCount(); i++) {
            if ((lb=findChild(parent.getChildBean(i),componentName))!=null){
                return lb;
            }
        }
        return null;
    }

    /**
     * Select component with specified name.
     * @param componentName Name of component.
     * @return True if component selected.
     */
    public boolean selectComponent(String componentName) {
        DesignBean[] lbs = null;
        Object rootChild = structureTree.getChildren(structureTree.getRoot())[0];
        for (int i = 0; i < structureTree.getChildCount(rootChild); i++) {
            DesignBean lb = (DesignBean)structureTree.getChildren(rootChild)[i];
            DesignBean res=findChild(lb,componentName);
            if (res!=null) {
                lbs = new DesignBean[] {res};
                break;
            }
        }
        if (lbs != null) {
            TestUtils.wait(500);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Open page with specified name.
     * @param page Name of page.
     */
    public void openPage(String page){
        new JButtonOperator(this,page).clickForPopup();
        new JPopupMenuOperator().pushMenu(
            Bundle.getStringTrimmed("org.netbeans.modules.visualweb.outline.Bundle","Open"));
    }

    /**
     * Open source code of page with specified name.
     * @param page Name of page.
     */
    public void openPageSource(String page){
        new JButtonOperator(this,page).clickForPopup();
        new JPopupMenuOperator().pushMenu(Bundle.getStringTrimmed("org.netbeans.modules.visualweb.outline.Bundle","ViewBF",
                new String[]{page})
        );
    }

    public static class DocumentOutlineChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().equalsIgnoreCase("org.netbeans.modules.visualweb.outline.OutlinePanel");
        }

        public String getDescription() {
            return "Document Outline Operator";
        }
    }
}
