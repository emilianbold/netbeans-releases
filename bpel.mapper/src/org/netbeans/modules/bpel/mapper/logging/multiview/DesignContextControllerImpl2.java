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
package org.netbeans.modules.bpel.mapper.logging.multiview;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.EventObject;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.mapper.logging.model.LoggingMapperModelFactory;
import org.netbeans.modules.bpel.mapper.model.GraphExpandProcessor;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.multiview.BpelModelSynchListener;
import org.netbeans.modules.bpel.mapper.multiview.DesignContextController;
import org.netbeans.modules.bpel.mapper.multiview.DesignContextUtil;
import org.netbeans.modules.bpel.mapper.multiview.MapperMultiviewElement;
import org.netbeans.modules.bpel.mapper.multiview.MapperStateManager;
import org.netbeans.modules.bpel.mapper.multiview.ShowMapperCookie;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.Nameable;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 * Controls the state of the BPEL mapper and manages different events:
 * - change of activated node (correspondent mapper TC activated nodes)
 * - change of mapper Tc lifecycle state (intrested in mapperTc showing, hidden, closed )
 * - change in the related BPEL model
 * - change the state of BPEL model
 *
 * @author nk160297
 * @author Vitaly Bychkov
 *
 */
public class DesignContextControllerImpl2
        implements DesignContextController, PropertyChangeListener
{

    private BpelDesignContext mContext;

    private MapperTcContext mMapperTcContext;
    private BpelModelSynchListener mBpelModelSynchListener;

    private BpelModel myBpelModel;
    // flag property for MapperTC showing/hidding states
    private boolean isMapperShown;
    private BpelDesignContext mNewContext;
    private MapperStateManager myMapperStateManager;

    private static final int ACTION_NODE_CHANGE_TASK_DELAY = 150;
    private transient RequestProcessor.Task myPreviousTask;
    private int myDelay = ACTION_NODE_CHANGE_TASK_DELAY;

    private WeakReference<Object> mBpelModelUpdateSourceRef;

    /**
     * The lookup of mapper Tc must contain BPELDataObject, BpelModel and ShowMapperCookie
     * @param mapperTc
     */
    public DesignContextControllerImpl2(TopComponent mapperTc) {
        assert mapperTc != null;
        assert mapperTc instanceof MapperTcContext;

        mMapperTcContext = (MapperTcContext)mapperTc;
        myBpelModel = mapperTc.getLookup().lookup(BpelModel.class);
        assert myBpelModel != null;
        mBpelModelSynchListener = new BpelModelSynchListener(this);
        mBpelModelSynchListener.register(myBpelModel);

        TopComponent.getRegistry().addPropertyChangeListener(this);

        initContext();
    }

    public void cleanup() {
        TopComponent.getRegistry().removePropertyChangeListener(this);
        mBpelModelSynchListener.unregisterAll();
        myBpelModel = null;
        mMapperTcContext = null;
    }

    public synchronized void setBpelModelUpdateSource(Object source) {
        mBpelModelUpdateSourceRef = new WeakReference<Object>(source);
    }

    public synchronized BpelDesignContext getContext() {
        return mContext;
    }

    public synchronized void setContext(BpelDesignContext newContext) {
        assert EventQueue.isDispatchThread();
        setContext(newContext, false);
    }

    // context changes if selectedEntity changes
    public synchronized void setContext(BpelDesignContext newContext, boolean forceReload) {
        assert EventQueue.isDispatchThread();
        //
        
        if (mNewContext == null && forceReload) {
            reloadMapper(new EventObject(new Object()));
            return;
        }
        
        mNewContext = newContext;

        boolean isValidContext = DesignContextUtil.isValidContext(mContext);
        // null means unsupported context - in result the old context must be stored
        if (newContext == null && isValidContext) {
            return;
        }

        // todo m
        if (newContext == null && !isValidContext) {
            if (isMapperShown) {
                showNotValidContext(mContext);
            }
            return;
        }

        BpelEntity newEntity = newContext.getSelectedEntity();

        // avoid entities from another BpelModel
        if ((newEntity != null && !myBpelModel.equals(newEntity.getBpelModel()))
                || newEntity == null)
        {
            return;
        }

        // the context have to be updated just if context changes,
        // in case the bpel model changes doesn't need to update context
        if (newContext.equals(mContext) && !forceReload)
        {
            return;
        }

        setActivatedNodes(newContext == null ? null : newContext.getActivatedNode());

        if (isMapperShown) {
            setContextImpl(forceReload);
        }
    }

    // TODO m
    public void reloadMapper(EventObject event) {
        //
        // Ignore reload if it has been initiated by the mapper itself
        if (event.getSource() == getBpelModelUpdateSource()) {
            return;
        }

        if (myPreviousTask != null) {
            myPreviousTask.cancel();
        }
        if (myPreviousTask != null && !myPreviousTask.isFinished()
                && RequestProcessor.getDefault().isRequestProcessorThread()) // issue 125439
        {
            myPreviousTask.waitFinished();
            myPreviousTask = null;
        }

        if (!EventQueue.isDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    performReloadMapperInAwt();
                }
            });
        } else {
            performReloadMapperInAwt();
        }
    }

    synchronized void performReloadMapperInAwt() {
        assert EventQueue.isDispatchThread();
        if (!DesignContextUtil.isValidContext(mContext)) {
            setDelay(0);
            updateContext(-1, false);
            return;
        }

        if (isMapperShown) {
            reloadMapperImpl();
        }
    }

    private void setDelay(int delay) {
        myDelay = delay;
    }

    private int getDelay() {
        return myDelay;
    }

    // TODO m
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();

        if (propertyName.equals(TopComponent.Registry.PROP_ACTIVATED_NODES)) {
            updateContext();
        } else {
            // Other properties are not supported
            return;
        }
    }

    public void showMapper() {
        assert EventQueue.isDispatchThread();
        // TODO m
        isMapperShown = true;
        Mapper mapper = mMapperTcContext != null
            ? mMapperTcContext.getMapper() : null;
        if (mapper != null) {
            mapper.setVisible(true);
        }

        updateContext(-1, true);
//        setContextImpl(true);
    }

    public void hideMapper() {
        assert EventQueue.isDispatchThread();
        isMapperShown = false;
        Mapper mapper = mMapperTcContext != null
            ? mMapperTcContext.getMapper() : null;
        if (mapper != null) {
            mapper.setVisible(false);
        }
    }

    public MapperTcContext getMapperTcContext() {
        return mMapperTcContext;
    }

    private synchronized void initContext() {
        setContext(LoggingDesignContextFactory.getInstance().getActivatedContext(myBpelModel));
        myMapperStateManager = new MapperStateManager(mMapperTcContext);
    }

    private void updateContext(int delay, boolean forceReload) {
        assert EventQueue.isDispatchThread();
        if (delay <= 0) {
            setDelay(ACTION_NODE_CHANGE_TASK_DELAY);
        }

        if (myPreviousTask != null) {
            myPreviousTask.cancel();
        }
        if (myPreviousTask != null && !myPreviousTask.isFinished()
                && RequestProcessor.getDefault().isRequestProcessorThread()) // issue 125439
        {
            myPreviousTask.waitFinished();
            myPreviousTask = null;
        }

        if (delay <= 0) {
            setContext(LoggingDesignContextFactory.getInstance().getActivatedContext(myBpelModel), forceReload);
        } else {
            myPreviousTask = RequestProcessor.getDefault().post(
                    new Runnable() {
                public void run() {
    //                BpelDesignContext newBpelContext = DesignContextChangeListener.getActivatedContext(myBpelModel);
                    SwingUtilities.invokeLater(new  Runnable() {
                        public void run() {
                            setContext(LoggingDesignContextFactory.getInstance().getActivatedContext(myBpelModel));
                        }
                    });
                }
            }, delay);
        }
    }

    // todo m
    private void updateContext() {
        updateContext(getDelay(), false);
    }

    // TODO m correct behaviour if just selectedEntity changes
    private void setContextImpl() {
        setContextImpl(false);
    }

    private void setContextImpl(boolean forceReload) {
        // Copy the context to a new local variable at first.
        BpelDesignContext newContext = mNewContext;
        //
        if (newContext == null) {
            // do nothing - simple continue to show the old context
            return;
        }

        if  (isModelInvalid()) {
            myMapperStateManager.storeOldEntityContext(mContext);
            // hide mapper if BpelModel is invalid
            showModelIsInvalid();
            return;
        }
        //
        BpelEntity oldContextEntity = mContext != null ? mContext.getContextEntity() : null;
        BpelEntity newContextEntity = newContext.getContextEntity();
        if (newContextEntity == null) {
            myMapperStateManager.storeOldEntityContext(mContext);
            // Hide the mapper if unmappable BPEL entity is selected
            showUnsupportedEntity(newContext);
            return;
        }
        //

        if (forceReload || !newContext.equals(mContext)) {
            if (forceReload || !newContextEntity.equals(oldContextEntity)) {
                myMapperStateManager.storeOldEntityContext(mContext);
                //
                MapperModel newMapperModel = new LoggingMapperModelFactory(mMapperTcContext, 
                    newContext).constructModel();
                //

                mContext = newContext;
                setMapperModel(newMapperModel);
                myMapperStateManager.restoreOldEntityContext(mContext);
                //
            }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    GraphExpandProcessor.expandGraph(mMapperTcContext, mContext);
                }
            });
            //
