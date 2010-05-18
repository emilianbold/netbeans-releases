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


package org.netbeans.modules.visualweb.designer.jsf.virtualforms;


import com.sun.rave.designtime.markup.MarkupDesignBean;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Toolkit;
import javax.faces.component.UIComponent;
import javax.swing.UIManager;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.ext.componentgroup.ColorWrapper;
import com.sun.rave.designtime.ext.componentgroup.ComponentGroup;
import com.sun.rave.designtime.ext.componentgroup.ComponentGroupHolder;
import com.sun.rave.designtime.ext.componentgroup.ComponentSubset;
import com.sun.rave.designtime.ext.componentgroup.impl.ColorWrapperImpl;
import com.sun.rave.designtime.ext.componentgroup.util.ComponentGroupHelper;
import javax.faces.component.NamingContainer;
import org.netbeans.modules.visualweb.api.designer.Designer;
import org.w3c.dom.Element;


/**
 * This class implements most of the support for Component Groups
 * in the designer.
 *
 * @todo App outline
 *
 * @author Tor Norbye
 * @author mbohm
 */
public class ComponentGroupSupport {
    /** Width of the drop shadow (if 0, don't paint one) */
    private static final int DROP_SHADOW_WIDTH = 4;

    /** Border thickness painted around components */
    private static final int THICKNESS = 2;

    /** Size to add to font height to make each line in the legend */
    private static final int EXTRA_LINE_SPACING = 2;

    /** Legend contents offset from the outer border */
    private static final int MARGIN = 4;

    /** Distance between color square and description text */
    private static final int SPACING = 6;

    /** Size of the sides of the color-box shown in the legend next to each form name.
     * This should be a constant, but can be adjusted in extreme cases where the
     * look and feel fontsize is smaller than the requested value.
     */
    private static int colorBoxSize = 9;

    /** Distance from right side viewport edge to the legend righthand side -
      * ditto for bottom */
    private static final int LEGEND_OFFSET = 5 + DROP_SHADOW_WIDTH;

    /** Creates a new instance of VirtualFormSupport */
    private ComponentGroupSupport() {
    }

