/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.codegen;

import java.util.*;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.*;
import org.netbeans.modules.php.editor.codegen.CGSGenerator.GenWay;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.*;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 */
public class CGSInfo {

    private String className;
    // cotain the class consructor?
    private boolean hasConstructor;
    final private List<Property> properties;
    final private List<Property> possibleGetters;
    final private List<Property> possibleSetters;
    final private List<Property> possibleGettersSetters;
    final private List<MethodProperty> possibleMethods;
    final private JTextComponent textComp;
    /**
     * how to generate  getters and setters method name
     */
    private CGSGenerator.GenWay howToGenerate;
    private boolean generateDoc;
    private boolean fluentSetter;

    private CGSInfo(JTextComponent textComp) {
        properties = new ArrayList<Property>();
        possibleGetters = new ArrayList<Property>();
        possibleSetters = new ArrayList<Property>();
        possibleGettersSetters = new ArrayList<Property>();
        possibleMethods = new ArrayList<MethodProperty>();
        className = null;
        this.textComp = textComp;
        hasConstructor = false;
        this.generateDoc = true;
        fluentSetter = false;
        this.howToGenerate = CGSGenerator.GenWay.AS_JAVA;
    }

    public static CGSInfo getCGSInfo(JTextComponent textComp) {
        CGSInfo info = new CGSInfo(textComp);
        info.findPropertyInScope();
        return info;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public List<MethodProperty> getPossibleMethods() {
        return possibleMethods;
    }

    public List<Property> getPossibleGetters() {
        return possibleGetters;
    }

    public List<Property> getPossibleGettersSetters() {
        return possibleGettersSetters;
    }

    public List<Property> getPossibleSetters() {
        return possibleSetters;
    }

    public String getClassName() {
        return className;
    }

    public boolean hasConstructor() {
        return hasConstructor;
    }

    public GenWay getHowToGenerate() {
        return howToGenerate;
    }

    public void setHowToGenerate(GenWay howGenerate) {
        this.howToGenerate = howGenerate;
    }

    public boolean isGenerateDoc() {
        return generateDoc;
    }

    public void setGenerateDoc(boolean generateDoc) {
        this.generateDoc = generateDoc;
    }

    public boolean isFluentSetter() {
        return fluentSetter;
    }

    public void setFluentSetter(final boolean fluentSetter) {
        this.fluentSetter = fluentSetter;
    }

    /**
     * Extract attributes and methods from caret enclosing class and initialize list of properties.
     */
    private void findPropertyInScope() {
        FileObject file = NavUtils.getFile(textComp.getDocument());
        if (file == null) {
            return;
        }
        try {
            ParserManager.parse(Collections.singleton(Source.create(textComp.getDocument())), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    PHPParseResult info = (PHPParseResult) resultIterator.getParserResult();
                    if (info != null) {
                        int caretOffset = textComp.getCaretPosition();
                        ClassDeclaration classDecl = findEnclosingClass(info, caretOffset);
                        if (classDecl != null) {
                            className = classDecl.getName().getName();
                            if (className != null) {
                                FileObject fileObject = info.getSnapshot().getSource().getFileObject();
                                Index index = ElementQueryFactory.getIndexQuery(info);
                                final ElementFilter forFilesFilter = ElementFilter.forFiles(fileObject);
                                QualifiedName fullyQualifiedName = VariousUtils.getFullyQualifiedName(QualifiedName.create(className), caretOffset, info.getModel().getVariableScope(caretOffset));
                                Set<ClassElement> classes = forFilesFilter.filter(index.getClasses(NameKind.exact(fullyQualifiedName)));
                                for (ClassElement classElement : classes) {
                                    ElementFilter forNotDeclared = ElementFilter.forExcludedElements(index.getDeclaredMethods(classElement));
                                    final Set<MethodElement> accessibleMethods = new HashSet<MethodElement>();
                                    accessibleMethods.addAll(forNotDeclared.filter(index.getAccessibleMethods(classElement, classElement)));
                                    accessibleMethods.addAll(ElementFilter.forExcludedElements(accessibleMethods).filter(forNotDeclared.filter(index.getConstructors(classElement))));
                                    accessibleMethods.addAll(ElementFilter.forExcludedElements(accessibleMethods).filter(forNotDeclared.filter(index.getAccessibleMagicMethods(classElement))));
                                    final Set<TypeElement> preferedTypes = forFilesFilter.prefer(ElementTransformation.toMemberTypes().transform(accessibleMethods));
                                    final TreeElement<TypeElement> enclosingType = index.getInheritedTypesAsTree(classElement, preferedTypes);
                                    final List<MethodProperty> properties = new ArrayList<MethodProperty>();
                                    final Set<MethodElement> methods = ElementFilter.forMembersOfTypes(preferedTypes).filter(accessibleMethods);
                                    for (final MethodElement methodElement : methods) {
                                        if (!methodElement.isFinal()) {
                                            properties.add(new MethodProperty(methodElement, enclosingType));
                                        }
                                    }
                                    Collections.<MethodProperty>sort(properties, MethodProperty.getComparator());
                                    getPossibleMethods().addAll(properties);
                                }
                            }

                            List<String> existingGetters = new ArrayList<String>();
                            List<String> existingSetters = new ArrayList<String>();

                            PropertiesVisitor visitor = new PropertiesVisitor(existingGetters, existingSetters, Utils.getRoot(info));
                            visitor.scan(classDecl);
                            String propertyName;
                            boolean existGetter, existSetter;
                            for (Property property : getProperties()) {
                                propertyName = property.getName().toLowerCase();
                                existGetter = existingGetters.contains(propertyName);
                                existSetter = existingSetters.contains(propertyName);
                                if (!existGetter && !existSetter) {
                                    getPossibleGettersSetters().add(property);
                                    getPossibleGetters().add(property);
                                    getPossibleSetters().add(property);
                                } else if (!existGetter) {
                                    getPossibleGetters().add(property);
                                } else if (!existSetter) {
                                    getPossibleSetters().add(property);
                                }
                            }
                        }
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Find out class enclosing caret
     * @param info
     * @param offset caret offset
     * @return class declaration or null
     */
    private ClassDeclaration findEnclosingClass(ParserResult info, int offset) {
        List<ASTNode> nodes = NavUtils.underCaret(info, offset);
        int count = nodes.size();
        if (count > 2) {  // the cursor has to be in class block see issue #142417
            ASTNode declaration = nodes.get(count - 2);
            ASTNode block = nodes.get(count - 1);
            if (block instanceof Block &&  declaration instanceof ClassDeclaration) {
                return (ClassDeclaration) declaration;
            }
        }
        return null;
    }

    private class PropertiesVisitor extends DefaultVisitor {

        private final List<String> existingGetters;
        private final List<String> existingSetters;
        private final Program program;

        public PropertiesVisitor(List<String> existingGetters, List<String> existingSetters, Program program) {
            this.existingGetters = existingGetters;
            this.existingSetters = existingSetters;
            this.program = program;
        }

        @Override
        public void visit(FieldsDeclaration node) {
            List<SingleFieldDeclaration> fields = node.getFields();
            if (!BodyDeclaration.Modifier.isStatic(node.getModifier())) {
                for (SingleFieldDeclaration singleFieldDeclaration : fields) {
                    Variable variable = singleFieldDeclaration.getName();
                    if (variable != null && variable.getName() instanceof Identifier) {
                        String name = ((Identifier) variable.getName()).getName();
                        getProperties().add(new Property(name, node.getModifier(), getPropertyType(singleFieldDeclaration)));
                    }
                }
            }
        }

        private String getPropertyType(final ASTNode node) {
            String result = ""; //NOI18N
            Comment comment = Utils.getCommentForNode(program, node);
            if (comment instanceof PHPDocBlock) {
                result = getFirstTypeFromBlock((PHPDocBlock) comment);
            }
            return result;
        }

        private String getFirstTypeFromBlock(final PHPDocBlock phpDoc) {
            String result = ""; //NOI18N
            for (PHPDocTag pHPDocTag : phpDoc.getTags()) {
                if (pHPDocTag instanceof PHPDocTypeTag && pHPDocTag.getKind().equals(PHPDocTag.Type.VAR)) {
                    result = getFirstTypeFromTag((PHPDocTypeTag) pHPDocTag);
                    if (!result.isEmpty()) {
                        break;
                    }
                }
            }
            return result;
        }

        private String getFirstTypeFromTag(final PHPDocTypeTag typeTag) {
            String result = ""; //NOI18N
            for (PHPDocTypeNode typeNode : typeTag.getTypes()) {
                String type = typeNode.getValue();
                if (!VariousUtils.isPrimitiveType(type) && !VariousUtils.isSpecialClassName(type)) {
                    result = type;
                    break;
                }
            }
            return result;
        }

        @Override
        public void visit(MethodDeclaration node) {
            String name = node.getFunction().getFunctionName().getName();
            String possibleProperty;
            if (name != null) {
                if (name.startsWith(CGSGenerator.START_OF_GETTER)) {
                    possibleProperty = name.substring(CGSGenerator.START_OF_GETTER.length());
                    existingGetters.addAll(getAllPossibleProperties(possibleProperty));
                } else if (name.startsWith(CGSGenerator.START_OF_SETTER)) {
                    possibleProperty = name.substring(CGSGenerator.START_OF_GETTER.length());
                    existingSetters.addAll(getAllPossibleProperties(possibleProperty));
                }
                else if (className!= null && (className.equals(name) || "__construct".equals(name))) { //NOI18N
                    hasConstructor = true;
                }
            }
        }

        /**
         * Returns all possible properties which are based on the passed property derived from method name.
         *
         * @param possibleProperty Name of the property which was derived from method name (setField() -> field).
         * @return field => (field, _field) OR _field => (_field, field)
         */
        private List<String> getAllPossibleProperties(String possibleProperty) {
            List<String> allPossibleProperties = new LinkedList<String>();
            possibleProperty = possibleProperty.toLowerCase();
            allPossibleProperties.add(possibleProperty);
            if (possibleProperty.startsWith("_")) { // NOI18N
                allPossibleProperties.add(possibleProperty.substring(1));
            } else {
                allPossibleProperties.add("_" + possibleProperty); // NOI18N
            }
            return allPossibleProperties;
        }
    }
}
