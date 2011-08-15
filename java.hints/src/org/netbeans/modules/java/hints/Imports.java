/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.editor.semantic.RemoveUnusedImportFix;
import org.netbeans.modules.java.hints.jackpot.spi.JavaFix;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 * Implementation of all hints for import statements
 *
 * @author phrebejk
 * @author Max Sauer
 */
public class Imports extends AbstractHint implements  PreferenceChangeListener {
  
    private static final String DEFAULT_PACKAGE = "java.lang"; // NOI18N
    private static final List<Fix> NO_FIXES = Collections.<Fix>emptyList();
    
    private static Imports delegate; // Always of kind null

    private Imports duplicate;
    private Imports defaultPackage; 
    private Imports samePackage; 
    private Imports excludedPackage;
    private Imports unused; 
    private Imports star;
        
    private String IMPORTS_ID = "Imports_"; // NOI18N
    
    private Set<Tree.Kind> treeKinds;
    
    private ImportHintKind kind;
    
    private Imports( ImportHintKind kind ) {
        super( kind.defaultOn(), true, HintSeverity.WARNING );
        this.kind = kind;        
        treeKinds = EnumSet.of(Tree.Kind.IMPORT);
    }

    public static Imports createDelegate() {
        return getDelegate();
    }
    
    public static Imports createDuplicate() {
        Imports d = getDelegate();
        d.duplicate = new Imports( ImportHintKind.DUPLICATE );
        return getDelegate();
    }
    
    public static Imports createDefaultPackage() {
        Imports d = getDelegate();        
        d.defaultPackage = new Imports( ImportHintKind.DEFAULT_PACKAGE );
        return d.defaultPackage;
    }
    
    public static Imports createExcluded() {
        Imports d = getDelegate();
        d.excludedPackage = new Imports( ImportHintKind.EXCLUDED );
        return d.excludedPackage;
    }
    
    public static Imports createSamePackage() {
        Imports d = getDelegate();
        d.samePackage = new Imports( ImportHintKind.SAME_PACKAGE );
        return  d.samePackage;
    }
    
    public static Imports createUnused() {
        Imports d = getDelegate();
        d.unused = new Imports( ImportHintKind.UNUSED );
        d.unused.getPreferences(null); // Adds listener        
        return d.unused;
    }
    
    public static Imports createStar() {
        Imports d = getDelegate();
        d.star = new Imports( ImportHintKind.STAR );
        return d.star;
    }
    
    public Set<Kind> getTreeKinds() {
        return treeKinds;        
    }

