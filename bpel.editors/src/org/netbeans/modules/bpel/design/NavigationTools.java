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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class NavigationTools extends JPanel implements MouseListener, 
        MouseMotionListener, MouseWheelListener, KeyListener, FocusListener, 
        ActionListener {

    
    private DiagramView currentDiagramView;
    private DesignView designView; 
    
    private Mode mode = Mode.OFF; 
    
    private int toolX;
    private int toolY;
    
    private JToggleButton editingModeButton;
    private JToggleButton navigationModeButton;
    
    private ButtonGroup buttonGroup;

    private JComponent[] controllers;
    
    
    public NavigationTools(DesignView designView) {
        this.designView = designView;
        
        setFocusable(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBackground(null);
        setOpaque(false);
        setVisible(false);
        
        designView.getProcessView().addKeyListener(this);
        designView.getConsumersView().addKeyListener(this);
        designView.getProvidersView().addKeyListener(this);
        
        designView.getProcessView().addFocusListener(this);
        designView.getConsumersView().addFocusListener(this);
        designView.getProvidersView().addFocusListener(this);
        
        editingModeButton = new JToggleButton(SELECT_TOOL_ICON);
        editingModeButton.setSelected(true);
        editingModeButton.setFocusable(false);
        editingModeButton.setToolTipText(
                getMessage("LBL_NavigationTools_EditingMode")); // NOI18N
        
        navigationModeButton = new JToggleButton(HAND_TOOL_ICON);
        navigationModeButton.setSelected(false);
        navigationModeButton.setFocusable(false);
        navigationModeButton.setToolTipText(
                getMessage("LBL_NavigationTools_NavigationMode")); // NOI18N
        
        buttonGroup = new ButtonGroup();
        buttonGroup.add(editingModeButton);
        buttonGroup.add(navigationModeButton);
        
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);

        editingModeButton.addActionListener(this);
        navigationModeButton.addActionListener(this);
        
        controllers = new JComponent[] {
            editingModeButton,
            navigationModeButton
        };
    }
    
    
    public int getControllersCount() {
        return controllers.length;
    }
    
    
    public JComponent getController(int i) {
        return controllers[i];
    }

    
    public DesignView getDesignView() {
        return designView;
    }
    
    
    private void turnOn(Mode newMode) {
        this.mode = newMode;
        setVisible(true);
    }
    
    
    private void turnOff() {
        this.mode = Mode.OFF;
        setVisible(false);
    }
    
    
    public void setEnabled(boolean v) {
        editingModeButton.setEnabled(v);
        navigationModeButton.setEnabled(v);
    }
    
    
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE && mode == Mode.OFF) {
            turnOn(Mode.WEAK);
        }
    }

    
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE && mode == Mode.WEAK) {
            turnOff();
        }
    }
    
    
    public void focusLost(FocusEvent e) {
        if (mode == Mode.WEAK) {
            turnOff();
        }
    }


    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() > 0) {
            getDesignView().getZoomManager().zoomIn(e.getX(), e.getY());
        } else {
            getDesignView().getZoomManager().zoomOut(e.getX(), e.getY());
        }
        
        Point p = getDesignView().getMousePosition(true);
        
        if (p != null) {
            toolX = p.x;
            toolY = p.y;
        }
    }

    
    public void mouseDragged(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        
        int dx = mx - toolX;
        int dy = my - toolY;

        toolX = mx;
        toolY = my;
        
        if (currentDiagramView != null) {
            Rectangle rect = currentDiagramView.getVisibleRect();
            
            rect.x -= dx;
            rect.y -= dy;

            currentDiagramView.scrollRectToVisible(rect);
        }
    }
    
    
    public void mousePressed(MouseEvent e) {
        TriScrollPane scrollPane = (TriScrollPane) getParent();
        
        int mx = e.getX();
        int my = e.getY();
        
        JViewport processViewport = (JViewport) (designView
                .getProcessView().getParent());
        JComponent consumerTopComponent = (JComponent) (designView
                .getConsumersView().getParent().getParent().getParent());
        JComponent providerTopComponent = (JComponent) (designView
                .getProvidersView().getParent().getParent().getParent());
        
        if (contains(processViewport, mx, my)) {
            currentDiagramView = designView.getProcessView();
        } else if (contains(consumerTopComponent, mx, my)) {
            currentDiagramView = designView.getConsumersView();
        } else if (contains(providerTopComponent, mx, my)) {
            currentDiagramView = designView.getProvidersView();
        } else {
            currentDiagramView = null;
        }
        
        toolX = e.getX();
        toolY = e.getY();
    }
    
    
    public void mouseReleased(MouseEvent e) {
        currentDiagramView = null;
    }

    
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
    public void keyTyped(KeyEvent e) {}
    public void focusGained(FocusEvent e) {}

    public void actionPerformed(ActionEvent e) {
        if (navigationModeButton.isSelected()) {
            turnOn(Mode.STRONG);
        }
        
        if (editingModeButton.isSelected()) {
            turnOff();
        }
    }

    private static enum Mode { OFF, WEAK, STRONG }
    
    
    private static String getMessage(String key) {
        return NbBundle.getBundle(NavigationTools.class).getString(key); 
    }
    
    
    private static Icon loadIcon(String name) {
        return new ImageIcon(NavigationTools.class
                .getResource("resources/" + name)); // NOI18N
    }       
    
    private static boolean contains(Component c, int px, int py) {
        if (!c.isVisible()) return false;
        px -= c.getX();
        py -= c.getY();
        return 0 <= px && 0 <= py && px < c.getWidth() && py < c.getHeight();
    }
    
    private static Icon HAND_TOOL_ICON = loadIcon("hand_tool.png"); // NOI18N
    private static Icon SELECT_TOOL_ICON = loadIcon("select_tool.png"); // NOI18N
}
