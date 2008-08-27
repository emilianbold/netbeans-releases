/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.web.client.javascript.debugger.ui;

import java.awt.EventQueue;
import java.io.File;
import java.util.logging.Level;

import javax.swing.Action;

import org.netbeans.modules.web.client.javascript.debugger.api.NbJSDebugger;
import org.netbeans.modules.web.client.javascript.debugger.filesystem.URLFileObject;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSCallStackFrame;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSSource;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSWindow;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSFactory;
import org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.NbJSBreakpoint;
import org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.NbJSBreakpointNodeActions;
import org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.NbJSURIBreakpoint;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.netbeans.modules.web.client.tools.api.JSLocation;
import org.netbeans.spi.viewmodel.Models;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.NbBundle;

public final class NbJSEditorUtil {

    public static final String HTML_MIME_TYPE = "text/html";
    public static final String JAVASCRIPT_MIME_TYPE = "text/javascript";
    private static NbJSDebuggerAnnotation currentLineDA;
	
    
    public static final String BREAKPOINT_ANNOTATION_TYPE = new String("Breakpoint"); //NOI18N
    public static final String DISABLED_BREAKPOINT_ANNOTATION_TYPE =  new String("DisabledBreakpoint"); //NOI18N
    public static final String CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE =  new String("CondBreakpoint"); //NOI18N
    public static final String DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE =  new String("DisabledCondBreakpoint"); //NOI18N
    public static final String CURRENT_LINE_ANNOTATION_TYPE =  new String("CurrentPC"); //NOI18N
    public static final String CALL_STACK_FRAME_ANNOTATION_TYPE =  new String("CallSite"); //NOI18N
    public static final String PROP_LINE_NUMBER = new String("lineNumber"); //NOI18N
    
    public static String getAnnotationTooltip(String annotationType) {
        return getMessage("TOOLTIP_"+ annotationType);
    }
    
    private static String getMessage(final String key) {
        return NbBundle.getBundle(NbJSEditorUtil.class).getString(key);
    }

    /**
     * Make line in the editor current - shows the line and highlights it
     * appropriately (green-striped by default). Note that editor counts lines
     * from zero.
     *
     * @see unmarkCurrent()
     */
    static void markCurrent(final String filePath, final int lineNumber) {
        markCurrent(getLineAnnotable(filePath, lineNumber));
    }

    private static void markCurrent(final Line line) {
        unmarkCurrent();
        if (line == null) {
            return;
        }
        currentLineDA = new NbJSDebuggerAnnotation(NbJSEditorUtil.CURRENT_LINE_ANNOTATION_TYPE, line);
        showLine(line, true);
    }

    /**
     * Cancel effect of {@link #markCurrent(String, int)} method. I.e. removes
     * annotation, usually the green stripe.
     */
    static void unmarkCurrent() {
        if (currentLineDA != null) {
            currentLineDA.detach();
            currentLineDA = null;
        }
    }

