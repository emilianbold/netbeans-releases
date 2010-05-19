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

package org.netbeans.modules.edm.editor.graph.jgo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import com.nwoods.jgo.JGoControl;
import com.nwoods.jgo.JGoView;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ComboBoxArea extends JGoControl {
    private static final Color COLOR_BACKGROUND = new Color(240, 240, 240); // light gray

    private static final Color COLOR_BACKGROUND_HOVER = new Color(254, 254, 244); // light
                                                                                    // beige

    private static final Color COLOR_TEXT_LABEL = new Color(100, 100, 90); // gray

    private static final Color COLOR_TEXT_EDITABLE = new Color(30, 70, 230); // navy

    private Vector comboBoxItems;
    private Vector comboBoxLabels;
    private JComboBox comboBox;
    private ArrayList itemListeners = new ArrayList();
    private boolean enabled = true;
    private Object selectedItem;
    private String tooltipText;
    private boolean editable;
    private Map viewControlMap = new HashMap();

    /** Creates a new instance of BasicComboBoxArea */
    public ComboBoxArea(Vector items) {
        this(items, null, null, false);
    }

    public ComboBoxArea(Vector items, Vector displayLabels) {
        this(items, null, null, false);
    }

    public ComboBoxArea(Vector items, Vector displayLabels, String toolTipText) {
        this(items, null, null, false);
    }

    public ComboBoxArea(Vector items, Vector displayLabels, String toolTipText, boolean isEditable) {
        super();

        this.setSelectable(false);
        this.setResizable(false);

        // temporarily create a combo box to set the size
        if (displayLabels != null && displayLabels.size() != 0) {
            this.setSize((new JComboBox(displayLabels)).getPreferredSize());
            this.comboBoxLabels = displayLabels;
        } else {
            this.setSize((new JComboBox(items)).getPreferredSize());
        }

        this.comboBoxItems = items;
        this.tooltipText = toolTipText;
        this.editable = isEditable;
    }

    /**
     * Each JGoControl subclass is responsible for representing the JGoControl with a
     * JComponent that will be added to the JGoView's canvas.
     * <p>
     * You may wish to return null when no JComponent is desired for this JGoControl,
     * perhaps just for the given view.
     * 
     * @param view the view for which this control should be created
     * @return a JComponent
     */
    // Note we are returning same combo box for all views this may
    // be a problem if there are multiple views where this area is added.
    // Or else we need only one combo box so that we can listen for its events.
    public JComponent createComponent(JGoView view) {
        comboBox = (JComboBox) viewControlMap.get(view);
        if (comboBox == null) {
            comboBox = new JComboBox(comboBoxItems);
            comboBox.addItemListener(new CBItemListener());
            comboBox.addMouseListener(new CBMouseListener());
            comboBox.setEnabled(enabled);
            comboBox.setToolTipText(tooltipText);
            comboBox.setBackground(COLOR_BACKGROUND);
            comboBox.setForeground(COLOR_TEXT_LABEL);

            viewControlMap.put(view, comboBox);

            // Use display labels to render the list entries if they were supplied in the
            // constructor.
            if (comboBoxLabels != null) {
                comboBox.setRenderer(new VectorLabelCellRenderer(comboBoxLabels));
            }

            comboBox.setEditable(editable);
            if (editable) {
                comboBox.setEditor(new VectorLabelCellEditor());
            }

            if (this.selectedItem != null) {
                comboBox.setSelectedItem(selectedItem);
            }

            MouseFocusHandler hoverHandler = new MouseFocusHandler.Basic();
            comboBox.addFocusListener(hoverHandler);
            comboBox.addMouseListener(hoverHandler);
        }

        return comboBox;
    }

    public void setSelectedItem(Object anObject) {
        this.selectedItem = anObject;
        // for each JScrollBar, set its parameters
        Iterator it = getIterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            JGoView view = (JGoView) pair.getKey();
            JComboBox cBox = (JComboBox) pair.getValue();
            if (cBox != null) {
                cBox.setSelectedItem(anObject);
                view.getCanvas().validate();
            }
        }

    }

    public synchronized void addItemListener(ItemListener l) {
        itemListeners.add(l);
    }

    public synchronized void removeItemListener(ItemListener l) {
        itemListeners.remove(l);
    }

    public String getToolTipText() {
        return tooltipText;
    }

    public void fireItemStateChanged(ItemEvent e) {
        Iterator it = itemListeners.iterator();
        while (it.hasNext()) {
            ItemListener listener = (ItemListener) it.next();
            listener.itemStateChanged(e);
        }
    }

    public JComboBox getComboBox() {
        return comboBox;
    }

    public List getAcceptableValues() {
        return new ArrayList(this.comboBoxItems);
    }

    public List getAcceptableDisplayValues() {
        return new ArrayList(this.comboBoxLabels);
    }

    public boolean isEditable() {
        return this.editable;
    }

    public void setComboBoxEnabled(boolean enabled) {
        this.enabled = enabled;
        comboBox.setEnabled(enabled);
    }

    class VectorLabelCellRenderer extends DefaultListCellRenderer implements ListCellRenderer {
        Vector labels = null;

        public VectorLabelCellRenderer(Vector labelVector) {
            super();
            labels = labelVector;
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            int id = comboBoxItems.indexOf(value);
            if (id != -1) {
                value = (String) labels.elementAt(id);
            }

            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    class VectorLabelCellEditor extends BasicComboBoxEditor {
        public VectorLabelCellEditor() {
            super();

            MouseFocusHandler hoverListener = new MouseFocusHandler.Editable();
            hoverListener.setBackgroundColor(COLOR_BACKGROUND);
            hoverListener.setHoverColor(COLOR_BACKGROUND_HOVER);
            if (editable) {
                hoverListener.setForegroundColor(COLOR_TEXT_EDITABLE);
            } else {
                hoverListener.setForegroundColor(COLOR_TEXT_LABEL);
            }
            hoverListener.showRenderingColors(this.editor);

            editor.addFocusListener(hoverListener);
            editor.addMouseListener(hoverListener);
        }
    }

    class CBItemListener implements ItemListener {
        /**
         * Invoked when an item has been selected or deselected by the user. The code
         * written for this method performs the operations that need to occur when an item
         * is selected (or deselected).
         */
        public void itemStateChanged(ItemEvent e) {
            fireItemStateChanged(e);
        }
    }

    /**
     * MouseListener associated with combo box to ensure that the mouse cursor is set to
     * the default icon whenever it hovers over the control. This is necessary because the
     * table title area sets the cursor over itself to MOVE_CURSOR, and the cursor does
     * not reset to DEFAULT_CURSOR for JGoArea derivatives which incorporate Swing or AWT
     * components.
     * 
     * @author Jonathan Giron
     */
    class CBMouseListener extends MouseAdapter {
        public void mouseEntered(MouseEvent e) {
            ComboBoxArea.this.comboBox.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
}

