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
package org.netbeans.modules.cnd.refactoring.plugins;


import org.netbeans.modules.refactoring.api.Problem;
import java.text.MessageFormat;
import java.util.*;




import javax.swing.text.Position.Bias;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult.Difference;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;

import org.openide.filesystems.FileUtil;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionRef;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * The actual Renaming refactoring work for C/C++. The skeleton (name checks etc.) based
 * on the Java refactoring module by Jan Becicka, Martin Matula, Pavel Flaska and Daniel Prusa.
 * 
 * @author Jan Becicka
 * @author Martin Matula
 * @author Pavel Flaska
 * @author Daniel Prusa
 * @author Vladimir Voskresensky
 *
 * @todo Complete this. Most of the prechecks are not implemented - and the refactorings themselves need a lot of work.
 */
public class CsmRenameRefactoringPlugin extends CsmRefactoringPlugin {
    
    //private RubyElementCtx treePathHandle = null;
    private final CsmObject startReferenceObject;
    private Collection overriddenByMethods = null; // methods that override the method to be renamed
    private Collection overridesMethods = null; // methods that are overridden by the method to be renamed
    private boolean doCheckName = true;
    private List<CsmObject> referencedObjects;
    private final RenameRefactoring refactoring;
    
    /** Creates a new instance of RenameRefactoring */
    public CsmRenameRefactoringPlugin(RenameRefactoring rename) {
        this.refactoring = rename;
        startReferenceObject = refactoring.getRefactoringSource().lookup(CsmObject.class);     
        assert startReferenceObject != null : "no start reference";
    }
    
    private static final String getCannotRename(FileObject r) {
        return new MessageFormat(NbBundle.getMessage(CsmRenameRefactoringPlugin.class, "ERR_CannotRenameFile")).format(new Object[] {r.getNameExt()});
    }
    
    @Override
    public Problem fastCheckParameters() {
        Problem fastCheckProblem = null;
        String newName = refactoring.getNewName();
        String oldName = CsmRefactoringUtils.getSimpleText(startReferenceObject);
        
        if (oldName.equals(newName)) {
            fastCheckProblem = createProblem(fastCheckProblem, true, getString("ERR_NameNotChanged")); // NOI18N
            return fastCheckProblem;
        }
        
        if (!Utilities.isJavaIdentifier(newName)) {
            String s = getString("ERR_InvalidIdentifier"); //NOI18N
            String msg = new MessageFormat(s).format(
                    new Object[] {newName}
            );
            fastCheckProblem = createProblem(fastCheckProblem, true, msg);
            return fastCheckProblem;
        }        
        return fastCheckProblem;
    }
    
    @Override
    public Problem checkParameters() {
        return fastCheckParameters();
    }
//    protected Problem checkParameters(CompilationController info) throws IOException {
//        
//        Problem checkProblem = null;
//        int steps = 0;
//        if (overriddenByMethods != null) {
//            steps += overriddenByMethods.size();
//        }
//        if (overridesMethods != null) {
//            steps += overridesMethods.size();
//        }
//        
//        fireProgressListenerStart(refactoring.PARAMETERS_CHECK, 8 + 3*steps);
//        
//        info.toPhase(org.netbeans.api.retouche.source.Phase.RESOLVED);
////        Element element = treePathHandle.resolveElement(info);
//        
//        fireProgressListenerStep();
//        fireProgressListenerStep();
//        String msg;
//        
//
//        fireProgressListenerStop();
//        return checkProblem;
//    }
    
