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
package org.netbeans.modules.java.source.usages;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.CompletionFailure;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.TransTypes;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.JCDiagnostic.SimpleDiagnosticPosition;
import com.sun.tools.javac.util.ListBuffer;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.SimpleTypeVisitor6;

/**TODO: the format below is obsolete:
 *  <pre>
 *     methodSignature ::= 'E' flags methodtype '@' attributes
 *     fieldSignature  ::= 'A' flags type realname '@' attributes
 *     classSignature  ::= 'G' flags [ typeparams ] realname supertype { interfacetype } '@' attributes
 *     type       ::= ... | classtype | methodtype | typevar | error
 *     classtype  ::= classsig { '.' classsig }
 *     classig    ::= 'L' name [typeargs] ';'
 *     methodtype ::= [ typeparams ] realname '(' { flags type realname } ')' '(' { /throws/type } ')' type
 *     typevar    ::= 'T' name ';'
 *     typeargs   ::= '<' type { type } '>'
 *     typeparams ::= '<' typeparam { typeparam } '>'
 *     typeparam  ::= name ':' type
 *     realname   ::= 'N' name ';'
 *     error      ::= 'R' name ;
 *
 *     flags      ::= 'M' modifiers specification ';'
 *     inner-class ::= 'O' /inner-class/realname
 *  </pre>
 *
 * @author Jan Lahoda
 */
public class SymbolDumper extends SimpleTypeVisitor6<Void, Boolean> {
    
    private PrintWriter output;
    private Types types;
    private boolean hasErrors;
    
    /** Creates a new instance of SymbolDumper */
    public SymbolDumper(PrintWriter output, Types types) {
        this.output = output;
        this.types = types;
    }

    public Void visitPrimitive(PrimitiveType t, Boolean p) {
        switch (t.getKind()) {
            case BOOLEAN:
                output.append('Z'); // NOI18N
                break;
            case BYTE:
                output.append('B'); // NOI18N
                break;
            case SHORT:
                output.append('S'); // NOI18N
                break;
            case INT:
                output.append('I'); // NOI18N
                break;
            case LONG:
                output.append('J'); // NOI18N
                break;
            case CHAR:
                output.append('C'); // NOI18N
                break;
            case FLOAT:
                output.append('F'); // NOI18N
                break;
            case DOUBLE:
                output.append('D'); // NOI18N
                break;
            default:
                throw new IllegalArgumentException("Should not happend. Or can it?");
        }
        return null;
    }

    public Void visitNoType(NoType t, Boolean p) {
        switch (t.getKind()) {
            case VOID:
                output.append('V');
                break;
            case PACKAGE:
                new Exception("what should be printed here?").printStackTrace();
                break;                
        }
        return null;
    }

    public Void visitNull(NullType t, Boolean p) {
        new Exception("what should be printed here?").printStackTrace();
        return null;
    }

    public Void visitArray(ArrayType t, Boolean p) {
        output.append('['); // NOI18N
        visit(t.getComponentType());
        return null;
    }

    public Void visitDeclared(DeclaredType t, Boolean p) {
        if (t.getEnclosingType() != null && t.getEnclosingType().getKind() == TypeKind.DECLARED) {
            visit(t.getEnclosingType(), Boolean.TRUE);
            output.append('$');
            output.append(t.asElement().getSimpleName());
        } else {
            output.append('L');
            output.append(ClassFileUtil.encodeClassName((TypeElement) t.asElement()));
        }
        List<? extends TypeMirror> actualTypeParameters = t.getTypeArguments();
        
        if (!actualTypeParameters.isEmpty()) {
            output.append('<');
            for (TypeMirror param : actualTypeParameters) {
                visit(param);
            }
            output.append('>');
        }
        if (p != Boolean.TRUE)
            output.append(';');
        return null;
    }

    public Void visitError(ErrorType t, Boolean p) {
        TypeElement te = (TypeElement) t.asElement();
        
        output.append('R');
        
        if (te != null) {
            output.append(te.getSimpleName().toString());
        }
        
        output.append(';');
        
        hasErrors = true;
        
        return null;
    }