    public List<ErrorDescription> run(CompilationInfo ci, TreePath treePath) {
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();

        Tree t = treePath.getLeaf();
        if (t.getKind() != Tree.Kind.IMPORT) {
            return null;
        }

        ImportTree it = (ImportTree) t;

        if (it.isStatic() || !(it.getQualifiedIdentifier() instanceof MemberSelectTree)) {
            return null; // XXX
        }

        MemberSelectTree ms = (MemberSelectTree) it.getQualifiedIdentifier();
        Fix allFix = null;

        switch (kind) {
            case STAR:
                Imports starImport = getDelegate().star;
                if (starImport != null &&
                        starImport.isEnabled() &&
                        "*".equals(ms.getIdentifier().toString())) { // NOI18N

                    result.add(ErrorDescriptionFactory.createErrorDescription(
                            starImport.getSeverity().toEditorSeverity(),
                            starImport.getDisplayName(),
                            NO_FIXES,
                            ci.getFileObject(),
                            (int) ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), it),
                            (int) ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), it)));
                }
                break;
            case DEFAULT_PACKAGE:
                // Has to be done in order to provide 'remove all' fix
                allFix = null;
                List<ImportTree> defaultList = getAllImportsOfKind(ci, ImportHintKind.DEFAULT_PACKAGE);
                if (defaultList.size() > 1) {
                    allFix = createFix(ci, defaultList, ImportHintKind.DEFAULT_PACKAGE);
                }
                Imports defPackage = getDelegate().defaultPackage;
                if (defPackage != null && defPackage.isEnabled() && ms.getExpression().toString().equals(DEFAULT_PACKAGE)) {
                    List<Fix> fixes = new ArrayList<Fix>();
                    fixes.add(createFix(ci, it, ImportHintKind.DEFAULT_PACKAGE));
                    if (allFix != null) {
                        fixes.add(allFix);
                    }
                    result.add(ErrorDescriptionFactory.createErrorDescription(
                            defPackage.getSeverity().toEditorSeverity(),
                            defPackage.getDisplayName(),
                            fixes,
                            ci.getFileObject(),
                            (int) ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), it),
                            (int) ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), it)));
                }
                break;

            case SAME_PACKAGE:
                ExpressionTree packageName = ci.getCompilationUnit().getPackageName();
                allFix = null;
                List<ImportTree> sameList = getAllImportsOfKind(ci, ImportHintKind.SAME_PACKAGE);
                if (sameList.size() > 1) {
                    allFix = createFix(ci, sameList, ImportHintKind.SAME_PACKAGE);
                }
                Imports samePack = getDelegate().samePackage;

                if (samePack != null &&
                        samePack.isEnabled() &&
                        packageName != null &&
                        ms.getExpression().toString().equals(packageName.toString())) {
                    List<Fix> fixes = new ArrayList<Fix>();
                    fixes.add(createFix(ci, it, ImportHintKind.SAME_PACKAGE));
                    if (allFix != null) {
                        fixes.add(allFix);
                    }
                    result.add(ErrorDescriptionFactory.createErrorDescription(
                            samePack.getSeverity().toEditorSeverity(),
                            samePack.getDisplayName(),
                            fixes,
                            ci.getFileObject(),
                            (int) ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), it),
                            (int) ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), it)));
                }

                break;
            case EXCLUDED:
                Imports excludePackage = getDelegate().excludedPackage;
				String pkg = ms.getExpression().toString();
				String klass = ms.getIdentifier().toString();
				String exp = pkg + "." + (!klass.equals("*") ? klass : ""); //NOI18N
                if (excludePackage != null &&
                        excludePackage.isEnabled() &&
                        Utilities.isExcluded(exp)) {
                    result.add(ErrorDescriptionFactory.createErrorDescription(
                            excludePackage.getSeverity().toEditorSeverity(),
                            excludePackage.getDisplayName(),
                            NO_FIXES,
                            ci.getFileObject(),
                            (int) ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), it),
                            (int) ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), it)));
                }

                break;
            default:
                return null;
        }


        return result;
    }

    private List<ImportTree> getAllImportsOfKind(CompilationInfo ci, ImportHintKind kind) {
        //allow only default and samepackage
        assert (kind == ImportHintKind.DEFAULT_PACKAGE || kind == ImportHintKind.SAME_PACKAGE);

        CompilationUnitTree cut = ci.getCompilationUnit();
        List<ImportTree> result = new ArrayList<ImportTree>(3);

        List<? extends ImportTree> imports = cut.getImports();
        for (ImportTree it : imports) {
            if (it.isStatic()) {
                continue; // XXX
            }
            if (it.getQualifiedIdentifier() instanceof MemberSelectTree) {
                MemberSelectTree ms = (MemberSelectTree) it.getQualifiedIdentifier();
                if (kind == ImportHintKind.DEFAULT_PACKAGE) {
                    if (getDelegate().defaultPackage != null &&
                            getDelegate().defaultPackage.isEnabled() &&
                            ms.getExpression().toString().equals(DEFAULT_PACKAGE)) {
                        result.add(it);
                    }
                }
                if (kind == ImportHintKind.SAME_PACKAGE) {
                    ExpressionTree packageName = cut.getPackageName();
                    if (getDelegate().samePackage != null &&
                            getDelegate().samePackage.isEnabled() &&
                            packageName != null &&
                            ms.getExpression().toString().equals(packageName.toString())) {
                        result.add(it);
                    }
                }
            }
        }
        return result;
    }
    

    public void cancel() {
        
    }

    public String getId() {
        return IMPORTS_ID + kind.toString();
    }
    
    public String getDisplayName() {        
        return NbBundle.getMessage(Imports.class, "LBL_Imports_" + kind.toString()); // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(Imports.class, "DSC_Imports_" + kind.toString()); // NOI18N
    }

    @Override
    public Preferences getPreferences(String profile) {
        Preferences p = super.getPreferences(profile);
        
        if( kind == ImportHintKind.UNUSED ) {
            try {
                p.removePreferenceChangeListener(this);
            }
            catch( IllegalArgumentException e) {
                // Ignore if not set
            }
            p.addPreferenceChangeListener(this);
        }
        
        return p;
    }
    
    public void preferenceChange(PreferenceChangeEvent evt) {
        if ( kind == ImportHintKind.UNUSED ) {
            RemoveUnusedImportFix.setEnabled(isEnabled());
            RemoveUnusedImportFix.setSeverity(getDelegate().unused.getSeverity().toEditorSeverity());
        }
    }

    // Private methods ---------------------------------------------------------
    
    private static synchronized Imports getDelegate() {
        if ( delegate == null ) {
            delegate = new Imports( ImportHintKind.DELEGATE );
        }
        return delegate;
    }

    private Fix createFix( CompilationInfo ci, ImportTree tree, ImportHintKind ihk ) {
        return createFix(ci, Collections.<ImportTree>singletonList(tree), ihk);
    }
    
    private Fix createFix( CompilationInfo ci, List<ImportTree> trees, ImportHintKind ihk ) {
        
        List<TreePathHandle> paths = new ArrayList<TreePathHandle>();
        Trees t = ci.getTrees();
        CompilationUnitTree cut = ci.getCompilationUnit();
        
        for( ImportTree tree : trees ) {
            paths.add(TreePathHandle.create( t.getPath(cut, tree), ci ));
        }
        
        return JavaFix.toEditorFix(new ImportsFix(paths, 
                        ihk ));
    }
    
    private static enum ImportHintKind {

        DELEGATE,
        UNUSED,
        DUPLICATE,
        SAME_PACKAGE,
        DEFAULT_PACKAGE,
        EXCLUDED,
        STAR;

        boolean defaultOn() {

            switch (this) {
                case DELEGATE:
                case EXCLUDED:
                case SAME_PACKAGE:
                case DEFAULT_PACKAGE:
                case UNUSED:
                    return true;
                default:
                    return false;
            }
        }
    }

    private static class ImportsFix extends JavaFix {

        List<TreePathHandle> tphList;
        ImportHintKind ihk;
        
        public ImportsFix(List<TreePathHandle> tphList, ImportHintKind ihk) {
            super(tphList.get(0));
            this.tphList = tphList;
            this.ihk = ihk;
        }
        
        public String getText() {
            if ( tphList.size() == 1 ) {
                return NbBundle.getMessage(Imports.class, "LBL_Imports_Fix_One_" + ihk.toString()); // NOI18N
            }
            else {
                return NbBundle.getMessage(Imports.class, "LBL_Imports_Fix_All_" + ihk.toString()); // NOI18N
            }
        }

        @Override
        protected void performRewrite(WorkingCopy copy, TreePath tp, boolean canShowUI) {
            CompilationUnitTree cut = copy.getCompilationUnit();
            
            TreeMaker make = copy.getTreeMaker();
            
            CompilationUnitTree newCut = cut;
            for (TreePathHandle tph : tphList) {
                TreePath path = tph.resolve(copy);
                if ( path != null && path.getLeaf() instanceof ImportTree) {
                     newCut = make.removeCompUnitImport(newCut, (ImportTree)path.getLeaf());
                }
            }
            copy.rewrite(cut, newCut);
                        
        }
                
    }

    
    
}
