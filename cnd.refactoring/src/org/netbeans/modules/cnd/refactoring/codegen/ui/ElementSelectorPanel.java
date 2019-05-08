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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.cnd.refactoring.codegen.ui;

import org.netbeans.modules.cnd.modelutil.ui.CheckTreeView;
import java.awt.BorderLayout;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.modelutil.ui.ElementNode;
import org.netbeans.modules.cnd.modelutil.ui.ElementNode.Description;
import org.netbeans.modules.cnd.refactoring.support.DeclarationGenerator;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 */
public class ElementSelectorPanel extends JPanel implements ExplorerManager.Provider {

    private final ExplorerManager manager = new ExplorerManager();
    private final CheckTreeView elementView;
    private final JCheckBox inline = new JCheckBox();
    private boolean inlineMethod;
    /** Creates new form ElementSelectorPanel */
    public ElementSelectorPanel(ElementNode.Description elementDescription, boolean singleSelection, boolean supportInline, boolean sortChildren) {
        setLayout(new BorderLayout());
        elementView = new CheckTreeView();
        elementView.setRootVisible(false);
        add(elementView, BorderLayout.CENTER);
        if (supportInline) {
            Mnemonics.setLocalizedText(inline, NbBundle.getMessage(ElementSelectorPanel.class, "LBL_inline_implementation")); // NOI18N
            inlineMethod = NbPreferences.forModule(DeclarationGenerator.class).getBoolean(DeclarationGenerator.INSERT_CODE_INLINE_PROPERTY, true);
            inline.setSelected(inlineMethod);
            inline.setEnabled(false);
            add(inline, BorderLayout.SOUTH);
        } else {
            inlineMethod = false;
        }
        setRootElement(elementDescription, singleSelection, sortChildren);
        //make sure that the first element is pre-selected
        Node root = manager.getRootContext();
        Node[] children = root.getChildren().getNodes();
        if (null != children && children.length > 0) {
            try {
                manager.setSelectedNodes(new org.openide.nodes.Node[]{children[0]});
            } catch (PropertyVetoException ex) {
                //ignore
            }
        }
    }

    public boolean isMethodInline() {
        inlineMethod = inline.isSelected();
        NbPreferences.forModule(DeclarationGenerator.class).putBoolean(DeclarationGenerator.INSERT_CODE_INLINE_PROPERTY, inlineMethod);
        return inlineMethod;
    }

    public List<CsmDeclaration> getTreeSelectedElements() {
        ArrayList<CsmDeclaration> handles = new ArrayList<>();

        for (Node node : manager.getSelectedNodes()) {
            if (node instanceof ElementNode) {
                ElementNode.Description description = node.getLookup().lookup(ElementNode.Description.class);
                handles.add(description.getElementHandle());
            }
        }

        return handles;
    }

    public List<CsmDeclaration> getSelectedElements() {
        ArrayList<CsmDeclaration> handles = new ArrayList<>();

        Node n = manager.getRootContext();
        ElementNode.Description description = n.getLookup().lookup(ElementNode.Description.class);
        getSelectedHandles(description, handles);

        return handles;
    }

    public final void setRootElement(ElementNode.Description elementDescription, boolean singleSelection, boolean sortChildren) {

        Node n;
        if (elementDescription != null) {
            ElementNode en = new ElementNode(elementDescription, sortChildren);
            en.setSingleSelection(singleSelection);
            n = en;
        } else {
            n = Node.EMPTY;
        }
        manager.setRootContext(n);

    }

    public void doInitialExpansion(int howMuch) {

        Node root = getExplorerManager().getRootContext();
        Node[] subNodes = root.getChildren().getNodes(true);

        if (subNodes == null) {
            return;
        }
        Node toSelect = null;

        int row = 0;

        boolean oldScroll = elementView.getScrollsOnExpand();
        elementView.setScrollsOnExpand(false);

        for (int i = 0; subNodes != null && i < (howMuch == - 1 || howMuch > subNodes.length ? subNodes.length : howMuch); i++) {
            // elementView.expandNode2(subNodes[i]);
            row++;
            elementView.expandRow(row);
            Node[] ssn = subNodes[i].getChildren().getNodes(true);
            row += ssn.length;
            if (toSelect == null) {
                if (ssn.length > 0) {
                    toSelect = getSelectedNode(ssn);
                }
            }
        }

        elementView.setScrollsOnExpand(oldScroll);

        try {
            if (toSelect != null) {
                getExplorerManager().setSelectedNodes(new org.openide.nodes.Node[]{toSelect});
            }
        } catch (PropertyVetoException ex) {
            // Ignore
        }
    }

    // ExplorerManager.Provider imlementation ----------------------------------
    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    private void getSelectedHandles(ElementNode.Description description,
            ArrayList<CsmDeclaration> target) {

        //#143049
        if (description == null) {
            return;
        }

        List<ElementNode.Description> subs = description.getSubs();

        if (subs == null) {
            return;
        }

        for (ElementNode.Description d : subs) {
            if (d.isSelectable() && d.isSelected()) {
                target.add(d.getElementHandle());
            } else {
                getSelectedHandles(d, target);
            }
        }
    }

    private Node getSelectedNode(Node[] children) {
        assert children.length > 0 : "array must have elements";
        for (Node node : children) {
            Description descr = node.getLookup().lookup(ElementNode.Description.class);
            if (descr != null && descr.isSelected()) {
                return node;
            }
        }
        return children[0];
    }
}
