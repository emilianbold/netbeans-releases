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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.StructureItem;
import org.netbeans.modules.gsf.api.StructureScanner;
import org.netbeans.modules.php.editor.parser.GSFPHPElementHandle.ClassDeclarationHandle;
import org.netbeans.modules.php.editor.parser.GSFPHPElementHandle.FieldsFromTagProperty;
import org.netbeans.modules.php.editor.parser.GSFPHPElementHandle.FunctionDeclarationHandle;
import org.netbeans.modules.php.editor.parser.GSFPHPElementHandle.InterfaceDeclarationHandle;
import org.netbeans.modules.php.editor.parser.GSFPHPElementHandle.MethodDeclarationHandle;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.*;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 *
 * @author Petr Pisl
 */
public class PhpStructureScanner implements StructureScanner {

    private CompilationInfo info;

    private static ImageIcon INTERFACE_ICON = null;

    private static final String FOLD_CODE_BLOCKS = "codeblocks"; //NOI18N

    private static final String FOLD_CLASS = "codeblocks"; //NOI18N

    private static final String FOLD_PHPDOC = "comments"; //NOI18N

    private static final String FOLD_COMMENT = "initial-comment"; //NOI18N

    private static final String FONT_GRAY_COLOR = "<font color=\"#999999\">"; //NOI18N

    private static final String CLOSE_FONT = "</font>";                   //NOI18N

    private static final String LAST_CORRECT_FOLDING_PROPERTY = "LAST_CORRECT_FOLDING_PROPERY";

    public List<? extends StructureItem> scan(final CompilationInfo info) {
        this.info = info;
        Program program = Utils.getRoot(info);
        final List<StructureItem> items = new ArrayList<StructureItem>();
        if (program != null) {
            program.accept(new StructureVisitor(items, program));
            return items;
        }
        return Collections.emptyList();
    }

    public Map<String, List<OffsetRange>> folds(CompilationInfo info) {
        Program program = Utils.getRoot(info);
        final Map<String, List<OffsetRange>> folds = new HashMap<String, List<OffsetRange>>();
        if (program != null) {
            if (program.getStatements().size() == 1) {
                // check whether the ast is broken.
                if (program.getStatements().get(0) instanceof ASTError) {
                    Object lastCorrect = info.getDocument().getProperty(LAST_CORRECT_FOLDING_PROPERTY);
                    if (lastCorrect != null){
                        return ( Map<String, List<OffsetRange>>)lastCorrect;
                    }
                    else {
                        return Collections.emptyMap();
                    }
                }
            }
            (new FoldVisitor(folds)).scan(program);
            List<Comment> comments = program.getComments();
            if (comments != null) {
                for (Comment comment : comments) {
                    if (comment.getCommentType() == Comment.Type.TYPE_PHPDOC) {
                        getRanges(folds, FOLD_PHPDOC).add(createOffsetRange(comment));
                    } else {
                        if (comment.getCommentType() == Comment.Type.TYPE_MULTILINE) {
                            getRanges(folds, FOLD_COMMENT).add(createOffsetRange(comment));
                        }
                    }
                }
            }
            info.getDocument().putProperty(LAST_CORRECT_FOLDING_PROPERTY, folds);
            return folds;
        }
        return Collections.emptyMap();
    }

    private OffsetRange createOffsetRange(ASTNode node) {
        return new OffsetRange(node.getStartOffset(), node.getEndOffset());
    }

    private List<OffsetRange> getRanges(Map<String, List<OffsetRange>> folds, String kind) {
        List<OffsetRange> ranges = folds.get(kind);
        if (ranges == null) {
            ranges = new ArrayList<OffsetRange>();
            folds.put(kind, ranges);
        }
        return ranges;
    }

    public Configuration getConfiguration() {
        return null;
    }

    private class StructureVisitor extends DefaultVisitor {

        final List<StructureItem> items;
        private List<StructureItem> children = null;
        private String className;
        private final Program program;

        public StructureVisitor(List<StructureItem> items, Program program) {
            this.items = items;
            this.program = program;
        }

        @Override
        public void visit(FunctionDeclaration function) {
            if (children == null && function.getFunctionName() != null) {
                PHPStructureItem item = new PHPFunctionStructureItem(new GSFPHPElementHandle.FunctionDeclarationHandle(info, function));
                items.add(item);
            }
        }

