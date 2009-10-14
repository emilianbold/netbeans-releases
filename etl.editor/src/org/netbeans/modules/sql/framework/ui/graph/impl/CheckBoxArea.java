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

