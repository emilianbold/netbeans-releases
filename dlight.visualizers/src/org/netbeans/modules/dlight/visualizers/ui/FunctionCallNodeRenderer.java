/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.visualizers.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.swing.etable.ETable;
import org.openide.awt.HtmlRenderer;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;

/**
 *
 * @author ak119685
 */
public final class FunctionCallNodeRenderer extends DefaultTableCellRenderer {

    private static final Color htmlEnabledForeground = getColor("FormattedTextField.foreground", Color.BLACK); // NOI18N
    private static final Color htmlDisabledForeground = getColor("FormattedTextField.inactiveForeground", Color.GRAY); // NOI18N
    private static final Color tooltipBG = getColor("ToolTip.background", Color.YELLOW); // NOI18N
    private final static String dots = " ... "; // NOI18N
    private final Graphics2D scratchGraphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).createGraphics();
    private FunctionCallNode node;
    private int cellwidth;
    private int cellheight;
    private final ExplorerManager manager;

    private static Color getColor(String propName, Color defaultColor) {
        Color result = UIManager.getDefaults().getColor(propName);
        return result == null ? defaultColor : result;
    }

    // TODO: seems not good idea to pass ExplorerManager here ...
    public FunctionCallNodeRenderer(ExplorerManager manager) {
        super();
        this.manager = manager;
        setVerticalAlignment(javax.swing.SwingConstants.TOP);
    }

    @Override
    public String getToolTipText() {
        return ensureVisible(node.getHtmlDisplayName(), tooltipBG);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Even when this renderer is set as default for any object,
        // we need to call super, as it sets bacgrounds and does some other
        // things...
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (table instanceof ETable) {
            row = ((ETable) table).convertRowIndexToModel(row);
            Node n = manager.getRootContext().getChildren().getNodeAt(row);
            if (n instanceof FunctionCallNode) {
                node = (FunctionCallNode) n;
                setText(ensureVisible(node.getHtmlDisplayName(), getBackground()));
            }
        }

        return this;
    }

    /**
     * see IZ#176678 do not wrap lines in TreeCellRenderer if it's html
     * To make html renderer not to wrap the line - just extend width
     * to be large enough to fit all the text...
     *
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        int strw = 0;
        if (width > 0 && height > 0) {
            cellwidth = width;
            cellheight = height;
            // Avoid html wrapping - make sure that string fits
            strw = (int) HtmlRenderer.renderHTML(node.getHtmlDisplayName() + ' ',
                    scratchGraphics,
                    x, y, width, height, getFont(),
                    Color.black, HtmlRenderer.STYLE_CLIP, false);
        }
        super.setBounds(x, y, Math.max(width, strw) + 10, height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        FontMetrics fm = g.getFontMetrics();
        int strw = (int) HtmlRenderer.renderHTML(node.getHtmlDisplayName() + ' ',
                scratchGraphics, 0, 0, cellwidth, 0,
                getFont(), Color.black, HtmlRenderer.STYLE_CLIP, false);

        if (cellwidth < strw) {
            int dotsw = (int) g.getFontMetrics().getStringBounds(dots, g).getMaxX();
            ((Graphics2D) g).setBackground(getBackground());
            g.setColor(getContrastGrayColor(htmlDisabledForeground, getBackground()));
            g.clearRect(cellwidth - dotsw, 0, dotsw, cellheight);
            g.drawString(dots, cellwidth - dotsw,
                    fm.getHeight() + fm.getLeading() - fm.getDescent());
        }
    }

    private Color getContrastGrayColor(Color orig, Color bg) {
        int rgb = orig.getRGB();

        int orig_gray = (((rgb >> 16) & 0xff)
                + ((rgb >> 8) & 0xff)
                + (rgb & 0xff)) / 3;

        rgb = bg.getRGB();

        int bg_gray = (((rgb >> 16) & 0xff)
                + ((rgb >> 8) & 0xff)
                + (rgb & 0xff)) / 3;

        if (Math.abs(orig_gray - bg_gray) > 100) {
            return new Color(orig_gray, orig_gray, orig_gray);
        }

        int avg = bg_gray > 128 ? bg_gray - 100 : bg_gray + 100;

        return new Color(avg, avg, avg);
    }

    private String ensureVisible(String html, Color bg) {
        Color black = getContrastGrayColor(htmlEnabledForeground, bg);
        Color gray = getContrastGrayColor(htmlDisabledForeground, bg);

        String sblack = String.format("color='#%02x%02x%02x'", black.getRed(), black.getGreen(), black.getBlue()); // NOI18N
        String sgray = String.format("color='#%02x%02x%02x'", gray.getRed(), gray.getGreen(), gray.getBlue()); // NOI18N

        html = html.replace("color='#000000'", sblack); // NOI18N
        return html.replace("color='#808080'", sgray); // NOI18N
    }
}
