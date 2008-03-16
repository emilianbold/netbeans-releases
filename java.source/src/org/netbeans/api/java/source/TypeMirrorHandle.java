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
package org.netbeans.api.java.source;

import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

/**
 * Represents a handle for {@link TypeMirror} which can be kept and later resolved
 * by another javac. The Javac {@link TypeMirror}s are valid only in the single
 * {@link javax.tools.CompilationTask} or single run of the
 * {@link org.netbeans.api.java.source.CancellableTask}. If the client needs to
 * keep a reference to the {@link TypeMirror} and use it in the other CancellableTask
 * he has to serialize it into the {@link TypeMirrorHandle}.
 * <div class="nonnormative"> 
 * <p>
 ** Typical usage of TypeMirrorHandle is:
 * </p>
 * <pre>
 * final TypeMirrorHandle[] typeMirrorHandle = new TypeMirrorHandle[1];
 * javaSource.runCompileControlTask(new CancellableTask<CompilationController>() {
 *     public void run(CompilationController compilationController) {
 *         parameter.toPhase(Phase.RESOLVED);
 *         CompilationUnitTree cu = compilationController.getTree ();
 *         TypeMirror type = getInterestingType(cu);
 *         typeMirrorHandle[0] = TypeMirrorHandle.create (type);
 *    }
 * },priority);
 *
 * otherJavaSource.runCompileControlTask(new CancellableTask<CompilationController>() {
 *     public void run(CompilationController compilationController) {
 *         parameter.toPhase(Phase.RESOLVED);
 *         TypeMirror type = typeMirrorHandle[0].resolve (compilationController);
 *         ....
 *    }
 * },priority);
 * </pre>
 * </div>
 * Currently, not all the {@link TypeMirror} {@link TypeKind kinds} are supported by handle.
 * The unsupported {@link TykeKind kinds} are: {@link TypeKind#EXECUTABLE}, {@link TypeKind#OTHER},
 * and {@link TypeKind#PACKAGE}.
 *
 * @author Jan Lahoda, Dusan Balek
 */
public final class TypeMirrorHandle<T extends TypeMirror> {

    private TypeKind kind;
    private ElementHandle<? extends Element> element;
    private List<TypeMirrorHandle<? extends TypeMirror>> typeMirrors;
    
    private TypeMirrorHandle(TypeKind kind, ElementHandle<? extends Element> element, List<TypeMirrorHandle<? extends TypeMirror>> typeArguments) {
        this.kind = kind;
        this.element = element;
        this.typeMirrors = typeArguments;
    }

    /**
     * Factory method for creating {@link TypeMirrorHandle}.
     * @param {@link TypeMirror} for which the {@link TypeMirrorHandle} should be created.
     * Not all the {@link TypeMirror} {@link TypeKind kinds} are currently supported.
     * The unsupported {@link TykeKind kinds} are: {@link TypeKind#EXECUTABLE}, {@link TypeKind#OTHER},
     * and {@link TypeKind#PACKAGE}.
     * @return a new {@link TypeMirrorHandle}
     * @throws {@link IllegalArgumentException} if the {@link TypeMirror} is of an unsupported
     * {@link TypeKind}.
     */
    public static <T extends TypeMirror> TypeMirrorHandle<T> create(T tm) {
        TypeKind kind = tm.getKind();
        ElementHandle<? extends Element> element = null;
        List<TypeMirrorHandle<? extends TypeMirror>> typeMirrors = null;
        switch (kind) {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case NONE:
            case NULL:
            case SHORT:
            case VOID:
                break;
            case DECLARED:
                DeclaredType dt = (DeclaredType)tm;
                TypeElement te = (TypeElement)dt.asElement();
                element = ElementHandle.create(te);
                Element encl = te.getEnclosingElement();
                boolean genericOuter = (encl.getKind().isClass() || encl.getKind().isInterface()) && !((TypeElement)encl).getTypeParameters().isEmpty() && !te.getModifiers().contains(Modifier.STATIC);
                if (te.getTypeParameters().isEmpty() && !genericOuter)
                    break;
                List<? extends TypeMirror> targs = dt.getTypeArguments();
                if (!targs.isEmpty()) {
                    TypeMirror targ = targs.get(0);
                    if (targ.getKind() == TypeKind.TYPEVAR) {
                        TypeParameterElement tpe = (TypeParameterElement)((TypeVariable)targ).asElement();
                        if (tpe.getGenericElement() == dt.asElement())
                            break;
                    }
                }
                if (genericOuter) {
                    typeMirrors = new ArrayList<TypeMirrorHandle<? extends TypeMirror>>(targs.size() + 1);
                    typeMirrors.add(create(dt.getEnclosingType()));
                } else {
                    typeMirrors = new ArrayList<TypeMirrorHandle<? extends TypeMirror>>(targs.size());
                }
                for (TypeMirror ta : targs)
                    typeMirrors.add(create(ta));
                break;
            case ARRAY:
                typeMirrors = Collections.<TypeMirrorHandle<? extends TypeMirror>>singletonList(TypeMirrorHandle.create(((ArrayType)tm).getComponentType()));
                break;
            case TYPEVAR:
                TypeVariable tv = (TypeVariable)tm;
                element = ElementHandle.create(tv.asElement());
                typeMirrors = new ArrayList<TypeMirrorHandle<? extends TypeMirror>>();
                typeMirrors.add(tv.getLowerBound() != null ? TypeMirrorHandle.create(tv.getLowerBound()) : null);
                typeMirrors.add(tv.getUpperBound() != null ? TypeMirrorHandle.create(tv.getUpperBound()) : null);
                break;
            case WILDCARD:
                WildcardType wt = (WildcardType)tm;
                typeMirrors = new ArrayList<TypeMirrorHandle<? extends TypeMirror>>();
                typeMirrors.add(wt.getExtendsBound() != null ? TypeMirrorHandle.create(wt.getExtendsBound()) : null);
                typeMirrors.add(wt.getSuperBound() != null ? TypeMirrorHandle.create(wt.getSuperBound()) : null);
                break;
            case ERROR:
                ErrorType et = (ErrorType)tm;
                element = ElementHandle.create(et.asElement());
                break;
            default:
                throw new IllegalArgumentException("Currently unsupported TypeKind: " + tm.getKind());
        }
        return new TypeMirrorHandle(kind, element, typeMirrors);
    }
    
