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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class WrongPackageSuggestion extends AbstractHint {
    
    /** Creates a new instance of WrongPackageSuggestion */
    public WrongPackageSuggestion() {
        super( true, true, AbstractHint.HintSeverity.ERROR );
    }
    
    public Set<Kind> getTreeKinds() {
        return Collections.singleton(Kind.COMPILATION_UNIT);
    }

    public List<ErrorDescription> run(CompilationInfo info, TreePath treePath) {
        Tree t = treePath.getLeaf();
        
        assert t.getKind() == Kind.COMPILATION_UNIT;
        
        CompilationUnitTree tree = (CompilationUnitTree) t;
        StringBuffer packageNameBuffer = new StringBuffer();
        boolean hasPackageClause = tree.getPackageName() != null;
        
        if (hasPackageClause) {
            new TreeScanner<Void, StringBuffer>() {
                @Override
                public Void visitIdentifier(IdentifierTree node, StringBuffer p) {
                    p.append(node.getName().toString());
                    return null;
                }
                
                @Override
                public Void visitMemberSelect(MemberSelectTree node, StringBuffer p) {
                    super.visitMemberSelect(node, p);
                    p.append('.');
                    p.append(node.getIdentifier().toString());
                    return null;
                }
                
            }.scan(tree.getPackageName(), packageNameBuffer);
        }
        
        String packageName = packageNameBuffer.toString();
        
        ClassPath cp = info.getClasspathInfo().getClassPath(PathKind.SOURCE);
        
        if (cp == null || !cp.isResourceVisible(info.getFileObject())) {
            Logger.getLogger(WrongPackageSuggestion.class.getName()).log(Level.INFO, "source cp is either null or does not contain the compiled source cp={0}", cp);
            return null;
        }
        
        String packageLocation = cp.getResourceName(info.getFileObject().getParent(), '.', false);
        
        if ((isCaseSensitive() && packageName.equals(packageLocation)) || (!isCaseSensitive() && packageName.toLowerCase().equals(packageLocation.toLowerCase()))) {
            return null;
        }
        
        long startPos;
        long endPos;
        
        if (hasPackageClause) {
            startPos = info.getTrees().getSourcePositions().getStartPosition(tree, tree.getPackageName());
            endPos   = info.getTrees().getSourcePositions().getEndPosition(tree, tree.getPackageName());
        } else {
            startPos = 0;
            endPos   = 1;
        }
        
        if (startPos == (-1) || endPos == (-1))
            return null;
        
        List<Fix> fixes = Arrays.<Fix>asList(/*new MoveToCorrectPlace(info.getFileObject(), cp, packageName), */new CorrectPackageDeclarationFix(info.getFileObject(), packageLocation));
        return Collections.singletonList(ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), "Incorrect package", fixes, info.getFileObject(), (int) startPos, (int) endPos));
    }

    public void cancel() {
        // XXX implement me
    }
    
    public String getId() {
        return WrongPackageSuggestion.class.getName();
    }

    public String getDisplayName() {
        return "Wrong Package";
    }

    public String getDescription() {
        return "Wrong Package by Jan Lahoda";
    }
    
    public Preferences getPreferences() {
        return null;
    }
    
    public JComponent getCustomizer(Preferences node) {
        return null;
    }    
    

    private static boolean isCaseSensitive () {
        return ! new File ("a").equals (new File ("A"));    //NOI18N
    }
    
    static final class MoveToCorrectPlace implements Fix {
        
        private FileObject file;
        private ClassPath  cp;
        private String packageName;
        
        public MoveToCorrectPlace(FileObject file, ClassPath  cp, String packageName) {
            this.file = file;
            this.cp   = cp;
            this.packageName = packageName;
        }
        
        public String getText() {
            return "Move class to correct folder";
        }
        
        public ChangeInfo implement() {
            try {
                String path = packageName.replace('.', '/');
                FileObject root = cp.findOwnerRoot(file);
                
                FileObject packFile = root.getFileObject(path);
                
                if (packFile != null && !packFile.isFolder()) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message("Cannot move the source, the target path already exists and is not a folder.", NotifyDescriptor.Message.ERROR_MESSAGE);
                    
                    DialogDisplayer.getDefault().notifyLater(nd);
                    
                    return null;
                }
                
                packFile = FileUtil.createFolder(root, packageName.replace('.', '/'));
                
                DataObject fileDO = DataObject.find(file);
                DataFolder folder = DataFolder.findFolder(packFile);
                
                fileDO.move(folder);
            } catch (IllegalArgumentException e) {
                Exceptions.attachLocalizedMessage(e, "Cannot move the source.");
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
                Logger.getLogger(WrongPackageSuggestion.class.getName()).log(Level.INFO, null, e);
            } catch (IOException e ) {
                Exceptions.attachLocalizedMessage(e, "Cannot move the source.");
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
                Logger.getLogger(WrongPackageSuggestion.class.getName()).log(Level.INFO, null, e);
            }
            
            return null;
        }
    }
    
    static final class CorrectPackageDeclarationFix implements Fix {
        
        private FileObject file;
        private String packageName;
        
        public CorrectPackageDeclarationFix(FileObject file, String packageName) {
            this.file = file;
            this.packageName = packageName;
        }
        
        public String getText() {
            return packageName.length() == 0 ? "Remove package declaration" : "Change package declaration to " + packageName;
        }
        
        public ChangeInfo implement() {
            JavaSource js = JavaSource.forFileObject(file);
            
            try {
                js.runModificationTask(new CancellableTask<WorkingCopy>() {
                    public void cancel() {}
                    
                    public void run(WorkingCopy copy) throws Exception {
                        copy.toPhase(Phase.PARSED);
                        
                        CompilationUnitTree cut = copy.getCompilationUnit();
                        
                        if (packageName.length() == 0) {
                            copy.rewrite(cut, copy.getTreeMaker().CompilationUnit(null, cut.getImports(), cut.getTypeDecls(), cut.getSourceFile()));
                        } else {
                            if (cut.getPackageName() == null) {
                                copy.rewrite(cut, copy.getTreeMaker().CompilationUnit(createForFQN(copy, packageName), cut.getImports(), cut.getTypeDecls(), cut.getSourceFile()));
                            } else {
                                copy.rewrite(cut.getPackageName(), createForFQN(copy, packageName));
                            }
                        }
                    }
                }).commit();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            
            return null;
        }
        
        private ExpressionTree createForFQN(WorkingCopy copy, String fqn) {
            int dot = fqn.indexOf('.');
            
            if (dot == (-1)) {
                return copy.getTreeMaker().Identifier(fqn);
            } else {
                return copy.getTreeMaker().MemberSelect(createForFQN(copy, fqn.substring(0, dot)), fqn.substring(dot + 1));
            }
        }
    }
}
