/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.hints.errors;

import org.netbeans.modules.java.hints.infrastructure.Pair;
import org.netbeans.modules.java.hints.*;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.java.editor.imports.ComputeImports;
import org.netbeans.modules.java.hints.errors.ImportClass.ImportCandidatesHolder;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jan Lahoda
 */
public final class ImportClass implements ErrorRule<ImportCandidatesHolder> {
    
    static RequestProcessor WORKER = new RequestProcessor("ImportClassEnabler", 1);
    
    public ImportClass() {
    }
    
    public Set<String> getCodes() {
        return new HashSet<String>(Arrays.asList(
                "compiler.err.cant.resolve",
                "compiler.err.cant.resolve.location",
                "compiler.err.doesnt.exist",
                "compiler.err.not.stmt"));
    }
    
    public List<Fix> run(final CompilationInfo info, String diagnosticKey, final int offset, TreePath treePath, Data<ImportCandidatesHolder> data) {
        resume();
        
        int errorPosition = offset + 1; //TODO: +1 required to work OK, rethink
        
        if (errorPosition == (-1)) {
            ErrorHintsProvider.LOG.log(Level.FINE, "ImportClassEnabler.create errorPosition=-1"); //NOI18N
            
            return Collections.<Fix>emptyList();
        }

        TreePath path = info.getTreeUtilities().pathFor(errorPosition);
        
        if (path.getParentPath() != null && path.getParentPath().getLeaf().getKind() == Kind.METHOD_INVOCATION) {
            //#86313:
            //if the error is in the type parameter, import should be proposed:
            MethodInvocationTree mit = (MethodInvocationTree) path.getParentPath().getLeaf();
            
            if (!mit.getTypeArguments().contains(path.getLeaf())) {
                return Collections.<Fix>emptyList();
            }
        }
        
        Token ident = ErrorHintsProvider.findUnresolvedElementToken(info, offset);
        
        ErrorHintsProvider.LOG.log(Level.FINE, "ImportClassEnabler.create ident={0}", ident); //NOI18N
        
        if (ident == null) {
            return Collections.<Fix>emptyList();
        }
        
        FileObject file = info.getFileObject();
        String simpleName = ident.text().toString();
        Pair<List<String>, List<String>> candidates = getCandidateFQNs(info, file, simpleName, data);
        
        if (isCancelled()) {
            ErrorHintsProvider.LOG.log(Level.FINE, "ImportClassEnabler.cancelled."); //NOI18N
            
            return Collections.<Fix>emptyList();
        }
        
        List<String> filtered = candidates.getA();
        List<String> unfiltered = candidates.getB();
        List<Fix> fixes = new ArrayList<Fix>();
        
        if (unfiltered != null && filtered != null) {
            //TODO: simplify when editor hints allow ordering:
            for (String fqn : filtered) {
                fixes.add(new FixImport(file, fqn, true));
            }
            
            for (String fqn : unfiltered) {
                if (!filtered.contains(fqn))
                    fixes.add(new FixImport(file, fqn, false));
            }
        }
        
        ErrorHintsProvider.LOG.log(Level.FINE, "ImportClassEnabler.create finished."); //NOI18N

        return fixes;
    }
    
    public synchronized void cancel() {
        ErrorHintsProvider.LOG.log(Level.FINE, "ImportClassEnabler.cancel called."); //NOI18N
        
        cancelled = true;
        
        if (compImports != null) {
            compImports.cancel();
        }
    }
    
    public String getId() {
        return ImportClass.class.getName();
    }
    
    public String getDisplayName() {
        return "Add Import Fix";
    }
    
    public String getDescription() {
        return "Add Import Fix";
    }
    
    private synchronized void resume() {
        ErrorHintsProvider.LOG.log(Level.FINE, "ImportClassEnabler.resume called."); //NOI18N
        
        cancelled = false;
    }
    
    private synchronized boolean isCancelled() {
        return cancelled;
    }
    
    private boolean cancelled;
    private ComputeImports compImports;
    
    private synchronized void setComputeImports(ComputeImports compImports) {
        this.compImports = compImports;
    }
    