        @Override
        public void visit(ClassDeclaration cldec) {
            if (cldec.getName() != null) {
                children = new ArrayList<StructureItem>();
                className = cldec.getName().getName();
                super.visit(cldec);
                for (PHPDocPropertyTag tag : Utils.getPropertyTags(program, cldec)) {
                    children.add(new PHPFieldFromPropertyTagItem(new FieldsFromTagProperty(info, tag), "0"));
                }
                PHPStructureItem item = new PHPClassStructureItem(new GSFPHPElementHandle.ClassDeclarationHandle(info, cldec), children); //NOI18N
                items.add(item);
                children = null;
            }
        }

        @Override
        public void visit(InterfaceDeclaration indec) {
            if (indec.getName() != null) {
                children = new ArrayList<StructureItem>();
                super.visit(indec);
                PHPStructureItem item = new PHPInterfaceStructureItem(new GSFPHPElementHandle.InterfaceDeclarationHandle(info, indec), children);
                items.add(item);
            }
        }

        @Override
        public void visit(MethodDeclaration method) {
            FunctionDeclaration function = method.getFunction();
            if (function != null && function.getFunctionName() != null) {
                String functionName = function.getFunctionName().getName();
                PHPStructureItem item;
                // className doesn't have to be defined if it's interace
                if (className!= null && (className.equals(functionName) || "__construct".equals(functionName))) { //NOI18N
                    item = new PHPConstructorStructureItem(new GSFPHPElementHandle.MethodDeclarationHandle(info, method));
                }
                else {
                    item = new PHPMethodStructureItem(new GSFPHPElementHandle.MethodDeclarationHandle(info, method));
                }
                children.add(item);
            }

        }

        @Override
        public void visit(FieldsDeclaration fields) {
            Variable[] variables = fields.getVariableNames();
            if (variables != null) {
                for (Variable variable : variables) {
                    String name = Utils.resolveVariableName(variable);
                    if (name != null) {
                        String text = name;
                        if (variable.isDollared()) {
                            text = "$"+name; //NOI18N
                        }
                        PHPStructureItem item = new PHPSimpleStructureItem(new GSFPHPElementHandle.FieldsDeclarationHandle(info, fields), text, "0"); //NOI18N
                        children.add(item);
                    }
                }
            }
        }

        @Override
        public void visit(ClassConstantDeclaration constants) {
            List<Identifier> names = constants.getNames();

            for (Identifier identifier : names) {
                String name = identifier.getName();
                if (name != null) {
                    PHPStructureItem item = new PHPSimpleStructureItem(new GSFPHPElementHandle.ClassConstantDeclarationHandle(info, constants), name, "con"); //NOI18N
                    children.add(item);
                }
            }
        }

        @Override
        public void visit(FunctionInvocation function) {
            if (function.getFunctionName().getName() instanceof Identifier){
                String name = ((Identifier)function.getFunctionName().getName()).getName();
                if ("define".equals(name)) {
                    List<Expression> parameters = function.getParameters();
                    if (parameters.size() == 2 && parameters.get(0) instanceof Scalar && parameters.get(1) instanceof Scalar) {
                        Scalar scalar = (Scalar)parameters.get(0);
                        if (scalar.getScalarType() == Scalar.Type.STRING) {
                            String text = scalar.getStringValue().substring(1);
                            text = text.substring(0, text.length()-1);
                            PHPStructureItem item = new PHPSimpleStructureItem(new GSFPHPElementHandle.GlobalConstant(info, function), text, "con"); //NOI18N
                            items.add(item);
                        }
                    }
                }
            }
        }




    }

    private abstract class PHPStructureItem implements StructureItem {

        final private GSFPHPElementHandle elementHandle;
        final private List<? extends StructureItem> children;
        final private String sortPrefix;

        public PHPStructureItem(GSFPHPElementHandle elementHandle, List<? extends StructureItem> children, String sortPrefix) {
            this.elementHandle = elementHandle;
            this.sortPrefix = sortPrefix;
            if (children != null) {
                this.children = children;
            } else {
                this.children = Collections.emptyList();
            }
        }

        @Override
        public boolean equals(Object obj) {
            boolean thesame = false;
            if (obj instanceof PHPStructureItem) {
                PHPStructureItem item = (PHPStructureItem)obj;
                thesame = item.elementHandle.getName().equals(elementHandle.getName())
                        && item.elementHandle.getASTNode().getStartOffset() == elementHandle.getASTNode().getStartOffset();
            }
            return thesame;
        }

        @Override
        public int hashCode() {
            //int hashCode = super.hashCode();
            int hashCode = 11;
            hashCode = 31 * elementHandle.getName().hashCode() + hashCode;
            hashCode = 31 * elementHandle.getASTNode().getStartOffset() + hashCode;
            return hashCode;
        }



