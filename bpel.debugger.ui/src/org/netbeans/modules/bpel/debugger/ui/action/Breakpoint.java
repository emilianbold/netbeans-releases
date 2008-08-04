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

package org.netbeans.modules.bpel.debugger.ui.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.bpel.debugger.api.EditorContextBridge;
import org.netbeans.modules.bpel.debugger.api.breakpoints.LineBreakpoint;
import org.netbeans.modules.bpel.debugger.ui.breakpoint.BpelBreakpointListener;
import org.netbeans.modules.bpel.debugger.ui.util.EditorUtil;
import org.netbeans.modules.bpel.debugger.ui.util.ModelUtil;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.editors.api.nodes.actions.BpelNodeTypedAction;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.soa.ui.nodes.InstanceRef;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * @author Vladimir Yaroslavskiy
 * @author Alexander Zgursky
 * @author Kirill Sorokin
 */
public class Breakpoint extends ActionsProviderSupport 
        implements PropertyChangeListener {
    
    private BpelBreakpointListener myBreakpointAnnotationListener;
    private AtomicBoolean myIsExecuting = new AtomicBoolean(false);
    
    /**{@inheritDoc}*/
    public Breakpoint() {
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, true);
        
        TopComponent.getRegistry().addPropertyChangeListener(
                WeakListeners.propertyChange(this, TopComponent.getRegistry()));
    }
    
    /**{@inheritDoc}*/
    public void doAction(
            final Object action) {
        if (myIsExecuting.compareAndSet(false, true)) {
            try {
                toggleBreakpoint();
            } finally {
                myIsExecuting.set(false);
            }
        }
    }
    
    /**{@inheritDoc}*/
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }

    public void propertyChange(
            final PropertyChangeEvent event) {
        
        // IZ 135771. Ensure we're in EDT.
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Breakpoint.this.propertyChange(event);
                }
            });
            
            return;
        }
        
        final TopComponent activeTc = 
                WindowManager.getDefault().getRegistry().getActivated();
        final MultiViewHandler mvh = 
                MultiViews.findMultiViewHandler(activeTc);
        
        final TopComponent navigatorTc = 
                WindowManager.getDefault().findTopComponent("navigatorTC");
        
        String currentViewName = null;
        
        if (mvh != null) {
            final MultiViewPerspective mvp = mvh.getSelectedPerspective();
            
            if (mvp != null) {
                currentViewName = mvp.preferredID();
            }
        }
        
        boolean enabled;
        if ((activeTc != null) && (activeTc.equals(navigatorTc))) {
            enabled = true;
        } else {
            enabled = (EditorUtil.getCurrentLine() != null) &&
                    ("orch-designer".equals(currentViewName) || 
                    "bpelsource".equals(currentViewName));
                    
            if (enabled && "orch-designer".equals(currentViewName)) {
                final Node node = getCurrentNode();

                final javax.swing.Action[] actions = node.getActions(true);
                if (actions != null) {
                    enabled = false;

                    for (javax.swing.Action action: actions) {
                        if ((action instanceof BpelNodeTypedAction) && 
                                ((BpelNodeTypedAction) action).getType() == ActionType.TOGGLE_BREAKPOINT) {
                            enabled = true;
                            break;
                        }
                    }
                }
            }
            
        }
        
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, enabled);
    }
    
    // Private /////////////////////////////////////////////////////////////////
    private void toggleBreakpoint() {
        final Node node = getCurrentNode();
        if (node == null) {
            return;
        }
        
        final DataObject dataObject = 
                node.getLookup().lookup(DataObject.class);
        if (dataObject == null) {
            return;
        }
        
        final String ext = getFileExt(dataObject);
        if (!"bpel".equals(ext)) { // NOI18N
            return;
        }
        
        final String url = FileUtil.toFile(dataObject.getPrimaryFile()).
                getPath().replace("\\", "/"); // NOI18N
        
        int lineNumber = -1;
        UniqueId bpelEntityId = null;
        if ((node instanceof InstanceRef) && !isInSourceEditor()) {
            final Object model = ((InstanceRef) node).getReference();
            
            if ((model == null) || !(model instanceof BpelEntity)){
                return;
            }
            
            final BpelEntity bpelEntity = (BpelEntity) model;
            
            bpelEntityId = bpelEntity.getUID();
            lineNumber = ModelUtil.getLineNumber(bpelEntityId);
            
            final int translatedLineNumber = EditorContextBridge.
                    translateBreakpointLine(url, lineNumber);
            if ((translatedLineNumber != -1)) {
                lineNumber = translatedLineNumber;
            }
        } else {
            lineNumber = EditorUtil.getLineNumber(node);
            
            if (lineNumber > 0) {
                final int translatedLineNumber = EditorContextBridge.
                        translateBreakpointLine(url, lineNumber);
                
                if ((translatedLineNumber != -1) &&
                        (translatedLineNumber >= lineNumber - 5) && 
                        (translatedLineNumber <= lineNumber + 5)) {
                    final StyledDocument document = 
                            EditorUtil.getDocument(dataObject);
                    final BpelModel model = 
                            EditorUtil.getBpelModel(dataObject);
                    
                    if ((document != null) && 
                            (model != null) && 
                            (translatedLineNumber > 0)) {
                        final int offset = EditorUtil.findOffset(
                                document, 
                                translatedLineNumber);
                        bpelEntityId = ModelUtil.getBpelEntityId(model, offset);
                        lineNumber = translatedLineNumber;
                    }
                }
            }
        }
        
        if (lineNumber == -1) {
            return;
        }
        
        String xpath = null;
        if (bpelEntityId != null) {
            xpath = ModelUtil.getXpath(bpelEntityId);
        }
        
        final DebuggerManager debuggerManager = 
                DebuggerManager.getDebuggerManager();
        final LineBreakpoint breakpoint = getBreakpointAnnotationListener().
                findBreakpoint(url, xpath, lineNumber);
        
        
        if (breakpoint != null) {
            debuggerManager.removeBreakpoint(breakpoint);
        } else {
            debuggerManager.addBreakpoint(
                    LineBreakpoint.create(url, xpath, lineNumber));
        }
    }
    
    private Node getCurrentNode() {
        final Node[] nodes = 
                WindowManager.getDefault().getRegistry().getCurrentNodes();
        
        if ((nodes == null) || (nodes.length != 1)) {
            return null;
        }
        
        return nodes[0];
    }
    
    private boolean isInSourceEditor() {
        final TopComponent tc = 
                WindowManager.getDefault().getRegistry().getActivated();
        if (tc == null) {
            return false;
        }
        
        final MultiViewHandler mvh = MultiViews.findMultiViewHandler(tc);
        if (mvh == null) {
            return false;
        }

        final MultiViewPerspective mvp = mvh.getSelectedPerspective();
        
        return mvp.preferredID().equals("bpelsource");
    }
    
    private String getFileExt(
            final DataObject dataObject) {
        
        if (dataObject == null) {
            return null;
        }
        
        final FileObject fileObject = dataObject.getPrimaryFile();
        if (fileObject == null) {
            return null;
        }
        
        return fileObject.getExt();
    }
    
    private BpelBreakpointListener getBreakpointAnnotationListener() {
        if (myBreakpointAnnotationListener == null) {
            myBreakpointAnnotationListener = DebuggerManager.getDebuggerManager().lookupFirst(null, BpelBreakpointListener.class);
        }
        
        return myBreakpointAnnotationListener;
    }
}
