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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.bpel.debugger.ui.action;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.StyledDocument;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.bpel.debugger.api.EditorContextBridge;
import org.netbeans.modules.bpel.debugger.ui.breakpoint.BpelBreakpointListener;
import org.netbeans.modules.bpel.debugger.ui.util.ModelUtil;
import org.netbeans.modules.bpel.debugger.ui.util.EditorUtil;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.support.UniqueId;

import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ActionsProviderSupport;

import org.netbeans.modules.bpel.debugger.api.breakpoints.LineBreakpoint;
import org.netbeans.modules.bpel.debugger.ui.util.Log;
import org.netbeans.modules.soa.ui.nodes.InstanceRef;

/**
 * @author Vladimir Yaroslavskiy
 * @author Alexander Zgursky
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
        if (myIsExecuting.compareAndSet(false, true)) {
            try {
                toggleBreakpoint();
            } finally {
                myIsExecuting.set(false);
            }
        }
    }
    
    //TODO:rewrite this messy code
    private void toggleBreakpoint() {
        Node node = getCurrentNode();
        if (node == null) {
            return;
        }
        
        DataObject dataObject = node.getLookup().lookup(DataObject.class);
        if (dataObject == null) {
            return;
        }
        
        String ext = getFileExt(dataObject);
        if (!"bpel".equals(ext)) {                  // NOI18N
            return;
        }
        
        String url = FileUtil.toFile(dataObject.getPrimaryFile()).getPath();
        //TODO:consider using FileUtil.normalizeFile()
        url = url.replace("\\", "/"); // NOI18N
        
        UniqueId bpelEntityId = null;
        if ((node instanceof InstanceRef) && !isInSourceEditor()) {
            Object modelReference = ((InstanceRef)node).getReference();
            
            if (modelReference == null || !(modelReference instanceof Activity)) {
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
                StyledDocument doc = EditorUtil.getDocument(dataObject);
                BpelModel model = EditorUtil.getBpelModel(dataObject);
                if (doc != null && model != null && translatedLineNumber > 0) {
                    int offset = EditorUtil.findOffset(doc, translatedLineNumber);
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
        LineBreakpoint breakpoint = getBreakpointAnnotationListener().
                findBreakpoint(url, xpath);
        if (breakpoint != null) {
            // Breakpoint already exists
            debuggerManager.removeBreakpoint(breakpoint);
            return;
        }
        
        // No breakpoint exists - add the breakpoint.
        LineBreakpoint newBreakpoint = LineBreakpoint.create(url, xpath);
        debuggerManager.addBreakpoint(newBreakpoint);
    }
    
    /**{@inheritDoc}*/
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }
    
    private Node getCurrentNode() {
        Node [] nodes = WindowManager.getDefault().getRegistry().getCurrentNodes();
        
        if (nodes == null || nodes.length != 1 ) {
            return null;
        }
        return nodes [0];
    }
    
    private boolean isInSourceEditor() {
        TopComponent tc = WindowManager.getDefault().getRegistry().getActivated();
        if (tc == null) {
            return false;
        }
        
        MultiViewHandler mvh = MultiViews.findMultiViewHandler(tc);
        if (mvh == null) {
            return false;
        }

        MultiViewPerspective mvp = mvh.getSelectedPerspective();
        return mvp.preferredID().equals("bpelsource");
    }
    
    private String getFileExt(DataObject dataObject) {
        if (dataObject == null) {
            return null;
        }
        
        FileObject fileObject = dataObject.getPrimaryFile();
        if (fileObject == null) {
            return null;
        }
        
        return fileObject.getExt();
    }
    
    private int getCurrentLineNumber(Node node) {
        EditorCookie editorCookie = node.getLookup().lookup(EditorCookie.class);
        if (editorCookie == null) {
            return -1;
        }
        
        JEditorPane[] editorPanes = editorCookie.getOpenedPanes();
        if (editorPanes == null || editorPanes.length == 0) {
            return -1;
        }
        
        Caret caret = editorPanes[0].getCaret();
        if (caret == null) {
            return -1;
        }
        
        int offset = caret.getDot();
        
        StyledDocument document = editorCookie.getDocument();
        if (document == null) {
            return -1;
        }
        
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
