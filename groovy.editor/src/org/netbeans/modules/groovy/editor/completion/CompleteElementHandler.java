/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.editor.completion;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.AstUtilities;
import org.netbeans.modules.groovy.editor.api.GroovyIndex;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedClass;
import org.netbeans.modules.groovy.editor.java.Utilities;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
public final class CompleteElementHandler {

    private static final Logger LOG = Logger.getLogger(CompleteElementHandler.class.getName());

    private final ParserResult info;

    private final GroovyIndex index;

    private CompleteElementHandler(ParserResult info) {
        this.info = info;

        FileObject fo = info.getSnapshot().getSource().getFileObject();
        if (fo != null) {
            // FIXME index is broken when invoked on start
            this.index = GroovyIndex.get(QuerySupport.findRoots(fo,
                    Collections.singleton(ClassPath.SOURCE), null, null));
        } else {
            index = null;
        }
    }

    public static CompleteElementHandler forCompilationInfo(ParserResult info) {
        return new CompleteElementHandler(info);
    }

    // FIXME ideally there should be something like nice CompletionRequest once public and stable
    // then this class could implement some common interface
    public Map<MethodSignature, CompletionItem> getMethods(
            ClassNode source, ClassNode node, String prefix, int anchor, boolean nameOnly) {

        //Map<MethodSignature, CompletionItem> meta = new HashMap<MethodSignature, CompletionItem>();

        Map<MethodSignature, CompletionItem> result = getMethodsInner(
                source, node, prefix, anchor, 0, AccessLevel.create(source, node), nameOnly);

        //fillSuggestions(meta, result);
        return result;
    }

    public Map<FieldSignature, CompletionItem> getFields(
            ClassNode source, ClassNode node, String prefix, int anchor) {

        //Map<MethodSignature, CompletionItem> meta = new HashMap<MethodSignature, CompletionItem>();
        Map<FieldSignature, CompletionItem> result = getFieldsInner(source, node, prefix, anchor, 0);

        //fillSuggestions(meta, result);
        return result;
    }

    // FIXME configure acess levels
    private Map<MethodSignature, CompletionItem> getMethodsInner(
            ClassNode source, ClassNode node, String prefix, int anchor, int level, Set<AccessLevel> access, boolean nameOnly) {

        boolean leaf = (level == 0);
        Set<AccessLevel> modifiedAccess = AccessLevel.update(access, source, node);

        Map<MethodSignature, CompletionItem> result = new HashMap<MethodSignature, CompletionItem>();
        ClassDefinition definition = loadDefinition(node);
        ClassNode typeNode = definition.getNode();

        Map<MethodSignature, ? extends CompletionItem> groovyItems = GroovyElementHandler.forCompilationInfo(info)
                .getMethods(index, typeNode.getName(), prefix, anchor, leaf, access, nameOnly);

        fillSuggestions(groovyItems, result);

        // we can't go groovy and java - helper methods would be visible
        if (groovyItems.isEmpty()) {
            String[] typeParameters = new String[(typeNode.isUsingGenerics() && typeNode.getGenericsTypes() != null)
                    ? typeNode.getGenericsTypes().length : 0];
            for (int i = 0; i < typeParameters.length; i++) {
                GenericsType genType = typeNode.getGenericsTypes()[i];
                if (genType.getUpperBounds() != null) {
                    typeParameters[i] = Utilities.translateClassLoaderTypeName(genType.getUpperBounds()[0].getName());
                } else {
                    typeParameters[i] = Utilities.translateClassLoaderTypeName(genType.getName());
                }
            }

            fillSuggestions(JavaElementHandler.forCompilationInfo(info)
                    .getMethods(typeNode.getName(), prefix, anchor, typeParameters,
                            leaf, modifiedAccess, nameOnly), result);
        }

        // FIXME not sure about order of the meta methods, perhaps interface
        // methods take precedence
        fillSuggestions(MetaElementHandler.forCompilationInfo(info)
                .getMethods(typeNode.getName(), prefix, anchor, nameOnly), result);

        if (source != null) {
            fillSuggestions(DynamicElementHandler.forCompilationInfo(info)
                    .getMethods(source.getName(), typeNode.getName(), prefix, anchor, nameOnly, leaf, definition.getFileObject()), result);
        }

        if (typeNode.getSuperClass() != null) {
            fillSuggestions(getMethodsInner(source, typeNode.getSuperClass(),
                    prefix, anchor, level + 1, modifiedAccess, nameOnly), result);
        } else if (leaf) {
            fillSuggestions(JavaElementHandler.forCompilationInfo(info)
                    .getMethods("java.lang.Object", prefix, anchor, new String[]{}, false, modifiedAccess, nameOnly), result); // NOI18N
        }

        for (ClassNode inter : typeNode.getInterfaces()) {
            fillSuggestions(getMethodsInner(source, inter,
                    prefix, anchor, level + 1, modifiedAccess, nameOnly), result);
        }

        return result;
    }

