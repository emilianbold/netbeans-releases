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
package org.netbeans.modules.cnd.api.codemodel.query;

import org.netbeans.modules.cnd.api.codemodel.CMCursor;
import org.netbeans.modules.cnd.api.codemodel.CMCursorKind;
import org.netbeans.modules.cnd.api.codemodel.CMFile;
import org.netbeans.modules.cnd.api.codemodel.visit.CMDeclarationContext;
import org.netbeans.modules.cnd.api.codemodel.visit.CMEntity;
import org.netbeans.modules.cnd.api.codemodel.visit.CMObject;
import org.netbeans.modules.cnd.api.codemodel.visit.CMReference;

/**
 *
 * @author Vladimir Kvashin
 */
public class CMKindUtilities {

    private CMKindUtilities() {}

    public static boolean isEntity(CMObject obj) {
        return obj instanceof CMEntity;
    }

    public static boolean isFile(CMObject obj) {
        return obj instanceof CMFile;
    }
    
    public static boolean isFile(CMCursor obj) {
        return obj.getKind().isTranslationUnit();
    }

    public static boolean isreference(CMObject obj) {
        return obj instanceof CMReference;
    }

    public static boolean isDeclarationContext(CMObject obj) {
        return obj instanceof CMDeclarationContext;
    }

    public static boolean isFunctionDefinition(CMEntity obj) {
        if (isFunction(obj)) {
            new UnsupportedOperationException("Can't distingwish function definition").printStackTrace(); //NOI18N
            return true;
        }
        return false;
    }

    public static boolean isFunctionDefinition(CMCursor obj) {
        if (isFunction(obj)) {
            return obj.isDefinition();
        }
        return false;
    }

    public static boolean isLocalVariable(CMEntity refObject) {
        return refObject != null && refObject.getKind() == CMEntity.Kind.Variable;
    }

    public static boolean isLocalVariable(CMCursor refObject) {
        if (refObject != null) {
            switch (refObject.getKind()) {
                case VarDecl:
                case VariableRef:
                    return true;
            }
        }
        return false;
    }
    
    public static boolean isFileLocalVariable(CMCursor obj) {
        if (isFunction(obj)) {
            return obj.getSemanticParent().getLocation().getFile().equals(obj.getLexicalParent().getLocation().getFile());
//            return isFile(((CsmFunction)obj).getScope());
        } else {
            return false;
        }
    }
    
    public static boolean isGlobalVariable(CMCursor obj) {
        if (isVariable(obj)) {
            // global variable has scope - namespace
            return isNamespace(obj.getSemanticParent().getReferencedEntityCursor());
        } else {
            return false;
        }
    }

    public static boolean isInclude(CMCursor refObject) {
        if (refObject != null) {
            switch (refObject.getKind()) {
                case InclusionDirective:
                    return true;
            }
        }
        return false;
    }

    public static boolean isMethod(CMEntity refObject) {
        if (refObject != null) {
            switch (refObject.getKind()) {
                case CXXConstructor:
                case CXXDestructor:
                case CXXInstanceMethod:
                case CXXStaticMethod:
                case ObjCClassMethod:
                case ObjCInstanceMethod:
                    return true;
            }
        }
        return false;
    }

    public static boolean isMethod(CMCursor refObject) {
        if (refObject != null) {
            switch (refObject.getKind()) {
                case FunctionDecl:
                case FunctionTemplate:
                    return isClass(refObject.getSemanticParent());
            }
        }
        return false;
    }
    
    public static boolean isConstructor(CMCursor refObject) {
        return CMCursorKind.Constructor.equals(refObject.getKind());
    }
    
    public static boolean isDestructor(CMCursor refObject) {
        return CMCursorKind.Destructor.equals(refObject.getKind());
    }
    
    public static boolean isClassMember(CMCursor refObject) {
        return CMKindUtilities.isMethod(refObject) || CMKindUtilities.isField(refObject);
    }

    public static boolean isFunction(CMEntity refObject) {
        if (refObject != null) {
            switch (refObject.getKind()) {
                case CXXConstructor:
                case CXXConversionFunction:
                case CXXDestructor:
                case CXXInstanceMethod:
                case CXXStaticMethod:
                case Function:
                case ObjCClassMethod:
                case ObjCInstanceMethod:
                    return true;
            }
        }
        return false;
    }

    public static boolean isFunction(CMCursor refObject) {
        if (refObject != null) {
            switch (refObject.getKind()) {
                case FunctionDecl:
                case FunctionTemplate:
                    return true;
            }
        }
        return false;
    }
    
    public static boolean isGlobalFunction(CMCursor obj) {
        if (isFunction(obj)) {
            return !isClassMember(obj.getReferencedEntityCursor());
        } else {
            return false;
        }
    }
    
    public static boolean isFileLocalFunction(CMCursor obj) {
        if (isFunction(obj)) {
            return obj.getSemanticParent().getLocation().getFile().equals(obj.getLexicalParent().getLocation().getFile());
//            return isFile(((CsmFunction)obj).getScope());
        } else {
            return false;
        }
    }
    
    public static boolean isOperator(CMCursor obj) {
        if (isFunction(obj)) {
            switch (obj.getKind()) {
                case UnaryOperator:
                case BinaryOperator:
                case CompoundAssignOperator:
                case ConditionalOperator:
                    return true;
            }
        }
        return false;
    } 

