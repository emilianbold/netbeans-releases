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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Position.Bias;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.csl.spi.support.ModificationResult.Difference;
import org.netbeans.modules.css.editor.CssProjectSupport;
import org.netbeans.modules.css.indexing.CssFileModel;
import org.netbeans.modules.css.refactoring.api.Entry;
import org.netbeans.modules.css.indexing.CssIndex;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.netbeans.modules.web.common.api.DependenciesGraph.Node;
import org.netbeans.modules.css.parser.CssParserTreeConstants;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.css.refactoring.api.RefactoringElementType;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.web.common.api.FileReference;
import org.netbeans.modules.web.common.api.FileReferenceModification;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public class CssRenameRefactoringPlugin implements RefactoringPlugin {

    private static final String SELECTOR_RENAME_MSG_KEY = "MSG_Rename_Selector"; //NOI18N
    private static final String COLOR_RENAME_MSG_KEY = "MSG_Rename_Color"; //NOI18N
    private static final String UNRELATED_PREFIX_MSG_KEY = "MSG_Unrelated_Prefix"; //NOI18N

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
        CssProjectSupport sup = CssProjectSupport.findFor(context.getFileObject());
        if (sup == null) {
            return null;
        }
        CssIndex index = sup.getIndex();
        ModificationResult modificationResult = new ModificationResult();

        if (context instanceof CssElementContext.Editor) {
            //editor elements refactoring
            CssElementContext.Editor econtext = (CssElementContext.Editor) context;
            //get selected element in the editor
            int kind = econtext.getElement().kind();
            String elementImage = econtext.getElementName();

            if (kind == CssParserTreeConstants.JJT_CLASS) {
                int elementPrefixLength = 1;
                elementImage = elementImage.substring(elementPrefixLength); //cut off the dot
                Collection<FileObject> files = index.findClasses(elementImage);
                refactor(lookup, modificationResult, RefactoringElementType.CLASS, files, elementPrefixLength, econtext, index, SELECTOR_RENAME_MSG_KEY);
            } else if (kind == CssParserTreeConstants.JJTHASH) {
                int elementPrefixLength = 1;
                elementImage = elementImage.substring(elementPrefixLength); //cut off the dot
                Collection<FileObject> files = index.findIds(elementImage);
                refactor(lookup, modificationResult, RefactoringElementType.ID, files, elementPrefixLength, econtext, index, SELECTOR_RENAME_MSG_KEY);
            } else if (kind == CssParserTreeConstants.JJTHEXCOLOR) {
                Collection<FileObject> files = index.findColor(elementImage);
                refactor(lookup, modificationResult, RefactoringElementType.COLOR, files, 0, econtext, index, COLOR_RENAME_MSG_KEY);
            } else if (kind == CssParserTreeConstants.JJTELEMENTNAME) {
                refactorElement(modificationResult, econtext, index);
            } else {
                //other nodes which may appear under the simple selector node
                //we do not refactor them
            }

        } else if (context instanceof CssElementContext.File) {
            //refactor a file in explorer
            CssElementContext.File fileContext = (CssElementContext.File) context;
            refactorFile(modificationResult, fileContext, index);

        } else if (context instanceof CssElementContext.Folder) {
            //refactor a folder in explorer
            CssElementContext.Folder folderContext = (CssElementContext.Folder) context;
            refactorFolder(modificationResult, folderContext, index);
            //add folder rename element implementation, there doesn't seem to a default one
            //like for file rename
            refactoringElements.add(refactoring, new RenameFolder(folderContext.getFileObject()));
        }

        //commit the transaction and add the differences to the result
        refactoringElements.registerTransaction(new RetoucheCommit(Collections.singletonList(modificationResult)));
        for (FileObject fo : modificationResult.getModifiedFileObjects()) {
            for (Difference diff : modificationResult.getDifferences(fo)) {
                refactoringElements.add(refactoring, DiffElement.create(diff, fo, modificationResult));

            }
        }

        return null;
    }

    private void refactorFile(ModificationResult modificationResult, CssElementContext.File context, CssIndex index) {
        LOGGER.fine("refactor file " + context.getFileObject().getPath()); //NOI18N
        String newName = refactoring.getNewName();

        //a. get all importing files
        //b. rename the references
        //c. rename the file itself - done via default rename plugin
        DependenciesGraph deps = index.getDependencies(context.getFileObject());
        Collection<Node> refering = deps.getSourceNode().getReferingNodes();
        for (Node ref : refering) {
            FileObject file = ref.getFile();
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

                CssFileModel model = new CssFileModel(source);

                List<Difference> diffs = new ArrayList<Difference>();
                for (Entry entry : model.getImports()) {
                    String imp = entry.getName(); //unquoted
                    FileObject resolvedFileObject = WebUtils.resolve(file, imp);
                    if (resolvedFileObject != null && resolvedFileObject.equals(context.getFileObject())) {
                        //the import refers to me - lets refactor it
                        if (entry.isValidInSourceDocument()) {
                            //new relative path creation
                            String newImport;
                            String extension = context.getFileObject().getExt(); //use the same extension as source file (may not be .css)
                            int slashIndex = imp.lastIndexOf('/'); //NOI18N
                            if (slashIndex != -1) {
                                newImport = imp.substring(0, slashIndex) + "/" + newName + "." + extension; //NOI18N
                            } else {
                                newImport = newName + "." + extension; //NOI18N
                            }

                            diffs.add(new Difference(Difference.Kind.CHANGE,
                                    editor.createPositionRef(entry.getDocumentRange().getStart(), Bias.Forward),
                                    editor.createPositionRef(entry.getDocumentRange().getEnd(), Bias.Backward),
                                    entry.getName(),
                                    newImport,
                                    NbBundle.getMessage(CssRenameRefactoringPlugin.class, "MSG_Modify_Css_File_Import"))); //NOI18N
                        }
                    }
                }

                modificationResult.addDifferences(file, diffs);

            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }

    private void refactorFolder(ModificationResult modificationResult, CssElementContext.Folder context, CssIndex index) {
        LOGGER.fine("refactor folder " + context.getFileObject().getPath()); //NOI18N
        String newName = refactoring.getNewName();
        try {
            CssIndex.AllDependenciesMaps alldeps = index.getAllDependencies();
            Map<FileObject, Collection<FileReference>> source2dest = alldeps.getSource2dest();
            FileObject renamedFolder = context.getFileObject();

            Map<FileObject, CssFileModel> modelsCache = new WeakHashMap<FileObject, CssFileModel>();
            //now I need to find out what links go through the given folder
            for (FileObject source : source2dest.keySet()) {
                List<Difference> diffs = new ArrayList<Difference>();
                Collection<FileReference> destinations = source2dest.get(source);
                for (FileReference dest : destinations) {
                    FileReferenceModification modification = dest.createModification();
                    if (modification.rename(renamedFolder, newName)) {
                        //the link is affected, we need to update it
                        //find the css model and the link position in the file
                        CssFileModel model = modelsCache.get(source);
                        if (model == null) {
                            try {
                                model = new CssFileModel(Source.create(source)); //use file to parse
                                modelsCache.put(source, model);
                            } catch (ParseException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }

                        if (model != null) {
                            //we have the model for source file
                            Collection<Entry> imports = model.getImports();
                            //XXX the model should contain string representation 2 entry map
                            //linear search :-(
                            for (Entry entry : imports) {
                                if (entry.isValidInSourceDocument() && entry.getName().equals(dest.linkPath())) {
                                    //a matching entry found, add the rename refactoring
                                    CloneableEditorSupport editor = GsfUtilities.findCloneableEditorSupport(source);

                                    diffs.add(new Difference(Difference.Kind.CHANGE,
                                            editor.createPositionRef(entry.getDocumentRange().getStart(),
                                            Bias.Forward),
                                            editor.createPositionRef(entry.getDocumentRange().getEnd(),
                                            Bias.Backward),
                                            entry.getName(),
                                            modification.getModifiedReferencePath(),
                                            NbBundle.getMessage(CssRenameRefactoringPlugin.class, "MSG_Modify_Css_File_Import"))); //NOI18N
                                }
                            }
                        }
                    }
                }
                modificationResult.addDifferences(source, diffs);
            }

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void refactorElement(ModificationResult modificationResult, CssElementContext.Editor context, CssIndex index) {
        //type selector: div
        //we do refactor only elements in the current css file, and even this is questionable if makes much sense
        SimpleNode element = context.getElement();
        String elementImage = element.image();

        CssFileModel model = new CssFileModel(context.getParserResult());
        List<Difference> diffs = new ArrayList<Difference>();
        CloneableEditorSupport editor = GsfUtilities.findCloneableEditorSupport(context.getFileObject());
        for (Entry entry : model.getHtmlElements()) {
            if (entry.isValidInSourceDocument() && elementImage.equals(entry.getName())) {
                diffs.add(new Difference(Difference.Kind.CHANGE,
                        editor.createPositionRef(entry.getDocumentRange().getStart(), Bias.Forward),
                        editor.createPositionRef(entry.getDocumentRange().getEnd(), Bias.Backward),
                        entry.getName(),
                        refactoring.getNewName(),
                        NbBundle.getMessage(CssRenameRefactoringPlugin.class, "MSG_Rename_Selector"))); //NOI18N
            }
        }
        modificationResult.addDifferences(context.getFileObject(), diffs);

    }

    private void refactor(Lookup lookup, ModificationResult modificationResult, RefactoringElementType type, Collection<FileObject> files, int elementPrefixLenght, CssElementContext.Editor context, CssIndex index, String renameMsgKey) {
        String elementImage = context.getElementName().substring(elementPrefixLenght);
        List<FileObject> involvedFiles = new LinkedList<FileObject>(files);
        DependenciesGraph deps = index.getDependencies(context.getFileObject());
        Collection<FileObject> relatedFiles = deps.getAllRelatedFiles();

         //refactor all occurances support
        CssRefactoringExtraInfo extraInfo =
                lookup.lookup(CssRefactoringExtraInfo.class);

        //if the "refactor all occurances" checkbox hasn't been
        //selected the occurances must be searched only in the related files
        if (extraInfo == null || !extraInfo.isRefactorAll()) {
            //filter out those files which have no relation with the current file.
            //note: the list of involved files also contains the currently edited file.
            involvedFiles.retainAll(relatedFiles);
            //now we have a list of files which contain the given class or id and are
            //related to the base file
        }

        if (LOG) {
            LOGGER.fine("Refactoring element " + elementImage + " in file " + context.getFileObject().getPath()); //NOI18N
            LOGGER.fine("Involved files declaring the element " + elementImage + ":"); //NOI18N
            for (FileObject fo : involvedFiles) {
                LOGGER.fine(fo.getPath() + "\n"); //NOI18N
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

                CssFileModel model = new CssFileModel(source);
                Collection<Entry> entries = model.get(type);

                boolean related = relatedFiles.contains(file);

                List<Difference> diffs = new ArrayList<Difference>();
                for (Entry entry : entries) {
                    if (entry.isValidInSourceDocument() && elementImage.equals(entry.getName())) {
                        diffs.add(new Difference(Difference.Kind.CHANGE,
                                editor.createPositionRef(entry.getDocumentRange().getStart(), Bias.Forward),
                                editor.createPositionRef(entry.getDocumentRange().getEnd(), Bias.Backward),
                                entry.getName(),
                                newName,
                                related
                                ? NbBundle.getMessage(CssRenameRefactoringPlugin.class, renameMsgKey)
                                : NbBundle.getMessage(CssRenameRefactoringPlugin.class, UNRELATED_PREFIX_MSG_KEY) + " " +
                                NbBundle.getMessage(CssRenameRefactoringPlugin.class, renameMsgKey))); //NOI18N
                    }
                }
                modificationResult.addDifferences(file, diffs);

            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

     private class RenameFolder extends SimpleRefactoringElementImplementation {

        private FileObject fo;
        private String oldName;

        public RenameFolder(FileObject fo) {
            this.fo = fo;
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(CssRenameRefactoringPlugin.class, "TXT_RenameFolder", fo.getNameExt());
        }

        @Override
        public String getDisplayText() {
            return getText();
        }

        @Override
        public void performChange() {
            try {
                oldName = fo.getName();
                DataObject.find(fo).rename(refactoring.getNewName());
            } catch (DataObjectNotFoundException ex) {
                throw new IllegalStateException(ex);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public void undoChange(){
            try {
                DataObject.find(fo).rename(oldName);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public FileObject getParentFile() {
            return fo;
        }

        @Override
        public PositionBounds getPosition() {
            return null;
        }
    }


}
