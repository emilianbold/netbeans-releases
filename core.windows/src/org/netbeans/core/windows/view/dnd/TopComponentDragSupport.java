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


import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.Debug;
import org.netbeans.core.windows.view.ui.ModeComponent;
import org.netbeans.core.windows.view.ui.Tabbed;
import org.netbeans.core.windows.view.ui.tabcontrol.TabbedAdapter;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakSet;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Window system drag support for <code>TopComponet</code>'s.
 * It imitates role of drag gesture recognizer, possible
 * on any kind of <code>Component</code>, currently on <code>Tabbed</code>.
 * Starts also programatically the DnD for TopComponent in container
 * when the starting gestures are Shift+Mouse Drag or Ctrl+Mouse Drag
 * respectivelly.
 * It serves as <code>DragSourceListener</code> during the DnD in progress
 * and sets dragging cursor appopriatelly.
 *
 * <em>Note:</em> There is used only one singleton instance in window system
 * DnD available via {@link #getDefault}.
 *
 *
 * @author  Peter Zavadsky
 *
 * @see java awt.dnd.DragSourceListener
 */
final class TopComponentDragSupport 
implements AWTEventListener, DragSourceListener {
    
    /** Mime type for <code>TopComponent</code> <code>DataFlavor</code>. */
    public static final String MIME_TOP_COMPONENT = 
        DataFlavor.javaJVMLocalObjectMimeType
        // Note: important is the space after semicolon, thus to match
        // when comparing.
        + "; class=org.openide.windows.TopComponent"; // NOI18N

    /** Mime type for <code>TopComponent.Cloneable</code> <code>DataFlavor</code>. */
    public static final String MIME_TOP_COMPONENT_CLONEABLE = 
        DataFlavor.javaJVMLocalObjectMimeType
        + "; class=org.openide.windows.TopComponent$Cloneable"; // NOI18N
    

    /** Mime type for <code>TopComponent</code>'s array <code>DataFlavor</code>. */
    public static final String MIME_TOP_COMPONENT_ARRAY =
        DataFlavor.javaJVMLocalObjectMimeType
        + "; class=org.netbeans.core.windows.view.dnd.TopComponentDragSupport$TopComponentArray"; // NOI18N

    
    /** 'Copy window' cursor type. */
    private static final int CURSOR_COPY    = 0;
    /** 'Copy_No window' cursor type. */
    private static final int CURSOR_COPY_NO = 1;
    /** 'Move window' cursor type. */
    private static final int CURSOR_MOVE    = 2;
    /** 'Move_No window' cursor type. */
    private static final int CURSOR_MOVE_NO = 3;
    /** Cursor type indicating there cannont be copy operation 
     * done, but could be done move operation. In fact is
     * the same like {@link #CURSOR_COPY_NO} with the diff name
     * to be recognized correctly when switching action over drop target */
    private static final int CURSOR_COPY_NO_MOVE = 4;

    /** Name for 'Copy window' cursor. */
    private static final String NAME_CURSOR_COPY         = "CursorTopComponentCopy"; // NOI18N
    /** Name for 'Copy_No window' cursor. */
    private static final String NAME_CURSOR_COPY_NO      = "CursorTopComponentCopyNo"; // NOI18N
    /** Name for 'Move window' cursor. */
    private static final String NAME_CURSOR_MOVE         = "CursorTopComponentMove"; // NOI18N
    /** Name for 'Move_No window' cursor. */
    private static final String NAME_CURSOR_MOVE_NO      = "CursorTopComponentMoveNo"; // NOI18N
    /** */
    private static final String NAME_CURSOR_COPY_NO_MOVE = "CursorTopComponentCopyNoMove"; // NOI18N
    
    /** Debugging flag. */
    private static final boolean DEBUG = Debug.isLoggable(TopComponentDragSupport.class);
    
    private final WindowDnDManager windowDnDManager;

    /** Weak reference to <code>DragSourceContext</code> used in processed
     * drag operation. Used for by fixing bugs while not passed correct
     * order of events to <code>DragSourceListener</code>. */
    private Reference dragContextWRef = new WeakReference(null);
    
    /** Flag indicating the current window drag operation transferable
     * can be 'copied', i.e. the dragged <code>TopComponent</code> is
     * <code>TopComponent.Cloneable</code> instance. */
    private boolean canCopy;
    
    // #21918. There is not possible to indicate drop action in "free" desktop
    // area. This field helps to workaround the problem.
    /** Flag indicating user drop action. */
    private int hackUserDropAction;

    // #21918. Determine the ESC pressed.
    /** Flag indicating the user has cancelled drag operation by pressing ESC key. */
    private boolean hackESC;

    /** Weak set of componens on which we listen for ESC key. */
    private final Set keyObservers = new WeakSet(4);

    private Point startingPoint;
    private Component startingComponent;
    private long startingTime;
    
    
    /** Creates a new instance of TopComponentDragSupport. */
    TopComponentDragSupport(WindowDnDManager windowDnDManager) {
        this.windowDnDManager = windowDnDManager;
    }

    
    /** Informs whether the 'copy' operation is possible. Gets valid result
     * during processed drag operation only.
     * @return <code>true</code> if the drop copy operation is possible from
     * drag source point of view
     * @see #canCopy */
    public boolean isCopyOperationPossible() {
        return canCopy;
    }

    /** Simulates drag gesture recongition valid for winsys.
     * Implements <code>AWTEventListener</code>. */
    public void eventDispatched(AWTEvent evt) {
        MouseEvent me = (MouseEvent) evt;

        // #40736: only left mouse button drag should start DnD
        if((me.getID() == MouseEvent.MOUSE_PRESSED) && SwingUtilities.isLeftMouseButton(me)) {
                startingPoint = me.getPoint();
            startingComponent = me.getComponent();
            startingTime = me.getWhen();
        } else if(me.getID() == MouseEvent.MOUSE_RELEASED) {
            startingPoint = null;
            startingComponent = null;
        }
        
        if(evt.getID() != MouseEvent.MOUSE_DRAGGED) {
            return;
        }
        
        if(windowDnDManager.isDragging()) {
            return;
        }
        if(startingPoint == null) {
            return;
        }

        Component srcComp = startingComponent;
        if(srcComp == null) {
            return;
        }
        
        final Point point = new Point(startingPoint);
        Point currentPoint = me.getPoint();
        Component currentComponent = me.getComponent();
        if(currentComponent == null) {
            return;
        }
        currentPoint = SwingUtilities.convertPoint(currentComponent, currentPoint, srcComp);
        if(Math.abs(currentPoint.x - point.x) <= Constants.DRAG_GESTURE_START_DISTANCE
        && Math.abs(currentPoint.y - point.y) <= Constants.DRAG_GESTURE_START_DISTANCE) {
            return;
        }
        // time check, to prevent wild mouse clicks to be considered DnD start
        if (me.getWhen() - startingTime <= Constants.DRAG_GESTURE_START_TIME) {
            return;
        }
        startingPoint = null;
        startingComponent = null;
        
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("eventDispatched (MOUSE_DRAGGED)"); // NOI18N
        }
        
        // XXX Do not clash with JTree (e.g. in explorer) drag.
        if((srcComp instanceof JTree)
        && ((JTree)srcComp).getPathForLocation(me.getX(), me.getY()) != null) {
            return;  
        }
        
        // #22622: AWT listener just passivelly listnens what is happenning around,
        // and we need always the deepest component to start from.
        srcComp = SwingUtilities.getDeepestComponentAt(srcComp, point.x, point.y);

        boolean ctrlDown  = me.isControlDown();
        
        TopComponent tc = null;

        if (tc == null) {
            Tabbed tabbed;
            if(srcComp instanceof Tabbed) {
                tabbed = (Tabbed)srcComp;
            } else {
                tabbed = (Tabbed)SwingUtilities.getAncestorOfClass(Tabbed.class, srcComp);
            }
            if(tabbed == null) {
                return;
            }

            Point pp = new Point(point);
            Point p = SwingUtilities.convertPoint(srcComp, pp, (Component)tabbed);

            // #38362 Don't start DnD when closing tab.
            String cmd = tabbed.getCommandAtPoint(p);

            if (TabbedAdapter.COMMAND_SELECT.equals(cmd)) {
                tc = tabbed.getTopComponentAt(tabbed.tabForCoordinate(p));
            }
        }

        if (tc == null) {
            return;
        }

        // #21918. See above.
        if (ctrlDown) {
            hackUserDropAction = DnDConstants.ACTION_COPY;
        }
        else {
            hackUserDropAction = DnDConstants.ACTION_MOVE;
        }
                 

        List list = new ArrayList();
        list.add(me);

        // Get start droppable (if there) and its starting point.
        TopComponentDroppable startDroppable = (TopComponentDroppable)SwingUtilities
                            .getAncestorOfClass(TopComponentDroppable.class, tc);
        Point startPoint;
        if(startDroppable != null) {
            startPoint = point;
            Point pp = new Point(point);
            startPoint = SwingUtilities.convertPoint(srcComp, pp, (Component)startDroppable);
        } else {
            startPoint = null;
        }
        //dragSource.startDrag(event, Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR) ,image , new Point(-offX, -offY),text, this);

        doStartDrag(
            tc, 
            new DragGestureEvent(
                new FakeDragGestureRecognizer(windowDnDManager, me),
                hackUserDropAction,
                point,
                list
            ),
            startDroppable,
            startPoint
        );
    }
    
    /** Actually starts the drag operation. */
    private void doStartDrag(Object transfer, DragGestureEvent evt,
    TopComponentDroppable startingDroppable, Point startingPoint) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("doStartDrag"); // NOI18N
        }
        
        TopComponent firstTC = transfer instanceof TopComponent
                ? (TopComponent)transfer
                : (((TopComponent[])transfer)[0]);
        
        // #22132. If in modal dialog no drag allowed.
        Dialog dlg = (Dialog)SwingUtilities.getAncestorOfClass(
                Dialog.class, firstTC);
        if(dlg != null && dlg.isModal()) {
            return; 
        }
        
        if(firstTC instanceof TopComponent.Cloneable) {
            canCopy = true;
        } else {
            canCopy = false;
        }
        
        // Inform window sys there is DnD about to start.
        // XXX Using the firstTC in DnD manager is a hack.
        windowDnDManager.dragStarting(startingDroppable, startingPoint, firstTC);

        Cursor cursor = hackUserDropAction == DnDConstants.ACTION_MOVE
            ? getDragCursor(CURSOR_MOVE)
            : (canCopy 
                ? getDragCursor(CURSOR_COPY)
                : getDragCursor(CURSOR_COPY_NO_MOVE));

        
        Container con = SwingUtilities.getAncestorOfClass(
                ModeComponent.class, firstTC);
                        
        if(con == null) {
            // TopComponent not in mode container. Can not start drag!
            windowDnDManager.resetDragSource();
            return;
        }
        
        // Sets listnening for ESC key.
        addListening(con);
        hackESC = false;
        
        Tabbed tabbed = (Tabbed) SwingUtilities.getAncestorOfClass (Tabbed.class,
            firstTC);
        
        Image img = null;
        if (tabbed != null && Constants.SWITCH_USE_DRAG_IMAGES) {
            int idx = tabbed.indexOf(firstTC);
            img = tabbed.createImageOfTab(idx);
        }

        try {
            evt.startDrag(
                cursor,
                img,
                new Point (0,0), 
                (transfer instanceof TopComponent
                        ? (Transferable)new TopComponentTransferable(
                                (TopComponent)transfer)
                        : (Transferable)new TopComponentArrayTransferable(
                                (TopComponent[])transfer)),
                this
            );
        } catch(InvalidDnDOperationException idoe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    idoe);
            
            removeListening();
            windowDnDManager.resetDragSource();
        }
    }

    private AWTEventListener keyListener = new AWTEventListener() {
            public void eventDispatched(AWTEvent event) {
                KeyEvent keyevent = (KeyEvent)event;
                
                if (keyevent.getID() == KeyEvent.KEY_RELEASED && 
                    keyevent.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hackESC = true;
                }                
            }
            
        };
    /** Adds <code>KeyListener</code> to container and its component
     * hierarchy to listen for ESC key. */
    private void addListening(Container con) {
        Toolkit.getDefaultToolkit().addAWTEventListener(keyListener, AWTEvent.KEY_EVENT_MASK);
    }
    
    /** Removes ESC listening. Helper method. */
    private void removeListening() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(keyListener);
    }
    
    // >> DragSourceListener implementation >>
    /** Implements <code>DragSourceListener</code> method.
     * It just refreshes the weak reference of <code>DragSourceContext</code>
     * for the sake of setSuccessCursor method.
     * The excpected code, changing of cursor, is done in setSuccessCursor method
     * due to an undeterministic calls of this method especially in MDI mode.
     * @see #setSuccessCursor */
    public void dragEnter(DragSourceDragEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragEnter");// NOI18N
        }
            
        // Just refresh the weak ref to the context if necessary.
        // The expected code here is done by ragExitHack method called from DropTarget's.
        if(dragContextWRef.get() == null) {
            dragContextWRef = new java.lang.ref.WeakReference(evt.getDragSourceContext());
        }
    }

    /** Dummy implementation of <code>DragSourceListener</code> method. */
    public void dragOver(DragSourceDragEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragOver"); // NOI18N
        }
    }

    /** Implements <code>DragSourceListener</code> method.
     * It just refreshes the weak reference of <code>DragSourceContext</code>
     * for the sake of setUnsuccessCursor method.
     * The excpected code, changing of cursor, is done in setUnsuccessCursor method
     * due to an undeterministic calls of this method especially in MDI mode.
     * @see #setUnsuccessCursor */
    public void dragExit(DragSourceEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragExit"); // NOI18N
        }
        
        // Just refresh the weak ref to the context if necessary.
        // The expected code here is done by ragExitHack method called from DropTarget's.
        if(dragContextWRef.get() == null) {
            dragContextWRef = new WeakReference(evt.getDragSourceContext());
        }
    }
    
    /** Implements <code>DragSourceListener</code> method.
     * It changes the cursor type from copy to move and bakc accordting the
     * user action. */
    public void dropActionChanged(DragSourceDragEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dropActionChanged"); // NOI18N
        }
        String name = evt.getDragSourceContext().getCursor().getName();
        
        if(name == null) {
            // Not our cursor??
            return;
        }

        // For us is the user action important.
        int userAction = evt.getUserAction();

        // Consider NONE action as MOVE one.
        if(userAction == DnDConstants.ACTION_NONE) {
            userAction = DnDConstants.ACTION_MOVE;
        }
        // #21918. See above.
        hackUserDropAction = userAction;
        
        int type;
        if((NAME_CURSOR_COPY.equals(name)
        || NAME_CURSOR_COPY_NO_MOVE.equals(name))
        && userAction == DnDConstants.ACTION_MOVE) {
            type = CURSOR_MOVE;
        } else if(NAME_CURSOR_COPY_NO.equals(name)
        && userAction == DnDConstants.ACTION_MOVE) {
            type = CURSOR_MOVE_NO;
        } else if(NAME_CURSOR_MOVE.equals(name)
        && userAction == DnDConstants.ACTION_COPY) {
            type = CURSOR_COPY;
        } else if(NAME_CURSOR_MOVE_NO.equals(name)
        && userAction == DnDConstants.ACTION_COPY) {
            type = CURSOR_COPY_NO;
        } else {
            return;
        }

        // There can't be copy operation performed,
        // transferreed TopComponent in not of TopComponent.Cloneable instance.
        if(type == CURSOR_COPY && !canCopy) {
            type = CURSOR_COPY_NO_MOVE;
        }

        // Check if there is already our cursor.
        if(getDragCursorName(type).equals(
        evt.getDragSourceContext().getCursor().getName())) {
            return;
        }
        
        evt.getDragSourceContext().setCursor(getDragCursor(type));
    }

    /** Implements <code>DragSourceListener</code> method.
     * Informs window dnd manager the drag operation finished.
     * @see WindowDnDManager#dragFinished */
    public void dragDropEnd(final DragSourceDropEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragDropEnd"); // NOI18N
        }
        
        windowDnDManager.dragFinished();
        
        try {
            if(checkDropSuccess(evt)) {
                removeListening();
                return;
            }
            
            // Now simulate drop into "free" desktop area.
            
            // Finally schedule the "drop" task later to be able to
            // detect if ESC was pressed.
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    SwingUtilities.invokeLater(createDropIntoFreeAreaTask(
                            evt, evt.getLocation()));
                }},
                250 // XXX #21918, Neccessary to skip after possible ESC key event.
            );
        } finally {
            windowDnDManager.dragFinishedEx();
        }
    }
    // << DragSourceListener implementation <<

    /** Checks whether there was a successfull drop. */
    private boolean checkDropSuccess(DragSourceDropEvent evt) {
        // XXX #21917.
        if(windowDnDManager.isDropSuccess()) {
            return true;
        }
        
        // Gets location.
        Point location = evt.getLocation();
        if(location == null) {
            return true;
        }
        
        if(WindowDnDManager.isInMainWindow(location)
        || windowDnDManager.isInFloatingFrame(location)
        || WindowDnDManager.isAroundCenterPanel(location)) {
            return false;
        }
//        else if(evt.getDropSuccess()) {
//            return true;
//        } // PENDING it seem it is not working correctly (at least on linux).
        return false;
    }
    
    /** Creates task which performs actual drop into "free area", i.e. it
     * creates new separated (floating) window. */
    private Runnable createDropIntoFreeAreaTask(final DragSourceDropEvent evt,
    final Point location) {
        return new Runnable() {
            public void run() {
                // XXX #21918. Don't move the check sooner
                // (before the enclosing blocks), it would be invalid.
                if(hackESC) {
                    removeListening();
                    return;
                }

                TopComponent[] tcArray = WindowDnDManager.extractTopComponent(
                    false,
                    evt.getDragSourceContext().getTransferable()
                );
                
                // Provide actual drop into "free" desktop area.
                if(tcArray != null) {
                    // XXX there is a problem if jdk dnd framework sets as drop action
                    // ACTION_NONE, there is not called drop event on DropTargetListener,
                    // even it is there.
                    // Performs hacked drop action, simulates ACTION_MOVE when
                    // system set ACTION_NONE (which we do not use).
                    WindowDnDManager.tryPerformDrop(
                        windowDnDManager.getController(),
                        windowDnDManager.getFloatingFrames(),
                        location,
                        DnDConstants.ACTION_MOVE, // MOVE only
                        evt.getDragSourceContext().getTransferable());
                }
            }
        };
    }

    /** Gets bounds for the new mode created in the "free area". */
    private static Rectangle getBoundsForNewMode(TopComponent tc, Point location) {
        int width = tc.getWidth();
        int height = tc.getHeight();
        
        // Take also the native title and borders into account.
        java.awt.Window window = SwingUtilities.getWindowAncestor(tc);
        if(window != null) {
            java.awt.Insets ins = window.getInsets();
            width += ins.left + ins.right;
            height += ins.top + ins.bottom;
        }
        // PENDING else { how to get the insets of newly created window? }

        Rectangle tcBounds = tc.getBounds();
        Rectangle initBounds = new Rectangle(
            location.x,
            location.y,
            width,
            height
        );

        return initBounds;
    }
    
    /** Hacks problems with <code>dragEnter</code> wrong method calls.
     * It plays its role. Sets the cursor from 'no-drop' state
     * to its 'drop' state sibling.
     * @see #dragEnter */
    void setSuccessCursor() {
        int dropAction = hackUserDropAction;
        DragSourceContext ctx = (DragSourceContext)dragContextWRef.get();
        
        if(ctx == null) {
            return;
        }

        int type;
        if(dropAction == DnDConstants.ACTION_MOVE) {
            type = CURSOR_MOVE;
        } else if(dropAction == DnDConstants.ACTION_COPY) {
            if(canCopy) {
                type = CURSOR_COPY;
            } else {
                type = CURSOR_COPY_NO_MOVE;
            }
        } else {
            // PENDING throw exception?
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException(
                            "Invalid action type->"+dropAction)); // NOI18N
            return;
        }

        // Check if there is already our cursor.
        if(getDragCursorName(type).equals(ctx.getCursor().getName())) {
            return;
        }
        
        ctx.setCursor(getDragCursor(type));
    }
    
    /** Hacks problems with <code>dragExit</code> wrong method calls.
     * It plays its role. Sets the cursor from 'drop' state
     * to its 'no-drop' state sibling.
     * @see #dragExit */
    void setUnsuccessCursor() {
        DragSourceContext ctx = (DragSourceContext)dragContextWRef.get();
        
        if(ctx == null) {
            return;
        }
        
        String name = ctx.getCursor().getName();
        
        int type;
        if(NAME_CURSOR_COPY.equals(name)
        || NAME_CURSOR_COPY_NO_MOVE.equals(name)) {
            type = CURSOR_COPY_NO;
        } else if(NAME_CURSOR_MOVE.equals(name)) {
            type = CURSOR_MOVE_NO;
        } else {
            return;
        }

        ctx.setCursor(getDragCursor(type));
    }

    /** Provides cleanup when finished drag operation. Ideally the code
     * should reside in {@ling #dragDropEnd} method only. But that one
     * is not called in case of error in DnD framework. */
    void dragFinished() {
        dragContextWRef = new WeakReference(null);
    }
   
    private static void debugLog(String message) {
        Debug.log(TopComponentDragSupport.class, message);
    }
    
    // Helpers>>
    /** Gets window drag <code>Cursor</code> of specified type. Utility method.
     * @param type valid one of {@link #CURSOR_COPY}, {@link #CURSOR_COPY_NO}, 
     *             {@link #CURSOR_MOVE}, {@link #CURSOR_MOVE_NO} */
    private static String getDragCursorName(int type) {
        if(type == CURSOR_COPY) {
            return NAME_CURSOR_COPY;
        } else if(type == CURSOR_COPY_NO) {
            return NAME_CURSOR_COPY_NO;
        } else if(type == CURSOR_MOVE) {
            return NAME_CURSOR_MOVE;
        } else if(type == CURSOR_MOVE_NO) {
            return NAME_CURSOR_MOVE_NO;
        } else if(type == CURSOR_COPY_NO_MOVE) {
            return NAME_CURSOR_COPY_NO_MOVE;
        } else {
            return null;
        }
    }
    
    /** Gets window drag <code>Cursor</code> of specified type. Utility method.
     * @param type valid one of {@link #CURSOR_COPY}, {@link #CURSOR_COPY_NO}, 
     *             {@link #CURSOR_MOVE}, {@link #CURSOR_MOVE_NO}
     * @exception IllegalArgumentException if invalid type parameter passed in */
    private static Cursor getDragCursor(int type) {
        Image image = null;
        String name = null;
        
        if(type == CURSOR_COPY) {
            image = Utilities.loadImage(
                "org/netbeans/core/resources/topComponentDragCopy.gif"); // NOI18N
            name = NAME_CURSOR_COPY;
        } else if(type == CURSOR_COPY_NO) {
            image = Utilities.loadImage(
                "org/netbeans/core/resources/topComponentDragCopyNo.gif"); // NOI18N
            name = NAME_CURSOR_COPY_NO;
        } else if(type == CURSOR_MOVE) {
            image = Utilities.loadImage(
                "org/netbeans/core/resources/topComponentDragMove.gif"); // NOI18N
            name = NAME_CURSOR_MOVE;
        } else if(type == CURSOR_MOVE_NO) {
            image = Utilities.loadImage(
                "org/netbeans/core/resources/topComponentDragMoveNo.gif"); // NOI18N
            name = NAME_CURSOR_MOVE_NO;
        } else if(type == CURSOR_COPY_NO_MOVE) {
            image = Utilities.loadImage(
                "org/netbeans/core/resources/topComponentDragCopyNo.gif"); // NOI18N
            name = NAME_CURSOR_COPY_NO_MOVE;
        } else {
            throw new IllegalArgumentException("Unknown cursor type=" + type); // NOI18N
        }
        
        return createCustomCursor(image, name);
    }

    // XXX Copied from openide/explorer code.
    /** Creates window drag <code>Cursor</code> created from given icon
     * and name. Utility method. */
    private static Cursor createCustomCursor(Image icon, String name) {
        Toolkit t = Toolkit.getDefaultToolkit();
        Dimension d = t.getBestCursorSize(16, 16);
        Image i = icon;
        if (d.width != icon.getWidth(null)) {
            // need to resize the icon
            Image empty = createBufferedImage(d.width, d.height);
            i = Utilities.mergeImages(icon, empty, 0, 0);
        }
        
        return t.createCustomCursor(i, new Point(11, 9), name);
    }
    
    /** Creates <code>BufferedImage</code> and <code>Transparency.BITMASK</code> 
     * <em>Note:</em> this method is copied
     * from <code>org.openide.util.IconManager</code>.
     * Should it be exposed in Utilities? I don't know (dstrupl). */
    private static final BufferedImage createBufferedImage(int width, int height) {
        ColorModel model = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice().getDefaultConfiguration()
            .getColorModel(Transparency.BITMASK);
        
        BufferedImage buffImage = new BufferedImage(
            model,
            model.createCompatibleWritableRaster(width, height),
            model.isAlphaPremultiplied(),
            null
        );
        
        return buffImage;
    }

    // Helpers<<
    
    /** <code>Transferable</code> used for <code>TopComponent</code> instances
     * to be used in window system DnD. */
    private static class TopComponentTransferable extends Object
    implements Transferable {

        /** <code>TopComponent</code> to be transferred. */
        private TopComponent tc;

        
        /** Crates <code>Transferable</code> for specified <code>TopComponent</code> */
        public TopComponentTransferable(TopComponent tc) {
            this.tc = tc;
        }

        
        // >> Transferable implementation >>
        /** Implements <code>Transferable</code> method.
         * @return <code>TopComponent</code> instance for <code>DataFlavor</code>
         * with mimetype equal to {@link #MIME_TOP_COMPONENT} or if mimetype
         * equals to {@link #MIME_CLONEABLE_TOP_COMPONENT} and the top component
         * is instance of <code>TopComponent.Cloneable</code> returns the instance */
        public Object getTransferData(DataFlavor df) {
            if(MIME_TOP_COMPONENT.equals(df.getMimeType())) {
                return tc;
            } else if(MIME_TOP_COMPONENT_CLONEABLE.equals(
                df.getMimeType())
            && tc instanceof TopComponent.Cloneable) {
                return tc;
            }

            return null;
        }

        /** Implements <code>Transferable</code> method.
         * @return Array of <code>DataFlavor</code> with mimetype
         * {@link #MIME_TOP_COMPONENT} and also with mimetype
         * {@link #MIME_CLONEABLE_TOP_COMPONENT}
         * if the <code>tc</code> is instance
         * of <code>TopComponent.Cloneable</code> */
        public DataFlavor[] getTransferDataFlavors() {
            if(tc instanceof TopComponent.Cloneable) {
                return new DataFlavor[] {
                    new DataFlavor(MIME_TOP_COMPONENT, null),
                    new DataFlavor(
                        MIME_TOP_COMPONENT_CLONEABLE, null)
                };
            } else {
                return new DataFlavor[] {
                    new DataFlavor(MIME_TOP_COMPONENT, null)
                };
            }
        }

        /** Implements <code>Transferable</code> method.
         * @return <code>true</code> for <code>DataFlavor</code> with mimetype
         * equal to {@link #MIME_TOP_COMPONENT}
         * and if <code>tc</code> is instance
         * of <code>TopComponent.Cloneable</code> also for the one
         * with mimetype {@link #MIME_TOP_COMPONENT_CLONEABLE},
         * <code>false</code> otherwise */
        public boolean isDataFlavorSupported(DataFlavor df) {
            if(MIME_TOP_COMPONENT.equals(df.getMimeType())) {
                return true;
            } else if(MIME_TOP_COMPONENT_CLONEABLE.equals(
                df.getMimeType())
            && tc instanceof TopComponent.Cloneable) {
                return true;
            }

            return false;
        }
        // << Transferable implementation <<
    } // End of class TopComponentTransferable.

    /** <code>Transferable</code> used for <code>TopComponent</code> instances
     * to be used in window system DnD. */
    private static class TopComponentArrayTransferable extends Object
    implements Transferable {

        /** <code>TopComponent</code> to be transferred. */
        private TopComponent[] tcArray;

        
        /** Crates <code>Transferable</code> for specified <code>TopComponent</code> */
        public TopComponentArrayTransferable(TopComponent[] tcArray) {
            this.tcArray = tcArray;
        }

        
        // >> Transferable implementation >>
        /** Implements <code>Transferable</code> method.
         * @return <code>TopComponent</code> instance for <code>DataFlavor</code>
         * with mimetype equal to {@link #MIME_TOP_COMPONENT}
         * or if mimetype equals to
         * {@link #MIME_CLONEABLE_TOP_COMPONENT} and
         * the top component is instance
         * of <code>TopComponent.Cloneable</code> returns the instance. */
        public Object getTransferData(DataFlavor df) {
            if(MIME_TOP_COMPONENT_ARRAY
            .equals(df.getMimeType())) {
                return tcArray;
            }

            return null;
        }

        /** Implements <code>Transferable</code> method.
         * @return Array of <code>DataFlavor</code> with mimetype
         * {@link #MIME_TOP_COMPONENT} and also with mimetype
         * {@link #MIME_CLONEABLE_TOP_COMPONENT}
         * if the <code>tc</code> is 
         * instance of <code>TopComponent.Cloneable</code> */
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {
                new DataFlavor(MIME_TOP_COMPONENT_ARRAY, null),
            };
        }

        /** Implements <code>Transferable</code> method.
         * @return <code>true</code> for <code>DataFlavor</code> with mimetype
         * equal to {@link #MIME_TOP_COMPONENT}
         * and if <code>tc</code> is instance
         * of <code>TopComponent.Cloneable</code> also for the one
         * with mimetype {@link #MIME_TOP_COMPONENT_CLONEABLE},
         * <code>false</code> otherwise. */
        public boolean isDataFlavorSupported(DataFlavor df) {
            if(MIME_TOP_COMPONENT_ARRAY.equals(
            df.getMimeType())) {
                return true;
            }

            return false;
        }
        // << Transferable implementation <<
    } // End of class TopComponentArrayTransferable.

    
    /** Fake <code>DragGestureRecognizer</code> used when starting
     * DnD programatically. */
    private static class FakeDragGestureRecognizer extends DragGestureRecognizer {

        /** Constructs <code>FakeDragGestureRecpgnizer</code>.
         * @param evt trigger event */
        public FakeDragGestureRecognizer(WindowDnDManager windowDnDManager, MouseEvent evt) {
            super(windowDnDManager.getWindowDragSource(),
                (Component)evt.getSource(), DnDConstants.ACTION_COPY_OR_MOVE, null);

            appendEvent(evt);
        }

        /** Dummy implementation of superclass abstract method. */
        public void registerListeners() {}
        /** Dummy implementation of superclass abstract method. */
        public void unregisterListeners() {}
        
    } // End of class FakeDragGestureRecognizer

    
    /**
     * Ugly fake class to pass by the issue #4752224. There is not possible
     * to create DataFlavor of mime type application/x-java-jvm-local-objectref 
     * for array class type. */
    static class TopComponentArray {
    } // End of TopComponentArray.
    
}
