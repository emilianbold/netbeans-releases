/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.graph;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LevelOfDetailsWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
class ArtifactWidget extends Widget {
    final Color ROOT = new Color(71, 215, 217);
    final Color DIRECTS = new Color(50, 217, 217);
    final Color DIRECTS_CONFLICT = new Color(235, 88, 194);
    final Color DISABLE_HIGHTLIGHT = new Color(255, 255, 194);
    final Color HIGHTLIGHT = new Color(255, 255, 129);
    final Color DISABLE_CONFLICT = new Color(255, 168, 168);
    final Color CONFLICT = new Color(255, 103, 103);

    final Color PROVIDED = new Color(191, 255, 255);
    final Color COMPILE = new Color(191, 191, 255);
    final Color RUNTIME = new Color(191, 255, 191);
    final Color TEST = new Color(202, 151, 151);

    private Widget defaultCard;
    private Widget hiddenCard;
    Widget label1;
    private ArtifactGraphNode node;
    private String currentSearchTerm;
    private List<String> scopes;

    ArtifactWidget(DependencyGraphScene scene, ArtifactGraphNode node) {
        super(scene);
        this.node = node;

        Artifact artifact = node.getArtifact().getArtifact();
        setLayout(LayoutFactory.createCardLayout(this));

        setToolTipText(NbBundle.getMessage(DependencyGraphScene.class,
                "TIP_Artifact", new Object[]{artifact.getGroupId(),
                    artifact.getArtifactId(), artifact.getVersion(),
                    artifact.getScope(), artifact.getType(), constructConflictText(node)}));
        defaultCard = createCardContent(scene, artifact, true);
        addChild(defaultCard);
        hiddenCard = createCardContent(scene, artifact, false);
        addChild(hiddenCard);
        LayoutFactory.setActiveCard(this, defaultCard);
    }


    public void switchToHidden() {
        LayoutFactory.setActiveCard(this, hiddenCard);
        bringToBack();
        setVisible(true);
        this.revalidate();
    }

    public void switchToDefault() {
        LayoutFactory.setActiveCard(this, defaultCard);
        bringToFront();
        setVisible(true);
        this.revalidate();
    }

    public void checkBackground(ArtifactGraphNode node, Widget widget, boolean shown) {
        if (node.isRoot()) {
            widget.setBackground(ROOT);
        } else {
            Color primaryBackground = Color.WHITE;
            if (scopes != null && scopes.size() > 0 && scopes.contains(node.getArtifact().getArtifact().getScope())) {
                primaryBackground = colorForScope(node.getArtifact().getArtifact().getScope());
            }
            boolean conflict = false;
            for (DependencyNode src : node.getDuplicatesOrConflicts()) {
                if (src.getState() == DependencyNode.OMITTED_FOR_CONFLICT) {
                    conflict = true;
                }
            }
            Rectangle bnds = widget.getBounds();
            int h;
            int w;
                h = 25;
                w = 25;
            if (conflict) {
                if (node.getPrimaryLevel() == 1) {
                    widget.setBackground(new GradientPaint(0, 0, DIRECTS_CONFLICT, w, h, primaryBackground));
                }
                else if (shown) {
                    widget.setBackground(new GradientPaint(0, 0, CONFLICT, w, h, primaryBackground));
                } else {
                    widget.setBackground(new GradientPaint(0, 0, DISABLE_CONFLICT, w, h, primaryBackground));
                }
            } else {
                if (node.getPrimaryLevel() == 1) {
                    widget.setBackground(new GradientPaint(0, 0, DIRECTS, w, h, primaryBackground));
                } else {
                    widget.setBackground(primaryBackground);
                }
            }
        }
    }

    void hightlightText(String searchTerm) {
        this.currentSearchTerm = searchTerm;
        doHightlightText(searchTerm, hiddenCard);
        doHightlightText(searchTerm, defaultCard);
    }

    private String constructConflictText(ArtifactGraphNode node) {
        String toRet = "";
        for (DependencyNode nd : node.getDuplicatesOrConflicts()) {
            if (nd.getState() == DependencyNode.OMITTED_FOR_CONFLICT) {
                if (toRet.length() == 0) {
                    toRet = "<b>Conflicts with:</b><table><thead><tr><th>Version</th><th>Artifact</th></tr></thead><tbody>";
                }
                toRet = toRet + "<tr><td>" + nd.getArtifact().getVersion() + "</td>";
                toRet = toRet + "<td>" + nd.getParent().getArtifact().getId() + "</td></tr>";
            }
        }
        if (toRet.length() > 0) {
            toRet = toRet + "</tbody></table>";
        }
        return toRet;
    }

    private void doHightlightText(String searchTerm, Widget wid) {
        LabelWidget firstChild = (LabelWidget) wid.getChildren().get(0);
        boolean hidden = wid == hiddenCard;
        if (searchTerm != null && node.getArtifact().getArtifact().getArtifactId().contains(searchTerm)) {
            if (hidden) {
                firstChild.setBackground(DISABLE_HIGHTLIGHT);
            } else {
                firstChild.setBackground(HIGHTLIGHT);
            }
            firstChild.setOpaque(true);
        } else {
            //reset
            firstChild.setBackground(Color.WHITE);
            firstChild.setOpaque(false);
        }
    }

    void hightlightScopes(List<String> scopes) {
        this.scopes = scopes;
        checkBackground(node, defaultCard, true);
        checkBackground(node, hiddenCard, false);
    }



    private Color colorForScope(String scope) {
        if (Artifact.SCOPE_COMPILE.equals(scope)) {
            return COMPILE;
        }
        if (Artifact.SCOPE_PROVIDED.equals(scope)) {
            return PROVIDED;
        }
        if (Artifact.SCOPE_RUNTIME.equals(scope)) {
            return RUNTIME;
        }
        if (Artifact.SCOPE_TEST.equals(scope)) {
            return TEST;
        }
        return Color.BLACK;
    }


    private Widget createCardContent(DependencyGraphScene scene, Artifact artifact, boolean shown) {
        Widget root = new LevelOfDetailsWidget(scene, 0.05, 0.1, Double.MAX_VALUE, Double.MAX_VALUE);
        if (shown) {
            root.setBorder(BorderFactory.createLineBorder(10));
        } else {
            root.setBorder(BorderFactory.createLineBorder(10,Color.lightGray));
        }
        root.setOpaque(true);
        root.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 1));
        LabelWidget lbl = new LabelWidget(scene);
        lbl.setLabel(artifact.getArtifactId() + "  ");
        if (!shown) {
            lbl.setForeground(Color.lightGray);
        }
//            lbl.setFont(scene.getDefaultFont().deriveFont(Font.BOLD));
        root.addChild(lbl);
        label1 = lbl;
        Widget details1 = new LevelOfDetailsWidget(scene, 0.5, 0.7, Double.MAX_VALUE, Double.MAX_VALUE);
        details1.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 1));
        root.addChild(details1);
        LabelWidget lbl2 = new LabelWidget(scene);
        lbl2.setLabel(artifact.getVersion() + "  ");
        if (!shown) {
            lbl2.setForeground(Color.lightGray);
        }
        details1.addChild(lbl2);
        checkBackground(node, root, shown);
        return root;
    }
}
