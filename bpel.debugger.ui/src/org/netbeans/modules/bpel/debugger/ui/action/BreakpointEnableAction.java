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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.StyledDocument;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.bpel.debugger.api.breakpoints.LineBreakpoint;
import org.netbeans.modules.bpel.debugger.ui.breakpoint.BpelBreakpointListener;
import org.netbeans.modules.bpel.debugger.ui.util.EditorUtil;
import org.netbeans.modules.bpel.debugger.ui.util.ModelUtil;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.BooleanStateAction;
import org.openide.windows.WindowManager;

/**
 * Enables or disables breakpoints.
 *
 * @author Alexander Zgursky
 */
public class BreakpointEnableAction extends BooleanStateAction {
    private BpelBreakpointListener myBreakpointAnnotationListener;

//TODO: the whole class needs refactoring - most of the code was copy-pasted from
//org.netbeans.modules.bpel.debugger.ui.action.Breakpoint

    public boolean isEnabled() {
        LineBreakpoint b = findCurrentBreakpoint();
        if (b != null) {
            boolean value = b.isEnabled();
            super.setBooleanState(value);
            return true;
        }
        return false;
    }
    
    public String getName() {
        return NbBundle.getMessage(BreakpointEnableAction.class, "CTL_enabled");
    }
    
    public void setBooleanState(boolean value) {
        LineBreakpoint b = findCurrentBreakpoint();
        if (value) {
            b.enable();
        } else {
            b.disable();
        }
        super.setBooleanState(value);
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    private LineBreakpoint findCurrentBreakpoint() {
        Node node = getCurrentNode();
        if (node == null) {
            return null;
        }
        
        DataObject dataObject = node.getLookup().lookup(DataObject.class);
        if (dataObject == null) {
            return null;
        }
        
        String ext = getFileExt(dataObject);
        if (!"bpel".equals(ext)) {                  // NOI18N
            return null;
        }
        
        int lineNumber = getCurrentLineNumber(node);
        if (lineNumber < 1) {
            return null;
        }
        
        StyledDocument doc = EditorUtil.getDocument(dataObject);
        if (doc == null) {
            return null;
        }
        
        int offset = EditorUtil.findOffset(doc, lineNumber);
        BpelModel model = EditorUtil.getBpelModel(dataObject);
        if (model == null) {
            return null;
        }
        
        UniqueId bpelEntityId = ModelUtil.getBpelEntityId(model, offset);
        
        String url = FileUtil.toFile(dataObject.getPrimaryFile()).getPath();
        //TODO:consider using FileUtil.normalizeFile()
        url = url.replace("\\", "/"); // NOI18N
        if (bpelEntityId == null) {
            return null;
        }
        
        String xpath = ModelUtil.getXpath(bpelEntityId);
        if (xpath == null) {
            return null;
        }
        
        return getBreakpointAnnotationListener().findBreakpoint(url, xpath);
    }

    private Node getCurrentNode() {
        Node [] nodes = WindowManager.getDefault().getRegistry().getCurrentNodes();
        
        if (nodes == null || nodes.length != 1 ) {
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
