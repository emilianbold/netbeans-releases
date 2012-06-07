/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.editor.javadoc;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MemberDoc;
import com.sun.javadoc.MethodDoc;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.openide.util.Exceptions;

/**
 * Represents a reference to java element.
 * E.g. &#123;@link String#charAt(int) } where {@code fqn} is {@code String},
 * {@code member} is {@code charAt}
 *
 * @author Jan Pokorsky
 */
public final class JavaReference {
    CharSequence fqn;
    CharSequence member;
    CharSequence tag;
    List<JavaReference> parameters;
    int begin = -1; // inclusive
    int end = -1; // exclusive
    private int tagEndPosition;
    private String paramsText;

    private JavaReference() {
    }

    @Override
    public String toString() {
        return String.format(
                "fqn: %1$s, member: %2$s, [%3$d, %4$d]", // NOI18N
                fqn, member, begin, end);
    }
    
    /**
     * 
     * @param jdts token sequence to analyze
     * @param offset offset of the first token to resolve
     * @return reference
     */
    public static JavaReference resolve(TokenSequence<JavadocTokenId> jdts, int offset, int tagEndPosition) {
        JavaReference ref = new JavaReference();
        ref.tagEndPosition = tagEndPosition;
        jdts.move(offset);
        ref.insideFQN(jdts);
        return ref;
    }

    public List<JavaReference> getAllReferences () {
        if (parameters == null)
            return Collections.<JavaReference>singletonList (this);
        List<JavaReference> references = new ArrayList<JavaReference> ();
        references.add (this);
        references.addAll (parameters);
        return references;
    }

    public Element getReferencedElement(CompilationInfo javac, TypeElement scope) {
        if (!isReference()) {
            return null;
        }
        Element result = null;
        TypeElement declaredElement = null;
        if (fqn != null && fqn.length() > 0) {
            TypeMirror type = javac.getTreeUtilities().parseType(fqn.toString(), scope);
            if (type != null) {
                switch(type.getKind()) {
                    case DECLARED:
                    case UNION:
                        declaredElement = (TypeElement) ((DeclaredType) type).asElement();
                        result = declaredElement;
                        break;
                    case TYPEVAR:
                        result = ((TypeVariable) type).asElement();
                        break;
                    default:
                        return null;
                }
            } else {
                return null;
            }
        } else {
            declaredElement = scope;
        }

        if (declaredElement != null && member != null && member.length() > 0) {
            String[] paramarr;
            String memName = member.toString();
            MemberDoc referencedMember;
            ClassDoc referencedClass = (ClassDoc) javac.getElementUtilities().javaDocFor(declaredElement);
            
            if (paramsText != null) {
                // has parameter list -- should be method or constructor
                paramarr = new ParameterParseMachine(paramsText).parseParameters();
                if (paramarr != null) {
                    referencedMember = findExecutableMember(memName, paramarr, referencedClass);
                } else {
                    referencedMember = null;
                }
            } else {
                // no parameter list -- should be field
                referencedMember = findExecutableMember(memName, null, referencedClass);
                FieldDoc fd = findField(referencedClass, memName);
                // when no args given, prefer fields over methods
                if (referencedMember == null ||
                        (fd != null && fd.containingClass()
                            .subclassOf(referencedMember.containingClass()))) {
                    referencedMember = fd;
                }
            }
            if (referencedMember != null) {
                result = javac.getElementUtilities().elementFor(referencedMember);
            }
        }
        return result;
    }

    boolean isReference() {
        return begin > 0;
    }

    private void insideMember(TokenSequence<JavadocTokenId> jdts) {
        Token<JavadocTokenId> token;
        if (!jdts.moveNext() || JavadocTokenId.IDENT != (token = jdts.token()).id()) {
            return;
        }
        // member identifier
        member = token.text ();
        end = jdts.offset() + token.length();
        
        // params part (int, String)
        if (!jdts.moveNext ()) return;
        token = jdts.token ();
        if (JavadocTokenId.OTHER_TEXT != token.id ()) return;
        CharSequence cs = token.text ();
        if (cs.length () == 0 ||
            cs.charAt (0) != '(') {
            // no params
            return;
        }

        StringBuilder params = new StringBuilder();
        while (jdts.offset() < tagEndPosition) {
            int len = tagEndPosition - jdts.offset();
            cs = len > 0
                    ? token.text()
                    : token.text().subSequence(0, len);
            if (token.id () == JavadocTokenId.IDENT) {
                JavaReference parameter = JavaReference.resolve (
                    jdts,
                    jdts.offset(),
                    jdts.offset() + len
                );
                if (parameters == null)
                    parameters = new ArrayList<JavaReference> ();
                parameters.add (parameter);
                if (parameter.fqn != null) {
                    params.append(parameter.fqn);
                } else {
                    params.append(cs);
                }
            } else {
                params.append(cs);
            }
            if (params.indexOf (")") > 0)
                break;
            if (!jdts.moveNext()) {
                break;
            }
            token = jdts.token();
        }
        paramsText = parseParamString(params);
    }

