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

package org.netbeans.modules.soa.mapper.basicmapper;

import java.awt.Point;
import java.awt.dnd.DragSourceContext;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.SwingUtilities;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoPort;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.AbstractCanvasView;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.BasicCanvasController;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.BasicCanvasSimpleLink;
import org.netbeans.modules.soa.mapper.basicmapper.tree.DestTreeViewController;
import org.netbeans.modules.soa.mapper.basicmapper.tree.SourceTreeViewController;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicController;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapperModel;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicViewController;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicViewManager;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasLink;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.dnd.IBasicDragController;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IBasicTreeViewSelectionPathController;
import org.netbeans.modules.soa.mapper.common.IMapperEvent;
import org.netbeans.modules.soa.mapper.common.IMapperListener;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMapperLink;

/**
 * <p>
 *
 * Title: Basic Mapper Controller </p> <p>
 *
 * Description: The mapper controller provide functionalities of
 * IMapperController. This controller contains its own Thread to dispatch
 * MapperEvent. Caller should not execute dispatchEvent directly, although doing
 * so will not harm anything. To dispatchEvent, post the event into
 * IMapperViewManager.postMapperEvent. </p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public class BasicMapperController
     implements IBasicController, IBasicDragController {

    /**
     * the maxmium period of seconds to run the event runner. It will yield
     * AFTER the period.
     */
    private static final int MAX_SECONDS_INVOKED_BEFORE_YIELD = 3;

    /**
     * The event queue, from view manager.
     */
    private List mEventQueue;

    /**
     * the event thread executor.
     */
    private EventRunner mEventRunner;

    /**
     * the storage of mapper listener that listens to all event type.
     */
    private List mListenersList;

    /**
     * the storage of mapper listener that listens to specified event type.
     * EventType is the key, and a List, that contains listeners, is the value.
     */
    private Map mSourceListenersMap;

    /**
     * the view manager of the mapper.
     */
    private IBasicViewManager mViewManager;

    /**
     * the dest tree controller.
     */
    private DestTreeViewController mDestTreeController;

    /**
     * the source tree controller
     */
    private SourceTreeViewController mSourceTreeController;

    /**
     * the canvas view controller
     */
    private BasicCanvasController mCanvasController;

    private Object mTransferObject;
    
    private IMapperNode mOriginatingDragNode;
    
    private BasicCanvasSimpleLink mDragLink;
    
    private IBasicTreeViewSelectionPathController mDragLinkSourceController;
    
    private DragSourceContext mLinkDragSourceContext;
    
    
    
    /**
     * Event runner is a thread specially design to dispatch mapper event. Since
     * there is no infomation about how long a listener method will be executed,
     * it provides a functionality to continue execute for a specified period of
     * time and yield to other threads. Specifying a zero or less than zero
     * period indicates no yield. The default is not to yeild.
     *
     * @author    Un Seng Leong
     * @created   December 4, 2002
     * @version   1.0
     */
    private class EventRunner
         extends Thread {
        /**
         * a place holder for start time.
         */
        private long mStartTime = 0;

        /**
         * the period of time to be continusely run without yeild, in millis
         * seconds.
         */
        private long mMillisSecondsRunBeforeYield = 0;

        /**
         * flag indicates should exist run method.
         */
        private boolean mShouldStop = false;

        /**
         * Contruct a event runner with specified the period time of running, in
         * seconds.
         *
         * @param secondsRunBeforeYield  the seconds to run without yeild.
         */
        private EventRunner(int secondsRunBeforeYield) {
            super("Mapper Controller Event Invoker");
            mMillisSecondsRunBeforeYield = secondsRunBeforeYield * 1000;
        }

        /**
         * Dispath event. This method getn a event from event queue. If no event
         * available, it waits. Then it start its timer and call dispatchEvent.
         */
        public void run() {
            while (!mShouldStop) {
                IMapperEvent event = null;
                synchronized (mEventQueue) {
                    while (mEventQueue.isEmpty()) {
                        try {
                            mEventQueue.wait();
                        } catch (InterruptedException i) {
                            if (mShouldStop) {
                                return;
                            }
                        }
                    }

                    event = (IMapperEvent) mEventQueue.remove(0);
                }
                startTimer();
                dispatchEvent(event);
            }
        }

        /**
         * Set the flag to ask the thread to stop running.
         *
         * @param shouldStop  flag that indicates stop or not.
         */
        private void setShouldStop(boolean shouldStop) {
            mShouldStop = shouldStop;
        }

        /**
         * Return true is the thread should stop running, that means exist the
         * run method normally.
         *
         * @return   true if this thread should stop running
         */
        private boolean shouldStop() {
            return mShouldStop;
        }

        /**
         * Return true if the continus running period is reached, false
         * otherwise. That means this thread should yield at any time after.
         *
         * @return   true if the running period is reached.
         */
        private boolean shouldYeild() {
            if (mMillisSecondsRunBeforeYield <= 0) {
                return false;
            }

            return (System.currentTimeMillis() - mStartTime) >= mMillisSecondsRunBeforeYield;
        }

        /**
         * Record the start time, in millis seconds.
         */
        private void startTimer() {
            mStartTime = System.currentTimeMillis();
        }
    }
    
    
    
    /**
     * Construct a controller with empty listeners.
     *
     * @param viewManager  Description of the Parameter
     */
    public BasicMapperController(IBasicViewManager viewManager) {
        super();
        mListenersList = new Vector();
        mSourceListenersMap = new Hashtable();
        mViewManager = viewManager;

        mEventQueue = viewManager.getEventQueue();
        mEventRunner = new EventRunner(MAX_SECONDS_INVOKED_BEFORE_YIELD);
        mEventRunner.start();

        mDestTreeController = new DestTreeViewController();
        mDestTreeController.setView(mViewManager.getDestView());
        mDestTreeController.setMapperController(this);
        mDestTreeController.setMapperDragController(this);

        mSourceTreeController = new SourceTreeViewController();
        mSourceTreeController.setView(mViewManager.getSourceView());
        mSourceTreeController.setMapperController(this);
        mSourceTreeController.setMapperDragController(this);

        mCanvasController = new BasicCanvasController();
        mCanvasController.setView(mViewManager.getCanvasView());
        mCanvasController.setMapperController(this);
        mCanvasController.setMapperDragController(this);
        mViewManager.getCanvasView().getCanvas().setCanvasController(mCanvasController);
    }

    /**
     * Return the mapper model of this controller holds.
     *
     * @return   the mapper model of this controller holds.
     */
    public IBasicMapperModel getMapperModel() {
        return getViewManager().getMapperModel();
    }

    /**
     * Return the view manager of this controller holds.
     *
     * @return   the view manager of this controller holds.
     */
    public IBasicViewManager getViewManager() {
        return mViewManager;
    }

    /**
     * Return the destinated tree view controller.
     *
     * @return   the destinated tree view controller.
     */
    public IBasicViewController getDestViewController() {
        return mDestTreeController;
    }

    /**
     * Return the source tree view controller.
     *
     * @return   the source tree view controller.
     */
    public IBasicViewController getSourceViewController() {
        return mSourceTreeController;
    }

    /**
     * Return the canvas view controller.
     *
     * @return   the canvas view controller.
     */
    public IBasicViewController getCanvasViewController() {
        return mCanvasController;
    }

    /**
     * Add a mapper listener to listening to mapper events.
     *
     * @param listener  the mapper listener to be added.
     */
    public void addMapperListener(IMapperListener listener) {
        mListenersList.add(listener);
    }

    /**
     * Add a mapper listener to listening to mapper events of a specified event
     * type.
     *
     * @param listener   the mapper listener to be added
     * @param eventType  the specified event type to listen to
     */
    public void addMapperListener(IMapperListener listener, String eventType) {
        List sourceListenersList = null;

        if (mSourceListenersMap.containsKey(eventType)) {
            sourceListenersList = (List) mSourceListenersMap.get(eventType);
        } else {
            sourceListenersList = new Vector();
            mSourceListenersMap.put(
                eventType,
                sourceListenersList);
        }

        sourceListenersList.add(listener);
    }

    /**
     * Dispatch the specified mapper event to register listener. Caller should
     * not call this method directly, but does no harm to do so. To regiester a
     * event, use IMapperViewManager.postMapperEvent.
     *
     * @param event  the mapper event to be dispatched.
     */
    public void dispatchEvent(IMapperEvent event) {
        List listenerList =
            (List) mSourceListenersMap.get(event.getEventType());

        if ((listenerList != null) && !listenerList.isEmpty()) {
            invokeListeners(listenerList, event);
        }

        if (!mListenersList.isEmpty()) {
            invokeListeners(mListenersList, event);
        }
    }

    /**
     * Remove a mapper listener from this mapper.
     *
     * @param listener  the mapper listener to be removed.
     */
    public void removeMapperListener(IMapperListener listener) {
        mListenersList.remove(listener);
    }

    /**
     * Remove a mapper listener that listen to a specified event type.
     *
     * @param listener   the mapper listener to be removed
     * @param eventType  the specified event type object to listen to
     */
    public void removeMapperListener(
        IMapperListener listener,
        String eventType) {
        if (mSourceListenersMap.containsKey(eventType)) {
            ((List) mSourceListenersMap.get(eventType)).remove(listener);
        }
    }

    /**
     * Stop the event thread.
     */
    protected void finalize() {
        stopDispatch();
    }

    /**
     * Forcing the dispatching event thread to stop.
     */
    public void stopDispatch() {
        mEventRunner.setShouldStop(true);
        mEventRunner.interrupt();
    }

    public void releaseControl() {
        stopDispatch();
        mListenersList.clear();
        mSourceTreeController.getDnDHandler().releaseHandler();
        mDestTreeController.getDnDHandler().releaseHandler();
    }

    /**
     * Convinence method for event runner to fire event to a list of listeners.
     * Listeners should not be null and empty. This method also works with event
     * runner to yeild and ask-stop. When one listener throws exception, this
     * method igrone it (currectly print it to system.err) and continue
     * dispatching to next listener.
     *
     * @param listeners  the listeners that receives the event.
     * @param event      the event to be dispatched
     */
    private void invokeListeners(List xlisteners, 
                                 final IMapperEvent event) {
        final List listeners = new LinkedList();
        listeners.addAll(xlisteners);
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    for (int i = listeners.size() - 1; i >= 0; i--) {
                        IMapperListener listener = (IMapperListener) listeners.get(i);

                        try {
                            listener.eventInvoked(event);
                        } catch (java.lang.Throwable t) {
                            System.err.println(
                                               Thread.currentThread().getName()
                                               + " caught Exception on MapperListener:"
                                               + listener.getClass().getName());
                            t.printStackTrace(System.err);
                        }
                        
                    }
                }
            });
    }


    public void setTransferObject(Object ob) {
        mTransferObject = ob;
    }
    
    public Object getTransferObject() {
        return mTransferObject;
    }
    
    /**
     * We keep track of the originating drag node mainly
     * for the purposes of cleaning it up after the drag is done.
     */
    public void setOriginatingDragNode(IMapperNode node) {
        mOriginatingDragNode = node;
    }
    
    /**
     * The originating drag node is used almost solely by the
     * canvas controller for its own purposes.
     */
    public IMapperNode getOriginatingDragNode() {
        return mOriginatingDragNode;
    }
    
    /**
     * Resets the canvas drag link end location to the start location.
     * This effectively makes the drag link invisible without actually
     * removing it from the canvas.
     */
    public void resetDragLinkEndLocation() {
        if (mDragLink != null) {
            JGoPort from = mDragLink.getFromPort();
            JGoPort to = mDragLink.getToPort();
            if (from.isValidDestination()) {
                // User dragged from a methoid input port on the canvas,
                // so we must switch the ports before we set the location.
                JGoPort tmp = to;
                to = from;
                from = tmp;
            }
            to.setLocation(from.getLocation());
        }
    }
    
    /**
     * This method sets the end location of the canvas drag link.
     * The input parameter to this method is the point of the current
     * mouse location (or some other arbitrary point, such as a tree node mid-point).
     */
    public void setDragLinkEndLocation(Point viewLocation) {
        ICanvasView canvasView = mViewManager.getCanvasView().getCanvas();

        if (mDragLinkSourceController != null) {
            // We set the from point because the user can, while dragging,
            // scroll through the mapper. This messes up our start point so
            // we need to reset this based upon the new location of the 
            // tree edge of the originating tree node.
            Point srcLocation = mDragLinkSourceController.getSelectionPathPoint();
            canvasView.convertViewToDoc(srcLocation);
            mDragLink.getFromPort().setLocation(srcLocation);
        } 
        
        Point location = new Point(viewLocation);
        canvasView.convertViewToDoc(location);
        if (mDragLink != null) {

            JGoPort jgoport = ((AbstractCanvasView) canvasView).pickNearestPort(location);
            if(jgoport != null) {
                // If our source controller is set, we're dragging from the tree.
                // In this case, we found a port on a methoid in the canvas that
                // we are attempting to connect to. Depending upon our type of tree,
                // we only connect to input or output ports. We must also determine
                // exactly where the link point is on the input/output port.

                if (mDragLinkSourceController instanceof SourceTreeViewController) {
                    if (jgoport.isValidDestination()) {
                        location = jgoport.getToLinkPoint(new Point(jgoport.getBoundingRect().x, jgoport.getBoundingRect().y));
                    }
                } else if (mDragLinkSourceController instanceof DestTreeViewController) {
                    if (jgoport.isValidSource()) {
                        location = jgoport.getFromLinkPoint(new Point(jgoport.getBoundingRect().x, jgoport.getBoundingRect().y));
                    }
                } else {
                }
            }
            JGoPort from = mDragLink.getFromPort();
            JGoPort to = mDragLink.getToPort();
            if (from.isValidDestination()) {
                // User dragged from a methoid input port on the canvas,
                // so we must switch the ports before we set the location.
                to = from;
            }
            to.setLocation(location);
        }
    }

    /**
     * DRAG FROM CANVAS:
     * Create a new drag link. This link will be drawn on the canvas.
     * One port will be connected to the methoid port that the drag
     * originated from, the other end will be wherever the mouse is.
     */
    public void setDragLink(ICanvasLink link) {
        mDragLink = (BasicCanvasSimpleLink)link;
        mDragLinkSourceController = null;
    }
    
    /**
     * DRAG FROM TREE:
     * Create a new drag link. This link will be drawn on the canvas.
     * One port will be connected to the edge of the tree that the
     * drag originated from, the other end will be wherever the mouse is.
     */
    public void setDragLink(Point viewLocation, IBasicTreeViewSelectionPathController dragLinkSourceController) {
        mDragLinkSourceController = dragLinkSourceController;
        ICanvasView canvasView = mViewManager.getCanvasView().getCanvas();
        
        Point location = new Point(viewLocation);
        canvasView.convertViewToDoc(location);
        
        JGoPort startPort = new JGoPort();
        startPort.setLocation(location);
        startPort.setToSpot(-1);
        startPort.setFromSpot(-1);
        
        JGoPort endPort = new JGoPort();
        endPort.setLocation(location);
        endPort.setToSpot(-1);
        endPort.setFromSpot(-1);
        
        mDragLink = new BasicCanvasSimpleLink(startPort, endPort);

        mDragLink.setPen(JGoPen.make(
                JGoPen.SOLID,
                1,
                ICanvasMapperLink.DEFAULT_LINK_SELECTED_COLOR));
        mDragLink.setBrush(JGoBrush.makeStockBrush(ICanvasMapperLink.DEFAULT_LINK_SELECTED_COLOR));

        canvasView.addRawLink(mDragLink);
    }
    
    public void clearDragLink() {
        ICanvasView canvasView = mViewManager.getCanvasView().getCanvas();
        canvasView.removeRawLink(mDragLink);
        mDragLink = null;
        mDragLinkSourceController = null;
    }

    public void setLinkDragSourceContext(DragSourceContext dsc) {
        // The DragSourceContext is set whenever a drag is detected, whether
        // we are dragging from the tree or the canvas.
        mLinkDragSourceContext = dsc;
    }
    
    public DragSourceContext getLinkDragSourceContext() {
        // The DragSourceContext allows access to the drag cursor.
        return mLinkDragSourceContext;
    }
}
