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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bpel.mapper.multiview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.EventObject;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.ui.UserNotification;
import org.netbeans.modules.soa.ui.nodes.InstanceRef;
import org.netbeans.modules.xml.xam.Model.State;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 * An abstract design context's controller. It manages state of the mapper and
 * processes different events:
 * - change of activated node (correspondent mapper TC activated nodes)
 * - change of mapper Tc lifecycle state (intrested in mapperTc showing, hidden, closed )
 * - change in the related BPEL, wsdl and schema models
 * - change the state of models
 *
 * It manages mapper's reloading and guarantees that the mapper shows correct and
 * up to date content.
 *
 * @author Nikita Krjukov
 * @author Vitaly Bychkov
 *
 */
public abstract class AbstractDccImpl
        implements BpelDesignContextController, PropertyChangeListener {

    private static final int ACTIVATED_NODE_CHANGED_DELAY = 1000;
    private static final int SOURCE_MODEL_CHANGED_DELAY = 800;
    
    protected BpelDesignContext mContext;

    protected MapperTcContext mMapperTcContext;
    private SourcesSynchListener mSourcesSynchListener;

    protected BpelModel mBpelModel;

    // Indicates if the mapper visible
    private boolean mIsVisible;

    // Indicates if the mapper is synchronized with the source models.
    private boolean mIsInSynch = true;

    private MapperStateManager mMapperStateManager;

    private RequestProcessor.Task mPreviousTask;

    private RequestProcessor mModelLoadingRProcessor;
    private RequestProcessor.Task mModelLoadingTask;

    private WeakReference<Object> mBpelModelUpdateSourceRef;

    /**
     * The lookup of mapper Tc must contain BPELDataObject, BpelModel and ShowMapperCookie
     * @param mapperTc
     */
    public AbstractDccImpl(TopComponent mapperTc) {
        assert mapperTc != null;
        assert mapperTc instanceof MapperTcContext;

        mMapperTcContext = (MapperTcContext)mapperTc;
        mBpelModel = mapperTc.getLookup().lookup(BpelModel.class);
        assert mBpelModel != null;
        mSourcesSynchListener = new SourcesSynchListener(this);
        mSourcesSynchListener.register(mBpelModel);
        TopComponent.getRegistry().addPropertyChangeListener(this);
        mMapperStateManager = new MapperStateManager(mMapperTcContext);
        mModelLoadingRProcessor = new RequestProcessor("Mapper Model Loading", 1, true);
        synchActivatedNodes();
    }

    public void cleanup() {
        TopComponent.getRegistry().removePropertyChangeListener(this);
        mSourcesSynchListener.unregisterAll();
        mBpelModel = null;
        mMapperTcContext = null;
        mContext = null;
        mMapperStateManager = null;
        mModelLoadingRProcessor = null;
    }

    public void processDataObject(Object dataObject) {
        mSourcesSynchListener.processDataObject(dataObject);
    }

    public synchronized void setBpelModelUpdateSource(Object source) {
        mBpelModelUpdateSourceRef = new WeakReference<Object>(source);
    }

    private synchronized Object getBpelModelUpdateSource() {
        if (mBpelModelUpdateSourceRef == null) {
            // Mapper static context is the default synchronization source
            return mMapperTcContext;
        } else {
            return mBpelModelUpdateSourceRef.get();
        }
    }

    public synchronized BpelDesignContext getContext() {
        return mContext;
    }

    public synchronized void invalidateMapper(EventObject event) {
        //
        // Ignore reload if it has been initiated by the mapper itself
        if (event.getSource() == getBpelModelUpdateSource()) {
            return;
        }
        //
        if (mIsVisible) {
            showMapperLoading(true);
            //
            if (mPreviousTask != null) {
                mPreviousTask.cancel();
                //
                if (!mPreviousTask.isFinished() &&
                        RequestProcessor.getDefault().isRequestProcessorThread()) { // issue 125439
                    mPreviousTask.waitFinished();
                    mPreviousTask = null;
                }
            }
            //
            // The old context is used here because the selected entity isn't changed.
            // It's necessary to check if the entity isn't deleted
            boolean thereIsSelectedEntity = false;
            if (mContext != null) {
                BpelEntity selectedEntity = mContext.getSelectedEntity();
                if (selectedEntity != null) {
                    thereIsSelectedEntity = selectedEntity.isInDocumentModel();
                }
            }
            final BpelDesignContext newContext = thereIsSelectedEntity ? mContext : null;
            //
            mPreviousTask = RequestProcessor.getDefault().post(
                    new Runnable() {
                public void run() {
                    SwingUtilities.invokeLater(new  Runnable() {
                        public void run() {
                            reloadMapperImpl(newContext);
                        }
                    });
                }
            }, SOURCE_MODEL_CHANGED_DELAY);
        } else {
            mIsInSynch = false;
        }
    }

    public synchronized void reloadMapper() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                    reloadMapperImpl(mContext);
                }
            });
        } else {
            reloadMapperImpl(mContext);
        }
    }

    // Implements PropertyChangeListener interface in order to listen changes of
    // activated top component.
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (TopComponent.Registry.PROP_ACTIVATED_NODES.equals(propertyName)) {
            processActivateComponentChanged();
        } else {
            // Other properties are not supported
            return;
        }
    }

    // This method isn't used now, but it can be helpfull in future.
