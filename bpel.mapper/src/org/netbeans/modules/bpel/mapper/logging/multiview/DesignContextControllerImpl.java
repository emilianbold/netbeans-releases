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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.EventObject;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.modules.bpel.mapper.logging.model.LoggingMapperModelFactory;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.multiview.BpelModelSynchListener;
import org.netbeans.modules.bpel.mapper.multiview.DesignContextController;
import org.netbeans.modules.bpel.mapper.tree.TreeExpandedState;
import org.netbeans.modules.bpel.mapper.tree.TreeExpandedStateImpl;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.xml.xam.Model.State;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

/**
 * Controls the state of the BPEL mapper and manages different events:
 * - change of activated node 
 * - change in the related BPEL model
 * 
 * @author nk160297
 * @author Vitaly Bychkov
 *
 */
public class DesignContextControllerImpl implements DesignContextController {

    private static int RELOAD_DELAY = 100;
    private static int NODE_UPDATE_DELAY = 200;
    
    private BpelDesignContext mContext;
    
    private MapperTcContext mMapperTcContext;
    private BpelModelSynchListener mBpelModelSynchListener;
    
    // A new instance of ReloadProcessor is created for each timer event
    // The field holds the last created processor. 
    // It is intended to allow the process to be interrupted.
    private ReloadProcessor mReloadProcessor;
    
    private Timer reloadTimer;
    private Timer nodeUpdateTimer;
    
    // Fields which hold parameters for the UpdateProcessor
    private BpelDesignContext mNewContext;
    private boolean mNeedReload;
    
    private WeakReference<Object> mBpelModelUpdateSourceRef;
    
