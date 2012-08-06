/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.web.javascript.debugger;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.web.clientproject.api.RemoteFileCache;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.netbeans.spi.viewmodel.Models;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public final class MiscEditorUtil {

    public static final String HTML_MIME_TYPE = "text/html";
    public static final String JAVASCRIPT_MIME_TYPE = "text/javascript";
	
    
    public static final String BREAKPOINT_ANNOTATION_TYPE = "Breakpoint"; //NOI18N
    public static final String DISABLED_BREAKPOINT_ANNOTATION_TYPE =  "DisabledBreakpoint"; //NOI18N
    public static final String CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE =  "CondBreakpoint"; //NOI18N
    public static final String DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE =  "DisabledCondBreakpoint"; //NOI18N
    public static final String CURRENT_LINE_ANNOTATION_TYPE =  "CurrentPC"; //NOI18N
    public static final String CALL_STACK_FRAME_ANNOTATION_TYPE =  "CallSite"; //NOI18N
    public static final String PROP_LINE_NUMBER = "lineNumber"; //NOI18N
    
    private static final Logger LOG = Logger.getLogger(MiscEditorUtil.class.getName());
    
    public static String getAnnotationTooltip(String annotationType) {
        return getMessage("TOOLTIP_"+ annotationType);
    }
    
    private static String getMessage(final String key) {
        return NbBundle.getBundle(MiscEditorUtil.class).getString(key);
    }

    public static void openFileObject(FileObject fileObject) {
        if (fileObject == null) {
            return;
        }
        
        try {
            DataObject dataObject = DataObject.find(fileObject);
            EditorCookie cookie = dataObject.getCookie(EditorCookie.class);
            cookie.open();
        } catch (DataObjectNotFoundException e) {
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

        FileObject fileObject = null;
        if (filePath.startsWith("http:") || filePath.startsWith("https:")) {    // NOI18N
            try {
                fileObject = RemoteFileCache.getRemoteFile(URI.create(filePath).toURL());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            File file;
            if (filePath.startsWith("file:/")) {                                // NOI18N
                file = Utilities.toFile(URI.create(filePath));
            } else {
                file = new File(filePath);
            }
            fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        }
        if (fileObject == null) {
            LOG.log(Level.INFO, "Cannot resolve \"{0}\"", filePath);
            return null;
        }

        LineCookie lineCookie = getLineCookie(fileObject);
        if (lineCookie == null) {
            LOG.log(Level.INFO, "No line cookie for \"{0}\"", fileObject);
            return null;
        }
        return lineCookie.getLineSet().getCurrent(lineNumber);
    }

    public static Line getLine(final FileObject fileObject, final int lineNumber) {
        if (fileObject != null) {
            LineCookie lineCookie = MiscEditorUtil.getLineCookie(fileObject);
            if (lineCookie != null) {
                Line.Set ls = lineCookie.getLineSet();
                if (ls != null) {
                    return ls.getCurrent(lineNumber - 1);
                }
            }
        }
        return null;
    }

    public static LineCookie getLineCookie(final FileObject fo) {
        LineCookie result = null;
        try {
            DataObject dataObject = DataObject.find(fo);
            if (dataObject != null) {
                result = dataObject.getCookie(LineCookie.class);
            }
        } catch (DataObjectNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void showLine(final Line line, final boolean toFront) {
        if (line == null) {
            return;
        }

        EventQueue.invokeLater(new Runnable() {

            public void run() {
                line.show(Line.ShowOpenType.REUSE, 
                    toFront ? Line.ShowVisibilityType.FRONT : Line.ShowVisibilityType.FOCUS);
            }
        });
    }

    public static void showLine(final Line line) {
        showLine(line, false);
    }

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
        if (!MiscEditorUtil.isJavascriptSource(fo) && !MiscEditorUtil.isHTMLSource(fo)) {
            return null;
        }
        return EditorContextDispatcher.getDefault().getCurrentLine();
    }

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
        Line line = MiscEditorUtil.getLine(fileObject.getPath(), lineNumber - 1);
        MiscEditorUtil.showLine(line);
    }
    
    public static Action createDebuggerGoToAction () {
        Models.ActionPerformer actionPerform =  new Models.ActionPerformer () {
            @Override
            public boolean isEnabled (Object object) {
                return true;
            }
            @Override
            public void perform (Object[] nodes) {
                Object node = nodes[0];
                /*if( node instanceof JSWindow ){
                    JSWindow window = (JSWindow)node;
                    String strURI = window.getURI();
                    JSSource source = JSFactory.createJSSource(strURI);
                    MiscEditorUtil.openFileObject(debugger.getFileObjectForSource(source));                    
                } else if( node instanceof JSSource ){
                    JSSource jsSource = (JSSource) node;
                    FileObject fileObject = debugger.getFileObjectForSource(jsSource);
                    MiscEditorUtil.openFileObject(fileObject);
                } else*/ if ( node instanceof CallFrame ){
                    CallFrame cf = ((CallFrame)node);
                    Line line = MiscEditorUtil.getLine(cf.getScript().getURL(), cf.getLineNumber());
                    if ( line != null ) {
                        showLine(line, true);
                    }
                }
            }
        };
        return Models.createAction(
                NbBundle.getMessage(MiscEditorUtil.class, "CTL_GoToSource"),
                actionPerform, Models.MULTISELECTION_TYPE_EXACTLY_ONE);
    }
    
}
