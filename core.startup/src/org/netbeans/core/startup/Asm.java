/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.core.startup;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * More complex patching code which requires Asm is placed here. It
 * is called by reflection for [@link PatchByteCode}.
 */
final class Asm {
    private static final String DESC_PATCHED_PUBLIC_ANNOTATION = "Lorg/openide/modules/PatchedPublic;";
    private static final String DESC_CTOR_ANNOTATION = "Lorg/openide/modules/ConstructorDelegate;";
    private static final String DESC_DEFAULT_CTOR = "()V";
    private static final String CONSTRUCTOR_NAME = "<init>"; // NOI18N
    
    private Asm() {
    }


    public static byte[] patch(
        byte[] data, String extender, ClassLoader theClassLoader
    ) throws IOException {
        // must analyze the extender class, as some annotations there may trigger
        ClassReader clr = new ClassReader(data);
        ClassWriter wr = new ClassWriter(clr, 0);
        ClassNode theClass = new ClassNode();
        
        clr.accept(theClass, 0);
        
        MethodNode defCtor = null;
        
        String extInternalName = extender.replace(".", "/"); // NOI18N
        
        // patch the superclass
        theClass.superName = extInternalName;
        String resName = extInternalName + ".class"; // NOI18N
        
        try (InputStream istm = theClassLoader.getResourceAsStream(resName)) {
            if (istm == null) {
                throw new IOException("Could not find classfile for extender class"); // NOI18N
            }
            ClassReader extenderReader = new ClassReader(istm);
            ClassNode extenderClass = new ClassNode();
            extenderReader.accept(extenderClass, ClassReader.SKIP_FRAMES);
            
            // search for a no-arg ctor, replace all invokespecial calls in ctors
            for (MethodNode m : (Collection<MethodNode>)theClass.methods) {
                if (CONSTRUCTOR_NAME.equals(m.name)) {
                    if (DESC_DEFAULT_CTOR.equals(m.desc)) { // NOI18N
                        defCtor = m;
                    }
                    replaceSuperCtorCalls(theClass, extenderClass, m);
                }
            }
            for (Object o : extenderClass.methods) {
                MethodNode mn = (MethodNode)o;

                if (mn.invisibleAnnotations != null && (mn.access & Opcodes.ACC_STATIC) > 0) {
                    // constructor, possibly annotated
                    for (AnnotationNode an : (Collection<AnnotationNode>)mn.invisibleAnnotations) {
                        if (DESC_CTOR_ANNOTATION.equals(an.desc)) {
                            delegateToFactory(an, extenderClass, mn, theClass, defCtor);
                            break;
                        }
                    }
                }
            }
            
            for (MethodNode mn : (Collection<MethodNode>)theClass.methods) {
                if (mn.invisibleAnnotations == null) {
                    continue;
                }
                for (AnnotationNode an : (Collection<AnnotationNode>)mn.invisibleAnnotations) {
                    if (DESC_PATCHED_PUBLIC_ANNOTATION.equals(an.desc)) {
                        mn.access = (mn.access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED)) | Opcodes.ACC_PUBLIC;
                        break;
                    }
                }
            }
        }
        
