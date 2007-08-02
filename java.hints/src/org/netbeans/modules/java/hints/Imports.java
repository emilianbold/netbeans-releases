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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javax.swing.JComponent;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.semantic.RemoveUnusedImportFix;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author phrebejk
 */
public class Imports extends AbstractHint implements  PreferenceChangeListener {
  
    private static final String DEFAULT_PACKAGE = "java.lang"; // NOI18N
    private static final List<Fix> NO_FIXES = Collections.<Fix>emptyList();
    
    private static Imports delegate; // Always of kind null

    private Imports duplicate;
    private Imports defaultPackage; 
    private Imports samePackage; 
    private Imports forbiddenPackage; 
    private Imports unused; 
    private Imports star;
        
    private String IMPORTS_ID = "Imports_"; // NOI18N
    
    private Set<Tree.Kind> treeKinds;
    
    private ImportHintKind kind;
    
    private Imports( ImportHintKind kind ) {
        super( kind.defaultOn(), true, HintSeverity.WARNING );
        this.kind = kind;        
        treeKinds = kind == ImportHintKind.DELEGATE ? EnumSet.of(Tree.Kind.COMPILATION_UNIT) :
                                              EnumSet.noneOf(Tree.Kind.class);
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
    
    public static Imports createForbidden() {
        Imports d = getDelegate();
        d.forbiddenPackage = new Imports( ImportHintKind.FORBIDDEN );
        return d.forbiddenPackage;
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

    public List<ErrorDescription> run(CompilationInfo compilationInfo, TreePath treePath) {
        
        // XXX
        //if ( shouldRun() ) 
        
        
        return analyseImports( compilationInfo );        
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
    
    @Override
    public JComponent getCustomizer(Preferences node) {
        if ( kind == ImportHintKind.FORBIDDEN ) {
            return new ForbiddenImportsCustomizer( node );
        }
        else {
            return super.getCustomizer(node);
        }
    }

    public void preferenceChange(PreferenceChangeEvent evt) {
        if ( kind == ImportHintKind.UNUSED ) {
            RemoveUnusedImportFix.setEnabled(isEnabled());
        }
    }

    // Private methods ---------------------------------------------------------
    
    private static synchronized Imports getDelegate() {
        if ( delegate == null ) {
            delegate = new Imports( ImportHintKind.DELEGATE );
        }
        return delegate;
    }
    
    private List<ErrorDescription> analyseImports( CompilationInfo ci ) {
        
        CompilationUnitTree cut = ci.getCompilationUnit();
        ExpressionTree packageName = cut.getPackageName();
        
        List<? extends ImportTree> imports = cut.getImports();
        
        List<ImportTree> defaultList = new ArrayList<ImportTree>(3);
        List<ImportTree> sameList = new ArrayList<ImportTree>(3);
        List<ImportTree> forbiddenList = new ArrayList<ImportTree>(3);
        List<ImportTree> starList = new ArrayList<ImportTree>(3);
        
        String[] regexps = getRegexps(forbiddenPackage.getPreferences(null));
        
        
        // Check imports
        for (ImportTree it : imports) {
            if ( it.isStatic() ) {
                continue; // XXX
            }
            
            
            if ( it.getQualifiedIdentifier() instanceof MemberSelectTree ) {
                MemberSelectTree ms = (MemberSelectTree)it.getQualifiedIdentifier();
                
                if ( defaultPackage != null &&
                     defaultPackage.isEnabled() && 
                     ms.getExpression().toString().equals(DEFAULT_PACKAGE) ) {
                    defaultList.add(it);
                }
                
                else if ( samePackage != null &&
                          samePackage.isEnabled() && 
                          packageName != null &&   
                          ms.getExpression().toString().equals(packageName.toString())) {
                    sameList.add(it); 
                }
                else if ( forbiddenPackage != null &&
                          forbiddenPackage.isEnabled() &&
                          isForbidden( ms.getExpression().toString(), regexps) ) {
                    
                    forbiddenList.add(it);
                }
                else if ( star != null &&
                          star.isEnabled() &&
                          "*".equals(ms.getIdentifier().toString()) ) { // NOI18N
                    starList.add(it);
                }
            }
        }
        
        // Create the descriptions
        
        
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        
        
        Fix allFix = null;
        
        // Default package                
        if ( defaultList.size() > 1 ) {
            allFix  = createFix(ci, defaultList, ImportHintKind.DEFAULT_PACKAGE);
        }
        for (ImportTree importTree : defaultList) {
            List<Fix> fixes = new ArrayList<Fix>();
            fixes.add( createFix(ci, importTree, ImportHintKind.DEFAULT_PACKAGE) );
            if ( allFix != null ) {
                fixes.add( allFix );
            }            
            result.add(ErrorDescriptionFactory.createErrorDescription(
                defaultPackage.getSeverity().toEditorSeverity(), 
                defaultPackage.getDisplayName(), 
                fixes,
                ci.getFileObject(),
                (int)ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), importTree ),
                (int)ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), importTree ) ) ); 
        }

