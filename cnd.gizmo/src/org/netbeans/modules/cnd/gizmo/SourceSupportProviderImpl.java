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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.gizmo;

import org.netbeans.modules.dlight.spi.SourceSupportProvider;
import java.awt.Container;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.JumpList;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.DataEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author mt154047
 */
@ServiceProvider(service = SourceSupportProvider.class, position=200)
public class SourceSupportProviderImpl implements SourceSupportProvider {

    private static String loc(String key, String... arg) {
        return NbBundle.getMessage(SourceSupportProviderImpl.class, key, arg);
    }

    public boolean showSource(SourceFileInfo lineInfo, boolean isReadOnly) {
        FileObject fo = CndFileUtils.toFileObject(CndFileUtils.normalizeAbsolutePath(lineInfo.getFileName()));
        try {
            new ROEditor(fo).open();
        } catch (DataObjectNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static final class ROEditor extends DataEditorSupport {

        private ROEditor(DataObject d) {
            super(d, new E(d));
        }

        public ROEditor(FileObject fo) throws DataObjectNotFoundException {
            this(DataObject.find(fo));
        }
    }

    private static final class E extends DataEditorSupport.Env {

        public E(DataObject d) {
            super(d);
        }

        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        protected FileLock takeLock() throws IOException {
            throw new IOException("No way!"); // NOI18N

        }
    }

    /**
     *
     * @param lineInfo
     */
    public boolean showSource(final SourceFileInfo lineInfo) {
        if (lineInfo == null) {
            StatusDisplayer.getDefault().setStatusText(loc("SourceSupportProviderImpl.NoInfo")); // NOI18N
            return false;
        }

        if (!lineInfo.isSourceKnown()) {
            // Perhaps it's better to show some dialog here?
            // Or, even better to have special GUI icon that indicates availability
            // of source code...
            StatusDisplayer.getDefault().setStatusText(loc("SourceSupportProviderImpl.UnknownSource")); // NOI18N
            return false;
        }
        String fileName = lineInfo.getFileName();
        try {
            FileObject fo = CndFileUtils.toFileObject(fileName);
            if (fo == null || ! fo.isValid()) {
                InputStream inputStream = null;
                try {
                    URI uri = new URI(lineInfo.getFileName());
                    if (uri.getScheme() != null && uri.getScheme().equals("file")) { // NOI18N
                        fo = CndFileUtils.toFileObject(FileUtil.normalizeFile(new File(uri))); // XXX:fullRemote
                    }
                    if (fo == null || !fo.isValid()) {
                        String rowPath = uri.getRawPath();
                        int lastIndexOfSeparator = rowPath.lastIndexOf('/'); // NOI18N
                        if (lastIndexOfSeparator == -1) {
                            return false;
                        }
                        String file = rowPath.substring(lastIndexOfSeparator, rowPath.length());
                        inputStream = uri.toURL().openStream();
                        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                        String line = null;
                        StringBuilder buffer = new StringBuilder();
                        while ((line = in.readLine()) != null) {
                            buffer.append(line + "\n"); // NOI18N

                        }//end while

                        in.close();
                        inputStream.close();
                        //we have file content in buffer
                        fileName = System.getProperty("java.io.tmpdir") + File.separator + file; // NOI18N

                        File tempFile = new File(fileName);
                        tempFile.deleteOnExit();
                        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
                        writer.write(buffer.toString());
                        writer.flush();
                        writer.close();
                        fo = CndFileUtils.toFileObject(CndFileUtils.normalizeFile(tempFile));
                    }
                } catch (Throwable e) {
                    //catch it and show message that it is impossible to open source file
                    Throwable t = ErrorManager.getDefault().annotate(e, loc("SourceSupportProviderImpl.CannotOpenFile", fileName)); // NOI18N
                    ErrorManager.getDefault().notify(ErrorManager.WARNING, t);
                    StatusDisplayer.getDefault().setStatusText(loc("SourceSupportProviderImpl.CannotOpenFile", fileName)); // NOI18N
                    return false;
                    //Exceptions.printStackTrace(e);
                }
            }

            if (fo == null || !fo.isValid()) {
                StatusDisplayer.getDefault().setStatusText(loc("SourceSupportProviderImpl.CannotOpenFile", fileName)); // NOI18N
                return false;
            }

            final DataObject dob = DataObject.find(fo);

            // TODO: this is a hack to make Annotations work, as they 
            // are implemented so that have cache, where key is a path to 
            // a file (to a mirror local file in case of remote).
            // See Bug 193172
            FileObjectsToSourceMap.getInstance().put(fo, lineInfo.getFileName());
            
            final EditorCookie.Observable ec = dob.getCookie(EditorCookie.Observable.class);
            if (ec != null) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        JumpList.checkAddEntry();
                        JEditorPane pane = NbDocument.findRecentEditorPane(ec);
                        boolean opened = true;
                        if (pane == null) {
                            ec.open();
                            opened = false;
                            JEditorPane[] panes = ec.getOpenedPanes();
                            pane = panes != null && panes.length > 0? panes[0] : null ;
                        }
                        if (pane != null){
                            jumpToLine(pane, lineInfo, !opened);
                        }
                    }
                });
                return true;
            }
        } catch (DataObjectNotFoundException e) {
            e.printStackTrace(System.err);
            StatusDisplayer.getDefault().setStatusText(loc("SourceSupportProviderImpl.CannotOpenFile", fileName)); // NOI18N
        }
        return false;
    }

    private static void jumpToLine(final JEditorPane pane, final SourceFileInfo sourceFileInfo, boolean delayProcessing) {
        // try to activate outer TopComponent
        Container temp = pane;
        while (temp != null && !(temp instanceof TopComponent)) {
            temp = temp.getParent();
        }
        if (temp instanceof TopComponent) {
            ((TopComponent) temp).open();
            ((TopComponent) temp).requestActive();
            ((TopComponent) temp).requestVisible();
        }
        // have to be called in EDT, because working with swing objects
        jumpToLine(pane, sourceFileInfo);
    }

    private static void jumpToLine(JEditorPane pane, SourceFileInfo sourceFileInfo) {
        assert SwingUtilities.isEventDispatchThread() : "must be called in EDT";
        long start;
        if (sourceFileInfo.hasOffset()) {
            start = sourceFileInfo.getOffset();
        } else {
            start = Utilities.getRowStartFromLineOffset((BaseDocument) pane.getDocument(), sourceFileInfo.getLine() - 1);
        }

        if (pane.getDocument() != null && start >= 0 && start < pane.getDocument().getLength()) {
            pane.setCaretPosition((int) start);
        }
        StatusDisplayer.getDefault().setStatusText(""); // NOI18N
    }
}
