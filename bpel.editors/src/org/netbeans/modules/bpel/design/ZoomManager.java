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

package org.netbeans.modules.bpel.design;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import org.netbeans.modules.bpel.design.geometry.FDimension;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class ZoomManager implements ActionListener {

    private int zoomValue = 100;
    
    private JButton fitDiagramButton;
    private JButton fitWidthButton;
    private JButton oneToOneButton;
    
    private JComboBox zoomChooser;
    
    private JButton zoomOutButton;
    private JButton zoomInButton;
    
    private JComponent[] components;
    
    private DesignView designView;
    
    private Action zoomInAction;
    private Action zoomOutAction;
    
    
    public ZoomManager(DesignView designView) {
        this.designView = designView;
        
        fitDiagramButton = createButton(FIT_DIAGRAM_ICON, 
                "LBL_ZoomPanel_FitDiagram"); // NOI18N
        fitWidthButton = createButton(FIT_WIDTH_ICON, 
                "LBL_ZoomPanel_FitWidth"); // NOI18N
        oneToOneButton = createButton(ONE_TO_ONE_ICON, 
                "LBL_ZoomPanel_OneToOne"); // NOI18N
        
        zoomOutButton = createButton(ZOOM_OUT_ICON, 
                "LBL_ZoomPanel_ZoomOut"); // NOI18N
        zoomInButton = createButton(ZOOM_IN_ICON, 
                "LBL_ZoomPanel_ZoomIn"); // NOI18N
        
        zoomChooser = new JComboBox(ZOOM_PRESETS);
        zoomChooser.setSelectedItem("100%"); // NOI18N
        zoomChooser.setEditable(true);
        zoomChooser.addActionListener(this);
        zoomChooser.setToolTipText(
                getMessage("LBL_ZoomPanel_ZoomValue")); // NOI18N
        
        ((JTextField) zoomChooser.getEditor().getEditorComponent())
                .setColumns(4);
        
        components = new JComponent[] { fitDiagramButton, fitWidthButton,
                oneToOneButton, zoomChooser, zoomOutButton, zoomInButton };
        
        zoomInAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                zoomIn();
            }
        };
        
        
        zoomOutAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                zoomOut();
            }
        };
        
        registerZoomInZoomOutActions(zoomChooser);
        registerZoomInZoomOutActions(designView);
    }
    
    
    public int getComponentCount() { 
        return components.length; 
    }
    
    
    public JComponent getComponent(int i) { 
        return components[i]; 
    }
    
    
    public DesignView getDesignView() {
        return designView;
    }
    

    public void actionPerformed(ActionEvent e) {
        Object eventSource = e.getSource();

        if (eventSource == fitWidthButton) {
            fitWidth();
        } else if (eventSource == fitDiagramButton) {
            fitDiagram();
        } else if (eventSource == oneToOneButton) {
            oneToOne();
        } else if (eventSource == zoomInButton) {
            zoomIn();
        } else if (eventSource == zoomOutButton) {
            zoomOut();
        } else if (eventSource == zoomChooser) {
            String s = zoomChooser.getSelectedItem().toString().trim();
            if (s.endsWith("%")) { // NOI18N
                s = s.substring(0, s.length() - 1);
            }
            
            int newZoomPercents = zoomValue;
            
            try {
                newZoomPercents = Integer.parseInt(s);
            } catch (NumberFormatException exception) {}
            
            setZoom(newZoomPercents);
        }
    }
    
    
    
    private void registerZoomInZoomOutActions(JComponent component) {
        InputMap im1 = component.getInputMap(JComponent
                .WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        InputMap im2 = component.getInputMap(JComponent.WHEN_FOCUSED);

        im1.put(ZOOM_IN_KEY_STROKE, ZOOM_IN_KEY);
        im2.put(ZOOM_IN_KEY_STROKE, ZOOM_IN_KEY);

        im1.put(ZOOM_OUT_KEY_STROKE, ZOOM_OUT_KEY);
        im2.put(ZOOM_OUT_KEY_STROKE, ZOOM_OUT_KEY);
        
        ActionMap am = component.getActionMap();
        
        am.put(ZOOM_IN_KEY, zoomInAction);
        am.put(ZOOM_OUT_KEY, zoomOutAction);
    }
    
    
    private Point getDefaultZoomPoint() {
        Rectangle rect = getDesignView().getVisibleRect();
        return new Point(rect.x + rect.width / 2, rect.y + rect.height / 2);
    }
    
    
    public void fitWidth() {
        double zc = DiagramFontUtil.getZoomCorrection();
        
        
        int screen_width = designView.getWidth();
        screen_width -= DiagramViewLayout.MARGIN_LEFT * 3;
        screen_width -= DiagramViewLayout.MARGIN_RIGHT * 3;
        
        float content_width  = 
                getDesignView().getProcessView().getContentSize().width +
                getDesignView().getConsumersView().getContentSize().width +
                getDesignView().getProvidersView().getContentSize().width;

        double newZoom = screen_width * 100.0 / content_width / zc;
        
        if (newZoom != newZoom) {
            newZoom = 100;
        } else if (newZoom < MIN_ZOOM_VALUE) {
            newZoom = MIN_ZOOM_VALUE;
        } else if (newZoom > MAX_ZOOM_VALUE) {
            newZoom = MAX_ZOOM_VALUE;
        }
        
        setZoom((int) newZoom);        
    }
    
    
    public void fitDiagram() {
        double zc = DiagramFontUtil.getZoomCorrection();
        
        JScrollPane scrollPane = (JScrollPane) getDesignView().getProcessView().getParent()
                .getParent();
        
        Dimension pSize = scrollPane.getSize();
        Insets insets = scrollPane.getInsets();
        
        FDimension dSize = getDesignView().getDiagramSize();
        
        int width = pSize.width - insets.left - insets.right;
        width -= scrollPane.getVerticalScrollBar().getWidth();
        width -= DiagramViewLayout.MARGIN_LEFT;
        width -= DiagramViewLayout.MARGIN_RIGHT;
        
        int height = pSize.height - insets.top - insets.bottom;
        height -= scrollPane.getHorizontalScrollBar().getHeight();
        height -= DiagramViewLayout.MARGIN_BOTTOM;
        height -= DiagramViewLayout.MARGIN_TOP;
        
        double zoomX = width * 100.0 / dSize.width / zc;
        double zoomY = height * 100.0 / dSize.height / zc;
        
        double newZoom = Math.min(zoomX, zoomY);
        
        if (newZoom != newZoom) {
            newZoom = 100;
        } else if (newZoom < MIN_ZOOM_VALUE) {
            newZoom = MIN_ZOOM_VALUE;
        } else if (newZoom > MAX_ZOOM_VALUE) {
            newZoom = MAX_ZOOM_VALUE;
        }
        
        setZoom((int) newZoom);
    }
    
    
    public void oneToOne() {
        setZoom(100);
    }
    
    
    public void zoomIn() {
        Point zoomPoint = getDefaultZoomPoint();
        zoomIn(zoomPoint.x, zoomPoint.y);
    }
    
    
    public void zoomIn(int zoomPointX, int zoomPointY) {
        int newZoomValue;
        
        if (zoomValue >= 100) {
            newZoomValue = ((zoomValue + ZOOM_STEP_G100) / ZOOM_STEP_G100) 
                    * ZOOM_STEP_G100;
        } else {
            newZoomValue = ((zoomValue + ZOOM_STEP_L100) / ZOOM_STEP_L100) 
                    * ZOOM_STEP_L100;
        }
        
        setZoom(newZoomValue, zoomPointX, zoomPointY);
    }
    

    public void zoomOut() {
        Point zoomPoint = getDefaultZoomPoint();
        zoomOut(zoomPoint.x, zoomPoint.y);
    }
    
    
    public void zoomOut(int zoomPointX, int zoomPointY) {
        int newZoomValue;
        
        if (zoomValue > 100) {
            newZoomValue = ((zoomValue - 1) / ZOOM_STEP_G100) * ZOOM_STEP_G100;
        } else {
            newZoomValue = ((zoomValue - 1) / ZOOM_STEP_L100) * ZOOM_STEP_L100;
        }
        
        setZoom(newZoomValue, zoomPointX, zoomPointY);
    }
    
    
    public void setZoom(int newZoomValue) {
        Point zoomPoint = getDefaultZoomPoint();
        setZoom(newZoomValue, zoomPoint.x, zoomPoint.y);
    }
    

    public void setZoom(int newZoomValue, int zoomPointX, int zoomPointY) {
        DesignView desingView = getDesignView();
//        JViewport viewport = (JViewport) desingView.getParent();
      // JScrollPane scrollPane = (JScrollPane) viewport.getParent();
        
        if (newZoomValue < MIN_ZOOM_VALUE) {
            newZoomValue = MIN_ZOOM_VALUE;
        } else if (newZoomValue > MAX_ZOOM_VALUE) {
            newZoomValue = MAX_ZOOM_VALUE;
        }
        
        if (newZoomValue != zoomValue) {
           /* FPoint diagramZoomPoint = desingView.convertScreenToDiagram(
                    new Point(zoomPointX, zoomPointY));

            Rectangle rect = getDesignView().getVisibleRect();

            int dx = zoomPointX - rect.x;
            int dy = zoomPointY - rect.y;*/

            zoomValue = newZoomValue;

           // desingView.getDecorationManager().repositionComponentsRecursive();

//            viewport.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        
          //  desingView.invalidate();
        //    desingView.validate();

  //          viewport.setScrollMode(JViewport.BLIT_SCROLL_MODE);
        
            //FIXME desingView.getNameEditor().updateBounds();
/*
            Point newScreenZoomPoint = desingView.convertDiagramToScreen(
                    diagramZoomPoint);
        
            rect = desingView.getVisibleRect();
            rect.x = newScreenZoomPoint.x - dx;
            rect.y = newScreenZoomPoint.y - dy;

            
            int designViewWidth = desingView.getWidth();
            int designViewHeight = designView.getHeight();
            
            if (rect.x + rect.width > designViewWidth) {
                rect.x = designViewWidth - rect.width;
            }

            if (rect.y + rect.height > designViewHeight) {
                rect.y = designViewHeight - rect.height;
            }

            if (rect.x < 0) {
                rect.x = 0;
            }

            if (rect.y < 0) {
                rect.y = 0;
            }
        
            desingView.scrollRectToVisible(rect);*/
            desingView.getProcessView().invalidate();
            desingView.getConsumersView().invalidate();
            desingView.getProvidersView().invalidate();
            
            
            desingView.validate();
            desingView.repaint();   
        
            designView.getRightStripe().repaint();
        }
        
        updateComponentsStates();
    }
    
    
    private void updateComponentsStates() {
        zoomChooser.removeActionListener(this);
        zoomChooser.setSelectedItem(getZoomString());
        zoomChooser.addActionListener(this);
        zoomInButton.setEnabled(!isMaxZoom());
        zoomOutButton.setEnabled(!isMinZoom());
    }
    
    
    public void setEnabled(boolean value) {
        fitWidthButton.setEnabled(value);
        fitDiagramButton.setEnabled(value);
        oneToOneButton.setEnabled(value);
        
        zoomChooser.setEnabled(value);
        
        zoomInButton.setEnabled(value);
        zoomOutButton.setEnabled(value);
        
        zoomInAction.setEnabled(value);
        zoomOutAction.setEnabled(value);
        
        if (value) {
            updateComponentsStates();
        }
    }
    
    
    public int getZoom() { return zoomValue; }
    public String getZoomString() { return zoomValue + "%"; } // NOI18N
    
    public double getScale() { return 0.01 * zoomValue; }
    
    public boolean isMinZoom() { return zoomValue == MIN_ZOOM_VALUE; }
    public boolean isMaxZoom() { return zoomValue == MAX_ZOOM_VALUE; }

    
    private JButton createButton(Icon icon, String toolTipTextKey) {
        JButton button = new JButton(icon);
        button.setToolTipText(getMessage(toolTipTextKey));
        button.addActionListener(this);
        return button;
    }
    

    private static String getMessage(String key) {
        return NbBundle.getBundle(ZoomManager.class).getString(key); 
    }
    
    
    private static Icon loadIcon(String name) {
        return new ImageIcon(ZoomManager.class
                .getResource("resources/" + name)); // NOI18N
    }    
    
    
    public static final int MIN_ZOOM_VALUE = 33;
    public static final int MAX_ZOOM_VALUE = 200;
    
    private static final String[] ZOOM_PRESETS = { 
            "33%", "50%", "75%", "100%", "150%", "200%" }; // NOI18N
    
    private static final Icon FIT_DIAGRAM_ICON = loadIcon("fit_diagram.png"); // NOI18N
    private static final Icon FIT_WIDTH_ICON = loadIcon("fit_width.png"); // NOI18N
    private static final Icon ONE_TO_ONE_ICON = loadIcon("normal_size.png"); // NOI18N

    private static final Icon ZOOM_IN_ICON = loadIcon("zoom_in.png"); // NOI18N
    private static final Icon ZOOM_OUT_ICON = loadIcon("zoom_out.png"); // NOI18N
    
    private static final int ZOOM_STEP_L100 = 5;
    private static final int ZOOM_STEP_G100 = 25;
    
    private static final String ZOOM_IN_KEY = "diagramZoomInAction";
    private static final String ZOOM_OUT_KEY = "diagramZoomOutAction";
        
    private static final KeyStroke ZOOM_IN_KEY_STROKE = KeyStroke
            .getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.CTRL_DOWN_MASK);
    
    private static final KeyStroke ZOOM_OUT_KEY_STROKE = KeyStroke
            .getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK);
}