    @Override
    public Problem preCheck() {
        Problem preCheckProblem = null;
        fireProgressListenerStart(RenameRefactoring.PRE_CHECK, 4);
        if (this.referencedObjects == null) {
            initReferencedObjects(startReferenceObject);
            fireProgressListenerStep();
        }    
        preCheckProblem = isResovledElement(startReferenceObject);
        if (preCheckProblem != null) {
            return preCheckProblem;
        }
        CsmObject directReferencedObject = CsmRefactoringUtils.getReferencedElement(startReferenceObject);
        // check read-only elements
        FileObject fo = null;
        if (CsmKindUtilities.isOffsetable(directReferencedObject)) {
            fo = CsmUtilities.getFileObject(((CsmOffsetable)directReferencedObject).getContainingFile());
            fireProgressListenerStep();
        }
        if (fo != null && (FileUtil.getArchiveFile(fo)!= null || !fo.canWrite())) {
            preCheckProblem = createProblem(preCheckProblem, true, getCannotRename(fo));
            return preCheckProblem;            
        }
        fireProgressListenerStop();
        return preCheckProblem;
//        info.toPhase(JavaSource.Phase.RESOLVED);
//        Element el = treePathHandle.resolveElement(info);
//        preCheckProblem = isElementAvail(treePathHandle, info);
//        if (preCheckProblem != null) {
//            return preCheckProblem;
//        }
//        FileObject file = SourceUtils.getFile(el, info.getClasspathInfo());
//        if (file!=null && FileUtil.getArchiveFile(file)!= null) { //NOI18N
//            preCheckProblem = createProblem(preCheckProblem, true, getCannotRename(file));
//            return preCheckProblem;
//        }
//        
//        if (file==null || !RetoucheUtils.isElementInOpenProject(file)) {
//            preCheckProblem = new Problem(true, NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_ProjectNotOpened"));
//            return preCheckProblem;
//        }
//        
//        switch(el.getKind()) {
//        case METHOD:
//            fireProgressListenerStep();
//            fireProgressListenerStep();
//            overriddenByMethods = RetoucheUtils.getOverridingMethods((ExecutableElement)el, info);
//            fireProgressListenerStep();
//            if (el.getModifiers().contains(Modifier.NATIVE)) {
//                preCheckProblem = createProblem(preCheckProblem, false, NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_RenameNative", el));
//            }
//            if (!overriddenByMethods.isEmpty()) {
//                String msg = new MessageFormat(getString("ERR_IsOverridden")).format(
//                        new Object[] {SourceUtils.getEnclosingTypeElement(el).getSimpleName().toString()});
//                preCheckProblem = createProblem(preCheckProblem, false, msg);
//            }
//            for (ExecutableElement e : overriddenByMethods) {
//                if (e.getModifiers().contains(Modifier.NATIVE)) {
//                    preCheckProblem = createProblem(preCheckProblem, false, NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_RenameNative", e));
//                }
//            }
//            overridesMethods = RetoucheUtils.getOverridenMethods((ExecutableElement)el, info);
//            fireProgressListenerStep();
//            if (!overridesMethods.isEmpty()) {
//                boolean fatal = false;
//                for (Iterator iter = overridesMethods.iterator();iter.hasNext();) {
//                    ExecutableElement method = (ExecutableElement) iter.next();
//                    if (method.getModifiers().contains(Modifier.NATIVE)) {
//                        preCheckProblem = createProblem(preCheckProblem, false, NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_RenameNative", method));
//                    }
//                    if (RetoucheUtils.isFromLibrary(method, info.getClasspathInfo())) {
//                        fatal = true;
//                        break;
//                    }
//                }
//                String msg = fatal?getString("ERR_Overrides_Fatal"):getString("ERR_Overrides");
//                preCheckProblem = createProblem(preCheckProblem, fatal, msg);
//            }
//            break;
//        case FIELD:
//        case ENUM_CONSTANT:
//            fireProgressListenerStep();
//            fireProgressListenerStep();
//            Element hiddenField = hides(el, el.getSimpleName().toString(), info);
//            fireProgressListenerStep();
//            fireProgressListenerStep();
//            if (hiddenField != null) {
//                String msg = new MessageFormat(getString("ERR_Hides")).format(
//                        new Object[] {SourceUtils.getEnclosingTypeElement(hiddenField)}
//                );
//                preCheckProblem = createProblem(preCheckProblem, false, msg);
//            }
//            break;
//        case PACKAGE:
//            //TODO: any prechecks?
//            break;
//        case LOCAL_VARIABLE:
//            //TODO: any prechecks for formal parametr or local variable?
//            break;
//        case CLASS:
//        case INTERFACE:
//        case ANNOTATION_TYPE:
//        case ENUM:
//            //TODO: any prechecks for JavaClass?
//            break;
//        default:
//            //                if (!((jmiObject instanceof Resource) && ((Resource)jmiObject).getClassifiers().isEmpty()))
//            //                    result = createProblem(result, true, NbBundle.getMessage(RenameRefactoring.class, "ERR_RenameWrongType"));
//        }
    }
    
//    private Set<RubyElementCtx> allMethods;
    