    public static boolean isTypedef(CMEntity refObject) {
        return refObject != null && refObject.getKind() == CMEntity.Kind.Typedef;
    }

    public static boolean isTypedef(CMCursor refObject) {
        if (refObject != null) {
            switch (refObject.getKind()) {
                case TypedefDecl:
                    return true;
            }
        }
        return false;
    }

    public static boolean isEnum(CMEntity refObject) {
        return refObject != null && refObject.getKind() == CMEntity.Kind.Enum;
    }

    public static boolean isEnum(CMCursor refObject) {
        if (refObject != null) {
            switch (refObject.getKind()) {
                case EnumDecl:
                    return true;
            }
        }
        return false;
    }

    public static boolean isEnumerator(CMEntity refObject) {
        return refObject != null && refObject.getKind() == CMEntity.Kind.EnumConstant;
    }

    public static boolean isEnumerator(CMCursor refObject) {
        if (refObject != null) {
            switch (refObject.getKind()) {
                case EnumConstantDecl:
                    return true;
            }
        }
        return false;
    }

    public static boolean isField(CMEntity refObject) {
        return refObject != null && refObject.getKind() == CMEntity.Kind.Field;
    }

    public static boolean isField(CMCursor refObject) {
        if (refObject != null) {
            switch (refObject.getKind()) {
                case FieldDecl:
                    return true;
            }
        }
        return false;
    }

    public static boolean isVariable(CMEntity refObject) {
        if (refObject != null) {
            switch (refObject.getKind()) {
                case CXXStaticVariable:
                case Field:
                case ObjCIvar:
                case Variable:
                    return true;
            }
        }
        return false;
    }

    public static boolean isVariable(CMCursor refObject) {
        if (refObject != null) {
            switch (refObject.getKind()) {
                case VarDecl:
                case VariableRef:
                    return true;
            }
        }
        return false;
    }
    
    public static boolean isStatic(CMCursor refObject) {
        if (refObject != null) {
            //TODO: implement
        }
        return false;
    }
    
    public static boolean isExtern(CMCursor refObject) {
        if (refObject != null) {
            //TODO: implement
        }
        return false;
    }

    public static boolean isNamespace(CMEntity refObject) {
        if (refObject != null) {
            switch (refObject.getKind()) {
                case CXXNamespace:
                case CXXNamespaceAlias:
                    return true;
            }
        }
        return false;
    }

    public static boolean isNamespace(CMCursor refObject) {
        if (refObject != null) {
            switch (refObject.getKind()) {
                case Namespace:
                    return true;
            }
        }
        return false;
    }

    public static boolean isMacro(CMCursor refObject) {
        if (refObject != null) {
            switch (refObject.getKind()) {
                //TODO: implement
            }
        }
        return false;
    }

    public static boolean isQualified(CMEntity refObject) {
        if (refObject != null) {
            switch (refObject.getKind()) {
                //TODO: implement
            }
        }
        return false;
    }

    public static boolean isClass(CMEntity refObject) {
        if (refObject != null) {
            switch (refObject.getKind()) {
                case CXXClass:
                case CXXInterface:
                case ObjCClass:
                case Struct:
                case Union:
                    return true;
            }
        }
        return false;
    }

    public static boolean isClass(CMCursor refObject) {
        if (refObject != null) {
            switch (refObject.getKind()) {
                case ClassDecl:
                case ClassTemplate:
                case ClassTemplatePartialSpecialization:
                case StructDecl:
                    return true;
            }
        }
        return false;
    }
    
    public static boolean isForwardClass(CMCursor refObject) {
        if (isClass(refObject)) {
            //TODO: implement
        }
        return false;
    }
    
    public static boolean isTemplate(CMCursor refObject) {
        if (refObject != null) {
            switch (refObject.getKind()) {
                case FunctionTemplate:
                case ClassTemplate:
//                case ClassTemplatePartialSpecialization:    ??
//                case TemplateRef:   ??
                    return true;
            }
        }
        return false;
    }
    
    public static boolean isTemplate(CMEntity refObject) {
        if (refObject != null) {
            switch (refObject.getTemplateKind()) {
                case Template:
//                case TemplatePartialSpecialization:   ??
//                case TemplateSpecialization:  ??
                    return true;
            }
        }
        return false;
    }

    public static boolean isUnion(CMEntity refObject) {
        return refObject != null && refObject.getKind() == CMEntity.Kind.Union;
    }

    public static boolean isUnion(CMCursor refObject) {
        switch (refObject.getKind()) {
            case UnionDecl:
                return true;
        }
        return false;
    }

    public static boolean isStruct(CMEntity refObject) {
        return refObject != null && refObject.getKind() == CMEntity.Kind.Struct;
    }

    public static boolean isStruct(CMCursor refObject) {
        switch (refObject.getKind()) {
            case StructDecl:
                return true;
        }
        return false;
    }
    
    public static boolean isUsing(CMEntity object) {
        // TODO
        return false;
    }
    
    public static boolean isUsing(CMCursor object) {
        switch (object.getKind()) {
            case UsingDirective:
            case UsingDeclaration:
                return true;
        }
        return false;
    }

    public static boolean isNamespaceAlias(CMEntity object) {
        return object != null && object.getKind() == CMEntity.Kind.CXXNamespaceAlias;
}

    public static boolean isNamespaceAlias(CMCursor object) {
        switch (object.getKind()) {
            case NamespaceAlias:
                return true;
        }
        return false;
    }
}
