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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.java.source;

import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.jvm.Target;
import com.sun.tools.javac.model.JavacElements;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.usages.ClassFileUtil;

/**
 * Represents a handle for {@link Element} which can be kept and later resolved
 * by another javac. The javac {@link Element}s are valid only in a single
 * {@link javax.tools.CompilationTask} or a single run of a
 * {@link CancellableTask}. A client needing to
 * keep a reference to an {@link Element} and use it in another {@link CancellableTask}
 * must serialize it into an {@link ElementHandle}.
 * Currently not all {@link Element}s can be serialized. See {@link #create} for details.
 * <div class="nonnormative">
 * <p>
 * Typical usage of {@link ElementHandle} is as follows:
 * </p>
 * <pre>
 * final ElementHandle[] elementHandle = new ElementHandle[1];
 * javaSource.runUserActionTask(new CancellableTask&lt;CompilationController>() {
 *     public void run(CompilationController compilationController) {
 *         compilationController.toPhase(Phase.RESOLVED);
 *         CompilationUnitTree cu = compilationController.getTree();
 *         List&lt;? extends Tree> types = getTypeDecls(cu);
 *         Tree tree = getInterestingElementTree(types);
 *         Element element = compilationController.getElement(tree);
 *         elementHandle[0] = ElementHandle.create(element);
 *    }
 * }, true);
 *
 * otherJavaSource.runUserActionTask(new CancellableTask&lt;CompilationController>() {
 *     public void run(CompilationController compilationController) {
 *         compilationController.toPhase(Phase.RESOLVED);
 *         Element element = elementHandle[0].resolve(compilationController);
 *         // ....
 *    }
 * }, true);
 * </pre>
 * </div>
 * @author Tomas Zezula
 */
public final class ElementHandle<T extends Element> {
    
    static {
        ElementHandleAccessor.INSTANCE = new ElementHandleAccessorImpl ();
    }
    
