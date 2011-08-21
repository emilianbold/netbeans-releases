/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.visual.ui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.ImageObserver;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.modules.debugger.jpda.visual.spi.ComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.spi.RemoteScreenshot;
import org.netbeans.modules.debugger.jpda.visual.spi.ScreenshotUIManager;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * This component draws the screenshot of a remote application.
 * 
 * @author Martin Entlicher
 */
public class ScreenshotComponent extends TopComponent {
    
    private static final Logger logger = Logger.getLogger(ScreenshotComponent.class.getName());
    
    private static final Map<DebuggerEngine, Set<ScreenshotComponent>> openedScreenshots =
                         new HashMap<DebuggerEngine, Set<ScreenshotComponent>>();
    private static volatile ScreenshotComponent activeScreenshotComponent;
    
    private RemoteScreenshot screenshot;
    private ScreenshotUIManager manager;
    private NavigatorLookupHint componentHierarchyNavigatorHint = new ComponentHierarchyNavigatorHint();
    private ComponentNode componentNodes;
    private ScreenshotCanvas canvas;
    private JScrollPane scrollPane;
    private boolean propertiesOpened;
    
    public ScreenshotComponent(RemoteScreenshot screenshot, ScreenshotUIManager manager) {
        this.screenshot = screenshot;
        this.manager = manager;
        screenshot.getImage();
        ScreenshotCanvas c = new ScreenshotCanvas(screenshot.getImage());
        scrollPane = new JScrollPane(c);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        this.canvas = c;
        String title = screenshot.getTitle();
        title = (title == null) ? NbBundle.getMessage(ScreenshotComponent.class, "LBL_DebuggerSnapshot") :
                    NbBundle.getMessage(ScreenshotComponent.class, "LBL_DebuggerSnapshotOf", title);
        setDisplayName(title);
        componentNodes = new ComponentNode(screenshot.getComponentInfo());
        ComponentHierarchy.getInstance().getExplorerManager().setRootContext(componentNodes);
        setActivatedNodes(new Node[] { componentNodes });
    }
    
    public static ScreenshotComponent getActive() {
        return activeScreenshotComponent;
        /*
        WindowManager wm = WindowManager.getDefault();
        synchronized (openedScreenshots) {
            for (Set<ScreenshotComponent> ssc : openedScreenshots.values()) {
                for (ScreenshotComponent sc : ssc) {
                    Mode m = wm.findMode(sc);
                    if (m != null && m.getSelectedTopComponent() == sc) {
                        return sc;
                    }
                }
            }
        }
        return null;
         */
    }
    
    public ScreenshotUIManager getManager() {
        return manager;
    }
    
    public ComponentInfo getSelectedComponent() {
        Node[] nodes = getActivatedNodes();
        if (nodes.length > 0) {
            return nodes[0].getLookup().lookup(ComponentInfo.class);
        }
        return null;
    }

    @Override
    public Lookup getLookup() {
        Lookup lookup = super.getLookup();
        return new ProxyLookup(lookup, Lookups.singleton(componentHierarchyNavigatorHint));
    }