        public String getName() {
            return elementHandle.getName();
        }

        public String getSortText() {
            return sortPrefix + elementHandle.getName();
        }

        public ElementHandle getElementHandle() {
            return elementHandle;
        }

        public ElementKind getKind() {
            return elementHandle.getKind();
        }

        public Set<Modifier> getModifiers() {
            return elementHandle.getModifiers();
        }

        public boolean isLeaf() {
            return (children.size() == 0);
        }

        public List<? extends StructureItem> getNestedItems() {
            return children;
        }

        public long getPosition() {
            return elementHandle.getASTNode().getStartOffset();
        }

        public long getEndPosition() {
            return elementHandle.getASTNode().getEndOffset();
        }

        public ImageIcon getCustomIcon() {
            return null;
        }

        protected void appendInterfeas(List<Identifier> interfaes, HtmlFormatter formatter) {
            boolean first = true;
            for (Identifier identifier : interfaes) {
                if (identifier != null) {
                    if (!first) {
                        formatter.appendText(", ");  //NOI18N

                    } else {
                        first = false;
                    }
                    formatter.appendText(identifier.getName());
                }

            }
        }

        protected void appendFunctionDescription(FunctionDeclaration function, HtmlFormatter formatter) {
            formatter.reset();
            if (function == null || function.getFunctionName() == null) {
                return;
            }
            formatter.appendText(function.getFunctionName().getName());
            formatter.appendText("(");   //NOI18N

            List<FormalParameter> parameters = function.getFormalParameters();
            if (parameters != null && parameters.size() > 0) {
                boolean first = true;
                for (FormalParameter formalParameter : parameters) {
                    String name = null;
                    Expression parameter = formalParameter.getParameterName();
                    if (parameter != null) {
                        Variable variable = null;
                        boolean isReference = false;
                        if (parameter instanceof Reference) {
                            Reference reference = (Reference)parameter;
                            isReference = true;
                            if (reference.getExpression() instanceof Variable) {
                                variable = (Variable)reference.getExpression();
                            }
                        }
                        else if (parameter instanceof Variable) {
                            variable = (Variable)parameter;
                        }

                        if (variable != null) {
                            name = Utils.resolveVariableName(variable);
                            if (name != null) {
                                name = (variable.isDollared() ? "$" + name: name); //NOI18N
                                if (isReference) {
                                    name = '&' + name; //NOI18N
                                }
                            }
                        }
                        else {
                            name = "??"; //NOI18N
                        }
                    }
                    String type = null;
                    if (formalParameter.getParameterType() != null) {
                        type = formalParameter.getParameterType().getName();
                    }
                    if (name != null) {
                        if (!first) {
                            formatter.appendText(", "); //NOI18N

                        }

                        if (type != null) {
                            formatter.appendHtml(FONT_GRAY_COLOR);
                            formatter.appendText(type);
                            formatter.appendText(" ");   //NOI18N

                            formatter.appendHtml(CLOSE_FONT);
                        }
                        formatter.appendText(name);
                        first = false;
                    }
                }
            }
            formatter.appendText(")");   //NOI18N

        }
    }

    private class PHPSimpleStructureItem extends PHPStructureItem {

        private String simpleText;

        public PHPSimpleStructureItem(GSFPHPElementHandle elementHandle, String simpleText, String prefix) {
            super(elementHandle, null, prefix);
            this.simpleText = simpleText;
        }

        public String getHtml(HtmlFormatter formatter) {
            formatter.appendText(simpleText);
            return formatter.getText();
        }

    }

    private class PHPClassStructureItem extends PHPStructureItem {