    private ElementKind kind;
    private String[] signatures;
        
       
    private ElementHandle(final ElementKind kind, String[] signatures) {
        assert kind != null;
        assert signatures != null;
        this.kind = kind;
        this.signatures = signatures;
    }
    
    
    /**
     * Resolves an {@link Element} from the {@link ElementHandle}.
     * @param compilationInfo representing the {@link javax.tools.CompilationTask}
     * in which the {@link Element} should be resolved.
     * @return resolved subclass of {@link Element} or null if the elment does not exist on
     * the classpath/sourcepath of {@link javax.tools.CompilationTask}.
     */
    @SuppressWarnings ("unchecked")     // NOI18N
    public T resolve (final CompilationInfo compilationInfo) {
        assert compilationInfo != null;        
        switch (this.kind) {
            case PACKAGE:
                assert signatures.length == 1;
                return (T) compilationInfo.getElements().getPackageElement(signatures[0]);
            case CLASS:
            case INTERFACE:
            case ENUM:
            case ANNOTATION_TYPE:
            case OTHER:
                assert signatures.length == 1;
                return (T) getTypeElementByBinaryName (signatures[0], compilationInfo);
            case METHOD:
            case CONSTRUCTOR:
            case INSTANCE_INIT:
            case STATIC_INIT:
            {
                assert signatures.length == 3;
                final TypeElement type = getTypeElementByBinaryName (signatures[0], compilationInfo);
                if (type != null) {
                   final List<? extends Element> members = type.getEnclosedElements();
                   for (Element member : members) {
                       if (this.kind == member.getKind()) {
                           String[] desc = ClassFileUtil.createExecutableDescriptor((ExecutableElement)member);
                           assert desc.length == 3;
                           if (this.signatures[1].equals(desc[1]) && this.signatures[2].equals(desc[2])) {
                               return (T) member;
                           }
                       }
                   }
                }
                break;
            }
            case FIELD:
            case ENUM_CONSTANT:
            {
                assert signatures.length == 3;
                final TypeElement type = getTypeElementByBinaryName (signatures[0], compilationInfo);
                if (type != null) {
                    final List<? extends Element> members = type.getEnclosedElements();
                    for (Element member : members) {
                        if (this.kind == member.getKind()) {
                            String[] desc = ClassFileUtil.createFieldDescriptor((VariableElement)member);
                            assert desc.length == 3;
                            if (this.signatures[1].equals(desc[1]) && this.signatures[2].equals(desc[2])) {
                                return (T) member;
                            }
                        }
                    }
                }
                break;
            }
            case TYPE_PARAMETER:
            {
                if (signatures.length == 2) {
                     TypeElement type = getTypeElementByBinaryName (signatures[0], compilationInfo);
                     if (type != null) {
                         List<? extends TypeParameterElement> tpes = type.getTypeParameters();
                         for (TypeParameterElement tpe : tpes) {
                             if (tpe.getSimpleName().contentEquals(signatures[1])) {
                                 return (T)tpe;
                             }
                         }
                     }
                }
                else if (signatures.length == 4) {
                    final TypeElement type = getTypeElementByBinaryName (signatures[0], compilationInfo);
                    if (type != null) {
                        final List<? extends Element> members = type.getEnclosedElements();
                        for (Element member : members) {
                            if (member.getKind() == ElementKind.METHOD || member.getKind() == ElementKind.CONSTRUCTOR) {
                                String[] desc = ClassFileUtil.createExecutableDescriptor((ExecutableElement)member);
                                assert desc.length == 3;
                                if (this.signatures[1].equals(desc[1]) && this.signatures[2].equals(desc[2])) {
                                    assert member instanceof ExecutableElement;
                                    List<? extends TypeParameterElement> tpes =((ExecutableElement)member).getTypeParameters();
                                    for (TypeParameterElement tpe : tpes) {
                                        if (tpe.getSimpleName().contentEquals(signatures[3])) {
                                            return (T) tpe;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else {
                    throw new IllegalStateException ();
                }
                break;
            }
            default:
                throw new IllegalStateException ();
        }
        return null;
    }
    
    
    /**
     * Tests if the handle has the same signature as the parameter.
     * The handles with the same signatures are resolved into the same
     * element in the same {@link javax.tools.JavaCompiler} task, but may be resolved into
     * the different {@link Element}s in the different {@link javax.tools.JavaCompiler} tasks.
     * @param handle to be checked
     * @return true if the handles resolve into the same {@link Element}s
     * in the same {@link javax.tools.JavaCompiler} task.
     */
    public boolean signatureEquals (final ElementHandle<? extends Element> handle) {
         if (this.kind != handle.kind || this.signatures.length != handle.signatures.length) {
             return false;
         }
         for (int i=0; i<signatures.length; i++) {
             if (!signatures[i].equals(handle.signatures[i])) {
                 return false;
             }
         }
         return true;
    }
    
    
    /**
     * Returns a binary name of the {@link TypeElement} represented by this
     * {@link ElementHandle}. When the {@link ElementHandle} doesn't represent
     * a {@link TypeElement} it throws a {@link IllegalStateException}
     * @return the qualified name
     * @throws an {@link IllegalStateException} when this {@link ElementHandle} 
     * isn't creatred for the {@link TypeElement}.
     */
    public String getBinaryName () throws IllegalStateException {
        if ((this.kind.isClass() && !isArray(signatures[0])) || this.kind.isInterface()) {
            return this.signatures[0];
        }
        else {
            throw new IllegalStateException ();
        }
    }
    
    
    /**
     * Returns a qualified name of the {@link TypeElement} represented by this
     * {@link ElementHandle}. When the {@link ElementHandle} doesn't represent
     * a {@link TypeElement} it throws a {@link IllegalStateException}
     * @return the qualified name
     * @throws an {@link IllegalStateException} when this {@link ElementHandle} 
     * isn't creatred for the {@link TypeElement}.
     */
    public String getQualifiedName () throws IllegalStateException {
        if ((this.kind.isClass() && !isArray(signatures[0])) || this.kind.isInterface()) {
            return this.signatures[0].replace (Target.DEFAULT.syntheticNameChar(),'.');    //NOI18N
        }
        else {
            throw new IllegalStateException ();
        }
    }
    
    
    /**
     * Tests if the handle has this same signature as the parameter.
     * The handles has the same signatures if it is resolved into the same
     * element in the same {@link javax.tools.JavaCompiler} task, but may be resolved into
     * the different {@link Element} in the different {@link javax.tools.JavaCompiler} task.
     * @param element to be checked
     * @return true if this handle resolves into the same {@link Element}
     * in the same {@link javax.tools.JavaCompiler} task.
     */
    public boolean signatureEquals (final T element) {
        if (!element.getKind().equals(getKind())) {
            return false;
        }
        final ElementHandle<T> handle = create (element);
        return signatureEquals (handle);
    }
    
    /**
     * Returns the {@link ElementKind} of this element handle,
     * it is the kind of the {@link Element} from which the handle
     * was created.
     * @return {@link ElementKind}
     *
     */
    public ElementKind getKind () {
        return this.kind;
    }
    
    
    /**
     * Factory method for creating {@link ElementHandle}.
     * @param element for which the {@link ElementHandle} should be created. Permitted
     * {@link ElementKind}s
     * are: {@link ElementKind#PACKAGE}, {@link ElementKind#CLASS},
     * {@link ElementKind#INTERFACE}, {@link ElementKind#ENUM}, {@link ElementKind#ANNOTATION_TYPE}, {@link ElementKind#METHOD},
     * {@link ElementKind#CONSTRUCTOR}, {@link ElementKind#INSTANCE_INIT}, {@link ElementKind#STATIC_INIT},
     * {@link ElementKind#FIELD}, and {@link ElementKind#ENUM_CONSTANT}.
     * @return a new {@link ElementHandle}
     * @throws IllegalArgumentException if the element is of an unsupported {@link ElementKind}
     */
    public static<T extends Element> ElementHandle<T> create (final T element) throws IllegalArgumentException {
        assert element != null;
        ElementKind kind = element.getKind();
        String[] signatures;
        switch (kind) {
            case PACKAGE:
                assert element instanceof PackageElement;
                signatures = new String[]{((PackageElement)element).getQualifiedName().toString()};
                break;
            case CLASS:
            case INTERFACE:
            case ENUM:
            case ANNOTATION_TYPE:
                assert element instanceof TypeElement;
                signatures = new String[] {ClassFileUtil.encodeClassNameOrArray((TypeElement)element)};
                break;
            case METHOD:
            case CONSTRUCTOR:                
            case INSTANCE_INIT:
            case STATIC_INIT:
                assert element instanceof ExecutableElement;
                signatures = ClassFileUtil.createExecutableDescriptor((ExecutableElement)element);
                break;
            case FIELD:
            case ENUM_CONSTANT:
                assert element instanceof VariableElement;
                signatures = ClassFileUtil.createFieldDescriptor((VariableElement)element);
                break;
            case TYPE_PARAMETER:
                assert element instanceof TypeParameterElement;
                TypeParameterElement tpe = (TypeParameterElement) element;
                Element ge = tpe.getGenericElement();
                ElementKind gek = ge.getKind();
                if (gek.isClass() || gek.isInterface()) {
                    assert ge instanceof TypeElement;
                    signatures = new String[2];
                    signatures[0] = ClassFileUtil.encodeClassNameOrArray((TypeElement)ge);
                    signatures[1] = tpe.getSimpleName().toString();
                }
                else if (gek == ElementKind.METHOD || gek == ElementKind.CONSTRUCTOR) {
                    assert ge instanceof ExecutableElement;
                    String[] _sigs = ClassFileUtil.createExecutableDescriptor((ExecutableElement)ge);
                    signatures = new String[_sigs.length + 1];
                    System.arraycopy(_sigs, 0, signatures, 0, _sigs.length);
                    signatures[_sigs.length] = tpe.getSimpleName().toString();
                }
                else {
                    throw new IllegalArgumentException(gek.toString());
                }
                break;
            default:
                throw new IllegalArgumentException(kind.toString());
        }
        return new ElementHandle<T> (kind, signatures);
    }
    
    
    public @Override String toString () {
        final StringBuilder result = new StringBuilder ();
        result.append (this.getClass().getSimpleName());
        result.append ('[');                                // NOI18N
        result.append ("kind=" +this.kind.toString());      // NOI18N
        result.append ("; sigs=");                          // NOI18N
        for (String sig : this.signatures) {
            result.append (sig);
            result.append (' ');                            // NOI18N
        }
        result.append (']');                                // NOI18N
        return result.toString();
    }
    
    
    /**@inheritDoc*/
    @Override
    public int hashCode () {
        return signatures.length == 0 || signatures[0] == null ? 0 : signatures[0].hashCode();
    }
    
    /**@inheritDoc*/
    @Override
    public boolean equals (Object other) {
        if (other instanceof ElementHandle) {
            return signatureEquals((ElementHandle)other);
        }
        return false;
    }
    
    
    /**
     * Returns the element signature.
     * Package private, used by ClassIndex.
     */
    String[] getSignature () {
        return this.signatures;
    }
        
    
    private static class ElementHandleAccessorImpl extends ElementHandleAccessor {
        
        public ElementHandle create(ElementKind kind, String... descriptors) {
            assert kind != null;
            assert descriptors != null;
            switch (kind) {
                case PACKAGE:
                    if (descriptors.length != 1) {
                        throw new IllegalArgumentException ();
                    }
                    return new ElementHandle<PackageElement> (kind, descriptors);
                case CLASS:
                case INTERFACE:
                case ENUM:
                case ANNOTATION_TYPE:
                case OTHER:
                    if (descriptors.length != 1) {
                        throw new IllegalArgumentException ();
                    }
                    return new ElementHandle<TypeElement> (kind, descriptors);
                case METHOD:
                case CONSTRUCTOR:                
                case INSTANCE_INIT:
                case STATIC_INIT:
                    if (descriptors.length != 3) {
                        throw new IllegalArgumentException ();
                    }
                    return new ElementHandle<ExecutableElement> (kind, descriptors);
                case FIELD:
                case ENUM_CONSTANT:
                    if (descriptors.length != 3) {
                        throw new IllegalArgumentException ();
                    }
                    return new ElementHandle<VariableElement> (kind, descriptors);
                default:
                    throw new IllegalArgumentException ();
            }            
        }        
    }
    
    private static TypeElement getTypeElementByBinaryName (final String signature, final CompilationInfo ci) {
        if (isArray(signature)) {
            return Symtab.instance(ci.getJavacTask().getContext()).arrayClass;
        }
        else {
            assert ci.getElements() instanceof JavacElements;
            final JavacElements elements = (JavacElements) ci.getElements();                    
            return (TypeElement) elements.getTypeElementByBinaryName(signature);
        }
    }
    
    private static boolean isArray (String signature) {
        return signature.length() == 1 && signature.charAt(0) == '[';
    }
    
}
