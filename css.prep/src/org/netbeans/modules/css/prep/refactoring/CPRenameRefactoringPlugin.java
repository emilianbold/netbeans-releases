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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.prep.refactoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Position.Bias;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.csl.spi.support.ModificationResult.Difference;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.refactoring.api.Entry;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.netbeans.modules.css.refactoring.api.RefactoringElementType;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CPRenameRefactoringPlugin implements RefactoringPlugin {

    private static final String SELECTOR_RENAME_MSG_KEY = "MSG_Rename_Selector"; //NOI18N
    private static final String COLOR_RENAME_MSG_KEY = "MSG_Rename_Color"; //NOI18N
    private static final String UNRELATED_PREFIX_MSG_KEY = "MSG_Unrelated_Prefix"; //NOI18N

    private static final Logger LOGGER = Logger.getLogger(CPRenameRefactoringPlugin.class.getSimpleName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    private RenameRefactoring refactoring;
    private Lookup lookup;
    private RefactoringElementContext context;

    public CPRenameRefactoringPlugin(RenameRefactoring refactoring) {
        this.refactoring = refactoring;
        this.lookup = refactoring.getRefactoringSource();
        this.context = lookup.lookup(RefactoringElementContext.class);
    }

    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        String newName = refactoring.getNewName();
        if(newName.length() == 0) {
            return new Problem(true, NbBundle.getMessage(CPRenameRefactoringPlugin.class, "MSG_Error_ElementEmpty")); //NOI18N
        }

//        if(context instanceof CssElementContext.Editor) {
//            CssElementContext.Editor editorContext = (CssElementContext.Editor)context;
//            char firstChar = refactoring.getNewName().charAt(0);
//            switch(editorContext.getElement().type()) {
//                case cssId:
//                case hexColor:
//                    //hex color code
//                    //id
//                    if(firstChar != '#') {
//                        return new Problem(true, NbBundle.getMessage(CssRenameRefactoringPlugin.class, "MSG_Error_MissingHash")); //NOI18N
//                    }
//                    break;
//                case cssClass:
//                    //class
//                    if(firstChar != '.') {
//                        return new Problem(true, NbBundle.getMessage(CssRenameRefactoringPlugin.class, "MSG_Error_MissingDot")); //NOI18N
//                    }
//                    break;
//            }
//            if(newName.length() == 1) {
//                return new Problem(true, NbBundle.getMessage(CssRenameRefactoringPlugin.class, "MSG_Error_ElementShortName")); //NOI18N
//            }
//
//        }

        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        return checkParameters();
    }

    @Override
    public void cancelRequest() {
        //no-op
    }

    @Override
    public Problem prepare(final RefactoringElementsBag refactoringElements) {
        CssIndex index = null;
        ModificationResult modificationResult = new ModificationResult();

            //get selected element in the editor
            NodeType kind = context.getElement().type();
            String elementImage = context.getElementName();
//
//            if (kind == NodeType.cssClass) {
//                int elementPrefixLength = 1;
//                elementImage = elementImage.substring(elementPrefixLength); //cut off the dot
//                Collection<FileObject> files = index.findClasses(elementImage);
//                refactor(lookup, modificationResult, RefactoringElementType.CLASS, files, elementPrefixLength, context, index, SELECTOR_RENAME_MSG_KEY);
//            } else if (kind == NodeType.cssId) {
//                int elementPrefixLength = 1;
//                elementImage = elementImage.substring(elementPrefixLength); //cut off the dot
//                Collection<FileObject> files = index.findIds(elementImage);
//                refactor(lookup, modificationResult, RefactoringElementType.ID, files, elementPrefixLength, context, index, SELECTOR_RENAME_MSG_KEY);
//            } else if (kind == NodeType.hexColor) {
//                Collection<FileObject> files = index.findColor(elementImage);
//                refactor(lookup, modificationResult, RefactoringElementType.COLOR, files, 0, context, index, COLOR_RENAME_MSG_KEY);
//            } else if (kind == NodeType.elementName) {
//                refactorElement(modificationResult, context, index);
//            } else {
//                //other nodes which may appear under the simple selector node
//                //we do not refactor them
//            }

        
        return null;
    }

 

    private void refactor(Lookup lookup, ModificationResult modificationResult, RefactoringElementType type, Collection<FileObject> files, int elementPrefixLenght, RefactoringElementContext context, CssIndex index, String renameMsgKey) {
        String elementImage = context.getElementName().substring(elementPrefixLenght);
        List<FileObject> involvedFiles = new LinkedList<FileObject>(files);
        DependenciesGraph deps = index.getDependencies(context.getFileObject());
        Collection<FileObject> relatedFiles = deps.getAllRelatedFiles();


        if (LOG) {
            LOGGER.log(Level.FINE, "Refactoring element {0} in file {1}", new Object[]{elementImage, context.getFileObject().getPath()}); //NOI18N
            LOGGER.log(Level.FINE, "Involved files declaring the element {0}:", elementImage); //NOI18N
            for (FileObject fo : involvedFiles) {
                LOGGER.log(Level.FINE, "{0}\n", fo.getPath()); //NOI18N
            }
        }

        String newName = refactoring.getNewName().substring(elementPrefixLenght); //cut off the dot or hash
        //make css simple models for all involved files
        //where we already have the result
        for (FileObject file : involvedFiles) {
            try {
                Source source;
                CloneableEditorSupport editor = GsfUtilities.findCloneableEditorSupport(file);
                //prefer using editor
                //XXX this approach doesn't match the dependencies graph
                //which is made strictly upon the index data
                if (editor != null && editor.isModified()) {
                    source = Source.create(editor.getDocument());
                } else {
                    source = Source.create(file);
                }

//                CssFileModel model = CssFileModel.create(source);
//                Collection<Entry> entries = model.get(type);
                Collection<Entry> entries = null;

                boolean related = relatedFiles.contains(file);

                List<Difference> diffs = new ArrayList<Difference>();
                for (Entry entry : entries) {
                    if (entry.isValidInSourceDocument() && 
                            LexerUtils.equals(elementImage, entry.getName(), type == RefactoringElementType.COLOR, false)) {
                        diffs.add(new Difference(Difference.Kind.CHANGE,
                                editor.createPositionRef(entry.getDocumentRange().getStart(), Bias.Forward),
                                editor.createPositionRef(entry.getDocumentRange().getEnd(), Bias.Backward),
                                entry.getName(),
                                newName,
                                related
                                ? NbBundle.getMessage(CPRenameRefactoringPlugin.class, renameMsgKey)
                                : NbBundle.getMessage(CPRenameRefactoringPlugin.class, UNRELATED_PREFIX_MSG_KEY) + " " +
                                NbBundle.getMessage(CPRenameRefactoringPlugin.class, renameMsgKey))); //NOI18N
                    }
                }
                if(!diffs.isEmpty()) {
                    modificationResult.addDifferences(file, diffs);
                }

            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

   

}