//    private boolean isMapperActive() {
//        TopComponent activeTC = TopComponent.getRegistry().getActivated();
//        MultiViewHandler mvh = MultiViews.findMultiViewHandler(activeTC);
//        if (mvh != null) {
//            MultiViewPerspective mvp = mvh.getSelectedPerspective();
//            if (mvp != null) {
//                String mvpId = mvp.preferredID();
//                if (MapperMultiviewElement.MAPPER_PANEL_ID.equals(mvpId)) {
//                    return true;
//                }
//            }
//        }
//        //
//        return false;
//    }

    private synchronized void processActivateComponentChanged() {
        //
        // It doesn't matter to change the mapper when selection is changed
        // in another BPEL model.
        if (!isSameBpelModel()) {
            return;
        }
        //
        // Cantroller always synchronizes it's activated node with external
        // changes! It happens regardless of the mapper is visible or not.
        // Activated nodes are always changed before mapper processes the changes.
        synchActivatedNodes();
        //
        if (mIsVisible) {
            //
            boolean theSameMapper = false;
            //
            if (mContext != null) {
                //
                BpelDesignContext newContext = constructActivatedContext();
                theSameMapper = mContext.showsTheSameMapper(newContext);
            }
            //
            // Schedule the mapper reloading
            if (theSameMapper) {
                // a decision about reloading is made later
                updateContext(0);
            } else {
                updateContext(ACTIVATED_NODE_CHANGED_DELAY);
            }
        }
    }

    /**
     * Checks that the current activated node relates to the same BPEL model as the mapper .
     * @return
     */
    private boolean isSameBpelModel() {
        Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
        if (nodes != null && nodes.length > 0) {
            if (nodes[0] instanceof InstanceRef) {
                Object entity = ((InstanceRef) nodes[0]).getReference();
                if (entity instanceof BpelEntity) {
                    BpelModel selectedBpelModel =
                            BpelEntity.class.cast(entity).getBpelModel();
                    if (mBpelModel.equals(selectedBpelModel)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Synchronizes mapper's activated node with external changes
     */
    private void synchActivatedNodes() {
        assert SwingUtilities.isEventDispatchThread();
        //
        if (mMapperTcContext != null) {
            TopComponent mapperTc = mMapperTcContext.getTopComponent();
            if (mapperTc != null) {
                Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
                mapperTc.setActivatedNodes(nodes);
            }
        }
    }

    public void showMapper() {
        assert SwingUtilities.isEventDispatchThread();
        //
        mIsVisible = true;
        Mapper mapper = mMapperTcContext != null ?
            mMapperTcContext.getMapper() : null;
        if (mapper != null) {
            mapper.setVisible(true);
        }
        //
        // The update has to be invoked later because the activated node's
        // changes has to be processed first.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Skip mappers reloading if it already has a content and
                // current activated node doesn't relate to mapper's BPEL model
                if (mContext != null && !isSameBpelModel()) {
                    return;
                }
                updateContext(0);
            }
        });
    }

    public void hideMapper() {
        assert SwingUtilities.isEventDispatchThread();
        mIsVisible = false;
        Mapper mapper = mMapperTcContext != null ?
            mMapperTcContext.getMapper() : null;
        if (mapper != null) {
            mapper.setVisible(false);
        }
    }

    /**
     * Is it called when the context is changed.
     * @param delay
     * @param forceReload
     */
    private void updateContext(int delay) {
        assert SwingUtilities.isEventDispatchThread();
        //
        if (mPreviousTask != null) {
            mPreviousTask.cancel();
            //
            if (!mPreviousTask.isFinished() &&
                    RequestProcessor.getDefault().isRequestProcessorThread()) { // issue 125439
                mPreviousTask.waitFinished();
                mPreviousTask = null;
            }
        }
        //
        if (delay == 0) {
            updateContextImpl();
        } else {
            showMapperLoading(true);
            mPreviousTask = RequestProcessor.getDefault().post(
                    new Runnable() {
                public void run() {
                    SwingUtilities.invokeLater(new  Runnable() {
                        public void run() {
                            updateContextImpl();
                        }
                    });
                }
            }, delay);
        }
    }

    /**
     * Changes the desigh context to correspond to current activated node.
     *
     * @param forceReload makes to reload even if the new conext equals to the old one.
     */
    private synchronized void updateContextImpl() {
        assert SwingUtilities.isEventDispatchThread();
        //
        BpelDesignContext newContext = constructActivatedContext();
        //
        if (newContext == null) {
            showMapperIsEmpty();
            return;
        }
        //
        // Check
        BpelEntity newContextEntity = newContext.getContextEntity();
        if (newContextEntity == null) {
            // Hide the mapper if unmappable BPEL entity is selected
            showUnsupportedEntity(newContext);
            return;
        }
        //
//        // avoid entities from another BpelModel
//        BpelEntity newEntity = newContext.getSelectedEntity();
//        if ((newEntity != null && !myBpelModel.equals(newEntity.getBpelModel()))
//                || newEntity == null) {
//            return;
//        }
        //
        // Check the new context is valid
        boolean isValidContext = DesignContextUtil.isValidContext(newContext);
        if (!isValidContext) {
            showNotValidContext(newContext);
            return;
        }
        //
        // the context have to be updated just if context changes,
        // in case the bpel model changes doesn't need to update context
        boolean theSameContext = newContext.showsTheSameMapper(mContext);
        if (!theSameContext || !mIsInSynch) {
            reloadMapperImpl(newContext);
        } else {
            if (!isMModelLoading()) {
                // If the mapper isn't being loaded now
                showMapperLoading(false);
            }
        }
    }

    /**
     * Indicates if a Bpel Mapper model is being loaded right now
     * @return
     */
    private synchronized boolean isMModelLoading() {
        return mModelLoadingTask != null && !mModelLoadingTask.isFinished();
    }

    /**
     * Initiate mapper's reloading. The reloading happens in separate thread.
     * @param newContext
     */
    private synchronized void reloadMapperImpl(BpelDesignContext newContext) {
        assert SwingUtilities.isEventDispatchThread();
        //
        final BpelDesignContext oldContext = mContext;
        //
        if (newContext == null) {
            mContext = newContext; // Now the mContex is the new context
            showMapperIsEmpty();
            return;
        }
        //
        if (isModelInvalid()) {
            mContext = newContext; // Now the mContex is the new context
            showModelIsInvalid();
            return;
        }
        //
        showMapperLoading(true);
        if (mModelLoadingTask != null) {
            mModelLoadingTask.cancel();
            //
            if (!mModelLoadingTask.isFinished()) {
                mModelLoadingTask.waitFinished();
                mModelLoadingTask = null;
            }
        }
        //
        mContext = newContext; // Now the mContex is the new context
        //
        mModelLoadingTask = mModelLoadingRProcessor.post(
                new Runnable() {
            public void run() {
                try {
                    // Start constructing a new mapper model as a separate task
                    final MapperModel newMapperModel = constructMapperModel();
                    //
                    if (Thread.currentThread().isInterrupted() ||
                            newMapperModel == null) {
                        return;
                    }
                    //
                    SwingUtilities.invokeLater(new  Runnable() {
                        public void run() {
    //                        if (isMModelLoading()) {
    //                            // If the mapper is being loaded now.
    //                            // Maybe another loading has started
    //                            return;
    //                        }
                            //
                            mMapperStateManager.storeOldEntityContext(oldContext);
                            //
                            assert newMapperModel != null;
                            mMapperTcContext.setMapperModel(newMapperModel);
                            if (!mMapperStateManager.restoreOldEntityContext(mContext)) {
                                mMapperTcContext.getMapper().expandNonEmptyGraphs();
                                mMapperTcContext.getMapper().expandMappedLeftTreeItems();
                            }
                            //
                            mIsInSynch = true;
                        }
                    });
                } finally {
                    SwingUtilities.invokeLater(new  Runnable() {
                        public void run() {
                            showMapperLoading(false);
                            displayInvalidXPathExpressions();
                        }
                    });
                }
            }
        });
    }

    //==========================================================================
    
    /**
     * Shows error panel with the specified message.
     * @param message
     */
    protected void disableMapper(String message) {
        assert SwingUtilities.isEventDispatchThread();
        //
        // Save current state of the mapper before switching to error panel.
        if (mMapperStateManager != null) {
            mMapperStateManager.storeOldEntityContext(mContext);
        }
        //
        if (mMapperTcContext != null) {
            TopComponent tc = mMapperTcContext.getTopComponent();
            if (tc != null) {
                ShowMapperCookie showCookie =
                        tc.getLookup().lookup(ShowMapperCookie.class);
                if (showCookie != null) {
                    showCookie.show(message);
                }
            }
        }
        //
        mContext = null;
        showMapperLoading(false);
    }

    /**
     * Show "Loading..." message over the mapper.
     * @param flag
     */
    public void showMapperLoading(boolean flag) {
        assert SwingUtilities.isEventDispatchThread();
        //
        TopComponent tc = mMapperTcContext.getTopComponent();
        if (tc == null) {
            return;
        }

        ShowMapperCookie showCookie = tc.getLookup().lookup(ShowMapperCookie.class);
        if (showCookie != null) {
            showCookie.showLoading(flag);
        }
    }

    private boolean isModelInvalid() {
        BpelModel bpelModel = mBpelModel;
        if (bpelModel != null) {
            return bpelModel.getState().equals(State.NOT_WELL_FORMED);
        }
        return false; // Consider the model valid by default
    }

    /**
     * Displays a dialog with notifications about mapper's initialization errors.
     */
    protected void displayInvalidXPathExpressions() {
        //
        if (mContext == null) {
            return;
        }
        //
        BpelEntity bpelEntity = mContext.getContextEntity();
        if (bpelEntity == null) {
            return;
        }
        //
        StringBuffer errMsgBuffer = mContext.getValidationErrMsgBuffer();
        if ((errMsgBuffer == null) || (errMsgBuffer.length() == 0)) {
            return;
        }
        //
        String xpathValidationErrMsg = errMsgBuffer.toString().trim();
        xpathValidationErrMsg = xpathValidationErrMsg.substring(0,
            xpathValidationErrMsg.lastIndexOf(",")) + ".";
        if (xpathValidationErrMsg != null && xpathValidationErrMsg.length() != 0) {
            UserNotification.showMessage(MessageFormat.format(NbBundle.getMessage(
                this.getClass(), "LBL_Bpel_Mapper_Err_Msg_Wrong_XPathExpr_Title"),
                new Object[] {bpelEntity.getElementType().getSimpleName()}) +
                " \n" + xpathValidationErrMsg);
        }
    }

    //==========================================================================

    protected abstract BpelDesignContext constructActivatedContext();

    protected abstract MapperModel constructMapperModel();

    protected abstract void showMapperIsEmpty();

    protected abstract void showModelIsInvalid();

    protected abstract void showUnsupportedEntity(BpelDesignContext context);

    protected abstract void showNotValidContext(BpelDesignContext context);

}
