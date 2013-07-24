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

package org.netbeans.modules.web.javascript.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.api.RemoteFileCache;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.netbeans.modules.web.javascript.debugger.MiscEditorUtil;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.text.Line;
import org.openide.util.WeakListeners;


public class LineBreakpoint extends AbstractBreakpoint {
    
    /**
     * This property is fired when a new Line object is set to this breakpoint.
     */
    public static final String PROP_LINE = "line";      // NOI18N
    /**
     * This property is fired when a line number of the breakpoint's Line object changes.
     * It's the same as listening on the current Line object's {@link Line#PROP_LINE_NUMBER} events.
     */
    public static final String PROP_LINE_NUMBER = "lineNumber";      // NOI18N
    /**
     * This property is fired when the file changes.
     * Please note that the Line object may stay the same when the file is renamed.
     */
    public static final String PROP_FILE = "fileChanged";           // NOI18N

    private Line myLine;
    private final FileRemoveListener myListener = new FileRemoveListener();
    private FileChangeListener myWeakListener;
    private final LineChangesListener lineChangeslistener = new LineChangesListener();
    private PropertyChangeListener lineChangesWeak;

    public LineBreakpoint(Line line) {
        myLine = line;
        lineChangesWeak = WeakListeners.propertyChange(lineChangeslistener, line);
        line.addPropertyChangeListener(lineChangesWeak);
        FileObject fileObject = line.getLookup().lookup(FileObject.class);
        if( fileObject != null ){
            myWeakListener = WeakListeners.create( 
                    FileChangeListener.class, myListener, fileObject);
            fileObject.addFileChangeListener( myWeakListener );
        }
    }

    public final void setValid(String message) {
        setValidity(VALIDITY.VALID, message);
    }

    public final void setInvalid(String message) {
        setValidity(VALIDITY.INVALID, message);
    }
    
    final void resetValidity() {
        setValidity(VALIDITY.UNKNOWN, null);
    }


    public Line getLine() {
        return myLine;
    }
    
    public void setLine(Line line) {
        removed();
        Line oldLine = myLine;
        myLine = line;
        lineChangesWeak = WeakListeners.propertyChange(lineChangeslistener, line);
        line.addPropertyChangeListener(lineChangesWeak);
        FileObject fileObject = line.getLookup().lookup(FileObject.class);
        if (fileObject != null) {
            myWeakListener = WeakListeners.create(
                    FileChangeListener.class, myListener, fileObject);
            fileObject.addFileChangeListener(myWeakListener);
        }
        firePropertyChange(PROP_LINE, oldLine, line);
    }
    
    void setLine(int lineNumber) {
        if (myLine.getLineNumber() == lineNumber) {
            return ;
        }
        LineCookie lineCookie = myLine.getLookup().lookup(LineCookie.class);
        Line line = lineCookie.getLineSet().getCurrent(lineNumber);
        setLine(line);
    }
    
//    @Override
//    public int isTemp() {
//        return 0;
//    }
//    
//    @Override
//    public boolean isSessionRelated( DebugSession session ){
//        SessionId id = session != null ? session.getSessionId() : null;
//        if ( id == null ){
//            return false;
//        }
//        Project project = id.getProject();
//        if ( project == null ){
//            return false;
//        }
//        return true;
//    }
    
    @Override
    public void removed() {
        Line line = getLine();
        line.removePropertyChangeListener(lineChangesWeak);
        lineChangesWeak = null;
        FileObject fileObject = line.getLookup().lookup(FileObject.class);
        if( fileObject != null ){
            fileObject.removeFileChangeListener( myWeakListener );
            myWeakListener = null;
        }
    }

    /**
     * Difference from getURLString method is that project's local file URL 
     * (eg file://myproject/src/foo.html) is not converted into project's
     * deployment URL (ie http://localhost/smth/foo.html). When persisting 
     * breakpoints they should always be persisted in form of project's local
     * file URL.
     */
    String getURLStringToPersist() {
        return getURLStringImpl(null, null, false);
    }
    
    /**
     * See also getURLStringToPersist().
     */
    String getURLString(Project p, URL urlConnectionBeingDebugged) {
        return getURLStringImpl(p, urlConnectionBeingDebugged, true);
    }
    
    private String getURLStringImpl(Project p, URL urlConnectionBeingDebugged, boolean applyInternalServerMapping) {
        FileObject fo = getLine().getLookup().lookup(FileObject.class);
        String url;
        URL remoteURL = RemoteFileCache.isRemoteFile(fo);
        if (remoteURL == null) {
            // should "file://foo.bar" be translated into "http://localhost/smth/foo.bar"?
            if (applyInternalServerMapping && p != null) {
                assert urlConnectionBeingDebugged != null;
                URL internalServerURL = ServerURLMapping.toServer(p, ServerURLMapping.CONTEXT_PROJECT_SOURCES, fo);
                boolean useTestingContext = false;
                if (internalServerURL == null) {
                    useTestingContext = true;
                } else {
                    if (!internalServerURL.getHost().equals(urlConnectionBeingDebugged.getHost()) ||
                            internalServerURL.getPort() != urlConnectionBeingDebugged.getPort()) {
                        // if FileObject was resolved to a server which is different from current
                        // debugging connection then try to resolve the FileObject 
                        // in ServerURLMapping.CONTEXT_PROJECT_TESTS context
                        useTestingContext = true;
                    }
                }
                if (useTestingContext && p != null) {
                    URL internalServerURL2 = ServerURLMapping.toServer(p, ServerURLMapping.CONTEXT_PROJECT_TESTS, fo);
                    if (internalServerURL2 != null && 
                            (internalServerURL2.getHost().equals(urlConnectionBeingDebugged.getHost()) ||
                            internalServerURL2.getPort() == urlConnectionBeingDebugged.getPort())) {
                        // use it:
                        internalServerURL = internalServerURL2;
                    }
                }
                if (internalServerURL != null) {
                    return internalServerURL.toExternalForm();
                }
            }
            url = fo.toURL().toExternalForm();
        } else {
            url = remoteURL.toExternalForm();
        }
        return url;
    }

    private class FileRemoveListener extends FileChangeAdapter {

        /* (non-Javadoc)
         * @see org.openide.filesystems.FileChangeListener#fileDeleted(org.openide.filesystems.FileEvent)
         */
        @Override
        public void fileDeleted( FileEvent arg0 ) {
            DebuggerManager.getDebuggerManager().removeBreakpoint( 
                    LineBreakpoint.this);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            FileObject renamedFo = fe.getFile();
            Line newLine = MiscEditorUtil.getLine(renamedFo, getLine().getLineNumber() + 1);
            if (!newLine.equals(getLine())) {
                setLine(newLine);
            } else {
                firePropertyChange(PROP_FILE, fe.getName(), fe.getFile().getName());
            }
        }

    }
    
    private class LineChangesListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (Line.PROP_LINE_NUMBER.equals(evt.getPropertyName())) {
                firePropertyChange(PROP_LINE_NUMBER, evt.getOldValue(), evt.getNewValue());
            }
        }
        
    }
}
