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

package org.netbeans.core.windows.view.ui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.view.EditorView;
import org.netbeans.core.windows.view.SlidingView;
import org.netbeans.core.windows.view.ViewElement;
import org.netbeans.core.windows.view.ui.slides.SlideOperation;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.openide.ErrorManager;
import org.openide.windows.TopComponent;

/** Implementation of compact mode desktop, containing split views as well
 * as slide bars.
 *
 * @author  Dafe Simonek
 */
public final class DesktopImpl {

    /** overall layered pane, contains desktop at regular layer and slided 
     * component on upper layers */ 
    private JLayeredPane layeredPane;
    /** panel which holds regular desktop - split view root and slide bars */ 
    private JPanel desktop;
    /** root of slit views */
    private ViewElement splitRoot;
    private ViewElement maximizedMode;
    private Component splitRootComponent;
    private Component viewComponent;
    
    /** slide bars. Lazy initialization, because slide bars are optional. */
    private Set slidingViews;
    /** slide in operation in progress or null if no component is currently slided in */
    private SlideOperation curSlideIn;

    /** Minimal thick of slided component when system is trying to align
     * slided component with editor area */
    private static final int MIN_EDITOR_ALIGN_THICK = 200;
    
    /** Creates a new instance of DesktopImpl */
    public DesktopImpl () {
        // layered pane with absolute positioning, to enable overlapping
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(new LayeredLayout());
        // desktop represents regular layer of layeredPane
        desktop = new JPanel();
        desktop.setLayout(new BorderLayout());
        layeredPane.add(desktop);
    }
    
    public Component getDesktopComponent () {
        return layeredPane;
    }

    public Dimension getInnerPaneDimension() {
        int width = desktop.getSize().width;
        int height = desktop.getSize().height;
        SlidingView view = findView(Constants.LEFT);
        width = (view != null ? width - view.getComponent().getSize().width : width);
        view = findView(Constants.RIGHT);
        width = (view != null ? width - view.getComponent().getSize().width : width);
        view = findView(Constants.BOTTOM);
        height = (view != null ? height - view.getComponent().getSize().height : height);
        return new Dimension(width, height);
    }
    
   public void setSplitRoot (ViewElement splitRoot) {
 
        this.splitRoot = splitRoot;
        if (splitRoot != null) {
//            System.out.println("desktopimpl: splitroot comp");
            setViewComponent(splitRoot.getComponent());
        } else {
//            System.out.println("desktopimpl: null");
            setViewComponent(null);
        }

    }
   

    
   public void setMaximizedView(ViewElement component) {

        maximizedMode = component;
        if (component.getComponent() != viewComponent) {
//            System.out.println("desktopimpl : maximized perform");
            setViewComponent(component.getComponent());

        }
    }
    
    private void setViewComponent( Component component) {
        if (viewComponent == component) {
            return;
        }
        if (viewComponent != null) {
//            System.out.println("desktopimpl : removing compl");
            desktop.remove(viewComponent);
        }
        viewComponent = component;
        if (viewComponent != null) {
//            System.out.println("desktopimpl : adding compl");
            desktop.add(component, BorderLayout.CENTER);
        }
        layeredPane.revalidate();
        layeredPane.repaint();
    }    
    
    public ViewElement getSplitRoot () {
        return splitRoot;
    }
    
    public void addSlidingView (SlidingView view) {
        Set slidingViews = getSlidingViews();
        if (slidingViews.contains(view)) {
            return;
        }
        slidingViews.add(view);
        String constraint = convertSide(view.getSide());
        desktop.add(view.getComponent(), constraint);
        if (constraint == BorderLayout.WEST || constraint == BorderLayout.SOUTH) {
            redefineBorderForSouthBar();
        }
        // #45033 fix - invalidate isn't enough, revalidate is correct
        layeredPane.revalidate();
    }
    
    public void removeSlidingView (SlidingView view) {
        Set slidingViews = getSlidingViews();
        if (!slidingViews.contains(view)) {
            return;
        }
        slidingViews.remove(view);
        String constraint = convertSide(view.getSide());
        desktop.remove(view.getComponent());
        if (constraint == BorderLayout.WEST || constraint == BorderLayout.SOUTH) {
            redefineBorderForSouthBar();
        }
        checkCurSlide();
        // #45033 fix - invalidate isn't enough, revalidate is correct
        layeredPane.revalidate();
    }
    
    private void checkCurSlide() {
        if (curSlideIn != null) {
            SlidingView curView = null;
            Component curSlideComp = curSlideIn.getComponent();
            for (Iterator iter = slidingViews.iterator(); iter.hasNext(); ) {
                curView = (SlidingView)iter.next();
                if (curView.getTopComponents().contains(curSlideComp)) {
                    return;
                }
            }
            // currently slided component not found in view data, so remove
            layeredPane.remove(curSlideComp);
        }
    }
    