    /*
     * <p>Paint legends and the colored borders of appropriate components.</p>
     */
    public static void paint(LiveUnit liveUnit, Designer.RenderContext renderContext, Graphics2D g2d,
    boolean showVirtualForms, boolean showAjaxTransactions) {
        ComponentGroupHolder[] holders = ComponentGroupHelper.getComponentGroupHolders(liveUnit);

        if (holders == null || holders.length == 0) {
            return;
        }
        
        ComponentGroup[][] groupArr = new ComponentGroup[holders.length][];

        Dimension d = renderContext.getVieportDimension();
        Point p = renderContext.getViewportPosition();

        int maxX = (p.x + d.width) - LEGEND_OFFSET;
        int maxY = (p.y + d.height) - LEGEND_OFFSET;
        
        DesignBean paintChildrenRootBean = liveUnit.getRootContainer(); //the top bean whose children we will walk when we call paintChildren
        String paintChildrenRootBeanServerId = String.valueOf(NamingContainer.SEPARATOR_CHAR) + paintChildrenRootBean.getInstanceName();
        
        ComponentGroupHelper.populateColorGroupArray(liveUnit, holders, groupArr);
        
        for (int h = 0; h < holders.length; h++) {
            ComponentGroupHolder holder = holders[h];

            if (holder == null) {
                continue;
            }

            ComponentGroup[] groups = groupArr[h];

            if ((groups == null) || (groups.length == 0)) {
                continue;
            }
            
            paintChildren(renderContext, paintChildrenRootBean, holders, groupArr, g2d, paintChildrenRootBeanServerId);
            
            // Paint legend
            Font font = (Font)UIManager.getDefaults().get("Label.font");
            FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
            g2d.setFont(font);

            int fontHeight = metrics.getHeight();
            int baseline = metrics.getHeight() - metrics.getDescent();

            if (colorBoxSize > baseline) {
                colorBoxSize = baseline;
            }

            int maxStringWidth = 0;

            for (int i = 0; i < groups.length; i++) {
                String legendEntryLabel = groups[i].getLegendEntryLabel();
                char[] s = legendEntryLabel.toCharArray(); // XXX I should make a utility which operates on Strings directly!
                int stringWidth = renderContext.getNonTabbedTextWidth(s, 0, s.length, metrics);

                if (stringWidth > maxStringWidth) {
                    maxStringWidth = stringWidth;
                }
            }

            maxStringWidth += (colorBoxSize + SPACING);

            String legendText = holder.getLegendLabel();
            char[] s = legendText.toCharArray();
            int stringWidth = renderContext.getNonTabbedTextWidth(s, 0, s.length, metrics);

            if (stringWidth > maxStringWidth) {
                maxStringWidth = stringWidth;
            }

            int height = (2 * MARGIN) + ((groups.length + 1) * (fontHeight + EXTRA_LINE_SPACING));
            int width = (2 * MARGIN) + maxStringWidth;
            g2d.setColor(Color.BLACK);

            // XXX check for scrolling!
            int x = maxX - width;
            int y = maxY - height;
            
            g2d.drawRect(x, y, width, height);

            // Header
            g2d.setColor(new Color(0, 0, 255, 128));
            g2d.fillRect(x + 1, y + 1, width - 1, fontHeight + EXTRA_LINE_SPACING);

            g2d.setColor(new Color(192, 192, 192, 128));
            g2d.fillRect(x + 1, y + 1 + fontHeight + EXTRA_LINE_SPACING, width - 1,
                height - 1 - fontHeight - EXTRA_LINE_SPACING);

            if (DROP_SHADOW_WIDTH > 0) {
                int alphaIncrement = 128 / DROP_SHADOW_WIDTH;
                int alpha = 128;

                for (int sw = 0; sw < DROP_SHADOW_WIDTH; sw++, alpha -= alphaIncrement) {
                    g2d.setColor(new Color(0, 0, 0, alpha));

                    int tx = x + 1 + sw;
                    int ty = y + 1 + sw + height;
                    g2d.drawLine(tx, ty, tx + width, ty);
                    tx = x + width + 1 + sw;
                    ty = y + 1 + sw;
                    g2d.drawLine(tx, ty, tx, ty + height);
                }
            }

            // TODO - instead of starting at y+1, start after the header!
            x += MARGIN;
            y += (EXTRA_LINE_SPACING / 2);
            y += (MARGIN / 2);

            g2d.setColor(Color.WHITE);
            g2d.drawString(legendText, x, y + baseline);
            y += (fontHeight + EXTRA_LINE_SPACING);
            y += (MARGIN / 2);

            int boxOffset = fontHeight - baseline;

            for (int i = 0; i < groups.length; i++) {
                // Draw color label
                String legendEntryLabel = groups[i].getLegendEntryLabel();
                g2d.setColor(Color.BLACK);
                g2d.drawString(legendEntryLabel, x + colorBoxSize + SPACING, y + baseline);

                g2d.drawRect(x, y + boxOffset, colorBoxSize, colorBoxSize);

                Color color = groups[i].getColor();
                assert color != null;
                g2d.setColor(color);
                g2d.fillRect(x + 1, y + boxOffset + 1, colorBoxSize - 1, colorBoxSize - 1);

                y += (fontHeight + EXTRA_LINE_SPACING);
                
                //throw this group's color information into the contextData, if not already stored
                String key = ComponentGroupHelper.getComponentGroupColorKey(holder.getName(), groups[i].getName());
                boolean colorAlreadyStored = false;
                Object colorWrapperObj = liveUnit.getContextData(key);
                if (colorWrapperObj instanceof ColorWrapper) {
                    ColorWrapper wrapper = (ColorWrapper)colorWrapperObj;
                    if (color.equals(wrapper.getColor())) {
                        colorAlreadyStored = true;
                    }
                }
                if (!colorAlreadyStored) {
                    liveUnit.setContextData(key, new ColorWrapperImpl(color));
                }
            }

            maxX -= (width + LEGEND_OFFSET); // in case there are more forms on this page
        }
    }

