/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows.view;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Arrays;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.view.dnd.TopComponentDroppable;
import org.netbeans.core.windows.view.dnd.WindowDnDManager; // PENDING
import org.netbeans.core.windows.ModeImpl; // PENDING
import org.netbeans.core.windows.WindowManagerImpl; // PENDING

import org.openide.ErrorManager;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;


/**
 * Class which represents model of editor area element for GUI hierarchy. 
 *
 * @author  Peter Zavadsky
 */
public class EditorView extends ViewElement {

    private ViewElement editorArea;
    
    private Component component;
    
    // XXX PENDING
    private final WindowDnDManager windowDnDManager;
    
    
    public EditorView(Controller controller, WindowDnDManager windowDnDManager,
    double resizeWeight, ViewElement editorArea) {
        super(controller, resizeWeight);
        
        this.editorArea = editorArea;
        this.windowDnDManager = windowDnDManager;
    }
    
    
    public Component getComponent() {
        if(component == null) {
            initComponent();
        }

        return component;
    }
    
    // XXX
    Rectangle getPureBounds() {
        if(editorArea == null) {
            return new Rectangle();
        } else {
            Component comp = editorArea.getComponent();
            Rectangle bounds = comp.getBounds();
            Point location = new Point(0, 0);
            javax.swing.SwingUtilities.convertPointToScreen(location, comp);
            bounds.setLocation(location);
            return bounds;
        }
    }
    
    
    private void initComponent() {
        JPanel panel = new EditorComponent(this, windowDnDManager);
        if(editorArea != null) {
            manageBorder(panel);
            panel.add(editorArea.getComponent(), BorderLayout.CENTER);
        }
        component = panel;
    }
    
    /** Handles special border policy - scroll pane like border only 
     * if editor area is null.
     */
    private void manageBorder (JPanel panel) {
        if (editorArea != null) {
            panel.setBorder(null);
        } else {
            // special border installed into UI manager by netbeans
            panel.setBorder((Border)UIManager.get("Nb.ScrollPane.border"));
        }
    }
    
    public ViewElement getEditorArea() {
        return editorArea;
    }
    
    public void setEditorArea(ViewElement editorArea, boolean addingAllowed) {
        EditorComponent editorComp = (EditorComponent)getComponent();        
        
        if(this.editorArea == editorArea) {
            if(this.editorArea == null
            || Arrays.asList(editorComp.getComponents()).contains(this.editorArea.getComponent())) {
                return;
            }
        }

        // Remove the old one.
        if(this.editorArea != null) {
            editorComp.remove(this.editorArea.getComponent());
        }
        this.editorArea = editorArea;
        manageBorder(editorComp);
        
        // XXX #36885 When in maximixed and compact mode, we cannot add the components
        // into the editor area, it would remove it from the screen.
        if(addingAllowed) {
            if(this.editorArea != null) {
                editorComp.add(this.editorArea.getComponent(), BorderLayout.CENTER);
            }

            editorComp.validate();
            editorComp.repaint();
        }
    }

    
    private static class EditorComponent extends JPanel
    implements TopComponentDroppable {
        
        private final EditorView editorView;
        
        // XXX PENDING
        private final WindowDnDManager windowDnDManager;
        
        
        public EditorComponent(EditorView editorView, WindowDnDManager windowDnDManager) {
            this.editorView = editorView;
            this.windowDnDManager = windowDnDManager;
            
            init();
        }

        
        private void init() {
            setLayout(new BorderLayout());
            // special background for XP style
            String lfID = UIManager.getLookAndFeel().getID();
            if (lfID.equals("Windows")) {
                setBackground((Color)UIManager.get("nb_workplace_fill"));
            }

            // PENDING TEMP Adding image into empty area.
            String imageSource = Constants.SWITCH_IMAGE_SOURCE; // NOI18N
            if(imageSource != null) {
                Image image = Utilities.loadImage(imageSource);
                if(image != null) {
                    JLabel label = new JLabel(new ImageIcon(image));
                    label.setMinimumSize(new Dimension(0, 0)); // XXX To be able shrink the area.
                    add(label, BorderLayout.CENTER);
                } else {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new NullPointerException("Image not found at " + imageSource)); // NOI18N
                }
            }
        }
        
        public Shape getIndicationForLocation(Point location) {
            ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(windowDnDManager.getStartingTransfer());
            int kind = mode != null ? mode.getKind() : Constants.MODE_KIND_EDITOR;
                        
            if(kind == Constants.MODE_KIND_EDITOR) {
                Rectangle rect = getBounds();
                rect.setLocation(0, 0);
                return rect;
            } else {
                Rectangle rect = getBounds();
                rect.setLocation(0, 0);

                String side = getSideForLocation(location);

                double ratio = Constants.DROP_AROUND_RATIO;
                if(side == Constants.TOP) {
                    return new Rectangle(0, 0, rect.width, (int)(rect.height * ratio));
                } else if(side == Constants.LEFT) {
                    return new Rectangle(0, 0, (int)(rect.width * ratio), rect.height);
                } else if(side == Constants.RIGHT) {
                    return new Rectangle(rect.width - (int)(rect.width * ratio), 0, (int)(rect.width * ratio), rect.height);
                } else if(side == Constants.BOTTOM) {
                    return new Rectangle(0, rect.height - (int)(rect.height * ratio), rect.width, (int)(rect.height * ratio));
                } else {
                    return null;
                }
            }
        };
        
        public Object getConstraintForLocation(Point location) {
            ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(windowDnDManager.getStartingTransfer());
            int kind = mode != null ? mode.getKind() : Constants.MODE_KIND_EDITOR;
                        
            if(kind == Constants.MODE_KIND_EDITOR) {
                return null;
            } else {
                return getSideForLocation(location);
            }
        }
        
        private String getSideForLocation(Point location) {
            Rectangle bounds = getBounds();
            bounds.setLocation(0, 0);

            // Size of area which indicates creation of new split.
            int delta = Constants.DROP_AREA_SIZE;

            Rectangle top = new Rectangle(0, 0, bounds.width, delta);
            if(top.contains(location)) {
                return Constants.TOP;
            }

            Rectangle left = new Rectangle(0, delta, delta, bounds.height - 2 * delta);
            if(left.contains(location)) {
                return Constants.LEFT;
            }

            Rectangle right = new Rectangle(bounds.width - delta, delta, delta, bounds.height - 2 * delta);
            if(right.contains(location)) {
                return Constants.RIGHT;
            }

            Rectangle bottom = new Rectangle(0, bounds.height - delta, bounds.width, delta);
            if(bottom.contains(location)) {
                return Constants.BOTTOM;
            }

            return null;
        }
        
        public Component getDropComponent() {
            return this;
        }
        
        public ViewElement getDropViewElement() {
            return editorView;
        }
        
        public boolean canDrop(TopComponent transfer, Point location) {
            if(Constants.SWITCH_MODE_ADD_NO_RESTRICT
            || WindowManagerImpl.getInstance().isTopComponentAllowedToMoveAnywhere(transfer)) {
                return true;
            }
            
            ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(transfer);
            int kind = mode != null ? mode.getKind() : Constants.MODE_KIND_EDITOR;

            if(kind == Constants.MODE_KIND_EDITOR) {
                return true;
            } else {
                if(getSideForLocation(location) != null) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        
        public boolean supportsKind(int kind, TopComponent tc) {
            return true;
        }
        
    } // End of EditorComponent class.
    
    
}