    @Override
    protected void componentActivated() {
        logger.fine("componentActivated() root = "+componentNodes+", ci = "+componentNodes.getLookup().lookup(ComponentInfo.class));
        activeScreenshotComponent = this;
        ComponentHierarchy.getInstance().getExplorerManager().setRootContext(componentNodes);
        ComponentHierarchy.getInstance().getExplorerManager().setExploredContext(componentNodes);
        canvas.activated();
    }

    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
        //canvas.deactivated();
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        TopComponent properties = WindowManager.getDefault().findTopComponent("properties");    // NOI18N
        if (properties != null) {
            if (!properties.isOpened()) {
                propertiesOpened = true;
                properties.open();
            }
        }
    }
    
    @Override
    protected void componentHidden() {
        super.componentHidden();
        if (propertiesOpened) {
            propertiesOpened = false;
            TopComponent properties = WindowManager.getDefault().findTopComponent("properties");    // NOI18N
            if (properties != null) {
                properties.close();
            }
        }
    }
    
    @Override
    protected void componentOpened() {
        synchronized (openedScreenshots) {
            Set<ScreenshotComponent> components = openedScreenshots.get(screenshot.getDebuggerEngine());
            if (components == null) {
                components = new HashSet<ScreenshotComponent>();
                openedScreenshots.put(screenshot.getDebuggerEngine(), components);
            }
            components.add(this);
        }
    }
    
    @Override
    protected void componentClosed() {
        if (activeScreenshotComponent == this) {
            activeScreenshotComponent = null;
        }
        synchronized (openedScreenshots) {
            Set<ScreenshotComponent> components = openedScreenshots.get(screenshot.getDebuggerEngine());
            if (components != null) {
                components.remove(this);
                if (components.isEmpty()) {
                    openedScreenshots.remove(screenshot.getDebuggerEngine());
                }
            }
        }
        canvas.deactivated();
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }
    
    public static void closeScreenshots(DebuggerEngine engine) {
        synchronized (openedScreenshots) {
            Set<ScreenshotComponent> components = openedScreenshots.get(engine);
            if (components != null) {
                final Set<ScreenshotComponent> theComponents = new HashSet<ScreenshotComponent>(components);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        for (ScreenshotComponent c : theComponents) {
                            c.close();
                        }
                    }
                });
            }
        }
    }
    
    private class ScreenshotCanvas extends JComponent {
        
        private Image image;
        private Rectangle selection;
        private Listener listener;
        private boolean active;
        
        public ScreenshotCanvas(Image image) {
            this.image = image;
            listener = new Listener();
            initSize();
        }
        
        private void initSize() {
            ImageObserver io = new ImageObserver() {
                @Override
                public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                    boolean hasHeight = (infoflags & ImageObserver.HEIGHT) > 0;
                    boolean hasWidth = (infoflags & ImageObserver.WIDTH) > 0;
                    if (!hasHeight || !hasWidth) {
                        return true;
                    }
                    ScreenshotCanvas.this.setSize(width, height);
                    ScreenshotCanvas.this.setPreferredSize(ScreenshotCanvas.this.getSize());
                    return false;
                }
            };
            int width = image.getWidth(io);
            int height = image.getHeight(io);
            if (width > 0 && height > 0) {
                setSize(width, height);
                setPreferredSize(getSize());
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            g.drawImage(image, 1, 1, null);
            g.drawRect(0, 0, image.getWidth(null) + 2, image.getHeight(null) + 2);
            if (selection != null) {
                Color c = g.getColor();
                g.setColor(Color.BLUE);
                g.drawRect(selection.x, selection.y, selection.width + 1, selection.height + 1);
                g.setColor(c);
            }
            //image.getSource();
        }
        
        void activated() {
            if (selection != null) {
                listener.selectComponentAt(selection.x + 1, selection.y + 1, true);
            }
            if (!active) {
                addMouseListener(listener);
                ComponentHierarchy.getInstance().getExplorerManager().addPropertyChangeListener(listener);
            }
            active = true;
        }
        
        void deactivated() {
            if (!active) return ;
            active = false;
            removeMouseListener(listener);
            ComponentHierarchy.getInstance().getExplorerManager().removePropertyChangeListener(listener);
        }
        
        private class Listener implements MouseListener, PropertyChangeListener {

            @Override
            public void mouseClicked(MouseEvent e) {
                //selectComponentAt(e.getX(), e.getY());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    selectComponentAt(e.getX(), e.getY(), false);
                    showPopupMenu(e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                selectComponentAt(e.getX(), e.getY(), false);
                if (e.isPopupTrigger()) {
                    showPopupMenu(e.getX(), e.getY());
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
            
            private void showPopupMenu(int x, int y) {
                Node[] activatedNodes = getActivatedNodes();
                if (activatedNodes.length == 1) {
                    Action[] actions = activatedNodes[0].getActions(true);
                    JPopupMenu contextMenu = Utilities.actionsToPopup(actions, ScreenshotComponent.this);
                    contextMenu.show(ScreenshotComponent.this.canvas, x, y);
                }
            }
            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                logger.fine("propertyChange("+evt+") propertyName = "+evt.getPropertyName());
                String propertyName = evt.getPropertyName();
                if (ExplorerManager.PROP_SELECTED_NODES.equals(propertyName)) {
                    Node[] nodes = ComponentHierarchy.getInstance().getExplorerManager().getSelectedNodes();
                    ComponentInfo ci = null;
                    if (nodes.length > 0) {
                        ci = nodes[0].getLookup().lookup(ComponentInfo.class);
                    }
                    logger.fine("nodes = "+Arrays.toString(nodes)+" => selectComponent("+ci+")");
                    selectComponent(ci, false);
                } else if (ExplorerManager.PROP_ROOT_CONTEXT.equals(propertyName)) {
                    deactivated();
                }
            }
            
            private void selectComponentAt(int x, int y, boolean reActivated) {
                x -= 1;
                y -= 1;
                ComponentInfo ci = screenshot.findAt(x, y);
                logger.fine("Component Info at "+x+", "+y+" is: "+((ci != null) ? ci.getDisplayName() : null));
                selectComponent(ci, reActivated);
            }
            
            private void selectComponent(ComponentInfo ci, boolean reActivated) {
                Node node = null;
                if (ci != null) {
                    Rectangle oldSelection = null;
                    if (selection != null) {
                        oldSelection = selection;
                    }
                    selection = ci.getWindowBounds();
                    if (oldSelection != null) {
                        if (oldSelection.equals(selection) && !reActivated) {
                            return ; // already selected
                        }
                        repaint(oldSelection.x, oldSelection.y, oldSelection.width + 3, oldSelection.height + 3);
                    }
                    repaint(selection.x, selection.y, selection.width + 3, selection.height + 3);
                    logger.fine("New selection = "+selection);
                    node = componentNodes.findNodeFor(ci);
                    logger.fine("FindNodeFor("+ci+") on '"+componentNodes+"' gives: "+node);
                }
                Node[] nodes;
                if (node != null) {
                    nodes = new Node[] { node };
                } else {
                    nodes = new Node[] {};
                }
                logger.fine("setActivated/SelectedNodes("+Arrays.toString(nodes)+")");
                setActivatedNodes(nodes);
                try {
                    ComponentHierarchy.getInstance().getExplorerManager().setSelectedNodes(nodes);
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

        }
        
    }
    
}