    public Problem prepare(RefactoringElementsBag elements) {
//        if (treePathHandle == null) {
//            return null;
//        }
//        Set<FileObject> a = getRelevantFiles();
//        fireProgressListenerStart(ProgressEvent.START, a.size());
//        if (!a.isEmpty()) {
//            TransformTask transform = new TransformTask(new RenameTransformer(refactoring.getNewName(), allMethods), treePathHandle);
//            final Collection<ModificationResult> results = processFiles(a, transform);
//            elements.registerTransaction(new RetoucheCommit(results));
//            for (ModificationResult result:results) {
//                for (FileObject jfo : result.getModifiedFileObjects()) {
//                    for (Difference diff: result.getDifferences(jfo)) {
//                        String old = diff.getOldText();
//                        if (old!=null) {
//                            //TODO: workaround
//                            //generator issue?
//                            elements.add(refactoring,DiffElement.create(diff, jfo, result));
//                        }
//                    }
//                }
//            }
//        }
//        fireProgressListenerStop();
        if (this.referencedObjects == null || this.referencedObjects.size() == 0) {
            return null;
        }
        Collection<CsmFile> files = new HashSet<CsmFile>();
        CsmFile startFile = getCsmFile(startReferenceObject);
        for (CsmObject obj : referencedObjects) {
            Collection<CsmProject> prjs = CsmRefactoringUtils.getRelatedCsmProjects(obj, true);
            CsmProject[] ar = prjs.toArray(new CsmProject[prjs.size()]);
            refactoring.getContext().add(ar);
            files.addAll(getRelevantFiles(startFile, obj, refactoring));
        }
        fireProgressListenerStart(ProgressEvent.START, files.size());
        createAndAddElements(files, elements, refactoring);
        fireProgressListenerStop();        
        return null;
    }
    
    @Override
    protected ModificationResult processFiles(Collection<CsmFile> files) {
        ModificationResult out = null;
        for (CsmFile csmFile : files) {
            if (cancelRequest) {
                // may be return what we already have?
                return null;
            }
            if (out == null) {
                out = new ModificationResult(csmFile.getProject());
            }
            processFile(csmFile, out);            
            fireProgressListenerStep();
        }
        return out;
    }
    
    private static final String getString(String key) {
        return NbBundle.getMessage(CsmRenameRefactoringPlugin.class, key);
    }

    private void initReferencedObjects(CsmObject startReferenceObject) {
        CsmObject referencedObject = CsmRefactoringUtils.getReferencedElement(startReferenceObject);
        if (referencedObject != null) {
            this.referencedObjects = new ArrayList<CsmObject>();
            if (CsmKindUtilities.isClass(referencedObject)) {
                // for class we need to add all needed elements
                this.referencedObjects.addAll(getRenamingClassObjects((CsmClass)referencedObject));
            } else if (CsmKindUtilities.isConstructor(referencedObject) || CsmKindUtilities.isDestructor(referencedObject)) {
                // for constructor/destructor we need to add all needed elements
                CsmFunction fun = (CsmFunction)referencedObject;
                if (CsmKindUtilities.isFunctionDefinition(fun)) {
                    fun = ((CsmFunctionDefinition)fun).getDeclaration();
                }
                if (fun != null && CsmKindUtilities.isMethod(fun)) {
                    CsmClass cls = ((CsmMethod)fun).getContainingClass();
                    if (cls != null) {
                        this.referencedObjects.addAll(getRenamingClassObjects(cls));
                    }
                }
            } else {
                this.referencedObjects.add(referencedObject);
            }
        }
    }

    private Collection<CsmObject> getRenamingClassObjects(CsmClass clazz) {
        Collection<CsmObject> out = new ArrayList<CsmObject>(5);
        if (clazz != null) {
            out.add(clazz);
            for (CsmMember member : clazz.getMembers()) {
                if (CsmKindUtilities.isConstructor(member)) {
                    out.add(member);
                } else if (CsmKindUtilities.isDestructor(member)) {
                    out.add(member);
                }
            }
        }
        return out;

    }
    