    /** Creates a new instance of DesignContextChangeListener */
    public DesignContextControllerImpl(MapperTcContext mapperTcContext) {
        mMapperTcContext = mapperTcContext;
        mBpelModelSynchListener = new BpelModelSynchListener(this);
        //
        reloadTimer = new Timer(RELOAD_DELAY, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                // Create new instance of UpdateProcessor
                ReloadProcessor reloadProcessor = new ReloadProcessor();
                synchronized(DesignContextControllerImpl.this) {
                    mReloadProcessor = reloadProcessor;
                }
                //
                reloadProcessor.reloadMapperImpl();
                //
                if (!(reloadProcessor.isInterrupted())) {
                    // Discard the flag in case the processor 
                    // hasn't been interrupted.
                    // The flag has to be discarded here!
                    synchronized(DesignContextControllerImpl.this) {
                        mNeedReload = false;
                    }
                }
            }
        });
        reloadTimer.setRepeats(false);
        //
        nodeUpdateTimer = new Timer(NODE_UPDATE_DELAY, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                // Create new instance of UpdateProcessor
                ReloadProcessor reloadProcessor = new ReloadProcessor();
                synchronized(DesignContextControllerImpl.this) {
                    mReloadProcessor = reloadProcessor;
                }
                //
                reloadProcessor.setContextImpl();
                //
                if (!(reloadProcessor.isInterrupted())) {
                    // Discard the flag in case the processor 
                    // hasn't been interrupted.
                    // The flag has to be discarded here!
                    synchronized(DesignContextControllerImpl.this) {
                        mNeedReload = false;
                    }
                }
            }
        });
        nodeUpdateTimer.setRepeats(false);
    }
    
    public MapperTcContext getMapperTcContext() {
        return mMapperTcContext;
    }
    
    public void setBpelModelUpdateSource(Object source) {
        mBpelModelUpdateSourceRef = new WeakReference<Object>(source);
    }
    
    private synchronized Object getBpelModelUpdateSource() {
        if (mBpelModelUpdateSourceRef == null) {
            // Mapper is the default synchronization source
            return mMapperTcContext.getMapper();
        } else {
            return mBpelModelUpdateSourceRef.get();
        }
    }
    
    public synchronized BpelDesignContext getContext() {
        return mContext;
    }
    
    public synchronized void setContext(BpelDesignContext newContext) {
        if (newContext == null) {
            return;
        }
        Node aNode = newContext.getActivatedNode();
        if ( aNode != null && mMapperTcContext != null) {
            TopComponent mapperTc = mMapperTcContext.getTopComponent();
            if (mapperTc != null) {
                mapperTc.setActivatedNodes(new Node[] {aNode});
            }
        }
        
        BpelModel model = getCurrBpelModel();
//        BpelEntity newEntity = newContext.getBpelEntity();
        BpelEntity newEntity = newContext.getSelectedEntity();
        // avoid entities from another BpelModel
        if ((model != null && newEntity != null 
                && !model.equals(newEntity.getBpelModel())) || newEntity == null) 
        {
            return;
        }
        
        reloadTimer.stop();
        nodeUpdateTimer.stop();
        //
        if (mReloadProcessor != null) {
            mReloadProcessor.interrupt();
        }
        //
        mNewContext = newContext;
        //
        nodeUpdateTimer.start();
    }
    
    public synchronized void reloadMapper(EventObject event) {
        //
        // Ignore reload if is has been initiated by the mapper itself 
        if (event.getSource() == getBpelModelUpdateSource()) {
            return;
        }
        //
        if (!nodeUpdateTimer.isRunning()) {
            reloadTimer.stop();
            //
            if (mReloadProcessor != null) {
                mReloadProcessor.interrupt();
            }
            //
            reloadTimer.start();
        }
        //
        // The flag has to be set at the end of the method!
        mNeedReload = true;
    }
    
    private synchronized BpelModel getCurrBpelModel() {
        if (mContext != null) {
            return mContext.getBpelModel();
        }
        return null;
    }
    
    private void setMapperModel(MapperModel newMapperModel) {
        mMapperTcContext.setMapperModel(newMapperModel);
    }
    
    /**
     * Processes events from reload and node update timers and 
     * reloads the mapper content.
     */
    private class ReloadProcessor {

        private boolean isInterrupted = false;
        
        /**
         * Sets isInterrupted flag. 
         */
        public void interrupt() {
            isInterrupted = true;
        }
        
        public boolean isInterrupted() {
            return isInterrupted;
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
            //
            // No need to resubscribe to another BPEL model here 
            // because of the same design context.
            //
            MapperModel newMapperModel = new LoggingMapperModelFactory(mMapperTcContext, 
                mContext).constructModel();
            //
            // Save left tree state
            TreeExpandedState leftTreeState = null;
            Mapper mapper = mMapperTcContext.getMapper();
            if (mapper != null) {
//                JTree leftTree = mapper.getLeftTree();
//                if (leftTree != null) {
                    leftTreeState = new TreeExpandedStateImpl(mapper);
                    leftTreeState.save();
//                }
            }
            //
            setMapperModel(newMapperModel);
            //
            // Restore left tree state
            final TreeExpandedState state = leftTreeState;
            if (state != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        state.restore();
                    }
                });
                // TODO: restore tree selection, restore right tree, expanded graph
            }
        }
    
        public void setContextImpl() {
            // Copy the context to a new local variable at first.
            BpelDesignContext newContext = mNewContext;
            //
            if (newContext == null || newContext.getSelectedEntity() == null) {
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
                BpelEntity contextEntity = newContext.getSelectedEntity();
                boolean needShow = 
                        LoggingMapperModelFactory.needShowMapper(contextEntity);
                //
                if (!needShow) {
                    disableMapper();
                } else {
                    //
                    // Re subscribe to another BPEL model if necessary.
                    if (newContext.getBpelModel() != getCurrBpelModel()) {
                        setListenBpelModel(getCurrBpelModel(), false);
                        setListenBpelModel(newContext.getBpelModel(), true);
                    }
                    //
                    MapperModel newMapperModel = new LoggingMapperModelFactory(mMapperTcContext, 
                        newContext).constructModel();
                    //
                    mContext = newContext;
                    setMapperModel(newMapperModel);
                    //
                    mMapperTcContext.showMapperTcGroup(true);
                }
            } else {
                //
                boolean needReload;
                synchronized(DesignContextControllerImpl.this) {
                    needReload = mNeedReload;
                }
                if (needReload) {
                    reloadMapperImpl();
                }
            }
        }

        private void setListenBpelModel(BpelModel bpelModel, boolean isEnabled) {
            if (bpelModel == null) {
                return;
            }
            //
            if (isEnabled) {
                if (mBpelModelSynchListener != null) {
                    bpelModel.addEntityChangeListener(mBpelModelSynchListener);
                }
            } else {
                if (mBpelModelSynchListener != null) {
                    bpelModel.removeEntityChangeListener(mBpelModelSynchListener);
                }
            }
        }

        private void disableMapper() {
            mMapperTcContext.showMapperTcGroup(false);
            mMapperTcContext.setMapper(null);
            setListenBpelModel(getCurrBpelModel(), false);
            //
            mContext = null;
        }

        private boolean isModelInvalid() {
            BpelModel bpelModel = getCurrBpelModel();
            if (bpelModel != null) {
                return bpelModel.getState().equals(State.NOT_WELL_FORMED);
            }
            return false; // Consider the model valid by default
        }
        
    }

    public void showMapper() {
        // do nothing
    }

    public void hideMapper() {
        // do nothing
    }

    public void cleanup() {
        //TODO a
    }

    public void processDataObject(Object dataObject) {
        // Do nothing. This DCC implementation is never used
    }
}
