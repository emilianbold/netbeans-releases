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
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.Timer;
import javax.swing.UIManager;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.netbeans.api.visual.action.TwoStateHoverProvider;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LevelOfDetailsWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
class ArtifactWidget extends Widget implements TwoStateHoverProvider, ActionListener {

    final Color ROOT = new Color(178, 228, 255);
    final Color DIRECTS = new Color(178, 228, 255);
    final Color DIRECTS_CONFLICT = new Color(235, 88, 194);
    final Color DISABLE_HIGHTLIGHT = new Color(255, 255, 194);
    final Color HIGHTLIGHT = new Color(255, 255, 129);
    final Color DISABLE_CONFLICT = new Color(219, 155, 153);
    final Color CONFLICT = new Color(219, 11, 5);
    final Color MANAGED = new Color(30, 255, 150);
    final Color OVERRIDES_MANAGED = new Color(255, 150, 20);

    final Color PROVIDED = new Color(191, 255, 255);
    final Color COMPILE = new Color(191, 191, 255);
    final Color RUNTIME = new Color(191, 255, 191);
    final Color TEST = new Color(202, 151, 151);

    private static final int LEFT_TOP = 1;
    private static final int LEFT_BOTTOM = 2;
    private static final int RIGHT_TOP = 3;
    private static final int RIGHT_BOTTOM = 4;

    private static final int UNHOVERED = 1;
    private static final int HOVERED = 2;
    private static final int ENSURE_READABLE = 3;

    private static final int UNSELECTED = 1;
    private static final int SELECTED = 2;
    private static final int READABLE = 3;

    private static final Image lockImg = ImageUtilities.loadImage("org/netbeans/modules/maven/graph/lock.png");
    private static final Image brokenLockImg = ImageUtilities.loadImage("org/netbeans/modules/maven/graph/lock-broken.png");

    private ArtifactGraphNode node;
    private String currentSearchTerm;
    private List<String> scopes;
    private int hoverState = UNHOVERED;
    private int selectState = UNSELECTED;

    private Timer hoverTimer;
    private Color hoverBorderC;

    private LabelWidget artifactW, versionW;
    private Widget detailsW, contentW;
    private ImageWidget lockW, hintBulbW;

    private boolean grayed = false;

    private Font origFont;
    private Color origForeground;

    ArtifactWidget(DependencyGraphScene scene, ArtifactGraphNode node) {
        super(scene);
        this.node = node;

        Artifact artifact = node.getArtifact().getArtifact();
        setLayout(LayoutFactory.createVerticalFlowLayout());

        setToolTipText(NbBundle.getMessage(DependencyGraphScene.class,
                "TIP_Artifact", new Object[]{artifact.getGroupId(),
                    artifact.getArtifactId(), artifact.getVersion(),
                    artifact.getScope(), artifact.getType(), constructConflictText(node)}));
        initContent(scene, artifact);

        hoverTimer = new Timer(500, this);
        hoverTimer.setRepeats(false);

        hoverBorderC = UIManager.getColor("TextPane.selectionBackground");
        if (hoverBorderC == null) {
            hoverBorderC = Color.GRAY;
        }
    }

