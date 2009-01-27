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
package org.netbeans.modules.cnd.navigation.macroview;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.model.tasks.CaretAwareCsmFileTaskFactory;
import org.netbeans.modules.cnd.model.tasks.CsmFileTaskFactory;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 * Updates information in macro expansion view if caret position of main document changes and vice versa.
 *
 * @author Nick Ktasilnikov
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.model.tasks.CsmFileTaskFactory.class, position=11)
public class MacroExpansionCaretAwareFactory extends CaretAwareCsmFileTaskFactory {

    public final static String MACRO_EXPANSION_UPDATING_CARET = "macro-expansion-updating-caret"; // NOI18N

    @Override
    protected PhaseRunner createTask(final FileObject fo) {
        return new PhaseRunner() {

            public void run(Phase phase) {
                boolean changed = false;
                Document doc = CsmUtilities.getDocument(fo);
                if (doc == null) {
                    return;
                }
                if (doc.getProperty(MACRO_EXPANSION_UPDATING_CARET) != null) {
                    doc.putProperty(MACRO_EXPANSION_UPDATING_CARET, null);
                    return;
                }
                if (!isMacroExpansionDoc(doc)) {
                    Document doc2 = (Document) doc.getProperty(Document.class);
                    if (doc2 != null && isContextSuncEnabled(doc2)) {
                        changed = CsmMacroExpansion.updateMacroExpansionView(CaretAwareCsmFileTaskFactory.getLastPosition(fo));
                    }
                }
                if(!changed) {
                    syncRelatedDocumentCaretPosition(fo);
                } else {
                    doc.putProperty(MACRO_EXPANSION_UPDATING_CARET, true);
                }
            }

            public boolean isValid() {
                return true;
            }
            
            public void cancel() {
            }

            public boolean isHighPriority() {
                return false;
            }

            private void syncRelatedDocumentCaretPosition(FileObject fo) {
                Document doc = CsmUtilities.getDocument(fo);
                if (doc != null && isCaretSuncEnabledOnCurrentDoc(doc)) {
                    Document doc2 = (Document) doc.getProperty(Document.class);
                    if (doc2 != null && isCaretSuncEnabledOnRelatedDoc(doc2)) {
                        FileObject file2 = CsmUtilities.getFileObject(doc2);
                        if (file2 != null) {
                            int doc2CarretPosition = getDocumentOffset(doc2, getFileOffset(doc, CaretAwareCsmFileTaskFactory.getLastPosition(fo)));
                            if (doc2CarretPosition >= 0 && doc2CarretPosition < doc2.getLength()) {
                                JEditorPane ep = getEditor(doc2);
                                if (ep != null && ep.getCaretPosition() != doc2CarretPosition && !ep.hasFocus()) {
                                    ep.setCaretPosition(doc2CarretPosition);
                                    doc.putProperty(MACRO_EXPANSION_UPDATING_CARET, true);
                                }
                            }
                        }
                    }
                }
            }

            private int getFileOffset(Document doc, int documentOffset) {
                return CsmMacroExpansion.getOffsetInOriginalText(doc, documentOffset);
            }

            private int getDocumentOffset(Document doc, int fileOffset) {
                return CsmMacroExpansion.getOffsetInExpandedText(doc, fileOffset);
            }

            private JEditorPane getEditor(Document doc) {
                Object jEditorPane = doc.getProperty(JEditorPane.class);
                if (jEditorPane != null) {
                    return (JEditorPane) jEditorPane;
                }
                DataObject dobj = NbEditorUtilities.getDataObject(doc);
                if(dobj != null) {
                    EditorCookie ec = dobj.getCookie(EditorCookie.class);
                    JEditorPane jEditorPanes[] = CsmUtilities.getOpenedPanesInEQ(ec);
                    if (jEditorPanes != null && jEditorPanes.length > 0) {
                        return jEditorPanes[0];
                    }
                }
                return null;
            }

            private boolean isMacroExpansionDoc(Document doc) {
                if (doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) == null) {
                    return true;
                }
                return false;
            }

            private boolean isCaretSuncEnabledOnCurrentDoc(Document doc) {
                boolean enable = true;
                if (doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) != null) {
                    Object o = doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_SYNC_CARET);
                    if (o instanceof Boolean) {
                        enable &= (Boolean) o;
                    }
                }
                Object obj = doc.getProperty(CsmFileTaskFactory.USE_OWN_CARET_POSITION);
                if (obj != null) {
                    enable &= (Boolean) obj;
                }
                return enable;
            }

            private boolean isCaretSuncEnabledOnRelatedDoc(Document doc) {
                boolean enable = true;
                if (doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) != null) {
                    Object o = doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_SYNC_CARET);
                    if (o instanceof Boolean) {
                        enable &= (Boolean) o;
                    }
                }
                return enable;
            }

            private boolean isContextSuncEnabled(Document doc) {
                if (doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) != null) {
                    Object o = doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_SYNC_CONTEXT);
                    if (o instanceof Boolean) {
                        return (Boolean) o;
                    }
                }
                return false;
            }
        };
    }

    @Override
    protected int taskDelay() {
        return 0;
    }

    @Override
    protected int rescheduleDelay() {
        return 0;
    }
}
