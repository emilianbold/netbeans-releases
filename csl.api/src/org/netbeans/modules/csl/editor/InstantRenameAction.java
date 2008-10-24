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
package org.netbeans.modules.csl.editor;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.swing.Action;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.modules.csl.api.CancellableTask;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.napi.gsfret.source.CompilationController;
import org.netbeans.modules.csl.api.Phase;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.napi.gsfret.source.SourceUtils;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


/**
 * This file is originally from Retouche, the Java Support
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible.
 *
 *
 * @author Jan Lahoda
 * @author Tor Norbye
 */
public class InstantRenameAction extends BaseAction {
    /** Creates a new instance of InstantRenameAction */
    public InstantRenameAction() {
        super("in-place-refactoring", MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
    }

    public void actionPerformed(ActionEvent evt, final JTextComponent target) {
        try {
            final int caret = target.getCaretPosition();
            String ident = Utilities.getIdentifier(Utilities.getDocument(target), caret);

            if (ident == null) {
                Utilities.setStatusBoldText(target, "Cannot perform rename here.");

                return;
            }
            
            if (SourceUtils.isScanInProgress()) {
                Utilities.setStatusBoldText(target, "Scanning In Progress");

                return;
            }

            Source js = Source.forFileObject(DataLoadersBridge.getDefault().getFileObject(target));
            if (js == null) {
                return;
            }

            final boolean[] wasResolved = new boolean[1];
            final String[] message = new String[1];
            final Set<OffsetRange>[] changePoints = new Set[1];

            js.runUserActionTask(new CancellableTask<CompilationController>() {
                    public void cancel() {
                    }

                    public void run(CompilationController controller)
                        throws Exception {
                        if (controller.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                            return;
                        }

                        Document doc = target.getDocument();
                        BaseDocument baseDoc = (BaseDocument)doc;
                        List<Language> list = LanguageRegistry.getInstance().getEmbeddedLanguages(baseDoc, caret);
                        Language language = null;
                        for (Language l : list) {
                            if (l.getInstantRenamer() != null) {
                                language = l;
                                break;
                            }
                        }
                        
                        if (language != null) {
                            InstantRenamer renamer = language.getInstantRenamer();
                            assert renamer != null;

                            String[] descRetValue = new String[1];

                            if ((renamer == null) ||
                                    !renamer.isRenameAllowed(controller, caret, descRetValue)) {
                                wasResolved[0] = false;
                                message[0] = descRetValue[0];

                                return;
                            }

                            wasResolved[0] = true;

                            Set<OffsetRange> regions = renamer.getRenameRegions(controller, caret);

                            if ((regions != null) && (regions.size() > 0)) {
                                changePoints[0] = regions;
                            }
                        }
                    }
                }, true);

            if (wasResolved[0]) {
                if (changePoints[0] != null) {
                    doInstantRename(changePoints[0], target, caret, ident);
                } else {
                    doFullRename((EditorCookie)DataLoadersBridge.getDefault().getCookie(target,EditorCookie.class), DataLoadersBridge.getDefault().getNodeDelegate(target));
                }
            } else {
                if (message[0] == null) {
                    message[0] = NbBundle.getMessage(InstantRenameAction.class,
                            "InstantRenameDenied");
                }

                Utilities.setStatusBoldText(target, message[0]);
            }
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }

    @Override
    protected Class getShortDescriptionBundleClass() {
        return InstantRenameAction.class;
    }

    private void doInstantRename(Set<OffsetRange> changePoints, JTextComponent target, int caret,
        String ident) throws BadLocationException {
        InstantRenamePerformer.performInstantRename(target, changePoints, caret);
    }

    private void doFullRename(EditorCookie ec, Node n) {
        InstanceContent ic = new InstanceContent();
        ic.add(ec);
        ic.add(n);

        Lookup actionContext = new AbstractLookup(ic);

        Action a =
            RefactoringActionsFactory.renameAction().createContextAwareInstance(actionContext);
        a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
    }
}
