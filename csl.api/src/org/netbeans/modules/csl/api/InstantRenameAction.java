/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.csl.api;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.parsing.api.Embedding;

import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.editor.InstantRenamePerformer;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.spi.Parser;
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

    public @Override void actionPerformed(ActionEvent evt, final JTextComponent target) {
        try {
            final int caret = target.getCaretPosition();
            String ident = Utilities.getIdentifier(Utilities.getDocument(target), caret);

            if (ident == null) {
                Utilities.setStatusBoldText(target, NbBundle.getMessage(InstantRenameAction.class, "InstantRenameDenied"));
                return;
            }

            if (IndexingManager.getDefault().isIndexing()) {
                Utilities.setStatusBoldText(target, NbBundle.getMessage(InstantRenameAction.class, "scanning-in-progress"));
                return;
            }

            Source js = Source.create (DataLoadersBridge.getDefault().getFileObject(target));
            if (js == null) {
                return;
            }

            final Set<OffsetRange>[] changePoints = new Set[1];

            ParserManager.parse (
                Collections.<Source> singleton (js), 
                new UserTask () {
                    public @Override void run (ResultIterator resultIterator) throws Exception {
                        Map<String, Parser.Result> embeddedResults = new HashMap<String, Parser.Result>();
                        outer:for(;;) {
                            embeddedResults.put(resultIterator.getParserResult().getSnapshot().getMimeType(),
                                    resultIterator.getParserResult());
                            
                            Iterable<Embedding> embeddings = resultIterator.getEmbeddings();
                            for(Embedding e : embeddings) {
                                if(e.containsOriginalOffset(caret)) {
                                    resultIterator = resultIterator.getResultIterator(e);
                                    continue outer;
                                }
                            }
                            break;
                        }

                        BaseDocument baseDoc = (BaseDocument)target.getDocument();
                        List<Language> list = LanguageRegistry.getInstance().getEmbeddedLanguages(baseDoc, caret);
                        for (Language language : list) {
                            if (language.getInstantRenamer() != null) {
                                //the parser result matching with the language is just
                                //mimetype based, it doesn't take mimepath into account,
                                //which I belive is ok here.
                                Parser.Result result = embeddedResults.get(language.getMimeType());
                                if(!(result instanceof ParserResult)) {
                                    return ;
                                }
                                ParserResult parserResult = (ParserResult)result;

                                InstantRenamer renamer = language.getInstantRenamer();
                                assert renamer != null;

                                String[] descRetValue = new String[1];

                                if (!renamer.isRenameAllowed(parserResult, caret, descRetValue)) {
                                    return;
                                }

                                Set<OffsetRange> regions = renamer.getRenameRegions(parserResult, caret);

                                if ((regions != null) && (regions.size() > 0)) {
                                    changePoints[0] = regions;
                                }

                                break; //the for break
                            }
                        }
                    }
                }
            );

            if (changePoints[0] != null) {
                doInstantRename(changePoints[0], target, caret, ident);
            } else {
                doFullRename((EditorCookie)DataLoadersBridge.getDefault().getCookie(target,EditorCookie.class), DataLoadersBridge.getDefault().getNodeDelegate(target));
            }
            
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        } catch (ParseException ioe) {
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