        theClass.accept(wr);
        byte[] result = wr.toByteArray();
        return result;
    }

    /**
     * Replaces class references in super constructor invocations.
     * Must not replace references in this() constructor invocations.
     * 
     * @param theClass the class being patched
     * @param extenderClass the injected superclass
     * @param mn method to process
     */
    private static void replaceSuperCtorCalls(final ClassNode theClass, final ClassNode extenderClass, MethodNode mn) {
        for (Iterator it = mn.instructions.iterator(); it.hasNext(); ) {
            AbstractInsnNode aIns = (AbstractInsnNode)it.next();
            if (aIns.getOpcode() == Opcodes.INVOKESPECIAL) {
                MethodInsnNode mins = (MethodInsnNode)aIns;
                if (CONSTRUCTOR_NAME.equals(mins.name) && mins.owner.equals(extenderClass.superName)) {
                    // replace with the extender class name
                    mins.owner = extenderClass.name;
                }
                break;
            }
        }
    }
    
    /**
     * No-op singature visitor
     */
    private static class NullSignVisitor extends SignatureVisitor {
        public NullSignVisitor() {
            super(Opcodes.ASM5);
        }
    }
    
    /**
     * Pushes parameters with correct opcodes that correspond to the
     * method's signature. Assumes that the first parameter is the
     * object's class itself.
     */
    private static class CallParametersWriter extends SignatureVisitor {
        private final MethodNode mn;
        private int localSize;
        private int[] paramIndices;
        int [] out = new int[10];
        private int cnt;
        
        /**
         * Adds opcodes to the method's code
         * 
         * @param mn method to generate
         * @param firstSelf if true, assumes the first parameter is reference to self and will generate aload_0
         */
        public CallParametersWriter(MethodNode mn, boolean firstSelf) {
            super(Opcodes.ASM5);
            this.mn = mn;
            this.paramIndex = firstSelf ? 0 : 1;
        }
        
        public CallParametersWriter(MethodNode mn, int[] indices) {
            super(Opcodes.ASM5);
            this.mn = mn;
            this.paramIndices = indices;
        }
        
        private int paramIndex = 0;
        
        void storeLoads() {
            for (int i : paramIndices) {
                mn.visitVarInsn(out[i * 2], out[i * 2 + 1]);
            }
        }
        
        private void load(int opcode, int paramIndex) {
            if (paramIndices == null) {
                mn.visitVarInsn(opcode, paramIndex);
            } else {
                if (out.length <= paramIndex + 1) {
                    out = Arrays.copyOf(out, out.length * 2);
                }
                out[cnt * 2]  = opcode;
                out[cnt * 2 + 1] = paramIndex;
            }
            cnt++;
        }

        @Override
        public void visitEnd() {
            // end of classtype
            load(Opcodes.ALOAD, paramIndex++);
            localSize++;
        }

        @Override
        public void visitBaseType(char c) {
            int idx = paramIndex++;
            int opcode;

            switch (c) {
                // two-word data
                case 'J': opcode = Opcodes.LLOAD; paramIndex++; localSize++; break;
                case 'D': opcode = Opcodes.DLOAD; paramIndex++; localSize++; break;
                // float has a special opcode
                case 'F': opcode = Opcodes.FLOAD; break;
                default: opcode = Opcodes.ILOAD; break;

            }
            load(opcode, idx);
            localSize++;
        }

        @Override
        public SignatureVisitor visitTypeArgument(char c) {
            return new NullSignVisitor();
        }

        @Override
        public void visitTypeArgument() {}

        @Override
        public void visitInnerClassType(String string) {}

        @Override
        public void visitClassType(String string) {}

        @Override
        public SignatureVisitor visitArrayType() {
            load(Opcodes.ALOAD, paramIndex++);
            localSize++;
            return new NullSignVisitor();
        }

        @Override
        public void visitTypeVariable(String string) {}

        @Override
        public SignatureVisitor visitExceptionType() {
            return new NullSignVisitor();
        }

        @Override
        public SignatureVisitor visitReturnType() {
            return new NullSignVisitor();
        }

        @Override
        public SignatureVisitor visitParameterType() {
            return this;
        }

        @Override
        public SignatureVisitor visitInterface() {
            return null;
        }

        @Override
        public SignatureVisitor visitSuperclass() {
            return null;
        }

        @Override
        public SignatureVisitor visitInterfaceBound() {
            return new NullSignVisitor();
        }

        @Override
        public SignatureVisitor visitClassBound() {
            return new NullSignVisitor();
        }

        @Override
        public void visitFormalTypeParameter(String string) {
            super.visitFormalTypeParameter(string); //To change body of generated methods, choose Tools | Templates.
        }

    }
    
    private static class CtorDelVisitor extends AnnotationVisitor {
        int[]   indices;
       
        public CtorDelVisitor(int i) {
            super(i);
        }

        @Override
        public void visit(String string, Object o) {
            if ("delegateParams".equals(string)) {  // NOI18N
                indices = (int[])o;
            }
            super.visit(string, o);
        }
    }
    
    private static String[] splitDescriptor(String desc) {
        List<String> arr = new ArrayList<>();
        int lastPos = 0;
        F: for (int i = 0; i < desc.length(); i++) {
            char c = desc.charAt(i);
            switch (c) {
                case '(':
                    lastPos = i+1;
                    break;
                case ')':
                    break F;
                case 'B': case 'C': case 'D': case 'F': case 'I': case 'J':
                case 'S': case 'Z':
                    arr.add(desc.substring(lastPos, i + 1));
                    lastPos = i + 1;
                    break;
                    
                case '[':
                    break;
                    
                case 'L':
                    i = desc.indexOf(';', i);
                    arr.add(desc.substring(lastPos, i + 1));
                    lastPos = i + 1;
                    break;
            }
        }
        return arr.toArray(new String[arr.size()]);
    }
    
    private static void delegateToFactory(
        AnnotationNode an, ClassNode targetClass, MethodNode targetMethod, ClassNode clazz,
        MethodNode noArgCtor
    ) {
        String desc = targetMethod.desc;
        CtorDelVisitor v = new CtorDelVisitor(Opcodes.ASM5);
        an.accept(v);
        int nextPos = desc.indexOf(';', 2); // NOI18N
        desc = "(" + desc.substring(nextPos + 1); // NOI18N
        MethodNode mn = new MethodNode(Opcodes.ASM5, 
                targetMethod.access & (~Opcodes.ACC_STATIC), CONSTRUCTOR_NAME,
                desc,
                targetMethod.signature,
                (String[])targetMethod.exceptions.toArray(new String[targetMethod.exceptions.size()]));

        mn.visibleAnnotations = targetMethod.visibleAnnotations;
        mn.visibleParameterAnnotations = targetMethod.visibleParameterAnnotations;
        mn.parameters = targetMethod.parameters;
        mn.exceptions = targetMethod.exceptions;
        mn.visitCode();
        // this();
        mn.visitVarInsn(Opcodes.ALOAD, 0);
        if (v.indices == null) {
            // assume the first parameter is the class:
            mn.visitMethodInsn(Opcodes.INVOKESPECIAL, 
                    clazz.name, 
                    noArgCtor.name, noArgCtor.desc, false);
        } else {
            String[] paramDescs = splitDescriptor(targetMethod.desc);
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i : v.indices) {
                sb.append(paramDescs[i]);
            }
            sb.append(")V");
            SignatureReader r = new SignatureReader(targetMethod.desc);
            CallParametersWriter callWr = new CallParametersWriter(mn, v.indices);
            r.accept(callWr);
            // generate all the parameter loads:
            for (int i : v.indices) {
                mn.visitVarInsn(callWr.out[i * 2], callWr.out[i * 2 + 1]);
            }
            mn.visitMethodInsn(Opcodes.INVOKESPECIAL, 
                    clazz.name, 
                    "<init>", sb.toString(), false);
        }
        // finally call the static method
        // push parameters
        SignatureReader r = new SignatureReader(targetMethod.desc);
        CallParametersWriter callWr = new CallParametersWriter(mn, true);
        r.accept(callWr);
        mn.visitMethodInsn(Opcodes.INVOKESTATIC, targetClass.name, targetMethod.name, targetMethod.desc, false);
        
        mn.visitInsn(Opcodes.RETURN);
        mn.maxStack = callWr.localSize;
        mn.maxLocals = callWr.localSize;
        
        clazz.methods.add(mn);
    }

    
}
