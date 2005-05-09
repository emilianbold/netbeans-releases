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


package org.netbeans.core.windows.view.dnd;


import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.Debug;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.*;
import org.netbeans.core.windows.view.ui.MainWindow;
import org.netbeans.core.windows.view.ui.ModeComponent;
import org.openide.ErrorManager;
import org.openide.util.WeakSet;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;


/**
 * Manager for window DnD. Manages notifying all opened
 * <code>ModeContainer</code>'s to be notified about starting
 * and finishe window drag operation.
 *
 *
 * @author  Peter Zavadsky
 *
 * @see TopComponentDragSupport
 * @see DropTargetGlassPane
 */
public final class WindowDnDManager
implements DropTargetGlassPane.Observer, DropTargetGlassPane.Informer {

    
    private final TopComponentDragSupport topComponentDragSupport = new TopComponentDragSupport(this);

    /** Only instance of drag source used for window system DnD. */
    private DragSource windowDragSource;

    /** Flag indicating the drag operation is in progress. */
    private boolean dragging;
    
    // XXX #21917. The flag is not correct in jdk dnd framework,
    // this field is used to workaround the problem. Be aware is 
    // valisd only for DnD of window component.
    /** Flag keeping info about last drop operation. */
    private boolean dropSuccess;

    /** Maps root panes to original glass panes. */
    private final Map root2glass = new HashMap();
    
    /** Set of floating frame types, i.e. separate windows. */
    private final Set floatingFrames = new WeakSet(4);
    
    /** Used to hack the last Drop target to clear its indication. */ 
    private Reference lastTargetWRef = new WeakReference(null);

    /** Accesses view. */
    private final ViewAccessor viewAccessor;
    
    // Helpers
    private TopComponentDroppable startingDroppable;
    private Point startingPoint;
    // XXX Normal way it should be possible to retrieve from DnD events.
    private TopComponent startingTransfer;
    
    
    /** Keeps ref to fake center panel droppable. */
    private static WeakReference centerDropWRef = new WeakReference(null);
    
    /** Debugging flag. */
    private static final boolean DEBUG = Debug.isLoggable(WindowDnDManager.class);
    
    
    /** Creates a new instance of <code>WindowsDnDManager</code>. */
    public WindowDnDManager(ViewAccessor viewAccessor) {
        this.viewAccessor = viewAccessor;
        
        // PENDING Be aware it is added only once.
        Toolkit.getDefaultToolkit().addAWTEventListener(
            topComponentDragSupport,
             AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
        );
    }
    

    
    /** Indicates whether the window drag and drop is enabled. */
    public static boolean isDnDEnabled() {
        return !Constants.SWITCH_DND_DISABLE;
    }

    /** Gets the only current instance of <code>DragSource</code> used in 
     * window system DnD. */
    public synchronized DragSource getWindowDragSource() {
        if(windowDragSource == null) {
            windowDragSource = new DragSource();
            windowDragSource.addDragSourceMotionListener(
                new MotionListener(this, topComponentDragSupport));
        }
        
        return windowDragSource;
    }

    /** Indicates whether the drag is in progress or not. */
    public boolean isDragging() {
        return dragging;
    } 
    
    /** Sets the <code>dropSuccess</code> flag. */
    public void setDropSuccess(boolean dropSuccess) {
        this.dropSuccess = dropSuccess;
    }
    
    /** Indicates whether the last drop operation was successful. */
    public boolean isDropSuccess() {
        return dropSuccess;
    }
    
    /**Sets the last drop target compoent over which hovered the mouse.
     * Hacking purpose only. */
    public void setLastDropTarget(DropTargetGlassPane target) {
        if(target != lastTargetWRef.get()) {
            lastTargetWRef = new WeakReference(target);
        }
    }
    
    // XXX try out to recover the DnD,
    // currently impossible no API see #21791, no such API.
    /** Tries to reset window DnD system in case some DnD problem occured. */
    public void resetDragSource() {
        dragFinished();
    }
    
    public TopComponentDroppable getStartingDroppable() {
        return startingDroppable;
    }
    
    public Point getStartingPoint() {
        return startingPoint;
    }
    
    public TopComponent getStartingTransfer() {
        return startingTransfer;
    }
    
    /** Called when there is pending drag operation to be started.
     * Informs all currently opened <code>ModeContainer</code>'s implementing
     * <code>ModeContainer.DropInidicator</code> interface about
     * starting drag operation. */
    public void dragStarting(TopComponentDroppable startingDroppable, Point startingPoint,
    TopComponent startingTransfer) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragStarting"); // NOI18N
        }
        
        // PENDING
        this.startingDroppable = startingDroppable;
        this.startingPoint = startingPoint;
        // XXX
        this.startingTransfer = startingTransfer;
        
        Map addedRoots = new HashMap();
        Set addedFrames = new HashSet();

        for(Iterator it = viewAccessor.getModeComponents().iterator(); it.hasNext(); ) {
            Component comp = (Component)it.next();
            if(comp instanceof TopComponentDroppable) {
                // Find root pane.
                JRootPane root = null;
                if(comp instanceof RootPaneContainer) {
                    root = ((RootPaneContainer)comp).getRootPane();
                } else {
                    RootPaneContainer rootContainer = (RootPaneContainer)SwingUtilities
                            .getAncestorOfClass(RootPaneContainer.class, comp);
                    if(rootContainer != null) {
                        root = rootContainer.getRootPane();
                    }
                }
                
                if(root != null) {
                    Component originalGlass = setDropTargetGlassPane(root, this);
                    if(originalGlass != null) {
                        addedRoots.put(root, originalGlass);
                    }
                }
            }
        }
        for(Iterator it = viewAccessor.getSeparateModeFrames().iterator(); it.hasNext(); ) {
            Frame frame = (Frame)it.next();
            if(frame != null) {
                addedFrames.add(frame);
            }
        }
        
        if(!addedRoots.isEmpty()) {
            synchronized(root2glass) {
                root2glass.putAll(addedRoots);
            }
        }
        
        if(!addedFrames.isEmpty()) {
            synchronized(floatingFrames) {
                floatingFrames.addAll(addedFrames);
            }
        }
        
        dragging = true;
        dropSuccess = false;
    }

    
    /** Sets <code>DropTargetGlassPane<code> for specified JRootPane.
     * @return original glass pane or null if there was already set drop target
     *      glass pane */
    private static Component setDropTargetGlassPane(
    JRootPane rootPane, WindowDnDManager windowDnDManager) {
        Component glassPane = rootPane.getGlassPane();
        if(glassPane instanceof DropTargetGlassPane) {
            // There is already our glass pane.
            return null;
        }
        
        DropTargetGlassPane dropGlass = new DropTargetGlassPane(windowDnDManager);
        // Associate with new drop target, and initialize.
        new DropTarget(
            dropGlass,
            DnDConstants.ACTION_COPY_OR_MOVE,
            dropGlass
        );
 
        rootPane.setGlassPane(dropGlass);
        // !!! Necessary to initialize it after setGlassPane(..),
        // i.e. the visibility state.
        dropGlass.initialize();
        
        return glassPane;
    }
    
    /** Called when there finished drag operation.
     * Informs all turned on <code>TopComponentDroppable</code>'s
     * about fininshed drag and drop. */
    public void dragFinished() {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragFinished"); // NOI18N
        }

        // PENDING
        startingDroppable = null;
        startingPoint = null;
        startingTransfer = null;
        
        // Inform the drag support instance about finishing of the DnD.
        topComponentDragSupport.dragFinished();

        dragging = false;

        Map removedRoots;
        synchronized(root2glass) {
            removedRoots = new HashMap(root2glass);
            root2glass.clear();
        }

        for(Iterator it = removedRoots.keySet().iterator(); it.hasNext(); ) {
            JRootPane root = (JRootPane)it.next();
            setOriginalGlassPane(root, (Component)removedRoots.get(root));
        }
    }
    
    /** Sets orgiginal glass pane to specified root pane. */
    private static void setOriginalGlassPane(
    JRootPane rootPane, Component originalGlass) {
        Component glass = rootPane.getGlassPane();
        
        if(glass instanceof DropTargetGlassPane) {
            DropTargetGlassPane dropGlass = (DropTargetGlassPane)glass;
            
            // Release the drop target, and unititialize.
            dropGlass.setDropTarget(null);
            dropGlass.uninitialize();
        }

        if(originalGlass != null) {
            rootPane.setGlassPane(originalGlass);
        }
        
        // #22962. Not selected JInternalFrame needs to have its glass pane
        // switched on to work properly, ensure it is so.
        JInternalFrame internalFrame = (JInternalFrame)SwingUtilities.
            getAncestorOfClass(JInternalFrame.class, originalGlass);
        if(internalFrame != null && !internalFrame.isSelected()
        && !originalGlass.isVisible()) {
            originalGlass.setVisible(true);
        }
    }
    
    // PENDING Better design of this issue, it needs to be performed
    // later than dragFinished.
    /** Maked post drag finished cleaning. Clears references to separated
     * modes */
    public void dragFinishedEx() {
        synchronized(floatingFrames) {
            floatingFrames.clear();
        }
    }    

    
    /** Gets set of floating frames. */
    public Set getFloatingFrames() {
        synchronized(floatingFrames) {
            return new HashSet(floatingFrames);
        }
    }
    
    /** Checks whether the point is inside separated (floating) frame
     * droppable area. The point is relative to screen. */
    public boolean isInFloatingFrame(Point location) {
        for(Iterator it = getFloatingFrames().iterator(); it.hasNext(); ) {
            Frame frame = (Frame)it.next();
            if(frame.getBounds().contains(location)) {
                return true;
            }
        }
        
        return false;
    }


    // XXX
    public boolean isCopyOperationPossible() {
        return topComponentDragSupport.isCopyOperationPossible();
    }
    
    public Controller getController() {
        return viewAccessor.getController();
    }
    
    private static void debugLog(String message) {
        Debug.log(WindowDnDManager.class, message);
    }

    // Helpers>>
    /** Checks whether the point is inside main window droppable area. 
     * The point is relative to screen. */
    static boolean isInMainWindow(Point location) {
        return WindowManagerImpl.getInstance().getMainWindow().getBounds().contains(location);
    } 
    
    /** Indicates whether there is a droppable in main window, specified
     * by screen location. */
    private boolean isInMainWindowDroppable(Point location, int kind, TopComponent transfer) {
        return findMainWindowDroppable(location, kind, transfer) != null;
    }
    
    /** Checks whetner the point is inside one of floating window,
     * i.e. separated modes, droppable area. The point is relative to screen. */
    private static boolean isInFloatingFrameDroppable(Set floatingFrames, Point location, int kind, TopComponent transfer) {
        return findFloatingFrameDroppable(floatingFrames, location, kind, transfer) != null;
    }
    
    private static boolean isInFreeArea(Point location) {
        Frame[] frames = Frame.getFrames();
        for(int i = 0; i < frames.length; i++) {
            //#40782 fix. don't take the invisible frames into account when deciding what is 
            // free space.
            if(frames[i].isVisible() && frames[i].getBounds().contains(location.x, location.y)) {
                return false;
            }
        }
        
        return true;
    }

    /** Finds <code>TopComponentDroppable</code> from specified screen location. */
    private TopComponentDroppable findDroppableFromScreen(
    Set floatingFrames, Point location, int kind, TopComponent transfer) {
        TopComponentDroppable droppable = findMainWindowDroppable(location, kind, transfer);
        if(droppable != null) {
            return droppable;
        }
        
        droppable = findFloatingFrameDroppable(floatingFrames, location, kind, transfer);
        if(droppable != null) {
            return droppable;
        }
        
//        // PENDING center panel area. Maybe editor empty area -> revise later.
//        if(isAroundCenterPanel(location)) {
//            return getCenterPanelDroppable();
//        }
        
        if(isInFreeArea(location)) {
            return getFreeAreaDroppable(location);
        }
        return null;
    }

    private CenterSlidingDroppable lastSlideDroppable;
    
    /** Gets droppable from main window, specified by screen location.
     * Helper method. */
    private TopComponentDroppable findMainWindowDroppable(
    Point location, int kind, TopComponent transfer) {
        
        MainWindow mainWindow = (MainWindow)WindowManagerImpl.getInstance().getMainWindow();
        Point p = new Point(location);
        SwingUtilities.convertPointFromScreen(p, mainWindow.getContentPane());
        if (lastSlideDroppable != null) {
            if (lastSlideDroppable.isWithinSlide(p)) {
                return lastSlideDroppable;
            }
        }
        TopComponentDroppable droppable = findSlideDroppable(viewAccessor.getSlidingModeComponent(Constants.LEFT));
        if (droppable != null) {
            CenterSlidingDroppable drop = new CenterSlidingDroppable(viewAccessor, droppable, Constants.LEFT);
            if (drop.isWithinSlide(p)) {
                lastSlideDroppable = drop;
                return drop;
            }
        }
        droppable = findSlideDroppable(viewAccessor.getSlidingModeComponent(Constants.RIGHT));
        if (droppable != null) {
            CenterSlidingDroppable drop = new CenterSlidingDroppable(viewAccessor, droppable, Constants.RIGHT);
            if (drop.isWithinSlide(p)) {
                lastSlideDroppable = drop;
                return drop;
            }
        }
        droppable = findSlideDroppable(viewAccessor.getSlidingModeComponent(Constants.BOTTOM));
        if (droppable != null) {
            CenterSlidingDroppable drop = new CenterSlidingDroppable(viewAccessor, droppable, Constants.BOTTOM);
            if (drop.isWithinSlide(p)) {
                lastSlideDroppable = drop;
                return drop;
            }
        }
        lastSlideDroppable = null;
        if (isNearEdge(location, viewAccessor)) {
            return getCenterPanelDroppable();
        }
        Point mainP = new Point(location);
        SwingUtilities.convertPointFromScreen(mainP, mainWindow);
        return findDroppable(mainWindow, mainP, kind, transfer);
    }
    
    private static TopComponentDroppable findSlideDroppable(Component comp) {
        TopComponentDroppable droppable = null;
        if(comp instanceof TopComponentDroppable) {
            droppable = (TopComponentDroppable)comp;
        } else {
            droppable = (TopComponentDroppable)SwingUtilities.getAncestorOfClass(TopComponentDroppable.class, comp);
        }
        return droppable;
    }

    /** Gets droppable from separated (floating) window, specified
     * by screen location. Helper method. */
    private static TopComponentDroppable findFloatingFrameDroppable(
    Set floatingFrames, Point location, int kind, TopComponent transfer) {
        for(Iterator it = floatingFrames.iterator(); it.hasNext(); ) {
            Component comp = (Component)it.next();
            Rectangle bounds = comp.getBounds();
            
            if(bounds.contains(location)) {
                TopComponentDroppable droppable = findDroppable(comp,
                        new Point(location.x - bounds.x, location.y - bounds.y),
                        kind,
                        transfer);
                if(droppable != null) {
                    return droppable;
                }
            }
        }
        
        return null;
    }
    
    /** Finds <code>TopComponentDroppable</code> for the location in component.
     * The location has to be relative to the specified component. Then the
     * method finds if there is a droppable component in the hierarchy, which
     * also contains the specified location.
     * Utilitity method. */
    private static TopComponentDroppable findDroppable(Component comp,
                         Point location, int kind, TopComponent transfer) {
        JFrame frame;
        if(comp instanceof JFrame) {
            frame = (JFrame)comp;
        } else {
            Window w = SwingUtilities.getWindowAncestor(comp);
            if(w instanceof JFrame) {
                frame = (JFrame)w;
            } else {
                return null;
            }
        }

        Component contentPane = frame.getContentPane();
        location = SwingUtilities.convertPoint(comp, location, contentPane);
        Component deepest = SwingUtilities.getDeepestComponentAt(
                contentPane, location.x, location.y);
        if(deepest instanceof TopComponentDroppable) {
            TopComponentDroppable droppable = (TopComponentDroppable)deepest;
            if(droppable.supportsKind(kind, transfer)) {
                return droppable;
            }
        }
        
        while(deepest != null) {
            TopComponentDroppable nextDroppable = (TopComponentDroppable)SwingUtilities.getAncestorOfClass(
                    TopComponentDroppable.class, deepest);
            if(nextDroppable != null && nextDroppable.supportsKind(kind, transfer)) {
                return nextDroppable;
            }
            deepest = (Component)nextDroppable;
        }
        return null;
    }
    
    /** Indicates whether the cursor is around center panel of main window.
     * In that case is needed also to provide a drop. */
    static boolean isAroundCenterPanel(Point location) {
        Component desktop = ((MainWindow)WindowManagerImpl.getInstance().getMainWindow()).getDesktop();
        if(desktop == null) {
            return false;
        }
        
        Point p = new Point(location);
        SwingUtilities.convertPointFromScreen(p, desktop.getParent());
        Rectangle centerBounds = desktop.getBounds();

        if(!centerBounds.contains(p)) {
            centerBounds.grow(Constants.DROP_AREA_SIZE, Constants.DROP_AREA_SIZE);
            if(centerBounds.contains(p)) {
                return true;
            }
        }
        return false;
    }
    
    /** Indicates whether the cursor is around center panel of main window.
     * In that case is needed also to provide a drop. */
    static boolean isNearEdge(Point location, ViewAccessor viewAccessor) {
        Component desktop = ((MainWindow)WindowManagerImpl.getInstance().getMainWindow()).getDesktop();
        if(desktop == null) {
            return false;
        }
        Point p = new Point(location);
        SwingUtilities.convertPointFromScreen(p, desktop);
        Rectangle centerBounds = desktop.getBounds();
        Rectangle shrinked = desktop.getBounds();
        shrinked.grow(-10,-10);
        shrinked.y = shrinked.y + 10;
        Component dr = viewAccessor.getSlidingModeComponent(Constants.LEFT);
        if (dr != null) {
            shrinked.x = shrinked.x + dr.getBounds().width;
            shrinked.width = shrinked.width - dr.getBounds().width;
        }
        dr = viewAccessor.getSlidingModeComponent(Constants.RIGHT);
        if (dr != null) {
            shrinked.width = shrinked.width - dr.getBounds().width;
        }
        dr = viewAccessor.getSlidingModeComponent(Constants.BOTTOM);
        if (dr != null) {
            shrinked.height = shrinked.height - dr.getBounds().height;
        }
        boolean cont =  centerBounds.contains(p) && !shrinked.contains(p);
        
        return cont;
    }    
    
    /** Creates fake droppable for center panel. */
    private TopComponentDroppable getCenterPanelDroppable() {
        CenterPanelDroppable droppable
                = (CenterPanelDroppable)centerDropWRef.get();
        
        if(droppable == null) {
            droppable = new CenterPanelDroppable();
            centerDropWRef = new WeakReference(droppable);
        }
        
        return droppable;
    }
    
    private static TopComponentDroppable getFreeAreaDroppable(Point location) {
        return new FreeAreaDroppable(location);
    }
    
    /** 
     * Tries to perform actual drop.
     * @param location screen location */
    boolean tryPerformDrop(Controller controller, Set floatingFrames,
    Point location, int dropAction, Transferable transferable) {
        TopComponent[] tcArray = extractTopComponent(
            dropAction == DnDConstants.ACTION_COPY,
            transferable
        );
        
        if(tcArray == null || tcArray.length == 0) {
            return false;
        }
        
        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(tcArray[0]);
        int kind = mode != null ? mode.getKind() : Constants.MODE_KIND_EDITOR;
        
        TopComponentDroppable droppable
                = findDroppableFromScreen(floatingFrames, location, kind, tcArray[0]);
        if(droppable == null) {
            return false;
        }
        
        Component dropComponent = droppable.getDropComponent();
        if(dropComponent != null) {
            SwingUtilities.convertPointFromScreen(location, dropComponent);
        }
        return performDrop(controller, droppable, dropAction, tcArray, location);
    }
    
    /** Extracts <code>TopComponent</code> instance from
     * <code>Transferable</code> according the <code>dropAction</code>.
     * Utility method. */
    static TopComponent[] extractTopComponent(boolean clone,
    Transferable tr) {
        DataFlavor df = getDataFlavorForDropAction(clone);
        
        if(df == null) {
            // No data flavor -> unsupported drop action.
            return null;
        }

        // Test whether the requested dataflavor is supported by transferable.
        if(tr.isDataFlavorSupported(df)) {
            try {
                TopComponent tc; 

                if(clone) {
                    TopComponent.Cloneable ctc = (TopComponent.Cloneable)tr
                        .getTransferData(df);

                    // "Copy" the top component.
                    tc = ctc.cloneComponent();
                } else {
                    tc = (TopComponent)tr.getTransferData(df);
                }

                return new TopComponent[] {tc};
            } catch(UnsupportedFlavorException ufe) {
                ErrorManager.getDefault().notify(
                        ErrorManager.INFORMATIONAL, ufe);
            } catch(IOException ioe) {
                ErrorManager.getDefault().notify(
                        ErrorManager.INFORMATIONAL, ioe);
            }
        }
    
        df = new DataFlavor(TopComponentDragSupport.MIME_TOP_COMPONENT_ARRAY, null);
        if(tr.isDataFlavorSupported(df)) {
            try {
                return (TopComponent[])tr.getTransferData(df);
            } catch(UnsupportedFlavorException ufe) {
                ErrorManager.getDefault().notify(
                        ErrorManager.INFORMATIONAL, ufe);
            } catch(IOException ioe) {
                ErrorManager.getDefault().notify(
                        ErrorManager.INFORMATIONAL, ioe);
            }
        }
        
        return null;
    }
    
    /** Gets <code>DataFlavor</code> for specific drop action type.
     * Helper utility method. */
    private static DataFlavor getDataFlavorForDropAction(boolean clone) {
        // Create needed dataflavor.
        DataFlavor df;
        if(clone) {
            df = new DataFlavor(TopComponentDragSupport.MIME_TOP_COMPONENT_CLONEABLE, null);
        } else {
            df = new DataFlavor(TopComponentDragSupport.MIME_TOP_COMPONENT, null);
        }
        
        return df;
    }    
    
    /**
     * Performs actual drop operation. Called from DropTargetListener.
     * @return <code>true</code> if the drop was successful */
    private static boolean performDrop(Controller controller,
    TopComponentDroppable droppable, int dropAction, TopComponent[] tcArray, Point location) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("performDrop"); // NOI18N
            debugLog("droppable=" + droppable); // NOI18N
        }

        if(tcArray == null || tcArray.length == 0) {
            return true;
        }
        
        if(!droppable.canDrop(tcArray[0], location)) {
            return true;
        }
        
        ViewElement viewElement = droppable.getDropViewElement();
        Object constr = droppable.getConstraintForLocation(location);

        if(viewElement instanceof EditorView)  {
            ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(tcArray[0]);
            int kind = mode != null ? mode.getKind() : Constants.MODE_KIND_EDITOR;
            if(kind == Constants.MODE_KIND_EDITOR) {
                controller.userDroppedTopComponentsIntoEmptyEditor(tcArray);
            } else {
                if(constr == Constants.TOP
                || constr == Constants.LEFT
                || constr == Constants.RIGHT
                || constr == Constants.BOTTOM) {
                    controller.userDroppedTopComponentsAroundEditor(tcArray, (String)constr);
                } else if(Constants.SWITCH_MODE_ADD_NO_RESTRICT
                || WindowManagerImpl.getInstance().isTopComponentAllowedToMoveAnywhere(tcArray[0])) {
                    controller.userDroppedTopComponentsIntoEmptyEditor(tcArray);
                }
            }
        } else if(viewElement instanceof ModeView) {
            ModeView modeView = (ModeView)viewElement;
            if(constr == Constants.TOP
            || constr == Constants.LEFT
            || constr == Constants.RIGHT
            || constr == Constants.BOTTOM) {
                controller.userDroppedTopComponents(modeView, tcArray, (String)constr);
            } else if(constr instanceof Integer) {
                controller.userDroppedTopComponents(modeView, tcArray, ((Integer)constr).intValue());
            } else {
                controller.userDroppedTopComponents(modeView, tcArray);
            }
        } else if(viewElement == null) { // XXX around area or free area
            if(constr == Constants.TOP
            || constr == Constants.LEFT
            || constr == Constants.RIGHT
            || constr == Constants.BOTTOM) { // XXX around area
                controller.userDroppedTopComponentsAround(tcArray, (String)constr);
            } else if(constr instanceof Rectangle) { // XXX free area
                Rectangle bounds = (Rectangle)constr;
                // #38657 Refine bounds.
                Component modeComp = SwingUtilities.getAncestorOfClass(ModeComponent.class, tcArray[0]);
                if(modeComp != null) {
                    bounds.setSize(modeComp.getWidth(), modeComp.getHeight());
                }
                
                controller.userDroppedTopComponentsIntoFreeArea(tcArray, bounds);
            }
        }

        return true;
    }
    // Helpers<<
    
    
    /** Jdk1.4 <code>DragSourceMotionListener</code>
     * change the code when starting to build on jdk1.4 and higher only */
    private static class MotionListener implements DragSourceMotionListener {

        private final WindowDnDManager windowDnDManager;
        private final TopComponentDragSupport topComponentDragSupport;
        
        /** Constrtucts the instance.
         * Adds the listener to the window dnd <code>DragSource</code>. */
        private MotionListener(WindowDnDManager windowDnDManager,
        TopComponentDragSupport topComponentDragSupport) {
            this.windowDnDManager = windowDnDManager;
            this.topComponentDragSupport = topComponentDragSupport;
        }
        

        /** Implements <code>DragSourceMotionListener</code>. */
        public void dragMouseMoved(DragSourceDragEvent evt) {
            if(DEBUG) {
                debugLog("dragMouseMoved evt=" + evt); // NOI18N
            }
            
            Point location = evt.getLocation();
            if(location == null) {
                return;
            }

            ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(windowDnDManager.startingTransfer);
            int kind = mode != null ? mode.getKind() : Constants.MODE_KIND_EDITOR;
            
            boolean isInMainDroppable
                    = windowDnDManager.isInMainWindowDroppable(location, kind, windowDnDManager.startingTransfer);
            boolean isInFrameDroppable
                    = isInFloatingFrameDroppable(windowDnDManager.getFloatingFrames(), location, kind, windowDnDManager.startingTransfer);
            boolean isAroundCenterPanel
                    = isAroundCenterPanel(location);

            if(isInMainDroppable || isInFrameDroppable || isAroundCenterPanel) {
                TopComponentDroppable droppable 
                        = windowDnDManager.findDroppableFromScreen(windowDnDManager.getFloatingFrames(), location, kind, windowDnDManager.startingTransfer);
                //hack - can't get the bounds correctly, sometimes freearedroppable gets here..
                
                if (droppable instanceof FreeAreaDroppable) {
                    if(WindowManagerImpl.getInstance().getEditorAreaState() == Constants.EDITOR_AREA_SEPARATED
                        && droppable.canDrop(windowDnDManager.startingTransfer, location)) {
                        topComponentDragSupport.setSuccessCursor();
                    } else {
                        topComponentDragSupport.setUnsuccessCursor();
                    }                    
                    // for the status bar it's null somehow, workarounding by checking for null.. should go away..
                } else if (droppable != null) {
                    
                    // was probably forgotten to set the lastdrop target, was causing strange repaint side effects when 2 frames overlapped.
                    JComponent cp = (JComponent)droppable.getDropComponent();
                    Component glass = cp.getRootPane().getGlassPane();
                    if (glass instanceof DropTargetGlassPane) {
                        windowDnDManager.setLastDropTarget((DropTargetGlassPane)glass);
                    }
                    Point p = new Point(location);
                    SwingUtilities.convertPointFromScreen(p, droppable.getDropComponent());
                    if(droppable.canDrop(windowDnDManager.startingTransfer, p)) {
                        topComponentDragSupport.setSuccessCursor();
                    } else {
                        topComponentDragSupport.setUnsuccessCursor();
                    }
                    dragOverDropTarget(location, droppable);
                }
            } else if(!isInMainWindow(location)
            && windowDnDManager.isInFloatingFrame(location)) {
                // Simulates success drop in free area.
                topComponentDragSupport.setSuccessCursor();
            } else if(isInFreeArea(location) && WindowManagerImpl.getInstance().getEditorAreaState() == Constants.EDITOR_AREA_SEPARATED
            && getFreeAreaDroppable(location).canDrop(windowDnDManager.startingTransfer, location)) {
                topComponentDragSupport.setSuccessCursor();
            } else {
                topComponentDragSupport.setUnsuccessCursor();
            }
            
            if(!isInMainDroppable && !isInFrameDroppable && !isAroundCenterPanel) {
                clearExitedDropTarget();
            }
        }
        
        /** Simulates dropOver event, for glass pane to indicate possible drop
         * operation. */
        private /*static*/ void dragOverDropTarget(Point location,
        TopComponentDroppable droppable) {
            DropTargetGlassPane lastTarget
                = (DropTargetGlassPane)windowDnDManager.lastTargetWRef.get();

            if(lastTarget != null) {
                Point p = new Point(location);
                SwingUtilities.convertPointFromScreen(p, lastTarget);
                lastTarget.dragOver(p, droppable);
            }
        }

        /** Hacks drag exit from drop target (glass pane).
         * Eliminates bug, where remained drop indicator drawed
         * even for cases the cursor was away from drop target. Missing
         * drag exit event. */
        private /*static*/ void clearExitedDropTarget() {
            DropTargetGlassPane lastTarget
                = (DropTargetGlassPane)windowDnDManager.lastTargetWRef.get();

            if(lastTarget != null) {
                lastTarget.clearIndications();
                windowDnDManager.lastTargetWRef = new WeakReference(null);
            }
        }
        
        /** Gets main drop target glass pane.*/
        private static DropTargetGlassPane getMainDropTargetGlassPane() {
            Component glass = ((JFrame)WindowManagerImpl.getInstance().getMainWindow()).getGlassPane();
            if(glass instanceof DropTargetGlassPane) {
                return (DropTargetGlassPane)glass;
            } else {
                return null;
            }
        }

    } // End of class MotionListener.
    

    // XXX
    /** Interface for accessing   */
    public interface ViewAccessor {
        public Set getModeComponents();
        public Set getSeparateModeFrames();
        public Controller getController();
        public Component getSlidingModeComponent(String side);
    } // End of ViewState.
    
    /** Fake helper droppable used when used around  */
    private class CenterPanelDroppable implements TopComponentDroppable {

        /** Implements <code>TopComponentDroppable</code>. */
        public java.awt.Shape getIndicationForLocation(Point p) {
            Rectangle bounds = getDropComponent().getBounds();
            Rectangle res = null;
            double ratio = Constants.DROP_AROUND_RATIO;
            Object constraint = getConstraintForLocation(p);
            if(constraint == JSplitPane.LEFT) {
                res = new Rectangle(0, 0, (int)(bounds.width * ratio) - 1, bounds.height - 1);
            } else if(constraint == JSplitPane.TOP) {
                res = new Rectangle(0, 0, bounds.width - 1, (int)(bounds.height * ratio) - 1);
            } else if(constraint == JSplitPane.RIGHT) {
                res = new Rectangle(bounds.width - (int)(bounds.width * ratio), 0,
                        (int)(bounds.width * ratio) - 1, bounds.height - 1);
            } else if(constraint == JSplitPane.BOTTOM) {
                res = new Rectangle(0, bounds.height - (int)(bounds.height * ratio), bounds.width - 1,
                        (int)(bounds.height * ratio) - 1);
            }

            return res;
        }

        /** Implements <code>TopComponentDroppable</code>. */
        public Object getConstraintForLocation(Point p) {
            Rectangle bounds = getDropComponent().getBounds();
            Component leftSlide = viewAccessor.getSlidingModeComponent(Constants.LEFT);
            Component rightSlide = viewAccessor.getSlidingModeComponent(Constants.RIGHT);
            Component bottomSlide = viewAccessor.getSlidingModeComponent(Constants.BOTTOM);
                    
            if(p.x <  leftSlide.getBounds().width + 10) {
                return javax.swing.JSplitPane.LEFT;
            } else if(p.y < 0) {
                return javax.swing.JSplitPane.TOP;
            } else if(p.x > bounds.width - 10 - rightSlide.getBounds().width - leftSlide.getBounds().width) {
                return javax.swing.JSplitPane.RIGHT;
            } else if(p.y > bounds.height - 10 - bottomSlide.getBounds().height) {
                return javax.swing.JSplitPane.BOTTOM;
            }

            return null;
        }

        /** Implements <code>TopComponentDroppable</code>. */
        public Component getDropComponent() {
            return ((MainWindow)WindowManagerImpl.getInstance().getMainWindow()).getDesktop();
        }
        
        /** Implements <code>TopComponentDroppable</code>. */
        public ViewElement getDropViewElement() {
            return null;
        }
        
        public boolean canDrop(TopComponent transfer, Point location) {
            if(Constants.SWITCH_MODE_ADD_NO_RESTRICT
            || WindowManagerImpl.getInstance().isTopComponentAllowedToMoveAnywhere(transfer)) {
                return true;
            }

            ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(transfer);
            return mode != null && (mode.getKind() == Constants.MODE_KIND_VIEW || mode.getKind() == Constants.MODE_KIND_SLIDING);
        }
        
        public boolean supportsKind(int kind, TopComponent transfer) {
            if(Constants.SWITCH_MODE_ADD_NO_RESTRICT
            || WindowManagerImpl.getInstance().isTopComponentAllowedToMoveAnywhere(transfer)) {
                return true;
            }

            return kind == Constants.MODE_KIND_VIEW || kind == Constants.MODE_KIND_SLIDING;
        }


    } // End of class CenterPanelDroppable.
    
    
    /** Fake helper droppable used when dropping is done into free area.  */
    private static class FreeAreaDroppable implements TopComponentDroppable {
        
        private Point location;
        
        public FreeAreaDroppable(Point location) {
            this.location = location;
        }
        
        /** Implements <code>TopComponentDroppable</code>. */
        public java.awt.Shape getIndicationForLocation(Point p) {
            return null;
        }
        
        /** Implements <code>TopComponentDroppable</code>. */
        public Object getConstraintForLocation(Point p) {
            return new Rectangle(location.x, location.y,
                Constants.DROP_NEW_MODE_SIZE.width, Constants.DROP_NEW_MODE_SIZE.height);
        }
        
        /** Implements <code>TopComponentDroppable</code>. */
        public Component getDropComponent() {
            return null;
        }
        
        /** Implements <code>TopComponentDroppable</code>. */
        public ViewElement getDropViewElement() {
            return null;
        }
        
        public boolean canDrop(TopComponent transfer, Point location) {
            ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(transfer);
            return mode != null && (mode.getKind() == Constants.MODE_KIND_VIEW || mode.getKind() == Constants.MODE_KIND_SLIDING);
        }
        
        public boolean supportsKind(int kind, TopComponent transfer) {
            if(Constants.SWITCH_MODE_ADD_NO_RESTRICT
            || WindowManagerImpl.getInstance().isTopComponentAllowedToMoveAnywhere(transfer)) {
                return true;
            }
            
            return kind == Constants.MODE_KIND_VIEW || kind == Constants.MODE_KIND_SLIDING;
        }

    } // End of class FreeAreaDroppable.
    
    /**
     * droppable for the sliding bars, both inside and outside of the main window.
     *
     */
    private static class CenterSlidingDroppable implements TopComponentDroppable, EnhancedDragPainter {
        
        private ViewAccessor accesor;
        private TopComponentDroppable original;
        private String side;
        JPanel pan;
        private boolean isShowing;
        
        public CenterSlidingDroppable(ViewAccessor viewAccesor, TopComponentDroppable slidingBarDelegate,
                                      String side) {
            original = slidingBarDelegate;
            accesor = viewAccesor;
            this.side = side;
            pan = new JPanel();
            isShowing = false;
        }
        
        public boolean canDrop(TopComponent transfer, Point location) {
            return original.canDrop(transfer, location);
        }

        public Object getConstraintForLocation(Point location) {
            return original.getConstraintForLocation(location);
        }

        public Component getDropComponent() {
            return original.getDropComponent();
        }

        public ViewElement getDropViewElement() {
            return original.getDropViewElement();
        }

        public Shape getIndicationForLocation(Point location) {
            Shape toReturn = original.getIndicationForLocation(location);
            Rectangle dim = original.getDropComponent().getBounds();
            if (dim.width < 10 || dim.height < 10) {
                Rectangle rect = toReturn.getBounds();
                if (Constants.LEFT.equals(side)) {
                    toReturn = new Rectangle(0, 0, Math.max(rect.width, Constants.DROP_AREA_SIZE), 
                                                   Math.max(rect.height, Constants.DROP_AREA_SIZE));
                } else if (Constants.RIGHT.equals(side)) {
                    toReturn = new Rectangle(- Constants.DROP_AREA_SIZE, 0, Math.max(rect.width, Constants.DROP_AREA_SIZE), 
                                                                           Math.max(rect.height, Constants.DROP_AREA_SIZE));
                } else if (Constants.BOTTOM.equals(side)) {
                    toReturn = new Rectangle(0, - Constants.DROP_AREA_SIZE, Math.max(rect.width, Constants.DROP_AREA_SIZE), 
                                                                           Math.max(rect.height, Constants.DROP_AREA_SIZE));
                }
            }
            return toReturn;
        }
        
        public boolean isWithinSlide(Point location) {
            Component root = SwingUtilities.getRootPane(original.getDropComponent());
            Point barLoc = SwingUtilities.convertPoint(root, location, original.getDropComponent());
            if (original.getDropComponent().contains(barLoc)) {
                return true;
            }
            if (Constants.LEFT.equals(side)) {
                Dimension dim = original.getDropComponent().getSize();
                int abs = Math.abs(barLoc.x);
                if (isShowing && abs < Constants.DROP_AREA_SIZE) {
                    //&& barLoc.y > 0 && barLoc.y < dim.height) {
                    return true;
                }
                if (!isShowing && barLoc.x <= 0 && barLoc.x > - Constants.DROP_AREA_SIZE) {
                    return true;
                }
            }
            else if (Constants.RIGHT.equals(side)) {
                Dimension dim = original.getDropComponent().getSize();
                if (isShowing && ((barLoc.x < 0 && barLoc.x > - Constants.DROP_AREA_SIZE)
                                 || barLoc.x > 0 && barLoc.x - dim.width < Constants.DROP_AREA_SIZE)) {
//                        && barLoc.y > 0 && barLoc.y < dim.height) {
                    return true;
                }
                if (!isShowing && barLoc.x >= 0 && barLoc.x < Constants.DROP_AREA_SIZE + dim.width) {
                    return true;
                }
            } 
            else if (Constants.BOTTOM.equals(side)) {
                Dimension dim = original.getDropComponent().getSize();
//                System.out.println("barloc =" + barLoc);
                if (isShowing && ((barLoc.y < 0 && barLoc.y > - Constants.DROP_AREA_SIZE)
                                 || barLoc.y > 0 && barLoc.y - dim.height < Constants.DROP_AREA_SIZE)) {
//                    System.out.println("bottom showing" + location);
//                } && barLoc.x > 0 && barLoc.x < dim.width) {
                    return true;
                }
                if (!isShowing && barLoc.y >= 0 && barLoc.y < Constants.DROP_AREA_SIZE + dim.height) {
                    return true;
                }
            } 
            return false;
            
        }

        public boolean supportsKind(int kind, TopComponent transfer) {
            return original.supportsKind(kind, transfer);
        }

        public void additionalDragPaint(Graphics2D g) {
            Rectangle dim = original.getDropComponent().getBounds();
            if (dim.width > 10 && dim.height > 10) {
                return;
            }
            isShowing = true;
            Component glassPane = ((JComponent)original.getDropComponent()).getRootPane().getGlassPane();
            Point leftTop = SwingUtilities.convertPoint(original.getDropComponent(), 0, 0, glassPane);
            Point firstDivider;
            Point secondDevider;
                    
            if (Constants.RIGHT.equals(side)) {
                leftTop = new Point(leftTop.x - 24, leftTop.y);
                firstDivider = new Point(leftTop);
                secondDevider = new Point(leftTop.x, leftTop.y + dim.height);
            }
            else if (Constants.BOTTOM.equals(side)) {
                leftTop = new Point(0, leftTop.y - 24);
                firstDivider = new Point(leftTop);
                secondDevider = new Point(leftTop.x + glassPane.getBounds().width, leftTop.y);
            } else {
                firstDivider = new Point(leftTop.x + 25, leftTop.y);
                secondDevider = new Point(leftTop.x + 25, leftTop.y + dim.height);
            }
            Rectangle rect = new Rectangle(leftTop.x, leftTop.y, Math.max(25, dim.width), Math.max(25, dim.height));
            if (Constants.BOTTOM.equals(side)) {
                // for bottom has special hack to use the whole width
                rect.width = glassPane.getBounds().width;
            }
            
            Color col = g.getColor();
            g.setColor(pan.getBackground());
            g.fill(rect);
            g.setColor(pan.getBackground().darker());
            g.drawLine(firstDivider.x, firstDivider.y, secondDevider.x, secondDevider.y);
            g.setColor(col);
        }
        
    }

}

