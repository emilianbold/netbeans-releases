/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.clientproject.ui.action;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Date;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.web.clientproject.RemoteFilesCache;
import org.netbeans.modules.web.clientproject.ui.ClientSideProjectLogicalView;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 */
public class OpenRemoteFileAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        ClientSideProjectLogicalView.RemoteFile remoteFile = activatedNodes[0].getLookup().lookup(ClientSideProjectLogicalView.RemoteFile.class);
        try {
            FileObject fo = RemoteFilesCache.getDefault().getRemoteFile(remoteFile.getUrl());
            for (TopComponent tc : WindowManager.getDefault().getRegistry().getOpened()) {
                String tooltip = tc.getToolTipText();
                if (tooltip != null && tooltip.equals(remoteFile.getDescription())) {
                    tc.requestActive();
                    return;
                }
            }
            ViewEnv env = new ViewEnv(fo);
            CloneableEditorSupport ces = new ViewCES(env, remoteFile.getName(), remoteFile.getDescription(), FileEncodingQuery.getEncoding(fo)); // NOI18N
            ces.view();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        return activatedNodes.length > 0 && activatedNodes[0].getLookup().lookup(ClientSideProjectLogicalView.RemoteFile.class) != null;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    @Override
    public String getName() {
        return "Open";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    private static class ViewEnv implements CloneableEditorSupport.Env, FileChangeListener {

        private final FileObject    file;
        private static final long serialVersionUID = -5788777967029507963L;
        private PropertyChangeSupport support = new PropertyChangeSupport(this);

        public ViewEnv(FileObject file) {
            this.file = file;
            this.file.addFileChangeListener(this);
        }

        @Override
        public InputStream inputStream() throws IOException {
            if (file.getSize() == 0) {
                return new ByteArrayInputStream("Remote document is being downloaded...".getBytes());
            }
            return file.getInputStream();
        }

        @Override
        public OutputStream outputStream() throws IOException {
            throw new IOException();
        }

        @Override
        public Date getTime() {
            return file.lastModified();
        }

        @Override
        public String getMimeType() {
            return file.getMIMEType();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            support.addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            support.removePropertyChangeListener(l);
        }

        @Override
        public void addVetoableChangeListener(VetoableChangeListener l) {
        }

        @Override
        public void removeVetoableChangeListener(VetoableChangeListener l) {
        }

        @Override
        public boolean isValid() {
            return file.isValid();
        }

        @Override
        public boolean isModified() {
            return false;
        }

        @Override
        public void markModified() throws IOException {
            throw new IOException();
        }

        @Override
        public void unmarkModified() {
        }

        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            return null;
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
        }

        @Override
        public void fileChanged(FileEvent fe) {
            support.firePropertyChange(CloneableEditorSupport.Env.PROP_TIME, null, null);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
        
    }

    private static class ViewCES extends CloneableEditorSupport {

        private final String name;
        private final Charset charset;
        private String tooltip;

        public ViewCES(CloneableEditorSupport.Env env, String name, String tooltip, Charset charset) {
            super(env);
            this.name = name;
            this.charset = charset;
            this.tooltip = tooltip;
        }

        @Override
        protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
            kit.read(new InputStreamReader(stream, charset), doc, 0);
        }

        @Override
        protected String messageSave() {
            return name;
        }

        @Override
        protected String messageName() {
            return name;
        }

        @Override
        protected String messageToolTip() {
            return tooltip;
        }

        @Override
        protected String messageOpening() {
            return name;
        }

        @Override
        protected String messageOpened() {
            return name;
        }

        @Override
        protected boolean asynchronousOpen() {
            return false;
        }
    }
    
}