    public Void visitTypeVariable(TypeVariable t, Boolean p) {
        if (p == Boolean.TRUE) {
            output.append(t.asElement().getSimpleName().toString());
            output.append(':');
            assert t.getLowerBound().getKind() == TypeKind.NULL : "currently not handled!" ;
            Type boundImpl = ((Type) t.getUpperBound());
            
            if (boundImpl.isCompound()) {
                if (boundImpl.getKind() == TypeKind.EXECUTABLE || boundImpl.getKind() == TypeKind.PACKAGE)
                    throw new IllegalArgumentException(boundImpl.toString());
                Type sup = types.supertype(boundImpl);
                visit((sup == Type.noType || sup == boundImpl || sup == null)
                ? types.interfaces(boundImpl)
                : types.interfaces(boundImpl).prepend(sup));
            } else {
                visit(t.getUpperBound());
            }
            
            output.append(';');
        } else {
            output.append('Q');
            output.append(t.asElement().getSimpleName().toString());
            output.append(';');
        }
        
        return null;
    }

    public Void visitWildcard(WildcardType t, Boolean p) {
        boolean wasSomething = false;
        
        if (t.getExtendsBound() != null) {
            output.append("+");
            visit(t.getExtendsBound());
            wasSomething = true;
        }
        if (t.getSuperBound() != null) {
            output.append("-");
            visit(t.getSuperBound());
            wasSomething = true;
        }
        
        if (!wasSomething) {
            output.append('?');
        }
        return null;
    }

    public Void visitExecutable(ExecutableType t, Boolean p) {
        throw new IllegalStateException("This cannot be handled correctly, and should hopefully never happen...");
    }

    public Void visitUnknown(TypeMirror t, Boolean p) {
        new Exception("what should be printed here?").printStackTrace();
        return null;
    }

    public void visit(List<? extends TypeMirror> l) {
        for (TypeMirror t : l) {
            visit(t);
        }
    }
    
    public void visit(List<? extends TypeMirror> l, Boolean p) {
        for (TypeMirror t : l) {
            visit(t, p);
        }
    }
    
    public static boolean dump(PrintWriter output, Types types, TransTypes trans, TypeElement type, Element enclosingElement) {
        boolean result = SymbolDumper.dumpImpl(output, types, type, enclosingElement);
        
        output.append('\n');
        Symbol.ClassSymbol sym = (Symbol.ClassSymbol)type;        
        com.sun.tools.javac.util.List<Symbol> list = com.sun.tools.javac.util.List.nil();

        for (Scope.Entry e = sym.members().elems; e != null; e = e.sibling) {
            try {
                if (e.sym.getKind().isClass() || e.sym.getKind().isInterface()) {
                    //ignore innerclasses:
                    continue;
                }
                
                if (e.sym != null && ((e.sym.flags() & Flags.SYNTHETIC) == 0 || (e.sym.flags() & Flags.BRIDGE) != 0) && e.sym.owner == sym) {
                    list = list.prepend(e.sym);
                }
            } catch (CompletionFailure cf) {}
        }

        
        for (Symbol symToDump : list) {
            if ((symToDump.flags() & Flags.BRIDGE) != 0) {
                symToDump.flags_field |= Flags.SYNTHETIC;
            }
            result |= SymbolDumper.dumpImpl(output, types, symToDump);
        }
        
        ListBuffer<JCTree> bridges = new ListBuffer<JCTree>();
        DiagnosticPosition pos = new SimpleDiagnosticPosition(-1);
        trans.getBridges(pos, sym, bridges);
        
        for (JCTree bridge : bridges) {
            if (bridge.getKind() == Tree.Kind.METHOD) {
                assert ((JCTree.JCMethodDecl)bridge).sym != null;
                result |= SymbolDumper.dumpImpl(output, types, (Element)(((JCTree.JCMethodDecl)bridge).sym));
            }
        }
        
        output.append('W');
        
        SymbolDumper d = new SymbolDumper(output, types);
        
        dumpAnnotations(d, type);
        
        return result | d.hasErrors;
    }
    
