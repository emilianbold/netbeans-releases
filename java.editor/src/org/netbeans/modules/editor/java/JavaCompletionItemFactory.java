/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.java;

import java.util.EnumSet;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.modules.parsing.api.Source;

/**
 *
 * @author Dusan Balek
 */
public interface JavaCompletionItemFactory {
    
    JavaCompletionItem createKeywordItem(String kwd, String postfix, int substitutionOffset, boolean smartType);
    JavaCompletionItem createPackageItem(String pkgFQN, int substitutionOffset, boolean inPackageStatement);
    JavaCompletionItem createTypeItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars, boolean addSimpleName, boolean smartType, boolean autoImportEnclosingType, WhiteListQuery.WhiteList whiteList);
    JavaCompletionItem createTypeItem(ElementHandle<TypeElement> handle, EnumSet<ElementKind> kinds, int substitutionOffset, ReferencesCount referencesCount, Source source, boolean insideNew, boolean addTypeVars, boolean afterExtends, WhiteListQuery.WhiteList whiteList);
    JavaCompletionItem createArrayItem(CompilationInfo info, ArrayType type, int substitutionOffset, ReferencesCount referencesCount, Elements elements, WhiteListQuery.WhiteList whiteList);
    JavaCompletionItem createTypeParameterItem(TypeParameterElement elem, int substitutionOffset);
    JavaCompletionItem createVariableItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean smartType, int assignToVarOffset, WhiteListQuery.WhiteList whiteList);
    JavaCompletionItem createVariableItem(CompilationInfo info, String varName, int substitutionOffset, boolean newVarName, boolean smartType);
    JavaCompletionItem createExecutableItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean inImport, boolean addSemicolon, boolean smartType, int assignToVarOffset, boolean memberRef, WhiteListQuery.WhiteList whiteList);
    JavaCompletionItem createThisOrSuperConstructorItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, String name, WhiteListQuery.WhiteList whiteList);
    JavaCompletionItem createOverrideMethodItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean implement, WhiteListQuery.WhiteList whiteList);
    JavaCompletionItem createGetterSetterMethodItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, String name, boolean setter);
    JavaCompletionItem createDefaultConstructorItem(TypeElement elem, int substitutionOffset, boolean smartType);
    JavaCompletionItem createParametersItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, int activeParamIndex, String name);
    JavaCompletionItem createAnnotationItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, WhiteListQuery.WhiteList whiteList);
    JavaCompletionItem createAttributeItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated);
    JavaCompletionItem createAttributeValueItem(CompilationInfo info, String value, String documentation, TypeElement element, int substitutionOffset, ReferencesCount referencesCount, WhiteListQuery.WhiteList whiteList);
    JavaCompletionItem createStaticMemberItem(CompilationInfo info, DeclaredType type, Element memberElem, TypeMirror memberType, boolean multipleVersions, int substitutionOffset, boolean isDeprecated, boolean addSemicolon, WhiteListQuery.WhiteList whiteList);
    JavaCompletionItem createStaticMemberItem(ElementHandle<TypeElement> handle, String name, int substitutionOffset, boolean addSemicolon, ReferencesCount referencesCount, Source source, WhiteListQuery.WhiteList whiteList);
    JavaCompletionItem createChainedMembersItem(CompilationInfo info, List<? extends Element> chainedElems, List<? extends TypeMirror> chainedTypes, int substitutionOffset, boolean isDeprecated, boolean addSemicolon, WhiteListQuery.WhiteList whiteList);
    JavaCompletionItem createInitializeAllConstructorItem(CompilationInfo info, boolean isDefault, Iterable<? extends VariableElement> fields, ExecutableElement superConstructor, TypeElement parent, int substitutionOffset);

    static final class DefaultImpl implements JavaCompletionItemFactory {
        
        @Override
        public JavaCompletionItem createKeywordItem(String kwd, String postfix, int substitutionOffset, boolean smartType) {
            return JavaCompletionItem.createKeywordItem(kwd, postfix, substitutionOffset, smartType);
        }

        @Override
        public JavaCompletionItem createPackageItem(String pkgFQN, int substitutionOffset, boolean inPackageStatement) {
            return JavaCompletionItem.createPackageItem(pkgFQN, substitutionOffset, inPackageStatement);
        }

        @Override
        public JavaCompletionItem createTypeItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars, boolean addSimpleName, boolean smartType, boolean autoImportEnclosingType, WhiteListQuery.WhiteList whiteList) {
            return JavaCompletionItem.createTypeItem(info, elem, type, substitutionOffset, referencesCount, isDeprecated, insideNew, addTypeVars, addSimpleName, smartType, autoImportEnclosingType, whiteList);
        }

        @Override
        public JavaCompletionItem createTypeItem(ElementHandle<TypeElement> handle, EnumSet<ElementKind> kinds, int substitutionOffset, ReferencesCount referencesCount, Source source, boolean insideNew, boolean addTypeVars, boolean afterExtends, WhiteListQuery.WhiteList whiteList) {
            return LazyJavaCompletionItem.createTypeItem(handle, kinds, substitutionOffset, referencesCount, source, insideNew, addTypeVars, afterExtends, whiteList);
        }

        @Override
        public JavaCompletionItem createArrayItem(CompilationInfo info, ArrayType type, int substitutionOffset, ReferencesCount referencesCount, Elements elements, WhiteListQuery.WhiteList whiteList) {
            return JavaCompletionItem.createArrayItem(info, type, substitutionOffset, referencesCount, elements, whiteList);
        }

        @Override
        public JavaCompletionItem createTypeParameterItem(TypeParameterElement elem, int substitutionOffset) {
            return JavaCompletionItem.createTypeParameterItem(elem, substitutionOffset);
        }

        @Override
        public JavaCompletionItem createVariableItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean smartType, int assignToVarOffset, WhiteListQuery.WhiteList whiteList) {
            return JavaCompletionItem.createVariableItem(info, elem, type, substitutionOffset, referencesCount, isInherited, isDeprecated, smartType, assignToVarOffset, whiteList);
        }

        @Override
        public JavaCompletionItem createVariableItem(CompilationInfo info, String varName, int substitutionOffset, boolean newVarName, boolean smartType) {
            return JavaCompletionItem.createVariableItem(info, varName, substitutionOffset, newVarName, smartType);
        }

        @Override
        public JavaCompletionItem createExecutableItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean inImport, boolean addSemicolon, boolean smartType, int assignToVarOffset, boolean memberRef, WhiteListQuery.WhiteList whiteList) {
            return JavaCompletionItem.createExecutableItem(info, elem, type, substitutionOffset, referencesCount, isInherited, isDeprecated, inImport, addSemicolon, smartType, assignToVarOffset, memberRef, whiteList);
        }

        @Override
        public JavaCompletionItem createThisOrSuperConstructorItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, String name, WhiteListQuery.WhiteList whiteList) {
            return JavaCompletionItem.createThisOrSuperConstructorItem(info, elem, type, substitutionOffset, isDeprecated, name, whiteList);
        }

        @Override
        public JavaCompletionItem createOverrideMethodItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean implement, WhiteListQuery.WhiteList whiteList) {
            return JavaCompletionItem.createOverrideMethodItem(info, elem, type, substitutionOffset, implement, whiteList);
        }

        @Override
        public JavaCompletionItem createGetterSetterMethodItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, String name, boolean setter) {
            return JavaCompletionItem.createGetterSetterMethodItem(info, elem, type, substitutionOffset, name, setter);
        }

        @Override
        public JavaCompletionItem createDefaultConstructorItem(TypeElement elem, int substitutionOffset, boolean smartType) {
            return JavaCompletionItem.createDefaultConstructorItem(elem, substitutionOffset, smartType);
        }

        @Override
        public JavaCompletionItem createParametersItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, int activeParamIndex, String name) {
            return JavaCompletionItem.createParametersItem(info, elem, type, substitutionOffset, isDeprecated, activeParamIndex, name);
        }

        @Override
        public JavaCompletionItem createAnnotationItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, WhiteListQuery.WhiteList whiteList) {
            return JavaCompletionItem.createAnnotationItem(info, elem, type, substitutionOffset, referencesCount, isDeprecated, whiteList);
        }

        @Override
        public JavaCompletionItem createAttributeItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated) {
            return JavaCompletionItem.createAttributeItem(info, elem, type, substitutionOffset, isDeprecated);
        }

        @Override
        public JavaCompletionItem createAttributeValueItem(CompilationInfo info, String value, String documentation, TypeElement element, int substitutionOffset, ReferencesCount referencesCount, WhiteListQuery.WhiteList whiteList) {
            return JavaCompletionItem.createAttributeValueItem(info, value, documentation, element, substitutionOffset, referencesCount, whiteList);
        }

        @Override
        public JavaCompletionItem createStaticMemberItem(CompilationInfo info, DeclaredType type, Element memberElem, TypeMirror memberType, boolean multipleVersions, int substitutionOffset, boolean isDeprecated, boolean addSemicolon, WhiteListQuery.WhiteList whiteList) {
            return JavaCompletionItem.createStaticMemberItem(info, type, memberElem, memberType, multipleVersions, substitutionOffset, isDeprecated, addSemicolon, whiteList);
        }

        @Override
        public JavaCompletionItem createStaticMemberItem(ElementHandle<TypeElement> handle, String name, int substitutionOffset, boolean addSemicolon, ReferencesCount referencesCount, Source source, WhiteListQuery.WhiteList whiteList) {
            return LazyJavaCompletionItem.createStaticMemberItem(handle, name, substitutionOffset, addSemicolon, referencesCount, source, whiteList);
        }

        @Override
        public JavaCompletionItem createChainedMembersItem(CompilationInfo info, List<? extends Element> chainedElems, List<? extends TypeMirror> chainedTypes, int substitutionOffset, boolean isDeprecated, boolean addSemicolon, WhiteListQuery.WhiteList whiteList) {
            return JavaCompletionItem.createChainedMembersItem(info, chainedElems, chainedTypes, substitutionOffset, isDeprecated, addSemicolon, whiteList);
        }

        @Override
        public JavaCompletionItem createInitializeAllConstructorItem(CompilationInfo info, boolean isDefault, Iterable<? extends VariableElement> fields, ExecutableElement superConstructor, TypeElement parent, int substitutionOffset) {
            return JavaCompletionItem.createInitializeAllConstructorItem(info, isDefault, fields, superConstructor, parent, substitutionOffset);
        }
    }
}
