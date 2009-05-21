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
package org.netbeans.modules.refactoring.java;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.text.Position.Bias;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.refactoring.java.plugins.JavaWhereUsedQueryPlugin;
import org.netbeans.modules.refactoring.java.ui.UIUtilities;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.refactoring.java.ui.tree.ElementGripFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionRef;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

public class WhereUsedElement extends SimpleRefactoringElementImplementation {
    private PositionBounds bounds;
    private String displayText;
    private FileObject parentFile;
    public WhereUsedElement(PositionBounds bounds, String displayText, FileObject parentFile, TreePath tp, CompilationInfo info) {
        this.bounds = bounds;
        this.displayText = displayText;
        this.parentFile = parentFile;
        if (tp!=null)
            ElementGripFactory.getDefault().put(parentFile, tp, info);
    }

    public String getDisplayText() {
        return displayText;
    }

    public Lookup getLookup() {
        Object composite = ElementGripFactory.getDefault().get(parentFile, bounds.getBegin().getOffset());
        if (composite==null) 
            composite = parentFile;
        return Lookups.singleton(composite);
    }

    public PositionBounds getPosition() {
        return bounds;
    }

    public String getText() {
        return displayText;
    }

    public void performChange() {
    }

    public FileObject getParentFile() {
        return parentFile;
    }
    
