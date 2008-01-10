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

package org.netbeans.modules.bpel.mapper.multiview;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModelFactory;
import org.netbeans.modules.bpel.mapper.model.GraphExpandProcessor;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTcContext;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.xml.xam.Model.State;
import org.openide.nodes.Node;
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
    private boolean isMapperShown;
    
    // Fields which hold parameters for the UpdateProcessor
    private BpelDesignContext mNewContext;
//    private boolean mNeedReload;
    private MapperStateManager myMapperStateManager;

    private static final int ACTION_NODE_CHANGE_TASK_DELAY = 200;
    private transient RequestProcessor.Task myPreviousTask;
    
    private WeakReference<Object> mBpelModelUpdateSourceRef;
    
    /**
     * The lookup of mapper Tc contains BPELDataObject and BpelModel
     * @param mapperTc 
     */
    public DesignContextControllerImpl2(TopComponent mapperTc) {
        assert mapperTc != null;
        assert mapperTc instanceof MapperTcContext;
        
        mMapperTcContext = (MapperTcContext)mapperTc;
        mBpelModelSynchListener = new BpelModelSynchListener(this);
        
        myBpelModel = mapperTc.getLookup().lookup(BpelModel.class);
        assert myBpelModel != null;
        
        TopComponent.getRegistry().addPropertyChangeListener(this);
        
        myBpelModel.addEntityChangeListener(mBpelModelSynchListener);
        initContext();
    }
    
    public void cleanup() {
        TopComponent.getRegistry().removePropertyChangeListener(this);
        myBpelModel.removeEntityChangeListener(mBpelModelSynchListener);
        myBpelModel = null;
        mMapperTcContext = null;
    }
    
    public MapperTcContext getMapperTcContext() {
        return mMapperTcContext;
    }
    
    public synchronized void setBpelModelUpdateSource(Object source) {
        mBpelModelUpdateSourceRef = new WeakReference<Object>(source);
    }
    
    private synchronized Object getBpelModelUpdateSource() {
        if (mBpelModelUpdateSourceRef != null) {
            return mBpelModelUpdateSourceRef.get();
        }
        //
        return null;
    }
    
    public synchronized BpelDesignContext getContext() {
        return mContext;
    }
    
    private synchronized void initContext() {
        setContext(DesignContextChangeListener.getActivatedContext(myBpelModel));
        myMapperStateManager = new MapperStateManager(mMapperTcContext);
    }
    
    public synchronized void setContext(BpelDesignContext newContext) {
        if (newContext == null) {
            return;
        }
        BpelEntity newEntity = newContext.getBpelEntity();
        // avoid entities from another BpelModel
        if ((newEntity != null && !myBpelModel.equals(newEntity.getBpelModel())) 
                || newEntity == null) 
        {
            return;
        }

        BpelEntity oldEntity = mContext != null ? mContext.getBpelEntity() : null;
        UniqueId oldEntityUid = oldEntity != null ? oldEntity.getUID() : null;
        UniqueId newEntityUid = newEntity.getUID();
        // the context doesn't changed
        if (newEntityUid.equals(oldEntityUid)) {
            return;
        }
        
        setActivatedNodes(newContext.getActivatedNode());
        //
        mNewContext = newContext;

        if (isMapperShown) {
            setContextImpl();
        }
    }
    
    /**
     * works in swing thread only
     * 
     */
    private void setActivatedNodes(final Node aNode) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if ( aNode != null && mMapperTcContext != null) {

                    TopComponent mapperTc = mMapperTcContext.getTopComponent();
                    if (mapperTc != null) {
                        mapperTc.setActivatedNodes(new Node[] {aNode});
                    }
                }
            }
        });
    }
    
    public synchronized void reloadMapper(ChangeEvent event) {
        //
        // Ignore reload if is has been initiated by the mapper itself 
        if (event.getSource() == getBpelModelUpdateSource()) {
            return;
        }
        
        if (isMapperShown) {
            reloadMapperImpl();
        }
    }
    
    private void setMapperModel(MapperModel newMapperModel) {
        mMapperTcContext.setMapperModel(newMapperModel);
    }
    
        public void reloadMapperImpl() {
            if (mContext == null) {
                return;
            }
            //
            if (isModelInvalid()) {
                disableMapper();
                return;
            }
            MapperModel newMapperModel = new BpelMapperModelFactory().
                    constructModel(mMapperTcContext, mContext);
            
            myMapperStateManager.storeOldEntityContext(mContext);
            setMapperModel(newMapperModel);
            myMapperStateManager.restoreOldEntityContext(mContext);
        }
    
        public void setContextImpl() {
            // Copy the context to a new local variable at first.
            BpelDesignContext newContext = mNewContext;
            //
            if (newContext == null || newContext.getBpelEntity() == null) {
                // Hide the mapper if there is not a BPEL entity selected
                disableMapper();
                return;
            } 
            //
            if  (isModelInvalid()) {
                disableMapper();
                return;
            } 
            //
            if (!newContext.equals(mContext)) {
                myMapperStateManager.storeOldEntityContext(mContext);
                BpelEntity contextEntity = newContext.getBpelEntity();
                boolean needShow = 
                        BpelMapperModelFactory.needShowMapper(contextEntity);
                //
                if (!needShow) {
                    disableMapper();
                } else {
                    //
                    MapperModel newMapperModel = new BpelMapperModelFactory().
                            constructModel(mMapperTcContext, newContext);
                    //
                    mContext = newContext;
                    setMapperModel(newMapperModel);
                    myMapperStateManager.restoreOldEntityContext(mContext);
                    //
                    GraphExpandProcessor.expandGraph(mMapperTcContext, mContext);
                    //
                    mMapperTcContext.showMapperTcGroup(true);
                }
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

        private void disableMapper() {
            mMapperTcContext.showMapperTcGroup(false);
            mMapperTcContext.setMapper(null);
            //
            mContext = null;
        }

        private boolean isModelInvalid() {
            BpelModel bpelModel = myBpelModel;
            if (bpelModel != null) {
                return bpelModel.getState().equals(State.NOT_WELL_FORMED);
            }
            return false; // Consider the model valid by default
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

    private void updateContext() {
        if (myPreviousTask != null) {
            myPreviousTask.cancel();
        }
        if (myPreviousTask != null && !myPreviousTask.isFinished()) {
            myPreviousTask.waitFinished();
            myPreviousTask = null;
        }

        myPreviousTask = RequestProcessor.getDefault().post(
                new Runnable() {
            public void run() {
                BpelDesignContext newBpelContext = DesignContextChangeListener.getActivatedContext(myBpelModel);
                setContext(newBpelContext);
            }
        }, ACTION_NODE_CHANGE_TASK_DELAY);
        
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
        setContextImpl();
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
}
