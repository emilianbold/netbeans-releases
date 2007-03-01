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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.java.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import org.netbeans.api.java.source.CompilationInfo;

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
                if (te.getTypeParameters().isEmpty())
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
                typeMirrors = new ArrayList<TypeMirrorHandle<? extends TypeMirror>>(targs.size());
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
                List<TypeMirror> resolvedTypeArguments = new ArrayList<TypeMirror>();
                for (TypeMirrorHandle t : typeMirrors) {
                    TypeMirror resolved = t.resolve(info);
                    if (resolved == null)
                        return null;
                    resolvedTypeArguments.add(resolved);
                }
                return (T)info.getTypes().getDeclaredType(te, resolvedTypeArguments.toArray(new TypeMirror[resolvedTypeArguments.size()]));
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
}