    private static void paintChildren(Designer.RenderContext renderContext,
    DesignBean parent, ComponentGroupHolder[] holders, ComponentGroup[][] groupArr, Graphics2D g2d, String precedingIds) {
        DesignBean[] childBeans = parent.getChildBeans();
        if (childBeans == null || childBeans.length == 0) {
            return;
        }

        for (int c = 0; c < childBeans.length; c++) {
            //get child bean and its instance
            DesignBean child = childBeans[c];

            if (child == null) {
                continue;
            }

            Object childInstance = child.getInstance();

            if (childInstance == null) {
                continue;
            }
            
            String childFqId = precedingIds;
            
            if (childInstance instanceof UIComponent) {
            
                childFqId = precedingIds + NamingContainer.SEPARATOR_CHAR + child.getInstanceName();

                //paint boxes around this child for each group
                if (childInstance instanceof UIComponent) {
                    int nestingLevel = 0;

                    for (int h = 0; h < holders.length; h++) {
                        ComponentGroup[] groups = groupArr[h];

                        if (groups == null || groups.length == 0) {
                            continue;
                        }

                        for (int g = 0; g < groups.length; g++) {
                            ComponentGroup group = groups[g];

                            if (group == null) {
                                continue;
                            }

                            String groupName = group.getName();

                            if (groupName == null) {
                                continue;
                            }

                            Color color = group.getColor();

                            if (color == null) {
                                continue;
                            }

                            boolean paintSolid = false;
                            boolean paintDashed = false;
                            ComponentSubset[] subsets = group.getComponentSubsets();
                            if (subsets != null) {
                                for (int s = 0; s < subsets.length; s++) {
                                    ComponentSubset subset = subsets[s];
                                    ComponentSubset.LineType lineType = subset.getLineType();
                                    if (lineType == ComponentSubset.LineType.NONE) {
                                        continue;
                                    }
                                    String[] members = subset.getMembers();
                                    if (members != null) {
                                        for (int m = 0; m < members.length; m++) {
                                            if (childFqId.endsWith(members[m])) {
                                                if (lineType == ComponentSubset.LineType.SOLID) {
                                                    paintSolid = true;
                                                    break;
                                                }
                                                else if (lineType == ComponentSubset.LineType.DASHED) {
                                                    paintDashed = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (paintSolid || paintDashed) {
                                paintHighlight(renderContext, g2d, child, color, paintSolid, paintDashed,
                                    nestingLevel);
                                nestingLevel++;
                            }
                        }
                    }
                }
            }

            //this child is all grown up and is now a parent. paint its children.
            paintChildren(renderContext, child, holders, groupArr, g2d, childFqId);
        }
    }

    private static void paintHighlight(Designer.RenderContext renderContext,
    Graphics2D g2d, DesignBean bean, Color color, boolean paintSolid, boolean paintDashed, int nestingLevel) {
        if (bean != null) {
            Element componentRootElement;
            if (bean instanceof MarkupDesignBean) {
                Element sourceElement = ((MarkupDesignBean)bean).getElement();
                componentRootElement = MarkupService.getRenderedElementForElement(sourceElement);
            } else {
                componentRootElement = null;
            }
            Rectangle bounds = renderContext.getBoundsForComponent(componentRootElement);

            if (bounds != null) {
                // Draw a highlight using this box' extents in the given color
                int x = bounds.x;
                int y = bounds.y;
                int w = bounds.width;
                int h = bounds.height;

                // Move outside the bounding box
                x -= 1;
                y -= 1;
                w += 2;
                h += 2;

                // Support nested highlights
                int offset = ((THICKNESS + 1) * nestingLevel) + 2;
                x -= offset;
                y -= offset;
                offset *= 2;
                w += offset;
                h += offset;

                g2d.setColor(color);

                Stroke prevStroke = g2d.getStroke();

                Stroke paintDashedStroke =
                    new BasicStroke(THICKNESS, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL,
                        1.0f, 
                    //new float[] { 2 * THICKNESS, 2 * THICKNESS }, 0.0f);
                    new float[] { 2.25f * THICKNESS, 4.75f * THICKNESS }, 0.0f);

                Stroke paintSolidStroke =
                    new BasicStroke(THICKNESS, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);

                int topLeftX = x;
                int topLeftY = y;
                int topRightX = x + w;
                int topRightY = y;
                int bottomRightX = x + w;
                int bottomRightY = y + h;
                int bottomLeftX = x;
                int bottomLeftY = y + h;

                if (paintSolid) {
                    //bean participates and maybe submits
                    g2d.setStroke(paintSolidStroke);
                } else {
                    //bean only submits
                    g2d.setStroke(paintDashedStroke);
                }

                g2d.drawLine(topLeftX, topLeftY, topRightX, topRightY);
                g2d.drawLine(topLeftX, topLeftY, bottomLeftX, bottomLeftY);

                if (paintDashed) {
                    //bean submits and maybe participates
                    g2d.setStroke(paintDashedStroke);
                } else {
                    //bean only participates
                    g2d.setStroke(paintSolidStroke);
                }

                g2d.drawLine(bottomRightX, bottomRightY, bottomLeftX, bottomLeftY);
                g2d.drawLine(bottomRightX, bottomRightY, topRightX, topRightY);

                g2d.setStroke(prevStroke);
            }
        }
    }
}