    private void processFile(CsmFile csmFile, ModificationResult mr) {
        assert this.referencedObjects != null && this.referencedObjects.size() > 0: "method must be called for resolved element";
        FileObject fo = CsmUtilities.getFileObject(csmFile);
        Collection<CsmReference> refs = new LinkedHashSet<CsmReference>();
        for (CsmObject obj : referencedObjects) {
            Collection<CsmReference> curRefs = CsmReferenceRepository.getDefault().getReferences(obj, csmFile, true);
            refs.addAll(curRefs);
        }
        if (refs.size() > 0) {
            List<CsmReference> sortedRefs = new ArrayList<CsmReference>(refs);
            Collections.sort(sortedRefs, new Comparator<CsmReference>() {
                public int compare(CsmReference o1, CsmReference o2) {
                    return o1.getStartOffset() - o2.getStartOffset();
                }
            });
            CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(csmFile);
            String newName = refactoring.getNewName();
            for (CsmReference ref : sortedRefs) {
                String oldName = ref.getText().toString();
                String descr = getDescription(ref, oldName);
                Difference diff = rename(ref, ces, oldName, newName, descr);
                assert diff != null;
                mr.addDifference(fo, diff);
            }
        }
    }
    
    private String getDescription(CsmReference ref, String targetName) {
        String out = NbBundle.getMessage(CsmRenameRefactoringPlugin.class, "UpdateRef", targetName);
        return out;
    }
    