    private static boolean dumpImpl(PrintWriter output, Types types, TypeElement type, Element enclosingElement) {
        SymbolDumper d = new SymbolDumper(output, types);
        
        output.append('G');
        dumpFlags(output, ((Symbol) type).flags_field & ~ (Flags.FROMCLASS|Flags.UNATTRIBUTED|Flags.ACYCLIC));
        List<? extends TypeParameterElement>  params = type.getTypeParameters();
        
        if (!params.isEmpty()) {
            output.append('<');
            for (TypeParameterElement e : params) {
                d.visit(e.asType(), Boolean.TRUE);
            }
            output.append('>');
        }
        dumpName(output, ClassFileUtil.encodeClassName(type));
        dumpEnclosingElement(d, enclosingElement);
        
        TypeMirror tm = type.getSuperclass();
        
        if (tm.getKind() != TypeKind.NONE) {
            d.visit(tm);
        } else {
            //tm.getKind() == TypeKind.NONE also for interfaces, but interface's supertype is j.l.Object, so dump it as such:
            if (!"java.lang.Object".equals(type.getQualifiedName().toString())) {
                output.append("Ljava.lang.Object;");
            } else {
                output.append(";");
            }
        }
        
        d.visit(type.getInterfaces());
        output.append(';');
        
        dumpInnerclasses(output, ElementFilter.typesIn(type.getEnclosedElements()));
        
        return d.hasErrors;
    }
    
    private static void dumpEnclosingElement(SymbolDumper dumper, Element enclosingElement) {
        if (enclosingElement != null) {
            if (enclosingElement.getKind().isClass() || enclosingElement.getKind().isInterface()) {
                dumpName(dumper.output, ClassFileUtil.encodeClassName((TypeElement)enclosingElement));
            } else {
                dumpName(dumper.output, ClassFileUtil.encodeClassName((TypeElement)enclosingElement.getEnclosingElement()));
                ExecutableElement enclosingMethod = (ExecutableElement)enclosingElement;
                dumpName(dumper.output, enclosingMethod.getSimpleName());
                ExecutableType enclosingMethodType = (ExecutableType)((Symbol)enclosingMethod).externalType(dumper.types);
                dumper.output.append('(');
                dumper.visit(enclosingMethodType.getParameterTypes());
                dumper.output.append(')');
                dumper.output.append('(');
                dumper.visit(enclosingMethodType.getThrownTypes());
                dumper.output.append(')');
                dumper.visit(enclosingMethodType.getReturnType());
            }
        }        
        dumper.output.append(';');
    }
    
    private static void dumpInnerclasses(PrintWriter output, List<TypeElement> innerClasses) {
        for (TypeElement innerClass : innerClasses) {
            dumpName(output, innerClass.getSimpleName());
            dumpFlags(output, ((Symbol)innerClass).flags_field & ~ (Flags.FROMCLASS|Flags.UNATTRIBUTED|Flags.ACYCLIC));
        }
        output.append(';');
    }
    
    private static boolean dumpImpl(PrintWriter output, Types types, Element el) {
        if (el.getKind().isField()) {
            boolean result = dumpImpl(output, types, (VariableElement) el);
            output.append('\n');
            return result;
        }
        if (el.getKind() == ElementKind.METHOD || el.getKind() == ElementKind.CONSTRUCTOR) {
            boolean result = dumpImpl(output, types, (ExecutableElement) el);
            output.append('\n');
            return result;
        }
        
        Logger.getLogger(SymbolDumper.class.getName()).info("Unhandled ElementKind: " + el.getKind());
        
        return false;
    }
    
