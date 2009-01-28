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
    final Color DIRECTS = new Color(154, 215, 217);
    final Color DISABLE_HIGHTLIGHT = new Color(255, 255, 194);
    final Color HIGHTLIGHT = new Color(255, 255, 129);
    final Color DISABLE_CONFLICT = new Color(255, 168, 168);
    final Color CONFLICT = new Color(255, 103, 103);

    private Widget defaultCard;
    private Widget hiddenCard;
    Widget label1;
    private ArtifactGraphNode node;
    private String currentSearchTerm;

    ArtifactWidget(DependencyGraphScene scene, ArtifactGraphNode node) {
        super(scene);
        this.node = node;

        Artifact artifact = node.getArtifact().getArtifact();
        setLayout(LayoutFactory.createCardLayout(this));

        setToolTipText(NbBundle.getMessage(DependencyGraphScene.class,
                "TIP_Artifact", new Object[]{artifact.getGroupId(),
                    artifact.getArtifactId(), artifact.getVersion(),
                    artifact.getScope(), artifact.getType()}));
        defaultCard = createCardContent(scene, artifact, true);
        addChild(defaultCard);
        hiddenCard = createCardContent(scene, artifact, false);
        addChild(hiddenCard);
        LayoutFactory.setActiveCard(this, defaultCard);
    }


    public void switchToHidden() {
        LayoutFactory.setActiveCard(this, hiddenCard);
        setVisible(true);
        this.revalidate();
    }

    public void switchToDefault() {
        LayoutFactory.setActiveCard(this, defaultCard);
        setVisible(true);
        this.revalidate();
    }

    public void checkBackground(ArtifactGraphNode node, Widget widget, boolean shown) {
        if (node.isRoot()) {
            widget.setBackground(new GradientPaint(0, 0, ROOT, 100, 50, Color.WHITE));
        } else if (node.getPrimaryLevel() == 1) {
            widget.setBackground(new GradientPaint(0, 0, DIRECTS, 15, 15, Color.WHITE));
        } else {
            boolean conflict = false;
            for (DependencyNode src : node.getDuplicatesOrConflicts()) {
                if (src.getState() == DependencyNode.OMITTED_FOR_CONFLICT) {
                    conflict = true;
                }
            }
            if (conflict) {
                if (shown) {
                    widget.setBackground(new GradientPaint(0, 0, CONFLICT, 15, 15, Color.WHITE));
                } else {
                    widget.setBackground(new GradientPaint(0, 0, DISABLE_CONFLICT, 15, 15, Color.WHITE));
                }
            } else {
                widget.setBackground(Color.WHITE);
            }
        }
    }

    void hightlightText(String searchTerm) {
        this.currentSearchTerm = searchTerm;
        doHightlightText(searchTerm, hiddenCard);
        doHightlightText(searchTerm, defaultCard);
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

    private Widget createCardContent(DependencyGraphScene scene, Artifact artifact, boolean shown) {
        Widget root = new LevelOfDetailsWidget(scene, 0.05, 0.1, Double.MAX_VALUE, Double.MAX_VALUE);
        if (shown) {
            root.setBorder(BorderFactory.createLineBorder(10));
        } else {
            root.setBorder(BorderFactory.createLineBorder(10,Color.lightGray));
        }
        checkBackground(node, root, shown);
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
        return root;
    }
}