    private Difference rename(CsmReference ref, CloneableEditorSupport ces,
            String oldName, String newName, String descr) {
        if (oldName == null) {
            oldName = ref.getText().toString();
        }
        if (newName == null) {
            newName = refactoring.getNewName();
        }
        assert oldName != null;
        assert newName != null;
        PositionRef startPos = ces.createPositionRef(ref.getStartOffset(), Bias.Forward);
        PositionRef endPos = ces.createPositionRef(ref.getEndOffset(), Bias.Backward);
        Difference diff = new Difference(Difference.Kind.CHANGE, ref, startPos, endPos, oldName, newName, descr);
        return diff;
    }
    /**
     *
     * @author Jan Becicka
     */
//    public class RenameTransformer extends SearchVisitor {
//
//        private Set<RubyElementCtx> allMethods;
//        private String newName;
//        private String oldName;
//        private CloneableEditorSupport ces;
//        private List<Difference> diffs;
//
//        @Override
//        public void setWorkingCopy(WorkingCopy workingCopy) {
//            // Cached per working copy
//            this.ces = null;
//            assert diffs == null; // Should have been committed already
//            super.setWorkingCopy(workingCopy);
//        }
//        
//        public RenameTransformer(String newName, Set<RubyElementCtx> am) {
//            this.newName = newName;
//            this.oldName = treePathHandle.getSimpleName();
//            this.allMethods = am;
//        }
//        
//        @Override
//        public void scan() {
//            // TODO - do I need to force state to resolved?
//            //compiler.toPhase(org.netbeans.api.retouche.source.Phase.RESOLVED);
//
//            diffs = new ArrayList<Difference>();
//            RubyElementCtx searchCtx = treePathHandle;
//            Error error = null;
//            Node root = AstUtilities.getRoot(workingCopy);
//            if (root != null) {
//                
//                Element element = AstElement.create(root);
//                Node node = searchCtx.getNode();
//                RubyElementCtx fileCtx = new RubyElementCtx(root, node, element, workingCopy.getFileObject(), workingCopy);
//                Node method = null;
//                if (node instanceof ArgumentNode) {
//                    AstPath path = searchCtx.getPath();
//                    assert path.leaf() == node;
//                    Node parent = path.leafParent();
//
//                    if (!(parent instanceof MethodDefNode)) {
//                        method = AstUtilities.findLocalScope(node, path);
//                    }
//                } else if (node instanceof LocalVarNode || node instanceof LocalAsgnNode || node instanceof DAsgnNode || 
//                        node instanceof DVarNode) {
//                    // A local variable read or a parameter read, or an assignment to one of these
//                    AstPath path = searchCtx.getPath();
//                    method = AstUtilities.findLocalScope(node, path);
//                }
//
//                if (method != null) {
//                    findLocal(searchCtx, fileCtx, method, oldName);
//                } else {
//                    // Full AST search
//                    AstPath path = new AstPath();
//                    path.descend(root);
//                    find(path, searchCtx, fileCtx, root, oldName, Character.isUpperCase(oldName.charAt(0)));
//                    path.ascend();
//                }
//            } else {
//                //System.out.println("Skipping file " + workingCopy.getFileObject());
//                // See if the document contains references to this symbol and if so, put a warning in
//                if (workingCopy.getText().indexOf(oldName) != -1) {
//                    // TODO - icon??
//                    if (ces == null) {
//                        ces = RetoucheUtils.findCloneableEditorSupport(workingCopy);
//                    }
//                    int start = 0;
//                    int end = 0;
//                    String desc = NbBundle.getMessage(CsmRenameRefactoringPlugin.class, "ParseErrorFile", oldName);
//                    List<Error> errors = workingCopy.getDiagnostics();
//                    if (errors.size() > 0) {
//                        for (Error e : errors) {
//                            if (e.getSeverity() == Severity.ERROR) {
//                                error = e;
//                                break;
//                            }
//                        }
//                        if (error == null) {
//                            error = errors.get(0);
//                        }
//                        
//                        String errorMsg = error.getDisplayName();
//                        
//                        if (errorMsg.length() > 80) {
//                            errorMsg = errorMsg.substring(0, 77) + "..."; // NOI18N
//                        }
//
//                        desc = desc + "; " + errorMsg;
//                        start = error.getStartPosition().getOffset();
//                        start = LexUtilities.getLexerOffset(workingCopy, start);
//                        if (start == -1) {
//                            start = 0;
//                        }
//                        end = start;
//                    }
//                    PositionRef startPos = ces.createPositionRef(start, Bias.Forward);
//                    PositionRef endPos = ces.createPositionRef(end, Bias.Forward);
//                    Difference diff = new Difference(Difference.Kind.CHANGE, startPos, endPos, "", "", desc); // NOI18N
//                    diffs.add(diff);
//                }
//            }
//
//            if (error == null && refactoring.isSearchInComments()) {
//                Document doc = RetoucheUtils.getDocument(workingCopy, workingCopy.getFileObject());
//                if (doc != null) {
//                    //force open
//                    TokenHierarchy<Document> th = TokenHierarchy.get(doc);
//                    TokenSequence<?extends TokenId> ts = th.tokenSequence();
//
//                    ts.move(0);
//
//                    searchTokenSequence(ts);
//                }
//            }
//
//            // Sort the diffs, if applicable
//            if (diffs.size() > 0) {
//                Collections.sort(diffs, new Comparator<Difference>() {
//                    public int compare(Difference d1, Difference d2) {
//                        return d1.getStartPosition().getOffset() - d2.getStartPosition().getOffset();
//                    }
//                });
//                for (Difference diff : diffs) {
//                    workingCopy.addDiff(diff);
//                }
//            }
//            diffs = null;
//            ces = null;
//            
//        }
//        
//        private void searchTokenSequence(TokenSequence<? extends TokenId> ts) {
//            if (ts.moveNext()) {
//                do {
//                    Token<?extends TokenId> token = ts.token();
//                    TokenId id = token.id();
//
//                    String primaryCategory = id.primaryCategory();
//                    if ("comment".equals(primaryCategory) || "block-comment".equals(primaryCategory)) { // NOI18N
//                        // search this comment
//                        String text = token.text().toString();
//                        int index = text.indexOf(oldName);
//                        if (index != -1) {
//                            // TODO make sure it's its own word. Technically I could
//                            // look at identifier chars like "_" here but since they are
//                            // used for other purposes in comments, consider letters
//                            // and numbers as enough
//                            if ((index == 0 || !Character.isLetterOrDigit(text.charAt(index-1))) &&
//                                    (index+oldName.length() >= text.length() || 
//                                    !Character.isLetterOrDigit(text.charAt(index+oldName.length())))) {
//                                int start = ts.offset() + index;
//                                int end = start + oldName.length();
//                                if (ces == null) {
//                                    ces = RetoucheUtils.findCloneableEditorSupport(workingCopy);
//                                }
//                                PositionRef startPos = ces.createPositionRef(start, Bias.Forward);
//                                PositionRef endPos = ces.createPositionRef(end, Bias.Forward);
//                                String desc = getString("ChangeComment");
//                                Difference diff = new Difference(Difference.Kind.CHANGE, startPos, endPos, oldName, newName, desc);
//                                diffs.add(diff);
//                            }
//                        }
//                    } else {
//                        TokenSequence<? extends TokenId> embedded = ts.embedded();
//                        if (embedded != null) {
//                            searchTokenSequence(embedded);
//                        }                                    
//                    }
//                } while (ts.moveNext());
//            }
//        }
//
//        private void rename(Node node, String oldCode, String newCode, String desc) {
//            OffsetRange range = AstUtilities.getNameRange(node);
//            assert range != OffsetRange.NONE;
//            int pos = range.getStart();
//
//            if (desc == null) {
//                // TODO - insert "method call", "method definition", "class definition", "symbol", "attribute" etc. and from and too?
//                if (node instanceof MethodDefNode) {
//                    desc = getString("UpdateMethodDef");
//                } else if (AstUtilities.isCall(node)) {
//                    desc = getString("UpdateCall");
//                } else if (node instanceof SymbolNode) {
//                    desc = getString("UpdateSymbol");
//                } else if (node instanceof ClassNode || node instanceof SClassNode) {
//                    desc = getString("UpdateClassDef");
//                } else if (node instanceof ModuleNode) {
//                    desc = getString("UpdateModule");
//                } else if (node instanceof LocalVarNode || node instanceof LocalAsgnNode || node instanceof DVarNode || node instanceof DAsgnNode) {
//                    desc = getString("UpdateLocalvar");
//                } else if (node instanceof GlobalVarNode || node instanceof GlobalAsgnNode) {
//                    desc = getString("UpdateGlobal");
//                } else if (node instanceof InstVarNode || node instanceof InstAsgnNode) {
//                    desc = getString("UpdateInstance");
//                } else if (node instanceof ClassVarNode || node instanceof ClassVarDeclNode || node instanceof ClassVarAsgnNode) {
//                    desc = getString("UpdateClassvar");
//                } else {
//                    desc = NbBundle.getMessage(CsmRenameRefactoringPlugin.class, "UpdateRef", oldCode);
//                }
//            }
//
//            if (ces == null) {
//                ces = RetoucheUtils.findCloneableEditorSupport(workingCopy);
//            }
//            
//            // Convert from AST to lexer offsets if necessary
//            pos = LexUtilities.getLexerOffset(workingCopy, pos);
//            if (pos == -1) {
//                // Translation failed
//                return;
//            }
//            
//            int start = pos;
//            int end = pos+oldCode.length();
//            // TODO if a SymbolNode, +=1 since the symbolnode includes the ":"
//            try {
//                BaseDocument doc = (BaseDocument)ces.openDocument();
//
//                if (start > doc.getLength()) {
//                    start = end = doc.getLength();
//                }
//
//                if (end > doc.getLength()) {
//                    end = doc.getLength();
//                }
//
//                // Look in the document and search around a bit to detect the exact method reference
//                // (and adjust position accordingly). Thus, if I have off by one errors in the AST (which
//                // occasionally happens) the user's source won't get munged
//                if (!oldCode.equals(doc.getText(start, end-start))) {
//                    // Look back and forwards by 1 at first
//                    int lineStart = Utilities.getRowFirstNonWhite(doc, start);
//                    int lineEnd = Utilities.getRowLastNonWhite(doc, start)+1; // +1: after last char
//                    if (lineStart == -1 || lineEnd == -1) { // We're really on the wrong line!
//                        System.out.println("Empty line entry in " + FileUtil.getFileDisplayName(workingCopy.getFileObject()) +
//                                "; no match for " + oldCode + " in line " + start + " referenced by node " + 
//                                node + " of type " + node.getClass().getName());
//                        return;
//                    }
//
//                    if (lineStart < 0 || lineEnd-lineStart < 0) {
//                        return; // Can't process this one
//                    }
//
//                    String line = doc.getText(lineStart, lineEnd-lineStart);
//                    if (line.indexOf(oldCode) == -1) {
//                        System.out.println("Skipping entry in " + FileUtil.getFileDisplayName(workingCopy.getFileObject()) +
//                                "; no match for " + oldCode + " in line " + line + " referenced by node " + 
//                                node + " of type " + node.getClass().getName());
//                    } else {
//                        int lineOffset = start-lineStart;
//                        int newOffset = -1;
//                        // Search up and down by one
//                        for (int distance = 1; distance < line.length(); distance++) {
//                            // Ahead first
//                            if (lineOffset+distance+oldCode.length() <= line.length() &&
//                                    oldCode.equals(line.substring(lineOffset+distance, lineOffset+distance+oldCode.length()))) {
//                                newOffset = lineOffset+distance;
//                                break;
//                            }
//                            if (lineOffset-distance >= 0 && lineOffset-distance+oldCode.length() <= line.length() &&
//                                    oldCode.equals(line.substring(lineOffset-distance, lineOffset-distance+oldCode.length()))) {
//                                newOffset = lineOffset-distance;
//                                break;
//                            }
//                        }
//
//                        if (newOffset != -1) {
//                            start = newOffset+lineStart;
//                            end = start+oldCode.length();
//                        }
//                    }
//                }
//            } catch (IOException ie) {
//                Exceptions.printStackTrace(ie);
//            } catch (BadLocationException ble) {
//                Exceptions.printStackTrace(ble);
//            }
//            
//            if (newCode == null) {
//                // Usually it's the new name so allow client code to refer to it as just null
//                newCode = refactoring.getNewName(); // XXX isn't this == our field "newName"?
//            }
//
//            PositionRef startPos = ces.createPositionRef(start, Bias.Forward);
//            PositionRef endPos = ces.createPositionRef(end, Bias.Forward);
//            Difference diff = new Difference(Difference.Kind.CHANGE, startPos, endPos, oldCode, newCode, desc);
//            diffs.add(diff);
//        }
//    
//        /** Search for local variables in local scope */
//        private void findLocal(RubyElementCtx searchCtx, RubyElementCtx fileCtx, Node node, String name) {
//            if (node instanceof ArgumentNode) {
//                // TODO - check parent and make sure it's not a method of the same name?
//                // e.g. if I have "def foo(foo)" and I'm searching for "foo" (the parameter),
//                // I don't want to pick up the ArgumentNode under def foo that corresponds to the
//                // "foo" method name!
//                if (((ArgumentNode)node).getName().equals(name)) {
//                    RubyElementCtx matchCtx = new RubyElementCtx(fileCtx, node);
//                    rename(node, name, null, getString("RenameParam"));
//                }
//// I don't have alias nodes within a method, do I?                
////            } else if (node instanceof AliasNode) { 
////                AliasNode an = (AliasNode)node;
////                if (an.getNewName().equals(name) || an.getOldName().equals(name)) {
////                    RubyElementCtx matchCtx = new RubyElementCtx(fileCtx, node);
////                    elements.add(refactoring, WhereUsedElement.create(matchCtx));
////                }
//            } else if (node instanceof LocalVarNode || node instanceof LocalAsgnNode) {
//                if (((INameNode)node).getName().equals(name)) {
//                    RubyElementCtx matchCtx = new RubyElementCtx(fileCtx, node);
//                    rename(node, name, null, getString("UpdateLocalvar"));
//                }
//            } else if (node instanceof DVarNode || node instanceof DAsgnNode) {
//                 if (((INameNode)node).getName().equals(name)) {
//                    // Found a method call match
//                    // TODO - make a node on the same line
//                    // TODO - check arity - see OccurrencesFinder
//                    RubyElementCtx matchCtx = new RubyElementCtx(fileCtx, node);
//                    rename(node, name, null, getString("UpdateDynvar"));
//                 }                 
//            } else if (node instanceof SymbolNode) {
//                // XXX Can I have symbols to local variables? Try it!!!
//                if (((SymbolNode)node).getName().equals(name)) {
//                    RubyElementCtx matchCtx = new RubyElementCtx(fileCtx, node);
//                    rename(node, name, null, getString("UpdateSymbol"));
//                }
//            }
//
//            @SuppressWarnings("unchecked")
//            List<Node> list = node.childNodes();
//
//            for (Node child : list) {
//                findLocal(searchCtx, fileCtx, child, name);
//            }
//        }
//        
//        /**
//         * @todo P1: This is matching method names on classes that have nothing to do with the class we're searching for
//         *   - I've gotta filter fields, methods etc. that are not in the current class
//         *  (but I also have to search for methods that are OVERRIDING the class... so I've gotta work a little harder!)
//         * @todo Arity matching on the methods to preclude methods that aren't overriding or aliasing!
//         */
//        private void find(AstPath path, RubyElementCtx searchCtx, RubyElementCtx fileCtx, Node node, String name, boolean upperCase) {
//            /* TODO look for both old and new and attempt to fix
//             if (node instanceof AliasNode) {
//                AliasNode an = (AliasNode)node;
//                if (an.getNewName().equals(name) || an.getOldName().equals(name)) {
//                    RubyElementCtx matchCtx = new RubyElementCtx(fileCtx, node);
//                    elements.add(refactoring, WhereUsedElement.create(matchCtx));
//                }
//            } else*/ if (!upperCase) {
//                // Local variables - I can be smarter about context searches here!
//                
//                // Methods, attributes, etc.
//                // TODO - be more discriminating on the filetype
//                if (node instanceof MethodDefNode) {
//                    if (((MethodDefNode)node).getName().equals(name)) {
//                                                
//                        boolean skip = false;
//
//                        // Check that we're in a class or module we're interested in
//                        String fqn = AstUtilities.getFqnName(path);
//                        if (fqn == null || fqn.length() == 0) {
//                            fqn = RubyIndex.OBJECT;
//                        }
//                        
//                        if (!fqn.equals(searchCtx.getDefClass())) {
//                            // XXX THE ABOVE IS NOT RIGHT - I shouldn't
//                            // use equals on the class names, I should use the
//                            // index and see if one derives fromor includes the other
//                            skip = true;
//                        }
//
//                        // Check arity
//                        if (!skip && AstUtilities.isCall(searchCtx.getNode())) {
//                            // The reference is a call and this is a definition; see if
//                            // this looks like a match
//                            // TODO - enforce that this method is also in the desired
//                            // target class!!!
//                            if (!AstUtilities.isCallFor(searchCtx.getNode(), searchCtx.getArity(), node)) {
//                                skip = true;
//                            }
//                        } else {
//                            // The search handle is a method def, as is this, with the same name.
//                            // Now I need to go and see if this is an override (e.g. compatible
//                            // arglist...)
//                            // XXX TODO
//                        }
//                        
//                        if (!skip) {
//                            // Found a method match
//                            // TODO - check arity - see OccurrencesFinder
//                            node = AstUtilities.getDefNameNode((MethodDefNode)node);
//                            rename(node, name, null, getString("UpdateMethodDef"));
//                        }
//                    }
//                } else if (AstUtilities.isCall(node)) {
//                     if (((INameNode)node).getName().equals(name)) {
//                         // TODO - if it's a call without a lhs (e.g. Call.LOCAL),
//                         // make sure that we're referring to the same method call
//                        // Found a method call match
//                        // TODO - make a node on the same line
//                        // TODO - check arity - see OccurrencesFinder
//                        rename(node, name, null, null);
//                     }
//                } else if (AstUtilities.isAttr(node)) {
//                    SymbolNode[] symbols = AstUtilities.getAttrSymbols(node);
//                    for (SymbolNode symbol : symbols) {
//                        if (symbol.getName().equals(name)) {
//                            // TODO - can't replace the whole node here - I need to replace only the text!
//                            rename(node, name, null, null);
//                        }
//                    }
//                } else if (node instanceof SymbolNode) {
//                    if (((SymbolNode)node).getName().equals(name)) {
//                        // TODO do something about the colon?
//                        rename(node, name, null, null);
//                    }
//                } else if (node instanceof GlobalVarNode || node instanceof GlobalAsgnNode ||
//                        node instanceof InstVarNode || node instanceof InstAsgnNode ||
//                        node instanceof ClassVarAsgnNode || node instanceof ClassVarDeclNode || node instanceof ClassVarNode) {
//                    if (((INameNode)node).getName().equals(name)) {
//                        rename(node, name, null, null);
//                    }
//                }
//            } else {
//                // Classes, modules, constants, etc.
//                if (node instanceof Colon2Node) {
//                    Colon2Node c2n = (Colon2Node)node;
//                    if (c2n.getName().equals(name)) {
//                        rename(node, name, null, null);
//                    }
//                    
//                } else if (node instanceof ConstNode || node instanceof ConstDeclNode) {
//                    if (((INameNode)node).getName().equals(name)) {
//                        rename(node, name, null, null);
//                    }
//                }
//            }
//            
//            @SuppressWarnings("unchecked")
//            List<Node> list = node.childNodes();
//
//            for (Node child : list) {
//                path.descend(child);
//                find(path, searchCtx, fileCtx, child, name, upperCase);
//                path.ascend();
//            }
//        }
//    
//    }
    
}