    private void redefineBorderForSouthBar() {
        SlidingView south = null;
        SlidingView west = null;
        Iterator it = slidingViews.iterator();
        while (it.hasNext()) {
            SlidingView view = (SlidingView)it.next();
            if (Constants.LEFT.equals(view.getSide())) {
                west = view;
            }
            if (Constants.BOTTOM.equals(view.getSide())) {
                south = view;
            }
        }
        if (south != null) {
            if (west != null) {
                ((JComponent)south.getComponent()).setBorder(new EmptyBorder(1, 26, 2, 26));
            } else {
                ((JComponent)south.getComponent()).setBorder(new EmptyBorder(1, 2, 2, 26));
            }
        }
    }
    
    public void performSlideIn(SlideOperation operation, Rectangle editorBounds) {
        Rectangle slideInBounds = computeSlideInBounds(operation, editorBounds);
        operation.setFinishBounds(slideInBounds);
        operation.setStartBounds(computeThinBounds(operation, slideInBounds));
        performSlide(operation);
        curSlideIn = operation;
    }
    
    public void performSlideOut(SlideOperation operation, Rectangle editorBounds) {
        Rectangle slideOutBounds = operation.getComponent().getBounds();
        operation.setStartBounds(slideOutBounds);
        operation.setFinishBounds(computeThinBounds(operation, slideOutBounds));

        curSlideIn = null;
        performSlide(operation);
        desktop.revalidate();
        desktop.repaint();
    }
    
    public void performSlideIntoEdge(SlideOperation operation, Rectangle editorBounds) {
        operation.setFinishBounds(computeLastButtonBounds(operation));
        Rectangle screenStart = operation.getStartBounds();
        operation.setStartBounds(convertRectFromScreen(layeredPane, screenStart));
        
        performSlide(operation);
    }
    
    public void performSlideIntoDesktop(SlideOperation operation, Rectangle editorBounds) {
        Rectangle screenStart = operation.getStartBounds();
        operation.setStartBounds(convertRectFromScreen(layeredPane, screenStart));
        Rectangle screenFinish = operation.getStartBounds();
        operation.setStartBounds(convertRectFromScreen(layeredPane, screenFinish));
        
        performSlide(operation);
    }
    
    /************** private stuff ***********/
    
    private void performSlide(SlideOperation operation) {
        operation.run(layeredPane, new Integer(102));
    }
    
    private Rectangle convertRectFromScreen (Component comp, Rectangle screenRect) {
        // safety call to not crash on null bounds
        if (screenRect == null) {
            screenRect = new Rectangle(0, 0, 0, 0);
        }
        Point leftTop = screenRect.getLocation();
        SwingUtilities.convertPointFromScreen(leftTop, comp);
        
        return new Rectangle(leftTop, screenRect.getSize());
    }
    
    private String convertSide(String origSide) {
        if (Constants.TOP.equals(origSide)) {
            return BorderLayout.NORTH;
        } else if (Constants.LEFT.equals(origSide)) {
            return BorderLayout.WEST;
        } else if (Constants.RIGHT.equals(origSide)) {
            return BorderLayout.EAST;
        }
        return BorderLayout.SOUTH;
    }
    
    
    /** Updates slide operation by setting correct finish bounds of component 
     * which will component have after slide in. It should cover whole one side
     * of desktop, but overlap editor area only if necessary.
     */
    private Rectangle computeSlideInBounds(SlideOperation operation, Rectangle editorBounds) {
        Point editorLeftTop = editorBounds.getLocation();
        SwingUtilities.convertPointFromScreen(editorLeftTop, layeredPane);
        editorBounds = new Rectangle(editorLeftTop, editorBounds.getSize());
        String side = operation.getSide();
        SlidingView view = findView(side);
        Rectangle splitRootRect = viewComponent.getBounds();
        Rectangle result = new Rectangle();
        Rectangle viewRect = view.getComponent().getBounds();
        
        if (Constants.LEFT.equals(side)) {
            result.x = viewRect.x + viewRect.width;
            result.y = 0;
            result.height = splitRootRect.height;
            result.width = editorBounds.x - result.x + 6;
            if (result.width < MIN_EDITOR_ALIGN_THICK) {
                result.width = splitRootRect.width / 3;
            }
        } else if (Constants.RIGHT.equals(side)) {
            result.x = (viewRect.x - (editorBounds.x + editorBounds.width) < MIN_EDITOR_ALIGN_THICK)
                        ? viewRect.x - splitRootRect.width / 3 : editorBounds.x + editorBounds.width - 6;
            result.y = 0;
            result.height = splitRootRect.height;
            result.width = viewRect.x - result.x;
            
        } else if (Constants.BOTTOM.equals(side)) {
            result.x = splitRootRect.x;
            result.y = (viewRect.y - (editorBounds.y + editorBounds.height) < MIN_EDITOR_ALIGN_THICK)
                        ? viewRect.y - splitRootRect.height / 3 : editorBounds.y + editorBounds.height - 6;
            result.height = viewRect.y - result.y;
            result.width = splitRootRect.width;
        }
        
        return result;
    }
    
