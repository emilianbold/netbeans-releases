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

package org.netbeans.modules.sql.framework.ui.zoom;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopComponent;
import org.openide.windows.TopComponent;


/**
 * @author Ritesh Adval
 */
public class ZoomComboBox extends JPanel implements PropertyChangeListener {
    
    private ZoomSupport zoomableComponent;
    private JComboBox zoomBox;
    private double lastValue;
    private static final Dimension COMBO_BOX_SIZE = new Dimension(60, 20);
    private static final Dimension ZOOM_PANEL_SIZE = new Dimension(70, 25);
    
    /** Creates a new instance of ZoomComboBox */
    public ZoomComboBox() {
        this.setLayout(new FlowLayout(java.awt.FlowLayout.LEFT));
        
        zoomBox = new JComboBox(initializeValues());
        zoomBox.setSelectedIndex(4);
        
        zoomBox.addItemListener(new ZoomFactorItemListener());
        
        zoomBox.setPreferredSize(COMBO_BOX_SIZE);
        zoomBox.setSize(COMBO_BOX_SIZE);
        this.add(zoomBox);
        this.setMaximumSize(ZOOM_PANEL_SIZE);
        
        TopComponent.getRegistry().addPropertyChangeListener(this);
        
    }
    
    private Vector initializeValues() {
        Vector vec = new Vector();
        ZoomValues val1 = new ZoomValues("400%", 4.0);
        vec.add(val1);
        ZoomValues val2 = new ZoomValues("300%", 3.0);
        vec.add(val2);
        ZoomValues val3 = new ZoomValues("200%", 2.0);
        vec.add(val3);
        ZoomValues val4 = new ZoomValues("150%", 1.5);
        vec.add(val4);
        ZoomValues val5 = new ZoomValues("100%", 1.0);
        vec.add(val5);
        ZoomValues val6 = new ZoomValues("75%", .75);
        vec.add(val6);
        ZoomValues val7 = new ZoomValues("66%", .66);
        vec.add(val7);
        ZoomValues val8 = new ZoomValues("50%", .50);
        vec.add(val8);
        ZoomValues val9 = new ZoomValues("33%", .33);
        vec.add(val9);
        ZoomValues val10 = new ZoomValues("25%", .25);
        vec.add(val10);
        ZoomValues val11 = new ZoomValues("Fit", 1.0);
        vec.add(val11);
        
        return vec;
    }
    
    /**
     * This method gets called when a bound property is changed.
     *
     * @param evt A PropertyChangeEvent object describing the event source and the
     *        property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        zoomBox.setEnabled(true);
    }
    
    class ZoomFactorItemListener implements ItemListener {
        
        /**
         * Invoked when an item has been selected or deselected by the user. The code
         * written for this method performs the operations that need to occur when an item
         * is selected (or deselected).
         */
        public void itemStateChanged(ItemEvent e) {
            ZoomValues val = (ZoomValues) e.getItem();
            if (zoomableComponent != null && val.getValue() != lastValue) {
                zoomableComponent.setZoomFactor(val.getValue());
            }
            
            lastValue = val.getValue();
            ETLCollaborationTopComponent topComp = null;
            try {
                topComp = DataObjectProvider.getProvider().getActiveDataObject().getETLEditorTC();
                topComp.setZoomFactor(lastValue);
            } catch (Exception ex) {
                // ignore
            }
        }
    }
    
    class ZoomValues {
        private String displayValue;
        private double value;
        
        ZoomValues(String displayValue, double value) {
            this.displayValue = displayValue;
            this.value = value;
        }
        
        public String toString() {
            return displayValue;
        }
        
        public double getValue() {
            return this.value;
        }
    }
}

