/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */

package org.netbeans.modules.kenai.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author Milan Kubec
 */
public class MultiLabel extends JLabel {

    private String text;
    private String pattern;
    private int numLines = 0;

    private List<String[]> textInLines = null;

    private int realNumLines;

    private int componentWidth = 0;

    private static final int TEXT_GAP = 15;

    private static final Font FONT = UIManager.getDefaults().getFont("Label.font"); // NOI18N
    private static final FontMetrics FM = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).getGraphics().getFontMetrics(FONT);

    public MultiLabel() {
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setHighlightPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setNumLines(int numLines) {
        this.numLines = numLines;
    }

    @Override
    public void paint(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        if (componentWidth == 0 && textInLines == null) {
            componentWidth = getViewportWidth();
            textInLines = text2Lines(componentWidth);
            realNumLines = textInLines.size();
        }

        Iterator<String[]> iter = textInLines.iterator();
        int lineNum = 0;
        while (iter.hasNext()) {
            lineNum++;
            String [] w = iter.next();
            StringBuffer sb = new StringBuffer();
            for (String s : w) {
                sb.append(s);
                sb.append(" "); // NOI18N
            }
            g2d.drawString(sb.toString(), 0, lineNum * FM.getHeight());
        }

    }

    private List<String[]> text2Lines(int width) {

        List<String[]> lines = new ArrayList<String[]>();
        List<String> line = new ArrayList<String>();
        int currWidth = 0;
        for (String word : text.split("\\s")) { // NOI18N
            int wordWidth = SwingUtilities.computeStringWidth(FM, word + " "); // NOI18N
            if (currWidth + wordWidth < width - TEXT_GAP) {
                line.add(word);
                currWidth += wordWidth;
            } else {
                lines.add(line.toArray(new String[line.size()]));
                currWidth = wordWidth;
                line = new ArrayList<String>();
                line.add(word);
            }
        }
        if (!line.isEmpty()) {
            lines.add(line.toArray(new String[line.size()]));
        }
        return lines;
        
    }

    private int getViewportWidth() {
        Container parent = getParent();
        int width = 0;
        while (parent != null) {
            if (parent instanceof JViewport) {
                width = parent.getWidth();
                break;
            }
            parent = parent.getParent();
        }
        return width;
    }

    // ----------

    @Override
    public Dimension getPreferredSize() {
        if (componentWidth == 0 && textInLines == null) {
            componentWidth = getViewportWidth();
            textInLines = text2Lines(componentWidth);
            realNumLines = textInLines.size();
        }
        return new Dimension(componentWidth, realNumLines * FM.getHeight() + FM.getMaxDescent());
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        componentWidth = preferredSize.width;
    }

    @Override
    public Dimension getSize() {
        return super.getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    // ----------

    private String stripHTML(String htmlSnippet) {
        String res = htmlSnippet.replaceAll("<[^>]*>", ""); // NOI18N
        res = res.replaceAll("&nbsp;", " "); // NOI18N
        return res.trim();
    }

    private String getSubstrWithElipsis(String text, FontMetrics fm, int reqWidth, float charWidth, Graphics2D context) {

        int textCharLen = text.length();
        int mIndex = textCharLen;

        int textPixWidth = (int) fm.getStringBounds(text, 0, mIndex, context).getWidth();

        // text is already smaller than required width
        if (reqWidth > textPixWidth) {
            return text;
        }

        // find longest possible substring that would fit into the required
        // width by binary division over text length
        while (Math.abs(reqWidth - textPixWidth) > charWidth) {

            textCharLen = textCharLen == 1 ? 1 : textCharLen / 2;

            if (reqWidth - textPixWidth < 0) {
                mIndex = mIndex - textCharLen;
            } else {
                mIndex = mIndex + textCharLen;
            }

            textPixWidth = (int) fm.getStringBounds(text, 0, mIndex, context).getWidth();

        }

        return text.substring(0, mIndex) + "..."; // NOI18N

    }

}
