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
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
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
 * Currently not all the {@link TypeMirror}s are supported by handle, the supported
 * elements are: primitive types, {@link TypeKind#DECLARED}, {@link TypeKind#ARRAY}.
 *
 * @author Jan Lahoda
 */
public final class TypeMirrorHandle<T extends TypeMirror> {

    private TypeKind kind;
    private ElementHandle<TypeElement> element;
    private List<TypeMirrorHandle> typeArguments;
    private TypeMirrorHandle elementType;
    
    private TypeMirrorHandle(TypeKind kind, ElementHandle<TypeElement> element, java.util.List<TypeMirrorHandle> typeArguments) {
        this.kind = kind;
        this.element = element;
        this.typeArguments = typeArguments;
    }
    
    private TypeMirrorHandle(TypeKind kind) {
        this.kind = kind;
    }
    
    private TypeMirrorHandle(TypeKind kind, TypeMirrorHandle elementType) {
        this.kind = kind;
        this.elementType = elementType;
    }
    
    /**
     * Factory method for creating {@link TypeMirrorHandle}.
     * @param element for which the {@link TypeMirrorHandle} should be created.
     * Not all {@link TypeKind}s are currently supported, supported {@link TypeKind}s
     * are: primitive types, {@link TypeKind#DECLARED}, {@link ElementKind#ARRAY}.
     * @return a new {@link TypeMirrorHandle}
     * @throws {@link IllegalArgumentException} if the element is of not supported
     * {@link TypeKind}.
     */
    public static <T extends TypeMirror> TypeMirrorHandle<T> create(T tm) {
        if (tm.getKind().isPrimitive()) {
            return create(tm.getKind());
        }
        
        switch (tm.getKind()) {
            case DECLARED:
                return create((DeclaredType) tm);
            case ARRAY:
                return new TypeMirrorHandle(TypeKind.ARRAY, TypeMirrorHandle.create(((ArrayType) tm).getComponentType()));
            default:
                throw new IllegalArgumentException("Currently unsupported TypeKind: " + tm.getKind());
        }
    }
    
    private static TypeMirrorHandle create(TypeKind primitive) {
        return new TypeMirrorHandle(primitive);
    }
    
    private static TypeMirrorHandle create(DeclaredType dt) {
        TypeElement te= (TypeElement) dt.asElement();
        List<TypeMirrorHandle> typeArguments = new ArrayList<TypeMirrorHandle>();
        
        for (TypeMirror tm : dt.getTypeArguments()) {
            typeArguments.add(create(tm));
        }
        
        return new TypeMirrorHandle(TypeKind.DECLARED, ElementHandle.create(te), typeArguments);
    }
    
    /**
     * Resolves an {@link TypeMirror} from the {@link TypeMirrorHandle}.
     * @param compilationInfo representing the {@link javax.tools.CompilationTask}
     * in which the {@link TypeMirror} should be resolved.
     * @return resolved subclass of {@link TypeMirror} or null if the type cannot be
     * resolved in this {@link javax.tools.CompilationTask}.
     */
    public T resolve(CompilationInfo info) {
        if (kind.isPrimitive()) {
            return (T) info.getTypes().getPrimitiveType(kind);
        }
        
        switch (kind) {
            case DECLARED:
                List<TypeMirror> resolvedTypeArguments = new ArrayList<TypeMirror>();
                
                for (TypeMirrorHandle t : typeArguments) {
                    resolvedTypeArguments.add(t.resolve(info));
                }
                
                TypeElement te = element.resolve(info);
                
                if (te == null)
                    return null;
                
                return (T) info.getTypes().getDeclaredType(te, resolvedTypeArguments.toArray(new TypeMirror[0]));
            case ARRAY:
                return (T) info.getTypes().getArrayType(elementType.resolve(info));
        }
        
        throw new IllegalStateException("Internal error: unknown TypeHandle kind: " + kind);
    }
    
}