    void hightlightText(String searchTerm) {
        this.currentSearchTerm = searchTerm;
        doHightlightText(searchTerm);
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

    private void doHightlightText(String searchTerm) {
        if (searchTerm != null && node.getArtifact().getArtifact().getArtifactId().contains(searchTerm)) {
            if (grayed) {
                artifactW.setBackground(DISABLE_HIGHTLIGHT);
            } else {
                artifactW.setBackground(HIGHTLIGHT);
            }
            artifactW.setOpaque(true);
        } else {
            //reset
            artifactW.setBackground(Color.WHITE);
            artifactW.setOpaque(false);
        }
    }

    void hightlightScopes(List<String> scopes) {
        this.scopes = scopes;
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

    void setGrayed (boolean grayed) {
        if (this.grayed == grayed) {
            return;
        }
        this.grayed = grayed;

        if (origForeground == null) {
            origForeground = getForeground();
        }

        Color c = grayed ? UIManager.getColor("textInactiveText") : origForeground;
        if (c == null) {
            c = Color.LIGHT_GRAY;
        }

        contentW.setBorder(BorderFactory.createLineBorder(10, c));
        artifactW.setForeground(c);
        versionW.setForeground(c);
        if (lockW != null) {
            lockW.setPaintAsDisabled(grayed);
        }

        contentW.repaint();
    }

    private void initContent (DependencyGraphScene scene, Artifact artifact) {
        contentW = new LevelOfDetailsWidget(scene, 0.05, 0.1, Double.MAX_VALUE, Double.MAX_VALUE);
        contentW.setBorder(BorderFactory.createLineBorder(10));
        contentW.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 1));
        artifactW = new LabelWidget(scene);
        artifactW.setLabel(artifact.getArtifactId() + "  ");
        if (node.isRoot()) {
            Font defF = getOrigFont();
            artifactW.setFont(defF.deriveFont(Font.BOLD, defF.getSize() + 3f));
        }
        contentW.addChild(artifactW);
        Widget versionDetW = new LevelOfDetailsWidget(scene, 0.5, 0.7, Double.MAX_VALUE, Double.MAX_VALUE);
        versionDetW.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 2));
        contentW.addChild(versionDetW);
        versionW = new LabelWidget(scene);
        versionW.setLabel(artifact.getVersion());
        int mngState = node.getManagedState();
        if (mngState != ArtifactGraphNode.UNMANAGED) {
             lockW = new ImageWidget(scene,
                    mngState == ArtifactGraphNode.MANAGED ? lockImg : brokenLockImg);
        }
        versionDetW.addChild(versionW);
        if (lockW != null) {
            versionDetW.addChild(lockW);
        }
        addChild(contentW);
    }

    @Override
    protected void paintBackground() {
        super.paintBackground();
        Graphics2D g = getScene().getGraphics();
        Rectangle bounds = getClientArea();

        if (node.isRoot()) {
            paintBottom(g, bounds, ROOT, Color.WHITE, bounds.height / 2);
        } else {
            if (scopes != null && scopes.size() > 0 && scopes.contains(node.getArtifact().getArtifact().getScope())) {
                Color scopeC = colorForScope(node.getArtifact().getArtifact().getScope());
                paintCorner(RIGHT_BOTTOM, g, bounds, scopeC, Color.WHITE, bounds.width / 2, bounds.height / 2);
            }
            boolean conflict = false;
            for (DependencyNode src : node.getDuplicatesOrConflicts()) {
                if (src.getState() == DependencyNode.OMITTED_FOR_CONFLICT) {
                    conflict = true;
                }
            }
            Color leftTopC = Color.WHITE;
            if (conflict) {
                leftTopC = grayed ? DISABLE_CONFLICT : CONFLICT;
            } else {
                int state = node.getManagedState();
                if (ArtifactGraphNode.OVERRIDES_MANAGED == state) {
                    leftTopC = OVERRIDES_MANAGED;
                }
            }
            paintCorner(LEFT_TOP, g, bounds, leftTopC, Color.WHITE, bounds.width, bounds.height / 2);

            if (node.getPrimaryLevel() == 1) {
                paintBottom(g, bounds, DIRECTS, Color.WHITE, bounds.height / 6);
            }
        }

        if (hoverState != UNHOVERED) {
            paintHover(g, bounds, hoverBorderC);
        }
    }

    private static void paintCorner (int corner, Graphics2D g, Rectangle bounds,
            Color c1, Color c2, int x, int y) {
        double h = y*y + x*x;
        int gradX = (int)(y*y*x / h);
        int gradY = (int)(y*x*x / h);

        Point startPoint = new Point();
        Point direction = new Point();
        switch (corner) {
            case LEFT_TOP:
                startPoint.x = bounds.x;
                startPoint.y = bounds.y;
                direction.x = 1;
                direction.y = 1;
            break;
            case LEFT_BOTTOM:
                startPoint.x = bounds.x;
                startPoint.y = bounds.y + bounds.height;
                direction.x = 1;
                direction.y = -1;
            break;
            case RIGHT_TOP:
                startPoint.x = bounds.x + bounds.width;
                startPoint.y = bounds.y;
                direction.x = -1;
                direction.y = 1;
            break;
            case RIGHT_BOTTOM:
                startPoint.x = bounds.x + bounds.width;
                startPoint.y = bounds.y + bounds.height;
                direction.x = -1;
                direction.y = -1;
            break;
            default:
                throw new IllegalArgumentException("Corner id not valid"); //NOI18N
        }
        
        g.setPaint(new GradientPaint(startPoint.x, startPoint.y, c1,
                startPoint.x + direction.x * gradX,
                startPoint.y + direction.y * gradY, c2));
        g.fillRect(
                Math.min(startPoint.x, startPoint.x + direction.x * x),
                Math.min(startPoint.y, startPoint.y + direction.y * y),
                x, y);
    }

    private static void paintBottom (Graphics2D g, Rectangle bounds, Color c1, Color c2, int thickness) {
        g.setPaint(new GradientPaint(bounds.x, bounds.y + bounds.height, c1,
                bounds.x, bounds.y + bounds.height - thickness, c2));
        g.fillRect(bounds.x, bounds.y + bounds.height - thickness, bounds.width, thickness);
    }

    private static void paintHover (Graphics2D g, Rectangle bounds, Color c) {
        g.setColor(c);
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g.drawRect(bounds.x + 1, bounds.y + 1, bounds.width - 2, bounds.height - 2);
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 150));
        g.drawRect(bounds.x + 2, bounds.y + 2, bounds.width - 4, bounds.height - 4);
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 75));
        g.drawRect(bounds.x + 3, bounds.y + 3, bounds.width - 6, bounds.height - 6);
    }

    /*** TwoStateHoverProvider ***/

    public void unsetHovering(Widget widget) {
        hoverTimer.stop();
        int previous = hoverState;
        hoverState = UNHOVERED;
        if (previous == HOVERED) {
            repaint();
        } else if (previous == ENSURE_READABLE) {
            artifactW.setPreferredBounds(artifactW.getPreferredBounds());
            updateFonts();
            getScene().getSceneAnimator().animatePreferredBounds(artifactW, null);
        }
    }

    public void setHovering(Widget widget) {
        hoverState = HOVERED;
        hoverTimer.restart();
        repaint();
    }

    /*** ActionListener ***/

    public void actionPerformed(ActionEvent e) {
        hoverState = ENSURE_READABLE;
        artifactW.setPreferredBounds(artifactW.getPreferredBounds());
        bringToFront();
        updateFonts();
        getScene().getSceneAnimator().animatePreferredBounds(artifactW, null);
    }

    public void setSelected (boolean select) {
        this.selectState = select ? SELECTED : UNSELECTED;
        updateFonts();
    }

    public boolean isSelected () {
        return selectState == SELECTED;
    }

    public void setReadable (boolean readable) {
        this.selectState = readable ? READABLE : UNSELECTED;
        updateFonts();
    }

    public boolean isReadable () {
        return selectState == READABLE;
    }

    private void updateFonts () {
        Font f;
        Font origF = getOrigFont();
        if (hoverState == ENSURE_READABLE || selectState != UNSELECTED) {
            // enlarge fonts so that content is readable
            float fSizeRatio = getScene().getDefaultFont().getSize() / (float)origF.getSize();
            float ratio = (float) Math.max (1, fSizeRatio / Math.max(0.0001f, getScene().getZoomFactor()));
            f = origF.deriveFont(origF.getSize() * ratio);
        } else {
            f = origF;
        }
        artifactW.setFont(f);
        versionW.setFont(f);
    }

    private Font getOrigFont () {
        if (origFont == null) {
            origFont = artifactW.getFont();
            if (origFont == null) {
                origFont = getScene().getDefaultFont();
            }
        }
        return origFont;
    }

}
