/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.ui.action;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.bpel.debugger.api.EditorContextBridge;
import org.netbeans.modules.bpel.debugger.ui.breakpoint.BpelBreakpointListener;
import org.netbeans.modules.bpel.debugger.ui.util.ModelUtil;
import org.netbeans.modules.bpel.debugger.ui.util.Util;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.bpel.nodes.BpelNode;

import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.windows.WindowManager;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ActionsProviderSupport;

import org.netbeans.modules.bpel.debugger.api.breakpoints.LineBreakpoint;
import org.netbeans.modules.bpel.debugger.ui.util.Log;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.10.19
 */
public class Breakpoint extends ActionsProviderSupport {
    
    private BpelBreakpointListener myBreakpointAnnotationListener;
    private AtomicBoolean myIsExecuting = new AtomicBoolean(false);

    /**{@inheritDoc}*/
    public Breakpoint() {
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, true);
    }
    
    /**{@inheritDoc}*/
    public void doAction(Object action) {
        try {
            if (myIsExecuting.compareAndSet(false, true)) {
                toggleBreakpoint();
            }
        } finally {
            myIsExecuting.set(false);
        }
    }
    
    private void toggleBreakpoint() {
        Node node = getNode();
        if (node == null) {
            return;
        }
        
        DataObject dataObject = getDataObject(node);
        if (dataObject == null) {
            return;
        }
        
        String ext = getFileExt(dataObject);
        if (!"bpel".equals(ext)) {                  // NOI18N
            return;
        }
        
        String url = FileUtil.toFile(dataObject.getPrimaryFile()).getPath();
        url = url.replace("\\", "/"); // NOI18N
        
        UniqueId bpelEntityId = null;
        if (node instanceof BpelNode) {
            Object modelReference = ((BpelNode)node).getReference();
            
            if (modelReference == null) {
                return;
            }

            if (!(modelReference instanceof Activity)) {
                return;
            }
            
            Activity activity = (Activity)modelReference;
            bpelEntityId = activity.getUID();
        } else {
            int lineNumber = getCurrentLineNumber(node);
            if (lineNumber > 0) {
                int translatedLineNumber =
                        EditorContextBridge.translateBreakpointLine(url, lineNumber);
                
                //TODO:optimize it - we have already obtained document in the getCurrentLineNumber
                StyledDocument doc = Util.getDocument(dataObject);
                BpelModel model = Util.getBpelModel(dataObject);
                if (doc != null && model != null && translatedLineNumber > 0) {
                    int offset = Util.findOffset(doc, translatedLineNumber);
                    bpelEntityId = ModelUtil.getBpelEntityId(model, offset);
                }
            }
            
        }
        
        if (bpelEntityId == null) {
            return;
        }
        
        String xpath = ModelUtil.getXpath(bpelEntityId);
        if (xpath == null) {
            return;
        }
        
        DebuggerManager debuggerManager = DebuggerManager.getDebuggerManager();
        LineBreakpoint breakpoint =
                findBreakpoint(url, xpath);
        if (breakpoint != null) {
            // Breakpoint already exists
            debuggerManager.removeBreakpoint(breakpoint);
            return;
        }
        
        // No breakpoint exists - add the breakpoint.
        LineBreakpoint newBreakpoint =
                LineBreakpoint.create(url, xpath);
        debuggerManager.addBreakpoint(newBreakpoint);
    }
    
    private LineBreakpoint findBreakpoint(String url, String xpath) {
        return getBreakpointAnnotationListener().findBreakpoint(url, xpath);
    }
    
    /**{@inheritDoc}*/
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }
    
    private Node getNode() {
        Node [] nodes = WindowManager.getDefault().getRegistry().getCurrentNodes();
        
        if (nodes == null) {
            Log.out("Current nodes are null"); // NOI18N
            return null;
        }
        if (nodes.length == 0) {
            Log.out("There is no current node"); // NOI18N
            return null;
        }
        if (nodes.length != 1) {
            Log.out("There are too many current nodes"); // NOI18N
            return null;
        }
        return nodes [0];
    }
    
    private String getFileExt(DataObject dataObject) {
        if (dataObject == null) {
            return null;
        }
        FileObject fileObject = dataObject.getPrimaryFile();
        
        if (fileObject == null) {
            Log.out("fileObject is null"); // NOI18N
            return null;
        }
        return fileObject.getExt();
    }
    
    private DataObject getDataObject(Node node) {
        if (node == null) {
            return null;
        }
        return (DataObject)node.getLookup().lookup(DataObject.class);
    }
    
    private int getCurrentLineNumber(Node node) {
        EditorCookie editorCookie =
            (EditorCookie) node.getLookup().lookup(EditorCookie.class);
        
        if (editorCookie == null) {
            Log.out("Editor cookie is null"); // NOI18N
            return -1;
        }
        StyledDocument document = editorCookie.getDocument();
        
        if (document == null) {
            Log.out("Document is null"); // NOI18N
            return -1;
        }
        
        JEditorPane[] editorPanes = editorCookie.getOpenedPanes();

        if (editorPanes == null) {
            Log.out("Editor panes are null"); // NOI18N
            return -1;
        }
        if (editorPanes.length == 0) {
            Log.out("There is no editor pane"); // NOI18N
            return -1;
        }
        Caret caret = editorPanes [0].getCaret();

        if (caret == null) {
            Log.out("Caret is null"); // NOI18N
            return -1;
        }
        int offset = caret.getDot();

        return NbDocument.findLineNumber(document, offset) + 1;
    }
    
    private BpelBreakpointListener getBreakpointAnnotationListener () {
        if (myBreakpointAnnotationListener == null) {
            myBreakpointAnnotationListener = (BpelBreakpointListener) 
                    DebuggerManager.getDebuggerManager ().lookupFirst 
                    (null, BpelBreakpointListener.class);
        }
        return myBreakpointAnnotationListener;
    }
}
