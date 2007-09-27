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

package org.netbeans.modules.sql.framework.ui.graph.impl;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;

import org.netbeans.modules.sql.framework.ui.graph.IGraphFieldNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoText;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class BasicComboBoxArea extends CanvasArea implements ItemListener, IGraphFieldNode {

    private BasicCellArea cellArea;
    private ComboBoxArea cbArea;
    private JGoRectangle rect;
    private String areaName;
    private List itemListeners = new ArrayList();

    public BasicComboBoxArea(String cbName, Vector items) {
        this(cbName, null, items, null, null);
    }

    /** Creates a new instance of BasicComboBoxArea */
    public BasicComboBoxArea(String cbName, String cbLabel, Vector items) {
        this(cbName, cbLabel, items, null, null);
    }

    public BasicComboBoxArea(String cbName, String cbLabel, Vector items, Vector displayLabels, String tooltipText) {
        this(cbName, cbLabel, items, null, null, false);
    }

    public BasicComboBoxArea(String cbName, String cbLabel, Vector items, Vector displayLabels, String tooltipText, boolean isEditable) {
        super();

        this.setSelectable(false);
        this.setResizable(false);

        areaName = cbName;

        rect = new JGoRectangle();
        rect.setPen(JGoPen.makeStockPen(Color.lightGray));
        rect.setBrush(JGoBrush.makeStockBrush(Color.WHITE));
        rect.setSelectable(false);
        rect.setResizable(false);
        addObjectAtHead(rect);

        if (cbLabel != null) {
            cellArea = new BasicCellArea(cbLabel);
            cellArea.setTextAlignment(JGoText.ALIGN_CENTER);
            this.addObjectAtTail(cellArea);
        }

        cbArea = new ComboBoxArea(items, displayLabels, tooltipText, isEditable);
        cbArea.addItemListener(this);
        this.addObjectAtTail(cbArea);

        this.setSize(getMaximumWidth(), getMaximumHeight());
    }

    public int getMaximumHeight() {
        int h = this.insets.top + this.insets.bottom;

        if (cellArea != null) {
            h += cellArea.getMaximumHeight();
        }

        if (cbArea != null) {
            h += cbArea.getHeight();
        }

        return h;
    }

    public int getMaximumWidth() {
        int w = this.insets.left + this.insets.right;
        int width = 0;

        if (cellArea != null) {
            width = cellArea.getMaximumWidth();
        }

        if (cbArea != null && cbArea.getWidth() > width) {
            width = cbArea.getWidth();
        }

        w += width;

        return w;
    }

    public int getMinimumWidth() {
        int width = 0;

        if (cellArea != null) {
            width = cellArea.getMinimumWidth();
        }

        if (cbArea != null) {
            JComboBox comboBox = cbArea.getComboBox();
            if (comboBox != null) {
                int cbWidth = comboBox.getPreferredSize().width;
                if (cbWidth > width) {
                    width = cbWidth;
                }
            }
        }

        // Always account for horizontal insets.
        width += this.getInsets().left + this.getInsets().right;
        return width;
    }

    public void layoutChildren() {
        rect.setBoundingRect(this.getBoundingRect());

        int rectleft = getLeft();
        int recttop = getTop();
        int rectwidth = getWidth();

        int left = rectleft + insets.left;
        int top = recttop + insets.top;
        int width = rectwidth - insets.left - insets.right;
        int cbTop = top;
        if (cellArea != null) {
            cbTop = top + cellArea.getHeight();
        }

        cbArea.setBoundingRect(left, cbTop, width, cbArea.getHeight());

        if (cellArea != null) {
            cellArea.setBoundingRect(left, top, width, cellArea.getHeight());
        }

    }

    //we need to call set visible on our Jgo control so that it can be
    //removed from the view
    public void setVisible(boolean bFlag) {
        cbArea.setVisible(bFlag);
        super.setVisible(bFlag);
    }

    public void setSelectedItem(Object anObject) {
        cbArea.setSelectedItem(anObject);
    }

    public void addItemListener(ItemListener l) {
        if (l != null) {
            synchronized (itemListeners) {
                itemListeners.add(l);
            }
        }
    }

    public void removeItemListener(ItemListener l) {
        if (l != null) {
            synchronized (itemListeners) {
                itemListeners.remove(l);
            }
        }
    }

    public void setComboBoxEnabled(boolean enabled) {
        cbArea.setComboBoxEnabled(enabled);
    }

    public String getName() {
        return areaName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    public void itemStateChanged(ItemEvent e) {
        // Redispatch this ItemEvent with this as the (proxy) source.
        ItemEvent newEvent = new ItemEvent(e.getItemSelectable(), e.getID(), e.getItem(), e.getStateChange());
        newEvent.setSource(this);

        synchronized (itemListeners) {
            Iterator iter = itemListeners.iterator();
            while (iter.hasNext()) {
                ItemListener l = (ItemListener) iter.next();
                try {
                    l.itemStateChanged(newEvent);
                } catch (Exception ignore) {
                    // Ignore and continue with other listeners.
                }
            }
        }
    }

    public JComboBox getComboBox() {
        return cbArea.getComboBox();
    }

    public List getAcceptableValues() {
        return cbArea.getAcceptableValues();
    }

    public List getAcceptableDisplayValues() {
        return cbArea.getAcceptableDisplayValues();
    }

    public boolean isEditable() {
        return cbArea.isEditable();
    }

    public Object getDataObject() {
        return null;
    }

    public IGraphNode getGraphNode() {
        return null;
    }

    public void setObject(Object obj) {

    }

    public IGraphPort getLeftGraphPort() {
        return null;
    }

    public IGraphPort getRightGraphPort() {
        return null;
    }
}