    /**
     * Resolves an {@link TypeMirror} from the {@link TypeMirrorHandle}.
     * @param {@link CompilationInfo} representing the {@link javax.tools.CompilationTask}
     * in which the {@link TypeMirror} should be resolved.
     * @return resolved subclass of {@link TypeMirror} or null if the type cannot be
     * resolved in this {@link javax.tools.CompilationTask}.
     */
    public T resolve(CompilationInfo info) {
        switch (kind) {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case SHORT:
                return (T)info.getTypes().getPrimitiveType(kind);
            case NONE:
            case VOID:
                return (T)info.getTypes().getNoType(kind);
            case NULL:
                return (T)info.getTypes().getNullType();
            case DECLARED:
                TypeElement te = (TypeElement)element.resolve(info);
                if (te == null)
                    return null;
                if (typeMirrors == null)
                    return (T)te.asType();
                Iterator<TypeMirrorHandle<? extends TypeMirror>> it = typeMirrors.iterator();
                Element encl = te.getEnclosingElement();
                boolean genericOuter = (encl.getKind().isClass() || encl.getKind().isInterface()) && !((TypeElement)encl).getTypeParameters().isEmpty() && !te.getModifiers().contains(Modifier.STATIC);
                TypeMirror outer = null;
                if (genericOuter) {
                    outer = it.hasNext() ? it.next().resolve(info) : null;
                    if (outer == null || outer.getKind() != TypeKind.DECLARED)
                        return null;
                }
                List<TypeMirror> resolvedTypeArguments = new ArrayList<TypeMirror>();
                while(it.hasNext()) {
                    TypeMirror resolved = it.next().resolve(info);
                    if (resolved == null)
                        return null;
                    resolvedTypeArguments.add(resolved);
                }
                return outer != null ? (T)info.getTypes().getDeclaredType((DeclaredType)outer, te, resolvedTypeArguments.toArray(new TypeMirror[resolvedTypeArguments.size()]))
                        : (T)info.getTypes().getDeclaredType(te, resolvedTypeArguments.toArray(new TypeMirror[resolvedTypeArguments.size()]));
            case ARRAY:
                TypeMirror resolved = typeMirrors.get(0).resolve(info);
                if (resolved == null)
                    return null;
                return (T)info.getTypes().getArrayType(resolved);
            case TYPEVAR:
                Element e = element.resolve(info);
                if (!(e instanceof com.sun.tools.javac.code.Symbol.TypeSymbol))
                    return null;
                TypeMirrorHandle<? extends TypeMirror> lBound = typeMirrors.get(0);
                TypeMirror lowerBound = lBound != null ? lBound.resolve(info) : null;
                TypeMirrorHandle<? extends TypeMirror> uBound = typeMirrors.get(1);
                TypeMirror upperBound = uBound != null ? uBound.resolve(info) : null;
                return (T)new com.sun.tools.javac.code.Type.TypeVar((com.sun.tools.javac.code.Symbol.TypeSymbol)e,
                        (com.sun.tools.javac.code.Type)lowerBound, (com.sun.tools.javac.code.Type)upperBound);
            case WILDCARD:
                TypeMirrorHandle<? extends TypeMirror> eBound = typeMirrors.get(0);
                TypeMirror extendsBound = eBound != null ? eBound.resolve(info) : null;
                TypeMirrorHandle<? extends TypeMirror> sBound = typeMirrors.get(1);
                TypeMirror superBound = sBound != null ? sBound.resolve(info) : null;
                return (T)info.getTypes().getWildcardType(extendsBound, superBound);
            case ERROR:
                e = element.resolve(info);
                if (e == null) {
                    String[] signatures = element.getSignature();
                    assert signatures.length == 1;
                    Context context = info.impl.getJavacTask().getContext();
                    return (T)new com.sun.tools.javac.code.Type.ErrorType(Name.Table.instance(context).fromString(signatures[0]), Symtab.instance(context).rootPackage); 
                }
                if (!(e instanceof com.sun.tools.javac.code.Symbol.ClassSymbol))
                    return null;
                return (T)new com.sun.tools.javac.code.Type.ErrorType((com.sun.tools.javac.code.Symbol.ClassSymbol)e);
            default:
                throw new IllegalStateException("Internal error: unknown TypeHandle kind: " + kind);
        }
    }
    
    /**
     * Returns the {@link TypeKind} of this handle,
     * it is the kind of the {@link TypeMirror} from which the handle
     * was created.
     * @return {@link TypeKind}
     */
    public TypeKind getKind () {
        return this.kind;
    }
    
    ElementHandle<? extends Element> getElementHandle() {
        return element;
    }
}
