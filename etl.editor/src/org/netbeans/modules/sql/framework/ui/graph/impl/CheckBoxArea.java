/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.sql.framework.ui.graph.impl;

import java.awt.Color;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

import com.nwoods.jgo.JGoControl;
import com.nwoods.jgo.JGoView;

/**
 * A Check box area
 * 
 * @author radval
 */
public class CheckBoxArea extends JGoControl {

    private JCheckBox checkBox;
    private String toolTip;
    private Color bgColor;
    private boolean selected = false;

    private ArrayList itemListeners = new ArrayList();

    /** Creates a new instance of BasicComboBoxArea */
    public CheckBoxArea() {
        super();
        this.setSelectable(false);
        this.setResizable(false);

        //temporaily create a combox box to set the size
        this.setSize((new JCheckBox()).getPreferredSize());

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

    public JComponent createComponent(JGoView view) {

        //JCheckBox
        checkBox = new JCheckBox();
        checkBox.setSelected(this.selected);

        if (this.toolTip != null) {
            checkBox.setToolTipText(this.toolTip);
        }

        if (this.bgColor != null) {
            checkBox.setBackground(this.bgColor);
        }

        Iterator it = itemListeners.iterator();
        while (it.hasNext()) {
            ItemListener l = (ItemListener) it.next();
            checkBox.addItemListener(l);
        }

        return checkBox;
    }

    public void setToolTipText(String tTip) {
        this.toolTip = tTip;
    }

    public void setBackground(Color c) {
        this.bgColor = c;
    }

    public void addItemListener(ItemListener l) {
        if (!itemListeners.contains(l)) {
            itemListeners.add(l);
        }

        if (checkBox != null) {
            checkBox.addItemListener(l);
        }
    }

    public void removeItemListener(ItemListener l) {
        itemListeners.remove(l);

    }

    public void setSelected(boolean b) {
        this.selected = b;
        if (checkBox != null) {
            checkBox.setSelected(b);
        }
    }
}