    private Map<FieldSignature, CompletionItem> getFieldsInner(
            ClassNode source, ClassNode node, String prefix, int anchor, int level) {

        boolean leaf = (level == 0);

        Map<FieldSignature, CompletionItem> result = new HashMap<FieldSignature, CompletionItem>();
        ClassDefinition definition = loadDefinition(node);
        ClassNode typeNode = definition.getNode();

        fillSuggestions(GroovyElementHandler.forCompilationInfo(info)
                .getFields(index, typeNode.getName(), prefix, anchor, leaf), result);

        fillSuggestions(JavaElementHandler.forCompilationInfo(info)
                .getFields(typeNode.getName(), prefix, anchor, leaf), result);

        // FIXME not sure about order of the meta methods, perhaps interface
        // methods take precedence
        fillSuggestions(MetaElementHandler.forCompilationInfo(info)
                .getFields(typeNode.getName(), prefix, anchor), result);

        if (source != null) {
            fillSuggestions(DynamicElementHandler.forCompilationInfo(info)
                    .getFields(source.getName(), typeNode.getName(), prefix, anchor, leaf, definition.getFileObject()), result);
        }

        if (typeNode.getSuperClass() != null) {
            fillSuggestions(getFieldsInner(source, typeNode.getSuperClass(), prefix, anchor, level + 1), result);
        } else if (leaf) {
            fillSuggestions(JavaElementHandler.forCompilationInfo(info)
                    .getFields("java.lang.Object", prefix, anchor, false), result); // NOI18N
        }

        for (ClassNode inter : typeNode.getInterfaces()) {
            fillSuggestions(getFieldsInner(source, inter, prefix, anchor, level + 1), result);
        }

        return result;
    }

    private ClassDefinition loadDefinition(ClassNode node) {
        if (index == null) {
            return new ClassDefinition(node, null);
        }

        Set<IndexedClass> classes = index.getClasses(node.getName(), QuerySupport.Kind.EXACT, true, false, false);

        if (!classes.isEmpty()) {
            IndexedClass indexed = classes.iterator().next();
            ASTNode astNode = AstUtilities.getForeignNode(indexed);
            if (astNode instanceof ClassNode) {
                return new ClassDefinition((ClassNode) astNode, indexed);
            }
        }

        return new ClassDefinition(node, null);
    }

    private static <T> void fillSuggestions(Map<T, ? extends CompletionItem> input, Map<T, ? super CompletionItem> result) {
        for (Map.Entry<T, ? extends CompletionItem> entry : input.entrySet()) {
            if (!result.containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private static class ClassDefinition {

        private final ClassNode node;

        private final IndexedClass indexed;

        public ClassDefinition(ClassNode node, IndexedClass indexed) {
            this.node = node;
            this.indexed = indexed;
        }

        public ClassNode getNode() {
            return node;
        }

        public FileObject getFileObject() {
            return indexed != null ? indexed.getFileObject() : null;
        }
    }
}
