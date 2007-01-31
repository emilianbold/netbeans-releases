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

import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.jvm.Target;
import com.sun.tools.javac.model.JavacElements;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.usages.ClassFileUtil;

/**
 * Represents a handle for {@link Element} which can be kept and later resolved
 * by another javac. The Javac {@link Element}s are valid only in the single 
 * {@link javax.tools.CompilationTask} or single run of the
 * {@link org.netbeans.api.java.source.CancellableTask}. If the client needs to
 * keep a reference to the {@link Element} and use it in the other CancellableTask 
 * he has to serialize it into the {@link ElementHandle}.
 * <div class="nonnormative"> 
 * <p>
 ** Typical usage of ElementHandle is:
 * </p>
 * <pre>
 * final ElementHandle[] elementHandle = new ElementHandle[1];
 * javaSource.runCompileControlTask(new CancellableTask<CompilationController>() {            
 *     public void run(CompilationController compilationController) {
 *         parameter.toPhase(Phase.RESOLVED);
 *         CompilationUnitTree cu = compilationController.getTree ();
 *         List<? extends Tree> types = getTypeDecls();
 *         Tree tree = getInterestingElementTree (types);
 *         Element element = compilationController.getElement (tree);
 *         elementHanlde[0] = ElementHandle.create (element);
 *    }
 * },priority);
 *
 * otherJavaSource.runCompileControlTask(new CancellableTask<CompilationController>() {            
 *     public void run(CompilationController compilationController) {
 *         parameter.toPhase(Phase.RESOLVED);
 *         Element element = elementHanlde[0].resolve (compilationController);
 *         ....
 *    }
 * },priority);
 * </pre>
 * </div>
 * Currently not all the {@link Element}s are supported by handle, the supported
 * elements are of {@link ElementKind}: {@link ElementKind#PACKAGE}, {@link ElementKind#CLASS},
 * {@link ElementKind#INTERFACE}, {@link ElementKind#ENUM}, {@link ElementKind#METHOD},
 * {@link ElementKind#CONSTRUCTOR}, {@link ElementKind#INSTANCE_INIT}, {@link ElementKind#STATIC_INIT},
 * {@link ElementKind#FIELD}, {@link ElementKind#ENUM_CONSTANT}
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
     * @param element for which the {@link ElementHandle} should be created,
     * not all {@link ElementKind}s are supported, supported {@link ElementKind}s
     * are: {@link ElementKind#PACKAGE}, {@link ElementKind#CLASS},
     * {@link ElementKind#INTERFACE}, {@link ElementKind#ENUM}, {@link ElementKind#METHOD},
     * {@link ElementKind#CONSTRUCTOR}, {@link ElementKind#INSTANCE_INIT}, {@link ElementKind#STATIC_INIT},
     * {@link ElementKind#FIELD}, {@link ElementKind#ENUM_CONSTANT}
     * @return a new {@link ElementHandle}
     * @throws {@link IllegalArgumentException} if the element is of not supported
     * {@link ElementKind}.
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
