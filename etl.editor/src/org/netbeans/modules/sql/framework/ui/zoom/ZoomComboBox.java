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
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopPanel;
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
    
    private static final Dimension COMBO_BOX_SIZE_MAC = new Dimension(100, 20);
    private static final Dimension ZOOM_PANEL_SIZE_MAC = new Dimension(110, 25);
    

    /** Creates a new instance of ZoomComboBox */
    public ZoomComboBox() {
        this.setLayout(new FlowLayout(java.awt.FlowLayout.LEFT));

        zoomBox = new JComboBox(initializeValues());
        zoomBox.setSelectedIndex(4);

        zoomBox.addItemListener(new ZoomFactorItemListener());


        Dimension dim = COMBO_BOX_SIZE;
        Dimension dim2 = ZOOM_PANEL_SIZE;
        if(!System.getProperty("os.name").contains("Win")) {
            dim = COMBO_BOX_SIZE_MAC;
            dim2 = ZOOM_PANEL_SIZE_MAC;
        }
        
        zoomBox.setPreferredSize(dim);
        zoomBox.setSize(dim);
        
        this.add(zoomBox);
        this.setMaximumSize(dim2);

        TopComponent.getRegistry().addPropertyChangeListener(this);
    }

    private void createZoomValue(Vector<ZoomComboBox.ZoomValues> v, String str, double val) {
        ZoomValues zoomVal = new ZoomValues(str, val);
        v.add(zoomVal);
    }

    private Vector initializeValues() {
        Vector<ZoomValues> vec = new Vector<ZoomValues>();
        createZoomValue(vec, "400%", 4.0);
        createZoomValue(vec, "300%", 3.0);
        createZoomValue(vec, "200%", 2.0);
        createZoomValue(vec, "150%", 1.5);
        createZoomValue(vec, "100%", 1.0);
        createZoomValue(vec, "75%", .75);
        createZoomValue(vec, "66%", .66);
        createZoomValue(vec, "50%", .50);
        createZoomValue(vec, "33%", .33);
        createZoomValue(vec, "25%", .25);
        createZoomValue(vec, "Fit", 1.0);
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
            ETLCollaborationTopPanel topComp = null;
            try {
                topComp = DataObjectProvider.getProvider().getActiveDataObject().getETLEditorTopPanel();
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

        @Override
        public String toString() {
            return displayValue;
        }

        public double getValue() {
            return this.value;
        }
    }
}