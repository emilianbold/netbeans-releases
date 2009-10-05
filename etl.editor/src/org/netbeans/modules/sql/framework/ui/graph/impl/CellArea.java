/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.sql.framework.ui.graph.impl;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.nwoods.jgo.JGoImage;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class CellArea extends CanvasArea implements PropertyChangeListener {

    /**
     * TEXT Property
     */
    public static final String TEXT = "text";

    private BasicText text;
    private JGoImage img;
    //this should not be here
    private java.net.URL url = getClass().getResource("/org/netbeans/modules/sql/framework/ui/resources/images/filter16.gif");

    /**
     * create an instance of CellArea
     */
    public CellArea() {
        super();
        this.setSelectable(false);
        this.setResizable(false);
        this.setDraggable(true);
        this.set4ResizeHandles(false);

    }

    /**
     * Creates a new instance of CellArea
     * 
     * @param val -
     */
    public CellArea(String val) {
        this();
        initializeText(val);
        ImageIcon icon = new ImageIcon(url);
        initializeImage(icon);
        img.setVisible(false);
    }

    /**
     * get maximum height
     * 
     * @return max height
     */
    public int getMaximumHeight() {
        return this.getHeight();
    }

    /**
     * get the maximum width
     * 
     * @return max width
     */
    public int getMaximumWidth() {
        return text.getMaximumWidth();
    }

    public int getMinimumWidth() {
        return text.getMaximumWidth();
    }

    /**
     * initialize the text
     * 
     * @param val text
     */
    void initializeText(String val) {
        if (val == null) {
            return;
        }
        text = new BasicText(val);
        text.setAlignment(JGoText.ALIGN_LEFT);
        text.setSelectable(false);
        text.setResizable(false);
        text.setDraggable(false);
        text.set4ResizeHandles(false);
        text.addPropertyChangeListener(this);
        //text.setBkColor(new Color(0xFF, 0xFF, 0xCC));
        this.addObjectAtTail(text);

    }

    /**
     * create an instance of CellArea
     * 
     * @param icon icon
     */
    public CellArea(Icon icon) {
        this();
        initializeImage(icon);
    }

    /**
     * set the text in this cell
     * 
     * @param val text
     */
    public void setText(String val) {
        text.setOriginalText(val);
        layoutChildren();
    }

    /**
     * get the text of this cell
     * 
     * @return text
     */
    public String getText() {
        if (text != null) {
            return text.getText();
        }

        return null;
    }

    public String getOriginalText() {
        if (text != null) {
            return text.getOriginalText();
        }

        return null;
    }

    /**
     * set whether text in this area is editable
     * 
     * @param editable text is editable
     */
    public void setTextEditable(boolean editable) {
        text.setSelectable(editable);
        text.setEditable(editable);
        text.setEditOnSingleClick(editable);

    }

    /**
     * Is the text editable
     * 
     * @return whether the text is editable
     */
    public boolean isTextEditable() {
        return text.isEditable();
    }

    /**
     * set the text alignment
     * 
     * @param alignment alignment
     */
    public void setTextAlignment(int alignment) {
        text.setAlignment(alignment);
    }

    /**
     * initialize the image
     * 
     * @param icon icon
     */
    void initializeImage(Icon icon) {
        if (icon == null) {
            return;
        }

        img = new JGoImage();
        ImageIcon imgIcon = (ImageIcon) icon;
        img.loadImage(imgIcon.getImage(), false);

        img.setSize(imgIcon.getImage().getWidth(null), imgIcon.getImage().getHeight(null));
        img.setSelectable(false);
        img.setResizable(false);
        addObjectAtTail(img);

    }

    /**
     * set the icon in the cell area
     * 
     * @param icon icon
     */
    public void setIcon(Icon icon) {
        if (icon == null) {
            text.setVisible(true);
        } else {
            if (img == null) {
                initializeImage(icon);
            } else {
                this.removeObject(img);
                initializeImage(icon);
            }
            text.setVisible(false);
        }

    }

    /**
     * set the icon to be visible
     * 
     * @param visible true or false
     */
    public void setIconVisible(boolean visible) {
        if (img != null && text != null) {
            img.setVisible(visible);
            text.setVisible(!visible);
        }
    }

    /**
     * override this method to handle the changes in the geometry of this area we will lay
     * out all the cell again
     * 
     * @param prevRect previous bounds rectangle
     */
    protected void geometryChange(Rectangle prevRect) {
        // handle any size changes by repositioning all the items
        if (prevRect.width != getWidth() || prevRect.height != getHeight()) {
            layoutChildren();
        } else {
            super.geometryChange(prevRect);
        }
    }

    /**
     * layout the children of this cell area
     */
    public void layoutChildren() {
        Rectangle rectangle = this.getBoundingRect();
        if (text != null) {
            text.setBoundingRect(rectangle);
        }
        if (img != null) {
            img.setBoundingRect(rectangle);
        }
    }

    /**
     * paint this area
     * 
     * @param g Graphics2D
     * @param view view
     */
    public void paint(java.awt.Graphics2D g, JGoView view) {
        super.paint(g, view);
    }

    /**
     * Let single click on a label mean start editing that label. Because the label is not
     * selectable, a mouse click will be passed on up to its parent, which will be this
     * area.
     * 
     * @param modifiers mouse event modifiers
     * @param dc document point
     * @param vc view point
     * @param view view
     * @return true if mouse click is handled by this area
     */
    public boolean doMouseClick(int modifiers, Point dc, Point vc, JGoView view) {
        JGoObject obj = view.pickDocObject(dc, false);
        if (obj instanceof JGoText && obj.getLayer() != null && obj.getLayer().isModifiable()) {
            JGoText lab = (JGoText) obj;
            if (lab.isEditable() && lab.isEditOnSingleClick()) {
                lab.doStartEdit(view, vc);
                return true;
            }
        }
        return false;
    }

    /**
     * This method gets called when a bound property is changed.
     * 
     * @param evt A PropertyChangeEvent object describing the event source and the
     *        property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(BasicText.ORIGINAL_TEXT)) {
            this.firePropertyChangeEvent(TEXT, evt.getOldValue(), evt.getNewValue());
        }
    }

    public void setBackGroundColor(Color bkColor) {
        this.text.setBkColor(bkColor);
    }
    
    public void setTextColor(Color textColor) {
        this.text.setTextColor(textColor);
    }
    
    public Color getTextColor() {
        return text.getTextColor();
    }
}