    private void insideFQN (
        TokenSequence<JavadocTokenId> tokenSequence
    ) {
        StringBuilder sb = new StringBuilder ();
        STOP: while (tokenSequence.moveNext ()) {
            Token<JavadocTokenId> token = tokenSequence.token();
            switch(token.id()) {
                case IDENT:
                    sb.append(token.text());
                    if (begin < 0) {
                        begin = tokenSequence.offset();
                    }
                    end = tokenSequence.offset() + token.length();
                    break;
                case HASH:
                    if (begin < 0) {
                        begin = tokenSequence.offset();
                    }
                    end = tokenSequence.offset() + token.length();
                    insideMember(tokenSequence);
                    break STOP;
                case DOT:
                    if (sb.length() == 0 || '.' == sb.charAt(sb.length() - 1)) {
                        break STOP;
                    }
                    sb.append('.');
                    end = tokenSequence.offset() + token.length();
                    break;
                default:
                    tokenSequence.movePrevious ();
                    break STOP;
            }
        }
        if (sb.length() > 0) {
            fqn = sb;
        }
    }
    
    private String parseParamString(CharSequence text) {
        int len = text.length();
        if (len == 0 || text.charAt(0) != '(') {
            return null;
        }

        // check that the text is param list with possible parentheses

        // the code assumes that there is no initial white space.
        int parens = 0;
        int commentstart = 0;
        int start = 0;
        int cp;
        for (int i = start; i < len ; i += Character.charCount(cp)) {
            cp = Character.codePointAt(text, i);
            switch (cp) {
                case '(': parens++; break;
                case ')': parens--; break;
                case '[': case ']': case '.': case '#': break;
                case ',':
                    if (parens <= 0) {
//                        docenv().warning(holder,
//                                         "tag.see.malformed_see_tag",
//                                         name, text);
                        return null;
                    }
                    break;
                case ' ': case '\t': case '\n':
                    if (parens == 0) { //here onwards the comment starts.
                        commentstart = i;
                        i = len;
                    }
                    break;
                default:
//                    if (!Character.isJavaIdentifierPart(cp)) {
//                        docenv().warning(holder,
//                                         "tag.see.illegal_character",
//                                         name, ""+cp, text);
//                    }
                    break;
            }
        }
        if (parens != 0) {
//            docenv().warning(holder,
//                             "tag.see.malformed_see_tag",
//                             name, text);
            return null;
        }

        String params;

        if (commentstart > 0) {
            params = text.subSequence(start, commentstart).toString();
        } else {
            params = text.toString();
        }
        return params;
    }
    
    // separate "int, String" from "(int, String)"
    // (int i, String s) ==> [0] = "int",  [1] = String
    // (int[][], String[]) ==> [0] = "int[][]" // [1] = "String[]"
    private static final class ParameterParseMachine {

        final int START = 0;
        final int TYPE = 1;
        final int NAME = 2;
        final int TNSPACE = 3;  // space between type and name

        final int ARRAYDECORATION = 4;
        final int ARRAYSPACE = 5;
        String parameters;
        StringBuilder typeId;
        List<String> paramList;

        ParameterParseMachine(String parameters) {
            this.parameters = parameters;
            this.paramList = new ArrayList<String>();
            typeId = new StringBuilder();
        }