        public PHPClassStructureItem(GSFPHPElementHandle elementHandle, List<? extends StructureItem> children) {
            super(elementHandle, children, "cl"); //NOI18N
        }

        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            ClassDeclarationHandle handle = (ClassDeclarationHandle) getElementHandle();
            formatter.appendText(handle.getName());
            ClassDeclaration cldec = (ClassDeclaration) handle.getASTNode();
            if (cldec.getSuperClass() != null) {
                formatter.appendHtml(FONT_GRAY_COLOR + "::"); //NOI18N
                formatter.appendText(cldec.getSuperClass().getName());
                formatter.appendHtml(CLOSE_FONT);
            }
            List<Identifier> interfaes = cldec.getInterfaes();
            if (interfaes != null && interfaes.size() > 0) {
                formatter.appendHtml(FONT_GRAY_COLOR + ":"); //NOI18N
                appendInterfeas(interfaes, formatter);
                formatter.appendHtml(CLOSE_FONT);
            }
            return formatter.getText();
        }

    }

    private class PHPFieldFromPropertyTagItem extends PHPStructureItem {

        private FieldsFromTagProperty tagElement;
        
        public PHPFieldFromPropertyTagItem(FieldsFromTagProperty elementHandle, String sortPrefix) {
            super(elementHandle, null, sortPrefix);
            this.tagElement = elementHandle;
        }

        public String getHtml(HtmlFormatter formatter) {
            return tagElement.getName();
        }
    }

    private class PHPFunctionStructureItem extends PHPStructureItem {

        public PHPFunctionStructureItem(GSFPHPElementHandle elementHandle) {
            super(elementHandle, null, "fn"); //NOI18N
        }

        public String getHtml(HtmlFormatter formatter) {
                formatter.reset();
                FunctionDeclarationHandle handle = (FunctionDeclarationHandle)getElementHandle();
                FunctionDeclaration function = (FunctionDeclaration)handle.getASTNode();
                appendFunctionDescription(function, formatter);
                return formatter.getText();
        }

    }

    private class PHPMethodStructureItem extends PHPStructureItem {

        public PHPMethodStructureItem(GSFPHPElementHandle elementHandle) {
            super(elementHandle, null, "fn"); //NOI18N
        }

        public String getHtml(HtmlFormatter formatter) {
                formatter.reset();
                MethodDeclarationHandle handle = (MethodDeclarationHandle)getElementHandle();
                MethodDeclaration method = (MethodDeclaration)handle.getASTNode();
                appendFunctionDescription(method.getFunction(), formatter);
                return formatter.getText();
        }

    }

    private class PHPInterfaceStructureItem extends PHPStructureItem {

        private static final String PHP_INTERFACE_ICON = "org/netbeans/modules/php/editor/resources/interface.png"; //NOI18N

        public PHPInterfaceStructureItem(GSFPHPElementHandle elementHandle, List<? extends StructureItem> children) {
            super(elementHandle, children, "cl"); //NOI18N
        }

        @Override
        public ImageIcon getCustomIcon() {
            if (INTERFACE_ICON == null) {
                INTERFACE_ICON = new ImageIcon(org.openide.util.Utilities.loadImage(PHP_INTERFACE_ICON));
            }
            return INTERFACE_ICON;
        }

        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            formatter.appendText(getElementHandle().getName());
            InterfaceDeclarationHandle handle = (InterfaceDeclarationHandle) getElementHandle();
            InterfaceDeclaration indec = (InterfaceDeclaration) handle.getASTNode();
            List<Identifier> interfaes = indec.getInterfaes();
            if (interfaes != null && interfaes.size() > 0) {
                formatter.appendHtml(FONT_GRAY_COLOR + "::"); //NOI18N
                appendInterfeas(interfaes, formatter);
                formatter.appendHtml(CLOSE_FONT);
            }
            return formatter.getText();
        }
    }

    private class PHPConstructorStructureItem extends PHPStructureItem {

        public PHPConstructorStructureItem(GSFPHPElementHandle elementHandle) {
            super(elementHandle, null, "con");
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CONSTRUCTOR;
        }

        public String getHtml(HtmlFormatter formatter) {
                formatter.reset();
                MethodDeclarationHandle handle = (MethodDeclarationHandle)getElementHandle();
                MethodDeclaration method = (MethodDeclaration)handle.getASTNode();
                appendFunctionDescription(method.getFunction(), formatter);
                return formatter.getText();
        }

    }

    private class FoldVisitor extends DefaultVisitor {

        final Map<String, List<OffsetRange>> folds;
        private String foldType;

        public FoldVisitor(Map<String, List<OffsetRange>> folds) {
            this.folds = folds;
            foldType = null;

        }

        @Override
        public void visit(ClassDeclaration cldec) {
            foldType = FOLD_CLASS;
            if (cldec.getBody() != null) {
                scan(cldec.getBody());
            }
        }

        @Override
        public void visit(InterfaceDeclaration node) {
            foldType = FOLD_CLASS;
            if (node.getBody() != null) {
                scan(node.getBody());
            }
        }

        @Override
        public void visit(Block block) {
            if (foldType != null) {
                getRanges(folds, foldType).add(createOffsetRange(block));
                foldType = null;
            }
            if (block.getStatements() != null) {
                scan(block.getStatements());
            }
        }

        @Override
        public void visit(FunctionDeclaration function) {
            foldType = FOLD_CODE_BLOCKS;
            if (function.getBody() != null) {
                scan(function.getBody());
            }
        }
    }
}
