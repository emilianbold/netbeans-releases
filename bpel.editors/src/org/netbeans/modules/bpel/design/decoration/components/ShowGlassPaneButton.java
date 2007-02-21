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


package org.netbeans.modules.bpel.design.decoration.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.nodes.BpelNode.NodeBadges.WARNING;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.openide.util.NbBundle;

/**
 *
 * @author aa160298
 */
public class ShowGlassPaneButton extends JToggleButton
        implements ActionListener, HierarchyListener, DecorationComponent 
{
    private GlassPane glassPane = new GlassPane();
    
    private Icon icon;
    
    public ShowGlassPaneButton(List<ResultItem> resultItems) {
        super(ERROR_ICON);
        addActionListener(this);
        addHierarchyListener(this);
        
        setOpaque(false);
        setBorder(null);
        setRolloverEnabled(true);
        setContentAreaFilled(false);
        setFocusable(false);
        
        fillGlassPaneHeader(resultItems);
        fillGlassPaneContent(resultItems);
        
        setPreferredSize(new Dimension(icon.getIconWidth() + 6, 
                icon.getIconHeight() + 6));
        
        glassPane.addHierarchyListener(this);
    }
    
    
    private void fillGlassPaneHeader(List<ResultItem> resultItems) {
        int errorsCount = 0;
        int warningsCount = 0;
        int advicesCount = 0;
        
        for (ResultItem item : resultItems) {
            switch (item.getType()) {
                case ERROR: 
                    errorsCount++; 
                    break;
                case WARNING: 
                    warningsCount++; 
                    break;
                case ADVICE: 
                    advicesCount++; 
                    break;
            }
        }
        
        if (errorsCount > 0) {
            if (errorsCount > 1) {
                glassPane.addHeader(ERROR_ICON, "" + errorsCount + " "
                        + NbBundle.getMessage(getClass(), 
                        "LBL_ShowGlassPaneButton_N_Errors").trim()); // NOI18N
            } else {
                glassPane.addHeader(ERROR_ICON, NbBundle.getMessage(getClass(), 
                        "LBL_ShowGlassPaneButton_1_Error").trim()); // NOI18N
            }
        } 

        if (warningsCount > 0) {
            if (warningsCount > 1) {
                glassPane.addHeader(WARNING_ICON, "" + warningsCount + " " 
                        + NbBundle.getMessage(getClass(), 
                        "LBL_ShowGlassPaneButton_N_Warnings").trim()); // NOI18N
            } else {
                glassPane.addHeader(WARNING_ICON, NbBundle.getMessage(getClass(), 
                        "LBL_ShowGlassPaneButton_1_Warning").trim()); // NOI18N
            }
        } 

        if (advicesCount > 0) {
            if (advicesCount > 1) {
                glassPane.addHeader(ADVICE_ICON, "" + advicesCount + " "
                        + NbBundle.getMessage(getClass(), 
                        "LBL_ShowGlassPaneButton_N_Advices").trim()); // NOI18N
            } else {
                glassPane.addHeader(ADVICE_ICON, NbBundle.getMessage(getClass(), 
                        "LBL_ShowGlassPaneButton_1_Advice").trim()); // NOI18N
            }
        } 
        
        
        if (errorsCount > 0) {
            icon = ERROR_ICON;
        } else if (warningsCount > 0) {
            icon = WARNING_ICON;
        } else if (advicesCount > 0) {
            icon = ADVICE_ICON;
        }
    }
    
    
    private void fillGlassPaneContent(List<ResultItem> resultItems) {
        addToGlassPane(resultItems, ResultType.ERROR); // NOI18N
        addToGlassPane(resultItems, ResultType.ADVICE); // NOI18N
        addToGlassPane(resultItems, ResultType.WARNING); // NOI18N
        glassPane.updateHTML();
    }
    
    
    private void addToGlassPane(List<ResultItem> resultItems, ResultType type) 
    {
        List<ResultItem> items = filterResultItems(resultItems, type);

        String icon = null;
        String slowIcon = null;
        
        switch (type) {
            case ERROR:
                icon = ERROR_ICON_PATH;
                slowIcon = ERROR_SLOW_ICON_PATH;
                break;
            case WARNING:
                icon = WARNING_ICON_PATH;
                slowIcon = WARNING_SLOW_ICON_PATH;
                break;
            case ADVICE:
                icon = ADVICE_ICON_PATH;
                slowIcon = ADVICE_SLOW_ICON_PATH;
        }
        
        for (ResultItem item : items) {
            if ("BPELSchemaValidator".equals(item.getValidator().getName())) { // NOI18N
                glassPane.addListItem(slowIcon, item.getDescription());
            } else {
                glassPane.addListItem(icon, item.getDescription());
            }
        }
    }
    
    
    private List<ResultItem> filterResultItems(List<ResultItem> resultItems, 
            ResultType type) 
    {
        List<ResultItem> result = new ArrayList<ResultItem>();
        for (ResultItem item : resultItems) {
            if (item.getType() == type) {
                result.add(item);
            }
        }
        return result;
    }

    
    public void actionPerformed(ActionEvent e) {
        if (!isGlassPaneShown()) {
            showGlassPane();
        } else {
            hideGlassPane();
        }
    }


    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        
        if (isGlassPaneShown()) {
            updateGlassPaneBounds();
        }
    }
    
    
    private void showGlassPane() {
        getDesignView().add(glassPane);
        updateGlassPaneBounds();
        glassPane.scrollRectToVisible(new Rectangle(0, 0, 
                glassPane.getWidth(), glassPane.getHeight()));
    }
    
    
    private void hideGlassPane() {
        DesignView designView = (DesignView) glassPane.getParent();
        designView.remove(glassPane);
        designView.revalidate();
        designView.repaint();
    }
    
    
    private DesignView getDesignView() {
        return (DesignView) SwingUtilities
                .getAncestorOfClass(DesignView.class, this);
    }
    
    
    private boolean isGlassPaneShown() {
        return (glassPane.getParent() != null);
    }
    
    
    private void updateGlassPaneBounds() {
        DesignView designView = getDesignView();

        Point coords = SwingUtilities.convertPoint(this, 
                getWidth() + 1, getHeight() / 2, designView);
        
        Dimension size = glassPane.getPreferredSize();
        
        glassPane.setBounds(coords.x, coords.y, size.width, size.height);
        
        designView.revalidate();
        designView.repaint();
    }
    
    
    public void hierarchyChanged(HierarchyEvent e) {
        if (e.getSource() == this) {
            if ((getParent() == null) && isGlassPaneShown()) {
                hideGlassPane();
            }
        } else if (e.getSource() == glassPane) {
            if (glassPane.getParent() == null) {
                setSelected(false);
            } else {
                setSelected(true);
            }
        }
    }
    
    
    protected void paintComponent(Graphics g) {
        ButtonModel model = getModel();
        
        if (model.isPressed()) {
            ButtonRenderer.paintButton(this, g, 
                    ButtonRenderer.PRESSED_FILL_COLOR, false, 
                    ButtonRenderer.PRESSED_BORDER_COLOR, 
                    ButtonRenderer.PRESSED_STROKE_WIDTH, icon);
        } else if (model.isRollover()) {
            ButtonRenderer.paintButton(this, g, 
                    ButtonRenderer.ROLLOVER_FILL_COLOR, true, 
                    ButtonRenderer.ROLLOVER_BORDER_COLOR, 
                    ButtonRenderer.ROLLOVER_STROKE_WIDTH, icon);
            
        } else if (model.isSelected()) {
            ButtonRenderer.paintButton(this, g, BACKGROUND, false, 
                    ButtonRenderer.PRESSED_BORDER_COLOR, 
                    ButtonRenderer.PRESSED_STROKE_WIDTH, icon);
        } else {
            ButtonRenderer.paintButton(this, g, BACKGROUND, false, 
                    null, ButtonRenderer.NORMAL_STROKE_WIDTH, icon);
        }
    }
    
    private static Color BACKGROUND = new Color(0xCCFFFFFF, true);
    
    private static final String ERROR_ICON_PATH = "resources/error.png"; // NOI18N
    private static final String ERROR_SLOW_ICON_PATH = "resources/error_explicit.png"; // NOI18N
    
    private static final String WARNING_ICON_PATH = "resources/warning.png"; // NOI18N
    private static final String WARNING_SLOW_ICON_PATH = "resources/warning_explicit.png"; // NOI18N
    
    private static final String ADVICE_ICON_PATH = "resources/advice.png"; // NOI18N
    private static final String ADVICE_SLOW_ICON_PATH = "resources/advice_explicit.png"; // NOI18N
    
    private static final Icon ERROR_ICON;
    private static final Icon ERROR_SLOW_ICON;
    
    private static final Icon WARNING_ICON;
    private static final Icon WARNING_SLOW_ICON;

    private static final Icon ADVICE_ICON;
    private static final Icon ADVICE_SLOW_ICON;
    
    static {
        ERROR_ICON        = new ImageIcon(Decoration.class.getResource(ERROR_ICON_PATH));
        ERROR_SLOW_ICON   = new ImageIcon(Decoration.class.getResource(ERROR_SLOW_ICON_PATH));
        WARNING_ICON      = new ImageIcon(Decoration.class.getResource(WARNING_ICON_PATH));
        WARNING_SLOW_ICON = new ImageIcon(Decoration.class.getResource(WARNING_SLOW_ICON_PATH));
        ADVICE_ICON       = new ImageIcon(Decoration.class.getResource(ADVICE_ICON_PATH));
        ADVICE_SLOW_ICON  = new ImageIcon(Decoration.class.getResource(ADVICE_SLOW_ICON_PATH));
    }
}