    public static WhereUsedElement create(CompilationInfo compiler, TreePath tree) {
        CompilationUnitTree unit = tree.getCompilationUnit();
        CharSequence content = compiler.getSnapshot().getText();
        SourcePositions sp = compiler.getTrees().getSourcePositions();
        Tree t= tree.getLeaf();
        int start;
        int end;
        boolean anonClassNameBug128074 = false;
        TreeUtilities treeUtils = compiler.getTreeUtilities();

        if (t.getKind() == Tree.Kind.IDENTIFIER
                && "super".contentEquals(((IdentifierTree) t).getName()) // NOI18N
                && treeUtils.isSynthetic(tree)) {
            // in case of synthetic constructor call find real constructor or class declaration
            tree = getEnclosingTree(tree);
            if (treeUtils.isSynthetic(tree)) {
                tree = getEnclosingTree(tree.getParentPath());
            }
            t = tree.getLeaf();
        }

        if (t.getKind() == Tree.Kind.CLASS) {
            int[] pos = treeUtils.findNameSpan((ClassTree)t);
            if (pos == null) {
                //#121084 hotfix
                //happens for anonymous innerclasses
                anonClassNameBug128074 = true;
                start = end = (int) sp.getStartPosition(unit, t);
            } else {
                start = pos[0];
                end = pos[1];
            }
        } else if (t.getKind() == Tree.Kind.METHOD) {
            int[] pos = treeUtils.findNameSpan((MethodTree)t);
            if (pos == null) {
                //#121084 hotfix
                start = end = (int) sp.getStartPosition(unit, t);
            } else {
                start = pos[0];
                end = pos[1];
            }
        } else if (t.getKind() == Tree.Kind.NEW_CLASS) {
            ExpressionTree ident = ((NewClassTree)t).getIdentifier();
            if (ident.getKind()== Tree.Kind.MEMBER_SELECT) {
                int[] pos = treeUtils.findNameSpan((MemberSelectTree) ident);
                if (pos == null) {
                    //#121084 hotfix
                    start = end = (int) sp.getStartPosition(unit, ident);
                } else {
                    start = pos[0];
                    end = pos[1];
                }
            } else {
                TreePath varTreePath = tree.getParentPath();
                Tree varTree = varTreePath.getLeaf();
                Trees trees = compiler.getTrees();
                Element element = trees.getElement(varTreePath);
                if (varTree.getKind() == Tree.Kind.VARIABLE && element.getKind() == ElementKind.ENUM_CONSTANT) {
                    int[] pos = treeUtils.findNameSpan((VariableTree)varTree);
                    if (pos == null) {
                        //#121084 hotfix
                        start = end = (int) sp.getStartPosition(unit, varTree);
                    } else {
                        start = pos[0];
                        end = pos[1];
                    }
                } else {
                    start = (int) sp.getStartPosition(unit, ident);
                    end = (int) sp.getEndPosition(unit, ident);
                }
            }
        } else if (t.getKind() == Tree.Kind.MEMBER_SELECT) {
            int[] pos = treeUtils.findNameSpan((MemberSelectTree) t);
            if (pos == null) {
                //#121084 hotfix
                start = end = (int) sp.getStartPosition(unit, t);
            } else {
                start = pos[0];
                end = pos[1];
            }
        } else {
            start = (int) sp.getStartPosition(unit, t);
            end = (int) sp.getEndPosition(unit, t);
            if (end == -1) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new RuntimeException("Cannot get end position for " + t.getClass().getName() + " " + t + " file:" + compiler.getFileObject().getPath())); // NOI18N
                end = start;
            }
        }
                
        assert start>0:"Cannot find start position in file " + unit.getSourceFile().getName() + "\n tree=" + tree.toString();
        assert end>0:"Cannot find end position in file " + unit.getSourceFile().getName() + "\n tree=" + tree.toString();
        LineMap lm = tree.getCompilationUnit().getLineMap();
        long line = lm.getLineNumber(start);
        long endLine = lm.getLineNumber(end);
        long sta = lm.getStartPosition(line);
        int eof = content.length();
        long lastLine = lm.getLineNumber(eof);
        long en = lastLine > endLine ? lm.getStartPosition(endLine + 1) - 1 : eof;
        StringBuffer sb = new StringBuffer();
        sb.append(RetoucheUtils.getHtml(trimStart(content.subSequence((int) sta, start).toString())));
        sb.append("<b>"); //NOI18N
        sb.append(content.subSequence(start, end));
        sb.append("</b>");//NOI18N
        sb.append(RetoucheUtils.getHtml(trimEnd(content.subSequence(end, (int) en).toString())));
        
        DataObject dob = null;
        try {
            dob = DataObject.find(compiler.getFileObject());
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
        }
        CloneableEditorSupport ces = JavaWhereUsedQueryPlugin.findCloneableEditorSupport(dob);
        PositionRef ref1 = ces.createPositionRef(start, Bias.Forward);
        PositionRef ref2 = ces.createPositionRef(end, Bias.Forward);
        PositionBounds bounds = new PositionBounds(ref1, ref2);
        TreePath tr = getEnclosingTree(tree);
        return new WhereUsedElement(
                bounds,
                start==end && anonClassNameBug128074 ? NbBundle.getMessage(UIUtilities.class, "LBL_AnonymousClass"):sb.toString().trim(),
                compiler.getFileObject(),
                tr,
                compiler);
    }
    
    private static String trimStart(String s) {
        for (int x = 0; x < s.length(); x++) {
            if (Character.isWhitespace(s.charAt(x))) {
                continue;
            } else {
                return s.substring(x, s.length());
            }
        }
        return "";
    }
    
    private static String trimEnd(String s) {
        for (int x = s.length()-1; x >=0; x--) {
            if (Character.isWhitespace(s.charAt(x))) {
                continue;
            } else {
                return s.substring(0, x + 1);
            }
        }
        return "";
    }
    
    public static WhereUsedElement create(int start, int end, CompilationInfo compiler) {
        CharSequence content = compiler.getSnapshot().getText();
        LineMap lm = compiler.getCompilationUnit().getLineMap();
        long line = lm.getLineNumber(start);
        long endLine = lm.getLineNumber(end);
        long sta = lm.getStartPosition(line);
        int eof = content.length();
        long lastLine = lm.getLineNumber(eof);
        long en = lastLine > endLine ? lm.getStartPosition(endLine + 1) - 1 : eof;
        StringBuffer sb = new StringBuffer();
        sb.append(RetoucheUtils.getHtml(trimStart(content.subSequence((int) sta, start).toString())));
        sb.append("<b>"); //NOI18N
        sb.append(content.subSequence(start, end));
        sb.append("</b>");//NOI18N
        sb.append(RetoucheUtils.getHtml(trimEnd(content.subSequence(end, (int) en).toString())));
        
        DataObject dob = null;
        try {
            dob = DataObject.find(compiler.getFileObject());
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
        }
        CloneableEditorSupport ces = JavaWhereUsedQueryPlugin.findCloneableEditorSupport(dob);
        PositionRef ref1 = ces.createPositionRef(start, Bias.Forward);
        PositionRef ref2 = ces.createPositionRef(end, Bias.Forward);
        PositionBounds bounds = new PositionBounds(ref1, ref2);
        return new WhereUsedElement(bounds, sb.toString().trim(), compiler.getFileObject(), null, compiler);
    }
    
    
    private static TreePath getEnclosingTree(TreePath tp) {
        while(tp != null) {
            Tree tree = tp.getLeaf();
            if (tree.getKind() == Tree.Kind.CLASS || tree.getKind() == Tree.Kind.METHOD || tree.getKind() == Tree.Kind.IMPORT) {
                return tp;
            } 
            tp = tp.getParentPath();
        }
        return null;
    }

}