        // Same package
        allFix = null;
        if ( sameList.size() > 1 ) {
            allFix  = createFix(ci, sameList, ImportHintKind.SAME_PACKAGE);
        }
        for (ImportTree importTree : sameList) {
            List<Fix> fixes = new ArrayList<Fix>();
            fixes.add( createFix(ci, importTree, ImportHintKind.SAME_PACKAGE) );
            if ( allFix != null ) {
                fixes.add( allFix );
            }            
            result.add(ErrorDescriptionFactory.createErrorDescription(
                samePackage.getSeverity().toEditorSeverity(), 
                samePackage.getDisplayName(), 
                fixes, 
                ci.getFileObject(),
                (int)ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), importTree ),
                (int)ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), importTree ) ) ); 
        }
        
        // Forbidden package
        for (ImportTree importTree : forbiddenList) {
            result.add(ErrorDescriptionFactory.createErrorDescription(
                forbiddenPackage.getSeverity().toEditorSeverity(), 
                forbiddenPackage.getDisplayName(), 
                NO_FIXES, 
                ci.getFileObject(),
                (int)ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), importTree ),
                (int)ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), importTree ) ) ); 
        }
        
        // Star import
        for (ImportTree importTree : starList) {
            result.add(ErrorDescriptionFactory.createErrorDescription(
                star.getSeverity().toEditorSeverity(), 
                star.getDisplayName(), 
                NO_FIXES, 
                ci.getFileObject(),
                (int)ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), importTree ),
                (int)ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), importTree ) ) ); 
        }
        
        return result;
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
        
        return new ImportsFix( ci.getFileObject(),
                        paths, 
                        ihk );
    }
    
    private String[] getRegexps( Preferences node ) {
        
        String[] texts = ForbiddenImportsCustomizer.getForbiddenImports(node);
        String[] result = new String[texts.length]; 
        
        for (int i = 0; i < texts.length; i++) {
            String t = texts[i];
            t = t.replace(".", "\\.");
            t = t.replace("**", ";;" );
            t = t.replace("*", "[^\\.]*");
            t = t.replace(";;", ".*");
            result[i] = t;
        }

        return result;
    }
    
    private static boolean isForbidden( String pkg, String regexps[] ) {
        
        Pattern p;
               
        for( String rg : regexps ) {
            
            if ( Pattern.matches(rg, pkg)) {
                return true;
            }
            
        }
                
        return false;
    }
    
    private static enum ImportHintKind {
        DELEGATE,
        UNUSED,
        DUPLICATE,
        SAME_PACKAGE,
        DEFAULT_PACKAGE,
        FORBIDDEN,
        STAR;
        
        boolean defaultOn() {
        
            switch( this ) {
                case DELEGATE:
                case FORBIDDEN:
                case SAME_PACKAGE:
                case DEFAULT_PACKAGE:
                case UNUSED:
                    return true;
                default:
                    return false;
            }
        }
        
    }

    private static class ImportsFix implements Fix, Task<WorkingCopy> {

        FileObject file;
        List<TreePathHandle> tphList;
        ImportHintKind ihk;
        
        public ImportsFix(FileObject file, List<TreePathHandle> tphList, ImportHintKind ihk) {
            this.file = file;
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

        public ChangeInfo implement() {
            JavaSource js = JavaSource.forFileObject(file);
            try {
                js.runModificationTask(this).commit();
            }
            catch( IOException e ) {
                Exceptions.printStackTrace(e);
            }
            return null;
        }


        public void run(WorkingCopy copy) throws Exception {
            copy.toPhase(JavaSource.Phase.PARSED);            
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
