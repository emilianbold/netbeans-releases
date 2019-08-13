/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.cnd.diagnostics.clank.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.cnd.diagnostics.clank.ui.codesnippet.ClankDiagnosticsDetailsTopComponent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.clang.tools.services.ClankDiagnosticInfo;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.diagnostics.clank.ClankCsmErrorInfo;
import org.netbeans.modules.cnd.diagnostics.clank.ClankErrorPathDetailsProvider;
import org.netbeans.modules.cnd.diagnostics.clank.impl.ClankCsmErrorInfoAccessor;
import org.netbeans.modules.cnd.diagnostics.clank.ui.tooltip.ToolTipUI;
import org.netbeans.modules.cnd.diagnostics.clank.ui.tooltip.ViewFactory;
import org.netbeans.modules.cnd.diagnostics.clank.ui.views.DiagnosticsAnnotationProvider;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author masha
 */
@ServiceProvider(service = ClankErrorPathDetailsProvider.class, position = 1000)
public class ClankErrorPathDetailsProviderImpl implements ClankErrorPathDetailsProvider {

    @Override
    public void implement(final ClankCsmErrorInfo errorInfo) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DiagnosticsAnnotationProvider.setCurrentDiagnostic( ClankCsmErrorInfoAccessor.getDefault().getDelegate(errorInfo));
                final ClankDiagnosticsDetailsTopComponent details = ClankDiagnosticsDetailsTopComponent.findInstance();
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            CsmFile csmErrorFile = ClankCsmErrorInfoAccessor.getDefault().getCsmFile(errorInfo);                            
                            FileSystem fSystem = csmErrorFile.getFileObject().getFileSystem();
                            DiagnosticsAnnotationProvider.clearAll();
                            final ClankDiagnosticInfo problem = ClankCsmErrorInfoAccessor.getDefault().getDelegate(errorInfo);
                            pinCreation(fSystem, problem, details);
                            for (ClankDiagnosticInfo note : problem.notes()) {
                                pinCreation(fSystem, note, details);
                            }
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    details.setData(errorInfo);
                                    details.open();
                                    details.requestActive();                                                                        
                                }
                            });
                            
                        } catch (FileStateInvalidException | BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                    }

                }
                );

            }

        }
        );

    }

    private void pinCreation(FileSystem fSystem, ClankDiagnosticInfo note, final PropertyChangeListener listener ) throws BadLocationException {
        FileObject fo = CndFileUtils.toFileObject(fSystem, note.getSourceFileName());
        CsmFile csmNoteFile = CsmUtilities.getCsmFile(fo, true, false);
        JTextComponent textComponent = EditorRegistry.findComponent(CsmUtilities.getDocument(csmNoteFile));
        JEditorPane editorPane = null;
        //JEditorPane editorPane =
        if (textComponent instanceof JEditorPane) {
            editorPane = ((JEditorPane) textComponent);
        }
        final JEditorPane ep = editorPane;
        Document document = ep.getDocument();
        if (ep == null) {
            return;
        }
        int offset = note.getEndOffsets()[0];
        if (document instanceof LineDocument) {
            offset = LineDocumentUtils.getNextNonWhitespace((LineDocument) document, offset);
        }
        final int locOffset = offset;
        final int[] lineColumnByOffset = CsmFileInfoQuery.getDefault().getLineColumnByOffset(csmNoteFile, note.getStartOffsets()[0]);
        //find last symbol at the line
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                EditorUI eui = Utilities.getEditorUI(ep);
                if (eui == null) {
                    return;
                }
                ToolTipUI noteTooltip = ViewFactory.getDefault().createToolTip(note.getMessage(),
                        null,
                        new ToolTipUI.Pinnable(note.getMessage(), lineColumnByOffset[0] - 1, note),
                        listener);
                noteTooltip.pin(ep, locOffset);
            }
        });
    }
}
