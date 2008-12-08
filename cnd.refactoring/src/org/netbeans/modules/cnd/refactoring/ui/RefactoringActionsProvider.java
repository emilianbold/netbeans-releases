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
package org.netbeans.modules.cnd.refactoring.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelState;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * provides support for refactoring actions
 * 
 * @author Vladimir Voskresensky
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider.class, position = 150)
public class RefactoringActionsProvider extends ActionsImplementationProvider {

    /** Creates a new instance of RefactoringActionsProvider */
    public RefactoringActionsProvider() {
    }

    @Override
    public boolean canFindUsages(Lookup lookup) {
        CsmObject ctx = CsmRefactoringUtils.findContextObject(lookup);
        if (CsmRefactoringUtils.isSupportedReference(ctx)) {
            return true;
        }
        return false;
    }

    @Override
    public void doFindUsages(final Lookup lookup) {
        Runnable task;
        if (isFromEditor(lookup)) {
            task = new TextComponentTask(lookup) {

                @Override
                protected RefactoringUI createRefactoringUI(CsmObject selectedElement, int startOffset, int endOffset) {
                    return new WhereUsedQueryUI(selectedElement);
                }
            };
        } else {
            task = new NodeToElementTask(lookup) {

                protected RefactoringUI createRefactoringUI(CsmObject selectedElement) {
                    return new WhereUsedQueryUI(selectedElement);
                }
            };
        }
        task.run();
    }

    @Override
    public void doRename(final Lookup lookup) {
        Runnable task;
        if (isFromEditor(lookup)) {
            task = new TextComponentTask(lookup) {

                @Override
                protected RefactoringUI createRefactoringUI(CsmObject selectedElement, int startOffset, int endOffset) {
                    return new RenameRefactoringUI(selectedElement);
                }
            };
        } else {
            task = new NodeToElementTask(lookup) {

                @Override
                protected RefactoringUI createRefactoringUI(CsmObject selectedElement) {
                    return new RenameRefactoringUI(selectedElement);
                }
            };
        }
        task.run();
//        RetoucheUtils.invokeAfterScanFinished(task, getActionName(RefactoringActionsFactory.renameAction()));
    }

    private static String getActionName(Action action) {
        String arg = (String) action.getValue(Action.NAME);
        arg = org.openide.util.Utilities.replaceString(arg, "&", ""); // NOI18N
        return org.openide.util.Utilities.replaceString(arg, "...", ""); // NOI18N
    }

    /**
     * returns true if refactorable element is selected
     */
    @Override
    public boolean canRename(Lookup lookup) {
        if (CsmModelAccessor.getModelState() != CsmModelState.ON) {
            return false;
        }
        Set<Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        // only one node can be renamed at once
        if (nodes.size() == 1) {
            CsmObject ctx = CsmRefactoringUtils.findContextObject(lookup);
            if (CsmRefactoringUtils.isSupportedReference(ctx)) {
                return true;
            }
        }
        return false;
    }

    public static abstract class TextComponentTask implements Runnable {

        private JTextComponent textC;
        private int caret;
        private int start;
        private int end;
        private RefactoringUI ui;
        private Lookup lookup;

        public TextComponentTask(Lookup lkp) {
            this.textC = lkp.lookup(EditorCookie.class).getOpenedPanes()[0];
            this.caret = textC.getCaretPosition();
            this.start = textC.getSelectionStart();
            this.end = textC.getSelectionEnd();
            this.lookup = lkp;
            assert caret != -1;
            assert start != -1;
            assert end != -1;
        }

        public final void run() {
            CsmObject ctx = CsmRefactoringUtils.findContextObject(lookup);
            if (!CsmRefactoringUtils.isSupportedReference(ctx)) {
                return;
            }
            ui = createRefactoringUI(ctx, start, end);
            TopComponent activetc = TopComponent.getRegistry().getActivated();

            if (ui != null) {
                UI.openRefactoringUI(ui, activetc);
            } else {
                JOptionPane.showMessageDialog(null, NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_CannotRenameLoc"));
            }
        }

        protected abstract RefactoringUI createRefactoringUI(CsmObject selectedElement, int startOffset, int endOffset);
    }

    public static abstract class NodeToElementTask implements Runnable {

        private Lookup context;
        private RefactoringUI ui;

        public NodeToElementTask(Lookup context) {
            this.context = context;
        }

        public void cancel() {
        }

        public final void run() {
            CsmObject ctx = CsmRefactoringUtils.findContextObject(context);
            if (!CsmRefactoringUtils.isSupportedReference(ctx)) {
                return;
            }
            ui = createRefactoringUI(ctx);
            TopComponent activetc = TopComponent.getRegistry().getActivated();

            if (ui != null) {
                UI.openRefactoringUI(ui, activetc);
            } else {
                JOptionPane.showMessageDialog(null, NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_CannotRenameLoc"));
            }
        }

        protected abstract RefactoringUI createRefactoringUI(CsmObject selectedElement/*RubyElementCtx selectedElement, CompilationInfo info*/);
    }

    public static abstract class NodeToFileObjectTask implements Runnable {

        private Collection<? extends Node> nodes;
        private RefactoringUI ui;
        public NonRecursiveFolder pkg[];
        Collection<CsmObject> handles = new ArrayList<CsmObject>();

        public NodeToFileObjectTask(Collection<? extends Node> nodes) {
            this.nodes = nodes;
        }

        public void cancel() {
        }

        public void run() {
            FileObject[] fobs = new FileObject[nodes.size()];
            pkg = new NonRecursiveFolder[fobs.length];
            int i = 0;
            for (Node node : nodes) {
                DataObject dob = node.getCookie(DataObject.class);
                if (dob != null) {
                    fobs[i] = dob.getPrimaryFile();
//                    Source source = RetoucheUtils.getSource(fobs[i]);
//                    assert source != null;
//                    try {
//                        source.runUserActionTask(this, false);
//                    } catch (IllegalArgumentException ex) {
//                        ex.printStackTrace();
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                    }

                    pkg[i++] = node.getLookup().lookup(NonRecursiveFolder.class);
                }
            }
            UI.openRefactoringUI(createRefactoringUI(fobs, handles));
        }

        protected abstract RefactoringUI createRefactoringUI(FileObject[] selectedElement, Collection<CsmObject> handles);
    }

    static boolean isFromEditor(Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (ec != null && ec.getOpenedPanes() != null) {
            // This doesn't seem to work well - a lot of the time, I'm right clicking
            // on the editor and it still has another activated view (this is on the mac)
            // and as a result does file-oriented refactoring rather than the specific
            // editor node...
            //            TopComponent activetc = TopComponent.getRegistry().getActivated();
            //            if (activetc instanceof CloneableEditorSupport.Pane) {
            //
            return true;
        //            }
        }

        return false;
    }
}