    public Pair<List<String>, List<String>> getCandidateFQNs(CompilationInfo info, FileObject file, String simpleName, Data<ImportCandidatesHolder> data) {
        ImportCandidatesHolder holder = data.getData();
        
        if (holder == null) {
            data.setData(holder = new ImportCandidatesHolder());
        }
        
        Pair<Map<String, List<String>>, Map<String, List<String>>> result = holder.getCandidates();
        
        if (result == null || result.getA() == null || result.getB() == null) {
            //compute imports:
            Map<String, List<String>> candidates = new HashMap<String, List<String>>();
            ComputeImports imp = new ComputeImports();
            
            setComputeImports(imp);
            
            ComputeImports.Pair<Map<String, List<TypeElement>>, Map<String, List<TypeElement>>> rawCandidates = imp.computeCandidates(info);
            
            setComputeImports(null);
            
            if (isCancelled()) {
                ErrorHintsProvider.LOG.log(Level.FINE, "ImportClassEnabler.getCandidateFQNs cancelled, returning."); //NOI18N
                
                return null;
            }
            
            for (String sn : rawCandidates.a.keySet()) {
                List<String> c = new ArrayList<String>();
                
                for (TypeElement te : rawCandidates.a.get(sn)) {
                    c.add(te.getQualifiedName().toString());
                }
                
                candidates.put(sn, c);
            }
            
            Map<String, List<String>> notFilteredCandidates = new HashMap<String, List<String>>();
            
            for (String sn : rawCandidates.b.keySet()) {
                List<String> c = new ArrayList<String>();
                
                for (TypeElement te : rawCandidates.b.get(sn)) {
                    c.add(te.getQualifiedName().toString());
                }
                
                notFilteredCandidates.put(sn, c);
            }
            
            result = new Pair(candidates, notFilteredCandidates);
            
            holder.setCandidates(result);
        }
        
        List<String> candList = result.getA().get(simpleName);
        List<String> notFilteredCandList = result.getB().get(simpleName);
        
        return new Pair(candList, notFilteredCandList);
    }
    
    public static class ImportCandidatesHolder {
        private Pair<Map<String, List<String>>, Map<String, List<String>>> candidates;
        
        public Pair<Map<String, List<String>>, Map<String, List<String>>> getCandidates() {
            return candidates;
        }
        
        public void setCandidates(Pair<Map<String, List<String>>, Map<String, List<String>>> candidates) {
            this.candidates = candidates;
        }
    }
    
    static final class FixImport implements Fix {
        
        private FileObject file;
        private String fqn;
        private boolean isValid;
        
        public FixImport(FileObject file, String fqn, boolean isValid) {
            this.file = file;
            this.fqn = fqn;
            this.isValid = isValid;
        }
        
        public String getText() {
            if (isValid)
                return NbBundle.getMessage(ImportClass.class, "Add_import_for_X", new Object[] {fqn});
            else
                return "<html><font color='#808080'><s>" + NbBundle.getMessage(ImportClass.class, "Add_import_for_X", new Object[] {fqn});
        }

        public ChangeInfo implement() {
            JavaSource js = JavaSource.forFileObject(file);            
            
            CancellableTask task = new CancellableTask<WorkingCopy>() {
                
                    public void run(WorkingCopy copy) throws Exception {
                        if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0)
                           return;
                  
                        TypeElement te = copy.getElements().getTypeElement(fqn);
                        
                        if (te == null) {
                            Logger.getAnonymousLogger().warning(String.format("Attempt to fix import for FQN: %s, which does not have a TypeElement in currect context", fqn));
                            return ;
                        }
                        
                        CompilationUnitTree cut = SourceUtils.addImports(
                            copy.getCompilationUnit(),
                            Collections.singletonList(te.getQualifiedName().toString()),
                            copy.getTreeMaker()
                        );
                        copy.rewrite(copy.getCompilationUnit(), cut);
                    }
                    
                    public void cancel() {
                    }
            };
            try {
                js.runModificationTask(task).commit();
                return null;
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            return null;
        }
        
        @Override
        public int hashCode() {
            return fqn.hashCode();
        }
        
        @Override
        public boolean equals(Object o) {
            if (o instanceof FixImport) {
                return fqn.equals(((FixImport) o).fqn);
            }
            
            return false;
        }
    }
}
