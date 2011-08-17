/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.jellytools.modules.form;

import java.awt.Component;
import java.awt.Point;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NavigatorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 * Provides access to Navigator TopComponent belonging to form editor.
 */
public class ComponentInspectorOperator extends NavigatorOperator {

    private PropertySheetOperator _properties;
    private static final String PROPERTIES_POPUP = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Properties");

    /** Getter for component tree.
     * @return JTreeOperator instance
     */
    public JTreeOperator treeComponents() {
        new FormDesignerOperator(null).makeComponentVisible();
        return new JTreeOperator(this, new FormTreeComponentChooser());
    }

    /** Getter for PropertySheetOperator. It returns first found property
     * sheet within IDE. It is not guaranteed that it is the global property
     * placed next to Component Inspector by default.
     * @return PropertySheetOperator instance
     */
    public PropertySheetOperator properties() {
        if (_properties == null) {
            _properties = new PropertySheetOperator();
        }
        return (_properties);
    }

    /** Opens property sheet and returns PropertySheetOperator instance for
     * given component path.
     * @param componentPath path in component tree (e.g. "[JFrame]|jPanel1")
     * @return instance of PropertySheetOperator
     */
    public PropertySheetOperator properties(String componentPath) {
        new FormDesignerOperator(null).makeComponentVisible();
        JTreeOperator treeOper = getTree();
        // do not use Node.performPopup() because it changes context of navigator
        Node node = new Node(treeOper, componentPath);
        TreePath treePath = node.getTreePath();
        treeOper.expandPath(treePath.getParentPath());
        treeOper.scrollToPath(treePath);
        Point point = treeOper.getPointToClick(treePath);
        new JPopupMenuOperator(JPopupMenuOperator.callPopup(treeOper, (int) point.getX(), (int) point.getY(), getPopupMouseButton())).pushMenu(PROPERTIES_POPUP);
        return new PropertySheetOperator(node.getText());
    }

    /** Selects component in the tree.
     * @param componentPath path in component tree (e.g. "[JFrame]|jPanel1")
     */
    public void selectComponent(String componentPath) {
        new FormDesignerOperator(null).makeComponentVisible();
        JTreeOperator treeOper = getTree();
        // do not use Node.select() because it changes context of navigator
        treeOper.setSelectionPath(new Node(treeOper, componentPath).getTreePath());
    }

    /** Performs verification by accessing all sub-components */
    public void verify() {
        getTree();
        properties().verify();
    }

    private static final class FormTreeComponentChooser implements ComponentChooser {

        @Override
        public boolean checkComponent(Component comp) {
            Object root = ((JTree) comp).getModel().getRoot();
            return root != null && root.toString().startsWith("Form");
        }

        @Override
        public String getDescription() {
            return "Form Tree";  //NOI18N
        }
    }
}