        public String[] parseParameters() {
            if (parameters.equals("()")) { // NOI18N

                return new String[0];
            }   // now strip off '(' and ')'

            int state = START;
            int prevstate = START;
            parameters = parameters.substring(1, parameters.length() - 1);
            int cp;
            for (int index = 0; index < parameters.length(); index += Character.charCount(cp)) {
                cp = parameters.codePointAt(index);
                switch (state) {
                    case START:
                        if (Character.isJavaIdentifierStart(cp)) {
                            typeId.append(Character.toChars(cp));
                            state = TYPE;
                        }
                        prevstate = START;
                        break;
                    case TYPE:
                        if (Character.isJavaIdentifierPart(cp) || cp == '.') {
                            typeId.append(Character.toChars(cp));
                        } else if (cp == '[') {
                            typeId.append('[');
                            state = ARRAYDECORATION;
                        } else if (Character.isWhitespace(cp)) {
                            state = TNSPACE;
                        } else if (cp == ',') {  // no name, just type

                            addTypeToParamList();
                            state = START;
                        }
                        prevstate = TYPE;
                        break;
                    case TNSPACE:
                        if (Character.isJavaIdentifierStart(cp)) { // name

                            if (prevstate == ARRAYDECORATION) {
                                // missing comma space
                                return (String[]) null;
                            }
                            addTypeToParamList();
                            state = NAME;
                        } else if (cp == '[') {
                            typeId.append('[');
                            state = ARRAYDECORATION;
                        } else if (cp == ',') {   // just the type

                            addTypeToParamList();
                            state = START;
                        } // consume rest all

                        prevstate = TNSPACE;
                        break;
                    case ARRAYDECORATION:
                        if (cp == ']') {
                            typeId.append(']');
                            state = TNSPACE;
                        } else if (!Character.isWhitespace(cp)) {
                            // illegal char in arr dim
                            return (String[]) null;
                        }
                        prevstate = ARRAYDECORATION;
                        break;
                    case NAME:
                        if (cp == ',') {  // just consume everything till ','

                            state = START;
                        }
                        prevstate = NAME;
                        break;
                }
            }
            if (state == ARRAYDECORATION ||
                    (state == START && prevstate == TNSPACE)) {
                // illegal see tag
            }
            if (typeId.length() > 0) {
                paramList.add(typeId.toString());
            }
            return paramList.toArray(new String[paramList.size()]);
        }

        void addTypeToParamList() {
            if (typeId.length() > 0) {
                paramList.add(typeId.toString());
                typeId.setLength(0);
            }
        }
    }

    private MemberDoc findReferencedMethod(String memName, String[] paramarr,
                                           ClassDoc referencedClass) {
        MemberDoc meth = findExecutableMember(memName, paramarr, referencedClass);
        ClassDoc[] nestedclasses = referencedClass.innerClasses();
        if (meth == null) {
            for (int i = 0; i < nestedclasses.length; i++) {
                meth = findReferencedMethod(memName, paramarr, nestedclasses[i]);
                if (meth != null) {
                    return meth;
                }
            }
        }
        return null;
    }

    private MemberDoc findExecutableMember(String memName, String[] paramarr,
                                           ClassDoc referencedClass) {
        if (memName.equals(referencedClass.name())) {
            return findConstructor(referencedClass, memName, paramarr);
        } else {   // it's a method.
            return findMethod(referencedClass, memName, paramarr);
        }
    }
    
    // Here starts dark side. It is necessary to introduce API for following methods
    // since they come from ClassDocImpl
    
    private static MethodDoc findMethod(ClassDoc clazz, String methodName, String[] paramTypes) {
        try {
            Method findMethod = clazz.getClass().getMethod(
                    "findMethod", String.class, String[].class); // NOI18N
            Object result = findMethod.invoke(clazz, methodName, paramTypes);
            return result instanceof MethodDoc ? (MethodDoc) result : null;
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    private static ConstructorDoc findConstructor(ClassDoc clazz, String methodName, String[] paramTypes) {
        try {
            Method findConstructor = clazz.getClass().getMethod(
                    "findConstructor", String.class, String[].class); // NOI18N
            Object result = findConstructor.invoke(clazz, methodName, paramTypes);
            return result instanceof ConstructorDoc ? (ConstructorDoc) result : null;
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    private FieldDoc findField(ClassDoc clazz, String fieldName) {
        try {
            Method findField = clazz.getClass().getMethod(
                    "findField", String.class); // NOI18N
            Object result = findField.invoke(clazz, fieldName);
            return result instanceof FieldDoc ? (FieldDoc) result : null;
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

}
