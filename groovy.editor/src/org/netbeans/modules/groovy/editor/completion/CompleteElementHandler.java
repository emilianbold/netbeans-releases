/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.groovy.editor.completion;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.netbeans.modules.groovy.editor.api.AstUtilities;
import org.netbeans.modules.groovy.editor.api.GroovyIndex;
import org.netbeans.modules.groovy.editor.api.elements.IndexedClass;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.completion.JavaElementHandler.ClassType;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.NameKind;

/**
 *
 * @author Petr Hejl
 */
public final class CompleteElementHandler {

    private static final Logger LOG = Logger.getLogger(CompleteElementHandler.class.getName());

    private final CompilationInfo info;

    private CompleteElementHandler(CompilationInfo info) {
        this.info = info;
    }

    public static CompleteElementHandler forCompilationInfo(CompilationInfo info) {
        return new CompleteElementHandler(info);
    }

    // FIXME ideally there should be something like nice CompletionRequest once public and stable
    // then this class could implement some common interface
    public Map<MethodSignature, ? extends CompletionItem> getMethods(
            CompletionType completionType, ClassNode node, ClassType type, String prefix, int anchor) {

        //Map<MethodSignature, CompletionItem> meta = new HashMap<MethodSignature, CompletionItem>();
        Map<MethodSignature, CompletionItem> result = getMethodsInner(
                completionType, node, type, prefix, anchor);

        //fillSuggestions(meta, result);
        return result;
    }

    // FIXME configure acess levels
    private Map<MethodSignature, CompletionItem> getMethodsInner(
            CompletionType completionType, ClassNode node, ClassType type, String prefix, int anchor) {

        Map<MethodSignature, CompletionItem> result = new HashMap<MethodSignature, CompletionItem>();
        ClassNode typeNode = loadDefinition(completionType, node, type);

        fillSuggestions(GroovyElementHandler.forCompilationInfo(info)
                .getMethods(typeNode.getName(), prefix, anchor, type == ClassType.CLASS), result);

        String[] typeParameters = new String[(typeNode.isUsingGenerics() && typeNode.getGenericsTypes() != null)
                ? typeNode.getGenericsTypes().length : 0];
        for (int i = 0; i < typeParameters.length; i++) {
            GenericsType genType = typeNode.getGenericsTypes()[i];
            if (genType.getUpperBounds() != null) {
                typeParameters[i] = org.netbeans.modules.groovy.editor.java.Utilities.translateClassLoaderTypeName(
                        genType.getUpperBounds()[0].getName());
            } else {
                typeParameters[i] = org.netbeans.modules.groovy.editor.java.Utilities.translateClassLoaderTypeName(
                        genType.getName());
            }
        }

        fillSuggestions(JavaElementHandler.forCompilationInfo(info)
                .getMethods(typeNode.getName(), prefix, anchor, typeParameters, type == ClassType.CLASS), result);

        // FIXME not sure about order of the meta methods, perhaps interface
        // methods take precedence
        fillSuggestions(MetaElementHandler.forCompilationInfo(info)
                .getMethods(typeNode.getName(), prefix, anchor), result);

        if (typeNode.getSuperClass() != null) {
            fillSuggestions(getMethods(completionType, typeNode.getSuperClass(),
                    ClassType.SUPERCLASS, prefix, anchor), result);
        } else if (type == ClassType.CLASS) {
            fillSuggestions(JavaElementHandler.forCompilationInfo(info)
                    .getMethods("java.lang.Object", prefix, anchor, new String[]{}, false), result); // NOI18N
        }

        for (ClassNode inter : typeNode.getInterfaces()) {
            fillSuggestions(getMethods(completionType, inter,
                    ClassType.SUPERINTERFACE, prefix, anchor), result);
        }

        return result;
    }

    private ClassNode loadDefinition(CompletionType completionType, ClassNode node, ClassType type) {
        if (completionType == CompletionType.THIS && type == ClassType.CLASS) {
            return node;
        }

        ClassNode superNode = null;
        String name = node.getName();

        if (completionType == CompletionType.SUPER && type == ClassType.CLASS) {
            superNode = node.getSuperClass();
            if (superNode != null) {
                name = superNode.getName();
            } else {
                return new ClassNode("java.lang.Object", ClassNode.ACC_PUBLIC, null);
            }
        }

        // FIXME index is broken when invoked on start
        GroovyIndex index = new GroovyIndex(info.getIndex(GroovyTokenId.GROOVY_MIME_TYPE));

        if (index == null) {
            return superNode != null ? superNode : node;
        }

        Set<IndexedClass> classes = index.getClasses(name, NameKind.EXACT_NAME, true, false, false);

        if (!classes.isEmpty()) {
            ASTNode astNode = AstUtilities.getForeignNode(classes.iterator().next());
            if (astNode instanceof ClassNode) {
                return (ClassNode) astNode;
            }
        }

        return superNode != null ? superNode : node;
    }

    private static <T> void fillSuggestions(Map<T, ? extends CompletionItem> input, Map<T, ? super CompletionItem> result) {
        for (Map.Entry<T, ? extends CompletionItem> entry : input.entrySet()) {
            if (!result.containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public static enum CompletionType {

        OBJECT,

        THIS,

        SUPER
    }
}