//            mMapperTcContext.showMapperTcGroup(true);
        }
/*
        else {
            //
            boolean needReload;
            synchronized(DesignContextControllerImpl2.this) {
                needReload = mNeedReload;
            }
            if (needReload) {
                reloadMapperImpl();
            }
        }
*/
    }

    private void reloadMapperImpl() {
        if (mContext == null) {
            return;
        }
        //
        if (isModelInvalid()) {
            myMapperStateManager.storeOldEntityContext(mContext);
            showModelIsInvalid();
            return;
        }

//        if (!isValidContext(mContext)) {
//            updateContext();
//            return;
//        }
//
//
        MapperModel newMapperModel = new LoggingMapperModelFactory(mMapperTcContext, 
            mContext).constructModel();

        myMapperStateManager.storeOldEntityContext(mContext);
        setMapperModel(newMapperModel);
        myMapperStateManager.restoreOldEntityContext(mContext);
    }

    private synchronized Object getBpelModelUpdateSource() {
        if (mBpelModelUpdateSourceRef == null) {
            // Mapper is the default synchronization source
            return mMapperTcContext.getMapper();
        } else {
            return mBpelModelUpdateSourceRef.get();
        }
    }

    /**
     * works in swing thread only
     *
     */
    private void setActivatedNodes(final Node aNode) {
        assert EventQueue.isDispatchThread();
        if (mMapperTcContext != null) {

            TopComponent mapperTc = mMapperTcContext.getTopComponent();
            if (mapperTc != null) {
                if (aNode != null) {
                    mapperTc.setActivatedNodes(new Node[] {aNode});
                } else {
                    mapperTc.setActivatedNodes(new Node[0]);
                }
            }
        }
    }

    private void setMapperModel(MapperModel newMapperModel) {
        assert newMapperModel != null;
        mMapperTcContext.setMapperModel(newMapperModel);
    }

    private void showModelIsInvalid() {
        disableMapper(NbBundle.getMessage(MapperMultiviewElement.class, "LBL_LoggingInvalid_BpelModel")); // NOI18N
    }

    private void showUnsupportedEntity(BpelDesignContext context) {
        assert context != null;
        String entityName = null;
        Node node = context.getActivatedNode();
        entityName = node != null ? node.getDisplayName() : null;
        if (entityName == null) {
            BpelEntity entity = context.getSelectedEntity();
            entityName = entity instanceof Nameable ? ((Nameable)entity).getName() : EditorUtil.getTagName(entity);
        }
        entityName = entityName == null ? "" : entityName;
        disableMapper(NbBundle.getMessage(MapperMultiviewElement.class,
                                            "LBL_LoggingEmptyMapper", entityName)); // NOI18N
    }

    private void showNotValidContext(BpelDesignContext context) {
        assert context != null;
        String entityName = null;
        Node node = context.getActivatedNode();
        entityName = node != null ? node.getDisplayName() : null;
        if (entityName == null) {
            BpelEntity entity = context.getSelectedEntity();
            entityName = entity instanceof Nameable ? ((Nameable)entity).getName() : EditorUtil.getTagName(entity);
        }
        entityName = entityName == null ? "" : entityName;
        disableMapper(NbBundle.getMessage(MapperMultiviewElement.class,
                                            "LBL_LoggingInValidMapperContext", entityName)); // NOI18N
    }

    private void disableMapper(String message) {
        TopComponent tc = mMapperTcContext.getTopComponent();
        if ( tc == null) {
            return;
        }

        ShowMapperCookie showCookie = tc.getLookup().lookup(ShowMapperCookie.class);
        if (showCookie != null) {
            showCookie.show(message);
        }

        mContext = null;
    }

    private boolean isModelInvalid() {
        BpelModel bpelModel = myBpelModel;
        if (bpelModel != null) {
            return bpelModel.getState().equals(State.NOT_WELL_FORMED);
        }
        return false; // Consider the model valid by default
    }

    public void processDataObject(Object dataObject) {
        mBpelModelSynchListener.processDataObject(dataObject);
    }
}
