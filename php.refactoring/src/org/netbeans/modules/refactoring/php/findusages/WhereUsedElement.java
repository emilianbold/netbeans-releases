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
package org.netbeans.modules.refactoring.php.findusages;


import java.util.List;
import javax.swing.Icon;
import javax.swing.text.Position.Bias;

import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.napi.gsfret.source.UiUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.ClassConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.refactoring.php.ui.tree.ElementGripFactory;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * An element in the refactoring preview list which holds information about the find-usages-match
 * 
 * @author Tor Norbye
 */

public class WhereUsedElement extends SimpleRefactoringElementImplementation {
    private PositionBounds bounds;
    private String displayText;
    private FileObject parentFile;

    public WhereUsedElement(PositionBounds bounds, String displayText, FileObject parentFile, String name,
        OffsetRange range, Icon icon) {
        this.bounds = bounds;
        this.displayText = displayText;
        this.parentFile = parentFile;
        ElementGripFactory.getDefault().put(parentFile, name, range, icon);
    }

    public String getDisplayText() {
        return displayText;
    }

    public Lookup getLookup() {
        Object composite =
            ElementGripFactory.getDefault().get(parentFile, bounds.getBegin().getOffset());

        if (composite == null) {
            composite = parentFile;
        }

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
    
    public static String extractVariableName(Variable var) {
        if (var.getName() instanceof Identifier) {
            Identifier id = (Identifier) var.getName();
            StringBuilder varName = new StringBuilder();

            if (var.isDollared()) {
                varName.append("$");
            }

            varName.append(id.getName());
            return varName.toString();
        } else if (var.getName() instanceof Variable) {
            Variable name = (Variable) var.getName();
            return extractVariableName(name);
        }

        return null;
    }
    
    private static OffsetRange getRange(ASTNode node, String name) {        
        ASTNode rangeNode = node;
        while(true) {
            if (rangeNode instanceof ClassDeclaration) {
                rangeNode = ((ClassDeclaration) rangeNode).getName();
            } else if (rangeNode instanceof FunctionDeclaration) {
                rangeNode = ((FunctionDeclaration) rangeNode).getFunctionName();
            } else if (rangeNode instanceof FunctionInvocation) {
                rangeNode = ((FunctionInvocation) rangeNode).getFunctionName();
            } else if (rangeNode instanceof ClassConstantDeclaration) {
                final List<Identifier> names = ((ClassConstantDeclaration) rangeNode).getNames();
                for (Identifier id : names) {
                    if (name.equals(id.getName())) {
                        rangeNode = id;
                        break;
                    }
                }
                rangeNode = names.get(0);
            } else if (rangeNode instanceof StaticConstantAccess) {
                rangeNode = ((StaticConstantAccess) rangeNode).getConstant();
            } else if (rangeNode instanceof MethodDeclaration) {
                rangeNode = ((MethodDeclaration) rangeNode).getFunction().getFunctionName();
            } else if (rangeNode instanceof FieldAccess) {
                rangeNode = ((FieldAccess) rangeNode).getField();
            } else if (rangeNode instanceof FieldsDeclaration) {
                final List<SingleFieldDeclaration> fields = ((FieldsDeclaration) rangeNode).getFields();
                for (SingleFieldDeclaration fDeclaration : fields) {
                    if (name.equals(extractVariableName(fDeclaration.getName()))) {
                        rangeNode = fDeclaration;
                        break;
                    }
                }
                rangeNode = fields.get(0).getName();
            } else if (rangeNode instanceof ClassInstanceCreation) {
                rangeNode = ((ClassInstanceCreation) rangeNode).getClassName();
            } else if (rangeNode instanceof StaticFieldAccess) {
                rangeNode = ((StaticFieldAccess) rangeNode).getField();
            } else if (rangeNode instanceof ArrayAccess) {
                rangeNode = ((ArrayAccess) rangeNode).getName();
            } else {
                break;
            }
        }
        return new OffsetRange(rangeNode.getStartOffset(), rangeNode.getEndOffset());
    }
    
    public static WhereUsedElement create(WhereUsedSupport.ResultElement result) {
        ASTNode node = result.getASTNode();
        OffsetRange range = getRange(node, result.getName());
        assert range != null && range != OffsetRange.NONE;
        Icon icon = UiUtils.getElementIcon(result.getElementKind(), result.getModifiers());
        return create(result, range, icon);
    }
    
    public static WhereUsedElement create(WhereUsedSupport.ResultElement result, OffsetRange range, Icon icon) {
        String name = result.getName();
        FileObject fo = result.getFileObject();
        int start = range.getStart();
        int end = range.getEnd();
        
        int sta = start;
        int en = start; // ! Same line as start
        String content = null;
        
        try {
            BaseDocument bdoc = result.getDocument();
            // I should be able to just call tree.getInfo().getText() to get cached
            // copy - but since I'm playing fast and loose with compilationinfos
            // for for example find subclasses (using a singly dummy FileInfo) I need
            // to read it here instead
            content = bdoc.getText(0, bdoc.getLength());
            sta = Utilities.getRowFirstNonWhite(bdoc, start);

            if (sta == -1) {
                sta = Utilities.getRowStart(bdoc, start);
            }

            en = Utilities.getRowLastNonWhite(bdoc, start);

            if (en == -1) {
                en = Utilities.getRowEnd(bdoc, start);
            } else {
                // Last nonwhite - left side of the last char, not inclusive
                en++;
            }

            // Sometimes the node we get from the AST is for the whole block
            // (e.g. such as the whole class), not the argument node. This happens
            // for example in Find Subclasses out of the index. In this case
            if (end > en) {
                end = start + name.length();

                if (end > bdoc.getLength()) {
                    end = bdoc.getLength();
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        StringBuilder sb = new StringBuilder();
        if (end < sta) {
            // XXX Shouldn't happen, but I still have AST offset errors
            sta = end;
        }
        if (start < sta) {
            // XXX Shouldn't happen, but I still have AST offset errors
            start = sta;
        }
        if (en < end) {
            // XXX Shouldn't happen, but I still have AST offset errors
            en = end;
        }
        sb.append(RefactoringUtils.getHtml(content.subSequence(sta, start).toString()));
        sb.append("<b>"); // NOI18N
        sb.append(content.subSequence(start, end));
        sb.append("</b>"); // NOI18N
        sb.append(RefactoringUtils.getHtml(content.subSequence(end, en).toString()));

        CloneableEditorSupport ces = RefactoringUtils.findCloneableEditorSupport(result);
        PositionRef ref1 = ces.createPositionRef(start, Bias.Forward);
        PositionRef ref2 = ces.createPositionRef(end, Bias.Forward);
        PositionBounds bounds = new PositionBounds(ref1, ref2);

        return new WhereUsedElement(bounds, sb.toString().trim(), fo, name, 
                new OffsetRange(start, end), icon);
    }
}