    private static boolean dumpImpl(PrintWriter output, Types types, VariableElement variable) {
        SymbolDumper d = new SymbolDumper(output, types);
        
        output.append('A');
        dumpFlags(output, ((Symbol) variable).flags_field);
        d.visit(variable.asType());
        dumpName(output, variable.getSimpleName());

        if (variable.getModifiers().contains(Modifier.STATIC) && variable.getConstantValue() != null) {
            switch (variable.asType().getKind()) {
            case BOOLEAN:
                output.append('Z'); // NOI18N
                output.append(String.valueOf(variable.getConstantValue()));
                break;
            case BYTE:
                output.append('B'); // NOI18N
                output.append(String.valueOf(variable.getConstantValue()));
                break;
            case SHORT:
                output.append('S'); // NOI18N
                output.append(String.valueOf(variable.getConstantValue()));
                break;
            case INT:
                output.append('I'); // NOI18N
                output.append(String.valueOf(variable.getConstantValue()));
                break;
            case LONG:
                output.append('J'); // NOI18N
                output.append(String.valueOf(variable.getConstantValue()));
                break;
            case CHAR:
                output.append('C'); // NOI18N
                appendEscapedString(output, String.valueOf(variable.getConstantValue()));
                break;
            case FLOAT:
                output.append('F'); // NOI18N
                output.append(String.valueOf(variable.getConstantValue()));
                break;
            case DOUBLE:
                output.append('D'); // NOI18N
                output.append(String.valueOf(variable.getConstantValue()));
                break;
            case DECLARED:
                output.append('L');
                TypeMirror varType = variable.asType();
                if (varType.getKind() == TypeKind.DECLARED && "java.lang.String".equals(((TypeElement)((DeclaredType)varType).asElement()).getQualifiedName().toString())) {
                    appendEscapedString(output, String.valueOf(variable.getConstantValue()));
                }
                break;
                default:
                    output.append("X");
            }
        } else {
            output.append("X");
        }
        
        output.append(";");
        
        dumpAnnotations(d, variable);
        
        return d.hasErrors;
    }
    
    private static boolean dumpImpl(PrintWriter output, Types types, ExecutableElement executable) {
        SymbolDumper d = new SymbolDumper(output, types);
        ExecutableType type = (ExecutableType) executable.asType();
        
        output.append('E');
        dumpFlags(output, ((Symbol) executable).flags_field);
        dumpTypeVariables(d, type.getTypeVariables());
        dumpName(output, executable.getSimpleName());
        output.append('(');
        List<? extends TypeMirror> paramTypes = type.getParameterTypes();
        List<? extends VariableElement> paramElems = executable.getParameters();
        
        assert paramElems.size() == paramTypes.size();
        
        for (int cntr = 0; cntr < paramTypes.size(); cntr++) {
            dumpFlags(output, ((Symbol) paramElems.get(cntr)).flags_field);
            d.visit(paramTypes.get(cntr), Boolean.FALSE);
            dumpName(output, paramElems.get(cntr).getSimpleName());
        }
        output.append(')');
        
        output.append('(');
        
        for (TypeMirror t : executable.getThrownTypes()) {
            d.visit(t);
        }
        
        output.append(')');
        
        d.visit(type.getReturnType());
        
        dumpAnnotations(d, executable);
        
        //dump default value (for annotation's attributes):
        AnnotationValue value = executable.getDefaultValue();
        
        if (value != null) {
            new AnnotationValueVisitorImpl().visit(value, d);
        } else {
            output.append(';');
        }
        
        return d.hasErrors;
    }
    
    private static void appendEscapedString(PrintWriter output, String value) {
        value = value.replaceAll("\\\\", "\\\\\\\\");
        value = value.replaceAll("@", "\\\\a");
        value = value.replaceAll(";", "\\\\b");
        
        StringBuffer result = new StringBuffer();
        
        for (int cntr = 0; cntr < value.length(); cntr++) {
            char c = value.charAt(cntr);
            
            if (c < 32) {
                result.append('\\');
                
                String v = Integer.toHexString(c);
                
                result.append("0000".substring(0, 4 - v.length()));
                result.append(v);
            } else {
                result.append(c);
            }
        }
        
        output.append(result.toString());
    }
    
    private static void dumpName(PrintWriter output, CharSequence name) {
        output.append('N');
        output.append(name.toString());
        output.append(';');
    }
    