    private Rectangle computeThinBounds (SlideOperation operation, Rectangle slideInFinish) {
        String side = operation.getSide();
        Rectangle result = new Rectangle();
        
        if (Constants.LEFT.equals(side)) {
            result.x = slideInFinish.x;
            result.y = slideInFinish.y;
            result.height = slideInFinish.height;
            result.width = 0;
        } else if (Constants.RIGHT.equals(side)) {
            result.x = slideInFinish.x + slideInFinish.width;
            result.y = slideInFinish.y;
            result.height = slideInFinish.height;
            result.width = 0;
        } else if (Constants.BOTTOM.equals(side)) {
            result.x = slideInFinish.x;
            result.y = slideInFinish.y + slideInFinish.height;
            result.height = 0;
            result.width = slideInFinish.width;
        }
        
        return result;
    }
    
    /** Returns bounds of last button in sliding view to which given
     * operation belongs. Bounds are relative to desktop layered pane.
     */
    private Rectangle computeLastButtonBounds(SlideOperation operation) {
        String side = operation.getSide();
        SlidingView view = findView(side);
        Rectangle screenRect = view.getTabBounds(view.getTopComponents().size() - 1);
        Point leftTop = screenRect.getLocation();
        
        if (Constants.BOTTOM.equals(side)) {
            leftTop.y += desktop.getHeight() - view.getComponent().getPreferredSize().height;
        } else if (Constants.RIGHT.equals(side)) {
            leftTop.x += desktop.getWidth() - view.getComponent().getPreferredSize().width;
        }
        
        return new Rectangle(leftTop, screenRect.getSize());
    }
    
    private SlidingView findView (String side) {
        SlidingView view;
        for (Iterator iter = getSlidingViews().iterator(); iter.hasNext(); ) {
            view = (SlidingView)iter.next();
            if (side.equals(view.getSide())) {
                return view;
            }
        }
        return null;
    }
    
    private Set getSlidingViews() {
        if (slidingViews == null) {
            slidingViews = new HashSet(5);
        }
        return slidingViews;
    }
    
    
    /** Special layout manager for layered pane, just keeps desktop panel
     * coreving whole layered pane and if sliding is in progress, it keeps
     * slided component along right edge.
     */
    private final class LayeredLayout implements LayoutManager {
        
        public void layoutContainer(Container parent) {
            Dimension size = parent.getSize();
            desktop.setBounds(0, 0, size.width, size.height);
            // keep right bounds of slide in progress 
            if ((curSlideIn != null) && curSlideIn.getComponent().isVisible()) {
                String side = curSlideIn.getSide();
                SlidingView curView = findView(side);
                // #43865 - sliding wiew could be removed by closing
                if (curView != null) {
                    Component slidedComp = curSlideIn.getComponent();
                    Rectangle result = slidedComp.getBounds();
                    Rectangle viewRect = curView.getComponent().getBounds();
                    Rectangle splitRootRect = viewComponent.getBounds();

                    if (Constants.LEFT.equals(side)) {
                        result.height = splitRootRect.height;
                    } else if (Constants.RIGHT.equals(side)) {
                        result.x = size.width - viewRect.width - result.width;
                        result.height = splitRootRect.height;
                    } else if (Constants.BOTTOM.equals(side)) {
                        result.y = size.height - viewRect.height - result.height;
                        result.width = splitRootRect.width;
                    }

                    slidedComp.setBounds(result);
                }
            }
        }
        
        public Dimension minimumLayoutSize(Container parent) {
            return desktop.getMinimumSize();
        }
        
        public Dimension preferredLayoutSize(Container parent) {
            return desktop.getPreferredSize();
        }
        
        public void addLayoutComponent(String name, Component comp) {
            // no op, slided components are added/removed via SlideOperation.run() calls.
        }
        
        public void removeLayoutComponent(Component comp) {
            // no op, slided components are added/removed via SlideOperation.run() calls.
        }
        
    } // end of LayeredLayout
    
}
