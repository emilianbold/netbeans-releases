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

package org.netbeans.modules.compapp.casaeditor.design;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.NbBundle;


public class ZoomControl extends AbstractAction implements ChangeListener, FocusListener {
    
    private static final int DEF_ZOOM = 100;
    private static final int MIN_ZOOM = 33;
    private static final int MAX_ZOOM = 200;

    private static final int HGAP = 1;
    
    private static final Icon FIT_DIAGRAM_ICON = loadIcon("fit_diagram.png"); // NOI18N
    private static final Icon FIT_WIDTH_ICON = loadIcon("fit_width.png"); // NOI18N
    private static final Icon NORMAL_SIZE_ICON = loadIcon("normal_size.png"); // NOI18N

    private CasaModelGraphScene mScene;
    
    private JButton fitDiagram;
    private JButton fitWidth;
    private JButton oneToOne;
    private JTextField zoomValue;
    private JSlider zoomSlider;
    

    public ZoomControl(CasaModelGraphScene scene) {
        mScene = scene;
        
        fitDiagram = new JButton(FIT_DIAGRAM_ICON);
        fitDiagram.setToolTipText(getMessage("LBL_ZoomPanel_FitDiagram")); // NOI18N
        fitDiagram.addActionListener(this);
        fitDiagram.setFocusable(false);

        fitWidth = new JButton(FIT_WIDTH_ICON);
        fitWidth.setToolTipText(getMessage("LBL_ZoomPanel_FitWidth")); // NOI18N
        fitWidth.addActionListener(this);
        fitWidth.setFocusable(false);
        
        oneToOne = new JButton(NORMAL_SIZE_ICON);
        oneToOne.setToolTipText(getMessage("LBL_ZoomPanel_OneToOne")); // NOI18N
        oneToOne.addActionListener(this);
        oneToOne.setFocusable(false);
        
        zoomValue = new JTextField("" + getZoom() + "%", 4); // NOI18N
        zoomValue.setToolTipText(getMessage("LBL_ZoomPanel_ZoomValue")); // NOI18N
        zoomValue.addFocusListener(this);
        zoomValue.addActionListener(this);
        
        zoomSlider = new JSlider(MIN_ZOOM, MAX_ZOOM, getZoom());
        zoomSlider.addChangeListener(this);
        zoomSlider.setOpaque(false);
        zoomSlider.setBackground(null);
        zoomSlider.setFocusable(false);
    }
    
    
    public int getComponetsCount() {
        return 5;
    }
    

    public JComponent getComponent(int i) {
        switch (i) {
            case 0: return fitDiagram;
            case 1: return fitWidth;
            case 2: return oneToOne;
            case 3: return zoomValue;
            case 4: return zoomSlider;
        }
        
        return null;
    }
    
    
    public void setEnabled(boolean b) {
        fitDiagram.setVisible(b);
        fitWidth.setVisible(b);
        oneToOne.setVisible(b);
        zoomValue.setVisible(b);
        zoomSlider.setVisible(b);
    }
    
    
    private void updateView() {
        if (zoomValue.hasFocus()) {
            zoomValue.setText("" + getZoom()); // NOI18N
        } else {
            zoomValue.setText("" + getZoom() + "%"); // NOI18N
        }
        zoomSlider.setValue(getZoom());
        
        mScene.validate();
    }

    
    private int getZoom() {
        return (int) Math.round(mScene.getZoomFactor() * 100);
    }
    
    
    private void setZoom(int zoom) {
        zoomSlider.removeChangeListener(this);
        zoom = Math.max(MIN_ZOOM, Math.min(zoom, MAX_ZOOM));
        mScene.setZoomFactor(((float) zoom) / 100);
        updateView();
        zoomSlider.addChangeListener(this);
    }
    
    private void fitDiagram() {
//        float zc = DiagramFontUtil.getZoomCorrection();
//        
//        JScrollPane scrollPane = (JScrollPane) getDesignView().getParent()
//                .getParent();
//        
//        Dimension pSize = scrollPane.getSize();
//        FDimension dSize = getDesignView().getDiagramSize();
//        
//        int width = pSize.width;
//        width -= scrollPane.getVerticalScrollBar().getWidth();
//        width -= DesignViewLayout.MARGIN_LEFT;
//        width -= DesignViewLayout.MARGIN_RIGHT;
//        
//        int height = pSize.height;
//        height -= scrollPane.getHorizontalScrollBar().getHeight();
//        height -= DesignViewLayout.MARGIN_BOTTOM;
//        height -= DesignViewLayout.MARGIN_TOP;
//        
//        float zoomX = width * 100f / dSize.width / zc;
//        float zoomY = height * 100f / dSize.height / zc;
//        
//        float newZoom = Math.min(zoomX, zoomY);
//        
//        if (Float.isNaN(newZoom)) {
//            newZoom = 100;
//        }
//        
//        if (newZoom < MIN_ZOOM) {
//            setZoom(MIN_ZOOM);
//        }
//        
//        if (newZoom > MAX_ZOOM) {
//            setZoom(MAX_ZOOM);
//        }
//        
//        setZoom((int) Math.floor(newZoom));
    }
    
    
    public void fitWidth() {
//        float zc = DiagramFontUtil.getZoomCorrection();
//        
//        JScrollPane scrollPane = (JScrollPane) getDesignView().getParent()
//                .getParent();
//        
//        Dimension pSize = scrollPane.getSize();
//        
//        int width = pSize.width;
//        width -= scrollPane.getVerticalScrollBar().getWidth();
//        width -= DesignViewLayout.MARGIN_LEFT;
//        width -= DesignViewLayout.MARGIN_RIGHT;
//        
//        FDimension dSize = getDesignView().getDiagramSize();
//
//        float newZoom = width * 100f / dSize.width / zc;
//        
//        if (Float.isNaN(newZoom)) {
//            newZoom = 100;
//        }
//        
//        if (newZoom < MIN_ZOOM) {
//            setZoom(MIN_ZOOM);
//        }
//        
//        if (newZoom > MAX_ZOOM) {
//            setZoom(MAX_ZOOM);
//        }
//        
//        setZoom((int) Math.floor(newZoom));
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == fitDiagram) {
            fitDiagram();
        } else if (e.getSource() == fitWidth) {
            fitWidth();
        } else if (e.getSource() == oneToOne) {
            setZoom(DEF_ZOOM);
        } else if (e.getSource() == zoomValue) {
            try {
                int zoom = Integer.parseInt(zoomValue.getText());
                setZoom(zoom);
            } catch (NumberFormatException ex) {
                updateView();
            }
        }
    }
    
    
    public void stateChanged(ChangeEvent e) {
        setZoom(zoomSlider.getValue());
    }

    
    public void focusGained(FocusEvent e) {
        zoomValue.setText("" + getZoom()); // NOI18N
        zoomValue.selectAll();
    }

    
    public void focusLost(FocusEvent e) {
        updateView();
    }


    private String getMessage(String key) {
        return NbBundle.getBundle(ZoomControl.class).getString(key); 
    }
    
    
    private static Icon loadIcon(String name) {
        return new ImageIcon(ZoomControl.class.getResource("resources/" + name)); // NOI18N
    }
}