    private static void dumpTypeVariables(SymbolDumper dumper, List<? extends TypeVariable> params) {
        if (params.isEmpty())
            return ;
        
        dumper.output.append('<');
        
        dumper.visit(params, Boolean.TRUE);
        
        dumper.output.append('>');
    }
    
    private static void dumpFlags(PrintWriter output, long flags) {
        output.append('M');
        output.append(Long.toHexString(flags));
        output.append(';');
    }
    
    private static void dumpAnnotations(SymbolDumper d, Element e) {
        for (AnnotationMirror m : e.getAnnotationMirrors()) {
            dumpAnnotation(d, m);
        }
        
        d.output.append(';');
    }
    
    private static void dumpAnnotation(SymbolDumper d, AnnotationMirror m) {
        d.visit(m.getAnnotationType());
        
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : m.getElementValues().entrySet()) {
            dumpName(d.output, entry.getKey().getSimpleName().toString());
            new AnnotationValueVisitorImpl().visit(entry.getValue(), d);
        }
        
        d.output.append(';');
    }
    
    private static class AnnotationValueVisitorImpl extends SimpleAnnotationValueVisitor6<Void, SymbolDumper> {
        
        @Override
        public Void visitBoolean(boolean b, SymbolDumper d) {
            d.output.append('Z'); // NOI18N
            d.output.append(String.valueOf(b));
            d.output.append(';');
            
            return null;
        }

        @Override
        public Void visitByte(byte b, SymbolDumper d) {
            d.output.append('B'); // NOI18N
            d.output.append(String.valueOf(b));
            d.output.append(';');
            
            return null;
        }

        @Override
        public Void visitChar(char c, SymbolDumper d) {
            d.output.append('C'); // NOI18N
            appendEscapedString(d.output, String.valueOf(c));
            d.output.append(';');
            
            return null;
        }

        @Override
        public Void visitDouble(double v, SymbolDumper d) {
            d.output.append('D'); // NOI18N
            d.output.append(String.valueOf(v));
            d.output.append(';');
            
            return null;
        }

        @Override
        public Void visitFloat(float f, SymbolDumper d) {
            d.output.append('F'); // NOI18N
            d.output.append(String.valueOf(f));
            d.output.append(';');
            
            return null;
        }

        @Override
        public Void visitInt(int i, SymbolDumper d) {
            d.output.append('I'); // NOI18N
            d.output.append(String.valueOf(i));
            d.output.append(';');
            
            return null;
        }

        @Override
        public Void visitLong(long i, SymbolDumper d) {
            d.output.append('J'); // NOI18N
            d.output.append(String.valueOf(i));
            d.output.append(';');
            
            return null;
        }

        @Override
        public Void visitShort(short s, SymbolDumper d) {
            d.output.append('S'); // NOI18N
            d.output.append(String.valueOf(s));
            d.output.append(';');
            
            return null;
        }

        @Override
        public Void visitString(String s, SymbolDumper d) {
            d.output.append('L');
            appendEscapedString(d.output, String.valueOf(s));
            d.output.append(';');
            
            return null;
        }

        @Override
        public Void visitType(TypeMirror t, SymbolDumper p) {
            p.output.append('Y');
            p.visit(t);
            
            return null;
        }

        @Override
        public Void visitEnumConstant(VariableElement c, SymbolDumper d) {
            d.output.append('O');
            d.visit(c.getEnclosingElement().asType());
            dumpName(d.output, c.getSimpleName());
            
            return null;
        }

        @Override
        public Void visitAnnotation(AnnotationMirror a, SymbolDumper d) {
            d.output.append('P');
            dumpAnnotation(d, a);
            
            return null;
        }

        @Override
        public Void visitArray(List<? extends AnnotationValue> vals, SymbolDumper d) {
            d.output.append('[');
            for (AnnotationValue v : vals) {
                visit(v, d);
            }
            d.output.append(';');
            
            return null;
        }

        @Override
        public Void visitUnknown(AnnotationValue av, SymbolDumper p) {
            throw new UnsupportedOperationException("SymbolDumper should be fixed to incorporate unknown value: " + av);
        }
        
    }
    
}
