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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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


package org.netbeans.core.windows.view.dnd;



import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.IOException;
import java.lang.ref.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.netbeans.core.windows.*;
import org.netbeans.core.windows.view.*;
import org.netbeans.core.windows.view.ui.*;
import org.openide.util.WeakSet;
import org.openide.windows.TopComponent;


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
    private final Map<JRootPane,Component> root2glass = new HashMap<JRootPane, Component>();
    
    /** Set of floating frame types, i.e. separate windows. */
    private final Set<Component> floatingFrames = new WeakSet<Component>(4);
    
    /** Used to hack the last Drop target to clear its indication. */ 
    private Reference<DropTargetGlassPane> lastTargetWRef = new WeakReference<DropTargetGlassPane>(null);

    /** Accesses view. */
    private final ViewAccessor viewAccessor;
    
    // Helpers
    private TopComponentDroppable startingDroppable;
    private Point startingPoint;
    // XXX Normal way it should be possible to retrieve from DnD events.
    private TopComponent startingTransfer;
    /** kind of mode into which belonged last dragged TopComponent at that time */
    private int draggedKind;

    /** drag feedback handler, listen to the mouse pointer motion during the drag */
    private MotionListener motionListener;

    /** Keeps ref to fake center panel droppable. */
    private static Reference<CenterPanelDroppable> centerDropWRef = 
            new WeakReference<CenterPanelDroppable>(null);

    /** Keeps ref to fake editor area droppable. */
    private static Reference<EditorAreaDroppable> editorDropWRef = 
            new WeakReference<EditorAreaDroppable>(null);
    
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
        return !Constants.SWITCH_DND_DISABLE && Switches.isTopComponentDragAndDropEnabled();
    }

    /** Gets the only current instance of <code>DragSource</code> used in 
     * window system DnD. */
    public synchronized DragSource getWindowDragSource() {
        if(windowDragSource == null) {
            windowDragSource = new DragSource();
            windowDragSource.addDragSourceMotionListener(getMotionListener());
        }
        return windowDragSource;
    }

    /** Accessor for mouse motion listener */
    MotionListener getMotionListener () {
        if (motionListener == null) {
            motionListener = new MotionListener(this, topComponentDragSupport);
        }
        return motionListener;
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
            lastTargetWRef = new WeakReference<DropTargetGlassPane>(target);
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
        
        this.startingDroppable = startingDroppable;
        this.startingPoint = startingPoint;
        this.startingTransfer = startingTransfer;
        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(startingTransfer);
        this.draggedKind = mode != null ? mode.getKind() : Constants.MODE_KIND_EDITOR;

        // exclude dragged separate view
        /*Window rpc = SwingUtilities.getWindowAncestor(startingTransfer);
        if (rpc != null && !WindowManagerImpl.getInstance().getMainWindow().equals(rpc)) {
            ZOrderManager.getInstance().setExcludeFromOrder((RootPaneContainer)rpc, true);
        }*/
        
        Map<JRootPane,Component> addedRoots = new HashMap<JRootPane, Component>();
        Set<Component> addedFrames = new HashSet<Component>();

        for(Component comp: viewAccessor.getModeComponents()) {
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
        for(Component w: viewAccessor.getSeparateModeFrames()) {
            if(w != null) {
                addedFrames.add(w);
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

        // include dragged separate view back
        /*Window w = SwingUtilities.getWindowAncestor(startingTransfer);
        if (w != null && !WindowManagerImpl.getInstance().getMainWindow().equals(w)) {
            ZOrderManager.getInstance().setExcludeFromOrder((RootPaneContainer)w, false);
        }*/

        // notify motion handler
        getMotionListener().dragFinished();

        // PENDING
        startingDroppable = null;
        startingPoint = null;
        startingTransfer = null;
        
        // Inform the drag support instance about finishing of the DnD.
        topComponentDragSupport.dragFinished();

        dragging = false;

        Map<JRootPane, Component> removedRoots;
        synchronized(root2glass) {
            removedRoots = new HashMap<JRootPane, Component>(root2glass);
            root2glass.clear();
        }

        for(JRootPane root: removedRoots.keySet()) {
            setOriginalGlassPane(root, removedRoots.get(root));
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
    public Set<Component> getFloatingFrames() {
        synchronized(floatingFrames) {
            return new HashSet<Component>(floatingFrames);
        }
    }
    
    /** Checks whether the point is inside separated (floating) frame
     * droppable area. The point is relative to screen. */
    public boolean isInFloatingFrame(Point location) {
        for(Component w: getFloatingFrames()) {
            if(w.getBounds().contains(location)) {
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
    private static boolean isInFloatingFrameDroppable(Set<Component> floatingFrames, Point location, int kind, TopComponent transfer) {
        return findFloatingFrameDroppable(floatingFrames, location, kind, transfer) != null;
    }
    
    /** 
     * Tests if given location is in free area or not.
     * @param location the location to test
     * @param exclude Window to exclude from test (used for fake drag-under effect window)
     * @return true when location point is on free screen, not contained in any 
     * frames or windows of the system, false otherwise
     */
    private static boolean isInFreeArea(Point location, Window exclude) {
        // prepare array of all our windows
        Window mainWindow = WindowManagerImpl.getInstance().getMainWindow();
        Window[] owned = mainWindow.getOwnedWindows();
        Window[] frames = Frame.getFrames();
        Window[] windows = new Window[owned.length + frames.length];
        System.arraycopy(frames, 0, windows, 0, frames.length);
        System.arraycopy(owned, 0, windows, frames.length, owned.length);

        for(int i = 0; i < windows.length; i++) {
            // #114064: exclude fake drar under effect window from the test
            if (windows[i] == exclude) {
                continue;
            }
            //#40782 fix. don't take the invisible frames into account when deciding what is
            // free space.
            if(windows[i].isVisible() && windows[i].getBounds().contains(location.x, location.y)) {
                return false;
            }
        }
        
        return true;
    }

    /** Finds <code>TopComponentDroppable</code> from specified screen location. */
    private TopComponentDroppable findDroppableFromScreen(
    Set<Component> floatingFrames, Point location, int kind, TopComponent transfer) {

        TopComponentDroppable droppable = findMainWindowDroppable(location, kind, transfer);
        if(droppable != null) {
            return droppable;
        }
        
        if( Switches.isTopComponentUndockingEnabled() ) {
            droppable = findFloatingFrameDroppable(floatingFrames, location, kind, transfer);
            if(droppable != null) {
                return droppable;
            } 
        
//        // PENDING center panel area. Maybe editor empty area -> revise later.
//        if(isAroundCenterPanel(location)) {
//            return getCenterPanelDroppable();
//        }
        
            if(isInFreeArea(location, motionListener.fakeWindow)) {
                return getFreeAreaDroppable(location);
            }
        }
        return null;
    }

    private CenterSlidingDroppable lastSlideDroppable;
    
    /** Gets droppable from main window, specified by screen location.
     * Helper method. */
    private TopComponentDroppable findMainWindowDroppable(
    Point location, int kind, TopComponent transfer) {
        
        MainWindow mainWindow = (MainWindow)WindowManagerImpl.getInstance().getMainWindow();

        if (!ZOrderManager.getInstance().isOnTop(mainWindow, location)) {
            return null;
        }

        Point p = new Point(location);
        SwingUtilities.convertPointFromScreen(p, mainWindow.getContentPane());
        if( Switches.isTopComponentSlidingEnabled() ) {
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
        }
        lastSlideDroppable = null;
        if (isNearEditorEdge(location, viewAccessor, kind)) {
            return getEditorAreaDroppable();
        }
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
    Set<Component> floatingFrames, Point location, int kind, TopComponent transfer) {
        for(Component comp: floatingFrames) {
            Rectangle bounds = comp.getBounds();
            
            if(bounds.contains(location) &&
               ZOrderManager.getInstance().isOnTop((RootPaneContainer)comp, location)) {
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
        RootPaneContainer rpc;
        if(comp instanceof RootPaneContainer) {
            rpc = (RootPaneContainer)comp;
        } else {
            Window w = SwingUtilities.getWindowAncestor(comp);
            if(w instanceof RootPaneContainer) {
                rpc = (RootPaneContainer)w;
            } else {
                return null;
            }
        }

        Component contentPane = rpc.getContentPane();
        location = SwingUtilities.convertPoint(comp, location, contentPane);
        Component deepest = SwingUtilities.getDeepestComponentAt(
                contentPane, location.x, location.y);
        
        if( deepest instanceof MultiSplitPane ) {
            MultiSplitPane splitPane = (MultiSplitPane)deepest;
            int dx = 0, dy = 0;
            if( splitPane.isHorizontalSplit() )
                dx = splitPane.getDividerSize()+1;
            else
                dy = splitPane.getDividerSize()+1;
            Point pt = SwingUtilities.convertPoint( contentPane, location, deepest );
            deepest = SwingUtilities.getDeepestComponentAt( deepest, pt.x+dx, pt.y+dy );
        }
        
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
    
    /** Indicates whether the cursor is around the editor area of the main window.
     * In that case is needed also to provide a drop. */
    static boolean isNearEditorEdge(Point location, ViewAccessor viewAccessor, int kind) {
        Component editor = WindowManagerImpl.getInstance().getEditorAreaComponent();
        if(editor == null) {
            return false;
        }
        Point p = new Point(location);
        SwingUtilities.convertPointFromScreen(p, editor.getParent());
        Rectangle editorBounds = editor.getBounds();
        editorBounds.y -= 10;
        editorBounds.height += 10;
        Rectangle shrinked = editor.getBounds();
        shrinked.grow(-10,0);
        shrinked.height -= 10;
        Component dr = viewAccessor.getSlidingModeComponent(Constants.RIGHT);
        if (dr != null) {
            shrinked.width = shrinked.width - dr.getBounds().width;
        }
        dr = viewAccessor.getSlidingModeComponent(Constants.BOTTOM);
        if (dr != null) {
            shrinked.height = shrinked.height - dr.getBounds().height;
        }
        return editorBounds.contains(p) && !shrinked.contains(p) && kind == Constants.MODE_KIND_EDITOR;
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
        centerBounds.y -= 20;
        centerBounds.height += 20;
        Rectangle shrinked = desktop.getBounds();
        shrinked.grow(-10,0);
        shrinked.height -= 10;
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
        CenterPanelDroppable droppable = centerDropWRef.get();
        
        if(droppable == null) {
            droppable = new CenterPanelDroppable();
            centerDropWRef = new WeakReference<CenterPanelDroppable>(droppable);
        }
        
        return droppable;
    }
    
    private static TopComponentDroppable getFreeAreaDroppable(Point location) {
        return new FreeAreaDroppable(location);
    }

    /** Creates fake droppable for editor area. */
    private TopComponentDroppable getEditorAreaDroppable() {
        EditorAreaDroppable droppable = editorDropWRef.get();
        
        if(droppable == null) {
            droppable = new EditorAreaDroppable();
            editorDropWRef = new WeakReference<EditorAreaDroppable>(droppable);
        }
        
        return droppable;
    }
    
    
    /** 
     * Tries to perform actual drop.
     * @param location screen location */
    boolean tryPerformDrop(Controller controller, Set<Component> floatingFrames,
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
        return performDrop(controller, droppable, dropAction, tcArray, location, draggedKind);
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
                Logger.getLogger(WindowDnDManager.class.getName()).log(Level.WARNING, null, ufe);
            } catch(IOException ioe) {
                Logger.getLogger(WindowDnDManager.class.getName()).log(Level.WARNING, null, ioe);
            }
        }
    
        df = new DataFlavor(TopComponentDragSupport.MIME_TOP_COMPONENT_ARRAY, null);
        if(tr.isDataFlavorSupported(df)) {
            try {
                return (TopComponent[])tr.getTransferData(df);
            } catch(UnsupportedFlavorException ufe) {
                Logger.getLogger(WindowDnDManager.class.getName()).log(Level.WARNING, null, ufe);
            } catch(IOException ioe) {
                Logger.getLogger(WindowDnDManager.class.getName()).log(Level.WARNING, null, ioe);
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
    TopComponentDroppable droppable, int dropAction, TopComponent[] tcArray, Point location, int draggedKind) {
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
                    controller.userDroppedTopComponentsAroundEditor(tcArray, (String)constr, kind);
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
                if( droppable instanceof EditorAreaDroppable ) {
                    controller.userDroppedTopComponentsAroundEditor(tcArray, (String)constr, Constants.MODE_KIND_EDITOR);
                } else {
                    controller.userDroppedTopComponentsAround(tcArray, (String)constr);
                }
            } else if(constr instanceof Rectangle) { // XXX free area
                Rectangle bounds = (Rectangle)constr;
                // #38657 Refine bounds.
                Component modeComp = SwingUtilities.getAncestorOfClass(ModeComponent.class, tcArray[0]);
                if(modeComp != null) {
                    bounds.setSize(modeComp.getWidth(), modeComp.getHeight());
                }
                
                controller.userDroppedTopComponentsIntoFreeArea(tcArray, bounds, draggedKind);
            }
        }

        return true;
    }
    // Helpers<<
    

    /** Handles mouse cursors shapes and drag-under feedback during the drag.
     */
    private static class MotionListener implements DragSourceMotionListener {

        private final WindowDnDManager windowDnDManager;
        private final TopComponentDragSupport topComponentDragSupport;

        private Point previousDragLoc;

        /** window used to simulate drag under effect when dropping to free screen area */
        private Window fakeWindow;

        /** helper; true when size of fake window set and known, false otherwise */
        private boolean isSizeSet;
        
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

            if(windowDnDManager.startingTransfer == null)
                return;

            // move separate windows along with the mouse
            /*if (Constants.MODE_STATE_SEPARATED == mode.getState()) {
                handleWindowMove(mode, windowDnDManager.startingTransfer, evt);
            }*/
            boolean isInMainDroppable
                    = windowDnDManager.isInMainWindowDroppable(location, windowDnDManager.draggedKind, windowDnDManager.startingTransfer);
            boolean isInFrameDroppable
                    = isInFloatingFrameDroppable(windowDnDManager.getFloatingFrames(), location, windowDnDManager.draggedKind, windowDnDManager.startingTransfer)
                    && Switches.isTopComponentUndockingEnabled();
            boolean isAroundCenterPanel
                    = isAroundCenterPanel(location);
            boolean shouldPaintFakeWindow = false;

            if(isInMainDroppable || isInFrameDroppable || isAroundCenterPanel) {
                TopComponentDroppable droppable 
                        = windowDnDManager.findDroppableFromScreen(windowDnDManager.getFloatingFrames(), location, windowDnDManager.draggedKind, windowDnDManager.startingTransfer);
                //hack - can't get the bounds correctly, sometimes freearedroppable gets here..
                
                if (droppable instanceof FreeAreaDroppable) {
                    if(WindowManagerImpl.getInstance().getEditorAreaState() == Constants.EDITOR_AREA_SEPARATED
                        && droppable.canDrop(windowDnDManager.startingTransfer, location)) {
                        topComponentDragSupport.setSuccessCursor(true);
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
                        topComponentDragSupport.setSuccessCursor(false);
                    } else {
                        topComponentDragSupport.setUnsuccessCursor();
                    }
                    dragOverDropTarget(location, droppable);
                }
            } else if(!isInMainWindow(location)
            && windowDnDManager.isInFloatingFrame(location)) {
                // Simulates success drop in free area.
                topComponentDragSupport.setSuccessCursor(false);
            } else if(isInFreeArea(location, fakeWindow)
            && getFreeAreaDroppable(location).canDrop(windowDnDManager.startingTransfer, location)
                    && Switches.isTopComponentUndockingEnabled()) {
                topComponentDragSupport.setSuccessCursor(true);
                // paint fake window during move over free area
//                shouldPaintFakeWindow = true;
            } else {
                topComponentDragSupport.setUnsuccessCursor();
            }
            paintFakeWindow(shouldPaintFakeWindow, evt);
            
            if(!isInMainDroppable && !isInFrameDroppable && !isAroundCenterPanel) {
                clearExitedDropTarget();
            }
        }
        
        /** Simulates dropOver event, for glass pane to indicate possible drop
         * operation. */
        private /*static*/ void dragOverDropTarget(Point location,
        TopComponentDroppable droppable) {
            DropTargetGlassPane lastTarget
                = windowDnDManager.lastTargetWRef.get();

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
                = windowDnDManager.lastTargetWRef.get();

            if(lastTarget != null) {
                lastTarget.clearIndications();
                windowDnDManager.lastTargetWRef = new WeakReference<DropTargetGlassPane>(null);
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

        void dragFinished () {
            previousDragLoc = null;
            if (fakeWindow != null) {
                fakeWindow.dispose();
                fakeWindow = null;
            }
        }
        
        /** Shows or hides fake window as drag under effect
         */
        private void paintFakeWindow (boolean visible, DragSourceDragEvent evt) {
//            Point loc = evt.getLocation();
//            // no window if location is unknown
//            if (loc == null) {
//                return;
//            }
//            if (fakeWindow == null) {
//                fakeWindow = createFakeWindow();
//                isSizeSet = false;
//            }
//            fakeWindow.setLocation(loc);
//            fakeWindow.setVisible(visible);
//            // calculate space for window with decorations (title, border...)
//            if (visible && !isSizeSet) {
//                Dimension size = windowDnDManager.startingTransfer.getSize();
//                Insets insets = fakeWindow.getInsets();
//                size.width += insets.left + insets.right;
//                size.height += insets.top + insets.bottom;
//                fakeWindow.setSize(size);
//                isSizeSet = true;
//            }
        }

        /** Creates and returns fake window as drag under effect */
        private Window createFakeWindow () {
            Window result;
            if (windowDnDManager.draggedKind == Constants.MODE_KIND_EDITOR) {
                result = new JFrame();
            } else {
                result = new JDialog((JFrame) null);
            }
            result.setAlwaysOnTop(true);
            return result;
        }

    } // End of class MotionListener.
    

    // XXX
    /** Interface for accessing   */
    public interface ViewAccessor {
        public Set<Component> getModeComponents();
        public Set<Component> getSeparateModeFrames();
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
            if(null != leftSlide && p.x <  leftSlide.getBounds().width + 10) {
                return javax.swing.JSplitPane.LEFT;
            } else if(p.y < bounds.y) {
                return javax.swing.JSplitPane.TOP;
            } else if(null !=rightSlide && null != leftSlide 
                      && p.x > bounds.width - 10 - rightSlide.getBounds().width - leftSlide.getBounds().width) {
                return javax.swing.JSplitPane.RIGHT;
            } else if(null != bottomSlide && p.y > bounds.height - 10 - bottomSlide.getBounds().height) {
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
    
    /** Fake helper droppable used when used around  */
    private class EditorAreaDroppable implements TopComponentDroppable {

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
            if(null != leftSlide && p.x <  leftSlide.getBounds().width + 10) {
                return javax.swing.JSplitPane.LEFT;
            } else if(p.y < bounds.y) {
                return javax.swing.JSplitPane.TOP;
            } else if(null !=rightSlide && null != leftSlide 
                      && p.x > bounds.width - 10 - rightSlide.getBounds().width - leftSlide.getBounds().width) {
                return javax.swing.JSplitPane.RIGHT;
            } else if(null != bottomSlide && p.y > bounds.height - 10 - bottomSlide.getBounds().height) {
                return javax.swing.JSplitPane.BOTTOM;
            }

            return null;
        }

        /** Implements <code>TopComponentDroppable</code>. */
        public Component getDropComponent() {
            return WindowManagerImpl.getInstance().getEditorAreaComponent();
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
            return mode != null && mode.getKind() == Constants.MODE_KIND_EDITOR;
        }
        
        public boolean supportsKind(int kind, TopComponent transfer) {
            if(Constants.SWITCH_MODE_ADD_NO_RESTRICT
            || WindowManagerImpl.getInstance().isTopComponentAllowedToMoveAnywhere(transfer)) {
                return true;
            }

            return kind == Constants.MODE_KIND_EDITOR;
        }


    } // End of class EditorAreaDroppable.

    
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
            if (mode == null) {
                return false;
            }
            if (Constants.SWITCH_MODE_ADD_NO_RESTRICT ||
                WindowManagerImpl.getInstance().isTopComponentAllowedToMoveAnywhere(transfer)) {
                return true;
            }

            // don't accept drop from separated mode with single component in it,
            // it makes no sense (because such DnD into free area equals to
            // simple window move)
            if (mode.getState() == Constants.MODE_STATE_SEPARATED &&
                mode.getOpenedTopComponents().size() == 1) {
                return false;
            }

            return true;
        }
        
        public boolean supportsKind(int kind, TopComponent transfer) {
            return true;
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
            if( null == root || null == SwingUtilities.getWindowAncestor(original.getDropComponent()) ) {
                return false;
            }
            Point barLoc = SwingUtilities.convertPoint(root, location, original.getDropComponent());
            if (original.getDropComponent().contains(barLoc)) {
                return true;
            }
            Dimension dim = original.getDropComponent().getSize();
            if (Constants.LEFT.equals(side)) {
                int abs = Math.abs(barLoc.x);
                if (barLoc.y > - Constants.DROP_AREA_SIZE && barLoc.y < dim.height + Constants.DROP_AREA_SIZE) {
                    if (isShowing && abs < Constants.DROP_AREA_SIZE) {
                        return true;
                    }
                    if (!isShowing && barLoc.x <= 0 && barLoc.x > - Constants.DROP_AREA_SIZE) {
                        return true;
                    }
                }
            }
            else if (Constants.RIGHT.equals(side)) {
                if (barLoc.y > - Constants.DROP_AREA_SIZE && barLoc.y < dim.height + Constants.DROP_AREA_SIZE) {
                    if (isShowing && ((barLoc.x < 0 && barLoc.x > - Constants.DROP_AREA_SIZE)
                                     || barLoc.x > 0 && barLoc.x - dim.width < Constants.DROP_AREA_SIZE)) {
                        return true;
                    }
                    if (!isShowing && barLoc.x >= 0 && barLoc.x < Constants.DROP_AREA_SIZE + dim.width) {
                        return true;
                    }
                }
            } 
            else if (Constants.BOTTOM.equals(side)) {
                if (barLoc.x > - Constants.DROP_AREA_SIZE && barLoc.x < dim.width + Constants.DROP_AREA_SIZE) {
                    if (isShowing && ((barLoc.y < 0 && barLoc.y > - Constants.DROP_AREA_SIZE)
                                     || barLoc.y > 0 && barLoc.y - dim.height < Constants.DROP_AREA_SIZE)) {
                        return true;
                    }
                    if (!isShowing && barLoc.y >= 0 && barLoc.y < Constants.DROP_AREA_SIZE + dim.height) {
                        return true;
                    }
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
        
        public Rectangle getPaintArea() {
            Rectangle dim = original.getDropComponent().getBounds();
            if (dim.width > 10 && dim.height > 10) {
                return null;
            }
            Component glassPane = ((JComponent)original.getDropComponent()).getRootPane().getGlassPane();
            Point leftTop = SwingUtilities.convertPoint(original.getDropComponent(), 0, 0, glassPane);
                    
            if (Constants.RIGHT.equals(side)) {
                leftTop = new Point(leftTop.x - 24, leftTop.y);
            }
            else if (Constants.BOTTOM.equals(side)) {
                leftTop = new Point(0, leftTop.y - 24);
            }
            Rectangle rect = new Rectangle(leftTop.x, leftTop.y, Math.max(25, dim.width), Math.max(25, dim.height));
            if (Constants.BOTTOM.equals(side)) {
                // for bottom has special hack to use the whole width
                rect.width = glassPane.getBounds().width;
            }
            return rect;
        }
    }

}