    public static void openFileObject(FileObject fileObject) {
        if (fileObject == null) {
            return;
        }
        
        try {
            DataObject dataObject = DataObject.find(fileObject);
            EditorCookie cookie = (EditorCookie) dataObject.getCookie(EditorCookie.class);
            cookie.open();
        } catch (DataObjectNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static Line getLineAnnotable(final String filePath,
            final int lineNumber) {
        Line line = getLine(filePath, lineNumber);
        return line;
    }

    public static Line getLine(final String filePath, final int lineNumber) {
        if (filePath == null || lineNumber < 0) {
            return null;
        }

        File file = new File(filePath);
        FileObject fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if (fileObject == null) {
            NbJSUtil.info("Cannot resolve \"" + filePath + '"');
            return null;
        }

        LineCookie lineCookie = getLineCookie(fileObject);
        assert lineCookie != null;
        return lineCookie.getLineSet().getCurrent(lineNumber);
    }

    public static Line getLine(final FileObject fileObject, final int lineNumber) {
        if (fileObject != null) {
            LineCookie lineCookie = NbJSEditorUtil.getLineCookie(fileObject);
            if (lineCookie != null) {
                Line.Set ls = lineCookie.getLineSet();
                if (ls != null) {
                    return ls.getCurrent(lineNumber - 1);
                }
            }
        }
        return null;
    }

    public static Line getLine(final NbJSDebugger debugger, final JSCallStackFrame frame) {
        JSLocation location = frame.getLocation();
        JSSource source = JSFactory.createJSSource(location.getURI().toString());
        FileObject fileObject = debugger.getFileObjectForSource(source);
        return NbJSEditorUtil.getLine(fileObject, frame.getLineNumber());
    }

    public static LineCookie getLineCookie(final FileObject fo) {
        LineCookie result = null;
        try {
            DataObject dataObject = DataObject.find(fo);
            if (dataObject != null) {
                result = dataObject.getCookie(LineCookie.class);
            }
        } catch (DataObjectNotFoundException e) {
            NbJSUtil.LOGGER.log(Level.FINE,
                    "Cannot find DataObject for: " + fo, e.getMessage());
        }
        return result;
    }

    public static void showLine(final Line line, final boolean toFront) {
        if (line == null) {
            return;
        }

        EventQueue.invokeLater(new Runnable() {

            public void run() {
                line.show(toFront ? Line.SHOW_TOFRONT : Line.SHOW_GOTO);
            }
        });
    }

    public static void showLine(final Line line) {
        showLine(line, false);
    }

    // <editor-fold defaultstate="collapsed" desc=" Editor boilerplate ">
    /**
     * Returns current editor line (the line where the caret currently is).
     * Might be <code>null</code>. Works only for
     * {@link NbJSUtil#isJavascriptSource supported mime-types}. For unsupported
     * ones returns <code>null</code>.
     */
    public static Line getCurrentLine() {
        FileObject fo = EditorContextDispatcher.getDefault().getCurrentFile();
        if (fo == null) {
            return null;
        }
        if (!NbJSEditorUtil.isJavascriptSource(fo) && !NbJSEditorUtil.isHTMLSource(fo)) {
            return null;
        }
        return EditorContextDispatcher.getDefault().getCurrentLine();
    }

    // </editor-fold>
    public static boolean isHTMLSource(final FileObject fo) {
        return HTML_MIME_TYPE.equals(fo.getMIMEType());
    }

    /**
     * Supported mime-types:
     *
     * <ul>
     * <li>text/javascript</li>
     * </ul>
     */
    public static boolean isJavascriptSource(final FileObject fo) {
        return JAVASCRIPT_MIME_TYPE.equals(fo.getMIMEType());
    }
	

    /**
     * Goes to editor location
     * @param fileObject
     * @param lineNumber - assumes index starts at 1 instead of 0.
     */
    public static final void goToSource(FileObject fileObject, int lineNumber) {
        Line line = NbJSEditorUtil.getLine(fileObject.getPath(), lineNumber - 1);
        NbJSEditorUtil.showLine(line);
    }
    
    public static final Action createDebuggerGoToAction ( final NbJSDebugger debugger ) {
        Models.ActionPerformer actionPerform =  new Models.ActionPerformer () {
            public boolean isEnabled (Object object) {
                return true;
            }
            public void perform (Object[] nodes) {
                Object node = nodes[0];
                if( node instanceof JSWindow ){
                    JSWindow window = (JSWindow)node;
                    String strURI = window.getURI();
                    JSSource source = JSFactory.createJSSource(strURI);
                    NbJSEditorUtil.openFileObject(debugger.getFileObjectForSource(source));                    
                } else if( node instanceof JSSource ){
                    JSSource jsSource = (JSSource) node;
                    FileObject fileObject = debugger.getFileObjectForSource(jsSource);
                    NbJSEditorUtil.openFileObject(fileObject);
                } else if ( node instanceof JSCallStackFrame ){
                    JSCallStackFrame frame = ((JSCallStackFrame)node);
                    JSLocation jsLoc = frame.getLocation();
                    FileObject fileObject = debugger.getFileObjectForSource(JSFactory.createJSSource(jsLoc.getURI().toString()));
                    if ( fileObject != null ) {
                        showLine(getLine(fileObject, jsLoc.getLineNumber() ), true);
                    }
                }
            }
        };
        return Models.createAction(
                NbBundle.getMessage(NbJSBreakpointNodeActions.class, "CTL_GoToSource"),
                actionPerform, Models.MULTISELECTION_TYPE_EXACTLY_ONE);
    }
    
    public static final Action createDebuggerGoToClientSourceAction ( final NbJSDebugger debugger ) {
        Models.ActionPerformer actionPerform =  new Models.ActionPerformer () {
            public boolean isEnabled (Object object) {
                return true;
            }
            public void perform (Object[] nodes) {
                Object node = nodes[0];
                if( node instanceof JSSource ){
                    JSSource jsSource = (JSSource) node;
                    FileObject fileObject = debugger.getFileObjectForSource(jsSource);
                    if (!(fileObject instanceof URLFileObject)) {
                        fileObject = debugger.getURLFileObjectForSource(jsSource);
                    }
                    NbJSEditorUtil.openFileObject(fileObject);
                } 
            }
        };
        return Models.createAction(
                NbBundle.getMessage(NbJSEditorUtil.class, "CTL_GoToClientSource"),
                actionPerform, Models.MULTISELECTION_TYPE_EXACTLY_ONE);
    }
    
    public static final Action createGoToAction ( ) {
        Models.ActionPerformer actionPerform =  new Models.ActionPerformer () {
            public boolean isEnabled (Object object) {
                return true;
            }
            public void perform (Object[] nodes) {
                Object node1 = nodes[0];
                if( node1 instanceof NbJSBreakpoint ){
                    NbJSBreakpoint bp = (NbJSBreakpoint)node1;
                    Line line = bp.getLine();
                    if ( line != null ) {
                        showLine(line, true);
                    } else {
                        assert bp instanceof NbJSURIBreakpoint; /* Just for now we know it must be a NbJSURIBreakpoint.  Later this will change.*/
                        DialogDescriptor dialogDescriptor = new DialogDescriptor(null, "Can't open without a Client-Side Debugger Session Running.");
                        java.awt.Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
                        dialog.setVisible(true);
                    }
                } 
            }
        };
        return Models.createAction(
                NbBundle.getMessage(NbJSBreakpointNodeActions.class, "CTL_GoToSource"),
                actionPerform, Models.MULTISELECTION_TYPE_EXACTLY_ONE);
    }

}
