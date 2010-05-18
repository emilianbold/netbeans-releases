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
package org.netbeans.modules.edm.editor.graph.jgo;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.netbeans.modules.edm.editor.graph.jgo.IDataNode;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphPort;
import org.netbeans.modules.edm.editor.graph.jgo.IHighlightConfigurator;
import org.netbeans.modules.edm.editor.graph.jgo.IHighlightable;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class BasicCellArea extends CanvasArea implements PropertyChangeListener, IDataNode {

    /**
     * TEXT Property
     */
    public static final String TEXT = "text";

    /**
     * constant that describe that this cell has ports on both side of it
     */
    public static final int LEFT_RIGHT_PORT_AREA = 0;

    /**
     * constant that describe that this cell has ports on left side of it
     */
    public static final int LEFT_PORT_AREA = 1;

    /**
     * constant that describe that this cell has ports on right side of it
     */
    public static final int RIGHT_PORT_AREA = 2;

    /**
     * constants
     */
    public static final int LEFT = 0;
    public static final int RIGHT = 1;

    public static final int IMAGE_EXTRACTION = 0;
    public static final int IMAGE_VALIDATION = 1;

    public static final int[] IMAGES = new int[] { IMAGE_EXTRACTION, IMAGE_VALIDATION};

    public static final Color DEFAULT_TEXT_COLOR = new Color(30, 70, 230); // navy;

    private static final JGoPen DEFAULT_PEN = JGoPen.makeStockPen(new Color(201, 230, 247));

    protected boolean drawBoundingRect = false;

    // this area has one cell area
    protected CellArea cellArea;

    protected JGoRectangle rect;

    // gap between text and icon
    protected int iconTextGap = 2;

    protected int leftGap = 5;

    private ColumnPortArea leftPortArea;
    private ColumnPortArea rightPortArea;

    // this area has zero or more images also
    private BasicImageArea[] imgs = new BasicImageArea[] { new BasicImageArea() // extraction
            , new BasicImageArea() // validation
    };

    // default this area will have port at both ends
    private int portType = 0;

    // gap between icons
    private int iconIconGap = 2;

    private JGoPen linePen = null;

    private boolean arrowPort = true;

    private Object dataObject;

    /** Creates a new instance of BasicCellArea */
    public BasicCellArea() {
        super();
        this.setSelectable(false);
        this.setResizable(false);
        this.setDraggable(true);

        rect = new JGoRectangle();
        rect.setPen(DEFAULT_PEN);
        rect.setBrush(JGoBrush.makeStockBrush(Color.WHITE));
        rect.setSelectable(false);
        rect.setResizable(false);
        addObjectAtHead(rect);

        this.insets = new Insets(2, 1, 2, 1);

        initImageAreas();
    }

    /**
     * Creates a new instance of BasicCellArea
     * 
     * @param text text
     */
    public BasicCellArea(String text) {
        this();

        // add cell area
        cellArea = new CellArea(text);
        cellArea.addPropertyChangeListener(this);
        this.addObjectAtTail(cellArea);

        this.setSize(this.getMinimumWidth(), this.getMinimumHeight());
    }

    /**
     * Creates a new instance of BasicCellArea Assumes image/Icon is for DataExtraction.
     * 
     * @param text text
     * @param icon icon
     */
    public BasicCellArea(String text, Icon icon) {
        this(text);

        setImageIcon(IMAGE_EXTRACTION, icon);
        this.setSize(this.getMinimumWidth(), this.getMinimumHeight());
    }

    /**
     * Creates a cell area and specify its port. Assumes image/Icon is for DataExtraction.
     * 
     * @param portType type of port
     * @param text text
     * @param icon icon
     */
    public BasicCellArea(int portType, String text, Icon icon) {
        this(portType, text);

        setImageIcon(IMAGE_EXTRACTION, icon);

        // Prepare to handle variable number of Icons
        BasicImageArea bImg = null;

        for (int i = 0; i < imgs.length; i++) {
            if (i != IMAGE_EXTRACTION) {
                bImg = imgs[i];
                bImg.setSelectable(false);
                bImg.setResizable(false);
                this.addObjectAtTail(bImg);
            }
        }

        this.setSize(this.getMinimumWidth(), this.getMinimumHeight());
    }

    /**
     * create a new instance of BasicCellArea
     * 
     * @param portType port type
     * @param text text
     */
    public BasicCellArea(int aPortType, String text) {
        this(text);
        portType = aPortType;

        // add port areas
        if (portType == LEFT_PORT_AREA || portType == LEFT_RIGHT_PORT_AREA) {
            leftPortArea = new ColumnPortArea(ColumnPortArea.LEFT_PORT_AREA, 1);
            leftPortArea.setInsets(new Insets(0, 0, 0, 0));
            this.addObjectAtTail(leftPortArea);
        }
        if (portType == RIGHT_PORT_AREA || portType == LEFT_RIGHT_PORT_AREA) {
            rightPortArea = new ColumnPortArea(ColumnPortArea.RIGHT_PORT_AREA, 1);
            rightPortArea.setInsets(new Insets(0, 0, 0, 0));
            this.addObjectAtTail(rightPortArea);
        }

        this.setSize(this.getMinimumWidth(), this.getMinimumHeight());
    }

    /**
     * set whether text in this area is editable
     * 
     * @param editable text is editable
     */
    public void setTextEditable(boolean editable) {
        cellArea.setTextEditable(editable);
    }

    /**
     * Is the text editable
     * 
     * @return whether the text is editable
     */
    public boolean isTextEditable() {
        return cellArea.isTextEditable();
    }

    /**
     * Sets whether a bounding rectangle needs to be drawn
     * 
     * @param drawRect boolean
     */
    public void drawBoundingRect(boolean drawRect) {
        this.drawBoundingRect = drawRect;
    }

    /**
     * Sets the bounding rectangle's border color
     * 
     * @param color color
     */
    public void setBoundingRectBorderColor(Color color) {
        rect.setPen(JGoPen.makeStockPen(color));
    }

    /**
     * get the maximum width
     * 
     * @return max width
     */
    public int getMaximumWidth() {
        int minWidth = getInsets().left + getInsets().right;
        minWidth += leftGap;

        if (leftPortArea != null) {
            minWidth += leftPortArea.getWidth();
        }
        if (rightPortArea != null) {
            minWidth += rightPortArea.getWidth();
        }
        if (cellArea != null) {
            minWidth += cellArea.getMaximumWidth();
            minWidth += iconTextGap;
        }

        if (imgs != null) {
            BasicImageArea bia = null;
            for (int i = 0; i < imgs.length; i++) {
                bia = imgs[i];
                if (bia != null) {
                    minWidth += bia.getWidth() + iconIconGap;
                }
            }

        }
        return minWidth;
    }

    /**
     * get the minimum width of this cell area
     * 
     * @return min width
     */
    public int getMinimumWidth() {
        int minWidth = getInsets().left + getInsets().right;
        minWidth += leftGap;

        if (leftPortArea != null) {
            minWidth += leftPortArea.getWidth();
        }
        if (rightPortArea != null) {
            minWidth += rightPortArea.getWidth();
        }
        if (cellArea != null) {
            minWidth += cellArea.getMinimumWidth();
            minWidth += iconTextGap;
        }

        if (imgs != null) {
            BasicImageArea bia = null;
            for (int i = 0; i < imgs.length; i++) {
                bia = imgs[i];
                if (bia != null) {
                    minWidth += bia.getWidth() + iconIconGap;
                }
            }
        }

        return minWidth;
    }

    /**
     * get the minimum height of this cell area
     * 
     * @return minimum height of this cell area
     */
    public int getMinimumHeight() {
        int minHeight = getInsets().top + getInsets().bottom;

        int height = 0;
        if (cellArea != null) {
            height = cellArea.getHeight();
        }

        if (imgs != null) {
            BasicImageArea bia = null;
            for (int i = 0; i < imgs.length; i++) {
                bia = imgs[i];
                if ((bia != null) && (height < bia.getHeight())) {
                    height = bia.getHeight();
                }
            }
        }

        minHeight += height;
        return minHeight;
    }

    /**
     * Sets a ImageIcon for the icon Type specified.
     * 
     * @param iconType can be IMAGE_EXTRACTION, IMAGE_VALIDATION etc
     * @param icon
     * @param toolTip
     */
    public void setImageIcon(int iconType, Icon icon, String toolTip) {

        if (iconType >= IMAGES.length) {
            return;
        }

        if (icon != null) {
            if (imgs != null) {
                BasicImageArea imageArea = imgs[iconType];
                if (imageArea != null) {
                    imageArea.setVisible(true);
                    ImageIcon imgIcon = (ImageIcon) icon;
                    imageArea.loadImage(imgIcon.getImage(), false);
                    imageArea.setSize(imgIcon.getImage().getWidth(null), imgIcon.getImage().getHeight(null));
                    if (toolTip != null) {
                        imageArea.setToolTipText(toolTip);
                    }
                }
            }
        } else {
            if (imgs != null) {
                BasicImageArea imageArea = imgs[iconType];
                if (imageArea != null) {
                    imageArea.setVisible(false);
                }
            }
        }

        layoutChildren();
    }

    /**
     * Sets a ImageIcon for the icon Type specified.
     * 
     * @param iconType can be IMAGE_EXTRACTION, IMAGE_VALIDATION etc
     * @param icon
     */
    public void setImageIcon(int iconType, Icon icon) {
        setImageIcon(iconType, icon, null);
    }

    /**
     * Sets image for DataExtraction icon.
     * 
     * @param icon
     */
    public void setDataExtractionImageIcon(Icon icon) {
        setImageIcon(IMAGE_EXTRACTION, icon);
    }

    /**
     * Sets image for DataExtraction icon.
     * 
     * @param icon
     */
    public void setDataExtractionImageIcon(Icon icon, String toolTip) {
        setImageIcon(IMAGE_EXTRACTION, icon, toolTip);
    }

    /**
     * set the image in this cell area
     * 
     * @param icon icon
     * @deprecated 12/28/2004 Use setImage(int iconType, Icon icon) or
     *             setDataExtractionImage(Icon icon).
     */
    public void setImage(Icon icon) {
        setImageIcon(IMAGE_EXTRACTION, icon);
    }

    /**
     * @param icon
     * @param toolTip
     * @deprecated 12/28/2004 Use setImage(int iconType, Icon icon, String toolTip) or
     *             setDataExtractionImage(Icon icon, String toolTip).
     */
    public void setImage(Icon icon, String toolTip) {
        if (icon != null) {
            this.setImageIcon(IMAGE_EXTRACTION, icon, toolTip);
        }
    }

    /**
     * set the text in this cell area
     * 
     * @param text text
     */
    public void setText(String text) {
        cellArea.setText(text);
    }

    /**
     * set the text alignment
     * 
     * @param align text alignment
     */
    public void setTextAlignment(int align) {
        if (cellArea != null) {
            cellArea.setTextAlignment(align);
        }
    }

    /**
     * set the gap between icon and text
     * 
     * @param iconTextGap gap between icon and text
     */
    public void setIconTextGap(int iconTextGap) {
        this.iconTextGap = iconTextGap;
    }

    /**
     * set the gap between icons
     * 
     * @param iconTextGap gap between icons
     */
    public void setIconIconGap(int iconIconGap) {
        this.iconIconGap = iconIconGap;
    }

    /**
     * get gap between icon and text
     * 
     * @return gap between icon and text
     */
    public int getIconTextGap() {
        return iconTextGap;
    }

    /**
     * get gap between the icons
     * 
     * @return gap between icons
     */
    public int getIconIconGap() {
        return iconIconGap;
    }

    /**
     * set the gap from the left where this area should start rendering cell area it
     * contains
     * 
     * @param leftGap left gap
     */
    public void setLeftGap(int leftGap) {
        this.leftGap = leftGap;
    }

    /**
     * get the left gap of this cell area
     * 
     * @return left gap
     */
    public int getLeftGap() {
        return leftGap;
    }

    /**
     * get the port width of this cell area
     * 
     * @return port width
     */
    public int getPortAreaWidth() {
        if (leftPortArea != null) {
            return leftPortArea.getWidth();
        } else if (rightPortArea != null) {
            return rightPortArea.getWidth();
        }
        return 0;
    }

    /**
     * get the left side graph port this area
     * 
     * @return left port
     */
    public IGraphPort getLeftGraphPort() {
        if (leftPortArea != null) {
            PortArea pArea = leftPortArea.getPortAreaAt(0);
            return pArea.getGraphPort();
        }
        return null;
    }

    /**
     * get the right graph port of this cell
     * 
     * @return right side graph port
     */
    public IGraphPort getRightGraphPort() {
        if (rightPortArea != null) {
            PortArea pArea = rightPortArea.getPortAreaAt(0);
            return pArea.getGraphPort();
        }
        return null;
    }

    /**
     * get the text of cell area
     * 
     * @return text
     */
    public String getText() {
        if (cellArea != null) {
            return cellArea.getText();
        }

        return null;
    }

    public String getOriginalText() {
        if (cellArea != null) {
            return cellArea.getOriginalText();
        }

        return null;
    }

    /**
     * get the line pen
     * 
     * @return line pen
     */
    public JGoPen getLinePen() {
        return (linePen != null) ? linePen : DEFAULT_PEN;
    }

    /**
     * set the line pen
     * 
     * @param pen line pen
     */
    public void setLinePen(JGoPen pen) {
        JGoPen oldPen = linePen;
        if (oldPen != pen) {
            linePen = pen;

            layoutChildren();
        }
    }

    /**
     * layout the children of list area
     */
    public void layoutChildren() {
        if (rect != null) {
            rect.setPen(getLinePen());
        }

        if (leftPortArea != null) {
            leftPortArea.setLinePen(getLinePen());
        }

        if (rightPortArea != null) {
            rightPortArea.setLinePen(getLinePen());
        }

        if (arrowPort) {
            layoutChildrenForArrowPort();
        } else {
            layoutChildrenWithoutArrowPort();
        }

    }

    private void layoutChildrenForArrowPort() {
        if (drawBoundingRect) {
            rect.setBoundingRect(this.getBoundingRect());
        }
        int rectleft = getLeft();
        int recttop = getTop();
        int rectwidth = getWidth();
        int rectheight = getHeight();

        int width = rectwidth - insets.left - insets.right;
        int height = rectheight - insets.top - insets.bottom;

        int leftPortAreaW = 0;
        int righPortAreaW = 0;

        if (leftPortArea != null) {
            leftPortArea.setBoundingRect(rectleft, recttop, leftPortArea.getWidth(), rectheight);
            leftPortAreaW = leftPortArea.getWidth();
        }

        if (rightPortArea != null) {
            rightPortArea.setBoundingRect(rectleft + rectwidth - rightPortArea.getWidth(), recttop, rightPortArea.getWidth(), rectheight);
            righPortAreaW = rightPortArea.getWidth();
        }

        if (cellArea != null) {
            if (leftPortArea != null) {
                cellArea.setSpotLocation(JGoObject.Left, leftPortArea, JGoObject.Right);
            } else {
                cellArea.setSpotLocation(JGoObject.Left, this, JGoObject.Left);
            }
            cellArea.setLeft(cellArea.getLeft() + leftGap);

            int w = width - leftPortAreaW - righPortAreaW - leftGap - iconTextGap;

            BasicImageArea bia = null;

            if (imgs != null) {
                for (int i = 0; i < imgs.length; i++) {
                    bia = imgs[i];
                    if ((bia != null) && (bia.isVisible())) {
                        w = w - bia.getWidth() - iconIconGap;
                    }
                }
            }
            cellArea.setSize(w, height);
        }

        BasicImageArea bia = null;
        BasicImageArea prevImage = null;
        boolean firstImage = true;

        if (imgs != null) {
            for (int i = 0; i < imgs.length; i++) {
                bia = imgs[i];
                if ((bia != null) && (bia.isVisible())) {
                    if (cellArea != null) {

                        if (prevImage == null) {
                            bia.setSpotLocation(JGoObject.Left, cellArea, JGoObject.Right);
                        } else {
                            bia.setSpotLocation(JGoObject.Left, prevImage, JGoObject.Right);
                        }

                        if (firstImage) {
                            bia.setLeft(bia.getLeft() + iconTextGap);
                            firstImage = false;
                        } else {
                            bia.setLeft(bia.getLeft() + iconIconGap);
                        }

                        prevImage = bia;

                    } else if (leftPortArea != null) {
                        bia.setSpotLocation(JGoObject.Left, leftPortArea, JGoObject.Right);
                    } else {
                        bia.setSpotLocation(JGoObject.Left, this, JGoObject.Left);
                    }
                }
            }// for loop
        } // if imgs != null
    }

    protected boolean geometryChangeChild(JGoObject child, Rectangle prevRect) {
        // do nothing as we do not want to listen to changes in children
        return true;
    }

    private void layoutChildrenWithoutArrowPort() {
        if (drawBoundingRect) {
            rect.setBoundingRect(this.getBoundingRect());
        }
        int rectleft = getLeft();
        int recttop = getTop();
        int rectwidth = getWidth();
        int rectheight = getHeight();

        int width = rectwidth - insets.left - insets.right;
        int height = rectheight - insets.top - insets.bottom;

        if (leftPortArea != null) {
            leftPortArea.setBoundingRect(rectleft, recttop, width, rectheight);
        }

        if (rightPortArea != null) {
            rightPortArea.setBoundingRect(rectleft + rectwidth, recttop, width, rectheight);
        }

        if (cellArea != null) {

            cellArea.setSpotLocation(JGoObject.Left, this, JGoObject.Left);
            cellArea.setLeft(cellArea.getLeft() + leftGap);

            int w = width - leftGap - iconTextGap;

            BasicImageArea bia = null;

            if (imgs != null) {
                for (int i = 0; i < imgs.length; i++) {
                    bia = imgs[i];
                    if (bia != null) {
                        w = w - bia.getWidth() - iconIconGap;
                    }
                }
            }

            cellArea.setSize(w, height);
        }

        BasicImageArea bia = null;
        BasicImageArea prevImage = null;
        boolean firstImage = true;

        if (imgs != null) {
            for (int i = 0; i < imgs.length; i++) {
                bia = imgs[i];
                if ((bia != null) && (bia.isVisible())) {
                    if (cellArea != null) {
                        bia.setSpotLocation(JGoObject.Left, cellArea, JGoObject.Right);

                        if (prevImage == null) {
                            bia.setSpotLocation(JGoObject.Left, cellArea, JGoObject.Right);
                        } else {
                            bia.setSpotLocation(JGoObject.Left, prevImage, JGoObject.Right);
                        }
                        int topGap = this.getHeight() - bia.getHeight();
                        if (topGap > 0) {
                            bia.setTop(this.getTop() + topGap / 2);
                        }

                        if (firstImage) {
                            bia.setLeft(bia.getLeft() + iconTextGap);
                            firstImage = false;
                        } else {
                            bia.setLeft(bia.getLeft() + iconIconGap);
                        }
                    } else if (leftPortArea != null) {
                        bia.setSpotLocation(JGoObject.Left, leftPortArea, JGoObject.Right);
                    } else {
                        bia.setSpotLocation(JGoObject.Left, this, JGoObject.Left);
                    }
                }
            }
        }

    }

    /**
     * This method gets called when a bound property is changed.
     * 
     * @param evt A PropertyChangeEvent object describing the event source and the
     *        property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(CellArea.TEXT)) {
            this.firePropertyChangeEvent(TEXT, evt.getOldValue(), evt.getNewValue());
        }
    }

    public void setBackGroundColor(Color bkColor) {
        rect.setBrush(JGoBrush.makeStockBrush(bkColor));
        this.cellArea.setBackGroundColor(bkColor);
        this.layoutChildren();
    }

    public void setBorder(JGoPen borderPen) {
        rect.setPen(borderPen);
    }

    public Object getDataObject() {
        return dataObject;
    }

    public void setDataObject(Object obj) {
        this.dataObject = obj;
    }

    protected void initImageAreas() {
        // Prepare to handle variable number of Icons
        BasicImageArea bImg = null;

        for (int i = 0; i < imgs.length; i++) {
            bImg = imgs[i];
            if (bImg != null) {
                bImg.setSelectable(false);
                bImg.setResizable(false);
                this.addObjectAtTail(bImg);
            }
        }
    }

    /**
     * Sets text color to the given value.
     * 
     * @param textColor new text color
     */
    public void setTextColor(Color textColor) {
        if (cellArea != null) {
            cellArea.setTextColor(textColor != null ? textColor : DEFAULT_TEXT_COLOR);
        }
    }

    /**
     * Gets current text color.
     * 
     * @return current text color
     */
    public Color getTextColor() {
        return (cellArea != null) ? cellArea.getTextColor() : DEFAULT_TEXT_COLOR;
    }

    /**
     * Sets background brush to the given value.
     * 
     * @param newBrush new JGoBrush to use in painting background
     */
    public void setBrush(JGoBrush newBrush) {
        repaintBackground(newBrush);
    }

    protected void repaintBackground(JGoBrush aBrush) {
        if (aBrush != null) {
            Color c = aBrush.getColor();
            if (cellArea != null) {
                cellArea.setBackGroundColor(c);
            }

            if (leftPortArea != null) {
                leftPortArea.setBackgroundColor(c);
            }

            if (rightPortArea != null) {
                rightPortArea.setBackgroundColor(c);
            }

            if (rect != null) {
                rect.setBrush(aBrush);
            }
        }
    }

    /**
     * Extends BasicCellArea to implement IHighlightable interface, to allow for a change
     * in presentation/appearance whenever a mouse cursor hovers over an instance.
     * 
     * @author Jonathan Giron
     * @version $Revision$
     */
    public static class Highlightable extends BasicCellArea implements IHighlightable {
        private boolean highlightEnabled = true;

        private IHighlightConfigurator highlightConfigurator = new HighlightConfiguratorImpl();

        /**
         * Constructs a default instance of BasicCellArea.Highlightable.
         */
        public Highlightable() {
            super();
        }

        /**
         * Constructs an instance of BasicCellArea.Highlightable with the given text.
         * 
         * @param text text label for the new instance
         */
        public Highlightable(String text) {
            super(text);
        }

        /**
         * Constructs an instance of BasicCellArea.Highlightable with the given port type
         * and text.
         * 
         * @param portType indicates what kind of port to display with the new instance
         * @param text text label for the new instance
         */
        public Highlightable(int portType, String text) {
            super(portType, text);
        }

        /**
         * Constructs an instance of BasicCellArea.Highlightable with the given port type,
         * text, and icon.
         * 
         * @param portType indicates what kind of port to display with the new instance
         * @param text text label for the new instance
         * @param icon icon for the new instance
         */
        public Highlightable(int portType, String text, Icon icon) {
            super(portType, text, icon);
            // TODO Auto-generated constructor stub
        }

        /**
         * Gets current JGoBrush for this instance
         * 
         * @return current JGoBrush
         */
        public JGoBrush getBrush() {
            return (highlightConfigurator != null) ? highlightConfigurator.getNormalBrush() : JGoBrush.makeStockBrush(IHighlightConfigurator.DEFAULT_BASIC_COLOR);
        }

        public void setHighlighted(boolean shouldHighlight) {
            if (highlightConfigurator == null) {
                return;
            }

            if (shouldHighlight && highlightEnabled) {
                repaintBackground(highlightConfigurator.getHoverBrush());
            } else {
                repaintBackground(highlightConfigurator.getNormalBrush());
            }
        }

        public void setHighlightEnabled(boolean enableHighlighting) {
            highlightEnabled = enableHighlighting;
        }

        public boolean isHighlightEnabled() {
            return highlightEnabled;
        }

        public IHighlightConfigurator getHighlightConfigurator() {
            return highlightConfigurator;
        }

        public void setHighlightConfigurator(IHighlightConfigurator hc) {
            highlightConfigurator = hc;
            if (hc != null) {
                repaintBackground(hc.getNormalBrush());
            } else {
                repaintBackground(JGoBrush.makeStockBrush(IHighlightConfigurator.DEFAULT_BASIC_COLOR));
            }
        }

        public void setBrush(JGoBrush newBrush) {
            if (highlightConfigurator != null) {
                highlightConfigurator.setNormalBrush(newBrush);
            }
            super.setBrush(newBrush);
        }
    }
}

