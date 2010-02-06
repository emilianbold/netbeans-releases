/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.refactoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Position.Bias;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.csl.spi.support.ModificationResult.Difference;
import org.netbeans.modules.css.editor.Css;
import org.netbeans.modules.css.editor.CssProjectSupport;
import org.netbeans.modules.css.indexing.CssFileModel;
import org.netbeans.modules.css.indexing.CssFileModel.Entry;
import org.netbeans.modules.css.indexing.CssIndex;
import org.netbeans.modules.css.indexing.DependenciesGraph;
import org.netbeans.modules.css.parser.CssParserTreeConstants;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public class CssRenameRefactoringPlugin implements RefactoringPlugin {

    private static final Logger LOGGER = Logger.getLogger(CssRenameRefactoringPlugin.class.getSimpleName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    private RenameRefactoring refactoring;

    public CssRenameRefactoringPlugin(RenameRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    public void cancelRequest() {
        //no-op
    }

    @Override
    public Problem prepare(final RefactoringElementsBag refactoringElements) {
        Lookup lookup = refactoring.getRefactoringSource();
        CssElementContext context = lookup.lookup(CssElementContext.class);

        if (context instanceof CssElementContext.Editor) {
            //find all occurances of selected element in (and only in) THIS file.
            CssElementContext.Editor econtext = (CssElementContext.Editor) context;
            //get selected element in the editor
            SimpleNode element = econtext.getElement();
            String elementImage = element.image();

            CssProjectSupport sup = CssProjectSupport.findFor(context.getFileObject());
            if (sup == null) {
                return null;
            }
            CssIndex index = sup.getIndex();
            ModificationResult modificationResult = new ModificationResult();
            if (element.kind() == CssParserTreeConstants.JJT_CLASS
                    || element.kind() == CssParserTreeConstants.JJTHASH) {
                //class or id refactoring
                elementImage = elementImage.substring(1); //cut off the dot or hash
                Collection<FileObject> files = element.kind() == CssParserTreeConstants.JJT_CLASS
                        ? index.findClasses(elementImage)
                        : index.findIds(elementImage);

                List<FileObject> involvedFiles = new LinkedList<FileObject>(files);
                DependenciesGraph deps = index.getDependencies(context.getFileObject());
                Collection<FileObject> relatedFiles = deps.getAllRelatedFiles();

                //refactor all occurances support
                CssRefactoringExtraInfo extraInfo =
                    lookup.lookup(CssRefactoringExtraInfo.class);

                if(extraInfo == null || !extraInfo.isRefactorAll()) {
                    //if the "refactor all occurances" checkbox hasn't been
                    //selected the occurances must be searched only in the related files

                    //filter out those files which have no relation with the current file.
                    //note: the list of involved files also contains the currently edited file.
                    involvedFiles.retainAll(relatedFiles);
                    //now we have a list of files which contain the given class or id and are
                    //related to the base file
                }
                
                if (LOG) {
                    LOGGER.fine("Refactoring element " + element.image() + " in file " + context.getFileObject().getPath()); //NOI18N
                    LOGGER.fine("Involved files declaring the element " + element.image() + ":"); //NOI18N
                    for (FileObject fo : involvedFiles) {
                        LOGGER.fine(fo.getPath() + "\n"); //NOI18N
                    }
                }

                String newName = refactoring.getNewName().substring(1); //cut off the dot or hash
                //make css simple models for all involved files 
                //where we already have the result
                for (FileObject file : involvedFiles) {
                    try {
                        Source source;
                        CloneableEditorSupport editor = Css.findCloneableEditorSupport(file);
                        //prefer using editor
                        //XXX this approach doesn't match the dependencies graph
                        //which is made strictly upon the index data
                        if (editor != null && editor.isModified()) {
                            source = Source.create(editor.getDocument());
                        } else {
                            source = Source.create(file);
                        }

                        CssFileModel model = new CssFileModel(source);
                        Collection<Entry> entries = element.kind() == CssParserTreeConstants.JJT_CLASS
                                ? model.getClasses() : model.getIds();

                        boolean related = relatedFiles.contains(file);

                        List<Difference> diffs = new ArrayList<Difference>();
                        for (Entry entry : entries) {
                            if (entry.isValidInSourceDocument() && elementImage.equals(entry.getName())) {
                                diffs.add(new Difference(Difference.Kind.CHANGE,
                                        editor.createPositionRef(entry.getDocumentRange().getStart(), Bias.Forward),
                                        editor.createPositionRef(entry.getDocumentRange().getEnd(), Bias.Backward),
                                        entry.getName(),
                                        newName,
                                        related ? 
                                            NbBundle.getMessage(CssRenameRefactoringPlugin.class, "MSG_Rename_Selector") :
                                            NbBundle.getMessage(CssRenameRefactoringPlugin.class, "MSG_Rename_Unrelated_Selector")
                                            )); //NOI18N
                            }
                        }
                        modificationResult.addDifferences(file, diffs);

                    } catch (ParseException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

            } else if (element.kind() == CssParserTreeConstants.JJTELEMENTNAME) {
                //type selector: div
                //we do refactor only elements in the current css file, and even this is questionable if makes much sense
                CssFileModel model = new CssFileModel(econtext.getParserResult());
                List<Difference> diffs = new ArrayList<Difference>();
                CloneableEditorSupport editor = Css.findCloneableEditorSupport(context.getFileObject());
                for(Entry entry : model.getHtmlElements()) {
                    if(entry.isValidInSourceDocument() && elementImage.equals(entry.getName())) {
                        diffs.add(new Difference(Difference.Kind.CHANGE,
                                        editor.createPositionRef(entry.getDocumentRange().getStart(), Bias.Forward),
                                        editor.createPositionRef(entry.getDocumentRange().getEnd(), Bias.Backward),
                                        entry.getName(),
                                        refactoring.getNewName(),
                                        NbBundle.getMessage(CssRenameRefactoringPlugin.class, "MSG_Rename_Selector"))); //NOI18N
                    }
                }
                modificationResult.addDifferences(context.getFileObject(), diffs);

            } else {
                //other nodes which may appear under the simple selector node
                //we do not refactor them
            }

            refactoringElements.registerTransaction(new RetoucheCommit(Collections.singletonList(modificationResult)));

            for (FileObject fo : modificationResult.getModifiedFileObjects()) {
                for (Difference diff : modificationResult.getDifferences(fo)) {
                    refactoringElements.add(refactoring, DiffElement.create(diff, fo, modificationResult));

                }
            }
        } else if (context instanceof CssElementContext.File) {
            //refactor a file in explorer
            CssElementContext.File fileContext = (CssElementContext.File) context;
            LOGGER.info("refactor file " + fileContext.getFileObject().getPath()); //NOI18N
        } else if (context instanceof CssElementContext.Folder) {
            //refactor a folder in explorer
            CssElementContext.Folder fileContext = (CssElementContext.Folder) context;
            LOGGER.info("refactor folder " + fileContext.getFileObject().getPath()); //NOI18N
        }

        return null;
    }
}
