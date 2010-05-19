/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.mapper;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.EventObject;
import javax.swing.SwingUtilities;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.wlm.model.api.TAction;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.worklist.dataloader.WorklistDataObject;
import org.netbeans.modules.worklist.editor.mapper.model.WlmMapperModelFactory;
import org.netbeans.modules.worklist.editor.mapper.multiview.MapperMultiviewElement;
import org.netbeans.modules.worklist.editor.mapper.multiview.ShowMapperCookie;
import org.netbeans.modules.worklist.editor.mapper.state.MapperStateManager;
import org.netbeans.modules.xml.xam.Model.State;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 * Controls the state of the WLM mapper and manages different events:
 * - change of activated node (correspondent mapper TC activated nodes)
 * - change of mapper Tc lifecycle state (intrested in mapperTc showing, hidden, closed )
 * - change in the related WLM model
 * - change the state of WLM model
 *
 * @author nk160297
 * @author Vitaly Bychkov
 *
 */
public class DesignContextControllerImpl
        implements DesignContextController, PropertyChangeListener
{

    private WlmDesignContext mContext;

    private MapperTcContext mMapperTcContext;
    private WlmModelSynchListener mWlmModelSynchListener;
    private WLMModel myWlmModel;
    // flag property for MapperTC showing/hidding states
    private transient boolean isMapperShown;
    private WlmDesignContext mNewContext;
    private MapperStateManager myMapperStateManager;
    private static final int ACTION_NODE_CHANGE_TASK_DELAY = 150;
    private transient RequestProcessor.Task myPreviousTask;
    private int myDelay = ACTION_NODE_CHANGE_TASK_DELAY;
    private WeakReference<Object> mWlmModelUpdateSourceRef;
    private int mModelUpdated = 0; // If >0 indicates that the WLM model is updated by the Mapper

    /**
     * The lookup of mapper Tc must contain WroklistDataObject, WLMModel and ShowMapperCookie
     * @param mapperTc
     */
    public DesignContextControllerImpl(TopComponent mapperTc, WorklistDataObject dObj) {
        assert mapperTc != null;
        assert mapperTc instanceof MapperTcContext;

        mMapperTcContext = (MapperTcContext) mapperTc;
        myWlmModel = dObj.getModel();
//        myWlmModel = mapperTc.getLookup().lookup(WLMModel.class);
        assert myWlmModel != null;
        mWlmModelSynchListener = new WlmModelSynchListener(this);
        mWlmModelSynchListener.register(myWlmModel);

        TopComponent.getRegistry().addPropertyChangeListener(this);

        initContext();
    }

    public void cleanup() {
        TopComponent.getRegistry().removePropertyChangeListener(this);
        mWlmModelSynchListener.unregisterAll();
        myWlmModel = null;
        mMapperTcContext = null;
    }

    public synchronized void setWlmModelUpdateSource(Object source) {
        mWlmModelUpdateSourceRef = new WeakReference<Object>(source);
    }

    public synchronized WlmDesignContext getContext() {
        return mContext;
    }

    // context changes if selectedEntity changes
    public synchronized void setContext(WlmDesignContext newContext) {
        assert EventQueue.isDispatchThread();
        //
        mNewContext = newContext;

        boolean isValidContext = isValidContext(mContext);
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

        WLMComponent newEntity = newContext.getContextEntity();

        // avoid entities from another WLMModel
        if ((newEntity != null && !myWlmModel.equals(newEntity.getModel()))
                || newEntity == null)
        {
            return;
        }

        // the context have to be updated just if context changes,
        // in case the WLM model changes doesn't need to update context
        if (newContext.equals(mContext))
        {
            return;
        }


//        setActivatedNodes(newContext == null ? null : newContext.getActivatedNode());

        if (isMapperShown) {
            setContextImpl();
        }
    }

    public synchronized void setModelUpdated(boolean flag) {
        if (flag) {
            mModelUpdated++;
        } else {
            mModelUpdated--;
        }
    }

    // TODO m
    public synchronized void reloadMapper(EventObject event) {
        //
        // Ignore reload if it has been initiated by the mapper itself
        if (mModelUpdated > 0) {
//        if (event.getSource() == getWlmModelUpdateSource()) {
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

    public void processActionDeletion(TAction action) {
        WLMComponent contextEntity = mContext.getContextEntity();
        if (contextEntity == action) {
            mMapperTcContext.notifyDesignContextChanged();
        }
    }

    synchronized void performReloadMapperInAwt() {
        assert EventQueue.isDispatchThread();
        if (!isValidContext(mContext)) {
            setDelay(0);
            updateContext(-1);
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
        setContextImpl(true);
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
        WlmDesignContext context = 
                new WlmDesignContextImpl(myWlmModel, myWlmModel.getTask());
        setContext(context);
        myMapperStateManager = new MapperStateManager(mMapperTcContext);
    }

    private void updateContext(int delay) {
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
            
//            setContext(BpelDesignContextFactory.getInstance().getActivatedContext(myWlmModel));
            // TODO: The mapper doesn't react on activated node now.

        } else {
            myPreviousTask = RequestProcessor.getDefault().post(
                    new Runnable() {
                public void run() {
    //                BpelDesignContext newBpelContext = DesignContextChangeListener.getActivatedContext(myBpelModel);
                    SwingUtilities.invokeLater(new  Runnable() {
                        public void run() {
                            // setContext(BpelDesignContextFactory.getInstance().getActivatedContext(myWlmModel));
                        }
                    });
                }
            }, delay);
        }
    }

    // todo m
    private void updateContext() {
        updateContext(getDelay());
    }

    // TODO m correct behaviour if just selectedEntity changes
    private void setContextImpl() {
        setContextImpl(false);
    }

    private void setContextImpl(final boolean forceReload) {
        // Copy the context to a new local variable at first.
        if (mNewContext == null && forceReload) {
            reloadMapper(new EventObject(new Object()));
            return;
        }
            
        WlmDesignContext newContext = mNewContext;
        
        if (newContext == null) {
            // do nothing - simple continue to show the old context
            return;
        }

        if (isModelInvalid()) {
            myMapperStateManager.storeOldEntityContext(mContext);
            // hide mapper if BpelModel is invalid
            showModelIsInvalid();
            return;
        }
        //
        WLMComponent oldContextEntity = mContext != null ? mContext.getContextEntity() : null;
        WLMComponent newContextEntity = newContext.getContextEntity();
        if (newContextEntity == null) {
            myMapperStateManager.storeOldEntityContext(mContext);
            // Hide the mapper if unmappable BPEL entity is selected
            showUnsupportedEntity(newContext);
            return;
        }
                
        if (forceReload || !newContext.equals(mContext)) {
            if (forceReload || !newContextEntity.equals(oldContextEntity)) {
                // newContext.getValidationErrMsgBuffer().setLength(0);
                
                myMapperStateManager.storeOldEntityContext(mContext);
                //
                MapperModel newMapperModel = new WlmMapperModelFactory(mMapperTcContext,
                    newContext).constructModel();

                mContext = newContext;
                setMapperModel(newMapperModel);
                myMapperStateManager.restoreOldEntityContext(mContext);
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // GraphExpandProcessor.expandGraph(mMapperTcContext, mContext);
                    
                    if (forceReload) displayInvalidXPathExpressions(mContext, true);
                }
            });
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
            Mapper mapper = mMapperTcContext != null ? mMapperTcContext.getMapper() : null;
            if ( mapper != null) {
                MapperModel mapperModel = mapper.getModel();
                if (mapperModel == null) {
                    showMapperIsEmpty();
                }
            }
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
        MapperModel newMapperModel = new WlmMapperModelFactory(mMapperTcContext,
            mContext).constructModel();

        myMapperStateManager.storeOldEntityContext(mContext);
        setMapperModel(newMapperModel);
        myMapperStateManager.restoreOldEntityContext(mContext);
    }

    private synchronized Object getWlmModelUpdateSource() {
        if (mWlmModelUpdateSourceRef == null) {
            // Mapper is the default synchronization source
            return mMapperTcContext.getMapper();
        } else {
            return mWlmModelUpdateSourceRef.get();
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
                    mapperTc.setActivatedNodes(new Node[]{aNode});
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
        disableMapper(NbBundle.getMessage(MapperMultiviewElement.class, "LBL_Invalid_WlmModel")); // NOI18N
    }

    private void showMapperIsEmpty() {
        disableMapper(NbBundle.getMessage(MapperMultiviewElement.class, "LBL_EmptyMapperContext")); // NOI18N
    }

    private void showUnsupportedEntity(WlmDesignContext context) {
        assert context != null;
        String entityName = null;
//        Node node = context.getActivatedNode();
//        entityName = node != null ? node.getDisplayName() : null;
//        if (entityName == null) {
//            WLMComponent entity = context.getSelectedEntity();
//            entityName = entity instanceof Nameable ? ((Nameable) entity).getName() : EditorUtil.getTagName(entity);
//        }
//        entityName = entityName == null ? "" : entityName;
        disableMapper(NbBundle.getMessage(MapperMultiviewElement.class,
                "LBL_EmptyMapper", entityName)); // NOI18N
    }

    private void showNotValidContext(WlmDesignContext context) {
        assert context != null;
        String entityName = null;
//        Node node = context.getActivatedNode();
//        entityName = node != null ? node.getDisplayName() : null;
//        if (entityName == null) {
//            WLMComponent entity = context.getSelectedEntity();
//            entityName = entity instanceof Nameable ? ((Nameable) entity).getName() : EditorUtil.getTagName(entity);
//        }
//        entityName = entityName == null ? "" : entityName;
        disableMapper(NbBundle.getMessage(MapperMultiviewElement.class,
                "LBL_InValidMapperContext", entityName)); // NOI18N
    }

    private void disableMapper(String message) {
        TopComponent tc = mMapperTcContext.getTopComponent();
        if (tc == null) {
            return;
        }

        ShowMapperCookie showCookie = tc.getLookup().lookup(ShowMapperCookie.class);
        if (showCookie != null) {
            showCookie.show(message);
        }

        mContext = null;
    }

    private boolean isModelInvalid() {
        WLMModel wlmModel = myWlmModel;
        if (wlmModel != null) {
            return wlmModel.getState().equals(State.NOT_WELL_FORMED);
        }
        return false; // Consider the model valid by default
    }

    public void processDataObject(Object dataObject) {
        mWlmModelSynchListener.processDataObject(dataObject);
    }

    private String displayInvalidXPathExpressions(WlmDesignContext wlmDesignContext,
        boolean displayErrMessage) {
        if (wlmDesignContext == null) return null;
        WLMComponent WLMComponent = wlmDesignContext.getContextEntity();
        if (WLMComponent == null) return null;

        return null;

//        StringBuffer errMsgBuffer = wlmDesignContext.getValidationErrMsgBuffer();
//        if ((errMsgBuffer == null) || (errMsgBuffer.length() == 0)) return null;
//
//        String xpathValidationErrMsg = errMsgBuffer.toString().trim();
//        xpathValidationErrMsg = xpathValidationErrMsg.substring(0,
//            xpathValidationErrMsg.lastIndexOf(",")) + ".";
//        if ((displayErrMessage) && (xpathValidationErrMsg != null)) {
//            UserNotification.showMessage(MessageFormat.format(NbBundle.getMessage(
//                this.getClass(), "LBL_Bpel_Mapper_Err_Msg_Wrong_XPathExpr_Title"),
//                new Object[] {WLMComponent.getElementType().getSimpleName()}) +
//                " \n" + xpathValidationErrMsg);
//        }
//        return (xpathValidationErrMsg);
    }
    
    public static void addErrMessage(StringBuffer errMsgBuffer, 
        String xpathExpression, String tagName) {
        if ((errMsgBuffer == null) || (xpathExpression == null) || (tagName == null)) return;

        String 
            errMsgPattern = NbBundle.getMessage(DesignContextControllerImpl.class, 
            "LBL_Bpel_Mapper_Err_Msg_Wrong_XPathExpr_Data"),
            errMsg = MessageFormat.format(errMsgPattern, new Object[] {tagName,
                xpathExpression});                

            errMsgBuffer.append((errMsgBuffer.length() > 0) ? 
                " " + errMsg : errMsg);                
            errMsgBuffer.append(",\n");
    }

    public static class WlmDesignContextImpl implements WlmDesignContext {

        private WLMModel mWlmModel;
        private WLMComponent mWLMComponent;

        public WlmDesignContextImpl(WLMModel wlmModel, WLMComponent WLMComponent)  {
            mWlmModel = wlmModel;
            mWLMComponent = WLMComponent;
        }

        public WLMModel getWlmModel() {
            return mWlmModel;
        }

        public WLMComponent getContextEntity() {
            return mWLMComponent;
        }
    }

    public static boolean isValidContext(WlmDesignContext context) {
        return true;
    }

}